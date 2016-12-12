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

import junit.framework.TestCase;

import com.redprairie.moca.MocaType;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.SQLTokenizer;
import com.redprairie.moca.server.db.translate.TokenType;
import com.redprairie.moca.server.db.translate.TokenizerException;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.server.db.translate.TranslationOptions;

/**
 * Abstract unit test class for TranslationFilters.  
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public abstract class TU_AbstractFilterTest extends TestCase {
    
    public void testTrivialQueries() {
        _testTranslation("", "");
        _testTranslation(" \t\r\n   "," \t\r\n   ");
        _testTranslation("select * from foo", "select * from foo");
    }
    
    //
    // Subclass interface
    //
    
    /**
     * Runs a test case, given an original string, and an expected
     * result string.  <code>sql</code> is tokenized and the tokens are passed
     * through the filter returned from the _getFilter() method.  Then, the
     * tokens are turned back into a string and compared with 
     * <code>expected</code>.
     * 
     * Default bind variables are furnished for this test.  See _getDefaultArgs
     * for more details.
     * 
     * @param sql
     *          The original SQL statement to be processed.
     * @param expected
     *          The expected result of the translation process. 
     * @param options
     *          The translation options to be passed in (and out) of the translation.
     */
    protected void _testTranslationWithOptions(String sql, String expected, TranslationOptions options) {
        _testTranslation(sql, expected, _getDefaultArgs(), options);
    }
    
    /**
     * Runs a test case, given an original string, and an expected
     * result string.  <code>sql</code> is tokenized and the tokens are passed
     * through the filter returned from the _getFilter() method.  Then, the
     * tokens are turned back into a string and compared with 
     * <code>expected</code>.
     * 
     * Default bind variables are furnished for this test.  See _getDefaultArgs
     * for more details.
     * 
     * @param sql
     *          The original SQL statement to be processed.
     * @param expected
     *          The expected result of the translation process.              
     */
    protected void _testTranslation(String sql, String expected) {
        _testTranslation(sql, expected, _getDefaultArgs());
    }
    
    /**
     * Runs a test case, given an original string, and an expected
     * result string.  <code>sql</code> is tokenized and the tokens are passed
     * through the filter returned from the _getFilter() method.  Then, the
     * tokens are turned back into a string and compared with 
     * <code>expected</code>.
     * 
     * @param sql
     *          The original SQL statement to be processed.
     * @param expected
     *          The expected result of the translation process.
     * @param args
     *          The arguments needed to perform this test.
     */
    protected void _testTranslation(String sql, String expected,
                                    BindList args) {
        _testTranslation(sql, expected, args, new TranslationOptions());
    }
    
    protected void _testTranslation(String sql, String expected,
                                    BindList args, TranslationOptions options) {
        try {
            _testSQLEquality(expected, _performTranslation(sql, args, options), false);
        }
        catch (TokenizerException e) {
            fail("tokenizer failure " + e);
        }
        catch (TranslationException e) {
            fail("translation failure " + e);
        }
    }
    
    protected void _testTranslationWithWhitespace(String sql, String expected) {
        try {
            _testSQLEquality(expected, _performTranslation(sql, _getDefaultArgs(), new TranslationOptions()), true);
        }
        catch (TokenizerException e) {
            fail("tokenizer failure " + e);
        }
        catch (TranslationException e) {
            fail("translation failure " + e);
        }
    }
    
    /**
     * Verifies that the passed in SQL statements are logically equivalent.  Whitespace is ignored and individual
     * tokens are compared.
     * @param expected
     * @param translated
     * @param checkWhitespace
     * @throws TranslationException
     */
    protected void _testSQLEquality(String expected, String translated, boolean checkWhitespace) throws TokenizerException {
        SQLElement[] expectedTokens = new SQLTokenizer(expected).getAllTokens();
        SQLElement[] translatedTokens = new SQLTokenizer(translated).getAllTokens();
        
        for (int i = 0; i < expectedTokens.length; i++) {
            assertEquals("at token " + i + ": [" + expectedTokens[i] + "] != [" + translatedTokens[i] + "]" ,
                expectedTokens[i].getType(), translatedTokens[i].getType());
            if (translatedTokens[i].getType() == TokenType.WORD) {
                assertEquals("at token " + i + ": [" + expectedTokens[i] + "] != [" + translatedTokens[i] + "]" ,
                    expectedTokens[i].getValue().toLowerCase(), translatedTokens[i].getValue().toLowerCase());
            }
            else {
                assertEquals("at token " + i + ": [" + expectedTokens[i] + "] != [" + translatedTokens[i] + "]" ,
                    expectedTokens[i].getValue(), translatedTokens[i].getValue());
            }
            if (checkWhitespace) {
                assertEquals("at token " + i + ": [" + expectedTokens[i] + "] != [" + translatedTokens[i] + "]" ,
                    expectedTokens[i].getLeadingWhitespace(), translatedTokens[i].getLeadingWhitespace());
            }
        }
    }

    /**
     * Perform a SQL translation operation, and return the result as a 
     * string.
     * @param sql the incoming SQL statement
     * @param args arguments to be used for this operation
     * @return a translated SQL string.
     * 
     * @throws TokenizerException if an error occurred parsint the original
     * statement into SQL elements.
     * @throws TranslationException if an error occurred during the filter
     * operation.
     * 
     */
    protected String _performTranslation(String sql, BindList args)
    throws TranslationException {
        return _performTranslation(sql, args, new TranslationOptions());
    }
    
    protected String _performTranslation(String sql, BindList args, TranslationOptions options)
            throws TranslationException {
        SQLTokenizer tokenizer = new SQLTokenizer(sql);
        SQLElement[] tokens = tokenizer.getAllTokens();
        TranslationFilter filter = _getFilter();
        SQLElement[] result = filter.filter(tokens, args, options);
        return SQLTokenizer.getString(result);
    }

    /**
     * Provide default bind variables for all tests.  These include
     * three integers (i001=1, i002=2, i003=3), three doubles (f001=1.1,
     * f002=2.2, f003=3.3) and three strings (c001="str001", c002="str002",
     * c003="str003").

     * @return a BindList containing the default set of bind variables.
     */
    protected BindList _getDefaultArgs() {
        BindList args = new BindList();
        args.add("i001", MocaType.INTEGER, Integer.valueOf(1));
        args.add("i002", MocaType.INTEGER, Integer.valueOf(2));
        args.add("i003", MocaType.INTEGER, Integer.valueOf(3));
        args.add("f001", MocaType.DOUBLE, Double.valueOf(1.1));
        args.add("f002", MocaType.DOUBLE, Double.valueOf(2.2));
        args.add("f003", MocaType.DOUBLE, Double.valueOf(3.3));
        args.add("c001", MocaType.STRING, "str001");
        args.add("c002", MocaType.STRING, "str002");
        args.add("c003", MocaType.STRING, "str003");
        
        return args;
    }
    
    /**
     * This method should be overridded in subclasses to provide the instance
     * of the filter to use for running tests.
     * @return an instance of TranslationFilter to perform all tests.  This
     * method can not return null.
     */
    protected abstract TranslationFilter _getFilter();
}
