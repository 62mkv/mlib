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

package com.sam.moca.advice;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.management.ThreadInfo;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.MalformedObjectNameException;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.jmx.MBeanServerNotFoundException;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadMetrics;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaInterruptedException;
import com.sam.moca.MocaResults;
import com.sam.moca.RowIterator;
import com.sam.moca.TooManyRowsException;
import com.sam.moca.client.ProtocolException;
import com.sam.moca.client.XMLResultsDecoder;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.ServerUtils.CurrentValues;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.server.TestServerUtils;
import com.sam.moca.server.db.BindList;
import com.sam.moca.server.db.BindMode;
import com.sam.moca.server.db.DBAdapter;
import com.sam.moca.server.db.MocaTransaction;
import com.sam.moca.server.dispatch.MessageResolver;
import com.sam.moca.server.exec.ArgumentSource;
import com.sam.moca.server.exec.DefaultServerContextFactory;
import com.sam.moca.server.exec.LocalSessionContext;
import com.sam.moca.server.exec.MocaScriptException;
import com.sam.moca.server.exec.RemoteConnectionFactory;
import com.sam.moca.server.exec.RequestContext;
import com.sam.moca.server.exec.ScriptAdapter;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.exec.SessionContext;
import com.sam.moca.server.exec.SessionType;
import com.sam.moca.server.legacy.NativeAdapterFactory;
import com.sam.moca.server.legacy.NativeProcessPoolBuilder;
import com.sam.moca.server.profile.CommandPath;
import com.sam.moca.server.profile.CommandUsage;
import com.sam.moca.server.repository.CommandRepository;
import com.sam.moca.server.session.SessionToken;
import com.sam.moca.util.ConcatString;
import com.sam.moca.util.MocaUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class tests to ensure that the server context jmx proxies are working
 * correctly by trying various methods on the interface
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
@RunWith(Parameterized.class)
public class TU_ServerContextConfig {
 
    // Control the number of iterations during test run
    private static int ITERATIONS = 1;
    private static boolean ENABLE_THREAD_DUMP = false;
    
