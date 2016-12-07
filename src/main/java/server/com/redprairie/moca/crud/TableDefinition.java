/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

package com.redprairie.moca.crud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A meta-data representation of a table in the database.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class TableDefinition {

    /** Creates a new TableDefinition instance
     * @param tableName The name of the table
     */
    public TableDefinition(String tableName) {
        this.tableName = tableName;
    }
    
    /** Adds a column to the table
     * @param column The column to add
     */
    void addColumn(ColumnDefinition column) {
        if (column != null) {
            columnMap.put(column.getColumnName().toUpperCase(), column);        
            columns.add(column);
        }
    }
    
    /** Gets the table name
     * @return The table name as a string
     */
    public String getTableName() {
        return tableName;
    }
    
    /** Gets a specific column by name from the table
     * @param columnName The name of the column to locate
     * @return A column definition or null if not found
     */
    public ColumnDefinition getColumn(String columnName) {
        
        if (columnName == null || columnName.equals(""))
            return null;
        
        columnName = columnName.toUpperCase();
        if (columnMap.containsKey(columnName))
            return columnMap.get(columnName);
        
        return null;
    }
    
    /** Gets a list of columns for the table.
     * @return A list of column definitions
     */
    public List<ColumnDefinition> getColumns() {
        return columns;
    }
    
    /** Gets a list of primary key fields for the table.
     * @return A list of column definitions
     */
    public List<ColumnDefinition> getPKFields() {

        List<ColumnDefinition> pkList = new ArrayList<ColumnDefinition>();
        
        for (ColumnDefinition column: columns){
            if (column.isPKField())
                pkList.add(column);
        }
        
        return pkList;
    }
    
    /** Gets the list of required (non-null or PK) columns for the table.
     * @return A list of column definition
     */
    public List<ColumnDefinition> getRequiredFields()
    {
        List<ColumnDefinition> reqList = new ArrayList<ColumnDefinition>();
        
        for (ColumnDefinition column: columns){
            if (column.isRequired() || column.isPKField())
                reqList.add(column);
        }
        
        return reqList;
    }
    
    /** Sets a particular column as being an identity column based on "value"
     * @param columnName The column name to set
     * @param value The value to set it to
     */
    void setColumnAsIdentity(String columnName, boolean value) {
        ColumnDefinition column = getColumn(columnName);
        
        if (column != null)
            column.setIdentity(value); 
    }
    
    /** Sets a particular column as being a PK column based on "value"
     * @param columnName The column name to set
     * @param value The value to set it to
     */
    void setColumnAsPK(String columnName, boolean value) {
        ColumnDefinition column = getColumn(columnName);
        
        if (column != null)
            column.setPKField(value);
    }
    
    // Private Fields
    private String tableName;
    private List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
    private Map<String, ColumnDefinition> columnMap = new HashMap<String, ColumnDefinition>();
}
