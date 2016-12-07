/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.cluster.dao.hibernate;

import java.util.List;

import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.cluster.dao.RoleDefinitionDAO;
import com.redprairie.moca.dao.hibernate.AbstractHibernateDAO;

/**
 * DAO implementation that allows for creation of role definition objects
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class RoleDefinitionHibernateDAO extends AbstractHibernateDAO<RoleDefinition, String>
        implements RoleDefinitionDAO {
    /**
     */
    public RoleDefinitionHibernateDAO() {
        super(RoleDefinition.class);
    }

    // @see com.redprairie.moca.cluster.dao.RoleDefinitionDAO#readAllWithoutStar()
    @Override
    public List<RoleDefinition> readAllWithoutStar() {
        return createEmptyCriteria().addNotEqRestriction("roleId", "*").list();
    }
    
    
}
