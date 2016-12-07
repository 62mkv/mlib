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

package com.redprairie.moca.server.legacy.socket;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.Level;

import com.redprairie.mad.server.MadServerStart;
import com.redprairie.moca.Builder;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.legacy.AbstractNativeAdapterFactory;
import com.redprairie.moca.server.legacy.ContextMocaServerAdapter;
import com.redprairie.moca.server.legacy.MocaNativeCommunicationException;
import com.redprairie.moca.server.legacy.MocaServerAdapter;
import com.redprairie.moca.server.legacy.NativeProcess;
import com.redprairie.moca.server.legacy.NativeProcessPoolBuilder;
import com.redprairie.moca.server.legacy.RemoteNativeProcess;
import com.redprairie.moca.server.log.TraceState;
import com.redprairie.moca.server.log.TraceUtils;
import com.redprairie.moca.server.log.exceptions.LoggingException;
import com.redprairie.moca.server.repository.CommandRepository;
import com.redprairie.moca.util.ConcatString;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.CommandLineParser;
import com.redprairie.util.ProcessWatcher;

/**
 * Native process factory that communicates with its children over a simple TCP 
 * stream socket.  The socket is used for high-performance remote protocol handling.
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 * @version $Revision$
 */
public class SocketNativeAdapterFactory extends AbstractNativeAdapterFactory {
    
    /**
     * 
     */
    public SocketNativeAdapterFactory(NativeProcessPoolBuilder poolBuilder, 
            int processTimeout, SystemContext sys, CommandRepository repos) throws MocaException {
        super(poolBuilder.pooltimeout(processTimeout, TimeUnit.SECONDS), repos);
        _processTimeout = processTimeout;
        
        String jvm = sys.getConfigurationElement(MocaRegistry.REGKEY_JAVA_VM32);
        if (jvm == null) {
            jvm = sys.getConfigurationElement(MocaRegistry.REGKEY_JAVA_VM);
        }

        if (jvm != null) {
            _javaCmd = jvm;
        }
        else {
            _javaCmd = "java";
        }
        
        String jvmArgs = sys.getConfigurationElement(MocaRegistry.REGKEY_JAVA_VMARGS32);
        if (jvmArgs == null) {
            jvmArgs = sys.getConfigurationElement(MocaRegistry.REGKEY_JAVA_VMARGS);
        }
        if (jvmArgs != null) {
            _javaVMArgs = CommandLineParser.split(jvmArgs);
        }
        else {
            _javaVMArgs = new ArrayList<String>();
        }
        
        _environment = sys.getConfigurationSection(MocaRegistry.REGSEC_ENVIRONMENT, true);

        try {
            // We create a new server socket channel so we can listen
            // for client socket connections.
            ServerSocketChannel sc = ServerSocketChannel.open();
            _sock = sc.socket();
            // Now we bind to an emphemeral port.
            _sock.bind(null);
            // Save off the port so we can send to our native processes.
            _port = _sock.getLocalPort();
            _logger.debug(MocaUtils.concat(
                    "Native adapter: listening on address localhost:",
                    _port));
            
            // Start up a socket server for just error propagation
            ServerSocketChannel sc2 = ServerSocketChannel.open();
            _sockCrash = sc2.socket();
            // Now we bind to an emphemeral port.
            _sockCrash.bind(null);
            // Save off the port so we can send to our native processes.
            _portCrash = _sockCrash.getLocalPort();
            _logger.debug(MocaUtils.concat(
                    "Native adapter: crash-listener on address localhost:",
                    _portCrash));
            
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new MocaNativeCommunicationException("Unable to create native socket: " + e, e);
        }
        
        // Create a thread to handle incoming native process requests
        _socketWatcher = new Thread(new ServerSocketWatcher(), "NativeSocketWatcher");
        
        // We only want the watcher as a daemon so it won't keep our
        // JVM up if it is the only one.
        _socketWatcher.setDaemon(true);
        
        // Create a thread to handle incoming native socket crash connections
        _socketCrashWatcher = new Thread(new CrashSocketWatcher(), "NativeSocketCrashWatcher");
        
        // We only want the watcher as a daemon so it won't keep our
        // JVM up if it is the only one.
        _socketCrashWatcher.setDaemon(true);
        
    }
    
