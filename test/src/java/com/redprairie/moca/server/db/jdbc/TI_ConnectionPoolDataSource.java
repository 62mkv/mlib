/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2015
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

package com.redprairie.moca.server.db.jdbc;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

import com.redprairie.moca.util.AbstractMocaTestCase;
import com.redprairie.moca.util.MocaTestUtils;

/**
 * Integration type tests for {@link ConnectionPoolDataSource}
 * 
 * Copyright (c) 2015 JDA Software
 * All Rights Reserved
 */
public class TI_ConnectionPoolDataSource extends AbstractMocaTestCase {
    
    /**
     * The goal of this test is to test {@link ConnectionPoolDataSource} pooling while
     * heavily concurrent to make sure concurrency issues don't show up such as 
     * delegating multiple threads the same connection or something along those lines.
     * @throws InterruptedException
     * @throws BrokenBarrierException
     * @throws ExecutionException
     */
    @Test
    public void testHeavyConcurrency() throws InterruptedException, BrokenBarrierException, ExecutionException {
        MocaTestUtils.makeConcurrentDbRequests(50, 100);
    }

}