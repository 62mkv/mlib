/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

import java.util.Collection;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.exceptions.MissingArgumentException;
import com.redprairie.moca.exceptions.MissingPKException;
import com.redprairie.moca.exceptions.MocaDBException;


/**
 * The methods responsible for all of the CRUD command usage.
 * An instance of this class is not <B>thread safe</B> and should only
 * be called from a single thread at a time.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public interface CrudManager {
    
    /**
     * This method will attempt to change a given row in the desired table.  The
     * row is checked for it's existence before changing.  If the row is not
     * found it will attempt to insert the new row if the <B>forceUpdate</B>
     * argument is false.  Then the columns for the table are scanned.  It will 
     * then attempt to find a value for each column first from the 
     * MocaArguments provided.  If a value is specified including null it will 
     * use that value.  If there is no MocaArgument with that name available 
     * it will then attempt to use the current MOCA stack to see if that value 
     * is available.
     * @param tableName The table of which to change a record of.
     * @param capitalizePkValues Whether or not to capitalize all the PK values
     *        before passing them in.
     * @param forceUpdate If true this will make sure that a row is indeed 
     *        updated and will throw an exception if there isn't one.
     * @param mode The concurrency Mode of which to run in.  Please check 
     *        documentation of the different modes here {@link ConcurrencyMode}
     * @param arguments The arguments to use for the values.  This will take
     *        priority over the values provided on the stack.
     * @return A collection containing all the values that were used when 
     *         changing the row.
     * @throws NotFoundException This is thrown if the table doesn't exist or
     *         if the row is not present and force update is true.
     * @throws MissingPKException This is thrown if a pk value is not found
     *         in the arguments or the stack.
     * @throws MocaDBException This is thrown if there was an error while
     *         trying to change the row.
     * @throws IllegalArgumentException This is thrown if the table name is null
     *         or empty.
     */
    public Collection<MocaArgument> changeRecord(String tableName, 
            boolean capitalizePkValues, boolean forceUpdate, 
            ConcurrencyMode mode, MocaArgument... arguments) 
            throws NotFoundException, MissingPKException, MocaDBException,
            IllegalArgumentException;
    
    /**
     * This will check to make sure that a field exists on the stack or not.
     * @param fieldName The field to check on the stack
     * @param type The type of field to check for to make sure it is 
     *        correct.
     * @throws MissingArgumentException This is thrown if the argument is 
     *         missing from the stack.  This will be thrown only if the type
     *         requires the value to be present in all cases.
     * @throws EmptyArgumentException This is thrown if the argument is on
     *         the stack and is empty.  This will be thrown no matter the type.
     * @throws IllegalArgumentException This is thrown if either argument
     *         is null or empty if a string.
     */
    public void checkField(String fieldName, FieldRequirement type) 
            throws MissingArgumentException, EmptyArgumentException, 
            IllegalArgumentException;
    
    /**
     * This method will attempt to create a given row in the desired table.  The
     * table will be scanned for all columns.  It will then attempt to find a
     * value for each column first from the MocaArguments provided.  If a value
     * is specified including null it will use that value.  If there is no
     * MocaArgument with that name available it will then attempt to use the
     * current MOCA stack to see if that value is available.
     * PK Values can be automatically capitalized if desired as well by passing
     * in true to the argument.
     * @param tableName The table of which to create a new record into
     * @param capitalizePkValues Whether or not to capitalize all the pk values
     *        when creating the record.
     * @param arguments The argument values when creating the record.  These
     *        will be used before checking the stack.  If a value of null is
     *        present this will be used and the stack will not be searched.
     * @return A collection containing all the values that were used when 
     *         creating the row.
     * @throws MocaDBException this is thrown if there was an issue creating
     *         the record.
     * @throws NotFoundException This is thrown if the table is not found.
     * @throws MissingPKException This is thrown if a PK value for the table is 
     *         not provided.
     * @throws IllegalArgumentException This is thrown if the table name is
     *         null or empty.
     */
    public Collection<MocaArgument> createRecord(String tableName, 
            boolean capitalizePkValues, MocaArgument... arguments) 
            throws MocaDBException, NotFoundException, MissingPKException, 
            IllegalArgumentException;
    
    /**
     * This method will attempt to remove a record from the given table.  The
     * table will be scanned for all PK columns.  Only PK columns are checked
     * for the removal of the row.  The values will first be checked in the
     * provided MocaArguments.  If there is no argument that matches the name
     * it will fall back to the MOCA stack.  If no value is found in either 
     * place it will check the <B>requireAllPkArguments</B> argument.  If this is true
     * then a MissingPKException is thrown if not it will attempt the removal
     * with only a partial PK.
     * PK Values can be automatically capitalized if desired as well by passing
     * in true to the argument.
     * @param tableName The table of which to remove a record from
     * @param capitalizePkValues Whether or not to capitalize the pk values when
     *        attempting removal.
     * @param requireAllPkArguments Whether or not all PK arguments are
     *        required to be passed in to remove an entry.
     * @param arguments The argument values when creating the record.  These
     *        will be used before checking the stack.
     * @return A collection containing all the values that were used in the 
     *         delete statement.
     * @throws MocaDBException This is thrown if there was an issue removing
     *         the row from the database.
     * @throws NotFoundException This is thrown if the table doesn't exist or
     *         if the row was not updated in the database.
     * @throws MissingPKException This is thrown if there is no value
     *         provided for the MocaArguments and the PK names are not present 
     *         on the stack and the <B>requireAllPkArguments</B> argument is
     *         false.
     * @throws IllegalArgumentException This is thrown if there is no 
     *         MocaArgument or the MocaArgument has no name or value provided.
     */
    public Collection<MocaArgument> removeRecord(String tableName, boolean capitalizePkValues, 
            boolean requireAllPkArguments, MocaArgument... arguments) 
            throws MocaDBException, NotFoundException, MissingPKException, 
            IllegalArgumentException;
    
    /**
     * This method will validate that a code value is valid in the system.  The
     * name is the code entry name and the value is the code value in the 
     * system to see if they match.  If no value is provided the stack 
     * @param nameValue The name of the code to check against.  If a value is
     *        also provided for it, it will validate this value.  If not it
     *        will check the current MOCA stack to get the value to validate.
     * @throws CodeInvalidException This is thrown if the code is deemed to be
     *         invalid.
     * @throws MissingStackArgumentException This is thrown if there is no value
     *         provided for the MocaArgument and the name is not present on the
     *         stack.
     * @throws IllegalArgumentException This is thrown if there is no 
     *         MocaArgument or the MocaArgument has no name or value provided.
     */
    public void validateCodeValue(MocaArgument nameValue) 
            throws CodeInvalidException, MissingStackArgumentException, 
            IllegalArgumentException;
    
    /**
     * This command will check the named table to verify if data exists using
     * the required columns as part of a where clause when selecting into the
     * table.  The values for these columns can either be passed in as an
     * argument to this method and if not the stack will be used for the value.
     * We then return whether or not this data exists.
     * @param tableName The name of the table to check against.
     * @param columns The columns to use the in the existance check.  There 
     *        must be at least 1 column.
     * @param arguments The argument values when checking the existance.  These
     *        will be used before checking the stack.
     * @return Whether or not the data was found.
     * @throws NotFoundException This is thrown if the table doesn't exist
     * @throws MissingArgumentException This is thrown if column is desired and
     *         the value on the stack is not present, null, or empty string
     * @throws MissingPKException This occurs if no valid column values are
     *         provided that exist on the table.
     * @throws IllegalArgumentException This is thrown if the table is null, an
     *         emptry string or if there are no columns provided at all.
     */
    public boolean validateDataExists(String tableName, String[] columns, 
            MocaArgument... arguments) throws NotFoundException, 
            MissingArgumentException, MissingPKException, 
            IllegalArgumentException;
    
    /**
     * This method will validate that a given string value is in the correct
     * date format as specified by MOCA that being.
     * <B>YYYYMMddHHmmss</B>
     * <P>The format can be broken up into the following columns:
     *  <OL>
     *  <LI><B>YYYY</B> The 4 digit value of the current year
     *  <LI><B>MM</B> The 2 digit value of the month (1-12)
     *  <LI><B>dd</B> The 2 digit value of the day (1-31) depends on month
     *  <LI><B>HH</B> The 2 digit value of the hour (0-23)
     *  <LI><B>mm</B> The 2 digit value of the minute (0-59)
     *  <LI><B>ss</B> The 2 digit value of the second (0-59)
     *  </OL>
     * @param nameValue The name of the argument and also an optional value if
     *        desired.  If the value is not provided the stack will be searched
     *        instead.
     * @throws DateInvalidException This is thrown if the date is indeed an 
     *         incorrect format
     * @throws MissingStackArgumentException This is thrown if the value is not
     *         provided and when looking on the stack there is no value found
     *         there either.
     * @throws IllegalArgumentException This is thrown if the there is no name
     *         provided in the nameValue object
     */
    public void validateDateFormat(MocaArgument nameValue) 
            throws DateInvalidException, MissingStackArgumentException, 
            IllegalArgumentException;
    
    /**
     * This method will validate whether or not the value is a valid flag.  It 
     * will first try to use the value passed in the MocaArgument.  If there is
     * not one it will use the name of the MocaArgument and try to get the value
     * from the MOCA stack and use that to validate against.
     * A valid value is either 0 or 1.
     * @param nameValue The MocaArgument to  use.  If a value is provided we
     *        will use that value to validate if it is a flag value or not.
     *        If only a name is provided then we lookup the stack for the value.
     * @throws FlagInvalidException This is thrown if there is a value and it
     *         deemed to not be valid.
     * @throws MissingStackArgumentException This is thrown if only a name is 
     *         provided and that name is not on the stack.
     * @throws IllegalArgumentException This is thrown if the MocaArgument is 
     *         null or there is no name or value in the argument.
     */
    public void validateFlagValue(MocaArgument nameValue) 
            throws FlagInvalidException, MissingStackArgumentException, 
            IllegalArgumentException;
    
    /**
     * This enumeration is to define the different modes when doing a change
     * record.
     * 
     * <b><pre>
     * Copyright (c) 2009 RedPrairie Corporation
     * All Rights Reserved
     * </pre></b>
     * 
     * @author wburns
     * @version $Revision$
     * @see CrudManager#changeRecord(String, boolean, boolean, ConcurrencyMode, MocaArgument...)
     */
    public static enum ConcurrencyMode {
        NONE,
        /**
         * This will verify that u_version has not increased while the update
         * is occurring to ensure that someone else hasn't updated the row.
         */
        U_VERSION,
        /**
         * This will verify that the data hasn't changed and will verify that
         * all values on the stack are present in the table.
         */
        DATA_OVERLAY
    }
    
    /**
     * This enumeration is to define the different types of requirement when
     * doing a check field call.
     * 
     * <b><pre>
     * Copyright (c) 2009 RedPrairie Corporation
     * All Rights Reserved
     * </pre></b>
     * 
     * @author wburns
     * @version $Revision$
     * @see CrudManager#checkField(String, FieldRequirement)
     */
    public static enum FieldRequirement {
        /**
         * This requires the value to be available on the stack, not null, and
         * not empty if a string.
         */
        REQUIRE_VALUE,
        /**
         * This requires the value to either be not on the stack or on the 
         * stack, not null and not empty if a string. 
         */
        NOT_PRESENT_OR_REQUIRE_VALUE
    }
}
