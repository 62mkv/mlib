/*
 *  $URL$
 *  $Revision$
 *  $Author: sprakash$
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

package com.redprairie.moca.components.base;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.RequiredArgumentException;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.util.AbstractMocaTestCase;
import com.redprairie.moca.util.test.FileCreationTestUtility;

/**
 * Unit test for CoreService
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author sprakash
 * @version $Revision$
 */
public class TU_CoreService extends AbstractMocaTestCase {

    private static final String DUMP_MODE_CSV = "CSV";
    private static final String DUMP_MODE_XML = "XML";
    private static final String DUMP_OVERWRITE = "F";
    private static final String DUMP_APPEND = "T";

    private File tmpFile = null;

    @Override
    protected void mocaSetUp() throws IOException {
        tmpFile = null;
    }

    @Override
    protected void mocaTearDown() {
        if (tmpFile != null && tmpFile.exists()) {
            if (!tmpFile.delete()) {
                System.out.println(
                    "ERROR removing file " + tmpFile.getAbsolutePath());
            }
        }
    }

    /**
     * Get dump data command for tests
     * 
     * @param dumpMode CSV/XML
     * @param dumpAppend T/F
     * @param fileName
     * @return
     */
    private String getDumpDataCommand(String dumpMode, String dumpAppend,
                                      String fileName) {
        return "dump data where dump_mode = '" + dumpMode + "'"
                + " and dump_append = '" + dumpAppend + "'"
                + " and file_name = '" + fileName + "'"
                + " and dump_command = 'format data where format_mode = \""
                + dumpMode + "\" "
                + " and command = \"publish data where column1 = ''value1'' and column2 = ''value2''\"'";
    }

    public void testDumpDataCsvOverwrite() throws MocaException, IOException {

        tmpFile = File.createTempFile(TU_CoreService.class.getSimpleName(),
            ".tmp");

        _moca.executeCommand(getDumpDataCommand(DUMP_MODE_CSV, DUMP_OVERWRITE,
            tmpFile.getAbsolutePath()));

        assertTrue(tmpFile.isFile());

        List<String> lines = Files.readLines(tmpFile, Charsets.UTF_8);
        assertEquals(2, lines.size());

        _moca.executeCommand(getDumpDataCommand(DUMP_MODE_CSV, DUMP_OVERWRITE,
            tmpFile.getAbsolutePath()));

        lines = Files.readLines(tmpFile, Charsets.UTF_8);
        assertEquals(2, lines.size());
    }

    public void testDumpDataCsvAppend() throws MocaException, IOException {

        tmpFile = File.createTempFile(TU_CoreService.class.getSimpleName(),
            ".tmp");

        // First call still with overwrite
        _moca.executeCommand(getDumpDataCommand(DUMP_MODE_CSV, DUMP_OVERWRITE,
            tmpFile.getAbsolutePath()));

        assertTrue(tmpFile.isFile());

        List<String> lines = Files.readLines(tmpFile, Charsets.UTF_8);
        assertEquals(2, lines.size());

        // Now append
        _moca.executeCommand(getDumpDataCommand(DUMP_MODE_CSV, DUMP_APPEND,
            tmpFile.getAbsolutePath()));

        lines = Files.readLines(tmpFile, Charsets.UTF_8);
        assertEquals(3, lines.size());
    }

    public void testDumpDataXmlOverwrite() throws MocaException, IOException {

        tmpFile = File.createTempFile(TU_CoreService.class.getSimpleName(),
            ".tmp");

        _moca.executeCommand(getDumpDataCommand(DUMP_MODE_XML, DUMP_OVERWRITE,
            tmpFile.getAbsolutePath()));

        assertTrue(tmpFile.isFile());

        List<String> lines = Files.readLines(tmpFile, Charsets.UTF_8);
        assertEquals(5, lines.size());

        _moca.executeCommand(getDumpDataCommand(DUMP_MODE_XML, DUMP_OVERWRITE,
            tmpFile.getAbsolutePath()));

        lines = Files.readLines(tmpFile, Charsets.UTF_8);
        assertEquals(5, lines.size());
    }

    public void testDumpDataXmlAppend() throws MocaException, IOException {
        // NOTE: append behavior for XML currently results in invalid XML!

        tmpFile = File.createTempFile(TU_CoreService.class.getSimpleName(),
            ".tmp");

        // First call still with overwrite
        _moca.executeCommand(getDumpDataCommand(DUMP_MODE_XML, DUMP_OVERWRITE,
            tmpFile.getAbsolutePath()));

        assertTrue(tmpFile.isFile());

        List<String> lines = Files.readLines(tmpFile, Charsets.UTF_8);
        assertEquals(5, lines.size());

        // Now append
        _moca.executeCommand(getDumpDataCommand(DUMP_MODE_XML, DUMP_APPEND,
            tmpFile.getAbsolutePath()));

        lines = Files.readLines(tmpFile, Charsets.UTF_8);
        assertEquals(7, lines.size());
    }
    
    public void testDoLoop() throws MocaException {
        MocaResults res = _moca.executeCommand("do loop where count = 100");
        RowIterator rowIter = res.getRows();
        for (int i = 0; i < 100; i++) {
            assertTrue(rowIter.next());
            assertEquals(i, rowIter.getInt("i"));
        }
        assertFalse(rowIter.next());
    }
    
    /**
     * This tests to make sure that expand environment variable works with
     * Windows syntax of %VARIABLE% notation
     * @throws MocaException 
     */
    public void testExpandEnvironmentVariableWindows() throws MocaException {
        String valueToConvert = "%MOCADIR%/src/java";
        testVariableReplacementPass(valueToConvert, true);
    }
    
    /**
     * This tests to make sure that expand environment variable works with
     * UNIX syntax of $VARIABLE notation
     * @throws MocaException 
     */
    public void testExpandEnvironmentVariableUnix1() throws MocaException {
        String valueToConvert = "$MOCADIR/src/java";
        testVariableReplacementPass(valueToConvert, true);
    }
    
    /**
     * This tests to make sure that expand environment variable works with
     * UNIX syntax of $VARIABLE notation
     * @throws MocaException 
     */
    public void testExpandEnvironmentVariableUnix1WithAlias() throws MocaException {
        String valueToConvert = "$MOCADIR/src/java";
        
        MocaResults res = _moca.executeCommand(
                "expand environment variable" +
                "  where variable = '" + valueToConvert + "'");
    
    RowIterator rowIter = res.getRows();
    
    // We should have gotten at least 1 row
    assertTrue("We didn't get any rows back", rowIter.next());
    
    // We should have returned name back with the value passed in
    assertEquals(valueToConvert, rowIter.getString("name"));
    
    // Get the variable straight from the system
    String mocaDirectory = _moca.getSystemVariable("MOCADIR");

    // We should have gotten value with the variable expanded
    assertEquals(mocaDirectory + "/src/java", rowIter.getString("value"));
    
    assertFalse("We got more than 1 row back", rowIter.next());
    }
    
    /**
     * This tests to make sure that expand environment variable works with
     * UNIX syntax of ${VARIABLE} notation
     * @throws MocaException 
     */
    public void testExpandEnvironmentVariableUnix2() throws MocaException {
        String valueToConvert = "${MOCADIR}/src/java";
        testVariableReplacementPass(valueToConvert, true);
    }
    
