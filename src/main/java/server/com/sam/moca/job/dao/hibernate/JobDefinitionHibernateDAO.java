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

package com.sam.moca.job.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.sam.moca.cluster.RoleDefinition;
import com.sam.moca.dao.hibernate.AbstractHibernateDAO;
import com.sam.moca.db.hibernate.HibernateTools;
import com.sam.moca.job.JobDefinition;
import com.sam.moca.job.dao.JobDefinitionDAO;

/**
 * Data Access Object class used to retrieve Task Detail objects implemented by
 * using Hibernate as the medium.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class JobDefinitionHibernateDAO
     extends AbstractHibernateDAO<JobDefinition, String> implements JobDefinitionDAO {

    /**
     * @param session
     * @param instanceType
     */
    public JobDefinitionHibernateDAO() {
        super(JobDefinition.class);
    }

    // @see com.sam.moca.job.dao.JobDefinitionDAO#readAllJobsForAllAndRoles(com.sam.moca.cluster.RoleDefinition[])
    @SuppressWarnings("unchecked")
    @Override
    public List<JobDefinition> readForAllAndRoles(RoleDefinition... roles) {
        Criteria criteria = HibernateTools.getSession().createCriteria(JobDefinition.class);
        
        Criterion criterion = Restrictions.sqlRestriction("{alias}.role_id = '*'");
        
        if (roles.length > 0) {
            
            criterion = Restrictions.or(criterion, 
                Restrictions.in("role", roles));
        }
        
        criteria.add(criterion);
        return (List<JobDefinition>)criteria.list();
    }

    // @see com.sam.moca.job.dao.JobDefinitionDAO#readForAllNoRoleAndRoles(com.sam.moca.cluster.RoleDefinition[])
    @SuppressWarnings("unchecked")
    @Override
    public List<JobDefinition> readForAllNoRoleAndRoles(RoleDefinition... roles) {
        Criteria criteria = HibernateTools.getSession().createCriteria(JobDefinition.class);
        
        Criterion criterion = null;
        Criterion firstCrit = Restrictions.sqlRestriction("{alias}.role_id = '*'");
        Criterion secondCrit = Restrictions.isNull("role");
        
        if (roles.length > 0) {
            
            criterion = Restrictions.or(firstCrit, secondCrit,
                Restrictions.in("role", roles));
        }
        else {
            criterion = Restrictions.or(firstCrit, secondCrit);
        }
        
        criteria.add(criterion);
        return (List<JobDefinition>)criteria.list();
    }
}
