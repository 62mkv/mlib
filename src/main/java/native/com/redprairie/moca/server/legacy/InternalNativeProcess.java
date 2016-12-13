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

package com.redprairie.moca.server.legacy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaLibInfo;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.util.ClassUtils;

/**
 * Native library adapter that operates fully within a single process.  This is the basic low-level
 * JNI interface to the native (C) functions that must be called as part of MOCA.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class InternalNativeProcess implements RemoteNativeProcess {
    
    /**
     * 
     */
    public InternalNativeProcess(String id) {
        String logDir = System.getProperty("com.redprairie.moca.NativeLog");
        if (logDir != null) {
            try {
                
                Writer logfile = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(logDir, "nativelog-" + id
                            + ".log"), true), "UTF-8"));
                
                logfile.append("\n\nNew Native Process Started\n");
                logfile.append(String.valueOf(new Date()));
                logfile.append("\n\n");
                _stats = new NativeCallStatistics(logfile);
            }
            catch (InterruptedIOException e) {
                throw new MocaInterruptedException(e);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            _stats = null;
        }
        
        _createdDate = new Date();
    }
    
    // @see com.redprairie.moca.server.legacy.NativeProcess#loadLibrary(java.lang.String, java.lang.String)
    @Override
    synchronized
    public void loadLibrary(String libraryName, String pathToLibrary) throws MocaException {
        _LibraryDescriptor desc = _loadedLibraries.get(libraryName);
        if (desc == null) {
            desc = new _LibraryDescriptor();
            desc.calledAppInitialize = false;
            
            // Put the descriptor in the collection, so we can look it up later. 
            _loadedLibraries.put(libraryName, desc);
            
            // Deal with library load failure.
            desc.libraryHandle = _loadLibrary(pathToLibrary);
            
            // If the library was not found, no harm, but watch out for problems later.
            desc.versionInfo = _initializeLibrary(desc.libraryHandle);
            
        }
    }
    
    synchronized
    public MocaLibInfo initializeLibrary(String libraryName) throws MocaException {
        _LibraryDescriptor desc = _loadedLibraries.get(libraryName);
        if (desc == null) {
            throw new MocaNativeException(libraryName, "initialize", "Library " + libraryName + " is not loaded");
        }
        return desc.versionInfo;
    }
    
    public NativeReturnStruct callFunction(MocaServerAdapter adapter, String libraryName,
                                    String functionName, MocaType[] types, Object[] args,
                                    boolean simpleFunction, boolean traceEnabled)
            throws MocaException {
        // Look up the library, to see if it's loaded.
        _LibraryDescriptor desc = _loadedLibraries.get(libraryName);
        
        // No library, that means they tried to call a function on a non-loaded library
        if (desc == null) {
            throw new MocaNativeException(libraryName, functionName, "Library not loaded");
        }
        
        
        // Convert Argument Types.  Incoming argument types are MocaType enum.  The command needs
        // c chars.
        char[] typeCodes = new char[types.length];
        for (int i = 0; i < types.length; i++) {
            typeCodes[i] = types[i].getTypeCode();
        }
        
        // Look up the actual function handle.  The function handle is essentially a pointer,
        // encoded as an integer.
        Integer functionHandle;
        synchronized (desc) {
            // Initialize the application library.  This is distinct from the step of loading the
            // library.
            if (!desc.calledAppInitialize) {
                _initializeAppLibrary(wrapAdapter(adapter), desc.libraryHandle);
                desc.calledAppInitialize = true;
            }
            
            functionHandle = desc.methods.get(functionName);
            if (functionHandle == null) {
                if (desc.libraryHandle == 0) {
                    throw new MocaNativeException(libraryName, functionName, "Library not found");
                }
                
                functionHandle = Integer.valueOf(_findCFunction(desc.libraryHandle, functionName));
                
                if (functionHandle == 0) {
                    throw new MocaNativeException(libraryName, functionName, "Function not found");
                }

                desc.methods.put(functionName, functionHandle);
            }
        }
        
        List<WrappedResults> convertedResults = new ArrayList<WrappedResults>();
        
        for (int i = 0; i < args.length; i++) {
            if (types[i] == MocaType.RESULTS && args[i] != null) {
                MocaResults arg = (MocaResults)args[i];
                WrappedResults wr = new WrappedResults(arg, false);
                args[i] = wr;
                convertedResults.add(wr);
            }
        }
        
        try {
            NativeReturnStruct functionResults = _callCFunction(
                wrapAdapter(adapter), functionHandle, 
                    typeCodes, args, simpleFunction, traceEnabled);
            return functionResults;
        }
        finally {
            for (WrappedResults wr : convertedResults) {
                wr.close();
            }
        }
    }
    
    // @see com.redprairie.moca.server.legacy.NativeProcess#initializeCOMLibrary(java.lang.String)
    @Override
    public MocaLibInfo initializeCOMLibrary(String progID) throws MocaException {
        return _initializeCOMLibrary(progID);
    }
    
    // @see com.redprairie.moca.server.legacy.NativeProcess#callCOMMethod(com.redprairie.moca.server.legacy.MocaServerAdapter, java.lang.String, java.lang.String, com.redprairie.moca.MocaType[], java.lang.Object[])
    @Override
    public NativeReturnStruct callCOMMethod(MocaServerAdapter adapter, 
            String progID, String methodName, MocaType[] types, Object[] args, 
            boolean traceEnabled) {
        
        // Convert Argument Types
        char[] typeCodes = new char[types.length];
        for (int i = 0; i < types.length; i++) {
            typeCodes[i] = types[i].getTypeCode();
        }
        
        NativeReturnStruct comResults = _callCOMMethod(wrapAdapter(adapter), progID,
            methodName, typeCodes, args, traceEnabled);

        return comResults;
    }
    
    @Override
    public void preCommit(MocaServerAdapter adapter) throws MocaException {
        _preCommit(wrapAdapter(adapter));
    }
    
    @Override
    public void postTransaction(MocaServerAdapter adapter, boolean isCommit) {
        _postTransaction(wrapAdapter(adapter), isCommit);
    }
    
    // @see com.redprairie.moca.server.legacy.NativeProcess#isAlive()
    @Override
    public boolean isAlive() {
        return true;
    }
    
    @Override
    public void close() {
        return;
    }
    
    @Override
    public boolean isKeepaliveSet() throws RemoteException {
        return (_getKeepaliveCounter() != 0);
    }
    
    @Override
    public void release(MocaServerAdapter server) throws RemoteException {
        _release(server);
        if (_stats != null) {
            _stats.dumpLog("Release");
        }
    }
    
    // @see com.redprairie.moca.server.legacy.NativeProcess#setEnvironment(java.lang.String, java.lang.String)
    @Override
    public void setEnvironment(String name, String value)
            throws RemoteException {
        _setEnvironment(name, value);
    }
    
    //
    // Implementation
    //
    
    private MocaServerAdapter wrapAdapter(MocaServerAdapter adapter) {
        MocaServerAdapter wrapped = new InProcessMocaServerAdapter(adapter);
        
        if (_stats != null) {
            return (MocaServerAdapter) Proxy.newProxyInstance(
                    ClassUtils.getClassLoader(),
                new Class<?>[] {MocaServerAdapter.class},
                new LoggingMocaServerAdapter(wrapped, _stats));
        }
        else {
            return wrapped;
        }
        
    }
    
    private static class _LibraryDescriptor {
        private int libraryHandle;
        private MocaLibInfo versionInfo;
        private boolean calledAppInitialize;
        private Map<String, Integer> methods = new HashMap<String, Integer>();
    }

    private native int _loadLibrary(String pathToLibrary) throws MocaNativeException;
    private native MocaLibInfo _initializeLibrary(int libraryHandle) throws MocaNativeException;
    private native MocaLibInfo _initializeCOMLibrary(String progID) throws MocaNativeException;
    private native void _initializeAppLibrary(MocaServerAdapter server, int libraryHandle) throws MocaNativeException;
    private native int _findCFunction(int libraryHandle, String functionName) throws MocaNativeException;
    private native NativeReturnStruct _callCFunction(MocaServerAdapter server, 
            int functionPointer, char[] types, Object[] args, 
            boolean simpleFunction, boolean traceEnabled);
    private native NativeReturnStruct _callCOMMethod(MocaServerAdapter server, 
            String progID, String methodName, char[] types, Object[] args, 
            boolean traceEnabled);
    private native void _preCommit(MocaServerAdapter server) throws MocaException;
    private native void _postTransaction(MocaServerAdapter server, boolean isCommit);
    private native int _getKeepaliveCounter();
    private native void _release(MocaServerAdapter server);
    private native void _setEnvironment(String name, String value);

    // JNI Support method.
    private static native void _initIDs();
    
    private Map<String, _LibraryDescriptor> _loadedLibraries = new HashMap<String, _LibraryDescriptor>();
    private final Date _createdDate;
    
    private NativeCallStatistics _stats;
    
    static
    {
        System.loadLibrary("MOCA");
        _initIDs();
    }
}
