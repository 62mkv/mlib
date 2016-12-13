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

package com.redprairie.moca.components.databaseupgrade;

import java.util.ArrayList;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.util.MocaUtils;

/**
 * TODO Class Description
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author sehlke
 * @version $Revision$
 */
public class DatabaseUpgradeService {

    /**
     * Creates scripts for creating and dropping foreign keys
     * 
     * @param moca The moca context
     * @param table The database table
     * @return The moca results containing the add and drop scripts
     * @throws MocaException If a problem occurs while explaining the query
     */
    public MocaResults processDBUnicodeConversion(MocaContext moca,
                                                  String table_name)
            throws MocaException {

        String dbType = getDBType(moca);

        MocaResults resFK = null;
        try {
            resFK = scriptForeignKeysInt(moca, dbType, table_name, 1);

        }
        catch (NotFoundException notFound) {
            resFK = null;
        }

        // Create results for returning converted columns
        EditableResults convertedColumns = moca.newResults();
        convertedColumns.addColumn("table_name", MocaType.STRING);
        convertedColumns.addColumn("column_name", MocaType.STRING);
        convertedColumns.addColumn("old_datatype", MocaType.STRING);
        convertedColumns.addColumn("new_datatype", MocaType.STRING);
        if (table_name != null) table_name = table_name.toUpperCase();

        try {
            String cmd = "list user tables ";
            if (table_name != null && !table_name.equals(""))
                cmd += " where table_name = '" + table_name + "' ";

            MocaResults resTables = moca.executeCommand(cmd);

            while (resTables != null && resTables.next()) {
                String table = resTables.getString("table_name");
                
                boolean hasIdentity = false;
                if (dbType.equals("MSSQL")) {
                    hasIdentity = getTableHasIdentity(moca, dbType, table);
                }
                
                /*
                 * We can skip this table if it is a table whose sole purpose is to 
                 * represent a sequence.
                 */

                if (dbType.equals("MSSQL") && hasIdentity && isMSSQLSequenceTable(moca, table)) {
                    continue;
                }
                
                MocaResults resColumns = getColumnsToConvert(moca, dbType, hasIdentity,
                    table);
                
                if (resColumns != null && resColumns.getRowCount() > 0) {
                    // remove constraints

                    MocaResults resConstraints = scriptConstraintsInt(moca,
                        dbType, table, 1);
                    MocaResults resIndexes = scriptIndexesInt(dbType, table, 1);

                    if (dbType.equals("ORACLE")) {
                        convertOracleColumnsToUnicode(moca, table, resColumns,
                            convertedColumns);
                    }
                    else if (dbType.equals("MSSQL")) {                      
                        convertMSSQLTableToUnicode(moca, table, resColumns, hasIdentity,
                            convertedColumns);
                    }

                    if (resConstraints != null) {
                        // add the constraints back
                        while (resConstraints.next()) {
                            // add the constraint
                            moca.executeCommand("["
                                    + resConstraints.getString("add_script")
                                    + "]");

                        }
                    }
                    if (resIndexes != null && resIndexes.getRowCount() > 0) {

                        while (resIndexes.next()) {
                            moca.executeCommand("["
                                    + resIndexes.getString("add_script") + "]");
                        }

                    }
                }
            }
        }
        catch (MocaException ex) {
            throw ex;

        }
        finally {
            if (resFK != null) {
                while (resFK.next()) {
                    moca.executeCommand("[" + resFK.getString("add_script")
                            + "]");
                }
            }
        }
        return convertedColumns;

    }
    private boolean getTableHasIdentity(MocaContext moca, String dbType, String tableName)
        throws MocaException {
        
        if (tablesWithIdentity == null) {
            tablesWithIdentity = new ArrayList<String>();
        
            MocaResults res = moca.executeCommand(
                "[ /*#NOBIND*/ select OBJECT_NAME(object_id) table_name "
                    + " from SYS.IDENTITY_COLUMNS "
                    +  " ] catch (-1403) ");
            while (res.next()) {
                tablesWithIdentity.add(res.getString("table_name").toLowerCase());           
            }
        }
        
        return tablesWithIdentity.contains(tableName.toLowerCase());
        
    }

