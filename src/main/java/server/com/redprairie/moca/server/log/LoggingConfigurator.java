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

package com.redprairie.moca.server.log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import ch.elca.el4j.services.xmlmerge.AbstractXmlMergeException;
import ch.elca.el4j.services.xmlmerge.MergeAction;
import ch.elca.el4j.services.xmlmerge.XmlMerge;
import ch.elca.el4j.services.xmlmerge.action.OrderedMergeAction;
import ch.elca.el4j.services.xmlmerge.factory.StaticOperationFactory;
import ch.elca.el4j.services.xmlmerge.matcher.TagMatcher;
import ch.elca.el4j.services.xmlmerge.merge.DefaultXmlMerge;

import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.log.appender.MocaAppender;
import com.redprairie.moca.server.log.exceptions.LoggingRuntimeException;
import com.redprairie.moca.server.log.matcher.MocaSpecificLogOperationFactory;
import com.redprairie.util.ClassUtils;

/**
 * A configuration class that reads a logging configuration file if it is
 * available or the checks the configuration or configures a default logging
 * configuration.
 * 
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
public class LoggingConfigurator {

    /**
     * Configures the root logging configuration by merging
     * together all prod-dirs logging.xml under the "data" directory.
     * By default this will rebuild the runtime-logging.xml.
     */
    public static void configure() throws SystemConfigurationException {
        configure(true);
    }
    
    /**
     * Configures the root logging configuration. If buildLoggingXml is
     * true this will create the runtime-logging.xml file by merging
     * all prod-dirs logging.xml files under the corresponding products
     * "data" directory. If buildLoggingXml is false then it will only be only
     * be rebuilt if the product logging.xml files are older than runtime-logging.xml.
     * @param rebuildLoggingXml Specifies whether to build the runtime-logging.xml
     * @throws SystemConfigurationException
     */
    public static void configure(boolean rebuildLoggingXml) throws SystemConfigurationException {
        if (_isConfigured.compareAndSet(false, true)) {
            Configuration config = readConfigFile(rebuildLoggingXml);
            Logger logger = org.apache.logging.log4j.LogManager.getLogger(
                LoggingConfigurator.class);
            validateConfiguration(logger, config);
            setupJavaRedirect();
            setupJMXLogging(config);
        }
    }
    
    static File getRuntimeLoggingXml() {
        return new File(RUNTIME_LOGGING_XML_PATH);
    }

    /**
     * Reads the configuration file if it exists. The config file will be rebuilt
     * if rebuildLoggingXml is true or if the product logging.xml
     * files are out of date.
     * @param rebuildLoggingXml Whether to rebuild the runtime logging xml
     * @return The logging configuration
     * @throws SystemConfigurationException
     */
    private static Configuration readConfigFile(boolean rebuildLoggingXml)
            throws SystemConfigurationException {
        final SystemContext system = ServerUtils.globalContext();
        final File runtimeLoggingXml = getRuntimeLoggingXml();

        // rebuild the runtime-logging.xml if it was requested or
        // if it is needed because it is old
        if (rebuildLoggingXml) {
            buildLoggingXml(system, runtimeLoggingXml);
        }
        else {
            final File[] configFiles = system.getDataFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.equalsIgnoreCase("logging.xml")) {
                        return true;
                    }
                    return false;
                }
            }, true);
            if (needToRebuildRuntimeLogging(system, runtimeLoggingXml, configFiles)) {
                buildLoggingXml(system, runtimeLoggingXml);
            }
        }
        
        // runtime-logging.xml is all setup now, build the configuration from it
        try {
            LoggerContext loggerContext = Configurator.initialize(ClassUtils
                .getClassLoader(), new ConfigurationSource(new FileInputStream(
                    runtimeLoggingXml), runtimeLoggingXml));
            system.putAttribute(LoggerContext.class.getName(), loggerContext);
            return loggerContext.getConfiguration();
        }
        catch (FileNotFoundException e) {
            throw new SystemConfigurationException(
                "Problem while configuring log4j2 configuration", e);
        }
    }
    
    /**
     * Check if we need to rebuild runtime-logging.xml based on the modified
     * dates of the logging.xml files compared to the runtime-logging.xml.
     * @param system
     * @param runtimeLoggingXml
     * @return
     */
    static boolean needToRebuildRuntimeLogging(SystemContext system, File runtimeLoggingXml, File[] configFiles) {
        if (!runtimeLoggingXml.exists()) {
            return true;
        }
        
        final long runtimeModified = runtimeLoggingXml.lastModified();
        for (File f : configFiles) {
            if (Long.compare(f.lastModified(), runtimeModified) > 0) {
                return true;
            }
        }
        return false;
    }

    // Builds the runtime-logging.xml by merging all products data/logging.xml files
    private static void buildLoggingXml(SystemContext system,
                                        File runtimeLoggingXml)
                                                throws SystemConfigurationException {
        // Get the configuration file from the system, using the one of
        // highest priority
        File[] configFiles = system.getDataFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                if (name.equalsIgnoreCase("logging.xml")) {
                    return true;
                }
                return false;
            }
        }, true);
        
        if(configFiles == null || configFiles.length == 0) {
            throw new SystemConfigurationException("There was no logging.xml "
                    + "found in a data directory of the supplied prod-dirs.");
        }    
        
        // If the runtime-logging.xml already exists, we delete it at this point before
        // generating the new version.
        if (runtimeLoggingXml.exists()) {
            boolean deleted = runtimeLoggingXml.delete();
            if (!deleted) {
                throw new LoggingRuntimeException("Could not delete stale runtime-logging.xml.");
            }
        }
        
        // Sort XML files so that MOCA is the first one
        sort(configFiles, system);
        
        //Merge the xml files. We throw a MocaRuntimeException for any exception
        // so that we fail quickly since logging is messed up.
        try {
            StringBuilder sb = null;
            String[] xmls = new String[configFiles.length];
            int i = 0;
            String s;
            for (File f: configFiles) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
                    sb = new StringBuilder();
                    while ((s = br.readLine()) != null) {
                        sb.append(s);
                    }
                    xmls[i++] = sb.toString();
                }
            }
            Document doc = mergeXMLFiles(xmls);
            writeXMLFile(doc, runtimeLoggingXml);
        }
        catch (ParserConfigurationException | SAXException | IOException
                | AbstractXmlMergeException | TransformerException e1) {
            throw new LoggingRuntimeException("Configuring log4j2 failed!", e1);
        }
    }
    
    private static void writeXMLFile(Document doc, File configFile)
            throws TransformerException {
        // Write out the file.
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(configFile);
        TransformerFactory transformerFactory = TransformerFactory
            .newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);
    }
    
    /**
     * Merge XML files.
     * @param xmlFiles the xml files to be merged. The order is important because the
     * first xml file will be treated as the original.
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws AbstractXmlMergeException
     */
    static Document mergeXMLFiles(String[] xmlFiles)
            throws ParserConfigurationException, SAXException, IOException,
            AbstractXmlMergeException {
        // Configure the xml merge so that we use our definition
        // for merging the xml.
        XmlMerge xmlMerge = new DefaultXmlMerge();
        MergeAction mergeAction = new OrderedMergeAction();
        mergeAction.setMatcherFactory(new StaticOperationFactory(MocaSpecificLogOperationFactory.MocaSpecificTagMatcher.INSTANCE));
        mergeAction.setActionFactory(new MocaSpecificLogOperationFactory());
        xmlMerge.setRootMergeAction(mergeAction);
        
        // Get all the xml files and parse them out into documents.
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document[] src = new Document[xmlFiles.length];
        int i = 0;
        for (String s : xmlFiles) {
            src[i++] = dBuilder.parse(new ByteArrayInputStream(s.getBytes("UTF-8")));
        }

        // Merge the documents into the main document.
        return xmlMerge.merge(src);
    }
    
    /**
     * Swap file array so that MOCA is first in the array.
     * @param configFiles files to sort
     * @param ctx context
     */
    static void sort(File[] configFiles, SystemContext ctx) {
        final File mocaXml = new File(ctx.getVariable("MOCADIR") + File.separator + "data" + File.separator + LOGGING_XML);
        
        int foundPos = 0;
        for (int i = 0; i < configFiles.length; i++) {
            if (configFiles[i].equals(mocaXml)) {
                foundPos = i;
            }
        }
        
        // swap elements so that MOCA is first
        if (foundPos > 0) {
            File t = configFiles[0];
            configFiles[0] = configFiles[foundPos];
            configFiles[foundPos] = t;
        }
    }

    /**
     * Redirects the Java logging framework handlers to Log4j for all messages
     * at or above the info level.
     */
    private static void setupJavaRedirect() {
        java.util.logging.LogManager manager = java.util.logging.LogManager
            .getLogManager();
        Enumeration<String> names = manager.getLoggerNames();

        while (names.hasMoreElements()) {
            String loggerName = names.nextElement();
            java.util.logging.Logger logger = manager.getLogger(loggerName);
            
            if (logger != null) {
                for (Handler handler : logger.getHandlers()) {
                    logger.removeHandler(handler);
                }
                logger.addHandler(new AdapterHandler());
            }
        }
    }

    /**
     * Validates that a valid system configuration exists and adds any necessary
     * default appenders.
     * 
     * @param config The configuration object holding the appenders
     */
    private static void validateConfiguration(Logger logger,
                                              Configuration config) {

        Map<String, Appender> appenders = config.getAppenders();
        Appender appender = appenders.get(_routingAppenderName);
        if (!(appender instanceof MocaAppender)) {
            logger.warn("There is no MocaAppender defined with name: "
                    + _routingAppenderName + " in the configuration.  Without"
                    + " this appender client tracing will not operate properly"
                    + " leading to possibly leaked handles or inoperability.");
        }
    }

    private static void setupJMXLogging(Configuration configuration) {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

        try {
            mbeanServer.registerMBean(new MocaLogging(configuration),
                new ObjectName("com.redprairie.moca:type=logging"));
        }
        catch (InstanceAlreadyExistsException e) {
            e.printStackTrace();
            System.out.println("Unable to register moca logging mbean");
        }
        catch (MBeanRegistrationException e) {
            e.printStackTrace();
            System.out.println("Unable to register moca logging mbean");
        }
        catch (NotCompliantMBeanException e) {
            e.printStackTrace();
            System.out.println("Unable to register moca logging mbean");
        }
        catch (MalformedObjectNameException e) {
            e.printStackTrace();
            System.out.println("Unable to register moca logging mbean");
        }
    }
    
    // Internal method for testing to allow multiple calls to LoggingConfigurator.configure()
    static void resetConfiguration() {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName loggingMbean = new ObjectName("com.redprairie.moca:type=logging");
            if (mbeanServer.isRegistered(loggingMbean)) {
                mbeanServer.unregisterMBean(loggingMbean);
            }
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        _isConfigured.set(false);
        
    }
    
    public final static String _consoleAppenderName = "DefaultConsoleAppender";
    public final static String _traceFileAppenderName = "TraceFileAppender";
    public final static String _routingAppenderName = "RoutingAppender";   
    public final static String RUNTIME_LOGGING_XML = "runtime-logging.xml";
    public final static String LOGGING_XML = "logging.xml";
    // %LESDIR%/data/runtime-logging.xml
    public final static String RUNTIME_LOGGING_XML_PATH = System.getenv("LESDIR") + File.separator +
                                              "data" + File.separator + RUNTIME_LOGGING_XML;
    //public final static String RUNTIME_LOGGING_XML_PATH = System.getProperty("LESDIR") + File.separator +
    //        "data" + File.separator + RUNTIME_LOGGING_XML;
    private final static AtomicBoolean _isConfigured = new AtomicBoolean(false);
}
