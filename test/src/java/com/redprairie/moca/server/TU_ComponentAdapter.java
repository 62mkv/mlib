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

package com.redprairie.moca.server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaArgumentException;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaRuntimeException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.server.exec.MocaScriptException;
import com.redprairie.moca.util.MocaUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit tests for MOCA Command Processing behavior.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_ComponentAdapter {
       
    // @see junit.framework.TestCase#setUp()
    @BeforeClass
    public static void setupContext() throws Exception {
        ServerUtils.setupDaemonContext(TU_ComponentAdapter.class.getName(), true);
    }
    
    @Test
    public void testInitiateCommand() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("publish data where x='Hello'");
        assertTrue(res.next());
        assertEquals("Hello", res.getString("x"));
        assertFalse(res.next());
        res.close();
    }

    @Test
    public void testInitiateCommandReturningObject() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("[[x = [a:'Hello']; y = 'something']]");
        assertTrue(res.next());
        Object xValue = res.getValue("x");
        assertTrue(xValue instanceof Map<?, ?>);
        assertEquals("Hello", ((Map<?, ?>)xValue).get("a"));
        assertFalse(res.next());
        res.close();
    }

    @Test
    public void testInitiateCommandWithError() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            ctx.executeCommand("set return status where status = -1403");
            fail("Expected NotFoundException");
        }
        catch (NotFoundException e) {
            assertNull(e.getResults());
        }
    }
    

    @Test
    public void testInitiateCommandWithNestedResults() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("publish data where x = 'hello' and y = 'goodbye' >> res");
        assertTrue(res.containsColumn("res"));
        assertFalse(res.containsColumn("x"));
        assertFalse(res.containsColumn("y"));
        assertTrue(res.next());
        MocaResults inner = res.getResults("res"); 
        assertNotNull(inner);
        assertTrue(inner.containsColumn("x"));
        assertTrue(inner.containsColumn("y"));
        assertTrue(inner.next());
        assertEquals("hello", inner.getString("x"));
        assertEquals("goodbye", inner.getString("y"));
        assertFalse(inner.next());
        assertFalse(res.next());
        res.close();
    }
    
    @Test
    public void testInitiateCommandWithErrorArgs() {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            ctx.executeCommand(
                    "set return status " +
                    "where status = 90210 " +
                    "  and message = 's is ^s^, i is ^i^, f is ^f^' " +
                    "  and s = 'bar' and i = 200 and f = 3.14159");
            fail("Expected MocaException");
        }
        catch (MocaException e) {
            assertNull(e.getResults());
            assertEquals(90210, e.getErrorCode());
            assertEquals("bar", e.getArgValue("s"));
            assertEquals(Integer.valueOf(200), e.getArgValue("i"));
            assertEquals(Double.valueOf(3.14159), e.getArgValue("f"));
        }
    }
    
    @Test
    public void testInitiateCommandWithRuntimeErrorArgs() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            try {
                ctx.executeCommand(
                        "set return status " +
                        "where status = 90210 " +
                        "  and message = 's is ^s^, i is ^i^, f is ^f^' " +
                        "  and s = 'bar' and i = 200 and f = 3.14159");
                fail("Expected MocaException");
            }
            catch (MocaException e) {
                throw new MocaRuntimeException(e);
            }
        }
        catch (MocaRuntimeException e) {
            assertNull(e.getResults());
            assertEquals(90210, e.getErrorCode());
            assertEquals("bar", e.getArgValue("s"));
            assertEquals(Integer.valueOf(200), e.getArgValue("i"));
            assertEquals(Double.valueOf(3.14159), e.getArgValue("f"));
        }
    }
    
    @Test
    public void testInitiateCommandReturningNoRows() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("[select 'a' a, 'b' b, 123 c from dual where 1=0] catch (-1403)");
        assertEquals(MocaType.STRING, res.getColumnType("a"));
        assertEquals(MocaType.STRING, res.getColumnType("b"));
        assertTrue(res.getColumnType("c") == MocaType.INTEGER || res.getColumnType("c") == MocaType.DOUBLE);
    }
    
    @Test
    public void testInitiateCommandThrowsNotFound() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            ctx.executeCommand("[select 'a' a, 'b' b, 123 c from dual where 1=0]");
            fail("expected error");
        }
        catch (NotFoundException e) {
            MocaResults res = e.getResults();
            assertEquals(MocaType.STRING, res.getColumnType("a"));
            assertEquals(MocaType.STRING, res.getColumnType("b"));
            assertTrue(res.getColumnType("c") == MocaType.INTEGER || res.getColumnType("c") == MocaType.DOUBLE);
        }
    }
    
    @Test
    public void testInitiateCommandThrowsMocaRuntimeException() {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            ctx.executeCommand("[[throw new MocaRuntimeException(9999, 'ABCD')]]");
            fail("expected error");
        }
        catch (MocaException e) {
            assertEquals(9999, e.getErrorCode());
            assertEquals("ABCD", e.getMessage());
            assertFalse(e.isMessageResolved());
        }
    }
    
    @Test
    public void testInitiateCommandThrowsMocaRuntimeExceptionWrappingResolvedMessage() {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            ctx.executeCommand("[[throw new MocaRuntimeException(" +
            		"new com.redprairie.moca.test.ResolvedMessageException(9999, 'ABCD'))]]");
            fail("expected error");
        }
        catch (MocaException e) {
            assertEquals(9999, e.getErrorCode());
            assertEquals("ABCD", e.getMessage());
//  TODO    assertTrue(e.isMessageResolved());
        }
    }
    
    @Test
    public void testInitiateCommandWithArgs() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("a", "Hello");
        args.put("b", 1000);
        args.put("c", 3.14159);
        MocaResults res = ctx.executeCommand(
                "publish data where x = @a and y = @b and z = @c", args);
        assertTrue(res.next());
        assertEquals("Hello", res.getString("x"));
        assertEquals(1000, res.getInt("y"));
        assertEquals(3.14159, res.getDouble("z"), 0.0);
        assertFalse(res.next());
    }
    
    @Test
    public void testInitiateCommandWithObjectArg() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        Map<String, Object> args = new HashMap<String, Object>();
        Object testObject = new Object();
        args.put("a", testObject);
        MocaResults res = ctx.executeCommand(
                "publish data where x = @a", args);
        assertTrue(res.next());
        assertSame(testObject, res.getValue("x"));
        assertFalse(res.next());
    }
    
    @Test
    public void testGetConnection() throws MocaException, SQLException {
        MocaContext ctx = MocaUtils.currentContext();
        
        Connection conn = ctx.getDb().getConnection();
        assertFalse(conn.getAutoCommit());
        
        Statement s = null;
        ResultSet res = null;
        try {
            s = conn.createStatement();
            res = s.executeQuery("select 'Hello' foo from dual");
            assertTrue(res.next());
            assertEquals("Hello", res.getString("foo"));
        }
        finally {
            if (res != null) {
                try {
                    res.close();
                }
                catch (SQLException e) {
                    System.err.println("There was a problem closing the result");
                }
            }
            if (s != null) {
                try {
                    s.close();
                }
                catch (SQLException e) {
                    System.err.println("There was a problem closing the statement");
                }
            }
        }
    }

    @Test
    public void testInitiateScript() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
            "[[" +
            "map = new java.util.HashMap();" +
            "map.put('x', 'foo');" +
            "str = 'Hello, world';" +
            "i = 10;" +
            "f = 3.55" +
            "]]");
        assertEquals(MocaType.STRING, res.getColumnType("str"));
        assertEquals(MocaType.INTEGER, res.getColumnType("i"));
        assertEquals(MocaType.DOUBLE, res.getColumnType("f"));
        assertEquals(MocaType.OBJECT, res.getColumnType("map"));
        assertTrue(res.next());
        assertEquals(10, res.getInt("i"));
        assertEquals(3.55, res.getDouble("f"), 0.01);
        assertEquals("Hello, world", res.getString("str"));
        Object mapObj = res.getValue("map");
        assertTrue(mapObj instanceof HashMap<?,?>);
        Map<?, ?> map = (Map<?, ?>) mapObj;
        assertEquals(1, map.size());
        assertEquals("foo", map.get("x"));
    }
    
    @Test
    public void testInitiateScriptWithPipes() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
            "[[" +
            "map = new java.util.HashMap();" +
            "map.put('x', 'foo');" +
            "str = 'Hello, world';" +
            "i = 10;" +
            "f = 3.55;" +
            "]] | " +
            "[[y=map.get('x')]]");
        assertEquals(MocaType.STRING, res.getColumnType("y"));
        assertTrue(res.next());
        assertEquals("foo", res.getString("y"));
    }
    
    @Test
    public void testGetResultsVariable() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
            "publish data where x = 'hello' and y = 'goodbye' >> res | " +
            "[[" +
            "    import com.redprairie.moca.*;" +
            "    res.addColumn('z', MocaType.STRING);" +
            "    while (res.next()) {" +
            "        res.setValue('z', 'value_of_z');" +
            "    };" +
            "    res;" +
            "]]");
        assertEquals(MocaType.STRING, res.getColumnType("x"));
        assertEquals(MocaType.STRING, res.getColumnType("y"));
        assertEquals(MocaType.STRING, res.getColumnType("z"));
        assertTrue(res.next());
        assertEquals("hello", res.getString("x"));
        assertEquals("goodbye", res.getString("y"));
        assertEquals("value_of_z", res.getString("z"));
        assertFalse(res.next());
    }

    @Test
    public void testThrowExceptionFromScript() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            ctx.executeCommand(
                "[[" +
                "    import com.redprairie.moca.*;" +
                "    throw new MocaException(222, 'Error 222');" +
                "]]");
            fail("Expected Exception");
        }
        catch (MocaException e) {
            assertEquals(222, e.getErrorCode());
        }
    }

    @Test
    public void testScriptWithContext() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
            "[[" +
            "    import com.redprairie.moca.*;" +
            "    moca.executeInline(\"publish data where x = 'Hello'\");" +
            "]]");
        assertTrue(res.next());
        assertEquals("Hello", res.getString("x"));
        assertFalse(res.next());
    }

    @Test
    public void testSQLErrorFromScript() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            ctx.executeCommand(
                "[[" +
                "    import com.redprairie.moca.*;" +
                "    moca.executeInline(\"[select 'x' from dual where 1=0]\");" +
                "]]");
            fail("Expected Exception");
        }
        catch (NotFoundException e) {
            // OK
        }
    }

    @Test
    public void testBadlyFormedScript() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            ctx.executeCommand(
                "[[" +
                "    importPackage(com.redprairie.moca);" +
                "    moca.executeInline(\"[select \"x\" from dual where 1=0]\");" +
                "]]");
            fail("Expected Exception");
        }
        catch (MocaException e) {
            assertEquals(MocaScriptException.CODE, e.getErrorCode());
        }
    }

    @Test
    public void testScriptWithMissingVariable() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            ctx.executeCommand(
                "[[x = not_here + 2]]");
            fail("Expected Exception");
        }
        catch (MocaException e) {
            assertEquals(MocaScriptException.CODE, e.getErrorCode());
        }
    }

    @Test
    public void testScriptWithNullOutput() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("[[null]]");
        assertFalse(res.next());
    }
    
    @Test
    public void testScriptWithNullColumn() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("[[x = null;]]");
        assertTrue(res.next());
        assertTrue(res.containsColumn("x"));
        assertNull(res.getValue("x"));
    }

    @Test
    public void testScriptResultOrder() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("[[x = 'foo'; y = 'bar']]");
        assertTrue(res.containsColumn("x"));
        assertTrue(res.containsColumn("y"));
        assertEquals(0, res.getColumnNumber("x"));
        assertEquals(1, res.getColumnNumber("y"));
        assertTrue(res.next());
        assertEquals("foo", res.getValue("x"));
        assertEquals("bar", res.getValue("y"));
    }
    
    @Test
    public void testSysdateFunction() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("publish data where x = sysdate");
        assertTrue(res.next());
        assertEquals(MocaType.DATETIME, res.getColumnType("x"));
    }
    
    @Test
    public void testVariableReferences() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("publish data where x = 'Hello' and y = 'World' | if (@x#onstack) publish data where result = @x || ', ' || @y#keep");
        assertTrue(res.next());
        assertEquals(MocaType.STRING, res.getColumnType("result"));
        assertEquals("Hello, World", res.getString("result"));
    }
    
    @Test
    public void testDateFunction() throws MocaException {
        // Set up a simulated date object.
        Calendar cal = Calendar.getInstance();
        cal.set(2008, 6, 11, 15, 30, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        _testFunction(cal.getTime(), "date('20080711153000')");
        _testNullFunction(MocaType.DATETIME, "date(null)");
    }
    
    @Test
    public void testStringFunction() throws MocaException {
        _testFunction("abcd", "string('abcd')");
        _testFunction("1000", "string(1000)");
        _testFunction("13.3", "string(13.3)");
        _testFunction("20080711153000", "string(date('20080711153000'))");
        _testNullFunction(MocaType.STRING, "string(null)");
    }
    
    @Test
    public void testIntFunction() throws MocaException {
        _testFunction(100, "int('100')");
        _testFunction(-1, "int(3.3 - 4.3)");
        _testFunction(0, "int(-0.3)");
        _testFunction(0, "int(0.3)");
        _testNullFunction(MocaType.INTEGER, "int(null)");
    }
    
    @Test
    public void testFloatFunction() throws MocaException {
        _testFunction(100.0, "float('100')");
        _testFunction(-1.0, "float(3 - 4)");
        _testFunction(-0.3, "float(-0.3)");
        _testFunction(0.3, "float(0.3)");
        _testNullFunction(MocaType.DOUBLE, "float(null)");
    }
    
    @Test
    public void testUpperFunction() throws MocaException {
        _testFunction("100", "upper(100)");
        _testFunction("ABCD", "upper('abcd')");
        _testFunction("HELLO", "upper('Hello')");
        _testNullFunction(MocaType.STRING, "upper(null)");
    }
    
    @Test
    public void testLowerFunction() throws MocaException {
        _testFunction("100", "lower(100)");
        _testFunction("abcd", "lower('abcd')");
        _testFunction("hello", "lower('Hello')");
        _testNullFunction(MocaType.STRING, "lower(null)");
    }
    
    @Test
    public void testTrimFunction() throws MocaException {
        _testFunction("100", "trim(100)");
        _testFunction("abcd", "trim(\"abcd \")");
        _testFunction("\t\tNo Whitespace", "TRIM('\t\tNo Whitespace\r\n')");
        _testNullFunction(MocaType.STRING, "trim(null)");
        _testFunction("100", "rtrim(100)");
        _testFunction("wxyz", "rtrim(\"wxyz\t \")");
        _testFunction("\t\tNo Whitespace", "rTRIM('\t\tNo Whitespace\r\n')");
        _testNullFunction(MocaType.STRING, "Rtrim(null)");
    }
    
    @Test
    public void testSubstrFunction() throws MocaException {
        _testFunction("el", "substr('Hello', 2, 2)");
        _testFunction("llo", "substr('Hello', 3)");
        _testFunction("", "substr('Hello', 6, 1)");
        _testNullFunction(MocaType.STRING, "substr(null, 2, 3)");
    }
    
    @Test
    public void testSprintfFunction() throws MocaException {
        _testFunction("blah0003", "sprintf('%.5s%04d', 'blah', 3)");
        _testFunction("xxx/9/3.2", "sprintf('%s/%d/%3.1f', 'xxx', 9, 3.2)");
    }
    
    @Test
    public void testMinMaxFunction() throws MocaException {
        _testFunction(100, "min(100, 1000, 2903.2)");
        _testFunction(-2903.2, "min(100, -1000, -2903.2)");
        _testFunction(100, "max(100, -1000, -2903.2)");
        _testFunction(2903.2, "max(100, 1000, 2903.2)");
    }
    
    @Test
    public void testTrueFalseFunctions() throws MocaException {
        _testFunction(true, "true");
        _testFunction(false, "false");
    }
    
    @Test
    public void testIifFunction() throws MocaException {
        _testFunction(100, "iif(@x is null, 100, 200)");
        _testFunction("b", "iif(false, 'a', 'b')");
    }
    
    @Test
    public void testNvlFunction() throws MocaException {
        _testFunction("----", "nvl(@x, '----')");
        _testFunction("a", "nvl(null, nvl(@x, 'a'))");
        _testFunction("b", "nvl('b', 'x')");
    }
    
    @Test
    public void testReferenceArguments() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("noop where x = 'Hello' and y = 'World' | publish data where @+x and @+foo^y and y = 2");
        assertTrue(res.next());
        assertEquals("Hello", res.getValue("x"));
        assertEquals("World", res.getValue("foo"));
        assertEquals(2, res.getValue("y"));
    }
    
    @Test
    public void testCatchExpression() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
                "set return status " +
                "where status = 90210 catch(90210)");
    
        assertFalse(res.next());
    }
    
    @Test
    public void testCatchExpressionWithWildcard() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
                "set return status " +
                "where status = 90210 catch(@?)");
    
        assertFalse(res.next());
    }
    
    @Test
    public void testCatchLastError() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
                "set return status " +
                "where status = 90210 catch(@?) | publish data where x = @?");
    
        assertTrue(res.next());
        assertEquals(90210, res.getInt("x"));
    }
    
    @Test
    public void testCatchMultipleErrors() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
                "set return status " +
                "where status = 90210 catch(-1403, 90210, 510) | publish data where x = @?");
    
        assertTrue(res.next());
        assertEquals(90210, res.getInt("x"));
    }
    
    @Test
    public void testCatchWithNestedTest() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
                "set return status " +
                "where status = 90210 catch(@?) | " +
                "if (@? = 0) publish data where x = 'OK' " +
                "else if (@? = -1403) publish data where x = 'NOT FOUND' " +
                "else publish data where x = 'ERR' || @?");
    
        assertTrue(res.next());
        assertEquals("ERR90210", res.getString("x"));
    }
    
    @Test
    public void testCatchWithinLoop() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
            "do loop where count = 4 |" +
            "if (@? = 0) {" +
            "    publish data where blah = 'blah' | " +
            "    [[" +
            "        if (i == 3) throw new MocaException(90210)" +
            "        else z = 'Hello ' + i" +
            "    ]]  catch (@?)" +
            "} |" +
            "publish data where a = @z and b = @?");
        assertTrue(res.next());
        assertEquals("Hello 0", res.getString("a"));
        assertEquals(0, res.getInt("b"));
        assertTrue(res.next());
        assertEquals("Hello 1", res.getString("a"));
        assertEquals(0, res.getInt("b"));
        assertTrue(res.next());
        assertEquals("Hello 2", res.getString("a"));
        assertEquals(0, res.getInt("b"));
        assertTrue(res.next());
        assertNull(res.getString("a"));
        assertEquals(90210, res.getInt("b"));
        assertFalse(res.next());
    }

    @Test
    public void testCatchWithinLoopWithErrorAtStartOfInnerBlock() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
            "do loop where count = 4 |" +
            "if (@? = 0) {" +
            "    [[" +
            "        if (i == 0) throw new MocaException(90210)" +
            "        else z = 'Hello ' + i" +
            "    ]]  catch (@?) | publish data where z = @z" +
            "} |" +
            "publish data where a = @z and b = @?");
        assertTrue(res.next());
        assertNull(res.getString("a"));
        assertEquals(0, res.getInt("b"));
        assertTrue(res.next());
        assertEquals("Hello 1", res.getString("a"));
        assertEquals(0, res.getInt("b"));
        assertTrue(res.next());
        assertEquals("Hello 2", res.getString("a"));
        assertEquals(0, res.getInt("b"));
        assertTrue(res.next());
        assertEquals("Hello 3", res.getString("a"));
        assertEquals(0, res.getInt("b"));
        assertFalse(res.next());
    }
    
    @Test
    public void testCatchAsEntireBodyOfLoop() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
            "do loop where count = 4 |" +
            "if (@? = 0) {" +
            "    [[" +
            "        if (i == 0) throw new MocaException(90210)" +
            "        else z = 'Hello ' + i" +
            "    ]]  catch (@?)" +
            "}");
        assertTrue(res.next());
        assertEquals("Hello 1", res.getString("z"));
        assertTrue(res.next());
        assertEquals("Hello 2", res.getString("z"));
        assertTrue(res.next());
        assertEquals("Hello 3", res.getString("z"));
        assertFalse(res.next());
    }

    @Test
    public void testCatchWithSemicolonSeparatedLoopBodyWithPipes() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        Map<String, Integer> stats = new HashMap<String, Integer>();
        stats.put("a", 0);
        stats.put("b", 0);
        MocaResults res = ctx.executeCommand(
            "do loop where count = 4 |" +
            "if (@? = 0) {" +
            "    [[" +
            "        stats.a++;" +
            "        if (i == 0) throw new MocaException(90210)" +
            "        else z = 'Hello ' + i" +
            "    ]]  catch (@?) |" +
            "    if (@? = 0) {" +
            "        publish data where a = 'A' || @i " +
            "    };" +
            "    [[" +
            "        stats.b++;" +
            "        if (i == 1) throw new MocaException(90210)" +
            "        else z = 'Hello ' + i" +
            "    ]]  catch (@?) |" +
            "    if (@? = 0) {" +
            "        publish data where b = 'B' || @i " +
            "    }" +
            "}", new MocaArgument("stats", stats));
        assertEquals(4, stats.get("a").intValue());
        assertEquals(4, stats.get("b").intValue());
        assertTrue(res.next());
        assertEquals("B0", res.getString("b"));
        assertTrue(res.next());
        assertEquals("B2", res.getString("b"));
        assertTrue(res.next());
        assertEquals("B3", res.getString("b"));
        assertFalse(res.next());
    }

    @Test
    public void testCatchWithSemicolonSeparatedLoopBody() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        Map<String, Integer> stats = new HashMap<String, Integer>();
        stats.put("a", 0);
        stats.put("b", 0);
        MocaResults res = ctx.executeCommand(
            "do loop where count = 4 |" +
            "if (@? = 0) {" +
            "    [[" +
            "        stats.a++;" +
            "        if (i == 0) throw new MocaException(90210)" +
            "        else z = 'Hello ' + i" +
            "    ]]  catch (@?);" +
            "    [[" +
            "        stats.b++;" +
            "        if (i == 1) throw new MocaException(90210)" +
            "        else z = 'Foo ' + i" +
            "    ]]  catch (@?)" +
            "}", new MocaArgument("stats", stats));
        assertEquals(4, stats.get("a").intValue());
        assertEquals(4, stats.get("b").intValue());
        assertTrue(res.next());
        assertEquals("Foo 0", res.getString("z"));
        assertTrue(res.next());
        assertEquals("Foo 2", res.getString("z"));
        assertTrue(res.next());
        assertEquals("Foo 3", res.getString("z"));
        assertFalse(res.next());
    }

    @Test
    public void testTryCatch() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
                "try { " +
                "  set return status " +
                "  where status = 90210 " +
                "} catch(@?) {" +
                "  publish data where x = 'ERR' || @? " +
                "}");
    
        assertTrue(res.next());
        assertEquals("ERR90210", res.getString("x"));
    }
    
    @Test
    public void testErrorInFinally() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
                "try { " +
                "  set return status " +
                "  where status = 90210 " +
                "} catch(@?) {" +
                "  publish data where x = 'ERR' || @? " +
                "} | publish data where a = @x and b = @?");
    
        assertTrue(res.next());
        assertEquals("ERR90210", res.getString("a"));
        assertEquals(90210, res.getInt("b"));
    }
    
    @Test
    public void testCatchInBraces() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
                "{ " +
                "  set return status " +
                "  where status = 90210 catch (@?)" +
                "} | " +
                "{" +
                "  publish data where x = 'ERR' || @? " +
                "}");
    
        assertTrue(res.next());
        assertEquals("ERR90210", res.getString("x"));
    }
    
    @Test
    public void testTryCatchFinally() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
                "[[ out = [:];null]] | " +
                "try { " +
                "  [[ out.a = 'main' ]];" +
                "  set return status " +
                "  where status = 90210 and out = @out " +
                "} catch(@?) {" +
                "  [[out.a += 'catch']];" +
                "  publish data where x = 'ERR' || @? and out = @out" +
                "}" +
                "finally {" +
                "    [[out.a += 'finally']];" +
                "    publish data where x = 'NEVER' and out = @out" +
                "}");
    
        assertTrue(res.next());
        assertEquals("ERR90210", res.getString("x"));
        @SuppressWarnings("unchecked")
        Map<String, String> outMap = (Map<String, String>)res.getValue("out");
        assertNotNull(outMap);
        assertEquals("maincatchfinally", outMap.get("a"));
    }
    

    @Test
    public void testPublishWithIsNullArg() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("publish data where x is null");
        assertTrue(res.containsColumn("x"));
        assertTrue(res.next());
        assertNull(res.getString("x"));
        assertFalse(res.next());
        res.close();
    }
    
    @Test
    public void testAggregationWhereClause() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand(
            "publish data where X = 'HELLO' & " +
            "publish data where X = 'YES, ' || @X");
        assertTrue(res.next());
        assertEquals("HELLO", res.getString("x"));
        assertTrue(res.next());
        assertEquals("YES, ", res.getString("x"));
        assertFalse(res.next());

        res = ctx.executeCommand(
            "publish data where X = 'HELLO' & {" +
            "    if (@x = 'HELLO') publish data where X = 'YES'" +
            "}");
        assertTrue(res.next());
        assertEquals("HELLO", res.getString("x"));
        assertFalse(res.next());
    }
    
    @Test
    public void testInvalidArgumentTypeToSQL() {
        MocaContext ctx = MocaUtils.currentContext();
        
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("x", 'c');
        
        try {
            MocaResults res = ctx.getDb().executeSQL("select @x x from dual", args);
            fail("Expected exception, got " + res);
        }
        catch (MocaException e) {
            assertEquals(MocaArgumentException.CODE, e.getErrorCode());
        }
    }

    @Test
    public void testSQLWithWhereClause() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        
        try {
            MocaResults res = ctx.executeCommand("[select 'x' from dual where @*] where [1=0]");
            fail("Expected exception, got " + res);
        }
        catch (NotFoundException e) {
            // Normal
        }
    }

    @Test
    public void testCatchWithIfTest() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("set return status where status = 40 catch (@?) |" +
        		"if (@? = 40) {publish data where x= 100}");
        assertTrue(res.next());
        assertEquals(100, res.getInt("x"));
    }
    
    @Test
    public void testSQLExecuteWithBindVariables() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.getDb().executeSQL("select :aaa foo from dual", new MocaArgument("aaa", "value"));
        assertTrue(res.next());
        assertEquals("value", res.getString("foo"));
    }
    
    @Test
    public void testSQLExecuteWithUpperCaseBindVariables() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.getDb().executeSQL("select :AAA foo from dual", new MocaArgument("AAA", "value"));
        assertTrue(res.next());
        assertEquals("value", res.getString("foo"));
    }
    
    @Test
    public void testSQLExecuteWithReusedBindVariables() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.getDb().executeSQL("select :aaa foo, upper(:aaa) bar from dual", new MocaArgument("aaa", "value"));
        assertTrue(res.next());
        assertEquals("value", res.getString("foo"));
        assertEquals("VALUE", res.getString("bar"));
    }
    
    @Test
    public void testSQLExecuteWithOverstackedBindVariables() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.getDb().executeSQL("select :aaa foo from dual", new MocaArgument("aaa", "avalue"), new MocaArgument("bbb", "bvalue"));
        assertTrue(res.next());
        assertEquals("avalue", res.getString("foo"));
    }
    
    @Test
    public void testSQLExecuteWithMissingBindVariables() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        try {
            MocaResults res = ctx.getDb().executeSQL("select :aaa foo from dual", new MocaArgument("bbb", "bvalue"));
            fail("Expected exception, got " + res.toString());
        }
        catch (MocaException e) {
            // Expecting some error, possibly db-specific.
        }
    }
    
    @Test
    public void testRequestAttribute() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        Object value = new Object();
        String name = "TEST.TEST";
        ctx.setRequestAttribute(name, value);
        try {
            MocaResults res = ctx.executeCommand("[[xxx = moca.getRequestAttribute(name)]]", new MocaArgument("name", name));
            assertNotNull(res);
            assertTrue(res.next());
            assertSame(value, res.getValue("xxx"));
        }
        finally {
            ctx.removeRequestAttribute("TEST.TEST");
        }
    }
    
    @Test
    public void testRemoveRequestAttribute() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        Object value = new Object();
        String name = "TEST.TEST";
        ctx.setRequestAttribute(name, value);
        try {
            ctx.executeCommand("[[moca.removeRequestAttribute(name)]]", new MocaArgument("name", name));
            assertNull(ctx.getRequestAttribute(name));
        }
        finally {
            ctx.removeRequestAttribute("TEST.TEST");
        }
    }
    
    //
    // Test utility methods
    //
    private void _testFunction(Object expected, String functionCall) throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("publish data where x = " + functionCall);
        assertTrue(res.next());
        assertEquals(expected, res.getValue("x"));
    }

    private void _testNullFunction(MocaType expectedType, String functionCall) throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("publish data where x = " + functionCall);
        assertTrue(res.next());
        assertEquals(expectedType, res.getColumnType("x"));
        assertNull(res.getValue("x"));
    }
}
