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

package com.redprairie.moca.job.dao.hibernate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
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
import com.redprairie.moca.job.JobDefinition;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.util.MocaUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This is a test class for the hibernate implementation of a job definition
 * DAO.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_JobDefinitionHibernateDAO {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ServerUtils.setupDaemonContext(
                TU_JobDefinitionHibernateDAO.class.getName(), true);
    }
    
    @Before
    public void beforeTest() throws MocaException {
        MocaUtils.currentContext().executeCommand("[delete from job_definition where 1=1] catch (-1403)");
        _dao = new JobDefinitionHibernateDAO();
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
        JobDefinition jobDef = createDefaultJobDefinition();
        createDatabaseEntry(jobDef);
        
        JobDefinition jobDefFromHibernate = _dao.read(jobDef.getJobId());
        
        Method[] jobDefinitionMethods = 
            jobDefFromHibernate.getClass().getMethods();
        boolean triedComparison = false;
        
        for (Method method : jobDefinitionMethods) {
            if (method.getName().startsWith("is") || method.getName().startsWith("get")) {
                assertEquals(method.invoke(jobDef), 
                        method.invoke(jobDefFromHibernate));
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
        JobDefinition jobDef1 = createDefaultJobDefinition();
        JobDefinition jobDef2 = createDefaultJobDefinition();
        jobDef2.setJobId("TEST2");
        JobDefinition jobDef3 = createDefaultJobDefinition();
        jobDef3.setJobId("TEST3");
        
        createDatabaseEntry(jobDef1);
        createDatabaseEntry(jobDef2);
        createDatabaseEntry(jobDef3);
        
        List<JobDefinition> jobDefs = _dao.readAll();
        
        assertTrue("We should have received 3 jobs!", 
                jobDefs.size() == 3);
        
        assertTrue(jobDefs.containsAll(Arrays.asList(jobDef1, jobDef2, 
                jobDef3)));
    }
    
    /**
     * Test method for {@link com.redprairie.moca.dao.hibernate.AbstractUnknownKeyHibernateDAO#readAllJobsForAllAndRoles()}.
     * @throws SQLException 
     */
    @Test
    public void testReadAllQuestionValues() throws SQLException {
        RoleDefinition role = new RoleDefinition();
        role.setRoleId("*");
        
        JobDefinition jobDef1 = createDefaultJobDefinition();
        jobDef1.setRole(role);
        JobDefinition jobDef2 = createDefaultJobDefinition();
        jobDef2.setJobId("TEST2");
        jobDef2.setRole(role);
        // This job should not be returned
        JobDefinition jobDef3 = createDefaultJobDefinition();
        jobDef3.setJobId("TEST3");
        
        createDatabaseEntry(jobDef1);
        createDatabaseEntry(jobDef2);
        createDatabaseEntry(jobDef3);
        
        List<JobDefinition> jobDefs = _dao.readForAllAndRoles();
        
        assertTrue("We should have received 2 jobs!", 
                jobDefs.size() == 2);
        
        assertTrue(jobDefs.containsAll(Arrays.asList(jobDef1, jobDef2)));
    }
    
    /**
     * Test method for {@link com.redprairie.moca.dao.hibernate.AbstractUnknownKeyHibernateDAO#readAllJobsForAllAndRoles()}.
     * @throws SQLException 
     */
    @Test
    public void testReadAllQuestionValuesPlusAnother() throws SQLException {
        RoleDefinition role = new RoleDefinition();
        role.setRoleId("*");
        
        JobDefinition jobDef1 = createDefaultJobDefinition();
        jobDef1.setRole(role);
        JobDefinition jobDef2 = createDefaultJobDefinition();
        jobDef2.setJobId("TEST2");
        jobDef2.setRole(role);
        
        RoleDefinition role2 = new RoleDefinition();
        role2.setRoleId("RETURN_ROLE");
        
        JobDefinition jobDef3 = createDefaultJobDefinition();
        jobDef3.setJobId("TEST3");
        jobDef3.setRole(role2);
        // This job should not be returned
        JobDefinition jobDef4 = createDefaultJobDefinition();
        jobDef4.setJobId("TEST4");
        
        JobDefinition jobDef5 = createDefaultJobDefinition();
        jobDef5.setJobId("TEST5");
        
        // This role maps to the job, but no longer matches
        RoleDefinition role3 = new RoleDefinition();
        role3.setRoleId("TEST5");
        
        createDatabaseEntry(jobDef1);
        createDatabaseEntry(jobDef2);
        createDatabaseEntry(jobDef3);
        createDatabaseEntry(jobDef4);
        createDatabaseEntry(jobDef5);
        
        List<JobDefinition> jobDefs = _dao.readForAllAndRoles(role2, role3);
        
        assertEquals(3, jobDefs.size());
        
        assertTrue(jobDefs.contains(jobDef1));
        assertTrue(jobDefs.contains(jobDef2));
        assertTrue(jobDefs.contains(jobDef3));
    }
    
    /**
     * Test method for {@link com.redprairie.moca.dao.hibernate.AbstractUnknownKeyHibernateDAO#readForAllNoRoleAndRoles()}.
     * @throws SQLException 
     */
    @Test
    public void testReadForAllNoRoleAndRoles() throws SQLException {
        RoleDefinition role = new RoleDefinition();
        role.setRoleId("*");
        
        JobDefinition jobDef1 = createDefaultJobDefinition();
        jobDef1.setRole(role);
        JobDefinition jobDef2 = createDefaultJobDefinition();
        jobDef2.setJobId("TEST2");
        jobDef2.setRole(role);
        
        RoleDefinition role2 = new RoleDefinition();
        role2.setRoleId("RETURN_ROLE");
        
        JobDefinition jobDef3 = createDefaultJobDefinition();
        jobDef3.setJobId("TEST3");
        jobDef3.setRole(role2);
        // This job should not be returned
        JobDefinition jobDef4 = createDefaultJobDefinition();
        jobDef4.setJobId("TEST4");
        jobDef4.setRole(null);
        
        RoleDefinition role4 = new RoleDefinition();
        role4.setRoleId("NOMATCH");
        
        JobDefinition jobDef5 = createDefaultJobDefinition();
        jobDef5.setJobId("TEST5");
        jobDef5.setRole(role4);
        
        // This role maps to the job, but no longer matches
        RoleDefinition role3 = new RoleDefinition();
        role3.setRoleId("TEST5");
        
        createDatabaseEntry(jobDef1);
        createDatabaseEntry(jobDef2);
        createDatabaseEntry(jobDef3);
        createDatabaseEntry(jobDef4);
        createDatabaseEntry(jobDef5);
        
        List<JobDefinition> jobDefs = _dao.readForAllNoRoleAndRoles(role2, role3);
        
        assertTrue(jobDefs.contains(jobDef1));
        assertTrue(jobDefs.contains(jobDef2));
        assertTrue(jobDefs.contains(jobDef3));
        assertTrue(jobDefs.contains(jobDef4));
        
        assertEquals(4, jobDefs.size());
    }
    
    /**
     * Test method for {@link com.redprairie.moca.dao.hibernate.AbstractUnknownKeyHibernateDAO#save(java.lang.Object)}.
     * @throws SQLException 
     */
    @Test
    public void testSave() throws SQLException {
        JobDefinition jobDef = createDefaultJobDefinition();
        
        _dao.save(jobDef);
        
        _dao.flush();
        
        assertTrue("There should be an entry", isEntryAvailable(jobDef));
    }

    /**
     * Test method for {@link com.redprairie.moca.dao.hibernate.AbstractUnknownKeyHibernateDAO#delete(java.lang.Object)}.
     * @throws SQLException 
     */
    @Test
    public void testDelete() throws SQLException {
        JobDefinition jobDef = createDefaultJobDefinition();
        createDatabaseEntry(jobDef);
        
        _dao.delete(jobDef);
        
        _dao.flush();
        
        assertFalse("There shouldn't be an entry", isEntryAvailable(jobDef));
    }
    
    private boolean isEntryAvailable(JobDefinition jobDef) throws SQLException {
        Connection conn = MocaUtils.currentContext().getDb().getConnection();
        
        PreparedStatement pstmt = null;
        ResultSet res = null;
        boolean available = false;
        
        try {
            // Since it is a pool point it will save with the pool_flg as true
            pstmt = conn.prepareStatement(        
                    "select count(1) count from job_definition" +
                    "  where job_id = ?");
            
            pstmt.setString(1, jobDef.getJobId());
            
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
     * This test is here to confirm that since job_definition can be a 
     * nvarchar that we are able to do a select for it using sql query.
     */
    @Test
    public void testIsEntryAvailableHibernate() {
        Session hibernateSession = HibernateTools.getSession();
        
        SQLQuery query = hibernateSession.createSQLQuery(
                "select job_id from job_definition" +
                "  where job_id = ?");
        
        query.setString(0, "NOT-PRESENT");

        Assert.assertTrue("There shouldn't be a NOT-PRESENT job in the db.", 
                query.list().isEmpty());
    }
    
    private JobDefinition createDefaultJobDefinition() {
        JobDefinition jobDef = new JobDefinition();
        jobDef.setJobId("TEST");
        
        RoleDefinition nodeDef = new RoleDefinition();
        nodeDef.setRoleId("NODE");
        
        // Set the non nullable values
        jobDef.setEnabled(false);
        jobDef.setCommand("command");
        jobDef.setOverlap(false);
        jobDef.setLogFile("log file");
        jobDef.setName("NAME");
        jobDef.setRole(nodeDef);
        jobDef.setSchedule("schedule");
        jobDef.setStartDelay(23);
        jobDef.setTimer(32);
        jobDef.setTraceLevel("level");
        jobDef.setType("type");
        jobDef.setEnvironment(new HashMap<String, String>());
        
        return jobDef;
    }
    
    private void createDatabaseEntry(JobDefinition jobDef) throws SQLException {
        Connection conn = MocaUtils.currentContext().getDb().getConnection();
        
        PreparedStatement pstmt = null;
        String roleId;
        if (jobDef.getRole() != null) {
            roleId = jobDef.getRole().getRoleId();
        }
        else {
            roleId = null;
        }
        
        try {
            // Since it is a pool point it will save with the pool_flg as true
            pstmt = conn.prepareStatement(        
                    "insert into job_definition (" +
                    "  job_id, role_id, name, enabled, command, log_file, " +
                    "trace_level, overlap, schedule, start_delay, timer, type) " +
                    "values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            
            pstmt.setString(1, jobDef.getJobId());
            if (roleId != null) {
                pstmt.setString(2, roleId);
            }
            else {
                pstmt.setNull(2, Types.VARCHAR);
            }
            pstmt.setString(3, jobDef.getName());
            pstmt.setBoolean(4, jobDef.isEnabled());
            pstmt.setString(5, jobDef.getCommand());
            pstmt.setString(6, jobDef.getLogFile());
            pstmt.setString(7, jobDef.getTraceLevel());
            pstmt.setBoolean(8, jobDef.isOverlap());
            pstmt.setString(9, jobDef.getSchedule());
            pstmt.setInt(10, jobDef.getStartDelay());
            pstmt.setInt(11, jobDef.getTimer());
            pstmt.setString(12, jobDef.getType());
            
            pstmt.executeUpdate();
        }
        finally {
            if (pstmt != null) {
                pstmt.close();
            }
        }
    }

    JobDefinitionHibernateDAO _dao;
}
