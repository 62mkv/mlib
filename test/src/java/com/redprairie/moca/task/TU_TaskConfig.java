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

package com.redprairie.moca.task;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.task.dao.TaskDefinitionDAO;

import static org.junit.Assert.assertNotNull;

/**
 * This test is to make sure that the spring context will correctly return
 * an object as it should.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_TaskConfig {

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ServerUtils.setupDaemonContext(
                TU_TaskConfig.class.getName(), true);
    }
    
    @Before
    public void beforeTest() {
        _ctx = new AnnotationConfigApplicationContext(TaskConfig.class);
    }

    /**
     * Test method for {@link com.redprairie.moca.task.TaskConfig#taskDefinitionDAO()}.
     */
    @Test
    public void testTaskDefinitionDAO() {
        assertNotNull("There should be a task defintion DAO available", 
                _ctx.getBean(TaskDefinitionDAO.class));
    }
    
    ApplicationContext _ctx;
}
