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

/**
 * A class used for hook points to determine all
 * the product groups Zabbix template names
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class ZabbixTemplateHook {

    /**
     * Gets the product name used in the Zabbix template
     * For example, for Integrator this should be set to
     * "Integrator" and the Zabbix template provided should follow
     * the naming convention of:<br>
     * [major_minor_version] [product_name] RP Template<br>
     * So an example for the Integrator template full name would be:<br>
     * 2012.2 Integrator RP Template
     * 
     * @return The product name used in the Zabbix template
     */
    public String getTemplateProductName() {
        return _templateName;
    }
    
    /**
     * Sets the template product name
     * @param templateName The template product name
     */
    public void setTemplateProductName(String templateName) {
        _templateName = templateName;
    }
   
    
    private String _templateName;
}