    // @see com.redprairie.moca.server.legacy.AbstractNativeAdapterFactory#setup()
    @Override
    protected void setup() throws MocaException {
        _socketWatcher.start();
        _socketCrashWatcher.start();
    }
    
    private class OurBuilder implements Builder<NativeProcess> {

        @Override
        public NativeProcess build() {
            final String processID = getNextID();
            
            List<String> commandLine = new ArrayList<String>();
            commandLine.add(_javaCmd);
            
            if (_environment.containsKey("CLASSPATH")) {
                commandLine.add("-classpath");
                commandLine.add(_environment.get("CLASSPATH"));
            }   

            for (String vmArg : _javaVMArgs) {
                commandLine.add(vmArg);
            }
            
            // We need to add -Xrs to all Windows versions < 6.0.             
            try {
                String osName = System.getProperty("os.name").toLowerCase(); 
                Double osVersion = Double.parseDouble(System.getProperty("os.version"));   
                
                if (osName.contains("windows") && osVersion < 6.0)
                    commandLine.add("-Xrs");
            }
            catch (NumberFormatException ignore) {
                // Ignore exceptions raised parsing the OS version.
                ;
            }
            
            commandLine.add(SocketNativeProcessMain.class.getName());
            commandLine.add(String.valueOf(_port));
            commandLine.add(String.valueOf(_portCrash));
            commandLine.add(processID);
            
            ProcessBuilder builder = new ProcessBuilder(commandLine);
            builder.redirectErrorStream(true);
            Map<String, String> env = builder.environment();

            // Remove commonly used path variables with different case.
            env.remove("Path");
            env.remove("path");
            
            // Put the PATH variable back, this time in upper case
            env.put("PATH", System.getenv("PATH"));
            
            // Put the MAD_PATH variable in the path if it's running 
            int madPort = MadServerStart.getPort();
            if (madPort != 0) {
                env.put("MAD_PORT", Integer.toString(madPort));
            } 

            // If the path variable is reset in the system-configured
            // environment, it'll be in upper case.
            env.putAll(_environment);

            
            PoolProcessWatcher watcher;

            final Process p;
            try {
                _logger.debug(MocaUtils.concat("Spawning process: ", 
                    builder.command()));
                p = builder.start();
                
                watcher =  new PoolProcessWatcher(processID, p);
                
                // Create a process monitor thread for this child process
                Thread processWatcher = new Thread(watcher,
                           "ProcessWatcher-" + processID);
                
                // We only want the watcher as a daemon so it won't keep our
                // JVM up if it is the only one.
                processWatcher.setDaemon(true);
                
                processWatcher.start();
            }
            catch (IOException e) {
                _logger.error("Unable to spawn child process", e);
                return null;
            }

            Socket crashSocket;
            RemoteNativeProcess proc;
            boolean fullyRegistered = false;
            try {
                Lock tempLock = new ReentrantLock();
                Lock lock = _namedProcessLocks.putIfAbsent(processID, 
                    tempLock);
                if (lock == null) {
                    lock = tempLock;
                }
                lock.lock();
                try {
                    while ((proc = _namedProcess.remove(processID)) == null) {
                        Condition tempCondition = lock.newCondition();
                        Condition condition = _namedProcessConditions
                            .putIfAbsent(processID, tempCondition);
                        if (condition == null) {
                            condition = tempCondition;
                        }
                        // If the other thread has already registered we should be
                        // done by now as well
                        if (!condition.await(_processTimeout, TimeUnit.SECONDS)) {
                            _logger.warn("A timeout occurred attempting to spawn a native proces");
                            return null;
                        }
                    }
                }
                finally {
                    lock.unlock();
                }
                
                tempLock = new ReentrantLock();
                lock = _namedCrashSocketLocks.putIfAbsent(processID, 
                    tempLock);
                if (lock == null) {
                    lock = tempLock;
                }
                lock.lock();
                try {
                    while ((crashSocket = _namedCrashSocket.remove(processID)) == null) {
                        Condition tempCondition = lock.newCondition();
                        Condition condition = _namedCrashSocketConditions
                            .putIfAbsent(processID, tempCondition);
                        if (condition == null) {
                            condition = tempCondition;
                        }
                        // If the other thread has already registered we should be
                        // done by now as well
                        if (!condition.await(_processTimeout, TimeUnit.SECONDS)) {
                            _logger.warn("Timeout waiting for crash socket connection to be created");
                            return null;
                        }
                    }
                    // At this point we're good to go, everything is registered so we can keep our process
                    fullyRegistered = true;
                }
                finally {
                    lock.unlock();
                }
            }
            catch (InterruptedException e) {
                _logger.warn("Interrupted while waiting for crash socket connection to be created", e);
                // This exception should bubble up as the thread was explicitly interrupted
                throw new MocaInterruptedException(e);
            }
            finally {
                if (!fullyRegistered) {
                    // We have to kill the process that we spawned if an issue occurred
                    // where we didn't actually end up fully registering the process
                    Process process = null;
                    synchronized (_processes) {
                        process = _processes.remove(processID);
                    }
                    if (process != null) {
                        _logger.info("Shutting down native process " + processID + 
                                " due to an issue during startup");
                        process.destroy();
                    }
                }
                _namedProcess.remove(processID);
                _namedProcessLocks.remove(processID);
                _namedProcessConditions.remove(processID);
                
                _namedCrashSocket.remove(processID);
                _namedCrashSocketLocks.remove(processID);
                _namedCrashSocketConditions.remove(processID);
            }

            // We wrap the process so a few methods don't actually
            NativeProcess wrappedProc = new RemoteNativeProcessWrapper(proc, p, 
                processID, crashSocket);
            
            // We associate that process with it's watcher for tracing purposes
            _watchers.put(wrappedProc, watcher);
 
            return wrappedProc;
        }
    }
    
    
    // @see com.redprairie.moca.server.legacy.AbstractNativeAdapterFactory#getServerAdapter(com.redprairie.moca.server.exec.ServerContext)
    @Override
    protected MocaServerAdapter getServerAdapter(ServerContext ctx)
            throws MocaException {
        return new ContextMocaServerAdapter(ctx);
    }
    
