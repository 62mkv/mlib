package com.redprairie.moca.web.console;

import java.util.Collection;
import java.util.Set;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.cache.infinispan.InfinispanCacheProvider;
import com.redprairie.moca.client.MocaConnection;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.server.ClientTestUtils;
import com.redprairie.moca.server.InstanceUrl;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.util.AbstractMocaJunit4TestCase;
import org.infinispan.Cache;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * More in-depth MocaClusterAdministration tests.
 * Copyright (c) 2015 JDA Software All Rights Reserved
 *
 * @author mdobrinin
 */
public class TI_MocaClusterAdministration extends AbstractMocaJunit4TestCase {

    /**
     * Test that when were not in a cluster then we don't cause a deadlock because
     * the RPC latch was never opened. Any command that uses MocaClusterAdministration would block.
     * @throws MocaException
     */
    @Test
    public void testLock() throws MocaException {
        final MocaConnection conn = ClientTestUtils.newConnection();
        try {
            conn.executeCommand("remove job where job_id = 'CNT-TAM7' catch (@?)");
            conn.executeCommand("add job where enabled='1' " +
                    "and overlap='0' " +
                    "and timer='10' " +
                    "and name='CYCLE COUNT TEMPLATE' " +
                    "and command='process cycle count template request' " +
                    "and job_id='CNT-TAM7'");
        }
        finally {
            conn.executeCommand("remove job where job_id = 'CNT-TAM7'");
        }
    }
}
