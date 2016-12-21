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

package com.sam.moca.crud;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.sam.moca.DatabaseTool;
import com.sam.moca.MocaArgument;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaRuntimeException;
import com.sam.moca.MocaType;
import com.sam.moca.MocaValue;
import com.sam.moca.NotFoundException;
import com.sam.moca.exceptions.MissingArgumentException;
import com.sam.moca.exceptions.MissingPKException;
import com.sam.moca.exceptions.MocaDBException;
import com.sam.moca.exceptions.UniqueConstraintException;
import com.sam.moca.server.db.DBType;
import com.sam.moca.server.db.translate.OracleDialect;
import com.sam.moca.server.db.translate.SQLServerDialect;
import com.sam.moca.util.MocaUtils;
import com.sam.util.ArgCheck;

/**
 * This manager is used to do various CRUD operations by checking all the
 * columns on a table
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class JDBCCrudManager implements CrudManager {
    
    public JDBCCrudManager(MocaContext moca) {
        _moca = moca;
        _db = moca.getDb();
        _type = DBType.valueOf(_db.getDbType());
    }

    // @see com.sam.moca.crud.CrudManager#changeRecord(java.lang.String, boolean, boolean, com.sam.moca.crud.CrudManager.ConcurrencyMode, com.sam.moca.MocaArgument[])
    @Override
    public Collection<MocaArgument> changeRecord(String tableName,
            boolean capitalizePkValues, boolean forceUpdate,
            ConcurrencyMode mode, MocaArgument... arguments)
            throws NotFoundException, MissingPKException, MocaDBException,
            IllegalArgumentException {
        ArgCheck.notNull(tableName, "The table name is required!");
        ArgCheck.isFalse(containsOnlyWhitespace(tableName), 
                "The table name provided was empty!");
        
        Collection<MocaArgument> pkValues = new ArrayList<MocaArgument>();
        TableDefinition definition = TableFactory.getTableDefinition(tableName);
        // Now we check the pk values and update our map with them
        updateValues(pkValues, definition, capitalizePkValues, true, 
                ColumnRetrieval.ONLY_PK, _updateEvaluations, arguments);
        
        boolean exists = validateDataExists(tableName, pkValues);
        
        if (!exists) {
            _logger.debug("Record with matching arguments could not be found.");
            if (forceUpdate)
                throw new NotFoundException();
            else {
                _logger.debug("Creating new record instead.");
                updateValues(pkValues, definition, capitalizePkValues, false, 
                        ColumnRetrieval.NO_PK, _createEvaluations, arguments);
                createRecord(tableName, pkValues);
                return pkValues;
            }
        }
        
        Collection<MocaArgument> otherValues = new ArrayList<MocaArgument>();

        updateValues(otherValues, definition, false, false, 
                ColumnRetrieval.NO_PK, _updateEvaluations, arguments);
        
        boolean throwException = false;

        switch(mode) {
            case U_VERSION:
                _logger.debug("U_VERSION concurrency check enabled.");
                throwException = !checkUVersion(tableName, otherValues, 
                        pkValues);
                break;
            case DATA_OVERLAY:
                _logger.debug("Data overlay concurrency check enabled");
                Collection<MocaArgument> allValues = 
                    new ArrayList<MocaArgument>(otherValues.size() + 
                            pkValues.size());
                allValues.addAll(pkValues);
                allValues.addAll(otherValues);
                
                throwException = !validateDataExists(tableName, allValues);
                break;
            default:
                break;
        }
        
        if (throwException) {
            throw new UniqueConstraintException(
                  "This record has already been updated, please refresh your data.");
        }
                
        if (otherValues.isEmpty()) {
            _logger.debug("No arguments found to update... skipping SQL statement");
        }
        else {
            changeRecord(tableName, pkValues, otherValues);
        }

        // We finally combine them together for the return
        pkValues.addAll(otherValues);
        return pkValues;
    }
    
    private boolean checkUVersion(String tableName, Collection<MocaArgument> otherValues, 
            Collection<MocaArgument> pkValues) throws MocaDBException {
        final String uVersionString = "U_VERSION";
        Object uVersion = null;
        for (MocaArgument otherValue : otherValues) {
            if (otherValue.getName().equalsIgnoreCase(uVersionString)) {
                uVersion = otherValue.getValue();
                break;
            }
        }
        
        if (uVersion == null) {
            MocaValue stackValue = _moca.getStackVariable(uVersionString);
            if (stackValue != null) {
                uVersion = stackValue.getValue();   
            }
        }

        int existingUVersion = -1;
        if (uVersion != null && !(uVersion instanceof String && 
                containsOnlyWhitespace(uVersion.toString()))) {
            try {
                Integer uVersionInt = Integer.valueOf(uVersion.toString());
                _logger.debug("U_VERSION column exists and is valid.");
                
                // Now that we have a name and value we can actually do the check :)
                Connection conn = _db.getConnection();
                
                StringBuilder builder = new StringBuilder(
                        "SELECT u_version FROM ");
                builder.append(tableName);
                builder.append(" WHERE ");
                
                boolean firstKey = true;
                // Now we build up the column names
                for (MocaArgument pkValue : pkValues) {
                    String key = pkValue.getName();
                    if (firstKey) {
                        firstKey = false;
                    }
                    else {
                        builder.append(" AND ");
                    }
                    
                    builder.append(key);
                    builder.append(" = ?");
                }
                
                try {
                    PreparedStatement pstmt = conn.prepareStatement(builder.toString());
                    
                    try {
                        int i = 1;
                        
                        for (MocaArgument value : pkValues) {
                            bindArgument(pstmt, i++, value);
                        }

                        ResultSet res = pstmt.executeQuery();
                        try {
                            // If there isn't at least one row then throw the invalid
                            // exception.
                            if (!res.next()) {
                                existingUVersion = res.getInt(1);
                            }
                        }
                        finally {
                            res.close();
                        }
                    }
                    catch (SQLException e) {
                        throw new MocaDBException(e);
                    }
                    finally {
                        pstmt.close();
                    }
                }
                catch (SQLException e) {
                    throw new MocaRuntimeException(-2, 
                            "Unexpected Database Error Encountered", e);
                }
                
                if (existingUVersion < uVersionInt.intValue()) {
                    _logger.debug(MocaUtils.concat(
                            "U_VERSION check failed, passed value:", 
                            uVersionInt, " DB value:", existingUVersion));
                    return false;
                }
            }
            catch (NumberFormatException ignore) {
                // If we couldn't get a valid number just ignore it
            }
        }
        return true;
    }
    
    private void changeRecord(String tableName, Collection<MocaArgument> pkValues, 
            Collection<MocaArgument> otherValues) throws MocaDBException {
        // Now that we have a name and value we can actually do the check :)
        Connection conn = _db.getConnection();
        
        StringBuilder builder = new StringBuilder("UPDATE ");
        builder.append(tableName);
        builder.append(" SET ");
        
        boolean firstKey = true;
        // Now we build up the value pairs
        Iterator<MocaArgument> iter = otherValues.iterator();
        while (iter.hasNext()) {
            MocaArgument arg = iter.next();
            String key = arg.getName();
            if (firstKey) {
                firstKey = false;
            }
            else {
                builder.append(", ");
            }
            
            if (key.equalsIgnoreCase("U_VERSION")) {
                // First we remove the iterator so we don't set a value
                iter.remove();
                switch (_type) {
                case ORACLE:
                    builder.append("U_VERSION = {fn MOD(coalesce(u_version + 1, 0), 1000)}");
                    break;
                // We do this because jtds doesn't support coalesce in an 
                // escaped mod call
                case MSSQL:
                    builder.append("U_VERSION = coalesce(u_version + 1, 0) % 1000");
                    break;
                default: throw new UnsupportedOperationException(
                        "Database type of " + _type + " not supported!");
                }
                continue;
            }
            
            builder.append(key);
            builder.append(" = ?");
        }
        
        builder.append(" WHERE ");
        
        firstKey = true;
        // Now we build up the where clause
        for (MocaArgument pkValue : pkValues) {
            String key = pkValue.getName();
            if (firstKey) {
                firstKey = false;
            }
            else {
                builder.append(" AND ");
            }
            
            builder.append(key);
            builder.append(" = ?");
        }
        
        try {
            PreparedStatement pstmt = conn.prepareStatement(builder.toString());
            
            try {
                int i = 1;
                
                for (MocaArgument value : otherValues) {
                    bindArgument(pstmt, i++, value);
                }
                
                for (MocaArgument value : pkValues) {
                    bindArgument(pstmt, i++, value);
                }

                pstmt.executeUpdate();
            }
            catch (SQLException e) {
                throw new MocaDBException(e);
            }
            finally {
                pstmt.close();
            }
        }
        catch (SQLException e) {
            throw new MocaRuntimeException(-2, 
                    "Unexpected Database Error Encountered", e);
        }
    }

    // @see com.sam.moca.crud.CrudManager#checkField(java.lang.String, com.sam.moca.crud.CrudManager.FieldRequirement)
    @Override
    public void checkField(String fieldName, FieldRequirement type)
            throws MissingArgumentException, EmptyArgumentException, 
            IllegalArgumentException {
        ArgCheck.notNull(fieldName, "The field name cannot be null!");
        ArgCheck.isFalse(containsOnlyWhitespace(fieldName), 
                "The field name cannot be empty!");
        ArgCheck.notNull(type, "The type cannot be null!");
        
        MocaValue stackValue = _moca.getStackVariable(fieldName);
        
        if (stackValue == null) {
            // If we required the value
            if (type == FieldRequirement.NOT_PRESENT_OR_REQUIRE_VALUE) {
                return;
            }
            else {
                throw new MissingArgumentException(fieldName);
            }
        }


        Object fieldValue = stackValue.getValue();

        if (fieldValue == null || (fieldValue instanceof String
                && containsOnlyWhitespace(fieldValue.toString()))) {
            throw new EmptyArgumentException(fieldName);
        }
    }

    // @see com.sam.moca.crud.CrudManager#createRecord(java.lang.String, boolean, com.sam.moca.MocaArgument[])
    @Override
    public Collection<MocaArgument> createRecord(String tableName, 
            boolean capitalizePkValues, MocaArgument... arguments) 
            throws MocaDBException, NotFoundException, MissingPKException, 
            IllegalArgumentException {
        ArgCheck.notNull(tableName, "The table name is required!");
        ArgCheck.isFalse(containsOnlyWhitespace(tableName), 
                "The table name provided was empty!");
        
        Collection<MocaArgument> values = new ArrayList<MocaArgument>();
        TableDefinition definition = TableFactory.getTableDefinition(tableName);
        // Now we update the value map with all the pertinent values
        updateValues(values, definition, capitalizePkValues, true, 
                ColumnRetrieval.ALL, _createEvaluations, arguments);
        
        createRecord(tableName, values);
        
        return values;
    }
    
    private void createRecord(String tableName, Collection<MocaArgument> values) 
            throws MocaDBException {
        // Now that we have a name and value we can actually do the check :)
        Connection conn = _db.getConnection();
        
        StringBuilder builder = new StringBuilder("insert into ");
        builder.append(tableName);
        builder.append(" (");
        
        StringBuilder valueMarks = new StringBuilder();
        boolean firstKey = true;
        // Now we build up the column names
        for (MocaArgument value : values) {
            String key = value.getName();
            if (firstKey) {
                firstKey = false;
            }
            else {
                builder.append(',');
                valueMarks.append(',');
            }
            
            builder.append(key);
            valueMarks.append('?');
        }
        
        builder.append(") VALUES (");
        builder.append(valueMarks);
        builder.append(')');
        
        try {
            PreparedStatement pstmt = conn.prepareStatement(builder.toString());
            
            try {
                int i = 1;
                
                for (MocaArgument value : values) {
                    bindArgument(pstmt, i++, value);
                }

                pstmt.executeUpdate();
            }
            catch (SQLException e) {
                int errorCode = e.getErrorCode();
                if (SQLServerDialect.SQL_UNIQUE_CONS == errorCode ||
                        SQLServerDialect.SQL_UNIQUE_INDEX == errorCode ||
                        OracleDialect.ORACLE_UNIQUE_CONS == errorCode) {
                    throw new UniqueConstraintException(e);
                }
                throw new MocaDBException(e);
            }
            finally {
                pstmt.close();
            }
        }
        catch (SQLException e) {
            throw new MocaRuntimeException(-2, 
                    "Unexpected Database Error Encountered", e);
        }
    }

    // @see com.sam.moca.crud.CrudManager#removeRecord(java.lang.String, boolean, boolean, com.sam.moca.MocaArgument[])
    @Override
    public Collection<MocaArgument> removeRecord(String tableName, 
            boolean capitalizePkValues, boolean requireAllPkArguments, 
            MocaArgument... arguments) throws MocaDBException, 
            NotFoundException, MissingPKException, IllegalArgumentException {
        ArgCheck.notNull(tableName, "The table name is required!");
        ArgCheck.isFalse(containsOnlyWhitespace(tableName), 
                "The table name provided was empty!");
        
        Collection<MocaArgument> values = new ArrayList<MocaArgument>();
        TableDefinition definition = TableFactory.getTableDefinition(tableName);
        // Now we update the value map with all the pertinent values
        updateValues(values, definition, capitalizePkValues, 
                requireAllPkArguments, ColumnRetrieval.ONLY_PK, 
                _removeEvaluations, arguments);
        
        if (!removeRecord(tableName, values)) {
            throw new NotFoundException();
        }
        
        return values;
    }
    
    private boolean removeRecord(String tableName, Collection<MocaArgument> values) 
            throws MocaDBException {
        // Now that we have a name and value we can actually do the check :)
        Connection conn = _db.getConnection();
        
        StringBuilder builder = new StringBuilder("DELETE FROM ");
        builder.append(tableName);
        builder.append(" WHERE ");
        
        boolean firstKey = true;
        // Now we build up the column names
        for (MocaArgument value : values) {
            String key = value.getName(); 
            if (firstKey) {
                firstKey = false;
            }
            else {
                builder.append(" AND ");
            }
            
            builder.append(key);
            builder.append(" = ?");
        }
        
        try {
            PreparedStatement pstmt = conn.prepareStatement(builder.toString());
            
            try {
                int i = 1;
                
                for (MocaArgument value : values) {
                    bindArgument(pstmt, i++, value);
                }

                return pstmt.executeUpdate() > 0;
            }
            catch (SQLException e) {
                throw new MocaDBException(e);
            }
            finally {
                pstmt.close();
            }
        }
        catch (SQLException e) {
            throw new MocaRuntimeException(-2, 
                    "Unexpected Database Error Encountered", e);
        }
    }

    // @see com.sam.moca.crud.CrudManager#validateCodeValue(com.sam.moca.MocaArgument)
    @Override
    public void validateCodeValue(MocaArgument nameValue) 
            throws CodeInvalidException, MissingStackArgumentException, 
            IllegalArgumentException {
        // First we have to do all our argument checking to protect ourselves
        ArgCheck.notNull(nameValue, "The MocaArgument cannot be null.");
        String name = nameValue.getName();
        ArgCheck.notNull(name, "The name provided cannot be null for the " +
        		"argument");
        Object value = nameValue.getValue();
        
        // If we weren't given a value then we have to check the stack
        if (value == null || containsOnlyWhitespace(value.toString())) {
            MocaValue stackValue = _moca.getStackVariable(name);
            
            // In this if block we check if the stack doesnt' have the value 
            // first, then we check if the value on the stack is null and assign
            // it to the local value and finally if it wasn't null we check
            // the now assigned value to see if it is only whitespace.
            if (stackValue == null || (value = stackValue.getValue()) == null || 
                    containsOnlyWhitespace(value.toString())) {
                throw new MissingStackArgumentException(2962, "colnam", name);
            }
        }
        
        // Now we make sure to convert it to a String
        String valueString = String.valueOf(value);
        
        Collection<MocaArgument> codeValueMap = new ArrayList<MocaArgument>();
        codeValueMap.add(new MocaArgument("colnam", MocaType.STRING, name));
        codeValueMap.add(new MocaArgument("codval", MocaType.STRING, valueString));
        boolean exists = validateDataExists("codmst", codeValueMap);

        if (!exists) {
            throw new CodeInvalidException(name, valueString);
        }
    }
    
    private boolean validateDataExists(String tableName, 
            Collection<MocaArgument> values) {
        // Now that we have a name and value we can actually do the check :)
        Connection conn = _db.getConnection();
        
        StringBuilder builder = new StringBuilder("SELECT 'x'");

        switch (_type) {
        case MSSQL:
            break;
        case ORACLE:
        case H2:
            builder.append(" FROM DUAL");
            break;
        default: throw new UnsupportedOperationException(
            "Database type of " + _type + " not supported!");
        }

        
        builder.append(" where exists (SELECT 'x' FROM ");
        builder.append(tableName);
        builder.append(" WHERE ");
        
        boolean firstKey = true;
        // Now we build up the column names
        for (MocaArgument value : values) {
            String key = value.getName();
            if (firstKey) {
                firstKey = false;
            }
            else {
                builder.append(" AND ");
            }
            
            builder.append(key);
            builder.append(" = ?");
        }
        
        builder.append(')');
        
        try {
            PreparedStatement pstmt = conn.prepareStatement(builder.toString());
            
            try {
                int i = 1;
                
                for (MocaArgument value : values) {
                    bindArgument(pstmt, i++, value);
                }

                ResultSet res = pstmt.executeQuery();
                try {
                    // If there isn't at least one row then throw the invalid
                    // exception.
                    if (!res.next()) {
                        return false;
                    }
                    return true;
                }
                finally {
                    res.close();
                }
            }
            finally {
                pstmt.close();
            }
        }
        catch (SQLException e) {
            throw new MocaRuntimeException(-2, 
                    "Unexpected Database Error Encountered", e);
        }
    }
    
    private void bindArgument(PreparedStatement pstmt, int position, 
            MocaArgument argument) throws SQLException {
        Object value = argument.getValue();
        MocaType argType = argument.getType();
        switch (argType) {
        case DATETIME:
            long time = 0;
            if (value instanceof Date) {
                time = ((Date)value).getTime();
                time -= time % 1000; // clear out Millisecond values.
            }
            else if (value instanceof String) {
                int length = ((String) value).length();
                // If it is the correct length then try to parse it.
                if (length == 14) {
                    try {
                        time = _formatter.parseMillis((String)value);
                    }
                    catch (IllegalArgumentException e) {
                        _logger.warn("Illegal Date Format provided!  Setting date null", e);
                    }
                }
                else if (length == 0) {
                    _logger.debug("Date provided was an empty string.  Setting date null.");
                }
                else {
                    _logger.warn(MocaUtils.concat("Illegal Date Format provided [", value, 
                            "]!  14 characters are required. Setting date null."));
                }
            }
            else if (value != null) {
                _logger.warn("Illegal Date Type [" + value.getClass() + 
                        "] provided!  Only String or java.util.Date supported.");
            }
            
            if (time != 0) {
                pstmt.setTimestamp(position, new Timestamp(time));
            }
            else {
                pstmt.setNull(position, Types.TIMESTAMP);
            }
            break;
        case STRING:
            // If it is a date change back to a string to bind first
            if (value instanceof Date) {
                value = _formatter.print(((Date)value).getTime());
            }
            
            if (value == null || value.equals("")) {
                pstmt.setNull(position, argType.getSQLType());
            }
            else {
                pstmt.setObject(position, value);
            }
            break;
        default:
            if (value == null || value.equals("")) {
                pstmt.setNull(position, argType.getSQLType());
            }
            else {
                pstmt.setObject(position, value);
            }
            break;
        }
        
    }
    
    // @see com.sam.moca.crud.CrudManager#validateDataExists(java.lang.String, java.lang.String[], com.sam.moca.MocaArgument[])
    @Override
    public boolean validateDataExists(String tableName, String[] columns,
            MocaArgument... arguments) throws NotFoundException, 
            MissingArgumentException, MissingPKException, 
            IllegalArgumentException {
        ArgCheck.notNull(tableName, "The table name is required!");
        ArgCheck.isFalse(containsOnlyWhitespace(tableName), 
                "The table name provided was empty!");
        ArgCheck.notNull(columns, "The columns are required!");
        ArgCheck.isFalse(columns.length == 0, 
                "The column names provided are empty!");
        
        Map<String, Object> argumentMap = new HashMap<String, Object>();
        // First put all the arguments into a map for faster access.
        // We put the arguments into the map ignoring the argument datatype,
        // since the table definition has the datatype
        for (MocaArgument argument : arguments) {
            if (argument.getName() != null) {
                argumentMap.put(argument.getName().toUpperCase(), argument.getValue());
            }
        }
        TableDefinition definition = TableFactory.getTableDefinition(tableName);
        // The max size it can be is the columns size so just start it that big
        Collection<MocaArgument> values = new ArrayList<MocaArgument>(columns.length);
        // First we need to find all the valid columns that were chosen.
        for (String column : columns) {
            ColumnDefinition columnDefinition = definition.getColumn(column);
            // If the column exists on the table then we try it
            if (columnDefinition != null) {
                String upperCaseColumnName = column.toUpperCase();
                MocaValue stackValue;
                if (argumentMap.containsKey(upperCaseColumnName)) {
                    values.add(new MocaArgument(column, 
                            columnDefinition.getDataType(), 
                            argumentMap.get(upperCaseColumnName)));
                }
                else if ((stackValue = _moca.getStackVariable(column)) != null) {
                    Object value = stackValue.getValue();
                    if (value == null || (value instanceof String && 
                            containsOnlyWhitespace(value.toString()))) {
                        throw new EmptyArgumentException(column);
                    }
                    values.add(new MocaArgument(column, 
                            columnDefinition.getDataType(),  value));
                }
                else {
                    throw new MissingArgumentException(column);
                }
            }
        }
        
        // If there were no valid columns found then we can't proceed
        if (values.isEmpty()) {
            throw new MissingPKException(tableName);
        }
        
        return validateDataExists(tableName, values);
    }

    // @see com.sam.moca.crud.CrudManager#validateDateFormat(com.sam.moca.MocaArgument)
    @Override
    public void validateDateFormat(MocaArgument nameValue) 
        throws DateInvalidException, IllegalArgumentException, MissingStackArgumentException {
        ArgCheck.notNull(nameValue);
        String name = nameValue.getName();
        Object value = nameValue.getValue();

        if (value == null) {
            ArgCheck.notNull(name, "A value for the argument or a name of a value on the " +
            		"stack must be provided.");
            MocaValue stackValue = _moca.getStackVariable(name);
            if (stackValue == null || (value = stackValue.getValue()) == null) {
                throw new MissingStackArgumentException(2960, "dtenam", name);
            }
        }
        
        // If it isn't already a date object then try parsing it
        if (!(value instanceof Date)) {
            String valueString = String.valueOf(value);
            
            try {
                _formatter.parseDateTime(valueString);
            }
            catch (IllegalArgumentException e) {
                throw new DateInvalidException(name, valueString, e);
            }
        }
    }

    // @see com.sam.moca.crud.CrudManager#validateFlagValue(com.sam.moca.MocaArgument)
    @Override
    public void validateFlagValue(MocaArgument nameValue) 
            throws FlagInvalidException, MissingStackArgumentException, 
            IllegalArgumentException {
        ArgCheck.notNull(nameValue);
        String name = nameValue.getName();
        Object value = nameValue.getValue();
        
        if (value == null) {
            ArgCheck.notNull(name, "A value for the argument or a name of a value on the " +
                        "stack must be provided.");
            MocaValue stackValue = _moca.getStackVariable(name);
            if (stackValue == null || (value = stackValue.getValue()) == null) {
                throw new MissingStackArgumentException(2964, "flagnam", name);
            }

        }
        
        int valueInt;
        try {
            valueInt = Integer.valueOf(String.valueOf(value));
        }
        catch (NumberFormatException e) {
            throw new FlagInvalidException(name, value, e);
        }
        
        if (valueInt != 0 && valueInt != 1) {
            throw new FlagInvalidException(name, value);
        }
    }
    
    private enum ColumnRetrieval {
        ONLY_PK,
        NO_PK,
        ALL
    }
    
    
    /**
     * This method is to update the value collection by checking a few
     * validations first.  Depending on what pkRetrieval type it will check
     * either all the pk columns, the non pk columns or all the columns of
     * the table to see if they have valid values in the MocaArgument array or
     * if they are on the stack.
     * @param values This object will have MocaArguments inserted into it
     * @param tableName
     * @param capitalizePkValues
     * @param requirePks
     * @param arguments
     * @throws NotFoundException
     * @throws MissingPKException
     */
    private void updateValues(Collection<MocaArgument> values, 
            TableDefinition definition, boolean capitalizePkValues, 
            boolean requirePks, ColumnRetrieval pkRetrieval, 
            Map<String, FieldEvaluation> evaluations, MocaArgument... arguments)  
            throws MissingPKException {
        Map<String, MocaValue> argumentMap = new HashMap<String, MocaValue>();
        
        // We put the arguments into the map ignoring the argument datatype,
        // since the table definition has the datatype
        for (MocaArgument argument : arguments) {
            if (argument.getName() != null) {
                argumentMap.put(argument.getName().toUpperCase(), 
                        argument.getDataValue());
            }
        }
        
        List<ColumnDefinition> pkColumns = definition.getPKFields();
        
        if (pkRetrieval != ColumnRetrieval.NO_PK) {
            // First we make sure we have all the PKs
            for (ColumnDefinition pkColumn : pkColumns) {
                String columnName = pkColumn.getColumnName();
                String upperCaseColumnName = columnName.toUpperCase();
                MocaType type = pkColumn.getDataType();
                
                // We have to do a contains since we could have a null value
                if (argumentMap.containsKey(upperCaseColumnName)) {
                    MocaValue mocaValue = argumentMap.get(upperCaseColumnName);
                    Object convertedValue = mocaValue.asType(type);
                    
                    if (capitalizePkValues && convertedValue != null && 
                            convertedValue instanceof String) {
                        values.add(new MocaArgument(columnName, 
                                type, convertedValue.toString().toUpperCase()));
                    }
                    else {
                        values.add(new MocaArgument(columnName, 
                                type, convertedValue));   
                    }
                }
                else {
                    MocaValue stackValue = _moca.getStackVariable(columnName);
                    if (stackValue != null) {
                        Object convertedValue = stackValue.asType(type);
    
                        if (capitalizePkValues && convertedValue != null && 
                                convertedValue instanceof String) {
                            values.add(new MocaArgument(columnName, 
                                    type, convertedValue.toString().toUpperCase()));
                        }
                        else {
                            values.add(new MocaArgument(columnName, 
                                    type, convertedValue));
                        }
                    }
                    else if (requirePks) {
                        throw new MissingPKException(definition.getTableName());
                    }
                }
            }
            
            // If we didn't find any pk values when that is what we wanted only, 
            // then throw an error since we should have at least one.  Currently
            // this only goes on removals and changes
            if (values.isEmpty() && pkRetrieval == ColumnRetrieval.ONLY_PK) {
                throw new MissingPKException(definition.getTableName());
            }
        }
        
        if (pkRetrieval != ColumnRetrieval.ONLY_PK) {
            // Now we have to obtain all the rest of the columns
            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>(
                    definition.getColumns());
            // First remove the pk columns since we have those values already
            columns.removeAll(pkColumns);
            
            for (ColumnDefinition column : columns) {
                // Skip any identity columns
                if (column.isIdentity()) {
                    continue;
                }
                String columnName = column.getColumnName();
                String upperCaseColumnName = columnName.toUpperCase();
                MocaType type = column.getDataType();
                
                if (evaluations.containsKey(upperCaseColumnName)) {
                    FieldEvaluation evaluation = evaluations
                            .get(upperCaseColumnName);
                    // If it was set to null that means skip that argument
                    if (evaluation != null) {
                        Object obj = evaluation.evaluate(_moca);
                        MocaValue mocaValue = new MocaValue(MocaType.forValue(
                                obj), obj);
                        values.add(new MocaArgument(columnName, type, 
                                mocaValue.asType(type)));
                    }
                }
                // We have to do a contains since we could have a null value
                else if (argumentMap.containsKey(upperCaseColumnName)) {
                    MocaValue mocaValue = argumentMap.get(upperCaseColumnName);
                    Object obj = mocaValue.getValue();
                    if (obj != null && !obj.toString().isEmpty()) {
                        obj = mocaValue.asType(type);
                    }
                    // We have to convert all nulls including empty string
                    // to actually be a null
                    else {
                        obj = null;
                    }
                    values.add(new MocaArgument(columnName, type, obj));
                }
                else {
                    MocaValue stackValue = _moca.getStackVariable(columnName);
                    if (stackValue != null) {
                        Object obj = stackValue.getValue();
                        if (obj != null && !obj.toString().isEmpty()) {
                            obj = stackValue.asType(type);
                        }
                        // We have to convert all nulls including empty string
                        // to actually be a null
                        else {
                            obj = null;
                        }
                        // If the column is nullable or it is not null and the
                        // value is not null then we add it.  Thus if the
                        // column was not null and the value was null we won't
                        // add it to the list which will allow for people to
                        // use default values
                        if (column.isNullable() || obj != null && 
                                !containsOnlyWhitespace(obj.toString())) {
                            values.add(new MocaArgument(columnName, 
                                    type, obj));   
                        }
                        else {
                            _logger.debug(MocaUtils.concat(
                                    "Skipping not nullable column ", columnName,
                                    " as the value for it on the stack is null"));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * This is a simple quick and dirty whitespace check.  This prevents having
     * to create a new string to check if it is not provided.
     * @param string The string to check
     * @return whether or not this String has only whitespace
     */
    private final static boolean containsOnlyWhitespace(String string) {
        for (char c : string.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }
    
    private interface FieldEvaluation {
        public Object evaluate(MocaContext moca);
    }
    
    private static class SimpleDateEvaluation implements FieldEvaluation {
        public Object evaluate(MocaContext moca) {
            return new Timestamp(System.currentTimeMillis());
        }
    }
    
    private static class SimpleConstantEvaluation implements FieldEvaluation {
        public SimpleConstantEvaluation(Object object) {
            _object = object;
        }
        public Object evaluate(MocaContext moca) {
            return _object;
        }
        
        private final Object _object;
    }
    
    private static class UserIdEvaluation implements FieldEvaluation {
        public Object evaluate(MocaContext moca) {
            String userId = moca.getSystemVariable("USR_ID");
            
            if (userId == null || containsOnlyWhitespace(userId)) {
                return "NOUSER";
            }
            else {
                return userId.toUpperCase();
            }
        }
    }

    private final MocaContext _moca;
    private final DBType _type;
    private final DatabaseTool _db;
    private final static DateTimeFormatter _formatter = 
        DateTimeFormat.forPattern("YYYYMMddHHmmss");
    private final static Logger _logger = LogManager.getLogger(
            JDBCCrudManager.class);
    private final static Map<String, FieldEvaluation> _createEvaluations;
    private final static Map<String, FieldEvaluation> _updateEvaluations;
    private final static Map<String, FieldEvaluation> _removeEvaluations;
    
    static {
        {
            Map<String, FieldEvaluation> createEvaluations = 
                new HashMap<String, FieldEvaluation>();
            
            createEvaluations.put("LAST_UPD_DT", new SimpleDateEvaluation());
            createEvaluations.put("MODDTE", new SimpleDateEvaluation());
            createEvaluations.put("INS_DT", new SimpleDateEvaluation());
            createEvaluations.put("LAST_UPD_USER_ID", new UserIdEvaluation());
            createEvaluations.put("MOD_USR_ID", new UserIdEvaluation());
            createEvaluations.put("INS_USER_ID", new UserIdEvaluation());
            createEvaluations.put("U_VERSION", new SimpleConstantEvaluation(1));
            
            _createEvaluations = Collections.unmodifiableMap(createEvaluations);
        }
        
        {
            Map<String, FieldEvaluation> updateEvaluations = 
                new HashMap<String, FieldEvaluation>();
            
            updateEvaluations.put("LAST_UPD_DT", new SimpleDateEvaluation());
            updateEvaluations.put("MODDTE", new SimpleDateEvaluation());
            updateEvaluations.put("INS_DT", null);
            updateEvaluations.put("LAST_UPD_USER_ID", new UserIdEvaluation());
            updateEvaluations.put("MOD_USR_ID", new UserIdEvaluation());
            updateEvaluations.put("INS_USER_ID", null);
            // We leave this one as a null value.  The update command has
            // to do a special replacement for this field.
            updateEvaluations.put("U_VERSION", new SimpleConstantEvaluation(null));
            
            _updateEvaluations = Collections.unmodifiableMap(updateEvaluations);
        }
        
        {
            _removeEvaluations = Collections.unmodifiableMap(
                    new HashMap<String, FieldEvaluation>());
        }
    }
}
