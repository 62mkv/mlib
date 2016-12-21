/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.sam.moca.probes.jobs;

import java.util.Map;

import javax.management.MXBean;

import com.sam.moca.cluster.RoleDefinition;

/**
 * The interface for a Job Definition
 * to expose it via MXBean
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
@MXBean
public interface JobDefinitionMXBean {

    /**
     * @return Returns the jobId.
     */
    public String getJobId();
    
    /**
     * @return Returns the role.
     */
    public RoleDefinition getRole();
    
    /**
     * @return Returns the name.
     */
    public String getName();

    /**
     * @return Returns the cmdline.
     */
    public String getCommand();

    /**
     * @return Returns the logFile.
     */
    public String getLogFile();
    
    /**
     * @return Returns the traceLevel.
     */
    public String getTraceLevel();

    /**
     * @return Returns the schedule.
     */
    public String getSchedule();

    /**
     * @return Returns the timer.
     */
    public Integer getTimer();

    /**
     * @return Returns the startDelay.
     */
    public Integer getStartDelay();

    /**
     * @return Returns the overlap.
     */
    public boolean isOverlap();
    
    /**
     * @return Returns the enabled.
     */
    public boolean isEnabled();
    
    /**
     * @return Returns the type.
     */
    public String getType();

    /**
     * @return Returns the _environment.
     */
    public Map<String, String> getEnvironment();
}
