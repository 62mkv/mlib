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

package com.sam.moca.server.db.translate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.dialect.Oracle10gDialect;

import com.sam.moca.server.db.BindList;

/**
 * Handles pagination/limiting for the Oracle DB engine.
 * The underlying work is done by Hibernate's translation layer
 * that provides modifying the query.
 * 
 * Given the query:
 * <pre>
 * select * from task_definition
 * </pre>
 * 
 * and the limit of start row = 25 and row limit = 50, the resulting
 * query would be:
 * <pre>
 * select * from ( select * from task_definition ) where rownum <= :moca_row_end__
 * </pre>
 * Additionally, the total number of possible rows without paging can be calculated, if so the
 * same query would transform into:
 * <pre>
 * select moca_subquery__.*, 
 *        (select count(*) from (select * from task_definition)) moca_total_rows__
 *   from ( select * from task_definition ) moca_subquery__ where rownum <= :moca_row_end__
 * </pre>
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
class OracleLimitHandler implements LimitHandler {
    
    OracleLimitHandler() {
        // Hardcoded to use the Hibernate Oracle 10gDialect which is the newest for Hibernate
        // and is compatible for all newer versions
        _dialect = new Oracle10gDialect();
    }

    // @see com.sam.moca.server.db.translate.LimitHandler#getExcludedColumns()
    @Override
    public List<String> getExcludedColumns() {
        return EXCLUDED_COLUMNS;
    }

    // @see com.sam.moca.server.db.translate.LimitHandler#getTotalColumnName()
    @Override
    public String getTotalColumnName() {
        return TOTAL_ROWS_COLUMN_IDENTIFER;
    }
        
    // @see com.sam.moca.server.db.translate.LimitHandler#addLimit(java.lang.String, int, int, com.sam.moca.server.db.BindList, boolean)
    @Override
    public String addLimit(String sql, int startRow, int rowLimit, BindList args, boolean findTotal) {
        String modifiedSql = _dialect.getLimitString(sql, startRow > 0);
        return handlePostProcessing(sql, modifiedSql, startRow, rowLimit, args, findTotal);
    }
    
    @Override
    public void rebindLimitVariables(int startRow, int rowLimit, BindList args) {
        args.add(ROW_START_IDENTIFIER, startRow);
        args.add(ROW_END_IDENTIFER, startRow + rowLimit);
    }
    
    // @see com.sam.moca.server.db.translate.LimitHandler#handleStartRow(int)
    @Override
    public int handleStartRow(int startRow) {
        return startRow;
    }
    
    // @see com.sam.moca.server.db.translate.LimitHandler#supportsLimit()
    @Override
    public boolean supportsLimit() {
        return true;
    }

    // @see com.sam.moca.server.db.translate.LimitHandler#supportsTotalCount()
    @Override
    public boolean supportsTotalCount() {
        return true;
    }

    // Handles taking the modified SQL statement after going through the Hibernate Oracle pagination engine
    // and substitutes in our bind variables as well as handles if the total count should be added to the query
    private String handlePostProcessing(String originalSql, String modifiedSql, final int startRow, final long rowLimit, BindList args, boolean findTotal) {
        StringBuilder finalSql = new StringBuilder(modifiedSql);
        // If the start row was specified (non-zero) we need to substitute in our bind variable and add to the bind list
        if (startRow > 0) {
            int tokenIndex = finalSql.lastIndexOf(HIBERNATE_START_ROW_INEQUALITY) + HIBERNATE_START_ROW_INEQUALITY.length();
            finalSql.replace(tokenIndex - 1, tokenIndex, ROW_START_TOKEN);
            args.add(ROW_START_IDENTIFIER, startRow);
        }
        
        long endRow;
        // If the row limit wasn't specified then for the <= inequality in the query we just set to Long.MAX which
        // is effectively unlimited, otherwise the end row is just the start row + the limit
        if (rowLimit == 0) {
            endRow = Long.MAX_VALUE;
        }
        else {
            endRow = startRow + rowLimit;
        }
        
        // Replace the end row <= inequality in the query with our bind variable name
        int tokenIndex = finalSql.lastIndexOf(HIBERNATE_END_ROW_INEQUALITY) + HIBERNATE_END_ROW_INEQUALITY.length();
        finalSql.replace(tokenIndex - 1, tokenIndex, ROW_END_TOKEN);
        args.add(ROW_END_IDENTIFER, endRow);
        
        // If it was requested that query calculate the total count (without rownum filtering) then we append that to the query as well
        // by appending a subquery that retrieves this count
        if (findTotal) {
            // The query will start with "select *" which is where we append our subquery
            tokenIndex = finalSql.indexOf("*");
            StringBuilder subQuery = new StringBuilder(MOCA_SUBQUERY_TEMP_TABLE)
                .append(".*, (select count(*) from (").append(originalSql).append(")) ").append(this.getTotalColumnName());
            finalSql.replace(tokenIndex, tokenIndex + 1, subQuery.toString());
            
            // Also, we need to alias the table in the query so we can do moca_subquery__.*, <count_subquery>
            // because Oracle will complain otherwise
            tokenIndex = finalSql.lastIndexOf("where");
            finalSql.insert(tokenIndex - 1, " " + MOCA_SUBQUERY_TEMP_TABLE);
        }
        
        return finalSql.toString();
    }

    // Hibernate Oracle10gDialiect used to translate the SQL statement
    private final Oracle10gDialect _dialect;
    
    // Bind variable token to replace with for the start row:
    // :moca_row_start__
    private static final String ROW_START_TOKEN = ":" + LimitHandler.ROW_START_IDENTIFIER;
    
    // Bind variable token to replace with for the end row:
    // :moca_row_end__
    private static final String ROW_END_TOKEN = ":" + LimitHandler.ROW_END_IDENTIFER;
    
    // Alias for the subquery table needed when calculating the total count
    private static final String MOCA_SUBQUERY_TEMP_TABLE = "moca_subquery__";
    
    // Hibernate genreated rownum identifier
    private static final String HIBERNATE_SUBQUERY_ROWNUM_IDENTIFER = "rownum_";
    
    // Hibernate generated start rownum inequality:
    // rownum_ > ?
    private static final String HIBERNATE_START_ROW_INEQUALITY = HIBERNATE_SUBQUERY_ROWNUM_IDENTIFER + " > ?";
    
    // Hibernate generated end rownum inequality:
    private static final String HIBERNATE_END_ROW_INEQUALITY = "rownum <= ?";

    // Column name used when calculating the total row count
    private static final String TOTAL_ROWS_COLUMN_IDENTIFER = "moca_total_rows__";
    
    // The list of columns to exclude (the total row column and the rownum_ column used for limiting)
    private static final List<String> EXCLUDED_COLUMNS = Collections.unmodifiableList(Arrays.asList(TOTAL_ROWS_COLUMN_IDENTIFER, HIBERNATE_SUBQUERY_ROWNUM_IDENTIFER));
}
