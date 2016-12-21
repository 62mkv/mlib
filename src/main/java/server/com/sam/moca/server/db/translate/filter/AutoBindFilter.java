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

package com.sam.moca.server.db.translate.filter;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.sam.moca.MocaType;
import com.sam.moca.server.db.BindList;
import com.sam.moca.server.db.translate.ReplacementElement;
import com.sam.moca.server.db.translate.SQLElement;
import com.sam.moca.server.db.translate.TokenType;
import com.sam.moca.server.db.translate.TranslationException;
import com.sam.moca.server.db.translate.TranslationOptions;

/**
 * Filter to automatically insert bind variables in a SQL statement where string or numeric
 * constants had been.  The behavior is to bind variables skipping over "order by" clauses.
 * This will avoid binding numeric parameters when <code>order by 2, 3</code>
 * is seen, which in normal SQL syntax, means to order by column numbers 2 and 3.  Some SQL
 * engines do not allow parameters in that context. Some SQL engines support order by
 * in subqueries therefore normal binding will start again after an order by in a subquery
 * is encountered.
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class AutoBindFilter implements TranslationFilter {
    
    public SQLElement[] filter(SQLElement[] input, BindList args,
            TranslationOptions options) throws TranslationException {
        List<SQLElement> output = new ArrayList<SQLElement>();
        int count = 0;
        boolean inhibit = !options.isAutoBind();
        SQLElement lastElement = null;
        
        for (int i = 0; i < input.length; i++) {
            SQLElement element = input[i];
            TokenType elementType = element.getType(); 
            if (inhibit) {
                // do nothing
            }
            else if (elementType == TokenType.STRING_LITERAL) {
                String name = "q" + count;
                String quotedValue = element.getValue();
                
                // length = 2 indicates empty string with quotes
                if (quotedValue.length() == 2) {
                    args.add(name, MocaType.STRING, null);
                }
                else {
                    args.add(name, MocaType.STRING, dequote(quotedValue, 1));
                }

                element = new ReplacementElement(TokenType.BIND_VARIABLE, ":" + name, element.getLeadingWhitespace());
                count++;
            }
            else if (elementType == TokenType.NSTRING_LITERAL) {
                String name = "u" + count;
                String quotedValue = element.getValue();
                
                // length = 2 indicates empty string with quotes
                if (quotedValue.length() == 2) {
                    args.add(name, MocaType.STRING, null);
                }
                else {
                    args.add(name, MocaType.STRING, dequote(quotedValue, 2));
                }

                element = new ReplacementElement(TokenType.BIND_VARIABLE, ":" + name, element.getLeadingWhitespace());
                count++;
            }
            else if (elementType  == TokenType.INT_LITERAL) {
                
                // If this element is of type 
                if ((i < input.length - 2) &&
                    (input[i+1].getType() == TokenType.EQ) &&
                    (input[i+2].getType() == TokenType.INT_LITERAL)) {
                    output.add(element);
                    output.add(input[i+1]);
                    i += 2;
                    element = input[i];
                }
                else {
                    String value = element.getValue();
                    Integer intValue = null;
                    if (value.length() < 10) {
                        intValue = new Integer(value);
                    }
                    else if (value.length() <= 11) {
                        BigInteger numValue = new BigInteger(value);
                        if (numValue.bitLength() <= 31) {
                            intValue = numValue.intValue();
                        }
                    }
                    
                    if (intValue != null) {
                        String name = "i" + count;
                        args.add(name, MocaType.INTEGER, intValue);
                        element = new ReplacementElement(TokenType.BIND_VARIABLE, ":" + name, element.getLeadingWhitespace());
                        count++;
                    }
                }
            }
            else if (elementType  == TokenType.FLOAT_LITERAL) {
                String name = "f" + count;
                args.add(name, MocaType.DOUBLE, Double.valueOf(element.getValue()));
                element = new ReplacementElement(TokenType.BIND_VARIABLE, ":" + name, element.getLeadingWhitespace());
                count++;
            }

            output.add(element);
            
            // Look for "order by" and inhibit parsing during it
            if (lastElement != null &&
                    elementType == TokenType.WORD && element.getValue().equalsIgnoreCase("by") &&
                    lastElement.getType() == TokenType.WORD && lastElement.getValue().equalsIgnoreCase("order")) {
            	
            	// Skip through the next elements and see if we're in a subquery. Some SQL engines such as Oracle
            	// support order by in a subquery in which case we want to go back to normal parsing after the subquery.
            	int parenCount = 1;
            	for (++i; i < input.length; i++) {
            		element = input[i];
                    elementType = element.getType(); 
                    output.add(element);
                    if (elementType == TokenType.LEFTPAREN) {
                    	parenCount++;
                    }
                    else if (elementType == TokenType.RIGHTPAREN) {
                    	parenCount--;
                    }
                    
                    // This means we're at the end of a subquery, go back to normal parsing
                    if (parenCount == 0) {
                    	break;
                    }
            	}
            }
            // Look for comment-borne hints.
            else if (elementType == TokenType.COMMENT) {
                if (element.getValue().equalsIgnoreCase("/*#nobind*/")) {
                    inhibit = true;
                }
                else if (element.getValue().equalsIgnoreCase("/*#bind*/")) {
                    inhibit = false;
                }
            }
            
            // We only hold non comment types as the last element
            if (elementType != TokenType.COMMENT) {
                lastElement = element;
            }
        }
        
        return output.toArray(new SQLElement[output.size()]);
    }
    
    private static String dequote(String in, int start) {
        StringBuilder out = new StringBuilder(in.length() - 2);
        int inLength = in.length();
        for (int i = start; i < inLength - 1; i++) {
            char inChar = in.charAt(i);
            out.append(inChar);
            if (inChar == '\'') {
                i++;
            }
        }
        return out.toString();
    }
}
