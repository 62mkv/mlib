/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.cache.infinispan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.config.ConfigurationException;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ConfigurationParser;
import org.infinispan.configuration.parsing.Namespace;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;
import org.infinispan.transaction.LockingMode;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.DummyTransactionManagerLookup;
import org.infinispan.util.FileLookup;
import org.infinispan.util.FileLookupFactory;
import org.infinispan.util.Util;
import org.jboss.staxmapper.XMLMapper;

import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.cache.CacheController;
import com.redprairie.moca.cache.MocaCacheProvider;
import com.redprairie.moca.cluster.infinispan.GlobalListener;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.util.SimpleFilenameFilter;

/**
 * This is a cache provider that will create infinispan clustered cache instanaces
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class InfinispanCacheProvider implements MocaCacheProvider {
    
    public InfinispanCacheProvider() {
        this(ServerUtils.globalContext(), null);
    }
    
    public InfinispanCacheProvider(SystemContext context, GlobalListener listener) {
        _manager = getInfinispanCacheManager(context, listener);
    }
    
    InfinispanCacheProvider(EmbeddedCacheManager manager) {
        _manager = manager;
    }
    
    private static class DummySerializable implements Serializable {
        private static final long serialVersionUID = 1L;
    }
    
    /**
     * This method will return the actual infinispan named cache and will not be
     * wrapped by the MocaCache framework.
     * @param <K>
     * @param <V>
     * @param name The named cache to retrieve
     * @return The infinispan Cache object for the given name
     */
    public <K, V> Cache<K, V> getCache(String name) {
        return _manager.getCache(name);
    }
    
    // @see com.redprairie.moca.cache.MocaCacheProvider#createCache(java.lang.String, java.util.Map, com.redprairie.moca.cache.CacheController)
    @Override
    public <K, V> ConcurrentMap<K, V> createCache(String name,
                                                  Map<String, String> params,
                                                  CacheController<K, V> controller) {
        Cache<K, V> cache = _manager.getCache(name);
        return cache;
    }
    
    public synchronized static EmbeddedCacheManager getInfinispanCacheManager(
        SystemContext context) {
        return getInfinispanCacheManager(context, null);
    }
    
    /**
     * This will give an EmbeddedCacheManager for the provided SystemContext
     * @param context
     * @return
     */
    public synchronized static EmbeddedCacheManager getInfinispanCacheManager(
        SystemContext context, GlobalListener listener) {
        
        EmbeddedCacheManager cacheManager = _cacheManagers.get(context);
        
        if (cacheManager == null) {
            Boolean taskModeBoolean = (Boolean)context.getAttribute(
                ServerUtils.TASK_MODE);
            boolean taskMode = taskModeBoolean != null ? taskModeBoolean : false;
            
            if (!taskMode) {
                // To prevent proliferation of empty session files we delete
                // empty ones upon startup.
                String lesDir = context.getVariable("LESDIR");
                File mocaSessionsDir = new File(new File(new File(lesDir),
                                "data"), "moca-sessions");
                if (mocaSessionsDir.isDirectory()) {
                    File[] mocaSessionFiles = mocaSessionsDir.listFiles();
                    for (File mocaSessionFile : mocaSessionFiles) {
                        if (mocaSessionFile.length() == 0
                                && !mocaSessionFile.delete()) {
                            _logger.warn("Unable to delete file ["
                                    + mocaSessionFile.getAbsolutePath()
                                    + "] server may not start properly.  "
                                    + "Please remove file manually.");
                        }
                    }
                }
            }
            
            // The following code allows for products to override the default
            // and named cached configurations - We always have our set global settings
            File[] files = context.getDataFiles(new SimpleFilenameFilter(
                "infinispan.xml"), true);
            
            List<String> fileNames = new ArrayList<String>(files.length);
            // Now we parse all files and pass in the same holder to override
            for (int i = 0; i < files.length; ++i) {
                fileNames.add(files[i].getAbsolutePath());
            }
            if (_logger.isDebugEnabled()) {
                _logger.debug("Parsing infinispan files " + fileNames);
            }
            ConfigurationBuilderHolder holder = configureInfinispanConfiguration(
                    fileNames, Thread.currentThread().getContextClassLoader());
            
            if (taskMode) {
                // If we are a task then we blow any cache loaders for the moca-sessions
                // cache
                ConfigurationBuilder sessionConfigBuilder = 
                        holder.getNamedConfigurationBuilders().get(
                            "moca-sessions");
                // Clear out the cache loaders cause we don't want people
                // reading/writing to the disk for tasks
                sessionConfigBuilder
                    .loaders()
                        .clearCacheLoaders();
                // Following is commented out until
                // https://issues.jboss.org/browse/ISPN-2853 can be fixed
                // else asymmetric clusters won't work properly causing issues.
//                disableClusteringSettings(sessionConfigBuilder);
//                
//                // Now we disable all the role based stuff for tasks
//                ConfigurationBuilder mocaCurrentRolesBuilder = 
//                        holder.getNamedConfigurationBuilders().get(
//                            "moca-current-roles");
//                disableClusteringSettings(mocaCurrentRolesBuilder);
//                
//                ConfigurationBuilder mocaForcedRolesBuilder = 
//                        holder.getNamedConfigurationBuilders().get(
//                            "moca-forced-roles");
//                disableClusteringSettings(mocaForcedRolesBuilder);
//                
//                ConfigurationBuilder mocaSingleForcedRolesBuilder = 
//                        holder.getNamedConfigurationBuilders().get(
//                            "moca-single-forced-roles");
//                disableClusteringSettings(mocaSingleForcedRolesBuilder);
//                
//                ConfigurationBuilder mocaNodeUrlsBuilder = 
//                        holder.getNamedConfigurationBuilders().get(
//                            "moca-node-urls");
//                disableClusteringSettings(mocaNodeUrlsBuilder);
//                
//                ConfigurationBuilder mocaJobBuilder = 
//                        holder.getNamedConfigurationBuilders().get(
//                            "moca-job-cache");
//                disableClusteringSettings(mocaJobBuilder);
//                
//                ConfigurationBuilder mocaTaskBuilder = 
//                        holder.getNamedConfigurationBuilders().get(
//                            "moca-task-cache");
//                disableClusteringSettings(mocaTaskBuilder);
            }
            else {
                // We override the session key idle timeout for moca-sessions
                // cache manually.
                int timeout = Integer.parseInt(context.getConfigurationElement(
                    MocaRegistry.REGKEY_SECURITY_SESSION_KEY_IDLE_TIMEOUT, 
                    MocaRegistry.REGKEY_SECURITY_SESSION_KEY_IDLE_TIMEOUT_DEFAULT));
                
                // We set the moca-sessions to the registry key, no one can
                // override in the configuration unfortunately
                ConfigurationBuilder sessionConfigBuilder = 
                        holder.getNamedConfigurationBuilders().get("moca-sessions");
                // This can't be null or else things won't work properly
                sessionConfigBuilder.expiration().maxIdle(timeout * 1000l);
            }
            
            String clusterName = context.getConfigurationElement(
                MocaRegistry.REGKEY_CLUSTER_NAME);
            GlobalConfigurationBuilder globalBuilder = holder.getGlobalConfigurationBuilder();
            // If the context doesn't contain a cluster name just use a normal infinispan cache
            if (clusterName == null) {
                globalBuilder.nonClusteredDefault();
                globalBuilder.globalJmxStatistics().cacheManagerName("MOCA-Local Cache");
                globalBuilder.globalJmxStatistics().jmxDomain("com.redprairie.moca.cache");
            }
            // If we have a cluster name, configure the transport with
            // required info
            else {
                globalBuilder.clusteredDefault();
                globalBuilder.globalJmxStatistics().cacheManagerName("MOCA-Distributed Cache");
                globalBuilder.globalJmxStatistics().jmxDomain("com.redprairie.moca.cache.cluster");
                globalBuilder.transport().clusterName(clusterName);
                
                globalBuilder.transport().addProperty(
                    JGroupsTransport.CHANNEL_LOOKUP, 
                    MocaJGroupsChannelLookup.class.getName());
            }
            
            // This is only used for non local
            String configTestCacheName = "moca-infinispan-caches-check";
            // If it is local we have to override all the caches
            // to be local as well
            if (clusterName == null) {
                disableClusteringSettings(holder.getDefaultConfigurationBuilder());
                
                // We also override all named caches that were configured
                // in the XML to be local
                
                for (ConfigurationBuilder configBuilder : 
                    holder.getNamedConfigurationBuilders().values()) {
                    disableClusteringSettings(configBuilder);
                }
            }
            // If cluster then add another cluster cache check cache
            else {
                ConfigurationBuilder builder = holder.newConfigurationBuilder(configTestCacheName);
                builder.clustering().cacheMode(CacheMode.REPL_SYNC);
                builder.transaction().transactionManagerLookup(new DummyTransactionManagerLookup());
                builder.transaction().transactionMode(TransactionMode.TRANSACTIONAL);
                builder.transaction().lockingMode(LockingMode.PESSIMISTIC);
                builder.clustering().stateTransfer().fetchInMemoryState(true);
            }
            
            cacheManager = new DefaultCacheManager(
                holder, false);
            
            if (listener != null) {
                cacheManager.addListener(listener);
            }
            
            cacheManager.start();
            
            // Finally we start all the caches, we can't piece meal start
            // them as infinispan will complain about asymmetric clusters
            Set<String> cacheNameSet = holder.getNamedConfigurationBuilders().keySet();
            String[] cacheNames = cacheNameSet.toArray(new String[cacheNameSet.size()]);
            
            cacheManager.startCaches(cacheNames);
            
            // Now we do a check if clustered to make sure the configurations
            // each have the same number of caches
            if (clusterName != null) {
                Cache<String, Object> cache = cacheManager.getCache(configTestCacheName);
                
                AdvancedCache<String, Object> advancedCache = cache.getAdvancedCache();
                
                TransactionManager manager = advancedCache.getTransactionManager();
                try {
                    manager.begin();
                }
                // These shouldn't happen
                catch (NotSupportedException e) {
                    throw new RuntimeException(e);
                }
                catch (SystemException e) {
                    throw new RuntimeException(e);
                }
                
                try {
                    // All nodes have to lock the test named cache to ensure
                    // another node isn't updating the map at the same time
                    // and we can guarantee # of cache names matches others
                    advancedCache.lock(configTestCacheName);
                    
                    if (!advancedCache.isEmpty()) {
                        if (advancedCache.size() != cacheNames.length) {
                            throw new RuntimeException(
                                "The configuration doesn't have the same" +
                                " number of configured named infinispan " +
                                "caches (" + cacheNames.length + 
                                ") that the cluster has (" + 
                                advancedCache.size() + ")!");
                        }
                        for (String cacheName : cacheNames) {
                            if (!advancedCache.containsKey(cacheName)) {
                                throw new RuntimeException(
                                    "The configuration doesn't contain " +
                                    "named infinispan cache " + cacheName + 
                                    " that the cluster has!");
                            }
                        }
                    }
                    else {
                        Object obj = new DummySerializable();
                        for (String cacheName : cacheNames) {
                            advancedCache.put(cacheName, obj);
                        }
                    }
                }
                finally {
                    try {
                        manager.commit();
                    }
                    // None of these exceptions should happen unless
                    // a different exception occurred possibly in the try
                    catch (SecurityException e) {
                        _logger.error("There was a problem updating " +
                            "internal infinispan configuration check!", e);
                    }
                    catch (RollbackException e) {
                        _logger.error("There was a problem updating " +
                            "internal infinispan configuration check!", e);
                    }
                    catch (HeuristicMixedException e) {
                        _logger.error("There was a problem updating " +
                            "internal infinispan configuration check!", e);
                    }
                    catch (HeuristicRollbackException e) {
                        _logger.error("There was a problem updating " +
                            "internal infinispan configuration check!", e);
                    }
                    catch (SystemException e) {
                        _logger.error("There was a problem updating " +
                            "internal infinispan configuration check!", e);
                    }
                }
            }
            _cacheManagers.put(context, cacheManager);
        }
        return cacheManager;
    }
    
    private static void disableClusteringSettings(ConfigurationBuilder builder) {
        // We cannot disable hash configuration, so someone will break if
        // they are using that atm - need to change that
        builder
            .clustering()
                .cacheMode(CacheMode.LOCAL)
                .l1()
                    .disable()
                    .onRehash(false)
                .sync();
    }
    
    private static ConfigurationBuilderHolder configureInfinispanConfiguration(
        List<String> fileNames, ClassLoader cl) {
        XMLMapper xmlMapper = XMLMapper.Factory.create();
        @SuppressWarnings("rawtypes")
        ServiceLoader<ConfigurationParser> parsers = ServiceLoader.load(ConfigurationParser.class, cl);
        for (ConfigurationParser<?> parser : parsers) {
           for (Namespace ns : parser.getSupportedNamespaces()) {
              xmlMapper.registerRootElement(new QName(ns.getUri(), ns.getRootElement()), parser);
           }
        }
        
        ConfigurationBuilderHolder holder = new ConfigurationBuilderHolder(cl);
        
        try {
            FileLookup fileLookup = FileLookupFactory.newInstance();
            for (String fileName : fileNames) {
                InputStream is = fileLookup.lookupFile(fileName, cl);
                if(is==null) {
                    throw new ConfigurationException(
                        new FileNotFoundException(fileName));
                }
                try {
                    BufferedInputStream input = new BufferedInputStream(is);
                    XMLStreamReader streamReader = XMLInputFactory.newInstance().createXMLStreamReader(input);
                    xmlMapper.parseDocument(holder, streamReader);
                    streamReader.close();
                } finally {
                    Util.close(is);
                }
            }
        }
        catch (XMLStreamException e) {
            throw new ConfigurationException(e);
        }
        
        return holder;
    }
    
    final EmbeddedCacheManager _manager;
    
    final static Map<SystemContext, EmbeddedCacheManager> _cacheManagers = 
        new ConcurrentHashMap<SystemContext, EmbeddedCacheManager>();
    private final static Logger _logger = LogManager.getLogger(InfinispanCacheProvider.class);
}
