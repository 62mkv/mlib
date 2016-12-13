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

package com.redprairie.moca.components.databaseutil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redprairie.moca.DatabaseTool;
import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.crud.ColumnDefinition;
import com.redprairie.moca.crud.TableDefinition;
import com.redprairie.moca.crud.TableFactory;
import com.redprairie.moca.exceptions.MocaDBException;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.ArgCheck;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class DatabaseUtilService {

    /**
     * Executes the 'set savepoint' moca command.  It will set a savepoint for 
     * the current transaction and save it with the given name
     * @param moca The moca context to use with this command
     * @param savepoint The name of the savepoint
     * @throws MocaDBException If an exception is encountered while setting the
     *         savepoint
     */
    public void setSavePoint(MocaContext moca, String savepoint) throws MocaDBException {
        ArgCheck.notNull(savepoint);
        DatabaseTool dbTool = moca.getDb();
        
        try {
            dbTool.setSavepoint(savepoint);
        }
        catch (SQLException e) {
            throw new MocaDBException(e);
        }
    }
    
    /**
     * Executes the 'rollback to savepoint' moca command.  It will rollback
     * to a savepoint of the given name if it exists
     * @param moca The moca context to use with this command
     * @param savepoint The savepoint name to rollback to
     * @throws MocaDBException If there was a problem rolling back to the 
     *         desired savepoint
     */
    public void rollbackToSavePoint(MocaContext moca, String savepoint) throws MocaDBException {
        ArgCheck.notNull(savepoint);
        DatabaseTool dbTool = moca.getDb();
        
        try {
            dbTool.rollbackDB(savepoint);
        }
        catch (SQLException e) {
            throw new MocaDBException(e);
        }
    }
    
    /**
     * Executes the 'create db documentation' moca command.  This will create the
     * table documentation first and will grab arguments off the stack that match
     * the columns on the table to create column comments.
     * @param moca The moca context
     * @param tableName The table to apply the documentation to
     * @param tableComment The comment for the table
     * @throws MocaException If an exception is encountered while creating the
     *         the documentation
     */
    public void createDatabaseDocumentation(MocaContext moca, String tableName, 
            String tableComment) throws MocaException {
        ArgCheck.notNull(tableName);
        
        // If the comment is provided, first try to apply the table comment
        if (tableComment != null && tableComment.trim().length() > 0) {
            Map<String, Object> arguments = new HashMap<String, Object>();
            arguments.put("table_name", tableName);
            arguments.put("table_comment", tableComment);
            moca.executeCommand("create db table comment", arguments);
        }
        
        MocaArgument[] args = moca.getArgs();
        int argumentLength = args.length;
        
        // If we have arguments on the stack other than our normal ones then
        // we should get the columns pertinent on the table
        if (argumentLength > 0) {
            MocaResults columnResults = moca.executeCommand(
                    "[select * from " + tableName + " where 1=2]catch(-1403)");
            
            for (int i = 0; i < argumentLength; ++i) {
                MocaArgument arg = args[i];
                
                // If the column has the argument then we need to act on it
                if (columnResults.containsColumn(arg.getName())) {
                    Map<String, Object> arguments = new HashMap<String, Object>();
                    arguments.put("table_name", tableName);
                    arguments.put("column_name", arg.getName());
                    arguments.put("column_comment", arg.getValue());
                    moca.executeCommand("create db column comment", arguments);
                }
            }
        }
        
    }
    
    /**
     * Executes the "list table columns" moca command.  It will retrieve 
     * information about all of the columns on the given table.
     * @param moca The moca context
     * @param tableName The table name to get the column information about
     * @param showDetails If this is 1 column description will be returned in
     *        the result set
     * @param showPkInfo If this is 1 pk information will be provided in the
     *        result set
     * @return A Moca results containing all the information about the columns
     * @throws MocaException If any command execution exception is raised
     */
    public MocaResults listTableColumns(MocaContext moca, String tableName, 
            int showDetails, int showPkInfo, int cache) throws MocaException {
        ArgCheck.notNull(tableName);
        
        EditableResults retRes = moca.newResults();
        
        retRes.addColumn("table_name", MocaType.STRING);
        retRes.addColumn("column_name", MocaType.STRING);
        retRes.addColumn("comtyp", MocaType.STRING);
        retRes.addColumn("length", MocaType.INTEGER);
        retRes.addColumn("null_flg", MocaType.BOOLEAN);
        retRes.addColumn("pk_flg", MocaType.BOOLEAN);
        retRes.addColumn("ident_flg", MocaType.BOOLEAN);
        retRes.addColumn("column_comment", MocaType.STRING);
        
        TableDefinition tableDef = TableFactory.getTableDefinition(tableName, 
                cache != 1);
        
        List<ColumnDefinition> columnDefinitions = tableDef.getColumns();
        
        for (ColumnDefinition columnDefinition : columnDefinitions) {
            retRes.addRow();
            
            retRes.setStringValue("table_name", tableName.toLowerCase());
            retRes.setStringValue("column_name", columnDefinition.getColumnName());
            retRes.setStringValue("comtyp", Character.toString(
                    columnDefinition.getDataType().getTypeCode()));
            retRes.setIntValue("length", columnDefinition.getLength());
            retRes.setBooleanValue("null_flg", columnDefinition.isNullable());
            
            if (showPkInfo == 1) {
                retRes.setBooleanValue("pk_flg", columnDefinition.isPKField());
            }
            retRes.setBooleanValue("ident_flg", columnDefinition.isIdentity());

            // If we want to show details then get all the column comments
            if (showDetails == 1) {
                MocaResults results = null;
                try {
                    results = moca.executeCommand(
                            "list column comment" +
                            "  where table_name = '" + tableName + "'" +
                            "    and column_name = '" + columnDefinition.getColumnName() + "'");
                    
                    // If we have a row add that comment to our column comment
                    if (results.next()) {
                        retRes.setStringValue("column_comment", 
                                results.getString("column_comment"));
                    }
                }
                catch (NotFoundException ignore) {
                    // If the column comment is not found than we just ignore it
                }
            }
        }
        
        return retRes;
        
    }
    
    /**
     * Executes the 'list common columns on tables' moca command.  This will try
     * to find the shared columns between the 2 tables.  If there are any they
     * will be returned in the result set along with the comtyp, length and 
     * whether the column is nullable
     * @param moca The moca context
     * @param table1 The first table to check
     * @param table2 The second table to check
     * @return A result set containing the columns that match each other
     * @throws MocaException If there was any problem executing the commands to
     *         get the column information
     */
    public MocaResults listCommonColumnsOnTables(MocaContext moca, 
            String table1, String table2) throws MocaException {
        ArgCheck.notNull(table1);
        ArgCheck.notNull(table2);
        
        EditableResults retRes = moca.newResults();
        
        retRes.addColumn("table1", MocaType.STRING);
        retRes.addColumn("table2", MocaType.STRING);
        retRes.addColumn("column", MocaType.STRING);
        retRes.addColumn("comtyp", MocaType.STRING);
        retRes.addColumn("length", MocaType.INTEGER);
        retRes.addColumn("null_flg", MocaType.BOOLEAN);
        
        // First we get the table columns, we don't need the pk or detail
        // information and we want to use the cache just in case
        MocaResults results1 = moca.executeCommand(
                "[select * from " + table1 + " where 1=2]catch(-1403)");

        // First we get the table columns, we don't need the pk or detail
        // information and we want to use the cache just in case
        MocaResults results2 = moca.executeCommand(
                "[select * from " + table2 + " where 1=2]catch(-1403)");
        
        MocaResults comparor;
        MocaResults compareTo;
        if (results1.getColumnCount() > results2.getColumnCount()) {
            comparor = results2;
            compareTo = results1;
        }
        else {
            comparor = results1;
            compareTo = results2;
        }
        
        for (int i = 0; i < comparor.getColumnCount(); ++i) {
            String columnName = comparor.getColumnName(i);
            // If the column is shared across and we haven't found it previously
            // (duplicate column names) then we need to add it
            if (compareTo.containsColumn(columnName)) {
                retRes.addRow();
                
                retRes.setStringValue("table1", table1);
                retRes.setStringValue("table2", table2);
                retRes.setStringValue("column", columnName);
                retRes.setStringValue("comtyp", Character.toString(
                        comparor.getColumnType(i).getTypeCode()));
                retRes.setIntValue("length", comparor.getMaxLength(i));
                retRes.setBooleanValue("null_flg", comparor.isNullable(i));
            }
        }
        
        return retRes;
    }
    
    /**
     * @param moca
     * @return
     * @throws SQLException 
     */
    public MocaResults getDatabaseInfo(MocaContext moca) throws SQLException {
        EditableResults retRes = moca.newResults();
        
        retRes.addColumn("db_name", MocaType.STRING);
        retRes.addColumn("db_version", MocaType.STRING);
        retRes.addColumn("db_instance", MocaType.STRING);
        retRes.addColumn("db_user", MocaType.STRING);
        
        DatabaseMetaData metaData = moca.getDb().getConnection().getMetaData();
        
        String userName = metaData.getUserName();
        
        retRes.addRow();
        
        retRes.setStringValue("db_name", metaData.getDatabaseProductName());
        retRes.setStringValue("db_version", metaData.getDatabaseProductVersion());
        retRes.setStringValue("db_instance", metaData.getURL());
        retRes.setStringValue("db_user", userName);
        
        return retRes;
    }
    
    /**
     * Executes the "explain query" moca command.  This will explain what
     * the database was trying to do when executing the given query.  Extra 
     * details can be shown if the argument provided is not 0.
     * @param moca The moca context
     * @param query The query to explain
     * @param details Whether or not to display extra details
     * @return The moca results containing the explained query stats
     * @throws MocaException If a problem occurs while explaining the query
     */
    public MocaResults explainQuery(MocaContext moca, String query, 
            Integer details) throws MocaException {
        DatabaseTool db = moca.getDb();
        // First we have to retrieve the database type
        String dbType = db.getDbType();
        
        Connection conn = db.getConnection();
        if (dbType.equals("MSSQL")) {
            try {
                Statement stmt = conn.createStatement();
                
                try {
                    stmt.execute("set showplan_all on");
                    try {
                        // We have to disable binding for the query to explain
                        // the plan correctly
                        MocaResults planResults = moca.executeCommand("[/*#nobind*/" + query + "]");
                        
                        // Allow mapping of certain column names (coming from SQL Server)
                        // to other, more well-known names.
                        Map<String, String> columnMap = new HashMap<String, String>();
                        columnMap.put("nodeid", "id");
                        columnMap.put("parent", "parent_id");
                        columnMap.put("logicalop", "operation");
                        columnMap.put("argument", "options");
                        
                        // Create a new output result set.
                        SimpleResults results = new SimpleResults();
                        MocaUtils.transformColumns(results, planResults, false, columnMap);
                        MocaUtils.copyRowsByIndex(results, planResults);
                        
                        // The result also contain an additional column, which will be null.
                        results.addColumn("object_name", MocaType.STRING);

                        return results;
                    }
                    finally {
                        stmt.execute("set showplan_all off");
                    }
                    
                }
                finally {
                    stmt.close();
                }
            }
            catch (SQLException e) {
                throw new MocaDBException(e);
            }
        }
        else if (dbType.equals("ORACLE")) {
            try {
                Statement stmt = conn.createStatement();
                
                String sessionId;
                try {
                    boolean hasResult = stmt.execute(
                            "select 1 from dual where exists " +
                           "(select 1 from plan_table)");
                    
                    if (!hasResult) {
                        throw new MocaDBException("There was a problem " +
                        		"querying the plan table");
                    }
                    
                    ResultSet results = null;
                    try {
                        results = stmt.executeQuery(
                                "select to_char(ltrim(userenv('sessionid'))) sessionid "
                                        + "from dual");
                        
                        if (!results.next()) {
                            throw new MocaDBException("There was a problem " +
                                "querying the session id"); 
                        }
                        
                        sessionId = results.getString("sessionid");
                    }
                    finally {
                        if (results != null) {
                            results.close();
                        }
                    }
                    
                    // First we make sure there is no previous statement
                    // on our plan table
                    PreparedStatement pstmt = null;
                    try {
                        pstmt = conn.prepareStatement("delete from plan_table "
                                + "  where statement_id = ?");
                        
                        pstmt.setString(1, sessionId);
                        
                        pstmt.execute();
                    }
                    finally {
                        pstmt.close();
                    }
                    
                    // We have to use a string literal for query, we cannot use
                    // a bind variable
                    stmt.execute("explain plan " +
                    		 "  set statement_id = '" + sessionId + "'" +
                    		 "    for " + query);
                    
                }
                finally {
                    stmt.close();
                }
                
                // Details are enabled so give all information
                if (details != null && details != 0) {
                    return moca.executeCommand("[select * " +
                        "   from plan_table " +
                        "  where statement_id = '" + sessionId + "' " +
                        " order by id]");
                }
                else {
                    return moca.executeCommand(
                         "[select LPAD(' ',2*(level-1))||level||'-'||" +
                             "position||'-'||OPERATION||' '||OPTIONS||' '||OBJECT_NAME" +
                          "        QUERY_PLAN" +
                          " from   plan_table" +
                          " where statement_id = '" + sessionId + "' " +
                          " connect by " +
                          "        prior id = parent_id " +
                          " and    statement_id = '" + sessionId + "' " +
                          " start with " +
                          "        id = 1 " +
                          " and    statement_id = '" + sessionId + "' " +
                          " order by id]");   
                }
            }
            catch (SQLException e) {
                throw new MocaDBException(e);
            }
        }
        else {
            throw new UnsupportedOperationException();
        }
    }
}
