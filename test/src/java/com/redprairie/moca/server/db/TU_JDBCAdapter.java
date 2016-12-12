/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.Oracle10gDialect;
import org.hibernate.service.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.service.jdbc.dialect.internal.DialectFactoryImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.TooManyRowsException;
import com.redprairie.moca.db.hibernate.UnicodeSQLServerDialect;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.exec.ArgumentSource;
import com.redprairie.moca.server.exec.SystemContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


/**
 * Unit tests to test JDBCAdapter. Specifically the server query limit.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class TU_JDBCAdapter {
    
    @BeforeClass
    public static void beforeClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_JDBCAdapter.class.getName(), true);
    }
    
    private void createTableAndInsert(DBType dbType, Connection connection, 
        long insertCount) throws SQLException {
        DialectFactoryImpl dialectFactory = new DialectFactoryImpl();
        dialectFactory.setClassLoaderService(new ClassLoaderServiceImpl());
        
        Dialect dialect;
        switch (dbType) {
        case MSSQL:
            dialect = dialectFactory.buildDialect(Collections.singletonMap(
                AvailableSettings.DIALECT, 
                UnicodeSQLServerDialect.class.getName()),
                null);
            break;
        case ORACLE:
            dialect = dialectFactory.buildDialect(Collections.singletonMap(
                AvailableSettings.DIALECT,
                Oracle10gDialect.class.getName()), null);
            break;
        case H2:
            dialect = dialectFactory.buildDialect(Collections.singletonMap(
                AvailableSettings.DIALECT, H2Dialect.class.getName()), null);
            break;
        default:
            throw new RuntimeException("There was no database supplied!");
        }
        StringBuilder tableCreate = new StringBuilder()
            .append(dialect.getCreateTableString())
            .append(' ')
            .append(TABLENAME)
            .append(" ( name ")
            .append(dialect.getTypeName( Types.INTEGER ))
            .append(" )");
        
        Statement stmt = connection.createStatement();
        try {
            stmt.execute(tableCreate.toString());
        }
        finally {
            stmt.close();
        }
        
        PreparedStatement pstmt =  connection.prepareStatement(
            "INSERT INTO QueryLimitTable (name) VALUES(?)");
        
        try {
            for (long i = 0; i < insertCount; i++) {
                pstmt.setLong(1, i);
                pstmt.addBatch();
            }
            
            pstmt.executeBatch();
        }
        finally {
            pstmt.close();
        }
    }
    
    private void dropTable(Connection conn) throws SQLException {
        // Teardown
        Statement stmt = conn.createStatement();
        try {
            stmt.execute("drop table QueryLimitTable");
        }
        finally {
            stmt.close();
        }
    }
    
    /**
     * Test that we can successfully set the server query limit
     * to no limit.
     * 
     * @throws MocaException
     * @throws SystemConfigurationException
     * @throws SQLException
     */
    @Test
    public void testNoQueryLimit() throws MocaException,
            SystemConfigurationException, SQLException {
        // Setup
        ArgumentSource argumentSource = Mockito.mock(ArgumentSource.class);
        SystemContext context = ServerUtils.globalContext();
        context.overrideConfigurationElement(
            MocaRegistry.REGKEY_SERVER_QUERY_LIMIT, "0");
        
        JDBCAdapter jdbcAdapter = new JDBCAdapter(
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_DRIVER),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_URL),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_USERNAME),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_PASSWORD),
            context, null);
        
        MocaTransaction tx = jdbcAdapter.newTransaction();
        
        Connection conn = jdbcAdapter.getConnection(tx, null);

        createTableAndInsert(jdbcAdapter.getDBType(), conn, Long.parseLong(
            MocaRegistry.REGKEY_SERVER_QUERY_LIMIT_DEFAULT) + 1);
        try{
           
            // Test
            MocaResults res = jdbcAdapter.executeSQL(argumentSource,
                tx, "select name from QueryLimitTable", new BindList(),
                BindMode.NONE, false, null);
            assertTrue(res.getRowCount() 
                == Long.parseLong(
                    MocaRegistry.REGKEY_SERVER_QUERY_LIMIT_DEFAULT)+1);
        }
        finally {
            dropTable(conn);
            jdbcAdapter.close();
            tx.rollback();
        }
    }
    
    /**
     * Test that we can successfully query within the server query limit.
     * 
     * @throws MocaException
     * @throws SystemConfigurationException
     * @throws SQLException
     */
    @Test
    public void testWithinQueryLimit() throws MocaException,
            SystemConfigurationException, SQLException {
        // Setup
        ArgumentSource argumentSource = Mockito.mock(ArgumentSource.class);
        SystemContext context = ServerUtils.globalContext();
        context.overrideConfigurationElement(
            MocaRegistry.REGKEY_SERVER_QUERY_LIMIT, "20");

        JDBCAdapter jdbcAdapter = new JDBCAdapter(
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_DRIVER),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_URL),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_USERNAME),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_PASSWORD),
            context, null);
        
        MocaTransaction tx = jdbcAdapter.newTransaction();
        
        Connection conn = jdbcAdapter.getConnection(tx, null);

        try{
            createTableAndInsert(jdbcAdapter.getDBType(), conn, 10);
            
            // Test
            MocaResults res = jdbcAdapter.executeSQL(argumentSource,
                tx, "select name from QueryLimitTable", new BindList(),
                BindMode.NONE, false, null);
            assertTrue(res.getRowCount() == 10);
        }
        finally {
            // Teardown
            dropTable(conn);
            jdbcAdapter.close();
            tx.rollback();
        }
    }
    
    /**
     * Test that we get an exception when exceeding the server
     * query limit.
     * 
     * @throws MocaException
     * @throws SystemConfigurationException
     * @throws SQLException
     */
    @Test
    public void testExceedQueryLimit() throws MocaException,
            SystemConfigurationException, SQLException {
        // Setup
        ArgumentSource argumentSource = Mockito.mock(ArgumentSource.class);
        SystemContext context = ServerUtils.globalContext();
        context.overrideConfigurationElement(
            MocaRegistry.REGKEY_SERVER_QUERY_LIMIT, "10");

        JDBCAdapter jdbcAdapter = new JDBCAdapter(
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_DRIVER),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_URL),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_USERNAME),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_PASSWORD),
            context, null);
        
        MocaTransaction tx = jdbcAdapter.newTransaction();
        
        Connection conn = jdbcAdapter.getConnection(tx, null);

        try{
            createTableAndInsert(jdbcAdapter.getDBType(), conn, 20);
            
            // Test
            try{
                jdbcAdapter.executeSQL(argumentSource,
                tx, "select name from QueryLimitTable", new BindList(),
                BindMode.NONE, false, null);
                fail("We should have thrown the TooManyRowsException.");
            }
            catch (TooManyRowsException e) {
                //Suppress the exception. We expect it.
            }
        }
        finally {
            // Teardown
            dropTable(conn);
            jdbcAdapter.close();
            tx.rollback();
        }
    }
    
    /**
     * Test that we only return a sub sequence of rows
     * 
     * @throws MocaException
     * @throws SystemConfigurationException
     * @throws SQLException
     */
    @Test
    public void testMinAndLimit() throws MocaException,
            SystemConfigurationException, SQLException {
        // Setup
        ArgumentSource argumentSource = Mockito.mock(ArgumentSource.class);
        SystemContext context = ServerUtils.globalContext();
        context.overrideConfigurationElement(
            MocaRegistry.REGKEY_SERVER_QUERY_LIMIT, "0");

        JDBCAdapter jdbcAdapter = new JDBCAdapter(
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_DRIVER),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_URL),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_USERNAME),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_PASSWORD),
            context, null);
        
        MocaTransaction tx = jdbcAdapter.newTransaction();
        
        Connection conn = jdbcAdapter.getConnection(tx, null);

        try{
            createTableAndInsert(jdbcAdapter.getDBType(), conn, 20);
            
            int start = 5;
            int limit = 12;
            // Test
            MocaResults res = jdbcAdapter.executeSQL(argumentSource, tx,
                "/*#limit=" + start + "," + limit + "*/select name from QueryLimitTable",
                new BindList(), BindMode.NONE, false, null);
            assertEquals(limit, res.getRowCount());
            for (int i = 0; i < limit; ++i) {
                assertTrue(res.next());
                assertEquals(start + i, res.getInt(0));
            }
        }
        finally {
            // Teardown
            dropTable(conn);
            jdbcAdapter.close();
            tx.rollback();
        }
    }
    
    /**
     * Test that we only return a sub sequence of rows when available on stack
     * 
     * @throws MocaException
     * @throws SystemConfigurationException
     * @throws SQLException
     */
    @Test
    public void testMinAndLimitStackHints() throws MocaException,
            SystemConfigurationException, SQLException {
        // Setup
        int startRow = 2;
        String startRowString = "start_rowBLAH";

        int limitRow = 17;
        String limitRowString = "row_limit23";
        
        ArgumentSource argumentSource = Mockito.mock(ArgumentSource.class);
        
        MocaValue startValue = Mockito.mock(MocaValue.class);
        Mockito.when(startValue.asInt()).thenReturn(startRow);
        Mockito.when(argumentSource.getVariable(Mockito.eq(startRowString), 
            Mockito.anyBoolean())).thenReturn(startValue);
        
        MocaValue limitValue = Mockito.mock(MocaValue.class);
        Mockito.when(limitValue.asInt()).thenReturn(limitRow);
        Mockito.when(argumentSource.getVariable(Mockito.eq(limitRowString), 
            Mockito.anyBoolean())).thenReturn(limitValue);

        SystemContext context = ServerUtils.globalContext();
        context.overrideConfigurationElement(
            MocaRegistry.REGKEY_SERVER_QUERY_LIMIT, "0");

        JDBCAdapter jdbcAdapter = new JDBCAdapter(
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_DRIVER),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_URL),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_USERNAME),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_PASSWORD),
            context, null);
        
        MocaTransaction tx = jdbcAdapter.newTransaction();
        
        Connection conn = jdbcAdapter.getConnection(tx, null);

        try{
            createTableAndInsert(jdbcAdapter.getDBType(), conn, 20);
            
            // Test
            MocaResults res = jdbcAdapter.executeSQL(argumentSource, tx,
                "/*#limit=@" + startRowString + ",@" + limitRowString + "*/select name from QueryLimitTable",
                new BindList(), BindMode.NONE, false, null);
            assertEquals(limitRow, res.getRowCount());
            for (int i = 0; i < limitRow; ++i) {
                assertTrue(res.next());
                assertEquals(startRow + i, res.getInt(0));
            }
        }
        finally {
            // Teardown
            dropTable(conn);
            jdbcAdapter.close();
            tx.rollback();
        }
    }
    
    /**
     * Test that we return all rows when not available on stack
     * 
     * @throws MocaException
     * @throws SystemConfigurationException
     * @throws SQLException
     */
    @Test
    public void testMinAndLimitStackHintsNotAvailable() throws MocaException,
            SystemConfigurationException, SQLException {
        // Setup
        int maxInsert = 20;
        String startRowString = "start_rowBLAH";

        String limitRowString = "row_limit23";
        
        ArgumentSource argumentSource = Mockito.mock(ArgumentSource.class);
        
        Mockito.when(argumentSource.getVariable(Mockito.eq(startRowString), 
            Mockito.anyBoolean())).thenReturn(null);
        
        Mockito.when(argumentSource.getVariable(Mockito.eq(limitRowString), 
            Mockito.anyBoolean())).thenReturn(null);

        SystemContext context = ServerUtils.globalContext();
        context.overrideConfigurationElement(
            MocaRegistry.REGKEY_SERVER_QUERY_LIMIT, "0");

        JDBCAdapter jdbcAdapter = new JDBCAdapter(
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_DRIVER),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_URL),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_USERNAME),
            context.getConfigurationElement(MocaRegistry.REGKEY_DB_PASSWORD),
            context, null);
        
        MocaTransaction tx = jdbcAdapter.newTransaction();
        
        Connection conn = jdbcAdapter.getConnection(tx, null);

        try{
            createTableAndInsert(jdbcAdapter.getDBType(), conn, maxInsert);
            
            // Test
            MocaResults res = jdbcAdapter.executeSQL(argumentSource, tx,
                "/*#limit=@" + startRowString + ",@" + limitRowString + "*/select name from QueryLimitTable",
                new BindList(), BindMode.NONE, false, null);
            assertEquals(maxInsert, res.getRowCount());
            for (int i = 0; i < maxInsert; ++i) {
                assertTrue(res.next());
                assertEquals(i, res.getInt(0));
            }
        }
        finally {
            // Teardown
            dropTable(conn);
            jdbcAdapter.close();
            tx.rollback();
        }
    }
    
    private final String TABLENAME = "QueryLimitTable";
}
