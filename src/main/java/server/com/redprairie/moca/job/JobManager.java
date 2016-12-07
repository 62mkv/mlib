/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009-10
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

package com.redprairie.moca.job;

import groovy.time.TimeCategory;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.cluster.ClusterCachable;
import com.redprairie.moca.cluster.ClusterInformation;
import com.redprairie.moca.cluster.ClusterRoleAware;
import com.redprairie.moca.cluster.Node;
import com.redprairie.moca.cluster.RoleDefinition;
import com.redprairie.moca.exceptions.InvalidArgumentException;
import com.redprairie.moca.job.dao.JobDefinitionDAO;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.util.DaemonThreadFactory;
import com.redprairie.moca.util.ExceptionSuppressingRunnable;
import com.redprairie.moca.util.MocaUtils;

/**
 * Manage the execution of jobs.  This class takes job definitions from the job_definition
 * table and interacts with a JobScheduler to manage the scheduling of jobs at a lower level.
 * 
 * Copyright (c) 2009-10 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author mlange
 */
public class JobManager extends ClusterCachable<JobDefinition> implements ClusterRoleAware {
    
    public static final String ATTRIBUTE_NAME = JobManager.class.getName();
    
    public JobManager(JobDefinitionDAO dao, JobScheduler scheduler,
         ClusterInformation clusterInfo, int syncSeconds, boolean startScheduling) {
        this(dao, scheduler, clusterInfo, syncSeconds, startScheduling, null, null, null);
    }
    
    /**
     * This Constructor adds on the additional fields related to Cluster Caching.
     * The additional fields include the Cluster's Cache and the Local Node.
     * 
     * @param dao
     * @param scheduler
     * @param clusterInfo
     * @param syncSeconds
     * @param startScheduling
     * @param jobMap
     * @param localNode
     */
    public JobManager(JobDefinitionDAO dao, JobScheduler scheduler, 
        ClusterInformation clusterInfo, int syncSeconds, boolean startScheduling,
        ConcurrentMap<Node, Set<JobDefinition>> jobMap, Node localNode, String name) {
        super(jobMap, localNode, name);
        _dao = dao;
        _syncSeconds = syncSeconds;
        
        _clusterInfo = clusterInfo;
        
        // Determine if job scheduling is disabled.
        if (!startScheduling) {
            _logger.info("Job auto scheduling is disabled");
            _disableScheduling = true;
        }
        else {
            _logger.info("Job scheduling is enabled");
            _disableScheduling = false;
        }
        
        _jobScheduler = scheduler;
    }
    
