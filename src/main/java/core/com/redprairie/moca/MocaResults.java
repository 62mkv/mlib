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

package com.redprairie.moca;

/**
 * Interface describing a MOCA result set, a data structure consisting of
 * rows of named columns of typed data.  Many of the methods to get values
 * from the result set work on a concept of a "current" row.  If no current
 * row has been established, through calling the next() method, or all of the
 * rows of the result set have been exhausted, any of the methods that work on
 * the current row (the <code>get*Value</code> methods, for instance) will throw
 * {@link java.lang.IllegalStateException}.  Similarly, if the
 * {@link #close close} method has been called, all other methods can throw  
 * {@link java.lang.IllegalStateException}.
 * 
 * <h2>Column Names</h2>
 * 
 * It is legal, with some implementations of MocaResults, to have two columns
 * with the same name.  All of the methods that operate on a column name,
 * therefore, have the potential of being ambiguous.  In cases where two
 * columns have the same name, the implemention should return the column
 * that comes first in numerical order.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
public interface MocaResults extends RowIterator
{
    /**
     * Resets the "current" row of this result set to its initial state.  It
     * is possible to fetch all the rows of a result set, reset it, then fetch
     * the results again.
     */
    public void reset();

    /**
     * Returns the MOCA <code>ColumnType</code> object associated with a
     * particular column, selecting the column by position.
     * 
     * @param index the position (zero-based) of the column element to inspect.
     * @return a ColumnType object indicating the defined datatype of the
     * column.
     */
    public MocaType getColumnType(int index);

    /**
     * Returns the MOCA <code>ColumnType</code> object associated with a
     * particular column, selecting the column by name.
     * 
     * @param name the name of the column to inspect.
     * @return a ColumnType object indicating the defined datatype of the
     * column.
     */
    public MocaType getColumnType(String name);
    

    /**
     * Returns the defined maximum length of a data column.  If no maximum can
     * be determined, zero is returned.
     * @param index the position (zero-based) of the column to examine.
     * @return the defined maximum length of the column, or zero if no maximum
     * has been defined.
     */
    public int getMaxLength(int index);
    
    /**
     * Returns the defined maximum length of a data column.  If no maximum can
     * be determined, zero is returned.
     * @param name the name of the column to examine.
     * @return the defined maximum length of the column, or zero if no maximum
     * has been defined.
     */
    public int getMaxLength(String name);

    /**
     * Determines if a column is nullable.
     * 
     * @param index the position (zero-based) of the column element to examine.
     * @return <code>true</code> if the given column is marked as nullable,
     * <code>false<code> otherwise.
     */
    public boolean isNullable(int index);
    
    /**
     * Determines if a column is nullable.
     * 
     * @param name the name of the column element to examine.
     * @return <code>true</code> if the given column is marked as nullable,
     * <code>false<code> otherwise.
     */
    public boolean isNullable(String name);

    /**
     * Gets the name of a column, or NULL if the result set does not contain
     * the appropriate number of columns.
     * @param index the column position (zero-bsed) to look up.
     * @return the name of column <code>index</code>.  If this result set
     * does not contain (<code>index - 1</code>) columns, this method returns
     * <code>null</code>.
     */
    public String getColumnName(int index);

    /**
     * Finds the column number associated with a given column name.  The column
     * name lookup is done with case-insensitive matching, and the first
     * column, positionally, is returned that has the given name.
     * @param name the name of the column to look up.
     * @return the column position of the first column with the given name, or
     * -1 if the column does not exist.
     */
    public int getColumnNumber(String name);
    
    /**
     * Determines if this result object contains a column of the given name.
     * 
     * @param name the name of the column to look up.
     * @return <code>true</code> if this results object contains the given
     * column, <code>false</code> otherwise.
     */
    public boolean containsColumn(String name);

    /**
     * Gets the number of columns defined in this result set.
     * @return  the number of columns in this result set.
     */
    public int getColumnCount();
    
    /**
     * Gets the number of rows defined in this result set.
     * 
     * @return the number of rows in the result set.
     */
    public int getRowCount();
    
    /**
     * Gets an iterator for the data in this result set.
     * @return an iterator that can inspect the data in this result set.
     */
    public RowIterator getRows();
    
    /**
     * Closes this result set.  All result sets should be closed by the caller.
     * Failing to close a result set can cause allocated memory to not be freed
     * in a timely manner.  After calling this method, the result set is in an
     * invalid state, and all other methods may throw IllegalStateException.
     * The close method may be called twice, and no exception will be thrown.
     */
    public void close();
}
