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

package com.sam.moca.applications.preprocessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.sam.moca.server.db.DBType;
import com.sam.moca.util.AbstractMocaTestCase;

/**
 * This class is used to test the Moca C Preprocessor
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_MocaCPreProcessor extends AbstractMocaTestCase {

    public void testCommentsFile() throws IOException {
        LinkedList<String> expectedStrings = new LinkedList<String>();
        
        expectedStrings.add("mset command on"); 
        expectedStrings.add("publish data where foo = 5");
        expectedStrings.add("/");
        expectedStrings.add("mset command off");
        
        ensureFileAndOutputAreCorrect("Comments.sql", expectedStrings);
    }
    
    public void testNestedMacroWithOtherText() throws IOException {
        LinkedList<String> expectedStrings = new LinkedList<String>();
        
        expectedStrings.add("create table foo"); 
        expectedStrings.add("(");
        expectedStrings.add("    foobar 10 * 4");
        expectedStrings.add(")");
        
        ensureFileAndOutputAreCorrect("NestedMacroWithOtherText.sql", 
                expectedStrings);
    }
    
    public void testBuiltInGroovyFunction() throws IOException {
        LinkedList<String> expectedStrings = new LinkedList<String>();
        
        expectedStrings.add("create table foo"); 
        expectedStrings.add("(");
        expectedStrings.add("    foobar nvarchar(30) ");
        expectedStrings.add(")");
        
        ensureFileAndOutputAreCorrect("BuiltInGroovyFunction.sql", 
                expectedStrings);
    }
    
    public void testBuiltInLocalSyntaxFunction() throws IOException {
        LinkedList<String> expectedStrings = new LinkedList<String>();
        
        expectedStrings.add("create table foo"); 
        expectedStrings.add("(");
        expectedStrings.add("    foobar nvarchar(GOOD) ");
        expectedStrings.add(")");
        
        ensureFileAndOutputAreCorrect("BuiltInLocalSyntaxFunction.sql", 
                expectedStrings);
    }
    
    public void testEmptyDefineInFunction() throws IOException {
        LinkedList<String> expectedStrings = new LinkedList<String>();
        
        expectedStrings.add("create table foo"); 
        expectedStrings.add("(");
        expectedStrings.add("    foo ( 5 -  ) ");
        expectedStrings.add(")");
        
        ensureFileAndOutputAreCorrect("EmptyDefineInFunction.sql", 
                expectedStrings);
    }
    
    public void testFunctionAndDefineSameLine() throws IOException {
        LinkedList<String> expectedStrings = new LinkedList<String>();
        
        expectedStrings.add("create table foo"); 
        expectedStrings.add("(");
        expectedStrings.add("    test1 foo test2");
        expectedStrings.add(")");
        
        ensureFileAndOutputAreCorrect("FunctionAndDefineSameLine.sql",
                expectedStrings);
    }
    
    public void testQuoteFunctionReplace() throws IOException {
        LinkedList<String> expectedStrings = new LinkedList<String>();
        
        expectedStrings.add("if ('clob' = @datatype)"); 
        
        ensureFileAndOutputAreCorrect("QuoteFunctionReplace.sql",
                expectedStrings);
    }
    
    /**
     * This test will make sure that the array is equal to the output from
     * the preprocessor.  It will empty the string if executed correctly.
     * @param file
     * @param expectedStrings
     * @throws IOException
     */
    private void ensureFileAndOutputAreCorrect(String file, 
            LinkedList<String> expectedStrings) throws IOException {
        // Read in the resource file
        URL url = getClass().getResource("test/" + file);
        List<String> files = Arrays.asList(url.getPath());
        
        // Now we actually preprocess the file
        Reader reader = _preprocessor.process(files, DBType.NONE);

        BufferedReader bufReader = null;
        bufReader = new BufferedReader(reader);
        
        try {
            String input;
            
            // Now we loop through the buffer and check each line
            while ((input = bufReader.readLine()) != null) {
                
                String expected = expectedStrings.remove();
                
                assertEquals(expected, input);
            }
            
            // The expected strings should be exhausted
            assertTrue("We didn't get back enough lines from preprocessor: " + 
                    expectedStrings, expectedStrings.isEmpty());
        }
        finally {
            bufReader.close();
        }
    }
    
    
    
    private final MocaCPreProcessor _preprocessor = new MocaCPreProcessor(".");
}
