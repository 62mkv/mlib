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

import java.util.List;

import com.sam.moca.server.db.BindList;

/**
 * A LimitHandler handles modifying SQL statements to handle
 * pagination type requests. The limit handler should be implemented
 * for a specific database engine.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public interface LimitHandler {
	
   /**
     * Bind variable identifier used for MOCA start row
     */
    public static final String ROW_START_IDENTIFIER = "moca_row_start__";
    
    /**
     * Bind variable identifier used for MOCA end row
     */
    public static final String ROW_END_IDENTIFER = "moca_row_end__";
    
    /**
     * Takes the original SQL statement which has already been translated to the given database
     * dialect with bind variables in Oracle fashion e.g. value = :bind_variable and transforms the
     * SQL statement into one that will limit the result set rows given the start row and row limit.
     * The start row is zero based. Additionally, if findTotal is specified the query should be
     * modified to calculate the total number of possible rows that would have been returned
     * without the pagination limits. This should be added as an additional column in the result
     * set using the column name specified by {@link #getTotalColumnName()}.
     * @param sql The original SQL statement to be modified
     * @param startRow The start row (0 based)
     * @param rowLimit The row limit
     * @param args The BindList args to add to if needed
     * @param findTotal Indicates whether the query should be modified to find the total
     *        number of rows possible as if the limits were not applied.
     * @return The modified SQL statement to support limiting
     */
    public String addLimit(String sql, int startRow, int rowLimit, BindList args, boolean findTotal);
    
    /**
     * Rebinds the limit variables given the new startRow and rowLimit adding to the bind list.
     * @param startRow The start row (0 based)
     * @param rowLimit The row limit
     * @param args The BindList to add to
     */
    public void rebindLimitVariables(int startRow, int rowLimit, BindList args);
    
    /**
     * Gets a list of columns to exclude from result sets when using this LimitHandler. Typically
     * this will include any rownumber column needed for calculating the limit and the column
     * that is for calculating the total possible rows.
     * @return
     */
    public List<String> getExcludedColumns();
    
    /**
     * The name of the column used when findTotal is specified in {@link #addLimit(String, int, int, BindList, boolean)}
     * @return
     */
    public String getTotalColumnName();
    
    /**
     * Start row is zero based, this handles modifying the start row if your SQL dialect does not use a
     * zero based row numbering scheme in it's limit calculation.
     * @param startRow The zero based start row
     * @return The modified start row
     */
    public int handleStartRow(int startRow);
    
    /**
     * Indicates if the LimitHandler actually supports modifying queries for limits
     * @return
     */
    public boolean supportsLimit();
    
    /**
     * Indicates if the LimitHandler actually supports modifying the query to retrieve the total count
     * @return
     */
    public boolean supportsTotalCount();

}
