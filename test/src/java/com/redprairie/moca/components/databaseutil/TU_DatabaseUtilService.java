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

package com.redprairie.moca.components.databaseutil;

import org.junit.Ignore;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * This class tests the DatabaseUtilService class to ensure proper functioning
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_DatabaseUtilService extends AbstractMocaTestCase {
    
    /**
     * This tests to make sure that list table columns is returning the correct
     * information for the comp_ver table
     * @throws MocaException If an exception occurs during listing the columns
     */
    public void testListTableColumns() throws MocaException {
         MocaResults res = _moca.executeCommand(
                 "list table columns " +
         	 "  where table = 'CoMp_VEr'");
         
         assertEquals("There should be 12 columns on the comp_ver table", 12, 
                 res.getRowCount());
         
         RowIterator rowIter = res.getRows();
         
         assertTrue(rowIter.next());
         assertEquals("comp_ver", rowIter.getString("table_name"));
         assertEquals("base_prog_id", rowIter.getString("column_name"));
         assertEquals(Character.toString(MocaType.STRING.getTypeCode()), 
                 rowIter.getString("comtyp"));
         assertEquals(256, rowIter.getInt("length"));
         assertTrue(rowIter.getBoolean("pk_flg"));
         assertFalse(rowIter.getBoolean("null_flg"));
         
         assertTrue(rowIter.next());
         assertEquals("comp_ver", rowIter.getString("table_name"));
         assertEquals("comp_maj_ver", rowIter.getString("column_name"));
         assertEquals(Character.toString(MocaType.INTEGER.getTypeCode()), 
                 rowIter.getString("comtyp"));
         assertTrue(rowIter.getBoolean("pk_flg"));
         assertFalse(rowIter.getBoolean("null_flg"));
         
         assertTrue(rowIter.next());
         assertEquals("comp_ver", rowIter.getString("table_name"));
         assertEquals("comp_min_ver", rowIter.getString("column_name"));
         assertEquals(Character.toString(MocaType.INTEGER.getTypeCode()), 
                 rowIter.getString("comtyp"));
         assertTrue(rowIter.getBoolean("pk_flg"));
         assertFalse(rowIter.getBoolean("null_flg"));
         
         assertTrue(rowIter.next());
         assertEquals("comp_ver", rowIter.getString("table_name"));
         assertEquals("comp_bld_ver", rowIter.getString("column_name"));
         assertEquals(Character.toString(MocaType.INTEGER.getTypeCode()), 
                 rowIter.getString("comtyp"));
         assertTrue(rowIter.getBoolean("pk_flg"));
         assertFalse(rowIter.getBoolean("null_flg"));
         
         assertTrue(rowIter.next());
         assertEquals("comp_ver", rowIter.getString("table_name"));
         assertEquals("comp_rev_ver", rowIter.getString("column_name"));
         assertEquals(Character.toString(MocaType.INTEGER.getTypeCode()), 
                 rowIter.getString("comtyp"));
         assertTrue(rowIter.getBoolean("pk_flg"));
         assertFalse(rowIter.getBoolean("null_flg"));
         
         assertTrue(rowIter.next());
         assertEquals("comp_ver", rowIter.getString("table_name"));
         assertEquals("comp_file_nam", rowIter.getString("column_name"));
         assertEquals(Character.toString(MocaType.STRING.getTypeCode()), 
                 rowIter.getString("comtyp"));
         assertFalse(rowIter.getBoolean("pk_flg"));
         assertEquals(256, rowIter.getInt("length"));
         assertFalse(rowIter.getBoolean("null_flg"));
         
         assertTrue(rowIter.next());
         assertEquals("comp_ver", rowIter.getString("table_name"));
         assertEquals("comp_prog_id", rowIter.getString("column_name"));
         assertEquals(Character.toString(MocaType.STRING.getTypeCode()), 
                 rowIter.getString("comtyp"));
         assertFalse(rowIter.getBoolean("pk_flg"));
         assertEquals(256, rowIter.getInt("length"));
         assertFalse(rowIter.getBoolean("null_flg"));
         
         assertTrue(rowIter.next());
         assertEquals("comp_ver", rowIter.getString("table_name"));
         assertEquals("comp_typ", rowIter.getString("column_name"));
         assertEquals(Character.toString(MocaType.STRING.getTypeCode()), 
                 rowIter.getString("comtyp"));
         assertFalse(rowIter.getBoolean("pk_flg"));
         assertEquals(1, rowIter.getInt("length"));
         assertFalse(rowIter.getBoolean("null_flg"));
         
         assertTrue(rowIter.next());
         assertEquals("comp_ver", rowIter.getString("table_name"));
         assertEquals("comp_file_ext", rowIter.getString("column_name"));
         assertEquals(Character.toString(MocaType.STRING.getTypeCode()), 
                 rowIter.getString("comtyp"));
         assertFalse(rowIter.getBoolean("pk_flg"));
         assertEquals(3, rowIter.getInt("length"));
         assertFalse(rowIter.getBoolean("null_flg"));
         
         assertTrue(rowIter.next());
         assertEquals("comp_ver", rowIter.getString("table_name"));
         assertEquals("comp_need_fw", rowIter.getString("column_name"));
         assertEquals(Character.toString(MocaType.INTEGER.getTypeCode()), 
                 rowIter.getString("comtyp"));
         assertFalse(rowIter.getBoolean("pk_flg"));
         assertTrue(rowIter.getBoolean("null_flg"));
         
         assertTrue(rowIter.next());

         handleColumn(rowIter);
         
         assertTrue(rowIter.next());

         handleColumn(rowIter);
         
         assertFalse(rowIter.next());
     }
    
    /**
     * This method is used to check the column information.  This is needed
     * since all databases do not return the columns in the same order
     * @param rowIter The row iterator to check
     */
    private void handleColumn(RowIterator rowIter) {
        assertEquals("comp_ver", rowIter.getString("table_name"));
        String columnName = rowIter.getString("column_name");
        
        if (columnName.equalsIgnoreCase("lic_key")) {
            assertEquals(Character.toString(MocaType.STRING.getTypeCode()), 
                    rowIter.getString("comtyp"));
            assertFalse(rowIter.getBoolean("pk_flg"));
            assertEquals(100, rowIter.getInt("length"));
            assertTrue(rowIter.getBoolean("null_flg"));
        }
        else if (columnName.equalsIgnoreCase("grp_nam")) {
            assertEquals(Character.toString(MocaType.STRING.getTypeCode()), 
                    rowIter.getString("comtyp"));
            assertFalse(rowIter.getBoolean("pk_flg"));
            assertEquals(40, rowIter.getInt("length"));
            assertFalse(rowIter.getBoolean("null_flg"));
        }
    }
    
    /**
     * This just tests to make sure the correct columns are returned
     * @throws MocaException
     */
    public void testDatabaseInformation() throws MocaException {
        MocaResults results = _moca.executeCommand("get db info");
        
        RowIterator rowIter = results.getRows();
        
        assertTrue(rowIter.next());
        
        assertNotNull(rowIter.getString("db_name"));
        assertNotNull(rowIter.getString("db_version"));
        assertNotNull(rowIter.getString("db_instance"));
        assertNotNull(rowIter.getString("db_user"));
    }
    
    /**
     * This test is just to make sure that describe table works correctly
     * @throws MocaException If any problem occurs while describing the table
     */
    public void testDescribeTable() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "describe table " +
                "  where table = 'comp_ver'");
        
        assertTrue(results.next());
    }
    
    /**
     * This test is just to make sure that getting extra database information
     * works correctly
     * @throws MocaException If any problem occurs while getting the extra
     *         database information
     */
    public void testGetExtraDatabaseInformation() throws MocaException {
        MocaResults results = _moca.executeCommand("get extra db info");
        
        assertTrue(results.next());
    }
    
    /**
     * This tests to make sure that list database locks returns correctly
     * @throws MocaException If an exception occurs during execution
     */
    @Ignore
    public void xtestListDatabseLocks() throws MocaException {
        try {
            _moca.executeCommand("list database locks");
            // OK to have locks
        }
        catch (NotFoundException e) {
            // OK to have no locks.
        }
    }
    
    /**
     * This tests to make sure that list table indexes works correctly
     * @throws MocaException If an exception occurs during execution
     */
    public void testListIndexesForTable() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "list indexes for table" +
                "  where table_name = 'comp_ver'");
        
        assertTrue(results.next());
    }
    
    /**
     * This tests to make sure that list primary keys works correctly
     * @throws MocaException If an exception occurs during execution
     */
    public void testListPrimaryKeysForTable() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "list primary key for table" +
                "  where table_name = 'comp_ver'");
        
        assertTrue(results.next());
    }
    
    /**
     * This tests to make sure that list table indexes works correctly
     * @throws MocaException If an exception occurs during execution
     */
    public void testListTableIndexes() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "list table indexes" +
                "  where table_name = 'comp_ver'");
        
        assertTrue(results.next());
    }
    
    /**
     * This tests to make sure that list tables with column works correctly
     * @throws MocaException If an exception occurs during execution
     */
    public void testListTablesWithColumn() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "list tables with column" +
                "  where column_name = 'base_prog_id'");
        
        assertTrue(results.next());
    }
    
    /**
     * This tests to make sure that list user sequences works correctly
     * @throws MocaException If an exception occurs during execution
     */
    public void testListUserSequences() throws MocaException {
        try {
            MocaResults results = _moca.executeCommand(
                    "list user sequences");
            
            assertTrue(results.next());
        }
        catch (NotFoundException e) {
            // This error is alright, since MOCA doesn't have sequences
        }
    }
    
    /**
     * This tests to make sure that list user tables works correctly when
     * returning all tables in the database
     * @throws MocaException If an exception occurs during execution
     */
    public void testListUserTablesForAllTables() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "list user tables");
        
        assertTrue(results.next());
    }
    
    /**
     * This tests to make sure that list user tables works correctly when
     * returning a single table in the database
     * @throws MocaException If an exception occurs during execution
     */
    public void testListUserTablesForOneTable() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "list user tables" +
                "  where table_name = 'comp_ver'");
        
        assertTrue(results.next());
        assertFalse("There should be only 1 table returned", results.next());
    }
    
    /**
     * This tests to make sure that list user views works correctly
     * @throws MocaException If an exception occurs during execution
     */
    public void testListUserViews() throws MocaException {
        try {
            MocaResults results = _moca.executeCommand(
                    "list user views");
            
            assertTrue(results.next());
        }
        catch (NotFoundException e) {
            // This is alright since MOCA doesn't have any views
        }
    }
    
    /**
     * This tests to make sure that list user objects works correctly
     * @throws MocaException If an exception occurs during execution
     */
    public void testListUserObjects() throws MocaException {
        MocaResults results = _moca.executeCommand("list user objects");
        
        // We should get at least one object
        assertTrue(results.next());
    }
    
    /**
     * This tests to make sure that list user objects works correctly when
     * returning a single table
     * @throws MocaException If an exception occurs during execution
     */
    public void testListUserObjectsForOneTable() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "list user objects " +
                "    where obj_name = 'comp_ver' " +
                "      and object_type = 'table'");
        
        assertTrue(results.next());
        assertFalse("There should be only 1 object returned", results.next());
    }
    
    /**
     * This tests to make sure that list user objects matches list user tables
     * when the object_type is table
     * @throws MocaException If an exception occurs during execution
     */
    public void testListUserObjectsForTables() throws MocaException {
        MocaResults objResults = _moca.executeCommand(
                "list user objects " +
                "    where obj_type = 'table'");
        
        MocaResults tableResults = _moca.executeCommand("list user tables");
        
        // The queries should return similar result sets
        while (objResults.next()) {
            assertTrue(
                "'list user tables' returned fewer rows than 'list user objects'",
                tableResults.next());
            
            assertEquals(
                "'list user tables' returned different data than 'list user objects'",
                objResults.getString("object_name"),
                tableResults.getString("table_name"));
        }
        
        assertFalse(
            "'list user tables' returned more rows than 'list user objects'",
            tableResults.next());
    }
    
    /**
     * This test just makes sure that explain query returns some values
     * 
     * If that is fixed, this should be changed to no longer catch
     * @throws MocaException
     */
    public void testExplainQuery() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "explain query " +
                "  where query = 'select count(1) from comp_ver'");
        
        assertNotNull(results);
    }
    
    /**
     * This test is to make sure that reseeding a sequence will work correctly
     * @throws MocaException If any kind of problem happens while reseeding
     *         the sequence
     */
    public void testReseedSequence() throws MocaException {
        int seed = 10000;
        int increment = 2;
        String sequenceName = "test_sequence";
        createSequence(sequenceName, seed, increment);
        
        _moca.executeCommand(
                "reseed sequence" +
        	"  where sequence = 'test_sequence'" +
        	"    and seed = " + seed +
        	"    and inc = " + increment + 
        	"    and max = 9999999999");
        
        dropSequence(sequenceName);
    }
    
    /**
     * This will create a new sequence for testing purposes.  If called, it
     * should make sure to drop the sequence afterwards
     * @param sequenceName The name of the sequence to create
     * @param seed The initial seed of the sequence
     * @param increment The increment of the sequence
     * @throws MocaException If any exception occurs while creating the sequence
     */
    private void createSequence(String sequenceName, int seed, int increment) 
            throws MocaException {        
        String dbType = _moca.getDb().getDbType();
        
        if (dbType.equals("MSSQL")) {
            _moca.executeCommand("[/*NOCONV*//*#nobind*/" +
                    "create table dbo." + sequenceName + " " +
                    "(" +
                    "currval numeric(28) identity (" + seed + ", " + increment + ") primary key," +
                    "nextval numeric(28)," +
                    "seedval numeric(28)," +
                    "incval  numeric(28)," +
                    "maxval  numeric(28)" +
                    ")]");
            
            _moca.executeCommand("[/*NOCONV*//*#nobind*/" +
            		"set identity_insert dbo." + sequenceName + " on]");
            
            _moca.executeCommand("[/*NOCONV*//*#nobind*/" +
                    "insert into dbo." + sequenceName + " (currval, nextval, seedval, incval, maxval)" +
                    "  values (-1, -1, " + seed + ", " + increment + ", 9999999999)]");
        }
        else if (dbType.equals("ORACLE")) {
            _moca.executeCommand("[/*NOCONV*//*#nobind*/" +
                    "create sequence " + sequenceName + " " +
                    "start with " + seed + " " +
                    "increment by " + increment + " " +
                    "maxvalue 9999999999 " +
                    "cycle cache 10 " +
                    "order]");
        }
        else {
            fail("Unsupported database type :" + dbType);
        }
    }
    
    public void testListColumnCommentWithNoComments() throws MocaException {
        String tableName = "MOCA3988";
        String columnName = "TEST";
        try {
            _moca.executeCommand("[create table " + tableName + 
                    " ( " + columnName + " int )]");
        
            MocaResults res = _moca.executeCommand("list column comment", 
                    new MocaArgument("table_name", tableName),
                    new MocaArgument("column_name", columnName));
            
            assertTrue(res.next());
            assertEquals(tableName, res.getString("table_name"));
            assertEquals(columnName, res.getString("column_name"));
            assertNull(res.getValue("column_comment"));
            assertFalse(res.next());
        }
        finally {
            try {
                _moca.executeCommand("[drop table " + tableName + "]");
            }
            catch (MocaException e) {
                // We ignore this exception since we don't want this to override
                // the real failure.
                e.printStackTrace();
            }
        }
    }
    
    /**
     * This will drop the desired sequence.  This should be called after 
     * creating a sequence to not leave the sequence on the database
     * @param sequenceName The name of the sequence to drop
     * @throws MocaException If any exception occurs while dropping the sequence
     */
    private void dropSequence(String sequenceName) throws MocaException {
        String dbType = _moca.getDb().getDbType();
        
        if (dbType.equals("MSSQL")) {
            _moca.executeCommand("[/*NOCONV*//*#nobind*/" +
            		"drop table dbo." + sequenceName + "]");
        }
        else if (dbType.equals("ORACLE")) {
            _moca.executeCommand("[/*NOCONV*//*#nobind*/" +
                    "drop sequence " + sequenceName + "]");
        }
        else {
            fail("Unsupported database type :" + dbType);
        }
    }
}
