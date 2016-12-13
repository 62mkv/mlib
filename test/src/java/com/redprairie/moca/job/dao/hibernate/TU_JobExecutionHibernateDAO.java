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

package com.redprairie.moca.job.dao.hibernate;

import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.job.JobDefinition;
import com.redprairie.moca.job.JobExecution;
import com.redprairie.moca.server.InstanceUrl;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.util.MocaUtils;

/**
 * This is a test class for the hibernate implementation of a job definition
 * DAO.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_JobExecutionHibernateDAO {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ServerUtils.setupDaemonContext(
                TU_JobExecutionHibernateDAO.class.getName(), true);
    }
    
    @Before
    public void beforeTest() throws MocaException {
        MocaUtils.currentContext().executeCommand("[delete from job_definition where 1=1] catch (-1403)");
        MocaUtils.currentContext().executeCommand("[delete from job_definition_exec where 1=1] catch (-1403)");
        _dao = new JobExecutionHibernateDAO();
    }
    
    @After
    public void afterTest() throws MocaException {
        MocaUtils.currentContext().rollback();
    }

    /**
     * This test just makes sure reads work properly, it doesn't test if
     * the object returned is valid or not, just making sure valid SQL is
     * generated.
     */
    @Test
    public void test() {
        _dao.read("TEST", new InstanceUrl(false, 
            "localhost", 4050), new Date());
    }
    
    @Test
    public void testSave() {
        JobDefinition def = new JobDefinition();
        def.setJobId("TEST");
        JobExecution jobExec = new JobExecution(def, new InstanceUrl(false, 
            "localhost", 4050));
        
        _dao.save(jobExec);
        
        _dao.flush();
    }

    JobExecutionHibernateDAO _dao;
}