    private MocaResults getColumnsToConvert(MocaContext moca, String dbType, boolean hasIdentity,
                                            String tableName) {
        MocaResults resCols = null;

        try {
            String sCmd = "";
            if (dbType.equals("ORACLE")) {
                sCmd = String
                    .format(
                        "[select column_name, data_type, data_length, nullable, "
                                + " column_id, char_col_decl_length, char_length, char_used "
                                + "  FROM user_tab_columns "
                                + " WHERE table_name = '%s' "
                                + "   AND ( (data_type = 'VARCHAR2' and char_used = 'B' and data_length < 4000 ) "
                                + "        OR (data_type in ('LONG', 'LONG RAW') and char_used is null) ) ]",
                        tableName.toUpperCase());
            }
            else if (dbType.equals("MSSQL")) {
                /* Check for identity columns.  If we have one, we need to get
                 * All columns because we need to do an insert with a list of columns
                 */
                
              
                sCmd = String
                    .format(" [/*#NOBIND*/  select c.column_name column_name, "
                                + " decode(c.is_nullable, 'NO', 'NOT NULL', 'NULL') nullable, "
                                + "  c.data_type data_type, "
                                + " c.character_maximum_length data_length, "
                                + " c.numeric_precision  data_precision, "
                                + " c.numeric_scale data_scale, "
                                + " c.character_octet_length byte_length "
                                + " from information_schema.columns c, sysobjects o "
                                + " where c.table_name = o.name "
                                + " and o.xtype = 'U' "
                                + " and o.id = object_id('%s') "
                                + ((hasIdentity == false) ? " and c.data_type in ('varchar', 'text') " : "")
                                + " order by c.ordinal_position ]", tableName
                            .toLowerCase());

            }
            resCols = moca.executeCommand(sCmd);
        }
        catch (MocaException ex) {

        }
        return resCols;

    }

    private void convertOracleColumnsToUnicode(MocaContext moca,
                                               String tableName,
                                               MocaResults resCols,
                                               EditableResults returnResults)
            throws MocaException {

        String scriptCmd = String.format(
            "[ /*#NOBIND*/ alter table %s modify ( ", tableName);

        while (resCols != null && resCols.next()) {

            String newDataType = "";

            String column = resCols.getString("column_name").toLowerCase();
            String currentDataType = resCols.getString("data_type");
            int length = resCols.getInt("char_length");

            if (currentDataType.equals("VARCHAR2")) {
                if (length >= 1000) {

                    // Originally we were converting anything 1000 bytes or
                    // larger to clob. Then we figured out that you can't order
                    // by clobs,
                    // so we changed this to ignore mls_text columns.
                    // Then we learned you can't use clob columns in a UNION
                    // select, so we're not enabling clobs for any columns at
                    // this point.
                    // At least not automatic conversions. If products deem a
                    // CLOB type is best, they still have the ability to convert
                    // in their
                    // own scripts
                    // if (!column.equals("mls_text"))
                    // {
                    //					
                    // // Must rename original column and recreate the column
                    // // as the correct data type.
                    // alterCmd = String.format(
                    // "[	/*#NOBIND*/ alter table %s rename column %s to %s_tmp ]"
                    // + "	| "
                    // + " [ /*#NOBIND*/ alter table %s add (%s clob %s ) ] "
                    // + " | "
                    // +
                    // " [ /*#NOBIND*/ update %s set %s = %s_tmp] catch (-1403) "
                    // + " | "
                    // + " [ /*#NOBIND*/ alter table %s drop column %s_tmp ] ",
                    // tableName, column, column,
                    // tableName, column,
                    // (resCols.getString("nullable").equals("NOT NULL") ?
                    // " NOT NULL" : ""),
                    // tableName, column, column,
                    // tableName, column);
                    // newDataType = "clob";
                    // }
                    // else
                    // {
                    // This column should not be a clob as we need to be able to
                    // sort it
                    newDataType = " varchar2(4000) ";
                    // }

                }
                else {
                    newDataType = String.format(" varchar2(%d CHAR) ", length);
                }

            }
            else if (currentDataType.equals("LONG")) {
                newDataType = " clob";
            }
            else {
                newDataType = " blob";
            }


            String alterCmd = scriptCmd + column + newDataType + " ) ] ; [commit]";

            try {
                moca.executeCommand(alterCmd);
            }
            catch (MocaException ex) {
                throw ex;
            }

            returnResults.addRow();
            returnResults.setStringValue("table_name", tableName);
            returnResults.setStringValue("column_name", column);
            returnResults.setStringValue("old_datatype", currentDataType);
            returnResults.setStringValue("new_datatype", newDataType);

        }

    }

