/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.sam.moca.server.exec;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.moca.MocaException;
import com.sam.moca.MocaInterruptedException;
import com.sam.moca.MocaRegistry;
import com.sam.moca.advice.ServerContextConfig;
import com.sam.moca.db.QueryHook;
import com.sam.moca.server.DefaultMessageResolver;
import com.sam.moca.server.ServerContextFactory;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.SpringTools;
import com.sam.moca.server.SystemConfigurationException;
import com.sam.moca.server.db.DBAdapter;
import com.sam.moca.server.dispatch.MessageResolver;
import com.sam.moca.server.legacy.NativeAdapterFactory;
import com.sam.moca.server.legacy.NativeProcessPool;
import com.sam.moca.server.legacy.NativeProcessPoolBuilder;
import com.sam.moca.server.legacy.NativeProcessTimeoutException;
import com.sam.moca.server.legacy.SingleProcessNativeAdapterFactory;
import com.sam.moca.server.legacy.socket.SocketNativeAdapterFactory;
import com.sam.moca.server.profile.CommandUsage;
import com.sam.moca.server.profile.InMemoryCommandUsage;
import com.sam.moca.server.profile.NullCommandUsage;
import com.sam.moca.server.profile.PersistedCommandUsage;
import com.sam.moca.server.repository.CommandRepository;
import com.sam.moca.server.repository.ComponentLevel;
import com.sam.moca.util.MocaUtils;

