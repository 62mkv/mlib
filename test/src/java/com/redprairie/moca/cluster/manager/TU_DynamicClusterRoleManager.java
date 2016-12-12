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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.redprairie.moca.cluster.ClusterRoleAware;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.cluster.dao.RoleDefinitionDAO;
import com.redprairie.moca.cluster.jgroups.JGroupsLockManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This class tests the behavior of the cluster role manager to make sure
 * that the roles are applied correctly.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_DynamicClusterRoleManager extends TU_AbstractClusterRoleManager {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testSingleAvailable() throws InterruptedException {
        Collection<RoleDefinition> manuals = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1));
        
        _roleManager = new ManualClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, manuals, excludeRoles, dao, _cacheContainer, 
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        _roleManager.getUpdater().run();
        
        ArgumentCaptor<Set> captor = ArgumentCaptor.forClass(Set.class);
        Mockito.verify(_roleMap).putIfAbsent(Mockito.eq(myNode), captor.capture());
        
        assertTrue(captor.getValue().contains(definition1));
        
        Mockito.verify(aware).activateRole(definition1);
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void testMultipleRoleUpdate() throws InterruptedException {
        Collection<RoleDefinition> manuals = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        _roleManager = new DynamicClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, manuals, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        // Make it look like another person was updating stuff
        _roleManager.getRoleMap().put(otherNode, 
            Sets.newHashSet(definition2, definition4));
        
        // Run it twice which should pick up defition 1 and 3
        _roleManager.getUpdater().run();
        _roleManager.getUpdater().run();
        
        ArgumentCaptor<Set> captor = ArgumentCaptor.forClass(Set.class);
        Mockito.verify(_roleMap).putIfAbsent(Mockito.eq(myNode), 
            captor.capture());
        Mockito.verify(_roleMap).replace(Mockito.eq(myNode), Mockito.anySet(), 
            captor.capture());
        
        
        assertTrue(captor.getAllValues().get(0).contains(definition1));
        
        assertTrue(captor.getAllValues().get(1).contains(definition1));
        assertTrue(captor.getAllValues().get(1).contains(definition3));
        
        Mockito.verify(aware).activateRole(definition1);
        Mockito.verify(aware).activateRole(definition3);
        
        _roleManager.getUpdater().run();
        
        Mockito.verifyNoMoreInteractions(_forcedRoleMap, _singleForcedRoles, aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertTrue(myRoles.contains(definition1));
        assertTrue(otherRoles.contains(definition2));
        assertTrue(myRoles.contains(definition3));
        assertTrue(otherRoles.contains(definition4));
        assertEquals(2, myRoles.size());
        assertEquals(2, otherRoles.size());
        assertEquals(2, roleMap.size());
    }
    
    @Test
    public void testManualRole() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        // We have 2 preferred roles, which we should pick up immediately and
        // then pick up others
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new DynamicClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        Mockito.verify(aware).activateRole(definition3);
        Mockito.verify(aware).activateRole(definition4);
        
        _roleManager.getUpdater().run();
        _roleManager.getUpdater().run();

        Mockito.verify(aware).activateRole(definition1);
        Mockito.verify(aware).activateRole(definition2);
        
        // but we shouldn't have any more interactions.
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());
        Set<RoleDefinition> myForced = forcedRoleMap.get(myNode);
        assertEquals(2, myForced.size());
        assertTrue(myForced.contains(definition3));
        assertTrue(myForced.contains(definition4));
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(4, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition2));
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));
        
        Set<RoleDefinition> singleForcedRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleForcedRoles.size());
    }
    
    @Test
    public void testOtherManualRoleAlreadyTaken() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new DynamicClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer, 
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        
        _roleManager.acquireRoleForcibly(true, otherNode, definition2);
        _roleManager.acquireRoleForcibly(true, otherNode, definition4);
        
        _roleManager.start(myNode);
        _roleManager.stop();
        
        // We should have notified others that we had this role
        Mockito.verify(aware).activateRole(definition3);
        Mockito.verify(aware).activateRole(definition4);
        
        _roleManager.getUpdater().run();
        
        Mockito.verify(aware).activateRole(definition1);
        
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
        assertEquals(3, myRoles.size());
        assertTrue(myRoles.contains(definition1));
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
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new DynamicClusterRoleManager(getLockManager(), 100L, 
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
        
        _roleManager.getUpdater().run();
        
        Mockito.verify(aware).activateRole(definition1);
        
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
        assertEquals(3, myRoles.size());
        assertTrue(myRoles.contains(definition1));
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
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new DynamicClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer, 
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        
        _roleManager.acquireRoleForcibly(true, otherNode, definition2);
        
        _roleManager.start(myNode);
        _roleManager.stop();
        
        // Make it look like a remote modification, since we are taking over
        // Another person would never take over a forced role except remotely
        _localModification.set(false);
        // This is called after the start method so we should still be
        // running definition4
        _roleManager.acquireRoleForcibly(true, otherNode, definition4);
        _localModification.set(true);
        
        _roleManager.getUpdater().run();
        
        // We should have notified others that we had this role
        InOrder order = Mockito.inOrder(aware);
        order.verify(aware).activateRole(definition3);
        order.verify(aware).activateRole(definition4);
        order.verify(aware).activateRole(definition1);
        
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
        assertEquals(3, myRoles.size());
        assertTrue(myRoles.contains(definition1));
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
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new DynamicClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer, 
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        
        _roleManager.getRoleMap().put(otherNode, 
            Sets.newHashSet(definition2, definition4));
        
        _roleManager.start(myNode);
        _roleManager.stop();

        // We should have activated our role after the timer
        Mockito.verify(aware).activateRole(definition3);
        Mockito.verify(aware).activateRole(definition4);
        
        _roleManager.getUpdater().run();
        
        Mockito.verify(aware).activateRole(definition1);
        
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
        assertEquals(3, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(1, otherRoles.size());
        assertTrue(otherRoles.contains(definition2));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
    }
    
    @Test
    public void testDynamicRoleAvailableAfterNodeLeaves() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> manuals = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new DynamicClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, manuals, excludeRoles, dao, _cacheContainer, 
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        Node otherNodeGoesAway = Mockito.mock(Node.class, Mockito.withSettings().name("otherNodeGoesAway"));
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));

        // This will leave definition1 open
        _roleManager.getRoleMap().put(otherNode, 
            Sets.newHashSet(definition2));
        _roleManager.getRoleMap().put(otherNodeGoesAway, 
            Sets.newHashSet(definition3, definition4));
        
        _roleManager.start(myNode);
        _roleManager.stop();
        
        _roleManager.getUpdater().run();

        Mockito.verify(aware).activateRole(definition1);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(3, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(1, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(1, otherRoles.size());
        assertTrue(otherRoles.contains(definition2));
        Set<RoleDefinition> otherRolesGoesDown = roleMap.get(otherNodeGoesAway);
        assertEquals(2, otherRolesGoesDown.size());
        assertTrue(otherRolesGoesDown.contains(definition3));
        assertTrue(otherRolesGoesDown.contains(definition4));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
        
        // This will tell it that the otherNodeGoesAway is down
        List<Node> empty = Collections.emptyList();
        _roleManager.notifyMembership(myNode,
            Lists.newArrayList(myNode, otherNode), empty,
            Lists.newArrayList(otherNodeGoesAway), false);
        
        _roleManager.getUpdater().run();
        _roleManager.getUpdater().run();
                
        Mockito.verify(aware).activateRole(definition3);
        Mockito.verify(aware).activateRole(definition4);
        
        forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());
        
        roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        myRoles = roleMap.get(myNode);
        assertEquals(3, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));
        otherRoles = roleMap.get(otherNode);
        assertEquals(1, otherRoles.size());
        assertTrue(otherRoles.contains(definition2));
        
        singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
    }
    
    @Test
    public void testThatARoleWasStolenFromUs() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> manuals = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new DynamicClusterRoleManager(getLockManager(), 50L, 
            TimeUnit.MILLISECONDS, manuals, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));

        // This will leave definition1 open
        
        _roleManager.start(myNode);
        _roleManager.stop();
        
        _roleManager.getUpdater().run();
        _roleManager.getUpdater().run();
        _roleManager.getUpdater().run();
        _roleManager.getUpdater().run();

        // We should obtain all 4 of them first
        Mockito.verify(aware).activateRole(definition1);
        Mockito.verify(aware).activateRole(definition2);
        Mockito.verify(aware).activateRole(definition3);
        Mockito.verify(aware).activateRole(definition4);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition2));
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
        
        List<Node> empty = Collections.emptyList();
        _roleManager.notifyMembership(myNode,
            Lists.newArrayList(myNode, otherNode), empty, empty, false);
        
        // Steals are always remote modifications
        _localModification.set(false);
        _roleManager.stealRole(definition3, myNode, otherNode);
        _roleManager.stealRole(definition4, myNode, otherNode);
        _localModification.set(true);
        
        // Now definition3 is open, so wait until we take it
        Mockito.verify(aware).deactivateRole(definition3);
        Mockito.verify(aware).deactivateRole(definition4);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());
        
        roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition2));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(2, otherRoles.size());
        assertTrue(otherRoles.contains(definition3));
        assertTrue(otherRoles.contains(definition4));
        
        singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
        
        // We check our roles in case if we stole something back
        // which we shouldn't have done.
        Runnable updater = _roleManager.getUpdater();
        updater.run();
        
        forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());
        
        roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition2));
        otherRoles = roleMap.get(otherNode);
        assertEquals(2, otherRoles.size());
        assertTrue(otherRoles.contains(definition3));
        assertTrue(otherRoles.contains(definition4));
        
        singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
        
        Mockito.verifyNoMoreInteractions(aware);
    }
    
    @Test
    public void testRecoverFromMerge() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);

        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(
            Lists.newArrayList(definition1, definition2, definition3,
                definition4));

        Collection<RoleDefinition> manuals = Lists.newArrayList(definition1,
            definition2);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();

        _roleManager = new DynamicClusterRoleManager(getLockManager(),
            5000000L, TimeUnit.MILLISECONDS, manuals, excludeRoles, dao,
            _cacheContainer, aware);

        configureCachesForManager(_roleManager);

        Node otherNode = Mockito.mock(Node.class,
            Mockito.withSettings().name("otherNode"));

        Node myNode = Mockito.mock(Node.class,
            Mockito.withSettings().name("myNode"));

        // This will leave definition1 open

        _roleManager.start(myNode);
        _roleManager.stop();

        _roleManager.getUpdater().run();
        _roleManager.getUpdater().run();
        _roleManager.getUpdater().run();
        _roleManager.getUpdater().run();

        // We should obtain all 4 of them first
        Mockito.verify(aware).activateRole(definition1);
        Mockito.verify(aware).activateRole(definition2);
        Mockito.verify(aware).activateRole(definition3);
        Mockito.verify(aware).activateRole(definition4);

        Mockito.verifyNoMoreInteractions(aware);

        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager
            .getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());

        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition2));
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));

        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());

        List<Node> empty = Collections.emptyList();
        _roleManager.notifyMembership(myNode,
            Lists.newArrayList(myNode, otherNode),
            Lists.newArrayList(otherNode), empty, false);

        // Steals are always remote modifications
        _localModification.set(false);
        _roleManager.stealRole(definition3, myNode, otherNode);
        _roleManager.stealRole(definition4, myNode, otherNode);
        _localModification.set(true);

        // Now definition3 is open, so wait until we take it
        Mockito.verify(aware).deactivateRole(definition3);
        Mockito.verify(aware).deactivateRole(definition4);

        Mockito.verifyNoMoreInteractions(aware);

        forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());

        roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition2));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(2, otherRoles.size());
        assertTrue(otherRoles.contains(definition3));
        assertTrue(otherRoles.contains(definition4));

        singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());

        // We check our roles in case if we stole something back
        // which we shouldn't have done.
        Runnable updater = _roleManager.getUpdater();
        updater.run();

        forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());

        roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition2));
        otherRoles = roleMap.get(otherNode);
        assertEquals(2, otherRoles.size());
        assertTrue(otherRoles.contains(definition3));
        assertTrue(otherRoles.contains(definition4));

        singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());

        // emulate removal
        _roleManager.notifyMembership(myNode, Lists.newArrayList(myNode),
            empty, Lists.newArrayList(otherNode), false);

        // verify 3 and 4 are deactivated
        Mockito.verify(aware).deactivateRole(definition3);
        Mockito.verify(aware).deactivateRole(definition4);
        Mockito.verifyNoMoreInteractions(aware);

        // verify roles have been removed
        roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition2));

        // emulate merge
        _roleManager.notifyMembership(myNode,
            Lists.newArrayList(myNode, otherNode),
            Lists.newArrayList(otherNode), empty, true);

        // merging restarts, so lets stop
        _roleManager.stop();

        // verify role map is empty on merge
        roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());

        Mockito.reset(aware);

        _roleManager.getUpdater().run();
        _roleManager.getUpdater().run();

        // just assume that myNode got all the roles post merge
        roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        myRoles = roleMap.get(myNode);
        assertEquals(4, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition2));
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));

        // verify all roles get activated post merge
        Mockito.verify(aware).activateRole(definition3);
        Mockito.verify(aware).activateRole(definition4);
        Mockito.verifyNoMoreInteractions(aware);
    }

    
    /**
     * This test should really be in {@link TU_AbstractClusterRoleManager} class.
     * We cannot put it there else it will be ran for everyone extending it.
     */
    @Test
    public void testNullAwareObjectsPassed() {
        ClusterRoleAware aware1 = Mockito.mock(ClusterRoleAware.class);
        ClusterRoleAware aware2 = Mockito.mock(ClusterRoleAware.class);
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        JGroupsLockManager lockManager = Mockito.mock(JGroupsLockManager.class);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        try {
            new AbstractClusterRoleManager(0, TimeUnit.MILLISECONDS, 
                excludeRoles, dao, lockManager, _cacheContainer, aware1, aware2, 
                null) {
            };
            fail("We should have thrown a NullPointerException");
        }
        catch (NullPointerException e) {
            // We should have thrown this
        }
    }
    
    /**
     * This test should really be in {@link TU_AbstractClusterRoleManager} class.
     * We cannot put it there else it will be ran for everyone extending it.
     */
    @Test
    public void testNoFirstAwareObjectPassed() {
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        JGroupsLockManager lockManager = Mockito.mock(JGroupsLockManager.class);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        try {
            new AbstractClusterRoleManager(0, TimeUnit.MILLISECONDS, 
                excludeRoles, dao, lockManager, _cacheContainer, null) {
            };
            fail("We should have thrown a NullPointerException");
        }
        catch (NullPointerException e) {
            // We should have thrown this
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testNewRoleComesUp() {
        Collection<RoleDefinition> manuals = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        List<RoleDefinition> singleRole = Lists.newArrayList(definition1);
        // The first invocation is in the constructor, where as the second is
        // on the third invocation of the runnable
        Mockito.when(dao.readAllWithoutStar()).thenReturn(singleRole, 
            Lists.newArrayList(definition1, definition2));
        
        _roleManager = new DynamicClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, manuals, excludeRoles, dao, _cacheContainer, 
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        Runnable updater = _roleManager.getUpdater();
        updater.run();
        // We should have activated our role after the run
        Mockito.verify(aware).activateRole(definition1);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(1, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
        
        // It takes 3 runs to get it to fire
        updater.run();
        updater.run();
        // We should have activated our role after the run
        Mockito.verify(aware).activateRole(definition2);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());
        
        roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition2));
        
        singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRoleGoesAway() {
        Collection<RoleDefinition> manuals = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        // This will first return 1 and 2 and next call will only return 1
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, definition2), 
            Lists.newArrayList(definition1));
        
        _roleManager = new DynamicClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, manuals, excludeRoles, dao, _cacheContainer, 
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        Runnable updater = _roleManager.getUpdater();
        updater.run();
        updater.run();
        
        // We should have activated our roles after the run
        Mockito.verify(aware).activateRole(definition1);
        Mockito.verify(aware).activateRole(definition2);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition2));
        
        // It takes 3 runs to get it to fire -  we ran it twice higher up
        // Now the the defintion2 has been removed
        updater.run();
        // We should have deactivated our role after the run
        Mockito.verify(aware).deactivateRole(definition2);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());
        
        roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        myRoles = roleMap.get(myNode);
        assertEquals(1, myRoles.size());
        assertTrue(myRoles.contains(definition1));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRoleComesUpButSomeoneHadManuallyAlready() {
        Collection<RoleDefinition> manuals = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        // This will first return 1 and next call will return 1 and 2
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1), 
            Lists.newArrayList(definition1, definition2));
        
        _roleManager = new DynamicClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, manuals, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        
        _localModification.set(false);
        _roleManager.acquireRoleForcibly(true, otherNode, definition1);
        _roleManager.acquireRoleForcibly(true, otherNode, definition2);
        _localModification.set(true);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        Runnable updater = _roleManager.getUpdater();
        updater.run();
        updater.run();
        
        Mockito.verifyZeroInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());
        Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
        assertEquals(2, otherForcedRoles.size());
        assertTrue(otherForcedRoles.contains(definition1));
        assertTrue(otherForcedRoles.contains(definition2));
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(2, otherRoles.size());
        assertTrue(otherRoles.contains(definition1));
        assertTrue(otherRoles.contains(definition2));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
    }
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRoleRemovedButWeStillKeepForcibly() {
        Collection<RoleDefinition> manuals = Lists.newArrayList(definition1, 
            definition2);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        // The first invocation is in the constructor, where as the second is
        // on the third invocation of the runnable
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, definition2), 
            Lists.newArrayList(definition1));
        
        _roleManager = new ManualClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, manuals, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().serializable().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        // We should have activated our roles after the run
        Mockito.verify(aware).activateRole(definition1);
        Mockito.verify(aware).activateRole(definition2);
        
        Mockito.verifyZeroInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());
        Set<RoleDefinition> myForcedRoles = forcedRoleMap.get(myNode);
        assertEquals(2, myForcedRoles.size());
        assertTrue(myForcedRoles.contains(definition1));
        assertTrue(myForcedRoles.contains(definition2));
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition2));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
        
        Runnable updater = _roleManager.getUpdater();
        updater.run();
        updater.run();
        // The third run will check the roles and see that definition 2 is gone
        updater.run();
        
        Mockito.verifyZeroInteractions(aware);
        
        forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());
        myForcedRoles = forcedRoleMap.get(myNode);
        assertEquals(2, myForcedRoles.size());
        assertTrue(myForcedRoles.contains(definition1));
        assertTrue(myForcedRoles.contains(definition2));
        
        roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition2));
        
        singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
    }
    
    @Test
    public void testOurRoleIsStolenByManual() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> manuals = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new DynamicClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, manuals, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().serializable().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        Runnable updater = _roleManager.getUpdater();
        
        // We grab all 4 of the roles
        updater.run();
        updater.run();
        updater.run();
        updater.run();
        
        Mockito.verify(aware).activateRole(definition1);
        Mockito.verify(aware).activateRole(definition2);
        Mockito.verify(aware).activateRole(definition3);
        Mockito.verify(aware).activateRole(definition4);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().serializable());
        
        _roleManager.acquireRoleForcibly(true, otherNode, definition1);
        _roleManager.acquireRoleForcibly(true, otherNode, definition2);
        _roleManager.acquireRoleForcibly(true, otherNode, definition4);
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());
        Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
        assertEquals(3, otherForcedRoles.size());
        assertTrue(otherForcedRoles.contains(definition1));
        assertTrue(otherForcedRoles.contains(definition2));
        assertTrue(otherForcedRoles.contains(definition4));
        
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(3, otherRoles.size());
        assertTrue(otherRoles.contains(definition1));
        assertTrue(otherRoles.contains(definition2));
        assertTrue(otherRoles.contains(definition4));
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(1, myRoles.size());
        assertTrue(myRoles.contains(definition3));
    }
    
    @Test
    public void testOurRoleIsAvailableAfterDynamicLeft() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> manuals = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new DynamicClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, manuals, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().serializable().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().serializable());
        
        _roleManager.acquireRoleForcibly(true, otherNode, definition1);
        _roleManager.acquireRoleForcibly(true, otherNode, definition2);
        _roleManager.acquireRoleForcibly(true, otherNode, definition4);
        
        Runnable updater = _roleManager.getUpdater();
        
        // Run in twice, we should only get 3
        updater.run();
        updater.run();

        Mockito.verify(aware).activateRole(definition3);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());
        Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
        assertEquals(3, otherForcedRoles.size());
        assertTrue(otherForcedRoles.contains(definition1));
        assertTrue(otherForcedRoles.contains(definition2));
        assertTrue(otherForcedRoles.contains(definition4));
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(3, otherRoles.size());
        assertTrue(otherRoles.contains(definition1));
        assertTrue(otherRoles.contains(definition2));
        assertTrue(otherRoles.contains(definition4));
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(1, myRoles.size());
        assertTrue(myRoles.contains(definition3));
        
        // Now we tell the manager that the other node is gone, which should
        // make the other 3 roles available
        List<Node> empty = Collections.emptyList();
        _roleManager.notifyMembership(myNode,
            Collections.singletonList(myNode), empty,
            Collections.singletonList(otherNode), false);
        
        // Run in 3 times, we should get all the rest
        updater.run();
        updater.run();
        updater.run();
        
        Mockito.verify(aware).activateRole(definition1);
        Mockito.verify(aware).activateRole(definition2);
        Mockito.verify(aware).activateRole(definition4);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
        
        forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());
        
        roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        myRoles = roleMap.get(myNode);
        assertEquals(4, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition2));
        assertTrue(myRoles.contains(definition3));
        assertTrue(myRoles.contains(definition4));
    }
    
    @Test
    public void testRoleOnOtherNodeIsStolenByPreferred() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> manuals = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new DynamicClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, manuals, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().serializable().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        Node otherNode2 = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode2"));
        
        // Make it look like otherNode was up first and grabbed everything
        // normally
        _roleManager.getRoleMap().put(otherNode, Sets.newHashSet(definition1, 
            definition2, definition3, definition4));
        
        // Now otherNode2 stole 2 of the nodes
        _localModification.set(false);
        _roleManager.acquireRoleForcibly(true, otherNode2, definition2);
        _roleManager.acquireRoleForcibly(true, otherNode2, definition3);
        _localModification.set(true);
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());
        Set<RoleDefinition> otherForcedRoles2 = forcedRoleMap.get(otherNode2);
        otherForcedRoles2.contains(definition2);
        otherForcedRoles2.contains(definition3);
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(2, otherRoles.size());
        assertTrue(otherRoles.contains(definition1));
        assertTrue(otherRoles.contains(definition4));
        Set<RoleDefinition> otherRoles2 = roleMap.get(otherNode2);
        assertEquals(2, otherRoles2.size());
        assertTrue(otherRoles2.contains(definition2));
        assertTrue(otherRoles2.contains(definition3));
    }
}
