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

import com.redprairie.moca.server.db.BindList;

import static org.junit.Assert.*;

/**
 * Tests the OracleLimitHandler
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_OracleLimitHandler extends AbstractSqlLimitHandlerTests {
    
    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#newHandlerImplementation()
    @Override
    LimitHandler newHandlerImplementation() {
        return new OracleLimitHandler();
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#getRowStartIdentifer()
    @Override
    String getRowStartIdentifer() {
        return OracleLimitHandler.ROW_START_IDENTIFIER;
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#getRowEndIdentifer()
    @Override
    String getRowEndIdentifer() {
        return OracleLimitHandler.ROW_END_IDENTIFER;
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestBasicTranslation()
    @Override
    void validateTestBasicTranslation(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("select * from ( select * from task_definition ) where rownum <= :moca_row_end__", transformedSql);
        assertEquals(1, bindList.getNames().size());
        assertEquals(Long.valueOf(50), bindList.getValue(OracleLimitHandler.ROW_END_IDENTIFER));
    }
    
    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestBasicTranslationWithTotalCount()
    @Override
    void validateTestBasicTranslationWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("select moca_subquery__.*, (select count(*) from (select * from task_definition)) moca_total_rows__ from ( select * from task_definition ) moca_subquery__ where rownum <= :moca_row_end__",
                transformedSql);
        assertEquals(1, bindList.getNames().size());
        assertEquals(Long.valueOf(50), bindList.getValue(OracleLimitHandler.ROW_END_IDENTIFER));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestBasicTranslationWithNonZeroStart()
    @Override
    void validateTestBasicTranslationWithNonZeroStart(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("select * from ( select row_.*, rownum rownum_ from ( select * from task_definition ) row_ where rownum <= :moca_row_end__) where rownum_ > :moca_row_start__", transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(Long.valueOf(startRow + rowLimit), bindList.getValue(getRowEndIdentifer()));
    }
    
    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestBasicTranslationWithNonZeroStartWithTotalCount()
    @Override
    void validateTestBasicTranslationWithNonZeroStartWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("select moca_subquery__.*, (select count(*) from (select * from task_definition)) moca_total_rows__ from ( select row_.*, rownum rownum_ from ( select * from task_definition ) row_ where rownum <= :moca_row_end__) moca_subquery__ where rownum_ > :moca_row_start__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(Long.valueOf(startRow + rowLimit), bindList.getValue(getRowEndIdentifer()));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestTranslationWithOrderBy()
    @Override
    void validateTestTranslationWithOrderBy(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("select * from ( select row_.*, rownum rownum_ from ( select task_id from task_definition where auto_start = 0 order by task_id ) row_ where rownum <= :moca_row_end__) where rownum_ > :moca_row_start__", transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(Long.valueOf(startRow + rowLimit), bindList.getValue(getRowEndIdentifer()));
    }
    
    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestTranslationWithOrderByWithTotalCount()
    @Override
    void validateTestTranslationWithOrderByWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("select moca_subquery__.*, (select count(*) from (select task_id from task_definition where auto_start = 0 order by task_id)) moca_total_rows__ from ( select row_.*, rownum rownum_ from ( select task_id from task_definition where auto_start = 0 order by task_id ) row_ where rownum <= :moca_row_end__) moca_subquery__ where rownum_ > :moca_row_start__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(Long.valueOf(startRow + rowLimit), bindList.getValue(getRowEndIdentifer()));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestSubSelectInSelect()
    @Override
    void validateTestSubSelectInSelect(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("select * from ( select row_.*, rownum rownum_ from ( select task_id, (select count(*) from task_definition) cnt from task_definition ) row_ where rownum <= :moca_row_end__) where rownum_ > :moca_row_start__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(Long.valueOf(startRow + rowLimit), bindList.getValue(getRowEndIdentifer()));
    }
    
    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestSubSelectInSelectWithTotalCount()
    @Override
    void validateTestSubSelectInSelectWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("select moca_subquery__.*, (select count(*) from (select task_id, (select count(*) from task_definition) cnt from task_definition)) moca_total_rows__ from ( select row_.*, rownum rownum_ from ( select task_id, (select count(*) from task_definition) cnt from task_definition ) row_ where rownum <= :moca_row_end__) moca_subquery__ where rownum_ > :moca_row_start__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(Long.valueOf(startRow + rowLimit), bindList.getValue(getRowEndIdentifer()));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestStartRowSpecifiedWithNoLimit()
    @Override
    void validateTestStartRowSpecifiedWithNoLimit(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("select * from ( select row_.*, rownum rownum_ from ( select * from task_definition ) row_ where rownum <= :moca_row_end__) where rownum_ > :moca_row_start__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(getRowStartIdentifer()));
        assertEquals(Long.MAX_VALUE, bindList.getValue(getRowEndIdentifer()));
    }
    
    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestStartRowSpecifiedWithNoLimitWithTotalCount()
    @Override
    void validateTestStartRowSpecifiedWithNoLimitWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("select moca_subquery__.*, (select count(*) from (select * from task_definition)) moca_total_rows__ from ( select row_.*, rownum rownum_ from ( select * from task_definition ) row_ where rownum <= :moca_row_end__) moca_subquery__ where rownum_ > :moca_row_start__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(OracleLimitHandler.ROW_START_IDENTIFIER));
        assertEquals(Long.MAX_VALUE, bindList.getValue(OracleLimitHandler.ROW_END_IDENTIFER));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestStartRowSpecifiedWithNoLimitWithOrderBy()
    @Override
    void validateTestStartRowSpecifiedWithNoLimitWithOrderBy(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("select * from ( select row_.*, rownum rownum_ from ( select task_id from task_definition where auto_start = 0 order by task_id ) row_ where rownum <= :moca_row_end__) where rownum_ > :moca_row_start__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(OracleLimitHandler.ROW_START_IDENTIFIER));
        assertEquals(Long.MAX_VALUE, bindList.getValue(OracleLimitHandler.ROW_END_IDENTIFER));
    }

    // @see com.redprairie.moca.server.db.translate.AbstractSqlLimitHandlerTests#expectedTestStartRowSpecifiedWithNoLimitWithOrderByWithTotalCount()
    @Override
    void validateTestStartRowSpecifiedWithNoLimitWithOrderByWithTotalCount(String transformedSql, BindList bindList, int startRow, int rowLimit) {
        assertEquals("select moca_subquery__.*, (select count(*) from (select task_id from task_definition where auto_start = 0 order by task_id)) moca_total_rows__ from ( select row_.*, rownum rownum_ from ( select task_id from task_definition where auto_start = 0 order by task_id ) row_ where rownum <= :moca_row_end__) moca_subquery__ where rownum_ > :moca_row_start__",
                transformedSql);
        assertEquals(2, bindList.getNames().size());
        assertEquals(startRow, bindList.getValue(OracleLimitHandler.ROW_START_IDENTIFIER));
        assertEquals(Long.MAX_VALUE, bindList.getValue(OracleLimitHandler.ROW_END_IDENTIFER));
    }

}
