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
 * This represents a pool object that can be used to retrieve and returned
 * a pooled object.
 * <p>
 * If the pool is shutdown any invocation that would attempt to remove something
 * from the pool will be met with an IllegalStateException.  It is still valid
 * to release objects back to the pool when it is shutdown, those objects
 * will usually then just immediately be invalidated.
 * <p>
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * <p>
 * @param <T> The type of object pooled
 * @author wburns
 */
public interface Pool<T> {
    /**
     * Returns an instance from the pool.  If the pool is empty then it will
     * return null.
     * 
     * @return T one of the pooled objects or null if the pool is empty
     */
    T poll();

    /**
     * Releases the object and puts it back to the pool.
     * 
     * The mechanism of putting the object back to the pool is implementation
     * specific and could be asynchronous or synchronous.
     * 
     * This should only be invoked on objects already taken from this pool.
     * 
     * @param t the object to return to the pool
     */
    void release(T t);

    /**
     * Shuts down the pool. In essence this call will not accept any more
     * requests and will release all resources.
     */
    void shutdown();
    
    /**
     * Returns <tt>true</tt> if this pool has been shut down.
     *
     * @return <tt>true</tt> if this pool has been shut down
     */
    boolean isShutdown();
    
    /**
     * Retrieve the maximum size this pool will get to.  If a pool is unbounded
     * this should return null 
     * @return how many elements this pool can grow to be or null for unlimited
     */
    Integer getMaximumSize();
    
    /**
     * Method that can be used to try to remove a pooled object from the
     * pool on request.
     * No validator methods are ran on this object so it's state cannot
     * be guaranteed to be valid.
     * @param t The pool object to remove
     * @return Whether or not the object was removed
     */
    boolean removePooledObject(T t);
}