    protected Builder<? extends NativeProcess> builder() {
        return new OurBuilder();
    }
    
    // @see com.redprairie.moca.server.legacy.AbstractNativeAdapterFactory#nativeProcessAssociated(com.redprairie.moca.server.legacy.NativeProcess, com.redprairie.moca.server.exec.ServerContext)
    @Override
    protected void nativeProcessAssociated(NativeProcess process,
                                           ServerContext context) {
        super.nativeProcessAssociated(process, context);
        
        PoolProcessWatcher watcher = _watchers.get(process);
        
        if (watcher != null) {
            watcher.setServerContext(context);
        }
    }
    
    // @see com.redprairie.moca.server.legacy.NativeAdapterFactory#close()
    @Override
    public void close() {
        // Destroying all the proceses should cause all the watchers to stop
        // as well
        _logger.debug("Destroying all native processes");
        synchronized (_processes) {
            for (Process process : _processes.values()) {
                process.destroy();
            }
            
            _processes.clear();
        }
        
        if (_socketWatcher != null) {
            _logger.debug("Stopping socket watcher by interrupting");
            _socketWatcher.interrupt();
        }
        
        if (_socketCrashWatcher != null) {
            _logger.debug("Stopping socket crash watcher by interrupting");
            _socketCrashWatcher.interrupt();
        }
    }
    
