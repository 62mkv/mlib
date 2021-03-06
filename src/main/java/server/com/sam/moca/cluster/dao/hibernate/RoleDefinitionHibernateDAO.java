/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.moca.cluster.dao.hibernate;

import java.util.List;

import com.sam.moca.cluster.RoleDefinition;
import com.sam.moca.cluster.dao.RoleDefinitionDAO;
import com.sam.moca.dao.hibernate.AbstractHibernateDAO;

/**
 * DAO implementation that allows for creation of role definition objects
 * 
 * Copyright (c) 2010 Sam Corporation
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

    // @see com.sam.moca.cluster.dao.RoleDefinitionDAO#readAllWithoutStar()
    @Override
    public List<RoleDefinition> readAllWithoutStar() {
        return createEmptyCriteria().addNotEqRestriction("roleId", "*").list();
    }
    
    
}
