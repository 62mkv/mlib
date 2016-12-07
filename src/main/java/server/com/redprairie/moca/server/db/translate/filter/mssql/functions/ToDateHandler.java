/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.server.db.translate.filter.mssql.functions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.ReplacementElement;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.filter.functions.FunctionHandler;

/**
 * A function handler designed to deal with the Oracle TO_DATE function.
 * Parameters are parsed, and the arguments and/or bind variables are
 * reorganized into a form that will work for SQL Server's date conversion
 * routines.  This involves client-side string manipulation, which is
 * probably not particularly efficient.
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ToDateHandler implements FunctionHandler {
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs)
            throws TranslationException{
        
        int argCount = args.size();
        if (argCount != 1 && argCount != 2) {
            throw new TranslationException(name + " requires 1 or 2 arguments, received " + argCount);
        }
        
        List<SQLElement> valueArg = args.get(0);
        
        // If value is null, just wrap it with a convert statement.
        if (isNullArg(valueArg, bindArgs)) {
            List<SQLElement> out = new ArrayList<SQLElement>();

            out.add(new ReplacementElement(TokenType.WORD, "CONVERT", " "));
            out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
            out.add(new ReplacementElement(TokenType.WORD, "DATETIME", ""));
            out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
            out.addAll(valueArg);
            out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
            return out;
        }

        
        // Pick out the format String
        String format = null;
        String value = getArgValue(valueArg, bindArgs);
        
        if (argCount == 1) {
            if (value.length() == 8) format = "YYYYMMDD";
            else format = "YYYYMMDDHH24MISS";
        }
        else {
            format = getArgValue(args.get(1), bindArgs);
        }
        
        // We go through the format string, looking for known formats
        
        format = format.toUpperCase();
        
        if (format.equals("J")) {
             // (DATEADD(day, CONVERT(INTEGER,xxx),CONVERT(DATETIME, '1753/01/01')))
             List<SQLElement> out = new ArrayList<SQLElement>();

             out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
             out.add(new ReplacementElement(TokenType.WORD, "DATEADD", ""));
             out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
             out.add(new ReplacementElement(TokenType.WORD, "day", ""));
             out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
             out.add(new ReplacementElement(TokenType.WORD, "CONVERT", " "));
             out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
             out.add(new ReplacementElement(TokenType.WORD, "INTEGER", ""));
             out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
             out.addAll(valueArg);
             out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
             out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
             out.add(new ReplacementElement(TokenType.WORD, "CONVERT", " "));
             out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
             out.add(new ReplacementElement(TokenType.WORD, "DATETIME", ""));
             out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
             out.add(new ReplacementElement(TokenType.STRING_LITERAL, "'1753/01/01'", " "));
             out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
             out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
             out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));

             return out;
        }

        String yyyy = null;
        String mm = null;
        String month = null;
        String dd = null;
        String mi = null;
        String hh = null;
        String ss = null;
        
        for (int i = 0,j = 0; i < format.length() && j < value.length();) {
            switch (format.charAt(i)) {
            case 'Y':
                if (format.startsWith("YYYY", i)) {
                    yyyy = value.substring(j, j + 4);
                    validateNumeric(yyyy);
                    i += 4;
                    j += 4;
                }
                else {
                    throw new TranslationException("Illegal Date Format: " + format);
                }
                break;
            
            case 'M':
                if (format.startsWith("MM", i)) {
                    mm = value.substring(j, j+2);
                    validateNumeric(mm);
                    i += 2;
                    j += 2;
                }
                else if (format.startsWith("MONTH", i)) {
                    StringBuilder tmp = new StringBuilder();
                    while (j < value.length() && Character.isLetter(value.charAt(j))) {
                        tmp.append(value.charAt(j));
                        j++;
                    }
                    j += tmp.length();
                    month = tmp.substring(0, 3);
                    i += 5;
                }
                else if (format.startsWith("MON", i)) {
                    month = value.substring(j, j + 3);
                    i += 3;
                    j += 3;
                }
                else if (format.startsWith("MI", i)) {
                    mi = value.substring(j, j + 2);
                    validateNumeric(mi);
                    i += 2;
                    j += 2;
                }
                else {
                    throw new TranslationException("Illegal Date Format: " + format);
                }
                break;
            
            case 'D':
                if (format.startsWith("DD", i)) {
                    dd = value.substring(j, j + 2);
                    validateNumeric(dd);
                    i += 2;
                    j += 2;
                }
                else {
                    throw new TranslationException("Illegal Date Format: " + format);
                }
                break;

            case 'H':
                if (format.startsWith("HH24", i)) {
                    hh = value.substring(j, j + 2);
                    validateNumeric(hh);
                    i += 4;
                    j += 2;
                }
                else {
                    throw new TranslationException("Illegal Date Format: " + format);
                }
                break;

            case 'S':
                if (format.startsWith("SS", i)) {
                    ss = value.substring(j, j + 2);
                    validateNumeric(ss);
                    i += 2;
                    j += 2;
                }
                else {
                    throw new TranslationException("Illegal Date Format: " + format);
                }
                break;

            case '-':
            case '/':
            case ':':
            case ' ':
                i++;
                j++;
                break;
            default:
                throw new TranslationException("Illegal Date Format: " + format);
            }
        }
        
        // Now check to that we can make a valid date.
        // We must have year, month, day
        if (yyyy == null || (mm == null && month == null) || dd == null) {
            throw new TranslationException ("Invalid Date Format: " + format);
        }
        
        String sqlFormat = "20";
        
        StringBuilder newValue = new StringBuilder();
        if (month == null) {
            newValue.append(yyyy).append('-').append(mm).append('-').append(dd);
        }
        else {
            newValue.append(dd).append(' ').append(month).append(' ').append(yyyy);
            sqlFormat = "13";
        }

        if (hh != null) {
            newValue.append(' ').append(hh);
            if (mi != null) {
                newValue.append(':').append(mi);
            }
            if (ss != null) {
                newValue.append(':').append(ss);
            }
        }

        
        List<SQLElement> out = new ArrayList<SQLElement>();

        out.add(new ReplacementElement(TokenType.WORD, "CONVERT", " "));
        out.add(new ReplacementElement(TokenType.LEFTPAREN, "(", ""));
        out.add(new ReplacementElement(TokenType.WORD, "DATETIME", ""));
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));

        // Check for bind variable.  If in use, replace with another one
        if (valueArg.size() == 1 && valueArg.get(0).getType() == TokenType.BIND_VARIABLE) {
            String varName = valueArg.get(0).getValue().substring(1);
            out.add(new ReplacementElement(TokenType.BIND_VARIABLE, ":" + varName + "__td", " "));
            bindArgs.add(varName + "__td", newValue.toString());
        }
        else {
            out.add(new ReplacementElement(TokenType.STRING_LITERAL, "'" + newValue + "'", " "));
        }
        
        out.add(new ReplacementElement(TokenType.COMMA, ",", ""));
        out.add(new ReplacementElement(TokenType.INT_LITERAL, sqlFormat, " "));
        out.add(new ReplacementElement(TokenType.RIGHTPAREN, ")", ""));
        
        return out;
    }
    
    private boolean isNullArg(List<SQLElement> arg, BindList bindArgs)
        throws TranslationException {
        boolean isNull = false;
        if (arg.size() == 1) {
            SQLElement argElement = arg.get(0);
            if (argElement.getType() == TokenType.STRING_LITERAL) {
                String argValue = argElement.getValue();
                int len = argValue.length();
                isNull = (len == 2);
            }
            else if (argElement.getType() == TokenType.NSTRING_LITERAL) {
                String argValue = argElement.getValue();
                int len = argValue.length();
                isNull = (len == 3);                
            }
            else if (argElement.getType() == TokenType.BIND_VARIABLE) {
                String bindVar = argElement.getValue().substring(1);
                Object bindValue = bindArgs.getValue(bindVar);
                isNull = (bindArgs.contains(bindVar) &&  (bindValue == null || bindValue.equals("")));
            }
            else if (argElement.getType() == TokenType.WORD &&
                argElement.getValue().equalsIgnoreCase("null")) {
                isNull = true; 
            }
        }
        else if (arg.size() >= 6 &&
                 (arg.get(0).getType() == TokenType.WORD && 
                  arg.get(0).getValue().equalsIgnoreCase("cast")) &&
                  arg.get(1).getType() == TokenType.LEFTPAREN && 
                  (arg.get(2).getType() == TokenType.WORD && 
                   arg.get(2).getValue().equalsIgnoreCase("null")) &&
                   (arg.get(3).getType() == TokenType.WORD && 
                           arg.get(3).getValue().equalsIgnoreCase("as"))) {
            isNull = true; 
        }

        return isNull;
    }
 
    private String getArgValue(List<SQLElement> arg, BindList bindArgs)
            throws TranslationException {
        String argValue = null;
        if (arg.size() == 1) {
            SQLElement argElement = arg.get(0);
            if (argElement.getType() == TokenType.STRING_LITERAL) {
                argValue = argElement.getValue();
                argValue = argValue.substring(1, argValue.length() - 1);
            }
            else if (argElement.getType() == TokenType.BIND_VARIABLE) {
                String bindVar = argElement.getValue().substring(1);
                Object bindValue = bindArgs.getValue(bindVar);
                if (bindValue != null) {
                    if (bindValue instanceof String) {
                        argValue = (String)bindArgs.getValue(bindVar);
                    }
                    else if (bindValue instanceof Timestamp) {
                        argValue = new DateTime(((Timestamp) bindValue)
                                .getTime()).toString("YYYYMMddHHmmss");
                    }
                }
            }
            else if (argElement.getType() == TokenType.WORD &&
                argElement.getValue().equalsIgnoreCase("null")) {
                argValue = null; 
            }
            else if (argElement.getType() == TokenType.NSTRING_LITERAL) {
                argValue = argElement.getValue();
                argValue = argValue.substring(2, argValue.length() - 1);
            }
            else {
                throw new TranslationException(
                    "Expected literal or bind variable, received " + argElement);
            }
        }
        else {
            throw new TranslationException(
                "Expected literal or bind variable, received " + arg);
        }
        return argValue;
    }
    
    private void validateNumeric(String value) throws TranslationException {
        int targetLength = value.length();
        for (int i = 0; i < targetLength; i++) {
            if (!Character.isDigit(value.charAt(i))) {
                throw new TranslationException("Illegal date field: " + value);
            }
        }
    }
}