    /**
     * Creates scripts for creating and dropping foreign keys
     * 
     * @param moca The moca context
     * @param table The database table
     * @return The moca results containing the add and drop scripts
     * @throws MocaException If a problem occurs while explaining the query
     */
    public MocaResults scriptForeignKeys(MocaContext moca, String table)
            throws MocaException {

        try {
            String dbType = getDBType(moca);
            return scriptForeignKeysInt(moca, dbType, table, 0);
        }
        catch (MocaException ex) {
            throw ex;
        }
    }

    private MocaResults scriptForeignKeysInt(MocaContext moca, String dbType,
                                             String table, Integer processDrop)
            throws MocaException {

        String cmd = "list foreign keys";

        if (table != null && !table.equals(""))
            cmd += String.format(" where table_name = '%s' ", table);
        MocaResults resFK = moca.executeCommand(cmd);

        String indexName = "";
        String fkTable = "";
        String pkTable = "";
        ArrayList<String> columnList = new ArrayList<String>();
        String onDelete = "";
        String onUpdate = "";

        EditableResults resReturn = moca.newResults();
        resReturn.addColumn("fk_name", MocaType.STRING);
        resReturn.addColumn("drop_script", MocaType.STRING);
        resReturn.addColumn("add_script", MocaType.STRING);

        while (resFK != null && resFK.next()) {
            if (!indexName.equals(resFK.getString("fk_name"))) {

                if (!indexName.equals("")) {
                    /* we have a new record, push this info into our array */
                    resReturn.addRow();
                    resReturn.setStringValue("fk_name", indexName);
                    String scripts[] = getForeignKeyScripts(dbType, indexName,
                        fkTable, columnList, pkTable, onDelete, onUpdate);
                    resReturn.setStringValue("drop_script", scripts[0]);
                    resReturn.setStringValue("add_script", scripts[1]);
                    if (processDrop != null && processDrop != 0) {
                        moca
                            .executeCommand("[" + scripts[0]
                                    + "] catch (-3728) "
                                    + "| if (@? = 0) [commit]");
                    }
                }

                columnList.clear();
                columnList.add(resFK.getString("fkcolumn_name"));
                indexName = resFK.getString("fk_name");
                fkTable = resFK.getString("fktable_name");
                pkTable = resFK.getString("pktable_name");
                onUpdate = resFK.getString("on_update_action");
                onDelete = resFK.getString("on_delete_action");

            }
            else {
                columnList.add(resFK.getString("fkcolumn_name"));
            }
        }

        if (!indexName.equals("")) {
            /* we have a new record, push this info into our array */
            resReturn.addRow();
            resReturn.setStringValue("fk_name", indexName);
            String scripts[] = getForeignKeyScripts(dbType, indexName, fkTable,
                columnList, pkTable, onDelete, onUpdate);
            resReturn.setStringValue("drop_script", scripts[0]);
            resReturn.setStringValue("add_script", scripts[1]);
            if (processDrop != null && processDrop != 0) {
                moca.executeCommand("[" + scripts[0] + "] catch (-3728) "
                        + "| if (@? = 0) [commit]");
            }
        }

        return resReturn;
    }

    /**
     * Creates scripts for creating and dropping constraints
     * 
     * @param moca The moca context
     * @param table The database table
     * @return The moca results containing the add and drop scripts
     * @throws MocaException If a problem occurs while explaining the query
     */
    public MocaResults scriptConstraints(MocaContext moca, String table)
            throws MocaException {
        try {
            String dbType = getDBType(moca);
            return scriptConstraintsInt(moca, dbType, table, 0);
        }
        catch (MocaException ex) {
            throw ex;
        }
    }

    private String getDBType(MocaContext moca) throws MocaException {

        // First we have to retrieve the database type
        MocaResults res = moca.executeCommand("publish data "
                + "  where dbtype = dbtype()");
        res.next();

        return res.getString("dbtype");
    }

