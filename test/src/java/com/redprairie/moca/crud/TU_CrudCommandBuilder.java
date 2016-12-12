/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.RequiredArgumentException;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.exceptions.MissingArgumentException;
import com.redprairie.moca.exceptions.MissingPKException;
import com.redprairie.moca.test.StubMocaContext;
import com.redprairie.util.ProxyStub;

/**
 * A Unit Test class to test the command builder
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class TU_CrudCommandBuilder extends TestCase {

    /** Tests the checkVariable method for an exception when the variable is null
     * @throws RequiredArgumentException 
     * @throws EmptyArgumentException 
     * 
     */
    public void testCheckVariableNotExists() throws EmptyArgumentException {
        
        CrudCommandBuilder builder = getBuilder(null);
        
        try {
            builder.checkVariable("string", true);
            fail("RequiredArgumentException should have thrown");
        }
        catch (MissingArgumentException e)
        {}  
    }
    
    /** Tests the checkVariable method for all cases that should pass
     * @throws RequiredArgumentException 
     * @throws EmptyArgumentException 
     * 
     */
    public void testCheckVariable() throws MissingArgumentException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "blblb");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        builder.checkVariable("string", true);
        builder.checkVariable("intvalue", true);
        builder.checkVariable("decimal", true);
        builder.checkVariable("boolean", true);
    }
    
    /** Tests the checkVariable method for all cases that should pass if ignore value is on
     * @throws RequiredArgumentException 
     * @throws EmptyArgumentException 
     * 
     */
    public void testCheckVariableValueIgnore() throws MissingArgumentException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", null);
        values.put("intvalue", null);
        values.put("decimal", null);
        values.put("boolean", null);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        builder.checkVariable("string", false);
        builder.checkVariable("intvalue", false);
        builder.checkVariable("decimal", false);
        builder.checkVariable("boolean", false);
    }
    
    /** Tests the checkVariable method for an exception when the variable is null
     * @throws RequiredArgumentException 
     * @throws EmptyArgumentException 
     * 
     */
    public void testCheckVariableNullException() throws MissingArgumentException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", null);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            builder.checkVariable("string", true);
            fail("EmptyArgumentException should have thrown");
        }
        catch (EmptyArgumentException e)
        {}
       
    }
    
    /** Tests the checkVariable method for an exception when the variable is null
     * @throws RequiredArgumentException 
     * @throws EmptyArgumentException 
     * 
     */
    public void testCheckVariableEmptyException() throws MissingArgumentException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "");
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            builder.checkVariable("string", true);
            fail("EmptyArgumentException should have thrown");
        }
        catch (EmptyArgumentException e)
        {}
       
    }
    
    /** Tests that the checkRequiredFields method succeeds in a normal insert case
     * 
     */
    public void testCheckRequiredFieldsInsert() {
                
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "blblb");
        values.put("intvalue", 1);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            builder.checkRequiredFields(getTestTable(), CrudMode.INSERT);
        }
        catch (MissingPKException e) {
            fail(e.getMessage());
        }
        
    }
    
    /** Tests that the checkRequiredFields method succeeds in a normal update case
     * 
     */
    public void testCheckRequiredFieldsUpdate() {
               
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "blblb");
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            builder.checkRequiredFields(getTestTable(), CrudMode.UPDATE);
        }
        catch (MissingPKException e) {
            fail(e.getMessage());
        }
    }
    
    
    /** Tests that the checkRequiredFields method fails when a PK field is missing
     *  on an update
     */
    public void testCheckRequiredFieldsUpdateMissingField() {
             
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            builder.checkRequiredFields(getTestTable(), CrudMode.INSERT);
            fail("RequiredArgumentException should have thrown");
        }
        catch (MissingPKException e) {
        }
    }
    
    /** Tests the createInsertCommand method under normal conditions
     *  
     */
    public void testCreateInsertCommand() {
              
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            String clause = builder.createInsertCommand(getTestTable(), false, columns);
            
            assertNotNull(clause);
            assertEquals("[INSERT INTO test (string, intvalue, decimal) VALUES (@string, @intvalue, @decimal)]",
                          clause);
            
            assertNotNull(columns);
            assertEquals(1, columns.size());
            assertEquals("string", columns.get(0).getColumnName());
        }
        catch (MocaException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    /** Tests the createInsertCommand method under normal conditions
     *  with a UVersion column in the table
     */
    public void testCreateInsertCommandUVersion() {
        
        TableDefinition def = getTestTable();
        def.getColumns().add(getColumn("u_version", "I"));
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            String clause = builder.createInsertCommand(def, false, columns);
            
            assertNotNull(clause);
            assertEquals("[INSERT INTO test (string, intvalue, decimal, u_version) VALUES (@string, @intvalue, @decimal, 1)]",
                          clause);
            
            assertNotNull(columns);
            assertEquals(1, columns.size());
            assertEquals("string", columns.get(0).getColumnName());
        }
        catch (MocaException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    
    /** Tests the createInsertCommand method under normal conditions
     *  with insert user and date in the definition
     */
    public void testCreateInsertCommandInsUserDate() {

        TableDefinition def =  getTestTable();     
        def.getColumns().add(getColumn("INS_USER_ID", "S"));
        def.getColumns().add(getColumn("INS_DT", "D"));
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            String clause = builder.createInsertCommand(def, false, columns);
            
            assertNotNull(clause);
            assertEquals("[INSERT INTO test (string, intvalue, decimal, INS_USER_ID, INS_DT) " +
            		 "VALUES (@string, @intvalue, @decimal, upper(nvl(@@USR_ID, 'NOUSER')), sysdate)]",
                          clause);
            
            assertNotNull(columns);
            assertEquals(1, columns.size());
            assertEquals("string", columns.get(0).getColumnName());
        }
        catch (MocaException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    /** Tests the createInsertCommand method under normal conditions
     *  with insert and modify user and date in the definition
     */
    public void testCreateInsertCommandInsUpdUserDate() {

        TableDefinition def =  getTestTable();     
        def.getColumns().add(getColumn("INS_USER_ID", "S"));
        def.getColumns().add(getColumn("INS_DT", "D"));
        def.getColumns().add(getColumn("MOD_USR_ID", "S"));
        def.getColumns().add(getColumn("MODDTE", "D"));
        
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            String clause = builder.createInsertCommand(def, false, columns);
            
            assertNotNull(clause);
            assertEquals("[INSERT INTO test (string, intvalue, decimal, INS_USER_ID, INS_DT, MOD_USR_ID, MODDTE) " +
                         "VALUES (@string, @intvalue, @decimal, upper(nvl(@@USR_ID, 'NOUSER')), sysdate, upper(nvl(@@USR_ID, 'NOUSER')), sysdate)]",
                          clause);
            
            assertNotNull(columns);
            assertEquals(1, columns.size());
            assertEquals("string", columns.get(0).getColumnName());
        }
        catch (MocaException e) {
            fail(e.getLocalizedMessage());
        }
    }
      
    /** Tests the createInsertCommand method under normal conditions
     *  with pkUpperCase set
     */
    public void testCreateInsertCommandUpper() {
              
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            String clause = builder.createInsertCommand(getTestTable(), true, columns);
            
            assertNotNull(clause);
            assertEquals("[INSERT INTO test (string, intvalue, decimal) VALUES (UPPER(@string), @intvalue, @decimal)]",
                          clause);
            
            assertNotNull(columns);
            assertEquals(1, columns.size());
            assertEquals("string", columns.get(0).getColumnName());
        }
        catch (MocaException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    /** Tests the createUpdateCommand method under normal conditions
     *  
     */
    public void testCreateUpdateCommand() {
              
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            String whereClause = "string=@string";
            String clause = builder.createUpdateCommand(getTestTable(), whereClause, columns);
            
            assertNotNull(clause);
            assertEquals("[UPDATE test SET intvalue=@intvalue, decimal=@decimal WHERE string=@string]",
                          clause);
            
            assertNotNull(columns);
            assertEquals(2, columns.size());
            assertEquals("intvalue", columns.get(0).getColumnName());
            assertEquals("decimal", columns.get(1).getColumnName());
        }
        catch (MocaException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    /** Tests the createInsertCommand method under normal conditions
     *  with modification date and user fields
     */
    public void testCreateUpdateCommandModDateUser() {
        TableDefinition def =  getTestTable();     
        def.getColumns().add(getColumn("LAST_UPD_USER_ID", "S"));
        def.getColumns().add(getColumn("LAST_UPD_DT", "D"));
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            String whereClause = "string=@string";
            String clause = builder.createUpdateCommand(def, whereClause, columns);
            
            assertNotNull(clause);
            assertEquals("[UPDATE test SET intvalue=@intvalue, decimal=@decimal, " +
            		 "LAST_UPD_USER_ID=upper(nvl(@@USR_ID, 'NOUSER')), LAST_UPD_DT=sysdate " +
            		 "WHERE string=@string]",
                          clause);
            
            assertNotNull(columns);
            assertEquals(4, columns.size());
            assertEquals("intvalue", columns.get(0).getColumnName());
            assertEquals("decimal", columns.get(1).getColumnName());
        }
        catch (MocaException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    /** Tests the createUpdateCommand method where only the PK
     *  and meta data (updt_usr_id) fields exist
     */
    public void testCreateUpdateCommandNoUpdateFieldsButMetaData() {
        
        TableDefinition def =  getTestTable();     
        def.getColumns().add(getColumn("LAST_UPD_USER_ID", "S"));
        def.getColumns().add(getColumn("LAST_UPD_DT", "D"));
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
                
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            String whereClause = "string=@string";
            String clause = builder.createUpdateCommand(def, whereClause, columns);
            
            assertNotNull(clause);
            assertEquals("[UPDATE test SET LAST_UPD_USER_ID=upper(nvl(@@USR_ID, 'NOUSER')), LAST_UPD_DT=sysdate WHERE string=@string]",
                          clause);
            
            assertNotNull(columns);           
        }
        catch (MocaException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    /** Tests the createUpdateCommand method where only the PK exists
     *  Command should return an empty string
     */
    public void testCreateUpdateCommandNoUpdateFields() {
               
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
                
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            String whereClause = "string=@string";
            String clause = builder.createUpdateCommand(getTestTable(), whereClause, columns);
            
            assertNotNull(clause);
            assertEquals("", clause);           
        }
        catch (MocaException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    /** Tests the createRemoveCommand method under normal conditions
     * 
     */
    public void testCreateRemoveCommand() {
        TableDefinition definition = new TableDefinition("test");
        definition.getColumns().add(getPKColumn("string", "S"));
        definition.getColumns().add(getPKColumn("intvalue", "I"));
        definition.getColumns().add(getPKColumn("decimal", "F"));
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            String clause = builder.createRemoveCommand(definition, false);
            
            assertNotNull(clause);
            assertEquals(
                "[DELETE FROM test WHERE string=@string AND intvalue=@intvalue AND decimal=@decimal]",
                clause);
        }
        catch (MocaException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    /** Tests the createRemoveCommand method under normal conditions
     *  With PK upper case set
     */
    public void testCreateRemoveCommandUpper() {
        TableDefinition definition = new TableDefinition("test");
        definition.getColumns().add(getPKColumn("string", "S"));
        definition.getColumns().add(getPKColumn("intvalue", "I"));
        definition.getColumns().add(getPKColumn("decimal", "F"));
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            String clause = builder.createRemoveCommand(definition, true);
            
            assertNotNull(clause);
            assertEquals(
                "[DELETE FROM test WHERE string=UPPER(@string) AND intvalue=@intvalue AND decimal=@decimal]",
                clause);
        }
        catch (MocaException e) {
            fail(e.getLocalizedMessage());
        }
    }
        
    /** Tests the createWhereClause method under normal conditions
     * 
     */
    public void testCreateWhereClause() {
                
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            String clause = builder.createWhereClause(getTestTable(), false, columns);
            
            assertNotNull(clause);
            assertEquals("string=@string", clause);
            
            assertNotNull(columns);
            assertEquals(1, columns.size());
            assertEquals("string", columns.get(0).getColumnName());
        }
        catch (MissingPKException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    /** Tests the createWhereClause method under normal conditions
     *  with a compound where clause and pkUpperCase set
     */
    public void testCreateWhereCompoundClauseUpper() {
        TableDefinition definition = new TableDefinition("test");
        definition.addColumn(getPKColumn("string", "S"));
        definition.addColumn(getPKColumn("intvalue", "I"));
        definition.addColumn(getPKColumn("decimal", "F"));
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            String clause = builder.createWhereClause(definition, true, columns);
            
            assertNotNull(clause);
            assertEquals("string=UPPER(@string) AND intvalue=@intvalue AND decimal=@decimal", clause);
            
            assertNotNull(columns);
            assertEquals(3, columns.size());
            assertEquals("string", columns.get(0).getColumnName());
            assertEquals("intvalue", columns.get(1).getColumnName());
            assertEquals("decimal", columns.get(2).getColumnName());
        }
        catch (MissingPKException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    /** Tests the createWhereClause method under normal conditions
     *  with a compound where clause
     */
    public void testCreateWhereCompoundClause() {
        TableDefinition definition = new TableDefinition("test");
        definition.addColumn(getPKColumn("string", "S"));
        definition.addColumn(getPKColumn("intvalue", "I"));
        definition.addColumn(getPKColumn("decimal", "F"));
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            String clause = builder.createWhereClause(definition, false, columns);
            
            assertNotNull(clause);
            assertEquals("string=@string AND intvalue=@intvalue AND decimal=@decimal", clause);
            
            assertNotNull(columns);
            assertEquals(3, columns.size());
            assertEquals("string", columns.get(0).getColumnName());
            assertEquals("intvalue", columns.get(1).getColumnName());
            assertEquals("decimal", columns.get(2).getColumnName());
        }
        catch (MissingPKException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    /** Tests the createConcurrencyWhereClause method under normal conditions
     * 
     */
    public void testCreateConcurrencyWhereClause() {
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            String clause = builder.createConcurrencyWhereClause(getTestTable(), false);
            
            assertEquals("string=@string AND intvalue=@intvalue AND decimal=@decimal", clause);
        }
        catch (MissingPKException e) {
            fail(e.getLocalizedMessage());
        }
    }
    
    /** Tests the createWhereClause method under normal conditions
     *  from a list of names rather than the table's PK list
     */
    public void testCreateWhereClauseFromList() {
                
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("string", "foo");
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            List<ColumnDefinition> columns = new ArrayList<ColumnDefinition>();
            String[] fields = new String[] {"intvalue", "decimal"};
            String clause = builder.createWhereClause(getTestTable(), fields, 
                                                      false, columns);
            
            assertNotNull(clause);
            assertEquals("intvalue=@intvalue AND decimal=@decimal", clause);
            
            assertNotNull(columns);
            assertEquals(2, columns.size());
            assertEquals("intvalue", columns.get(0).getColumnName());
            assertEquals("decimal", columns.get(1).getColumnName());
        }
        catch (MissingPKException e) {
            fail(e.getLocalizedMessage());
        }
    }
        
    /** Tests the createWhereClause method with no PK fields so a MissingPKException
     *  should be thrown
     */
    public void testCreateWhereClauseNoPKFields() {
                
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("intvalue", 1);
        values.put("decimal", 1.9);
        values.put("boolean", false);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        try {
            builder.createWhereClause(getTestTable(), false, null);
            fail("MissingPKException should have thrown");
        }
        catch (MissingPKException e) {
        }
    }
    
    /** Tests the uVersionValid command with the column not existing or 
     *  the argument not on the stack.
     */
    public void testUVersionNotFoundInTableOrStack() {
        TableDefinition notInDef = getTestTable();
        TableDefinition inDef = getTestTable();
        inDef.addColumn(getColumn("U_VERSION", "I"));
        
        CrudCommandBuilder builder = getBuilder(null);
        
        assertTrue(builder.uVersionValid(notInDef, ""));
        assertTrue(builder.uVersionValid(inDef, ""));
    }
    
    /** Tests the uVersionValid command with the existing on 
     *  the argument stack but is null.
     */
    public void testUVersionOnStackAsNull() {
        TableDefinition inDef = getTestTable();
        inDef.addColumn(getColumn("U_VERSION", "I"));
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("U_VERSION", null);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        assertTrue(builder.uVersionValid(inDef, ""));
    }
    
    /** Tests the uVersionValid command with the existing on 
     *  the argument stack but is not an integer value.
     */
    public void testUVersionOnStackAsNonInt() {
        TableDefinition inDef = getTestTable();
        inDef.addColumn(getColumn("U_VERSION", "I"));
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("U_VERSION", "blblb");
        
        CrudCommandBuilder builder = getBuilder(values);
        
        assertTrue(builder.uVersionValid(inDef, ""));
    }
    
    /** Tests the uVersionValid command with valid values. 
     * 
     */
    public void testUVersion() {
        TableDefinition inDef = getTestTable();
        inDef.addColumn(getColumn("U_VERSION", "I"));
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("U_VERSION", 2);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        assertTrue(builder.uVersionValid(inDef, "a=@a"));
    }
    
    /** Tests the uVersionValid command with an out-of-date value. 
     * 
     */
    public void testUVersionObsloete() {
        TableDefinition inDef = getTestTable();
        inDef.addColumn(getColumn("U_VERSION", "I"));
        
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("U_VERSION", 1);
        
        CrudCommandBuilder builder = getBuilder(values);
        
        assertFalse(builder.uVersionValid(inDef, "a=@a"));
    }
    
    
    /** Tests the verifyDataType command with some default values
     * 
     */
    public void testVerifyDataTypeDefault() {
        CrudCommandBuilder builder = getBuilder(null);
        
        assertEquals("@foo", builder.verifyDataType(getColumn("foo", "S")));
        assertEquals("@foo", builder.verifyDataType(getColumn("foo", "I")));
        assertEquals("@foo", builder.verifyDataType(getColumn("foo", "F")));
        assertEquals("to_date(@foo)", builder.verifyDataType(getColumn("foo", "D")));
    }
    
    /** Tests the verifyDataType command with PK_UPPER_CASE Flag some default values
     * 
     */
    public void testVerifyDataTypePKUpperCase() {
        CrudCommandBuilder builder = getBuilder(null);
        
        assertEquals("UPPER(@foo)", builder.verifyDataType(getPKColumn("foo", "S"), true));
        assertEquals("@foo", builder.verifyDataType(getPKColumn("foo", "I"), true));
        assertEquals("@foo", builder.verifyDataType(getPKColumn("foo", "F"), true));
        assertEquals("to_date(@foo)", builder.verifyDataType(getPKColumn("foo", "D"), true));
    }
    
    //Private helper methods
    /** Creates a new command builder with the virtual context
     * @param mapArgs The arguments to be on the virtual context
     * @return A new CrudCommandBuilder object
     */
    private CrudCommandBuilder getBuilder(Map<String, Object> mapArgs) {
        
        if (mapArgs == null)
            mapArgs = new HashMap<String, Object>();

        MocaContext stubContext = ProxyStub.newProxy(MocaContext.class, new StubMocaContext(mapArgs) {
            public MocaResults executeInline(String command) throws MocaException {
                
                if (command.startsWith("[SELECT nvl(U_VERSION, 0)")) {
                    EditableResults results = new SimpleResults();
                    results.addColumn("u_version", MocaType.INTEGER);
                    results.addRow();
                    results.setIntValue(0, 2);
                    
                    return results;
                }
                    
                
                return null;
            }
        });
        
        return new CrudCommandBuilder(stubContext);
    }
    
    /** Creates a new column definition
     * @param name The column name
     * @param dataType The C data type
     * @return A new column definition
     */
    private ColumnDefinition getColumn(String name, String dataType) {
        return new ColumnDefinition(name, dataType, 0, true, false, false);
    }
    
    /** Creates a new column definition as a PK field
     * @param name The column name
     * @param dataType The C data type
     * @return A new column definition
     */
    private ColumnDefinition getPKColumn(String name, String dataType) {
        return new ColumnDefinition(name, dataType, 0, false, true, false);
    }
    
    /** Creates a new column definition as a PK field
     * @param name The column name
     * @param dataType The C data type
     * @return A new column definition
     */
    private ColumnDefinition getRequiredColumn(String name, String dataType) {
        return new ColumnDefinition(name, dataType, 0, false, false, false);
    }

    /** Creates a test table definition
     * @return A new TableDefinition object
     */
    private TableDefinition getTestTable() {
        TableDefinition definition = new TableDefinition("test");
        definition.addColumn(getPKColumn("string", "S"));
        definition.addColumn(getRequiredColumn("intvalue", "I"));
        definition.addColumn(getColumn("decimal", "F"));
        
        return definition;
    }
}
