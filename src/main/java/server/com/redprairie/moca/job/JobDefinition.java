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

package com.redprairie.moca.job;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.redprairie.moca.cluster.RoleDefinition;

/**
 * This class is a bean representing a job definition as it is in a MOCA system.
 * This class is to only contain simple getter setters and nothing more as it is
 * a POJO.
 * 
 * Copyright (c) 2009 RedPrairie Corporation All Rights Reserved
 * 
 * @author dinksett
 * @author wburns
 */
@Entity
@Table(name="job_definition")
public class JobDefinition implements Serializable {
    private static final long serialVersionUID = 9000817023747525416L;
    
    /**
     * @return Returns the jobId.
     */
    @Id
    @Column(name="job_id")
    public String getJobId() {
        return _jobId;
    }

    /**
     * @param jobId The taskId to set.
     */
    public void setJobId(String jobId) {
        this._jobId = jobId;
    }
    
    /**
     * @return Returns the role.
     */
    @ManyToOne( cascade = {CascadeType.PERSIST, CascadeType.MERGE} )
    @JoinColumn(name="role_id")
    public RoleDefinition getRole() {
        return _role;
    }
    
    /**
     * @param role The role to set.
     */
    public void setRole(RoleDefinition role) {
        _role = role;
    }
    
    /**
     * @return Returns the name.
     */
    @Column(name="name")
    public String getName() {
        return _name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        _name = name;
    }
    /**
     * @return Returns the cmdline.
     */
    @Column(name="command")
    public String getCommand() {
        return _command;
    }
    /**
     * @param cmdline The cmdline to set.
     */
    public void setCommand(String command) {
        _command = command;
    }
    
    /**
     * @return Returns the logFile.
     */
    @Column(name="log_file")
    public String getLogFile() {
        return _logFile;
    }
    /**
     * @param logFile The logFile to set.
     */
    public void setLogFile(String logFile) {
        _logFile = logFile;
    }
    
    /**
     * @return Returns the traceLevel.
     */
    @Column(name="trace_level")
    public String getTraceLevel() {
        return _traceLevel;
    }
    /**
     * @param traceLevel The traceLevel to set.
     */
    public void setTraceLevel(String traceLevel) {
        _traceLevel = traceLevel;
    }

    /**
     * @return Returns the schedule.
     */
    @Column(name="schedule")
    public String getSchedule() {
        return _schedule;
    }

    /**
     * @param schedule The schedule to set.
     */
    public void setSchedule(String schedule) {
        _schedule = schedule;
    }

    /**
     * @return Returns the timer.
     */
    @Column(name="timer")
    public Integer getTimer() {
        return _timer;
    }

    /**
     * @param timer The timer to set.
     */
    public void setTimer(Integer timer) {
        _timer = timer;
    }

    /**
     * @return Returns the startDelay.
     */
    @Column(name="start_delay")
    public Integer getStartDelay() {
        return _startDelay;
    }

    /**
     * @param startDelay The startDelay to set.
     */
    public void setStartDelay(Integer startDelay) {
        _startDelay = startDelay;
    }

    /**
     * @return Returns the overlap.
     */
    @Column(name="overlap")
    public boolean isOverlap() {
        return _overlap;
    }

    /**
     * @param overlap The overlap to set.
     */
    public void setOverlap(boolean overlap) {
        _overlap = overlap;
    }
    
    /**
     * @return Returns the enabled.
     */
    @Column(name="enabled")
    public boolean isEnabled() {
        return _enabled;
    }
    
    /**
     * @param enabled The enabled to set.
     */
    public void setEnabled(boolean enabled) {
        _enabled = enabled;
    }
    
    /**
     * @return Returns the type.
     */
    @Column(name="type")
    public String getType() {
        return _type;
    }

    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this._type = type;
    }

    /**
     * @return Returns the grp_nam. 
     */ 
    @Column(name="grp_nam") 
    public String getGroupName() { 
        return _groupName; 
    } 
 
    /** 
     * @param grp_nam The grp_nam to set. 
     */ 
    public void setGroupName(String groupName) { 
        this._groupName = groupName; 
    }

    /**
     * @return Returns the _environment.
     */
    @ElementCollection
    @JoinTable(name="job_env_definition", joinColumns={@JoinColumn(name="job_id")})
    @MapKeyColumn(name="name")
    @Column(name="value")
    @Fetch(FetchMode.JOIN)
    public Map<String, String> getEnvironment() {
        return _environment;
    }

    /**
     * @param _environment The _environment to set.
     */
    public void setEnvironment(Map<String, String> environment) {
        _environment = environment;
    }

    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_jobId == null) ? 0 : _jobId.hashCode());
        return result;
    }

    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;    
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        JobDefinition other = (JobDefinition) obj;
        if (_jobId == null || other._jobId == null) {
            return false;
        }
        else if (!_jobId.equals(other._jobId)) {
            return false;
        }
        
        return true;
    }
    
    // @see java.lang.Object#toString()
	
	@Override
	public String toString() {
		return _jobId;
	}

	private String _jobId;
    private RoleDefinition _role;
    private String _name;
    private String _command;
    private String _logFile;
    private String _traceLevel;
    private String _schedule;
    private Integer _timer;
    private Integer _startDelay;
    private boolean _overlap;
    private boolean _enabled;
    private String _type;
    private String _groupName;
    private Map<String, String> _environment;
}
