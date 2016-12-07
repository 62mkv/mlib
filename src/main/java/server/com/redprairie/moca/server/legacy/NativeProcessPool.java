/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.server.legacy;

import java.util.Collection;

/**
 * Class that represents the pool that contains native processes.  A native
 * process is pooled since they are expensive objects and should be reused
 * whenever possible.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface NativeProcessPool {

    /**
     * @return
     * @throws NativeProcessTimeoutException
     */
    public NativeProcess getNativeProcess() throws NativeProcessTimeoutException;

    /**
     * 
     */
    public void restartPool();

    /**
     * 
     */
    public void forciblyRestartPool();
    
    public Collection<NativeProcess> getAllProcesses();
    
    public Collection<NativeProcess> getActiveProcesses();
    
    public Collection<NativeProcess> getTemporaryProcesses();
    
    public int getSize();
    
    public int getPeakSize();
    
    public int getMaximumSize();

    public void shutdownProcess(NativeProcess process);
    
    public Integer timesTaken(NativeProcess process);

    public void shutdown();
}