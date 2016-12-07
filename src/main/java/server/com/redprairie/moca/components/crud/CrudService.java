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

package com.redprairie.moca.components.crud;

import java.util.Collection;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.crud.CodeInvalidException;
import com.redprairie.moca.crud.CrudManager;
import com.redprairie.moca.crud.CrudManager.ConcurrencyMode;
import com.redprairie.moca.crud.CrudManager.FieldRequirement;
import com.redprairie.moca.crud.CrudMode;
import com.redprairie.moca.crud.DateInvalidException;
import com.redprairie.moca.crud.FlagInvalidException;
import com.redprairie.moca.crud.MissingStackArgumentException;
import com.redprairie.moca.crud.TableDefinition;
import com.redprairie.moca.crud.TableFactory;
import com.redprairie.moca.util.MocaUtils;

/**
 * A CRUD Service class set that automatically handles creation and updating of
 * data based on the table definitions and variables on the context.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class CrudService {

    /**
     * Creates a new CrudService class
     * 
     * @param mocaCtx The MOCA context
     * @throws MocaException
     */
    public CrudService(MocaContext mocaCtx) throws MocaException {
        _moca = mocaCtx;
        _manager = MocaUtils.crudManager(_moca);
    }

    /**
     * Validates that the given field/variable exists on the context.
     * 
     * @param fieldName The name of the field/variable to check.
     * @param requiredLevel A requirement level. Generally an exception will be
     *                thrown unless this value is "NNIG"
     * @return An empty result set (This command either passes or throws an
     *         exception)
     * @throws MocaException Thrown if the argument is missing or null or empty
     */
    public void checkField(String fieldName, String requiredLevel)
            throws MocaException {
        
        FieldRequirement requirement = FieldRequirement.REQUIRE_VALUE;
        if ("NNIG".equals(requiredLevel)) {
            requirement = FieldRequirement.NOT_PRESENT_OR_REQUIRE_VALUE;
        }
        
        _manager.checkField(fieldName, requirement);
    }

    /**
     * Creates a new record in the specified table. Automatically fills the
     * insert user/time fields.
     * 
     * @param table The table to create the data in.
     * @param pkUpperCase If true, PK values are wrapped in UPPER() functions.
     * @return A list of field values that have been created.
     * @throws MocaException Thrown if fields are missing or the insert fails.
     */
    public MocaResults createRecord(String table, Boolean pkUpperCase)
            throws MocaException {
        
        Collection<MocaArgument> valueMap = _manager.createRecord(table, 
                pkUpperCase != null && pkUpperCase.equals(Boolean.TRUE));

        return createChangedValuesResults(CrudMode.INSERT, valueMap);
    }

    /**
     * Updates a record in the specified table. Automatically fills the update
     * user/time fields.
     * 
     * @param tableName The table to update the data in.
     * @param pkUpperCase If true, PK values are wrapped in UPPER() functions.
     * @param forceUpdate If true, The command will fail if a record with the
     *                matching PK is found.
     * @param conCheckMode Indicates the concurrency check mode. 0=None,
     *                1=U_Version check, 2=Data Overlay Otherwise the data will
     *                update.
     * @return A list of field values that have been modified.
     * @throws MocaException Thrown if fields are missing or the update fails.
     */
    public MocaResults updateRecord(String table, Boolean pkUpperCase,
                                    Boolean forceUpdate, Integer conCheckMode)
            throws MocaException {
        
        ConcurrencyMode mode;
        if (conCheckMode != null) {
            switch(conCheckMode.intValue()) {
            case 1:
                mode = ConcurrencyMode.U_VERSION;
                break;
            case 2:
                mode = ConcurrencyMode.DATA_OVERLAY;
                break;
            default:
                mode = ConcurrencyMode.NONE;
                break;
            }
        }
        else {
            mode = ConcurrencyMode.NONE;
        } 
        
        Collection<MocaArgument> values = _manager.changeRecord(table, 
                pkUpperCase != null && pkUpperCase.equals(Boolean.TRUE),
                forceUpdate == null || forceUpdate.equals(Boolean.TRUE), mode);
        
        return createChangedValuesResults(CrudMode.UPDATE, values);
    }

    /**
     * Removes a record from the specified table.
     * 
     * @param table The table to create the data in.
     * @param pkUpperCase If true, PK values are wrapped in UPPER() functions.
     * @param allowPartialPK If true the command executes if not all PK values
     *                are filled in.
     * @return An empty OK results set
     * @throws MocaException Thrown if fields are missing or the update fails.
     */
    public MocaResults removeRecord(String table, Boolean pkUpperCase,
                                    Boolean allowPartialPK)
            throws MocaException {

        Collection<MocaArgument> values = _manager.removeRecord(table, 
                pkUpperCase != null && pkUpperCase.equals(Boolean.TRUE), 
                allowPartialPK == null || allowPartialPK.equals(Boolean.FALSE));

        return createChangedValuesResults(CrudMode.REMOVE, values);
    }
    
    /**
     * Validates that a given value on the stack of the flag name is in the
     * valid flag format.
     * The flag name is validated through the use of the moca command.
     * @param flagName The name of the flag value to check on the stack.
     * @throws FlagInvalidException This is thrown if the flag value on the
     *         stack is indeed invalid.
     * @throws MissingStackArgumentException This is thrown if the name provided
     *         doesn't have a matching value on the stack.
     * @throws IllegalArgumentException This should never come out since we
     *         always provide a name.
     */
    public void validateFlag(String flagName) throws FlagInvalidException, 
            MissingStackArgumentException, IllegalArgumentException {
        _manager.validateFlagValue(new MocaArgument(flagName, null));
    }
    
    /**
     * @param codeName
     * @throws CodeInvalidException
     * @throws MissingStackArgumentException
     * @throws IllegalArgumentException
     */
    public void validateCode(String codeName) throws CodeInvalidException, 
            MissingStackArgumentException, IllegalArgumentException {
        _manager.validateCodeValue(new MocaArgument(codeName, null));
    }

    /**
     * Validates that the given field name(s) and their corresponding values do
     * not exist in the given table. This command is most often used to
     * determine if an insert or update should be performed.
     * 
     * @param tableName The table to check for values in
     * @param fieldName The field list to check, multiple values should be comma
     *                separated.
     * @param valueType Either empty for ensuring the value exists or "RES" to
     *                ensure it does not exist.
     * @return
     * @throws MocaException Thrown if an argument is missing, the table is
     *                 invalid, or the data is invalid.
     */
    public MocaResults validateData(String tableName, String fieldName,
            String valueType) throws MocaException {

        boolean hasValueType = (valueType != null && valueType.equals("RES"));

        // Split the field list and validate that the args are available
        String[] columnNames = fieldName.split(",");
        
        String[] trimmedColumnNames = new String[columnNames.length];
        
        for (int i = 0; i < columnNames.length; ++i) {
            trimmedColumnNames[i] = columnNames[i].trim();
        }

        boolean hasRecord = _manager.validateDataExists(tableName, 
                trimmedColumnNames);

        // if no value was supplied and the record should exist
        if (!hasValueType && !hasRecord) 
            throw new InvalidValueException(translateAppropriateVariables(
                    tableName, trimmedColumnNames), "tbl_" + tableName);

        // if value was supplied and the record should *not* exist
        if (hasValueType && hasRecord)
            throw new PrimaryKeyExistsException(translateAppropriateVariables(
                    tableName, trimmedColumnNames));

        return _moca.newResults();
    }
    
    private String translateAppropriateVariables(String tableName, 
            String[] columns) throws NotFoundException {
        StringBuilder builder = new StringBuilder();
        TableDefinition definition = TableFactory.getTableDefinition(tableName);
        
        for (String column : columns) {
            
            MocaValue value = _moca.getStackVariable(column);
            if (definition.getColumn(column) != null && 
                    value != null) {
                
                if (builder.length() != 0) {
                    builder.append("\n");
                }
                
                //Get the MLS ID for the variable name
                String columnText = column;
                try {
                    MocaResults mlsResult = _moca.executeCommand(
                                                    "get mls text where mls_id='" + column + "'" + 
                                                    " and locale_id=@@LOCALE_ID catch(-1403)");
                    if (mlsResult.next()) {
                        String text = mlsResult.getString("mls_text");
                        
                        if (text != null && text.trim().length() != 0) {
                            columnText = text;
                        }
                    }
                }
                catch (MocaException e) {
                    // If the mls text couldn't be found, we don't care just
                    // leave it as is then.
                }
                
                builder.append(columnText);
                builder.append(": ");
                builder.append(value.getValue());
            }
        }
        
        return builder.toString();
    }
    
    /**
     * This method is only to be called from a MOCA command invocation.  The
     * command must validate that dateName is indeed provided hence we do
     * not check here to make sure we only check once
     * @param dateName
     * @throws DateInvalidException
     * @throws MissingStackArgumentException
     */
    public void validateDate(String dateName) throws DateInvalidException, 
            MissingStackArgumentException {
        _manager.validateDateFormat(new MocaArgument(dateName, null));
    }
    
    /**
     * Creates a results set from the changed or inserted values
     * 
     * @param changedArgs The list of column names that were modified or
     *                inserted
     * @return A new MocaResults set of the changed values.
     */
    private MocaResults createChangedValuesResults(CrudMode mode, 
            Collection<MocaArgument> changedArgs) {

        EditableResults results = _moca.newResults();

        results.addColumn("OPER", MocaType.STRING);
        results.addRow();

        // If remove just set the value and exit
        if (mode == CrudMode.REMOVE) {
            results.setStringValue("OPER", "D");
            return results;
        }

        results.setStringValue("OPER", (mode == CrudMode.INSERT) ? "I" : "U");

        for (MocaArgument changedArg : changedArgs) {

            String columnName = changedArg.getName();
            MocaValue value = changedArg.getDataValue();
            results.reset();
            results.addColumn(columnName, value.getType());

            if (results.getRowCount() == 0)
                results.addRow();
            else
                results.next();

            results.setValue(columnName, value.getValue());
        }

        return results;
    }
    
    // Private fields
    private final MocaContext _moca;
    private final CrudManager _manager;
}
