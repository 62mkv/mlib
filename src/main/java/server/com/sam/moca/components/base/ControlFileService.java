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

package com.sam.moca.components.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sam.moca.EditableResults;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.RowIterator;

/**
 * This class handles component calls for commands related to control files
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class ControlFileService {
    
    /**
     * Performs the "format control file" command.  It will return how the 
     * control file should be formatted based on the different types
     * @param moca The Moca Context
     * @param type The type of control file
     * @param tableName The table for the control file
     * @param whereCommand The where command to be used with "Data" Type
     * @return The results containing the formatted control file
     */
    public MocaResults formatControlFile(MocaContext moca, String type, 
            String tableName, String whereCommand) throws MocaException {
        EditableResults retRes = moca.newResults();
        String dataColumnName = "FORMATED_DATA";
        
        retRes.addColumn("rowtype", MocaType.STRING);
        retRes.addColumn(dataColumnName, MocaType.STRING);
        
        Map<String, Object> resultMap = new HashMap<String, Object>();
        
        resultMap.put("rowtype", "DATA");
        
        String dbType;
        {
            // First we have to retrieve the database type
            dbType = moca.getDb().getDbType();
        }
        
        String pkCommand;
        String columnNameCommand;
        // First we get our commands initialized
        {
            // Now we have to get the pk values
            if (MOCA_DB_MSQL.equals(dbType)) {
                pkCommand = String.format(LIST_MSSQL_PK_CMD, tableName, tableName, 
                        tableName);
                columnNameCommand = String.format(LIST_MSSQL_COLUMNS_CMD, tableName);
            }
            else if (MOCA_DB_ORACLE.equals(dbType)) {
                pkCommand = String.format(LIST_ORA_PK_CMD, tableName);
                columnNameCommand = String.format(LIST_ORA_COLUMNS_CMD, tableName);
            }
            else {
                throw new GenericException("Unsupported database type of " + 
                        dbType);
            }
        }
        
        MocaResults columnResults = moca.executeCommand(columnNameCommand);
        
        List<String> pkColumns = new ArrayList<String>();
        {
            MocaResults pkResults;

            try {
                pkResults = moca.executeCommand(pkCommand);
            }
            // If there was an exception then just default to all columns
            catch(MocaException e) {
                pkResults = columnResults;
            }
            
            RowIterator pkIterator = pkResults.getRows();
            
            while (pkIterator.next()) {
                pkColumns.add(pkIterator.getString(0));
            }
        }
        
        // Now we decide what type to use based on the first letter of the type
        switch (type.toUpperCase().charAt(0)) {
        case 'L':
            boolean firstColumn = true;
            RowIterator columnIterator = columnResults.getRows();
            StringBuilder columnSeparators = new StringBuilder();
            StringBuilder valueSeparators = new StringBuilder();
            StringBuilder pkWhereClause = new StringBuilder();
            // Now loop through all the columns
            while (columnIterator.next()) {
                StringBuilder columnValue = new StringBuilder();
                // If it is the first column we have to put the extra stuff
                // first in the data    
                if (firstColumn) {
                    resultMap.put(dataColumnName, "[ select count(*) row_count from " + tableName + " where");
                    retRes.addRow(resultMap);
                    
                    boolean firstPk = true;
                    // Now we loop through all the pk values to construct
                    // the where clause
                    for (String pkColumn : pkColumns) {
                        // Get the formatted values such as '@foo@'
                        String formatted = formatValue(moca, type.toUpperCase()
                                .charAt(0), dbType, tableName, pkColumn);
                        
                        if (!firstPk) {
                            pkWhereClause.append(" and");
                        }
                        pkWhereClause.append(' ');
                        pkWhereClause.append(pkColumn);
                        pkWhereClause.append(" = ");
                        pkWhereClause.append(formatted != null ? formatted : "''");
                        firstPk = false;
                    }
                    resultMap.put(dataColumnName, "   " + pkWhereClause.toString() + 
                            " ] | if (@row_count > 0) {");
                    retRes.addRow(resultMap);
                    
                    resultMap.put(dataColumnName, "       [ update " + tableName + " set");
                    retRes.addRow(resultMap);
                }
                else {
                    // If it isn't the first then append commas in the buffers
                    columnSeparators.append(", ");
                    valueSeparators.append(", ");
                    columnValue.append(",");
                }
                // Get the formatted values such as '@foo@'
                String column = columnIterator.getString(0);
                String formatted = formatValue(moca, type.toUpperCase()
                        .charAt(0), dbType, tableName, column);
                
                columnValue.append("          " + column + " = "); 
                columnValue.append(formatted != null ? formatted : "''");
                
                resultMap.put(dataColumnName, columnValue.toString());
                retRes.addRow(resultMap);
                
                columnSeparators.append(column);
                valueSeparators.append(formatted);
                
                firstColumn = false;
            }
            
            resultMap.put(dataColumnName, "             where " + pkWhereClause.toString() + " ] }");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "             else { [ insert into " + tableName);
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "                      (" + columnSeparators.toString() +")");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "                      VALUES");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "                      (" + valueSeparators.toString() +") ] }");
            retRes.addRow(resultMap);
            
            break;
        case 'U':
            resultMap.put(dataColumnName, "[ select");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "'@base_filename@'||'.'||lower('@format_mode@') file_name,");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "   '@format_mode@' dump_mode,");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "   '@append_mode@' dump_append");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "   from dual ] | dump data where dump_command =");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "    '[ select");
            retRes.addRow(resultMap);
            
            boolean hasGroupName = false;
            {
                RowIterator columnRowIterator = columnResults.getRows();
                // Loop through all the columns
                while (columnRowIterator.next()) {
                    // The name of the column should be the in the first column
                    String columnName = columnRowIterator.getString(0);
                    
                    if (columnName.equalsIgnoreCase("GRP_NAM")) {
                        hasGroupName = true;
                        break;
                    }
                }
            }
            
            List<String> firstValues = new ArrayList<String>();
            List<String> secondValues = new ArrayList<String>();
            // Now we put all the pk information
            {
                boolean firstPk = true;
                // Make sure to get every pk column
                for (String pkName : pkColumns) {
                    // This doesn't fire on the first
                    if (!firstPk) {
                        firstValues.add("        and");
                        secondValues.add("        and");
                    }
                    else if (hasGroupName) {
                        secondValues.add("        and");
                    }
                    resultMap.put(dataColumnName, "    " + pkName + ",");
                    retRes.addRow(resultMap);
                    
                    {
                        String formatted = formatValue(moca, 
                                type.toUpperCase().charAt(0), dbType, tableName, 
                                pkName);
                        firstValues.add("        " + pkName + "  = " + formatted);
                    }
                    
                    {
                        String formatted = formatValue(moca, 'u', dbType, 
                                tableName, pkName);
                        secondValues.add("        " + pkName + " like " + formatted);
                    }
                    firstPk = false;
                }
            }
            
            resultMap.put(dataColumnName, "    ''@format_mode@'' format_mode,");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "    ''[ select");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "        " + tableName + ".*");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "        from " + tableName + " where");
            retRes.addRow(resultMap);
            
            // For each of the values put a new line
            for (String value : firstValues) {
                resultMap.put(dataColumnName, value);
                retRes.addRow(resultMap);
            }
            
            resultMap.put(dataColumnName, "        ] '' command from " + tableName + " where");
            retRes.addRow(resultMap);
            
            if (hasGroupName) {
                resultMap.put(dataColumnName, "        grp_nam like nvl(''@grp_nam@'',''%'')");
                retRes.addRow(resultMap);
            }
            
            // For each of the values put a new line
            for (String value : secondValues) {
                resultMap.put(dataColumnName, value);
                retRes.addRow(resultMap);
            }
            
            resultMap.put(dataColumnName, "        ] | { if( ''@format_mode@'' = ''XML'') {");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "                 format data where format_mode = ''XMLSTAG'' and tag = ''" + tableName + "''  &");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "                 format data &");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "                 format data where format_mode = ''XMLETAG'' and tag = ''" + tableName + "''");
            retRes.addRow(resultMap);
            
            resultMap.put(dataColumnName, "                 } else { format data } } '");
            retRes.addRow(resultMap);
            
            break;
        case 'D':
            hasGroupName = false;
            {
                RowIterator columnRowIterator = columnResults.getRows();
                // Loop through all the columns
                while (columnRowIterator.next()) {
                    // The name of the column should be the in the first column
                    String columnName = columnRowIterator.getString(0);
                    
                    if (columnName.equalsIgnoreCase("GRP_NAM")) {
                        hasGroupName = true;
                        break;
                    }
                }
            }
            
            // Now we put all the pk information
            {
                StringBuilder pkBuilder = new StringBuilder();
                
                for (String pkName : pkColumns) {
                    pkBuilder.append(',');
                    pkBuilder.append(pkName);
                }
                
                String columns = "base_filename,format_mode,append_mode" + 
                    pkBuilder.toString() + (hasGroupName ? ",grp_nam" : "");
                
                resultMap.put(dataColumnName, columns);
                retRes.addRow(resultMap);
            }
            
            // If the where command isn't provided
            if (whereCommand == null || whereCommand.trim().length() == 0) {
                StringBuilder singleRowBuilder = new StringBuilder();
                
                singleRowBuilder.append("%UNLOAD_DIR%/");
                singleRowBuilder.append(tableName);
                singleRowBuilder.append('/');
                singleRowBuilder.append(tableName);
                singleRowBuilder.append(",CSV,F");
                
                int rowCount = pkColumns.size();
                for (int i = 0 ; i < rowCount; ++i) {
                    singleRowBuilder.append(",%");
                }
                
                if (hasGroupName) {
                    singleRowBuilder.append(",%");
                }
                
                resultMap.put(dataColumnName, singleRowBuilder.toString());
                retRes.addRow(resultMap);
            }
            else {
                MocaResults whereResults = moca.executeCommand(whereCommand);
                
                RowIterator rowIter = whereResults.getRows();
                boolean firstRow = true;
                
                // For each row we have to put the pk info
                while (rowIter.next()) {
                    StringBuilder rowBuilder = new StringBuilder();
                    
                    rowBuilder.append("%UNLOAD_DIR%/");
                    rowBuilder.append(tableName);
                    rowBuilder.append('/');
                    rowBuilder.append(tableName);
                    rowBuilder.append(",CSV,");
                    rowBuilder.append(firstRow ? 'F' : 'T');
                    
                    // Now for each pk column we get the value
                    for (String pkColumn : pkColumns) {
                        Object value = rowIter.getValue(pkColumn);
                        
                        if (value == null) {
                            rowBuilder.append(",%");
                        }
                        else  {
                            rowBuilder.append(',');
                            MocaType columnType = whereResults.getColumnType(
                                    pkColumn);
                            
                            // If it is a boolean put it as a 0 or 1
                            if (MocaType.BOOLEAN == columnType) {
                                rowBuilder.append(rowIter.getBoolean(pkColumn) ? 1 : 0);
                            }
                            else {
                                // Else just append the value
                                rowBuilder.append(rowIter.getValue(pkColumn));
                            }
                        }
                    }
                    
                    if (hasGroupName) {
                        rowBuilder.append(",%");
                    }
                    
                    resultMap.put(dataColumnName, rowBuilder.toString());
                    retRes.addRow(resultMap);
                    
                    firstRow = false;
                }
            }
            
            break;
        default:
            throw new GenericException("Type of " + type + " is unsupported");
        }
        
        return retRes;
    }
    
    /**
     * This will format a string based on the datatype as needed by the control
     * file type
     * @param type The control type 'L', 'U', 'D'
     * @param dbtype The database type
     * @param tableName The table the column is on
     * @param columnName The column of the table
     * @return The formatted string
     */
    private String formatValue(MocaContext moca, char type, String dbType, 
            String tableName, String columnName) throws MocaException {
        String returnString = null;
        switch(type) {
        case 'L':
        case 'U':
            String columnFormatCommand;
            {
                // Now we have to get the pk values
                if (MOCA_DB_MSQL.equals(dbType)) {
                    columnFormatCommand = String.format(LIST_MSSQL_COL_INFO_CMD, tableName, columnName);
                }
                else if (MOCA_DB_ORACLE.equals(dbType)){
                    columnFormatCommand = String.format(LIST_ORA_COL_INFO_CMD, tableName, columnName);
                }
                else {
                    throw new GenericException("Unsupported database type of " + 
                            dbType);
                }
            }
            
            MocaResults columnResults = moca.executeCommand(columnFormatCommand);
            
            RowIterator columnIterator = columnResults.getRows();
            
            // There should be only 1 row
            columnIterator.next();
            
            // nullable column should be the first column
            String nullable = columnIterator.getString(0);
            // datatype column should be the second column
            String datatype = columnIterator.getString(1);
            
            if (nullable != null && datatype != null) {
                if (type == 'L') {
                    if (Arrays.asList(new String[] {
                            "number", "numeric", "int", "integer", "smallint"
                            }).contains(datatype.toLowerCase())) {
                        if (nullable.toUpperCase().charAt(0) == 'Y') {
                            returnString = "to_number('@" + columnName + "@')";
                        }
                        else {
                            returnString = "@" + columnName + "@";
                        }
                    }
                    else if (Arrays.asList(new String[] {
                            "date", "datetime"
                            }).contains(datatype.toLowerCase())) {
                        returnString = "to_date('@" + columnName + "@','YYYYMMDDHH24MISS')";
                    }
                    else {
                        returnString = "'@" + columnName + "@'";
                    }
                }
                // This means it is 'U' type
                else {
                    if (Arrays.asList(new String[] {
                            "number", "numeric", "int", "integer", "smallint"
                            }).contains(datatype.toLowerCase())) {
                        returnString = "to_char(''||to_char(" + columnName + ")||'' )";
                    }
                    else {
                        returnString = "''''''||" + columnName + "||''''''";
                    }
                }
            }
            
            break;
        case 'u':
            returnString = "nvl(''@" + columnName + "@'',''%'')";
            break;
        }
        return returnString;
    }
    
    /* ORACLE commands */
    private static final String LIST_ORA_PK_CMD = "[select lower(tab_cols.column_name) column_name from user_tab_columns tab_cols, user_cons_columns cons_cols, user_constraints cons where tab_cols.table_name = cons_cols.table_name and tab_cols.column_name = cons_cols.column_name and cons.constraint_name = cons_cols.constraint_name and cons.constraint_type = 'P' and cons.table_name = UPPER('%s') order by tab_cols.column_id ]";
    private static final String LIST_ORA_COLUMNS_CMD = "[select lower(column_name) from user_tab_columns where table_name = UPPER('%s') order by column_id]";
    private static final String LIST_ORA_COL_INFO_CMD = "[select nullable, data_type from user_tab_columns where table_name = UPPER('%s') and column_name = UPPER('%s') ]";

    /* MSSQL commands */
    private static final String LIST_MSSQL_PK_CMD = "[select c.column_name from INFORMATION_SCHEMA.COLUMNS c, sysobjects o where c.column_name in (select index_col(lower('%s'), i.indid, c1.colid) from syscolumns c1, sysindexes i where i.id = o.id and (i.status & cast(2048 as int)) = cast(2048 as int) and c1.colid <= i.keycnt and c1.id = object_id(lower('%s'))) and o.id = object_id('%s') and o.name = lower(c.table_name) and o.xtype = 'U' ]";
    private static final String LIST_MSSQL_COLUMNS_CMD = "[select c.column_name from INFORMATION_SCHEMA.COLUMNS c, sysobjects o where o.id = object_id('%s') and o.name = lower(c.table_name) and o.xtype = 'U' ]";
    private static final String LIST_MSSQL_COL_INFO_CMD = "[select c.IS_NULLABLE nullable, c.DATA_TYPE from INFORMATION_SCHEMA.COLUMNS c, sysobjects o where o.id = object_id('%s') and o.name = lower(c.table_name) and o.xtype = 'U' and c.column_name = lower('%s') ]";
    
    private static final String MOCA_DB_MSQL = "MSSQL";
    private static final String MOCA_DB_ORACLE = "ORACLE";
}
