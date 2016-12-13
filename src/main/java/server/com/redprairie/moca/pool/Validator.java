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

package com.redprairie.moca.pool;

/**
 * Provides functionality to validate whether or not an object is valid or not.
 * Usually this is used with a pool to certify whether the object can still be
 * used and how to properly dispose of it.
 * 
 * Validators should be ensured to be thread safe as they will be invoked
 * concurrently.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @param <T> The type that this validator will validate
 * @author wburns
 */
public interface Validator<T> {
    /**
     * This is called on an object after it has been first been created.
     * @param t The object that was just created
     * @throws PoolException thrown if any error occurs during initialization code
     */
    public void initialize(T t) throws PoolException;
    
    /**
     * Calls any setup methods required by this object when it is retrieved
     * from the pool.
     * @param t the object to setup
     * @throws PoolException thrown if any error occurs while reseting the 
     *         pooled object
     */
    public void reset(T t) throws PoolException;
    
    /**
     * Checks whether the object is valid when returned back to the pool.  If
     * this method returns true the object will be placed in the pool.  If the
     * object is no longer valid it will instead be removed resulting in
     * the {@link #invalidate(Object)} method being called.  Any exceptions 
     * are not propagated and
     * are treated as the object being invalid.
     * 
     * @param t the object to check.
     * @return <code>true</code> if the object is valid else <code>false</code>.
     */
    public boolean isValid(T t);

    /**
     * Performs any cleanup activities before discarding the object. For example
     * before discarding database connection objects, the pool will want to
     * close the connections.  Any exceptions from the object are not propagated
     * and depending on implementation will most likely be logged.
     * 
     * @param t the object to cleanup
     */
    public void invalidate(T t);
}
