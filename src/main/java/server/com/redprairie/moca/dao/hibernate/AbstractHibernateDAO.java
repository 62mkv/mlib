/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.redprairie.moca.dao.hibernate;

import java.io.Serializable;

import com.redprairie.moca.dao.GenericDAO;
import com.redprairie.moca.db.hibernate.HibernateTools;

/**
 * Abstract hibernate DAO that also provides single primary key read method
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class AbstractHibernateDAO<T, PK extends Serializable> extends
        AbstractUnknownKeyHibernateDAO<T> implements GenericDAO<T, PK> {
    /**
     * @param instanceType
     */
    public AbstractHibernateDAO(Class<T> instanceType) {
        super(instanceType);
    }

    /**
     * 
     * @param id
     * @return <T> Instance of the read object.
     */
    @SuppressWarnings("unchecked")
    public T read(PK pkId) {
        T interfaceObject = (T) HibernateTools.getSession().get(_instanceType, 
                pkId);
        return interfaceObject;
    }
}
