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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaTrace;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.RequiredArgumentException;
import com.redprairie.moca.exceptions.MissingArgumentException;
import com.redprairie.moca.exceptions.MissingPKException;

/**
 * A class used to generate the SQL statements based on context data
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class CrudCommandBuilder {

    /**
     * Creates a new CrudCommandBuilder class.
     * 
     * @param dataProvider The ContextDataProvider instance.
     */
    public CrudCommandBuilder(MocaContext dataProvider) {
        _dataProvider = dataProvider;

        _autoArguments = new HashMap<String, String>();
        _autoArguments.put("LAST_UPD_DT", "sysdate");
        _autoArguments.put("MODDTE", "sysdate");
        _autoArguments.put("INS_DT", "sysdate");
        _autoArguments.put("LAST_UPD_USER_ID", "upper(nvl(@@USR_ID, 'NOUSER'))");
        _autoArguments.put("MOD_USR_ID", "upper(nvl(@@USR_ID, 'NOUSER'))");
        _autoArguments.put("INS_USER_ID", "upper(nvl(@@USR_ID, 'NOUSER'))");
        _autoArguments.put("U_VERSION", "mod(nvl(u_version, 0) + 1, 10000)");
    }

    /**
     * Checks the variable to ensure it exists on the stack and optionally not
     * null or empty.
     * 
     * @param variableName The name of the variable to check
     * @param checkValue Indicates if the value of the argument should be
     *                checked for null/empty
     * @throws MissingArgumentException Thrown if the variable is not found
     * @throws EmptyArgumentException Thrown if the variable is found but null or empty
     */
    public boolean checkVariable(String variableName, boolean checkValue)
            throws MissingArgumentException, EmptyArgumentException {

        MocaValue value = _dataProvider.getStackVariable(variableName);
        // check the field in the context
        if (value == null)
            throw new MissingArgumentException(variableName);

        if (checkValue) {

            Object fieldValue = value.getValue();

            if (fieldValue == null){
                throw new EmptyArgumentException(variableName);
            }

            if (fieldValue instanceof String
                    && fieldValue.toString().equals(""))
                throw new EmptyArgumentException(variableName);
        }

        return true;
    }

    /**
     * Checks that fields listed PK or Required are on the context and not null
     * or empty
     * 
     * @param definition The table definition to check fields on.
     * @param mode An enumeration indicating if this is update or insert mode
     * @throws MissingPKException Thrown if an argument is missing.
     */
    public void checkRequiredFields(TableDefinition definition, CrudMode mode)
            throws MissingPKException {

        List<ColumnDefinition> columns = definition.getPKFields();
        
        // TODO: Sometime in the future we can check non-null fields
        // if we can determine if the column has a default value
        // List<ColumnDefinition> columns = (mode == CrudMode.UPDATE) ? definition
        //    .getPKFields() : definition.getRequiredFields();

        for (ColumnDefinition column : columns) {
            try {
                checkVariable(column.getColumnName(), true);
            }
            catch (MissingArgumentException e) {
                throw new MissingPKException(definition.getTableName());
            }
        }
    }

    /** Creates a WHERE clause list based on the all fields on the table
    * 
    * @param definition The table definition
    * @param pkUpperCase Indicates if the PK fields should be wrapped in UPPER
    *                functions.
    * @param changedArgs The list of arguments that change or are PKs. Can be
    *                set to null.
    * @return A SQL WHERE clause list
    * @throws MissingPKException Thrown if no PK fields are located.
    */
   public String createConcurrencyWhereClause(TableDefinition definition,
                                   boolean pkUpperCase)
           throws MissingPKException {
       
       return createWhereClause(definition, definition.getColumns(), pkUpperCase, null);
   }
    
    /**
     * Constructs the SQL INSERT command for the table
     * 
     * @param definition The table definition
     * @param pkUpperCase If true, PK values are wrapped in UPPER() functions.
     * @param changedArgs A list of arguments that have been changed to populate
     *                the results with
     * @return The resulting SQL INSERT command
     * @throws MissingArgumentException Thrown if a needed argument is missing
     */
    public String createInsertCommand(TableDefinition definition,
                                      boolean pkUpperCase,
                                      List<ColumnDefinition> changedArgs)
            throws RequiredArgumentException {

        StringBuilder builder = new StringBuilder();
        StringBuilder columnList = new StringBuilder();
        StringBuilder valueList = new StringBuilder();
        boolean hasArguments = false;

        for (ColumnDefinition column : definition.getColumns()) {

            String columnName = column.getColumnName();
            String columnVal = null;

            if (columnName.equalsIgnoreCase("U_VERSION"))
            {
                columnVal = "1";
            }
            else if (_autoArguments.containsKey(columnName.toUpperCase())) {
                /* Note for inserts we'll fill in both insert and modify dates and times
                 * since most of the newer tables only contain modify fields and some
                 * existing tables have non-null modify fields */
                columnVal = _autoArguments.get(columnName.toUpperCase());
            }
            else if (!column.isIdentity()
                    && _dataProvider.getStackVariable(columnName) != null) {
                columnVal = verifyDataType(column, pkUpperCase);
                hasArguments = true;
            }

            if (columnVal != null) {

                // Append commas if necessary
                if (columnList.length() != 0) {
                    columnList.append(", ");
                    valueList.append(", ");
                }

                // Add to argument lists
                columnList.append(columnName);
                valueList.append(columnVal);

                //For now only add PK fields to the changed argument list
                if (column.isPKField()) {
                    changedArgs.add(column);
                }
            }
        }

        if (!hasArguments) throw new RequiredArgumentException("");

        builder.append("[INSERT INTO ");
        builder.append(definition.getTableName());
        builder.append(" (");
        builder.append(columnList);
        builder.append(") VALUES (");
        builder.append(valueList);
        builder.append(")");
        builder.append("]");

        return builder.toString();
    }

    /**
     * Constructs the SQL DELETE command for the table
     * 
     * @param definition The table definition
     * @param pkUpperCase If true, PK values are wrapped in UPPER() functions.
     * @return The resulting SQL DELETE command
     */
    public String createRemoveCommand(TableDefinition definition,
                                      boolean pkUpperCase) throws MocaException {

        StringBuilder builder = new StringBuilder();
        String whereList = createWhereClause(definition, pkUpperCase, null);

        builder.append("[DELETE FROM ");
        builder.append(definition.getTableName());
        builder.append(" WHERE ");
        builder.append(whereList);
        builder.append("]");

        return builder.toString();
    }

    /**
     * Creates a WHERE clause list based on the available PK/Identity fields on
     * the table
     * 
     * @param definition The table definition
     * @param pkUpperCase Indicates if the PK fields should be wrapped in UPPER
     *                functions.
     * @param changedArgs The list of arguments that change or are PKs. Can be
     *                set to null.
     * @return A SQL WHERE clause list
     * @throws MissingPKException Thrown if no PK fields are located.
     */
    public String createWhereClause(TableDefinition definition,
                                    boolean pkUpperCase,
                                    List<ColumnDefinition> changedArgs)
            throws MissingPKException {

        List<ColumnDefinition> selectedColumns = new ArrayList<ColumnDefinition>();
        for (ColumnDefinition column : definition.getColumns())
            if ((column.isPKField() || column.isIdentity()))
                selectedColumns.add(column);

        return createWhereClause(definition, selectedColumns, pkUpperCase,
            changedArgs);
    }

    /**
     * Creates a WHERE clause list based on the available PK/Identity fields on
     * the table
     * 
     * @param definition The table definition
     * @param columnNames The names of the columns to choose to create a clause
     *                from
     * @param pkUpperCase Indicates if the PK fields should be wrapped in UPPER
     *                functions.
     * @param changedArgs The list of arguments that change or are PKs. Can be
     *                set to null.
     * @return A SQL WHERE clause list
     * @throws MissingPKException Thrown if no PK fields are located.
     */
    public String createWhereClause(TableDefinition definition,
                                    String[] columnNames, boolean pkUpperCase,
                                    List<ColumnDefinition> changedArgs)
            throws MissingPKException {

        List<ColumnDefinition> selectedColumns = new ArrayList<ColumnDefinition>();
        for (String columnName : columnNames) {
            columnName = columnName.trim();
            ColumnDefinition column = definition.getColumn(columnName);
            if (column != null) selectedColumns.add(column);
        }

        return createWhereClause(definition, selectedColumns, pkUpperCase,
            changedArgs);
    }

    /**
     * Creates a WHERE clause list based on the available fields on the table.
     * Uses the columns list as a base.
     * 
     * @param definition The table definition
     * @param The columns to use
     * @param pkUpperCase Indicates if the PK fields should be wrapped in UPPER
     *                functions.
     * @param changedArgs The list of arguments that change or are PKs. Can be
     *                set to null.
     * @return A SQL WHERE clause list
     * @throws MissingPKException Thrown if no PK fields are located.
     */
    private String createWhereClause(TableDefinition definition,
                                     List<ColumnDefinition> columns,
                                     boolean pkUpperCase,
                                     List<ColumnDefinition> changedArgs)
            throws MissingPKException {

        StringBuilder whereList = new StringBuilder();
        boolean hasColumns = false;

        for (ColumnDefinition column : columns) {
            // Create the WHERE clause
            try {

                checkVariable(column.getColumnName(), true);
                hasColumns = true;
                whereList.append((whereList.length() == 0) ? "" : " AND ");
                whereList.append(String.format("%1$s=%2$s", column
                    .getColumnName(), verifyDataType(column, pkUpperCase)));

                if (changedArgs != null) changedArgs.add(column);
            }
            catch (MissingArgumentException e) {
            	//If the variable is missing or empty just leave it out
            }

        }

        if (!hasColumns)
            throw new MissingPKException(definition.getTableName());

        return whereList.toString();
    }

    /**
     * Constructs the SQL UPDATE command for the table
     * 
     * @param definition The table definition
     * @param whereList A string representing the where clause.
     * @param changedArgs A list of arguments that have been changed to populate
     *                the results with
     * @throws MissingArgumentException Thrown if a needed argument is missing
     */
    public String createUpdateCommand(TableDefinition definition,
                                      String whereList,
                                      List<ColumnDefinition> changedArgs)
            throws MocaException {

        StringBuilder builder = new StringBuilder();
        StringBuilder updateList = new StringBuilder();

        boolean hasArguments = false;

        for (ColumnDefinition column : definition.getColumns()) {

            String columnName = column.getColumnName();
            String columnVal = null;

            if (columnName.equalsIgnoreCase("INS_DT")
                    || columnName.equalsIgnoreCase("INS_USER_ID")) {
                // Do nothing since we don't want users forcing these values in
            }
            else if (_autoArguments.containsKey(columnName.toUpperCase())) {
                // In a change command auto arguments count as items to update
                columnVal = _autoArguments.get(columnName.toUpperCase());
                hasArguments = true;
            }
            else if (_dataProvider.getStackVariable(columnName) != null
                    && !column.isPKField() && !column.isIdentity()) {

                // Add to argument lists
                columnVal = verifyDataType(column);

                // Note that arguments have been added
                hasArguments = true;
            }

            if (columnVal != null) {

                // Append commas if necessary
                if (updateList.length() != 0) {
                    updateList.append(", ");
                }

                updateList.append(String.format("%1$s=%2$s", columnName,
                    columnVal));
                
                if (changedArgs != null) {
                    changedArgs.add(column);
                }
            }
        }

        // Current behavior is to simply continue if no fields
        // exist that need to be update, so return an empty string
        if (!hasArguments) {
            return "";
        }

        builder.append("[UPDATE ");
        builder.append(definition.getTableName());
        builder.append(" SET ");
        builder.append(updateList);
        builder.append(" WHERE ");
        builder.append(whereList);
        builder.append("]");

        return builder.toString();
    }

    /**
     * Determines if the u_version of a record is valid based on
     * 
     * @param definition The table definition
     * @param whereList The qualifier clause used to locate the record
     * @return true if it is valid, otherwise false.
     */
    public boolean uVersionValid(TableDefinition definition, String whereList) {

        ColumnDefinition uColumn = definition.getColumn("U_VERSION");
        if (uColumn != null) {

            String columnName = uColumn.getColumnName();
            
            try {
                checkVariable(columnName, true);
            }
            catch (MocaException e) {
                //Empty or not existent - exit
                return true;
            }
            
            MocaValue value = _dataProvider.getStackVariable(columnName);
            //Try to parse out the value
            Object fieldValue = value != null ? value.getValue() : null;
            int u_value = -1;
            if (fieldValue instanceof Integer) {
                u_value = ((Integer)fieldValue).intValue();
            }
            else if (fieldValue instanceof String) {
                Integer tmp;
                try {
                    tmp = Integer.valueOf(fieldValue.toString());
                }
                catch (NumberFormatException e) {
                    return true;
                }
                
                if (tmp != null)
                    u_value = tmp.intValue();
            }
            
            if (u_value > -1) {

                _dataProvider.trace(MocaTrace.FLOW, "U_VERSION column exists and is valid.");
                MocaResults res;
                try {
                    res = _dataProvider
                        .executeInline(String
                            .format(
                                "[SELECT nvl(%1$s, 0) FROM %2$s WHERE %3$s FOR UPDATE OF u_version]",
                                columnName, definition.getTableName(),
                                whereList));
                }
                catch (MocaException e) {
                    _dataProvider.logError("U_VERSION verify command failed.");
                    return false;
                }

                if (res.next()) {
                    int newValue = res.getInt(0);
                    if (newValue != 0 && newValue > u_value) {
                        _dataProvider.trace(MocaTrace.FLOW,
                                String.format("U_VERSION check failed, passed value:%1$s DB value:%2$s.",
                                               u_value, newValue));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Verifies that the parameter does not need a conversion such as a to_date
     * 
     * @param columnDefinition The column definition
     * @return The variable with the 'at' sign plus any conversion.
     */
    public String verifyDataType(ColumnDefinition column) {

        return verifyDataType(column, false);
    }

    /**
     * Verifies that the parameter does not need a conversion such as a to_date
     * 
     * @param columnDefinition The column definition
     * @param pkUpperCase If true, any PK fields are wrapped with an UPPER()
     *                method
     * @return The variable with the 'at' sign plus any conversion.
     */
    public String verifyDataType(ColumnDefinition column, boolean pkUpperCase) {

        if (column.getDataType().equals(MocaType.DATETIME))
            return String.format("to_date(@%1$s)", column.getColumnName());

        if (pkUpperCase && column.isPKField()
                && column.getDataType().equals(MocaType.STRING))
            return String.format("UPPER(@%1$s)", column.getColumnName());

        return String.format("@%1$s", column.getColumnName());
    }

    // Private fields
    private final MocaContext _dataProvider;
    private final Map<String, String> _autoArguments;
}
