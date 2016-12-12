/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.probes.asyncexec;

import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests AbstractExecutors
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public class TU_AbstractExecutors {
    @Test
    public void testEmpty() {
        AbstractExecutors executors = new AbstractExecutors() {
            @Override
            public List<AsynchronousExecution> getExecutors() {
                return Collections.emptyList();
            }
        };
        
        Assert.assertEquals("", executors.dumpExecutors());
    }
    
    @Test
    public void testOneItem() {
        AbstractExecutors executors = new AbstractExecutors() {
            @Override
            public List<AsynchronousExecution> getExecutors() {
                return Collections.singletonList(new AsynchronousExecution(1L, "callable", "status", 1L));
            }
        };
        
        Assert.assertEquals("Thread ID:\t1\nCallable:\tcallable\nStatus:\tstatus\nStatus Age:\t1 ms\n\n", 
            executors.dumpExecutors());
    }
}
