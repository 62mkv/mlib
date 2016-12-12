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

package com.redprairie.moca.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.ClosedByInterruptException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.transaction.Status;
import javax.transaction.SystemException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaRuntimeException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.legacy.GenericPointer;
import com.redprairie.moca.server.legacy.MocaNativeException;
import com.redprairie.moca.util.CommandInterceptor;
import com.redprairie.moca.util.MocaTestUtils;
import com.redprairie.moca.util.MocaUtils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for MOCA Command Processing behavior. These tests require that some test libraries
 * ($MOCADIR/test/src/libsrc/mocatest) be built and deployed.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TI_TestComponents {
 
    private static ServerContext previousContext = null;
    private static CountDownLatch _latch;
    
    @BeforeClass public static void beforeTests() throws SystemConfigurationException {
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
        ServerUtils.setupDaemonContext(TI_TestComponents.class.getName(), 
                false);
    }
    
    @AfterClass public static void afterTests() {
        // Now restore the context back to what it was
        ServerUtils.setCurrentContext(previousContext);
    }
    
    @After
    public void afterTest() throws MocaException {
        // We need to rollback after every test to release any information
        MocaUtils.currentContext().rollback();
    }
    
    @Test
    public void testSimpleComponent() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("test simple component where arg = 'blah'");
        assertTrue(res.next());
        assertEquals("zzz", res.getString("foo"));
        assertEquals("blah", res.getString("bar"));
    }

    @Test
    public void testGenericOnStack() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("test cache create | test cache close");
        assertFalse(res.next());
    }

    @Test
    public void testInitiateCommandTakingGenericArg() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("test cache create");
        assertTrue(res.next());
        Object cacheValue = res.getValue("cache");
        assertTrue(cacheValue instanceof GenericPointer);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("cache", cacheValue);
        ctx.executeCommand("test cache close", args);
    }
    
    @Test
    public void testCommandPullingValueFromGenericArg() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("test cache create");
        assertTrue(res.next());
        Object cacheValue = res.getValue("cache");
        assertTrue(cacheValue instanceof GenericPointer);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("cache", cacheValue);

        ctx.executeCommand("test cache add where key = 'foo' and value = 'xxxx'", args);
        ctx.executeCommand("test cache add where key = 'baz' and value = 'yyyy'", args);
        res = ctx.executeCommand("test cache get where key = 'foo'", args);
        
        assertTrue(res.next());
        assertEquals("xxxx", res.getString("value"));
        
        ctx.executeCommand("test cache close", args);
    }
    
    @Test
    public void testLegacyComponentInitiatingLegacyComponentWithGenericArgument() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        // This command creates a C-side cache object, populates it, then initiates
        // another command using that object, populating a transaction attribute along
        // the way.
        MocaResults res = ctx.executeCommand("test cache initiate");
        assertTrue(res.next());
        assertEquals("zzz", res.getString("foo"));
        assertEquals("yyy", ctx.getTransactionAttribute("initiateTest"));
    }

    @Test
    public void testCrashOfLegacyComponent() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            ctx.executeCommand("dump core");
            fail("Expected exception");
        }
        catch (MocaException e) {
            // normal;
        }
        
        // Now try again with a command that would normally work.
        try {
            ctx.executeCommand("test simple component where arg = 'blah'");
            fail("Expected exception");
        }
        catch (MocaException e) {
            // normal;
        }
        
        // This should release the native process
        ctx.rollback();
        
        // Now run another native command
        MocaResults res = ctx.executeCommand("test simple component where arg = 'blah'");
        assertTrue(res.next());
        assertEquals("zzz", res.getString("foo"));
        assertEquals("blah", res.getString("bar"));
    }

    @Test
    public void testIndirectCrash() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            ctx.executeCommand("test indirect crash");
            fail("Expected exception");
        }
        catch (MocaException e) {
            // normal;
        }
        
        // Now try again with a command that would normally work.
        try {
            ctx.executeCommand("test simple component where arg = 'blah'");
            fail("Expected exception");
        }
        catch (MocaException e) {
            // normal;
        }
        
        // This should release the native process
        ctx.rollback();
        
        // Now run another native command
        MocaResults res = ctx.executeCommand("test simple component where arg = 'blah'");
        assertTrue(res.next());
        assertEquals("zzz", res.getString("foo"));
        assertEquals("blah", res.getString("bar"));
    }

    @Test
    public void testSQLExecuteFromCComponent() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("test simple sql where arg = [select 'x' as foo from dual]");
        assertTrue(res.next());
        assertEquals("x", res.getString("foo"));
        assertFalse(res.next());
    }

    @Test
    public void testSQLExecuteFromCComponentThrowingNotFound() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            MocaResults res = ctx.executeCommand("test simple sql where arg = [select 'x' as foo from dual where 1=2]");
            fail("Expected NotFoundException, got " + res);
        }
        catch (NotFoundException e) {
            // Normal
        }
    }

    @Test
    public void testSQLErrorThrown() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            MocaResults res = ctx.executeCommand("test sql error");
            fail("Expected DB Exception, got " + res);
        }
        catch (MocaException e) {
            assertTrue(e.getErrorCode() < 0);
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testSQLExecuteFromCComponentThrowingSQLException() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            MocaResults res = ctx.executeCommand("test simple sql where arg = [select 'x' as foo from nosuchtable]");
            fail("Expected NotFoundException, got " + res);
        }
        catch (MocaException e) {
            // DB errors are negative
            assertTrue("Expected negative error code, got " + e.getErrorCode(), e.getErrorCode() < 0);
            System.out.println(e);
        }
    }
    
    @Test
    public void testSQLExecuteFromCComponentThrowingSQLExceptionUniqueConstraint() 
            throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            ctx.executeCommand("test simple sql where arg = " +
            		"[insert into comp_ver ( base_prog_id, comp_maj_ver, " +
            		"comp_min_ver, comp_bld_ver, comp_rev_ver, comp_file_nam, " +
            		"comp_prog_id, comp_typ,comp_file_ext, grp_nam ) " +
            		"values ( 'base_prog_id', 1, 1, 1, 1, 'comp_file_nam', " +
            		"'comp_prog_id', 'A', 'ext', 'grp_nam') ]");
            
            MocaResults res = ctx.executeCommand("test simple sql where arg = " +
                    "[insert into comp_ver ( base_prog_id, comp_maj_ver, " +
                    "comp_min_ver, comp_bld_ver, comp_rev_ver, comp_file_nam, " +
                    "comp_prog_id, comp_typ,comp_file_ext, grp_nam ) " +
                    "values ( 'base_prog_id', 1, 1, 1, 1, 'comp_file_nam', " +
                    "'comp_prog_id', 'A', 'ext', 'grp_nam') ]");
            fail("Expected Unique Constraint Exception, got " + res);
        }
        catch (MocaException e) {
            // DB errors are negative
            assertEquals("The error code is incorrect", -1, e.getErrorCode());
        }
    }
    
    @Test
    public void testCompiledCommandWithResultsArg() throws MocaException {
        // This command initiates the argument as a command, then passes the result set as
        // a compiled command argument to an initiated command.
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("test compiled command where arg = '[[ a = 100 ]] & [[a = 3]] & [[a = 20]]'" +
        		" and sortby = 'a'");
        assertTrue(res.next());
        assertEquals(3, res.getInt("a"));
        assertTrue(res.next());
        assertEquals(20, res.getInt("a"));
        assertTrue(res.next());
        assertEquals(100, res.getInt("a"));
        assertFalse(res.next());
        
    }

    @Test
    public void testCompiledCommandWithNullResultsArg() throws MocaException {
        // Since we're not passing a command argument, this will attempt to sort a null result set.
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("test compiled command where arg = null" +
                        " and sort_list = ''");
        assertFalse(res.next());
    }

    @Test
    public void testMissingFunction() throws MocaException {
        // This command is defined as a function that doesn't exist.
        MocaContext ctx = MocaUtils.currentContext();
        try {
            MocaResults res = ctx.executeCommand("test no such function");
            fail("expected error, got " + res.getRowCount() + " rows");
        }
        catch (MocaException e) {
            assertEquals(MocaNativeException.CODE, e.getErrorCode());
            assertEquals("testNoSuchFunction", e.getArgValue("function"));
        }
    }

    @Test
    public void testOverrideAliasProcessing() throws MocaException {
        // Make sure that an alias at the higher stack level overrides the
        // primary name at a lower one.
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("noop where arg = 'ARG' | test alias arg where value = 'VALUE'");
        assertTrue(res.next());
        assertEquals("VALUE", res.getString(0));
    }

    @Test
    public void testOverrideAliasOnTwoStackLevels() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("[[ arg = 'ARG']] | [[ value = 'VALUE']] | test alias arg");
        assertTrue(res.next());
        assertEquals("VALUE", res.getString(0));
    }

    @Test
    public void testOverrideAliasResultsOnSameStackLevel() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        // This is counterintuitive. An alias in a result set BEFORE the
        // argument name gets piority.
        MocaResults res = ctx.executeCommand("publish data where value = 'VALUE' and arg = 'ARG' | test alias arg");
        assertTrue(res.next());
        assertEquals("VALUE", res.getString(0));
    }

    @Test
    public void testOverrideAliasArgumentsOnSameStackLevel() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        // Even more counterintuitive. An alias in an arg list set after the
        // argument name gets piority.
        MocaResults res = ctx.executeCommand("noop where arg = 'ARG' and value = 'VALUE' | test alias arg");
        assertTrue(res.next());
        assertEquals("VALUE", res.getString(0));

        res = ctx.executeCommand("noop where value = 'VALUE' and arg = 'ARG' | test alias arg");
        assertTrue(res.next());
        assertEquals("ARG", res.getString(0));
    }

    @Test
    public void testAliasProcessing() throws MocaException {
        // Make sure an alias gets picked up as an argument.
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("[[ value = 'TESTARG']] | test alias arg");
        assertTrue(res.next());
        assertEquals("TESTARG", res.getString(0));

        res = ctx.executeCommand("noop where value = 'TESTARG' | test alias arg");
        assertTrue(res.next());
        assertEquals("TESTARG", res.getString(0));
    }


    @Test
    public void testAliasesWithHiddenVariable() throws MocaException {
        // Make sure an alias gets picked up as an argument.
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("[[ value = 'VALUE'; arg = 'ARG']] " +
        		"| hide stack variable where name = 'arg' " +
        		"| test alias arg");
        assertTrue(res.next());
        assertNull(res.getString(0));
    }

    @Test
    public void testNoAliasArgWithEmptyNameOnStack() throws MocaException {
        // Make sure an alias gets picked up as an argument.
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("publish data where arg = 'ARG' | " +
        		"[[ ['':'EMPTY' ] ]] | test no alias arg");
        assertTrue(res.next());
        assertEquals("ARG", res.getString(0));
        
        res = ctx.executeCommand("[[ ['':'EMPTY' ] ]] | test no alias arg");
        assertTrue(res.next());
        assertNull(res.getString(0));
    }


    @Test
    public void testGetVariableNotPresent() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
                "test get variable " +
        	"  where name = 'NOT-PRESENT'");
        assertTrue(res.next());
        assertNull("Value shouldn't be present in environment.", 
                res.getString(0));
    }
    
    @Test
    public void testGetVariablePresent() throws MocaException {
        String name = "PRESENT";
        String value = "TEST";
        MocaContext ctx = MocaUtils.currentContext();
        
        ctx.putSystemVariable(name, value);
        MocaResults res = ctx.executeCommand(
                "test get variable " +
                "  where name = '" + name + "'");
        assertTrue(res.next());
        assertEquals("Unable to retrieve value after setting on Java side.", 
                value, res.getString(0));
    }
    
    @Test
    public void testSetVariableAsNull() throws MocaException {
        String name = "PRESENT";
        MocaContext ctx = MocaUtils.currentContext();

        // Then we try to set the value which should check it for null
        ctx.executeCommand(
                "test set variable " +
                "  where name = '" + name + "'" +
                "    and value = null");
    }
    
    @Test
    public void testSetVariablePresentAndWasNull() throws MocaException {
        String name = "PRESENT";
        String value = "TEST";
        MocaContext ctx = MocaUtils.currentContext();
        
        // First we call the get to initialize the cache as null
        ctx.executeCommand(
                "test get variable " +
                "  where name = '" + name + "'");

        // Then we try to set the value which should check it for null
        ctx.executeCommand(
                "test set variable " +
                "  where name = '" + name + "'" +
                "    and value = '" + value + "'");
    }
    
    @Test
    public void testGetNeededElementWithAPointer() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        // This has to be an int, since we are calling into C.
        // This test will fail if it is a long.
        GenericPointer pointer = new GenericPointer(32);
        
        Map<String, Object> args = new HashMap<String, Object>();
        
        args.put("pointer", pointer);
        
        // Now we tell it to pick up the res argument
        args.put("arg", "pointer");
        
        MocaResults retRes = ctx.executeCommand("test get needed element pointer", args);

        assertTrue(retRes.next());
        
        Object value = retRes.getValue("value");
        
        assertTrue(value instanceof GenericPointer);
        
        assertEquals(pointer, value);
    }
    
    @Test
    public void testGetNeededElementWithANonSerializableObject() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        Object notSerializable = new Object();
        
        Map<String, Object> args = new HashMap<String, Object>();
        
        args.put("object", notSerializable);
        
        // Now we tell it to pick up the res argument
        args.put("arg", "object");
        
        MocaResults retRes = ctx.executeCommand("test get needed element pointer", args);

        assertTrue(retRes.next());
        
        Object value = retRes.getValue("value");

        // Since it isn't serializable there is no way to get the object across
        assertNull(value);
    }
    
    @Test
    public void testGetNeededElementWithASerializableObject() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        Map<String, String> serializable = Collections.singletonMap("answer", "works");
        
        Map<String, Object> args = new HashMap<String, Object>();
        
        args.put("object", serializable);
        
        // Now we tell it to pick up the res argument
        args.put("arg", "object");
        
        MocaResults retRes = ctx.executeCommand("test get needed element pointer", args);

        assertTrue(retRes.next());
        
        // Unfortunately C is not allowed to send us back the object.  This
        // test is more to make sure it doesn't crash.
        assertNull(retRes.getValue("value"));
        
    }
    
    @Test
    public void testGetNeededElementWithAResultSet() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        EditableResults res = ctx.newResults();
        
        res.addColumn("work", MocaType.STRING);
        
        res.addRow();
        
        res.setStringValue("work", "worked");
        
        Map<String, Object> args = new HashMap<String, Object>();
        
        args.put("res2", res);
        
        // Now we tell it to pick up the res argument
        args.put("arg", "res2");
        
        MocaResults retRes = ctx.executeCommand("test get needed element result", args);

        assertTrue(retRes.next());
        
        assertEquals("worked", retRes.getString("work"));
    }
    
    @Test
    public void testGetArgs() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        // The test command references variables a, b, and c, so those get
        // removed from the arg list.
        MocaResults retRes = ctx.executeCommand("test get args where foo = 'bar' and b != 2 and b < 10");

        assertTrue(retRes.next());
        
        assertEquals("foo", retRes.getString("name"));

        assertFalse(retRes.next());
    }
    
    @Test
    public void testGetArgsCaseSensitivity() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        // The test command references variables a, b, and c, so those get
        // removed from the arg list.
        MocaResults retRes = ctx.executeCommand("test get args where FOO = 'bar' and bAR > 50 and BaZ != 'BLAH'");

        assertTrue(retRes.next());
        assertEquals("FOO", retRes.getString("name"));
        assertTrue(retRes.next());
        assertEquals("bAR", retRes.getString("name"));
        assertTrue(retRes.next());
        assertEquals("BaZ", retRes.getString("name"));
        assertFalse(retRes.next());
    }
    
    @Test
    public void testGetArgsIndirectCaseSensitivity() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        // The test command references variables a, b, and c, so those get
        // removed from the arg list.
        MocaResults retRes = ctx.executeCommand("test get args indirect where FOO = 'bar' and bAR > 50 and BaZ != 'BLAH'");

        assertTrue(retRes.next());
        assertEquals("FOO", retRes.getString("name"));
        assertTrue(retRes.next());
        assertEquals("bAR", retRes.getString("name"));
        assertTrue(retRes.next());
        assertEquals("BaZ", retRes.getString("name"));
        assertFalse(retRes.next());
    }
    
    @Test
    public void testRemoteNullReturn() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        MocaResults retRes = ctx.executeCommand(
                "test null return" +
                "  where arg = 'NOT-PRESENT'");

        assertTrue(retRes.next());
        
        assertEquals("", retRes.getString("value"));

        assertFalse(retRes.next()); 
    }
    
    @Test
    public void testRollbackToSavepoint() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        try {
            
            // Use the comp_ver table from MOCA.  This table must have at least
            // one row in it for this test to work
            ctx.executeCommand(
                "[insert into comp_ver ( base_prog_id, comp_maj_ver, " +
                "comp_min_ver, comp_bld_ver, comp_rev_ver, comp_file_nam, " +
                "comp_prog_id, comp_typ,comp_file_ext, grp_nam ) " +
                "values ( 'base_prog_id', 1, 1, 1, 1, 'comp_file_nam', " +
                "'comp_prog_id', 'A', 'ext', 'grp_nam') ]");
            
            MocaResults res = ctx.executeCommand("[select count(*) x from comp_ver]");
            assertTrue(res.next());
            int count = res.getInt("x");
            
            ctx.executeCommand(
                    "test savepoint where table_name = 'comp_ver' and count= " + count);
            
            // No results -- the C component inidicates failure via exception
    
            res = ctx.executeCommand("[select count(*) x from comp_ver]");
            assertTrue(res.next());
            assertEquals(count, res.getInt("x"));
        }
        finally {
            ctx.rollback();
        }
    }
    
    @Test
    public void testBindNullDate() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        MocaResults retRes = ctx.executeCommand(
                "test sql bind date where arg = ''");

        assertTrue(retRes.next());
        
        assertNull(retRes.getDateTime(0));

        assertFalse(retRes.next()); 
    }
    
    @Test
    public void testBindReferenceValue() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        MocaResults retRes = ctx.executeCommand(
                "test sql bind reference");

        assertTrue(retRes.next());
        
        assertEquals("FOO", retRes.getString("result"));

        assertFalse(retRes.next()); 
    }
    
    @Test
    public void testBindSQLValue() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        MocaResults retRes = ctx.executeCommand(
                "test sql bind value where name = 'v_ABC' and value = 'BAR'");

        assertTrue(retRes.next());
        assertEquals("BAR", retRes.getString("result"));
        assertFalse(retRes.next()); 
    }
    
    @Test
    public void testInterrupt() throws Throwable {
        resetLatch();
        MocaContext moca = MocaUtils.currentContext();
        final Thread currentThread = Thread.currentThread();
        new Thread() {

            // @see java.lang.Thread#run()
            @Override
            public void run() {
                super.run();
                try {
                    _latch.await();
                    Thread.sleep(5000);
                    currentThread.interrupt();
                }
                catch (InterruptedException e) {
                    System.out.println("We got interrupted early, this may "
                            + "cause a failure : " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.start();

        try {
            // We sleep for 2 seconds to let us ample time to interrupt
            moca.executeCommand("test execute where arg = '[[ " + TI_TestComponents.class.getName()
                    + ".registerRunning();]]'; test sleep where duration = 30000");
            junit.framework.Assert.fail("We should have been interrupted!");
        }
        catch (MocaInterruptedException e) {
            // We should go here since we were interrupted
            Throwable cause = e.getCause();

            junit.framework.Assert
                .assertTrue(
                    "We got a script exeption other "
                            + "than being interrupted",
                    (cause == null || cause instanceof InterruptedException || cause instanceof ClosedByInterruptException));
        }
    }
    
    public static void registerRunning() {
        _latch.countDown();
    }
    
    public static void resetLatch() {
        _latch = new CountDownLatch(1);
    }
    
    @Test
    public void testInterruptFromCallInC() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            ctx.executeCommand("test execute where arg = '[[throw new MocaInterruptedException()]]'");
            fail("We should have thrown a MocaInterruptedException");
        }
        catch (MocaInterruptedException e) {
            // We either should go here or had the interrupt state set
        }
    }
    
    @Test
    public void testTriggerExecution() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        // Ignore the results
        ctx.executeCommand("test clear counter");
        ctx.executeCommand("test do nothing");
        
        // This counter is incremented by trigger 1
        MocaResults res = ctx.executeCommand("test get counter where name = 'nothing_trigger_1'");
        assertTrue(res.next());
        assertEquals(1, res.getInt("value"));

        // This counter is incremented by trigger 2
        res = ctx.executeCommand("test get counter where name = 'nothing_trigger_2'");
        assertTrue(res.next());
        assertEquals(1, res.getInt("value"));

        // This counter is incremented by trigger 3 (disabled)
        res = ctx.executeCommand("test get counter where name = 'nothing_trigger_3'");
        assertTrue(res.next());
        assertEquals(0, res.getInt("value"));
    }
    
    @Test
    public void testSrvHooksWithNewTransaction() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        // First we register the hook.
        ctx.executeCommand("test register srv execute after rollback");
        
        // Make sure the value is 0 as a baseline.
        MocaResults res = ctx.executeCommand(
                "test register srv execute after rollback count");
        
        assertTrue(res.next());
        
        assertEquals(0, res.getInt(0));
        
        assertFalse(res.next());
        
        // Now we execute out of transaction which used to call the hooks
        ctx.executeCommand("test srv execute new transaction", new MocaArgument(
                "cmd", "noop"));
        
        // Make sure the value is still 0.
        res = ctx.executeCommand(
                "test register srv execute after rollback count");
        
        assertTrue(res.next());
        
        assertEquals(0, res.getInt(0));
        
        assertFalse(res.next());
        
        // We now have to rollback which should call the rollback.
        ctx.rollback();
        
        // The value should now be 1.
        res = ctx.executeCommand(
                "test register srv execute after rollback count");
        
        assertTrue(res.next());
        
        assertEquals(1, res.getInt(0));
        
        assertFalse(res.next());
    }

    @Test
    public void testExternalGroovyCommand() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        MocaResults retRes = ctx.executeCommand(
                "test external groovy command where arg = 'blah'");

        assertTrue(retRes.next());
        
        assertEquals("blah", retRes.getString("xxx"));

        assertFalse(retRes.next()); 
    }
    
    @Test
    public void testTracingNativeOutput() throws MocaException, IOException, InterruptedException {
        MocaContext ctx = MocaUtils.currentContext();
        
        File tempFile = null;
        try {
            try {
                tempFile = File.createTempFile("trace", ".log");
                ctx.executeCommand("set trace where level='*' and activate=true",
                    new MocaArgument("directory", tempFile.getParent()),
                    new MocaArgument("filename", tempFile.getName()));
                
                ctx.executeCommand("test native output where arg = 'TEST OUTPUT'");
                Thread.sleep(1000L);
            }
            finally {
                ctx.executeCommand("set trace where activate = false");
            }
            
            BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(tempFile), "UTF-8"));
            boolean sawOutMessage = false;
            boolean sawErrMessage = false;
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.contains("STDOUT: [TEST OUTPUT]")) sawOutMessage = true;
                    if (line.contains("STDERR: [TEST OUTPUT]")) sawErrMessage = true;
                }
            }
            finally {
                in.close();
            }
            
            assertTrue(sawOutMessage);
            assertTrue(sawErrMessage);
        }
        finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }

    }
    @Test
    public void testResolvedErrorThrown() {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            MocaResults retRes = ctx.executeCommand("test resolved error");
            fail("Expected exception, got" + retRes);
        }
        catch (MocaException e) {
            assertEquals(49992, e.getErrorCode());
            assertEquals("RESOLVED", e.getMessage());
            assertTrue(e.isMessageResolved());
        }
    }

    @Test
    public void testPublishAndRetrievePointer() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        MocaResults res = ctx.executeCommand("test publish pointer where name = 'foo'");

        assertTrue(res.next());
        
        Object value = res.getValue("foo");
        assertNotNull(value);
        assertTrue(value instanceof GenericPointer);
        
        res = ctx.executeCommand("test retrieve pointer where name = 'foo'", new MocaArgument("foo", value));
        assertTrue(res.next());
        
        assertNotNull(res.getString("result"));
    }
    
    @Test
    public void testPublishAndRetrievePointerThroughNamedParameter() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        MocaResults res = ctx.executeCommand("test publish pointer where name = 'foo'");

        assertTrue(res.next());
        
        Object value = res.getValue("foo");
        assertNotNull(value);
        assertTrue(value instanceof GenericPointer);
        
        res = ctx.executeCommand("test take pointer argument", new MocaArgument("value", value));
        assertTrue(res.next());
        
        assertNotNull(res.getString("result"));
    }
    
    @Test
    public void testPublishAndRetrievePointerThroughArgumentPassing() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        MocaResults res = ctx.executeCommand("test publish pointer where name = 'foo'");

        assertTrue(res.next());
        
        Object value = res.getValue("foo");
        assertNotNull(value);
        assertTrue(value instanceof GenericPointer);
        
        res = ctx.executeCommand("test enumerate pointer where bar = @value", new MocaArgument("value", value));
        assertTrue(res.next());
        
        assertNotNull(res.getString("result"));
    }
    
    @Test
    public void testRetrieveResultsPointerFromC() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        EditableResults res1 = new SimpleResults();
        res1.addColumn("A", MocaType.STRING);
        res1.addRow();
        res1.setStringValue("A", "Hello");
        
        EditableResults res2 = new SimpleResults();
        res2.addColumn("B", MocaType.STRING);
        res2.addRow();
        res2.setStringValue("B", "Goodbye");
        
        MocaResults res = ctx.executeCommand("test retrieve results where name1 = 'foo' and name2 = 'bar'",
            new MocaArgument("foo", MocaType.RESULTS, res1),
            new MocaArgument("bar", MocaType.RESULTS, res2));

        assertTrue(res.next());
        
        Object value = res.getValue("result");
        assertEquals("HelloGoodbye", value);
    }

    @Test
    public void testNewTransactionBehavior() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        MocaResults res = ctx.executeCommand("[select count(*) x from comp_ver]");
        assertTrue(res.next());
        int beforeCount = res.getInt("x");
        assertFalse(res.next());
        
        // Use the comp_ver table from MOCA.  This table must have at least
        // one row in it for this test to work
        ctx.executeCommand(
            "[insert into comp_ver ( base_prog_id, comp_maj_ver, " +
            "comp_min_ver, comp_bld_ver, comp_rev_ver, comp_file_nam, " +
            "comp_prog_id, comp_typ,comp_file_ext, grp_nam ) " +
            "values ( 'base_prog_id23', 1, 1, 1, 1, 'comp_file_nam', " +
            "'comp_prog_id', 'A', 'ext', 'grp_nam') ]");
        
        int newTransCount = -1;
        try {
            ctx.executeCommand(
                "test srv execute new transaction" +
                "  where cmd = \"[select count(*) x from comp_ver] | " +
                "[insert into comp_ver ( base_prog_id, comp_maj_ver, " +
                "comp_min_ver, comp_bld_ver, comp_rev_ver, comp_file_nam, " +
                "comp_prog_id, comp_typ,comp_file_ext, grp_nam ) " +
                "values ( 'base_prog_id45', 1, 1, 1, 1, 'comp_file_nam', " +
                "'comp_prog_id', 'A', 'ext', 'grp_nam') ] " +
                "| set return status where status = @x + 1\"");
            fail("Test should have thrown an exception");
        }
        catch (MocaException e) {
            newTransCount = e.getErrorCode();
        }
        
        // The counts should be the same minus 1.  The reason for the minus 1
        // is because in the nested call we add 1 since a status of 0 doesn't
        // cause an exception to be thrown.  This proves we have a new
        // transaction since it can't see the row inserted
        assertEquals(beforeCount, newTransCount - 1);
        
        res = ctx.executeCommand("[select count(*) x from comp_ver]");
        assertTrue(res.next());
        int afterCount = res.getInt("x");
        assertFalse(res.next());
        
        // The after count should include the 1 row we have in our tx
        assertEquals(beforeCount + 1, afterCount);
        
        ctx.rollback();
        
        res = ctx.executeCommand("[select count(*) x from comp_ver]");
        assertTrue(res.next());
        int afterCountRolledBack = res.getInt("x");
        assertFalse(res.next());
        
        // The after count should be the same since we rolled back
        assertEquals(beforeCount, afterCountRolledBack);
    }
    
    @Test
    public void testLocalRemoteRollback() throws MocaException, SystemException {
        MocaContext ctx = MocaUtils.currentContext();
        SystemContext system = ServerUtils.globalContext();
        String url = system.getConfigurationElement(MocaRegistry.REGKEY_SERVER_URL);
        
        ctx.executeCommand("remote('" + url + "') [select 1 from dual]");
        
        ctx.rollback();
        
        javax.transaction.TransactionManager manager = TransactionManagerUtils.getManager();
        
        assertEquals(Status.STATUS_NO_TRANSACTION, manager.getStatus());
    }
    
    @Test
    public void testOverriddenCommandInC() throws MocaException {
        final int code = 1234;
        try {
            MocaTestUtils.overrideCommand("foo bar", new CommandInterceptor() {
                @Override
                public MocaResults intercept(MocaContext moca) throws MocaException {
                    throw new MocaException(code);
                }
            });
            
            MocaContext moca = MocaUtils.currentContext();
            
            moca.executeCommand("test execute where arg = 'foo bar'");
            fail("Should have thrown an exception!");
        }
        catch (MocaException e) {
            assertEquals(code, e.getErrorCode());
        }
        finally {
            MocaTestUtils.removeCommandOverrides();
        }
    }
    
    @Test
    public void testOverriddenCommand() throws MocaException {
        try {
            final EditableResults retRes = new SimpleResults();
            
            MocaTestUtils.overrideCommand("foo bar", new CommandInterceptor() {
                @Override
                public MocaResults intercept(MocaContext moca) throws MocaException {
                    return retRes;
                }
            });
            
            MocaContext moca = MocaUtils.currentContext();
            
            MocaResults res = moca.executeCommand("foo bar");
            assertTrue(retRes == res);
        }
        finally {
            MocaTestUtils.removeCommandOverrides();
        }
    }
    
    @Test
    public void testOverriddenCommandInStream() throws MocaException {
        final int code = 2371;
        try {
            MocaTestUtils.overrideCommand("run my command", new CommandInterceptor() {
                @Override
                public MocaResults intercept(MocaContext moca) throws MocaException {
                    throw new MocaRuntimeException(2371, "test");
                }
            });
            
            MocaContext moca = MocaUtils.currentContext();
            
            moca.executeCommand("publish data where foo = 'bar' | run my command");
        }
        catch (MocaRuntimeException e) {
            assertEquals(code, e.getErrorCode());
        }
        finally {
            MocaTestUtils.removeCommandOverrides();
        }
    }
}
