/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.cluster.manager;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.infinispan.manager.CacheContainer;

import com.redprairie.moca.cluster.ClusterRoleAware;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.cluster.dao.RoleDefinitionDAO;
import com.redprairie.moca.cluster.jgroups.JGroupsLockManager;
import com.redprairie.moca.util.MocaUtils;

/**
 * This is the base manual cluster role manager.  It provides access and support
 * for manual roles and the obtaining of them
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public abstract class AbstractManualBasedClusterRoleManager extends
        AbstractClusterRoleManager {
    
    /**
     * @param factory
     * @param delay
     * @param timeUnit
     * @param dao
     * @param lockManager
     * @param clusterRoleAwareObjects
     */
    public AbstractManualBasedClusterRoleManager(long delay, TimeUnit timeUnit,
            Iterable<RoleDefinition> manualRoles, Iterable<RoleDefinition> excludeRoles,
            RoleDefinitionDAO dao, JGroupsLockManager lockManager, 
            CacheContainer container, ClusterRoleAware aware, 
            ClusterRoleAware... clusterRoleAwareObjects) {
        super(delay, timeUnit, excludeRoles, dao, lockManager, container, aware,
            clusterRoleAwareObjects);
        
        // We create an inner linked hash set so we can make it unmodifiable below
        // Linked to preserve the original iterable ordering
        Set<RoleDefinition> manualRolesCopy = new LinkedHashSet<RoleDefinition>();
        
        for (RoleDefinition manualRole : manualRoles) {
            manualRolesCopy.add(manualRole);
        }
        
        _manualRoles = Collections.unmodifiableSet(manualRolesCopy);
    }
    
    protected void obtainManualRoles() {
        // Depending on the lock manager we don't want to mess up
        // the caller's transaction so we spawn a thread to be safe.
        Thread thread = new Thread() {
            // @see java.lang.Thread#run()
            @Override
            public void run() {
                for (RoleDefinition def : _manualRoles) {
                    Lock roleLock = _lockManager.getLock(def);
                    
                    roleLock.lock();
                    try {
                        // We manually grab the role since it is our manual
                        // role.
                        acquireRoleForcibly(true, def);
                    }
                    finally {
                        roleLock.unlock();
                    }
                }
            }
        };
        
        _logger.debug(MocaUtils.concat("Our manual roles: ", _manualRoles));
        
        thread.start();
        
        try {
            thread.join(); 
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * This set of manual roles cannot be modified and is thread safe
     */
    protected final Set<RoleDefinition> _manualRoles;
}