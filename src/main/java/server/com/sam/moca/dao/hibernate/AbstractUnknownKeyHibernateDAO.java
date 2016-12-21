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

package com.sam.moca.dao.hibernate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.LockAcquisitionException;
import org.hibernate.metadata.ClassMetadata;

import com.sam.moca.dao.DAOCriteria;
import com.sam.moca.dao.UnknownKeyDAO;
import com.sam.moca.db.hibernate.HibernateTools;
import com.sam.moca.util.MocaUtils;
import com.sam.util.ArgCheck;
import com.sam.util.NotImplementedException;

/**
 * A Hibernate specific implementation of the Data Access Object (DAO) to assist
 * hibernate specific configurations and generic handling of 
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @param <T> The mapped Hibernate class.
 * @param <PK> The primary key type.
 * @author Matt Horner
 * @author wburns
 * @version $Revision$
 */
public abstract class AbstractUnknownKeyHibernateDAO<T> 
        implements UnknownKeyDAO<T> {
    /**
     * 
     * @param instanceType
     */
    public AbstractUnknownKeyHibernateDAO(Class<T> instanceType) {
        ArgCheck.notNull(instanceType);
        // This is to check to make sure that the class is indeed mapped with
        // hibernate
        ClassMetadata metaData = HibernateTools.getSessionFactory(
            MocaUtils.currentContext()).getClassMetadata(instanceType);
        if (metaData == null) {
            throw new NotImplementedException("Class [" + instanceType.getName() + 
                    "] was not loaded into Hibernate.  Please ensure the " +
                    "prod-dirs registry setting is properly " +
                    "configured.");
        }
        _instanceType = instanceType;
    }
    
    /**
     * Retrieve all objects from the session.
     * 
     * @return List<T> The set of objects returned from the session.
     */
    @Override
    public List<T> readAll() {
        return (List<T>) createEmptyCriteria().list();
    }
    
    /**
     * 
     * @param <T> Object to save through Hibernate.
     */
    @Override
    public void save(T object) {
        if (object != null) {
            HibernateTools.getSession().saveOrUpdate(object);
        }
    }
    
    /**
     * Delete the object from the session.
     * @param object The object to remove.
     */
    @Override
    public void delete(T object) {
        if (object != null) {
            HibernateTools.getSession().delete(object);
        }
    }
    
    /**
     * Flush the current context to the database.
     * @return void
     */
    @Override
    public void flush() {
        HibernateTools.getSession().flush();
    }
    
    /**
     * This will lock the given and update at the same time.  Note that any
     * children objects may possibly be stale and if needed this object
     * should be reread after locking.
     * Depending on the database provider there is no guarantee as to whether
     * the wait argument is paid attention to.
     * @param obj The object to lock the row on
     * @param wait Whether to wait for the lock or timeout
     * @return whether or not the lock was obtained
     */
    public boolean lockRow(T obj, boolean wait) {
        try {
            LockOptions opts = new LockOptions();
            if (wait) {
                opts.setLockMode(LockMode.PESSIMISTIC_WRITE);
                opts.setTimeOut(LockOptions.WAIT_FOREVER);
            }
            else {
                opts.setLockMode(LockMode.UPGRADE_NOWAIT);
                opts.setTimeOut(LockOptions.NO_WAIT);
            }
            HibernateTools.getSession().refresh(obj, opts);
            return true;
        }
        catch (LockAcquisitionException e) {
            return false;
        }
    }
    
    
    /**
     * Creates a new empty criteria object. 
     * @return DAOCriteria<T> The new instance of hibernate criteria.
     */
    @Override
    public DAOCriteria<T> createEmptyCriteria() {
        return new DAOHibernateCriteria();
    }
    
    protected Class <T>_instanceType;
    
    class DAOHibernateCriteria implements DAOCriteria<T> {
        public DAOHibernateCriteria() {
            _criteria = HibernateTools.getSession().createCriteria(
                    _instanceType);
        }

        /**
         * List the results based on the criteria specified.  This will not
         * contain any duplicate objects.
         * @return List<T> The list of objects matching the criteria.
         */
        @SuppressWarnings("unchecked")
        public List<T> list() {
            List<T> origList = _criteria.list();
            // We wrap the list with a LinkedHashSet to remove duplicates while
            // maintaining order.  We then have to then pass that back into
            // an ArrayList since we have to return a List to the caller.
            Set<T> hibSet = new LinkedHashSet<T>(origList);
            
            return new ArrayList<T>(hibSet);
        }
        
        // @see com.sam.ems.dao.DAOCriteria#addEqRestriction(java.lang.String, java.lang.Object)
        public DAOCriteria<T> addEqRestriction(String name, Object value) {
            _criteria.add(Restrictions.eq(name, value));
            return this;
        }

        // @see com.sam.moca.dao.DAOCriteria#addNotEqRestriction(java.lang.String, java.lang.Object)
        
        @Override
        public DAOCriteria<T> addNotEqRestriction(String name, Object value) {
            _criteria.add(Restrictions.not(Restrictions.eq(name, value)));
            return this;
        }
        
        // @see com.sam.ems.dao.DAOCriteria#addInRestriction(java.lang.String, java.lang.Object[])
        public DAOCriteria<T> addInRestriction(String name, Object[] values) {
            if (values.length == 1) {
                return addEqRestriction(name, values[0]);
            }
            _criteria.add(Restrictions.in(name, values));
            return this;
        }

        // @see com.sam.ems.dao.DAOCriteria#addNotInRestriction(java.lang.String, java.lang.Object[])
        public DAOCriteria<T> addNotInRestriction(String name, Object[] values) {
            if (values.length == 1) {
                return addNotEqRestriction(name, values[0]);
            }
            _criteria.add(Restrictions.not(Restrictions.in(name, values)));
            return this;
        }
        
        // @see com.sam.ems.dao.DAOCriteria#addLtRestriction(java.lang.String, java.lang.Object)
        public DAOCriteria<T> addLtRestriction(String name, Object value) {
            _criteria.add(Restrictions.lt(name, value));
            return this;
        }

        // @see com.sam.ems.dao.DAOCriteria#addGtRestriction(java.lang.String, java.lang.Object)
        public DAOCriteria<T> addGtRestriction(String name, Object value) {
            _criteria.add(Restrictions.gt(name, value));
            return this;
        }
        
        
        // @see com.sam.ems.dao.DAOCriteria#addSqlRestriction(java.lang.String)
        public DAOCriteria<T> addSqlRestriction(String sql) {
            _criteria.add(Restrictions.sqlRestriction(sql));
            return this;
        }
        
        // @see com.sam.moca.dao.DAOCriteria#addOrderByRestriction(java.lang.String, boolean)
        public DAOCriteria<T> addOrderBy(String name, boolean ascending) {
            Order orderby;
            if (ascending) {
                orderby = Order.asc(name);
            }
            else {
                orderby = Order.desc(name);
            }
            
            _criteria.addOrder(orderby);
            return this;
        }
        
        // @see com.sam.ems.dao.DAOCriteria#addNullRestriction(java.lang.String)
        public DAOCriteria<T> addNullRestriction(String name) {
            _criteria.add(Restrictions.isNull(name));
            return this;
        }
        
        // @see com.sam.moca.dao.DAOCriteria#addNotNullRestriction(java.lang.String)
        @Override
        public DAOCriteria<T> addNotNullRestriction(String name) {
            _criteria.add(Restrictions.isNotNull(name));
            return this;
        }
        
        // @see com.sam.moca.dao.DAOCriteria#cacheIfAble()
        @Override
        public DAOCriteria<T> cacheIfAble() {
            _criteria.setCacheable(true);
            _criteria.setCacheMode(CacheMode.NORMAL);
            return this;
        }
        
        private final Criteria _criteria;
    }
}