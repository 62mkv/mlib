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

package com.redprairie.moca.components.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.DatabaseTool;
import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaConstants;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.client.ProtocolException;
import com.redprairie.moca.exceptions.MissingArgumentException;
import com.redprairie.moca.server.exec.DefaultServerContext;
import com.redprairie.moca.server.exec.ResultsAccumulator;
import com.redprairie.moca.server.legacy.GenericPointer;
import com.redprairie.moca.server.parse.MocaParseException;
import com.redprairie.moca.server.parse.MocaParser;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.ComponentLibraryFilter;
import com.redprairie.moca.servlet.support.SupportZip;
import com.redprairie.moca.util.MocaIOException;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.ArgCheck;

/**
 * This class contains all the core methods for the MOCA component libraries.
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
public class CoreService {

    /**
     * Performs the "do loop" MOCA command.
     * 
     * @param ctx The MOCA context
     * @param iterations The number of executions to loop for.
     * @return A MocaResult data set with the values in a column named "i".
     */
    public MocaResults doLoop(MocaContext ctx, int iterations) {
        EditableResults res = ctx.newResults();
        res.addColumn("i", MocaType.INTEGER);

        for (int i = 0; i < iterations; i++) {
            res.addRow();
            res.setIntValue(0, i);
        }

        return res;
    }

    /**
     * Performs the "publish data" command by obtaining the where clause
     * arguments and creating a result set from them.
     * 
     * @param ctx The current MOCA context
     * @return A result set with the published argument values
     */
    public MocaResults publishData(MocaContext ctx) {
        MocaArgument[] arguments = ctx.getArgs(true);
        
        EditableResults res = ctx.newResults();

        // If there were no arguments just return the empty result set
        if (arguments.length == 0) {
            return res;
        }

        // This must be a LinkedHashMap to preserve column order.
        Map<String, MocaArgument> argMap = new LinkedHashMap<String, MocaArgument>();
        for (MocaArgument arg : arguments) {
            argMap.put(arg.getName().toLowerCase(), arg);
        }
        
        res.addRow();

        for (MocaArgument arg : argMap.values()) {
            res.addColumn(arg.getName(), arg.getType());
            res.setValue(arg.getName(), arg.getValue());
        }
        
        return res;
    }

    /**
     * Performs the "publish data combination command by obtaining the where
     * clause and searching for a result set. If there is no result set it will
     * behave just like publish data
     * 
     * @param moca
     * @return
     * @throws MocaException
     */
    public MocaResults publishDataCombination(MocaContext moca)
            throws MocaException {
        MocaArgument[] arguments = moca.getArgs(true);

        EditableResults retRes = moca.newResults();

        // If there were no arguments just return empty
        if (arguments.length == 0) {
            return retRes;
        }

        ResultsAccumulator resAcc = new ResultsAccumulator();
        Map<String, Object> rowData = new LinkedHashMap<String, Object>();

        // Loop through to find the result set if any.
        for (int i = 0; i < arguments.length; i++) {
            MocaArgument arg = arguments[i];
            MocaType type = arg.getType();

            if (type == MocaType.RESULTS) {
                resAcc.addResults((MocaResults) arg.getValue());
            }
            else {
                rowData.put(arg.getName(), arg.getValue());
            }
        }

        // If there was at least one result set, we need to copy the information
        MocaResults result = resAcc.getResults();
        if (result != null) {
            MocaUtils.copyColumns(retRes, result, false);

            if (result.getRowCount() > 0) {
                // We first copy the result information
                MocaUtils.copyRowsByIndex(retRes, result);

                // Reset the result set so we can go over it
                retRes.reset();

                while (retRes.next()) {
                    // Then we copy the stack variables on top of the results
                    retRes.setValues(rowData);
                }
            }
            else if (!rowData.isEmpty()) {
                // Add the values if there were no rows and we had values
                retRes.addRow(rowData);
            }
        }
        // If not just add 1 row that the values can be put into
        else {
            retRes.addRow(rowData);
        }

        return retRes;
    }
    
    /**
     * Performs the "publish top rows" command.  Takes a result object, and
     * publishes an identical result set with no more than a certain number
     * of rows.
     * 
     * @param moca
     * @param res
     * @param rows
     * @return
     */
    public MocaResults publishTopRows(MocaContext moca, MocaResults res, int rows) {
        if (res.getRowCount() <= rows) {
            return res;
        }
        
        EditableResults retRes = moca.newResults();
        MocaUtils.copyColumns(retRes, res);

        // We copy all the rows until we get to the number or run out of rows
        RowIterator row = res.getRows();
        
        for (int i = 0; i < rows && row.next(); ++i) {
            MocaUtils.copyCurrentRowByIndex(retRes, row);
        }
        
        return retRes;
    }

    /**
     * Performs the "set return status" command by setting the arguments and
     * lookup values if needed.
     * 
     * @param ctx The current MOCA context
     * @param errorCode The exception error code to throw
     * @param message The exception's message
     * @return An empty MocaResults set if the status is 0;
     * @throws GenericException Thrown by this command if the status is not 0
     */
    public MocaResults setReturnStatus(MocaContext ctx, int errorCode,
            String message) throws GenericException {

        if (errorCode == 0) {
            return ctx.newResults();
        }

        Map<String, Object> args = new HashMap<String, Object>();
        Map<String, String> lookupArgs = new HashMap<String, String>();

        MocaArgument[] arguments = ctx.getArgs();

        for (int i = 0; i < arguments.length; i++) {
            MocaArgument arg = arguments[i];
            String argName = arg.getName();

            if (argName.startsWith("lookup_")) {
                argName = argName.substring(7);
                lookupArgs.put(argName, String.valueOf(arg.getValue()));
            }
            else {
                args.put(argName, arg.getValue());
            }
        }

        throw new GenericException(errorCode, message, args, lookupArgs);
    }

    /**
     * Performs the "expand statement variables" command by reading in the
     * string and finding any variables on the stack and creating the clause
     * separated by " and ". This also supports alias such as a.c
     * 
     * @param moca The moca context
     * @param string The string containing the variables to look on the stack
     *            for
     * @return A result set containing the "exdstr" value
     */
    public MocaResults expandStatementVariables(MocaContext moca, String string) {
        EditableResults retRes = moca.newResults();

        StringBuilder returnString = new StringBuilder();

        String[] splitString = string.split(",");

        for (String entry : splitString) {
            String[] splitAlias = entry.split("\\.", 2);
            String stackArg;
            String valueArg;

            // This means there wasn't an alias so use the same name
            // to look and append
            if (splitAlias.length == 1) {
                stackArg = splitAlias[0];
                valueArg = splitAlias[0];
            }
            // Since we are splitting on 2 it should only have 2
            else {
                stackArg = splitAlias[0];
                valueArg = splitAlias[1];
            }

            MocaValue value = moca.getStackVariable(valueArg);
            // If the value is on the stack do some stuff
            if (value != null) {

                // If we have previous values put an and in between
                if (returnString.length() > 0) {
                    returnString.append(" and ");
                }

                Object variable = value.getValue();

                returnString.append(stackArg);

                if (variable != null) {
                    returnString.append('=');

                    // If it is a string we have to put single quotes around it
                    if (variable instanceof String) {
                        returnString.append('\'');
                        returnString.append(variable);
                        returnString.append('\'');
                    }
                    else {
                        returnString.append(variable);
                    }
                }
                else {
                    returnString.append(" is null");
                }
            }
        }

        retRes.addColumn("exdstr", MocaType.STRING);

        retRes.addRow();

        retRes.setStringValue("exdstr", returnString.toString());

        return retRes;
    }

    /**
     * Performs the "get random number" by using a static RandomNumber object.
     * If a user provides a max number the number is guaranteed to be between 0
     * and max number
     * 
     * @param maxNumber The maximum number to generate up to
     * @return a result set containing the random number
     */
    public MocaResults getRandomNumber(MocaContext moca, Integer maxNumber) {

        int randomNumber;

        synchronized (_random) {
            // Now generate the random number, we take the abs value of the
            // max number since we don't want to give it negatives
            randomNumber = maxNumber != null ? _random.nextInt(Math
                .abs(maxNumber)) : _random.nextInt(100);
        }

        EditableResults retRes = moca.newResults();

        retRes.addColumn("random", MocaType.INTEGER);

        retRes.addRow();

        retRes.setIntValue("random", randomNumber);

        return retRes;
    }

    /**
     * This method will dump the results of a result set to the trace file if it
     * is enabled.
     * 
     * @param moca The moca context
     * @param results The result set to dump
     */
    public void dumpResults(MocaContext moca, MocaResults results) {
        _logger.debug("---------------------------------");

        int columnCount = results.getColumnCount();
        int rowCount = results.getRowCount();

        _logger.debug("Dumping result set...");
        // We do this since toString could be overridden as well as hascode
        // Also we don't have access to the memory location it is the identity
        _logger.debug("Identity: "
                + Integer.toHexString(System.identityHashCode(results)));
        _logger.debug("Rows   : " + rowCount);
        _logger.debug("Columns: " + columnCount);
        // TODO maybe DL and AL should be removed?
        _logger.debug("Legend : N=Name T=Type DL=Defined-Len AL=Actual-Len "
                + "X=Nullable H=Hidden");

        for (int i = 0; i < columnCount; ++i) {
            String columnName = results.getColumnName(i);
            MocaType columnType = results.getColumnType(i);

            // TODO this doesn't support defined and actual max length, nullable
            // or hidden
            _logger.debug("Column " + (i + 1) + ": N[" + columnName + "] T["
                    + columnType.getTypeCode() + "] X[1]");
        }

        if (rowCount == 0) {
            _logger.debug("There is no data for this result set");
        }

        RowIterator rowIter = results.getRows();

        // this is odd but basically it is a while(rowIter.next())
        for (int currentRow = 1; rowIter.next(); currentRow++) {
            _logger.debug("*** Row " + currentRow + " ***");

            // Now we loop through all of the columns of this row to trace
            for (int currentColumn = 0; currentColumn < columnCount; currentColumn++) {

                // First check if the value for this column is null
                if (rowIter.isNull(currentColumn)) {
                    _logger.debug("Column " + (currentColumn + 1)
                            + ": [(null)]");
                    continue;
                }

                // TODO this is where we would put the hidden check

                // Now lets switch on the column type
                switch (results.getColumnType(currentColumn)) {
                // If it was a result set we want to dump that as well
                case RESULTS:
                    MocaResults childResult = rowIter.getResults(currentColumn);
                    _logger
                        .debug("Column "
                                + (currentColumn + 1)
                                + ": ["
                                + Integer.toHexString(System
                                    .identityHashCode(childResult))
                                + "] (Results)");
                    _logger.debug("Dumping child result set...");
                    dumpResults(moca, childResult);
                    _logger.debug("Dumped child result set...");
                    break;
                // This is for pointers, does this even work with java now?
                case GENERIC:
                    // TODO Generics are not supported at this time
                    Object generic = rowIter.getValue(currentColumn);

                    _logger.debug("Column " + (currentColumn + 1) + ": ["
                            + generic + "] (Pointer)");
                    break;
                // This type is unknown how to display
                case UNKNOWN:
                    _logger.debug("Column " + (currentColumn + 1)
                            + ": [Can't Display]");
                    break;
                // We default everything else to just the toString
                // implementation
                default:
                    Object value = rowIter.getValue(currentColumn);
                    _logger.debug("Column " + (currentColumn + 1) + ": ["
                            + value + "]");
                    break;
                }
            }
        }

        _logger.debug("---------------------------------");
    }

    /**
     * Performs the "sort result set" command. It will sort the passed in result
     * set by the sort parameter list.
     * 
     * @param moca The moca context
     * @param results The results to sort
     * @param sortList The way to sort the list
     * @return A result set that contains the same result but is sorted
     */
    public MocaResults sortResultSet(MocaResults results, String sortList) {
        SimpleResults sortedResults = new SimpleResults();

        String[] sortListSplit = sortList.split(",");
        String[] sortColumns = new String[sortListSplit.length];
        Comparator<?>[] comparators = new Comparator<?>[sortListSplit.length];

        for (int i = 0; i < sortListSplit.length; ++i) {
            String columnDeclaration = sortListSplit[i].trim();
            // Find the first space, use this as delimiter
            int firstSpace = columnDeclaration.indexOf(' ');

            if (firstSpace != -1) {
                // Get the substring up to the space of the string, this should
                // be the column to sort on
                sortColumns[i] = columnDeclaration.substring(0, firstSpace);

                // If the space is not the last character than we need to check
                // if we are going to sort descending
                if (firstSpace < columnDeclaration.length() - 1) {
                    String descending = columnDeclaration.substring(
                        firstSpace + 1, columnDeclaration.length());

                    // If the first character is an d or D then use descending
                    if (descending.matches("^\\s*[Dd].*")) {

                        comparators[i] = new Comparator<Object>() {
                            @Override
                            @SuppressWarnings("unchecked")
                            public int compare(Object o1, Object o2) {

                                // If the objects are comparable then return
                                // the opposite or else just return equal
                                if (o1 instanceof Comparable
                                        && o2 instanceof Comparable) {
                                    return -1 * ((Comparable<Object>) o1).compareTo(o2);
                                }
                                return 0;
                            }

                        };
                    }
                }
            }
            else {
                // If there was no space just use that as the column
                sortColumns[i] = columnDeclaration;
            }

        }

        // First we copy the results into the simple results
        MocaUtils.copyResults(sortedResults, results);
        return sortedResults.sort(sortColumns, comparators);
    }

    /**
     * Performs the "set sequence next value" command. It will increment the
     * sequence until we get to the new value if it is greater than the current
     * value. If a next value is not provided we will increment until we get to
     * the value in the table column
     * 
     * @param moca The moca context
     * @param sequence The sequence to increment
     * @param nextValue The value to increment it to
     * @param safeMode Only 'T' is supported at this time.
     * @param tableName The table to check
     * @param columnName The column of the table to get the max on
     * @return The new value from the result
     * @throws GenericException This is thrown if not enough arguments are
     *             provided
     */
    public MocaResults setSequenceNextValue(MocaContext moca, String sequence,
            Integer nextValue, String safeMode, String tableName,
            String columnName) throws MocaException {

        int targetValue;

        // If the next value is not provided get it from the table
        if (nextValue == null) {
            // If the tale name or column name wasn't provided then we error
            if (tableName == null || tableName.trim().length() == 0
                    || columnName == null || columnName.trim().length() == 0) {
                throw new GenericException("There were not enough arguments "
                        + "provided");
            }

            MocaResults res = moca.executeCommand("[select nvl(max("
                    + columnName + "), 0) max_val" + "   from " + tableName
                    + "]");

            RowIterator rowIter = res.getRows();

            rowIter.next();

            targetValue = rowIter.getInt("max_val");
        }
        else {
            targetValue = nextValue;
        }

        DatabaseTool dbTool = moca.getDb();
        String nextSequence = dbTool.getNextSequenceValue(sequence);

        // If we are using safe mode, currently we always do
        // TODO implement way of doing fast in a single call
//        if (safeMode != null
//                && safeMode.toUpperCase().charAt(0) == 'T') {

            // while the next sequence is less than the target value
            while (Integer.parseInt(nextSequence) < targetValue) {
                nextSequence = dbTool.getNextSequenceValue(sequence);
            }
//        }

        EditableResults retRes = moca.newResults();

        retRes.addColumn("new_nextval", MocaType.INTEGER);

        retRes.addRow();

        retRes.setIntValue("new_nextval", Integer.parseInt(nextSequence));

        return retRes;
    }

    /**
     * Performs the "dump data" moca command. It will execute the given command
     * and then dump the results into a file.
     * 
     * @param moca
     * @param dumpCommand
     * @param fileName
     * @param createDirectory
     * @param dumpMode
     * @param dumpAppend
     * @param dumpTag
     */
    public void dumpData(MocaContext moca, String dumpCommand, String fileName,
            String createDirectory, String dumpMode, String dumpAppend,
            String version, String dumpTag) throws MocaException {

        ArgCheck.notNull(dumpCommand, "dump_command is required");
        ArgCheck.notNull(dumpMode, "dump_mode is required");

        // We append if the first letter is not f or F
        boolean append = dumpAppend.matches("^\\s*[^fF].*");
        boolean xmlMode = dumpMode.equalsIgnoreCase("XML");

        String expandedFileName;
        // If the file name is not provided then default it
        if (fileName == null || fileName.trim().length() == 0) {
            expandedFileName = MocaUtils.expandEnvironmentVariables(moca,
                "$LESDIR/log/dump_data." + dumpMode);
        }
        else {
            expandedFileName = MocaUtils.expandEnvironmentVariables(moca,
                fileName);
        }

        File fileToWriteTo = new File(expandedFileName);
        
        // If it isn't absolute then we have to apply it current directory
        if (!fileToWriteTo.exists() && !fileToWriteTo.isAbsolute()) {
            fileToWriteTo = new File(".", expandedFileName);
        }
        boolean createdFile = false;

        // If the file doesn't exist we need to create it
        if (!fileToWriteTo.exists()) {
            // If the parent directory doesn't exist we need to possibly create
            // the directories
            File parentDirectory = fileToWriteTo.getParentFile();
            if (!parentDirectory.exists()) {
                // If the create directory starts with a y, Y, t, T, or 1 then
                // we have to try to create the directory
                if (createDirectory.matches("^\\s*[yYtT1].*")) {
                    if (!parentDirectory.mkdirs()) {
                        throw new MocaIOException(
                            "There was an error creating " + "directories of "
                                    + parentDirectory.getAbsolutePath());
                    }
                }
            }
            try {
                // Now we actually try to create the file
                if (!fileToWriteTo.createNewFile()) {
                    throw new MocaIOException("Failure creating file "
                            + fileToWriteTo.getAbsolutePath());
                }
                createdFile = true;
            }
            catch (IOException e) {
                e.printStackTrace();
                throw new MocaIOException("Failure creating file "
                        + fileToWriteTo.getAbsolutePath(), e);
            }
        }

        MocaResults res = moca.executeCommand(dumpCommand);

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileToWriteTo, append), "UTF-8"));

            // If we are creating a new file put some of the xml header
            // information
            if (!append && xmlMode) {
                out.write("<?xml version=\"");
                out.write(version);
                out.write("\" ?>\n");
                out.write("<");
                out.write(dumpTag != null ? dumpTag : "xml_data");
                out.write(">\n");
            }

            RowIterator rowIter = res.getRows();

            String columnName = "FORMATED_DATA";
            boolean firstRow = true;
            while (rowIter.next()) {
                String rowType = rowIter.getString("rowtype");
                // If we are on the first row or we are doing a new file
                if (rowType.equalsIgnoreCase("HEADER")) {

                    // Skip writing the header if this isn't the first row or we're 
                    // running in append mode and didn't have to create the output file.
                    if (!firstRow || (append && !createdFile)) {
                        continue;
                    }
                }

                String columnValue = rowIter.getString(columnName);

                // If we are appending make sure to append it
                if (append) {
                    out.append(columnValue);
                    out.append('\n');
                }
                else {
                    out.write(columnValue);
                    out.write('\n');
                }

                firstRow = false;
            }

            // Lastly if it was XML put the ending tag when we aren't appending
            if (!append && xmlMode) {
                out.write("</");
                out.write(dumpTag != null ? dumpTag : "xml_data");
                out.write(">\n");
            }
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            e.printStackTrace();
            throw new MocaIOException("There was an issue writing to the file",
                e);
        }
        finally {
            try {
                if (out != null) {
                    out.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                _logger.debug("There was a problem closing the writer for file"
                        + fileToWriteTo.getAbsolutePath(), e);
            }
        }
    }

    /**
     * Performs the "filter data" command. It will get the values at the
     * provided stack level and then add to that result any values provided
     * overriding if neccessary
     * 
     * @param moca The moca context
     * @param mocaFilterLevel The level to get the results from
     * @return A result set containing all the values
     */
    public MocaResults filterData(MocaContext moca, Integer mocaFilterLevel) {

        // Use the provided level or 1 if not provided
        MocaResults res = moca.getLastResults(mocaFilterLevel == null ? 1
                : mocaFilterLevel);

        if (res == null) {
            res = new SimpleResults();
        }

        // Check to see if they passed arguments.
        MocaArgument[] passedArguments = moca.getArgs(true);
        if (passedArguments.length == 0) {
            return res;
        }

        // OK, they did, so we need to create our own results object.
        SimpleResults retRes;
        
        if (res instanceof SimpleResults) {
            retRes = (SimpleResults) res;
        }
        else {
            retRes = new SimpleResults();
            MocaUtils.copyResults(retRes, res);
        }
        
        // If there are no rows, then add our own
        if (retRes.getRowCount() == 0) {
            retRes.addRow();
        }

        // Throw all the arguments into a map
        for (MocaArgument argument : passedArguments) {

            // We want to skip moca_filter_level
            if (argument.getName().equals("moca_filter_level")) {
                continue;
            }

            // We have to play some games if this column already exists in the result set.
            int colNumber = retRes.getColumnNumber(argument.getName());
            if (colNumber >= 0)
            {
                // Change the column type to the type of the argument of the same name and set the value.
                retRes.setNull(colNumber);
                retRes.promoteColumn(colNumber, argument.getType());
                retRes.setValue(colNumber, argument.getValue());   
            }
            else
            {
                retRes.addColumn(argument.getName(), argument.getType());
                retRes.setValue(argument.getName(), argument.getValue());
            }
        }

        return retRes;
    }

    /**
     * Performs the "filter rows" command. It will get the values at the
     * provided stack level and use arguments to determine whether to
     * re-publish the row or not.
     * 
     * @param moca The moca context
     * @param mocaFilterLevel The level to get the results from
     * @return A result set containing all the values
     */
    public MocaResults filterRows(MocaContext moca, Integer mocaFilterLevel) throws MocaException {

        // Use the provided level or 1 if not provided
        MocaResults res = moca.getLastResults(mocaFilterLevel == null ? 1
                : mocaFilterLevel);
        
        // No data -- return empty results
        if (res == null) {
            return new SimpleResults();
        }
        
        return MocaUtils.filterResults(res, moca);
    }
    
    /**
     * Performs the "choose data" command by choosing data out of a previous
     * result set as decided by the columns value.
     * 
     * @param moca The moca context
     * @param columns The columns that will choose the data retrieved from the
     *            previous result set. This is a comma separated list. Column
     *            names can be aliased using newcol=oldcol
     * @return The new results as chosen.
     */
    public MocaResults chooseData(MocaContext moca, String columns) {
        EditableResults retRes = moca.newResults();
        MocaResults lastRes = moca.getLastResults(1);

        // If there was no previous result just return empty
        if (lastRes == null) {
            return retRes;
        }

        String[] splitColumns = columns.split(",");

        boolean currentRow = lastRes.next();

        retRes.addRow();

        for (int i = 0; i < splitColumns.length; ++i) {
            String columnDeclaration = splitColumns[i].trim();

            // If the string was empty just skip to next one
            if (columnDeclaration.length() == 0) {
                continue;
            }

            // Find the first space, use this as delimiter
            int firstEquals = columnDeclaration.indexOf('=');

            String aliasColumn = null;
            String valueColumn = null;

            if (firstEquals != -1) {
                // Get the substring up to the equals of the string, this should
                // be the column to alias on
                aliasColumn = columnDeclaration.substring(0, firstEquals);

                // If the equals is not the last character than we need to check
                // what the alias maps to
                if (firstEquals < columnDeclaration.length() - 1) {
                    valueColumn = columnDeclaration.substring(firstEquals + 1,
                        columnDeclaration.length());

                }
            }
            else {
                // If there was no equals just use that as the column for both
                valueColumn = columnDeclaration;
                aliasColumn = columnDeclaration;
            }

            MocaType type = MocaType.STRING;
            Object value = null;

            // If the last result set has the column use it
            if (valueColumn != null && lastRes.containsColumn(valueColumn)) {
                // If we have a current row grab it from it, else it is empty
                if (currentRow) {
                    value = lastRes.getValue(valueColumn);
                }
                type = lastRes.getColumnType(valueColumn);
            }
            // Else we get it from the stack
            else {
                MocaValue stackValue = moca.getStackVariable(valueColumn);
                if (stackValue != null) {
                    type = stackValue.getType();
                    value = stackValue.getValue();
                }
            }

            // Now we determine the type, add the column and set the value
            retRes.addColumn(aliasColumn, type);
            retRes.setValue(aliasColumn, value);
        }

        return retRes;
    }

    /**
     * Performs the "dump stack" command. This will dump the stack values to a
     * result set. If desired this will raise an exception if stop is provided
     * beginning with 'y' or 'Y'
     * 
     * @param moca The moca context
     * @param stop Whether or not to stop the execution by throwing an exception
     * @return The results sets containing the stack information
     * @throws GenericException Is thrown if stop begins with 'y' or 'Y' and
     *             will contain the results of the dump
     */
    public MocaResults dumpStack(MocaContext moca, String stop)
            throws GenericException {
        MocaResults retRes = DefaultServerContext.dumpStack(moca, true);

        if (stop != null && stop.matches("^\\s*[yY]")) {
            GenericException excp = new GenericException("Dump stack has "
                    + "stopped execution");
            excp.setResults(retRes);
            throw excp;
        }
        else {
            return retRes;
        }
    }

    /**
     * Performs the "reorder columns" command by reording the previous result
     * set as provided by the order argument. The order argument is a comma
     * separate list containing the ordering of the columns starting in the
     * first position. This command doesn't currently support result sets that
     * have more than one column of the same name. It will only take the first
     * column of that name from the result set.
     * 
     * @param moca The Moca Context
     * @param order The ordering of the result set to use
     * @return The last execution's results reordered
     * @throws GenericException
     */
    public MocaResults reorderColumns(MocaContext moca, String order)
            throws GenericException {
        // Get the last result set
        MocaResults results = moca.getLastResults(1);

        if (results == null) {
            throw new GenericException("There was no last result set");
        }

        String[] splitOrder = order.split(",");
        EditableResults retRes = moca.newResults();

        // Now go through each of the columns
        for (String column : splitOrder) {
            // If the column was empty then just ignore it
            if (column.trim().length() == 0) {
                continue;
            }

            // Check each column of the result set to see if it matches
            for (int i = 0; i < results.getColumnCount(); ++i) {
                if (column.trim().equalsIgnoreCase(results.getColumnName(i))) {
                    // If the column exists move it onto the new one
                    retRes.addColumn(results.getColumnName(i), results
                        .getColumnType(i));
                }
            }
        }

        // Now we have the columns in order as we wanted, so we can just merge
        // the results together
        MocaUtils.mergeResults(retRes, results);

        return retRes;
    }

    /**
     * Performs the "rename columns" command by taking the passed in result set
     * or the last result set (if one is not provided) and renaming the column
     * names to what is specified on the stack
     * 
     * @param moca The moca context
     * @param res The result set to rename columns on
     * @return A new result set with the same data but with renamed columns of
     *         the last result set
     * @throws IllegalArgumentException This is thrown if a result is not
     *             provided, or there are values on the stack that don't
     *             correspond to strings
     */
    public MocaResults renameColumns(MocaContext moca, MocaResults res)
            throws IllegalArgumentException {
        EditableResults retRes = moca.newResults();

        MocaResults resToRename;

        if (res == null) {
            resToRename = moca.getLastResults(1);

            // If the result set to get was still null, then error
            ArgCheck.notNull(resToRename, "There was no last result set");
        }
        else {
            resToRename = res;
        }
        MocaArgument[] mocaArguments = moca.getArgs();
        Map<String, String> mapRename = new HashMap<String, String>();

        // Stick all the renaming values into a map so that we can look
        // into it efficiently afterwards
        for (MocaArgument argument : mocaArguments) {
            if (argument.getType() == MocaType.STRING
                    && argument.getValue() != null) {
                mapRename.put(argument.getName(), (String) argument.getValue());
            }
            else {
                throw new IllegalArgumentException("Argument ["
                        + argument.getName() + "] must be of type String ["
                        + argument.getType() + "] and not null ["
                        + argument.getValue() + "]");
            }
        }

        int columns = resToRename.getColumnCount();
        for (int i = 0; i < columns; i++) {
            String columnName = resToRename.getColumnName(i);

            // We can assume since the map was constructed with non null
            // strings that we can straight swap it out
            if (mapRename.containsKey(columnName)) {
                columnName = mapRename.get(columnName);
            }

            MocaType type = resToRename.getColumnType(i);
            int maxLength = resToRename.getMaxLength(i);
            retRes.addColumn(columnName, type, maxLength);
        }

        // Now we can just copy the rows by index, since the columns should
        // be in the correct spots
        MocaUtils.copyRowsByIndex(retRes, resToRename);

        return retRes;
    }

    /**
     * Executes the hide stack variable command. This will hide a variable on
     * the stack by masking it with a special object
     * 
     * @param name The column variable on the stack to hide
     * @return The result set containing the hidden object
     */
    public MocaResults hideStackVariable(MocaContext moca, String name) {
        EditableResults hidden = moca.newResults();

        hidden.addColumn(name, MocaType.OBJECT);

        hidden.addRow();

        hidden.setValue(name, MocaConstants.HIDDEN);

        return hidden;
    }

    /**
     * Performs the list library versions command.
     * 
     * @param moca The moca context
     * @param filterCategory The category to filter on if provided
     * @return A result set containing all the libary information
     * @throws NotFoundException If there are no library versions found
     */
    public MocaResults listLibraryVersions(MocaContext moca,
            final String filterCategory) throws NotFoundException {
        MocaResults retRes = DefaultServerContext.listLibraryVersions(moca,
            new ComponentLibraryFilter() {

                @Override
                public boolean accept(ComponentLevel level) {
                    if (filterCategory == null ||
                            level.getName().equalsIgnoreCase(filterCategory)) {
                        return true;
                    }
                    return false;
                }
            });

        // If there were no rows found then we should throw up a not found
        // exception back to the caller
        if (retRes.getRowCount() == 0) {
            throw new NotFoundException(retRes);
        }

        return retRes;
    }
    
    public MocaResults checkCommandSyntax(MocaContext moca, String command) {
        int status = 0;
        int errpos = 0;
        int errline = 0;
        String errtext = null;
        
        SimpleResults res = new SimpleResults();
        HashMap<String, Object> rowData = new HashMap<String, Object>();

        try {
            MocaParser temp = new MocaParser(command);
            temp.parse();
        }
        catch (MocaParseException e) {
            errline = Integer.parseInt(e.getArgValue("line").toString());
            errpos = Integer.parseInt(e.getArgValue("byte").toString());
            errtext = e.getArgValue("text").toString();
            status = e.getErrorCode(); //which is eSRV_PARSE_ERROR [505]
        }
        
        res.addColumn("command", MocaType.STRING);
        res.addColumn("status", MocaType.INTEGER);
        res.addColumn("errorline", MocaType.INTEGER);
        res.addColumn("errorpos", MocaType.INTEGER);
        res.addColumn("errortext", MocaType.STRING);
        
        rowData.put("command", command);
        rowData.put("status", status);
        rowData.put("errorline", errline);
        rowData.put("errorpos", errpos);
        rowData.put("errortext", errtext);

        res.addRow(rowData);

        return res;
    }

    /**
     * Formats data in XML or CSV format
     * 
     * @param moca The moca context
     * @param command The command to retrieve data. Will ignore columns,
     *            tableName, whereClause, and orderBy if set
     * @param tag A tag to encapsulate XML
     * @param columns The columns to select
     * @param tableName The table to select from
     * @param whereClause The where clause to filter data
     * @param orderBy The statement to order the command results
     * @param mode The format for the returned data. Can be CSV, XML, XMLSTAG,
     *            or XMLETAG
     * @return A result set containing the formatted data
     * @throws MocaException If the command fails to execute
     */
    public MocaResults formatData(MocaContext moca, String command, String tag,
            String columns, String tableName, String whereClause,
            String orderBy, String mode) throws MocaException {
        SimpleResults res = new SimpleResults();

        if (mode == null) {
            mode = "XML";
        }

        // Output of command produces two columns: rowtype, data (CSV/XML)
        res.addColumn("rowtype", MocaType.STRING);
        res.addColumn("FORMATED_DATA", MocaType.STRING);

        StringBuilder buffer;
        HashMap<String, Object> rowData = new HashMap<String, Object>();

        if (mode.equals("XMLSTAG") || mode.equals("XMLETAG")) {
            // If have a tag
            if (tag != null) {
                buffer = new StringBuilder();
                buffer.append("<");
                if (mode.equals("XMLETAG")) {
                    buffer.append("/");
                }
                buffer.append(subSpecialChars(mode, tag));
                buffer.append(">");

                rowData.put("rowtype", "TAG");
                rowData.put("FORMATED_DATA", buffer.toString());
                res.addRow(rowData);
            }

            return res;
        }

        if (command == null) {
            if (tableName == null) {
                throw new MissingArgumentException("table_name");
            }

            command = String.format("[select %s from %s %s %s]",
                (columns != null) ? columns : "*", tableName,
                (whereClause != null) ? ("where " + whereClause) : "",
                (orderBy != null) ? ("order by " + orderBy) : "");
        }

        if (mode.equals("SHO")) {
            rowData.put("rowtype", tableName);
            rowData.put("FORMATED_DATA", command);
            res.addRow(rowData);
            return res;
        }

        // Execute command to be XMLed
        MocaResults commandResults = moca.executeCommand(command);

        RowIterator rowIter = commandResults.getRows();
        if (mode.equals("CSV")) {
            // Publish header with each row, handle it in dump component
            buffer = new StringBuilder();
            for (int col = 0; col < commandResults.getColumnCount(); col++) {
                if (col != 0) {
                    buffer.append(",");
                }
                buffer.append(commandResults.getColumnName(col));
            }
            rowData.put("rowtype", "HEADER");
            rowData.put("FORMATED_DATA", buffer.toString());
            res.addRow(rowData);
        }

        while (rowIter.hasNext()) {
            rowIter.next();

            if (mode.equals("XML")) {
                if (tag != null) {
                    buffer = new StringBuilder();
                    buffer.append("<");
                    buffer.append(subSpecialChars(mode, tag));
                    buffer.append(">");

                    rowData.put("rowtype", "TAG");
                    rowData.put("FORMATED_DATA", buffer.toString());
                    res.addRow(rowData);
                }
            }

            boolean first = true;

            // Loop through columns
            buffer = new StringBuilder();
            for (int col = 0; col < commandResults.getColumnCount(); col++) {
                String columnName = commandResults.getColumnName(col);
                MocaType dataType = commandResults.getColumnType(col);

                String tmpStr;

                if (!rowIter.isNull(col)) {
                    if (dataType == MocaType.DATETIME) {
                        SimpleDateFormat s = new SimpleDateFormat(
                            "yyyyMMddHHmmss");
                        tmpStr = subSpecialChars(mode, s.format(rowIter
                            .getDateTime(col)));
                    }
                    else if (dataType == MocaType.STRING) {
                        tmpStr = subSpecialChars(mode, rowIter.getString(col));
                    }
                    else if (dataType == MocaType.INTEGER) {
                        tmpStr = String.valueOf(rowIter.getInt(col));
                    }
                    else if (dataType == MocaType.DOUBLE) {
                        tmpStr = String.valueOf(rowIter.getDouble(col));
                    }
                    else if (dataType == MocaType.BOOLEAN) {
                        tmpStr = rowIter.getBoolean(col) ? "1" : "0";
                    }
                    else {
                        // TODO: Maybe do something better, but for now use a
                        // null value
                        tmpStr = "";
                    }
                }
                else {
                    // Maybe should skip this column for XML
                    tmpStr = "";
                }

                if (mode.equals("CSV")) {
                    if (!first) {
                        buffer.append(",");
                    }
                    buffer.append(tmpStr);
                    first = false;
                }
                else if (mode.equals("XML")) {
                    buffer = new StringBuilder();
                    buffer.append("<");
                    buffer.append(columnName);
                    buffer.append(" datatype=");
                    if (dataType == MocaType.DOUBLE) {
                        buffer.append("\"float\"");
                    }
                    else if (dataType == MocaType.STRING) {
                        buffer.append("\"string\"");
                    }
                    else if (dataType == MocaType.DATETIME) {
                        buffer.append("\"dattim\"");
                    }
                    else if (dataType == MocaType.INTEGER) {
                        buffer.append("\"int\"");
                    }
                    else if (dataType == MocaType.BOOLEAN) {
                        buffer.append("\"boolean\"");
                    }
                    else {
                        buffer.append("\"unknown\"");
                    }
                    buffer.append(">");
                    buffer.append(tmpStr);
                    buffer.append("</");
                    buffer.append(columnName);
                    buffer.append(">");

                    rowData.put("rowtype", "DATA");
                    rowData.put("FORMATED_DATA", buffer.toString());
                    res.addRow(rowData);
                }
            } // Get next column

            if (mode.equals("CSV")) {
                rowData.put("rowtype", "DATA");
                rowData.put("FORMATED_DATA", buffer.toString());
                res.addRow(rowData);
            }
            else if (mode.equals("XML")) {
                if (tag != null) {
                    buffer = new StringBuilder();
                    buffer.append("</");
                    buffer.append(subSpecialChars(mode, tag));
                    buffer.append(">");

                    rowData.put("rowtype", "TAG");
                    rowData.put("FORMATED_DATA", buffer.toString());
                    res.addRow(rowData);
                }
            }
        } // Get next row

        return res;
    }
    
    /**
     * @param moca
     * @param data
     * @param charsetStr
     * @param dataBinary
     * @param blockSize
     * @param pointer
     * @param pointerDataLength
     * @return
     * @throws MocaException
     */
    public MocaResults encryptDataUsingRPBF(MocaContext moca, String data, 
            String charsetStr, byte[] dataBinary, Integer blockSize, 
            GenericPointer pointer, Integer pointerDataLength) 
            throws MocaException {
        EditableResults retRes = moca.newResults();
        
        // We have to convert the string to binary first if it was provided.
        if (data != null) {
            Charset charset;
            if (charsetStr != null) {
                charset = Charset.forName(charsetStr);
            }
            else {
                charset = Charset.forName("UTF-8");
            }
            dataBinary = data.getBytes(charset);
        }
        // If we didn't get the string or binary we can't proceed.
        else if (dataBinary == null) {
            // Lastly we check the pointer and it's length.
            if (pointer == null || pointerDataLength == null) {
                throw new MissingArgumentException("data");
            }
            // Then delegate this to native code so we can still support the
            // pointer.
            return moca.executeCommand("encrypt data using rpbf native", 
                    new MocaArgument("data_ptr", pointer), 
                    new MocaArgument("data_len", pointerDataLength),
                    new MocaArgument("block_size", blockSize));
        }
        
        int dataSize = dataBinary.length;
        byte[] outData = new byte[dataSize];
        int inc = blockSize == null || blockSize <= 0 ? dataSize : blockSize;
        
        for (int i = 0; i < dataSize; i += inc) {
            // Loop through until we get to the max increment or we hit the
            // end as is.
            for (int j = 0; j < inc && j + i < dataSize; j++) {
                outData[i + j] = (byte)(dataBinary[i + j] ^ ((j % 255) + 1));   
            }
        }
        
        retRes.addColumn("encrypted_data", MocaType.BINARY);
        retRes.addRow();
        retRes.setBinaryValue(0, outData);
        
        return retRes;
    }
    
    /**
     * @param moca
     * @param data
     * @param charsetStr
     * @param dataBinary
     * @param blockSize
     * @return
     * @throws ProtocolException
     * @throws MissingArgumentException
     */
    public MocaResults decryptDataUsingRPBF(MocaContext moca, String data, 
            String charsetStr, byte[] dataBinary, Integer blockSize) 
            throws ProtocolException, MissingArgumentException {
        EditableResults retRes = moca.newResults();
        
        // We have to convert the string to binary first if it was provided.
        if (data != null) {
            Charset charset;
            if (charsetStr != null) {
                charset = Charset.forName(charsetStr);
            }
            else {
                charset = Charset.forName("UTF-8");
            }
            dataBinary = data.getBytes(charset);
        }
        // If we didn't get the string or binary we can't proceed.
        else if (dataBinary == null) {
            throw new MissingArgumentException("data");
        }
        
        int dataSize = dataBinary.length;
        byte[] outData = new byte[dataSize];
        int inc = blockSize == null || blockSize <= 0 ? dataSize : blockSize;
        
        for (int i = 0; i < dataSize; i += inc) {
            // Loop through until we get to the max increment or we hit the
            // end as is.
            for (int j = 0; j < inc && j + i < dataSize; j++) {
                outData[i + j] = (byte)(dataBinary[i + j] ^ ((j % 255) + 1));   
            }
        }
        
        retRes.addColumn("decrypted_data", MocaType.BINARY);
        retRes.addRow();
        retRes.setBinaryValue(0, outData);
        
        return retRes;
    }
    
    /**
     * This command will put an os variable into the system.
     * @param moca The moca context to put this into
     * @param name The name of the variable.
     * @param value The value to set.
     */
    public void putOsVariable(MocaContext moca, String name, String value) {
        moca.putSystemVariable(name, value);
    }
    
    /**
     * This will remove an os variable from the system.
     * @param moca The moca context to remove the variable.
     * @param name The name of the variable.
     */
    public void removeOsVariable(MocaContext moca, String name) {
        moca.removeSystemVariable(name);
    }
    
    /**
     * This will retrieve an os variable from the system.
     * @param moca The moca context to retrieve the variable from.
     * @param name The name of the variable value to get.
     * @return The result set containing the value.
     */
    public MocaResults getOsVariable(MocaContext moca, String name) {
        EditableResults retRes = moca.newResults();
        retRes.addColumn("value", MocaType.STRING);
        retRes.addRow();
        
        retRes.setStringValue(0, moca.getSystemVariable(name));
        
        return retRes;
    }
    
    public MocaResults generateSupportZip(MocaContext moca) throws IOException {
        File directory = new File(moca.getSystemVariable("LESDIR"), "log");
        String zipName = "support-" + moca.getSystemVariable("MOCA_ENVNAME") + "-"
                + MocaUtils.formatDate(new Date()) + ".zip";
        File supportZip = new File(directory, zipName);
        
        // Use this file name to create and populate a support zip.
        try (OutputStream out = new FileOutputStream(supportZip);) {
            SupportZip zip = new SupportZip(out, true);
            zip.generateSupportZip();
        }
        
        EditableResults res = moca.newResults();
        res.addColumn("filename", MocaType.STRING);
        res.addRow();
        res.setStringValue("filename", supportZip.getAbsolutePath());
        return res;
    }
    
    /**
     * Gathers information used for determining client encryption and security.
     * @param moca
     * @param name
     * @return
     */
    public MocaResults getEncryptionInformation(MocaContext moca) {
        String name = moca.getRegistryValue(MocaRegistry.REGKEY_SECURITY_ENCRYPTION);
        if (name == null) name = "";
        
        String charset = moca.getRegistryValue(MocaRegistry.REGKEY_SERVER_CLASSIC_ENCODING);
        if (charset == null) charset = "UTF-8";
        
        String serverKey = moca.getRegistryValue(MocaRegistry.REGKEY_SECURITY_SERVER_KEY);
        if (serverKey == null) serverKey = moca.getRegistryValue(MocaRegistry.REGKEY_CLUSTER_NAME);
        if (serverKey == null) serverKey = "";
        
        EditableResults res = moca.newResults();
        res.addColumn("name", MocaType.STRING);
        res.addColumn("charset", MocaType.STRING);
        res.addColumn("server_key", MocaType.STRING);
        res.addRow();
        res.setStringValue("name", name);
        res.setStringValue("charset", charset);
        res.setStringValue("server_key", serverKey);
        return res;
    }
    
    /**
     * Substitute special characters
     * 
     * @return the string with substitutions done
     */
    private String subSpecialChars(String mode, String str) {
        if (mode.equals("XML")) {
            str = str.replace("&", "&amp;");
            str = str.replace("<", "&lt;");
            str = str.replace(">", "&gt;");
            str = str.replace("'", "&apos;");
            str = str.replace("\"", "&quot;");
        }
        else if (mode.equals("CSV")) {
            str = str.replace("\"", "\"\"");
            // Quote the string if it contains one of the following:
            // Double quote, newline, carriage return, or comma
            if (str.contains("\"") || str.contains("\n") || str.contains("\r")
                    || str.contains(",")) {
                str = "\"" + str + "\"";
            }
            str = str.replace("\r", "");
        }

        return str;
    }

    private final static Logger _logger = LogManager.getLogger(CoreService.class);
    private final static Random _random = new Random(System.currentTimeMillis());
}
