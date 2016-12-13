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

package com.redprairie.moca.probes.tasks;

import java.util.Map;

import javax.management.MXBean;

import com.redprairie.moca.cluster.RoleDefinition;

/**
 * MXBean interface for Task Definitions
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
@MXBean
public interface TaskDefinitionMXBean {

    /**
     * @return Returns the Task ID.
     */
    public String getTaskId();
    
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
    public String getCmdLine();

    /**
     * @return Returns the runDirectory.
     */
    public String getRunDirectory();

    /**
     * @return Returns the logFile.
     */
    public String getLogFile();
    
    /**
     * @return Returns the trace level.
     */
    public String getTraceLevel();
    
    /**
     * @return Returns the restart.
     */
    public boolean isRestart();
    
    /**
     * @return Returns the autoStart.
     */
    public boolean isAutoStart();
    

    /**
     * @return Returns the startDelay.
     */
    public Integer getStartDelay();

    /**
     * @return Returns the type.
     */
    public String getType();   
    
    /**
     * @return Returns the environment.
     */
    public Map<String, String> getEnvironment();
}
