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

package com.redprairie.moca.mad.zabbix;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.redprairie.mad.zabbix.ZabbixConfiguration;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.SpringTools;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.util.AppUtils;

/**
 * Holds configuration information derived from the MOCA
 * Registry regarding the associated Zabbix monitoring server.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class MocaZabbixConfiguration implements ZabbixConfiguration {
    
    /**
     * Gathers the Zabbix Configuration. Package private so that
     * it is only utilized internally. Access as a singleton through
     * ZabbixUtils.getConfiguration()
     */
    public MocaZabbixConfiguration(SystemContext context) {
        
        // Server information
        _serverIp = context.getConfigurationElement(MocaRegistry.REGKEY_ZABBIX_IP);
        if (_serverIp != null && !_serverIp.isEmpty()) {
            _isEnabled = true;
        }
        
        String tempTrapperPort = context.getConfigurationElement(MocaRegistry.REGKEY_ZABBIX_TRAPPER_PORT,
                                                                 MocaRegistry.REGKEY_ZABBIX_TRAPPER_PORT_DEFAULT);
        try {
            _trapperPort = Integer.parseInt(tempTrapperPort);
        }
        catch (NumberFormatException e) {
            _logger.error(String.format("Invalid configuration - %s - for the Zabbix trapper port, the configuration must be numeric!", tempTrapperPort), e);
            throw e;
        }
        
        // Host information
        // Host group precedence:
        // 1) Registry entry
        // 2) Cluster name
        // 3) Empty - the MAD framework will default this accordingly
        String hostGroupsEntry = context.getConfigurationElement(MocaRegistry.REGKEY_ZABBIX_HOSTS_GROUP);
        if (hostGroupsEntry != null && !hostGroupsEntry.isEmpty()) {
            _hostGroups = stringToList(hostGroupsEntry);
        }
        else {
            String clusterName = context.getConfigurationElement(MocaRegistry.REGKEY_CLUSTER_NAME);
            if (clusterName != null ) {
                _hostGroups = new ArrayList<String>(1);
                _hostGroups.add(clusterName);
            }
        }
        
        // Machine hostname format: <DNS>
        // Application Host name format: <DNS>-MOCA-<MOCA_ENVNAME>
        // MOCA_ENVNAME should be set through service manager or rpset
        // If running through Eclipse this may not be set so it would have to be configured
        // as an environment variable. If it's missing Zabbix integration is disabled.
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            String hostBaseName = hostname + "-MOCA-";
            String instanceName = context.getVariable("MOCA_ENVNAME");
            if (instanceName == null || instanceName.isEmpty()) {
                _logger.warn("Environment variable MOCA_ENVNAME is not set which is required for Zabbix, disabling Zabbix integration.");
                _isEnabled = false;
            }
            else {
                _applicationHostName = hostBaseName + instanceName;
                // Use uppercase for hostnames
                _applicationHostName = _applicationHostName.toUpperCase();
            }
            _machineHostName = hostname.toUpperCase();
        }
        catch (UnknownHostException e) {
            _logger.warn("Unable to resolve host DNS name for Zabbix configuration, disabiling Zabbix integration", e);
            _isEnabled = false;
        }
        
        // Local agent port
        String tmpAgentPort = context.getConfigurationElement(MocaRegistry.REGKEY_ZABBIX_LOCAL_AGENT_PORT,
                                                              MocaRegistry.REGKEY_ZABBIX_LOCAL_AGENT_PORT_DEFAULT);
        try {
            _agentPort = Integer.parseInt(tmpAgentPort);
        }
        catch (NumberFormatException e) {
            _logger.error(String.format("Invalid configuration - %s - for Zabbix Agent port, the configuration must be numeric!", tmpAgentPort), e);
            throw e;
        }

        
        // JMX Port (uses Jolokia so this should just be the Moca port)
        String tmpJmxPort = context.getConfigurationElement(MocaRegistry.REGKEY_SERVER_PORT,
                                                            MocaRegistry.REGKEY_SERVER_PORT_DEFAULT);
        try {
            _jmxPort = Integer.parseInt(tmpJmxPort);
        }
        catch (NumberFormatException e) {
            _logger.error(String.format("Invalid configuration - %s - for the Moca port, the configuration must be numeric!", tmpJmxPort), e);
            throw e;
        }
        
        // Check if an IP address was specified, if it isn't this will get derived
        // by the MaD framework.
        _ipAddress = context.getConfigurationElement(MocaRegistry.REGKEY_ZABBIX_HOST_IP);
        
        // Figure out how to address host 
        _useDNS = translateBooleanEntry(
                context.getConfigurationElement(MocaRegistry.REGKEY_ZABBIX_USE_DNS, "false"));
        
        // API user information
        _apiUser = context.getConfigurationElement(MocaRegistry.REGKEY_ZABBIX_API_USER, MocaRegistry.REGKEY_ZABBIX_API_USER_DEFAULT);
        _apiPassword = context.getConfigurationElement(MocaRegistry.REGKEY_ZABBIX_API_PASSWORD, MocaRegistry.REGKEY_ZABBIX_API_PASSWORD_DEFAULT);
        
        _applicationTemplates = deteremineApplicationTemplates(context);
        
        // See if sync is enabled for the machine or application
        _enableMachineSync = translateBooleanEntry(
            context.getConfigurationElement(
                MocaRegistry.REGKEY_ZABBIX_ENABLE_MACHINE_SYNC,
                MocaRegistry.REGKEY_ZABBIX_ENABLE_MACHINE_SYNC_DEFAULT));
        
        _enableApplicationSync = translateBooleanEntry(
            context.getConfigurationElement(
                MocaRegistry.REGKEY_ZABBIX_ENABLE_APPLICATION_SYNC,
                MocaRegistry.REGKEY_ZABBIX_ENABLE_APPLICATION_SYNC_DEFAULT));
    }

    /**
     * Not installed if MOCA registry keys zabbix.server-ip or MOCA_ENVNAME
     * environment variable do not exist.
     * @see com.redprairie.mad.zabbix.ZabbixConfiguration#isIstalled()
     */
    public boolean isEnabled() {
        return _isEnabled;
    }
    

    // @see com.redprairie.mad.zabbix.ZabbixConfiguration#isMachineSyncEnabled()
    @Override
    public boolean isMachineSyncEnabled() {
        return _enableMachineSync;
    }

    // @see com.redprairie.mad.zabbix.ZabbixConfiguration#isApplicationSyncEnabled()
    @Override
    public boolean isApplicationSyncEnabled() {
        return _enableApplicationSync;
    }

    // @see com.redprairie.mad.zabbix.ZabbixConfiguration#getApplicationTemplates()
    @Override
    public List<String> getApplicationTemplates() {
        return _applicationTemplates;
    }

    /**
     * Gets the Zabbix Server IP defined in the MOCA registry with key
     * zabbix.server-ip
     * @see com.redprairie.mad.zabbix.ZabbixConfiguration#getServerConnection()
     * @return The Zabbix Server IP or DNS
     */
    public String getServerConnection() {
        return _serverIp;
    }
    
    /**
     * Gets the port which the Zabbix Trapper is running
     * on the Zabbix Server defined in the MOCA registry with key zabbix.trapper-port
     * @see com.redprairie.mad.zabbix.ZabbixConfiguration#getTrapperPort()
     * @return The Zabbix Trapper port
     */
    public Integer getTrapperPort() {
        return _trapperPort;
    }
    
    /**
     * Gets the port which the Zabbix Agent on this machine is configured
     * to run on. Defined in the MOCA Registry with key zabbix.local-agent-port.
     * @see com.redprairie.mad.zabbix.ZabbixConfiguration#getAgentPort()
     * @return The local Zabbix Agent port
     */
    public Integer getAgentPort() {
        return _agentPort;
    }
    
    /**
     * Gets the Host Group configured for this instance to be registered under in Zabbix.
     * The Host Group uses the following precedence:
     * 1) The Zabbix.host-group registry entry
     * 2) The MOCA cluster name
     * 3) Final default is "MOCA Servers"
     * @see com.redprairie.mad.zabbix.ZabbixConfiguration#getHostGroups()
     * @return The Zabbix Host Group for this instance
     */
    public List<String> getHostGroups() {
        return _hostGroups;
    }
    
    /**
     * Gets the Zabbix Host Name for this instance.
     * The format is as follows: <DNS>-MOCA-<MOCA_ENVNAME>
     * @see com.redprairie.mad.zabbix.ZabbixConfiguration#getApplicationHostName()
     * @return The Zabbix Host Name for this instance
     */
    @Override
    public String getApplicationHostName() {
        return _applicationHostName;
    }

    /**
     * Gets the Zabbix Host Name for the machine running the operating system.
     * @see com.redprairie.mad.zabbix.ZabbixConfiguration#getMachineHostName()
     * @return The Zabbix Host Name for this machine
     */
    @Override
    public String getMachineHostName() {
        return _machineHostName;
    }

    /**
     * Gets the JMX port to use for Zabbix integration. Zabbix uses Jolokia
     * with MOCA so this should be the MOCA port.
     * @see com.redprairie.mad.zabbix.ZabbixConfiguration#getJmxPort()
     * @return The JMX (Jolokia) port
     */
    public Integer getJmxPort() {
        return _jmxPort;
    }
    
    /**
     * Gets the API username for the Zabbix JSON API. Defined in the
     * MOCA Registry with key zabbix.api-user.
     * @see com.redprairie.mad.zabbix.ZabbixConfiguration#getApiUsername()
     * @return The Zabbix JSON API username
     */
    public String getApiUsername() {
        return _apiUser;
    }
    
    /**
     * Gets the API password for the Zabbix JSON API. Defined in the
     * MOCA Registry with key zabbix.api-password.
     * @see com.redprairie.mad.zabbix.ZabbixConfiguration#getApiPassword()
     * @return The Zabbix JSON API password
     */
    public String getApiPassword() {
        return  _apiPassword;
    }
    
    /**
     * Get the IP address to be used by machine and application level hosts.
     * @see com.redprairie.mad.zabbix.ZabbixConfiguration#getIp()
     * @return IP address
     */
    @Override
    public String getIp() {
        return _ipAddress;
    }

    // @see com.redprairie.mad.zabbix.ZabbixConfiguration#useDNS()
    @Override
    public boolean useDNS() {
        return _useDNS;
    }
    
    private List<String> deteremineApplicationTemplates(SystemContext ctx) {
        String appTemplates = ctx.getConfigurationElement(MocaRegistry.REGKEY_ZABBIX_APPLICATION_TEMPLATES);
        // We allow the MOCA Registry to override the auto detection behavior for templates so
        // templates can be manually specified in the MOCA Registry if wanted.
        if (appTemplates != null && !appTemplates.isEmpty()) {
            return stringToList(appTemplates);
        }
        else {
            // Automatically determine the applicable templates
            return getProductTemplates();
        }
    }
    
    // This is used to automatically determine which products are
    // installed and what their corresponding application template name is.
    // We do this by looking for all the defined classes that implement
    // ZabbixTemplateHook
    private List<String> getProductTemplates() {
        String majorMinorVersion = AppUtils.getMajorMinorRevision();
        List<String> templates = new ArrayList<String>();

        ApplicationContext ctx = SpringTools.getContextUsingDataDirectories("hooks.xml");

        if (ctx != null) {
            Collection<ZabbixTemplateHook> hooks = (Collection<ZabbixTemplateHook>)ctx
                    .getBeansOfType(ZabbixTemplateHook.class).values();
    
            Iterator<ZabbixTemplateHook> hooksItr = hooks.iterator();
            while (hooksItr.hasNext()) {
                ZabbixTemplateHook templateHook = (ZabbixTemplateHook) hooksItr.next();
                addTemplate(templates, templateHook.getTemplateProductName(), majorMinorVersion);  
            }
        }
        
        return templates;
    }
    
    // The template format is as follows: <Major>.<Minor version> <Template Name>
    private void addTemplate(List<String> templates, String templateName, String version) {
        templates.add(String.format("%s %s", version, templateName));
    }
    
    private List<String> stringToList(String commaSeperatedList) {
        List<String> list = new ArrayList<String>();
        if (commaSeperatedList == null || commaSeperatedList.isEmpty()) {
            return list;
        }
        
        String[] entries = commaSeperatedList.split(",");
        // Trim all the entries
        for (String entry : entries) {
            list.add(entry.trim());
        }
        
        return list;
    }
    
    private boolean translateBooleanEntry(String entry) {
        if (entry.equalsIgnoreCase("yes")
                || entry.equalsIgnoreCase("true")
                || entry.equals("1")) {
            return true;
        }
        else {
            return false;
        }
    }
    
    private boolean _isEnabled;
    private final String _serverIp;
    private final Integer _jmxPort;
    private final Integer _trapperPort;
    private final Integer _agentPort;
    private List<String> _hostGroups;
    private String  _applicationHostName;
    private String  _machineHostName;
    private final String  _apiUser;
    private final String  _apiPassword;
    private final List<String> _applicationTemplates;
    private final boolean _enableMachineSync;
    private final boolean _enableApplicationSync;
    private final String _ipAddress;
    private final boolean _useDNS;
    
    private static final Logger _logger = LogManager.getLogger(MocaZabbixConfiguration.class);
}
