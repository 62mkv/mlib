/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.nio.channels.FileChannel;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import oracle.jdbc.OracleResultSet;
import oracle.sql.BLOB;

import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.BeanResults;
import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaRuntimeException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.ModifiableResults;
import com.redprairie.moca.PagedResults;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.TooManyRowsException;
import com.redprairie.moca.cluster.ClusterCommandCallable;
import com.redprairie.moca.crud.CrudManager;
import com.redprairie.moca.crud.JDBCCrudManager;
import com.redprairie.moca.db.QueryHook;
import com.redprairie.moca.exceptions.InvalidArgumentException;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.db.translate.LimitHandler;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.servlet.support.SupportHook;
import com.redprairie.moca.servlet.support.SupportZip;
import com.redprairie.util.CommandLineParser;
import com.redprairie.util.StringReplacer;
import com.redprairie.util.VarStringReplacer;

/**
 * Utility functions to work with the MOCA container.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MocaUtils {
    
    /**
     * Gets the current thread's MOCA context.
     * @return the <code>MocaContext</code> object associated with the
     * current thread.
     */
    public static MocaContext currentContext() {
        ServerContext ctx = ServerUtils.getCurrentContext();
        if (ctx != null) {
            return ctx.getComponentContext();
        }
        else {
            return null;
        }
    }
    
    /**
     * Copies the data from one <code>MocaResults</code> object into
     * another one.  The source object's current row pointer is reset
     * before copying the source data, and the source object's current
     * row pointer will be at the end of the result after copying.  I.e,
     * {@link MocaResults#reset() source.reset} will be called before copying,
     * but not after.
     * @param res
     * @param source
     */
    public static void copyResults(EditableResults res, MocaResults source) {
        ResultUtils.copyResults(res, source);
    }
    
    /**
     * Copies the data from one <code>MocaResults</code> object into
     * another one, reusing columns on the target result set if they are
     * already defined.  The source object's current row pointer is reset
     * before copying the source data, and the source object's current
     * row pointer will be at the end of the result after copying.  I.e,
     * {@link MocaResults#reset() source.reset} will be called before copying,
     * but not after.
     * @param res
     * @param source
     */
    public static void mergeResults(EditableResults res, MocaResults source) {
        if (source != null) {
            MocaUtils.copyColumns(res, source, true);
            source.reset();
            MocaUtils.copyRows(res, source);
        }
    }
    
    /**
     * Copies the data from one <code>MocaResults</code> object into
     * another one.  It is assumed that columns are already defined in the
     * target result object.  Note that row data is copied from the source
     * to the target result set <i>by name</i>.  That means that if there are
     * duplicate columns in the source and target result set, they will not
     * both be copied to the target.  This is probably not what you want, since
     * the target result set may return null for that column.
     * @param res the target result set.
     * @param source the source result set.
     */
    public static void copyRows(ModifiableResults res, MocaResults source) {
        ResultUtils.copyRows(res, source);
    }
    
    /**
     * Copies the data from one <code>MocaResults</code> object into
     * another one.  It is assumed that columns are already defined in the
     * target result object, and that they correspond to the columns in the 
     * source result set, both in type and position.
     * @param res the target result set.
     * @param source the source result set.
     */
    public static void copyRowsByIndex(ModifiableResults res, MocaResults source) {
        ResultUtils.copyRowsByIndex(res, source);
    }
    
    /**
     * Copies the data from the current row of one <code>RowIterator</code>
     * object into another one. It is assumed that columns are already defined
     * in the target result object. Note that row data is copied from the source
     * to the target result set <i>by name</i>.  A new row is added to the target result set.
     * 
     * @param res the target result set
     * @param source the source result set
     */
    public static void copyCurrentRow(ModifiableResults res, RowIterator source) {
        ResultUtils.copyCurrentRow(res, source);
    }
    
    /**
     * Copies the data from the current row of one <code>RowIterator</code>
     * object into another one. It is assumed that columns are already defined
     * in the target result object. A new row is added to the target result set.
     * 
     * @param res the target result set
     * @param source the source result set
     */
    public static void copyCurrentRowByIndex(ModifiableResults res, RowIterator source) {
        ResultUtils.copyCurrentRowByIndex(res, source);
    }
    
    /**
     * Copies the column information (metadata) from one <code>MocaResults</code> object
     * into another one.  No data is copied into the target result object.  Duplicate
     * columns (i.e. ones having the same name) will be copied. 
     * @param res
     * @param source
     */
    public static void copyColumns(EditableResults res, MocaResults source) {
        ResultUtils.copyColumns(res, source);
    }
    
    /**
     * Copies the column information (metadata) from one <code>MocaResults</code> object
     * into another one.  No data is copied into the target result object.  If the
     * <code>merge</code> parameter is <code>true</code>, duplicate columns are ignored,
     * and the first column returned from the source results is used.
     * @param res the target result set
     * @param source the source result set.
     * @param merge if <code>true</code>, merge duplicate columns.  Otherwise, duplicate
     * columns are retained.
     */
    public static void copyColumns(EditableResults res, MocaResults source, boolean merge) {
        transformColumns(res, source, merge, null);
    }
    
    /**
     * Copies the column information (metadata) from one <code>MocaResults</code> object
     * into another one.  No data is copied into the target result object.  If the
     * <code>merge</code> parameter is <code>true</code>, duplicate columns are ignored,
     * and the first column returned from the source results is used.  The <code>nameMap</code>
     * argument is a mapping of old-to-new column names.  This method can be combined with the
     * <code>copyRowsByIndex</code> method to create a copy of a result set with modified
     * column names.
     * @param res the target result set
     * @param source the source result set.
     * @param merge if <code>true</code>, merge duplicate columns.  Otherwise, duplicate
     * columns are retained.
     * @param nameMap a mapping of source-to-target column names.  Source column
     * names must be in lower case.
     */
    public static void transformColumns(EditableResults res, MocaResults source, boolean merge, Map<String, String> nameMap) {
        ResultUtils.transformColumns(res, source, merge, nameMap);
    }
    
    /**
     * Copies the data from one <code>MocaResults</code> object into
     * an object array.  The JavaBeans properties of the given class
     * define the resulting columns.
     * @param cls the class of the classes to be copied.  The class must have
     * a default constructor.
     * @param source the results object to copy into the new objects.
     */
    public static <T> T[] createObjectArray(Class<? extends T> cls, MocaResults source) {
        BeanResults<T> res = new BeanResults<T>(cls);
        MocaUtils.copyRows(res, source);
        return res.getData();
    }
    
    /**
     * Puts an exception stack trace to the MOCA trace logs. The static log4j logger
     * now has an exception argument that should be used when logging.
     * @param moca the MOCA context that will publish the trace messages
     * @param level a MOCA trace level
     * @param e a z`able object
     * @param message A message to precede the stack trace in the trace log.
     */
    @Deprecated
    public static void traceException(MocaContext moca, int level, 
                                      Throwable e, String message) {
        if (moca.traceEnabled(level)) {
            do {
                moca.trace(level, message + ": " + e.toString());
                StackTraceElement[] stackTrace = e.getStackTrace();
                for (int i = 0; i < stackTrace.length; i++) {
                    moca.trace(level, " at " + stackTrace[i].toString());
                }
                message = "Caused by";
                e = e.getCause();
            } while (e != null);
        }
    }
    
    /**
     * Assist with spawning a java sub-process.  The returned array of Strings
     * will contain appropriate arguments to initiate a java VM with the
     * defined VM arguments for MOCA-controlled Java VM instances.
     * 
     * @return an array of <code>String</code> objects representing the
     * start of a java command line.  A main classname and arguments must
     * be added to the list to actually start a Java process.
     * 
     * @see MocaUtils#newVM32CommandLine()
     */
    public static String[] newVMCommandLine() {
        return newVMCommandLine(MocaRegistry.REGKEY_JAVA_VM, 
                MocaRegistry.REGKEY_JAVA_VMARGS);
    }
    
    /**
     * Assist with spawning a java sub-process.  The returned array of Strings
     * will contain appropriate arguments to initiate a java VM (32 bit) with 
     * the defined VM arguments for MOCA-controlled Java VM instances.
     * 
     * @return an array of <code>String</code> objects representing the
     * start of a java command line.  A main classname and arguments must
     * be added to the list to actually start a Java process.
     * 
     * @see MocaUtils#newVMCommandLine()
     */
    public static String[] newVM32CommandLine() {
        return newVMCommandLine(MocaRegistry.REGKEY_JAVA_VM32, 
                MocaRegistry.REGKEY_JAVA_VMARGS32);
    }
    
    /**
     * Constructs the vm command line using the specific vm and vmarg keys to
     * lookup in the registry.  If no vm is found then java is used instead.
     * @param vmKey
     * @param vmargKey
     * @return
     */
    private static String[] newVMCommandLine(String vmKey, String vmargKey) {
        List<String> commandLine = new ArrayList<String>();
        SystemContext ctx = ServerUtils.globalContext();
        
        String vmEnv = ctx.getConfigurationElement(vmKey);
        
        if (vmEnv == null || vmEnv.trim().length() == 0) {
            vmEnv = "java";
        }
        commandLine.add(vmEnv);
        
        // Add extra VM arguments, as specified by the environment
        String vmArgEnv = ctx.getVariable("MOCA_JAVA_VMARGS");
        if (vmArgEnv == null || vmArgEnv.trim().length() == 0) {
            vmArgEnv = ctx.getConfigurationElement(vmargKey);
        }
        
        if (vmArgEnv != null && vmArgEnv.trim().length() > 0) {
            commandLine.addAll(CommandLineParser.split(vmArgEnv));
        }

        // Add the classpath
        String classpath = System.getProperty("java.class.path");
        if (classpath != null) {
            commandLine.add("-classpath");
            commandLine.add(classpath);
        }

        // Add the arguments
        String[] commandLineArray = (String[]) commandLine.toArray(
                new String[commandLine.size()]);
        
        return commandLineArray;
    }
    
    /**
     * Initialize the state of the MOCA process logging.
     */
    public static void initLoggers() {
        if (_loggingInitialized) return;
        
        _loggingInitialized = true;
    }
    
    /**
     * Read properties from a standard configuration location.
     * @param ctx
     * @param props
     * @param filename
     */
    public static void readConfigProperties(MocaContext ctx, Properties props, String filename) {
        
        // Look in $LESDIR/data for files named *.properties or *.xml
        String topdir = ctx.getSystemVariable("LESDIR");
        
        // No LESDIR defined.  Just return
        if (topdir == null) {
            return;
        }
        
        // The "data" directory is where we expect configuration as well as run-time
        // files.
        File directory = new File(topdir, "data");
        
        File propFile = new File(directory, filename + ".properties");

        if (propFile.exists()) {
            InputStream str = null;
            try {
                str = new FileInputStream(propFile);
                props.load(str);
            }
            catch (InterruptedIOException e) {
                throw new MocaInterruptedException(e);
            }
            catch (IOException e) {
                // ignore error
            }
            finally {
                try { if (str != null) str.close(); }
                catch (IOException e) { 
                    // ignore error
                }
            }
        }

        File xmlFile = new File(directory, filename + ".xml");

        if (xmlFile.exists()) {
            InputStream str = null;
            try {
                str = new FileInputStream(xmlFile);
                props.loadFromXML(str);
            }
            catch (InterruptedIOException e) {
                throw new MocaInterruptedException(e);
            }
            catch (IOException e) {
                // ignore error
            }
            finally {
                try { if (str != null) str.close(); }
                catch (IOException e) { 
                    // ignore error
                }
            }
        }
    }
    
    /**
     * Parses a MOCA date String (YYYYMMDDHHmmss) into a standard Java date object.  This method
     * assumes that the string should be parsed in the current timezone.  If the date is formatted
     * incorrectly, <code>null</code> is returned.
     * 
     * @param dateString The date string to parse
     * @return The date as parsed from the string or null if invalid
     */
    public static Date parseDate(String dateString) {
        return DateUtils.parseDate(dateString);
    }

    /**
     * Formats a standard Java date into the MOCA date String (YYYYMMDDHHmmss) 
     * format.  If the date object provided is <code>null</code> then 
     * <code>null</code> is returned.
     * 
     * @param date The date object to format
     * @return The formatted string
     */
    public static String formatDate(Date date) {
        return DateUtils.formatDate(date);
    }
    
    /**
     * This method will take in a moca context and a path.  It will check the
     * path for any environment variables present and replace them with the
     * variables found on the moca context and return a string with the values
     * replaced
     * @param moca The moca context to get the environment variables from
     * @param path The path to replace environment variables in
     * @return the string with the replaced environment variables
     */
    public static String expandEnvironmentVariables(final MocaContext moca, 
            String path) {
        
        return new VarStringReplacer(new StringReplacer.ReplacementStrategy() {
            public String lookup(String name) {
                return moca.getSystemVariable(name);
            }
        }).translate(path);
    }
    
    /**
     * This method will take in a system context and a path.  It will check the
     * path for any environment variables present and replace them with the
     * variables found on the system context and return a string with the values
     * replaced
     * @param context The system context to get the environment variables from
     * @param path The path to replace environment variables in
     * @return the string with the replaced environment variables
     */
    public static String expandEnvironmentVariables(final SystemContext context, 
            String path) {
        
        return new VarStringReplacer(new StringReplacer.ReplacementStrategy() {
            public String lookup(String name) {
                return context.getVariable(name);
            }
        }).translate(path);
    }
    
    /**
     * This will copy a file from one location to another.  It currently doesn't
     * support privileges.  You must provide the whole path to the file with
     * all the values expanded. 
     * @see MocaUtils#expandEnvironmentVariables(MocaContext, String)
     * @param source The source file to copy from, may contain environment 
     *        variables
     * @param destination The destination file to copy to, may contain 
     *        environment variables
     * @throws MocaIOException this exception is thrown if the files
     *         don't exist, has problems transferring the files or problems
     *         closing the actual files when finishing
     */
    public static void copyFile(String source, String destination) 
            throws MocaIOException {
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        FileChannel inChannel = null; 
        FileChannel outChannel = null;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(destination);
            inChannel = inStream.getChannel();
            outChannel = outStream .getChannel();

            long transferred = 0;
            
            // Loop through until all of it is transferred
            while (transferred < inChannel.size()) {
                transferred += inChannel.transferTo(transferred, 
                        inChannel.size() - transferred, outChannel);
            }
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new MocaIOException(
                    "Error opening/transferring file", e);
        }
        finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            }
            catch (IOException e) {
                // Ignore Error
            }
            
            try {
                if (inChannel != null) {
                    inChannel.close();
                }
            }
            catch (IOException e) {
                // Ignore Error
            }
            
            try {
                if (outStream != null) {
                    outStream.close();
                }
            }
            catch (IOException e) {
                // Ignore Error
            }
            
            try {
                if (outChannel != null) {
                    outChannel.close();
                }
            }
            catch (IOException e) {
                // Ignore Error
            }
        }
    }

    public static FormatString format(String fmt, Object... args) {
        return new FormatString(fmt, args);
    }

    public static ConcatString concat(Object... args) {
        return new ConcatString(args);
    }
    
    /**
     * Copies the results from a SQL result set to a MOCA result set. All
     * columns are duplicated, even ones with redundant names. In addition,
     * column names are always forced to lower case.
     * 
     * This function will not close the SQL ResultSet and must be closed by the
     * caller.  The SQL ResultSet will be iterated through fully if no exception
     * is thrown, that is that {@link ResultSet#next()} will return false.
     * 
     * @param res an EditableResults object. This parameter may not be null.
     * @param sqlResults
     * @return the number of rows contained in the source result set.
     * @throws SQLException If there is any problem while reading the information
     *         from the SQL ResultSet
     */
    public static int copySqlResults(EditableResults res, ResultSet sqlResults)
            throws SQLException {
        try {
            return copySqlResults(res, sqlResults, null);
        }
        catch (MocaException e) {
            // This should never happen
            throw new MocaRuntimeException(e);
        }
    }
    
    public static int copySqlResults(EditableResults res, ResultSet sqlResults,
                                     QueryHook.QueryAdvisor advisor)
            throws SQLException, MocaException {
        return copySqlResults(res, sqlResults, advisor, 0L, 0L, false);
    }
    
    public static int copySqlResults(EditableResults res, ResultSet sqlResults,
                                     QueryHook.QueryAdvisor advisor,
                                     long start, long limit, boolean abortOnOverflow)
            throws SQLException, MocaException {
        // Bail out if we don't have a result set passed
        if (sqlResults == null) return 0;

        // First, set up results metadata
        List<MocaJdbcColumnMapping> metaData = processResultMetaData(res, sqlResults, advisor, null);

        return processResultSetRows(res, sqlResults, limit,
                abortOnOverflow, metaData, advisor);
    }
    
    /**
     * MOCA Internal Method:
     * Handles building SQL paged results given the SQL dialect
     * @param sqlResults The JDBC ResultSet
     * @param advisor Optional query advisor
     * @param limitHandler The LimitHandler for the SQL dialect that built the query
     * @param limit The max number of rows to process
     * @param abortOnOverflow Whether to throw an exception on the limit being hit or not
     * @param includesTotal Whether the SQL statement includes calculating the total possible
     *        number of rows as generated by the LimitHandler
     * @return
     * @throws SQLException
     * @throws MocaException
     */
    public static PagedResults buildSqlPagedResults(ResultSet sqlResults,
                                     QueryHook.QueryAdvisor advisor, LimitHandler limitHandler,
                                     long limit, boolean abortOnOverflow, boolean includesTotal)
            throws SQLException, MocaException {
        PagedResultsImpl pagedResults = new PagedResultsImpl();
        // Bail out if we don't have a result set passed
        if (sqlResults == null) return pagedResults;
        
        // First, set up results metadata
        List<MocaJdbcColumnMapping> metaData = processResultMetaData(pagedResults, sqlResults, advisor, limitHandler.getExcludedColumns());
        if (includesTotal && limitHandler.supportsTotalCount()) {
            pagedResults._totalRowCount = processResultSetRowsWithTotalCount(pagedResults, sqlResults, limit,
                    abortOnOverflow, metaData, advisor, limitHandler);
        }
        else {
            pagedResults._totalRowCount = processResultSetRows(pagedResults, sqlResults, limit,
                    abortOnOverflow, metaData, advisor);
        }
        
        return pagedResults;
    }
    
    /**
     * Processes JDBC ResultSet into a MOCA result set given the desired 
     * column meta data. Additionally handles that a LimitHandler was used
     * that calculated the total number of rows possible as a column in the
     * result set.
     * @param mocaRes The MOCA result set to add to
     * @param sqlResults The JDBC ResultSet to access data from
     * @param limit The limit # of rows to process
     * @param abortOnOverflow Whether to throw an exception or not if the limit is hit
     * @param columnData The column meta data, typically generated
     *        by {@link #processResultMetaData(EditableResults, ResultSet, com.redprairie.moca.db.QueryHook.QueryAdvisor, List)}
     * @param advisor The optional query advisor
     * @param handler The LimitHandler used for the query
     * @return The total possible number of rows as calculated by the LimitHandler
     * @throws SQLException
     * @throws MocaException
     */
    private static int processResultSetRowsWithTotalCount(EditableResults mocaRes,
            ResultSet sqlResults, long limit,
            boolean abortOnOverflow, List<MocaJdbcColumnMapping> columnData,
            QueryHook.QueryAdvisor advisor,
            LimitHandler handler)
            throws SQLException, MocaException {
        if (!sqlResults.next()) {
            return 0;
        }
        
        int totalRows = 0;
        long sqlRow = 0;
        totalRows = sqlResults.getInt(handler.getTotalColumnName());
        processRow(mocaRes, sqlResults,
                columnData);
        sqlRow++;
        
        for (;sqlResults.next(); sqlRow++) {
            // If they've passed the row limit, throw a "row limit exceeded" error.
            if (limit > 0 && sqlRow >= limit) {
                if (abortOnOverflow) {
                    throw new TooManyRowsException(limit);
                }
                else {
                    break;
                }
            }
            
            processRow(mocaRes, sqlResults,
                    columnData);
        }
        
        // TODO: This is getting called after the total result set is populated
        // rather than what the interface says is per row
        if (advisor != null) advisor.adviseRowData(mocaRes);
        
        return totalRows;
    }

    /**
     * Processes JDBC ResultSet into a MOCA result set given the desired 
     * column meta data.
     * @param mocaRes The MOCA result set to add to
     * @param sqlResults The JDBC ResultSet to access data from
     * @param limit The limit # of rows to process
     * @param abortOnOverflow Whether to throw an exception or not if the limit is hit
     * @param columnData The column meta data, typically generated
     *        by {@link #processResultMetaData(EditableResults, ResultSet, com.redprairie.moca.db.QueryHook.QueryAdvisor, List)}
     * @param advisor The optional query advisor
     * @return The number of rows in the built result set
     * @throws SQLException
     * @throws MocaException
     */
    private static int processResultSetRows(EditableResults mocaRes,
            ResultSet sqlResults, long limit,
            boolean abortOnOverflow, List<MocaJdbcColumnMapping> columnData, QueryHook.QueryAdvisor advisor)
            throws SQLException, MocaException {
        int rows = 0;
        for (long sqlRow = 0;sqlResults.next(); sqlRow++) {
            // If they've passed the row limit, throw a "row limit exceeded" error.
            if (limit > 0 && sqlRow >= limit) {
                if (abortOnOverflow) {
                    throw new TooManyRowsException(limit);
                }
                else {
                    return rows;
                }
            }
            
            processRow(mocaRes, sqlResults,
                    columnData);
            rows++;
        }
        
        // TODO: This is getting called after the total result set is populated
        // rather than what the interface says is per row
        if (advisor != null) advisor.adviseRowData(mocaRes);

        return rows;
    }

    /**
     * Processes the current row of the JDBC ResultSet (sqlResults) into
     * the MOCA result set (mocaRes) given the desired columns (columnData)
     * @param mocaRes The MOCA result set being added to
     * @param sqlResults The JDBC ResultSet where data is being accessed from
     * @param columnData List of meta data about the desired columns
     * @throws SQLException
     */
    private static void processRow(EditableResults mocaRes, ResultSet sqlResults,
            List<MocaJdbcColumnMapping> columnData)
            throws SQLException {
        // Allow us to be interrupted while processing this.
        checkInterrupt();
        
        mocaRes.addRow();
        for (MocaJdbcColumnMapping column : columnData) {
            Object value = null;
            if (column._mocaType == MocaType.STRING) {
                // Special case -- Oracle requires us to call clob.free in cases where
                // the CLOB is temporary.
                if (sqlResults instanceof OracleResultSet &&
                        (column._sqlType == Types.CLOB || column._sqlType == Types.NCLOB)) {
                    Clob clob = sqlResults.getClob(column._sqlColumnNumber);
                    if (clob != null) {
                        try {
                            value = clob.getSubString(1, (int)clob.length());
                        }
                        finally {
                            // This call to clob.free() is required by Oracle, who will not free
                            // temporary resources otherwise.
                            try {
                                clob.free();
                            }
                            catch (SQLException ignore) {
                            }
                        }
                    }
                }
                else {
                    value = sqlResults.getString(column._sqlColumnNumber);
                }
            }
            else if (column._mocaType == MocaType.INTEGER) {
                int temp = sqlResults.getInt(column._sqlColumnNumber);
                if (!sqlResults.wasNull()) {
                    value = Integer.valueOf(temp);
                }
            }
            else if (column._mocaType == MocaType.DOUBLE) {
                double temp = sqlResults.getDouble(column._sqlColumnNumber);
                if (!sqlResults.wasNull()) {
                    value = Double.valueOf(temp);
                }
            }
            else if (column._mocaType == MocaType.BINARY) {
                if (column._sqlType == Types.BLOB) {
                    Blob blob = sqlResults.getBlob(column._sqlColumnNumber);
                    if (blob != null) {
                        InputStream in = blob.getBinaryStream();
                        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int nbytes;
                        try {
                            while ((nbytes = in.read(buf)) >= 0) {
                                tmp.write(buf, 0, nbytes);
                            }
                        }
                        catch (InterruptedIOException e) {
                            throw new MocaInterruptedException(e);
                        }
                        catch (IOException e) {
                            throw new SQLException(
                                "IOException reading blob: " + e);
                        }
                        finally {
                            try {
                                in.close();
                            }
                            catch (IOException ignore) {
                            }
                            if (blob instanceof BLOB) {
                                // UGLY CODE WARNING.  This call to blob.free() is required by Oracle, who will
                                // not free temporary resources otherwise.  
                                try {
                                    blob.free();
                                }
                                catch (SQLException ignore) {
                                }
                            }
                        }

                        value = tmp.toByteArray();
                    }
                    else {
                        value = null;
                    }
                }
                else {
                    value = sqlResults.getBytes(column._sqlColumnNumber);
                }
            }
            else if (column._mocaType == MocaType.BOOLEAN) {
                boolean temp = sqlResults.getBoolean(column._sqlColumnNumber);
                if (!sqlResults.wasNull()) {
                    value = Boolean.valueOf(temp);
                }
            }
            else if (column._mocaType == MocaType.DATETIME) {
                java.sql.Timestamp temp = sqlResults
                    .getTimestamp(column._sqlColumnNumber);
                if (temp != null) {
                    value = new Date(temp.getTime());
                }
            }
            mocaRes.setValue(column._mocaColumnNumber, value);
        }
    }

    /**
     * Obtains the column meta data (mappings of JDBC ResultSet to MOCA result) for
     * the given JDBC ResultSet. Optionally, columns can be ignored by name.
     * @param mocaRes The MOCA Results to add columns to
     * @param sqlRes The JDBC ResultSet with the origin data
     * @param advisor The optional query advisor for advising meta data
     * @param ignoreColumns Optional list of columns to ignore
     * @return The list of column meta data
     * @throws SQLException
     * @throws MocaException
     */
    private static List<MocaJdbcColumnMapping> processResultMetaData(EditableResults mocaRes,
            ResultSet sqlRes,
            QueryHook.QueryAdvisor advisor,
            List<String> ignoreColumns) throws SQLException,
            MocaException {
        ResultSetMetaData meta = sqlRes.getMetaData();
        List<MocaJdbcColumnMapping> metaMapping = new ArrayList<MocaJdbcColumnMapping>(meta.getColumnCount());
        int addedColIndex = 0;
        for (int i = 0; i < meta.getColumnCount(); i++) {
            String columnName = meta.getColumnLabel(i + 1).toLowerCase();
            if (ignoreColumns != null && ignoreColumns.contains(columnName)) {
                continue;
            }
            
            String columnClassName = meta.getColumnClassName(i + 1);
            int sqlType = meta.getColumnType(i + 1);
            MocaType mocaType;
            int maxSize = 0;
            
            // Coerce columns of type java.sql.Types.OTHER to the appropriate type.  
            if (sqlType == Types.OTHER && columnClassName.equals("java.math.BigDecimal")) {
                sqlType = Types.DECIMAL;
            }
                    
            switch (sqlType) {
            case Types.BIT:
            case Types.BOOLEAN:
                mocaType = MocaType.BOOLEAN;
                maxSize = 4;
                break;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
                mocaType = MocaType.INTEGER;
                maxSize = 4;
                break;
            case Types.REAL:
            case Types.FLOAT:
            case Types.DOUBLE:
                mocaType = MocaType.DOUBLE;
                maxSize = 8;
                break;
            case Types.BIGINT:
            case Types.NUMERIC:
            case Types.DECIMAL:
                int precision = meta.getPrecision(i + 1);
                int scale = meta.getScale(i + 1);
                if (scale == 0 && precision > 0 && precision <= 10) {
                    mocaType = MocaType.INTEGER;
                    maxSize = 4;
                }
                else {
                    mocaType = MocaType.DOUBLE;
                    maxSize = 8;
                }
                break;
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case -101: // OracleTypes.TIMESTAMPTZ
            case -102: // OracleTypes.TIMESTAMPLTZ
                mocaType = MocaType.DATETIME;
                maxSize = 14;
                break;
            case Types.CLOB:
            case Types.NCLOB:
            case Types.LONGVARCHAR: // OracleTypes.LONG
            case Types.LONGNVARCHAR:
                mocaType = MocaType.STRING;
                maxSize = 51200;
                break;
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                mocaType = MocaType.BINARY;
                maxSize = 51200;
                break;
            case Types.BLOB:
                mocaType = MocaType.BINARY;
                maxSize = 0;
                break;
            case Types.CHAR:
            case Types.NCHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            default:
                mocaType = MocaType.STRING;
                maxSize = meta.getColumnDisplaySize(i + 1);
                break;
            }

            // Add the column to the result set and set its nullable flag.
            boolean isNullable = (meta.isNullable(i + 1) != ResultSetMetaData.columnNoNulls);
            mocaRes.addColumn(columnName, mocaType, maxSize,
                isNullable);
            metaMapping.add(new MocaJdbcColumnMapping(i + 1, sqlType, addedColIndex, mocaType));
            addedColIndex++;
        }
        
        if (advisor != null) advisor.adviseMetadata(mocaRes);

        return metaMapping;
    }
    
    public static MocaResults filterResults(MocaResults res, MocaContext moca) throws InvalidArgumentException {
        // Check to see if they passed arguments.
        MocaArgument[] passedArguments = moca.getArgs(false);
        if (passedArguments.length == 0) {
            return res;
        }
        
        RowIterator row = res.getRows();
        if (!row.hasNext()) {
            return res;
        }
        
        EditableResults out = new SimpleResults();
        MocaUtils.copyColumns(out, res);
        
        while (row.next()) {
            boolean publishRow = true;
            for (int i = 0; i < passedArguments.length && publishRow; i++) {
                
                MocaArgument arg = passedArguments[i];
                
                String name = arg.getName();
                if (!res.containsColumn(name)) {
                    throw new InvalidArgumentException(name);
                }
                
                MocaValue value = new MocaValue(res.getColumnType(name), row.getValue(name));
                MocaValue argValue = new MocaValue(arg.getType(), arg.getValue());
    
                switch (arg.getOper()) {
                case NOTNULL:
                    publishRow = value.isNull();
                    break;
                case ISNULL:
                    publishRow = !value.isNull();
                    break;
                case EQ:
                    publishRow = (value.compareTo(argValue) == 0);
                    break;
                case NE:
                    publishRow = (value.compareTo(argValue) != 0);
                    break;
                case GE:
                    publishRow = (value.compareTo(argValue) >= 0);
                    break;
                case GT:
                    publishRow = (value.compareTo(argValue) > 0);
                    break;
                case LE:
                    publishRow = (value.compareTo(argValue) <= 0);
                    break;
                case LT:
                    publishRow = (value.compareTo(argValue) < 0);
                    break;
                case LIKE:
                    if (value.isNull()) {
                        publishRow = false;
                    }
                    else {
                        publishRow = new LikeMatcher(argValue.asString()).match(value.asString());
                    }
                    break;
                case NOTLIKE:
                    if (value.isNull()) {
                        publishRow = false;
                    }
                    else {
                        publishRow = !new LikeMatcher(argValue.asString()).match(value.asString());
                    }
                    break;
                default:
                    // Ignore unknown argument comparisons
                    break;
                }
            }
            
            if (publishRow) {
                MocaUtils.copyCurrentRowByIndex(out, row);
            }
        }
        
        return out;
    }
    
    public static CrudManager crudManager(MocaContext moca) {
        return new JDBCCrudManager(moca);
    }
    
    /**
     * Returns the asynchronous executor that is available for this server.
     * @return the executor
     */
    public static AsynchronousExecutor asyncExecutor() {
        SystemContext sys = ServerUtils.globalContext();
        
        return (AsynchronousExecutor)sys.getAttribute(
                AsynchronousExecutor.class.getName());
    }
    
    /**
     * Returns the clustered asynchronous executor that is available for this
     * server.  If clustering is not enabled this will return null.
     * @return the clustered executor
     */
    public static AsynchronousExecutor clusterAsyncExecutor() {
        SystemContext sys = ServerUtils.globalContext();
        
        return (AsynchronousExecutor)sys.getAttribute("cluster-" +
                AsynchronousExecutor.class.getName());
    }
    
    /***
     * 
     * This is a convenience method to add a support hook to the 
     * MOCA support zip file generation. Any hooks that are added
     * are registered for the life time of the server and will be
     * run when support zip is generated.
     * 
     * 
     * @param the support hook
     */
    public static void addSupportHook(SupportHook hook) {
        SupportZip.addHook(hook);
    }
    
    
    /**
     * This is a convenience method to create a Callable that will execute the
     * given MOCA command with the provided arguments.
     * <p>
     * This callable will only call the command on the moca context of which
     * the calling thread of call was initiated.  Any transactional, native, 
     * threading or state implications are limited the command to be called.  
     * Thus this callable can be shared across threads and can be invoked 
     * multiple times assuming the command is safe to do so .
     * <p>
     * This callable is also serializable so it can be used with the cluster
     * asynchronous executor see - {@link #clusterAsyncExecutor()}. Note that if
     * using this with the clustered executor you should only execute commands
     * that do not require they be run on a specific node, for example if your command
     * interacts with the file system this would be unique to each node so you would want
     * to use the standard asynchronous executor in that
     * particular case, see - {@link #asyncExecutor()}
     * @param command The MOCA command to execute
     * @param args The arguments to use as part of the stack.
     * @return The callable that can be invoked.
     */
    public static Callable<MocaResults> mocaCommandCallable(final String command, 
            final MocaArgument... args) {
        return new ClusterCommandCallable(command, args);
    }
    
    /**
     * Gets the total number of possible rows for a result set generated by a paging query
     * if this was part of the SQL request. Otherwise, this simply returns the number of
     * rows in the given result set.
     * @param res The result set
     * @return The total number of possible rows (if a paging calculation was used), otherwise the current number of rows in the result set.
     */
    public static int getPagedTotalRowCount(MocaResults res) {
        if (res instanceof PagedResults) {
            return ((PagedResults) res).getTotalRowCount(); 
        }
        else {
            return res.getRowCount();
        }
    }
    
    private static void checkInterrupt() throws MocaInterruptedException {
        if (Thread.interrupted()) {
            throw new MocaInterruptedException();
        }
    }
    
    // Internal implementation of a PagedResults set where internally
    // we can set the total row count (# of possible rows without paging limitation)
    private static class PagedResultsImpl extends SimpleResults implements PagedResults {

        // @see com.redprairie.moca.PagedResults#getTotalRowCount()
        @Override
        public int getTotalRowCount() {
            return _totalRowCount;
        }
        
        private int _totalRowCount;
        private static final long serialVersionUID = -8023917073508506662L;
    }
    
    // Internal class to handle mapping of meta data from the
     // JDBC result set to the MOCA result set
     private static class MocaJdbcColumnMapping {
         
         /**
          * Meta data maps JDBC Result-->MOCA result for a column
          * @param jdbcColumnNumber The column number in the JDBC result set
          * @param sqlType The SQL type as defined in the JDBC result set
          * @param mocaColumnNumber The column number to be used in the MOCA result set
          * @param mocaType The MOCA type to be used in the MOCA result set for this column
          */
         public MocaJdbcColumnMapping(int jdbcColumnNumber, int sqlType, int mocaColumnNumber, MocaType mocaType) {
             _sqlColumnNumber = jdbcColumnNumber;
             _sqlType = sqlType;
             _mocaType = mocaType;
             _mocaColumnNumber= mocaColumnNumber;
         }
         
         private final int _sqlColumnNumber;
         private final int _mocaColumnNumber;
         private final int _sqlType;
         private final MocaType _mocaType;
     }

    // Implementation
    private static boolean _loggingInitialized = false;
}