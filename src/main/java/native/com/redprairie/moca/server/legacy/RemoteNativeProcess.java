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

package com.redprairie.moca.server.legacy;

import java.rmi.RemoteException;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaLibInfo;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface RemoteNativeProcess {

    /**
     * Loads the given library.
     * 
     * @param libraryName The name of the library. This does not correspond to
     *            anything related to the library file itself, but is just the
     *            name assigned to the library for future execute requests.
     * 
     * @param pathToLibrary The pathname of the library file itself. This is the
     *            library that is loaded via low-level OS functions to load
     *            shared libraries. If the path is not given as a
     *            fully-qualified name, then it is a responsibility of the
     *            caller to ensure that the appropriate
     *            PATH/LD_LIBRARY_PATH/SHLIB_PATH variables are set.
     * 
     * @throws RemoteException If a communication error occurs while
     *             communicating with the native process.
     * @throws MocaException If an error occurs while loading the library.
     */
    public void loadLibrary(String libraryName, String pathToLibrary)
            throws RemoteException, MocaException;

    /**
     * Initializes the given library. It is assumed that the named library has
     * already been loaded via the loadLibrary method. This method differs from
     * the loadLibrary method, mainly in that the loadLibrary method is
     * guaranteed to be called for every process, while initializeLibrary is
     * guaranteed to be called only once at system startup.
     * 
     * @param libraryName The name of the library.
     * @return A <code>MocaLibInfo</code> object that represents the library's
     *         version and license information.
     * @throws RemoteException If a communication error occurs while
     *             communicating with the native process.
     * @throws MocaException If an error occurs while initializing the library.
     */
    public MocaLibInfo initializeLibrary(String libraryName)
            throws RemoteException, MocaException;

    /**
     * Calls the given C function.  The function must be an exported member of the loaded
     * library associated with the given library name.  The <code>adapter</code> argument
     * is passed to the running native code to be used for further interactions with the
     * server.
     * @param adapter
     * @param libraryName
     * @param functionName
     * @param types  The types of arguments being passed to the function.
     * @param args The arguments to be passed to the function.  This array must be the same
     * size as the <code>types</code> array.
     * @param simpleFunction Whether the function execution is simple
     * @return A {@link MocaResults} object.
     * @throws RemoteException
     * @throws MocaException
     */
    public NativeReturnStruct callFunction(MocaServerAdapter adapter,
        String libraryName, String functionName, MocaType[] types,
        Object[] args, boolean simpleFunction, boolean traceEnabled)
            throws RemoteException, MocaException;

    public MocaLibInfo initializeCOMLibrary(String progID)
            throws RemoteException, MocaException;

    public NativeReturnStruct callCOMMethod(MocaServerAdapter adapter,
        String progID, String methodName, MocaType[] types, Object[] args,
        boolean traceEnabled) throws RemoteException;

    public void preCommit(MocaServerAdapter adapter) throws RemoteException,
            MocaException;

    public void postTransaction(MocaServerAdapter adapter, boolean isCommit)
            throws RemoteException;

    public boolean isAlive() throws RemoteException;

    public void close() throws RemoteException;

    public abstract void release(MocaServerAdapter adapter) throws RemoteException;

    public boolean isKeepaliveSet() throws RemoteException;

    public void setEnvironment(String name, String value)
            throws RemoteException;
}