    private MocaResults scriptConstraintsInt(MocaContext moca, String dbType,
                                             String table, Integer processDrop)
            throws MocaException {

        ArrayList<String> constNames = new ArrayList<String>();

        boolean isOracle = dbType.equals("ORACLE");

        String cmd = "list table constraints ";

        if (table != null && !table.equals(""))
            cmd += String.format(" where table_name = '%s' ", table);
        MocaResults res = moca.executeCommand(cmd);

        String constraintName = "";
        String type = "";
        String tableName = "";
        String definition = "";
        boolean isNotNull = false;
        ArrayList<String> columnList = new ArrayList<String>();

        EditableResults resReturn = moca.newResults();
        resReturn.addColumn("constraint_name", MocaType.STRING);
        resReturn.addColumn("drop_script", MocaType.STRING);
        resReturn.addColumn("add_script", MocaType.STRING);

        while (res != null && res.next()) {
            if (!constraintName.equals(res.getString("constraint_name"))) {
                if (!isOracle || !isNotNull) {
                    if (!constraintName.equals("")
                            && constNames.contains(constraintName) == false) {
                        resReturn.addRow();
                        resReturn.setStringValue("constraint_name",
                            constraintName);
                        String scripts[] = getConstraintScripts(dbType,
                            constraintName, tableName, type, columnList,
                            definition);

                        resReturn.setStringValue("drop_script", scripts[0]);
                        resReturn.setStringValue("add_script", scripts[1]);
                        if (processDrop != null && processDrop != 0) {
                            moca.executeCommand("[" + scripts[0] + "]");
                        }
                        constNames.add(constraintName);
                    }
                }

                /* New constraint, add our scripts */
                columnList.clear();
                tableName = res.getString("table_name");
                constraintName = res.getString("constraint_name");
                type = res.getString("constraint_type");

            }
            definition = res.getString("definition");
            columnList.add(res.getString("column_name"));
            isNotNull = definition != null
                    && definition.toUpperCase().lastIndexOf("IS NOT NULL") > 0;
        }

        if (constraintName.equals("") == false
                && constNames.contains(constraintName) == false) {
            if (!isOracle || !isNotNull) {
                resReturn.addRow();
                resReturn.setStringValue("constraint_name", constraintName);
                String scripts[] = getConstraintScripts(dbType, constraintName,
                    tableName, type, columnList, definition);

                resReturn.setStringValue("drop_script", scripts[0]);
                resReturn.setStringValue("add_script", scripts[1]);
                if (processDrop != null && processDrop != 0) {
                    moca.executeCommand("[" + scripts[0] + "]");
                }
            }
        }
        if (processDrop != null && processDrop != 0) {
            moca.executeCommand("[commit]");
        }

        return resReturn;
    }

    private String[] getForeignKeyScripts(String dbType, String fkName,
                                          String fkTable,
                                          ArrayList<String> columns,
                                          String pkTable, String onDelete,
                                          String onUpdate) {
        String[] scripts = new String[2];

        scripts[0] = String.format(
            " /*#NOBIND*/ alter table %s drop constraint %s ", fkTable, fkName);

        StringBuilder colList = new StringBuilder();
        for (String s : columns) {
            if (colList.length() > 0) {
                colList.append(',');
            }
            colList.append(s);
        }

        String actions = "";
        if (onDelete != null && !onDelete.matches("NO[_ ]ACTION")) {
            actions = " ON DELETE " + onDelete.replace('_', ' ');
        }
        if (onUpdate != null && !onUpdate.matches("NO[_ ]ACTION")) {
            actions += "ON UPDATE " + onUpdate.replace('_', ' ');
        }

        scripts[1] = String.format(" /*#NOBIND*/ alter table %s "
                + " add %c constraint %s " + " foreign key (%s) "
                + " references %s " + " %s %c ", fkTable, (dbType
            .equals("ORACLE")) ? '(' : ' ', fkName, colList.toString(), pkTable, actions,
            (dbType.equals("ORACLE")) ? ')' : ' ');

        return scripts;

    }