    protected void finalize() throws Throwable {
        close();
    }

    /**
     * A private thread that watches spawned processes to (a) capture their
     * output and (b) determine when they die.
     */
    private class PoolProcessWatcher extends ProcessWatcher {
        
        /**
         * Create a process watcher for this pool's processes. 
         * 
         */
        public PoolProcessWatcher(String name, Process p) {
            super(p);
            _name = name;
            _isTraceConfigured = false;
            synchronized(_processes) {
                _processes.put(_name, p);
            }
        }
        
        synchronized
        void setServerContext(ServerContext context) {
            _context = context;
        }
        
        @Override
        protected void processExit(int exitValue) {
            
            logSomething("Process " + _name + " exited with status: " + exitValue, 
                Level.INFO);
            if (exitValue != 0) {
                logSomething("Process " + _name + 
                    " exited with abnormal exit status: " + exitValue, 
                    Level.WARN);
            }

            synchronized(_processes) {
                _processes.remove(_name);
            }
            
            _watchers.values().remove(this);
            
            // We also want to make sure our thread is no longer associated
            ServerUtils.removeCurrentContext();
        }
        
        // @see com.redprairie.util.ProcessWatcher#handleOutput(java.lang.String)
        @Override
        protected void handleOutput(String line) {
            ConcatString string = MocaUtils.concat("Process ", _name, ": ", line);
            logSomething(string, Level.DEBUG);
        }
        
        private synchronized void logSomething(Object message, Level level) {
            if (_context != _currentlyAssociated) {
                _currentlyAssociated = _context;
                ServerUtils.setCurrentContext(_context);
            }

            if (!_isTraceConfigured) {
                TraceState state;
                try {
                    state = TraceUtils.getTraceState();
                    state.applyTraceStateToThread();
                }
                catch (LoggingException e) {
                    //Still try to log.  We can still
                    // get the exception to come through.
                }
                _isTraceConfigured = true;
            }

            switch (level) {
            case TRACE:
                _logger.trace(message);
                break;
            case DEBUG:
                _logger.debug(message);
                break;
            case INFO:
                _logger.info(message);
                break;
            case WARN:
                _logger.warn(message);
                break;
            case FATAL:
                _logger.fatal(message);
                break;
            default:
                throw new IllegalArgumentException("Invalid level specified: " + level);
            }
        }
        
        private String _name;
        private ServerContext _context;
        private ServerContext _currentlyAssociated;
        private boolean _isTraceConfigured;
    }
    
    private class CrashSocketWatcher implements Runnable {
        // @see java.lang.Runnable#run()
        @Override
        public void run() {
            while (true) {
                // Wait until someone connects
                try {
                    Socket connection = _sockCrash.accept();
                    connection.setTcpNoDelay(true);
                    
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), 
                            "UTF-8"));
                    String processId = reader.readLine();
                    _logger.debug("Identified crash socket for " + processId);
                    
                    _namedCrashSocket.put(processId, connection);
                    
