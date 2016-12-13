/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016-2007
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

package com.redprairie.moca.components.base;

import java.nio.charset.Charset;
import java.util.Properties;
import java.util.TreeSet;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;

/**
 * MOCA components to get Java system information.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class JavaInformation {
    /**
     * Returns a result set containing all Java system properties.
     */
    public MocaResults listJavaProperties(MocaContext moca, String name) {
        EditableResults res = moca.newResults();
        res.addColumn("name", MocaType.STRING);
        res.addColumn("value", MocaType.STRING);
        Properties props = System.getProperties();
        
        if (name != null) {
            res.addRow();
            res.setStringValue("name", name);
            res.setStringValue("value", System.getProperty(name));
        }
        else {
            for (Object keyObj : new TreeSet<Object>(props.keySet())) {
                String key = (String)keyObj;
                res.addRow();
                res.setStringValue("name", key);
                res.setStringValue("value", props.getProperty(key));
            }
        }
        return res;
    }
    
    /**
     * Returns a result set containing a number of key Java system
     * properties, as well as information about the current java runtime
     * environment.
     */
    public MocaResults listJavaInformation(MocaContext moca) {
        EditableResults res = moca.newResults();
        res.addColumn("java_version", MocaType.STRING);
        res.addColumn("java_vendor", MocaType.STRING);
        res.addColumn("java_home", MocaType.STRING);
        res.addColumn("classpath", MocaType.STRING);
        res.addColumn("os_name", MocaType.STRING);
        res.addColumn("processors", MocaType.INTEGER);
        res.addColumn("max_memory", MocaType.STRING);
        res.addColumn("total_memory", MocaType.STRING);
        res.addColumn("free_memory", MocaType.STRING);
        res.addColumn("charset", MocaType.STRING);
        
        res.addRow();
        
        res.setStringValue("java_version", System.getProperty("java.version"));
        res.setStringValue("java_vendor", System.getProperty("java.vendor"));
        res.setStringValue("java_home", System.getProperty("java.home"));
        res.setStringValue("classpath", System.getProperty("java.class.path"));
        res.setStringValue("os_name", System.getProperty("os.name"));

        Runtime vm = Runtime.getRuntime();
        res.setIntValue("processors", vm.availableProcessors());
        res.setStringValue("max_memory", Long.toString(vm.maxMemory()));
        res.setStringValue("free_memory", Long.toString(vm.freeMemory()));
        res.setStringValue("total_memory", Long.toString(vm.totalMemory()));
        
        res.setStringValue("charset", Charset.defaultCharset().name());
        
        return res;
    }
}
