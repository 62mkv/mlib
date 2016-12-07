/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca;

import java.util.Map;

/**
 * A MOCA results object that can be manipulated. This interface adds the
 * ability to add columns and rows to a <code>MocaResults</code> object, as
 * well as set values programmatically. The main difference between this kind of
 * results and <code>ModifiableResults</code> is this it is possible to define
 * columns using this interface.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public interface EditableResults extends ModifiableResults {

    /**
     * Add a column to the current result set.
     * 
     * @param columnName the name of the column to add
     * @param type the <code>MocaType</code> of the column.
     */
    public void addColumn(String columnName, MocaType type);

    /**
     * Add a column to the current result set.
     * 
     * @param columnName the name of the column to add
     * @param type the <code>MocaType</code> of the column.
     * @param length the defined maximum length of the column. This argument is
     *            ignored for some column types. A length of zero implies an
     *            unlimited length for strings.
     */
    public void addColumn(String columnName, MocaType type, int length);

    /**
     * Add a column to the current result set.
     * 
     * @param columnName the name of the column to add
     * @param type the <code>MocaType</code> of the column.
     * @param length the defined maximum length of the column. This argument is
     *            ignored for some column types. A length of zero implies an
     *            unlimited length for strings.
     * @param nullable <code>true</code> if the column is a nullable column.  The
     *            nullable column attribute is purely informational, and does not
     *            suggest that non-null enforcement will occur upon column, nor
     *            does it imply that <code>null</code> will never be returned from
     *            a call to one of methods to get a column's value for a row.  
     */
    public void addColumn(String columnName, MocaType type, int length, boolean nullable);
    
    /**
     * Adds a row, either reusing or adding columns as necessary.  A row is added to the
     * result set, and the Map of data is used to both indicate the columns to be added
     * and to associate data with the new row.  If the result set does not contain the
     * columns specified in the given <code>Map</code> object, then columns are added to
     * the result set.  The type of each column is determined from the values in the given
     * <code>Map</code> object.  If the value given is <code>null</code>, then a type of
     * <code>String</code> is assumed.  This can lead to problems if a non-string type is
     * intended, but the first value is <code>null</code>.
     * 
     * @param rowData a mapping of data to be added to the new row.
     */
    public void addRow(Map<String, Object> rowData);
    
    /**
     * Sets the values on the current row. The Map of data is used to both
     * indicate the columns to be added and to associate data with the new row.
     * If the result set does not contain the columns specified in the given
     * <code>Map</code> object, then columns are added to the result set. The
     * type of each column is determined from the values in the given
     * <code>Map</code> object. If the value given is <code>null</code>, then a
     * type of <code>String</code> is assumed. This can lead to problems if a
     * non-string type is intended, but the first value is <code>null</code>.
     * This will overwrite existing data contained in the row if the result set
     * has the column in it previously.
     * 
     * @param rowData a mapping of data to be added to the existing row.
     */
    public void setValues(Map<String, Object> rowData);
}
