/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.redprairie.moca.cluster.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

import org.infinispan.Cache;
import org.junit.Test;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.cache.infinispan.InfinispanCacheProvider;
import com.redprairie.moca.cluster.manager.simulator.ClusterManager;
import com.redprairie.moca.cluster.manager.simulator.ClusterNode;
import com.redprairie.moca.server.ServerUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertFalse;

/**
 * Tests for cache operations across a cluster.
 * 
 * Copyright (c) 2014 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author mdobrinin
 */
public class TI_InfinispanCacheTests extends AbstractClusterManagerTest {
    protected final static long BARRIER_TIMEOUT_SECONDS = 3L;
    protected final static long BARRIER_TIMEOUT_SECONDS_LONG = 20L;
    
    protected final static String TEST_CACHE_NAME = "test-cache";
    protected final static String TEST_CACHE_NAME2 = "test-cache-another";
    protected final static String TEST_KEY = "A";
    protected final static String TEST_KEY2 = "B";
    protected final static String TEST_VALUE = "1";
    protected final static String TEST_VALUE2 = "2";
    
    /**
     * Test multiple nodes inserting and removing from a cache simultaneously.
     * The aim of this test is to see is to make sure that we don't deadlock with
     * these operations that happen to be going on at the same time. 
     * @throws Throwable 
     */
    @Test
    public void testInsertAndRemoveWithNodes() throws Throwable {
        final int NUM_NODES = 3;
        final List<Thread> threads = new ArrayList<Thread>();
        
        final CyclicBarrier initBarrier = new CyclicBarrier(NUM_NODES + 1);
        final CyclicBarrier removeBarrier = new CyclicBarrier(NUM_NODES + 1);
        final CyclicBarrier readdBarrier = new CyclicBarrier(NUM_NODES + 1);
        final CyclicBarrier clearBarrier = new CyclicBarrier(NUM_NODES + 1);
        String clusterName = UUID.randomUUID().toString();
        startNewClusterManagerWithLogging(ClusterManager.builder().addNodes(NUM_NODES, newNodeConfiguration(RoleManagerType.FIXED, "", clusterName, false)),
            "TI_InfinispanCacheTests",
            false);
        
        for (ClusterNode node : getManager().getNodes()) {
            final Thread t = new Thread(new WaitingTask(node, initBarrier) {
                @Override
                public void work() {
                    try {
                        // any deadlocks would fail here on the barrier timeouts
                        // check concurrent puts are OK
                        getTestUtils().insertIntoCache(getNode(), TEST_CACHE_NAME2, TEST_KEY, TEST_VALUE);
                        getTestUtils().assertKeyExists(getNode(), TEST_CACHE_NAME2, TEST_KEY, TEST_VALUE);
                        
                        // check concurrent removes are OK
                        removeBarrier.await(BARRIER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        getTestUtils().removeFromCache(getNode(), TEST_CACHE_NAME2, TEST_KEY);
                        getTestUtils().assertKeyDoesNotExist(getNode(), TEST_CACHE_NAME2, TEST_KEY);
                        
                        // readd for next test
                        readdBarrier.await(BARRIER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        getTestUtils().insertIntoCache(getNode(), TEST_CACHE_NAME2, TEST_KEY, TEST_VALUE);
                        getTestUtils().assertKeyExists(getNode(), TEST_CACHE_NAME2, TEST_KEY, TEST_VALUE);
                        
                        // check that concurrent clears are OK
                        clearBarrier.await(BARRIER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        getTestUtils().clearFromCache(getNode(), TEST_CACHE_NAME2);
                        getTestUtils().assertKeyDoesNotExist(getNode(), TEST_CACHE_NAME2, TEST_KEY);
                    }
                    catch (Exception e) {
                        fail(e);
                    }
                }
            });
            
            t.start();
            threads.add(t);
        }
        
        // main thread starts everything off
        initBarrier.await(BARRIER_TIMEOUT_SECONDS_LONG, TimeUnit.SECONDS);
        checkFailure(threads);
        
        // let each thread perform remove operation
        removeBarrier.await(BARRIER_TIMEOUT_SECONDS_LONG, TimeUnit.SECONDS);
        checkFailure(threads);
        
        // readd for next test
        readdBarrier.await(BARRIER_TIMEOUT_SECONDS_LONG, TimeUnit.SECONDS);
        checkFailure(threads);
        
        // let each thread clear then
        // wait for threads to die and check status
        clearBarrier.await(BARRIER_TIMEOUT_SECONDS_LONG, TimeUnit.SECONDS);
        checkFailure(threads);
        for (Thread t : threads) {
            t.join();
        }
        checkFailure(threads);
    }
    
    /**
     * Test multiple threads inserting and removing from a cache simultaneously.
     * The aim of this test is to see is to make sure that we don't deadlock with
     * these operations that happen to be going on at the same time.
     * Since we aren't spawning multiple nodes, this test can be more intense.
     * @throws Throwable 
     */
    @Test
    public void testInsertAndRemoveWithThreads() throws Throwable {
        final int NUM_THREADS = 8;
        final int REPEAT = 3000;
        
        final CyclicBarrier initBarrier = new CyclicBarrier(NUM_THREADS + 1);
        final CyclicBarrier removeBarrier = new CyclicBarrier(NUM_THREADS + 1);
        final CyclicBarrier readdBarrier = new CyclicBarrier(NUM_THREADS + 1);
        final CyclicBarrier clearBarrier = new CyclicBarrier(NUM_THREADS + 1);
        
        final Cache<Object, Object> cache = InfinispanCacheProvider.getInfinispanCacheManager(ServerUtils.globalContext()).getCache(TEST_CACHE_NAME);
        
        for (int iteration = 0; iteration < REPEAT; iteration++) {
            final List<Thread> threads = new ArrayList<Thread>();
            
            for (int i = 0; i < NUM_THREADS; i++) {
                final Thread t = new Thread(new WaitingTask(null, initBarrier) {
                    @Override
                    public void work() {
                        try {
                            // any deadlocks would fail here on the barrier timeouts
                            // check concurrent puts are OK
                            cache.put(TEST_KEY, TEST_VALUE);
                            cache.put(TEST_KEY2, TEST_VALUE2);
                            assertEquals(TEST_VALUE, cache.get(TEST_KEY));
                            assertEquals(TEST_VALUE2, cache.get(TEST_KEY2));
                            
                            // check concurrent removes are OK
                            removeBarrier.await(BARRIER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                            cache.remove(TEST_KEY);
                            cache.remove(TEST_KEY2);
                            assertFalse(cache.containsKey(TEST_KEY));
                            assertFalse(cache.containsKey(TEST_KEY2));
                            
                            // reset to check clears
                            readdBarrier.await(BARRIER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                            cache.put(TEST_KEY, TEST_VALUE);
                            cache.put(TEST_KEY2, TEST_VALUE2);
                            assertEquals(TEST_VALUE, cache.get(TEST_KEY));
                            assertEquals(TEST_VALUE2, cache.get(TEST_KEY2));
                            
                            // check concurrent clears are OK
                            clearBarrier.await(BARRIER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                            cache.clear();
                            assertFalse(cache.containsKey(TEST_KEY));
                            assertFalse(cache.containsKey(TEST_KEY2));
                        }
                        catch (Exception e) {
                            fail(e);
                        }
                    }
                });
                
                threads.add(t);
                t.start();
            }
            
            // main thread starts everything off
            // check if any threads failed and continue with the next operation
            initBarrier.await(BARRIER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            checkFailure(threads);
            
            // let each thread perform remove operation
            removeBarrier.await(BARRIER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            checkFailure(threads);
            
            // let each thread readd again
            readdBarrier.await(BARRIER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            checkFailure(threads);
            
            // let each thread clear then
            // wait for threads to die and check status
            clearBarrier.await(BARRIER_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            checkFailure(threads);
            for (Thread t : threads) {
                t.join();
            }
            checkFailure(threads);
            
            initBarrier.reset();
            removeBarrier.reset();
            readdBarrier.reset();
            clearBarrier.reset();
        }
    }
    
    /**
     * Test that components that make up with test work.
     * @throws MocaException 
     * @throws IOException 
     */
    @Test
    public void testSelf() throws Throwable {
        final int NUM_NODES = 1;
        String clusterName = UUID.randomUUID().toString();
        startNewClusterManagerWithLogging(ClusterManager.builder().addNodes(NUM_NODES, newNodeConfiguration(RoleManagerType.FIXED, clusterName,"", false)),
            "TI_InfinispanCacheTests",
            false);
        
        final Collection<ClusterNode> nodes = getManager().getNodes();
        
        assertEquals(1, nodes.size());
        final ClusterNode node = nodes.iterator().next();
        Object o;
        
        assertNull(getTestUtils().getFromCache(node, TEST_CACHE_NAME, TEST_KEY));
        getTestUtils().assertKeyDoesNotExist(node, TEST_CACHE_NAME, TEST_KEY);
        
        getTestUtils().insertIntoCache(node, TEST_CACHE_NAME, TEST_KEY, TEST_VALUE);
        o = getTestUtils().getFromCache(node, TEST_CACHE_NAME, TEST_KEY);
        assertEquals(TEST_VALUE, o);
        getTestUtils().assertKeyExists(node, TEST_CACHE_NAME, TEST_KEY, TEST_VALUE);
        
        getTestUtils().removeFromCache(node, TEST_CACHE_NAME, TEST_KEY);
        assertNull(getTestUtils().getFromCache(node, TEST_CACHE_NAME, TEST_KEY));
        getTestUtils().assertKeyDoesNotExist(node, TEST_CACHE_NAME, TEST_KEY);
    }

    /**
     * Check for failure. This should only be called from the main thread.
     * @param threads 
     * @throws Throwable
     */
    private void checkFailure(List<Thread> threads) throws Throwable {
        if (errorCondition != null) {
            for (Thread t : threads) {
                t.interrupt();
            }
            throw new AssertionError(errorCondition);
        }
    }
    
    /**
     * Class that waits on other threads to do its work so that
     * each waiting thread starts to do its work at the same time.
     * It can also report failures to the main thread that is running the tests.
     */
    private static abstract class WaitingTask implements Runnable {
        
        /**
         * Method to implement for actual work.
         */
        public abstract void work();
        
        /**
         * Create a WaitingTask for a node.
         * @param node
         * @param init barrier to wait on before doing any work
         */
        public WaitingTask(ClusterNode node, CyclicBarrier init) {
            _node = node;
            _initBarrier = init;
        }
        
        /**
         * Get the node for this task
         * @return
         */
        public ClusterNode getNode() {
            return _node;
        }
        
        @Override
        public void run() {
            try {
                _initBarrier.await(BARRIER_TIMEOUT_SECONDS_LONG, TimeUnit.SECONDS);
                work();
            }
            catch (Exception e) {
                fail(e);
            }
            catch (AssertionError e) {
            	fail(e);
            }
        }
        
        /**
         * Mark a failure on the thread, this will be used to report failure
         * to the mail JUnit thread
         * @param e failure
         */
        protected void fail(Throwable e) {
            errorCondition = e;
        }
        
        private ClusterNode _node;
        private CyclicBarrier _initBarrier;
    }
    
    /**
     * Marker for when an error has occurred in another thread.
     */
    private volatile static Throwable errorCondition = null;
}
