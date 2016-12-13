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

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Unit tests for SQLTokenizer
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_SQLTokenizer extends TestCase {
    public void testEmpty() throws TokenizerException {
        _runTest(
                "",
                new TokenType[] {
                    TokenType.END
                },
                new String[] {
                        ""
                }
        );
    }
    
    public void testWhitespace() throws TokenizerException {
        _runTest(
                " \t\r\n   ",
                new TokenType[] {
                    TokenType.END
                },
                new String[] {
                    ""
                }
        );
    }
    
    public void testSimpleWhereClause() throws TokenizerException {
        _runTest(
                " where x <= 10",
                new TokenType[] {
                    TokenType.WORD, TokenType.WORD, TokenType.LE,
                    TokenType.INT_LITERAL, TokenType.END
                },
                new String[] {
                    "where", "x", "<=", "10", ""
                }
        );
    }
    
    public void testSelectWithPeriods() throws TokenizerException {
        _runTest(
                "select * from foo.bar xyz123 where xyz123.x = 'test'",
                new TokenType[] {
                    TokenType.WORD, TokenType.STAR, TokenType.WORD,
                    TokenType.WORD, 
                    TokenType.WORD, TokenType.WORD, TokenType.WORD,
                    TokenType.EQ,
                    TokenType.STRING_LITERAL, TokenType.END
                },
                new String[] {
                    "select", "*", "from", "foo.bar", "xyz123",
                    "where", "xyz123.x", "=", "'test'", ""
                }
        );
    }
    
    public void testUnusualIdentifiers() throws TokenizerException {
        _runTest(
                "select this#is#legal from this$too, _blah",
                new TokenType[] {
                    TokenType.WORD, TokenType.WORD, TokenType.WORD, TokenType.WORD, TokenType.COMMA, TokenType.WORD,
                    TokenType.END
                },
                new String[] {
                    "select", "this#is#legal", "from", "this$too", ",", "_blah", ""
                }
        );
    }
    
    public void testComplexExpression() throws TokenizerException {
        _runTest(
                "select * from foo where x = 10*2-3.4/-2.123412",
                new TokenType[] {
                    TokenType.WORD, TokenType.STAR, TokenType.WORD,
                    TokenType.WORD, TokenType.WORD, TokenType.WORD,
                    TokenType.EQ, TokenType.INT_LITERAL,
                    TokenType.STAR, TokenType.INT_LITERAL,
                    TokenType.MINUS, TokenType.FLOAT_LITERAL,
                    TokenType.SLASH, TokenType.MINUS, 
                    TokenType.FLOAT_LITERAL, TokenType.END
                },
                new String[] {
                    "select", "*", "from", "foo", "where", "x", "=", "10",
                    "*", "2", "-", "3.4", "/", "-", "2.123412", ""
                }
        );
    }
    
    public void testQuotedStrings() throws TokenizerException {
        _runTest(
                "insert into foo(bar, baz, flan)VALUES('string', '''quotedstring''', 3002);",
                new TokenType[] {
                    TokenType.WORD, TokenType.WORD, TokenType.WORD,
                    TokenType.LEFTPAREN, TokenType.WORD, TokenType.COMMA,
                    TokenType.WORD, TokenType.COMMA, TokenType.WORD,
                    TokenType.RIGHTPAREN, TokenType.WORD,
                    TokenType.LEFTPAREN, TokenType.STRING_LITERAL, TokenType.COMMA,
                    TokenType.STRING_LITERAL, TokenType.COMMA, TokenType.INT_LITERAL,
                    TokenType.RIGHTPAREN, TokenType.SEMICOLON,
                    TokenType.END
                },
                new String[] {
                    "insert", "into", "foo", "(", "bar", ",", "baz", ",", "flan",
                    ")", "VALUES", "(", "'string'", ",", "'''quotedstring'''",
                    ",", "3002", ")", ";", ""
                }
        );
    }
    
    public void testNoconvComment() throws TokenizerException {
        _runTest(
                "/*NOCONV*/" +
                "update foo set bar = -1, baz = 23, flan='fdsaf' " +
                "where flan is not null and upper(flan) like 'F%'",
                new TokenType[] {
                        TokenType.COMMENT,
                        TokenType.WORD, TokenType.WORD, TokenType.WORD, TokenType.WORD,
                        TokenType.EQ, TokenType.MINUS, TokenType.INT_LITERAL,
                        TokenType.COMMA, TokenType.WORD, TokenType.EQ,
                        TokenType.INT_LITERAL, TokenType.COMMA, TokenType.WORD,
                        TokenType.EQ, TokenType.STRING_LITERAL, TokenType.WORD,
                        TokenType.WORD, TokenType.WORD, TokenType.WORD, TokenType.WORD,
                        TokenType.WORD, TokenType.WORD, TokenType.LEFTPAREN,
                        TokenType.WORD, TokenType.RIGHTPAREN, TokenType.WORD,
                        TokenType.STRING_LITERAL, TokenType.END                },
                new String[] {
                        "/*NOCONV*/", 
                        "update", "foo", "set", "bar", "=", "-", "1", ",",
                        "baz", "=", "23", ",", "flan", "=", "'fdsaf'",
                        "where", "flan", "is", "not", "null", "and",
                        "upper", "(", "flan", ")", "like", "'F%'", ""
                }
        );
    }

    public void testBindVariable() throws TokenizerException {
        _runTest(
                "select :p001 from dual ",
                new TokenType[] {
                        TokenType.WORD, TokenType.BIND_VARIABLE,
                        TokenType.WORD, TokenType.WORD, TokenType.END
                },
                new String[] {
                        "select", ":p001", "from", "dual", ""
                }
        );
    }
    
    public void testTwoBindVariablesNoSpace() throws TokenizerException {
        _runTest(
                "BEGIN SELECT :p001 into :foo:foo_i from \"dual\"; END",
                new TokenType[] {
                        TokenType.WORD, TokenType.WORD, 
                        TokenType.BIND_VARIABLE, TokenType.WORD, 
                        TokenType.BIND_VARIABLE, TokenType.BIND_VARIABLE,
                        TokenType.WORD, TokenType.QUOTED_IDENTIFIER, TokenType.SEMICOLON,
                        TokenType.WORD, TokenType.END
                },
                new String[] {
                        "BEGIN", "SELECT", ":p001", "into", ":foo", ":foo_i", 
                        "from", "\"dual\"", ";", "END", ""
                }
        );
    }
    
    public void testSqlServerInternalFunctionSyntax() throws TokenizerException {
        _runTest(
                "select count(*) cnt " +
                "  from ::fn_listextendedproperty('MS_Description', " +
                "                                 'user', " +
                "                                 'dbo', " +
                "                                 'table', " +
                "                                 'foo', " +
                "                                 NULL, " +
                "                                 NULL)",
                new TokenType[] {
                        TokenType.WORD,
                        TokenType.WORD, TokenType.LEFTPAREN, TokenType.STAR,
                        TokenType.RIGHTPAREN, TokenType.WORD, TokenType.WORD,
                        TokenType.WORD,
                        TokenType.LEFTPAREN,
                        TokenType.STRING_LITERAL, TokenType.COMMA,
                        TokenType.STRING_LITERAL, TokenType.COMMA,
                        TokenType.STRING_LITERAL, TokenType.COMMA,
                        TokenType.STRING_LITERAL, TokenType.COMMA,
                        TokenType.STRING_LITERAL, TokenType.COMMA,
                        TokenType.WORD, TokenType.COMMA,
                        TokenType.WORD, TokenType.RIGHTPAREN,
                        TokenType.END
                },
                new String[] {
                        "select", 
                        "count", "(", "*", ")", "cnt", "from", 
                        "::fn_listextendedproperty",
                        "(",
                        "'MS_Description'", ",", "'user'", ",", "'dbo'", ",",
                        "'table'", ",", "'foo'", ",", "NULL", ",", "NULL", ")",
                        ""
                }
        );
    }
    
    public void testCommentWithEmbeddedSlash() throws TokenizerException {
        _runTest(
                "select * " +
                "  from foo " +
                "/* comment/hint */ " +
                " where x = 'abc' ",
                new TokenType[] {
                        TokenType.WORD, TokenType.STAR,
                        TokenType.WORD, TokenType.WORD,
                        TokenType.COMMENT,
                        TokenType.WORD, TokenType.WORD, TokenType.EQ, TokenType.STRING_LITERAL,
                        TokenType.END
                },
                new String[] {
                        "select", "*",
                        "from", "foo",
                        "/* comment/hint */",
                        "where", "x", "=", "'abc'",
                        ""
                }
        );
    }
    
    public void testLineComment() throws TokenizerException {
        _runTest(
                "select * " +
                "  from foo " +
                " where x = 'abc'\n" +
                "-- and y = 'tuv'\n" +
                "   and z = 'xyz'\n",
                new TokenType[] {
                        TokenType.WORD, TokenType.STAR,
                        TokenType.WORD, TokenType.WORD,
                        TokenType.WORD, TokenType.WORD, TokenType.EQ, TokenType.STRING_LITERAL,
                        TokenType.COMMENT,
                        TokenType.WORD, TokenType.WORD, TokenType.EQ, TokenType.STRING_LITERAL,
                        TokenType.END
                },
                new String[] {
                        "select", "*",
                        "from", "foo",
                        "where", "x", "=", "'abc'",
                        "-- and y = 'tuv'",
                        "and", "z", "=", "'xyz'",
                        ""
                }
        );
    }
    
    public void testLineCommentAtEnd() throws TokenizerException {
        _runTest(
                "select * " +
                "  from foo " +
                "-- where x = 'abc'",
                new TokenType[] {
                        TokenType.WORD, TokenType.STAR,
                        TokenType.WORD, TokenType.WORD,
                        TokenType.COMMENT,
                        TokenType.END
                },
                new String[] {
                        "select", "*",
                        "from", "foo",
                        "-- where x = 'abc'",
                        ""
                }
        );
    }
    
    public void testWordStartsWithInt() throws TokenizerException {
        _runTest(
                "select * " +
                "  from foo " +
                " where 3pl = 'abc'",
                new TokenType[] {
                        TokenType.WORD, TokenType.STAR,
                        TokenType.WORD, TokenType.WORD,
                        TokenType.WORD, TokenType.WORD,
                        TokenType.EQ, TokenType.STRING_LITERAL,
                        TokenType.END
                },
                new String[] {
                        "select", "*",
                        "from", "foo",
                        "where", "3pl",
                        "=", "'abc'",
                        ""
                }
        );
    }
    
    public void testWordStartsWithDot() throws TokenizerException {
        _runTest(
                "select * " +
                "  from foo " +
                " where .3_pl = 'abc'",
                new TokenType[] {
                        TokenType.WORD, TokenType.STAR,
                        TokenType.WORD, TokenType.WORD,
                        TokenType.WORD, TokenType.WORD,
                        TokenType.EQ, TokenType.STRING_LITERAL,
                        TokenType.END
                },
                new String[] {
                        "select", "*",
                        "from", "foo",
                        "where", ".3_pl",
                        "=", "'abc'",
                        ""
                }
        );
    }
    
    public void testOracleTableCreationWithTablespace() throws TokenizerException {
        _runTest(
                "create table car_acct_gen_num " +
                "(" +
                "    acct_num            varchar2(40 CHAR) not null," +
                "    ins_dt              date," +
                ")" +
                "tablespace TRACS3_DATA STORAGE (INITIAL 1024K NEXT 1024K PCTINCREASE 0)", 

                new TokenType[] {
                        TokenType.WORD, TokenType.WORD,
                        TokenType.WORD, TokenType.LEFTPAREN,
                        TokenType.WORD, TokenType.WORD,
                        TokenType.LEFTPAREN, TokenType.INT_LITERAL,
                        TokenType.WORD, TokenType.RIGHTPAREN,
                        TokenType.WORD, TokenType.WORD,
                        TokenType.COMMA, TokenType.WORD,
                        TokenType.WORD, TokenType.COMMA,
                        TokenType.RIGHTPAREN, TokenType.WORD,
                        TokenType.WORD, TokenType.WORD,
                        TokenType.LEFTPAREN, TokenType.WORD,
                        TokenType.WORD, TokenType.WORD,
                        TokenType.WORD, TokenType.WORD,
                        TokenType.INT_LITERAL, TokenType.RIGHTPAREN,
                        TokenType.END
                },
                new String[] {
                        "create", "table",
                        "car_acct_gen_num", "(",
                        "acct_num", "varchar2",
                        "(", "40",
                        "CHAR", ")",
                        "not", "null",
                        ",", "ins_dt",
                        "date", ",",
                        ")", "tablespace",
                        "TRACS3_DATA", "STORAGE", 
                        "(", "INITIAL",
                        "1024K", "NEXT",
                        "1024K", "PCTINCREASE",
                        "0", ")",
                        ""
                }
        );
    }
    
    public void testBadlyFormedSQL() throws TokenizerException {
        // Unmatched quotes
        String sql = "select * from foo where y = 'open\" and z = 'closed'"; 
        SQLTokenizer tokenizer = new SQLTokenizer(sql);
        try {
            SQLElement[] elements = tokenizer.getAllTokens();
            fail("Expected Tokenizer Exception, got " + Arrays.asList(elements));
        }
        catch (TokenizerException e) {
            // Normal
        }
    }
    
    public void testBadlyFormedNumber() throws TokenizerException {
        // Unmatched quotes
        String sql = "select * from foo where y = 2.2.2"; 
        SQLTokenizer tokenizer = new SQLTokenizer(sql);
        try {
            SQLElement[] elements = tokenizer.getAllTokens();
            fail("Expected Tokenizer Exception, got " + Arrays.asList(elements));
        }
        catch (TokenizerException e) {
            // Normal
        }
    }
    
    /**
     * Test class specific to this unit test.
     */
    
    private void _runTest(String sql, TokenType[] tokens, String[] tokenText) 
            throws TokenizerException {
        SQLTokenizer tokenizer = new SQLTokenizer(sql);
        for (int t = 0; t < tokens.length; t++) {
            SQLElement token = tokenizer.nextToken();
            assertEquals(tokens[t], token.getType());
            assertEquals(tokenText[t], token.getValue());
        }
        
        SQLElement endToken = tokenizer.nextToken();
        assertEquals(TokenType.END, endToken.getType());
    }
}
