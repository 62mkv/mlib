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

import org.hibernate.dialect.pagination.SQLServer2005LimitHandler;
import org.hibernate.engine.spi.RowSelection;

import com.sam.moca.db.hibernate.HibernateUtils;
import com.sam.moca.server.db.BindList;

/**
 * Handles limits (pagination) for Microsoft SQL
 * Server database engine. This essentially uses the underlying
 * Hibernate engine to transform the SQL accordingly and then additional
 * modifications are made to support MOCA's concept of bind variables.
 * 
 * An example is given the following query:
 * <pre>
 * select * from task_definition
 * </pre>
 * and the request is to start at row 25 and return 50 rows, the resulting query will be:
 * <pre>
 * WITH query AS (SELECT inner_query.*,
 *                    ROW_NUMBER () OVER (
 *                       ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__
 *               FROM (select *
 *                       from task_definition) inner_query) 
 * SELECT *
 *   FROM query
 *  WHERE __hibernate_row_nr__ >= :moca_row_start__
 *    AND __hibernate_row_nr__ < :moca_row_end__
 * </pre>
 * With the corresponding bind variables populated.<br>
 * 
 * Additionally, via the addLimit method, the total count can be specified to be added
 * to the query, this is used to calculate the total number of possible rows (without row number limits)
 * in addition to the paginated result set. An example of the same query but with the total count calculated
 * as well would look like:
 * <pre>
 * WITH query AS (SELECT inner_query.*,
 *                ROW_NUMBER () OVER (
 *                                    ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__
 *          FROM (select *
 *                  from task_definition) inner_query) 
 * SELECT (select count (*) from query) moca_total_rows,
 *        *
 *   FROM query
 *  WHERE __hibernate_row_nr__ >= :moca_row_start__
 *    AND __hibernate_row_nr__ < :moca_row_end__
 * </pre>
 * 
 * So the total count is calculated by wrapping the original query aliased as a Common Table Expression "query"
 * and doing a count on that.
 * Certain special cases are as follows:<br>
 * 1) If "order by" exists in the original SQL then TOP(<max_long>) must be added to support order by in a subquery<br><br>
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
class SQLServerLimitHandler implements LimitHandler {
    
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

    // @see com.sam.moca.server.db.translate.PageLimitHandler#addPageLimit(java.lang.String, int, int, com.sam.moca.server.db.BindList, boolean)
    @Override
    public String addLimit(String sql, int startRow, int rowLimit,
            BindList args, boolean findTotal) {
        RowSelection selection = new RowSelection();
        selection.setFirstRow(this.handleStartRow(startRow));
        selection.setMaxRows(rowLimit);
        SqlServerPagingHandler handler = new SqlServerPagingHandler(sql, selection, findTotal);
        return handlePostProcessing(handler, selection, args);
    }
    
    @Override
    public void rebindLimitVariables(int startRow, int rowLimit, BindList args) {
        int actualStartRow = handleStartRow(startRow);
        args.add(ROW_START_IDENTIFIER, actualStartRow);
        args.add(ROW_END_IDENTIFER, actualStartRow + rowLimit);
    }
    
    // @see com.sam.moca.server.db.translate.LimitHandler#handleStartRow(int)
    @Override
    public int handleStartRow(int startRow) {
        // The API uses a 0 based index where as the rownum is 1 based, so we must increment the startRow
        return startRow + 1;
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
    
    // Generates the Hibernate SQL then does bind variable replacement to work with MOCA
    private String handlePostProcessing(SqlServerPagingHandler handler,
            RowSelection selection, BindList args) {
        StringBuilder sql = new StringBuilder(handler.getProcessedSql());
        
        // At this point we have the Hibernate generated SQL that uses bindings like:
        // where __hibernate_row_nr__ >= ? and hibernate_row_nr__ < ?
        // We want to replace those ? tokens with bind variables MOCA can work with in the form
        // :<bind_variable_name> - then add this to our bind list
        
        // First replace the start row inequality
        int tokenIndex = sql.lastIndexOf(HIBERNATE_START_INQUALITY) + HIBERNATE_START_INQUALITY.length();
        sql.replace(tokenIndex - TOKEN.length(), tokenIndex, ROW_START_TOKEN);
        args.add(ROW_START_IDENTIFIER, selection.getFirstRow());

        // Next replace the max rows inequality if needed
        if (selection.getMaxRows() > 0) {
            // Handle the end row bind variable replacement
            // Find the row limit inequality: __hibernate_row_nr__ < ?
            // Replace the ? with our bind variable and the calculated end row
            tokenIndex = sql.lastIndexOf(HIBERNATE_END_INEQUALITY) + HIBERNATE_END_INEQUALITY.length();
            sql.replace(tokenIndex - TOKEN.length(), tokenIndex, ROW_END_TOKEN);
            args.add(ROW_END_IDENTIFER, selection.getFirstRow() + selection.getMaxRows());
        }
        else {
            // Max rows is 0 (unlimited) - remove the row limit inequality
            tokenIndex = sql.lastIndexOf(HIBERNATE_END_INEQUALITY);
            sql.replace(tokenIndex - " AND ".length(), sql.length(), "");
        }
        
        return sql.toString();
    }
    
    // Extension of Hibernates SQL Server Limit Handler to handle additional
    // things such as adding the total row calculation subquery and also disabling
    // the use of aliasing in the select clause
    // Notice SQLServer 2005 is used, this is the latest pagination technique provided by
    // Hibernate and is forward compatible to the newest versions of SQL Server
    private static class SqlServerPagingHandler extends SQLServer2005LimitHandler {
        
        // Since Hibernate logic assumes SQL is formatted in a certain way, we need to do special formatting of sql
        // Known issues fixed by this: [\ndistinct\n] and [\norder by\n], since Hibernate logic assumes keywords
        // are surrounded by spaces, i.e [ distinct ] and [ order by ].
        public SqlServerPagingHandler(String sql, RowSelection selection, boolean findTotal) {
            super(HibernateUtils.formatSQLForHibernate(sql), selection);
            _findTotal = findTotal;
        }
        
        /**
         * This is overridden because it determines the select clause to be used which by default
         * Hibernate tries to alias all the columns which is unneeded, instead we just do "select *" -
         * If we're trying to find the total we also append a subquery that calculates the total row count.
         * @param sb The query to modify (unneeded here)
         * @return The select clause to use
         */
        @Override
        protected String fillAliasInSelectClause(StringBuilder sb) {
            if (_findTotal) {
                // For find total we want our select clause to be:
                // select <moca_count_subquery>, *
                return SUBCOUNT_QUERY_WITH_SELECT_ALL;
            }
            return "*";
        }
        
        /**
         * This is overridden to substitute TOP(?) with TOP(Long.MAX_VALUE)
         * Top gets added to the expression if there is an "order by" in the original
         * SQL statement because SQL Server does not support sub queries with order by
         * in them without the use of TOP
         * @param sql The sql statement that is being modified
         */
        @Override
        protected void addTopExpression(StringBuilder sql) {
            super.addTopExpression(sql);
            // Replace the TOP(?) token with Long.MAX_VALUE
            // max long is used because the only reason top is added is as a workaround
            // due to the limitations of having an order by in the subselect
            // Note TOP(100) PERCENT does not respect the order by in a subselect
            int tokenIndex = sql.indexOf("TOP(?)") + 4;
            sql.replace(tokenIndex, tokenIndex + 1, LONG_MAX_VALUE_AS_STRING);
        }

        private final boolean _findTotal;
    }
    
    // Most of the constants below are used for string replacement going from the Hibernate generated
    // query to something MOCA can work with.
    
     // Bind variable token used by Hibernate generated SQL
    private static final String TOKEN = "?";
    
    
    // Bind variable token used for row start:
    // :moca_row_start__
    private static final String ROW_START_TOKEN = ":" + LimitHandler.ROW_START_IDENTIFIER;
    
    // Bind variable token used for row end:
    // :moca_row_end__
    private static final String ROW_END_TOKEN = ":" + LimitHandler.ROW_END_IDENTIFER;
    
    // Identifier used by Hibernate engine for row number
    private static final String HIBERNATE_ROW_ARG_IDENTIFIER = "__hibernate_row_nr__";
    
     // Identifier for the row start inequality by the Hibernate generated SQL:
     // __hibernate_row_nr__ >= ?
    private static final String HIBERNATE_START_INQUALITY = HIBERNATE_ROW_ARG_IDENTIFIER + " >= " + TOKEN;
    
    // The full end row inequality String generated by Hibernate SQL engine:
    // __hibernate_row_nr < ?
    private static final String HIBERNATE_END_INEQUALITY = HIBERNATE_ROW_ARG_IDENTIFIER + " < " + TOKEN;
    
     // Alias used for the column when calculating the total # or rows
    private static final String TOTAL_ROWS_COLUMN_IDENTIFER = "moca_total_rows__";
    
    // Subquery that is used to calculate the count of the original query
    private static final String SUBCOUNT_QUERY = "(select count(*) from query) " + TOTAL_ROWS_COLUMN_IDENTIFER;
    
    // Subquery with the * added to the select clause to return all other rows
    private static final String SUBCOUNT_QUERY_WITH_SELECT_ALL = SUBCOUNT_QUERY + ", *";
    
    private static String LONG_MAX_VALUE_AS_STRING = String.valueOf(Long.MAX_VALUE);
    
    // The columns to exclude from result sets (the total row column and the row number column)
    private static final List<String> EXCLUDED_COLUMNS = Collections.unmodifiableList(Arrays.asList(TOTAL_ROWS_COLUMN_IDENTIFER, HIBERNATE_ROW_ARG_IDENTIFIER));
}
