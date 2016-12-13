/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.server.db.BindList;

import static org.junit.Assert.assertEquals;

/**
 * Tests the Microsoft SQL Server database specific
 * LimitHandler
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_SQLServerLimitHandler extends AbstractSqlLimitHandlerTests {

    
    @Test
    // Hibernate assumes all literals are used as bound variables at this point,
    // so that embedded literals are currently handled incorrectly. This test is here to document current
    // incorrect Hibernate behavior for addLimit() call.  If this test breaks after upgrading Hibernate to a new version, 
    // then we will need to adjust our own HibernateUtils.formatSQLForHibernate() logic accordingly
    public void testEmbeddedLiteralBehavior(){
        LimitHandler limitHandler = newHandlerImplementation();
        String limitedSql = limitHandler.addLimit("select"
                + System.lineSeparator() + "'" 
                + System.lineSeparator() + "distinct" 
                + System.lineSeparator() + "' x," 
                + System.lineSeparator() + "task.task_id,"
                + System.lineSeparator() + "count(distinct job.job_id) x"
                + System.lineSeparator() + "from"
                + System.lineSeparator() + "task_definition task,"
                + System.lineSeparator() + "job_definition job"
                + System.lineSeparator() + "group by" 
                + System.lineSeparator() + "task.task_id"
                + System.lineSeparator() + "order by" 
                + System.lineSeparator() + "task_id", 
                5, 10, Mockito.mock(BindList.class), true);
        
        assertEquals(
            "WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM" +
            " ( select"
                // distinct needs to be surrounded by spaces
                + System.lineSeparator() + "'" 
                + System.lineSeparator() + " distinct TOP(9223372036854775807) " 
                + System.lineSeparator() + "' x," 
                + System.lineSeparator() + "task.task_id,"
                + System.lineSeparator() + "count(distinct job.job_id) x"
                + System.lineSeparator() + "from"
                + System.lineSeparator() + "task_definition task,"
                + System.lineSeparator() + "job_definition job"
                + System.lineSeparator() + "group by" 
                + System.lineSeparator() + "task.task_id"
                // order by needs to be surrounded by spaces
                + System.lineSeparator() + " order by "
                + System.lineSeparator() + "task_id ) inner_query )" +
             " SELECT (select count(*) from query) moca_total_rows__, * " +
             "FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__ AND __hibernate_row_nr__ < :moca_row_end__",
             limitedSql);
    }
    
    
    @Test
    public void testDistinctOnNewLine(){
        LimitHandler limitHandler = newHandlerImplementation();
        String limitedSql = limitHandler.addLimit("select"
                + System.lineSeparator() + "distinct" 
                + System.lineSeparator() + "task.task_id,"
                + System.lineSeparator() + "count(distinct job.job_id) x"
                + System.lineSeparator() + "from"
                + System.lineSeparator() + "task_definition task,"
                + System.lineSeparator() + "job_definition job"
                + System.lineSeparator() + "group by" 
                + System.lineSeparator() + "task.task_id"
                + System.lineSeparator() + "order by" 
                + System.lineSeparator() + "task_id", 
                5, 10, Mockito.mock(BindList.class), true);
        
        assertEquals(
            "WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM" +
            " ( select"
                // distinct needs to be surrounded by spaces
                + System.lineSeparator() + " distinct TOP(9223372036854775807) " 
                + System.lineSeparator() + "task.task_id,"
                + System.lineSeparator() + "count(distinct job.job_id) x"
                + System.lineSeparator() + "from"
                + System.lineSeparator() + "task_definition task,"
                + System.lineSeparator() + "job_definition job"
                + System.lineSeparator() + "group by" 
                + System.lineSeparator() + "task.task_id"
                // order by needs to be surrounded by spaces
                + System.lineSeparator() + " order by "
                + System.lineSeparator() + "task_id ) inner_query )" +
             " SELECT (select count(*) from query) moca_total_rows__, * " +
             "FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__ AND __hibernate_row_nr__ < :moca_row_end__",
             limitedSql);
    }
    
    
    /**
     * Tests an edge case where order by is specified in the select statement in which
     * case the use of TOP needs to be added to the select. The logic that determines this is looking
     * for " order by " with a starting space. However, if the order by starts on a new line the string would
     * be something like "\norder by" and the match would not occur. Since that logic is owned by Hibernate
     * the fix is to replace newline characters with newline + a space and the match will occur correctly.
     */
    @Test
    public void testOrderByOnNewLine() {
        LimitHandler limitHandler = newHandlerImplementation();
        String limitedSql = limitHandler.addLimit("select * from task_definition " 
                + System.lineSeparator() + "order by" + System.lineSeparator()  
                + " task_id" , 5, 10, Mockito.mock(BindList.class), true);
        assertEquals(
            "WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM" +
            " ( select TOP(9223372036854775807) * from task_definition " +
              System.lineSeparator() + " order by " + System.lineSeparator() + " task_id ) inner_query )" +
             " SELECT (select count(*) from query) moca_total_rows__, * " +
             "FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__ AND __hibernate_row_nr__ < :moca_row_end__",
             limitedSql);
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#newHandlerImplementation()
    @Override
    LimitHandler newHandlerImplementation() {
        return new SQLServerLimitHandler();
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#getRowStartIdentifer()
    @Override
    String getRowStartIdentifer() {
        return SQLServerLimitHandler.ROW_START_IDENTIFIER;
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#getRowEndIdentifer()
    @Override
    String getRowEndIdentifer() {
        return SQLServerLimitHandler.ROW_END_IDENTIFER;
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#validateTestBasicTranslation()
    @Override
    void validateTestBasicTranslation(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM ( select * from task_definition ) inner_query ) SELECT * FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__ AND __hibernate_row_nr__ < :moca_row_end__", transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(startRow + rowLimit, bindList.getValue(getRowEndIdentifer()));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestBasicTranslationWithTotalCount()
    @Override
    void validateTestBasicTranslationWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM ( select * from task_definition ) inner_query ) SELECT (select count(*) from query) moca_total_rows__, * FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__ AND __hibernate_row_nr__ < :moca_row_end__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(startRow + rowLimit, bindList.getValue(getRowEndIdentifer()));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestBasicTranslationWithNonZeroStart()
    @Override
    void validateTestBasicTranslationWithNonZeroStart(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM ( select * from task_definition ) inner_query ) SELECT * FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__ AND __hibernate_row_nr__ < :moca_row_end__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(startRow + rowLimit, bindList.getValue(getRowEndIdentifer()));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestBasicTranslationWithNonZeroStartWithTotalCount()
    @Override
    void validateTestBasicTranslationWithNonZeroStartWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM ( select * from task_definition ) inner_query ) SELECT (select count(*) from query) moca_total_rows__, * FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__ AND __hibernate_row_nr__ < :moca_row_end__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(startRow + rowLimit, bindList.getValue(getRowEndIdentifer()));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestTranslationWithOrderBy()
    @Override
    void validateTestTranslationWithOrderBy(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM ( select TOP(9223372036854775807) task_id from task_definition where auto_start = 0 order by task_id ) inner_query ) SELECT * FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__ AND __hibernate_row_nr__ < :moca_row_end__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(startRow + rowLimit, bindList.getValue(getRowEndIdentifer()));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestTranslationWithOrderByWithTotalCount()
    @Override
    void validateTestTranslationWithOrderByWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM ( select TOP(9223372036854775807) task_id from task_definition where auto_start = 0 order by task_id ) inner_query ) SELECT (select count(*) from query) moca_total_rows__, * FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__ AND __hibernate_row_nr__ < :moca_row_end__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(startRow + rowLimit, bindList.getValue(getRowEndIdentifer()));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestSubSelectInSelect()
    @Override
    void validateTestSubSelectInSelect(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM ( select task_id, (select count(*) from task_definition) cnt from task_definition ) inner_query ) SELECT * FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__ AND __hibernate_row_nr__ < :moca_row_end__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(startRow + rowLimit, bindList.getValue(getRowEndIdentifer()));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestSubSelectInSelectWithTotalCount()
    @Override
    void validateTestSubSelectInSelectWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM ( select task_id, (select count(*) from task_definition) cnt from task_definition ) inner_query ) SELECT (select count(*) from query) moca_total_rows__, * FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__ AND __hibernate_row_nr__ < :moca_row_end__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(startRow + rowLimit, bindList.getValue(getRowEndIdentifer()));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestStartRowSpecifiedWithNoLimit()
    @Override
    void validateTestStartRowSpecifiedWithNoLimit(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM ( select * from task_definition ) inner_query ) SELECT * FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__",
                transformedSql);
        assertEquals(1, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestStartRowSpecifiedWithNoLimitWithTotalCount()
    @Override
    void validateTestStartRowSpecifiedWithNoLimitWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM ( select * from task_definition ) inner_query ) SELECT (select count(*) from query) moca_total_rows__, * FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__",
                transformedSql);
        assertEquals(1, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestStartRowSpecifiedWithNoLimitWithOrderBy()
    @Override
    void validateTestStartRowSpecifiedWithNoLimitWithOrderBy(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM ( select TOP(9223372036854775807) task_id from task_definition where auto_start = 0 order by task_id ) inner_query ) SELECT * FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__",
                transformedSql);
        assertEquals(1, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestStartRowSpecifiedWithNoLimitWithOrderByWithTotalCount()
    @Override
    void validateTestStartRowSpecifiedWithNoLimitWithOrderByWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("WITH query AS (SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM ( select TOP(9223372036854775807) task_id from task_definition where auto_start = 0 order by task_id ) inner_query ) SELECT (select count(*) from query) moca_total_rows__, * FROM query WHERE __hibernate_row_nr__ >= :moca_row_start__",
                transformedSql);
        assertEquals(1, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
    }
}
