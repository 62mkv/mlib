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

package com.sam.moca;

import java.util.Date;


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
public interface RowIterator
{
    
    /**
     * Will check if there is a next row available in this result set.  Returns
     * <code>true</code> if the next row is available, or <code>false</code> if
     * the end of the result set has been reached.  Note this differs from {@link #next()}
     * in that it will not move to the next row if there is one available.
     * @return <code>true</code> if another row is available in this
     *         result set.
     */
    public boolean hasNext();
    
    /**
     * Moves to the next row in this result set.  Returns <code>true</code> if
     * the next row is available, or <code>false</code> if the end of this
     * result set has been reached.
     * @return <code>true</code> if another row is available in this
     * result set.
     */
    public boolean next();
    
    /**
     * Gets an element of the current row as a String, selecting a column by
     * position.
     * 
     * @param index the position (zero-based) of the column element to return.
     * @return the String element at the column position given by
     * <code>index</code>.
     */
    public String getString(int index);

    /**
     * Gets an element of the current row as a String, selecting a column by
     * name.
     * 
     * @param name the name of the column element to return.
     * @return the String element at the column corresponding to
     * <code>name</code>.
     */
    public String getString(String name);

    /**
     * Gets an element of the current row as an integer, selecting a column by
     * position.
     * 
     * @param index the position (zero-based) of the column element to return.
     * @return the integer element at the column position given by
     * <code>index</code>.  If the element is null, zero is returned.
     */
    public int getInt(int index);

    /**
     * Gets an element of the current row as an integer, selecting a column by
     * name. 
     * @param name the name of the column element to return.
     * @return the integer element at the column corresponding to
     * <code>name</code>.  If the element is null, zero is returned.
     */
    public int getInt(String name);

    /**
     * Gets an element of the current row as a double, selecting a column by
     * position.
     * 
     * @param index the position (zero-based) of the column element to return.
     * @return the double element at the column position given by
     * <code>index</code>.  If the element is null, 0.0 is returned.
     */
    public double getDouble(int index);

    /**
     * Gets an element of the current row as a double, selecting a column by
     * name.
     * @param name the name of the column element to return.
     * @return the boolean element at the column corresponding to
     * <code>name</code>.  If the element is null, zero is returned.
     */
    public double getDouble(String name);
    
    /**
     * Gets an element of the current row as a boolean value, selecting a
     * column by position.
     * @param index the position (zero-based) of the column element to return.
     * @return the boolean element at the column position given by
     * <code>index</code>.  If the element is null, <code>false</code> 
     * is returned.
     */
    public boolean getBoolean(int index);

    /**
     * Gets an element of the current row as a boolean value, selecting a
     * column by name.
     * @param name the name of the column element to return.
     * @return the boolean element at the column corresponding to
     * <code>name</code>.  If the element is null, false is returned.
     */
    public boolean getBoolean(String name);

    /**
     * Gets an element of the current row as a Date object, representing a
     * particular time of day, selecting a column by position. 
     * @param index the position (zero-based) of the column element to return.
     * @return the date element at the column position given by
     * <code>index</code>, or <code>null</code> if the element is null.
     */
    public Date getDateTime(int index);
    
    /**
     * Gets an element of the current row as a Date object, representing a
     * particular time of day, selecting a column by name. 
     * @param name the name of the column element to return.
     * @return the date element at the column corresponding to
     * <code>name</code>, or <code>null</code> if the element is null.
     */
    public Date getDateTime(String name);
    
    /**
     * Gets an element of the current row as a MocaResults object, representing
     * a complex nested data set, selecting a column by position. 
     * @param index the position (zero-based) of the column element to return.
     * @return the results element at the column position given by
     * <code>index</code>, or <code>null</code> if the element is null.
     */
    public MocaResults getResults(int index);
    
    /**
     * Gets an element of the current row as a MocaResults object, representing
     * a complex nested data set, selecting a column by name. 
     * @param name the name of the column element to return.
     * @return the results element at the column corresponding to
     * <code>name</code>, or <code>null</code> if the element is null.
     */
    public MocaResults getResults(String name);

    /**
     * Gets an element of the current row as an object, selecting a column by
     * position. 
     * @param index the position (zero-based) of the column element to return.
     * @return the element at the column at position <code>index</code>,
     * or <code>null</code> if the element is null.
     */
    public Object getValue(int index);

    /**
     * Gets an element of the current row as an object, selecting a column by
     * name. 
     * @param name the name of the column element to return.
     * @return the element at the column corresponding to
     * <code>name</code>, or <code>null</code> if the element is null.
     */
    public Object getValue(String name);
    
    /**
     * Indicates whether a column has a null value in the current row,
     * selecting a column by position.
     * @param index the position (zero-based) of the column element to inspect.
     * @return <code>true</code> if the column is null, <code>false</code>
     * otherwise.
     */
    public boolean isNull(int index);
    
    /**
     * Indicates whether a column has a null value in the current row,
     * selecting a column by name.
     * @param name the name of the column element to inspect.
     * @return <code>true</code> if the column is null, <code>false</code>
     * otherwise.
     */
    public boolean isNull(String name);
}
