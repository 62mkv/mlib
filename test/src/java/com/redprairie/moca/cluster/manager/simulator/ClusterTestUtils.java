/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.sam.moca.cluster.manager.simulator;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.sam.moca.MocaException;
import com.sam.moca.MocaInterruptedException;
import com.sam.moca.MocaResults;
import com.sam.util.ArgCheck;

import static org.junit.Assert.*;

/**
 * 
 * A collection of utility methods for validating a {@link ClusterManager}
 * and its state
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public final class ClusterTestUtils {
    
    public static final String CLUSTER_URL_CACHE_NAME = "moca-node-urls";
    public static final String TASK_CACHE_NAME = "moca-task-cache";
    public static final String JOB_CACHE_NAME = "moca-job-cache";
    public static final String CURRENT_ROLES_CACHE_NAME = "moca-current-roles";
    public static final String FORCED_ROLES_CACHE_NAME = "moca-forced-roles";
    public static final String SINGLE_FORCED_ROLES_CACHE_NAME = "moca-single-forced-roles";
    
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    
    /**
     * Creates the test utils instance with the given PrintStream (for debugging)
     * and cluster manager
     * @param out  The stream to write messages to
     * @param manager The manager to validate
     */
    public ClusterTestUtils(PrintStream out, ClusterManager manager) {
        ArgCheck.notNull(out);
        ArgCheck.notNull(manager);
        _out = out;
        _manager = manager;
    }
    
    /**
     * Asserts all nodes on the cluster are functional
     */
    public void assertClusterOperational() {
        for (ClusterNode node : _manager.getNodes()) {
            assertNodeOperational(node);
        }
    }
    
    /**
     * Asserts the specific node is functional
     * @param node
     */
    public void assertNodeOperational(ClusterNode node) {
        writeLineWithDate("Checking if node " + node + " is up...");
        MocaResults res;
        try {
            res = node.getConnection().executeCommand("publish data where foo='bar'");
            res.next();
            assertEquals("NODE " + node + " is not operational",
                "bar", res.getString("foo"));
        }
        catch (MocaException e) {
            fail("Unable to connect to node: " + node);
        }
    }
    
    /**
     * Prints the state of the cluster for the given duration blocking
     * on this call for that time.
     * @param time  The time to print for
     * @param unit  The time unit
     * @throws MocaException
     */
    public void printStateForDuration(long time, TimeUnit unit) throws MocaException {
        long endTime = System.currentTimeMillis() + unit.toMillis(time);
        while (System.currentTimeMillis() < endTime) {
            printClusterState();
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                throw new MocaInterruptedException(e);
            }
        }
    }
    
    /**
     * Blocks UDP traffic on the given node
     * @param node
     * @throws MocaException
     */
    public void blockUdpOnNode(ClusterNode node) throws MocaException {
        writeLineWithDate("Blocking UDP traffic on: " + node);
        node.getConnection().executeCommand(ClusterTestingAspect.getBlockUdpScript());
    }
    
    /**
     * Unblocks UDP traffic on the given node
     * @param node
     * @throws MocaException
     */
    public void unblockUdpOnNode(ClusterNode node) throws MocaException {
        writeLineWithDate("Unblocking UDP traffic on: " + node);
        node.getConnection().executeCommand(ClusterTestingAspect.getUnblockUdpScript());
    }
    
    /**
     * Validates the state of the cluster verifying the Infinispan cluster size and 
     * cluster URLs are correct on every node.
     * @param assertionMessage The assertion message to use on failure
     * @throws MocaException
     */
    public void validateCluster(String assertionMessage) throws MocaException {
        validateCluster(assertionMessage, null, true);
    }
    
    /**
     * Validates the state of the cluster verifying it matches the given {@link ClusterState}
     * @param assertionMessage The assertion message to use on failure
     * @param expectedState    The expected cluster state
     * @param checkForCacheInconistency 
     * @throws MocaException
     */
    public void validateCluster(String assertionMessage, ClusterState expectedState, boolean checkForCacheInconistency) throws MocaException {
        writeLineWithDate("Validating cluster state...");
        Collection<ClusterNode> nodes = _manager.getNodes();
        validateNodes(expectedState, checkForCacheInconistency, nodes.toArray(new ClusterNode[nodes.size()]));
    }
    
    /**
     * This is the same as {@link #validateCluster(String)} except it waits for the given
     * time to allow for flexibility if timing is involved with state changes of the cluster
     * @param assertionMessage
     * @param timeout
     * @param unit
     */
    public void waitForClusterState(String assertionMessage, long timeout, TimeUnit unit) {
        waitForClusterState(assertionMessage, null, timeout, unit);
    }
    
    /**
     * This is the same as {@link #validateCluster(String, ClusterState)} except it waits for
     * the given time to allow for flexibility if timing is involved with state changes of the cluster.
     * @param assertionMessage
     * @param expectedState
     * @param timeout
     * @param unit
     */
    public void waitForClusterState(String assertionMessage, ClusterState expectedState, long timeout, TimeUnit unit) {
        waitForNodesState(assertionMessage, expectedState, timeout, unit, 
            _manager.getNodes().toArray(new ClusterNode[_manager.getNodes().size()]));
    }
    
    /**
     * Validates the state of a subset of {@link ClusterNode}s, useful when validating that split brain occurred. The
     * collection of nodes specified is treated as its own cluster. This method additionally will wait for the given
     * amount of time trying to verify the state.
     * @param assertionMessage
     * @param expectedState
     * @param timeout
     * @param unit
     * @param nodes   The subset of nodes to validate the cluster state against
     */
    public void waitForNodesState(String assertionMessage, ClusterState expectedState, long timeout, TimeUnit unit, ClusterNode... nodes) {
        long endTime = System.currentTimeMillis() + unit.toMillis(timeout);
        boolean validated = false;
        while (!validated) {
            try {
                validateNodes(expectedState, false, nodes);
                validated = true;
            }
            catch (MocaException mocaException) {
                handleTimeout(assertionMessage, endTime, mocaException, nodes);
            }
            catch (AssertionError possibleError) {
                handleTimeout(assertionMessage, endTime, possibleError, nodes);
            }
        }
    }
    
    /**
     * Validates the collection of nodes, useful for testing split brain.
     * @param nodes
     * @throws MocaException
     */
    public void validateNodes(ClusterNode... nodes) throws MocaException {
        validateNodes(null, false, nodes);
    }
    
    public void validateNodes(ClusterState validator, boolean checkForCacheInconistency, ClusterNode... nodes) throws MocaException {
        writeLineWithDate("    Validating nodes across cluster...");
        
        // we don't always want to check this, because this will fail right away, and instead we would rather see
        // the logging that comes from validating the individual nodes first
        // the cache contents are checked anyway after any failure is detected in the handleTimeout method
        if (checkForCacheInconistency) {
            validateInfinispanCaches(nodes);
        }
        
        for (ClusterNode node : nodes) {
            validateNode(node, validator, nodes.length);
        }
        writeLineWithDate("    Done validating nodes across cluster.");
    }
    
    /**
     * Validate that the contents of known replicate caches are the same on all nodes.
     * @param nodes nodes to compare
     * @throws MocaException
     */
    public void validateInfinispanCaches(ClusterNode... nodes) throws MocaException {
        writeLineWithDate("    Validating cache contents are the same across cluster...");
        validateCacheContentsSameOnNodes(SINGLE_FORCED_ROLES_CACHE_NAME, CacheComparisonMode.KEYS, nodes);
        
        validateCacheContentsSameOnNodes(FORCED_ROLES_CACHE_NAME, CacheComparisonMode.MAPKEYTOSTRING, nodes);
        validateCacheContentsSameOnNodes(CURRENT_ROLES_CACHE_NAME, CacheComparisonMode.MAPKEYTOSTRING, nodes);
        validateCacheContentsSameOnNodes(JOB_CACHE_NAME, CacheComparisonMode.MAPKEYTOSTRING, nodes);
        validateCacheContentsSameOnNodes(TASK_CACHE_NAME, CacheComparisonMode.MAPKEYTOSTRING, nodes);
        validateCacheContentsSameOnNodes(CLUSTER_URL_CACHE_NAME, CacheComparisonMode.MAPKEYTOSTRING, nodes);
    }
    
    /**
     * Validate that every node has the same contents in a cache.
     * @param cacheName cache name
     * @param mode mode of what information to get from the cache
     * @param nodes nodes to check
     * @throws MocaException
     */
    @SuppressWarnings("unchecked")
    public void validateCacheContentsSameOnNodes(String cacheName, CacheComparisonMode mode, ClusterNode... nodes) throws MocaException {
        if (nodes.length < 2) return;
        
        StringBuilder errors = new StringBuilder();
        
        switch (mode) {
        //sets
        case KEYS:
        case VALUES:
        case KEYSTOSTRING:
        case KEYADDRESSTOSTRING:
            final List<Set<Object>> clusterCacheKeyOrValues = new ArrayList<Set<Object>>();
            for (ClusterNode  node : nodes) {
                clusterCacheKeyOrValues.add((Set<Object>) getCacheContents(cacheName, mode, node));
            }
            
            //TODO can we pick the "correct" contents better somehow instead of just choosing first in the list?
            final Set<Object> compareAgainstKeyOrValue = clusterCacheKeyOrValues.get(0);
            for (Set<Object> nodeCacheContents : clusterCacheKeyOrValues) {
                try {
                    assertSetContentsEqual("CACHE " + cacheName, compareAgainstKeyOrValue, nodeCacheContents);
                }
                catch (AssertionError e) {
                    final StringWriter sw = new StringWriter();
                    final PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    errors.append(e.getMessage() + LINE_SEPARATOR + sw.toString() + LINE_SEPARATOR);
                }
            }
            break;
        //maps
        case MAP:
        case MAPKEYTOSTRING:
            final List<Map<Object, Object>> clusterCacheMaps = new ArrayList<Map<Object, Object>>();
            for (ClusterNode  node : nodes) {
                clusterCacheMaps.add((Map<Object, Object>) getCacheContents(cacheName, mode, node));
            }
            
            //TODO can we pick the "correct" contents better somehow instead of just choosing first in the list?
            final Map<Object, Object> compareAgainstMap = clusterCacheMaps.get(0);
            for (Map<Object, Object> nodeCacheContents : clusterCacheMaps) {
                try {
                    assertMapContentsEqual(cacheName, compareAgainstMap, nodeCacheContents);
                }
                catch (AssertionError e) {
                    final StringWriter sw = new StringWriter();
                    final PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    errors.append(e.getMessage() + LINE_SEPARATOR + sw.toString() + LINE_SEPARATOR);
                }
            }
            break;
        default: throw new IllegalArgumentException("Unknown cache comparison mode " + mode);
        }
        
        if (errors.length() > 0) throw new AssertionError(errors.toString());
    }

    /**
     * Make sure that cache contents are the same. If not, generate a nice
     * message telling what went wrong.
     * @param cacheName
     * @param compareAgainstMap the map that serves as the "correct" map
     * @param nodeCacheContents the map that could be wrong that we are checking
     */
    void assertMapContentsEqual(String cacheName,
                                          final Map<Object, Object> compareAgainstMap,
                                          final Map<Object, Object> nodeCacheContents) {
        final StringBuilder err = new StringBuilder();
        if (!compareAgainstMap.equals(nodeCacheContents)) {
            err.append("DIFFERENT CACHE CONTENTS FOR CACHE " + cacheName + "! ");
            final MapDifference<Object, Object> d = Maps.difference(nodeCacheContents, compareAgainstMap);
            
            final Map<Object, Object> onlyLeft = d.entriesOnlyOnRight();
            if (onlyLeft.size() > 0) {
                err.append("MAP SHOULD HAVE INCLUDED: {" );
                for (Map.Entry<Object, Object> entry : onlyLeft.entrySet()) {
                    err.append("[" + entry.getKey() + "|" + entry.getValue() + "]");
                }
                err.append("} ");
            }
            
            final Map<Object, Object> onlyRight = d.entriesOnlyOnLeft();
            if (onlyRight.size() > 0) {
                err.append("MAP INCLUDED EXTRA: {");
                for (Map.Entry<Object, Object> entry : onlyRight.entrySet()) {
                    err.append("[" + entry.getKey() + "|" + entry.getValue() + "]");
                }
                err.append("} ");
            }
            
            final Map<Object, ValueDifference<Object>> diff = d.entriesDiffering();
            if (diff.size() > 0) {
                for (Map.Entry<Object, ValueDifference<Object>> e: diff.entrySet()) {
                    err.append("KEY {" + e.getKey() + "} HAD INCORRECT VALUE: {" + e.getValue().rightValue() + "}, expected {" + e.getValue().leftValue() + "} ");
                }
            }
            
            if (err.length() > 0) {
                writeLineWithDate(err.toString());
                throw new AssertionError(err.toString());
            }
        }
    }
    
    /**
     * Make sure that set contents are the same. If not, generate a nice
     * message telling what went wrong.
     * @param setName
     * @param compareAgainstSet the cache to compare against, the one that should have the right contents
     * @param nodeCacheContents the cache that could be different
     */
    void assertSetContentsEqual(String setName,
                                final Set<Object> compareAgainstSet,
                                final Set<Object> nodeCacheContents) {
        final StringBuilder err = new StringBuilder();
        if (!compareAgainstSet.equals(nodeCacheContents)) {
            err.append("DIFFERENT KEYS OR VALUES FOR CACHE " + setName + "!");
            final SetView<Object> onlyLeft = Sets.difference(compareAgainstSet, nodeCacheContents);
            final SetView<Object> onlyRight = Sets.difference(nodeCacheContents, compareAgainstSet);
            
            if (onlyLeft.size() > 0) {
                err.append(setName + " SHOULD HAVE INCLUDED: {" );
                for (Object entry : onlyLeft) {
                    err.append("[" + entry.toString() + "]");
                }
                err.append("}");
            }
            
            if (onlyRight.size() > 0) {
                err.append(setName + " INCLUDED EXTRA: {");
                for (Object entry : onlyRight) {
                    err.append("[" + entry.toString() + "]");
                }
                err.append("}");
            }
            
            if (err.length() > 0) {
                writeLineWithDate(err.toString());
                throw new AssertionError(err.toString());
            }
        }
    }
    
    /**
     * Get the node's role cache contents.
     * @param cacheName cache name
     * @param mode mode of what information to get from the cache
     * @param node cluster node
     * @return return the cache contents as a Map&lt;Object, Object&gt; if a map mode was requested
     * or Set&lt;Object&gt; for keys or values mode
     * @throws MocaException 
     */
    public Object getCacheContents(String cacheName, CacheComparisonMode mode, ClusterNode node) throws MocaException {
        final String cmd = "get cluster cache contents where " +
                "cacheName = \"" + cacheName + "\" and " +
                "mode = \"" + mode.getCommandArgumentName() + "\"";
        final MocaResults res = node.getConnection().executeCommand(cmd);
        final Object o;
        
        if (res == null || res.getRowCount() < 1) {
            fail("Failed to get cluster cache " + cacheName + " contents on node " + node);
        }
        
        res.next();
        o = res.getValue(RETURN_COLUMN);
        if (o == null) {
            fail("Failed to get cluster cache " + cacheName + " contents on node " + node 
                + " with " + res.getRowCount() + " rows.");
        }
        
        return o;
    }

    private void validateNode(ClusterNode node, ClusterState validator, int expectedSize) throws MocaException {
        writeLineWithDate("    Validating node: " + node);
        validateClusterUrl(node, expectedSize);
        if (validator != null) {
            validateRoles(node, validator);
        }
        writeLineWithDate("    Done validating node.");
        writeLine(System.lineSeparator());
    }
    
    private void validateRoles(ClusterNode node, ClusterState validator) throws MocaException {
        MocaResults res = printRoles(node);
        assertEquals(String.format("Node [%s] indicates the wrong number of nodes to roles", node), 
            validator.getNodeStates().size(), res.getRowCount());
        
        Map<Integer, Set<String>> portToRolesFound = new HashMap<Integer, Set<String>>(validator.getExpectedSize());
        // Splits the nodes to roles in node port --> roles (assumes different port numbers all on the same host)
        while (res.next()) {
            String nodeUrl = res.getString("node");
            Integer port = Integer.valueOf(nodeUrl.substring(nodeUrl.lastIndexOf(':') + 1));
            Set<String> foundRoles = new HashSet<String>();
            String roles = res.getString("roles");
            if (!roles.isEmpty()) {
                for (String role : res.getString("roles").trim().split(",")) {
                    foundRoles.add(role);
                }
            }
            portToRolesFound.put(port, foundRoles);
        }
        
        // Validates all the includes/exclude roles are correct for each node
        for (NodeState nodeState : validator.getNodeStates()) {
            Set<String> foundRoles = portToRolesFound.get(nodeState.getNodePort());
            for (String includedRole : nodeState.getIncludedRoles()) {
                assertTrue(String.format("Node [%s] should have included role [%s]",
                    nodeState.getNodePort(), includedRole),
                    foundRoles.contains(includedRole));
            }
            
            for (String excludedRole : nodeState.getExcludedRoles()) {
                assertFalse(String.format("Node [%s] should have excluded role [%s]",
                    nodeState.getNodePort(), excludedRole),
                    foundRoles.contains(excludedRole));
            }
        }
        
        // Validate floating roles, these roles can exist on one node but not any others
        for (String floatingRole : validator.getFloatingRoles()) {
            List<Integer> foundOnNodes = new ArrayList<Integer>();
            for (Map.Entry<Integer, Set<String>> entry : portToRolesFound.entrySet()) {
                if (entry.getValue().contains(floatingRole)) {
                    foundOnNodes.add(entry.getKey());
                }
            }
            
            if (foundOnNodes.size() == 0) {
                fail(String.format("Could not find the floating role [%s] on any node", floatingRole));
            }
            else if (foundOnNodes.size() > 1) {
                fail(String.format("Found the floating role [%s] on multiple nodes when it should only be on node, node ports: [%s]",
                    floatingRole, foundOnNodes));
            }
        }
    }
    
    private MocaResults printRoles(ClusterNode node) throws MocaException {
        MocaResults res = node.getConnection().executeCommand("list nodes to roles");
        writeLine(String.format("\tRoles cache for Node [%s]", node));
        while (res.next()) {
            writeLine(String.format("\t\tNode [%s] Roles [%s]", res.getString("node"), res.getString("roles")));
        }
        res.reset();
        return res;
    }
    
    /**
     * Prints the current state of the cluster
     * @throws MocaException
     */
    public void printClusterState() throws MocaException {
        writeLineWithDate("\nPrinting the state of the cluster");
        for (ClusterNode node : _manager.getNodes()) {
            printNodeState(node);
        }
    }
    
    /**
     * Prints the current state of the given node
     * @param node The node
     * @throws MocaException
     */
    public void printNodeState(ClusterNode node) throws MocaException {
        writeLineWithDate("    Printing the state of the node [" + node + "]");
        printInfinispanClusterSize(node);
        printClusterUrlsSize(node);
        printRoles(node);
    }
    
    /**
     * Testing method to stop and start a node verifying the state between steps
     * @param nodeId   The node ID to stop and start
     * @param afterStop The expected cluster state after stopping the node
     * @param afterRejoined The expected cluster state after starting the node again
     * @throws IOException If unable to start the node again
     */
    public void stopAndStartNodeWithValidation(NodeConfiguration nodeId, ClusterState afterStop,
                                               ClusterState afterRejoined) throws IOException {
        ArgCheck.notNull(afterStop);
        ArgCheck.notNull(afterRejoined);
        ClusterNode nodeToStop = _manager.getNode(nodeId);
        assertNotNull("Provided an invalid node ID that didn't exist", nodeToStop);
        _manager.stopAndRemoveNode(nodeToStop);
        waitForClusterState("Failed waiting for cluster state after removal of node ID: " + nodeId,
            afterStop,
            1, TimeUnit.MINUTES);
        
        _manager.startNewNode(nodeToStop.getConfiguration(), 1, TimeUnit.MINUTES);
        waitForClusterState("Failed waiting for cluster after restarting node ID: " + nodeId,
            afterRejoined,
            2, TimeUnit.MINUTES);
    }
    
    private void handleTimeout(String assertionMessage, long endTime, Throwable t, ClusterNode... nodes) {
        if (endTime < System.currentTimeMillis()) {
            final String msg = String.format(
                "Timeout occurred before state was met, Assertion message: [%s].%n Last Exception: [%s] ", 
                assertionMessage, t.getMessage());
            writeLineWithDate(
                "State validation failed and the timeout has expired, rethrowing the error, assertion message: " + assertionMessage);
            throw new AssertionError(msg + LINE_SEPARATOR + "Caused by: " + msg);
        }
        
        try {
            writeLineWithDate(String.format("State validation failed, retrying in 5 seconds, failure message [%s]", t.getMessage()));
            writeLineWithDate("Checking if the failure could be related to Infinispan cache inconistency...");
            try {
                validateInfinispanCaches(nodes);
            }
            catch (MocaException e) {
                writeLineWithDate(String.format("    Unable to check for Infinsipan cache inconsistency! [%s]", t.getMessage()));
            }
            catch (AssertionError inconsistency) {
                writeLineWithDate(String.format("    Infinispan cache inconsistency! [%s]", t.getMessage()));
            }
            Thread.sleep(5 * 1000);
        }
        catch (InterruptedException e) {
            throw new MocaInterruptedException(e);
        }
    }
    
    private int printInfinispanClusterSize(ClusterNode node) throws MocaException {
        MocaResults res = node.getConnection().executeCommand("get cluster member size");
        res.next();
        int size = res.getInt("result");
        writeLine(String.format("\tInfinispan cluster size: %d", size));
        return size;
    }
    
    private int printClusterUrlsSize(ClusterNode node) throws MocaException {
        MocaResults res = node.getConnection().executeCommand("get cluster urls catch(-1403)");
        int size = res.getRowCount();
        if (size == 0) {
            writeLine("\tCluster URLs was empty!");
        }
        else {
            StringBuilder urls = new StringBuilder();
            while (res.next()) {
                urls.append(res.getString("url")).append(", ");
            }
            
            writeLine(String.format("\tCluster URLs (%d) : %s",
                res.getRowCount(),
                urls.substring(0, urls.length() - 2)));
        }
        
        return size;
    }
    
    private void validateClusterUrl(ClusterNode node, int expectedSize) throws MocaException {
        final Set<Object> infinispanMembers = getInfinispanClusterMembers(node);
        final Set<Object> cacheMembers = getMocaClusterMembers(node);
        
        writeLine("        Expected size          : " + expectedSize);
        writeLine("        Infinispan cluster size: " + infinispanMembers.size() + ", " + infinispanMembers);
        writeLine("        MOCA cluster cache size: " + cacheMembers.size() + ", " + cacheMembers);
        
        assertEquals("WRONG INFINISPAN CLUSTER SIZE", expectedSize, infinispanMembers.size());
//        assertEquals("WRONG CLUSTER URLS ON NODE", expectedSize, cacheMembers.size());
        
        // check the contents instead of the count so that we have a nice message saying who should be there
        assertSetContentsEqual("CLUSTER MEMBER COMPARISON", infinispanMembers, cacheMembers);
    }
    
    /**
     * Get what the members according to Infinispan.
     * @param node
     * @return Set of strings corresponding to node names
     * @throws MocaException
     */
    public Set<Object> getInfinispanClusterMembers(ClusterNode node) throws MocaException {
        final MocaResults r = node.getConnection().executeCommand("[[\r\n" + 
            "  import javax.management.ObjectName;\r\n" + 
            "  import java.lang.management.ManagementFactory;\r\n" + 
            "  def objName = new ObjectName(\"com.sam.moca.cache.cluster:type=CacheManager,name=\\\"MOCA-Distributed Cache\\\",component=CacheManager\")\r\n" + 
            "  return ManagementFactory.getPlatformMBeanServer().getAttribute(objName, \"clusterMembers\")\r\n" + 
            "]]");
        
        if (r.getRowCount() != 1) {
            throw new AssertionError("Wrong number of rows returned: " + r.getRowCount());
        }
        r.next();
        
        final Set<Object> s = new HashSet<Object>();
        final String nodes = r.getString(RETURN_COLUMN);
        if (!nodes.startsWith("[") || !nodes.endsWith("]")) {
            throw new AssertionError("Invalid contents: " + nodes);
        }
        
        final String[] split = nodes.substring(1, nodes.length() - 1).split(",");
        for (String nodePart : split) {
            s.add(nodePart.trim());
        }
        return s;
    }
    
    /**
     * Get what MOCA thinks the members are according to the cache contents.
     * @param node
     * @return Set of strings corresponding to node names as addresses
     * @throws MocaException
     */
    @SuppressWarnings("unchecked")
    public Set<Object> getMocaClusterMembers(ClusterNode node) throws MocaException {
        return (Set<Object>) getCacheContents(CLUSTER_URL_CACHE_NAME, CacheComparisonMode.KEYADDRESSTOSTRING, node);
    }
    
    public void writeLineWithDate(String line) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        writeLine(line + " at (" + dateFormat.format(new Date()) + ")");
    }
    
    public void writeLine(String line) {
        _out.println(line);
        // Uncomment to force writing to stdout for debugging
        //System.out.println(line);
    }
    
    /**
     * Get a value from a cache on a node.
     * @param node
     * @param cacheName
     * @param key
     * @return value, or null if it doesn't exist
     * @throws MocaException
     */
    public String getFromCache(ClusterNode node, String cacheName, String key) throws MocaException {
        String cmd = GET_FROM_CACHE_COMMAND.replace(KEY_MARK, key).replace(CACHENAME_MARK, cacheName);
        MocaResults res = node.getConnection().executeCommand(cmd);
        res.next();
        return res.getString(RETURN_COLUMN); //TODO this should return Object
    }

    /**
     * Insert a key/value pair into a cache from a node.
     * @param node
     * @param cacheName
     * @param key
     * @param value
     * @throws MocaException
     */
    public void insertIntoCache(ClusterNode node, String cacheName, String key, String value) throws MocaException {
        String cmd = INSERT_INTO_CACHE_COMMAND.replace(KEY_MARK, key)
                .replace(VALUE_MARK, value)
                .replace(CACHENAME_MARK, cacheName);
        node.getConnection().executeCommand(cmd);
    }
    
    /**
     * Remove a key/value pair into a cache from a node.
     * @param node
     * @param cacheName
     * @param key
     * @throws MocaException
     */
    public void removeFromCache(ClusterNode node, String cacheName, String key) throws MocaException {
        String cmd = REMOVE_FROM_CACHE_COMMAND.replace(KEY_MARK, key).replace(CACHENAME_MARK, cacheName);
        node.getConnection().executeCommand(cmd);
    }
    
    /**
     * Clear a cache from a node.
     * @param node
     * @param cacheName
     * @throws MocaException
     */
    public void clearFromCache(ClusterNode node, String cacheName) throws MocaException {
        String cmd = CLEAR_CACHE_COMMAND.replace(CACHENAME_MARK, cacheName);
        node.getConnection().executeCommand(cmd);
    }
    
    /**
     * Check that a key exists in a cache on a particular node.
     * If value is given, check that the key exists AND it has the correct value,
     * otherwise just check that it exists.
     * @param node
     * @param cacheName
     * @param key
     * @param value optional value to compare against key
     * @throws MocaException
     */
    public void assertKeyExists(ClusterNode node, String cacheName, String key, String value) throws MocaException {
        final Object res = getFromCache(node, cacheName, key);
        assertNotNull(res);
        if (res != null) {
            assertEquals(value, res);
        }
    }
    
    /**
     * Assert that a key does not exist in the cache.
     * @param node
     * @param cacheName
     * @param key
     * @throws MocaException
     */
    public void assertKeyDoesNotExist(ClusterNode node, String cacheName, String key) throws MocaException {
        assertNull(getFromCache(node, cacheName, key));
    }

    private static final String RETURN_COLUMN = "result";
    private static final String KEY_MARK = "@KEY";
    private static final String VALUE_MARK = "@VALUE";
    private static final String CACHENAME_MARK = "@CACHENAME";
    private static final String INSERT_INTO_CACHE_COMMAND = "[[\r\n" +
            "import org.infinispan.manager.EmbeddedCacheManager;\r\n" + 
            "import com.sam.moca.cache.infinispan.InfinispanCacheProvider;\r\n" + 
            "import com.sam.moca.server.ServerUtils;\r\n" + 
            "import org.infinispan.Cache;\r\n" + 
            "Cache<Object, Object> cache = InfinispanCacheProvider.getInfinispanCacheManager(ServerUtils.globalContext()).getCache(\"" + CACHENAME_MARK + "\");\r\n" + 
            "cache.put(\"" + KEY_MARK + "\", \"" + VALUE_MARK + "\");\r\n" +
        "]]";
    private static final String REMOVE_FROM_CACHE_COMMAND = "[[\r\n" +
            "import org.infinispan.manager.EmbeddedCacheManager;\r\n" + 
            "import com.sam.moca.cache.infinispan.InfinispanCacheProvider;\r\n" + 
            "import com.sam.moca.server.ServerUtils;\r\n" + 
            "import org.infinispan.Cache;\r\n" + 
            "Cache<Object, Object> cache = InfinispanCacheProvider.getInfinispanCacheManager(ServerUtils.globalContext()).getCache(\"" + CACHENAME_MARK + "\");\r\n" + 
            "cache.remove(\"" + KEY_MARK + "\");\r\n" +
        "]]";
    private static final String GET_FROM_CACHE_COMMAND = "[[\r\n" +
            "import org.infinispan.manager.EmbeddedCacheManager;\r\n" + 
            "import com.sam.moca.cache.infinispan.InfinispanCacheProvider;\r\n" + 
            "import com.sam.moca.server.ServerUtils;\r\n" + 
            "import org.infinispan.Cache;\r\n" +
            "final String RETURN_COLUMN = \"result\";\r\n" + 
            "final Cache<Object, Object> cache = InfinispanCacheProvider.getInfinispanCacheManager(ServerUtils.globalContext()).getCache(\"" + CACHENAME_MARK + "\");\r\n" + 
            "EditableResults r = new SimpleResults();\r\n" + 
            "r.addColumn(RETURN_COLUMN, MocaType.STRING);\r\n" + //TODO this should be Object
            "r.addRow();\r\n" + 
            "r.setValue(RETURN_COLUMN, cache.get(\"" + KEY_MARK + "\"));\r\n" +
            "r;\r\n" + 
        "]]";
    private static final String CLEAR_CACHE_COMMAND = "[[\r\n" +
            "import org.infinispan.manager.EmbeddedCacheManager;\r\n" + 
            "import com.sam.moca.cache.infinispan.InfinispanCacheProvider;\r\n" + 
            "import com.sam.moca.server.ServerUtils;\r\n" + 
            "import java.util.HashMap;\r\n" + 
            "import org.infinispan.Cache;\r\n" +
            "Cache<Object, Object> cache = InfinispanCacheProvider.getInfinispanCacheManager(ServerUtils.globalContext()).getCache(\"" + CACHENAME_MARK + "\");\r\n" + 
            "cache.clear();\r\n" +
        "]]";
    
    private final PrintStream _out;
    private final ClusterManager _manager;
    
    private enum CacheComparisonMode {
        /**
         * Returns the keys as a Set.
         */
        KEYS("keys"),
        
        /**
         * Returns the values as a Set.
         */
        VALUES("values"),
        
        /**
         * Returns the toString() of each key in a Set.
         */
        KEYSTOSTRING("keys-tostring"),
        
        /**
         * Returns the getAddress().toString() of each key in a Set. Should only be used for
         * cluster node cache.
         */
        KEYADDRESSTOSTRING("key-address-tostring"),
        
        /**
         * Return the cache as a Map.
         */
        MAP("map"),
        
        /**
         * Returns the cache as a Map, but with all the entries converted to strings.
         * This is useful when classes are not Serializable.
         */
        MAPKEYTOSTRING("map-key-tostring");
        
        private CacheComparisonMode(String argName) {
            this._argName = argName;
        }
        
        /**
         * Get the name for this mode as it should be passed to the get cluster cache contents command
         * @return
         */
        public String getCommandArgumentName() {
            return _argName;
        }
        
        private final String _argName;
    };
}