    private String[] getConstraintScripts(String dbType, String constraintName,
                                          String tableName, String type,
                                          ArrayList<String> columns,
                                          String definition) {
        String[] scripts = new String[2];

        scripts[0] = String.format(
            "/*#NOBIND*/ alter table %s drop constraint %s ", tableName,
            constraintName);
        scripts[1] = String.format(" /*#NOBIND*/ alter table %s add ",
            tableName);

        String strConstDef = "";
        if (type.startsWith("P")) {
            // Primary Key constraint
            StringBuilder colList = new StringBuilder();
            for (String s : columns) {
                if (colList.length() > 0) {
                    colList.append(',');
                }
                colList.append(s);
            }
            strConstDef = String.format("constraint %s primary key (%s) ",
                constraintName, colList.toString());
            if (dbType.equals("ORACLE")) {
                scripts[0] += " DROP INDEX ";
            }

        }
        else if (type.startsWith("U")) {
            // unique key constraint
            StringBuilder colList = new StringBuilder();
            for (String s : columns) {
                if (colList.length() > 0) {
                    colList.append(',');
                }
                colList.append(s);
            }
            strConstDef = String.format("constraint %s UNIQUE (%s) ",
                constraintName, colList.toString());
            if (dbType.equals("ORACLE")) {
                scripts[0] += " DROP INDEX ";
            }

        }
        else if (type.startsWith("C")) {
            // Check constraint
            strConstDef = String.format("constraint %s check %c %s %c ",
                constraintName, dbType.equals("ORACLE") ? '(' : ' ',
                definition, dbType.equals("ORACLE") ? ')' : ' ');
        }
        else if (type.startsWith("D")) {
            // Default constraint
            strConstDef = String.format("constraint %s default %s for %s ",
                constraintName, definition, columns.get(0));
        }
        if (dbType.equals("ORACLE")) {
            strConstDef = "(" + strConstDef + ")";
        }
        scripts[1] += strConstDef;

        return scripts;

    }

    /**
     * Creates scripts for creating and dropping constraints
     * 
     * @param moca The moca context
     * @param table The database table
     * @return The moca results containing the add and drop scripts
     * @throws MocaException If a problem occurs while explaining the query
     */
    public MocaResults scriptIndexes(MocaContext moca, String table)
            throws MocaException {
        try {
            String dbType = getDBType(moca);
            return scriptIndexesInt(dbType, table, 0);

        }
        catch (MocaException ex) {
            throw ex;
        }
    }

    private String[] getIndexScripts(String dbType, String tableName,
                                     String indexName, String indexType,
                                     String indexKeys) {
        String[] scripts = new String[2];

        if (dbType.equals("ORACLE")) {
            scripts[0] = String
                .format(" /*#NOBIND*/ drop index %s ", indexName);
        }
        else {
            scripts[0] = String.format(" /*#NOBIND*/ drop index %s.%s ",
                tableName, indexName);
        }

        boolean isUnique = (indexType.indexOf("nonunique") < 0 && indexType
            .indexOf("unique") >= 0);

        scripts[1] = String.format(
            " /*#NOBIND*/ create %s index %s on %s (%s)", (isUnique ? "unique"
                    : ""), indexName, tableName, indexKeys);
        return scripts;
    }

    private MocaResults scriptIndexesInt(String dbType, String table,
                                         Integer processDrop)
            throws MocaException {

        MocaContext moca = MocaUtils.currentContext();

        String cmd = "list table indexes ";

        if (table != null && !table.equals(""))
            cmd += String.format(" where table_name = '%s' ", table);
        MocaResults resIdx = null;
        try {
            resIdx = moca.executeInline(cmd);
        }
        catch (NotFoundException ex) {
            return ex.getResults();
        }

        String indexName = "";
        String indexType = "";
        String tableName = "";
        String indexKeys = "";

        EditableResults resReturn = moca.newResults();
        resReturn.addColumn("index_name", MocaType.STRING);
        resReturn.addColumn("index_description", MocaType.STRING);
        resReturn.addColumn("drop_script", MocaType.STRING);
        resReturn.addColumn("add_script", MocaType.STRING);

        while (resIdx != null && resIdx.next()) {
            tableName = resIdx.getString("table_name");
            indexName = resIdx.getString("index_name");

            if (indexName != null) {

                indexType = resIdx.getString("index_description");
                indexKeys = resIdx.getString("index_keys");
                indexKeys = indexKeys.replaceAll("\\(\\-\\)", " desc ");

                resReturn.addRow();
                resReturn.setStringValue("index_name", indexName);
                String[] scripts = getIndexScripts(dbType, tableName,
                    indexName, indexType.toLowerCase(), indexKeys);
                resReturn.setStringValue("drop_script", scripts[0]);
                resReturn.setStringValue("add_script", scripts[1]);
                resReturn.setStringValue("index_description", indexType);
                if (processDrop != null && processDrop != 0) {
                    moca.executeCommand("[" + scripts[0] + "]");
                }
            }
        }

        return resReturn;
    }
    
