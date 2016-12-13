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

package com.redprairie.moca.task;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.task.dao.TaskDefinitionDAO;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * This test is to make sure that the spring context will correctly return
 * an object as it should.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_TaskCRUD extends AbstractMocaTestCase {

    @Override
    protected void mocaSetUp() throws Exception {
        super.mocaSetUp();
        _moca.executeCommand("[delete from task_definition where 1=1] catch (-1403)");
        _ctx = new AnnotationConfigApplicationContext(TaskConfig.class);
    }

    /**
     * Test method for {@link com.redprairie.moca.task.TaskConfig#taskDefinitionDAO()}.
     */
    public void testCreateTask() throws MocaException {
        _moca.executeCommand("add task" +
        		     " where task_id = 'foo'" +
        		     "   and role_id = 'bar'" +
        		     "   and name = 'Foo Bar'" +
        		     "   and cmd_line = '/bin/foo bar'" +
        		     "   and run_dir = '$LESDIR/log'" +
        		     "   and log_file = '$LESDIR/log/foo.log'" +
        		     "   and auto_start = 1" +
        		     "   and restart = 0");
        TaskDefinitionDAO dao = _ctx.getBean(TaskDefinitionDAO.class);
        TaskDefinition def = dao.read("foo");
        assertEquals("foo", def.getTaskId());
        assertEquals("bar", def.getRole().getRoleId());
        assertEquals("Foo Bar", def.getName());
        assertEquals("/bin/foo bar", def.getCmdLine());
        assertEquals("$LESDIR/log", def.getRunDirectory());
        assertEquals("$LESDIR/log/foo.log", def.getLogFile());
        assertTrue(def.isAutoStart());
        assertFalse(def.isRestart());
        
        try {
            _moca.executeCommand("list tasks where task_id = 'bar'");
            fail("Expected no results on list");
        }
        catch (NotFoundException e) {
            // Normal
        }
    }
    
    /**
     * Test method for {@link com.redprairie.moca.task.TaskConfig#taskDefinitionDAO()}.
     */
    public void testCreateTaskWithDefaults() throws MocaException {
        _moca.executeCommand("add task" +
                             " where task_id = 'foo'" +
                             "   and name = 'Foo Bar'" +
                             "   and run_dir = '$LESDIR/log'" +
                             "   and cmd_line = '/bin/foo bar'");
        TaskDefinitionDAO dao = _ctx.getBean(TaskDefinitionDAO.class);
        TaskDefinition def = dao.read("foo");
        assertEquals("foo", def.getTaskId());
        assertNull(def.getRole());
        assertEquals("Foo Bar", def.getName());
        assertEquals("/bin/foo bar", def.getCmdLine());
        assertEquals("$LESDIR/log", def.getRunDirectory());
        assertNull(def.getLogFile());
        assertTrue(def.isAutoStart());
        assertTrue(def.isRestart());
        
        MocaResults res = _moca.executeCommand("list tasks");
        assertTrue(res.next());
        assertEquals("foo", res.getString("task_id"));
        assertNull(res.getString("role_id"));
        assertEquals("Foo Bar", res.getString("name"));
        assertEquals("/bin/foo bar", res.getString("cmd_line"));
        assertEquals("$LESDIR/log", res.getString("run_dir"));
        assertNull(res.getString("log_file"));
        assertTrue(res.getBoolean("auto_start"));
        assertTrue(res.getBoolean("restart"));
        assertFalse(res.next());
    }

    public void testCreateAndRemoveTask() throws MocaException {
        _moca.executeCommand("add task" +
            " where task_id = 'foo'" +
            "   and name = 'Foo Bar'" +
            "   and cmd_line = '/bin/foo bar'" +
            "   and run_dir = '$LESDIR/log'");
        MocaResults res = _moca.executeCommand("list task where task_id = 'foo'");
        assertTrue(res.next());
        _moca.executeCommand("remove task where task_id = 'foo'");
        try {
            _moca.executeCommand("list task where task_id = 'foo'");
            fail("Expected no results on list");
        }
        catch (NotFoundException e) {
            // Normal
        }
    }
    
    private ApplicationContext _ctx;

}
