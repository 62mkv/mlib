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

package com.redprairie.moca.cluster.dao;

import java.util.List;

import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.dao.GenericDAO;

/**
 * Data Access Object that has available methods to work with Role Defintion
 * POJOs.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface RoleDefinitionDAO extends GenericDAO<RoleDefinition, String> {
    public boolean lockRow(RoleDefinition obj, boolean wait);
    
    public List<RoleDefinition> readAllWithoutStar();
}
