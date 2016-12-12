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

package com.redprairie.moca.server.log;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * LoggingConfigurator tests
 * 
 * Copyright (c) 2014 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_LoggingConfigurator {
    
    @BeforeClass
    public static void beforeClass() throws IOException {
        // Get a temp file name to use
        File backupFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".bak");
        deleteIfExists(backupFile);
        _backupPath = backupFile.getAbsolutePath();
        // Backup the original runtime-logging.xml if it exists
        File originalLoggingXml = LoggingConfigurator.getRuntimeLoggingXml();
        renameIfExists(originalLoggingXml, _backupPath);
        LoggingConfigurator.resetConfiguration();
        
    }
    
    @AfterClass
    public static void afterClass() throws IOException, SystemConfigurationException {
        // Put back the original runtime-logging.xml if it existed
        File loggingXml = LoggingConfigurator.getRuntimeLoggingXml(); 
        renameIfExists(new File(_backupPath), 
            loggingXml.getAbsolutePath());
        LoggingConfigurator.configure();
    }
    
    @After
    public void after() throws IOException {
        // Delete the runtime-logging.xml and reset the LoggingConfigurator
        deleteIfExists(LoggingConfigurator.getRuntimeLoggingXml());
        LoggingConfigurator.resetConfiguration();
    }
    
    @Test
    public void testLoggingSetup() throws SystemConfigurationException {
        File runtimeLoggingXml = LoggingConfigurator.getRuntimeLoggingXml();
        assertFalse(runtimeLoggingXml.exists());
        LoggingConfigurator.configure();
        assertTrue(runtimeLoggingXml.exists());
    }
    
    @Test
    public void testLoggingSetupRebuildWhenNotExist() throws SystemConfigurationException {
        File runtimeLoggingXml = LoggingConfigurator.getRuntimeLoggingXml();
        assertFalse(runtimeLoggingXml.exists());
        // configure(false) directs a rebuild not to happen but the runtime-logging.xml
        // doesn't exist at this point so it should automatically create it.
        LoggingConfigurator.configure(false);
        assertTrue(runtimeLoggingXml.exists());
    }
    
    /**
     * Tests setting up the runtime logging XML once and then
     * reset the LoggingConfigurator to force it to retry setup 
     * (basically trying to mock another process here) and then
     * call configure(false), verify the second call doesn't actually modify
     * the runtime-logging.xml because it was directed not to rebuild
     * @throws SystemConfigurationException
     */
    @Test
    public void testLoggingSetupFollowedByNoRebuild() throws SystemConfigurationException {
        testLoggingSetup();
        File runtimeLoggingXml = LoggingConfigurator.getRuntimeLoggingXml();
        long lastModified = runtimeLoggingXml.lastModified();
        LoggingConfigurator.resetConfiguration();
        configureWithSleep(false);
        assertEquals(lastModified, runtimeLoggingXml.lastModified());
    }
    
    @Test
    public void testDoubleConfigureWithoutReset() throws SystemConfigurationException {
        testLoggingSetup();
        File runtimeLoggingXml = LoggingConfigurator.getRuntimeLoggingXml();
        long lastModified = runtimeLoggingXml.lastModified();
        // The logging configurator is already setup at this point so it
        // should skip modifying the file.
        configureWithSleep(true);
        assertEquals(lastModified, runtimeLoggingXml.lastModified());
    }
    
    @Test
    public void testRebuildConfigure() throws SystemConfigurationException {
        testLoggingSetup();
        File runtimeLoggingXml = LoggingConfigurator.getRuntimeLoggingXml();
        long lastModified = runtimeLoggingXml.lastModified();
        // Reset is here to act like we started the process again,
        // the runtime-logging.xml should get regenerated as part of configure()
        LoggingConfigurator.resetConfiguration();
        configureWithSleep(true);
        assertTrue(String.format("Current time: %d Before time: %d",
            runtimeLoggingXml.lastModified(), lastModified),
            runtimeLoggingXml.lastModified() > lastModified);
    }
    
    /**
     * Test how we check whether we need to rebuild the runtime-logging.xml
     * @throws Exception 
     */
    @Test
    public void testNeedToRebuild() throws Exception {
        testLoggingSetup();

        final long GRANULARITY = 1000;
        
        final File fakeFile1 = File.createTempFile("TU_LoggingConfigurator1", ".xml");
        fakeFile1.deleteOnExit();
        final File fakeFile2 = File.createTempFile("TU_LoggingConfigurator2", ".xml");
        fakeFile2.deleteOnExit();
        final File runtimeLogging = LoggingConfigurator.getRuntimeLoggingXml();
        final File[] configFiles = new File[] {fakeFile1, fakeFile2};
        
        // both files are older
        assertTrue(fakeFile1.setLastModified(runtimeLogging.lastModified() - GRANULARITY));
        assertTrue(fakeFile1.lastModified() < runtimeLogging.lastModified());
        assertTrue(fakeFile2.setLastModified(runtimeLogging.lastModified() - GRANULARITY));
        assertTrue(fakeFile2.lastModified() < runtimeLogging.lastModified());
        assertFalse(LoggingConfigurator.needToRebuildRuntimeLogging(ServerUtils.globalContext(), runtimeLogging, configFiles));
        
        // one file is older
        assertTrue(fakeFile1.setLastModified(runtimeLogging.lastModified() + GRANULARITY));
        assertTrue(fakeFile1.lastModified() > runtimeLogging.lastModified());
        assertTrue(fakeFile2.setLastModified(runtimeLogging.lastModified() - GRANULARITY));
        assertTrue(fakeFile2.lastModified() < runtimeLogging.lastModified());
        assertTrue(LoggingConfigurator.needToRebuildRuntimeLogging(ServerUtils.globalContext(), runtimeLogging, configFiles));
        
        // the other file is older
        assertTrue(fakeFile1.setLastModified(runtimeLogging.lastModified() - GRANULARITY));
        assertTrue(fakeFile1.lastModified() < runtimeLogging.lastModified());
        assertTrue(fakeFile2.setLastModified(runtimeLogging.lastModified() + GRANULARITY));
        assertTrue(fakeFile2.lastModified() > runtimeLogging.lastModified());
        assertTrue(LoggingConfigurator.needToRebuildRuntimeLogging(ServerUtils.globalContext(), runtimeLogging, configFiles));
        
        // both files are newer
        assertTrue(fakeFile1.setLastModified(runtimeLogging.lastModified() + GRANULARITY));
        assertTrue(fakeFile1.lastModified() > runtimeLogging.lastModified());
        assertTrue(fakeFile2.setLastModified(runtimeLogging.lastModified() + GRANULARITY));
        assertTrue(fakeFile2.lastModified() > runtimeLogging.lastModified());
        assertTrue(LoggingConfigurator.needToRebuildRuntimeLogging(ServerUtils.globalContext(), runtimeLogging, configFiles));
        
        // both files are newer but runtime logging doesn't exist
        // this test should pass but it could fail intermittently because log4j2 could be scanning for changes at the moment
//        assertTrue("Unable to delete " + runtimeLogging.getName(), runtimeLogging.delete());
//        assertTrue(fakeFile1.setLastModified(System.currentTimeMillis()));
//        assertTrue(fakeFile2.setLastModified(System.currentTimeMillis()));
//        assertTrue(LoggingConfigurator.needToRebuildRuntimeLogging(ServerUtils.globalContext(), runtimeLogging, configFiles));
    }
    
    /**
     * Test that when we sort XML files we always put MOCA at the start.
     * @throws IOException 
     */
    @Test
    public void testFileSort() throws IOException {
        final String MOCADIR = System.getenv("MOCADIR");
        final File mocaXml = new File(MOCADIR + "/data/logging.xml");
        
        if (MOCADIR == null) throw new IllegalArgumentException("MOCADIR must be set to run this test");
        final File temp = File.createTempFile("logging", ".xml");
        
        try {
            File[] files = {
                mocaXml,
            };
            
            LoggingConfigurator.sort(files, ServerUtils.globalContext());
            assertTrue(files[0].equals(mocaXml));
            
            File[] files2 = {
                temp,
                mocaXml,
            };
            
            LoggingConfigurator.sort(files2, ServerUtils.globalContext());
            assertTrue(files2[0].equals(mocaXml));
            
            File[] files3 = {
                mocaXml,
                temp
            };
            
            LoggingConfigurator.sort(files3, ServerUtils.globalContext());
            assertTrue(files3[0].equals(mocaXml));
            
            File[] files4 = {
                temp,
                new File("fake"),
                new File("some/other"),
                mocaXml.getAbsoluteFile()
            };
            
            LoggingConfigurator.sort(files4, ServerUtils.globalContext());
            assertTrue(files4[0].equals(mocaXml));
        }
        finally {
            temp.delete();
        }
    }
    
    /**
     * Essentially testing testing methods.
     * @throws Throwable 
     */
    @Test
    public void testXMLMergeBasic() throws Throwable {
        final String original = 
                "<root>" +
                    "<a/>" +
                    "<i name = \"a\"/>" + 
                    "<appenders>" +
                        "<d/>" +
                        "<j name=\"a\"/>" +
                    "</appenders>" +
                    "<c/>" +
                    "<e name = \"a\"/>" +
                    "<f name = \"a\"/>" + 
                    "<k name = \"a\"/>" + 
                "</root>";
        final String patch = 
                "<root>" +
                    "<h/>" + 
                    "<g name = \"a\"/>" + 
                    "<appenders>" +
                        "<d name=\"other\"/>" +
                        "<j/>" +
                    "</appenders>" +
                    "<c name=\"cname\"/>" +
                    "<e name= \"b\"/>" + 
                    "<f/>" + 
                    "<k name = \"a\"/>" + 
                "</root>";
        final String[] xmls = {original, patch};
        final Document xml = LoggingConfigurator.mergeXMLFiles(xmls);
        
        // root there
        verifyMergeHas(xml, "/root");
        
        // original doesnt have name, patch doesn't exist
        verifyMergeHas(xml, "/root/a[not(@*)]");
        
        // original has name, patch doesn't have name
        verifyMergeHas(xml, "/root/f[not(@name)]");
        verifyMergeHas(xml, "/root/f[@name = \"a\"]");
        
        // original has name, patch doesn't exist
        verifyMergeHas(xml, "/root/i[@name = \"a\"]");
        
        // patch doest have name, original doesn't exist
        verifyMergeHas(xml, "/root/h");
        
        // patch has name, original doesn't have name
        verifyMergeHas(xml, "/root/c");
        verifyMergeHas(xml, "/root/c[@name = \"cname\"]");
        verifyMergeHas(xml, "/root/c[not(@name)]");
        
        // patch has name, original doesn't exist
        verifyMergeHas(xml, "/root/g[@name = \"a\"]");
        
        // two different names
        verifyMergeHas(xml, "/root/e[@name = \"a\"]");
        verifyMergeHas(xml, "/root/e[@name = \"b\"]");
        
        // k with the same name, only possible to override loggers so we have 2 of them
        verifyMergeHas(xml, "/root/k[@name = \"a\"]");
        verifyCount(2, xml, "count(/root/k)");
        
        verifyCount(1, xml, "count(/root)");
        verifyCount(13, xml, "count(/root/*)");
        verifyCount(1, xml, "count(/root/appenders)");
        verifyCount(4, xml, "count(/root/appenders/*)");
        
        // verifyMergeHas should fail on a fake element
        Throwable failure = null;
        try {
            verifyMergeHas(xml, "/root/fake");
            failure = new AssertionError("Incorrect test should have failed.");
        }
        catch (AssertionError expected) {
            // expected
        }
        
        if (failure != null) {
            throw failure;
        }
        
        // this is OK though
        verifyMergeDoesntHave(xml, "/fake");
        
        // verifyMergeDoesntHave should fail on a real element
        failure = null;
        try {
            verifyMergeDoesntHave(xml, "/root/a");
            failure = new AssertionError("Incorrect test should have failed.");
        }
        catch (AssertionError expected) {
            
        }
        
        if (failure != null) {
            throw failure;
        }
        
        // this is OK however
        verifyMergeDoesntHave(xml, "/root/fake");
    }
    
    /**
     * Test that appenders are getting merged correctly, and routes are added
     * and they ren't inserted inside each other like they were before.
     * @throws Exception
     */
    @Test
    public void testXMLMergeAppenders() throws Exception {
        final String moca = 
                "<configuration packages=\"com.redprairie.moca.server.log\" monitorInterval=\"30\">" + 
                    "<appenders>" +
                        "<MocaRouting name=\"RoutingAppender\">" +
                            "<Routes pattern=\"$${ctx:moca-trace-file}\">" +
                                "<Route>" +
                                    "<File name=\"File-${ctx:moca-trace-file}\" fileName=\"${ctx:moca-trace-file}\">" +
                                        "<PatternLayout>" +
                                            "<pattern>%d{ISO8601} %-5p [%-3T %-4.4X{moca-session}] %c{1} [%X{moca-stack-level}] %m []%n</pattern>" +
                                        "</PatternLayout>" +
                                    "</File>" +
                                "</Route>" +
                                "<Route ref=\"Console-TraceOnly\" key=\"${ctx:moca-trace-file}\"/> "+
                            "</Routes>" +
                        "</MocaRouting>" +
                        "<Rewrite name=\"Console-TraceOnly\">" +
                            "<appender-ref ref=\"Console\">" +
                                "<LogLevelFilter onMatch=\"DENY\" onMismatch=\"ACCEPT\"/>" +
                            "</appender-ref>" +
                        "</Rewrite>" +
                        "<Rewrite name=\"RewriteAppender\">" +
                            "<appender-ref ref=\"RoutingAppender\"/>" +
                        "</Rewrite>"+
                    "</appenders>" +
                    "<loggers>" +
                    "</loggers>" +
                "</configuration>";
        final String les = 
                "<configuration packages=\"com.redprairie.moca.server.log\" monitorInterval=\"30\">" + 
                    "<appenders>" +
                        "<MocaRouting name=\"RoutingAppender\">" +
                            "<Routes pattern=\"$${ctx:moca-trace-file}\">" +
                                "<Route key=\"${env:LESDIR}\\log\\yyyy.log\">" +
                                    "<RollingFile name=\"yyyy\" fileName=\"${ctx:moca-trace-file}\" filePattern=\"C:/dev/2013.2-dev/env/log/yyyy-%d{MM-dd-yyyy}-%i.log\">" +
                                        "<PatternLayout>" +
                                            "<pattern>%d{ISO8601} %-5p [%-3T %-4.4X{moca-session}] %c{1} [%X{moca-stack-level}] %m []%n</pattern>" +
                                        "</PatternLayout>" +
                                        "<Policies>" +
                                            "<SizeBasedTriggeringPolicy size=\"500\"/>" +
                                        "</Policies>" +
                                    "</RollingFile>" +
                                "</Route>" +
                            "</Routes>" +
                        "</MocaRouting>" +
                    "</appenders>" +
                    "<loggers>" +
                    "</loggers>" +
                "</configuration>";
        final String[] xmls = {moca, les};
        final Document xml = LoggingConfigurator.mergeXMLFiles(xmls);
        
        verifyMergeHas(xml, "/configuration");
        verifyMergeHas(xml, "/configuration/appenders");
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting");
        
        // has default route
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[not(@key)]");
        
        // but also has the other route as well
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[@key and string-length(@key)!=0]");
        
        // has route from patch
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[@key = \"${env:LESDIR}\\log\\yyyy.log\"]");
        
        //default route has only 1 appender inside (nothing was inserted)
        verifyCount(1, xml, "count(/configuration/appenders/MocaRouting/Routes/Route[@key = \"${env:LESDIR}\\log\\yyyy.log\"]/*)");
        
        // verify counts
        // this is important because we must ensure that the we are doing a "merge" instead of
        // appending elements e.g., <a><c/></a> + <a><b/></a> 
        // should equal <a><c/><b/></a>, NOT <a><c/></a><a><b/></a> 
        // which would satisfy the tests above, but log4j wouldn't be happy
        
        // only one appenders element
        verifyCount(1, xml, "count(/configuration/appenders)");
        
        // verify routes merged
        verifyCount(1, xml, "count(/configuration/appenders/MocaRouting/Routes)");
        verifyCount(1, xml, "count(/configuration/appenders/MocaRouting/Routes[@pattern = \"$${ctx:moca-trace-file}\"])");

        verifyMergeHas(xml, "/configuration/loggers");
    }
    
    /**
     * Test that appenders and loggers are merged correctly, allowing another product
     * to add their logging.xml and have it work as intended. This is also testing
     * that they can override loggers and that comments in the XML don't cause issues.
     * @throws Exception 
     */
    @Test
    public void testXMLMergeLoggersAndAppenders() throws Exception {
        final String moca = 
                "<configuration packages=\"com.redprairie.moca.server.log\" monitorInterval=\"30\">" +
                    "<appenders>" +
                        "<MocaRouting name=\"RoutingAppender\">" +
                            "<Routes pattern=\"$${ctx:moca-trace-file}\">" +
                                "<Route>" +
                                    "<File name=\"File-${ctx:moca-trace-file}\" fileName=\"${ctx:moca-trace-file}\">" +
                                        "<PatternLayout>" +
                                            "<pattern>%d{ISO8601} %-5p [%-3T %-4.4X{moca-session}] %c{1} [%X{moca-stack-level}] %m []%n</pattern>" +
                                        "</PatternLayout>" +
                                    "</File>" +
                                "</Route>" +
                                "<Route ref=\"Console-TraceOnly\" key=\"${ctx:moca-trace-file}\"/> "+
                            "</Routes>" +
                        "</MocaRouting>" +
                        "<Rewrite name=\"Console-TraceOnly\">" +
                            "<appender-ref ref=\"Console\">" +
                                "<LogLevelFilter onMatch=\"DENY\" onMismatch=\"ACCEPT\"/>" +
                            "</appender-ref>" +
                        "</Rewrite>" +
                        "<Rewrite name=\"RewriteAppender\">" +
                            "<appender-ref ref=\"RoutingAppender\"/>" +
                        "</Rewrite>"+
                    "</appenders>" +
                    "<loggers>" +
                        "<root level=\"WARN\">" + 
                            "<appender-ref ref=\"RewriteAppender\"/>" + 
                        "</root>" + 
                        "<logger name=\"com.redprairie\" level=\"INFO\"/>" + 
                        "<logger name=\"org.eclipse\" level=\"INFO\"/>" +
                        "<!-- TESTING COMMENT" +
                        "     This is the logger you would enable to get the MOCA\r\n" + 
                        "     activity log into a trace file. You can specify the specific appender.\r\n" + 
                        "     The Moca activity will always be logged as debug in the server trace.\r\n" + 
                        "<logger name=\"com.redprairie.moca.Activity\" level=\"INFO\">\r\n" + 
                            "<appender-ref ref=\"ActivityAppender\"/>\r\n" + 
                        "</logger>-->" +
                    "</loggers>" +
                "</configuration>";
        final String wfm = 
                "<?xml version=\"1.0\" encoding= \"UTF-8\" ?>" + 
                "<configuration packages=\"com.redprairie.moca.server.log\" monitorInterval=\"30\">" + 
                "  <appenders>" + 
                "      <appender name=\"WFMDefaultConsoleAppender\" class=\"org.apache.log4j.varia.NullAppender\"/>" + 
                "      <RollingFile name=\"WFMSCERollingFileAppender\" fileName=\"${env:LESDIR}/log/WFMSCELog.log\"" + 
                "                   filePattern=\"${env:LESDIR}/log/WFMSCELog-%i.log\">" + 
                "          <PatternLayout pattern=\"%-3T %-4.4X{moca-session} %p{TRACE=T,DEBUG=D,INFO=I,WARN=W,ERROR=E,FATAL=F} %d{ABSOLUTE} (%c{1}) %X{moca-stack-level}%m%n\"/>" + 
                "          <Policies>\r\n" + 
                "              <SizeBasedTriggeringPolicy size=\"300MB\"/>" + 
                "          </Policies>" + 
                "          <DefaultRolloverStrategy max=\"10\"/>" + 
                "      </RollingFile>" + 
                "  </appenders>" + 
                "  <loggers>" + 
                "      <logger name=\"com.redprairie\" level=\"DEBUG\"/>" + 
                "      <logger name=\"com.redprairie.lm\" level=\"debug\">" + 
                "          <appender-ref ref=\"WFMSCERollingFileAppender\"/>" + 
                "      </logger>" + 
                "  </loggers>" + 
                "</configuration>";
        
        final String[] xmls = {moca, wfm};
        final Document xml = LoggingConfigurator.mergeXMLFiles(xmls);
        
        // MOCA appenders still there
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting");
        verifyMergeHas(xml, "/configuration/appenders/Rewrite[@name = \"Console-TraceOnly\"]");
        verifyMergeHas(xml, "/configuration/appenders/Rewrite[@name = \"RewriteAppender\"]");
        
        // WFM appenders still there
        verifyMergeHas(xml, "/configuration/appenders/appender[@name = \"WFMDefaultConsoleAppender\"]");
        verifyMergeHas(xml, "/configuration/appenders/RollingFile[@name = \"WFMSCERollingFileAppender\"]");
        
        // WFM appenders were all added and no extras
        verifyCount(3 + 2, xml, "count(/configuration/appenders/*)");
        
        // WFM loggers was added
        verifyMergeHas(xml, "/configuration/loggers/logger[@name = \"com.redprairie.lm\"]");
        verifyMergeHas(xml, "/configuration/loggers/logger[@name = \"com.redprairie.lm\"]/appender-ref[@ref = \"WFMSCERollingFileAppender\"]");
        
        // WFM can override the MOCA logger
        verifyMergeHas(xml, "/configuration/loggers/logger[@name = \"com.redprairie\" and @level = \"DEBUG\"]");
        verifyCount(1, xml, "count(/configuration/loggers/logger[@name = \"com.redprairie\"])");
        
        // MOCA loggers are still there
        verifyMergeHas(xml, "/configuration/loggers/logger[@name = \"org.eclipse\"]");
        verifyMergeHas(xml, "/configuration/loggers/root");
        
        // WFM loggers added and no extra
        verifyCount(4, xml, "count(/configuration/loggers/*)");

        // verify counts
        // this is important because we must ensure that the we are doing a "merge" instead of
        // appending elements e.g., <a><c/></a> + <a><b/></a> 
        // should equal <a><c/><b/></a>, NOT <a><c/></a><a><b/></a> 
        // which would satisfy the tests above, but log4j wouldn't be happy

        // only one appenders element
        verifyCount(1, xml, "count(/configuration/appenders)");
        
        // only one appenders element
        verifyCount(1, xml, "count(/configuration/loggers)");
    }
    
    /**
     * Test we can add and override routes and that it can even have a nested routing appender underneath.
     */
    @Test
    public void testMergeRoutes() throws Exception {
        final String moca = 
                "<configuration packages=\"com.redprairie.moca.server.log\" monitorInterval=\"30\">" +
                    "<appenders>" +
                        "<MocaRouting name=\"RoutingAppender\">" +
                            "<Routes pattern=\"$${ctx:moca-trace-file}\">" +
                                "<Route>" +
                                    "<File name=\"File-${ctx:moca-trace-file}\" fileName=\"${ctx:moca-trace-file}\">" +
                                        "<PatternLayout>" +
                                            "<pattern>%d{ISO8601} %-5p [%-3T %-4.4X{moca-session}] %c{1} [%X{moca-stack-level}] %m []%n</pattern>" +
                                        "</PatternLayout>" +
                                    "</File>" +
                                "</Route>" +
                                "<Route ref=\"Console-TraceOnly\" key=\"${ctx:moca-trace-file}\"/> "+
                            "</Routes>" +
                        "</MocaRouting>" +
                        "<Rewrite name=\"Console-TraceOnly\">" +
                            "<appender-ref ref=\"Console\">" +
                                "<LogLevelFilter onMatch=\"DENY\" onMismatch=\"ACCEPT\"/>" +
                            "</appender-ref>" +
                        "</Rewrite>" +
                        "<Rewrite name=\"RewriteAppender\">" +
                            "<appender-ref ref=\"RoutingAppender\"/>" +
                        "</Rewrite>"+
                    "</appenders>" +
                    "<loggers>" +
                        "<root level=\"WARN\">" + 
                            "<appender-ref ref=\"RewriteAppender\"/>" + 
                        "</root>" + 
                        "<asyncLogger name=\"com.redprairie\" level=\"INFO\"/>" + 
                        "<logger name=\"org.eclipse\" level=\"INFO\"/>" +
                        "<!-- TESTING COMMENT" +
                        "     This is the logger you would enable to get the MOCA\r\n" + 
                        "     activity log into a trace file. You can specify the specific appender.\r\n" + 
                        "     The Moca activity will always be logged as debug in the server trace.\r\n" + 
                        "<logger name=\"com.redprairie.moca.Activity\" level=\"INFO\">\r\n" + 
                            "<appender-ref ref=\"ActivityAppender\"/>\r\n" + 
                        "</logger>-->" +
                    "</loggers>" +
                "</configuration>";
        final String les = 
                "<configuration packages=\"com.redprairie.moca.server.log\" monitorInterval=\"30\">" + 
                    "<appenders>" +
                        "<MocaRouting name=\"RoutingAppender\">" +
                            "<Routes pattern=\"$${ctx:moca-trace-file}\"> \r\n" + 
                            "    <Route>\r\n" + 
                            "        <!-- Routes based on whether we need synchronous session tracing -->\r\n" + 
                            "        <Routing name=\"SyncRouter\">\r\n" + 
                            "            <Routes pattern=\"$${ctx:moca_sync_mark}\">\r\n" + 
                            "                <!-- If we have the sync mark set we need to immediately flush -->\r\n" + 
                            "                <Route key=\"true\">\r\n" + 
                            "                    <File name=\"File-${ctx:moca-trace-file}\" fileName=\"${ctx:moca-trace-file}\" immediateFlush=\"true\">             \r\n" + 
                            "                        <PatternLayout> \r\n" + 
                            "                            <pattern>%d{DEFAULT} %-5p [%-3X{moca-thread-id} %-4.4X{moca-session}] %c{1} [%X{moca-stack-level}] %m []%n</pattern> \r\n" + 
                            "                        </PatternLayout> \r\n" + 
                            "                    </File >\r\n" + 
                            "                </Route>\r\n" + 
                            "                <!-- Default to normal appender -->\r\n" + 
                            "                <Route>\r\n" + 
                            "                    <File name=\"File-${ctx:moca-trace-file}\" fileName=\"${ctx:moca-trace-file}\" immediateFlush=\"false\">            \r\n" + 
                            "                        <PatternLayout> \r\n" + 
                            "                            <pattern>%d{DEFAULT} %-5p [%-3X{moca-thread-id} %-4.4X{moca-session}] %c{1} [%X{moca-stack-level}] %m []%n</pattern> \r\n" + 
                            "                        </PatternLayout> \r\n" + 
                            "                    </File >\r\n" + 
                            "                </Route>\r\n" + 
                            "            </Routes> \r\n" + 
                            "        </Routing>\r\n" + 
                            "    </Route> \r\n" + 
                            "    <Route key=\"Add-Another\"/> " +
                            "</Routes> \r\n" + 
                        "</MocaRouting>" +
                        "<RollingFile name=\"WFMSCERollingFileAppender\" fileName=\"${env:LESDIR}/log/WFMSCELog.log\"" + 
                        "             filePattern=\"${env:LESDIR}/log/WFMSCELog-%i.log\">" + 
                            "<PatternLayout pattern=\"%-3T %-4.4X{moca-session} %p{TRACE=T,DEBUG=D,INFO=I,WARN=W,ERROR=E,FATAL=F} %d{ABSOLUTE} (%c{1}) %X{moca-stack-level}%m%n\"/>" + 
                            "<Policies>\r\n" + 
                                "<SizeBasedTriggeringPolicy size=\"300MB\"/>" + 
                                "</Policies>" + 
                                "<DefaultRolloverStrategy max=\"10\"/>" + 
                        "</RollingFile>" + 
                    "</appenders>" +
                    "<loggers>" +
                        "<asyncLogger name=\"com.redprairie\" level=\"DEBUG\"/>" +
                        "<asyncLogger name=\"com.redprairie.lm\" level=\"debug\">" + 
                            "<appender-ref ref=\"WFMSCERollingFileAppender\"/>" + 
                        "</asyncLogger>" + 
                    "</loggers>" +
                "</configuration>";
        final String[] xmls = {moca, les};
        final Document xml = LoggingConfigurator.mergeXMLFiles(xmls);
        
        verifyMergeHas(xml, "/configuration");
        verifyMergeHas(xml, "/configuration/appenders");
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting");

        // verify has routes element
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes[@pattern = \"$${ctx:moca-trace-file}\"]");
        verifyCount(1, xml, "count(/configuration/appenders/MocaRouting/Routes[@pattern = \"$${ctx:moca-trace-file}\"])");

        // has default route which is overridden
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[not(@key)]");
        verifyCount(1, xml, "count(/configuration/appenders/MocaRouting/Routes/Route[not(@key)])");
        
        // but also has the other route as well
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[@key = \"${ctx:moca-trace-file}\"]");
        
        // and also has the new one we are adding
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[@key = \"Add-Another\"]");
        verifyCount(3, xml, "count(/configuration/appenders/MocaRouting/Routes/Route)");
        
        // the default route should be overridden so it has a lot of elements below it
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[not(@key)]/Routing[@name = \"SyncRouter\"]");
        verifyCount(1, xml, "count(/configuration/appenders/MocaRouting/Routes/Route[not(@key)]/Routing[@name = \"SyncRouter\"])");
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[not(@key)]/Routing[@name = \"SyncRouter\"]/Routes[@pattern = \"$${ctx:moca_sync_mark}\"]");
        verifyCount(1, xml, "count(/configuration/appenders/MocaRouting/Routes/Route[not(@key)]/Routing[@name = \"SyncRouter\"]/Routes)");
        verifyCount(2, xml, "count(/configuration/appenders/MocaRouting/Routes/Route[not(@key)]/Routing[@name = \"SyncRouter\"]/Routes/Route)");
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[not(@key)]/Routing[@name = \"SyncRouter\"]/Routes/Route[not(@key)]");
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[not(@key)]/Routing[@name = \"SyncRouter\"]/Routes/Route[@key = \"true\"]");
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[not(@key)]/Routing[@name = \"SyncRouter\"]/Routes/Route[not(@key)]/File");
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[not(@key)]/Routing[@name = \"SyncRouter\"]/Routes/Route[@key = \"true\"]/File");
    }

    /**
     * After the improvements to the merge, test that we can now override elements even if they are not first in the list.
     * This test tries to override the "Console-TraceOnly" route, which wouldn't work before.
     */
    @Test
    public void testMergeOutOfOrder() throws Exception {
        final String moca =
                "<configuration packages=\"com.redprairie.moca.server.log\" monitorInterval=\"30\">" +
                    "<appenders>" +
                        "<MocaRouting name=\"RoutingAppender\">" +
                            "<Routes pattern=\"$${ctx:moca-trace-file}\">" +
                                "<Route>" +
                                    "<File name=\"File-${ctx:moca-trace-file}\" fileName=\"${ctx:moca-trace-file}\">" +
                                        "<PatternLayout>" +
                                             "<pattern>%d{ISO8601} %-5p [%-3T %-4.4X{moca-session}] %c{1} [%X{moca-stack-level}] %m []%n</pattern>" +
                                        "</PatternLayout>" +
                                    "</File>" +
                                "</Route>" +
                                "<Route ref=\"Console-TraceOnly\" key=\"${ctx:moca-trace-file}\"/> "+
                            "</Routes>" +
                        "</MocaRouting>" +
                        "<Rewrite name=\"Console-TraceOnly\">" +
                            "<appender-ref ref=\"Console\">" +
                                "<LogLevelFilter onMatch=\"DENY\" onMismatch=\"ACCEPT\"/>" +
                            "</appender-ref>" +
                        "</Rewrite>" +
                        "<Rewrite name=\"RewriteAppender\">" +
                            "<appender-ref ref=\"RoutingAppender\"/>" +
                        "</Rewrite>"+
                    "</appenders>" +
                    "<loggers>" +
                        "<root level=\"WARN\">" +
                            "<appender-ref ref=\"RewriteAppender\"/>" +
                        "</root>" +
                        "<asyncLogger name=\"com.redprairie\" level=\"INFO\"/>" +
                        "<logger name=\"org.eclipse\" level=\"INFO\"/>" +
                        "<!-- TESTING COMMENT" +
                        "     This is the logger you would enable to get the MOCA\r\n" +
                        "     activity log into a trace file. You can specify the specific appender.\r\n" +
                        "     The Moca activity will always be logged as debug in the server trace.\r\n" +
                        "<logger name=\"com.redprairie.moca.Activity\" level=\"INFO\">\r\n" +
                        "<appender-ref ref=\"ActivityAppender\"/>\r\n" +
                        "</logger>-->" +
                    "</loggers>" +
                "</configuration>";
        final String les =
               "<configuration packages=\"com.redprairie.moca.server.log\" monitorInterval=\"30\"> \n" +
               "    <appenders> \n" +
               "        <MocaRouting name=\"RoutingAppender\"> \n" +
               "            <Routes pattern=\"$${ctx:moca-trace-file}\">  \n" +
               "                <Route key=\"${ctx:moca-trace-file}\">\n" +
               "                    <RollingFile name=\"MYROLLINGFILE\" fileName=\"${env:LESDIR}/log/hhhh.log\" filePattern=\"${env:LESDIR}/log/hhhh-%d{MM-dd-yyyy}-%i.log.gz\">\n" +
               "                        <PatternLayout>\n" +
               "                            <pattern>%d{ISO8601} %-5p [%-3T %-4.4X{moca-session}] %c{1} [%X{moca-stack-level}] %m []%n</pattern>\n" +
               "                        </PatternLayout>\n" +
               "                        <Policies>\n" +
               "                            <SizeBasedTriggeringPolicy size=\"1KB\" />\n" +
               "                        </Policies>\n" +
               "                        <DefaultRolloverStrategy max=\"20\" />\n" +
               "                    </RollingFile>\n" +
               "                </Route>\n" +
               "            </Routes>  \n" +
               "        </MocaRouting> \n" +
               "    </appenders> \n" +
               "</configuration>";
        final String[] xmlStreams = {moca, les};
        final Document xml = LoggingConfigurator.mergeXMLFiles(xmlStreams);

        verifyMergeHas(xml, "/configuration");
        verifyMergeHas(xml, "/configuration/appenders");
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting");

        // verify has routes element
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes[@pattern = \"$${ctx:moca-trace-file}\"]");
        verifyCount(1, xml, "count(/configuration/appenders/MocaRouting/Routes[@pattern = \"$${ctx:moca-trace-file}\"])");

        // has default route which is overridden
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[not(@key)]");
        verifyCount(1, xml, "count(/configuration/appenders/MocaRouting/Routes/Route[not(@key)])");

        // there should be 2 total routes
        verifyCount(2, xml, "count(/configuration/appenders/MocaRouting/Routes/Route)");

        // CHECK THAT IT HAS THE NEW OVERRIDDEN ONE
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[@key = \"${ctx:moca-trace-file}\"]");
        verifyMergeHas(xml, "/configuration/appenders/MocaRouting/Routes/Route[@key = \"${ctx:moca-trace-file}\"]/RollingFile[@name = \"MYROLLINGFILE\"]");
    }


    /**
     * Verify merge works by checking result against an xpath expression.
     * @param xml xml to check
     * @param x xpathExression
     * @throws Exception
     * @throws AssertionError
     */
    private void verifyMergeHas(Document xml, String x) throws Exception, AssertionError {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        XPathExpression expr = xpath.compile(x);
        
        if (!(Boolean) expr.evaluate(xml, XPathConstants.BOOLEAN)) {
            throw new AssertionError("Merged XML failed XPath: " + x + "\nXML:" + docToString(xml));
        }
    }
    
    /**
     * Verify merge does not have an element specified by an xpath expression
     * @param xml xml to check
     * @param x xpathExression
     * @throws AssertionError 
     * @throws Exception 
     */
    private void verifyMergeDoesntHave(Document xml, String x) throws AssertionError, Exception {
        AssertionError failure = null;
        try {
            verifyMergeHas(xml, x);
            failure = new AssertionError("XML merge should have failed.");
        }
        catch (AssertionError expected) {
            // expected
        }
        
        if (failure != null) throw failure;
    }
    
    /**
     * Count the number of elements according to XPath expression
     * @param expected expected number of elements
     * @param xml xml to check
     * @param x xpathExression
     * @throws Exception 
     */
    private void verifyCount(int expected, Document xml, String x) throws Exception {
        if (!x.contains("count")) throw new AssertionError("XPath expression must contain count function.");
        
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        XPathExpression expr = xpath.compile(x);
        
        assertEquals("Merged XML failed count XPath: " + x + "\\nXML:" + docToString(xml), 
            expected, ((Double) expr.evaluate(xml, XPathConstants.NUMBER)).intValue());
    }
    
    /**
     * @param d
     * @throws TransformerFactoryConfigurationError
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    private String docToString(Document d) throws TransformerFactoryConfigurationError,
            TransformerConfigurationException, TransformerException {
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.transform(new DOMSource(d), new StreamResult(sw));
        return sw.toString();
    }
    
    // File timestamp granularity may only be up to a second so
    // we have to sleep for a second before configuring again if we want
    // to do any file timestamp comparisons
    private static void configureWithSleep(boolean rebuildLoggingXml) throws SystemConfigurationException {
        sleep(1000);
        LoggingConfigurator.configure(rebuildLoggingXml);
    }
    
    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void renameIfExists(File file, String newPath) throws IOException {
        if (file.exists()) {
           try {
               Files.move(file.toPath(), new File(newPath).toPath());
           }
           catch (Exception e) {
               throw new IOException(String.format("Failed to rename file %s to new path %s",
                       file, newPath), e);
           }
        }
    }
    
    private static void deleteIfExists(File file) throws IOException {
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("Unable to delete file: " + file);
            }
        }
    }
    
    private static String _backupPath;
}
