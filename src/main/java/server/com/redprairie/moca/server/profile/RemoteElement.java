/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.server.profile;


/**
 * A command path element for working with remote commands.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class RemoteElement implements CommandPathElement {
    
    private static final long serialVersionUID = -8923088369401852778L;
    
    public RemoteElement(String connectionString) {
        _connectionString = connectionString;
    }
    
    @Override
    public String getGroup() {
        return "";
    }
    
    @Override
    public String getName() {
        return String.valueOf(_connectionString);
    }
    
    @Override
    public String getType() {
        return "REMOTE";
    }
    
    @Override
    public String toString() {
        return "REMOTE(" + _connectionString + ")";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RemoteElement)) return false;
        return String.valueOf(((RemoteElement)obj)._connectionString).equals(String.valueOf(_connectionString));
    }
    
    @Override
    public int hashCode() {
        return String.valueOf(_connectionString).hashCode();
    }
    
    private final String _connectionString;
}
