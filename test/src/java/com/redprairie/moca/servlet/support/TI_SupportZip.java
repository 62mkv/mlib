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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.zip.ZipFile;

import org.junit.*;

import com.redprairie.moca.AsynchronousExecutor;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.async.MocaAsynchronousExecutor;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.ServerUtils.CurrentValues;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.TestServerUtils;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.registry.RegistryReader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Support Zip Integration Tests
 * 
 * Copyright (c) 2014 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author j1014843
 */
public class TI_SupportZip {
    
    @Before
    public void mocaSetUp() throws Exception {
	ServerUtils.setupDaemonContext(getClass().getName(), true, true);
	_moca = MocaUtils.currentContext();
	        
        CurrentValues values = TestServerUtils.takeCurrentValues();
        AsynchronousExecutor executor = new MocaAsynchronousExecutor(
           values.getServerContextFactory(), 1);
        TestServerUtils.restoreValues(values);
        // Since this is a process based task (technically), MOCA doesn't allow
        // us to have a Asynchronous executor.  So we're going to make one for now. 
        ServerUtils.globalContext().putAttribute(AsynchronousExecutor.class.getName(), 
            executor);
    }
    
    @Test
    public void testSupportZip() throws IOException {
        // Create a test file with a zip extension.
        File directory = new File(ServerUtils.getCurrentContext().getSystemVariable("LESDIR"), "temp");
        File mocaRegistry = new File(ServerUtils.getCurrentContext().getSystemVariable("MOCA_REGISTRY"));
        File supportZip = new File(directory, "testsupport.zip");
        
        // Use this file name to create and populate a support zip.
        try (OutputStream out = new FileOutputStream(supportZip);) {
            SupportZip zip = new SupportZip(out, true);
            zip.generateSupportZip();
        }
        try (ZipFile zipCheck = new ZipFile(supportZip);) {

                // SupportUtils.createSupportZip(supportZip.getAbsolutePath());
                assertTrue("The test zip does not exist.", supportZip.exists());

                // Verify that all the proper files are inside the zip.
                Assert.assertTrue("The test zip file is empty.", zipCheck
                    .entries().hasMoreElements());

                // Check that we have the core MOCA files.
                for (String file : new String[] { "database-connections.csv",
                        "jobs.csv", "jstack.txt", "library-versions.csv",
                        "log-files.csv", "moca-environment.txt",
                        "native-processes.csv", "probes.csv",
                        mocaRegistry.getName() , "resource-usage.txt",
                        "runtime-logging.xml", "sessions.csv",
                        "system-environment.txt", "system-properties.txt",
                        "system-resources.txt", "tasks.csv",
                        "thread-cpu-report.txt" }) {
                    assertNotNull(file + " did not exist.",
                        zipCheck.getEntry(file));
                }

                // Check the processtable file as it could be either
                // processtable.txt or processtable.csv based on OS.
                if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("window")) {
                    assertNotNull("processtable.csv did not exist.", 
                	    zipCheck.getEntry("processtable.csv"));
    	        }
    	        else {
    	            assertNotNull("processtable.txt did not exist.", zipCheck.getEntry("processtable.txt"));
    	            assertNotNull("ulimit.txt did not exist.", zipCheck.getEntry("ulimit.txt"));
    	        }
                
                // Check the custom hooks we have in MOCADIR\test
                assertNotNull("moca_test_dual_data.csv did not exist.",
                    zipCheck.getEntry("moca_test_dual_data.csv"));
                assertNotNull("moca_test_exception.csv did not exist.",
                    zipCheck.getEntry("moca_test_exception.csv"));
                assertNotNull(
                    "moca_test_exception_exception.txt did not exist.",
                    zipCheck.getEntry("moca_test_exception_exception.txt"));
        }
        finally {
            // Delete our test zip file.
            deleteTestFile(supportZip);
        }
    }
    
    @Test
    public void testInstallLogs() throws IOException {
        final File lesdir = new File(ServerUtils.getCurrentContext().getSystemVariable("LESDIR"));
        final File logdir = new File(lesdir, "log");
        final File installDir = new File(lesdir, "install");
        final File temp = new File(lesdir, "temp");
        final File supportZip = new File(temp, "testInstallLogs.zip");
        
        final File fake = new File(logdir, "fakelog.log");
        final File installedProds = new File(logdir, "installed-products.dat");
        final File userInstall = new File(logdir, "userinstall-20140320-183239.log");
        final File rpinstall = new File(logdir, "RedPrairieServerInstall-20140411-085121.log");
        final File installProd = new File(logdir, "install-waffle-20140320-145635.log");
        final File installLog = new File(installDir, "Install.log");
        
        try (OutputStream out = new FileOutputStream(supportZip)) {
            if (!fake.exists()) {
                fake.createNewFile();
            }
            if (!installedProds.exists()) {
                installedProds.createNewFile();
            }
            if (!userInstall.exists()) {
                userInstall.createNewFile();
            }
            if (!rpinstall.exists()) {
                rpinstall.createNewFile();
            }
            if (!installProd.exists()) {
                installProd.createNewFile();
            }
            if (!installDir.exists()) {
                installDir.mkdir();
            }
            if (!installLog.exists()) {
                installLog.createNewFile();
            }
            
            new SupportZip(out, true).generateSupportZip();
        }
        try (ZipFile zipCheck = new ZipFile(supportZip)) {
            assertTrue("The test zip does not exist.", supportZip.exists());
            assertTrue("The test zip file is empty.", zipCheck.entries().hasMoreElements());
            
            // install logs should be included, but other logs shouldn't.
            assertNotNull("installed-products.dat wasn't picked up.",
                zipCheck.getEntry("log" + File.separatorChar + installedProds.getName()));
            assertNotNull("userinstall log wasn't picked up.",
                zipCheck.getEntry("log" + File.separatorChar + userInstall.getName()));
            assertNotNull("RedPrairieServerInstall log wasn't picked up.",
                zipCheck.getEntry("log" + File.separatorChar + rpinstall.getName()));
            assertNotNull("Individual product install log wasn't picked up.",
                zipCheck.getEntry("log" + File.separatorChar + installProd.getName()));
            assertNull("Fake log was included in zip.",
                zipCheck.getEntry("log" + File.separatorChar + fake.getName()));
            assertNotNull("Install.log wasn't picked up.",
                zipCheck.getEntry("install" + File.separatorChar + installLog.getName()));
        }
        finally {
            // cleanup
            deleteTestFile(fake);
            deleteTestFile(installedProds);
            deleteTestFile(userInstall);
            deleteTestFile(rpinstall);
            deleteTestFile(installProd);
            deleteTestFile(installLog);
            deleteTestFile(installDir);
            deleteTestFile(supportZip);
        }
    }
    
    @Test
    public void testLesLogs() throws IOException {
        final File lesdir = new File(ServerUtils.getCurrentContext().getSystemVariable("LESDIR"));
        final File logdir = new File(lesdir, "log");
        final File temp = new File(lesdir, "temp");
        final File supportZip = new File(temp, "testLesLogs.zip");
        
        final long now = System.currentTimeMillis();
        final long MILLIS3DAYS = 259200000L + 1000L;
        final long BYTES10MB = 10485760L;
        final long BYTES50MB = 52428800L;
        
        final File serviceOutLog = new File(logdir, "moca.ems-webui-stdout.2013-08-05.log");
        final File serviceErrLog = new File(logdir, "moca.ems-webui-stderr.2013-05-24.log");
        final File mocaserverLog = new File(logdir, "mocaserver.log");
        final File dbcreateOld = new File(logdir, "dbcreateold.log");
        final File dbCreateNew = new File(logdir, "dbcreate.log");
        final File dbCreateLarge = new File(logdir, "dbcreate-moca-Tables-20130328-222249.log");
        
        try (OutputStream out = new FileOutputStream(supportZip)) {
            if (deleteTestFile(serviceOutLog) && !serviceOutLog.createNewFile()) {
                fail("Could not create new file " + serviceOutLog.getName());
            }
            if (deleteTestFile(serviceErrLog) && !serviceErrLog.createNewFile()) {
                fail("Could not create new file " + serviceErrLog.getName());
            }
            if (deleteTestFile(mocaserverLog) && !mocaserverLog.createNewFile()) {
                fail("Could not create new file " + mocaserverLog.getName());
            }
            if (deleteTestFile(dbcreateOld) && !dbcreateOld.createNewFile()) {
                fail("Could not create new file " + dbcreateOld.getName());
            }
            if (deleteTestFile(dbCreateNew) && !dbCreateNew.createNewFile()) {
                fail("Could not create new file " + dbCreateNew.getName());
            }
            if (deleteTestFile(dbCreateLarge) && !dbCreateLarge.createNewFile()) {
                fail("Could not create new file " + dbCreateLarge.getName());
            }
            
            writeJunk(serviceOutLog, 1000L);
            assertTrue(serviceOutLog.setLastModified(now - MILLIS3DAYS)); //too old
            
            writeJunk(serviceErrLog, 1000L);
            assertTrue(serviceErrLog.setLastModified(now)); // good
            
            writeJunk(mocaserverLog, BYTES50MB);
            assertTrue(mocaserverLog.setLastModified(now)); //too big
            
            writeJunk(dbcreateOld, BYTES10MB);
            assertTrue(dbcreateOld.setLastModified(now - MILLIS3DAYS)); //too old, too big
            
            writeJunk(dbCreateNew, 1000L);
            assertTrue(dbCreateNew.setLastModified(now)); //good
            
            writeJunk(dbCreateLarge, BYTES10MB);
            assertTrue(dbCreateLarge.setLastModified(now)); // too big
            
            new SupportZip(out, true).generateSupportZip();
        }
        try (ZipFile zipCheck = new ZipFile(supportZip)) {
            assertTrue("The test zip does not exist.", supportZip.exists());
            assertTrue("The test zip file is empty.", zipCheck.entries().hasMoreElements());
            
            // two good logs should be included
            assertNotNull("Apropriate log wasn't picked up: moca.ems-webui-stderr",
                zipCheck.getEntry("log" + File.separatorChar + serviceErrLog.getName()));
            assertNotNull("Apropriate log wasn't picked up: dbcreate",
                zipCheck.getEntry("log" + File.separatorChar + dbCreateNew.getName()));
            
            assertNull("Old log shouldn't be picked up.",
                zipCheck.getEntry("log" + File.separatorChar + serviceOutLog.getName()));
            assertNull("Large log shouldn't be picked up.",
                zipCheck.getEntry("log" + File.separatorChar + mocaserverLog.getName()));
            assertNull("Large and old log shouldn't be picked up.",
                zipCheck.getEntry("log" + File.separatorChar + dbcreateOld.getName()));
            assertNull("Large log shouldn't be picked up.",
                zipCheck.getEntry("log" + File.separatorChar + dbCreateLarge.getName()));
        }
        finally {
            // cleanup
            deleteTestFile(serviceOutLog);
            deleteTestFile(serviceErrLog);
            deleteTestFile(mocaserverLog);
            deleteTestFile(dbcreateOld);
            deleteTestFile(dbCreateNew);
            deleteTestFile(dbCreateLarge);
            deleteTestFile(supportZip);
        }
    }
    
    /**
     * A unit test to generate a Support Zip with Clustering
     * enabled using the JGroups XML Config file to verify
     * that is exists in the newly created Support Zip.
     * @throws IOException
     * @throws SystemConfigurationException 
     */
    @Test
    public void testSupportZipWithJGroups() throws IOException, SystemConfigurationException {
	// Read in the Cluster Registry that doesn't use a JGroups XML config.
	SystemContext ctx = readFromResource("test/clusterJGroupsXML.registry");
		
	// Keep a backup of the original system context and then set the new context.
	SystemContext toRestore = TestServerUtils.getGlobalContext();
	TestServerUtils.overrideGlobalContext(ctx);
	
	// Setup test files and directories.
	final File lesdir = new File(ServerUtils.getCurrentContext().getSystemVariable("LESDIR"));
	final File temp = new File(lesdir, "temp");
	final File supportZip = new File(temp, "testClusterJGroupsXML.zip");
	
	try (OutputStream out = new FileOutputStream(supportZip);) {
     	    // Create the Support Zip file.
            SupportZip zip = new SupportZip(out, true);
            zip.generateSupportZip();
        }
     	try (ZipFile zipCheck = new ZipFile(supportZip)) {
     	    // Perform tests...
     	    assertTrue("The test zip does not exist.", supportZip.exists());
     	    assertTrue("The test zip file is empty.", zipCheck.entries().hasMoreElements());
     	    
     	    // Check that the JGroups XML setting is set in the context.
     	    assertNotNull("The Jgroups XML setting was not used in the registry",
     		 ServerUtils.globalContext().getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_JGROUPS_XML));
     	    
     	    // Check that the JGroups XML Config file is present.
     	    assertNotNull("The JGroups XML Config was not included the Support Zip",
                 zipCheck.getEntry("test-jgroups-config.xml"));
     	}
     	finally {
     	    // Delete the test support zip file.
            deleteTestFile(supportZip);
         	
            // Finally restore the old system context.
            TestServerUtils.overrideGlobalContext(toRestore);
     	}
    }
    
    /**
     * A unit test to generate a Support Zip with Clustering
     * enabled but not using the JGroups XML Config file to
     * verify that there will not be a JGroups file included in
     * the newly created Support Zip.
     * @throws IOException
     * @throws SystemConfigurationException 
     */
    @Test
    public void testSupportZipWithoutJGroups() throws IOException, SystemConfigurationException {
	// Read in the Cluster Registry that doesn't use a JGroups XML config.
	SystemContext ctx = readFromResource("test/clusterNonJGroupsXML.registry");
	
	// Keep a backup of the original system context and then set the new context.
	SystemContext toRestore = TestServerUtils.getGlobalContext();
        TestServerUtils.overrideGlobalContext(ctx);
        
        // Setup test files and directories.
     	final File lesdir = new File(ServerUtils.getCurrentContext().getSystemVariable("LESDIR"));
     	final File temp = new File(lesdir, "temp");
     	final File supportZip = new File(temp, "testClusterNonJGroupsXML.zip");
     	
     	try (OutputStream out = new FileOutputStream(supportZip);) {
     	    // Create the Support Zip file.
            SupportZip zip = new SupportZip(out, true);
            zip.generateSupportZip();
        }
     	try (ZipFile zipCheck = new ZipFile(supportZip)) {
     	    // Perform tests...
     	    assertTrue("The test zip does not exist.", supportZip.exists());
     	    assertTrue("The test zip file is empty.", zipCheck.entries().hasMoreElements());
     	    
     	    // Check that the JGroups XML setting isn't set in the context.
     	    assertNull("The Jgroups XML setting was used in the registry",
     		 ServerUtils.globalContext().getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_JGROUPS_XML));
     	    
     	    // Check that the JGroups XML Config file is not present.
     	    assertNull("The JGroups XML Config was included in the Support Zip",
                 zipCheck.getEntry("test-jgroups-config.xml"));
     	}
     	finally {
     	    // Delete the test support zip file.
            deleteTestFile(supportZip);
         	
            // Finally restore the old system context.
            TestServerUtils.overrideGlobalContext(toRestore);
     	}
    }

    /**
     * Write random data to a file for testing.
     * @param mocaserverLog file
     * @param l bytes to write
     * @throws IOException 
     */
    private void writeJunk(File mocaserverLog, long l) throws IOException {
        try (BufferedWriter b = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mocaserverLog),"UTF-8"), (int)l)) {
            for (int i = 0; i < l; i++) {
                b.write('a');
            }
        }
    }
    
    public static boolean deleteTestFile(File file) {
        if (!file.exists()) return true;

        if (file.isDirectory()) {
            for (File childFile : file.listFiles()) {
                deleteTestFile(childFile);
            }
        }
        
        assertTrue("Access Restricted: " + file.getAbsolutePath(),
            file.canWrite());
        assertTrue("Could not delete test file: " + file.getAbsolutePath(),
            file.delete());
        return true;
    }
    
    private RegistryReader readFromResource(String resource) throws IOException, SystemConfigurationException {
        InputStream in = TI_SupportZip.class.getResourceAsStream(resource);
        try {
            RegistryReader reader = new RegistryReader(new InputStreamReader(in, "UTF-8"));
            return reader;
        }
        finally {
            if (in != null) in.close();
        }
    }
    
    /**
     * The current MOCA context for this test class.  This will hold a current
     * working context whenever a test case is run.
     */
    protected MocaContext _moca;

}
