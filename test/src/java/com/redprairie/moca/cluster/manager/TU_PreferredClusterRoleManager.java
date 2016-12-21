/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.sam.moca.cluster.manager;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sam.moca.cluster.ClusterRoleAware;
import com.sam.moca.cluster.Node;
import com.sam.moca.cluster.RoleDefinition;
import com.sam.moca.cluster.dao.RoleDefinitionDAO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the behavior of the cluster role manager to make sure
 * that the roles are applied correctly.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_PreferredClusterRoleManager extends TU_AbstractClusterRoleManager {
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
        
        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L, 
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
        
        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L, 
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
        assertEquals(2, singleForcedRoles.size());
        assertTrue(singleForcedRoles.contains(definition3));
        assertTrue(singleForcedRoles.contains(definition4));
    }
    
    @Test
    public void testOtherManualRoleAlreadyTaken() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L, 
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
        
        _roleManager.getUpdater().run();
        
        Mockito.verify(aware).activateRole(definition1);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(2, forcedRoleMap.size());
        Set<RoleDefinition> myForcedRoles = forcedRoleMap.get(myNode);
        assertEquals(1, myForcedRoles.size());
        assertTrue(myForcedRoles.contains(definition3));
        Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
        assertEquals(2, otherForcedRoles.size());
        assertTrue(otherForcedRoles.contains(definition2));
        assertTrue(otherForcedRoles.contains(definition4));
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition3));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(2, otherRoles.size());
        assertTrue(otherRoles.contains(definition2));
        assertTrue(otherRoles.contains(definition4));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(1, singleRoles.size());
        assertTrue(singleRoles.contains(definition3));
    }
    
    @Test
    public void testOtherPreferredRoleAlreadyTaken() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L, 
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
        
        _roleManager.getUpdater().run();
        
        Mockito.verify(aware).activateRole(definition1);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(2, forcedRoleMap.size());
        Set<RoleDefinition> myForcedRoles = forcedRoleMap.get(myNode);
        assertEquals(1, myForcedRoles.size());
        assertTrue(myForcedRoles.contains(definition3));
        Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
        assertEquals(2, otherForcedRoles.size());
        assertTrue(otherForcedRoles.contains(definition2));
        assertTrue(otherForcedRoles.contains(definition4));
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition3));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(2, otherRoles.size());
        assertTrue(otherRoles.contains(definition2));
        assertTrue(otherRoles.contains(definition4));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(3, singleRoles.size());
        assertTrue(singleRoles.contains(definition2));
        assertTrue(singleRoles.contains(definition3));
        assertTrue(singleRoles.contains(definition4));
    }
    
    @Test
    public void testOtherManualRoleTakenAfterOurs() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L, 
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
        order.verify(aware).deactivateRole(definition4);
        order.verify(aware).activateRole(definition1);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(2, forcedRoleMap.size());
        Set<RoleDefinition> myForcedRoles = forcedRoleMap.get(myNode);
        assertEquals(1, myForcedRoles.size());
        assertTrue(myForcedRoles.contains(definition3));
        Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
        assertEquals(2, otherForcedRoles.size());
        assertTrue(otherForcedRoles.contains(definition2));
        assertTrue(otherForcedRoles.contains(definition4));
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition3));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(2, otherRoles.size());
        assertTrue(otherRoles.contains(definition2));
        assertTrue(otherRoles.contains(definition4));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(1, singleRoles.size());
        assertTrue(singleRoles.contains(definition3));
    }
    
    @Test
    public void testOtherDynamicRoleAlreadyTaken() throws InterruptedException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L, 
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
        assertEquals(2, singleRoles.size());
        assertTrue(singleRoles.contains(definition3));
        assertTrue(singleRoles.contains(definition4));
    }
    
    @Test
    public void testManualRoleThatOverrodeOursIsGone() throws InterruptedException, IOException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer, 
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        Node otherNodeGoesDown = Mockito.mock(Node.class, Mockito.withSettings().name("otherNodeGoesDown"));
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));

        _roleManager.acquireRoleForcibly(true, otherNode, definition2);
        
        _roleManager.start(myNode);
        _roleManager.stop();
        
        // Make it look like a remote modification, since we are taking over
        // Another person would never take over a forced role except remotely
        _localModification.set(false);
        // This is like a manual role manager coming up that only has 1 role
        _roleManager.acquireRoleForcibly(true, otherNodeGoesDown, definition4);
        _localModification.set(true);
        
        _roleManager.getUpdater().run();
        
        // We should have notified others that we had this role
        InOrder order = Mockito.inOrder(aware);
        order.verify(aware).activateRole(definition3);
        order.verify(aware).activateRole(definition4);
        order.verify(aware).deactivateRole(definition4);
        order.verify(aware).activateRole(definition1);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(3, forcedRoleMap.size());
        Set<RoleDefinition> myForcedRoles = forcedRoleMap.get(myNode);
        assertEquals(1, myForcedRoles.size());
        assertTrue(myForcedRoles.contains(definition3));
        Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
        assertEquals(1, otherForcedRoles.size());
        assertTrue(otherForcedRoles.contains(definition2));
        Set<RoleDefinition> otherForcedRolesGoesDown = forcedRoleMap.get(otherNodeGoesDown);
        assertEquals(1, otherForcedRolesGoesDown.size());
        assertTrue(otherForcedRolesGoesDown.contains(definition4));
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(3, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition3));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(1, otherRoles.size());
        assertTrue(otherRoles.contains(definition2));
        Set<RoleDefinition> otherRolesGoesDown = roleMap.get(otherNodeGoesDown);
        assertEquals(1, otherRolesGoesDown.size());
        assertTrue(otherRolesGoesDown.contains(definition4));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(1, singleRoles.size());
        assertTrue(singleRoles.contains(definition3));
        
        // We reset the mocks so we can reverify without having to do double
        Mockito.reset(aware);
        
        // By doing this the otherNodeGoesAway is symbolized to go down
        List<Node> empty = Collections.emptyList();
        _roleManager.notifyMembership(myNode,
            Lists.newArrayList(myNode, otherNode), empty,
            Lists.newArrayList(otherNodeGoesDown), false);
        
        // Sleep to let the update take effect to ge the role back - the
        // preferred role manager does the update on a separate thread
        // We should figure out a way to get rid of this
        Thread.sleep(500);
        
        Mockito.verify(aware).activateRole(definition4);
        
        forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(2, forcedRoleMap.size());
        myForcedRoles = forcedRoleMap.get(myNode);
        assertEquals(2, myForcedRoles.size());
        assertTrue(myForcedRoles.contains(definition3));
        assertTrue(myForcedRoles.contains(definition4));
        otherForcedRoles = forcedRoleMap.get(otherNode);
        assertEquals(1, otherForcedRoles.size());
        assertTrue(otherForcedRoles.contains(definition2));
        
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
        assertEquals(2, singleRoles.size());
        assertTrue(singleRoles.contains(definition3));
        assertTrue(singleRoles.contains(definition4));
        
        Mockito.verifyNoMoreInteractions(aware);
        
        _roleManager.stop();
    }

    @Test
    public void testRecoverFromMerge() throws InterruptedException, IOException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);

        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Lists.newArrayList(definition3, definition4);
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer, 
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        Node otherNodeGoesDown = Mockito.mock(Node.class, Mockito.withSettings().name("otherNodeGoesDown"));
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));

        _roleManager.acquireRoleForcibly(true, otherNode, definition2);
        
        _roleManager.start(myNode);
        _roleManager.stop();
        
        // Make it look like a remote modification, since we are taking over
        // Another person would never take over a forced role except remotely
        _localModification.set(false);
        // This is like a manual role manager coming up that only has 1 role
        _roleManager.acquireRoleForcibly(true, otherNodeGoesDown, definition4);
        _localModification.set(true);
        
        _roleManager.getUpdater().run();
        
        // We should have notified others that we had this role
        InOrder order = Mockito.inOrder(aware);
        order.verify(aware).activateRole(definition3);
        order.verify(aware).activateRole(definition4);
        order.verify(aware).deactivateRole(definition4);
        order.verify(aware).activateRole(definition1);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(3, forcedRoleMap.size());
        Set<RoleDefinition> myForcedRoles = forcedRoleMap.get(myNode);
        assertEquals(1, myForcedRoles.size());
        assertTrue(myForcedRoles.contains(definition3));
        Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
        assertEquals(1, otherForcedRoles.size());
        assertTrue(otherForcedRoles.contains(definition2));
        Set<RoleDefinition> otherForcedRolesGoesDown = forcedRoleMap.get(otherNodeGoesDown);
        assertEquals(1, otherForcedRolesGoesDown.size());
        assertTrue(otherForcedRolesGoesDown.contains(definition4));
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(3, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(2, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        assertTrue(myRoles.contains(definition3));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(1, otherRoles.size());
        assertTrue(otherRoles.contains(definition2));
        Set<RoleDefinition> otherRolesGoesDown = roleMap.get(otherNodeGoesDown);
        assertEquals(1, otherRolesGoesDown.size());
        assertTrue(otherRolesGoesDown.contains(definition4));
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(1, singleRoles.size());
        assertTrue(singleRoles.contains(definition3));
        
        // We reset the mocks so we can reverify without having to do double
        Mockito.reset(aware);
        
        // By doing this the otherNodeGoesAway is symbolized to go down
        List<Node> empty = Collections.emptyList();
        _roleManager.notifyMembership(myNode, 
            Lists.newArrayList(myNode, otherNode), empty, Lists.newArrayList(
                otherNodeGoesDown), false);
        
        // Sleep to let the update take effect to get the role back - the
        // preferred role manager does the update on a separate thread
        // We should figure out a way to get rid of this
        Thread.sleep(500);
        
        Mockito.verify(aware).activateRole(definition4);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Mockito.reset(aware);
        
        forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(2, forcedRoleMap.size());
        myForcedRoles = forcedRoleMap.get(myNode);
        assertEquals(2, myForcedRoles.size());
        assertTrue(myForcedRoles.contains(definition3));
        assertTrue(myForcedRoles.contains(definition4));
        otherForcedRoles = forcedRoleMap.get(otherNode);
        assertEquals(1, otherForcedRoles.size());
        assertTrue(otherForcedRoles.contains(definition2));
        
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
        assertEquals(2, singleRoles.size());
        assertTrue(singleRoles.contains(definition3));
        assertTrue(singleRoles.contains(definition4));
        
        _roleManager.notifyMembership(myNode,
            Lists.newArrayList(myNode, otherNode, otherNodeGoesDown),
            Lists.newArrayList(otherNodeGoesDown), empty, true);
        
        // the role manager may start to allocate merge causes the role manager
        // to stop and start on it's own
        //_roleManager.stop();
        
        Mockito.verify(aware).deactivateRole(definition1);
        // would need 2nd role manager to verify, not possible in single node test
        // Mockito.verify(aware).deactivateRole(definition2);
        Mockito.verify(aware).deactivateRole(definition3);
        Mockito.verify(aware).deactivateRole(definition4);
        
        
        // lets find out how many were allocated because of merge
        int allocated = _roleManager.getRoleMap().size();
        
        // just have our role manager take the rest
        
        for(int i = 0; i < 4 - allocated; i++){
                _roleManager.getUpdater().run();        
        }
        
        Thread.sleep(20000);
        

        roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        
        // in real world, other node's role manager would handle merge as well
        // the best we can do here is check if nothing has changed
        roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());

        myRoles = roleMap.get(myNode);
        assertEquals(4, myRoles.size());
        
    }
    
    @Test
    public void testExcludeRoleIsNotPickedFromOne()
            throws InterruptedException, IOException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);

        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(
            Lists.newArrayList(definition1, definition2));

        Collection<RoleDefinition> preferred = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Lists.newArrayList(
            definition1, definition2);

        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L,
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao,
            _cacheContainer, aware);

        configureCachesForManager(_roleManager);

        Node myNode = Mockito.mock(Node.class,
            Mockito.withSettings().name("myNode"));

        _roleManager.start(myNode);
        _roleManager.stop();

        _roleManager.getUpdater().run();

        // No interactions should happen since the role we have available is
        // in exclude list
        Mockito.verifyZeroInteractions(aware);

        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager
            .getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());

        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(0, roleMap.size());

        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
    }

    @Test
    public void testExcludeRoleIsNotPickedMultiple()
            throws InterruptedException, IOException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);

        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(
            Lists.newArrayList(definition1, definition2, definition3,
                definition4));

        Collection<RoleDefinition> preferred = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Lists.newArrayList(
            definition1, definition3);

        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L,
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao,
            _cacheContainer, aware);

        configureCachesForManager(_roleManager);

        Node myNode = Mockito.mock(Node.class,
            Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();

        Runnable updater = _roleManager.getUpdater();

        updater.run();
        updater.run();
        updater.run();

        Mockito.verify(aware).activateRole(definition2);
        Mockito.verify(aware).activateRole(definition4);

        Mockito.verifyNoMoreInteractions(aware);

        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager
            .getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());

        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(1, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertTrue(myRoles.contains(definition2));
        assertTrue(myRoles.contains(definition4));

        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
    }
    
    @Test
    public void testExcludeRoleIsNotStolen() throws InterruptedException, IOException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Lists.newArrayList(definition2, 
            definition3, definition4);
        
        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        
        _roleManager.getRoleMap().put(otherNode, 
            Sets.newHashSet(definition1, definition2, definition3, definition4));
        
        Runnable updater = _roleManager.getUpdater();
        
        // We should only steal definition 1 and no others since they are excluded
        updater.run();
        updater.run();
        
        Mockito.verify(aware).activateRole(definition1);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());
        
        Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
        assertEquals(2, roleMap.size());
        Set<RoleDefinition> myRoles = roleMap.get(myNode);
        assertEquals(1, myRoles.size());
        assertTrue(myRoles.contains(definition1));
        Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
        assertEquals(3, otherRoles.size());
        assertTrue(otherRoles.contains(definition2));
        assertTrue(otherRoles.contains(definition3));
        assertTrue(otherRoles.contains(definition4));
        
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
    }
    
    @Test
    public void testOurRoleIsStolenByPreferred() throws InterruptedException, IOException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
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
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        
        _roleManager.acquireRoleForcibly(false, otherNode, definition1);
        _roleManager.acquireRoleForcibly(false, otherNode, definition2);
        _roleManager.acquireRoleForcibly(false, otherNode, definition4);
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertTrue(singleRoles.contains(definition1));
        assertTrue(singleRoles.contains(definition2));
        assertTrue(singleRoles.contains(definition4));
        assertEquals(3, singleRoles.size());
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());
        Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
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
    public void testOurRoleIsAvailableAfterPreferredLeft() throws InterruptedException, IOException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().serializable());
        
        _roleManager.acquireRoleForcibly(false, otherNode, definition1);
        _roleManager.acquireRoleForcibly(false, otherNode, definition2);
        _roleManager.acquireRoleForcibly(false, otherNode, definition4);
        
        Runnable updater = _roleManager.getUpdater();
        
        // Run in twice, we should only get 3
        updater.run();
        updater.run();

        Mockito.verify(aware).activateRole(definition3);
        
        Mockito.verifyNoMoreInteractions(aware);
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(3, singleRoles.size());
        assertTrue(singleRoles.contains(definition1));
        assertTrue(singleRoles.contains(definition2));
        assertTrue(singleRoles.contains(definition4));
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());
        Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
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
        _roleManager.notifyMembership(myNode, Collections.singletonList(myNode), 
            empty, Collections.singletonList(otherNode), false);
        
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
    public void testRoleOnOtherNodeIsStolen() throws InterruptedException, IOException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().serializable());
        Node otherNode2 = Mockito.mock(Node.class, Mockito.withSettings().serializable());
        
        // Make it look like otherNode was up first and grabbed everything
        // normally
        _roleManager.getRoleMap().put(otherNode, Sets.newHashSet(definition1, 
            definition2, definition3, definition4));
        
        // Now otherNode2 stole 2 of the nodes
        _localModification.set(false);
        _roleManager.stealRole(definition2, otherNode, otherNode2);
        _roleManager.stealRole(definition3, otherNode, otherNode2);
        _localModification.set(true);
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertEquals(0, singleRoles.size());
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(0, forcedRoleMap.size());
        
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
    
    @Test
    public void testRoleOnOtherNodeIsStolenByPreferred() throws InterruptedException, IOException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().serializable());
        Node otherNode2 = Mockito.mock(Node.class, Mockito.withSettings().serializable());
        
        // Make it look like otherNode was up first and grabbed everything
        // normally
        _roleManager.getRoleMap().put(otherNode, Sets.newHashSet(definition1, 
            definition2, definition3, definition4));
        
        // Now otherNode2 stole 2 of the nodes
        _localModification.set(false);
        _roleManager.acquireRoleForcibly(false, otherNode2, definition2);
        _roleManager.acquireRoleForcibly(false, otherNode2, definition3);
        _localModification.set(true);
        
        Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
        assertTrue(singleRoles.contains(definition2));
        assertTrue(singleRoles.contains(definition3));
        assertEquals(2, singleRoles.size());
        
        Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
        assertEquals(1, forcedRoleMap.size());
        Set<RoleDefinition> otherForcedRoles2 = forcedRoleMap.get(otherNode2);
        assertTrue(otherForcedRoles2.contains(definition2));
        assertTrue(otherForcedRoles2.contains(definition3));
        
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
    
    @Test
    public void test2OthersStealAtSameTimeAgainstSameNode() 
            throws InterruptedException, BrokenBarrierException, 
            TimeoutException, ExecutionException {
        ClusterRoleAware aware = Mockito.mock(ClusterRoleAware.class);
        
        RoleDefinitionDAO dao = Mockito.mock(RoleDefinitionDAO.class);
        Mockito.when(dao.readAllWithoutStar()).thenReturn(Lists.newArrayList(definition1, 
            definition2, definition3, definition4));
        
        Collection<RoleDefinition> preferred = Collections.emptyList();
        Collection<RoleDefinition> excludeRoles = Collections.emptyList();
        
        _roleManager = new PreferredClusterRoleManager(getLockManager(), 100L, 
            TimeUnit.MILLISECONDS, preferred, excludeRoles, dao, _cacheContainer,
            aware);
        
        configureCachesForManager(_roleManager);
        
        Node myNode = Mockito.mock(Node.class, Mockito.withSettings().name("myNode"));
        _roleManager.start(myNode);
        _roleManager.stop();
        
        final Node otherNode = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode"));
        final Node otherNode2 = Mockito.mock(Node.class, Mockito.withSettings().name("otherNode2"));
        
        ExecutorService service = Executors.newFixedThreadPool(2);
        final CyclicBarrier barrier = new CyclicBarrier(3);
        
        for (int i = 0; i < 5; ++i) {
            System.out.println("Iteration: " + i);
            _roleManager.getRoleMap().clear();
            _roleManager.getForcedRoleMap().clear();
            _roleManager.getSingleForcedRoleSet().clear();
            // Make it look like otherNode was up first and grabbed everything
            // normally
            _roleManager.getRoleMap().put(myNode, Sets.newHashSet(definition1, 
                definition2, definition3, definition4));
            
            _localModification.set(false);
            
            Future<Void> future = service.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    barrier.await();
                    _roleManager.acquireRoleForcibly(false, otherNode, definition1);
                    _roleManager.acquireRoleForcibly(false, otherNode, definition3);
                    return null;
                }
            });
            Future<Void> future2 = service.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    barrier.await();
                    _roleManager.acquireRoleForcibly(false, otherNode2, definition2);
                    _roleManager.acquireRoleForcibly(false, otherNode2, definition4);
                    return null;
                }
            });
            
            barrier.await(2, TimeUnit.SECONDS);
            
            future.get(2, TimeUnit.SECONDS);
            future2.get(2, TimeUnit.SECONDS);
            
            _localModification.set(true);
            
            Set<RoleDefinition> singleRoles = _roleManager.getSingleForcedRoleSet();
            assertEquals(4, singleRoles.size());
            assertTrue(singleRoles.contains(definition1));
            assertTrue(singleRoles.contains(definition2));
            assertTrue(singleRoles.contains(definition3));
            assertTrue(singleRoles.contains(definition4));
            
            Map<Node, Set<RoleDefinition>> forcedRoleMap = _roleManager.getForcedRoleMap();
            assertEquals(2, forcedRoleMap.size());
            Set<RoleDefinition> otherForcedRoles = forcedRoleMap.get(otherNode);
            assertTrue(otherForcedRoles.contains(definition1));
            assertTrue(otherForcedRoles.contains(definition3));
            Set<RoleDefinition> otherForcedRoles2 = forcedRoleMap.get(otherNode2);
            assertTrue(otherForcedRoles2.contains(definition2));
            assertTrue(otherForcedRoles2.contains(definition4));
            
            Map<Node, Set<RoleDefinition>> roleMap = _roleManager.getRoleMap();
            assertEquals(3, roleMap.size());
            Set<RoleDefinition> myRoles = roleMap.get(myNode);
            assertEquals(0, myRoles.size());
            Set<RoleDefinition> otherRoles = roleMap.get(otherNode);
            assertEquals(2, otherRoles.size());
            assertTrue(otherRoles.contains(definition1));
            assertTrue(otherRoles.contains(definition3));
            Set<RoleDefinition> otherRoles2 = roleMap.get(otherNode2);
            assertEquals(2, otherRoles2.size());
            assertTrue(otherRoles2.contains(definition2));
            assertTrue(otherRoles2.contains(definition4));
        }
    }
}