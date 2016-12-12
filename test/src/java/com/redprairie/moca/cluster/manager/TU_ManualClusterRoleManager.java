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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.redprairie.moca.cluster.ClusterRoleAware;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.cluster.dao.RoleDefinitionDAO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the behavior of the cluster role manager to make sure
 * that the roles are applied correctly.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_ManualClusterRoleManager extends TU_AbstractClusterRoleManager {
    @SuppressWarnings("unchecked")
    @Test
    public void testSingleAvailable() throws InterruptedException {
        Collection<RoleDefinition> manuals = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAll()).thenReturn(Lists.newArrayList(definition1));
        
        long delay = 50;
        
        _roleManager = new ManualClusterRoleManager(getLockManager(), delay, 
            TimeUnit.MILLISECONDS, manuals, excludeRoles, dao, _cacheContainer, 
            aware);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        
        // We let the manager run for a couple loops, they shouldn't enable
        // anything at this point
        Thread.sleep(delay * 2 + (delay / 2));
        
        // We shouldn't have had any interactions because manual cannot pick
        // up single nodes
        Mockito.verifyZeroInteractions(_forcedRoleMap, _singleForcedRoles, aware);
        Mockito.verify(_roleMap, Mockito.never()).put(Mockito.any(Node.class), 
            Mockito.anySet());
    }
    
    @Test
    public void testMultipleRoleUpdate() throws InterruptedException {
        Collection<RoleDefinition> manuals = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAll()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        long delay = 50;
        
        _roleManager = new ManualClusterRoleManager(getLockManager(), delay, 
            TimeUnit.MILLISECONDS, manuals, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        
        // Make it look like another person was updating stuff
        _roleManager.getRoleMap().put(otherNode, 
            Sets.newHashSet(definition2, definition4));
        
        // We let the manager run for a couple loops, they shouldn't enable
        // anything at this point
        Thread.sleep(delay * 2 + (delay / 2));
        
        Mockito.verifyZeroInteractions(_forcedRoleMap, _singleForcedRoles, aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertNull(myRoles);
        assertTrue(otherRoles.contains(definition2));
        assertTrue(otherRoles.contains(definition4));
        assertEquals(2, otherRoles.size());
        assertEquals(1, roleMap.size());
    }
    
    @Test
    public void testManualRole() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAll()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        // We have 2 preferred roles, which we should pick up immediately and
        // then pick up others
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        long delay = 50;
        
        _roleManager = new ManualClusterRoleManager(getLockManager(), delay, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        
        Mockito.verify(aware).activateRole(definition3);
        Mockito.verify(aware).activateRole(definition4);
        
        // We let the manager run for a couple loops, they shouldn't enable
        // anything at this point
        Thread.sleep(delay * 2 + (delay / 2));

        // but we shouldn't have any more interactions.
        Mockito.verifyZeroInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());
        Set<RoleDefinition> myForced = forcedRoleMap.get(myNode);
        assertEquals(2, myForced.size());
        assertTrue(myForced.contains(definition3));
        assertTrue(myForced.contains(definition4));
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));
        
        Set<RoleDefinition> singleForcedRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleForcedRoles.size());
    }
    
    @Test
    public void testRecoverFromMerge() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);

        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAll()).thenReturn(
            Lists.newArrayList(definition1, definition2, definition3,
                definition4));

        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3,
            definition4);
        Collection<RoleDefinition> otherPreferred = Lists.newArrayList(
            definition1, definition2);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();

        long delay = 50;

        _roleManager = new ManualClusterRoleManager(getLockManager(), delay,
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao,
            _cacheContainer, aware);

        AbstractClusterRoleManager otherRoleManager = new ManualClusterRoleManager(
            getLockManager(), delay, TimeUnit.MILLISECONDS, otherPreferred,
            excludeRoles, dao, _cacheContainer, aware);

        configureCachesForManager(_roleManager);

        Node otherNode = Mockito.mock(Node.class,
            Mockito.withSettings().name("otherNode"));

        Node myNode = Mockito.mock(Node.class,
            Mockito.withSettings().name("myNode"));

        _roleManager.start(myNode);

        _localModification.set(false);
        otherRoleManager.start(otherNode);
        _localModification.set(true);

        // We should have notified others that we had this role
        Mockito.verify(aware).activateRole(definition3);
        Mockito.verify(aware).activateRole(definition4);

        Thread.sleep(delay * 2 + (delay / 2));

        Mockito.verifyNoMoreInteractions(aware);

        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager
            .getForcedRoleMap();

        assertEquals(2, forcedRoleMap.size());
        Set<RoleDefinition> myForcedRoles = forcedRoleMap.get(myNode);
        assertEquals(2, myForcedRoles.size());
        assertTrue(myForcedRoles.contains(definition3));
        assertTrue(myForcedRoles.contains(definition4));
        Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
        assertEquals(2, otherForcedRoles.size());
        assertTrue(otherForcedRoles.contains(definition1));
        assertTrue(otherForcedRoles.contains(definition2));

        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(2, otherRoles.size());
        assertTrue(otherRoles.contains(definition1));
        assertTrue(otherRoles.contains(definition2));

        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());

        List<Node> empty = Collections.emptyList();

        // emulate node leaving
        _roleManager.notifyMembership(myNode, Lists.newArrayList(myNode),
            empty, Lists.newArrayList(otherNode), false);

        roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));
        otherRoles = roleMap.get(otherNode);
        assertNull(otherRoles);

        final ArrayList<Node> members = Lists.newArrayList(myNode, otherNode);

        // emulate merge
        _roleManager.notifyMembership(myNode, members,
            Lists.newArrayList(otherNode), empty, true);

        _localModification.set(false);
        // emulate remote merge
        otherRoleManager.notifyMembership(otherNode, members,
            Lists.newArrayList(myNode), empty, true);
        _localModification.set(true);

        roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));
        otherRoles = roleMap.get(otherNode);
        assertEquals(2, otherRoles.size());
        assertTrue(otherRoles.contains(definition1));
        assertTrue(otherRoles.contains(definition2));
    }
    
    @Test
    public void testOtherManualRoleAlreadyTaken() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAll()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        long delay = 50;
        
        _roleManager = new ManualClusterRoleManager(getLockManager(), delay, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer, 
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        
        _roleManager.acquireRoleForcibly(true, otherNode, definition2);
        _roleManager.acquireRoleForcibly(true, otherNode, definition4);
        
        _roleManager.start(myNode);
        
        // We should have notified others that we had this role
        Mockito.verify(aware).activateRole(definition3);
        Mockito.verify(aware).activateRole(definition4);
        
        Thread.sleep(delay * 2 + (delay / 2));
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(2, forcedRoleMap.size());
        Set<RoleDefinition> myForcedRoles = forcedRoleMap.get(myNode);
        assertEquals(2, myForcedRoles.size());
        assertTrue(myForcedRoles.contains(definition3));
        assertTrue(myForcedRoles.contains(definition4));
        Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
        assertEquals(2, otherForcedRoles.size());
        assertTrue(otherForcedRoles.contains(definition2));
        assertTrue(otherForcedRoles.contains(definition4));
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(2, otherRoles.size());
        assertTrue(otherRoles.contains(definition2));
        assertTrue(otherRoles.contains(definition4));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
    }
    
    @Test
    public void testOtherPreferredRoleAlreadyTaken() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAll()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new ManualClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer, 
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.acquireRoleForcibly(false, otherNode, definition2);
        _roleManager.acquireRoleForcibly(false, otherNode, definition4);
        
        _roleManager.start(myNode);
        _roleManager.stop();
        
        // We should have notified others that we had this role
        Mockito.verify(aware).activateRole(definition3);
        Mockito.verify(aware).activateRole(definition4);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(2, forcedRoleMap.size());
        Set<RoleDefinition> myForcedRoles = forcedRoleMap.get(myNode);
        assertEquals(2, myForcedRoles.size());
        assertTrue(myForcedRoles.contains(definition3));
        assertTrue(myForcedRoles.contains(definition4));
        Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
        assertEquals(1, otherForcedRoles.size());
        assertTrue(otherForcedRoles.contains(definition2));
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(1, otherRoles.size());
        assertTrue(otherRoles.contains(definition2));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(1, singleRoles.size());
        assertTrue(singleRoles.contains(definition2));
    }
    
    @Test
    public void testOtherManualRoleTakenAfterOurs() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAll()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        long delay = 50;
        
        _roleManager = new ManualClusterRoleManager(getLockManager(), delay, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer, 
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        
        _roleManager.acquireRoleForcibly(true, otherNode, definition2);
        
        _roleManager.start(myNode);
        
        // Make it look like a remote modification, since we are taking over
        // Another person would never take over a forced role except remotely
        _localModification.set(false);
        // This is called after the start method so we should still be
        // running definition4
        _roleManager.acquireRoleForcibly(true, otherNode, definition4);
        _localModification.set(true);
        
        Thread.sleep(delay * 2 + (delay / 2));
        
        _roleManager.stop();
        
        // We should have notified others that we had this role
        InOrder order = Mockito.inOrder(aware);
        order.verify(aware).activateRole(definition3);
        order.verify(aware).activateRole(definition4);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(2, forcedRoleMap.size());
        Set<RoleDefinition> myForcedRoles = forcedRoleMap.get(myNode);
        assertEquals(2, myForcedRoles.size());
        assertTrue(myForcedRoles.contains(definition3));
        assertTrue(myForcedRoles.contains(definition4));
        Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
        assertEquals(2, otherForcedRoles.size());
        assertTrue(otherForcedRoles.contains(definition2));
        assertTrue(otherForcedRoles.contains(definition4));
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(2, otherRoles.size());
        assertTrue(otherRoles.contains(definition2));
        assertTrue(otherRoles.contains(definition4));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
    }
    
    @Test
    public void testOtherDynamicRoleAlreadyTaken() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAll()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        long delay = 50;
        
        _roleManager = new ManualClusterRoleManager(getLockManager(), delay, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer, 
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        
        _roleManager.getRoleMap().put(otherNode, 
            Sets.newHashSet(definition2, definition4));
        
        _roleManager.start(myNode);

        // We should have activated our role after the timer
        Mockito.verify(aware).activateRole(definition3);
        Mockito.verify(aware).activateRole(definition4);
        
        Thread.sleep(delay * 2 + (delay / 2));
        _roleManager.stop();
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());
        Set<RoleDefinition> myForcedRoles = forcedRoleMap.get(myNode);
        assertEquals(2, myForcedRoles.size());
        assertTrue(myForcedRoles.contains(definition3));
        assertTrue(myForcedRoles.contains(definition4));
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(1, otherRoles.size());
        assertTrue(otherRoles.contains(definition2));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
    }
}
