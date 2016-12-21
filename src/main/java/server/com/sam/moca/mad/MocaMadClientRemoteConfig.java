/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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
package com.sam.moca.mad;

import com.redprairie.mad.client.MadClientRemoteConfig;
import com.redprairie.mad.protocol.CustomMessageDispatcher;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaRegistry;
import com.sam.moca.mad.custom.ExecutorStatusHandler;
import com.sam.moca.util.MocaUtils;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to make Mad operate remotely.
 * 
 * This would be used by a MOCA process based task to report its 
 * probing information to MOCA.
 * 
 * @author klucas
 */
@Configuration
public class MocaMadClientRemoteConfig extends MadClientRemoteConfig {
    // @see com.redprairie.mad.client.MadClientRemoteConfig#port()
    @Override
    public Integer port() {
        int port = 0;

        // Get the port from the environment 
        String portString = System.getenv("MAD_PORT");
        
        try {
            port = Integer.parseInt(portString);
        } 
        catch (NumberFormatException nfe) {
            _logger.warn("MAD_PORT not set to a number.");
            throw nfe;
        }

        return port;
    }
    
    // @see com.redprairie.mad.client.MadClientRemoteConfig#jmxUrl()
    @Override
    @Bean
    public String jmxUrl() {
        MocaContext context = MocaUtils.currentContext();
        
        String rmiPortString = context.getRegistryValue(MocaRegistry.REGKEY_SERVER_RMI_PORT);
        
        if (rmiPortString == null) {
            rmiPortString = MocaRegistry.REGKEY_SERVER_RMI_PORT_DEFAULT;
        }

        int rmiPort = Integer.parseInt(rmiPortString);

        String taskId = System.getenv("MOCA_TASK_ID");

        String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:" + rmiPortString + "/tasks/" + taskId;

        try {
            JMXServiceURL url = new JMXServiceURL(jmxUrl);

            JMXConnectorServer connectorServer = JMXConnectorServerFactory
                .newJMXConnectorServer(url, null,
                    ManagementFactory.getPlatformMBeanServer());
            connectorServer.start();

            Runtime.getRuntime().addShutdownHook(
                new JmxShutdown(connectorServer, rmiPort, taskId));
        }
        // Monitoring and diagnostics can still work but without Gauges
        catch (IOException ioException) {
            _logger.warn("Cannot set up JMX url.  Gauges will not work.");
        }

        return jmxUrl;
    }
    
    // @see com.redprairie.mad.client.MadClientRemoteConfig#customMessageDispatcher()
    @Bean
    @Override
    public CustomMessageDispatcher customMessageDispatcher() {
        CustomMessageDispatcher dispatch = super.customMessageDispatcher();
        
        // Register custom handler
        dispatch.registerCustomHandler(ExecutorStatusHandler.CUSTOM_MESSAGE_TYPE, 
                                       executorStatusHandler());
        
        return dispatch;
    }
    
    @Bean
    public ExecutorStatusHandler executorStatusHandler() {
        return new ExecutorStatusHandler();
    }
    
    private static class JmxShutdown extends Thread {
        public JmxShutdown(JMXConnectorServer server, int rmiPort, String taskId) {
            _server = server;
            _rmiPort = rmiPort;
            _taskId = taskId;
        }

        @Override
        public void run() {
            try {
                _server.stop();

                LocateRegistry.getRegistry(_rmiPort).unbind("tasks/" + _taskId);
            }
            // Just log the exceptions because this is a shutdown hook
            catch (NotBoundException notBound) {
                _logger.debug("Cannot unregister JMX url: " + notBound.toString());
            }
            catch (IOException ioException) {
                _logger.debug("Cannot unregister JMX url: " + ioException.toString());
            }
        }

        private JMXConnectorServer _server;
        private int _rmiPort;
        private String _taskId;
        
    }
    
    private final static Logger _logger = Logger.getLogger(MocaMadClientRemoteConfig.class);
}
