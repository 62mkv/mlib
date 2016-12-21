/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.sam.moca.task.dao.hibernate;

import java.util.Date;

import com.sam.moca.dao.hibernate.AbstractUnknownKeyHibernateDAO;
import com.sam.moca.db.hibernate.HibernateTools;
import com.sam.moca.server.InstanceUrl;
import com.sam.moca.task.TaskDefinition;
import com.sam.moca.task.TaskExecution;
import com.sam.moca.task.dao.TaskExecutionDAO;

/**
 * Data access implementation using hibernate for Task Execution instances
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TaskExecutionHibernateDAO extends AbstractUnknownKeyHibernateDAO<TaskExecution> 
    implements TaskExecutionDAO {
    /**
     * @param instanceType
     */
    public TaskExecutionHibernateDAO() {
        super(TaskExecution.class);
    }

    // @see com.sam.moca.task.dao.TaskExecutionDAO#read(java.lang.String, com.sam.moca.server.InstanceUrl, java.util.Date)
    @Override
    public TaskExecution read(String taskId, InstanceUrl nodeUrl, Date startDate) {
        TaskDefinition taskDef = new TaskDefinition();
        taskDef.setTaskId(taskId);
        TaskExecution taskPk = new TaskExecution(taskDef, startDate, nodeUrl);
        TaskExecution taskExec = (TaskExecution) HibernateTools.getSession().get(_instanceType, 
            taskPk);
        return taskExec;
    }
}
