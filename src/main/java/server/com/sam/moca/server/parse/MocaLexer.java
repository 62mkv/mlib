/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20167
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

package com.sam.moca.server.parse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lexer for MOCA local syntax.  This splits input up into tokens to be used by the parser.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MocaLexer {
    
    public MocaLexer(CharSequence in) {
        _in = in;
        _length = in.length();
        _pos = 0;
        _line = 0;
    }
    
    /**
     * The actual tokens returned by the lexer. Tokens are snapshots into the original local syntax string.  We keep track of the beginning position, the ending position, and the leading whitespace.
     */
    public class MocaToken {
        public MocaTokenType getType() {
            return _type;
        }
        
        public String getValue() {
            return _in.subSequence(_beginToken, _end).toString();
        }
        
        public String toString() {
            return _in.subSequence(_beginWhitespace, _end).toString();
        }
        
        public String getLeadingWhitespace() {
            return _in.subSequence(_beginWhitespace, _beginToken).toString();
        }
        
        //
        // Implementation
        //
        private MocaToken(MocaTokenType type, int beginWhitespace, int beginToken) {
            _type = type;
            _beginToken = beginToken;
            _beginWhitespace = beginWhitespace;
            _end = _pos;
        }
        
        private final MocaTokenType _type;
        private final int _beginToken;
        private final int _beginWhitespace;
        private final int _end;
    }

    /**
     * A reference into the current lexer state.
     */
    public static class Reference {
        private Reference(int pos) {
            _refPos = pos;
        }
        private final int _refPos;
    }
    
    /**
     * Marks the current place in the input string. This can be used to get a portion
     * of the input string later.
     * @return
     */
    public Reference markPlace() {
        return new Reference(_currentToken._beginWhitespace);
    }
    
    /**
     * Gets a portion of the input as a string.  This can be used to get large, complex
     * parsed areas as text.
     * @param mark
     * @return
     */
    public String getTextSinceMark(Reference mark) {
        return _in.subSequence(mark._refPos, _currentToken._beginToken).toString();
    }
    
    /**
     * Return the current token under the lexer's thumb.
     * @return
     * @throws MocaLexException
     */
    public MocaToken current() {
        return _currentToken;
    }
    
    /**
     * Return the type of the current token of the lexer.  This is equivalent to calling
     * current().getType().
     * @return
     */
    public MocaTokenType tokenType() {
        return _currentToken._type;
    }

    /**
     * Gets the line on which the current token sits.
     * @return
     */
    public int getLine() {
        return _line;
    }
    
    /**
     * Gets the position within the line at which the current token sits.
     * @return
     */
    public int getLinePos() {
        return _linePos - (_pos - _currentToken._beginToken);
    }
    
    /**
     * Advance the token lexer and return the next token in our string.  This method removes
     * comments from the stream.
     * @return
     * @throws MocaLexException
     */
    public MocaToken next() throws MocaLexException {
        do {
            _currentToken = _nextToken();
        } while (_currentToken._type == MocaTokenType.COMMENT);

        return _currentToken;
    }

    /**
     * Return all remaining tokens in this statement.  Any tokens already
     * returned will not be processed again.  Note that this method does
     * not remove comments from the stream.
     *     
     * @return an array of MocaToken objects representing the individual 
     * tokens in the local syntax statement.
     * 
     * @throws MocaLexException if there was a problem parsing the SQL statement.
     */
    public MocaToken[] getAllTokens() throws MocaLexException {
        List<MocaToken> tokens = new ArrayList<MocaToken>();
        MocaToken token;
        do {
            token = _nextToken();
            tokens.add(token);
        } while (token.getType() != MocaTokenType.EOF);
        
        return tokens.toArray(new MocaToken[tokens.size()]);
    }
    
    /**
     * Builds a string from an array of command elements (keywords, etc.).
     * @param elements a list of MocaToken objects that make up a MOCA command.
     * @return the resulting command string.
     */
    public static String getString(MocaToken[] elements) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            buf.append(elements[i]);
        }
        return buf.toString();
    }

    /**
     * Builds a string from a collection of command elements (keywords, etc.).
     * @param elements a Collection object containing MocaToken objects that make up a MOCA command.
     * @return the resulting command string.
     */
    public static String getString(Collection<MocaToken> elements) {
        StringBuilder buf = new StringBuilder();
        for (MocaToken e : elements) {
            buf.append(e);
        }
        return buf.toString();
    }
    
    //
    // Implementation
    //
    
    /**
     * Return the next token.
     * @return a MocaToken object representing the next token in the SQL
     * statement. When the end of the statement is reached, a token of type
     * <code>MocaTokenType.END</code> is returned.  Subsequent calls, after the
     * end token is returned will also return an end token.
     * @throws MocaLexException if there was a problem parsing the SQL
     * statement.
     */
    private MocaToken _nextToken() throws MocaLexException {
        // Keep track of where we started
        _begin = _pos;
        _skipWhitespace();
        
        // If we're done, mark the end of the statement
        if (!_hasNext()) {
            return new MocaToken(MocaTokenType.EOF, _begin, _pos);
        }
        
        // We need to keep track of where this token's significant text began
        _startOfToken = _pos;
        
        // Read the first character
        char c = _nextChar();
        
        if (Character.isDigit(c)|| c == '.') {
            return _readNumeric(c);
        }
        else if (Character.isLetter(c) || c == '_') {
            return _readIdentifier();
        }
        else {
            switch (c) {
            case ':': 
                return new MocaToken(MocaTokenType.COLON, _begin, _startOfToken);
            case '\'':
            case '"':
            case '[':
                if (c == '[' && _hasNext() && _peekChar() == '[') {
                    return new MocaToken(_readJavaString(c), _begin, _startOfToken);
                }
                else {
                    return new MocaToken(_readSQLString(c), _begin, _startOfToken);
                }
            case ';':
                return new MocaToken(MocaTokenType.SEMICOLON, _begin, _startOfToken);
            case '|': 
                if (_hasNext() && _peekChar() == '|') {
                    // This is to go over the | character
                    _nextChar();
                    return new MocaToken(MocaTokenType.DOUBLEPIPE, _begin, _startOfToken);
                }
                else {
                    return new MocaToken(MocaTokenType.PIPE, _begin, _startOfToken);
                }
            case '(':
                return new MocaToken(MocaTokenType.OPEN_PAREN, _begin, _startOfToken);
            case ')':
                return new MocaToken(MocaTokenType.CLOSE_PAREN, _begin, _startOfToken);
            case '{':
                return new MocaToken(MocaTokenType.OPEN_BRACE, _begin, _startOfToken);
            case '}':
                return new MocaToken(MocaTokenType.CLOSE_BRACE, _begin, _startOfToken);
            case '^':
                return new MocaToken(MocaTokenType.CARET, _begin, _startOfToken);
            case '&':
                return new MocaToken(MocaTokenType.AMPERSAND, _begin, _startOfToken);
            case '@':
                return new MocaToken(MocaTokenType.ATSIGN, _begin, _startOfToken);
            case '?':
                return new MocaToken(MocaTokenType.QUESTION_MARK, _begin, _startOfToken);
            case '*':
                return new MocaToken(MocaTokenType.STAR, _begin, _startOfToken);
            case '+':
                return new MocaToken(MocaTokenType.PLUS, _begin, _startOfToken);
            case '-':
                return new MocaToken(MocaTokenType.MINUS, _begin, _startOfToken);
            case '/':
                if (_hasNext() && (_peekChar() == '*')) {
                    // This is to go over the * character
                    _nextChar();
                    do {
                        c = _nextChar();
                    } while (c != '*' || _peekChar() != '/');
                    // This is to go over the / character
                    _nextChar();
                    return new MocaToken(MocaTokenType.COMMENT, _begin,
                        _startOfToken);
                }
                else {
                    return new MocaToken(MocaTokenType.SLASH, _begin, _startOfToken);
                }
            case '%':
                return new MocaToken(MocaTokenType.PERCENT, _begin, _startOfToken);
            case '\\':
                return new MocaToken(MocaTokenType.BACKSLASH, _begin, _startOfToken);
            case ',':
                return new MocaToken(MocaTokenType.COMMA, _begin, _startOfToken);
            case '#':
                return new MocaToken(MocaTokenType.POUND, _begin, _startOfToken);
            case '=':
                return new MocaToken(MocaTokenType.EQ, _begin, _startOfToken);
            case '>':
                if (_hasNext() && _peekChar() == '=') {
                    // This is to go over the = character
                    _nextChar();
                    return new MocaToken(MocaTokenType.GE, _begin, _startOfToken);
                }
                else if (_hasNext() && _peekChar() == '>') {
                    // This is to go over the > character
                    _nextChar();
                    return new MocaToken(MocaTokenType.REDIR_INTO, _begin, _startOfToken);
                }
                else {
                    return new MocaToken(MocaTokenType.GT, _begin, _startOfToken);
                }
            case '<':
                if (_hasNext() && _peekChar() == '=') {
                    // This is to go over the = character
                    _nextChar();
                    return new MocaToken(MocaTokenType.LE, _begin, _startOfToken);
                }
                else if (_hasNext() && _peekChar() == '>') {
                    // This is to go over the > character
                    _nextChar();
                    return new MocaToken(MocaTokenType.NE, _begin, _startOfToken);
                }
                else {
                    return new MocaToken(MocaTokenType.LT, _begin, _startOfToken);
                }
            case '!':
                if (_hasNext() && _peekChar() == '=') {
                    // This is to go over the = character
                    _nextChar();
                    return new MocaToken(MocaTokenType.NE, _begin, _startOfToken);
                }
                else {
                    return new MocaToken(MocaTokenType.BANG, _begin, _startOfToken);
                }
            default:
                throw new MocaLexException(_line + 1, _linePos + 1, "Unrecognized identifier: " + c);
            }
        }
    }
    
    private MocaToken _readNumeric(char c) throws MocaLexException  {
        // If the first character is a plus or minus, skip over it.
        if (c == '+' || c == '-') {
            _nextChar();
        }
        
        // As long as the next character will be a digit, we should include it.
        while (_hasNext() && Character.isDigit(_peekChar())) {
            _nextChar();
        }
        
        // If the next character is a decimal point, continue.
        if (_hasNext() && _peekChar() == '.' ) {
            _nextChar();
        }
        
        // Now we have numbers after the decimal point.       
        while (_hasNext() && Character.isDigit(_peekChar())) {
            _nextChar();
        }
        
        // Scientific notation -- ##.##e[+-]###
        if (_hasNext() && (_peekChar() == 'e' || _peekChar() == 'E')) {
            // We expect another number if we see the exponential notation
            _nextChar();
            
            if (_peekChar() == '+' || _peekChar() == '-') {
                _nextChar();
            }
            
            while (_hasNext() && Character.isDigit(_peekChar())) {
                _nextChar();
            }
        }
        else if (_hasNext() && (Character.isLetter(_peekChar()) || _peekChar() == '_')) {
            return _readIdentifier();
        }
        return new MocaToken(MocaTokenType.NUMBER, _begin, _startOfToken);
    }
    
    private boolean _isValidIdentifier(char c) {
        return ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')||
                c == '_' || c == '$' || c == '.' ||
                Character.isDigit(c));
    }
    
    private MocaToken _readIdentifier() throws MocaLexException {
        return (_readIdentifierAs(null));
    }
    
    private MocaToken _readIdentifierAs(MocaTokenType type) throws MocaLexException {
        while (_hasNext() && _isValidIdentifier(_peekChar())) {
             _nextChar();
        }
        
        String word = _in.subSequence(_startOfToken, _pos).toString();
        MocaTokenType wordType;
        if (type != null) {
            wordType = type;
        }
        else {
            wordType = _RESERVED.get(word.toLowerCase());
            if (wordType == null) {
                wordType = MocaTokenType.VARWORD;
            }
        }
        
        return new MocaToken(wordType, _begin, _startOfToken);
    }
    
    private MocaTokenType _readSQLString(char c) throws MocaLexException {
        
        if (c == '\'' || c == '"') {
            char lookfor = c;
            
            do {
                c = _nextChar();
                
                while (c == lookfor && _hasNext() && _peekChar() == lookfor) {
                    _nextChar();
                    c = _nextChar();
                }
            } while (c != lookfor);
            
            if (lookfor == '\'') {
                return MocaTokenType.SINGLE_STRING;
            }
            else {
                return MocaTokenType.DOUBLE_STRING;
            }
        }
        else {
            boolean profileHint = false;
            c = _nextChar();
            while (c != ']') {
                if (c == '\'' || c == '"' || c == '[') {
                    _readSQLString(c);
                }
                else if (c == '/' && _peekChar() == '*') {
                    // This is to go over the * character
                    _nextChar();
                   
                    // Here we're going to check for a profile hint.
                    if (_peekChar() == '#') {
                        // This is to go over the # character
                        _nextChar();
                        profileHint = profileHint ? profileHint : containsSQLProfileHint();
                    }

                    do {
                        c = _nextChar();
                    } while (c != '*' || _peekChar() != '/');
                    // This is to go over the / character
                    _nextChar();
                }
                
                c = _nextChar();
            } 

            if (profileHint) {
                return MocaTokenType.BRACKET_STRING_WITH_HINT;
            }
            else {
                return MocaTokenType.BRACKET_STRING;
            }
        }
    }
    
    private boolean containsSQLProfileHint() throws MocaLexException {

        char c = ' ';
        // skip any whitespace
        while (_peekChar() == ' ') {
            _nextChar();
        }

        // See if the SQL hint is our profile hint.
        for (int i = 0; i < _PROFILE.length; i++) {
            c = _nextChar();
            // If the next character is not in the profile hint
            // or we have the asterisk coming up.
            if (c != _PROFILE[i] || _peekChar() == '*') {
                return false;
            }
        }

        // skip any more whitespace
        while (_peekChar() == ' ') {
            _nextChar();
        }

        // Check that it's not another hint, but the
        // one we're looking for by checking for the equals
        if (_peekChar() != '=') {
            return false;
        }

        return true;
    }
    
    private MocaTokenType _readJavaString(char c) throws MocaLexException {
        
        if (c == '\'' || c == '"') {
            char lookfor = c;
            
            do {
                c = _nextChar();
                
                while (c == '\\') {
                    _nextChar();
                    c = _nextChar();
                }
            } while (c != lookfor);
            
            if (lookfor == '\'') {
                return MocaTokenType.SINGLE_STRING;
            }
            else {
                return MocaTokenType.DOUBLE_STRING;
            }
        }
        else {
            c = _nextChar();
            while (c != ']') {
                if (c == '\'' || c == '"' || c == '[') {
                    _readJavaString(c);
                }
                else if (c == '/' && _peekChar() == '/') {
                    // This is to go over the / character
                    _nextChar();
                    do {
                        c = _nextChar();
                    } while (c != '\n');
                }
                else if (c == '/' && _peekChar() == '*') {
                    // This is to go over the * character
                    _nextChar();
                    do {
                        c = _nextChar();
                    } while (c != '*' || _peekChar() != '/');
                    // This is to go over the / character
                    _nextChar();
                }
                
                c = _nextChar();
            } 
            return MocaTokenType.BRACKET_STRING;
        }
    }
    
    private void _skipWhitespace() throws MocaLexException {
        while (_pos < _length && Character.isWhitespace(_peekChar())) {
            _nextChar();
        }
    }
    
    private char _nextChar() throws MocaLexException {
        if (_pos >= _length) {
            throw new MocaLexException(_line + 1, _linePos + 1, "Unexpected end of command text");
        }
        _linePos++;
        char next = _in.charAt(_pos++);

        if (next == '\n') {
            _line++;
            _linePos = 0;
        }
        
        return next;
    }
    
    private char _peekChar() {
        return _in.charAt(_pos);
    }
    
    private boolean _hasNext() {
        return _pos < _length;
    }

    //
    // Implementation
    //
    private final CharSequence _in;
    private final int _length;
    private int _pos;
    private int _line;
    private int _linePos;
    private int _begin;
    private int _startOfToken;
    private MocaToken _currentToken;
    
    private final static Map<String, MocaTokenType> _RESERVED = new HashMap<String, MocaTokenType>();
    private final static char[] _PROFILE = new char[] { 'p', 'r', 'o', 'f', 'i','l', 'e'};
    static {
        _RESERVED.put("if", MocaTokenType.IF);
        _RESERVED.put("else", MocaTokenType.ELSE);
        _RESERVED.put("where", MocaTokenType.WHERE);
        _RESERVED.put("remote", MocaTokenType.REMOTE);
        _RESERVED.put("parallel", MocaTokenType.PARALLEL);
        _RESERVED.put("inparallel", MocaTokenType.INPARALLEL);
        _RESERVED.put("and", MocaTokenType.AND);
        _RESERVED.put("or", MocaTokenType.OR);
        _RESERVED.put("not", MocaTokenType.NOT);
        _RESERVED.put("is", MocaTokenType.IS);
        _RESERVED.put("null", MocaTokenType.NULL_TOKEN);
        _RESERVED.put("like", MocaTokenType.LIKE);
        _RESERVED.put("try", MocaTokenType.TRY);
        _RESERVED.put("catch", MocaTokenType.CATCH);
        _RESERVED.put("finally", MocaTokenType.FINALLY);
    }
}
