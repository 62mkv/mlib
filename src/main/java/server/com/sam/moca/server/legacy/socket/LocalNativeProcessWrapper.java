/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.sam.moca.server.legacy.socket;

import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;

import org.apache.log4j.Logger;

import com.sam.moca.MocaException;
import com.sam.moca.MocaLibInfo;
import com.sam.moca.MocaType;
import com.sam.moca.server.legacy.MocaContextServerAdapter;
import com.sam.moca.server.legacy.MocaServerAdapter;
import com.sam.moca.server.legacy.NativeProcess;
import com.sam.moca.server.legacy.NativeReturnStruct;
import com.sam.moca.server.legacy.RemoteNativeProcess;
import com.sam.moca.server.profile.CommandPath;

/**
 * Native Process Wrapper used for actually local native processes
 * 
 * Copyright (c) 2013 Sam Corporation All Rights Reserved
 * 
 * @author wburns
 */
public class LocalNativeProcessWrapper implements NativeProcess {
    public LocalNativeProcessWrapper(RemoteNativeProcess process, String procId) {
        _remoteProcess = process;
        _dateCreated = new Date();
        _processId = procId;
    }

    // @see
    // com.sam.moca.server.legacy.NativeProcess#loadLibrary(java.lang.String,
    // java.lang.String)
    @Override
    public void loadLibrary(String libraryName, String pathToLibrary)
            throws RemoteException, MocaException {
        _remoteProcess.loadLibrary(libraryName, pathToLibrary);
    }

    // @see
    // com.sam.moca.server.legacy.NativeProcess#initializeLibrary(java.lang.String)
    @Override
    public MocaLibInfo initializeLibrary(String libraryName)
            throws RemoteException, MocaException {
        return _remoteProcess.initializeLibrary(libraryName);
    }

    // @see
    // com.sam.moca.server.legacy.NativeProcess#callFunction(com.sam.moca.server.legacy.MocaServerAdapter,
    // java.lang.String, java.lang.String, com.sam.moca.MocaType[],
    // java.lang.Object[], boolean, boolean)
    @Override
    public NativeReturnStruct callFunction(MocaServerAdapter adapter,
        String libraryName, String functionName, MocaType[] types,
        Object[] args, boolean simpleFunction, boolean traceEnabled)
            throws RemoteException, MocaException {
        synchronized (this) {
            if (_associatedThread == null) {
                _associatedThread = Thread.currentThread();
            }

            if (!_inCall) {
                _lastCalls.clear();
                _lastCallArgs.clear();
                _lastCallDates.clear();
                _lastCallPaths.clear();
                _inCall = true;
            }
            _lastCalls.push(libraryName + "." + functionName);
            _lastCallArgs.push(args);
            _lastCallDates.push(new Date());
            if (adapter instanceof MocaContextServerAdapter) {
                _lastCallPaths.push(((MocaContextServerAdapter)adapter).getCurrentCommandPath());
            }
        }
        try {
            return _remoteProcess.callFunction(adapter, libraryName,
                functionName, types, args, simpleFunction, traceEnabled);
        }
        finally {
            synchronized (this) {
                if (_lastCalls.size() == 1) {
                    _inCall = false;
                }
                else {
                    _lastCalls.pop();
                    _lastCallDates.pop();
                    _lastCallArgs.pop();
                    _lastCallPaths.pop();
                }
            }
        }
    }

    // @see
    // com.sam.moca.server.legacy.NativeProcess#initializeCOMLibrary(java.lang.String)
    @Override
    public MocaLibInfo initializeCOMLibrary(String progID)
            throws RemoteException, MocaException {
        return _remoteProcess.initializeCOMLibrary(progID);
    }

    // @see
    // com.sam.moca.server.legacy.NativeProcess#callCOMMethod(com.sam.moca.server.legacy.MocaServerAdapter,
    // java.lang.String, java.lang.String, com.sam.moca.MocaType[],
    // java.lang.Object[], boolean)
    @Override
    public NativeReturnStruct callCOMMethod(MocaServerAdapter adapter,
        String progID, String methodName, MocaType[] types, Object[] args,
        boolean traceEnabled) throws RemoteException {
        synchronized (this) {
            if (_associatedThread == null) {
                _associatedThread = Thread.currentThread();
            }

            if (!_inCall) {
                _lastCalls.clear();
                _lastCallArgs.clear();
                _lastCallDates.clear();
                _lastCallPaths.clear();
                _inCall = true;
            }
            _lastCalls.push(progID + "." + methodName);
            _lastCallArgs.push(args);
            _lastCallDates.push(new Date());
            if (adapter instanceof MocaContextServerAdapter) {
                _lastCallPaths.push(((MocaContextServerAdapter)adapter).getCurrentCommandPath());
            }
        }
        try {
            return _remoteProcess.callCOMMethod(adapter, progID, methodName,
                types, args, traceEnabled);
        }
        finally {
            synchronized (this) {
                if (_lastCalls.size() == 1) {
                    _inCall = false;
                }
                else {
                    _lastCalls.pop();
                    _lastCallDates.pop();
                    _lastCallArgs.pop();
                    _lastCallPaths.pop();
                }
            }
        }
    }

