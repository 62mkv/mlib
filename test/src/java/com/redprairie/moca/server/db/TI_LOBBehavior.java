/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

import java.util.Arrays;
import java.util.Random;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * Test for evaluating expected results from SQL statements.
 */
public class TI_LOBBehavior extends AbstractMocaTestCase {

    public void testCharacterLOB() throws MocaException {
        // Set up CLOB column
        
        String dbType = _moca.getDbType();
        String tableName = "MOCA_TEST_CLOB_TABLE";

        // Make a big String...
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            builder.append("Hello, World...");
        }
        String bigString = builder.toString();
        
        switch (DBType.valueOf(dbType)) {
        case ORACLE:
            populateLob(_moca, tableName, "varchar2(100)", "CLOB", MocaType.STRING, bigString);
            break;
        case MSSQL:
            populateLob(_moca, tableName, "nvarchar(100)", "nvarchar(MAX)", MocaType.STRING, bigString);
            break;
        default:
            break;
        }
        
        // Make sure we can fetch our CLOB the next time around.
        MocaResults res = _moca.executeSQL("select a, b, substr(b, 1, 100) z from " + tableName);
        
        assertTrue(res.next());
        assertEquals("XXXX0000", res.getString("a"));
        assertEquals(bigString, res.getString("b"));
        assertEquals(bigString.substring(0, 100), res.getString("z"));

        assertTrue(res.next());
        assertEquals("XXXX0001", res.getString("a"));
        assertTrue(res.isNull("b"));
        assertNull(res.getString("b"));
        assertFalse(res.next());

        dropLob(_moca, tableName);
    }
    
    public void testBinaryLOB() throws MocaException {
        // Set up CLOB column
        
        String dbType = _moca.getDbType();
        String tableName = "MOCA_TEST_BLOB_TABLE";

        // Make a chunk of data...
        Random rand = new Random();
        byte[] bigData = new byte[10000];
        rand.nextBytes(bigData);
        
        switch (DBType.valueOf(dbType)) {
        case ORACLE:
            populateLob(_moca, tableName, "varchar2(100)", "BLOB", MocaType.BINARY, bigData);
            break;
        case MSSQL:
            populateLob(_moca, tableName, "nvarchar(100)", "image", MocaType.BINARY, bigData);
            break;
        default:
            break;
        }
        
        // Make sure we can fetch our BLOB the next time around.
        MocaResults res = _moca.executeSQL("select a, b from " + tableName);
        
        assertTrue(res.next());
        assertEquals("XXXX0000", res.getString("a"));
        assertTrue(Arrays.equals(bigData, (byte[])res.getValue("b")));

        assertTrue(res.next());
        assertEquals("XXXX0001", res.getString("a"));
        assertTrue(res.isNull("b"));
        assertNull(res.getValue("b"));
        assertFalse(res.next());
        
        dropLob(_moca, tableName);
    }
    
    private void populateLob(MocaContext moca, String tableName, String pkType, String lobType, MocaType valueType, Object value) throws MocaException {
        try {
            moca.executeSQL("/*noconv*/DROP TABLE " + tableName);
        }
        catch (MocaException e) {
            // Ignore does not exist error
        }
            
        moca.executeSQL("/*noconv*/create table " + tableName + " (A " + pkType +"not null primary key, B " + lobType + ")");

        // Insert a few rows of that String as a CLOB.
        moca.executeSQL("/*noconv*/insert into " + tableName + " (a, b) values(:a,:b)",
            new MocaArgument("a", "XXXX0000"), new MocaArgument("b", valueType, value));

        // Insert a few rows of that String as a CLOB.
        moca.executeSQL("/*noconv*/insert into " + tableName + " (a, b) values(:a,:b)",
            new MocaArgument("a", "XXXX0001"), new MocaArgument("b", valueType, null));
    }
    
    private void dropLob(MocaContext moca, String tableName) throws MocaException {
        try {
            moca.executeSQL("/*noconv*/DROP TABLE " + tableName);
        }
        catch (MocaException e) {
            // Ignore does not exist error
        }
    }

}