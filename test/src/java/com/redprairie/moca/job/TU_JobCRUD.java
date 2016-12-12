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

package com.redprairie.moca.job;

import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.job.dao.JobDefinitionDAO;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.SpringTools;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.util.AbstractMocaTestCase;

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
public class TU_JobCRUD extends AbstractMocaTestCase {

    @Override
    protected void mocaSetUp() throws Exception {
        super.mocaSetUp();
        _moca.executeCommand("[delete from job_definition where 1=1] catch (-1403)");
        
        Map<String, Object> singletons = new HashMap<String, Object>();
        // We don't really need the next 3 beans just put empty mocks
        singletons.put("systemContext", Mockito.mock(SystemContext.class));
        singletons.put("serverContextFactory", Mockito.mock(
            ServerContextFactory.class));
        singletons.put("madFactory", Mockito.mock(
           MadFactory.class));
        ApplicationContext parent = SpringTools
            .getContextForPreinstantiatedSingletons(singletons);
        
        _ctx = SpringTools.getContextWithParent(parent, JobConfig.class);
    }

    /**
     * Test method for {@link com.redprairie.moca.job.JobConfig#jobDefinitionDAO()}.
     */
    public void testCreateJob() throws MocaException {
        _moca.executeCommand("add job" +
        		     " where job_id = 'foo'" +
        		     "   and role_id = 'bar'" +
        		     "   and name = 'Foo Bar'" +
                             "   and type = 'timer'" +
        		     "   and command = 'do foo | bar'" +
                             "   and log_file = '$LESDIR/log/foo.log'" +
        		     "   and overlap = 0" +
        		     "   and start_delay = 30" +
        		     "   and timer = 60" +
        		     "   and schedule = null");
        JobDefinitionDAO dao = _ctx.getBean(JobDefinitionDAO.class);
        JobDefinition def = dao.read("foo");
        assertEquals("foo", def.getJobId());
        assertEquals("bar", def.getRole().getRoleId());
        assertEquals("Foo Bar", def.getName());
        assertEquals("do foo | bar", def.getCommand());
        assertEquals("$LESDIR/log/foo.log", def.getLogFile());
        assertFalse(def.isOverlap());
        assertEquals(30, (int)def.getStartDelay());
        assertEquals(60, (int)def.getTimer());
        assertNull(def.getSchedule());
        
        try {
            _moca.executeCommand("list jobs where job_id = 'bar'");
            fail("Expected no results on list");
        }
        catch (NotFoundException e) {
            // Normal
        }
    }
    
    /**
     * Test method for {@link com.redprairie.moca.job.JobConfig#jobDefinitionDAO()}.
     */
    public void testCreateJobWithDefaults() throws MocaException {
        _moca.executeCommand("add job" +
            " where job_id = 'foo'" +
            "   and name = 'Foo Bar'" +
            "   and command = 'do foo | bar'" +
            "   and timer = 60");
        JobDefinitionDAO dao = _ctx.getBean(JobDefinitionDAO.class);
        JobDefinition def = dao.read("foo");
        assertEquals("foo", def.getJobId());
        assertNull(def.getRole());
        assertEquals("Foo Bar", def.getName());
        assertEquals("do foo | bar", def.getCommand());
        assertNull(def.getLogFile());
        assertFalse(def.isOverlap());
        assertEquals(0, (int)def.getStartDelay());
        assertEquals(60, (int)def.getTimer());
        assertNull(def.getSchedule());
        
        _moca.executeCommand("add job" +
            " where job_id = 'bar'" +
            "   and name = 'Bar Foo'" +
            "   and command = 'do bar | foo'" +
            "   and schedule = '0 * * * * ?'");

        def = dao.read("bar");
        assertEquals("bar", def.getJobId());
        assertNull(def.getRole());
        assertEquals("Bar Foo", def.getName());
        assertEquals("do bar | foo", def.getCommand());
        assertNull(def.getLogFile());
        assertFalse(def.isOverlap());
        assertNull(def.getStartDelay());
        assertNull(def.getTimer());
        assertEquals("0 * * * * ?", def.getSchedule());
        
        MocaResults res = _moca.executeCommand("list jobs");
        boolean foundFoo = false;
        boolean foundBar = false;
        int count = 0;
        while(res.next()) {
            if (res.getString("job_id").equals("foo")) {
                foundFoo = true;
            }
            else if (res.getString("job_id").equals("bar")) {
                foundBar = true;
                assertEquals("bar", res.getString("job_id"));
                assertNull(res.getString("role_id"));
                assertEquals("Bar Foo", res.getString("name"));
                assertEquals("do bar | foo", res.getString("command"));
                assertNull(res.getString("log_file"));
                assertTrue(res.isNull("start_delay"));
                assertTrue(res.isNull("timer"));
                assertEquals("0 * * * * ?", res.getString("schedule"));
            }
            
            count++;
        }
        
        assertTrue(foundFoo);
        assertTrue(foundBar);
        assertEquals(2, count);
    }

    public void testCreateAndRemoveJob() throws MocaException {
        _moca.executeCommand("add job" +
            " where job_id = 'bar'" +
            "   and name = 'Bar Foo'" +
            "   and command = 'do bar | foo'" +
            "   and schedule = '0 * * * * ?'");
        MocaResults res = _moca.executeCommand("list job where job_id = 'bar'");
        assertTrue(res.next());
        _moca.executeCommand("remove job where job_id = 'bar'");
        try {
            _moca.executeCommand("list job where job_id = 'bar'");
            fail("Expected no results on list");
        }
        catch (NotFoundException e) {
            // Normal
        }
    }
    
    private ApplicationContext _ctx;
}
