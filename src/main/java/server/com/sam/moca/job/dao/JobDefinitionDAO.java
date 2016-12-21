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

package com.sam.moca.job.dao;

import java.util.List;

import com.sam.moca.cluster.RoleDefinition;
import com.sam.moca.dao.GenericDAO;
import com.sam.moca.job.JobDefinition;


/**
 * Data Access Object that has available methods to work with Task Defintion
 * POJOs.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public interface JobDefinitionDAO extends GenericDAO<JobDefinition, String> {
    /**
     * This method returns all jobs that have the given roles and any job that
     * will run on any node.  Jobs unlike tasks do match role id to the job id
     * when a job doesn't have a role id.
     * @param roles The roles to find jobs for
     * @return All the jobs that match the given roles
     */
    public List<JobDefinition> readForAllAndRoles(RoleDefinition... roles);
    
    /**
     * This method returns all jobs that have the given roles, any job that
     * has no role and any job that has the all role.
     * @param roles The roles to find jobs for
     * @return All the jobs that match the given roles and empty roles
     */
    public List<JobDefinition> readForAllNoRoleAndRoles(RoleDefinition... roles);
}
