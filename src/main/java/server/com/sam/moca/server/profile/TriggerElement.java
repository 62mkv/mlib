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

package com.sam.moca.server.profile;

import com.sam.moca.server.repository.Trigger;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class TriggerElement implements CommandPathElement {
    private static final long serialVersionUID = -2510379703764681962L;

    public TriggerElement(Trigger command) {
        _trigger = command;
    }
    
    @Override
    public String getGroup() {
        return "";
    }
    
    @Override
    public String getName() {
        return _trigger.getName();
    }
    
    @Override
    public String getType() {
        return "TRIGGER";
    }
    
    @Override
    public String toString() {
        return "TRIGGER(" + _trigger.getName() + " on " + _trigger.getCommand() + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TriggerElement)) return false;
        
        return ((TriggerElement)obj)._trigger.equals(_trigger);
    }
    
    @Override
    public int hashCode() {
        return _trigger.hashCode();
    }
    
    private final Trigger _trigger;
}
