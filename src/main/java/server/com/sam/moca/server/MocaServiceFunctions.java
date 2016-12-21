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

package com.sam.moca.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.sam.moca.MocaRegistry;
import com.sam.moca.MocaServerAdministrationMBean;
import com.sam.moca.server.exec.SystemContext;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class MocaServiceFunctions {
    
    public static void main(String[] args) throws IOException, 
        MalformedObjectNameException {
        if (args.length != 1) {
            System.err.println("There were an invalid number of arguments " +
            		"provided.  Only 1 is allowed");
            System.exit(1);
        }
        
        String type = args[0];
        
        if (type.equalsIgnoreCase("restart")) {
            restart(null);
        }
        else if (type.equalsIgnoreCase("stop")) {
            stop(null);
        }
    }

    private static void restart(String processId) throws IOException, 
        MalformedObjectNameException {
        
        MocaServerAdministrationMBean bean = getMBean(processId,
                new MocaServerMain.Authenticated(_RESTARTUSER));
        bean.restart(true);
    }
    
    private static void stop(String processId) throws IOException, 
        MalformedObjectNameException {
        
        MocaServerAdministrationMBean bean = getMBean(processId, 
                new MocaServerMain.Authenticated(_SHUTDOWNUSER));
        bean.stop();
    }
    
    private static MocaServerAdministrationMBean getMBean(String processId,
            Object authenticated) 
        throws IOException, MalformedObjectNameException {
        SystemContext systemContext = ServerUtils.globalContext();
        JMXServiceURL url = new JMXServiceURL(
                "service:jmx:rmi:///jndi/rmi://localhost:"
                        + systemContext.getConfigurationElement(
                                MocaRegistry.REGKEY_SERVER_RMI_PORT, 
                                MocaRegistry.REGKEY_SERVER_RMI_PORT_DEFAULT) 
                                + "/admin");
        
        Map<String, Object> env = new HashMap<String, Object>();
        env.put(JMXConnector.CREDENTIALS, authenticated);
        
        JMXConnector jmxc = JMXConnectorFactory.newJMXConnector(url, env);
        try {
            jmxc.connect(env);
        }
        catch (IOException io) {
            // The server may not have completed startup yet.  Wait 5
            // seconds and try again before actually failing.
            try {
                Thread.sleep(5000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            jmxc.connect(env);
        }
        
        MBeanServerConnection sc = jmxc.getMBeanServerConnection();
        
        ObjectName objName = new ObjectName("com.sam.moca:type=server");
        
        return JMX.newMXBeanProxy(sc, objName, 
                MocaServerAdministrationMBean.class);
    }
    
    final static String _SHUTDOWNUSER = "SHUTDOWN";
    final static String _RESTARTUSER = "RESTART";
}
