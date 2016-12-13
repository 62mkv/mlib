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

import java.util.Date;

/**
 * A MOCA results object that can be manipulated.  This interface adds the
 * ability to add rows to a <code>MocaResults</code> object, as
 * well as set values programmatically.  It is not possible, using this 
 * interface to add columns to a result set.
 * 
 * An editable result set has an internal notion of the "current" edit row.
 * Depending on the operations that have been performed on the result set, 
 * the edit row may or may not be the same as the more general access row
 * that is set by calling the <code>reset</code> and <code>next</code> methods.
 * In particular, if the <code>addRow</code> method is called, the new row
 * becomes the current edit row, and the current access row is reset.  Calling
 * next() will set the access row <strong>and the edit row</strong> to the first
 * row in the result set.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public interface ModifiableResults extends MocaResults {
    /**
     * Add a new row to the current result set.  If the result set is
     * currently being traversed, this will have the same effect as
     * calling reset(), namely reset the row iteration.  I.e. after calling
     * this method, the next call to next() will make this results
     * object point to the first available row. 
     */
    public void addRow();

    /**
     * Remove the current row from the current result set.
     */
    public void removeRow();

    /**
     * Sets a binary column value by column number.
     * @param num the number of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setBinaryValue(int num, byte[] value);

    /**
     * Sets a binary column value by name.
     * @param name the name of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setBinaryValue(String name, byte[] value);

    /**
     * Sets a boolean column value by column number.
     * @param num the number of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setBooleanValue(int num, boolean value);

    /**
     * Sets a boolean column value by name.
     * @param name the name of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setBooleanValue(String name, boolean value);

    /**
     * Sets a date column value by column number.
     * @param num the number of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setDateValue(int num, Date value);

    /**
     * Sets a date column value by name.
     * @param name the name of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setDateValue(String name, Date value);

    /**
     * Sets a double column value by column number.
     * @param num the number of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setDoubleValue(int num, double value);

    /**
     * Sets a double column value by name.
     * @param name the name of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setDoubleValue(String name, double value);

    /**
     * Sets a integer column value by column number.
     * @param num the number of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setIntValue(int num, int value);

    /**
     * Sets a integer column value by name.
     * @param name the name of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setIntValue(String name, int value);

    /**
     * Sets a column's value to null in the current row.
     * @param num the number of the column you wish to set.
     */
    public void setNull(int num);

    /**
     * Sets a column's value to null in the current row.
     * @param name the name of the column you wish to set.
     */
    public void setNull(String name);

    /**
     * Sets a string column value by column number.
     * @param num the number of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setStringValue(int num, String value);

    /**
     * Sets a string column value by name.
     * @param name the name of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setStringValue(String name, String value);
    
    /**
     * Sets a results column value by column number.
     * @param num the number of the column you wish to set.
     * @param value the results object to place into the current row.
     */
    public void setResultsValue(int num, MocaResults value);

    /**
     * Sets a results column value by name.
     * @param name the name of the column you wish to set.
     * @param value the results object to place into the current row.
     */
    public void setResultsValue(String name, MocaResults value);

    /**
     * Sets a column value by column number.
     * @param num the number of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setValue(int num, Object value);

    /**
     * Sets a column value by name.
     * @param name the name of the column you wish to set.
     * @param value the value to give the column in the current row.
     */
    public void setValue(String name, Object value);
}