    @Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[ITERATIONS][0]);
    }  
    
    private static final int THREAD_JOIN_TIMEOUT = 100000;
    private static String _testSessionName = TU_ServerContextConfig.class.getName() + ":/\\ ";
    
    private static CountDownLatch _latch;
    private static ServerContext previousContext = null;
    
    private static SessionAdministrationManagerBean _manager;

    public static void registerRunning() {
        _latch.countDown();
    }
    
    @BeforeClass
    public static void beforeTests() throws SystemConfigurationException, InterruptedException {
        if(ENABLE_THREAD_DUMP){
            System.err.println("@BeforeClass thread dump:");
            TestServerUtils.dumpThreads(System.err);
        }

        try {
            previousContext = ServerUtils.getCurrentContext();
        }
        catch (IllegalStateException ignore) {
            // We don't care if it wasn't initialized yet.  We only do this
            // so we can restore the context afterwards
            // This only happens if it is the first test to initialize the 
            // context
        }
        // First we null out the current context
        ServerUtils.setCurrentContext(null);
        // Then we create a new context that should be unique so it doesn't cause collisions
        
        ServerUtils.setupDaemonContext(_testSessionName, true);
        
        _manager = ServerUtils.globalAttribute(SessionAdministrationManagerBean.class);
    }
    
    @AfterClass public static void afterTests() {
        ServerUtils.getCurrentContext().close();
        // Now restore the context back to what it was
        ServerUtils.setCurrentContext(previousContext);
        if(ENABLE_THREAD_DUMP){
            System.err.println("@AfterClass thread dump:");
            TestServerUtils.dumpThreads(System.err);
        }
    }
    
    @After
    public void afterTest() throws MocaException, MalformedObjectNameException, 
    UnsupportedEncodingException {
        // Some of our tests play with the manager so we have to make sure
        // he gets reset to the one we want between tests
        ServerUtils.globalContext().putAttribute(
            SessionAdministrationManagerBean.class.getName(), _manager);
        
        ServerUtils.getCurrentContext().rollback();
        
        TestServerUtils.verifyActiveContextPointers();
        
        Thread currentThread = Thread.currentThread();
        ServerContextAdministrationBean bean = getName(currentThread);
        ThreadInfo[] threadInfos = bean.getSessionThreads();
        boolean present = false;
        for (ThreadInfo threadInfo : threadInfos) {
            if (currentThread.getId() == threadInfo.getThreadId()) {
                present = true;
                break;
            }
        }
        assertTrue("Our thread wasn't known by the bean had:" + 
                Arrays.toString(threadInfos), present);
    }
    
    private static void resetStaticStuff() {
        _latch = new CountDownLatch(1);
    }
    
    @SuppressFBWarnings(value="DM_GC")
    @Before public void beforeEachTest() {
        // We need to force gc and finalizers to cleanup sessions from the previous test run.
        // Since tests reuse the same session name, the sessions from a previous run can be closed by finalizers
        // during the next run, causing the new session to disappear and resulting in a NullPointerException.
        System.gc();
        System.runFinalization();
        
        // This is here to make sure that a context doesn't leak between tests and possibly mess up
        // that context's registered sessions
        ServerContext context = ServerUtils.getCurrentContextNullable();
        if (context != null) {
            context.close();
            ServerUtils.removeCurrentContext();
        }
        
        resetStaticStuff();
        // Some of our tests play with the manager so we have to make sure
        // he gets reset to the one we want between tests
        ServerUtils.globalContext().putAttribute(
            SessionAdministrationManagerBean.class.getName(), _manager);
    }
    
    private static ServerContextAdministrationBean getName(Thread thread) 
            throws MalformedObjectNameException, UnsupportedEncodingException {
        SessionAdministrationBean session = _manager.getSession(_testSessionName);
        
        return _manager.getSessionBeans(session).get(thread.getId());
    }
    
    @Test
    public void testServerMode() throws MocaException, MalformedObjectNameException, UnsupportedEncodingException {
        // Force the context to be created since we blow it away between every test
        ServerUtils.getCurrentContext();
        ServerContextAdministrationBean adminBean = getName(Thread.currentThread());
        
        assertEquals("The host should not be present " +
        		"since we are running in server mode", "N/A - Moca Server Application", 
                adminBean.getConnectedIpAddress());
    }
    
    @Test
    public void testServerLastCommand() throws MocaException, MalformedObjectNameException, UnsupportedEncodingException, InterruptedException {
        
        MocaContext moca = MocaUtils.currentContext();
        
        moca.executeCommand("publish data where foo = 'bar'");
        
        ServerContextAdministrationBean adminBean = getName(Thread.currentThread());
        
        // This is to make sure the admin bean has had time to update
        Thread.sleep(100);
        
        assertEquals("We should have last executed "
                + "publish data", "publish data where foo = 'bar'", adminBean
                .getLastCommand());
    }
    
    @Test
    public void testServerLastCommandTime() throws MocaException, MalformedObjectNameException, UnsupportedEncodingException, InterruptedException {
        
        DateTime before = new DateTime();
        
        MocaContext moca = MocaUtils.currentContext();
        
        moca.executeCommand("publish data where foo = 'bar'");
        
        // This is to make sure the admin bean has had time to update
        Thread.sleep(100);
        
        ServerContextAdministrationBean adminBean = getName(Thread.currentThread());
        
        DateTime after = new DateTime();
        
        Date executedTime = adminBean.getLastCommandTime();
        
        assertTrue("The execution time " + executedTime + 
                " should be before or equal the start " + before, 
        	before.isBefore(executedTime.getTime()) || 
        	before.isEqual(executedTime.getTime()));
        
        assertTrue("The execution time " + executedTime + 
                " should be after or equal the start " + after, 
                after.isAfter(executedTime.getTime()) || 
                after.isEqual(executedTime.getTime()));
    }
    
    @Test
    public void testServerLastSQL() throws MocaException, MalformedObjectNameException, UnsupportedEncodingException, InterruptedException {
        
        MocaContext moca = MocaUtils.currentContext();
        
        moca.executeCommand("[select count(1) from comp_ver]");
        
        // This is to make sure the admin bean has had time to update
        Thread.sleep(100);
        
        ServerContextAdministrationBean adminBean = getName(Thread.currentThread());
        
        assertEquals("We should have last executed "
                + "select count(1) from comp_ver SQL statement", 
                "select count(1) from comp_ver", adminBean.getLastSqlStatement());
    }
    
    @Test
    public void testServerLastSQLTime() throws MocaException, MalformedObjectNameException, UnsupportedEncodingException, InterruptedException {
        
        DateTime before = new DateTime();
        
        MocaContext moca = MocaUtils.currentContext();
        
        moca.executeCommand("[select count(1) from comp_ver]");
        
        // This is to make sure the admin bean has had time to update
        Thread.sleep(100);
        
        ServerContextAdministrationBean adminBean = getName(Thread.currentThread());
        DateTime after = new DateTime();
        
        Date executedTime = adminBean.getLastSqlStatementTime();
        assertTrue("The execution time " + executedTime + 
                " should be before or equal the start " + before, 
                before.isBefore(executedTime.getTime()) || 
                before.isEqual(executedTime.getTime()));
        
        assertTrue("The execution time " + executedTime + 
                " should be after or equal the start " + after, 
                after.isAfter(executedTime.getTime()) || 
                after.isEqual(executedTime.getTime()));
    }
    
    @Test
    public void testServerInterrupt() throws MocaException, InterruptedException {
        MocaContext moca = MocaUtils.currentContext();
        final Thread currentThread = Thread.currentThread();
        
        Thread thread = new Thread() {

            // @see java.lang.Thread#run()
            @Override
            public void run() {
                super.run();
                
                try {
                    _latch.await();
                }
                catch (InterruptedException e) {
                    System.out.println("We got interrupted early, this may " +
                    		"cause a failure : " + e.getMessage());
                    e.printStackTrace();
                }
                
                try {
                    ServerContextAdministrationBean adminBean = 
                        TU_ServerContextConfig.getName(currentThread);
                    
                    adminBean.interrupt();
                }
                catch (MBeanServerNotFoundException e) {
                    System.err.println("Interruption of moca session did not " +
                            "occur because :" + e.getMessage());
                    e.printStackTrace();
                }
                catch (MalformedObjectNameException e) {
                    System.err.println("Interruption of moca session did not " +
                                "occur because :" + e.getMessage());
                    e.printStackTrace();
                }
                catch (UnsupportedEncodingException e) {
                    System.err.println("Interruption of moca session did not " +
                            "occur because :" + e.getMessage());
                    e.printStackTrace(); 
                }
            }
            
        };
        
        thread.start();
        
        try {
            // We sleep for 2 seconds to let us ample time to interrupt
            moca.executeCommand("[[ " + TU_ServerContextConfig.class.getName() + ".registerRunning(); Thread.sleep(2000) ]]"); 
            fail("We should have been interrupted!");
        }
        catch (MocaInterruptedException e) {
            // We should go here since we were interrupted
            Throwable cause = e.getCause();
            
            assertTrue("We got a script exeption other " +
            		"than being interrupted",
                    cause instanceof InterruptedException); 
        }
        
        thread.join(THREAD_JOIN_TIMEOUT);
    }
    
    @Test
    public void testServerStack() throws MBeanServerNotFoundException, 
            MalformedObjectNameException, ProtocolException, InterruptedException, UnsupportedEncodingException {
        
        Thread thread = new Thread() {

            // @see java.lang.Thread#run()
            @Override
            public void run() {
                super.run();
                
                try {
                    MocaContext moca = MocaUtils.currentContext();
                    moca.executeCommand("[[ " + TU_ServerContextConfig.class.getName() + ".registerRunning() ]] | go to sleep where time = 10000");
                }
                catch (MocaScriptException e) {
                    // We should go here since we were interrupted
                    Throwable cause = e.getCause();
                    
                    if (! (cause instanceof InterruptedException)) {
                        System.err.println("While sleeping there was a problem " +
                                "encountered : " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                catch (MocaException e) {
                    System.err.println("While sleeping there was a problem " +
                    		"encountered : " + e.getMessage());
                    e.printStackTrace();
                } 
            }
            
        };
        thread.start();

        _latch.await();
        
        // This is to make sure the admin bean has had time to update
        Thread.sleep(100);
        
        ServerContextAdministrationBean adminBean = getName(thread);
        
        String dataStack = adminBean.queryDataStack();
        
        // We want to wake up our interrupt thread
        thread.interrupt();
        
        assertNotNull("The stack shouldn't be null", 
                dataStack);
        
        assertFalse("Our context should be active", 
                "Context Inactive".equals(dataStack));
        
        XMLResultsDecoder decoder = new XMLResultsDecoder(new StringReader(dataStack));

        System.out.println(dataStack);
        
        MocaResults res = decoder.decode();
        
        // We should have 2-4 levels
        // Level 0 - The whole command passed in
        // Level 1 - The notification that the command is running
        // Level 2 - The call to go to sleep
        // Level 3 - The script resulting from the go to sleep call
        assertTrue(res.getRowCount() >= 2);
        
        RowIterator rowIter = res.getRows();
        
        // We can only reliably check the last 2 levels
        // So we have to increment it to the last 2
        for (int i = 2; i < res.getRowCount(); ++i) {
            rowIter.next();
        }
        
        rowIter.next();
        assertEquals(1, rowIter.getInt("stack_level"));
        assertEquals(
                "(SCRIPT:  com.sam.moca.advice.TU_ServerContextConfig.registerRunning() )", rowIter.getString("stack_command"));
        
        rowIter.next();
        assertEquals(0, rowIter.getInt("stack_level"));
        assertEquals(
                "{(SCRIPT:  com.sam.moca.advice.TU_ServerContextConfig.registerRunning() ) | go to sleep WHERE time EQ (TYPE: INTEGER, VALUE: 10000)}", rowIter.getString("stack_command"));
    
        thread.join(THREAD_JOIN_TIMEOUT);
    }
    
    @Test
    public void testServerStackInline() throws MBeanServerNotFoundException, 
            MalformedObjectNameException, ProtocolException, InterruptedException, UnsupportedEncodingException {

        Thread thread = new Thread() {

            // @see java.lang.Thread#run()
            @Override
            public void run() {
                super.run();

                try {
                    MocaContext moca = MocaUtils.currentContext();
                    moca.executeCommand("publish data where foo = 'bar' " +
                            "| " +
                            "execute server command " +
                            "  where inline = 0 " +
                            "    and cmd = '" +
                            "           [[ " + TU_ServerContextConfig.class.getName() + ".registerRunning() ]] " +
                            "           | " + 
                            "           go to sleep where time = 10000" +
                            "              '");
                }
                catch (MocaScriptException e) {
                    // We should go here since we were interrupted
                    Throwable cause = e.getCause();

                    if (! (cause instanceof InterruptedException)) {
                        System.err.println("While sleeping there was a problem " +
                                "encountered : " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                catch (MocaException e) {
                    System.err.println("While sleeping there was a problem " +
                            "encountered : " + e.getMessage());
                    e.printStackTrace();
                } 
            }

        };
        thread.start();

        _latch.await();
        
        ServerContextAdministrationBean adminBean = getName(thread);

        String dataStack = adminBean.queryDataStack();

        // We want to wake up our interrupt thread
        thread.interrupt();

        assertNotNull("The stack shouldn't be null", 
                dataStack);

        assertFalse("Our context should be active", 
                "Context Inactive".equals(dataStack));

        XMLResultsDecoder decoder = new XMLResultsDecoder(new StringReader(dataStack));

        System.out.println(dataStack);

        MocaResults res = decoder.decode();

        // We should have 5-7 levels, the first few are empty due to new context
        // Level 0 - empty
        // Level 1 - empty
        // Level 2 - empty
        // Level 3 - empty
        // Level 4 - The whole command passed in
        // Level 5 - The notification that the command is running
        // Level 6 - The call to go to sleep
        // Level 7 - The script resulting from the go to sleep call
        assertTrue(res.getRowCount() >= 5);

        RowIterator rowIter = res.getRows();

        // We can only reliably check levels 5 and below
        // So we have to increment past 6 & 7
        for (int i = 6; i < res.getRowCount(); ++i) {
            rowIter.next();
        }

        rowIter.next();
        assertEquals(5, rowIter.getInt("stack_level"));
        assertEquals(
                "(SCRIPT:  com.sam.moca.advice.TU_ServerContextConfig.registerRunning() )", rowIter.getString("stack_command"));

        rowIter.next();
        assertEquals(4, rowIter.getInt("stack_level"));
        assertEquals(
                "{(SCRIPT:  com.sam.moca.advice.TU_ServerContextConfig.registerRunning() ) | go to sleep WHERE time EQ (TYPE: INTEGER, VALUE: 10000)}", rowIter.getString("stack_command"));
        
        // Verify that the levels that aren't in context are empty
        for (int i = 3; i >= 0; --i) {
            rowIter.next();
            assertEquals(i, rowIter.getInt("stack_level"));
            assertEquals(null, rowIter.getString("stack_command"));
        }
        
        thread.join(THREAD_JOIN_TIMEOUT);
    }
    
    @Test
    public void testPersistentThreadAcrossContext() 
            throws SystemConfigurationException, InterruptedException, 
            ExecutionException {
        CurrentValues currentValues = TestServerUtils.takeCurrentValues();
        
        ServerContext context1 = null;
        ServerContext context2 = null;
        
        ExecutorService service = null;
        
        try {
            ServerUtils.setCurrentContext(null);
            
            context1 = ServerUtils.setupDaemonContext("TEST1", 
                    false);
            
            // Put in a system variable so we can check if it exists
            context1.putSystemVariable("foo", "bar");
            final Thread parentThread = Thread.currentThread();
            
            Callable<Boolean> callable = new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    try {
                        ServerUtils.associateCurrentThreadWithSession(parentThread);
                        MocaContext context = MocaUtils.currentContext();
                        String fooString = context.getSystemVariable("foo");
                        if ("bar".equals(fooString)) {
                            return true;
                        }
                        return false;
                    }
                    finally {
                        ServerUtils.getCurrentContext().close();
                    }
                }
                
            };
            
            // Now we create a persistent thread pool
            service = Executors.newSingleThreadExecutor();
            
            {
                // Submit the callable along
                Future<Boolean> future1 = service.submit(callable);
                
                Assert.assertTrue("The system variable should be present", 
                        future1.get());
            }
            
            // Now we reset the server context behind the scenes.  We do this
            // so we can emulate a new context coming online.  By doing so, we
            // need to have a way of signaling to persistent threads to update
            // their context information

            //ServerUtils.setCurrentContext(null);
            
            TestServerUtils.cleanupDaemonContext();
            
            context2 = ServerUtils.setupDaemonContext("TEST2", false);
            
            Assert.assertNull(
                    "The value shouldn't be present in the new context",
                    context2.getSystemVariable("foo"));
            
            // Now we try it with the new context associated, it shouldn't be 
            // there
            {
                // Submit the callable along
                Future<Boolean> future2 = service.submit(callable);
                
                Assert.assertFalse("The system variable shouldn't be present", 
                        future2.get());
            }
        }
        finally {
            if (context1 != null) {
                context1.close();
            }
            
            if (context2 != null) {
                context2.close();
            }

            if(service != null){
                service.shutdown();
            }
            
            // Clear out some stuff as well
            //ServerUtils.setCurrentContext(null);
            TestServerUtils.cleanupDaemonContext();
            
            // Lastly we put the context back in
            TestServerUtils.restoreValues(currentValues);
        }
    }
    
    @Test
    public void testGetNewDefaultServerContext() throws SystemConfigurationException {
        DefaultServerContextFactory factory = new DefaultServerContextFactory(
                "TU_ServerContextConfig", ServerUtils.globalContext() , false,
                Mockito.mock(NativeProcessPoolBuilder.class, Mockito.RETURNS_MOCKS));
        
        ServerContext ctx1 = factory.newContext(new RequestContext(new HashMap<String, String>()), 
                new LocalSessionContext("TESTDSC1", SessionType.TASK));
        
        ServerContext ctx2 = factory.newContext(new RequestContext(new HashMap<String, String>()), 
                new LocalSessionContext("TESTDSC2", SessionType.TASK));
        
        try {
            Assert.assertFalse(ctx1.equals(ctx2));
        }
        finally {
            ctx1.close();
            ctx2.close();
        }
        
        factory.close();
    }
    
    @Test
    public void testQueryLimitExceededNotification() throws SQLException, MocaException {
        // Mock out the DBAdapter to throw TooManyRow exceptions
        DBAdapter dbAdapter = Mockito.mock(DBAdapter.class);
        Mockito.when(
            dbAdapter.executeSQL(Mockito.any(ArgumentSource.class), Mockito.any(MocaTransaction.class), Mockito.anyString(), Mockito.any(BindList.class),
                Mockito.any(BindMode.class), Mockito.anyBoolean(), Mockito.any(CommandPath.class))).thenThrow(new TooManyRowsException(100000L));
        
        MadFactory origFact = MadMetrics.getFactory();
        MadFactory mockFactory = Mockito.mock(MadFactory.class);
        
        SessionContext session = new LocalSessionContext("TESTDB", SessionType.TASK);
        session.setSessionToken(new SessionToken("TEST"));
        
        ServerContext testContext = ServerContextConfig.serverContext(
            Mockito.mock(ScriptAdapter.class), dbAdapter,
            session, ServerUtils.globalContext(),
            new RequestContext(new HashMap<String, String>()), Mockito.mock(CommandRepository.class),
            Mockito.mock(NativeAdapterFactory.class), Mockito.mock(CommandUsage.class),
            new ArrayList<String>(), Mockito.mock(MessageResolver.class), Mockito.mock(RemoteConnectionFactory.class));
        
        try {
            MadMetrics.setFactory(mockFactory);

            try {
                testContext.executeSQLWithVars("[ select * from huge_table ]");
                fail("The mock should have thrown TooManyRows exception here");
            }
            catch (TooManyRowsException expected) {
                ArgumentCaptor<ConcatString> captor = ArgumentCaptor.forClass(ConcatString.class);
                // Verify the notification was sent and that it reports the sql statement.
                Mockito.verify(mockFactory).sendNotification(Mockito.eq("moca.query-row-limit"), captor.capture());
                assertTrue(captor.getValue().toString().contains("[ select * from huge_table ]"));
            }
        }
        finally {
            testContext.close();
            MadMetrics.setFactory(origFact);
        }
    }
}
