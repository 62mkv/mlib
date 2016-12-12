/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.mad.zabbix;


import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.mad.zabbix.MocaZabbixConfiguration;
import com.redprairie.moca.server.exec.SystemContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test Unit for Zabbix Configuration
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_MocaZabbixConfiguration {
    
    @BeforeClass
    public static void setUpClass() {
        TEST_HOST_GROUPS.add(TEST_HOST_GROUP);
    }
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testNotConfigured() {
        mockRegistryConfig(null, 
                           MocaRegistry.REGKEY_ZABBIX_TRAPPER_PORT_DEFAULT,
                           MocaRegistry.REGKEY_ZABBIX_LOCAL_AGENT_PORT_DEFAULT,
                           null,
                           TEST_INSTANCE_NAME,
                           MocaRegistry.REGKEY_ZABBIX_API_USER_DEFAULT,
                           MocaRegistry.REGKEY_ZABBIX_API_PASSWORD_DEFAULT,
                           "RP",
                           MocaRegistry.REGKEY_SERVER_PORT_DEFAULT);
        
        MocaZabbixConfiguration config = new MocaZabbixConfiguration(_mockSystemContext);
        // Should be flagged not installed due to missing server IP as  but defaults should exist
        // Cluster Name should be used for the Host Group because Host Name wasn't configured in the registry
        List<String> hostGroups = new ArrayList<String>(1);
        hostGroups.add("RP");
        validateConfiguration(config, false, null, 10051, 10050, hostGroups, TEST_INSTANCE_NAME, "admin", "zabbix", 4500);
    }
    
    @Test
    public void testConfigureEveryKey() {       
        mockRegistryConfig("192.168.1.2", 
            "1111",
            "9999",
            TEST_HOST_GROUP,
            TEST_INSTANCE_NAME,
            "testuser",
            "testpassword",
            "RP",
            "4600");
        
        MocaZabbixConfiguration config = new MocaZabbixConfiguration(_mockSystemContext);
        
        // Host group registry configuration should take precedence over the cluster name
        validateConfiguration(config, true, "192.168.1.2", 1111, 9999,
            TEST_HOST_GROUPS, TEST_INSTANCE_NAME, "testuser", "testpassword", 4600);
    }

    @Test
    public void testEnvNameMissing() {
        mockRegistryConfig("192.168.1.2", 
                            MocaRegistry.REGKEY_ZABBIX_TRAPPER_PORT_DEFAULT,
                            MocaRegistry.REGKEY_ZABBIX_LOCAL_AGENT_PORT_DEFAULT,
                            TEST_HOST_GROUP,
                            null,
                            MocaRegistry.REGKEY_ZABBIX_API_USER_DEFAULT,
                            MocaRegistry.REGKEY_ZABBIX_API_PASSWORD_DEFAULT,
                            "RP",
                            "4600");
        
        MocaZabbixConfiguration config = new MocaZabbixConfiguration(_mockSystemContext);
        // If the environment name is missing Zabbix integration is turned off
        validateConfiguration(config, false, "192.168.1.2", 10051, 10050,
            TEST_HOST_GROUPS, null, "admin", "zabbix", 4600);
    }
    
    @Test
    public void testInvalidPortConfigurations() {
        
        // Bad trapper port
        mockRegistryConfig("192.168.1.2", 
                            "badport",
                            MocaRegistry.REGKEY_ZABBIX_LOCAL_AGENT_PORT_DEFAULT,
                            TEST_HOST_GROUP,
                            TEST_INSTANCE_NAME,
                            MocaRegistry.REGKEY_ZABBIX_API_USER_DEFAULT,
                            MocaRegistry.REGKEY_ZABBIX_API_PASSWORD_DEFAULT,
                            "RP",
                            "4600");

        
        // Expecting an exception to be thrown here, when ports
        // are configured improperly the exception should bubble up so that
        // the issue can be fixed immediately rather than hiding the error.
        try {
            new MocaZabbixConfiguration(_mockSystemContext);
            fail("An exception should have been thrown for an improperly configured trapper port");
        }
        catch (NumberFormatException expected) {}
        
        // Bad agent port
        mockRegistryConfig("192.168.1.2", 
                            MocaRegistry.REGKEY_ZABBIX_TRAPPER_PORT_DEFAULT,
                            "badport",
                            TEST_HOST_GROUP,
                            TEST_INSTANCE_NAME,
                            MocaRegistry.REGKEY_ZABBIX_API_USER_DEFAULT,
                            MocaRegistry.REGKEY_ZABBIX_API_PASSWORD_DEFAULT,
                            "RP",
                            "4600");
        
        try {
            new MocaZabbixConfiguration(_mockSystemContext);
            fail("An exception should have been thrown for an improperly configured agent port");
        }
        catch (NumberFormatException expected) {}
        
        // Bad Moca port
        mockRegistryConfig("192.168.1.2", 
            MocaRegistry.REGKEY_ZABBIX_TRAPPER_PORT_DEFAULT,
            MocaRegistry.REGKEY_ZABBIX_LOCAL_AGENT_PORT_DEFAULT,
            TEST_HOST_GROUP,
            TEST_INSTANCE_NAME,
            MocaRegistry.REGKEY_ZABBIX_API_USER_DEFAULT,
            MocaRegistry.REGKEY_ZABBIX_API_PASSWORD_DEFAULT,
            "RP",
            "badport");
        
        try {
            new MocaZabbixConfiguration(_mockSystemContext);
            fail("An exception should have been thrown for an improperly configured Moca port");
        }
        catch (NumberFormatException expected) {}
    }
    
    @Test
    public void testMultipleHostGroups() {
        String hostGroupsConfig = TEST_HOST_GROUP + ",Another Group,One More";
        mockRegistryConfig("192.168.1.2", 
        MocaRegistry.REGKEY_ZABBIX_TRAPPER_PORT_DEFAULT,
        MocaRegistry.REGKEY_ZABBIX_LOCAL_AGENT_PORT_DEFAULT,
        hostGroupsConfig,
        TEST_INSTANCE_NAME,
        MocaRegistry.REGKEY_ZABBIX_API_USER_DEFAULT,
        MocaRegistry.REGKEY_ZABBIX_API_PASSWORD_DEFAULT,
        "RP",
        "4600");

        List<String> hostGroupsResult = new ArrayList<String>(3);
        hostGroupsResult.add(TEST_HOST_GROUP);
        hostGroupsResult.add("Another Group");
        hostGroupsResult.add("One More");
        MocaZabbixConfiguration config = new MocaZabbixConfiguration(_mockSystemContext);
        // If the environment name is missing Zabbix integration is turned off
        validateConfiguration(config, true, "192.168.1.2", 10051, 10050,
            hostGroupsResult, TEST_INSTANCE_NAME, "admin", "zabbix", 4600);
    }
    
    @Test
    public void testHostGroupTrim() {
        // Test host groups entry with whitespace around them
        String hostGroupsConfig = TEST_HOST_GROUP + ",  Another Group , One More  ";
        mockRegistryConfig("192.168.1.2", 
        MocaRegistry.REGKEY_ZABBIX_TRAPPER_PORT_DEFAULT,
        MocaRegistry.REGKEY_ZABBIX_LOCAL_AGENT_PORT_DEFAULT,
        hostGroupsConfig,
        TEST_INSTANCE_NAME,
        MocaRegistry.REGKEY_ZABBIX_API_USER_DEFAULT,
        MocaRegistry.REGKEY_ZABBIX_API_PASSWORD_DEFAULT,
        "RP",
        "4600");

        List<String> hostGroupsResult = new ArrayList<String>(3);
        hostGroupsResult.add(TEST_HOST_GROUP);
        hostGroupsResult.add("Another Group");
        hostGroupsResult.add("One More");
        MocaZabbixConfiguration config = new MocaZabbixConfiguration(_mockSystemContext);
        // If the environment name is missing Zabbix integration is turned off
        validateConfiguration(config, true, "192.168.1.2", 10051, 10050,
            hostGroupsResult, TEST_INSTANCE_NAME, "admin", "zabbix", 4600);
    }
    
    @Test
    public void testDefaultHostGroup() {
        mockRegistryConfig("192.168.1.2", 
            "1111",
            "9999",
            null,
            TEST_INSTANCE_NAME,
            "testuser",
            "testpassword",
            null,
            "4600");
        
        MocaZabbixConfiguration config = new MocaZabbixConfiguration(_mockSystemContext);
        
        // Host group registry configuration should take precedence over the cluster name
        validateConfiguration(config, true, "192.168.1.2", 1111, 9999,
            null, TEST_INSTANCE_NAME, "testuser", "testpassword", 4600);
    }
    
    // Validate the created configuration against expected values
    private void validateConfiguration(MocaZabbixConfiguration config,
                                       boolean installed, String ip, Integer trapperPort, Integer agentPort,
                                       List<String> hostGroups, String hostName, String apiUser,
                                       String apiPassword, Integer mocaPort) {
        if (hostName != null) hostName = hostName.toUpperCase();
        
        assertEquals(installed, config.isEnabled());
        assertEquals(ip, config.getServerConnection());
        assertEquals(trapperPort, config.getTrapperPort());
        assertEquals(agentPort, config.getAgentPort());
        if (hostName != null) {
            assertEquals(true, config.getApplicationHostName().endsWith("MOCA-"+hostName));
        }
        else {
            assertEquals(null, config.getApplicationHostName());
        }
        assertEquals(apiUser, config.getApiUsername());
        assertEquals(apiPassword, config.getApiPassword());
        assertEquals(hostGroups, config.getHostGroups());
        assertEquals(mocaPort, config.getJmxPort());
    }  
    
    // Mocks out the expected registry configurations
    private void mockRegistryConfig(String ip, String trapperPort, String agentPort,
                                  String hostGroups, String hostName, String apiUser,
                                  String apiPassword, String clusterName, String mocaPort) {
        // Server ip
        Mockito.when(_mockSystemContext.getConfigurationElement(
            MocaRegistry.REGKEY_ZABBIX_IP)).thenReturn(ip);
        
        // Trapper port
        Mockito.when(_mockSystemContext.getConfigurationElement(
            MocaRegistry.REGKEY_ZABBIX_TRAPPER_PORT,
            MocaRegistry.REGKEY_ZABBIX_TRAPPER_PORT_DEFAULT)).thenReturn(trapperPort);
        
        // Host group
        
        Mockito.when(_mockSystemContext.getConfigurationElement(
            MocaRegistry.REGKEY_ZABBIX_HOSTS_GROUP)).thenReturn(hostGroups);
        
        // Cluster name default
        Mockito.when(_mockSystemContext.getConfigurationElement(
            MocaRegistry.REGKEY_CLUSTER_NAME)).thenReturn(clusterName);
    
        // Host name
        Mockito.when(_mockSystemContext.getVariable("MOCA_ENVNAME")).thenReturn(hostName);
        
        // Agent Port
        Mockito.when(_mockSystemContext.getConfigurationElement(
            MocaRegistry.REGKEY_ZABBIX_LOCAL_AGENT_PORT,
            MocaRegistry.REGKEY_ZABBIX_LOCAL_AGENT_PORT_DEFAULT)).thenReturn(agentPort);
    
        // API user/pass
        Mockito.when(_mockSystemContext.getConfigurationElement(
            MocaRegistry.REGKEY_ZABBIX_API_USER,
            MocaRegistry.REGKEY_ZABBIX_API_USER_DEFAULT)).thenReturn(apiUser);
        Mockito.when(_mockSystemContext.getConfigurationElement(
            MocaRegistry.REGKEY_ZABBIX_API_PASSWORD,
            MocaRegistry.REGKEY_ZABBIX_API_PASSWORD_DEFAULT)).thenReturn(apiPassword);
    
        // Moca port (used for JMX/Jolokia)
        Mockito.when(_mockSystemContext.getConfigurationElement(
            MocaRegistry.REGKEY_SERVER_PORT,
            MocaRegistry.REGKEY_SERVER_PORT_DEFAULT)).thenReturn(mocaPort);
        
        // Sync enabled
        Mockito.when(_mockSystemContext.getConfigurationElement(
            MocaRegistry.REGKEY_ZABBIX_ENABLE_MACHINE_SYNC,
            MocaRegistry.REGKEY_ZABBIX_ENABLE_MACHINE_SYNC_DEFAULT)).thenReturn("true");
        
        Mockito.when(_mockSystemContext.getConfigurationElement(
            MocaRegistry.REGKEY_ZABBIX_ENABLE_APPLICATION_SYNC,
            MocaRegistry.REGKEY_ZABBIX_ENABLE_APPLICATION_SYNC_DEFAULT)).thenReturn("true");
        
        Mockito.when(_mockSystemContext.getConfigurationElement(
                MocaRegistry.REGKEY_ZABBIX_USE_DNS,
                "false")).thenReturn("true");
    }
    
    @Mock
    private SystemContext _mockSystemContext;
    
    private static final String TEST_HOST_GROUP = "My Host Group";
    private static final List<String> TEST_HOST_GROUPS = new ArrayList<String>();
    private static final String TEST_INSTANCE_NAME = "Trunk";
    
}
