/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
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

package com.redprairie.moca.server.db.translate;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import oracle.jdbc.OraclePreparedStatement;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.redprairie.moca.MocaType;
import com.redprairie.moca.pool.Validator;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.BindMode;
import com.redprairie.moca.server.db.MissingVariableException;
import com.redprairie.moca.server.db.SQLBinder;

/**
 * Various Unit Tests for the Oracle translator (auto-bind)
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_OracleDialect extends TU_AbstractDialect {

    // @see
    // com.redprairie.moca.server.db.translate.TU_AbstractDialect#getDialect()
    @Override
    protected BaseDialect getDialect() {
        return new OracleDialect();
    }

    // @see
    // com.redprairie.moca.server.db.translate.TU_AbstractDialect#dialectSetUp()
    @Override
    protected void dialectSetUp() throws Exception {
        super.dialectSetUp();

        // We disable the comments for Oracle because we don't ever remove them
        _putComments = false;
    }

    public void testTrivialTranslations() throws TranslationException {
        _runTest("", "");
        _runTest("\t\n\r", "\t\n\r");
    }

    public void testSelectFromDual() throws TranslationException {
        BindList args = new BindList();
        _runTest("select 'x' from dual", "select :q0 from dual", args);
        assertTrue(args.contains("q0"));

    }

    public void testSubquery() throws TranslationException {
        BindList args = new BindList();
        String orig = "select 'x' "
                + " from dual "
                + "where exists "
                + "(select 'x' "
                + "   from sl_retr_mthd_impl_def rmid, "
                + "        sl_retr_mthd_def rmd, "
                + "  sl_eo_seg es, "
                + "        sl_eo_def eo, "
                + "        sl_ifd_def ifd "
                + "  where exists (select 'x' "
                + "                  from sl_eo_col ec "
                + "                 where ec.eo_id = es.eo_id "
                + "                   and ec.eo_ver = es.eo_ver "
                + "                   and ec.eo_seg_id = es.eo_seg_id "
                + "                   and ec.retr_mthd_impl_genid = rmid.retr_mthd_impl_genid "
                + "                   and ec.retr_mthd_id = rmid.retr_mthd_id)  "
                + "    and ((rmid.sys_id = 'sys_id') or "
                + "          (rmid.ifd_id = 'ifd_id' and rmid.ifd_ver = 'ifd_id'))  "
                + "    and rmid.retr_mthd_id = rmd.retr_mthd_id "
                + "    and rmd.retr_mthd_id = es.retr_mthd_id "
                + "    and es.eo_id = eo.eo_id "
                + "    and es.eo_ver  = eo.eo_ver "
                + "    and eo.eo_id = ifd.eo_id "
                + "    and eo.eo_ver = ifd.eo_ver "
                + "    and ifd.ifd_id = 'ifd_id' "
                + "    and ifd.ifd_ver = 'ifd_ver' " + " ) ";
        String expected = "select :q0 "
                + " from dual "
                + "where exists "
                + "(select :q1 "
                + "   from sl_retr_mthd_impl_def rmid, "
                + "        sl_retr_mthd_def rmd, "
                + "  sl_eo_seg es, "
                + "        sl_eo_def eo, "
                + "        sl_ifd_def ifd "
                + "  where exists (select :q2 "
                + "                  from sl_eo_col ec "
                + "                 where ec.eo_id = es.eo_id "
                + "                   and ec.eo_ver = es.eo_ver "
                + "                   and ec.eo_seg_id = es.eo_seg_id "
                + "                   and ec.retr_mthd_impl_genid = rmid.retr_mthd_impl_genid "
                + "                   and ec.retr_mthd_id = rmid.retr_mthd_id)  "
                + "    and ((rmid.sys_id = :q3) or "
                + "          (rmid.ifd_id = :q4 and rmid.ifd_ver = :q5))  "
                + "    and rmid.retr_mthd_id = rmd.retr_mthd_id "
                + "    and rmd.retr_mthd_id = es.retr_mthd_id "
                + "    and es.eo_id = eo.eo_id "
                + "    and es.eo_ver  = eo.eo_ver "
                + "    and eo.eo_id = ifd.eo_id "
                + "    and eo.eo_ver = ifd.eo_ver "
                + "    and ifd.ifd_id = :q6 " + "    and ifd.ifd_ver = :q7 "
                + " ) ";

        _runTest(orig, expected, args);
        assertEquals("x", args.getValue("q0"));
        assertEquals("x", args.getValue("q1"));
        assertEquals("x", args.getValue("q2"));
        assertEquals("sys_id", args.getValue("q3"));
        assertEquals("ifd_id", args.getValue("q4"));
        assertEquals("ifd_id", args.getValue("q5"));
        assertEquals("ifd_id", args.getValue("q6"));
        assertEquals("ifd_ver", args.getValue("q7"));
    }

    public void testUnbindParams() throws TranslationException {
        BindList args = new BindList();
        TranslationOptions opt = new TranslationOptions(BindMode.UNBIND);
        args.add("var0", "hello");
        args.add("var1", -43);
        args.add("var2", 7.2);
        args.add("var3", "bob's your uncle's ''friend''");
        _runTest(
            "select 'x' a, :var0 b, func(:var1) c from foo where a = :var2 and b = :var3",
            "select 'x' a, 'hello' b, func(-43) c from foo where a = 7.2 and b = 'bob''s your uncle''s ''''friend'''''",
            args, opt);
    }

    public void testUnbindWithOutParams() throws TranslationException {
        BindList args = new BindList();
        TranslationOptions opt = new TranslationOptions(BindMode.UNBIND);
        args.add("n", MocaType.INTEGER, -2291);
        args.add("x", MocaType.STRING_REF, null);

        _runTest("begin :x := sqlerrm ( :n ); end;",
            "begin :x := sqlerrm ( -2291 ); end;", args, opt);
    }

    public void testOutParameter() throws TranslationException {
        BindList args = new BindList();
        args.add("x", MocaType.STRING_REF, null);
        args.add("n", MocaType.INTEGER, -2291);
        _runTest("begin :x := sqlerrm ( :n ); end;",
            "begin :x := sqlerrm ( :n ); end;", args);
        assertTrue(args.contains("x"));
        assertTrue(args.contains("n"));
    }

    /***
     * Unit test to test Oracles SQLException translation with Deadlock
     */
    @Test
    public void testTranslateDeadLockSQLException() {
        SQLException exception = Mockito.mock(SQLException.class);
        Mockito.when(exception.getErrorCode()).thenReturn(OracleDialect.ORACLE_DEADLOCK);
        Mockito.when(exception.getMessage()).thenReturn(
            "Test Deadlock exception");
        Mockito.when(exception.getSQLState()).thenReturn("Deadlock");
        OracleDialect oracleDialect = new OracleDialect();
        SQLException newException = oracleDialect
            .translateSQLException(exception);

        assertEquals(newException.getErrorCode(), BaseDialect.STD_DEADLOCK_CODE);
        assertEquals(newException.getMessage(), exception.getMessage());
        assertEquals(newException.getSQLState(), exception.getSQLState());

    }

    /***
     * Unit test to test Oracles SQLException translation with Unique
     */
    @Test
    public void testTranslateUniqueSQLException() {
        SQLException exception = Mockito.mock(SQLException.class);
        Mockito.when(exception.getErrorCode()).thenReturn(OracleDialect.ORACLE_UNIQUE_CONS);
        Mockito.when(exception.getMessage()).thenReturn("Unique exception");
        Mockito.when(exception.getSQLState()).thenReturn("Unique");
        OracleDialect oracleDialect = new OracleDialect();
        SQLException newException = oracleDialect
            .translateSQLException(exception);

        assertEquals(newException.getErrorCode(),
            BaseDialect.STD_UNIQUE_CONS_CODE);
        assertEquals(newException.getMessage(), exception.getMessage());
        assertEquals(newException.getSQLState(), exception.getSQLState());

    }

    /***
     * Unit test to test Oracles SQLException translation with LockTimeout
     */
    @Test
    public void testTranslateLockTimeoutSQLException() {
        SQLException exception = Mockito.mock(SQLException.class);
        Mockito.when(exception.getErrorCode()).thenReturn(OracleDialect.ORACLE_LOCK_TIMEOUT);
        Mockito.when(exception.getMessage()).thenReturn("Unique exception");
        Mockito.when(exception.getSQLState()).thenReturn("Unique");
        OracleDialect oracleDialect = new OracleDialect();
        SQLException newException = oracleDialect
            .translateSQLException(exception);

        assertEquals(newException.getErrorCode(),
            BaseDialect.STD_LOCK_TIMEOUT_CODE);
        assertEquals(newException.getMessage(), exception.getMessage());
        assertEquals(newException.getSQLState(), exception.getSQLState());

    }

    /***
     * Unit test to test any other SQLException
     */
    @Test
    public void testTranslateOtherSQLException() {
        SQLException exception = Mockito.mock(SQLException.class);
        Mockito.when(exception.getErrorCode()).thenReturn(80);
        Mockito.when(exception.getMessage()).thenReturn("Unknown exception");
        Mockito.when(exception.getSQLState()).thenReturn("Unknown");
        OracleDialect oracleDialect = new OracleDialect();
        SQLException newException = oracleDialect
            .translateSQLException(exception);
        assertEquals(newException, exception);
        assertEquals(newException.getSQLState(), exception.getSQLState());

    }

    /***
     * Unit test to verify the getSequenceValue prepare call
     * 
     * @throws SystemConfigurationException
     * @throws SQLException
     */
    @Test
    public void testGetSequenceValue() throws SystemConfigurationException,
            SQLException {

        Connection conn = Mockito.mock(Connection.class);
        CallableStatement stmt = Mockito.mock(CallableStatement.class);
        Mockito.when(stmt.execute()).thenReturn(true);
        Mockito.when(stmt.getString(Mockito.anyInt())).thenReturn(
            "TestSequence");

        Mockito.when(conn.prepareCall(Mockito.anyString())).thenReturn(stmt);

        OracleDialect oracleDialect = new OracleDialect();

        String verifySequenceValue = oracleDialect.getSequenceValue("test", conn);

        InOrder inOrder = Mockito.inOrder(conn, stmt);

        inOrder.verify(conn).prepareCall("begin select test.nextval into ? from dual; end;");

        inOrder.verify(stmt).registerOutParameter(1,12);
        inOrder.verify(stmt).execute();
        inOrder.verify(stmt).getString(1);
        inOrder.verify(stmt).close();
        
        assertEquals(verifySequenceValue, "TestSequence");

    }

    
    /***
     * Unit test to verify the proper handling of SQLExceptions on execute.
     * 
     * @throws SystemConfigurationException
     * @throws SQLException
     */
    @Test
    public void testGetSequenceValueException()
            throws SystemConfigurationException, SQLException {

        Connection conn = Mockito.mock(Connection.class);
        CallableStatement stmt = Mockito.mock(CallableStatement.class);
        
        Mockito.when(conn.prepareCall(Mockito.anyString())).thenReturn(stmt);

        SQLException sqlE= new SQLException();
        Mockito.when(stmt.execute()).thenThrow(sqlE);

        OracleDialect oracleDialect = new OracleDialect();
        try {
            oracleDialect.getSequenceValue("test", conn);

            fail("Should have thrown a SQLException!");
        }
        catch (SQLException e) {
            assertEquals(e, sqlE);
        }
        
        Mockito.verify(stmt).close();
    }

    
    /***
     * 
     * 
     * @throws SystemConfigurationException
     * @throws MissingVariableException
     * @throws SQLException
     */
    @Test
    public void testOraclePrepareStatement()
            throws SystemConfigurationException, MissingVariableException,
            SQLException {
        String query = "select * from dual where x = :x_no";
        OraclePreparedStatement stmt = Mockito
            .mock(OraclePreparedStatement.class);
        Connection conn = Mockito.mock(Connection.class);
        Mockito.when(
            conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY)).thenReturn(stmt);

        BindList args = new BindList();
        
        args.add("x_no", MocaType.STRING, "x");
        SQLBinder binder = new SQLBinder(query, args);
    
        OracleDialect oracleDialect = new OracleDialect();
        OraclePreparedStatement returnStatement = 
            (OraclePreparedStatement) oracleDialect.prepareStatement(
                conn, binder, args);
        
        Mockito.verify(returnStatement).setObjectAtName("x_no", "x");
    }

    
    /***
     * 
     * Unit test to verify that Dates are being properly handled as strings during
     * prepare statement.
     * 
     * @throws SystemConfigurationException
     * @throws SQLException
     * @throws MissingVariableException
     */
    @Test
    public void testOracleDateTypePrepareStatement()
            throws SystemConfigurationException, SQLException, MissingVariableException {
        String query = "select * from dual where x = :x_no";
        OraclePreparedStatement stmt = Mockito
            .mock(OraclePreparedStatement.class);
        Connection conn = Mockito.mock(Connection.class);
        Mockito.when(
            conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY)).thenReturn(stmt);

        BindList args = new BindList();
        
        Date now = new Date();
        args.add("x_no", MocaType.DATETIME, now);
        SQLBinder binder = new SQLBinder(query, args);
    
        OracleDialect oracleDialect = new OracleDialect();
        OraclePreparedStatement returnStatement = (OraclePreparedStatement) oracleDialect.prepareStatement(conn, binder, args);
        
        InOrder inOrder = Mockito.inOrder(returnStatement);
        
        inOrder.verify(returnStatement).setStringAtName("x_no", new DateTime(now.getTime()).toString(
        "YYYYMMddHHmmss"));

    }

    
    /***
     * 
     * Unit test to test the throwing of a missing bind variable
     * exception.
     * 
     * @throws SystemConfigurationException
     * @throws SQLException
     */
    @Test
    public void testMissingVariableException()
            throws SystemConfigurationException, SQLException {
        String query = "select * from dual where x = :x_no";
        OraclePreparedStatement stmt = Mockito
            .mock(OraclePreparedStatement.class);
        Connection conn = Mockito.mock(Connection.class);
        Mockito.when(
            conn.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY)).thenReturn(stmt);

        BindList args = new BindList();
        args.add("x_no", "x");
        SQLBinder binder = new SQLBinder(query, args);
        args = new BindList();
        OracleDialect oracleDialect = new OracleDialect();
        try {
            oracleDialect.prepareStatement(conn, binder, args);
            fail("We should have received a MissingVariableException here.");
        }
        catch (MissingVariableException e) {
            // We want this to be thrown here
        }

    }

    /***
     * Unit test to verify the return of a OraclePoolListener
     */
    @Test
    public void testOraclePoolListener() {
        OracleDialect oracleDialect = new OracleDialect();
        Validator<Connection> validator = oracleDialect.getValidator(null);
        assertTrue(validator instanceof OracleValidator);
    }

}
