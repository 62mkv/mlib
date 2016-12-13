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

package com.redprairie.moca.mad;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.mad.reporters.DatabaseConnectionDumper;
import com.redprairie.moca.mad.reporters.NativeProcessReporter;
import com.redprairie.moca.mad.reporters.SessionReporter;
import com.redprairie.moca.mad.reporters.UserReporter;
import com.redprairie.moca.util.MocaUtils;

/**
 * A MBean used to gather monitoring information.
 * 
 * Copyright (c) 2012 Sam Corporation All Rights Reserved
 * 
 * @author rrupp
 */
public class MonitoringInformation implements MonitoringMBean {
    public static final String MBEAN_TYPE = "monitoring";
    public static final String OBJECT_NAME = String.format("%s:type=%s",
        MonitoringUtils.MOCA_GROUP_NAME, MBEAN_TYPE);
    
    /**
     * Gets singleton instance
     * @return The MonitoringInformation instance
     */
    protected static MonitoringInformation getInstance() {
        return INSTANCE;
    }

    /**
     * Registers the monitoring Mbean
     */
    public synchronized static void registerMBean() {
        if (_initialized) return;
        
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName monitoringName = new ObjectName(OBJECT_NAME);
            server
                .registerMBean(INSTANCE, monitoringName);
        }
        catch (InstanceAlreadyExistsException e) {
            _log.warn(MocaUtils.concat("Attempt to ", "register Monitoring bean ",
                "failed, because it was already registered."), e);
        }
        catch (MBeanRegistrationException e) {
            _log.warn(MocaUtils.concat("Attempt to ", "register Monitoring bean ",
                "failed, because it threw an exception."), e);
        }
        catch (NotCompliantMBeanException e) {
            _log.warn(MocaUtils.concat("Attempt to ", "register Monitoring bean ",
                "failed, because it wasn't compliant."), e);
        }
        catch (MalformedObjectNameException e) {
            _log.warn(MocaUtils.concat("Attempt to ", "register Monitoring bean ",
                "failed, because the name was malformed."), e);
        }
        _initialized = true;
    }

    // @see com.redprairie.moca.monitoring.MonitoringMBean#dumpSessionInformation()
    @Override
    public String dumpSessionInformation() {
        return SessionReporter.generateSessionReport();
    }
    
    // @see com.redprairie.moca.monitoring.MonitoringMBean#dumpDatabaseConnectionInformation()
    @Override
    public String dumpDatabaseConnectionInformation() {
        return DatabaseConnectionDumper.dumpDatabaseConnectionInformation();
    }

    // @see com.redprairie.moca.monitoring.MonitoringMBean#dumpConnectedUserInformation()
    @Override
    public String dumpConnectedUserInformation() {
        return UserReporter.generateUserReport();
    }
    
    // @see com.redprairie.moca.mad.MonitoringMBean#dumpNativeProcessInformation()  
    @Override
    public String dumpNativeProcessInformation() {
        return NativeProcessReporter.generateNativeProcessReport();
    }

    private static boolean _initialized;

    private static final Logger _log = LogManager.getLogger(MonitoringInformation.class);

    private static final MonitoringInformation INSTANCE = new MonitoringInformation();

}