    /* 
     * Converts columns to new unicode aware data types for a sql server database table
     * Important note here... the column list in resCols can contain ONLY the columns to convert 
     * or it can contain all the columns on the table.  This is because when a table has an 
     * identity column, we need to pass the column list to the insert statement. 
     */
    private void convertMSSQLTableToUnicode(MocaContext moca, String tableName,
                                            MocaResults resCols, boolean hasIdentity,
                                            EditableResults returnResults)
            throws MocaException {
        
        /*
         * When we convert a table, we need to: 1) create the table with a temp
         * name 2) copy the data to the new table 3) Drop the old table 4)
         * rename the new table to the old table 5) Readd any constraints or
         * indexes
         */
        
        StringBuilder columnList = null;
        if (hasIdentity) {
            /* this has an identity column, so we need to do an insert with all 
             * the columns listed and the IDENTITY_INSERT ON
             */
            columnList = new StringBuilder();        
            
        }
        
        String tempTable = "TTT_";
        
        if (tempTable.length() > 26) {
            tempTable += tableName.subSequence(0, 25);
        }
        else {
            tempTable += tableName;
        }
        
        StringBuilder scriptCmd = new StringBuilder(String
            .format("[ /*#NOBIND*/ select * into %1$s "
                    + " from %2$s where 1=2 ] ", tempTable, tableName));

        String alterPrefix = String.format(
            "| [ alter table %s alter column ", tempTable);
        while (resCols != null && resCols.next()) {

            String newDataType = "";

            String currentDataType = resCols.getString("data_type");
            String column = resCols.getString("column_name");
            int length = resCols.getInt("data_length");

            if (currentDataType.startsWith("varchar")) {

                newDataType = String.format(" NVARCHAR(%d) ", length);
            }
            else if (currentDataType.equals("text")) {
                newDataType = " NVARCHAR(MAX) ";
            }

            if (newDataType.length() > 0) {
                scriptCmd.append(alterPrefix);
                scriptCmd.append(column);
                scriptCmd.append(newDataType);
                if (resCols.getString("nullable").equals("NOT NULL")) {
                    scriptCmd.append(" NOT NULL ]");
                }
                else {
                    scriptCmd.append(" ]");
                }

                returnResults.addRow();
                returnResults.setStringValue("table_name", tableName);
                returnResults.setStringValue("column_name", column);
                returnResults.setStringValue("old_datatype", currentDataType);
                returnResults.setStringValue("new_datatype", newDataType);

            }
            if (hasIdentity) {
                if (columnList.length()>0) 
                    columnList.append(",");
                columnList.append(column);
            }
        }

        if (hasIdentity) {
            scriptCmd.append(String.format(
                "; [ /*#NOBIND*/ SET IDENTITY_INSERT %1$s ON] "
                +"; [ /*#NOBIND*/ insert into %1$s (%2$s) select * from %3$s ] catch (-1403) "
                + "; [ /*#NOBIND*/ SET IDENTITY_INSERT %1$s OFF] ",
                tempTable, columnList, tableName));
                
        }
        else
        {
            scriptCmd.append(
                String.format("; [/*#NOBIND*/ insert into %1$s select * from %2$s ] catch (-1403) ",
                    tempTable, tableName));                
        }
        scriptCmd.append(String.format(" ; [ /*#NOBIND*/ drop table %1$s ] "
                    + " ; [ /*#NOBIND*/ sp_rename '%2$s', '%1$s' ] ", tableName, tempTable));
        moca.executeCommand(scriptCmd.toString());

    }
    
    private boolean isMSSQLSequenceTable(MocaContext moca, String tableName) throws MocaException {

        StringBuilder sql = new StringBuilder(
            String.format("[select * from %1$s where 1 = 2] catch(-1403)", tableName));
        
        MocaResults res = moca.executeCommand(sql.toString());
        
        // We can assume a table is a sequence table if it contains these columns.
        if (res.containsColumn("currval") &&
            res.containsColumn("nextval") &&
            res.containsColumn("seedval") &&
            res.containsColumn("incval") &&
            res.containsColumn("maxval")) {
            return true;
        }  
        
        return false;
    }
    
    private ArrayList<String> tablesWithIdentity = null;       
}
