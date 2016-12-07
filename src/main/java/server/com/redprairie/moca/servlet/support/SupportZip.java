/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2014
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

package com.redprairie.moca.servlet.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.redprairie.mad.reporters.SupportUtils;
import com.redprairie.mad.util.CsvWriter;
import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaRuntimeException;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.cluster.manager.ClusterRoleManager;
import com.redprairie.moca.server.InstanceUrl;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SpringTools;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.db.DBAdapter;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.legacy.NativeProcessPool;
import com.redprairie.moca.server.log.LoggingConfigurator;
import com.redprairie.moca.servlet.support.SupportHook.SupportType;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.moca.web.console.ComponentLibraryInformation;
import com.redprairie.moca.web.console.ConsoleException;
import com.redprairie.moca.web.console.DatabaseConnectionInformation;
import com.redprairie.moca.web.console.EnvironmentVariableInformation;
import com.redprairie.moca.web.console.JobInformation;
import com.redprairie.moca.web.console.LogFileInformation;
import com.redprairie.moca.web.console.MocaClusterAdministration;
import com.redprairie.moca.web.console.NativeProcessInformation;
import com.redprairie.moca.web.console.ResourceInformation;
import com.redprairie.moca.web.console.SessionAdministration;
import com.redprairie.moca.web.console.SessionInformation;
import com.redprairie.moca.web.console.TaskInformation;
import com.redprairie.util.SimpleFilenameFilter;

/**
 * SupportZip class creates a support zip of all known MOCA information. This
 * class assumes that you have a valid MOCA session BEFORE calling
 * generateSupportZip().
 * 
 * Copyright (c) 2014 RedPrairie Corporation All Rights Reserved
 * 
 * @author j1014843
 */
public class SupportZip {

    private final OutputStream _out;
    private final SystemContext _system;
    private final ServerContextFactory _factory;
    private final boolean _hasDatabase;
    private final int _customHookTimeout;
    private final static Logger _logger = LogManager
        .getLogger(SupportZip.class);
    private final static ConcurrentMap<String, SupportHook> _hooks = new ConcurrentHashMap<>();

    static {
        try {
            ApplicationContext ctx = SpringTools
                .getContextUsingDataDirectories("hooks.xml");

            if (ctx != null) {
                Collection<SupportHook> hooks = (Collection<SupportHook>) ctx
                    .getBeansOfType(SupportHook.class).values();
                for (SupportHook hook : hooks) {
                    _hooks.put(hook.getName(), hook);
                }
            }
        }
        catch (Exception e) {
            _logger.error(e);
            _logger.warn("Hooks defined in the hooks.xml will not be run.");
        }
    }

    private enum GenerationState {
        MAD("mad_probe_data"), SESSION(
                "moca_session"), NATIVE("native_process"), REGISTRY(
                "moca_registry"), LOG4J("runtime_log4j"), ENVIRONMENT(
                "moca_environment"), DATABASE("database_connections"), TASK(
                "moca_tasks"), JOB("moca_jobs"), RESOURCE("resource_usage"), LIBRARYVERSION(
                "library_version"), LOGFILES("log_files"), CLUSTERROLES(
                "cluster_roles"), CUSTOM_HOOKS("custom_hook_write"),
                INSTALL_LOG("install.log"), LESLOGS("logs"), JGROUPS_XML("jgroups_xml");

        GenerationState(String filename) {
            _name = filename;
        }

        String getFileName() {
            return _name;
        }

        private String _name;
    }

    public SupportZip(OutputStream out, boolean hasDatabase) {
        _out = out;
        _system = ServerUtils.globalContext();
        _factory = (ServerContextFactory) _system
            .getAttribute(ServerContextFactory.class.getName());
        _hasDatabase = hasDatabase;

        _customHookTimeout = Integer.parseInt(_system.getConfigurationElement(
            MocaRegistry.REGKEY_SERVER_SUPPORT_ZIP_TIMEOUT,
            MocaRegistry.REGKEY_SERVER_SUPPORT_ZIP_TIMEOUT_DEFAULT));
    }

    
    /***
     * Adds a support hook to generate some data into the support zip.
     * @param hook
     */
    public static void addHook(SupportHook hook) {
        // Here, we shouldn't have repeat registrations.
        // We can just warn/debug.
        if (!_hooks.containsKey(hook.getName())) {
            _hooks.put(hook.getName(), hook);
        }
        else {
            _logger.warn("Already registered support hook: " + hook.getName());
        }
    }

