/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

/**
 * TODO Class Description
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class ScriptElement implements CommandPathElement {
    
    private static final long serialVersionUID = -2506842933078187400L;
    
    public static final ScriptElement INSTANCE = new ScriptElement();
    
    private ScriptElement() {
        // private constructor to prevent instantiation
    }
    
    @Override
    public String getGroup() {
        return "";
    }
    
    @Override
    public String getName() {
        return "SCRIPT";
    }
    
    @Override
    public String getType() {
        return "SCRIPT";
    }
    
    @Override
    public String toString() {
        return "SCRIPT";
    }
    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof CommandPathElement);
    }
    
    @Override
    public int hashCode() {
        return 37;
    }
}
