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

package com.sam.moca.pool;

import java.util.concurrent.TimeUnit;

/**
 * Interface describing a blocking pool that adds additional methods to allow
 * for blocking retrievals from the pool.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface BlockingPool<T> extends Pool<T> {
    /**
     * Returns an instance from the pool, blocking until one is able to be
     * returned.  This method will only unblock from an interrupt request and
     * will then throw an InterruptedException.  This method can never return
     * null;
     * @return An instance from the pool, will always be non null
     * @throws InterruptedException if interrupted while waiting
     */
    public T get() throws InterruptedException;
    
    /**
     * Retrieves and removes an entry from the pool, waiting up to the
     * specified wait time if necessary for an element to become available.
     * @param timeout how long to wait before giving up, in units of
     *        <tt>unit</tt>
     * @param unit a <tt>TimeUnit</tt> determining how to interpret the
     *        <tt>timeout</tt> parameter
     * @return an entry from the pool, or <tt>null</tt> if the
     *         specified waiting time elapses before an element is available
     * @throws InterruptedException if interrupted while waiting
     * @throws PoolException This is thrown if there is an issue with 
     */
    public T get(long timeout, TimeUnit unit) throws InterruptedException;
}
