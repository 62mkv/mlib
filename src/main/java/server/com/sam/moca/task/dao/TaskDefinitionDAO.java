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

package com.sam.moca.task.dao;

import java.util.List;

import com.sam.moca.cluster.RoleDefinition;
import com.sam.moca.dao.GenericDAO;
import com.sam.moca.task.TaskDefinition;


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
public interface TaskDefinitionDAO extends GenericDAO<TaskDefinition, String> {
    public List<TaskDefinition> readAllTasksForAllAndRoles(RoleDefinition... roles);
}
