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

import junit.framework.TestCase;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.BindMode;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public abstract class TU_AbstractDialect extends TestCase {
    // @see junit.framework.TestCase#setUp()
    @Override
    protected final void setUp() throws Exception {
        super.setUp();
        
        _putComments = true;
        
        dialectSetUp();
    }
    
    protected void dialectSetUp() throws Exception {
        // Do nothing, classes can extend
    }
    
    protected abstract BaseDialect getDialect();
    
    //
    // Implementation
    //
    
    protected void _runTest(String orig, String expected) throws TranslationException {
        BindList args = new BindList();
        _runTest(orig, expected, args);
    }
    
    protected void _runTestNoBind(String orig, String expected) throws TranslationException {
        BindList args = new BindList();
        TranslationOptions options = new TranslationOptions(BindMode.NONE);
        _runTest(orig, expected, args, options);
    }
    
    protected void _runTest(String orig, String expected, BindList args) throws TranslationException {
        TranslationOptions options = new TranslationOptions();
        _runTest(orig, expected, args, options);
    }
    
    /**
     * This test will make sure the original and expected string match up after
     * parsing the tokens and using the specified bind list and translation
     * options.
     * 
     * The original string will be changed to have a comment before and after
     * every single token to ensure that comments in no way change the output
     * of the translated SQL.
     * @param orig
     * @param expected
     * @param args
     * @param options
     * @throws TranslationException
     */
    protected void _runTest(String orig, String expected, BindList args, TranslationOptions options)
            throws TranslationException {
        
        if (_putComments) {
            SQLTokenizer tokenizer = new SQLTokenizer(orig);
            SQLElement[] elements = tokenizer.getAllTokens();
            SQLElement[] elementsWithComments = new SQLElement[elements.length * 2 + 1];
            elementsWithComments[0] = new ReplacementElement(TokenType.COMMENT, "/*comment*/", "");
            for (int i = 0; i < elements.length; ++i) {
                elementsWithComments[i*2 + 1] = elements[i];
                elementsWithComments[i*2 + 2] = new ReplacementElement(TokenType.COMMENT, "/*comment*/", "");
            }
            orig = SQLTokenizer.getString(elementsWithComments);
        }
        
        BaseDialect translator = getDialect();
        String actual = translator.translateStatement(orig, args, options);
        _testSQLEquality(expected, actual);
    }
    
    /**
     * Verifies that the passed in SQL statements are logically equivalent.  Whitespace is ignored and individual
     * tokens are compared.
     * @param expected
     * @param translated
     * @throws TranslationException
     */
    protected void _testSQLEquality(String expected, String translated) throws TokenizerException {
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
        }
    }
    
    protected boolean _putComments = true;
}
