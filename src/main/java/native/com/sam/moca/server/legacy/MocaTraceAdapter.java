/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.sam.moca.server.legacy;

import java.rmi.RemoteException;

/**
 * This interface defines all the functions regarding tracing that a native
 * adapter will need to be aware of.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public interface MocaTraceAdapter {
    
    /**
     * This function will call back to the server to setup a file to trace with
     * @param fileName The file name to trace with
     * @param append Whether this file should be created anew or appended to
     * @throws RemoteException
     */
    public void setTraceFileName(String fileName, boolean append) throws RemoteException;
    

    /**
     * Logs a message at the given level.
     * @param level Which log level to use.  1 = DEBUG, 2 = INFO, 3 = WARN, 4 = ERROR.
     * @param message The message to log
     */
    public void log(int level, String message) throws RemoteException;
    
    /**
     * Puts a trace message to the trace file and/or log file.
     * @param level Which trace facility to use.  
     * @param message
     */
    public void trace(int level, String message) throws RemoteException;
    
    /**
     * @param level
     * @throws RemoteException
     */
    public void setTraceLevel(int level) throws RemoteException;
    
    /**
     * @return
     * @throws RemoteException
     */
    public int getTraceLevel() throws RemoteException;
}