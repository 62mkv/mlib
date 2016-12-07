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

package com.redprairie.moca.server.db.translate.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.TranslationOptions;
import com.redprairie.moca.server.db.translate.filter.functions.FunctionHandler;

/**
 * SQL Translation filter for translating arbitrary functions into other SQL.
 * This is useful when a SQL construct does not have a straightforward analog
 * in another SQL dialect.  A good example of this is the decode function in
 * Oracle, which needs to be translated into a long case expression.
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class FunctionFilter implements TranslationFilter {
    
    public FunctionFilter(Map<String, FunctionHandler> handlers) {
        _handlers = handlers;
    }

    // @see com.redprairie.moca.db.translate.filter.TranslationFilter#filter(com.redprairie.moca.db.translate.SQLElement[], com.redprairie.moca.db.BindList)
    public SQLElement[] filter(SQLElement[] input, BindList bindList,
            TranslationOptions options) throws TranslationException {
        return new _FunctionProcessor(input, bindList).process();
    }
    
    /**
     * Add a function handler to this filter.  Functions matching
     * <code>name</code> will be parsed and handed to the given instance of
     * <code>FunctionHandler</code>.
     * @param name the name of the function, as it appears in SQL expressions.
     * @param handler the handler to be used to translate this function.
     */
    protected void addHandler(String name, FunctionHandler handler) {
        _handlers.put(name, handler);
    }
    
    //
    // Implementation
    //

    // A private class used to handle recursive parsing of expressions.  This
    // object will keep track of a statement and where we are in the parser.
    private class _FunctionProcessor {

        private _FunctionProcessor(SQLElement[] input, BindList bindList) {
            _input = input;
            _bindList = bindList;
        }
        
        private SQLElement[] process() throws TranslationException {
            List<SQLElement> output = processNoHandler(null, false);
            return output.toArray(new SQLElement[output.size()]);
        }
        
        private List<SQLElement> processParentheses(SQLElement wordElement) throws TranslationException {
            // This processor will be invoked to handle something that looks
            // like a function. By the time this function gets called, we
            // should have already processed two tokens: the function name and
            // the opening parenthesis mark. If so, look up the handler for the
            // given function.
            if (wordElement != null) {
                String word = wordElement.getValue();
                FunctionHandler handler = _handlers.get(word.toLowerCase());
                if (handler != null) {
                    // Skip over the parentheses
                    _pos++;
                    return processHandler(word, handler);
                }
            }
            
            return processNoHandler(wordElement, true);
       }

        private List<SQLElement> processHandler(String name,
                FunctionHandler handler) throws TranslationException {
            
            // Each argument to a function can be a sequence of SQL elements
            // (e.g. a function call, a simple expression, etc.).  Therefore,
            // to parse the function call, we need to keep track of arguments.
            // a nested function will be processed by 
            List<List<SQLElement>> args = new ArrayList<List<SQLElement>>();
            List<SQLElement> currentArg = new ArrayList<SQLElement>();
            SQLElement last = null;

            for (;_pos < _input.length; _pos++) {
                SQLElement element = _input[_pos];
                if (element.getType() == TokenType.LEFTPAREN) {
                    List<SQLElement> temp = processParentheses(last);
                    currentArg.addAll(temp);
                    last = null;
                }
                else if (element.getType() == TokenType.RIGHTPAREN) {
                    if (last != null) currentArg.add(last);
                    args.add(currentArg);
                    break;
                }
                else if (element.getType() == TokenType.COMMA) {
                    if (last != null) currentArg.add(last);
                    args.add(currentArg);
                    currentArg = new ArrayList<SQLElement>();
                    last = null;
                }
                else if (element.getType() == TokenType.COMMENT) {
                    // We just ignore comments, we don't support hints inside
                    // functions currently
                }
                else {
                    if (last != null) currentArg.add(last);
                    last = element;
                }
            }
            
            // We should never see the end of the input while translating a
            // function.  There should always be an "end" token at the end,
            // even if the close paren is the last thing on the SQL statement.
            if (_pos >= _input.length) {
                throw new TranslationException("unmatched parentheses");
            }
            
            return handler.translate(name, args, _bindList);
        }

        private List<SQLElement> processNoHandler(SQLElement word, boolean inParentheses)
                throws TranslationException {
            List<SQLElement> output = new ArrayList<SQLElement>();

            // If we've seen parentheses, then we've got to add the preceding
            // two tokens to the output of this function.  We 
            if (word != null) {
                output.add(word);
            }
            
            if (inParentheses) {
                output.add(_input[_pos++]);
            }

            SQLElement last = null;
            List<SQLElement> commentElements = new ArrayList<SQLElement>();
            for (;_pos < _input.length; _pos++) {
                SQLElement element = _input[_pos];
                if (element.getType() == TokenType.COMMENT) {
                    commentElements.add(element);
                }
                else {
                    if (element.getType() == TokenType.LEFTPAREN) {
                        output.addAll(processParentheses(last));
                        last = null;
                    }
                    else if (element.getType() == TokenType.RIGHTPAREN) {
                        if (last != null) output.add(last);
                        last = element;
                        break;
                    }
                    else {
                        if (last != null) output.add(last);
                        last = element;
                    }
                    
                    // Add any previously found comments after we added the
                    // last value
                    output.addAll(commentElements);
                    commentElements.clear();
                }
            }
            
            if (last != null) output.add(last);

            // We should never see the end of the input while translating a
            // function.  There should always be an "end" token at the end,
            // even if the close paren is the last thing on the SQL statement.
            if (inParentheses && _pos >= _input.length) {
                throw new TranslationException("unmatched parentheses");
            }
            
            return output;
        }
        
        private int _pos = 0;
        private SQLElement[] _input;
        private BindList _bindList;
    }
    
    private final Map<String, FunctionHandler> _handlers;
}

