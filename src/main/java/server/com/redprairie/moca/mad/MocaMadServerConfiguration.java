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
package com.redprairie.moca.mad;

import com.redprairie.mad.protocol.CustomMessageDispatcher;
import com.redprairie.mad.server.AbstractMadServerConfiguration;
import com.redprairie.mad.zabbix.ZabbixConfiguration;
import com.redprairie.mad.zabbix.jmx.ZabbixDiscoveryHelper;
import com.redprairie.moca.mad.custom.ExecutorStatusHandler;
import com.redprairie.moca.mad.zabbix.MocaZabbixConfiguration;
import com.redprairie.moca.mad.zabbix.MocaZabbixDiscoveryHelper;
import com.redprairie.moca.server.ServerUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Mad in MOCA
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 *
 * @author klucas
 */
@Configuration
public class MocaMadServerConfiguration extends AbstractMadServerConfiguration {
    // @see com.redprairie.mad.configuration.AbstractMadConfiguration#zabbixConfiguration()
    @Bean
    @Override
    public ZabbixConfiguration zabbixConfiguration() {
        return new MocaZabbixConfiguration(ServerUtils.globalContext());
    }
    
    // @see com.redprairie.mad.configuration.AbstractMadConfiguration#zabbixDiscoveryHelper()
    @Bean
    @Override
    public ZabbixDiscoveryHelper zabbixDiscoveryHelper() {
        return new MocaZabbixDiscoveryHelper();
    }
    
    // @see com.redprairie.mad.server.AbstractMadServerConfiguration#customMessageDispatcher()
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
}