    /***
     * This method actually writes the zip to the output stream. You must have a
     * MOCA session BEFORE running this method.
     * 
     * @throws IOException
     */
    public void generateSupportZip() throws IOException {
        // Create the main environment zip entires.
        ZipOutputStream zip = new ZipOutputStream(_out);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zip,
            Charset.forName("UTF-8")));

        for (GenerationState state : GenerationState.values()) {
            CsvWriter csvWriter = new CsvWriter(writer);
            try {
                switch (state) {
                case MAD:
                    SupportUtils.populateSupportZip(zip);
                    break;
                case SESSION:
                    generateSessionEntries(zip, csvWriter, writer);
                    break;
                case NATIVE:
                    generateNativeProcessEntries(zip, csvWriter, writer);
                    break;
                case REGISTRY:
                    generateMocaRegistry(zip, csvWriter, writer);
                    break;
                case LOG4J:
                    generateLog4jConfigEntry(zip, csvWriter, writer);
                    break;
                case ENVIRONMENT:
                    generateMocaEnvironmentEntry(zip, csvWriter, writer);
                    break;
                case DATABASE:
                    if (!_hasDatabase) break;
                    generateDatabaseConnectionEntry(zip, csvWriter, writer);
                    break;
                case TASK:
                    if (!_hasDatabase) break;
                    generateTaskEntry(zip, csvWriter, writer);
                    break;
                case INSTALL_LOG:
                    generateInstallLogsEntries(zip, csvWriter, writer);
                    break;
                case LESLOGS:
                    generateLesLogsEntries(zip, csvWriter, writer);
                    break;
                case JOB:
                    if (!_hasDatabase) break;
                    generateJobEntry(zip, csvWriter, writer);
                    break;
                case RESOURCE:
                    generateResourceUsageEntry(zip, csvWriter, writer);
                    break;
                case LIBRARYVERSION:
                    generateLibraryVersionsEntry(zip, csvWriter, writer);
                    break;
                case LOGFILES:
                    generateLogFilesEntry(zip, csvWriter, writer);
                    break;
                case CLUSTERROLES:
                    generateClusterRolesEntry(zip, csvWriter, writer);
                    break;
                case CUSTOM_HOOKS:
                    generateCustomHooks(zip, csvWriter, writer);
                    break;
                case JGROUPS_XML:
                    generateJGroupsXMLEntry(zip, csvWriter, writer);
                    break;
                default:
                    break;
                }
            }
            catch (Exception e) {
                _logger.warn("Failed generating during state: " + state._name,
                    e);
                writeExceptionToZip(zip, writer, state.getFileName()
                        + "_exception.txt", e);
            }

        }
        zip.close();
    }

    /**
     * @param zip
     * @param csvWriter
     * @param writer
     * @throws IOException
     */
    private void generateCustomHooks(ZipOutputStream zip, CsvWriter csvWriter,
                                     BufferedWriter writer) throws IOException {
        for (Entry<String, SupportHook> hookEntry : _hooks.entrySet()) {
            String name = hookEntry.getKey();
            SupportHook hook = hookEntry.getValue();
            try {

                if (hook.getType() == SupportType.MOCA_RESULTS) {
                    zip.putNextEntry(new ZipEntry(name + ".csv"));
                    MocaResults results = runHookCallableMocaResults(hook);
                    writeMocaResultsToCsv(csvWriter, results);
                    writer.flush();
                }
                else {
                    zip.putNextEntry(new ZipEntry(name + ".txt"));
                    String str = runHookCallableString(hook);
                    writer.write(str);
                    writer.flush();
                }
            }
            catch (Exception e) {
                writeExceptionToZip(zip, writer, name + "_exception.txt", e);
            }
            finally {
                zip.closeEntry();
            }
        }
    }

    private MocaResults runHookCallableMocaResults(final SupportHook hook)
            throws InterruptedException, ExecutionException, TimeoutException {
        Callable<MocaResults> callable = new Callable<MocaResults>() {

            @Override
            public MocaResults call() throws Exception {
                return hook.getMocaResults(MocaUtils.currentContext());
            }

        };
        AsynchronousExecutor async = getExecutor();
        Future<MocaResults> future = async.executeAsynchronously(callable);
        return future.get(_customHookTimeout, TimeUnit.MILLISECONDS);
    }

    private String runHookCallableString(final SupportHook hook)
            throws InterruptedException, ExecutionException, TimeoutException {
        Callable<String> callable = new Callable<String>() {

            @Override
            public String call() throws Exception {
                return hook.getString(MocaUtils.currentContext());
            }

        };

        AsynchronousExecutor async = getExecutor();
        Future<String> future = async.executeAsynchronously(callable);
        return future.get(_customHookTimeout, TimeUnit.MILLISECONDS);
    }
    
    private AsynchronousExecutor getExecutor() {
        AsynchronousExecutor async = MocaUtils.asyncExecutor();
        if(async == null) { 
            throw new IllegalStateException(
                "Could not run custom support hook, because the async executor is not configured.");
        }
        return async;
    }

    /**
     * @param writer
     * @throws IOException
     * 
     */
    private void generateClusterRolesEntry(ZipOutputStream zip,
                                           CsvWriter csvWriter,
                                           BufferedWriter writer)
            throws IOException {
        ClusterRoleManager manager = (ClusterRoleManager) _system
            .getAttribute(ClusterRoleManager.class.getName());

        if (manager != null) {
            zip.putNextEntry(new ZipEntry("cluster-roles.csv"));
            Multimap<Node, RoleDefinition> multiMap = manager.getClusterRoles();
            MocaClusterAdministration clusterAdmin = (MocaClusterAdministration) _system
                .getAttribute(MocaClusterAdministration.class.getName());
            Map<Node, InstanceUrl> urls = clusterAdmin.getKnownNodes();

            Multimap<InstanceUrl, RoleDefinition> urlRoleMap = HashMultimap
                .create();

            for (Entry<Node, Collection<RoleDefinition>> entry : multiMap
                .asMap().entrySet()) {
                urlRoleMap.putAll(urls.get(entry.getKey()), entry.getValue());
            }

            Map<InstanceUrl, List<RoleDefinition>> copy = new HashMap<InstanceUrl, List<RoleDefinition>>();
            for (Entry<InstanceUrl, Collection<RoleDefinition>> entry : urlRoleMap
                .asMap().entrySet()) {
                copy.put(entry.getKey(),
                    new ArrayList<RoleDefinition>(entry.getValue()));
            }

            csvWriter.writeValue("url");
            csvWriter.writeValue("roles");
            csvWriter.writeEndLine();

            for (Entry<InstanceUrl, List<RoleDefinition>> entry : copy
                .entrySet()) {
                csvWriter.writeValue(entry.getKey());
                csvWriter.writeValue(entry.getValue());
                csvWriter.writeEndLine();
            }

            // Have to flush before closing the entry
            writer.flush();
            zip.closeEntry();
        }
    }

    /**
     * @param writer
     * @param csvWriter
     * @param zip
     * @throws IOException
     * 
     */
    private void generateLogFilesEntry(ZipOutputStream zip,
                                       CsvWriter csvWriter,
                                       BufferedWriter writer)
            throws IOException {
        zip.putNextEntry(new ZipEntry("log-files.csv"));

        MocaResults res;
        try {
            res = LogFileInformation.getLogFiles();
            if (res.getRowCount() > 0) {
                writeMocaResultsToCsv(csvWriter, res);
                writer.flush();
            }
        }
        finally {
            // Have to flush before closing the entry
            zip.closeEntry();
        }
    }

    /**
     * @param writer
     * @param csvWriter
     * @param zip
     * @throws IOException
     * 
     */
    private void generateLibraryVersionsEntry(ZipOutputStream zip,
                                              CsvWriter csvWriter,
                                              BufferedWriter writer)
            throws IOException {
        zip.putNextEntry(new ZipEntry("library-versions.csv"));
        try {
            MocaResults res;
            try {
                res = ComponentLibraryInformation.getComponentLibraries();
                if (res.getRowCount() > 0) {
                    writeMocaResultsToCsv(csvWriter, res);
                    writer.flush();
                }
            }
            catch (MocaException e) {
                throw new IOException("Could not read task information", e);
            }
        }
        finally {
            // Have to flush before closing the entry
            zip.closeEntry();
        }

    }

    /**
     * @param writer
     * @param csvWriter
     * @param zip
     * @throws IOException
     * 
     */
    private void generateResourceUsageEntry(ZipOutputStream zip,
                                            CsvWriter csvWriter,
                                            BufferedWriter writer)
            throws IOException {
        zip.putNextEntry(new ZipEntry("resource-usage.txt"));

        MocaResults res;
        try {
            res = ResourceInformation.getResourceInformation();
            if (res.getRowCount() == 1) {
                RowIterator iter = res.getRows();
                iter.next();
                // This should only contain 1 row so we want to make
                // it name value pair instead
                for (int i = 0; i < res.getColumnCount(); ++i) {
                    writer.write(res.getColumnName(i));
                    writer.write('=');
                    writer.write(String.valueOf(iter.getValue(i)));
                    writer.write('\n');
                }
            }
            else {
                throw new ConsoleException("Incorrect number of rows"
                        + " returned for resource information: "
                        + res.getRowCount());
            }
            writer.flush();
        }
        catch (MocaException e) {
            throw new IOException("Could not read task information", e);
        }
        catch (MocaRuntimeException e) {
            throw new IOException("Could not read task information", e);
        }
        finally {
            // Have to flush before closing the entry
            zip.closeEntry();
        }
    }

    private void generateDatabaseConnectionEntry(ZipOutputStream zip,
                                                 CsvWriter csvWriter,
                                                 BufferedWriter writer)
            throws IOException {
        zip.putNextEntry(new ZipEntry("database-connections.csv"));

        try {
            DBAdapter dbAdapter = _factory.getDBAdapter();
            DatabaseConnectionInformation databaseConnectionInformation = new DatabaseConnectionInformation(
                dbAdapter);
            MocaResults res = databaseConnectionInformation
                .getDatabaseConnections();
            writeMocaResultsToCsv(csvWriter, res);
            // Have to flush before closing the entry
            writer.flush();
        }
        catch (ConsoleException e) {
            throw new IOException("Could not read task information", e);
        }
        finally {

            zip.closeEntry();
        }
    }

    private void generateJobEntry(ZipOutputStream zip, CsvWriter csvWriter,
                                  BufferedWriter writer) throws IOException {
        zip.putNextEntry(new ZipEntry("jobs.csv"));

        JobInformation jobInformation = new JobInformation();
        MocaResults res;
        try {
            res = jobInformation.getJobDefinitions();
            if (res.getRowCount() > 0) {
                writeMocaResultsToCsv(csvWriter, res);
                // Have to flush before closing the entry
                writer.flush();
            }
        }
        catch (MocaException e) {
            throw new IOException("Could not read task information", e);
        }
        finally {
            zip.closeEntry();
        }
    }

    private void generateTaskEntry(ZipOutputStream zip, CsvWriter csvWriter,
                                   BufferedWriter writer) throws IOException {
        zip.putNextEntry(new ZipEntry("tasks.csv"));

        TaskInformation taskInformation = new TaskInformation();
        MocaResults res;
        try {
            res = taskInformation.getTaskDefinitions();
            if (res.getRowCount() > 0) {
                writeMocaResultsToCsv(csvWriter, res);
                // Have to flush before closing the entry
                writer.flush();
            }
        }
        catch (MocaException e) {
            throw new IOException("Could not read task information", e);
        }
        finally {

            zip.closeEntry();
        }
    }

    /**
     * @param writer
     * @param csvWriter
     * @param zip
     * @throws IOException
     * 
     */
    private void generateMocaEnvironmentEntry(ZipOutputStream zip,
                                              CsvWriter csvWriter,
                                              BufferedWriter writer)
            throws IOException {
        zip.putNextEntry(new ZipEntry("moca-environment.txt"));
        try {
            EnvironmentVariableInformation envVarInfo = new EnvironmentVariableInformation();
            Map<String, String> env = envVarInfo.getEnvironmentVariables();
            for (Entry<String, String> entry : env.entrySet()) {
                writer.write(entry.getKey());
                writer.write('=');
                writer.write(entry.getValue());
                writer.write('\n');
            }

            // Have to flush before closing the entry
            writer.flush();
        }
        finally {
            zip.closeEntry();
        }
    }

    /**
     * @param writer
     * @param csvWriter
     * @param zip
     * @throws IOException
     * 
     */
    private void generateLog4jConfigEntry(ZipOutputStream zip,
                                          CsvWriter csvWriter,
                                          BufferedWriter writer)
            throws IOException {
        // It should be full path
        String lesDir = System.getenv("LESDIR");
        File file = new File(new File(lesDir, "data"),
            LoggingConfigurator.RUNTIME_LOGGING_XML);
        // Copy the file contents to the output stream.
        writeFileToZip(file, zip, writer);
    }

    /**
     * @param writer
     * @param csvWriter
     * @param zip
     * @throws IOException
     * 
     */
    private void generateMocaRegistry(ZipOutputStream zip, CsvWriter csvWriter,
                                      BufferedWriter writer) throws IOException {
        // It should be full path
        String mocaRegistry = System.getenv("MOCA_REGISTRY");
        File file = new File(mocaRegistry);
        // Copy the file contents to the output stream.
        writeFileToZip(file, zip, writer);
    }
    
    /**
     * @param writer
     * @param csvWriter
     * @param zip
     * @throws IOException
     * 
     */
    private void generateInstallLogsEntries(ZipOutputStream zip, CsvWriter csvWriter,
                                      BufferedWriter writer) throws IOException {
        // Add all of the install logs that may exist in the LESDIR log directory.
        File logdir = new File(System.getenv("LESDIR"), "log");
        if (logdir.isDirectory()) {
            writeFilesToZipMatchingRegex(zip, writer, logdir, 
                "RedPrairieServerInstall-\\d+-\\d+\\.log",
                "userinstall-\\d+-\\d+\\.log",
                "install-\\w+-\\d+-\\d+\\.log",
                "installed-products\\.dat");
        }
        
        // Add all of the install logs that may exist in the LESDIR install directory.
        File installDir = new File(System.getenv("LESDIR"), "install");
        if (installDir.isDirectory()) {
            writeFilesToZipMatchingRegex(zip, writer, "install", installDir, "Install\\.log");
        }
    }
    
    /**
     * @param writer
     * @param csvWriter
     * @param zip
     * @throws IOException
     * 
     */
    private void generateLesLogsEntries(ZipOutputStream zip, CsvWriter csvWriter,
                                      BufferedWriter writer) throws IOException {
        final File logDir = new File(System.getenv("LESDIR"), "log");
        final long now = System.currentTimeMillis();
        final long MILLIS3DAYS = 259200000L;
        
        final long BYTES10MB =     10485760L;
        final long BYTES50MB =     52428800L;
        final long BYTES250MB  =  262144000L;
        
        // see if we can find mocaserver log from registry
        // if we find it, include it if was modified less than 3 days ago and is less than 50MB
        final String output = _system.getConfigurationElement(MocaRegistry.REGKEY_SERVICE_OUTPUT);
        final File outputLog = output == null ? null : new File(output);
        if (outputLog != null && outputLog.exists()) {
            final long fileBytes = outputLog.length();
            if (fileBytes < BYTES50MB && fileBytes > 0L && outputLog.lastModified() + MILLIS3DAYS >= now) {
                writeFileToZip(outputLog, "log", zip, writer);
            }
        }
        
        if (logDir.isDirectory()) {
            final File[] lesLogs = logDir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (!name.endsWith(".log")) {
                        return false;
                    }
                    
                    final File f = new File(dir, name);
                    final long fileBytes = f.length();
                    
                    // don't grab main log twice if it's in LES/log
                    if (f.equals(outputLog)) {
                        return false;
                    }
                    
                    // mocaserver main log
                    // grab it if it's less than 50MB and was modified 3 days ago
                    // LOGS THAT WE KNOW TO LOOK FOR:
                    // mocaserver.log
                    // servicename-stderr.YEAR-MONTH-DAY.log
                    // servicename-stdout.YEAR-MONTH-DAY.log
                    if (name.equals("mocaserver.log")
                            || _mocaLogsPattern.matcher(name).matches()) {
                        return fileBytes < BYTES50MB && fileBytes > 0L && f.lastModified() + MILLIS3DAYS >= now; 
                    }
                    
                    // all other logs
                    // grab it if it's less than 10MB and was modified 3 days ago
                    return fileBytes < BYTES10MB && fileBytes > 0L && f.lastModified() + MILLIS3DAYS >= now;
                }
            });
            
            // sort by modification date so that if we have to cut off
            // then we still take the newest files
            Arrays.sort(lesLogs, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return Long.valueOf(o2.lastModified()).compareTo(o1.lastModified());
                }
            });
           
            long zipBytes = 0;
            for (File logFile : lesLogs) {
                final long logFileSize = logFile.length();
                
                // dont go over maximum limit of 250MB
                // assumming average 90% compression for MOCA logs then
                // this will limit the compressed logs to ~25MB
                if (zipBytes + logFileSize >= BYTES250MB) {
                    break;
                }
                zipBytes += writeFileToZip(logFile, "log", zip, writer);
            }
        }
    }
    
    /**
     * @param writer
     * @param csvWriter
     * @param zip
     * @throws IOException
     * 
     */
    private void generateNativeProcessEntries(ZipOutputStream zip,
                                              CsvWriter csvWriter,
                                              BufferedWriter writer)
            throws IOException {
        zip.putNextEntry(new ZipEntry("native-processes.csv"));

        try {
            NativeProcessPool pool = _factory.getNativePool();
            NativeProcessInformation info = new NativeProcessInformation(pool);
            MocaResults res = info.getNativeProcesses();
            writeMocaResultsToCsv(csvWriter, res);
            // Have to flush before closing the entry
            writer.flush();
        }
        catch (ConsoleException e) {
            // Just log the error
            throw new IOException("Could not read native process data", e);
        }
        finally {
            zip.closeEntry();
        }

    }

    /**
     * @param writer
     * @param csvWriter
     * @param zip
     * @throws IOException
     * 
     */
    private void generateSessionEntries(ZipOutputStream zip,
                                        CsvWriter csvWriter,
                                        BufferedWriter writer)
            throws IOException {
        zip.putNextEntry(new ZipEntry("sessions.csv"));
        try {
            List<SessionInformation> infos = SessionAdministration
                .getSessionInformation();

            if (infos.size() > 0) {
                csvWriter.writeValue("session");
                csvWriter.writeValue("threadId");
                csvWriter.writeValue("lastCommand");
                csvWriter.writeValue("lastCommandTime");
                csvWriter.writeValue("lastSQL");
                csvWriter.writeValue("lastSQLTime");
                csvWriter.writeValue("lastScript");
                csvWriter.writeValue("lastScriptTime");
                csvWriter.writeValue("connectedIpAddress");
                csvWriter.writeValue("startedTime");
                csvWriter.writeValue("traceName");
                csvWriter.writeValue("sessionType");
                csvWriter.writeValue("environment");
                csvWriter.writeValue("status");
                csvWriter.writeEndLine();

                for (SessionInformation session : infos) {
                    csvWriter.writeValue(session.getName());
                    csvWriter.writeValue(session.getThreadId());
                    csvWriter.writeValue(session.getLastCommand());
                    csvWriter.writeValue(session.getLastCommandTime());
                    csvWriter.writeValue(session.getLastSQL());
                    csvWriter.writeValue(session.getLastSQLTime());
                    csvWriter.writeValue(session.getLastScript());
                    csvWriter.writeValue(session.getLastScriptTime());
                    csvWriter.writeValue(session.getConnectedIpAddress());
                    csvWriter.writeValue(session.getStartedTime());
                    csvWriter.writeValue(session.getTraceName());
                    csvWriter.writeValue(session.getSessionType());
                    csvWriter.writeValue(session.getEnvironment());
                    csvWriter.writeValue(session.getStatus());
                    csvWriter.writeEndLine();
                }
                // Have to flush before closing the entry
                writer.flush();
            }
        }
        finally {
            zip.closeEntry();
        }
    }
    
    /**
     * Generate an entry in the Support Zip for the
     * JGroups XML Configuration file if it exists.
     * @param writer
     * @param csvWriter
     * @param zip
     * @throws IOException
     * @throws SystemConfigurationException 
     * 
     */
    private void generateJGroupsXMLEntry(ZipOutputStream zip, CsvWriter csvWriter,
                                      BufferedWriter writer) throws IOException {
        // Get the JGroups XML Configuration file registry setting.
        String jgroupsXML = _system
                .getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_JGROUPS_XML);
        
        if (jgroupsXML != null) {
            // If the setting exists get the file from the data directories.
            File file = _system.getDataFile(new SimpleFilenameFilter(jgroupsXML));
            writeFileToZip(file, zip, writer);
        }
    }

    private long writeFileToZip(File file, ZipOutputStream zip,
                                BufferedWriter writer) throws IOException {
        return writeFileToZip(file, null, zip, writer);
    }
    
    private long writeFileToZip(File file, String baseName, ZipOutputStream zip,
                                BufferedWriter writer) throws IOException {
        // Copy the file contents to the output stream.
        if (baseName != null && !baseName.isEmpty()) {
            zip.putNextEntry(new ZipEntry(baseName + File.separatorChar + file.getName()));
        }
        else {
            zip.putNextEntry(new ZipEntry(file.getName()));
        }

        try (FileInputStream inputStream = new FileInputStream(file)) {
            int count;
            long bytesWritten = 0;
            byte[] bytes = new byte[1024];

            while ((count = inputStream.read(bytes)) != -1) {
                zip.write(bytes, 0, count);
                bytesWritten += count;
            }
            // Have to flush before closing the entry
            writer.flush();
            
            return bytesWritten;
        }
        finally {

            zip.closeEntry();
        }

    }

    // Helper method to write the Exception stack to the support zip
    private void writeExceptionToZip(ZipOutputStream zip,
                                     BufferedWriter writer, String entryName,
                                     Exception ex) throws IOException {
        zip.putNextEntry(new ZipEntry(entryName));
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            writer.write(sw.toString());
            writer.flush();
        }
        finally {
            zip.closeEntry();
        }
    }
    
    /**
     * Write multiple files to the zip output stream that match some regex(es).
     * This method accepts vararg or regexes and a file will be included if it is
     * under the <em>dir</em> directory and matches at least one of the regexes provided.
     * By default these files will be included inside of a directory named "log" 
     * inside of the zip output stream.
     * @param zip
     * @param writer
     * @param dir Parent dir where to look for files
     * @param strings List of regexes to test children on <em>dir</em> against
     * @throws IOException
     */
    private void writeFilesToZipMatchingRegex(ZipOutputStream zip,
                                              BufferedWriter writer, File dir, String... strings)
            throws IOException {
        writeFilesToZipMatchingRegex(zip, writer, "log", dir, strings);
    }
    
    /**
     * Write multiple files to the zip output stream that match some regex(es).
     * This method accepts vararg or regexes and a file will be included if it is
     * under the <em>dir</em> directory and matches at least one of the regexes provided.
     * This method also includes an optional directory name for the files to placed
     * inside of within the zip output stream.
     * @param zip
     * @param writer
     * @param insideZipDir An optional directory for the file to placed in within the zip stream.
     * @param dir Parent dir where to look for files
     * @param strings List of regexes to test children on <em>dir</em> against
     * @throws IOException
     */
    private void writeFilesToZipMatchingRegex(ZipOutputStream zip, BufferedWriter writer,
                                              String insideZipDir, File dir, String... strings)
            throws IOException {
        for (final String s : strings) {
            final String[] files = dir.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches(s);
                }
            });
            for(final String f : files) {
                writeFileToZip(new File(dir, f), insideZipDir, zip, writer);
            }
        }
    }

    private void writeMocaResultsToCsv(CsvWriter writer, MocaResults results)
            throws IOException {
        for (int i = 0; i < results.getColumnCount(); i++) {
            String columnName = results.getColumnName(i);
            writer.writeValue(columnName);
        }

        writer.writeEndLine();

        RowIterator iter = results.getRows();
        while (iter.next()) {
            for (int i = 0; i < results.getColumnCount(); ++i) {
                Object value = iter.getValue(i);
                writer.writeValue(value);
            }
            writer.writeEndLine();
        }
    }
    
    private static final Pattern _mocaLogsPattern = Pattern.compile("^moca\\.[\\w\\.-]+-(?:stdout|stderr)\\.\\d{4}-\\d{1,2}-\\d{1,2}\\.log$");
}