/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class CommandLineArgument {

    /**
     * Creates a new command line argument
     * @param argSwitch The argument switch character
     * @param registryKey The registry key
     */
    public CommandLineArgument(char argSwitch, String registryKey) {
        this(argSwitch, registryKey, null);
    }
    
    public CommandLineArgument(char argSwitch, String registryKey, String value) {
        _switch = argSwitch;
        _registryKey = registryKey;
        _value = value;
    }
    
    /**
     * @return Returns the argSwitch.
     */
    public char getArgSwitch() {
        return _switch;
    }

    /**
     * @return Returns the registryKey.
     */
    public String getRegistryKey() {
        return _registryKey;
    }
    
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return _value;
    }
    
    private final char _switch;
    private final String _registryKey;
    private final String _value;
}
