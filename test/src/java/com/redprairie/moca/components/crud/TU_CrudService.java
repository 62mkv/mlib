/*
 *  $URL$
 *  $Revision$
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

package com.redprairie.moca.components.crud;

import java.util.Date;

import org.joda.time.DateTime;
import org.junit.Test;

import com.redprairie.moca.DatabaseTool;
import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.crud.EmptyArgumentException;
import com.redprairie.moca.crud.TU_AbstractCrudTableCreate;
import com.redprairie.moca.exceptions.MissingArgumentException;
import com.redprairie.moca.exceptions.MissingPKException;
import com.redprairie.moca.exceptions.UniqueConstraintException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * This class tests various commands from the crud package
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_CrudService extends TU_AbstractCrudTableCreate {
    
    /**
     * This method is to test a specific command if it will throw the desired
     * error code to test JUNITs.  It just simply executes the command in the
     * current MOCA context and if a MocaException is tossed it will catch it
     * and see if the value is equal to the error number provided.  If it is
     * different then the test will fail, the same if the command executes
     * successfully.
     * 
     * This is copied from AbstractMocaTestCase
     * 
     * @param command - (Required) name of command to error.
     * @param error - (Required) int with value of error code to be returned.
     */
    protected void testCommandForError(String command, int error)
    {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(command);
            fail("Command succeeded -- expected error code " + error);
        }
        catch (MocaException e) {
            assertEquals("Expected error code", error, e.getErrorCode());
        }
        finally {
            if (res != null) {
                res.close();
            }
        }
    }

    @Test
    public void testValidateStackVariableNotNullNullValue() throws MocaException {
        testCommandForError(
                "publish data where test = ''" +
        	"|" +
        	"validate stack variable not null" +
        	"  where name  = 'test'", 802);
    }
    
    @Test
    public void testValidateStackVariableNotNullAbsentValue() {
        testCommandForError(
                "validate stack variable not null" +
                "  where name  = 'test'", 802);
    }
    
    @Test
    public void testValidateStackVariableNotNullOrAbsentNullValue() {
        testCommandForError(
                "publish data where test = ''" + 
                "|" + 
                "validate stack variable not null or absent" +
                "  where name  = 'test'", 802);
    }
    
    @Test
    public void testValidateStackVariableNotNullOrAbsentAbsentValue() throws MocaException {
        _moca.executeCommand(
                "validate stack variable not null or absent" + 
                "  where name  = 'test'");
    }
    
    @Test
    public void testValidateFlag() throws MocaException {
        _moca.executeCommand(
                "publish data" +
                "  where flag = '0'" +
                "|" +
                "validate flag" + 
                "  where flagnam = 'flag'");
    }
    
    @Test
    public void testValidateFlagForInvalidFormat() throws MocaException {
        testCommandForError(
                "publish data" +
                "  where flag = '2'" +
                "|" +
                "validate flag" + 
                "  where flagnam = 'flag'", 2965);
    }
    
    @Test
    public void testValidateFlagForNoArgument() throws MocaException {
        testCommandForError("validate flag", 507);
    }
    
    @Test
    public void testValidateDate() throws MocaException {
        _moca.executeCommand(
                "publish data" +
                "  where date = '20081228120000'" +
                "|" +
                "validate date" + 
                "  where dtenam = 'date'");
    }
    
    @Test
    public void testValidateDateForInvalidFormat() throws MocaException {
        testCommandForError(
                "publish data" +
                "  where date = '20081228X120000'" +
                "|" +
                "validate date" + 
                "  where dtenam = 'date'", 2961);
    }
    
    @Test
    public void testValidateDateForNoArgument() throws MocaException {
        testCommandForError("validate date", 507);
    }
    
    @Test
    public void testCreateRecord() throws MocaException {
        String colnam = "test";
        String codval = "the Test of your Life!";
        _moca.executeCommand(
                "publish data" +
                "  where colnam = '" + colnam + "'" +
                "    and codval = '" + codval +  "'" +
                "    and int_non_null = 23" +
                "|" +
                "create record" +
                "  where table = '" + tableName + "'");
        
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
    public void testCreateRecordWithDateOnStack() throws MocaException {
        String colnam = "Holiday";
        String codval = "Christmas";
        Date christmas2008 = new DateTime(2008, 12, 25, 00, 00, 00, 00).toDate();
        _moca.executeCommand(
                "publish data" +
                "  where colnam = '" + colnam + "'" +
                "    and codval = '" + codval +  "'" +
                "    and int_non_null = 23" +
                "    and change_dt = date('20081225000000')" +
                "    and change_int = null" +
                "|" +
                "create record" +
                "  where table = '" + tableName + "'");
        
        MocaResults res = _moca.executeCommand(
                "[select * from " + tableName + 
                "   where @+colnam and @+codval]", 
                new MocaArgument("colnam", colnam), 
                new MocaArgument("codval", codval));
        
        assertTrue("We should have found a single row", res.next());
        assertEquals(colnam, res.getString("colnam"));
        assertEquals(codval, res.getString("codval"));
        assertEquals(christmas2008, res.getDateTime("change_dt"));
        assertNotNull(res.getDateTime("ins_dt"));
        assertNotNull("There should have been a user id inserted", 
                res.getString("ins_user_id"));
        assertNotNull(res.getDateTime("last_upd_dt"));
        assertNotNull("There should have been a user id inserted", 
                res.getString("last_upd_user_id"));
        assertEquals("We just created a row so it should be 1", 1, 
                res.getInt("u_version"));
        assertTrue("We shouldn't have insertd a value for this", 
                res.isNull("change_int"));
        assertFalse("We should have found a single row", res.next());
    }
    
    @Test
    public void testCreateRecordWithDateStringOnStack() throws MocaException {
        String colnam = "Holiday";
        String codval = "NewYear";
        Date newYear2009 = new DateTime(2009, 1, 1, 00, 00, 00, 00).toDate();
        _moca.executeCommand(
                "publish data" +
                "  where colnam = '" + colnam + "'" +
                "    and codval = '" + codval +  "'" +
                "    and int_non_null = 23" +
                "    and change_dt = '20090101000000'" +
                "|" +
                "create record" +
                "  where table = '" + tableName + "'");
        
        MocaResults res = _moca.executeCommand(
                "[select * from " + tableName + 
                "   where @+colnam and @+codval]", 
                new MocaArgument("colnam", colnam), 
                new MocaArgument("codval", codval));
        
        assertTrue("We should have found a single row", res.next());
        assertEquals(colnam, res.getString("colnam"));
        assertEquals(codval, res.getString("codval"));
        assertEquals(newYear2009, res.getDateTime("change_dt"));
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
    public void testCreateRecordWithDateEmptyStringOnStack() throws MocaException {
        String colnam = "Change";
        String codval = "Change-22";
        _moca.executeCommand(
                "publish data" +
                "  where colnam = '" + colnam + "'" +
                "    and codval = '" + codval +  "'" +
                "    and int_non_null = 23" +
                "    and change_dt = ''" +
                "|" +
                "create record" +
                "  where table = '" + tableName + "'");
        
        MocaResults res = _moca.executeCommand(
                "[select * from " + tableName + 
                "   where @+colnam and @+codval]", 
                new MocaArgument("colnam", colnam), 
                new MocaArgument("codval", codval));
        
        assertTrue("We should have found a single row", res.next());
        assertEquals(colnam, res.getString("colnam"));
        assertEquals(codval, res.getString("codval"));
        assertNull("The date object should be null", res.getDateTime("change_dt"));
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
    public void testCreateRecordWithIntegerOnStackInsteadOfString() throws MocaException {
        String colnam = "Change";
        String codval = "Change-23";
        String changeString = "23";
        _moca.executeCommand(
                "publish data" +
                "  where colnam = '" + colnam + "'" +
                "    and codval = '" + codval +  "'" +
                "    and int_non_null = 23" +
                // NOTE this is passed as an int since it has no quotes
                "    and change = " + changeString +
                "|" +
                "create record" +
                "  where table = '" + tableName + "'");
        
        MocaResults res = _moca.executeCommand(
                "[select * from " + tableName + 
                "   where @+colnam and @+codval]", 
                new MocaArgument("colnam", colnam), 
                new MocaArgument("codval", codval));
        
        assertTrue("We should have found a single row", res.next());
        assertEquals(colnam, res.getString("colnam"));
        assertEquals(codval, res.getString("codval"));
        assertEquals(changeString, res.getString("change"));
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
    public void testCreateRecordWithStringOnStackInsteadOfInteger() throws MocaException {
        String colnam = "Change";
        String codval = "Change-24";
        int changeInt = 24;
        _moca.executeCommand(
                "publish data" +
                "  where colnam = '" + colnam + "'" +
                "    and codval = '" + codval +  "'" +
                "    and int_non_null = 23" +
                // NOTE this is passed as an string since it has quotes
                "    and change_int = '" + changeInt + "'" +
                "|" +
                "create record" +
                "  where table = '" + tableName + "'");
        
        MocaResults res = _moca.executeCommand(
                "[select * from " + tableName + 
                "   where @+colnam and @+codval]", 
                new MocaArgument("colnam", colnam), 
                new MocaArgument("codval", codval));
        
        assertTrue("We should have found a single row", res.next());
        assertEquals(colnam, res.getString("colnam"));
        assertEquals(codval, res.getString("codval"));
        assertEquals(changeInt, res.getInt("change_int"));
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
    public void testCreateRecordWithNullOnStackForNotNullValueWithDefault() throws MocaException {
        String colnam = "Change";
        String codval = "Change-25";
        _moca.executeCommand(
                "publish data" +
                "  where colnam = '" + colnam + "'" +
                "    and codval = '" + codval +  "'" +
                "    and int_non_null = 23" +
                // NOTE this is passed as null
                "    and def_flg = null " +
                "|" +
                "create record" +
                "  where table = '" + tableName + "'");
        
        MocaResults res = _moca.executeCommand(
                "[select * from " + tableName + 
                "   where @+colnam and @+codval]", 
                new MocaArgument("colnam", colnam), 
                new MocaArgument("codval", codval));
        
        assertTrue("We should have found a single row", res.next());
        assertEquals(colnam, res.getString("colnam"));
        assertEquals(codval, res.getString("codval"));
        // This should have defaulted to 1
        assertEquals(1, res.getInt("def_flg"));
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
    public void testCreateRecordWithSpecialArgOnStack() throws MocaException {
        String colnam = "LastUpdUserId";
        String codval = "Change";
        _moca.executeCommand(
                "publish data" +
                "  where colnam = '" + colnam + "'" +
                "    and codval = '" + codval +  "'" +
                "    and int_non_null = 23" +
                // NOTE this is a special argument to crud
                "    and last_upd_user_id = 'SUPER'" +
                "|" +
                "create record" +
                "  where table = '" + tableName + "'");
        
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
    public void testCreateRecordMissingPk() throws MocaException {
        try {
            _moca.executeCommand(
                    "publish data" +
                    "  where colnam = 'test'" +
                    "|" +
                    "create record" +
                    "  where table = '" + tableName + "'");
            fail("We should have thrown a MissingPKException");
        }
        catch (MissingPKException e) {
            // Should go here
        }
    }
    
    @Test
    public void testCreateRecordDuplicate() throws MocaException {
        _moca.executeCommand(
                "publish data" +
                "  where colnam = 'test2'" +
                "    and codval = 'The real Test of your Life!'" +
                "    and int_non_null = 23" +
                "|" +
                "create record" +
                "  where table = '" + tableName + "'");
        
        try {
            _moca.executeCommand("publish data" +
                "  where colnam = 'test2'" +
                "    and codval = 'The real Test of your Life!'" +
                "|" +
                "create record" +
                "  where table = '" + tableName + "'" +
                "    and int_non_null = 23");
            fail("We should have thrown a UniqueConstraintException");
        }
        catch (UniqueConstraintException e) {
            // We should have thrown this.
        }
    }
    
    @Test
    public void testChangeRecordNoPk() throws MocaException {
        testCommandForError(
                "change record" +
                "  where table = '" + tableName + "'" +
                "    and change = 'new'", 2967);
    }
    
    @Test
    public void testChangeRecordNoRow() throws MocaException {
        try {
            _moca.executeCommand(
                    "change record" +
                    "  where table = '" + tableName + "'" +
                    "    and colnam = 'no'" +
                    "    and codval = 'testChangeRecordNoRow'" +
                    "    and change = 'new'");
            fail("Should have got a NotFoundException");
        }
        catch (NotFoundException e) {
            // We expect this
        }
    }
    
    @Test
    public void testChangeRecordNoRowButInsert() throws MocaException {
        String colnam = "no";
        String codval = "testChangeRecordNoRowButInsert";
        _moca.executeCommand(
                "change record" +
                "  where table = '" + tableName + "'" +
                "    and colnam = '" + colnam + "'" +
                "    and codval = '" + codval + "'" +
                "    and int_non_null = 23" +
                "    and change = 'new'" +
                "    and force_update = 0");
        
        MocaResults res = _moca.executeCommand(
                "[select * from " + tableName + 
                "   where @+colnam and @+codval]", 
                new MocaArgument("colnam", colnam), 
                new MocaArgument("codval", codval));
        
        assertTrue("We should have found a single row", res.next());
        assertEquals(colnam, res.getString("colnam"));
        assertEquals(codval, res.getString("codval"));
        assertEquals("new", res.getString("change"));
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
        
        _moca.executeCommand(
                "change record" +
                "  where table = '" + tableName + "'" +
                "    and colnam = '" + colnam + "'" +
                "    and codval = '" + codval + "'" +
                "    and change = '" + change + "'" +
                "    and change_int = ''");
        
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
    public void testChangeRecordWithEmptyStringOnStackForNotNullColumnWithDefault() throws MocaException {
        String colnam = "not-null";
        String codval = "def_flg";
        String change = "new";
        int version = 12;
        
        DatabaseTool db = _moca.getDb();
        // First we insert the value
        db.executeSQL(
                "insert into " + tableName + 
                " (" +
                "colnam, codval, change, u_version, def_flg, int_non_null" +
                " )" +
                "  VALUES ('" +
                colnam + "','" + codval + "','not" + change + "'," + version + ", 23, 23" +
                ")");
        
        _moca.executeCommand(
                "change record" +
                "  where table = '" + tableName + "'" +
                "    and colnam = '" + colnam + "'" +
                "    and codval = '" + codval + "'" +
                "    and change = '" + change + "'" +
                // Note this is an empty string
                "    and def_flg = ''");
        
        MocaResults res = db.executeSQL(
                "select * from " + tableName + 
                "   where colnam = :colnam and codval = :codval", 
                new MocaArgument("colnam", colnam), 
                new MocaArgument("codval", codval));
        
        assertTrue("We should have found a single row", res.next());
        assertEquals(colnam, res.getString("colnam"));
        assertEquals(codval, res.getString("codval"));
        assertEquals(change, res.getString("change"));
        // This should have stayed the same
        assertEquals(23, res.getInt("def_flg"));
        assertNull("Insertion date should have been left null", res.getDateTime("ins_dt"));
        assertNull("Insertion user id should have been left null", 
                res.getString("ins_user_id"));
        assertNotNull(res.getDateTime("last_upd_dt"));
        assertNotNull("There should have been a user id inserted", 
                res.getString("last_upd_user_id"));
        assertEquals("We just change a row so it should be 1 higher", version + 1, 
                res.getInt("u_version"));
        assertFalse("We should have found a single row", res.next());
    }
    
    @Test
    public void testChangeRecordWithWhitespaceStringOnStack() throws MocaException {
        String colnam = "whitespace";
        String codval = "def_flg";
        String change = "new";
        int version = 12;
        
        DatabaseTool db = _moca.getDb();
        // First we insert the value
        db.executeSQL(
                "insert into " + tableName + 
                " (" +
                "colnam, codval, change, u_version, def_flg, int_non_null" +
                " )" +
                "  VALUES ('" +
                colnam + "','" + codval + "','not" + change + "'," + version + ", 23, 23" +
                ")");
        
        String whitespaceChange = " \t\r\n";
        _moca.executeCommand(
                "change record" +
                "  where table = '" + tableName + "'" +
                "    and colnam = '" + colnam + "'" +
                "    and codval = '" + codval + "'" +
                "    and change = '" + whitespaceChange + "'" +
                // Note this is an empty string
                "    and def_flg = ''");
        
        MocaResults res = db.executeSQL(
                "select * from " + tableName + 
                "   where colnam = :colnam and codval = :codval", 
                new MocaArgument("colnam", colnam), 
                new MocaArgument("codval", codval));
        
        assertTrue("We should have found a single row", res.next());
        assertEquals(colnam, res.getString("colnam"));
        assertEquals(codval, res.getString("codval"));
        assertEquals(whitespaceChange, res.getString("change"));
        // This should have stayed the same
        assertEquals(23, res.getInt("def_flg"));
        assertNull("Insertion date should have been left null", res.getDateTime("ins_dt"));
        assertNull("Insertion user id should have been left null", 
                res.getString("ins_user_id"));
        assertNotNull(res.getDateTime("last_upd_dt"));
        assertNotNull("There should have been a user id inserted", 
                res.getString("last_upd_user_id"));
        assertEquals("We just change a row so it should be 1 higher", version + 1, 
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
        
        _moca.executeCommand(
                "remove record" +
                "  where table = '" + tableName + "'" +
                "    and colnam = '" + colnam + "'" +
                "    and codval = '" + codval + "'");
    }
    
    @Test
    public void testRemoveRecordNotExists() throws MocaException {
        String colnam = "remove";
        String codval = "remove not present";
        try {
            _moca.executeCommand(
                    "remove record" +
                    "  where table = '" + tableName + "'" +
                    "    and colnam = '" + colnam + "'" +
                    "    and codval = '" + codval + "'");
            fail("Should have got a NotFoundException");
        }
        catch (NotFoundException e) {
            // We expect this
        }
    }
    
    @Test
    public void testRemoveRecordNotAllPks() throws MocaException {
        String colnam = "remove";
        try {
            _moca.executeCommand(
                    "remove record" +
                    "  where table = '" + tableName + "'" +
                    "    and colnam = '" + colnam + "'");
            fail("Should have got a MissingPkException");
        }
        catch (MissingPKException e) {
            // We expect this
        }
    }
    
    @Test
    public void testValidateDataExists() throws MocaException {
        String colnam = "present";
        String codval = "exists";
        
        // First we insert the value
        _moca.executeCommand(
                "[insert into " + tableName + 
                " (" +
                "colnam, codval, int_non_null" +
                " )" +
                "  VALUES ('" +
                colnam + "','" + codval + "', 23" +
                ")]");
        
        _moca.executeCommand(
                "publish data" +
                "  where colnam = '" + colnam + "'" +
                "    and codval = '" + codval + "'" +
                "|" +
                "validate data" +
                "  where tblnam = '" + tableName + "'" +
                "    and fldnam = 'codval, colnam'");
    }
    
    @Test
    public void testValidateDataDoesExistsNotExpected() throws MocaException {
        String colnam = "present";
        String codval = "exists2";
        
        // First we insert the value
        _moca.executeCommand(
                "[insert into " + tableName + 
                " (" +
                "colnam, codval, int_non_null" +
                " )" +
                "  VALUES ('" +
                colnam + "','" + codval + "', 23" +
                ")]");
        
        try {
            _moca.executeCommand(
                    "publish data" +
                    "  where colnam = '" + colnam + "'" +
                    "    and codval = '" + codval + "'" +
                    "|" +
                    "validate data" +
                    "  where tblnam = '" + tableName + "'" +
                    "    and fldnam = 'codval, colnam'" +
                    "    and val_type = 'RES'");    
            fail("We expected a PrimaryKeyExistsException");
        }
        catch (PrimaryKeyExistsException e) {
            // We should go here
        }
    }
    
    @Test
    public void testValidateDataDoesNotExist() throws MocaException {
        String colnam = "present";
        String codval = "no-exists";
        
        try {
            _moca.executeCommand(
                    "publish data" +
                    "  where colnam = '" + colnam + "'" +
                    "    and codval = '" + codval + "'" +
                    "|" +
                    "validate data" +
                    "  where tblnam = '" + tableName + "'" +
                    "    and fldnam = 'codval, colnam'");
            fail("We should have got an InvalidValueException");
        }
        catch (InvalidValueException e) {
            // We should have got this
        }
    }
    
    @Test
    public void testValidateDataDoesNotExistExpected() throws MocaException {
        String colnam = "present";
        String codval = "no-exists";
        
        _moca.executeCommand(
                "publish data" +
                "  where colnam = '" + colnam + "'" +
                "    and codval = '" + codval + "'" +
                "|" +
                "validate data" +
                "  where tblnam = '" + tableName + "'" +
                "    and fldnam = 'codval, colnam'" +
                "    and val_type = 'RES'");
    }
    
    @Test
    public void testValidateDataExistsNotOnStack() throws MocaException {
        try {
            _moca.executeCommand(
                    "validate data" +
                    "  where tblnam = '" + tableName + "'" +
                    "    and fldnam = 'codval, colnam'");
            fail("Should have got a MissingArgumentException");
        }
        catch (MissingArgumentException e) {
            // We expect this.
        }
    }
    
    @Test
    public void testValidateDataExistsOnStackButEmpty() throws MocaException {
        try {
            _moca.executeCommand(
                    "publish data" +
                    "  where codval = ''" +
                    "|" +
                    "validate data" +
                    "  where tblnam = '" + tableName + "'" +
                    "    and fldnam = 'codval'");
            fail("Should have got a EmptyArgumentException");
        }
        catch (EmptyArgumentException e) {
            // We expect this.
        }
    }
}
