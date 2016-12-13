/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.redprairie.moca.crud;

import com.redprairie.moca.MocaType;

/**
 * A meta-data representation of a table column in the database
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class ColumnDefinition {
    
    /** Creates a new ColumnDefinition instance.
     * @param columnName The name of the column.
     */
    public ColumnDefinition(String columnName){
        this.columnName = columnName;
    }
    
    /** Creates a new ColumnDefinition instance.
     * @param columnName The name of the column.
     * @param dataType The column's comp data type
     * @param length The column length
     * @param isNullable Indicates if the column is nullable.
     * @param isPKField Indicates if the column is a PK field
     * @param isIdentity Indicates if the column is an identity field.
     */
    public ColumnDefinition(String columnName, String dataType, int length, 
                            boolean isNullable, boolean isPKField, boolean isIdentity){
        this(columnName);
        
        if (dataType != null && dataType.length() == 1)
                this.dataType = MocaType.lookup(dataType.charAt(0));
        
        this.length = length;
        this.isNullable = isNullable;
        this.isPKField = isPKField;
        this.isIdentity = isIdentity;
    }
    
    /** Creates a new ColumnDefinition instance.
     * @param columnName The name of the column.
     * @param dataType The column's MocaType data type
     * @param length The column length
     * @param isNullable Indicates if the column is nullable.
     */
    public ColumnDefinition(String columnName, MocaType dataType, int length, 
                            boolean isNullable){
        this(columnName);
        
        this.dataType = dataType;
        this.length = length;
        this.isNullable = isNullable;
    }
    
    /** Gets the column name
     * @return The column name
     */
    public String getColumnName() {
        return columnName;
    }
    
    /** Gets the column's data type
     * @return The column type as a MocaType
     */
    public MocaType getDataType() {
        return dataType;
    }
    
    /** Gets the column's length
     * @return The column length or -1 if undefined.
     */
    public int getLength() {
        return length;
    }
    
    /** Indicates if the column's values can be null
     * @return true if they are nullable; otherwise false.
     */
    public boolean isNullable() {
        return isNullable;
    }
    
    /** Indicates if the column is a primary key field based on the index.
     * @return true if it is a primary key; otherwise false.
     */
    public boolean isPKField() {
        return isPKField;
    }
       
    /** Indicates if the column is a identity field.
     * @return true if it is an identity field; otherwise false.
     */
    public boolean isIdentity() {
        return isIdentity;
    }
    
    /** Indicates if the field is required meaning it is
     *  not nullable and not an identity field.
     * @return true if it is required; otherwise false.
     */
    public boolean isRequired() {
        return !isNullable && !isIdentity;
    }
    
    /** Internal method that sets the column as a PK field
     *  @param value The true or false value to set
     */
    void setPKField(boolean value) {
        isPKField = value;
    }
    
    /** Internal method that sets the column as a PK field
     *  @param value The true or false value to set
     */
    void setIdentity(boolean value) {
        isIdentity = value;
    }
    
    // Private fields
    private String columnName;
    private MocaType dataType;
    private int length = -1;
    private boolean isNullable = true;
    private boolean isPKField = false;
    private boolean isIdentity = false;
    
}
