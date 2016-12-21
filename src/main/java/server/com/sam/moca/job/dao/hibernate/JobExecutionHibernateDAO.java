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

package com.sam.moca.job.dao.hibernate;

import java.util.Date;

import com.sam.moca.dao.hibernate.AbstractUnknownKeyHibernateDAO;
import com.sam.moca.db.hibernate.HibernateTools;
import com.sam.moca.job.JobDefinition;
import com.sam.moca.job.JobExecution;
import com.sam.moca.job.dao.JobExecutionDAO;
import com.sam.moca.server.InstanceUrl;

/**
 * Data access implementation using hibernate for Job Execution instances
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class JobExecutionHibernateDAO extends AbstractUnknownKeyHibernateDAO<JobExecution> 
    implements JobExecutionDAO {
    /**
     * @param instanceType
     */
    public JobExecutionHibernateDAO() {
        super(JobExecution.class);
    }

    // @see com.sam.moca.job.dao.JobExecutionDAO#read(java.lang.String, com.sam.moca.server.InstanceUrl, java.util.Date)
    @Override
    public JobExecution read(String jobId, InstanceUrl nodeUrl, Date startDate) {
        JobDefinition jobDef = new JobDefinition();
        jobDef.setJobId(jobId);
        JobExecution jobPk = new JobExecution(jobDef, nodeUrl);
        JobExecution jobExec = (JobExecution) HibernateTools.getSession().get(_instanceType, 
            jobPk);
        return jobExec;
    }
}
