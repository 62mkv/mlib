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

package com.redprairie.moca.advice;

import java.lang.management.ThreadInfo;
import java.util.Map;

import com.redprairie.moca.client.XMLResultsDecoder;
import com.redprairie.moca.client.XMLResultsEncoder;
import com.redprairie.moca.server.exec.ServerContextStatus;


/**
 * This class defines the methods available bean that allows visibility to the 
 * current server context for the given session.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public interface ServerContextAdministrationBean extends
        SessionAdministrationBean {
    
    /**
     * Retrieve the current status of the session
     * @return the status of the session
     */
    public ServerContextStatus getStatus();
    
    /**
     * Retrieve the currently executing command path
     * @return the command path
     */
    public String getCurrentCommandPath();
    
    /**
     * Interrupts the currently associated thread with this session.  It is
     * possible that a thread will not respond to an interrupt.
     */
    public void interrupt();
    
    /**
     * Retrieve the thread information pertaining to all the threads associated
     * with this session.  The thread information will be returned in the 
     * order in which they were associated with the session.  So if
     * more than 1 thread is associated the first thread associated will be the
     * first thread information returned in the array. 
     * @return the thread information for each thread in reverse order of 
     *         affinity
     */
    public ThreadInfo[] getSessionThreads();
    
    /**
     * Retrieves the current data stack for the context associated with the
     * session.  This will be a encoded moca results in xml form.  It will state
     * each level of the data stack with the values published at those levels
     * as well as the stack level itself and the command that was executed
     * for that stack level.
     * 
     * If there is no available stack "Context Inactive" will be returned.
     * 
     * @see XMLResultsDecoder#decode()
     * @see XMLResultsEncoder#writeResults(com.redprairie.moca.MocaResults, Appendable)
     * @return the encoded data stack in xml
     */
    public String queryDataStack();
    
    /**
     * Returns a copy of a map that represents the request environment that
     * was used for this server context.  If the request is over an empty Map is
     * returned instead.
     * @return the unmodifiable map of the request environment
     */
    public Map<String, String> getRequestEnvironment();
}
