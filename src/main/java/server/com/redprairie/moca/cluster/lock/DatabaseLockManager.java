/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.cluster.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.cluster.ClusterLockManager;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.cluster.dao.RoleDefinitionDAO;
import com.redprairie.moca.util.MocaUtils;

/**
 * This class is an implementation of role lock manager using the database
 * tables to do the locking.
 * <p>
 * This lock implementation currently always uses the current transaction
 * in the calling thread.  Therefore it is advised to not do any transactional
 * work when using the locking mechanism as transaction may get messed up.
 * <p>
 * TODO: change this to have a thread per lock to guarantee transactional safety
 * 
 * Copyright (c) 2010 RedPrairie Corporation All Rights Reserved
 * 
 * @author wburns
 */
public class DatabaseLockManager implements ClusterLockManager {

    /**
     * @param channel
     * @param preferredRole
     * @param clusterRoleAwareObjects
     */
    public DatabaseLockManager(RoleDefinitionDAO dao) {
        if (dao == null) {
            throw new NullPointerException();
        }
        _dao = dao;
    }
    
    @Override
    public Lock getLock(RoleDefinition node) {
        // TODO: we would need to return same instance for each node to ensure the locking mechanism works, if retrieved from different places and get different HibernateLock instances for the same Node
        return new HibernateLock(node);
    }
    
    private class HibernateLock implements Lock {
        
        public HibernateLock(RoleDefinition node) {
            _node = node;
        }
        
        @Override
        public void unlock() {
            _lock.unlock();
            // If we no longer hold the lock then we roll the transaction
            // back releasing the database lock.
            if (!_lock.isLocked()) {
                try {
                    MocaUtils.currentContext().rollback();
                }
                catch (MocaException e) {
                    _logger.warn("There was a problem releasing lock.", e);
                }
            }
        }
        
        @Override
        public boolean tryLock(long time, TimeUnit unit)
                throws InterruptedException {
            return tryAcquireLock(_node, time, unit, true);
        }
        
        @Override
        public boolean tryLock() {
            try {
                return tryAcquireLock(_node, 0, TimeUnit.MILLISECONDS, false);
            }
            catch (InterruptedException e) {
                // This should never happen
                return false;
            }
        }
        
        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void lockInterruptibly() throws InterruptedException {
            tryAcquire(_node, true);
        }
        
        @Override
        public void lock() {
            try {
                tryAcquire(_node, false);
            }
            catch (InterruptedException e) {
                // This should never happen
            }
        }
        
        private void tryAcquire(RoleDefinition node, boolean interruptible) 
                throws InterruptedException {
            boolean acquired = false;
            boolean interrupted = false;
            
            while (!acquired) {
                try {
                    acquired = tryAcquireLock(node, Long.MAX_VALUE, TimeUnit.DAYS, 
                        true);
                }
                catch (InterruptedException e) {
                    if (interruptible) {
                        throw e;
                    }
                    interrupted = true;
                }
            }
            
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        
        private boolean tryAcquireLock(RoleDefinition node, long time,
                TimeUnit unit, boolean useTimeout) throws InterruptedException {
            long begin = System.currentTimeMillis();
            boolean acquired;
            // We grab the reentrant lock first, this way we won't
            // have multiple database connections waiting for the lock
            if (useTimeout) {
                acquired = _lock.tryLock(time, unit);
            }
            else {
                acquired = _lock.tryLock();
            }
            
            // If we couldn't even get the reentrant lock then just return
            // immediately
            if (!acquired) {
                return false;
            }
            
            boolean dbAcquired = false;
            if (useTimeout) {
                // We could use nano, but milli should be more than sufficient since
                // db may be seconds between timeouts
                long getMaxWait = TimeUnit.MILLISECONDS.convert(time, unit);
                
                while (!dbAcquired && System.currentTimeMillis() - begin < getMaxWait) {
                    dbAcquired = _dao.lockRow(node, false);
                    if (!dbAcquired) {
                        try {
                            // We sleep just a little to prevent the db from 
                            // possibly getting overwhelmed
                            Thread.sleep(100);
                        }
                        catch (InterruptedException e) {
                            // If we were  interrupted clear the lock and
                            // throw the exception
                            _lock.unlock();
                            throw e;
                        }
                    }
                }
            }
            else {
                dbAcquired = _dao.lockRow(node, false);
            }
            
            // If we couldn't get the db lock we have to release the reentrant
            // lock.
            if (!dbAcquired) {
                _lock.unlock();
            }
            
            return dbAcquired;
        }
        
        private final RoleDefinition _node;
        private ReentrantLock _lock = new ReentrantLock();
    }
    
    private final RoleDefinitionDAO _dao;
    private static final Logger _logger = LogManager.getLogger(
        DatabaseLockManager.class);
}