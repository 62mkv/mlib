/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.server.db.translate;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.PagedResults;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.util.MocaUtils;

/**
 * Integration tests for the "limit" hint made available by the MOCA engine via
 * JDBCAdapter and LimitHandler implementing classes. This test is platform independent
 * so it can run against Oracle and SQLServer in the same manner.
 * 
 * All the tests work by inserting 26 test rows into task_definition marked
 * TI_LimitHandler-<a-z> and then doing paging on those rows
 * 
 * Copyright (c) 2013 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TI_LimitHandler {
    
    @BeforeClass
    public static void setupDataForAllTests() throws SystemConfigurationException, MocaException {
        ServerUtils.setupDaemonContext(TI_LimitHandler.class.getName(), true);
        MOCA = MocaUtils.currentContext();
        // Create the test data TI_LimitHandler-<a-z>
        for (char i = 'a'; i <= 'z';  i++) {
            MOCA.executeCommand(String.format(CREATE_DATA_COMMAND, i));
        }
        // Get how many columns are in the task_definition table as we use this for result set validation
        TASK_DEFINITION_COLUMN_COUNT = MOCA.executeCommand("[select * from task_definition where rownum < 2]").getColumnCount();
    }
    
    @AfterClass
    public static void rollbackTestData() throws MocaException {
        MOCA.rollback();
    }
    
    /**
     * Tests retrieving various pages with a basic query including the total count
     * @throws MocaException
     */
    @Test
    public void testBasicQuery() throws MocaException {
        testBasicQuery(false);
    }
    
    /**
     * Tests retrieving various pages with a basic query including the total count
     * @throws MocaException
     */
    @Test
    public void testBasicQueryWithTotalCount() throws MocaException {
        testBasicQuery(true);
    }
    
    private void testBasicQuery(boolean findTotal) throws MocaException {
        String command = "select * from task_definition where task_id like 'TI_LimitHandler-%'";
        testRunner(command, TASK_DEFINITION_COLUMN_COUNT, findTotal, false);
    }
    
    /**
     * Tests various pagination with a query that has an order by in it
     * @throws MocaException
     */
    @Test
    public void testQueryWithOrderBy() throws MocaException {
        testNonZeroStartWithOrderBy(false);
    }
    
    /**
     * Tests various pagination with a query that has an order by in it including total count
     * @throws MocaException
     */
    @Test
    public void testQueryWithOrderByWithTotalCount() throws MocaException {
        testNonZeroStartWithOrderBy(true);
    }
    
    private void testNonZeroStartWithOrderBy(boolean findTotal) throws MocaException {
        String command = "select task_id from task_definition where task_id like 'TI_LimitHandler-%' and auto_start = 0 order by task_id";
        testRunner(command, 1, findTotal, false);
    }
    
    /**
     * Tests various pagination with a query that has a subselect in the select statement
     * @throws MocaException
     */
    @Test
    public void testSubSelectInSelect() throws MocaException {
        testSubSelectInSelect(false);
    }
    
    /**
     * Tests various pagination with a query that has a subselect in the select statement, includes the total count
     * @throws MocaException
     */
    @Test
    public void testSubSelectInSelectWithTotal() throws MocaException {
        testSubSelectInSelect(true);
    }
    
    private void testSubSelectInSelect(boolean findTotal) throws MocaException {
        String command = "select task_id, (select count(*) from task_definition) cnt from task_definition where task_id like 'TI_LimitHandler-%' order by task_id ";
        testRunner(command, 2, findTotal, false);
    }
    
    /**
     * Tests a more complex SQL statement with built other MOCA functions as well as order by/group by
     * @throws MocaException
     */
    @Test
    public void testWithOtherMocaFunctions() throws MocaException {
        testWithOtherMocaFunctions(false);
    }

    /**
     * Tests a more complex SQL statement with built other MOCA functions as well as order by/group by
     * - also calculates the total rows
     * @throws MocaException
     */
    @Test
    public void testWithOtherMocaFunctionsWithTotal() throws MocaException {
        testWithOtherMocaFunctions(true);
    }
    
    private void testWithOtherMocaFunctions(boolean findTotal) throws MocaException {
        String command = "select nvl(task_id, 'foo') task_id, decode(auto_start, 0, 'foo', 'bar') enabled_toggle from task_definition where task_id like 'TI_LimitHandler-%' group by task_id, auto_start order by task_id";
        testRunner(command, 2, findTotal, false);
    }
    
    /**
     * Tests argument substitution via MOCA arguments in the "limit" hint
     * @throws MocaException
     */
    @Test
    public void testArgumentSubstitution() throws MocaException {
        testArgumentSubstitution(false);
    }
    
    /**
     * Tests argument substitution via MOCA arguments in the "limit" hint - also calculates the total
     * @throws MocaException
     */
    @Test
    public void testArgumentSubstitutionWithTotal() throws MocaException {
        testArgumentSubstitution(true);
    }
    
    private void testArgumentSubstitution(boolean findTotal) throws MocaException {
        String publishStatement = String.format(PUBLISH_STATEMENT, 10, 5, findTotal);
        String command = publishStatement + " |  [/*#limit=@startRow,@rowLimit,@findTotal */ select nvl(task_id, 'foo') task_id, decode(auto_start, 0, 'foo', 'bar') enabled_toggle from task_definition where task_id like 'TI_LimitHandler-%' group by task_id, auto_start order by task_id ]";
        testRunner(command, 2, 5, 10, findTotal, false);
    }
    
    @Test
    public void testQueryWithDescendingOrder() throws MocaException {
        testQueryWithDescendingOrder(false);
    }
    
    @Test
    public void testQueryWithDescendingOrderWithTotalCount() throws MocaException {
        testQueryWithDescendingOrder(true);
    }
    
    @Test
    public void testNoResultsReturnedWithoutTotal() throws MocaException {
        testNoResultsReturned(false);
    }
    
    @Test
    public void testNoResultsReturnedWithTotal() throws MocaException {
        testNoResultsReturned(true);
    }

    private void testNoResultsReturned(boolean findTotal) throws MocaException {
        testNoResultsReturned(0, 10, findTotal);
        testNoResultsReturned(1000, 10, findTotal);
    }
    
    // Tests with a query that automatically returns no results (1=2). Should result
    // in a NotFoundException and the total row count should be 0
    private void testNoResultsReturned(int startRow, int rowLimit, boolean findTotal) throws MocaException {
        try {
            MOCA.executeCommand(String.format(
                "[ /*#limit=%d,%d,%b */ select task_id from task_definition where 1=2 ]", startRow, rowLimit, findTotal));
            fail("Should have resulted in NotFoundException");
        }
        catch (NotFoundException expected) {
            MocaResults res = expected.getResults();
            assertEquals(1, res.getColumnCount());
            res.containsColumn("task_id");
            if (findTotal) {
                PagedResults pagedRes = (PagedResults) res;
                assertEquals(0, pagedRes.getTotalRowCount());
            }
        }
    }

    private void testQueryWithDescendingOrder(boolean findTotal) throws MocaException {
        String command = "select task_id from task_definition where task_id like 'TI_LimitHandler-%' order by task_id desc";
        testRunner(command, 1, findTotal, true);
    }
    
    private void testRunner(String baseCommand, int expectedColumnCount, boolean validateTotal, boolean descendingOrder) throws MocaException {
        for (TestData data : PAGE_TESTS) {
            testRunner(getCommand(baseCommand, data, validateTotal), expectedColumnCount, data._expectedRows,
                    data._startRow == null ? 0 : data._startRow, validateTotal, descendingOrder);
        }
    }
    
    private void testRunner(String command, int expectedColumnCount, int expectedRowCount, int startRow,
            boolean validateTotal, boolean descendingOrder) throws MocaException {
        MocaResults res;
        try {
            res = MOCA.executeCommand(command);
        }
        catch (NotFoundException nfe) {
            if (expectedRowCount != 0) {
                fail(String.format("Expected row count was %d but 0 rows were returned", expectedRowCount));
            }
            
            res = nfe.getResults();
        }
        
        assertEquals(expectedRowCount, res.getRowCount());
        assertEquals(expectedColumnCount, res.getColumnCount());
        char startChar = descendingOrder ? (char) ('z' - startRow) : (char) ('a' + startRow);
        while (res.next()) {
            assertEquals("TI_LimitHandler-" + startChar, res.getString("task_id"));
            startChar = descendingOrder ? --startChar : ++startChar;
        }
        
        if (validateTotal) {
            assertEquals(A_Z_COUNT, MocaUtils.getPagedTotalRowCount(res));
        }
        else {
            // If total wasn't calculated then paged row count should
            // just return the actual row count
            assertEquals(expectedRowCount, MocaUtils.getPagedTotalRowCount(res));
        }
    }
    
    private String getCommand(String command, TestData data, boolean findTotal) {
        // If find total is specified then we have to use all 3 arguments e.g. #limit=5,10,true
        if (findTotal) {
            return String.format("[ /*#limit=%s,%d,%b */ %s ]", data._startRow == null ? "0" : Integer.toString(data._startRow), data._rowLimit, findTotal, command);
        }
        // If the start row was null use the other format which is #limit=<row_limit>
        else if (data._startRow == null && data._rowLimit > 0) {
            return String.format("[ /*#limit=%d */ %s ]", data._rowLimit, command);
        }
        else {
            return String.format("[ /*#limit=%d,%d */ %s ]", data._startRow, data._rowLimit, command);
        }
    }
    
    private static class TestData {
        
        TestData(Integer startRow, int rowLimit, int expectedRows) {
            _startRow = startRow;
            _rowLimit = rowLimit;
            _expectedRows = expectedRows;
        }
        
        private final Integer _startRow;
        private final int _rowLimit;
        private final int _expectedRows;
    }
    
    private static final List<TestData> PAGE_TESTS;
    
    static {
        PAGE_TESTS = new ArrayList<TestData>(7);
        PAGE_TESTS.add(new TestData(0, 10, 10)); // 10 rows starting from first row
        PAGE_TESTS.add(new TestData(10, 5, 5)); // start at 10 row offset with row limit of 5
        PAGE_TESTS.add(new TestData(20, 0, 6)); // last page (6 rows starting from 20th row)
        PAGE_TESTS.add(new TestData(0, 0, 26)); // no limit
        PAGE_TESTS.add(new TestData(null, 3, 3)); // 3 rows starting from first (without specifying start row of 0)
        PAGE_TESTS.add(new TestData(1, 5, 5)); // 5 rows starting from second (tests zero based indexing errors)
        PAGE_TESTS.add(new TestData(10000, 10, 0)); // Way beyond the available page data,
                       // should throw a NotFoundException but the total should be 26 still
    }

    private static int TASK_DEFINITION_COLUMN_COUNT;
    private static MocaContext MOCA;
    // Test data is TI_Limit-<a-z> for a total of 26 rows
    private static final int A_Z_COUNT = 26;
    private static final String CREATE_DATA_COMMAND = 
            "add task where task_id = 'TI_LimitHandler-%s' and name = 'test' and cmd_line = 'test' and auto_start = false";
    
    private static final String PUBLISH_STATEMENT = "publish data where startRow = %d and rowLimit = %d and findTotal = %b";
}
