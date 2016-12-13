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

package com.redprairie.moca.server.parse;

public enum MocaTokenType {
    EOF,
    VARWORD,
    NUMBER,
    DOUBLE_STRING,
    SINGLE_STRING,
    BRACKET_STRING,
    BRACKET_STRING_WITH_HINT,
    SEMICOLON,
    COLON,
    OPEN_PAREN, CLOSE_PAREN,
    OPEN_BRACE, CLOSE_BRACE,
    PIPE, DOUBLEPIPE,
    EQ, NE, LT, GT, LE, GE, LIKE,
    AND, OR, NOT,
    IS,
    NULL_TOKEN,
    BANG,
    ATSIGN, STAR, PLUS, PERCENT,
    IF,
    ELSE,
    WHERE,
    REMOTE,
    PARALLEL,
    INPARALLEL,
    TRY,
    CATCH,
    FINALLY,
    CARET,
    AMPERSAND,
    REDIR_INTO,
    MINUS, SLASH, BACKSLASH, COMMA, POUND,
    QUESTION_MARK,
    COMMENT,
    ERROR
}