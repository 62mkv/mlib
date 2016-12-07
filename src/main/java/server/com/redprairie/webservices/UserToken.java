/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
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

package com.redprairie.webservices;

/**
 * A class to hold user and password information for authenticating a web
 * service.
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class UserToken {
    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        _password = password;
    }
    
    public String getUsername() {
        return _username;
    }

    public void setUsername(String username) {
        _username = username;
    }
    
    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof UserToken)) return false;
        
        String otherUsername = ((UserToken)obj)._username;
        String otherPassword = ((UserToken)obj)._password;
        return _equalOrNull(otherUsername, _username) &&
               _equalOrNull(otherPassword, _password);
    }
    
    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        int hash = 17;
        if (_username != null) hash = hash * 37 + _username.hashCode();
        if (_password != null) hash = hash * 37 + _password.hashCode();
        return hash;
    }
    
    //
    // Implementation
    //
    private boolean _equalOrNull(Object a, Object b) {
        if (a == null && b == null) return true;
        else if (a == null || b == null) return false;
        else return a.equals(b);
    }
    
    private String _username;
    private String _password;
}
