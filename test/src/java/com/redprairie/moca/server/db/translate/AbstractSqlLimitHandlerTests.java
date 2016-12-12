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

import org.junit.Before;
import org.junit.Test;

import com.redprairie.moca.server.db.BindList;

/**
 * An abstract class which contains a set of tests for
 * testing the functionality of SQL LimitHandlers. This should be
 * subclassed by an implementing LimitHandler test class and the abstract
 * methods should implement the appropriate validation. This is useful to guarantee
 * the functionality exists for various sets of queries across multiple implementing
 * DB engines.
 * 
 * Copyright (c) 2013 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public abstract class AbstractSqlLimitHandlerTests {
    
    /**
     * Returns the test unit class, one that implements LimitHandler
     * @return
     */
    abstract LimitHandler newHandlerImplementation();
    
    /**
     * Gets the identifier used for row start in SQL statements
     * @return
     */
    abstract String getRowStartIdentifer();
    
    /**
     * Gets the identifier used for row end in SQL statements
     * @return
     */
    abstract String getRowEndIdentifer();
    
    /**
     * Validates basic translation given the simple query starting on the first row:
     * <pre>
     * select * from task_definition
     * </pre>
     * Start row = 0
     * Row limit = 50
     * 
     * @param transformedSql The transformed SQL after applying the limit
     * @param bindList The bind list
     * @param startRow The start row
     * @param rowLimit The row limit
     */
    abstract void validateTestBasicTranslation(String transformedSql, BindList bindList, int startRow, int rowLimit);
    
    /**
     * Validates basic translation given the simple query starting on the first row and also calculating the total possible count:
     * <pre>
     * select * from task_definition
     * </pre>
     * Start row = 0
     * Row limit = 50
     * 
     * @param transformedSql The transformed SQL after applying the limit
     * @param bindList The bind list
     * @param startRow The start row
     * @param rowLimit The row limit
     */
    abstract void validateTestBasicTranslationWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit);
    
    /**
     * Validates basic translation given the below query but starting on a non-zero row:
     * <pre>
     * select * from task_definition
     * </pre>
     * Start row = 50
     * Row limit = 25
     * 
     * @param transformedSql The transformed SQL after applying the limit
     * @param bindList The bind list
     * @param startRow The start row
     * @param rowLimit The row limit
     */
    abstract void validateTestBasicTranslationWithNonZeroStart(String transformedSql, BindList bindList, int startRow, int rowLimit);
    
    /**
     * Validates basic translation given the below query but starting on a non-zero row, also the total count is calculated:
     * <pre>
     * select * from task_definition
     * </pre>
     * Start row = 50
     * Row limit = 25
     * 
     * @param transformedSql The transformed SQL after applying the limit
     * @param bindList The bind list
     * @param startRow The start row
     * @param rowLimit The row limit
     */
    abstract void validateTestBasicTranslationWithNonZeroStartWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit);
    
    /**
     * Validates translation of a SQL statement with an order by in it, given below:
     * <pre>
     * select task_id from task_definition where auto_start = 0 order by task_id
     * </pre>
     * Start row = 150
     * Row limit = 50
     * 
     * @param transformedSql The transformed SQL after applying the limit
     * @param bindList The bind list
     * @param startRow The start row
     * @param rowLimit The row limit
     */
    abstract void validateTestTranslationWithOrderBy(String transformedSql, BindList bindList, int startRow, int rowLimit);
    
    /**
     * Validates translation of a SQL statement with an order by in it, also the total count is calculated, query below:
     * <pre>
     * select task_id from task_definition where auto_start = 0 order by task_id
     * </pre>
     * Start row = 150
     * Row limit = 50
     * 
     * @param transformedSql The transformed SQL after applying the limit
     * @param bindList The bind list
     * @param startRow The start row
     * @param rowLimit The row limit
     */
    abstract void validateTestTranslationWithOrderByWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit);
    
    /**
     * Validates translation of a SQL statement with a subselect in it, given below:
     * <pre>
     * select task_id, (select count(*) from task_definition) cnt from task_definition
     * </pre>
     * Start row = 150
     * Row limit = 50
     * 
     * @param transformedSql The transformed SQL after applying the limit
     * @param bindList The bind list
     * @param startRow The start row
     * @param rowLimit The row limit
     */
    abstract void validateTestSubSelectInSelect(String transformedSql, BindList bindList, int startRow, int rowLimit);
    
    /**
     * Validates translation of a SQL statement with a subselect in it additionally calculating the total, given below:
     * <pre>
     * select task_id, (select count(*) from task_definition) cnt from task_definition
     * </pre>
     * Start row = 150
     * Row limit = 50
     * 
     * @param transformedSql The transformed SQL after applying the limit
     * @param bindList The bind list
     * @param startRow The start row
     * @param rowLimit The row limit
     */
    abstract void validateTestSubSelectInSelectWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit);
    
    /**
     * Validates translation of a SQL statement with a start row specified but no limit, query below:
     * <pre>
     * select * from task_definition
     * </pre>
     * Start row = 50
     * Row limit = 0 (unlimited)
     * 
     * @param transformedSql The transformed SQL after applying the limit
     * @param bindList The bind list
     * @param startRow The start row
     * @param rowLimit The row limit
     */
    abstract void validateTestStartRowSpecifiedWithNoLimit(String transformedSql, BindList bindList, int startRow, int rowLimit);
    
    /**
     * Validates translation of a SQL statement with a start row specified but no limit, also calculates the total, query below:
     * <pre>
     * select * from task_definition
     * </pre>
     * Start row = 50
     * Row limit = 0 (unlimited)
     * 
     * @param transformedSql The transformed SQL after applying the limit
     * @param bindList The bind list
     * @param startRow The start row
     * @param rowLimit The row limit
     */
    abstract void validateTestStartRowSpecifiedWithNoLimitWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit);
    
    /**
     * Validates translation of a SQL statement with a start row specified but no limit with an order by, query below:
     * <pre>
     * select task_id from task_definition where auto_start = 0 order by task_id
     * </pre>
     * Start row = 50
     * Row limit = 0 (unlimited)
     * 
     * @param transformedSql The transformed SQL after applying the limit
     * @param bindList The bind list
     * @param startRow The start row
     * @param rowLimit The row limit
     */
    abstract void validateTestStartRowSpecifiedWithNoLimitWithOrderBy(String transformedSql, BindList bindList, int startRow, int rowLimit);
    
    /**
     * Validates translation of a SQL statement with a start row specified but no limit with an order by, also calculates total, query below:
     * <pre>
     * select task_id from task_definition where auto_start = 0 order by task_id
     * </pre>
     * Start row = 50
     * Row limit = 0 (unlimited)
     * 
     * @param transformedSql The transformed SQL after applying the limit
     * @param bindList The bind list
     * @param startRow The start row
     * @param rowLimit The row limit
     */
    abstract void validateTestStartRowSpecifiedWithNoLimitWithOrderByWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit);
    
    @Before
    public void before() {
        _handler = newHandlerImplementation();
    }
    
    @Test
    public void testBasicTranslation() {
        TransformResult result = testBasicTranslation(false);
        validateTestBasicTranslation(result._transformedSql, result._args, result._startRow, result._rowLimit);
    }
    
    @Test
    public void testBasicTranslationWithTotalCount() {
        TransformResult result = testBasicTranslation(true);
        validateTestBasicTranslationWithTotalCount(result._transformedSql, result._args, result._startRow, result._rowLimit);
    }
    
    private TransformResult testBasicTranslation(boolean findTotal) {
        return testRunner("select * from task_definition", 0, 50, findTotal);
    }
    
    @Test
    public void testBasicTranslationWithNonZeroStart() {
        TransformResult result = testBasicTranslationWithNonZeroStart(false);
        validateTestBasicTranslationWithNonZeroStart(result._transformedSql, result._args, result._startRow, result._rowLimit);
    }
    
    @Test
    public void testBasicTranslationWithNonZeroStartWithTotalCount() {
        TransformResult result = testBasicTranslationWithNonZeroStart(true);
        validateTestBasicTranslationWithNonZeroStartWithTotalCount(result._transformedSql, result._args, result._startRow, result._rowLimit);
    }
    
    private TransformResult testBasicTranslationWithNonZeroStart(boolean findTotal) {
        return testRunner("select * from task_definition", 50, 25, findTotal);
    }
    
    @Test
    public void testTranslationWithOrderBy() {
        TransformResult result = testTranslationWithOrderBy(false);
        validateTestTranslationWithOrderBy(result._transformedSql, result._args, result._startRow, result._rowLimit);
    }
    
    @Test
    public void testTranslationWithOrderByWithTotalCount() {
        TransformResult result = testTranslationWithOrderBy(true);
        validateTestTranslationWithOrderByWithTotalCount(result._transformedSql, result._args, result._startRow, result._rowLimit);
    }
    
    private TransformResult testTranslationWithOrderBy(boolean findTotal) {
        return testRunner("select task_id from task_definition where auto_start = 0 order by task_id", 150, 50, findTotal);
    }
    
    @Test
    public void testSubSelectInSelect() {
        TransformResult result = testSubSelectInSelect(false);
        validateTestSubSelectInSelect(result._transformedSql, result._args, result._startRow, result._rowLimit);
    }
    
    @Test
    public void testSubSelectInSelectWithTotalCount() {
        TransformResult result = testSubSelectInSelect(true);
        validateTestSubSelectInSelectWithTotalCount(result._transformedSql, result._args, result._startRow, result._rowLimit);
    }
    
    private TransformResult testSubSelectInSelect(boolean findTotal) {
        return testRunner("select task_id, (select count(*) from task_definition) cnt from task_definition", 150, 50, findTotal);
    }
    
    @Test
    public void testStartRowSpecifiedWithNoLimit() {
        TransformResult result = testStartRowSpecifiedWithNoLimit(false);
        validateTestStartRowSpecifiedWithNoLimit(result._transformedSql, result._args, result._startRow, result._rowLimit);
    }
    
    @Test
    public void testStartRowSpecifiedWithNoLimitWithTotalCount() {
        TransformResult result = testStartRowSpecifiedWithNoLimit(true);
        validateTestStartRowSpecifiedWithNoLimitWithTotalCount(result._transformedSql, result._args, result._startRow, result._rowLimit);
    }
    
    private TransformResult testStartRowSpecifiedWithNoLimit(boolean findTotal) {
        return testRunner("select * from task_definition", 50, 0, findTotal);
    }
    
    @Test
    public void testStartRowSpecifiedWithNoLimitWithOrderBy() {
        TransformResult result = testStartRowSpecifiedWithNoLimitWithOrderBy(false);
        validateTestStartRowSpecifiedWithNoLimitWithOrderBy(result._transformedSql, result._args, result._startRow, result._rowLimit);
    }
    
    @Test
    public void testStartRowSpecifiedWithNoLimitWithOrderByWithTotalCount() {
        TransformResult result = testStartRowSpecifiedWithNoLimitWithOrderBy(true);
        validateTestStartRowSpecifiedWithNoLimitWithOrderByWithTotalCount(result._transformedSql, result._args, result._startRow, result._rowLimit);
    }
    
    private TransformResult testStartRowSpecifiedWithNoLimitWithOrderBy(boolean findTotal) {
        return testRunner("select task_id from task_definition where auto_start = 0 order by task_id", 50, 0, findTotal);
    }
    
    private TransformResult testRunner(String sql, int startRow, int rowLimit, boolean findTotal) {
        BindList args = new BindList();
        String actualSql = _handler.addLimit(sql, startRow, rowLimit, args, findTotal);
        return new TransformResult(actualSql, args, _handler.handleStartRow(startRow), rowLimit);
    }
    
    private static class TransformResult {
        
        public TransformResult(String transformedSql, BindList args, int startRow, int rowLimit) {
            _startRow = startRow;
            _rowLimit = rowLimit;
            _args = args;
            _transformedSql = transformedSql;
        }
        
        private final int _startRow;
        private final int _rowLimit;
        private final BindList _args;
        private final String _transformedSql;
    }
    
    private LimitHandler _handler;

}