                    // Now we have to make sure to wake up anyone who may be waiting
                    // on the connection
                    Lock tempLock = new ReentrantLock();
                    Lock lock = _namedCrashSocketLocks.putIfAbsent(processId, 
                        tempLock);
                    if (lock == null) {
                        lock = tempLock;
                    }
                    lock.lock();
                    try {
                        Condition tempCondition = lock.newCondition();
                        Condition condition = _namedCrashSocketConditions
                            .putIfAbsent(processId, tempCondition);
                        if (condition == null) {
                            condition = tempCondition;
                        }
                        condition.signalAll();
                    }
                    finally {
                        lock.unlock();
                    }
                }
                catch (InterruptedIOException e) {
                    _logger.debug("Shutting down socket watcher due to io interrupt");
                    break;
                }
                catch (ClosedByInterruptException e) {
                    _logger.debug("Shutting down socket watcher due to socket close interrupt");
                    break;
                }
                catch (IOException e) {
                    _logger.debug("Exception while listening for crash socket connections", e);
                    _logger.error("Unable to receive new Messages: " + e);
                }
            }
            
        }
    }
    
    private class ServerSocketWatcher implements Runnable {
        // @see java.lang.Runnable#run()
        
        @Override
        public void run() {
            while (true) {
                try {
                    // Now we wait until someone connects.
                    Socket connection = _sock.accept();
                    connection.setTcpNoDelay(true);
                    
                    ObjectOutputStream out = new ObjectOutputStream(
                        new BufferedOutputStream(connection.getOutputStream()));
                    out.flush();
                    ObjectInputStream in = new ObjectInputStream(
                        new BufferedInputStream(connection.getInputStream()));
                    
                    String processId = in.readUTF();
                    _logger.debug("Identified socket for " + processId);
                    
                    // Create a new proxy with no callback target (yet).
                    RemoteNativeProcess proc = CallbackProxy.newProxy(out, in, 
                        RemoteNativeProcess.class, MocaServerAdapter.class, 
                            (MocaServerAdapter)null);
                    
                    _namedProcess.put(processId, proc);
                    
                    // Now we have to make sure to wake up anyone who may be waiting
                    // on the connection
                    Lock tempLock = new ReentrantLock();
                    Lock lock = _namedProcessLocks.putIfAbsent(processId, 
                        tempLock);
                    if (lock == null) {
                        lock = tempLock;
                    }
                    lock.lock();
                    try {
                        Condition tempCondition = lock.newCondition();
                        Condition condition = _namedProcessConditions
                            .putIfAbsent(processId, tempCondition);
                        if (condition == null) {
                            condition = tempCondition;
                        }
                        condition.signalAll();
                    }
                    finally {
                        lock.unlock();
                    }
                    
                }
                catch (InterruptedIOException e) {
                    _logger.debug("Shutting down socket watcher due to io interrupt");
                    break;
                }
                catch (ClosedByInterruptException e) {
                    _logger.debug("Shutting down socket watcher due to socket close interrupt");
                    break;
                }
                catch (IOException e) {
                    _logger.error("Unable to receive new Messages: " + e);
                }
            }
            
        }
    }
    
    private final Thread _socketWatcher;
    private final Thread _socketCrashWatcher;
    
    private final String _javaCmd;
    private final List<String> _javaVMArgs;
    private final int _port;
    private final int _portCrash;
    private final int _processTimeout;
    private final Map<String, Process> _processes = new HashMap<String, Process>();
    private final Map<String, String> _environment;
    private final ServerSocket _sock;
    private final ServerSocket _sockCrash;

    private final ConcurrentMap<RemoteNativeProcess, PoolProcessWatcher> _watchers = 
        new ConcurrentHashMap<RemoteNativeProcess, SocketNativeAdapterFactory.PoolProcessWatcher>();
    
    private final ConcurrentMap<String, RemoteNativeProcess> _namedProcess = 
            new ConcurrentHashMap<String, RemoteNativeProcess>();
    private final ConcurrentMap<String, Lock> _namedProcessLocks =
            new ConcurrentHashMap<String, Lock>();
    private final ConcurrentMap<String, Condition> _namedProcessConditions =
            new ConcurrentHashMap<String, Condition>();
    
    private final ConcurrentMap<String, Socket> _namedCrashSocket = 
            new ConcurrentHashMap<String, Socket>();
    private final ConcurrentMap<String, Lock> _namedCrashSocketLocks =
            new ConcurrentHashMap<String, Lock>();
    private final ConcurrentMap<String, Condition> _namedCrashSocketConditions =
            new ConcurrentHashMap<String, Condition>();
}