    /**
     * This tests to make sure that expand environment variable will not replace
     * any values if given %VARIABLE
     * @throws MocaException 
     */
    public void testExpandEnvironmentVariableWindowsNoReplace1() throws MocaException {
        String valueToConvert = "%MOCADIR/src/java";
        testVariableReplacementPass(valueToConvert, false);
    }
    
    /**
     * This tests to make sure that expand environment variable will not replace
     * any values if given VARIABLE%
     * @throws MocaException 
     */
    public void testExpandEnvironmentVariableWindowsNoReplace2() throws MocaException {
        String valueToConvert = "MOCADIR%/src/java";
        testVariableReplacementPass(valueToConvert, false);
    }
    
    /**
     * This tests to make sure that expand environment variable does not replace
     * given a value of ${VARIABLE
     * @throws MocaException 
     */
    public void testExpandEnvironmentVariableUnixNoReplace() throws MocaException {
        String valueToConvert = "${MOCADIR/src/java";
        testVariableReplacementPass(valueToConvert, false);
    }
    
    /**
     * This is a common method to test to make sure that variables were
     * replaced correctly
     * @param valueToConvert The value to convert variable replacements in
     * @param pass whether or not thi s
     * @throws MocaException 
     */
    private void testVariableReplacementPass(String valueToConvert, boolean pass) throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "expand environment variable" +
                    "  where name = '" + valueToConvert + "'");
        
        RowIterator rowIter = res.getRows();
        
        // We should have gotten at least 1 row
        assertTrue("We didn't get any rows back", rowIter.next());
        
        // We should have returned name back with the value passed in
        assertEquals(valueToConvert, rowIter.getString("name"));
        
        // This path is if the replacement should pass
        if (pass) {
            // Get the variable straight from the system
            String mocaDirectory = _moca.getSystemVariable("MOCADIR");

            // We should have gotten value with the variable expanded
            assertEquals(mocaDirectory + "/src/java", rowIter.getString("value"));
        }
        else {
            // It should just be the value passed in
            assertEquals(valueToConvert, rowIter.getString("value"));
        }
        
        assertFalse("We got more than 1 row back", rowIter.next());
    }

    /**
     * This method is to test to make sure going to sleep for the desired
     * time works correctly
     * @throws MocaException 
     */
    public void testGoToSleep() throws MocaException {
        int time = 2;
        long currentMilliseconds = System.currentTimeMillis();
        _moca.executeCommand(
                "go to sleep" +
                "  where time = " + time + "");
        
        long finishedMilliseconds = System.currentTimeMillis();
        
        long totalTime = finishedMilliseconds - currentMilliseconds;
        
        // Now make sure we slept for the time limit
        // - .030s for OS timing granularity (I've seen 1.970s)
        assertTrue("We slept for only " + totalTime + " milliseconds", 
                totalTime >= time * 1000 - 30);
    }
    
    /**
     * This method will check to make sure that describe object will
     * return the columns for a given table when executed
     * @throws MocaException 
     */
    public void testDescribeObjectForTable() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "describe object" +
                    "  where object = 'moca_dbversion'");
        assertTrue("We didn't get any metadata back", res.getColumnCount() > 0);
    }
    
    /**
     * This method will check to make sure that describe object will
     * return the columns for a given table when executed
     * @throws MocaException 
     */
    public void testDescribeObjectForTableWithAlias() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "describe object" +
                    "  where name = 'moca_dbversion'");
        assertTrue("We didn't get any metadata back", res.getColumnCount() > 0);
    }
    
    /**
     * This method will check to make sure that describe object will
     * return the columns for a given table when executed
     */
    public void testDescribeObjectForCommand() throws MocaException {
        // TODO we need to implement this after the list active command arguments is fixed
//            _moca.executeCommand(
//                    "describe object" +
//                    "  where object = 'publish data'");
    }
    
    /**
     * This method will check to make sure that describe object will
     * return the columns for a given table when executed
     * @throws MocaException 
     */
    public void testDescribeObjectForNothingFound() throws MocaException {
        try {
            _moca.executeCommand(
                    "describe object" +
                    "  where object = 'this shouldn''t exist!!'");
            fail("We should have thrown a NotFoundException");
        }
        catch (NotFoundException e) {
            // Code should go here
        }
    }
    
    /**
     * This method will check to make sure that when publish data is passed
     * with no arguments that it
     * @throws MocaException 
     */
    public void testPublishDataWithNoArguments() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "publish data");
        
        assertFalse("There should be no rows returned", res.next());
    }
    
    /**
     * This will test the basic usage of publish data combination
     * @throws MocaException 
     */
    public void testPublishDataCombination() throws MocaException {
        int loopCount = 3;
        int value = 9;
        MocaResults res = _moca.executeCommand(
                    "do loop" +
                    "  where count = " + loopCount + " >> res" +
                    "|" +
                    "publish data combination" +
                    "  where res = @res" +
                    "    and j = " + value);
        
        assertEquals("We got back incorrect # of rows", loopCount, 
                res.getRowCount());
        RowIterator rowIter = res.getRows();
        
        for (int i = 0; i < loopCount; ++i) {
            assertTrue(rowIter.next());
            assertEquals(i, rowIter.getInt("i"));
            assertEquals(value, rowIter.getInt("j"));
        }
        
        assertFalse("We got too many rows back", rowIter.next());
    }
    
    /**
     * This will test the usage of publish data combination with an empty result
     * set.  This should return a single row back with a single column and value
     * @throws MocaException 
     */
    public void testPublishDataCombinationEmptyResultSet() throws MocaException {
        int value = 9;
        MocaResults res = _moca.executeCommand(
                    "[[res = moca.newResults()]] >> res" +
                    "|" +
                    "publish data combination" +
                    "  where res = @res" +
                    "    and j = " + value);
        
        assertEquals("We got back incorrect # of rows", 1, 
                res.getRowCount());
        RowIterator rowIter = res.getRows();
        
        assertTrue(rowIter.next());
        assertEquals(value, rowIter.getInt("j"));
        
        assertFalse("We got too many rows back", rowIter.next());
    }
    
    /**
     * This will test the usage of publish data combination with a result set
     * that has only column metadata.  This should return a single row back with
     * 2 columns only 1 of which has a value
     * @throws MocaException 
     */
    public void testPublishDataCombinationResultSetWithMetaDataOnly() throws MocaException {
        int value = 9;
        MocaResults res = _moca.executeCommand(
                    "[[ " +
                    "    def res = moca.newResults();" +
                    "    res.addColumn(\"i\", MocaType.STRING);" +
                    "    res" +
                    "]] >> res" +
                    "|" +
                    "publish data combination" +
                    "  where res = @res" +
                    "    and j = " + value);
        
        assertEquals("We got back incorrect # of rows", 1, 
                res.getRowCount());
        RowIterator rowIter = res.getRows();
        
        assertTrue(rowIter.next());
        assertEquals(value, rowIter.getInt("j"));
        
        assertFalse("We got too many rows back", rowIter.next());
    }
    
    /**
     * This will test the usage of publish data combination with a result set
     * that has only column metadata.  This should return a single row back with
     * 2 columns only 1 of which has a value
     * @throws MocaException 
     */
    public void testPublishDataCombinationOverrideResultSet() throws MocaException {
        int value = 9;
        int loopCount = 3;
        MocaResults res = _moca.executeCommand(
                    "do loop" +
                    "  where count = " + loopCount + " >> res" +
                    "|" +
                    "publish data combination" +
                    "  where res = @res" +
                    "    and i = " + value);
        
        assertEquals("We got back incorrect # of rows", loopCount, 
                res.getRowCount());
        RowIterator rowIter = res.getRows();
        
        for (int i = 0; i < loopCount; ++i) {
            assertTrue(rowIter.next());
            assertEquals(value, rowIter.getInt("i"));
        }
        
        assertFalse("We got too many rows back", rowIter.next());
    }
    
    /**
     * This will test the usage of publish data combination with 2 result
     * sets passed in.  This should produce behavior similar to concatenating
     * result sets.
     */
    public void testPublishDataCombinationMultipleResultSets() throws MocaException {
        MocaResults res = _moca.executeCommand(
                "publish data where a = 1 >> res1" +
                "|" +
                "publish data where b = 2 >> res2" +
                "|" +
                "publish data combination" +
                "  where res1 = @res1" +
                "    and res2 = @res2");
        
        assertEquals("We got back incorrect # of rows", 2, 
                res.getRowCount());
        
        RowIterator rowIter = res.getRows();
        
        assertTrue(rowIter.next());
        assertEquals(1, rowIter.getInt("a"));
        assertEquals(null, rowIter.getValue("b"));
        assertTrue(rowIter.next());
        assertEquals(null, rowIter.getValue("a"));
        assertEquals(2, rowIter.getInt("b"));
        assertFalse(rowIter.next());
    }
    
    /**
     * This will test usage of publish data combination with only an empty
     * result set.  It should get no rows back.
     */
    public void testPublishDataCombinationOnlyEmptyResultSet() throws MocaException {
        MocaResults res = _moca.executeCommand(
                "publish data >> res" +
                "|" +
                "publish data combination where res = @res");
        
        assertEquals("We got back incorrect # of rows", 0, 
            res.getRowCount());
    }
    
    public void testPublishDataCombinationDuplicateColumns() throws MocaException {
        MocaResults res = _moca.executeCommand(
                "[select 'foo' as foo, 'bar' as foo from dual] >> res" +
                "|" +
                "publish data combination where res = @res");
        
        assertEquals("We got back incorrect # of rows", 1, 
            res.getRowCount());
        assertEquals("We got back incorrect # of columns", 2,
            res.getColumnCount());
    }
    
    /**
     * This will test to make sure that publish metadata is correctly publishing
     * only the column header information
     * @throws MocaException 
     */
    public void testPublishMetadata() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "do loop" +
                    "  where count = 2 " +
                    "|" +
                    "publish data where blah = 'yes' and i = @i >> resultset" +
                    "|" +
                    "publish metadata" +
                    "  where resultset = @resultset");
        
        assertEquals("There should be no rows found", 0, res.getRowCount());
        
        assertEquals("There should only be 2 columns", 2, res.getColumnCount());
        
        assertEquals("blah", res.getColumnName(0));
        assertEquals("i", res.getColumnName(1));
    }
    
    /**
     * This will test to make sure that publish metadata is correctly publishing
     * only the column header information
     * @throws MocaException 
     */
    public void testPublishMetadataWithAlias() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "do loop" +
                    "  where count = 2 " +
                    "|" +
                    "publish data where blah = 'yes' and i = @i >> result" +
                    "|" +
                    "publish metadata" +
                    "  where res = @result");
        
        assertEquals("There should be no rows found", 0, res.getRowCount());
        
        assertEquals("There should only be 2 columns", 2, res.getColumnCount());
        
        assertEquals("blah", res.getColumnName(0));
        assertEquals("i", res.getColumnName(1));
    }
    
    /**
     * This test is to ensure that publish top rows returns the correct number
     * of rows as well as the correct information for those rows when the row
     * number is smaller than available rows.
     * @throws MocaException 
     */
    public void testPublishTopRowsSmallerThanResult() throws MocaException {
        int totalRows = 6;
        int rows = 3;
        MocaResults res = _moca.executeCommand(
                    "do loop" +
                    "  where count = " + totalRows + " >> resultset" +
                    "|" +
                    "publish top rows" +
                    "  where resultset = @resultset" +
                    "    and rows = " + rows);
        
        assertEquals("There are not the correct number of rows", rows, 
                res.getRowCount());
        
        RowIterator rowIter = res.getRows();
        
        for (int i = 0; i < rows; ++i) {
            assertTrue(rowIter.next());
            assertEquals("The value returned doesn't match", i, 
                    rowIter.getInt(0));
        }
        
        assertFalse("There should not be anymore rows", rowIter.next());
    }
    
    /**
     * This is to test to make sure that when using publish top rows
     * that it will work with the alias 'res'
     * @throws MocaException
     */
    public void testPublishTopRowsWithAlias() throws MocaException {
        int totalRows = 6;
        int rows = 9;
        MocaResults res = _moca.executeCommand(
                    "do loop" +
                    "  where count = " + totalRows + " >> result" +
                    "|" +
                    "publish top rows" +
                    "  where res = @result" +
                    "    and count = " + rows);
        
        assertEquals("There are not the correct number of rows", totalRows, 
                res.getRowCount());
        
        RowIterator rowIter = res.getRows();
        
        for (int i = 0; i < totalRows; ++i) {
            assertTrue(rowIter.next());
            assertEquals("The value returned doesn't match", i, 
                    rowIter.getInt(0));
        }
        
        assertFalse("There should not be anymore rows", rowIter.next());
    }
    
    /**
     * This test is to ensure that publish top rows returns the correct number
     * of rows as well as the correct information for those rows when the row
     * number is larger than available rows.  This will then result in the result
     * set size being the same as before
     * @throws MocaException 
     */
    public void testPublishTopRowsLargerThanResult() throws MocaException {
        int totalRows = 6;
        int rows = 9;
        MocaResults res = _moca.executeCommand(
                    "do loop" +
                    "  where count = " + totalRows + " >> resultset" +
                    "|" +
                    "publish top rows" +
                    "  where resultset = @resultset" +
                    "    and rows = " + rows);
        
        assertEquals("There are not the correct number of rows", totalRows, 
                res.getRowCount());
        
        RowIterator rowIter = res.getRows();
        
        for (int i = 0; i < totalRows; ++i) {
            assertTrue(rowIter.next());
            assertEquals("The value returned doesn't match", i, 
                    rowIter.getInt(0));
        }
        
        assertFalse("There should not be anymore rows", rowIter.next());
    }
    
    public void testPublishTopRowsWithDuplicateColumns() throws MocaException {
        EditableResults res = new SimpleResults();
        res.addColumn("a", MocaType.STRING);
        res.addColumn("a", MocaType.STRING);
        
        for (int i = 0; i < 100; i++) {
            res.addRow();
            res.setStringValue(0, "a0:" + i);
            res.setStringValue(1, "a1:" + i);
        }
        
        MocaResults out = _moca.executeCommand(
            "publish top rows where count = 10", new MocaArgument("res", res));
        
        RowIterator rowIter = out.getRows();
        
        for (int i = 0; i < 10; ++i) {
            assertTrue(rowIter.next());
            assertEquals("The value returned doesn't match", "a0:" + i, 
                    rowIter.getString(0));
            assertEquals("The value returned doesn't match", "a1:" + i, 
                rowIter.getString(1));
        }
        
        assertFalse("There should not be anymore rows", rowIter.next());
    }
    
    /**
     * This method tests to make sure that trimming whitespace works correctly
     * @throws MocaException 
     */
    public void testRemoveExtraWhitespace() throws MocaException {
        String whitespaceString = "  foo - bar \t  yes \n got    them! @  . ";
        String whitespaceTrimmedString = "foo - bar yes got them! @ .";
        MocaResults res = _moca.executeCommand(
                    "remove extra whitespace" +
                    "  where string = '" + whitespaceString + "'");
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertEquals("The trimmed string was not the same", 
                whitespaceTrimmedString, rowIter.getString("string"));
    }
    
    /**
     * This method is to test to make sure the server execute command
     * is working correctly
     * @throws MocaException 
     */
    public void testExecuteServerCommand() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "publish data where val = 'worked' " +
                    "| " +
                    "execute server command " +
                    "  where cmd = 'publish data " +
                    "                 where value = @val'");
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertEquals("We didn't get the columns back correctly", 0, 
                res.getColumnNumber("value"));
        
        assertEquals("The command didn't execute correctly", 
                "worked", rowIter.getString("value"));
        
        assertFalse("We should have only gotten 1 row", rowIter.next());
    }
    
    /**
     * This command is to test the expand statement variable where there is
     * no alias present
     * @throws MocaException 
     */
    public void testExpandStatementVariablesNoAlias() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "publish data " +
                    "  where a=1 " +
                    "    and b=2 " +
                    "| " +
                    "expand statement variables " +
                    "  where columns='a,b'");
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertEquals("The command didn't execute correctly", 
                "a=1 and b=2", rowIter.getString("exdstr"));
        
        assertFalse("We should have only gotten 1 row", rowIter.next());
    }
    
    /**
     * This command is to test the expand statement variable where there is
     * an alias present
     * @throws MocaException 
     */
    public void testExpandStatementVariablesAlias() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "publish data " +
                    "  where a=1 " +
                    "    and b=2 " +
                    "| " +
                    "expand statement variables " +
                    "  where columns='c.a,b'");
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertEquals("The command didn't execute correctly", 
                "c=1 and b=2", rowIter.getString("exdstr"));
        
        assertFalse("We should have only gotten 1 row", rowIter.next());
    }
    
    /**
     * This command is to test the expand statement variable where there is
     * an alias present
     * @throws MocaException 
     */
    public void testExpandStatementVariablesWithNullAndOddAlias() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "publish data " +
                    "  where a=1 " +
                    "    and b=2 " +
                    "    and b.a = 'yes' " +
                    "    and d=null " +
                    "|" +
                    "expand statement variables " +
                    "  where columns='a.b.a,d,j'");
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertEquals("The command didn't execute correctly", 
                "a='yes' and d is null", rowIter.getString("exdstr"));
        
        assertFalse("We should have only gotten 1 row", rowIter.next());
    }
    
    /**
     * This method is to test to make sure that the get db method returns
     * back something
     * @throws MocaException 
     */
    public void testGetDb() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "get db");
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten a row", rowIter.next());
        
        // We need to make sure that we got a value back and it wasn't zero
        // length
        assertTrue("The command didn't execute correctly", 
                rowIter.getString("dbtype") != null && 
                rowIter.getString("dbtype").trim().length() > 0);
        
        assertFalse("We should have only gotten 1 row", rowIter.next());
    }
    
    /**
     * This method will test the get moca time command.
     * @throws MocaException 
     */
    public void testGetMocaTime() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "get moca time");
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten a row", rowIter.next());
        
        // We need to make sure that the value was published to the correct
        // column
        assertNotNull("The command didn't execute correctly", 
                rowIter.getInt("moca_time"));
        
        assertFalse("We should have only gotten 1 row", rowIter.next());
    }
    
    /**
     * This is to test the random number generation command to make sure it
     * comes back correctly
     * @throws MocaException 
     */
    public void testGetRandomNumber() throws MocaException {
        // We want to make the max number be 20, we can also run 20 tests
        // to make sure the number is 0 <= x < 19
        int maxValue = 20;
        
        for (int i = 0; i < maxValue; ++i) {
            MocaResults res = _moca.executeCommand(
                        "get random number" +
                        "  where max_value = " + maxValue);

            RowIterator rowIter = res.getRows();

            assertTrue("We should have gotten a row", rowIter.next());

            // We need to make sure that the value was published to the correct
            // column
            assertTrue("The command didn't execute correctly", 
                    0 <= rowIter.getInt("random") &&
                    rowIter.getInt("random") < maxValue);

            assertFalse("We should have only gotten 1 row", rowIter.next());
        }
    }
    
    /**
     * This method just tests to make sure that dump results returns okay
     * @throws MocaException 
     */
    public void testDumpResults() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "publish data " +
                    "  where foo = 'bar' >> res" +
                    "|" +
                    "publish data " +
                    "  where a = '1' " +
                    "    and b = 2 " +
                    "    and res = @res >> res2" +
                    "|" +
                    "dump results " +
                    "  where resultset = @res2");

        RowIterator rowIter = res.getRows();
        
        assertFalse("No rows should have been returned", rowIter.next());
    }
    
    /**
     * This is to test the set return status for result set moca command when
     * it doesn't throw an exception
     * @throws MocaException 
     */
    public void testSetReturnStatusForResultSetPass() throws MocaException {
        String value = "yes";
        MocaResults res = _moca.executeCommand(
                    "publish data " +
                    "  where value = '" + value + "' >> res" +
                    "|" +
                    "set return status for result set " +
                    "  where resultset = @res");

        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten 1 row", rowIter.next());
        
        assertEquals("The value doesn't match", value, 
                rowIter.getString("value"));
        
        assertFalse("There should only be 1 row", rowIter.next());
    }
    
    /**
     * This is to test the set return status moca command with arguments.
     * @throws MocaException 
     */
    public void testSetReturnStatusWithArgs() throws MocaException {
        try {
            MocaResults res = _moca.executeCommand(
                        "set return status where status = 903 and message = 'ERROR ^A^' and A = 'foo'");
            fail("expected MocaException, got " + res.getRowCount() + " rows.");
        }
        catch (MocaException e) {
            assertEquals(903, e.getErrorCode());
            assertEquals("ERROR ^A^", e.getMessage());
            assertEquals("foo", e.getArgValue("a"));
        }

    }
    
    /**
     * This is to test the set return status moca command with arguments.
     * @throws MocaException 
     */
    public void testSetReturnStatusWithLookupArgs() throws MocaException {
        try {
            MocaResults res = _moca.executeCommand(
                        "set return status where status = 903 and message = 'ERROR ^A^' and lookup_A = 'foo'");
            fail("expected MocaException, got " + res.getRowCount() + " rows.");
        }
        catch (MocaException e) {
            assertEquals(903, e.getErrorCode());
            assertEquals("ERROR ^A^", e.getMessage());
            MocaException.Args[] args = e.getArgList();
            assertEquals(1, args.length);
            assertEquals("A", args[0].getName());
            assertTrue(args[0].isLookup());
        }

    }
    /**
     * This is to test the set return status for result set moca command when
     * it doesn't throw an exception
     */
    public void testSetReturnStatusForResultSetError() {
        String value = "yes";
        int status = 502;
        try {
            _moca.executeCommand(
                    "publish data " +
                    "  where value = '" + value + "' >> result" +
                    "|" +
                    "set return status for result set " +
                    "  where resultset = @result" +
                    "    and status = " + status);
            fail("We should have thrown a Moca Exception");
        }
        catch (MocaException e) {
            // First check the error code
            assertEquals("The error codes don't match", status, 
                    e.getErrorCode());
            
            // Now we check to make sure the result set is good
            MocaResults res = e.getResults();

            RowIterator rowIter = res.getRows();
            
            assertTrue("We should have gotten 1 row", rowIter.next());
            
            assertEquals("The value doesn't match", value, 
                    rowIter.getString("value"));
            
            assertFalse("There should only be 1 row", rowIter.next());
        }
    }
    
    /**
     * This is to test to make sure that get registry comes back with something
     * @throws MocaException 
     */
    public void testGetRegistry() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "get registry value " +
                    "  where key = 'ENVIRONMENT' " +
                    "    and subkey = 'MOCADIR'");

        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten 1 row", rowIter.next());
        
        assertNotNull("The registry value for port could not be retrieved",
                rowIter.getString("value"));
        
        assertFalse("There should only be 1 row", rowIter.next());
    }
    
    /**
     * This is to test the set return status for result set moca command when
     * it doesn't throw an exception
     */
    public void testSetReturnStatusForResultSetErrorWithAlias() {
        String value = "yes";
        int status = 502;
        try {
            _moca.executeCommand(
                    "publish data " +
                    "  where value = '" + value + "' >> res" +
                    "|" +
                    "set return status for result set " +
                    "  where res = @res" +
                    "    and status = " + status);
            fail("We should have thrown a Moca Exception");
        }
        catch (MocaException e) {
            // First check the error code
            assertEquals("The error codes don't match", status, 
                    e.getErrorCode());
            
            // Now we check to make sure the result set is good
            MocaResults res = e.getResults();

            RowIterator rowIter = res.getRows();
            
            assertTrue("We should have gotten 1 row", rowIter.next());
            
            assertEquals("The value doesn't match", value, 
                    rowIter.getString("value"));
            
            assertFalse("There should only be 1 row", rowIter.next());
        }
    }
    
    /**
     * This is to test to make sure that get os var comes back with something
     * @throws MocaException 
     */
    public void testGetOsVariable() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "get os var" +
                    "  where var = 'MOCADIR'");

        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten 1 row", rowIter.next());
        
        assertNotNull("The registry value for MOCADIR could not be retrieved",
                rowIter.getString("value"));
        
        assertTrue("String should have some length", 
                rowIter.getString("value").length() > 0);
        
        assertFalse("There should only be 1 row", rowIter.next());
    }
    
    /**
     * This is to test to make sure that get os var comes back with something
     * @throws MocaException 
     */
    public void testGetOsVariableWithAlias() throws MocaException {
        MocaResults res = _moca.executeCommand(
                    "get os var" +
                    "  where variable = 'MOCADIR'");

        RowIterator rowIter = res.getRows();
        
        assertTrue("We should have gotten 1 row", rowIter.next());
        
        assertNotNull("The registry value for MOCADIR could not be retrieved",
                rowIter.getString("value"));
        
        assertTrue("String should have some length", 
                rowIter.getString("value").length() > 0);
        
        assertFalse("There should only be 1 row", rowIter.next());
    }
    
    /**
     * This is a simple test to make sure that sorting works correctly
     * @throws MocaException 
     */
    public void testSortResultSetSimple() throws MocaException {
        Map<String, Object> arguments = new HashMap<String, Object>();
        
        EditableResults results = _moca.newResults();
        
        fillInResultSet(results);
        
        arguments.put("result_set", results);
        arguments.put("sort_list", "col2");
        
        MocaResults res = _moca.executeCommand("sort result set", arguments);
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("There should be 1 row at least", rowIter.next());
        
        assertEquals("a", rowIter.getString("col1"));
        assertEquals("B", rowIter.getString("col2"));
        assertEquals("c", rowIter.getString("col3"));
        
        assertTrue("There should be 2 rows at least", rowIter.next());
        
        assertEquals("a", rowIter.getString("col1"));
        assertEquals("n", rowIter.getString("col2"));
        assertEquals("M", rowIter.getString("col3"));
        
        assertTrue("There should be 3 rows at least", rowIter.next());
        
        assertEquals("b", rowIter.getString("col1"));
        assertEquals("y", rowIter.getString("col2"));
        assertEquals("Z", rowIter.getString("col3"));
        
        assertFalse("There should only be 3 rows", rowIter.next());
    }
    
    /**
     * This is a test to make sure that sorting works correctly with 2 columns
     * one of which is descending
     */
    public void testSortResultSetTwoColumnOneDescending() {
        Map<String, Object> arguments = new HashMap<String, Object>();
        
        EditableResults results = _moca.newResults();
        
        fillInResultSet(results);
        
        arguments.put("result_set", results);
        arguments.put("sort_list", "col1, col2 descending");
        
        MocaResults res = null;
        try {
            res = _moca.executeCommand("sort result set", arguments);
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception: " + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("There should be 1 row at least", rowIter.next());
        
        assertEquals("a", rowIter.getString("col1"));
        assertEquals("n", rowIter.getString("col2"));
        assertEquals("M", rowIter.getString("col3"));
        
        assertTrue("There should be 2 rows at least", rowIter.next());
        
        assertEquals("a", rowIter.getString("col1"));
        assertEquals("B", rowIter.getString("col2"));
        assertEquals("c", rowIter.getString("col3"));
        
        assertTrue("There should be 3 rows at least", rowIter.next());
        
        assertEquals("b", rowIter.getString("col1"));
        assertEquals("y", rowIter.getString("col2"));
        assertEquals("Z", rowIter.getString("col3"));
        
        assertFalse("There should only be 3 rows", rowIter.next());
    }
    
    /**
     * This is just a duplicated code helper to make sure the values are inserted
     * in the same way
     * @param results The result set with the columns added and data inserted
     */
    private void fillInResultSet(EditableResults results) {
        results.addColumn("col1", MocaType.STRING);
        results.addColumn("col2", MocaType.STRING);
        results.addColumn("col3", MocaType.STRING);
        
        results.addRow();
        
        results.setStringValue("col1", "a");
        results.setStringValue("col2", "B");
        results.setStringValue("col3", "c");
        
        results.addRow();
        
        results.setStringValue("col1", "b");
        results.setStringValue("col2", "y");
        results.setStringValue("col3", "Z");
        
        results.addRow();
        
        results.setStringValue("col1", "a");
        results.setStringValue("col2", "n");
        results.setStringValue("col3", "M");
    }
    
    /**
     * This tests the usage of filter data.  It uses grovvy to ensure that
     * no arguments are on the stack so that it is correctly using the 
     * result sets
     * @throws MocaException 
     */
    public void testFilterData() throws MocaException {
        MocaResults results;
        RowIterator rowIter;
        
        results = _moca.executeCommand(
                    "[[ a = 'foo'; b = 'bar']] " +
                    "| " +
                    "[[ d = 'ignore' ]] " +
                    "|" +
                    "filter data " +
                    "  where moca_filter_level = 2 " +
                    "    and c = 3");
        
        rowIter = results.getRows();
        
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertEquals("foo", rowIter.getString("a"));
        assertEquals("bar", rowIter.getString("b"));
        assertEquals(3, rowIter.getInt("c"));
        assertEquals("d column should not be in the list", -1, 
                results.getColumnNumber("d"));
        assertEquals("Moca filter level should not be in the list", -1, 
                results.getColumnNumber("moca_filter_level"));
        
        assertFalse("There should only be 1 row", rowIter.next());
        
        /*
         * Override a published value with one of a different type.
         */
        results = _moca.executeCommand(
          "publish data where foo = 'A' and bar = 'B'" +
          "| " +
          "filter data where foo = 0");
        
        rowIter = results.getRows();
        
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertEquals(0, rowIter.getInt("foo"));
        assertEquals("B", rowIter.getString("bar"));
        assertEquals("Moca filter level should not be in the list", -1, 
                results.getColumnNumber("moca_filter_level"));
        
        assertFalse("There should only be 1 row", rowIter.next());        
    }
    
    public void testFilterDataMultipleRows() throws MocaException {
        
        MocaResults results = _moca.executeCommand(
                    "do loop where count = 100 | [[ a = 'Hello, ' + i; b = i ]] " +
                    "| " +
                    "if (@i % 2 = 0)" +
                    "filter data");
        
        assertEquals(2, results.getColumnCount());

        RowIterator rowIter = results.getRows();
        
        for (int i = 0; i < 50; i++) {
            assertTrue("Row " + i, rowIter.next());
            assertEquals("Hello, " + (i * 2), rowIter.getString("a"));
            assertEquals(i * 2, rowIter.getInt("b"));
        }
        
        assertFalse(rowIter.next());
    }
    
    public void testFilterDataWithInterveningFailedTest() throws MocaException {
        
        MocaResults results = _moca.executeCommand(
                    "do loop where count = 100 | [[ a = 'Hello, ' + i; b = i ]] " +
                    "| " +
                    "if (false) { publish data where x = 'Boo' }" +
                    "| " +
                    "filter data");
        
        RowIterator rowIter = results.getRows();
        assertFalse(rowIter.next());
    }
    
    /**
     * This will test the choose data command to make sure that the values
     * returned are correct
     * @throws MocaException 
     */
    public void testChooseData() throws MocaException {
        MocaResults results = _moca.executeCommand(
                    "[[ a=1; b=2; c=3 ]]" +
                    "| " +
                    "choose data " +
                    "  where columns='a, d=c'");
        
        RowIterator rowIter = results.getRows();
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertEquals(1, rowIter.getInt("a"));
        assertEquals(3, rowIter.getInt("d"));
        
        assertEquals("Column b should not have been returned", -1, 
                results.getColumnNumber("b"));
        assertEquals("Column c should not have been returned", -1, 
                results.getColumnNumber("c"));
        
        assertFalse("There should only be 1 row", rowIter.next());
    }
    
    /**
     * This will test the choose data command to make sure that the values
     * returned are correct when no rows are given.  That is they should have
     * empty values
     * @throws MocaException 
     */
    public void testChooseDataWithNoRows() throws MocaException {
        MocaResults results = _moca.executeCommand(
                    "[[ " +
                    "    def retRes = moca.newResults();" +
                    "    retRes.addColumn(\"a\", MocaType.STRING);" +
                    "    retRes.addColumn(\"c\", MocaType.STRING);" +
                    "]]" +
                    "|" +
                    "choose data " +
                    "  where columns='a, d=c'");
        
        RowIterator rowIter = results.getRows();
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertNull("There should be an empty value for a", rowIter.getValue("a"));
        assertNull("There should be an empty value for d", rowIter.getValue("d"));
        
        assertEquals("Column c should not have been returned", -1, 
                results.getColumnNumber("c"));
        
        assertFalse("There should only be 1 row", rowIter.next());
    }
    
    public void testChooseDataWithNullDataOnStack() throws MocaException {
        MocaResults results = _moca.executeCommand(
                    "publish data where a = date(null) |" +
                    "publish data where b = 'BBB' and c = 'CCC'" +
                    "|" +
                    "choose data " +
                    "  where columns='a, d=c'");
        
        RowIterator rowIter = results.getRows();
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertNull("There should be an empty value for a", rowIter.getValue("a"));
        assertEquals("Expecting Date Type", MocaType.DATETIME, results.getColumnType("a"));
        assertEquals("Wrong value for column D", "CCC", rowIter.getValue("d"));
        
        assertEquals("Column c should not have been returned", -1, 
                results.getColumnNumber("c"));
        
        assertFalse("There should only be 1 row", rowIter.next());
    }
    
    public void testChooseDataNoDataInResultSet() throws MocaException {
        EditableResults res = _moca.newResults();
        res.addColumn("binary", MocaType.BINARY);
        res.addColumn("boolean", MocaType.BOOLEAN);
        res.addColumn("datetime", MocaType.DATETIME);
        res.addColumn("double", MocaType.DOUBLE);
        res.addColumn("generic", MocaType.GENERIC);
        res.addColumn("integer", MocaType.INTEGER);
        res.addColumn("object", MocaType.OBJECT);
        res.addColumn("string", MocaType.STRING);
        res.addColumn("results", MocaType.RESULTS);
        
        MocaResults results = _moca.executeCommand(
            "[[ res ]]" +
            "|" +
            "choose data " +
            "  where columns='binary, datetime, boolean, integer, double, " +
            "           generic, results, string, object'", 
            new MocaArgument("res", res));
        
        assertEquals(MocaType.BINARY, results.getColumnType(0));
        assertEquals(MocaType.DATETIME, results.getColumnType(1));
        assertEquals(MocaType.BOOLEAN, results.getColumnType(2));
        assertEquals(MocaType.INTEGER, results.getColumnType(3));
        assertEquals(MocaType.DOUBLE, results.getColumnType(4));
        assertEquals(MocaType.GENERIC, results.getColumnType(5));
        assertEquals(MocaType.RESULTS, results.getColumnType(6));
        assertEquals(MocaType.STRING, results.getColumnType(7));
        assertEquals(MocaType.OBJECT, results.getColumnType(8));
        
        // Choose data always has a row, in our case it will be empty
        assertTrue("Choose data always has a row", results.next());
        for (int i = 0; i < results.getColumnCount(); ++i) {
            assertNull(results.getValue(i));
        }
        assertFalse(results.next());
    }
    
    /**
     * This tests the dump stack command to make sure that variable overwrites
     * and empty values are handled correctly
     * @throws MocaException 
     */
    public void testDumpStack() throws MocaException {
        MocaResults results = _moca.executeCommand(
                    "[[" +
                    "    def retRes = moca.newResults();" +
                    "    retRes.addColumn(\"a\", MocaType.STRING);" +
                    "    retRes.addColumn(\"c\", MocaType.STRING);" +
                    "]]" +
                    "|" +
                    "publish data " +
                    "  where a = 1 " +
                    "    and b = 2" +
                    "|" +
                    "publish data " +
                    "  where a = 'yes' " +
                    "    and foo = 'bar'" +
                    "|" +
                    "dump stack");
        
        RowIterator rowIter = results.getRows();
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertEquals("The value for a is incorrect", "yes", 
                rowIter.getString("a"));
        assertEquals("The value for foo is incorrect", "bar", 
                rowIter.getValue("foo"));
        assertEquals("The value for b is incorrect", 2, 
                rowIter.getInt("b"));
        
        assertEquals("Column c should not be provided", -1, 
                results.getColumnNumber("c"));
        
        assertFalse("There should only be 1 row", rowIter.next());
    }
    
    /**
     * This tests the dump stack command to make sure that variable overwrites
     * and empty values are handled correctly
     * @throws MocaException 
     */
    public void testDumpStackWithStop() {
        MocaResults results = null;
        try {
            _moca.executeCommand(
                        "[[" +
                        "    def retRes = moca.newResults();" +
                        "    retRes.addColumn(\"a\", MocaType.STRING);" +
                        "    retRes.addColumn(\"c\", MocaType.STRING);" +
                        "]]" +
                        "|" +
                        "publish data " +
                        "  where a = 1 " +
                        "    and b = 2" +
                        "|" +
                        "publish data " +
                        "  where a = 'yes' " +
                        "    and foo = 'bar'" +
                        "|" +
                        "dump stack" +
                        "  where stop = 'y'");
            fail("There should have been an exception thrown");
        }
        catch (MocaException e) {
            // The code should go in here
            results = e.getResults();
        }
        
        RowIterator rowIter = results.getRows();
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertEquals("The value for a is incorrect", "yes", 
                rowIter.getString("a"));
        assertEquals("The value for foo is incorrect", "bar", 
                rowIter.getValue("foo"));
        assertEquals("The value for b is incorrect", 2, 
                rowIter.getInt("b"));
        
        assertEquals("Column c should not be provided", -1, 
                results.getColumnNumber("c"));
        
        assertFalse("There should only be 1 row", rowIter.next());
    }

    /**
     * This tests to ensure the reorder columns command works correctly.  It
     * sorts the variables as well as makes sure that ambiguous
     * @throws MocaException
     */
    public void testReorderColumns() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "publish data combination" +
                "  where foo='bar'" +
                "    and b=2 " +
                "    and c=3 " +
                "    and a=1" +
                "|" +
                "reorder columns " +
                "  where order = 'b,a'");
    
        RowIterator rowIter = results.getRows();
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertEquals("There should be 4 columns", 4, results.getColumnCount());
        
        assertEquals("b", results.getColumnName(0));
        assertEquals(2, rowIter.getInt(0));
        
        assertEquals("a", results.getColumnName(1));
        assertEquals(1, rowIter.getInt(1));
        
        assertEquals("foo", results.getColumnName(2));
        assertEquals("bar", rowIter.getString(2));
        
        assertEquals("c", results.getColumnName(3));
        assertEquals(3, rowIter.getInt(3));
        
        assertFalse("There should only be 1 row", rowIter.next());
    }
    
    /**
     * Test to make sure that rename columns works when given a specific result
     * set to rename
     * @throws MocaException
     */
    public void testRenameColumns() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "publish data " +
                "  where a=1 " +
                "    and b=2 >> res " +
                "|" +
                "rename columns " +
                "  where res=@res " +
                "    and a='c' " +
                "    and b='d'");
    
        RowIterator rowIter = results.getRows();
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertTrue("c should be a column", results.getColumnNumber("c") >= 0);
        assertTrue("d should be a column", results.getColumnNumber("d") >= 0);
        
        assertEquals(1, rowIter.getInt("c"));
        assertEquals(2, rowIter.getInt("d"));
        
        assertFalse("There should have been only 1 row", rowIter.next());
    }
    
    /**
     * Test to make sure that rename columns works when given a specific result
     * set to rename
     * @throws MocaException
     */
    public void testRenameColumnsWithNoPassedResultSet() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "publish data " +
                "  where a=1 " +
                "    and b=2 " +
                "|" +
                "rename columns " +
                "  where a='c' " +
                "    and b='d'");
    
        RowIterator rowIter = results.getRows();
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertTrue("c should be a column", results.getColumnNumber("c") >= 0);
        assertTrue("d should be a column", results.getColumnNumber("d") >= 0);
        
        assertEquals(1, rowIter.getInt("c"));
        assertEquals(2, rowIter.getInt("d"));
        
        assertFalse("There should have been only 1 row", rowIter.next());
    }
    
    /**
     * Test to make sure that hiding a stack variable works correctly
     * @throws MocaException If an exception occurs during execution
     */
    public void testHideStackVariable() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "publish data where hideme = 'false'" +
                "|" +
                "hide stack variable where name = 'hideme'" +
                "|" +
                "publish data where foo = @hideme");
        
        RowIterator rowIter = results.getRows();
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertNull("foo should be blank", rowIter.getValue("foo"));
        
        assertFalse("There should have been only 1 row", rowIter.next());
    }
    
    public void testListLibraryVersions() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "list library versions");
        
        RowIterator rowIter = results.getRows();
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertNotNull("We should have a category", 
                rowIter.getString("category"));
        assertNotNull("We should have a library type", 
                rowIter.getString("library_type"));
        assertNotNull("All MOCA components use java now", 
                rowIter.getString("package_name"));
        assertNotNull("We should have a product", rowIter.getString("product"));
    }
    
    public void testListLibraryVersionsWithArgument() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "list library versions where category = 'MOCAbase'");
        
        RowIterator rowIter = results.getRows();
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertEquals("MOCAbase", rowIter.getString("category"));
        assertEquals("MOCAbase", rowIter.getString("library_name"));
        assertEquals("Java", rowIter.getString("library_type"));
        assertEquals("com.redprairie.moca.components.base", rowIter.getString(
                "package_name"));
        assertEquals("moca", rowIter.getString("product"));
        assertFalse("We should have only gotten 1 row", rowIter.next());
    }
    
    public void testCheckCommandSyntaxValid() throws MocaException {
        MocaResults res = _moca.executeCommand(
            "check command syntax " +
            "    where command = 'publish data where foo = 3'");
        
        RowIterator rowIter = res.getRows();
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertEquals("publish data where foo = 3", rowIter.getString("command"));
        assertEquals("0", rowIter.getString("status"));
        assertEquals("0", rowIter.getString("errorline"));
        assertEquals("0", rowIter.getString("errorpos"));
        assertNull(rowIter.getString("errortext"));
    }
    
    public void testCheckCommandSyntaxInvalid() throws MocaException {
        MocaResults res = _moca.executeCommand(
            "check command syntax " +
            "    where command = 'publish data foo = 3'");
        
        RowIterator rowIter = res.getRows();
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertEquals("publish data foo = 3", rowIter.getString("command"));
        assertEquals("505", rowIter.getString("status"));
        assertEquals("1", rowIter.getString("errorline"));
        assertEquals("18", rowIter.getString("errorpos"));
        assertEquals("Unexpected token:  =", rowIter.getString("errortext"));
        assertFalse("We should have gotten only a single row", rowIter.next());
    }
    
    public void testGetServerInformation() throws MocaException {
        MocaResults res = _moca.executeCommand("get server information");
        
        RowIterator rowIter = res.getRows();
        assertTrue("We should have gotten a row", rowIter.next());
        
        assertNotNull("There should be a server url entry in the registry!", 
                rowIter.getString("url"));
        assertFalse("We should have gotten only a single row", rowIter.next());
    }
    
    @Test
    public void testPublishDataWithDuplicateColumns() throws MocaException {
        MocaResults res = _moca.executeCommand("publish data where x = 'a' and x = 'b'");
        assertTrue(res.next());
        assertEquals(1, res.getColumnCount());
        assertEquals("b", res.getString("x"));
        assertEquals("b", res.getString(0));
    }

    @Test
    public void testPublishDataWithDuplicateColumnNamesWithDifferentTypes() throws MocaException {
        MocaResults res = _moca.executeCommand("publish data where x = 3.14 and x = 'easy as ' || @x");
        assertTrue(res.next());
        assertEquals(1, res.getColumnCount());
        assertEquals("easy as 3.14", res.getString("x"));
        assertEquals("easy as 3.14", res.getString(0));
    }

    /**
     * This test makes sure that executing a simple command while passing in
     * a directory of one below that it can be repeated multiple times without
     * error.  If this were to actually change the directory it would throw
     * an error after you have executed the command a number of times equal to
     * how many folders deep the working directory is.
     * @throws MocaException
     */
    @Test
    public void testOsExecuteCommandInDirectoryLeavesDirectoryAsIs() throws MocaException {
        String cmdToExecute;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            cmdToExecute = "dir";
        }
        else {
            cmdToExecute = "ls";
        }
        
        for (int i = 0; i < 20; i++) {
            
            _moca.executeCommand("execute os command in directory " +
            		"where cmd = @cmd and directory = '..'",
            		new MocaArgument("cmd", cmdToExecute));
        }
    }
    
    @Test
    public void testEncryptDecryptRPBFFile() throws MocaException {
        FileCreationTestUtility util = new FileCreationTestUtility();
        String randomString = "This is a test of the rp bit flip encryption." +
        		"  This is only a test.  \nPlease delete this file if" +
        		" there are no unit tests running. %M@!814.<-#1af";
        File randomFile = util.createTempFileAndInsertValues("RPBF1", randomString, null);
        
        long fileSizeBefore = randomFile.length();
        
        _moca.executeCommand("encrypt file using rpbf", 
                new MocaArgument("filename", randomFile.getAbsoluteFile()));
        
        // Make sure the file is at least the same size.
        assertEquals("RPBit flip should not change size", fileSizeBefore, 
                randomFile.length());
        
        _moca.executeCommand("decrypt file using rpbf", 
                new MocaArgument("filename", randomFile.getAbsoluteFile()));
        
        util.compareFileContentsWithString(randomFile, randomString, null);
    }
    
    @Test
    public void testEncryptDecryptDataUsingPointer() throws MocaException, UnsupportedEncodingException {
        String convertedString = "Convert me and I shall do whatever you want!";
        int blockSize = 14;
        MocaResults res = _moca.executeCommand("test convert to binary pointer", 
                new MocaArgument("value", convertedString));

        assertTrue(res.next());
        int length = res.getInt("data_len");
        assertEquals(convertedString.length(), length);
        Object pointer = res.getValue("data_ptr");
        assertFalse(res.next());

        // Here we pass in the data pointer directly to encrypt data
        res = _moca.executeCommand("encrypt data using rpbf", 
                new MocaArgument("data_len", length), 
                new MocaArgument("data_ptr", pointer),
                new MocaArgument("block_size", blockSize));

        assertTrue(res.next());

        Object returnBytes = res.getValue(0);
        assertTrue(returnBytes instanceof byte[]);
        byte[] encryptedBytes = (byte[]) returnBytes;
        assertEquals(convertedString.length(), encryptedBytes.length);
        
        assertFalse(res.next());

        res = _moca.executeCommand("decrypt data using rpbf", 
                new MocaArgument("data_bin", encryptedBytes),
                new MocaArgument("block_size", blockSize));

        assertTrue(res.next());

        returnBytes = res.getValue(0);
        assertTrue(returnBytes instanceof byte[]);
        byte[] decryptedBytes = (byte[]) returnBytes;
        assertEquals(convertedString.length(), decryptedBytes.length);
        
        assertFalse(res.next());
       
       assertEquals(convertedString, new String(decryptedBytes, "UTF-8"));
    }
    
    /**
     * If this test is broken it will actually freeze.  This is due to the block
     * size not incrementing correctly.
     * @throws MocaException
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testRPBFInvalidBlockSize() throws MocaException, UnsupportedEncodingException {
        String convertedString = "Convert me and I shall do whatever you want!";
        int blockSize = 0;

        // Here we pass in the data pointer directly to encrypt data
        MocaResults res = _moca.executeCommand("encrypt data using rpbf", 
                new MocaArgument("data", convertedString),
                new MocaArgument("block_size", blockSize));

        assertTrue(res.next());

        Object returnBytes = res.getValue(0);
        assertTrue(returnBytes instanceof byte[]);
        byte[] encryptedBytes = (byte[]) returnBytes;
        assertEquals(convertedString.length(), encryptedBytes.length);
        
        assertFalse(res.next());

        res = _moca.executeCommand("decrypt data using rpbf", 
                new MocaArgument("data_bin", encryptedBytes),
                new MocaArgument("block_size", blockSize));

        assertTrue(res.next());

        returnBytes = res.getValue(0);
        assertTrue(returnBytes instanceof byte[]);
        byte[] decryptedBytes = (byte[]) returnBytes;
        assertEquals(convertedString.length(), decryptedBytes.length);
        
        assertFalse(res.next());
       
       assertEquals(convertedString, new String(decryptedBytes, "UTF-8"));
    }
    
    public void testPutOsVar() throws MocaException {
        _moca.executeCommand("put os var where var = 'TEST' and val = 'foo'");
        _moca.executeCommand("put os var where variable = 'TEST2' and value = 'bar'");
        
        assertEquals("foo", _moca.getSystemVariable("TEST"));
        assertEquals("bar", _moca.getSystemVariable("TEST2"));
    }
    
    public void testPutOsVarException() throws MocaException {
        try {
            _moca.executeCommand("put os var where val = 'foo'");
        }
        catch (RequiredArgumentException e) {
            // Should go here.
        }
    }
    
    public void testGetOsVar() throws MocaException {
        _moca.putSystemVariable("TEST", "bar");
        
        MocaResults res = _moca.executeCommand("get os var where var = 'TEST'");
        RowIterator iter = res.getRows();
        
        assertTrue(iter.next());
        assertEquals("bar", iter.getString("value"));
        assertFalse(iter.next());
        
        _moca.putSystemVariable("TEST2", "foo");
        res = _moca.executeCommand("get os var where variable = 'TEST2'");
        iter = res.getRows();
        
        assertTrue(iter.next());
        assertEquals("foo", iter.getString("value"));
        assertFalse(iter.next());
    }
    
    public void testGetOsVarException() throws MocaException {
        try {
            _moca.executeCommand("get os var where vars = 'foo'");
        }
        catch (RequiredArgumentException e) {
            // Should go here.
        }
    }
    
    public void testRemoveOsVar() throws MocaException {
        _moca.putSystemVariable("TEST", "bar");
        assertEquals("bar", _moca.getSystemVariable("TEST"));
        _moca.executeCommand("remove os var where var = 'TEST'");
        assertNull(_moca.getSystemVariable("TEST"));
        
        _moca.putSystemVariable("TEST2", "foo");
        assertEquals("foo", _moca.getSystemVariable("TEST2"));
        _moca.executeCommand("remove os var where var = 'TEST2'");
        assertNull(_moca.getSystemVariable("TEST2"));    
    }
    
    public void testRemoveOsVarException() throws MocaException {
        try {
            _moca.executeCommand("remove os var where vars = 'foo'");
        }
        catch (RequiredArgumentException e) {
            // Should go here.
        }
    }
}
