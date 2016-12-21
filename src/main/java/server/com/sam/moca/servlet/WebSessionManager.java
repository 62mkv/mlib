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

package com.sam.moca.servlet;

import java.util.Collections;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.moca.MocaConstants;
import com.sam.moca.server.exec.LocalSessionContext;
import com.sam.moca.server.exec.ServerContext;
import com.sam.moca.server.exec.SessionContext;
import com.sam.moca.server.exec.SessionType;
import com.sam.moca.util.MocaUtils;
import com.sam.moca.util.NonMocaDaemonThreadFactory;

/**
 * This class is used to manage the list of sessions that are currently
 * configured in the MOCA system.  This will store idle and active sessions
 * and also provides an expiration schema that allows for idle sessions to
 * be terminated after a configurable period of time.
 * <p>
 * This class is thread safe in all normal operations.
 * </pre>
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class WebSessionManager {
    /**
     * This class just defines the callback when a session is closed from
     * the session manager.  A caller should never directly invoke the 
     * {@link #onSessionClose(SessionContext)} and should instead use the 
     * {@link WebSessionManager#closeSession(SessionContext)} method instead
     * if a session is deemed to be closed outside of the scope of the 
     * {@link WebSessionManager}.  Failure to do so may cause some memory 
     * concerns or an extra delayed call to the {@link #onSessionClose(SessionContext)}
     * method at a later point.
     * 
     * This callback must be able to be called from any thread and as such is
     * required to be thread safe.
     * 
     * Copyright (c) 2010 Sam Corporation
     * All Rights Reserved
     * 
     * @author wburns
     */
    public interface ClosedSessionCallback {
        public void onSessionClose(SessionContext sessionContext);
    }
    
    /**
     * Create a new session manager given the timeouts for each of the session
     * types in the time unit provided and the callback to call.
     * @param regularTimeout
     * @param keepaliveTimeout
     * @param unit
     * @param callback The callback that is invoked when a session is closed.
     *        This is so the caller can do other special stuff when a session
     *        is closed, such as notifying administrative code etc.  This cannot
     *        be null as the manager only removes the session from it's 
     *        registers when closing the session.
     */
    public WebSessionManager(long regularTimeout, TimeUnit unit,
                              int maxSessions, ClosedSessionCallback callback) {
        if (unit == null || callback == null) {
            throw new NullPointerException();
        }
        
        _timeout = regularTimeout;
        _timeUnit = unit;
        _maxSessions = maxSessions;
        _callback = callback;
    }
    
    /**
     * This is a best attempt at a clean shutdown.
     * This will close all known sessions and shut down any resources 
     * that this manager is responsible for.
     */
    public void close() {
        // This will stop any new requests from getting it, shutting down the
        // threads if any are left etc.
        _executor.shutdown();
        
        // Too bad ConcurrentHashMap doesn't have a drainTo method like the
        // blocking queues ;(
        for (SessionRunnableScheduledFuture<?> future : _sessions.values()) {
            // We cancel any outstanding tasks
            // And subsequently run the future.
            // If it was already running then it is fine since we don't allow
            // closing a session more than once.
            future.cancel(false);
            future.run();
        }
    }
    
    /**
     * This is to return the session back to the session manager after it has
     * been used.  This way the session manager can further watch the session
     * to ensure that if the session is inactive for too long it can close
     * it down.
     * @param context The session to mark as being idle.  This argument cannot
     *        be null.
     */
    public void registerIdle(SessionContext context) {
        register(context, _timeout);
    }
    
    private void register(SessionContext context, long timeout) {
        if (context == null) {
            throw new NullPointerException();
        }
        // We have to try to replace the session first as we don't want to
        // register a session that we don't know about.  Also we want to 
        // schedule the unregister after we validate the session is good.
        SessionRunnableScheduledFuture<Void> future = 
            new SessionRunnableScheduledFuture<Void>(context);
        if (_sessions.replace(context.getSessionId(), future) == null) {
            throw new IllegalStateException("Session was not registered!");
        }
        
        _logger.debug(MocaUtils.concat("Registering session ", context.getSessionId()));
        
        Callable<Void> timeoutHandler = new SessionCloser(context);
        
        // We can safely cast this since we control the executor.
        future._realFuture = (RunnableScheduledFuture<Void>)_executor.schedule(
                timeoutHandler, timeout, _timeUnit);
    }
    
    /**
     * This method is to be called when a session is known to be closed
     * outside of the session manager scope.
     * Note that this is the only way to properly close down a session without
     * direct influence because of the session manager.
     * @param context The session context that should be closed down.  This 
     *        argument cannot be null.
     */
    public void closeSession(SessionContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        
        SessionRunnableScheduledFuture<?> future = _sessions.remove(
                context.getSessionId());
        
        if (future == null) {
            _logger.debug("Session was not registered or already closed!");
            return;
        }
        else {
            _logger.debug(MocaUtils.concat("Closing session: ", 
                    future._session.getSessionId()));
        }
        
        _idleWaitQueueLock.lock();
        try {
            // We have to remove the real future from our queue so others can't
            // pick it up accidently.  If we were unable to remove that should be
            // okay since that just means that this runnable was chosen by a waiter
            // for an opening.
            _idleWaitQueue.remove(future._realFuture);
            
            // We have to decrement the count as well since we just closed this
            // session out.
            _sessionCount.decrementAndGet();
            // We then signal someone that we are decrementing the session
            // count so that they can try to create a new one.
            _idleWaitQueueCondition.signalAll();
        }
        finally {
            _idleWaitQueueLock.unlock();
        }

        // We remove it from the executor so it doesn't run this again.  This
        // is really only pertinent when close is called directly.
        _executor.remove(future._realFuture);
        
        // Lastly we run the session closing inline.
        _callback.onSessionClose(future._session);
    }
    
    /**
     * This method will retrieve an idle session for the name if one exists.
     * This will return null if there are no current idle sessions.
     * If a session is returned it will in turn mark this session as active.
     * @param sessionId The name of the session to look for.  This argument
     *        cannot be null.
     * @return The idle session if present or null if there is no idle session
     *         of that name.
     * @throws SessionRunningException This is thrown if the session is running
     *         that you are trying to retrieve.  The exception will contain
     *         the session context.  No commands should be ran against this
     *         session.
     */
    public SessionContext getIdleSession(String sessionId) 
            throws SessionRunningException {
        if (sessionId == null) {
            throw new NullPointerException();
        }
        
        SessionRunnableScheduledFuture<?> future = _sessions.get(sessionId);
        
        // First we see if saw this session before and if so make sure it isn't
        // currently running.
        if (future == null) {
            _logger.debug(MocaUtils.concat("Idle session not found: ", sessionId));
            return null;
        }

        boolean closing = false;
        
        if (future._realFuture != null) {
            synchronized(future._realFuture) {
                // If it is there and we can't 
                // cancel the future, that means that it is already done
                // or someone else has already canceled it.
                if (!future.cancel(false)) {
                    
                    // If the future couldn't be canceled, it can be 1 of 2 things.  
                    // Either it was canceled previously by an external call to closeSession
                    // or it was because it has become active.  When an external
                    // call cancels it will also remove it from the map, so we double
                    // check that to tell which occurred.
                    if (_sessions.containsKey(sessionId)) {
                        _logger.debug(MocaUtils.concat("Session is already running: ", 
                                sessionId));
                        throw new SessionRunningException(future._session);
                    }
                    // Since it was in the map means it was being closed.
                    closing = true;
                }
            }
        }
        else {
            // If there is no future tied to it, then we know for sure it is
            // actually running.
            throw new SessionRunningException(future._session);
        }
        
        if (closing) {
            // Since it wasn't in the map or queue means it was closed.
            _logger.debug(MocaUtils.concat("Found existing idle session: ", 
                    sessionId, ". However it is in the process of being closed"));
            return null;
        }
        
        // We have to remove references to the future as well to make sure we
        // don't reference the future object for the timeout duration if it
        // has now become active
        _executor.remove(future._realFuture);
        _futureCallableMap.remove(future._realFuture);
        _idleWaitQueueLock.lock();
        try {
            _idleWaitQueue.remove(future._realFuture);
        }
        finally {
            _idleWaitQueueLock.unlock();
        }
        
        _logger.debug(MocaUtils.concat("Found existing idle session: ", sessionId));
        return future._session;
    }
    
    /**
     * This will generate a new session for the given environment.  This
     * newly created session will automatically be marked as active.
     * @param environment The environment to apply to the session.  This can
     *        be null.
     * @return The newly created session.  This will never return null.
     * @throws InterruptedException This is thrown if while waiting for a 
     *         session to free up allowing a new one to be created that we
     *         are interrupted.
     */
    public SessionContext generateNewSessionContext(
            Map<String, String> environment) throws InterruptedException {
        
        boolean waiting = true;
        while (waiting) {
            RunnableScheduledFuture<?> future = null;
            int startingValue = _sessionCount.get();
           
            if (startingValue < _maxSessions) {
                // Now we try to increment, if we can't we loop back and try
                // again.
                if (_sessionCount.compareAndSet(startingValue, 
                        startingValue + 1)) {
                    waiting = false;
                }

                // Increment the peak session count if necessary.
                int currentSessionCount = _sessionCount.get();
                int currentPeakSessionCount = _peakSessionCount.get();
                
                if (currentSessionCount > currentPeakSessionCount) {
                    _peakSessionCount.compareAndSet(currentPeakSessionCount, currentPeakSessionCount + 1);
                }                
            }
            else {
                _logger.info("Max number of sessions " + _maxSessions + 
                    " reached.  Must wait for one to close or become idle to close.");
                _idleWaitQueueLock.lock();
                try {
                    // We have to check the session count again in case
                    // if it changed between when we checked and got in the
                    // lock.  Keep trying to update session count until
                    // it is maxxed out or we get it.
                    while ((startingValue = _sessionCount.get()) < _maxSessions) {
                        // Now we try to increment, if we can't we have to
                        // keep trying
                        if (_sessionCount.compareAndSet(startingValue, 
                                startingValue + 1)) {
                            waiting = false;
                            break;
                        }
                    }
                    
                    if (waiting) {
                        future = _idleWaitQueue.poll();
                        
                        if (future == null) {
                            // Now we wait until someone signals us telling us
                            // that something has changed, whether an idle session
                            // has come or a session has closed.  If so we have
                            // to loop back around and check both.
                            _idleWaitQueueCondition.await();
                        }
                    }
                }
                finally {
                    _idleWaitQueueLock.unlock();
                }
                
            }
        
            // We do this outside of the lock to improve responsiveness.
            if (future != null) {
                // We have to increment the session count so that someone
                // won't steal our spot creating a session, since we are freeing
                // one up below.
                _sessionCount.incrementAndGet();
                // We have to synchronize around this future so it doesn't
                // get confused to whether it is running or closed.
                synchronized (future) {
                    // We try to cancel the future if it hasn't started, if we succeed
                    // then we have to run it personally.  If we can't cancel it 
                    // that means it has already finished which is fine.
                    if (future.cancel(false)) {
                        SessionClosable<?> callable = (SessionClosable<?>)_futureCallableMap.get(future);
                        _logger.debug(MocaUtils.concat("Closing idle session ", 
                                callable.getContext().getSessionId(), 
                                " early to free space"));
                        try {
                            callable.call();
                        }
                        catch (Exception e) {
                            _logger.error("Error occured while closing idle session early", e);
                        }
                    }
                }
                // Since we closed a session and incremented the count, means we
                // don't have to loop around again.
                waiting = false;
            }
        }
        
        SessionContext sessionContext = null;
        boolean found = false;
        // We loop through until we can find a unique session
        while (!found) {
            // No session ID, generate a new session
            String sessionId = generateSessionId();
            sessionContext = new LocalSessionContext(sessionId, 
                SessionType.CLIENT, environment);
            // We put a dummy Future that will be just like a running session.
            if (_sessions.putIfAbsent(sessionId,
                    new SessionRunnableScheduledFuture<Void>(sessionContext)) == null) {
                found = true;
            }
            else {
                _logger.debug(MocaUtils.concat(
                        "Generated duplicate session - discarded: ", sessionId));
            }
        }
        _logger.debug(MocaUtils.concat("Generated new session: ", 
                sessionContext.getSessionId()));
        return sessionContext;
    }
    
    public int getSessionCount() {
        return _sessionCount.get();
    }

    public int getPeakSessionCount() {
        return _peakSessionCount.get();
    }
    
    public int getMaxSessions() {
        return _maxSessions;
    }
    
    protected String generateSessionId() {
        return Long.toHexString(_RAND.nextLong());
    }
    
    private static interface SessionClosable<V> extends Callable<V> {
        public SessionContext getContext();
    }
    
    private class SessionCloser implements SessionClosable<Void> {
        
        public SessionCloser(SessionContext context) {
            _context = context;
        }

        // @see java.util.concurrent.Callable#call()
        @Override
        public Void call() {
            ServerContext serverContext = _context.takeServerContext();
        
            if (serverContext != null) {
                _logger.info(MocaUtils.concat("Attempting to release active server context from session: ", 
                    _context.getSessionId()));
                Transaction transaction = (Transaction)_context.removeAttribute(
                    MocaConstants.SUSPENDED_TX);
                if (transaction != null) {
                    try {
                        transaction.rollback();
                    }
                    catch (SystemException e) {
                        _logger.warn("There was a problem rolling back " +
                                "transaction for idle session: " + 
                                _context.getSessionId(), e);
                    }
                }
                serverContext.close();
            }
            _logger.info(MocaUtils.concat("Attempting to close idle session: ", 
                    _context.getSessionId()));
            WebSessionManager.this.closeSession(_context);
            return null;
        }
        
        // @see com.sam.moca.servlet.WebSessionManager.SessionHolder#getContext()
        @Override
        public SessionContext getContext() {
            return _context;
        }
        
        private final SessionContext _context;
    }

    private class SessionRemovalScheduledService 
            extends ScheduledThreadPoolExecutor {

        /**
         * The default constructor for the session removal schedule service.
         * @param coreSize
         */
        public SessionRemovalScheduledService(int coreSize) {
            super(coreSize, new NonMocaDaemonThreadFactory("Idle Session Watcher", 
                    coreSize > 1));
        }
        
        // @see java.util.concurrent.ScheduledThreadPoolExecutor#decorateTask(java.util.concurrent.Callable, java.util.concurrent.RunnableScheduledFuture)
        @Override
        protected <V> RunnableScheduledFuture<V> decorateTask(
                Callable<V> callable, RunnableScheduledFuture<V> task) {
            // We add an element to the idle wait queue so that if someone is
            // waiting on a session to be marked as idle they can pick it up
            // right away and cancel our task.
            _futureCallableMap.put(task, callable);
            _idleWaitQueueLock.lock();
            try {
                _idleWaitQueue.add(task);
                // We have to signal them that the queue has been updated.
                _idleWaitQueueCondition.signalAll();
            }
            finally {
                _idleWaitQueueLock.unlock();
            }

            return task;
        }

        // @see java.util.concurrent.ScheduledThreadPoolExecutor#decorateTask(java.lang.Runnable, java.util.concurrent.RunnableScheduledFuture)
        @Override
        protected <V> RunnableScheduledFuture<V> decorateTask(
                Runnable runnable, RunnableScheduledFuture<V> task) {
            // We throw this so we know if there was a programming error.
            throw new UnsupportedOperationException();
        }
    }
    
    private static class SessionRunnableScheduledFuture<V> 
            implements RunnableScheduledFuture<V> {

        public SessionRunnableScheduledFuture(SessionContext session) {
            _session = session;
        }
        
        // @see java.util.concurrent.RunnableScheduledFuture#isPeriodic()
        @Override
        public boolean isPeriodic() {
            if (_realFuture != null)
                return _realFuture.isPeriodic();
            return false;
        }

        // @see java.util.concurrent.RunnableFuture#run()
        @Override
        public void run() {
            if (_realFuture != null)
                _realFuture.run();
        }

        // @see java.util.concurrent.Future#cancel(boolean)
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (_realFuture != null)
                return _realFuture.cancel(mayInterruptIfRunning);
            return false;
        }

        // @see java.util.concurrent.Future#get()
        @Override
        public V get() throws InterruptedException, ExecutionException {
            if (_realFuture != null)
                return _realFuture.get();
            return null;
        }

        // @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException,
                ExecutionException, TimeoutException {
            if (_realFuture != null)
                return _realFuture.get(timeout, unit);
            return null;
        }

        // @see java.util.concurrent.Future#isCancelled()
        @Override
        public boolean isCancelled() {
            if (_realFuture != null)
                return _realFuture.isCancelled();
            return true;
        }

        // @see java.util.concurrent.Future#isDone()
        @Override
        public boolean isDone() {
            if (_realFuture != null)
                return _realFuture.isDone();
            return true;
        }

        // @see java.util.concurrent.Delayed#getDelay(java.util.concurrent.TimeUnit)
        @Override
        public long getDelay(TimeUnit unit) {
            if (_realFuture != null)
                return _realFuture.getDelay(unit);
            return 0;
        }

        // @see java.lang.Comparable#compareTo(java.lang.Object)
        @Override
        public int compareTo(Delayed o) {
            if (_realFuture != null)
                return _realFuture.compareTo(o);
            return -1;
        }

        private final SessionContext _session;
        private volatile RunnableScheduledFuture<V> _realFuture;
    }
    
    // @see java.lang.Object#finalize()
    @Override
    protected void finalize() throws Throwable {
        // If we are getting garbage collected, then just forcibly terminate
        // everything.  Some sessions may lie around extra.
        _executor.shutdownNow();
    }
    
    private final SessionRemovalScheduledService _executor = 
        new SessionRemovalScheduledService(1);

    /**
     * This queue is used primarily so that if the generation of a new session
     * is waiting for space so it can forcibly close the new session.
     */
    private final Queue<RunnableScheduledFuture<?>> _idleWaitQueue = 
        new PriorityQueue<RunnableScheduledFuture<?>>();
    private final Lock _idleWaitQueueLock = new ReentrantLock();
    private final Condition _idleWaitQueueCondition = _idleWaitQueueLock.newCondition();
    
    private final Map<RunnableScheduledFuture<?>, Callable<?>> _futureCallableMap = Collections.synchronizedMap(
            new WeakHashMap<RunnableScheduledFuture<?>, Callable<?>>());
    private final ConcurrentMap<String, SessionRunnableScheduledFuture<?>> _sessions = 
        new ConcurrentHashMap<String, SessionRunnableScheduledFuture<?>>();
    private final long _timeout;
    private final TimeUnit _timeUnit;
    private final ClosedSessionCallback _callback;
    /**
     * This could be actually replaced by {@link WebSessionManager#_sessions}.size(), 
     * however that doesn't return in constant time so this is a little nicer.  
     * However whenever something is added/removed from 
     * {@link WebSessionManager#_sessions} this should be updated at the same 
     * time.
     */
    private final AtomicInteger _sessionCount = new AtomicInteger(0);
    private final AtomicInteger _peakSessionCount = new AtomicInteger(0);
    private final int _maxSessions;
    
    private static final Random _RAND = new Random();
    private static final Logger _logger = LogManager.getLogger(
            WebSessionManager.class);
}
