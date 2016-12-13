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

package com.redprairie.moca.cluster;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.redprairie.moca.cluster.dao.RoleDefinitionDAO;
import com.redprairie.moca.cluster.dao.hibernate.RoleDefinitionHibernateDAO;

/**
 * This class is the spring container for the task and jobs stuff.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
@Configuration
public class RoleConfig {
    @Bean
    public RoleDefinitionDAO roleDefinitionDAO() {
        return new RoleDefinitionHibernateDAO();
    }
}
