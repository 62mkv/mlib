/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2014
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

package com.redprairie.moca.server.db.translate.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.util.AbstractMocaTestCase;
import com.redprairie.moca.util.MocaUtils;

public class TI_NoBindFilter extends AbstractMocaTestCase {
    
    // Tests concurrently doing a /*#nobind*/ select which had a concurrency issue
    // in it previously reported in MOCA-5909 where this test would result in a NullPointerException
    // due to the BindList being shared as an instance variable in AbstractUnbindFilter
    @Test
    public void testConcurrentNoBind() throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        final CyclicBarrier barrier = new CyclicBarrier(2);

        List<Callable<Void>> callables = new ArrayList<>();
        callables.add(getTestCallable("publish data where bar = 'foo'", "[ /*#nobind*/ select @bar, @bar, @bar, @bar, @bar from dual ]", barrier));
        callables.add(getTestCallable("publish data where foo = 'bar'", "[ /*#nobind*/ select @foo, @foo, @foo, @foo, @foo from dual ]", barrier));
        List<Future<Void>> futures = executor.invokeAll(callables);
        for (Future<Void> future : futures) {
            future.get();
        }
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.MINUTES);
    }
    
    private Callable<Void> getTestCallable(final String publishStatement, final String noBindSelect, final CyclicBarrier barrier) {
        return new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                barrier.await();
                MocaContext moca = MocaUtils.currentContext();
                try {
                    moca.executeInline(publishStatement);
                    for (int i = 0; i < 1000; i++) {
                        moca.executeInline(noBindSelect);
                    }
                }
                finally {
                    moca.commit();
                }
                
                return null;
            }
            
        };
    }
}
