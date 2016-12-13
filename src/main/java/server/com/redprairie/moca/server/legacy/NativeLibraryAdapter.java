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

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaLibInfo;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;

/**
 * Adapter interface for interoperating with legacy MOCA C libraries.  This interface is used as an adapter for a
 * MOCA server system to talk to the native library.  Depending on system configuration and process structure, the
 * adapter implementation may be connected via RMI, but that fact is not exposed here.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public interface NativeLibraryAdapter {
    
    /**
     * Initializes a native library.  The library name is the name (interpretation is os-specific) of the library
     * to load.  When this method is called, the library is loaded and available for use.  The library name can
     * then be used later to call C and Simple C components.
     * @param libraryName
     * @return
     * @throws MocaException
     */
    public MocaLibInfo initializeLibrary(String libraryName) throws MocaException;
    
    public MocaResults callFunction(String libraryName, String functionName, 
            MocaType[] argTypes, Object[] args, boolean simpleFunction)
            throws MocaException;
    
    public MocaLibInfo initializeCOM(String progID) throws MocaException;
    
    public MocaResults callCOMMethod(String progID, String methodName, MocaType[] argTypes, Object[] args)
            throws MocaException;
    
    public void release() throws MocaException;
    
    public void preCommit() throws MocaException;
    
    public void postTransaction(boolean committed);
    
    public boolean isKeepaliveSet();
    
    public void setEnvironmentVariable(String name, String value);
    
    /**
     * This tells whether or not the adapter is currently executing any native 
     * code.
     * @return if the native adapter is processing anything.
     */
    public boolean currentlyActive();
}
