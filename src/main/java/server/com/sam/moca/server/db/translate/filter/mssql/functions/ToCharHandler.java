/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 */

package com.sam.moca.server.db.translate.filter.mssql.functions;

import java.util.ArrayList;
import java.util.List;

import com.sam.moca.server.db.BindList;
import com.sam.moca.server.db.translate.ReplacementElement;
import com.sam.moca.server.db.translate.SQLElement;
import com.sam.moca.server.db.translate.TokenType;
import com.sam.moca.server.db.translate.TranslationException;
import com.sam.moca.server.db.translate.filter.functions.FunctionHandler;

/**
 * A function handler designed to deal with the Oracle TO_CHAR function, and
 * replace it with a call to our own TP_TO_CHAR function.
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ToCharHandler implements FunctionHandler {
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs)
            throws TranslationException{
        int argCount = args.size();
        if (argCount != 1 && argCount != 2) {
            throw new TranslationException(name + " requires 1 or 2 arguments, received " + argCount);
        }
        
        List<SQLElement> out = new ArrayList<SQLElement>();
        String format = null;
        if (argCount == 2) {
            List<SQLElement> arg2 = args.get(1);
            if (arg2.size() == 1) {
                SQLElement argElement = arg2.get(0);
                if (argElement.getType() == TokenType.STRING_LITERAL) {
                    format = argElement.getValue();
                    format = format.substring(1, format.length() - 1);
                }
                else if (argElement.getType() == TokenType.BIND_VARIABLE) {
                    String bindVar = argElement.getValue().substring(1);
                    Object bindValue = bindArgs.getValue(bindVar);
                    if (bindValue != null && bindValue instanceof String) {
                        format = (String)bindValue;
                    }
                }
                else if (argElement.getType() == TokenType.NSTRING_LITERAL) {
                    format = argElement.getValue();
                    format = format.substring(2, format.length() - 1);
                }
            }
            
            if (format == null) {
                throw new TranslationException("Only constant or bind variable allowed for argument 2 of TO_CHAR");
            }
        }
        
        // First, handle the case where no format is passed in.
        if (format == null || format.length() == 0) {
            out.add(new ReplacementElement(TokenType.WORD, "CONVERT", " "));
            out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
            out.add(new ReplacementElement(TokenType.WORD, "NVARCHAR", " "));
            out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
            out.addAll(args.get(0));
            out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        }
        // If the first character is numeric, handle numeric formats.
        else if (format.charAt(0) == '9' || format.charAt(0) == '0') {
            boolean zeroPad = false;
            if (format.charAt(0) == '0') {
                zeroPad = true;
            }
            
            // CASE SIGN(%s) WHEN -1 THEN '-' ELSE ' ' END +"
            //    RIGHT(REPLICATE('0', %d)+
            //    LTRIM(STR(ABS(%s), %d, %d)),%d)
            
            // Example: 99.99
            // length = 5
            // dotPos = 2
            // precision = 5 - (2 + 1) == 2
            int length = format.length();
            int precision;
            int dotPos = format.indexOf('.');
            if (dotPos == -1) {
                precision = 0;
            }
            else {
                precision = length - (dotPos + 1);
            }
            
            if (zeroPad) {
                out.add(new ReplacementElement("CASE SIGN("));
                out.addAll(args.get(0));
                out.add(new ReplacementElement(") WHEN -1 THEN N'-' ELSE N' ' END +"));
                out.add(new ReplacementElement("RIGHT(N'000000000000000000000000000000' + LTRIM(STR(ABS("));
                out.addAll(args.get(0));
                out.add(new ReplacementElement("), " + length + ", " + precision + ")), " + length + ")"));
            }
            else {
                out.add(new ReplacementElement("STR("));
                out.addAll(args.get(0));
                out.add(new ReplacementElement(", " + (length + 1) + ", " + precision + ")"));
            }
        }
        else {
            out.add(new ReplacementElement("(CASE WHEN "));
            out.addAll(args.get(0));
            out.add(new ReplacementElement("is null THEN cast(null as nvarchar) ELSE "));
            List<SQLElement> valueArg = args.get(0);
            
            format = format.toUpperCase();

            for (int i = 0; i < format.length();) {
                if (i != 0) {
                    out.add(new ReplacementElement(TokenType.PLUS, "+", ""));
                }

                switch (format.charAt(i)) {
                case 'Y':
                    if (format.startsWith("YYYY", i)) {
                        out.add(new ReplacementElement("CONVERT(NVARCHAR, DATEPART(yy, "));
                        out.addAll(valueArg);
                        out.add(new ReplacementElement("))"));
                        i += 4;
                    }
                    else {
                        out.add(new ReplacementElement("RIGHT(CONVERT(NVARCHAR, DATEPART(yy, "));
                        out.addAll(valueArg);
                        if (format.startsWith("YYY", i)) {
                            out.add(new ReplacementElement(")), 3)"));
                            i += 3;
                        }
                        else if (format.startsWith("YY", i)) {
                            out.add(new ReplacementElement(")), 2)"));
                            i += 2;
                        }
                        else {
                            out.add(new ReplacementElement(")), 1)"));
                            i++;
                        }
                    }
                    break;
                
                case 'M':
                    if (format.startsWith("MM", i)) {
                        out.add(new ReplacementElement("RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(mm, "));
                        out.addAll(valueArg);
                        out.add(new ReplacementElement(")), 2)"));
                        i += 2;
                    }
                    else if (format.startsWith("MONTH", i)) {
                        out.add(new ReplacementElement("UPPER(LEFT(CONVERT(NVARCHAR,DATENAME(mm, "));
                        out.addAll(valueArg);
                        out.add(new ReplacementElement("))+SPACE(9), 9))"));
                        i += 5;
                    }
                    else if (format.startsWith("MI", i)) {
                        out.add(new ReplacementElement("RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(mi, "));
                        out.addAll(valueArg);
                        out.add(new ReplacementElement(")), 2)"));
                        i += 2;
                    }
                    else {
                        throw new TranslationException("Illegal Date Format: " + format);
                    }
                    break;
                
                case 'D':
                    if (format.startsWith("DDD", i)) {
                        out.add(new ReplacementElement("RIGHT(N'000' + DATENAME(dy, "));
                        out.addAll(valueArg);
                        out.add(new ReplacementElement("), 3)"));
                        i += 3;
                    }
                    else if (format.startsWith("DD", i)) {
                        out.add(new ReplacementElement("RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(dd, "));
                        out.addAll(valueArg);
                        out.add(new ReplacementElement(")), 2)"));
                        i += 2;
                    }
                    else if (format.startsWith("DAY", i)) {
                        out.add(new ReplacementElement("UPPER(LEFT(CONVERT(NVARCHAR, DATENAME(dw, "));
                        out.addAll(valueArg);
                        out.add(new ReplacementElement("))+ SPACE(9),9))"));
                        i += 3;
                    }
                    else {
                        out.add(new ReplacementElement("CONVERT(NVARCHAR, DATEPART(dw, "));
                        out.addAll(valueArg);
                        out.add(new ReplacementElement("))"));
                        i += 1;
                    }
                    break;

                case 'H':
                    if (format.startsWith("HH24", i)) {
                        out.add(new ReplacementElement("RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(hh, "));
                        out.addAll(valueArg);
                        out.add(new ReplacementElement(")), 2)"));
                        i += 4;
                    }
                    else {
                        throw new TranslationException("Illegal Date Format: " + format);
                    }
                    break;

                case 'S':
                    if (format.startsWith("SS", i)) {
                        out.add(new ReplacementElement("RIGHT(N'00' + CONVERT(NVARCHAR, DATEPART(ss, "));
                        out.addAll(valueArg);
                        out.add(new ReplacementElement(")), 2)"));
                        i += 2;
                    }
                    else {
                        throw new TranslationException("Illegal Date Format: " + format);
                    }
                    break;

                case '-':
                case '/':
                case ':':
                case ' ':
                    out.add(new ReplacementElement("N'" + format.charAt(i) + "'"));
                    i++;
                    break;
                default:
                    if (format.startsWith("IW", i) || format.startsWith("WW", i)) {
                        out.add(new ReplacementElement("RIGHT(N'00' + DATENAME(wk, "));
                        out.addAll(valueArg);
                        out.add(new ReplacementElement("), 2)"));
                        i += 2;
                    }
                    else if (format.startsWith("J", i)) {
                        out.add(new ReplacementElement("CONVERT(NVARCHAR, CEILING(CONVERT(REAL, DATEDIFF(DAY, CONVERT(DATETIME, N'1753/01/01'), "));
                        out.addAll(valueArg);
                        out.add(new ReplacementElement("))))"));
                        i += 1;
                    }
                    else if (format.startsWith("Q", i)) {
                        out.add(new ReplacementElement("DATENAME(q, "));
                        out.addAll(valueArg);
                        out.add(new ReplacementElement(")"));
                        i += 1;
                    }
                    else {
                        throw new TranslationException("Illegal Date Format: " + format);
                    }
                    break;
                }
            }
            out.add(new ReplacementElement("END)"));
        }
        
        return out;
    }
}
