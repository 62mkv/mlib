/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.redprairie.moca.coverage.repository;

import net.sourceforge.cobertura.coveragedata.ClassData;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaClassData extends ClassData {
    private static final long serialVersionUID = -6800582411563067970L;
    
    public enum ClassType {
        COMMAND,
        TRIGGER
    }

    /**
     * @param name
     */
    public MocaClassData(String name, ClassType type) {
        super(name);
        _type = type;
    }
    
    public void setComplexity(double complexity) {
        _complexity = complexity;
    }
    
    public double getComplexity() {
        return _complexity;
    }
    
    public ClassType getType() {
        return _type;
    }
    
    
    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(_complexity);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((_type == null) ? 0 : _type.hashCode());
        return result;
    }

    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (getClass() != obj.getClass()) return false;
        MocaClassData other = (MocaClassData) obj;
        if (Double.doubleToLongBits(_complexity) != Double
            .doubleToLongBits(other._complexity)) return false;
        if (_type != other._type) return false;
        return true;
    }


    private double _complexity;
    private final ClassType _type;
}
