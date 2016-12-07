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

package com.redprairie.moca.task.dao.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.dao.hibernate.AbstractHibernateDAO;
import com.redprairie.moca.db.hibernate.HibernateTools;
import com.redprairie.moca.task.TaskDefinition;
import com.redprairie.moca.task.dao.TaskDefinitionDAO;

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
public class TaskDefinitionHibernateDAO extends AbstractHibernateDAO<
        TaskDefinition, String> implements TaskDefinitionDAO {

    /**
     * @param session
     * @param instanceType
     */
    public TaskDefinitionHibernateDAO() {
        super(TaskDefinition.class);
    }

    // @see com.redprairie.moca.task.dao.TaskDefinitionDAO#readAllTasksForAllAndRoles(com.redprairie.moca.cluster.RoleDefinition[])
    @SuppressWarnings("unchecked")
    @Override
    public List<TaskDefinition> readAllTasksForAllAndRoles(RoleDefinition... roles) {
        Criteria criteria = HibernateTools.getSession().createCriteria(TaskDefinition.class);
        
        Criterion criterion = Restrictions.sqlRestriction("{alias}.role_id = '*'");
        
        if (roles.length > 0) {
            Object[] values = new Object[roles.length];
            Type[] types = new Type[roles.length];
            StringBuilder builder = new StringBuilder("{alias}.task_id in (");
            
            for (int i = 0; i < roles.length; ++i) {
                if (i != 0) {
                    builder.append(", ");
                }
                builder.append('?');
                values[i] = roles[i].getRoleId();
                types[i] = StandardBasicTypes.STRING;
            }
            
            builder.append(')');
            
            LogicalExpression expr = Restrictions.or(
                Restrictions.sqlRestriction(builder.toString(), values, types), 
                Restrictions.in("role", roles));
            criterion = Restrictions.or(criterion, expr);
        }
        
        criteria.add(criterion);
        return (List<TaskDefinition>)criteria.list();
    }
}
