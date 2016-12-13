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

package com.redprairie.moca.task.dao.hibernate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.db.hibernate.HibernateTools;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.task.TaskDefinition;
import com.redprairie.moca.util.MocaUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_TaskDefinitionHibernateDAO {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ServerUtils.setupDaemonContext(
                TU_TaskDefinitionHibernateDAO.class.getName(), true);
    }
    
    @Before
    public void beforeTest() throws MocaException {
        MocaUtils.currentContext().executeCommand("[delete from task_definition where 1=1] catch (-1403)");
        _dao = new TaskDefinitionHibernateDAO();
    }
    
    @After
    public void afterTest() throws MocaException {
        MocaUtils.currentContext().rollback();
    }

    /**
     * Test method for {@link com.redprairie.moca.dao.hibernate.AbstractUnknownKeyHibernateDAO#read(java.io.Serializable)}.
     * @throws SQLException 
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     */
    @Test
    public void testRead() throws SQLException, IllegalArgumentException, 
            IllegalAccessException, InvocationTargetException {
        TaskDefinition taskDef = createDefaultTaskDefinition();
        createDatabaseEntry(taskDef);
        
        TaskDefinition taskDefFromHibernate = _dao.read(taskDef.getTaskId());
        
        Method[] taskDefinitionMethods = 
            taskDefFromHibernate.getClass().getMethods();
        boolean triedComparison = false;
        
        for (Method method : taskDefinitionMethods) {
            if (method.getName().startsWith("is") || method.getName().startsWith("get")) {
                assertEquals(method.invoke(taskDef), 
                        method.invoke(taskDefFromHibernate));
                triedComparison = true;
            }
        }
        
        assertTrue("We didn't try any comparisons", triedComparison);
    }

    /**
     * Test method for {@link com.redprairie.moca.dao.hibernate.AbstractUnknownKeyHibernateDAO#readAll()}.
     * @throws SQLException 
     */
    @Test
    public void testReadAll() throws SQLException {
        TaskDefinition taskDef1 = createDefaultTaskDefinition();
        TaskDefinition taskDef2 = createDefaultTaskDefinition();
        taskDef2.setTaskId("TEST2");
        TaskDefinition taskDef3 = createDefaultTaskDefinition();
        taskDef3.setTaskId("TEST3");
        
        createDatabaseEntry(taskDef1);
        createDatabaseEntry(taskDef2);
        createDatabaseEntry(taskDef3);
        
        List<TaskDefinition> taskDefs = _dao.readAll();
        
        assertTrue("We should have received 3 tasks!", 
                taskDefs.size() == 3);
        
        assertTrue(taskDefs.containsAll(Arrays.asList(taskDef1, taskDef2, 
                taskDef3)));
    }
    
    /**
     * Test method for {@link com.redprairie.moca.dao.hibernate.AbstractUnknownKeyHibernateDAO#readAllTasksForAllAndRoles()}.
     * @throws SQLException 
     */
    @Test
    public void testReadAllQuestionValues() throws SQLException {
        RoleDefinition role = new RoleDefinition();
        role.setRoleId("*");
        
        TaskDefinition taskDef1 = createDefaultTaskDefinition();
        taskDef1.setRole(role);
        TaskDefinition taskDef2 = createDefaultTaskDefinition();
        taskDef2.setTaskId("TEST2");
        taskDef2.setRole(role);
        // This task should not be returned
        TaskDefinition taskDef3 = createDefaultTaskDefinition();
        taskDef3.setTaskId("TEST3");
        
        createDatabaseEntry(taskDef1);
        createDatabaseEntry(taskDef2);
        createDatabaseEntry(taskDef3);
        
        List<TaskDefinition> taskDefs = _dao.readAllTasksForAllAndRoles();
        
        assertTrue("We should have received 2 tasks!", 
                taskDefs.size() == 2);
        
        assertTrue(taskDefs.containsAll(Arrays.asList(taskDef1, taskDef2)));
    }
    
    /**
     * Test method for {@link com.redprairie.moca.dao.hibernate.AbstractUnknownKeyHibernateDAO#readAllTasksForAllAndRoles()}.
     * @throws SQLException 
     */
    @Test
    public void testReadAllQuestionValuesPlusAnother() throws SQLException {
        RoleDefinition role = new RoleDefinition();
        role.setRoleId("*");
        
        TaskDefinition taskDef1 = createDefaultTaskDefinition();
        taskDef1.setRole(role);
        TaskDefinition taskDef2 = createDefaultTaskDefinition();
        taskDef2.setTaskId("TEST2");
        taskDef2.setRole(role);
        
        RoleDefinition role2 = new RoleDefinition();
        role2.setRoleId("RETURN_ROLE");
        
        TaskDefinition taskDef3 = createDefaultTaskDefinition();
        taskDef3.setTaskId("TEST3");
        taskDef3.setRole(role2);
        // This task should not be returned
        TaskDefinition taskDef4 = createDefaultTaskDefinition();
        taskDef4.setTaskId("TEST4");
        
        TaskDefinition taskDef5 = createDefaultTaskDefinition();
        taskDef5.setTaskId("TEST5");
        
        // This role maps to the task
        RoleDefinition role3 = new RoleDefinition();
        role3.setRoleId("TEST5");
        
        createDatabaseEntry(taskDef1);
        createDatabaseEntry(taskDef2);
        createDatabaseEntry(taskDef3);
        createDatabaseEntry(taskDef4);
        createDatabaseEntry(taskDef5);
        
        List<TaskDefinition> taskDefs = _dao.readAllTasksForAllAndRoles(role2, role3);
        
        assertTrue("We should have received 4 tasks!", 
                taskDefs.size() == 4);
        
        assertTrue(taskDefs.containsAll(Arrays.asList(taskDef1, taskDef2, 
            taskDef3, taskDef5)));
    }
    
    /**
     * Test method for {@link com.redprairie.moca.dao.hibernate.AbstractUnknownKeyHibernateDAO#save(java.lang.Object)}.
     * @throws SQLException 
     */
    @Test
    public void testSave() throws SQLException {
        TaskDefinition taskDef = createDefaultTaskDefinition();
        
        _dao.save(taskDef);
        
        _dao.flush();
        
        assertTrue("There should be an entry", isEntryAvailable(taskDef));
    }

    /**
     * Test method for {@link com.redprairie.moca.dao.hibernate.AbstractUnknownKeyHibernateDAO#delete(java.lang.Object)}.
     * @throws SQLException 
     */
    @Test
    public void testDelete() throws SQLException {
        TaskDefinition taskDef = createDefaultTaskDefinition();
        createDatabaseEntry(taskDef);
        
        _dao.delete(taskDef);
        
        _dao.flush();
        
        assertFalse("There shouldn't be an entry", isEntryAvailable(taskDef));
    }
    
    private boolean isEntryAvailable(TaskDefinition taskDef) throws SQLException {
        Connection conn = MocaUtils.currentContext().getDb().getConnection();
        
        PreparedStatement pstmt = null;
        ResultSet res = null;
        boolean available = false;
        
        try {
            // Since it is a pool point it will save with the pool_flg as true
            pstmt = conn.prepareStatement(        
                    "select count(1) count from task_definition" +
                    "  where task_id = ?");
            
            pstmt.setString(1, taskDef.getTaskId());
            
            pstmt.execute();
            
            res = pstmt.getResultSet();
            
            if (res.next()) {
                available = res.getInt("count") == 1;
            }
        }
        finally {
            if (pstmt != null) {
                pstmt.close();
            }
        }
        
        return available;
    }
    
    /**
     * This test is here to confirm that since task_definition can be a 
     * nvarchar that we are able to do a select for it using sql query.
     */
    @Test
    public void testIsEntryAvailableHibernate() {
        Session hibernateSession = HibernateTools.getSession();
        
        SQLQuery query = hibernateSession.createSQLQuery(
                "select task_id from task_definition" +
                "  where task_id = ?");
        
        query.setString(0, "NOT-PRESENT");

        Assert.assertTrue("There shouldn't be a NOT-PRESENT task in the db.", 
                query.list().isEmpty());
    }
    
    private TaskDefinition createDefaultTaskDefinition() {
        TaskDefinition taskDef = new TaskDefinition();
        taskDef.setTaskId("TEST");
        
        RoleDefinition nodeDef = new RoleDefinition();
        nodeDef.setRoleId("NODE");
        
        // Set the non nullable values
        taskDef.setAutoStart(false);
        taskDef.setRestart(false);
        taskDef.setCmdLine("Execute Something");
        taskDef.setLogFile("log file");
        taskDef.setName("NAME");
        taskDef.setRole(nodeDef);
        taskDef.setRunDirectory("Run Here");
        taskDef.setType(TaskDefinition.PROCESS_TASK);
        taskDef.setEnvironment(new HashMap<String, String>());
        
        return taskDef;
    }
    
    private void createDatabaseEntry(TaskDefinition taskDef) throws SQLException {
        Connection conn = MocaUtils.currentContext().getDb().getConnection();
        
        PreparedStatement pstmt = null;
        String roleId = taskDef.getRole().getRoleId();
        
        try {
            // Since it is a pool point it will save with the pool_flg as true
            pstmt = conn.prepareStatement(        
                    "insert into task_definition (" +
                    "  task_id, role_id, name, task_typ, cmd_line, run_dir, log_file, " +
                    "  restart, auto_start) " +
                    "values ( ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            
            pstmt.setString(1, taskDef.getTaskId());
            pstmt.setString(2, roleId);
            pstmt.setString(3, taskDef.getName());
            pstmt.setString(4, taskDef.getType());
            pstmt.setString(5, taskDef.getCmdLine());
            pstmt.setString(6, taskDef.getRunDirectory());
            pstmt.setString(7, taskDef.getLogFile());
            pstmt.setBoolean(8, taskDef.isRestart());
            pstmt.setBoolean(9, taskDef.isAutoStart());
            
            pstmt.executeUpdate();
        }
        finally {
            if (pstmt != null) {
                pstmt.close();
            }
        }
    }

    TaskDefinitionHibernateDAO _dao;
}
