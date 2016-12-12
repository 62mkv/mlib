/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.crud;

import org.junit.Before;
import org.junit.Test;

import com.redprairie.moca.DatabaseTool;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.crud.CrudManager.ConcurrencyMode;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * This test is basically a copy of TU_CrudService but only some methods.
 * This is to make sure the java api works the same.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_JDBCCrudManager extends TU_AbstractCrudTableCreate {
    
    @Before
    public void beforeEachTest() {
        super.beforeEachTest();
        _manager = new JDBCCrudManager(_moca);
    }
    
    @Test
    public void testChangeRecordExists() throws MocaException {
        String colnam = "yes";
        String codval = "is present";
        String change = "new";
        int version = 12;
        
        DatabaseTool db = _moca.getDb();
        // First we insert the value
        db.executeSQL(
                "insert into " + tableName + 
                " (" +
                "colnam, codval, change, u_version, int_non_null" +
                " )" +
                "  VALUES ('" +
                colnam + "','" + codval + "','not" + change + "'," + version + ", 23" +
                ")");
        
        _manager.changeRecord(tableName, false, false, ConcurrencyMode.NONE, 
                new MocaArgument("colnam", colnam),
                new MocaArgument("codval", codval),
                new MocaArgument("change", change),
                new MocaArgument("change_int", ""));
        
        MocaResults res = db.executeSQL(
                "select * from " + tableName + 
                "   where colnam = :colnam and codval = :codval", 
                new MocaArgument("colnam", colnam), 
                new MocaArgument("codval", codval));
        
        assertTrue("We should have found a single row", res.next());
        assertEquals(colnam, res.getString("colnam"));
        assertEquals(codval, res.getString("codval"));
        assertEquals("new", res.getString("change"));
        assertNull("Insertion date should have been left null", res.getDateTime("ins_dt"));
        assertNull("Insertion user id should have been left null", 
                res.getString("ins_user_id"));
        assertNotNull(res.getDateTime("last_upd_dt"));
        assertNotNull("There should have been a user id inserted", 
                res.getString("last_upd_user_id"));
        assertEquals("We just change a row so it should be 1 higher", version + 1, 
                res.getInt("u_version"));
        assertTrue("We shouldn't have insertd a value for this", 
                res.isNull("change_int"));
        assertFalse("We should have found a single row", res.next());
    }
    
    @Test
    public void testCreateRecord() throws MocaException {
        String colnam = "test";
        String codval = "the Test of your Life!";
        
        _manager.createRecord(tableName, false, 
                new MocaArgument("colnam", colnam),
                new MocaArgument("codval", codval),
                new MocaArgument("int_non_null", 23));
        
        MocaResults res = _moca.executeCommand(
                "[select * from " + tableName + 
                "   where @+colnam and @+codval]", 
                new MocaArgument("colnam", colnam), 
                new MocaArgument("codval", codval));
        
        assertTrue("We should have found a single row", res.next());
        assertEquals(colnam, res.getString("colnam"));
        assertEquals(codval, res.getString("codval"));
        assertNotNull(res.getDateTime("ins_dt"));
        assertNotNull("There should have been a user id inserted", 
                res.getString("ins_user_id"));
        assertNotNull(res.getDateTime("last_upd_dt"));
        assertNotNull("There should have been a user id inserted", 
                res.getString("last_upd_user_id"));
        assertEquals("We just created a row so it should be 1", 1, 
                res.getInt("u_version"));
        assertFalse("We should have found a single row", res.next());
    }
    
    @Test
    public void testRemoveRecord() throws MocaException {
        String colnam = "present";
        String codval = "remove present";
        
        // First we insert the value
        _moca.executeCommand(
                "[insert into " + tableName + 
                " (" +
                "colnam, codval, int_non_null" +
                " )" +
                "  VALUES ('" +
                colnam + "','" + codval + "', 23" +
                ")]");
        
        _manager.removeRecord(tableName, false, true, 
                new MocaArgument("colnam", colnam),
                new MocaArgument("codval", codval)); 
    }
    
    @Test
    public void testCreateRecordWithIdentityColumn() throws MocaException {
        String colnam = "testColumn";
        String codval = "the Test of your Lives!";
        
        _manager.createRecord(tableName, false, 
                new MocaArgument("rowid", 1),
                new MocaArgument("colnam", colnam),
                new MocaArgument("codval", codval),
                new MocaArgument("int_non_null", 25));
        
        MocaResults res = _moca.executeCommand(
                "[select * from " + tableName + 
                "   where @+colnam and @+codval]", 
                new MocaArgument("rowid", 1),
                new MocaArgument("colnam", colnam), 
                new MocaArgument("codval", codval));
        
        assertTrue("We should have found a single row", res.next());
        assertEquals(colnam, res.getString("colnam"));
        assertEquals(codval, res.getString("codval"));
        assertNotNull(res.getDateTime("ins_dt"));
        assertNotNull("There should have been a user id inserted", 
                res.getString("ins_user_id"));
        assertNotNull(res.getDateTime("last_upd_dt"));
        assertNotNull("There should have been a user id inserted", 
                res.getString("last_upd_user_id"));
        assertEquals("We just created a row so it should be 1", 1, 
                res.getInt("u_version"));
        assertFalse("We should have found a single row", res.next());
    }
    
    
    private JDBCCrudManager _manager;
}
