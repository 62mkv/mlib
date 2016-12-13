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

package com.redprairie.moca.server.db.translate;

import java.util.ArrayList;
import java.util.List;

/**
 * A class to identify specific syntax elements of a SQL statement from a
 * given SQL input string.  The input to this class is a string, and the
 * class iterates through available <code>SQLToken</code> objects.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class SQLTokenizer {
    
    /**
     * Class describing a token identified in a tokenized SQL string.
     * 
     * @author dinksett
     * @version $Revision$
     */
    public class SQLToken implements SQLElement {
        
        public TokenType getType() {
            return _type;
        }
        
        public String getValue() {
            return _in.substring(_beginToken, _end);
        }
        
        public String toString() {
            return _in.substring(_beginWhitespace, _end);
        }
        
        public String getLeadingWhitespace() {
            return _in.substring(_beginWhitespace, _beginToken);
        }
        
        //
        // Implementation
        //
        private SQLToken(TokenType type, int beginWhitespace, int beginToken) {
            _type = type;
            _beginToken = beginToken;
            _beginWhitespace = beginWhitespace;
            _end = _pos;
        }
        
        private final TokenType _type;
        private final int _beginToken;
        private final int _beginWhitespace;
        private final int _end;
    }

    /**
     * Create a new tokenizer with the given SQL statement.
     */
    public SQLTokenizer(String in) {
        _in  = in;
        _pos = 0;
        _length = in.length();
    }
    
    /**
     * Return the next token.
     * @return a SQLElement object representing the next token in the SQL
     * statement. When the end of the statement is reached, a token of type
     * <code>TokenType.END</code> is returned.  Subsequent calls, after the
     * end token is returned will also return an end token.
     * @throws TokenizerException if there was a problem parsing the SQL
     * statement.
     */
    public SQLElement nextToken() throws TokenizerException {
        // Keep track of where we started
        int begin = _pos;
        _skipWhitespace();
        
        // If we're done, mark the end of the statement
        if (!_hasNext()) {
            return new SQLToken(TokenType.END, begin, _pos);
        }
        
        // We need to keep track of where this token's significant text began
        int startOfToken = _pos;
        
        // Read the first character
        char c = _next();
        
        // First, look for a few single-character tokens
        if (c == '(') {
            return new SQLToken(TokenType.LEFTPAREN, begin, startOfToken);
        }
        else if (c == ')') { 
            return new SQLToken(TokenType.RIGHTPAREN, begin, startOfToken);
        }
        else if (c == '+') {
            return new SQLToken(TokenType.PLUS, begin, startOfToken);
        }
        else if (c == '-') {
            if (_hasNext() && _peek() == '-') {
                c = _next();
                do {
                    c = _next();
                } while (_hasNext() && _peek() != '\n' && _peek() !='\r');

                return new SQLToken(TokenType.COMMENT, begin, startOfToken);
            }
            else {
                return new SQLToken(TokenType.MINUS, begin, startOfToken);
            }
        }
        else if (c == ',') {
            return new SQLToken(TokenType.COMMA, begin, startOfToken);
        }
        else if (c == ';') {
            return new SQLToken(TokenType.SEMICOLON, begin, startOfToken);
        }
        else if (c == '*') {
            return new SQLToken(TokenType.STAR, begin, startOfToken);
        }
        else if (c == '%') {
            return new SQLToken(TokenType.MOD, begin, startOfToken);
        }
        else if (c == '=') { 
            return new SQLToken(TokenType.EQ, begin, startOfToken);
        }
        else if (c == '|') { 
            if (_hasNext() && _peek() == '|') {
                // This is to go over the | character
                _next();
                return new SQLToken(TokenType.CONCAT, begin, startOfToken);
            }
            else {
                return new SQLToken(TokenType.OTHER, begin, startOfToken);
            }
        }
        else if (c == '!') {
            if (_hasNext() && _peek() == '=') {
                // This is to go over the = character
                _next();
                return new SQLToken(TokenType.NE, begin, startOfToken);
            }
            else {
                return new SQLToken(TokenType.OTHER, begin, startOfToken);
            }
        }
        else if (c == '>') {
            if (_hasNext() && _peek() == '=') {
                // This is to go over the = character
                _next();
                return new SQLToken(TokenType.GE, begin, startOfToken);
            }
            else {
                return new SQLToken(TokenType.GT, begin, startOfToken);
            }
        }
        else if (c == '<') { 
            if (_hasNext() && _peek() == '=') {
                // This is to go over the = character
                _next();
                return new SQLToken(TokenType.LE, begin, startOfToken);
            }
            else if (_hasNext() && _peek() == '>') {
                // This is to go over the > character
                _next();
                return new SQLToken(TokenType.NE, begin, startOfToken);
            }
            else {
                return new SQLToken(TokenType.LT, begin, startOfToken);
            }
        }
        else if (c == '/') {
            if (_hasNext() && (_peek() == '*')) {
                // This is to go over the * character
                _next();
                do {
                    c = _next();
                } while (c != '*' || _peek() != '/');
                // This is to go over the / character
                _next();
                return new SQLToken(TokenType.COMMENT, begin, startOfToken);
            }
            else {
                return new SQLToken(TokenType.SLASH, begin, startOfToken);
            }
        }
        else if (c == '"') {
            do {
                c = _next();
            } while (c != '"');
            return new SQLToken(TokenType.QUOTED_IDENTIFIER, begin, startOfToken);
        }
        else if (c == '\'') {
            do {
                c = _next();
                while (c == '\'' && _pos < _length && _peek() == '\'') {
                    c = _next();
                    c = _next();
                }
            } while (c != '\'');
            
            return new SQLToken(TokenType.STRING_LITERAL, begin, startOfToken);
        }
        else if (c == 'N' && _hasNext() && _peek() == '\'') {
            c = _next(); // Skip the N
            do {
                c = _next();
                while (c == '\'' && _pos < _length && _peek() == '\'') {
                    c = _next();
                    c = _next();
                }
            } while (c != '\'');
            
            return new SQLToken(TokenType.NSTRING_LITERAL, begin, startOfToken);
        }
        else if (c == '.') {
            if (_hasNext() && Character.isDigit(_peek())) {
                _readNumeric(c);
                
                // If the next character is a valid identifier than that means we 
                // have a word instead
                if (_hasNext() && _isValidIdentifier(_peek())) {
                    _readIdentifier(_in.charAt(_pos));
                    return new SQLToken(TokenType.WORD, begin, startOfToken);
                }
                
                return new SQLToken(TokenType.FLOAT_LITERAL, begin, startOfToken);
            }
            return new SQLToken(TokenType.DOT, begin, startOfToken);
        }
        else if (Character.isDigit(c)) {
            boolean isFloat = _readNumeric(c);
            
            // If the next character is a valid identifier than that means we 
            // have a word instead
            if (_hasNext() && _isValidIdentifier(_peek())) {
                _readIdentifier(_in.charAt(_pos));
                return new SQLToken(TokenType.WORD, begin, startOfToken);
            }
            
            if (isFloat) {
                return new SQLToken(TokenType.FLOAT_LITERAL, begin, startOfToken);
            }
            else {
                return new SQLToken(TokenType.INT_LITERAL, begin, startOfToken);
            }
        }
        else if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z'|| c == '_') {
            _readIdentifier(c);
            return new SQLToken(TokenType.WORD, begin, startOfToken);
        }
        else if (c == ':') {
            c = _next();
            if (c == ':') {
                _readIdentifier(_next());
                return new SQLToken(TokenType.WORD, begin, startOfToken);
            }
            else {
                _readIdentifier(c);
                return new SQLToken(TokenType.BIND_VARIABLE, begin, startOfToken);
            }
        }
        else {
            return new SQLToken(TokenType.OTHER, begin, startOfToken);
        }
    }

    /**
     * Return all remaining tokens in this statement.  Any tokens already
     * returned will not be processed again.
     *     
     * @return an array of SQLElement objects representing the individual SQL
     * tokens in the SQL statement.
     * 
     * @throws TokenizerException if there was a problem parsing the SQL statement.
     */
    public SQLElement[] getAllTokens() throws TokenizerException {
        List<SQLElement> tokens = new ArrayList<SQLElement>();
        SQLElement token;
        do {
            token = nextToken();
            tokens.add(token);
        } while (token.getType() != TokenType.END);
        
        return tokens.toArray(new SQLElement[tokens.size()]);
    }
    
    /**
     * Builds a string from a list of SQL elements (keywords, etc.).
     * @param elements a list of SQL tokens that make up a SQL statement.
     * @return the resulting SQL string.
     */
    public static String getString(SQLElement[] elements) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            sb.append(elements[i]);
        }
        return sb.toString();
    }
    
    //
    // Implementation
    //
    
    private boolean _readNumeric(char c) throws TokenizerException  {
        boolean sawDot = false;
        do {
            if (c == '.') {
                if (sawDot) {
                    throw new TokenizerException(
                            "Second period seen in Numeric literal");
                }
                else {
                    sawDot = true;
                }
            }
            
            if (!_hasNext()) break;
       
            c = _peek();
            if (c != '.' && !Character.isDigit(c)) break;

            _next();
        } while (true);
        return sawDot;
    }
    
    private boolean _isValidIdentifier(char c) {
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')||
                c == '_' || c == '$' || c == '#' || c == '.' ||
                Character.isDigit(c));
    }
    
    private void _readIdentifier(char c) throws TokenizerException {
        while (_hasNext() && _isValidIdentifier(_peek())) {
             _next();
         }
    }
    
    private void _skipWhitespace() throws TokenizerException {
        while (_pos < _length && Character.isWhitespace(_peek())) {
            _next();
        }
    }
    
    private char _next() throws TokenizerException {
        if (_pos >= _length) {
            throw new TokenizerException("unexpected end of text");
        }
        return _in.charAt(_pos++);
    }
    
    private char _peek() {
        return _in.charAt(_pos);
    }
    
    private boolean _hasNext() {
        return _pos < _length;
    }

    private int _pos;
    private final String _in;
    private final int _length;
}
