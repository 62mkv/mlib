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

package com.redprairie.moca.cluster;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This class is a representation of a role definition in the database.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@Entity
@Table(name="role_definition")
public class RoleDefinition implements Serializable {
    private static final long serialVersionUID = -8100252299625568643L;

    /**
     * @return Returns the roleId.
     */
    @Id
    @Column(name="role_id")
    public String getRoleId() {
        return _roleId;
    }

    /**
     * @param roleId The nodeId to set.
     */
    public void setRoleId(String roleId) {
        _roleId = roleId;
    }
    
    // @see java.lang.Object#hashCode()
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_roleId == null) ? 0 : _roleId.hashCode());
        return result;
    }

    // @see java.lang.Object#equals(java.lang.Object)
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        RoleDefinition other = (RoleDefinition) obj;
        if (_roleId == null) {
            if (other._roleId != null) return false;
        }
        else if (!_roleId.equals(other._roleId)) return false;
        return true;
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        return _roleId;
    }

    private String _roleId;
}
