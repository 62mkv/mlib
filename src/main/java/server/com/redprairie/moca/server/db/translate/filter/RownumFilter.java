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

package com.redprairie.moca.server.db.translate.filter;

import java.util.ArrayList;
import java.util.List;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.ReplacementElement;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.TranslationOptions;

/**
 * Filter to translate the Oracle construct <code>WHERE ROWNUM &lt; x</code>
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public abstract class RownumFilter implements TranslationFilter {

    // @see com.redprairie.moca.db.translate.filter.TranslationFilter#filter(com.redprairie.moca.db.translate.SQLElement[], com.redprairie.moca.db.BindList)
    public SQLElement[] filter(SQLElement[] input, BindList bindList,
            TranslationOptions options) throws TranslationException {
        return new _RownumProcessor(input, bindList).process();
    }
    
    //
    // Implementation
    //

    // Inner class to hold the state of a recursive filter.  We look for rownum elements, watching
    // out for subqueries (watch for parentheses).  If we see an opening parenthesis, we
    // recursively scan for another query.  If that parenthetical expression does NOT
    // contain a select statement, but DOES contain a rownum expression, we need to pass that
    // rownum value (i.e. max rows to return) back to the caller.
    private class _RownumProcessor {

        private _RownumProcessor(SQLElement[] input, BindList bindList) {
            _input = input;
            _bindList = bindList;
        }
        
        // Main entry point into the recursive filter.
        private SQLElement[] process() throws TranslationException {
            processQuery(false);
            return _output.toArray(new SQLElement[_output.size()]);
        }
        
        // Process the query recursively.  This function can get called at the top level for all
        // queries, and called recursively when parentheses are seen.  When it reaches a matching
        // closing parenthesis, it quits processing and deals with what it's seen.
        private long processQuery(boolean isSubQuery) throws TranslationException {
            boolean isSelect = false;
            long rownumValue = -1L;
            int selectElement = -1;
            
            // The main loop.  Keep looking until we've hit the end of the query
            for (;_pos < _input.length; _pos++) {
                SQLElement element = _input[_pos];
                
                if (isSubQuery && element.getType() == TokenType.RIGHTPAREN) {
                    // If we're processing a parenthetical expression, and we've hit the right side of
                    // it, break out of the loop.  The boolean isSubQuery indicates whether this is
                    // the top-level statement or a parenthetical expression.
                    break;
                }
                else if (element.getType() == TokenType.SEMICOLON ||
                         element.getType() == TokenType.END ||
                         (element.getType() == TokenType.WORD &&
                          element.getValue().equalsIgnoreCase("union"))) {
                    // We've hit the end of a statement, so let's look to see if we've seen a
                    // select statement and a rownum value.  If so, manipulate the query to add
                    // the rownum translation.
                    if (isSelect && rownumValue >= 0L) {
                        _addRownumClause(_output, rownumValue, selectElement);
                        isSelect = false;
                        rownumValue = -1L;
                    }
                }
                else if (element.getType() == TokenType.LEFTPAREN) {
                    // We've hit the a new parenthetical expression.  Since a rownum expression
                    // could sit inside parentheses, we need to capture any rownum value
                    // seen while processing a potential subquery.
                    _output.add(element);
                    _pos++;
                    long subRownumValue = processQuery(true);
                    
                    // If a rownum value was found, that means a rownum expression was seen, but
                    // no subquery was processed.
                    if (subRownumValue >= 0L) {
                        rownumValue = subRownumValue;
                    }
                    element = _input[_pos];
                }
                else if (element.getType() == TokenType.WORD &&
                        element.getValue().equalsIgnoreCase("SELECT")) {
                    // We've seen the SELECT keyword, which means we're processing a select
                    // statment or subquery
                    isSelect = true;
                    selectElement = _output.size();
                }
                else if (element.getType() == TokenType.WORD &&
                        element.getValue().equalsIgnoreCase("ROWNUM")) {
                    // We've seen the word "rownum", which means we need to handle
                    // rownum expressions (rownum < #, rownum = 1, rownum <= #

                    // Skip over comments
                    do {
                        _pos++;
                    } while (_input[_pos].getType() == TokenType.COMMENT);
                    
                    TokenType oper = _input[_pos].getType();
                    
                    // If the operator is not a "normal" operator, we can't understand
                    // the expression, so leave it alone.
                    if (oper != TokenType.LT &&
                        oper != TokenType.LE &&
                        oper != TokenType.GE &&
                        oper != TokenType.GT &&
                        oper != TokenType.EQ &&
                        oper != TokenType.NE) {
                        continue;
                    }
                    
                    // Skip over comments
                    do {
                        _pos++;
                    } while (_input[_pos].getType() == TokenType.COMMENT);
                    
                    if (_input[_pos].getType() == TokenType.BIND_VARIABLE) {
                        String bindName = _input[_pos].getValue().substring(1);

                        if (_bindList == null) {
                            throw new TranslationException("no bind variable: " + bindName);
                        }
                        
                        Object bindValue = _bindList.getValue(bindName);
                        if (bindValue == null) {
                            throw new TranslationException("missing bind variable: " + bindName);
                        }
                        
                        if (bindValue instanceof Number) {
                            rownumValue = ((Number)bindValue).longValue();
                        }
                        else {
                            throw new TranslationException(
                                    "invalid bind variable type: " + bindName +
                                    ", Type = " + _bindList.getType(bindName));
                        }
                    }
                    else if (_input[_pos].getType() == TokenType.INT_LITERAL) {
                        rownumValue = Long.parseLong(_input[_pos].getValue());
                    }
                    else if (_input[_pos].getType() == TokenType.FLOAT_LITERAL) {
                        rownumValue = (long) Double.parseDouble(_input[_pos].getValue());
                    } 
                    else {
                        throw new TranslationException(
                                "expected literal or bind variable after ROWNUM" +
                                ", got " + _input[_pos].getValue());
                    }
                    
                    // Deal with rownum < some-number or rownum <= some-number.
                    if (oper == TokenType.LE ||
                        (oper == TokenType.EQ && rownumValue == 1)) {
                        // No change to argValue
                    }
                    else if (oper == TokenType.LT) {
                        rownumValue--;
                    }
                    else {
                        throw new TranslationException(
                                "Invalid rownum operation: " + oper);
                    }
                    
                    // Since we'll be handling the rownum expression later, replace the entire
                    // rownum expression with "1=1".  This will allow it to continue to function
                    // no matter where the expression appeared in the original SQL statement.
                    element = new ReplacementElement("1=1");
                }
                _output.add(element);
            }

            // We've hit the end of the query.  If we have seen (and replaced) a rownum
            // expression, and this query is a select statement, insert the translated form
            // at the appropriate place in the translated statement.
            if (isSelect && rownumValue >= 0L) {
                _addRownumClause(_output, rownumValue, selectElement);
                rownumValue = -1L;
            }

            // This will be >=0 if we saw and replaced a rownum expression, but we weren't
            // processing a select statement. That indicates that we saw a parenthetical
            // rownum expression, and need to pass it up to the previous level of the filter.
            return rownumValue;
        }
        
        private int _pos = 0;
        private SQLElement[] _input;
        private BindList _bindList;
        List<SQLElement> _output = new ArrayList<SQLElement>();
    }
    
    protected abstract void _addRownumClause(List<SQLElement> output, long rownumValue, int selectElement);
    
}

