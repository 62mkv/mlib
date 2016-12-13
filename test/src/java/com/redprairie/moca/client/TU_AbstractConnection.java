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

package com.redprairie.moca.client;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaOperator;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.NotFoundException;

/**
 * Unit tests for ComponentAdapter
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public abstract class TU_AbstractConnection extends TestCase {
    
    public static final String DEFAULT_HOST = "localhost";
    public static final String LOGIN_USER = "SUPER";
    public static final String LOGIN_PASS = "MOCA";

    public void testLargeResults() throws MocaException {
        MocaConnection conn = _getConnection();
        try {
            MocaResults res = conn.executeCommand("do loop where count = 1000 | " +
                            "publish data where x='Hello' and y = @i");
            for (int i = 0; i < 1000; i++) {
                assertTrue(res.next());
                assertEquals("Hello", res.getString("x"));
                assertEquals(i, res.getInt("y"));
            }
            assertFalse(res.next());
            res.close();
        }
        finally {
            conn.close();
        }
    }

    public void testDifferentDatatypes() throws MocaException {
        MocaConnection conn = _getConnection();
        try {
            Calendar testDate = Calendar.getInstance();
            testDate.set(Calendar.YEAR, 2004);
            testDate.set(Calendar.MONTH, Calendar.JANUARY);
            testDate.set(Calendar.DATE, 1);
            testDate.set(Calendar.HOUR_OF_DAY, 22);
            testDate.set(Calendar.MINUTE, 2);
            testDate.set(Calendar.SECOND, 3);
            testDate.set(Calendar.MILLISECOND, 0);

            for (int x = 0; x < 10; x++) {
                MocaResults res = conn.executeCommand("do loop where count = 10 | " +
                                "publish data " +
                                " where s='String value' and i = 1000 " +
                                "   and l = (1024 * 1024 * 1024) and f = 3.1415 " +
                                "   and d = date('20040101220203')");
                for (int i = 0; i < 10; i++) {
                    assertTrue(res.next());
                    assertEquals("String value", res.getString("s"));
                    assertEquals(1000, res.getInt("i"));
                    assertEquals(1024 * 1024 * 1024, res.getInt("l"));
                    assertEquals(3.1415, res.getDouble("f"));
                    assertEquals(testDate.getTime(), res.getDateTime("d"));
                }
                assertFalse(res.next());
                res.close();
            }
        }
        finally {
            conn.close();
        }
    }

    public void testNestedResults() throws MocaException {
        MocaConnection conn = _getConnection();
        try {

            for (int x = 0; x < 10; x++) {
                MocaResults res = conn.executeCommand(
                        "{do loop where count = 10 |" +
                        " publish data where i = @i and x = 'This is part ' || @i " +
                        " and y = sysdate and z = 89324.123 } >> res");
                assertTrue(res.next());
                assertNotNull(res.getValue("res"));
                MocaResults testRes = (MocaResults)res.getValue("res");
                for (int j = 0; j < 10; j++) {
                    assertTrue(testRes.next());
                    assertEquals(j, testRes.getInt("i"));
                    assertEquals("This is part " + j, testRes.getString("x"));
                    assertEquals(MocaType.DATETIME, testRes.getColumnType("y"));
                    assertEquals(89324.123, testRes.getDouble("z"), 0.001);
                }
                assertFalse(testRes.next());
                testRes.close();
                assertFalse(res.next());
                res.close();
            }
        }
        finally {
            conn.close();
        }
    }

    public void testInitiateCommandWithNotFound() throws MocaException {
        MocaConnection conn = _getConnection();
        try {
            conn.executeCommand("set return status where status = -1403");
            fail("Expected NotFoundException");
        }
        catch (NotFoundException e) {
            assertEquals(510, e.getErrorCode());
            // Normal
        }
        finally {
            conn.close();
        }
    }
    
    public void testInitiateCommandWithNotFoundAndResults() throws MocaException {
        MocaConnection conn = _getConnection();
        try {
            // This command will throw a "Not Found" exception with a result set
            conn.executeCommand("publish data where x='hello' and y = 200 and z = 3.14159 >> res | " +
                                "[[" +
                                "    while (res.next()) res.removeRow();" +
                                "    throw new com.redprairie.moca.NotFoundException(-1403, res)" +
                                "]]");
            fail("Expected NotFoundException");
        }
        catch (NotFoundException e) {
            assertNotNull(e.getResults());
            MocaResults res = e.getResults();
            assertTrue(res.containsColumn("x"));
            assertEquals(MocaType.STRING, res.getColumnType("x"));
            assertTrue(res.containsColumn("y"));
            assertEquals(MocaType.INTEGER, res.getColumnType("y"));
            assertTrue(res.containsColumn("z"));
            assertEquals(MocaType.DOUBLE, res.getColumnType("z"));
        }
        finally {
            conn.close();
        }
    }
    
    public void testInitiateCommandWithError() throws MocaException {
        MocaConnection conn = _getConnection();
        try {
            conn.executeCommand("set return status where status = 9231");
            fail("Expected NotFoundException");
        }
        catch (MocaException e) {
            assertEquals(9231, e.getErrorCode());
        }
        finally {
            conn.close();
        }
    }

    public void testInitiateCommandWithErrorMessage() throws MocaException {
        checkErrorMessage("Testing");
        checkErrorMessage("\u03A0\u03A3\u03A6\u03A8");
        checkErrorMessage("\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB");
        checkErrorMessage("This (\u00C6\u00C7\u00C8\u00C9\u00CA\u00CB) is ~= a test (\u03A0\u03A3\u03A6\u03A8\u03A0\u03A3\u03A6\u03A8\u03A0\u03A3\u03A6\u03A8\u03A0\u03A3\u03A6\u03A8\u03A0\u03A3\u03A6\u03A8\u03A0\u03A3\u03A6\u03A8).");
        checkErrorMessage("\uB0B4 \uB545\uCF69 \uBC84\uD130 \uC0CC\uB4DC\uC704\uCE58\uAC00 \uB9DB\uC788\uC2B5\uB2C8\uB2E4.");
    }
    
    public void testEnvironment() throws MocaException {
        Map<String, String> env = new HashMap<String,String>();
        env.put("TEST1", "test 1 value");
        env.put("TEST2", "test 2 value");
        MocaConnection conn = _getConnection(env);

        try {
            MocaResults res = conn.executeCommand("publish data where x=@@test1 and y = @@TEST2");
            assertTrue(res.next());
            assertEquals("test 1 value", res.getString("x"));
            assertEquals("test 2 value", res.getString("y"));
            assertFalse(res.next());
            res.close();
        }
        finally {
            conn.close();
        }
    }

    public void testLargeCommand() throws MocaException {
        char[] characterSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890~`!@#$%^&*()_-+={[}]|\\:;\"<>,.?/".toCharArray();
        StringBuilder buf = new StringBuilder(5000000);
        Random r = new Random();
        for (int i = 0; i < 5000000; i++) {
            buf.append(characterSet[r.nextInt(characterSet.length)]);
        }
        
        String element = buf.toString();

        MocaConnection conn = _getConnection();
        try {
            MocaResults res = conn.executeCommand("publish data where x = '" + element + "'");
            assertTrue(res.next());
            assertEquals(element, res.getString("x"));
            res.close();
        }
        finally {
            conn.close();
        }
    }
    
    public void testLargeNumberOfArguments() throws MocaException {
        StringBuilder command = new StringBuilder();
        command.append("publish data where ");

        for (int i = 0; i < 10000; i++) {
            if (i > 0) command.append(" and ");
            command.append('x');
            command.append(i);
            command.append(" = ");
            command.append(i);
        }
        
        MocaConnection conn = _getConnection();
        try {
            MocaResults res = conn.executeCommand(command.toString());
            assertEquals(10000, res.getColumnCount());
            for (int i = 0; i < 10000; i++) {
                assertEquals("x" + i, res.getColumnName(i).toLowerCase());
            }
    
            assertTrue(res.next());
            
            for (int i = 0; i < 10000; i++) {
                assertEquals(i, res.getInt("x" + i));
            }
            
            res.close();
        }
        finally {
            conn.close();
        }
    }
    
    public void testLargeNumberOfCommands() throws MocaException {
        MocaConnection conn = _getConnection();
        try {
            for (int i = 0; i < 10000; i++) {
                MocaResults res = conn.executeCommand("publish data where x = 'ABCDEFG" + i + "'");
                assertEquals(1, res.getColumnCount());
                assertTrue(res.next());
                assertEquals("ABCDEFG" + i, res.getString("x"));
            }
        }
        finally {
            conn.close();
        }
    }
    
    private class ExecRunner implements Callable<MocaResults> {
        
        @Override
        public MocaResults call() throws MocaException {
            MocaConnection conn = _getConnection();
            try {
                MocaResults res = null;
                for (int i = 0; i < 10; i++) {
                    res = conn.executeCommand("publish data where x = 'ABCDEFG" + i + "'");
                }
                return res;
            }
            finally {
                conn.close();
            }
        }        
    }
    
    public void testMultiThreadingClient() throws Exception {
        
        CompletionService<MocaResults> pool = new ExecutorCompletionService<MocaResults>(
                Executors.newFixedThreadPool(30));
        
        int count = 1000;
        for (int i = 0; i < count; i++) {
            pool.submit(new ExecRunner());
        }
        
        for (int i = 0; i < count; i++) {
            MocaResults res = pool.take().get();
            assertEquals(1, res.getColumnCount());
            assertTrue(res.next());
            assertEquals("ABCDEFG9", res.getString("x"));
        }
    }
    
    public void testBinaryColumn() throws MocaException {
        MocaConnection conn = _getConnection();
        try {
            MocaResults res = conn.executeCommand("do loop where count = 100 | [[ x = new byte[i%10 + 1]; y = i]]");
            for (int i = 0; i < 100; i++) {
                assertTrue(res.next());
                assertEquals(i, res.getInt("y"));
                Object value = res.getValue("x");
                assertTrue(value instanceof byte[]);
                byte[] valueArray = (byte[]) value;
                assertTrue(Arrays.equals(new byte[i%10 + 1], valueArray));
            }
            assertFalse(res.next());
            res.close();
        }
        finally {
            conn.close();
        }
    }
        
    public void testZeroLengthBinaryColumn() throws MocaException {
        MocaConnection conn = _getConnection();
        try {
            MocaResults res = conn.executeCommand("[[ x = new byte[0]]]");
            assertTrue(res.next());
            Object value = res.getValue("x");
            assertTrue(value instanceof byte[]);
            byte[] valueArray = (byte[]) value;
            assertEquals(0, valueArray.length);
            assertFalse(res.next());
            res.close();
        }
        finally {
            conn.close();
        }
    }
    
    private static class InterruptTestThread extends Thread {
        public InterruptTestThread(MocaConnection conn) {
            _conn = conn;
        }
        
        public void run() {
            try {
                // Have the server sleep for 30 seconds
                _conn.executeCommand("[[ Thread.sleep(30000); 'done']]");
            }
            catch (Exception e) {
                errorState = e;
                
                // Now check all the causes to see if there was an interrupted
                // exception in the cause list
                Throwable t = e;
                
                if (Thread.interrupted()) {
                    interrupted = true;
                }
                else {
                    do {
                        if (t instanceof InterruptedException) {
                            interrupted = true;
                        }
                    } while ((t = t.getCause()) != null);
                }
            }
        }
        
        private Throwable errorState;
        private MocaConnection _conn;
        private boolean interrupted = false;
    }
    
    public void testInterruptBehavior() throws MocaException {
        MocaConnection conn = _getConnection();
        InterruptTestThread t = new InterruptTestThread(conn);
        t.start();

        try {
            // (HACK) Wait 1s for thread to send its request 
            Thread.sleep(1000);

            // Kill the thread and wait up to 5s for it to die
            t.interrupt();
            t.join(50000);
            
            // Validate that the thread died in a reasonable way
            assertTrue(t.interrupted);
            
            // Make sure the thread got an exception when killed
            assertNotNull(t.errorState);
            assertTrue(t.errorState instanceof MocaInterruptedException);
            // The actual client will return a 204 where as server will return
            // a 531 error
            assertTrue(Arrays.asList(204, MocaInterruptedException.CODE).contains(
                    ((MocaInterruptedException) t.errorState).getErrorCode()));
        }
        catch (InterruptedException e) {
            // Not expected here
            fail("Interrupted at the wrong place");
        }
        finally {
            conn.close();
        }
    }
    
    public void testLongURLEncodedCommand() throws MocaException {
        String data = "&lt;html_msg&gt;&lt;" +
        "![CDATA[Testing the Unit of Measure and Time Zone conversions." +
        "&lt;table&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;User ID&lt;/th&gt;&lt;td&gt;@emsusr_usr_id&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Locale_ID&lt;/th&gt;&lt;td&gt;@emsusr_locale_id&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;EMS Stored Time Zone&lt;/th&gt;&lt;td&gt;@ems_StoredTimZon&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;User Display Time Zone&lt;/th&gt;&lt;td&gt;@emsusr_DisplayTimZon&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Date Time&lt;/th&gt;&lt;td&gt;@test_dt&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Date Time Conversion&lt;/th&gt;&lt;td&gt;@test_DTC&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;String&lt;/th&gt;&lt;td&gt;@test_string&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Integer&lt;/th&gt;&lt;td&gt;@test_int&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Float&lt;/th&gt;&lt;td&gt;@test_float&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Boolean&lt;/th&gt;&lt;td&gt;@test_boolean&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Medium Temperature&lt;/th&gt;&lt;td&gt;@test_uom_t&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Medium Area&lt;/th&gt;&lt;td&gt;@test_uom_a&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Medium Linear&lt;/th&gt;&lt;td&gt;@test_uom_l&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Medium Distance&lt;/th&gt;&lt;td&gt;@test_uom_dist&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Medium Volume&lt;/th&gt;&lt;td&gt;@test_uom_v&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Medium Weight&lt;/th&gt;&lt;td&gt;@test_uom_w&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Small Area&lt;/th&gt;&lt;td&gt;@test_uom_sa&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Small Linear&lt;/th&gt;&lt;td&gt;@test_uom_sl&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Small Volume&lt;/th&gt;&lt;td&gt;@test_uom_sv&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;tr&gt;&lt;th align=\\&quot;right\\&quot;&gt;Small Weight&lt;/th&gt;&lt;td&gt;@test_uom_sw&lt;/td&gt;&lt;/tr&gt;" +
        "&lt;/table&gt;" +
        "&lt;b&gt;Each one of these conversions should be filled in.&lt;/b&gt;]]&gt;" +
        "&lt;/html_msg&gt;";
        String cmd = "publish data where a = \"" + data + "\"";
        MocaConnection conn = _getConnection();
        try {
            MocaResults res = conn.executeCommand(cmd);
            assertTrue(res.next());
            String value = res.getString("a");
            assertEquals(data, value);
            assertFalse(res.next());
            res.close();
        }
        finally {
            conn.close();
        }
    }
    
    public void testCommandWithPassedContext() throws MocaException {
        MocaConnection conn = _getConnection();

        try {
            MocaResults res = conn.executeCommandWithArgs("publish data where x=@var1 and y = @var2", 
                new MocaArgument("var1", 100), new MocaArgument("var2", "Hello, World"));
            assertTrue(res.next());
            assertEquals(100, res.getInt("x"));
            assertEquals("Hello, World", res.getString("y"));
            assertFalse(res.next());
            res.close();
        }
        finally {
            conn.close();
        }
    }
    
    public void testTimeout() throws MocaException {
        MocaConnection conn = _getConnection();

        try {
            conn.setTimeout(1000);
            conn.executeCommand("[[Thread.sleep(5000)]]");
            fail("expected timeout");
        }
        catch (ConnectionTimeoutException e) {
            // Normal
        }
        finally {
            conn.close();
        }
    }

    public void testCommandWithPassedArgs() throws MocaException {
        MocaConnection conn = _getConnection();

        try {
            MocaResults res = conn.executeCommandWithContext("[[nargs = moca.args.length; name = moca.args[0].name; oper = moca.args[0].oper as String; value = moca.args[0].value as String ]]",
                new MocaArgument[] {
                    new MocaArgument("var1", 100), new MocaArgument("var2", "Hello, World")},
                new MocaArgument[] {
                    new MocaArgument("xxx", MocaOperator.NOTLIKE, MocaType.STRING, "%foo%")});
            
            assertTrue(res.next());
            assertEquals(1, res.getInt("nargs"));
            assertEquals("xxx", res.getString("name"));
            assertTrue("Expected NOTLIKE operator, got " + res.getString("oper"), res.getString("oper").equals("NOTLIKE") || res.getString("oper").equals("14"));
            assertEquals("%foo%", res.getString("value"));
            assertFalse(res.next());
            res.close();
        }
        finally {
            conn.close();
        }
    }
    
    public void testSelectSpaceString() throws MocaException {
        MocaConnection conn = _getConnection();
        try {
            MocaResults res = conn.executeCommand("[select ' ' as foo from dual]");
            assertTrue(res.next());
            assertEquals(" ", res.getString("foo"));
            assertFalse(res.next());
            res.close();
        }
        finally {
            conn.close();
        }
    }
    
    public void testUnusualColumns() throws MocaException {
        MocaConnection conn = _getConnection();
        try {
            MocaResults res = conn.executeCommand(
                "[[" +
    		" [" +
    		"   '&': 'Column 1'," +
    		"   '<': 'Column 2'," +
    		"   '>': 'Column 3'," +
    		"   '\"': 'Column 4'," +
    		"   '\\'': 'Column 5'" +
    		" ]" +
    		"]]");
            assertTrue(res.next());
            assertEquals("Column 1", res.getString("&"));
            assertEquals("Column 2", res.getString("<"));
            assertEquals("Column 3", res.getString(">"));
            assertEquals("Column 4", res.getString("\""));
            assertEquals("Column 5", res.getString("'"));
            assertFalse(res.next());
            res.close();
        }
        finally {
            conn.close();
        }
    }

    public void testNormalErrorMangling() throws MocaException {
        MocaConnection conn = _getConnection();
        try {
            MocaResults res = conn.executeCommand(
                "[[throw new NotFoundException()]]");
            fail("expected error, got " + res);
        }
        catch (NotFoundException e) {
            assertEquals(NotFoundException.SERVER_CODE, e.getErrorCode());
        }
        finally {
            conn.close();
        }
    }

    public void testRemoteErrorMangling() throws MocaException {
        MocaConnection conn = _getConnection();
        try {
            conn.setAutoCommit(false);
            conn.setRemote(true);
            MocaResults res = conn.executeCommand(
                "[[throw new NotFoundException()]]");
            fail("expected error, got " + res);
        }
        catch (NotFoundException e) {
            assertEquals(NotFoundException.DB_CODE, e.getErrorCode());
        }
        finally {
            conn.close();
        }
    }

    /*
     * 
     */
    protected MocaConnection _getConnection() throws MocaException {
        return _getConnection(null);
    }
    
    protected MocaConnection _getConnection(Map<String, String> env) throws MocaException {
        MocaConnection conn = _getConnection("localhost", env);
        ConnectionUtils.login(conn, LOGIN_USER, LOGIN_PASS, "test");
        return conn;
    }

    protected abstract MocaConnection _getConnection(String host, Map<String, String> env) throws MocaException;

    protected void checkErrorMessage(String message) throws MocaException {
        MocaConnection conn = _getConnection();
        try {
            conn.executeCommandWithArgs(
                "set return status where status = 9233 and message = @message",
                new MocaArgument("message", message));
            fail("Expected Exception");
        }
        catch (MocaException e) {
            assertEquals(9233, e.getErrorCode());
            assertEquals(message, e.getMessage());
        }
        finally {
            conn.close();
        }
    }


}
