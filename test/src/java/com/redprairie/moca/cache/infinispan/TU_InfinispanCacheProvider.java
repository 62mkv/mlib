/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.cache.infinispan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.TransactionMode;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.advice.SessionAdministrationManager;
import com.redprairie.moca.advice.SessionAdministrationManagerBean;
import com.redprairie.moca.cache.infinispan.loaders.MocaCacheStore;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.TestServerUtils;
import com.redprairie.moca.server.TransactionManagerUtils;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.util.MocaUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class it to test to make sure infinispan cache providers work
 * properly in various cases.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_InfinispanCacheProvider {
    
    private static final String CACHE_NAME = "test";
    private static final String RETURN_CODE = "return_value_coded - ";
    
    private static SystemContext _oldContext;
    private static SystemContext _ourContext;
    
    public static void main(String[] args) throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_InfinispanCacheProvider.class.getName(), 
            false);
        
        String clusterName = args.length > 0 ? args[0] : null;
        
        GlobalConfiguration globalConfig;
        Configuration config;
        if (clusterName != null) {
             globalConfig = GlobalConfigurationBuilder
                 .defaultClusteredBuilder()
                 .build();
             config = new ConfigurationBuilder()
                 .clustering()
                     .cacheMode(CacheMode.REPL_SYNC)
                 .transaction()
                     .transactionManagerLookup(new MocaTransactionManagerLookup())
                     .transactionMode(TransactionMode.TRANSACTIONAL)
                 .build();
        }
        else {
            globalConfig = new GlobalConfigurationBuilder()
                .nonClusteredDefault()
                .build();
            config = new ConfigurationBuilder()
                .clustering()
                    .cacheMode(CacheMode.LOCAL)
                .transaction()
                    .transactionManagerLookup(new MocaTransactionManagerLookup())
                    .transactionMode(TransactionMode.TRANSACTIONAL)
                .build();
        }
        
        DefaultCacheManager cacheManager = new DefaultCacheManager(globalConfig, config);
        
        InfinispanCacheProvider provider1 = new InfinispanCacheProvider(cacheManager);
        System.out.println("Created provider");
        
        ConcurrentMap<String, String> mocaCache1 = provider1.createCache(CACHE_NAME, null, null);
        System.out.println("Created cache");
        
        String value = mocaCache1.get("key");
        System.out.println("Retrieved value: " + value);
        // This is always set
        System.out.print(RETURN_CODE);
        if (value != null) {
            System.out.print(value);
        }
        System.out.println();
        
        System.exit(0);
    }
    
    /**
     * This method will spawn a process and start up an infinispan cluster
     * using the given cluster name or if none will start up a non clustered
     * cache.
     * 
     * Returns the string value in the cache or empty string if there wasn't one
     * @param clusterName
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private static String getCacheValueInNewProcess(String clusterName) throws IOException, InterruptedException {
        String[] vmCommandLine = MocaUtils.newVMCommandLine();
        
        List<String> commandLine = new ArrayList<String>();
        
        commandLine.addAll(Arrays.asList(vmCommandLine));
        commandLine.add("-Djava.net.preferIPv4Stack=true");
        commandLine.add(TU_InfinispanCacheProvider.class.getCanonicalName());
        if (clusterName != null) {
            commandLine.add(clusterName);
        }
        
        ProcessBuilder pb = new ProcessBuilder(commandLine);
        // We redirect error stream so that if an error is printed out
        // we will see that message from the spawned process
        pb.redirectErrorStream(true);
        // We want to remove the trace level, so that we are sure we get the
        // ready signal only
        pb.environment().remove("MOCA_TRACE_LEVEL");
        
        final Process p = pb.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {

            // @see java.lang.Thread#run()
            @Override
            public void run() {
                p.destroy();
            }
        });

        final BufferedReader processOutput = new BufferedReader(
                new InputStreamReader(p.getInputStream(), Charset.defaultCharset()));
        final StringBuilder sb = new StringBuilder();
        
        Thread outputThread = new Thread() {
            @Override
            public void run() {
                try {
                    // We only want the first line output
                    String line;
                    while ((line = processOutput.readLine()) != null) {
                        // We do this to weed out any garbage
                        if (line.startsWith(RETURN_CODE)) {
                            sb.append(line.substring(RETURN_CODE.length()));
                            break;
                        }
                        else {
                            System.out.print("Line - ");
                            System.out.println(line);
                        }
                    }
                }
                catch (IOException e) {
                    sb.append(e);
                }
            }
        };
        
        outputThread.start();
        
        outputThread.join(20000);
        
        if (outputThread.isAlive()) {
            fail("Thread didn't stop correctly!");
        }
        
        return sb.toString();
    }
    
    @BeforeClass
    public static void beforeClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(
            TU_InfinispanCacheProvider.class.getName(), true);
        
        _oldContext = ServerUtils.globalContext();
        
        _ourContext = Mockito.mock(SystemContext.class);
        Mockito.when(_ourContext.getConfigurationElement(
            MocaRegistry.REGKEY_SECURITY_SESSION_KEY_IDLE_TIMEOUT, 
                    MocaRegistry.REGKEY_SECURITY_SESSION_KEY_IDLE_TIMEOUT_DEFAULT)).thenReturn("1800");
        Mockito.when(_ourContext.getVariable(Mockito.anyString())).thenAnswer(new Answer<String>() {

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable {
                return _oldContext.getVariable((String)invocation.getArguments()[0]);
            }
            
        });
        
        //We now close some sessions in our tests, so we need to make sure
        //that we have a SessionAdministrationBean
        SessionAdministrationManager manager = Mockito
            .mock(SessionAdministrationManager.class);
        Mockito.when(
            _ourContext.getAttribute(SessionAdministrationManagerBean.class
                .getName())).thenReturn(manager);
        
        String mocadir = _oldContext.getVariable("MOCADIR");
        // We only return the moca default infinispan config
        Mockito.when(_ourContext.getDataFiles(Mockito.any(FilenameFilter.class), 
            Mockito.eq(true))).thenReturn(new File[]{new File(mocadir + 
                "/data/infinispan.xml")});
        
        TestServerUtils.overrideGlobalContext(_ourContext);
    }
    
    @AfterClass
    public static void afterClass() {
        TestServerUtils.overrideGlobalContext(_oldContext);
    }
    
    @Before
    public void resetProvider() {
        Iterator<EmbeddedCacheManager> iter = 
                InfinispanCacheProvider._cacheManagers.values().iterator();
        while (iter.hasNext()) {
            EmbeddedCacheManager manager = iter.next();
            manager.stop();
            iter.remove();
        }
    }
    
    @Test
    public void testTransactionRollback() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        GlobalConfiguration globalConfig = new GlobalConfigurationBuilder().
                nonClusteredDefault().build();
        Configuration config = new ConfigurationBuilder()
            .clustering()
                .cacheMode(CacheMode.LOCAL)
            .transaction()
                .transactionManagerLookup(new MocaTransactionManagerLookup())
                .transactionMode(TransactionMode.TRANSACTIONAL)
            .build();
        
        DefaultCacheManager cacheManager = new DefaultCacheManager(globalConfig, config);
        try {
            InfinispanCacheProvider provider1 = new InfinispanCacheProvider(
                cacheManager);

            ConcurrentMap<String, String> mocaCache = provider1.createCache(
                CACHE_NAME, null, null);

            final String key = "key";
            final String value = "valueNoCluster";

            TransactionManager manager = TransactionManagerUtils.getManager();

            manager.begin();
            {
                mocaCache.put(key, value);
            }
            
            // This should undo the cache change
            manager.rollback();
            
            manager.begin();
            {
                assertNull(mocaCache.get(key));
            }
            manager.commit();
        }
        finally {
            cacheManager.stop();
        }
    }

    /**
     * This test for whatever reason is being very slow for spawning it's process
     * @throws IOException
     * @throws InterruptedException
     * @throws NotSupportedException
     * @throws SystemException
     * @throws SecurityException
     * @throws IllegalStateException
     * @throws RollbackException
     * @throws HeuristicMixedException
     * @throws HeuristicRollbackException
     */
    @Test
    public void testNoCluster() throws IOException, InterruptedException, 
            NotSupportedException, SystemException, SecurityException, 
            IllegalStateException, RollbackException, HeuristicMixedException, 
            HeuristicRollbackException {
        GlobalConfiguration globalConfig = new GlobalConfigurationBuilder().
                nonClusteredDefault().build();
        Configuration config = new ConfigurationBuilder()
            .clustering()
                .cacheMode(CacheMode.LOCAL)
            .transaction()
                .transactionManagerLookup(new MocaTransactionManagerLookup())
                .transactionMode(TransactionMode.TRANSACTIONAL)
            .build();
        
        DefaultCacheManager cacheManager = new DefaultCacheManager(globalConfig, config);
        try {
            InfinispanCacheProvider provider1 = new InfinispanCacheProvider(
                cacheManager);

            ConcurrentMap<String, String> mocaCache = provider1.createCache(
                CACHE_NAME, null, null);

            final String key = "key";
            final String value = "valueNoCluster";

            TransactionManager manager = TransactionManagerUtils.getManager();

            manager.begin();
            {
                mocaCache.put(key, value);
            }
            manager.commit();

            String processValue = getCacheValueInNewProcess(null);

            manager.begin();
            {
                assertTrue(processValue.isEmpty());
                assertEquals(value, mocaCache.get(key));
            }
            manager.commit();
        }

        finally {
            cacheManager.stop();
        }
    }

    @Ignore
    @Test
    public void testCluster() throws IOException, InterruptedException, 
            SecurityException, IllegalStateException, RollbackException, 
            HeuristicMixedException, HeuristicRollbackException, SystemException, 
            NotSupportedException {
        final String clusterName = "cluster-test";
        GlobalConfiguration globalConfig = 
                GlobalConfigurationBuilder.defaultClusteredBuilder().build();
        Configuration config = new ConfigurationBuilder()
            .clustering()
                .cacheMode(CacheMode.REPL_SYNC)
            .transaction()
                .transactionManagerLookup(new MocaTransactionManagerLookup())
                .transactionMode(TransactionMode.TRANSACTIONAL)
            .build();
        
        DefaultCacheManager cacheManager = new DefaultCacheManager(globalConfig, config);
        InfinispanCacheProvider provider1 = new InfinispanCacheProvider(cacheManager);
        
        ConcurrentMap<String, String> mocaCache = provider1.createCache(CACHE_NAME, 
            null, null);
        
        final String key = "key";
        final String value = "valueCluster";
        
        TransactionManager manager = TransactionManagerUtils.getManager();
        
        manager.begin();
        {
            mocaCache.put(key, value);
        }
        manager.commit();
        
        String processValue = getCacheValueInNewProcess(clusterName);
        
        manager.begin();
        {
            assertEquals(value, processValue);
            
            assertEquals(value, mocaCache.get(key));
            
            // Then we clear out the value and make sure it can see that as well
            mocaCache.remove(key);
        }
        manager.commit();
        
        processValue = getCacheValueInNewProcess(clusterName);
        
        assertTrue(processValue.isEmpty());
        
        manager.begin();
        {
            assertNull(mocaCache.get(key));
        }
        manager.commit();
    }
    
    @Test
    public void testMocaCacheLoader() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException{
        final String cacheName = "moca-test-cache";
        
        GlobalConfiguration globalConfig = 
                GlobalConfigurationBuilder.defaultClusteredBuilder().build();
        Configuration config = new ConfigurationBuilder()
            //Here is the critical point
            //Set up the loader using the moca cache store 
            //to use the legacy CacheController
            .loaders()
                .addStore()
                .cacheStore(new MocaCacheStore<String,String>())
                .addProperty("loaderClass", MocaTestController.class.getName())
            .build();
        
        DefaultCacheManager cacheManager = new DefaultCacheManager(globalConfig, config);
        
        try {
            InfinispanCacheProvider provider1 = new InfinispanCacheProvider(
                cacheManager);

            ConcurrentMap<String, String> mocaCache = provider1.createCache(
                cacheName, null, null);
            TransactionManager manager = TransactionManagerUtils.getManager();

            manager.begin();
            {
                // TestResults determined by MocaTestController.
                assertEquals("bar", mocaCache.get("foo"));
                assertEquals("blah", mocaCache.get("unknownKey"));
            }
            manager.commit();
        }
        finally {
            cacheManager.stop();
        }
    }
    
    @Test
    /***
     * This tests that we properly get nulls back from the cache if the
     * cachestore cannot find the entry in the provided cache controller.
     * 
     * 
     * @throws NotSupportedException
     * @throws SystemException
     * @throws SecurityException
     * @throws IllegalStateException
     * @throws RollbackException
     * @throws HeuristicMixedException
     * @throws HeuristicRollbackException
     */
    public void testMocaCacheLoaderReturnsNull() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException{
        final String cacheName = "moca-test-cache";
        
        GlobalConfiguration globalConfig = 
                GlobalConfigurationBuilder.defaultClusteredBuilder().build();
        Configuration config = new ConfigurationBuilder()
            //Here is the critical point
            //Set up the loader using the moca cache store 
            //to use the legacy CacheController
            .loaders()
                .addStore()
                .cacheStore(new MocaCacheStore<String,String>())
                .addProperty("loaderClass", MocaNullTestController.class.getName())
            .build();
        
        DefaultCacheManager cacheManager = new DefaultCacheManager(globalConfig, config);
        
        try {
            InfinispanCacheProvider provider1 = new InfinispanCacheProvider(
                cacheManager);

            ConcurrentMap<String, String> mocaCache = provider1.createCache(
                cacheName, null, null);
            TransactionManager manager = TransactionManagerUtils.getManager();

            manager.begin();
            {
                // TestResults determined by MocaNullTestController.
                assertNull(mocaCache.get("foo"));
                mocaCache.put("foo", "bar");
                assertEquals("bar", mocaCache.get("foo"));
            }
            manager.commit();
        }
        finally {
            cacheManager.stop();
        }
    }
}