    /**
     * Start the job manager.
     * 
     * @throws JobException
     */
    public void start() throws JobException {
        _syncLock.lock();
        try {
            _logger.info("Starting the job manager");
            
            if (_currentJobs != null) {
                throw new IllegalStateException("Job manager is already running");
            }
            
            // Build the current jobs list. 
            _currentJobs = new LinkedHashMap<String, JobDefinition>();

            List<JobDefinition> definedJobList = getDefinedJobs();
            for (JobDefinition job : definedJobList) {
                addJob(job);
            }
            
            // If scheduling isn't disabled add enabled jobs to the job scheduler.
            if (!_disableScheduling) {
                for (JobDefinition job : definedJobList) {
                    if (job.isEnabled()) {
                        try {
                            scheduleJob(job.getJobId());
                        }
                        catch (JobRuntimeException e) {
                            _logger.error("There was a problem starting job [" + 
                                    job.getJobId() + "]", e);
                        }
                    }
                }
            }
            
            // Start the job scheduler.
            _jobScheduler.start();
            
            if (!_disableScheduling) {
                // Create and start a job synchronizer thread.
                _jobSynchronizerHandle = _scheduler.scheduleAtFixedRate(
                        new ExceptionSuppressingRunnable(new JobSynchronizerRunnable(), _logger), 
                        _syncSeconds, _syncSeconds, TimeUnit.SECONDS);
            }
            
            notifyStarted();
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    /**
     * Stop the job manager.
     * @throws JobException
     */  
    public void stop() throws JobException  {
        _syncLock.lock();
        try {
            _logger.info("Stopping the job manager...");
            
            if (_currentJobs == null) {
                throw new IllegalStateException("Job manager is not running");
            }
    
            // Unschedule every job and clear the current jobs list.
            List<JobDefinition> currentJobList = getCurrentJobs();   
            for (JobDefinition job : currentJobList) {  
                unscheduleJob(job.getJobId());
            }
            _currentJobs = null;
            
            // Stop the job synchronizer schedule.
            if (_jobSynchronizerHandle != null) {
                _jobSynchronizerHandle.cancel(true);
            }
            
            // Stop the job scheduler.
            _jobScheduler.stop();
            notifyStopped();
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    /**
     * Restart the job manager.
     * 
     * @throws JobException
     */
    public void restart() throws JobException {
        _syncLock.lock();
        try {
            Map<String, JobDefinition> oldJobs =
                new LinkedHashMap<String, JobDefinition>(_currentJobs);
            try {
                _restarting = true;

                _logger.info("Restarting the job scheduler...");
                try {
                    stop();
                }
                catch (IllegalStateException ignore) {
                    // Ignore
                }
                
                start();
            }
            finally {
                _restarting = false;
                notifyRestarted(oldJobs);
            }
        }
        finally {
            _syncLock.unlock();
        }
    }
 
    /**
     * This method is used to start scheduling a specific job.
     * 
     * @param jobId The case-sensitive identifier of the job to start.
     * @throws JobException 
     * @throws InvalidArgumentException 
     * @throws JobRuntimeException
     */
    public void scheduleJob(String jobId) throws JobException, JobRuntimeException {
        _syncLock.lock();
        try {
            _logger.info(MocaUtils.concat("Scheduling job ", jobId)); 
            
            if (_currentJobs == null) {
                throw new IllegalStateException("Job manager is not running");
            }
            
            // Make sure the job exists.
            JobDefinition job = _currentJobs.get(jobId);
            if (job == null) {
                throw new IllegalArgumentException("Job " + jobId + " does not exist");
            }
            
            scheduleJob(job);
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    /**
     * This method is used to stop scheduling a specific job.
     * 
     * @param jobId The case-sensitive identifier of the job to stop.
     * @throws JobException 
     * @throws InvalidArgumentException 
     */
    public void unscheduleJob(String jobId) throws JobException {
        _syncLock.lock();
        try {
            _logger.info(MocaUtils.concat("Unscheduling job ", jobId)); 
    
            if (_currentJobs == null) {
                throw new IllegalStateException("Job manager is not running");
            }
            
            // Make sure the job exists.
            JobDefinition job = _currentJobs.get(jobId);
            if (job == null) {
                throw new IllegalArgumentException("Job " + jobId + " does not exist");
            }
            
            unscheduleJob(job);
        }
        finally {
            _syncLock.unlock();
        }
    }
  
    /**
     * This method is used to stop scheduling a specific job.
     * 
     * @param jobId The case-sensitive identifier of the job to stop.
     * @throws JobException 
     */
    public boolean isScheduled(String jobId) throws JobException {
        _syncLock.lock();
        try {
            if (_currentJobs == null) {
                throw new IllegalStateException("Job manager is not running");
            }
            
            // Make sure the job exists.
            JobDefinition job = _currentJobs.get(jobId);
            if (job == null) {
                throw new IllegalArgumentException("Job " + jobId + " does not exist");
            }
            
            return _jobScheduler.exists(job);
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    /**
     * Get the list of all jobs being managed.
     * 
     * @return the list of all managed jobs.
     */
    public List<JobDefinition> getAllJobs() {
        return new ArrayList<JobDefinition>(_dao.readAll());
    }
    
    /**
     * Returns the Map of all of the Jobs running accross the Cluster.
     * 
     * @return the Map of the Running Jobs in the Cluster.
     */
    public ConcurrentMap<Node, Set<JobDefinition>> getClusteredJobs() {
        return getClusterCache();
    }
    
    /**
     * Get the list of current jobs being managed.
     * 
     * @return the list of currently managed jobs.
     */
    public List<JobDefinition> getCurrentJobs() {
        _syncLock.lock();
        try {
            if (_currentJobs == null) {
                return new ArrayList<JobDefinition>();
            }
            else {
                return new ArrayList<JobDefinition>(_currentJobs.values());
            }
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    public JobDefinition getCurrentJob(String jobId) {
        _syncLock.lock();
        try {
            if (_currentJobs == null) {
                return null;
            }
            else {
                return _currentJobs.get(jobId);
            }
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    /**
     * Gets the date of the next scheduled execution for a job.
     * @param jobId The Job ID
     * @return The next execution date or null if the job is not scheduled
     * @throws SchedulerException
     */
    public Date getNextExecutionDate(String jobId) throws SchedulerException {
        return _jobScheduler.getNextExecutionDate(jobId);
    }
    
    /**
     * Gets the amount of time to the next execution date of the job
     * reported as a string
     * @param jobId The Job ID
     * @return The amount of time until the next execution date
     * @throws SchedulerException
     */
    public String getNextExecutionDateCountdown(String jobId) throws SchedulerException {
        Date nextExec = getNextExecutionDate(jobId);
        return nextExec == null ? null : TimeCategory.minus(nextExec, new Date()).toString();
    }
    
    /**
     * Gets the last execution date of a job.
     * @param jobId The Job ID
     * @return The last execution date or null if the job is not scheduled
     * @throws SchedulerException
     */
    public Date getLastExecutionDate(String jobId) throws SchedulerException {
        return _jobScheduler.getLastExecutionDate(jobId);
    }
    
    /**
     * Gets the elapsed time since the job executed reported as a string
     * @param jobId The Job ID
     * @return The elapsed time reported as a string
     * @throws SchedulerException
     */
    public String getLastExecutionDateElapsed(String jobId) throws SchedulerException {
        Date lastExec = getLastExecutionDate(jobId);
        return lastExec == null ? null : TimeCategory.minus(new Date(), lastExec).toString();
    }
    
    /**
     * Registers a Job Manager listener that 
     * responds to notifications.
     * @param listener The Job Manager Listener to register
     */
    public void addListener(JobManagerListener listener) {
        _listeners.add(listener);
    }
    
    /**
     * Unregisters a Job Manager listener
     * @param listener The Job Manager Listener to unregister
     */
    public void removeListener(JobManagerListener listener) {
        _listeners.remove(listener);
    }
   
    //
    // Implementation
    //
    
    private List<JobDefinition> getDefinedJobs() {
        if (_clusterDeactivated.get()) {
            return _dao.readAll();
        }
        else if (_clusterInfo.isLeader()) {
            return _dao.readForAllNoRoleAndRoles(_nodeIds.toArray(
                new RoleDefinition[_nodeIds.size()]));
        }
        else {
            return _dao.readForAllAndRoles(_nodeIds.toArray(
                new RoleDefinition[_nodeIds.size()]));
        }
    }
    
    // NOTE: This should only be called from JobSynchronizerRunnable.
    private void sync() throws JobException {
        // Build a map of the defined job list.  This is the list of jobs from
        // the job_definition table and could be different than the current job list.
        List<JobDefinition> definedJobList = getDefinedJobs();
        Map<String, JobDefinition> definedJobMap = new HashMap<String, JobDefinition>();
        for (JobDefinition job : definedJobList) {
            definedJobMap.put(job.getJobId(), job);
        }
        
        try {
            MocaUtils.currentContext().commit();
        }
        catch (MocaException e) {
            throw new JobException("Unable to commit transaction: " + e);
        }    
        
        // Build a map of the current job list.  This is the list of jobs that the
        // Job Manager is currently managing.  Any differences between this list and
        // the defined job list will be applied to this list.
        Map<String, JobDefinition> currentJobMap = new HashMap<String, JobDefinition>();
        
        List<JobDefinition> currentJobList = getCurrentJobs();
        for (JobDefinition job : currentJobList) {
            currentJobMap.put(job.getJobId(), job);
        }
        
        for (Map.Entry<String, JobDefinition> definedJobEntry : definedJobMap.entrySet()) {
            String definedJobId = definedJobEntry.getKey();
            JobDefinition definedJob = definedJobEntry.getValue();
            JobDefinition currentJob = currentJobMap.get(definedJobId);
            
            // Check for any jobs that were added.  Jobs that were added will
            // exist in the defined job list, but not in the current job list.
            if (currentJob == null) {
                _logger.debug(MocaUtils.concat("Adding job ", definedJobId));
                
                // Add this job to the current jobs list.
                addJob(definedJob);
                
                // If scheduling isn't disabled and this job is enabled add it to the job scheduler.
                if (!_disableScheduling) {
                    if (definedJob.isEnabled()) {
                        try {
                            // Make sure we quarantine any new jobs.
                            scheduleJob(definedJob, _syncSeconds * 2);
                        }
                        catch (JobRuntimeException e) {
                            _logger.error("There was a problem starting job [" + 
                                    definedJob.getJobId() + "]", e);
                        }
                    }
                }
            }
            
            // Check for any jobs that were changed.  Jobs that were changed will
            // exist in both the current and defined job lists, but will not be equal.
            else if (!jobsAreEqual(definedJob, currentJob)) {
                _logger.debug(MocaUtils.concat("Updating job ", definedJobId));
                
                // Remove this job from the job scheduler.
                unscheduleJob(currentJob);

                // Add this job to the current jobs list.
                addJob(definedJob);
                
                // If scheduling isn't disabled and this job is enabled add it to the job scheduler.
                if (!_disableScheduling) {
                    if (definedJob.isEnabled()) {
                        try {
                            // Make sure we quarantine any new jobs.
                            scheduleJob(definedJob, _syncSeconds * 2);
                        }
                        catch (JobRuntimeException e) {
                            _logger.error("There was a problem starting job [" + 
                                    definedJob.getJobId() + "]", e);
                        }
                    }
                }
            }  
        }
        
        // Check for any jobs that were removed.  Jobs that were removed
        // will exist in the current job list, but not the defined job list.
        for (Entry<String, JobDefinition>  currentJobEntry : currentJobMap.entrySet()) {
            String currentJobId = currentJobEntry.getKey();
            JobDefinition currentJob = currentJobEntry.getValue();
            
            if (!definedJobMap.containsKey(currentJobId)) {
                _logger.debug(MocaUtils.concat("Removing job ", currentJobId));
                
                // Remove this job from the job scheduler.
                unscheduleJob(currentJob);
                
                // Remove this job from the current jobs list.
                removeJob(currentJob);
            }
        }
        
        return; 
    }
    
    private boolean jobsAreEqual(JobDefinition job1, JobDefinition job2) {    
        if (job1 == job2) return true;
        
        if (!valuesAreEqual(job1.getJobId(),       job2.getJobId()))       return false;
        if (!valuesAreEqual(job1.getRole(),      job2.getRole()))      return false;
        if (!valuesAreEqual(job1.getName(),        job2.getName()))        return false;
        if (!valuesAreEqual(job1.getCommand(),     job2.getCommand()))     return false;
        if (!valuesAreEqual(job1.getTimer(),       job2.getTimer()))       return false;
        if (!valuesAreEqual(job1.getSchedule(),    job2.getSchedule()))    return false;
        if (!valuesAreEqual(job1.getStartDelay(),  job2.getStartDelay()))  return false;
        if (!valuesAreEqual(job1.getLogFile(),     job2.getLogFile()))     return false;
        if (!valuesAreEqual(job1.getTraceLevel(),  job2.getTraceLevel()))  return false;
        if (!valuesAreEqual(job1.getEnvironment(), job2.getEnvironment())) return false;
        if (!valuesAreEqual(job1.isOverlap(),      job2.isOverlap()))      return false;
        if (!valuesAreEqual(job1.isEnabled(),      job2.isEnabled()))      return false;
        
        return true;
    }
    
    private boolean valuesAreEqual(Object value1, Object value2) {
        if (value1 != null) {
            if (!value1.equals(value2)) {
                return false;
            }
        } 
        else if (value2 != null) {
            return false;
        }
        
        return true;
    }
    
    private void addJob(JobDefinition job) {
        JobDefinition oldJob = _currentJobs.put(job.getJobId(), job);
        if (oldJob != null) {
            notifyJobChanged(oldJob, job);
        }
        else {
            notifyJobAdded(job);
        } 
    }
    
    private void removeJob(JobDefinition job) {
        _currentJobs.remove(job.getJobId());
        notifyJobRemoved(job);
    }
    
    private void scheduleJob(JobDefinition job) throws JobRuntimeException, JobException {
        scheduleJob(job, null);
    }
    private void scheduleJob(JobDefinition job, Integer quartineTime) throws JobRuntimeException, JobException {
        try {
            if (quartineTime == null) {
                _jobScheduler.add(job);
            }
            else {
                _jobScheduler.add(job, quartineTime);
            }
            
            notifyJobScheduled(job);
        }
        catch (ParseException ignore) {
            // The configured job schedule was invalid and wasn't able to be parsed
            // An error message for this has already been logged but
            // we don't want the exception to bubble up.
        }  
    }
    
    private void unscheduleJob(JobDefinition job) throws JobException {
        _jobScheduler.remove(job);
        notifyJobUnscheduled(job);
    }
    
    private void notifyStarted() {
        // Don't fire this trigger during restarts
        if (_restarting) return;
        
        for (JobManagerListener listener : _listeners) {
            listener.onStart(this);
        }
    }
    
    private void notifyStopped() {
        // Don't fire this trigger during restarts
        if (_restarting) return;
        
        for (JobManagerListener listener : _listeners) {
            listener.onStop(this);
        }
    }
    
    private void notifyRestarted(Map<String, JobDefinition> oldJobs) {
        // A restart effectively causes a sync so we need to see
        // what changed since then.
        // Handle notifications for removed/changed jobs
        for (JobDefinition jobDef : oldJobs.values()) {
            JobDefinition currentJob = null;
            // Check if a task was removed
            if ((currentJob = _currentJobs.get(jobDef.getJobId())) == null) {
                notifyJobRemoved(jobDef);
            }
            // Check if the job changed
            else if (!jobsAreEqual(jobDef, currentJob)) {
                notifyJobChanged(jobDef, currentJob);
            }
        }
        
        // Handle notifications for added jobs
        for (JobDefinition jobDef : _currentJobs.values()) {
           // If the job wasn't present before then it was added
           if (!oldJobs.containsKey(jobDef.getJobId())) {
               notifyJobAdded(jobDef);
           } 
           
           try {
               // Notify for any jobs that were scheduled
               if (this.isScheduled(jobDef.getJobId())) {
                   notifyJobScheduled(jobDef);
               }
           }
           catch (JobException e) {
               _logger.error("Job Exception while checking for scheduled jobs after restart: " + e);
           }
        }
        
        // Finally notify that the job manager was restarted
        for (JobManagerListener listener : _listeners) {
            listener.onRestart(this);
        }
    }
    
    private void notifyJobAdded(JobDefinition job) {
        // Don't fire this trigger during restarts
        if (_restarting) return;
        
        for (JobManagerListener listener : _listeners) {
            listener.onJobAdded(this, job);
        }
    }
    
    private void notifyJobRemoved(JobDefinition job) {
        for (JobManagerListener listener : _listeners) {
            listener.onJobRemoved(this, job);
        }
    }
    
    private void notifyJobChanged(JobDefinition oldJob, JobDefinition newJob) {
        for (JobManagerListener listener : _listeners) {
            listener.onJobChanged(this, oldJob, newJob);
        }
    }
    
    private void notifyJobScheduled(JobDefinition job) {
        // Don't fire this trigger during restarts
        if (_restarting) return;
        
        for (JobManagerListener listener : _listeners) {
            listener.onJobScheduled(this, job);
        }
        
        // Populate the Job Cache with the Job entry.
        addToClusterCache(job);
    }
    
    private void notifyJobUnscheduled(JobDefinition job) {
        for (JobManagerListener listener : _listeners) {
            listener.onJobUnscheduled(this, job);
        }
        
        // Remove Job entry from the Job Cache.
        removeFromClusterCache(job);
    }
    
    // @see com.redprairie.moca.cluster.ClusterRoleAware#activateRole(java.lang.String)
    @Override
    public void activateRole(RoleDefinition role) {
        _syncLock.lock();
        try {
            _nodeIds.add(role);
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    // @see com.redprairie.moca.cluster.ClusterRoleAware#deactivateRole(java.lang.String)
    @Override
    public void deactivateRole(RoleDefinition role) {
        _syncLock.lock();
        try {
            _nodeIds.remove(role);
        }
        finally {
            _syncLock.unlock();
        }
    }
    
    // @see com.redprairie.moca.cluster.ClusterRoleAware#noCluster()
    @Override
    public void noCluster() {
        _clusterDeactivated.set(true);
    }
    
    private class JobSynchronizerRunnable implements Runnable {
        public void run() {
            _syncLock.lock();
            try {
                sync();
            }
            catch (MocaInterruptedException e) {
                _logger.info("JobSynchronizer Interrupted: " + e);
            }
            catch (JobException e) {
                _logger.error("Unable to resync jobs: " + e);
            }
            finally {
                ServerUtils.getCurrentContext().close();
                ServerUtils.removeCurrentContext();
                _syncLock.unlock();
            }
        }
    }

	// @see com.redprairie.moca.cluster.ClusterRoleAware#handleMerge()

	@Override
	public void handleMerge(List<Node> members, Node local) {
		if (!_disableScheduling) {
			replicate();
		}
	}

    /**
     * This lock is to be acquired after the startStopLock if applicable or
     * first if doing a sync operation
     */
    private final Lock _syncLock = new ReentrantLock();
    private final JobDefinitionDAO _dao;
    private final Set<RoleDefinition> _nodeIds = new HashSet<RoleDefinition>();
    private final boolean _disableScheduling;
    private final JobScheduler _jobScheduler;
    private final int _syncSeconds;
    private final ClusterInformation _clusterInfo;
    private final List<JobManagerListener> _listeners = new CopyOnWriteArrayList<JobManagerListener>();
    
    private final ScheduledExecutorService _scheduler = Executors.newSingleThreadScheduledExecutor(
            new DaemonThreadFactory("MOCA-JobSynchronizerThread", false));
    private ScheduledFuture<?> _jobSynchronizerHandle;
    
    private Map<String, JobDefinition> _currentJobs;
    private AtomicBoolean _clusterDeactivated = new AtomicBoolean(false);
    private boolean _restarting;
    
    private static final Logger _logger = LogManager.getLogger(JobManager.class);
}