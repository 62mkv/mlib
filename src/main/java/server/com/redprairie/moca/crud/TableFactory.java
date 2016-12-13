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

import java.util.HashMap;
import java.util.Map;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.util.MocaUtils;

/**
 * A factory class that creates and caches table definitions
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class TableFactory {

    /** Clears all table definitions from the table cache. 
     * 
     */
    public static void clearCache() {
       _tables.clear(); 
    }
    
    /** Clears the specified definition from the table cache. 
     * @param tableName The name of the table to clear.
     */
    public static void clearCache(String tableName) {
        if (_tables.containsKey(tableName))
            _tables.remove(tableName);
    }
    
    /** Gets a table definition for a table in the database
     * @param tableName The name of the table to retrieve a definition for.
     * @return A TableDefinition object or null if not found.
     * @throws NotFoundException Thrown if the table cannot be retrieved.
     */
    public static TableDefinition getTableDefinition(String tableName) throws NotFoundException {
        return getTableDefinition(tableName, false);
    }
    
    /** Gets a table definition for a table in the database
     * @param tableName The name of the table to retrieve a definition for.
     * @param noCache If true, do not get the cached table, get it from the DB
     * @return A TableDefinition object or null if not found.
     * @throws NotFoundException Thrown if the table cannot be retrieved.
     */
    public static TableDefinition getTableDefinition(String tableName, boolean noCache) throws NotFoundException {
        
        //Set table name to upper case to make searching case insensitive
        tableName = tableName.toUpperCase();
        
        synchronized (_tables) {
            if (_tables.containsKey(tableName) && !noCache)
                return _tables.get(tableName);
        }
            
        
        TableDefinition definition = createTableDefinition(tableName);
        
        if (!noCache) {
            synchronized (_tables) {
                _tables.put(tableName, definition);
            }
        }        
        
        return definition;
    }
      
    /** Creates a new table definition using the "list table columns" command as a base.
     * @param tableName The table name to retrieve.
     * @return A new TableDefinition object for that table.
     * @throws NotFoundException Thrown if the table cannot be retrieved.
     */
    private static TableDefinition createTableDefinition(String tableName) throws NotFoundException {
        MocaContext context = MocaUtils.currentContext();
        
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("tableName", tableName);
        
        try {
            
            MocaResults results = context.executeCommand(_columnsCommand, args);
            
            TableDefinition definition = new TableDefinition(tableName);
            
            //Create the base definition
            for (int index = 0; index < results.getColumnCount(); index++) {
                ColumnDefinition column = new ColumnDefinition(
                                                    results.getColumnName(index),
                                                    results.getColumnType(index),
                                                    results.getMaxLength(index),
                                                    results.isNullable(index));
                definition.addColumn(column);
            }
            
            //Get the table PK values
            try {
                results = context.executeCommand(_pkCommand, args);
                while (results.next()) {
                   String columnName = results.getString("column_name");
                   definition.setColumnAsPK(columnName, true);
                }
            }
            catch(NotFoundException e) {
                // Expected if table has no PK columns
            }
            
           //Get the table identity values
            try {
                results = context.executeCommand(_identityCommand, args);
                while (results.next()) {
                    boolean isIdentity = false;
                    
                    if (results.containsColumn("type_name")) {
                        if (results.getString("type_name").contains("identity"))
                            isIdentity = true;
                    }
                    else if (results.getColumnCount() == 1)
                        isIdentity = true;
                    
                    if (isIdentity) {
                        String columnName = results.getString("column_name");
                        definition.setColumnAsIdentity(columnName, true);
                    }
                }
            }
            catch(NotFoundException e) { 
                // Expected if table has no identity columns
            }
            
            return definition;
        }
        catch (MocaException e) {
            throw new NotFoundException();
        }
    }

    // Private static fields
    private static Map<String, TableDefinition> _tables = new HashMap<String, TableDefinition>();
    private static final String _columnsCommand = "[select * from @tableName:raw where 1=2]catch(-1403)";
    private static final String _pkCommand = "list primary key for table where table_name=@tableName catch(-1403)";
    private static final String _identityCommand = "if (dbtype() = 'MSSQL') " +
                                                   "{ [sp_columns @tableName:raw] catch(-1403) }";
                                                   
}