    // @see
    // com.sam.moca.server.legacy.NativeProcess#preCommit(com.sam.moca.server.legacy.MocaServerAdapter)
    @Override
    public void preCommit(MocaServerAdapter adapter) throws RemoteException,
            MocaException {
        _remoteProcess.preCommit(adapter);
    }

    // @see
    // com.sam.moca.server.legacy.NativeProcess#postTransaction(com.sam.moca.server.legacy.MocaServerAdapter,
    // boolean)
    @Override
    public void postTransaction(MocaServerAdapter adapter, boolean isCommit)
            throws RemoteException {
        _remoteProcess.postTransaction(adapter, isCommit);
    }

    // @see com.sam.moca.server.legacy.NativeProcess#isAlive()
    @Override
    public boolean isAlive() throws RemoteException {
        return _remoteProcess.isAlive();
    }

    // @see com.sam.moca.server.legacy.NativeProcess#close()
    @Override
    public void close() throws RemoteException {
        // Just let GC
    }

    // @see com.sam.moca.server.legacy.NativeProcess#isKeepaliveSet()
    @Override
    public boolean isKeepaliveSet() throws RemoteException {
        return _remoteProcess.isKeepaliveSet();
    }

    // @see
    // com.sam.moca.server.legacy.NativeProcess#release(com.sam.moca.server.legacy.MocaServerAdapter)
    @Override
    public void release(MocaServerAdapter adapter) throws RemoteException {
        _remoteProcess.release(adapter);

        synchronized (this) {
            _associatedThread = null;
        }
    }

    // @see
    // com.sam.moca.server.legacy.NativeProcess#setEnvironment(java.lang.String,
    // java.lang.String)
    @Override
    public void setEnvironment(String name, String value)
            throws RemoteException {
        _remoteProcess.setEnvironment(name, value);
    }

    /**
     * The following methods are not going to make remote calls.
     */
    // @see com.sam.moca.server.legacy.NativeProcess#getId()
    @Override
    public String getId() {
        return _processId;
    }

    // @see com.sam.moca.server.legacy.NativeProcess#dateCreated()
    @Override
    public synchronized Date dateCreated() {
        return new Date(_dateCreated.getTime());
    }

    // @see com.sam.moca.server.legacy.NativeProcess#lastCall()
    @Override
    public synchronized String lastCall() {
        String lastCommand = _lastCalls.peek();

        if (lastCommand != null) {
            StringBuilder builder = new StringBuilder(lastCommand);
            builder.append('(');
            Object[] args = _lastCallArgs.peek();

            int iMax = args.length - 1;

            // We had no arguments
            if (iMax == -1) {
                return builder.append(')').toString();
            }

            for (int i = 0;; i++) {
                String argString = String.valueOf(args[i]);
                int length = argString.length();
                // limit the size of each argument to 100 characters, if it gets
                // that high then trim to 97 and put ...
                if (length > 100) {
                    builder.append(argString, 0, 96);
                    builder.append('.');
                    builder.append('.');
                    builder.append('.');
                }
                else {
                    builder.append(argString);
                }
                if (i == iMax) return builder.append(')').toString();
                builder.append(", ");
            }
        }

        return lastCommand;
    }

    // @see com.sam.moca.server.legacy.NativeProcess#lastCallDate()
    @Override
    public synchronized Date lastCallDate() {
        Date lastCallDate = _lastCallDates.peek();
        if (lastCallDate != null) {
            return new Date(lastCallDate.getTime());
        }
        else {
            return null;
        }
    }

    // @see com.sam.moca.server.legacy.NativeProcess#getLastCommandPath()
    @Override
    public CommandPath getLastCommandPath() {
        return _lastCallPaths.peek();
    }

    // @see
    // com.sam.moca.server.legacy.NativeProcess#getAssociatedThread()
    @Override
    public synchronized Thread getAssociatedThread() {
        return _associatedThread;
    }

    protected final String _processId;
    protected final Date _dateCreated;
    protected final RemoteNativeProcess _remoteProcess;

    /**
     * This value tells whether or not we are actually still in a native call.
     * If not then we have to keep the last call and date until a new one comes
     * in, when we have to then clear those to be replaced
     */
    protected boolean _inCall = false;
    protected Deque<String> _lastCalls = new ArrayDeque<String>();
    protected Deque<Object[]> _lastCallArgs = new ArrayDeque<Object[]>();
    protected Deque<Date> _lastCallDates = new ArrayDeque<Date>();
    protected Deque<CommandPath> _lastCallPaths = new ArrayDeque<CommandPath>();
    protected Thread _associatedThread;

    protected final Logger _logger = Logger.getLogger(getClass());
}
