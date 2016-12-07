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

package com.redprairie.moca.dao;

import java.util.List;

/**
 * DAO Criteria interface for defining the API to the Criteria model.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @param <T> The implementation specific class type.
 * @author wburns
 * @author Matt Horner
 * @version $Revision$
 */
public interface DAOCriteria<T> {
    public List<T> list();
    
    /**
     * Adds an equality restriction to the query 
     * @param name The field name.
     * @param value The field value.
     * @return DAOCritiera<T> To continue building on the criteria.
     */
    public DAOCriteria<T> addEqRestriction(String name, Object value);
    
    /**
     * Adds an inequality restriction to the query 
     * @param name The field name.
     * @param value The field value.
     * @return DAOCritiera<T> To continue building on the criteria.
     */
    public DAOCriteria<T> addNotEqRestriction(String name, Object value);
    
    /**
     * Adds a less than restriction to the query 
     * @param name The field name.
     * @param value The field value.
     * @return DAOCritiera<T> To continue building on the criteria.
     */
    public DAOCriteria<T> addLtRestriction(String name, Object value);
    
    /**
     * Adds an in restriction to the query 
     * @param name The field name.
     * @param values The field values.
     * @return DAOCritiera<T> To continue building on the criteria.
     */
    public DAOCriteria<T> addInRestriction(String name, Object[] values);
    
    /**
     * Adds a not in restriction to the query 
     * @param name The field name.
     * @param values The field values.
     * @return DAOCritiera<T> To continue building on the criteria.
     */
    public DAOCriteria<T> addNotInRestriction(String name, Object[] values);
    
    /**
     * Adds an greater than  restriction to the query 
     * @param name The field name.
     * @param value The field value.
     * @return DAOCritiera<T> To continue building on the criteria.
     */
    public DAOCriteria<T> addGtRestriction(String name, Object value);
    
    /**
     * Adds a free-form sql restriction to the query 
     * @param sql The sql restriction.
     * @return DAOCritiera<T> To continue building on the criteria.
     */
    public DAOCriteria<T> addSqlRestriction(String sql);
    
    /**
     * Adds an order by to make sure the rows returned are in a certain order.
     * 
     * Multiple orderbys can be applied to sort by multiple columns
     * @param name The field name
     * @param ascending Whether to return them ordered ascending or descending.
     * @return DAOCritiera<T> To continue building on the criteria.
     */
    public DAOCriteria<T> addOrderBy(String name, boolean ascending);
    
    /**
     * Adds a null restriction to the query for the given column 
     * @param name The field name
     * @return DAOCritiera<T> To continue building on the criteria.
     */
    public DAOCriteria<T> addNullRestriction(String name);
    
    /**
     * Adds a not null restriction to the query for the given column 
     * @param name The field name
     * @return DAOCritiera<T> To continue building on the criteria.
     */
    public DAOCriteria<T> addNotNullRestriction(String name);
    
    /**
     * This will try to cache the given criteria so that multiple uses will
     * not have to hit the database in subsequent requests
     * @return DAOCritiera<T> To continue building on the criteria. 
     */
    public DAOCriteria<T> cacheIfAble();
}
