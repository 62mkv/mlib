/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.job.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.dao.hibernate.AbstractHibernateDAO;
import com.redprairie.moca.db.hibernate.HibernateTools;
import com.redprairie.moca.job.JobDefinition;
import com.redprairie.moca.job.dao.JobDefinitionDAO;

/**
 * Data Access Object class used to retrieve Task Detail objects implemented by
 * using Hibernate as the medium.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
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

    // @see com.redprairie.moca.job.dao.JobDefinitionDAO#readAllJobsForAllAndRoles(com.redprairie.moca.cluster.RoleDefinition[])
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

    // @see com.redprairie.moca.job.dao.JobDefinitionDAO#readForAllNoRoleAndRoles(com.redprairie.moca.cluster.RoleDefinition[])
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