/**
 * A Factory class that sets up the server context for a 
 * given servlet container.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class DefaultServerContextFactory implements ServerContextFactory {

    public DefaultServerContextFactory(String name, SystemContext system, 
            boolean singleProcess, NativeProcessPoolBuilder poolBuilder)
            throws SystemConfigurationException {
        _name = name;
        _system = system;
        
        _script = configureScriptAdapter();
        
        QueryHook queryHook = SpringTools.getLastBeanStoredInDataFiles(
                    system, "hooks.xml", QueryHook.class);

        _jdbc = ServerContextConfig.dbAdapter(system, queryHook);
        _stats = configureCommandUsage();
        
        _repositoryRef.set(configureCommandRepository());
        if (poolBuilder != null) {
            _nativeFactory = configureNativeFactory(singleProcess, poolBuilder);
        }
        else {
            _nativeFactory = null;
        }
        
        _blacklistedArgs = configureBlacklistedArgs();
        _messageResolver = configureMessageResolver();
        _connectionFactory = configureRemoteConnectionFactory();
    }
    
    @Override
    public void initialize() throws SystemConfigurationException {
        try {
            _nativeFactory.initialize();
            _nativePool = _nativeFactory.getNativeProcessPool();
        }
        catch (MocaException e) {
            throw new SystemConfigurationException("There was a problem " +
            		"initializing the native factory", e);
        }
        
        initializeRepository();
    }

    private NativeAdapterFactory configureNativeFactory(boolean singleProcess, 
            NativeProcessPoolBuilder poolBuilder) throws SystemConfigurationException {
        try {
            NativeAdapterFactory nativeFactory;
            
            String processTimeoutStr = _system.getConfigurationElement(
                MocaRegistry.REGKEY_SERVER_PROCESS_TIMEOUT,
                MocaRegistry.REGKEY_SERVER_PROCESS_TIMEOUT_DEFAULT);
            int processTimeout = Integer.parseInt(processTimeoutStr);
            
            if (singleProcess) {
                nativeFactory = new SingleProcessNativeAdapterFactory(poolBuilder, 
                        processTimeout, _repositoryRef.get());
            }
            else {
                nativeFactory = new SocketNativeAdapterFactory(poolBuilder, 
                        processTimeout, _system, _repositoryRef.get());
            }
            return nativeFactory;
        }
        catch (MocaException e) {
            throw new SystemConfigurationException("Unable to set up remote pool: " + e, e);
        }
    }

    /**
     * This method should be protected by synchronizing on 
     * {@link DefaultServerContextFactory#_repositoryRef} if the reference
     * was changed in any way before calling this to guarantee the correct
     * reference is used.
     * @throws SystemConfigurationException
     */
    private void initializeRepository() throws SystemConfigurationException {
        // Initialize command repository
        _logger.debug("Initializing Component Libraries");
        ServerContext ctx = newContext(null, null);
        
        // We want to backup the old context
        ServerContext oldCtx;
        try {
            oldCtx = ServerUtils.getCurrentContext();
        }
        catch (IllegalStateException e) {
            // This is okay if a current context is not present, we just use null
            // which will require people to set it later
            oldCtx = null;
        }
        // Now we link the temporary context in case if someone wants to 
        // execute a command in library initialization
        ServerUtils.setCurrentContext(ctx);
        try {
            for (ComponentLevel level : _repositoryRef.get().getLevels()) {
                _logger.debug(MocaUtils.concat("Initializing component library ", level.getName()));
                try {
                    level.initialize(ctx);
                }
                catch (NativeProcessTimeoutException e) {
                    // This way if we can't spawn a native process upon
                    // start up then we fail fast.
                    throw new SystemConfigurationException(
                            "Error spawning native process.", e);
                }
                catch (MocaException e) {
                   _logger.warn(MocaUtils.concat("Error Initializing component library ", level.getName()), e);
                }
            }
        }
        finally {
            ctx.close();
            // We restore back the old context
            ServerUtils.setCurrentContext(oldCtx);
        }
    }

    // @see com.sam.moca.server.ServerContextFactory#restart()
    @Override
    public void restart(boolean clean) throws MocaException {
        
        // This is synchronized so that the restart will initialize all
        // the repository and native factory before another person can change it.
        synchronized (_repositoryRef) {
            try {
                CommandRepository repo = configureCommandRepository();
                if (repo != null) {
                    _repositoryRef.set(repo);
                    initializeRepository();
                    _logger.info("Resetting command profile since a new " +
                            "commands.mem file has been loaded");
                    // Reset the old stats to clear up memory and in case if
                    // it isn't garbage collected so only some data will be
                    // written.
                    _stats.reset();
                    _stats = configureCommandUsage();
                }
            }
            catch (SystemConfigurationException e) {
                throw new MocaException(9999);
            }
            
            // We restart the pool each time.
            _nativeFactory.restart(clean, _repositoryRef.get());
        }
    }
    
    // @see com.sam.moca.server.ServerContextFactory#newContext(com.sam.moca.server.exec.RequestContext, com.sam.moca.server.exec.SessionContext)
    @Override
    public ServerContext newContext(RequestContext req, SessionContext session) {
        if (session != null) {
            session.putAttribute("commandUsage", _stats);
        }

        return ServerContextConfig.serverContext(
                _script, _jdbc, session, 
            _system, req, _repositoryRef.get(), _nativeFactory, 
            _stats, _blacklistedArgs, _messageResolver, _connectionFactory);
    }
    
    private RemoteConnectionFactory configureRemoteConnectionFactory() {
        return new DefaultRemoteConnectionFactory();
    }

    /**
     * Loads the command repository for the server.  This will return null
     * if the command repository doesn't need to be reloaded since the memory
     * file timestamp is not updated
     * @return the new command repository
     * @throws SystemConfigurationException
     */
    private CommandRepository configureCommandRepository()
            throws SystemConfigurationException {
        
        String memoryFile = _system.getConfigurationElement(MocaRegistry.REGKEY_SERVER_MEMORY_FILE);
        
        File file = new File(memoryFile);
        
        long lastModified = file.lastModified();
        
        Long previousValue = _memoryFileDate.getAndSet(lastModified);
        
        CommandRepository repository = null;
        if (previousValue == null || previousValue != lastModified) {
            ObjectInputStream in = null;
            try {
                in = new ObjectInputStream(new BufferedInputStream(
                    new FileInputStream(memoryFile)));
                
                repository = (CommandRepository) in.readObject();
                _logger.debug(MocaUtils.format("Component Levels: %4d", repository
                    .getLevelCount()));
                _logger.debug(MocaUtils.format("Commands: %4d", repository
                    .getCommandCount()));
                _logger.debug(MocaUtils.format("Triggers: %4d", repository
                    .getTriggerCount()));
            }
            catch (InterruptedIOException e) {
                throw new MocaInterruptedException(e);
            }
            catch (IOException e) {
                _logger.error("Error reading command file", e);
                throw new SystemConfigurationException("Error reading file: "
                        + memoryFile + ": " + e, e);
            }
            catch (ClassNotFoundException e) {
                _logger.error("Error reading command file", e);
                throw new SystemConfigurationException("Error reading file: "
                        + memoryFile + ": " + e, e);
            }
            finally {
                if (in != null) try {
                    in.close();
                }
                catch (IOException ignore) {
                }
            }
        }
        return repository;
    }
    
    private CommandUsage configureCommandUsage() {
        String profilePrefix = _system.getConfigurationElement(MocaRegistry.REGKEY_SERVER_COMMAND_PROFILE);
        
        if (profilePrefix == null) {
            // By default do some profiling of the running process only.
            return new InMemoryCommandUsage();
        }
            
        // If profiling is explicitly turned off, don't do anything
        if (profilePrefix.equalsIgnoreCase("off")) {
            return new NullCommandUsage();
        }
        
        // If a profile is set up, use the "process name" to separate
        // profiles for the main server and server-mode tasks. 
        String filename = profilePrefix + "-" + _name + ".csv";
        File profileLogFile = new File(filename);
        return new PersistedCommandUsage(profileLogFile);
    }
    
    private Collection<String> configureBlacklistedArgs() {
        Collection<String> set = new HashSet<String>();
        
        String args = _system.getConfigurationElement(MocaRegistry.REGKEY_SERVER_ARG_BLACKLIST);
        if (args != null) {
            for (String arg : args.split(",")) {
                set.add(arg.trim());
            }
        }
        
        return set;
    }
    
    private MessageResolver configureMessageResolver() {
        MessageResolver resolver = 
            SpringTools.getLastBeanStoredInDataFiles(_system, 
                "hooks.xml", MessageResolver.class);
        if (resolver == null) {
            resolver = new DefaultMessageResolver();
        }
        return resolver; 
    }

    @Override
    public <T> T getHook(Class<T> cls) {
        T hook = SpringTools.getLastBeanStoredInDataFiles(_system, "hooks.xml", cls);

        return hook;
    }
    
    private ScriptAdapter configureScriptAdapter() {
        List<String> scriptDirs = new ArrayList<String>();
        
        String pathSeparatedConfigDir = _system.getConfigurationElement(MocaRegistry.REGKEY_SERVER_PROD_DIRS);
        
        if (pathSeparatedConfigDir != null) {
            String[] prodDirs = pathSeparatedConfigDir.split(File.pathSeparator);
        
            // Now loop through all the configuration directories, so we can find
            // which ones contain the xml and add it to our list
            for (String prodDir : prodDirs) {
                File scriptDir = new File(prodDir + File.separator + "src" + File.separator + "groovy");
                if (scriptDir.exists() && scriptDir.isDirectory()) {
                    scriptDirs.add(scriptDir.toString());
                }
            }
        }

        return new GroovyScriptAdapter(scriptDirs);
    }
    
    // @see com.sam.moca.server.ServerContextFactory#associateContext(com.sam.moca.server.exec.RequestContext, com.sam.moca.server.exec.SessionContext)
    @Override
    public void associateContext(RequestContext req, SessionContext session) {
        // This currently does nothing, but check if anyone hooks into this
    }
    
    // @see com.sam.moca.server.ServerContextFactory#getNativePool()
    @Override
    public NativeProcessPool getNativePool() {
        return _nativePool;
    }

    // @see com.sam.moca.server.ServerContextFactory#getDBAdapter()
    @Override
    public DBAdapter getDBAdapter() {
        return _jdbc;
    }
    
    // @see com.sam.moca.server.ServerContextFactory#getCommandUsage()
    @Override
    public CommandUsage getCommandUsage() {
        return _stats;
    }
    
    // @see com.sam.moca.server.ServerContextFactory#close()
    @Override
    public void close() {
        try {
            _jdbc.close();
        }
        catch (SQLException e) {
            _logger.warn("Problem shutting down jdbc adapter!", e);
        }
        
        _nativeFactory.close();
    }
    
    private AtomicReference<Long> _memoryFileDate = new AtomicReference<Long>();
    private NativeProcessPool _nativePool = null;
    private CommandUsage _stats;
    private AtomicReference<CommandRepository> _repositoryRef = 
        new AtomicReference<CommandRepository>();
    private final String _name;
    private final DBAdapter _jdbc;
    private final static Logger _logger = LogManager.getLogger(
            DefaultServerContextFactory.class);
    private final SystemContext _system;
    private final ScriptAdapter _script;
    private final NativeAdapterFactory _nativeFactory;
    private final Collection<String> _blacklistedArgs;
    private final MessageResolver _messageResolver;
    private final RemoteConnectionFactory _connectionFactory;
}
