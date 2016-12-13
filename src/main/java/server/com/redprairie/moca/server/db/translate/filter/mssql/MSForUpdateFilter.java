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

package com.redprairie.moca.server.db.translate.filter.mssql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.ReplacementElement;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.TranslationOptions;
import com.redprairie.moca.server.db.translate.filter.TranslationFilter;

/**
 * Filter to handle the use of the FOR UPDATE clause in a SQL select statement.
 * For SQL Server, this filter looks for a "for update" clause, and adds the 
 * (UPDLOCK) hint at the point in the query where the table name is mentioned.
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MSForUpdateFilter implements TranslationFilter {
    public SQLElement[] filter(SQLElement[] input, BindList args,
            TranslationOptions options) throws TranslationException {
        List<SQLElement> output = new ArrayList<SQLElement>();
        List<String> lockItems = new ArrayList<String>();
        Map<String, Integer> tablePositions = new HashMap<String, Integer>();
        int lastTablePosition = -1;
        boolean isSelect = false;
        
        for (int i = 0; i < input.length; i++) {
            SQLElement element = input[i];
            if (!isSelect && matchWord(element, "select")) {
                isSelect = true;
            }
            else if (isSelect) {
                // Start looking for tables to update.  We're looking for
                // clauses like FROM foo [[as] alias][, bar [[as] alias]] 
                if (matchWord(element, "from")) {
                    do {
                        output.add(element);
                        i = findUntilNoComment(i, input, output);
                        element = input[i];
                        
                        String candidateName = null;

                        // Handle Subqueries.  We don't expect to have to manipulate
                        // a subquery, so just treat it as a sequence of tokens with
                        // matched parentheses.
                        if (element.getType() == TokenType.LEFTPAREN) {
                            int level = 0;
                            // Now we go until we find the end of the subquery
                            // and possibly and comments after it or the end
                            // of the query
                            do {
                                if (element.getType() == TokenType.LEFTPAREN) {
                                    level ++;
                                }
                                else if (element.getType() == TokenType.RIGHTPAREN) {
                                    level --;
                                }
                                output.add(element);
                                i = findUntilNoComment(i, input, output);
                                element = input[i];
                            } while (level > 0  && element.getType() != TokenType.END); 
                        }
                        else if (element.getType() == TokenType.WORD) {
                            candidateName = element.getValue();
                            // Skip past the table name
                            output.add(element);
                            i = findUntilNoComment(i, input, output);
                            element = input[i];
                        }
                        
                        // Now, there may be an alias
                        if (element.getType() == TokenType.WORD && !isReserved(element.getValue())) {
                            if (element.getValue().equalsIgnoreCase("as")) {
                                // Add the as and any comments
                                output.add(element);
                                i = findUntilNoComment(i, input, output);
                                element = input[i];
                            }
                            candidateName = element.getValue();
                            output.add(element);
                            i = findUntilNoComment(i, input, output);
                            element = input[i];
                        }
                        
                        if (candidateName != null) {
                            tablePositions.put(candidateName.toLowerCase(), output.size());
                        }

                        lastTablePosition = output.size();
                    } while (element.getType() == TokenType.COMMA);
                }

                if (matchWord(element, "join")) {
                    output.add(element);
                    i = findUntilNoComment(i, input, output);
                    element = input[i];
                    
                    String candidateName = null;

                    // Handle Subqueries.  We don't expect to have to manipulate
                    // a subquery, so just treat it as a sequence of tokens with
                    // matched parentheses.
                    if (element.getType() == TokenType.LEFTPAREN) {
                        int level = 0;
                        do {
                            if (element.getType() == TokenType.LEFTPAREN) {
                                level ++;
                            }
                            else if (element.getType() == TokenType.RIGHTPAREN) {
                                level --;
                            }
                            output.add(element);
                            i = findUntilNoComment(i, input, output);
                            element = input[i];
                        } while (level > 0 && element.getType() != TokenType.END); 
                    }
                    else if (element.getType() == TokenType.WORD) {
                        candidateName = element.getValue();
                        // Skip past the table name and comments
                        output.add(element);
                        i = findUntilNoComment(i, input, output);
                        element = input[i];
                    }
                    
                    // Now, there may be an alias
                    if (element.getType() == TokenType.WORD && !isReserved(element.getValue())) {
                        if (element.getValue().equalsIgnoreCase("as")) {
                            // Add the as and any comments
                            output.add(element);
                            i = findUntilNoComment(i, input, output);
                            element = input[i];
                        }
                        candidateName = element.getValue();
                        output.add(element);
                        i = findUntilNoComment(i, input, output);
                        element = input[i];
                    }
                    
                    if (candidateName != null) {
                        tablePositions.put(candidateName.toLowerCase(), output.size());
                    }
                    
                    lastTablePosition = output.size();
                }
                
                if (matchWord(element, "for") &&
                    matchWord(input[findUntilNoComment(i, input, output)], "update")) {
                    // The first one is update
                    i = findUntilNoComment(i, input, output);
                    element = input[i];
                    i = findUntilNoComment(i, input, output);
                    // The second one is our next word
                    element = input[i];
                    int numHintsInserted = 0;
                    
                    if (matchWord(element, "of")) {
                        do {
                            i = findUntilNoComment(i, input, output);
                            element = input[i];
                            lockItems.add(element.getValue());
                            i = findUntilNoComment(i, input, output);
                            element = input[i];
                        } while (element.getType() == TokenType.COMMA);
                        
                        for (String lockItem : lockItems) {
                            int dotPos = lockItem.lastIndexOf('.');
                            if (dotPos != -1) {
                                String testName = lockItem.substring(0, dotPos).toLowerCase();
                                Integer tablePos = tablePositions.get(testName);
                                if (tablePos != null) {
                                    output.add(tablePos + numHintsInserted, new ReplacementElement("(UPDLOCK)"));
                                    numHintsInserted++;
                                }
                            }
                        }
                    }
                    
                    if (matchWord(element, "NOWAIT")) {
                        // Skip the nowait
                        element = input[++i];
                        options.addPreStatement("SET LOCK_TIMEOUT 0");
                        options.addPostStatement("SET LOCK_TIMEOUT -1");
                    }
                    
                    if (numHintsInserted == 0) {
                        output.add(lastTablePosition, new ReplacementElement("(UPDLOCK)"));
                    }
                    
                }
            }
            output.add(element);
        }
        
        return output.toArray(new SQLElement[output.size()]);
    }
    
    private int findUntilNoComment(int pos, SQLElement[] input, 
            List<SQLElement> output) {
        SQLElement element = input[++pos];
        
        if (element.getType() == TokenType.COMMENT) {
            output.add(element);
            return findUntilNoComment(pos, input, output);
        }
        
        return pos;
    }
    
    private boolean isReserved(String word) {
        return _RESERVED.contains(word.toLowerCase());
    }
    
    private boolean matchWord(SQLElement element, String word) {
        return (element.getType() == TokenType.WORD &&
                element.getValue().equalsIgnoreCase(word));
    }
    
    private static Set<String> _RESERVED = new HashSet<String>();
    static {
        _RESERVED.add("select");
        _RESERVED.add("update");
        _RESERVED.add("where");
        _RESERVED.add("union");
        _RESERVED.add("for");
        _RESERVED.add("having");
        _RESERVED.add("order");
        _RESERVED.add("group");
        _RESERVED.add("start");
        _RESERVED.add("into");
        _RESERVED.add("left");
        _RESERVED.add("right");
        _RESERVED.add("full");
        _RESERVED.add("join");
        _RESERVED.add("on");
    }
}
