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

package com.redprairie.moca.server.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author mlange
 */
public class Service {

    Service(String name, String displayName, String description,
            StartStopInfo startInfo, StartStopInfo stopInfo, String[] dependsOn) {
        _name = name;
        _description = description;
        _displayName = displayName;
        _startInfo = startInfo;
        _stopInfo = stopInfo;
        _dependsOn = new HashSet<String>(Arrays.asList(dependsOn));
    }
    
    String getName() {
        return _name;
    }

    String getDisplayName() {
        return _displayName;
    }
    
    String getDescription() {
        return _description;
    }
    
    StartStopInfo getStartInfo() {
        return _startInfo;
    }
    
    StartStopInfo getStopInfo() {
        return _stopInfo;
    }
    
    String[] getDependsOn() {
        return _dependsOn.toArray(new String[_dependsOn.size()]);
    }
    
    void start(Map<String,String> environment) throws ServiceManagerException {
        String applicationName = ServiceTools.getApplicationName(_name);
        _startInfo.exec(applicationName, environment);
    }
    
    void stop(Map<String,String> environment) throws ServiceManagerException {
        String applicationName = ServiceTools.getApplicationName(_name);
        _stopInfo.exec(applicationName, environment);
    }
       
    public String toString() {

        // Build a nicer version of the depends on set.
        StringBuilder buffer = new StringBuilder();
        for (String value : _dependsOn) {
            if (buffer.length() > 0)
                buffer.append(", ");
            buffer.append(value);
        }
        
        // Build the actual representation of the service.
        StringBuilder str = new StringBuilder();
        str.append("Service Name: " + _name        + "\n");
        str.append("Display Name: " + _displayName + "\n");
        str.append(" Description: " + _description + "\n");
        str.append("  Start Info: " + _startInfo   + "\n");
        str.append("   Stop Info: " + _stopInfo);
        
        if (buffer.length() > 0) {
            str.append("\n");
            str.append("  Depends On: " + buffer);      
        }
        
        return str.toString();
    }
    
    void setName(String name) {
        _name = name;
    }

    void setDisplayName(String displayName) {
        _displayName = displayName;
    }
    
    void setDescription(String description) {
        _description = description;
    }
    
    void setStartInfo(StartStopInfo startInfo) {
        _startInfo = startInfo;
    }
    
    void setStopInfo(StartStopInfo stopInfo) {
        _stopInfo = stopInfo;
    }
    
    void setDependsOn(String[] dependsOnToAdd) {    
        if (dependsOnToAdd == null)
            return;
        
        for (int i = 0; i < dependsOnToAdd.length; i++)
            _dependsOn.add(dependsOnToAdd[i]);
    }
    
    private String _name;
    private String _displayName;
    private String _description;
    private StartStopInfo _startInfo;
    private StartStopInfo _stopInfo;
    private Set<String> _dependsOn;
}