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

package com.redprairie.moca.task;

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
 * This class is a bean representing a task definition as it is in a MOCA system.
 * This class is to only contain simple getter setters and nothing more as it is
 * a POJO.
 * 
 * Copyright (c) 2009 RedPrairie Corporation All Rights Reserved
 * 
 * @author dinksett
 * @author wburns
 */
@Entity
@Table(name="task_definition")
public class TaskDefinition implements Serializable {
    private static final long serialVersionUID = 2576244872089112849L;
    
    public static final String PROCESS_TASK = "P";
    public static final String THREAD_TASK = "T";
    public static final String DAEMON_TASK = "D";
    
    /**
     * @return Returns the taskId.
     */
    @Id
    @Column(name="task_id")
    public String getTaskId() {
        return _taskId;
    }

    /**
     * @param taskId The taskId to set.
     */
    public void setTaskId(String taskId) {
        this._taskId = taskId;
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
    @Column(name="cmd_line")
    public String getCmdLine() {
        return _cmdLine;
    }
    /**
     * @param cmdline The cmdline to set.
     */
    public void setCmdLine(String cmdLine) {
        _cmdLine = cmdLine;
    }
    /**
     * @return Returns the runDirectory.
     */
    @Column(name="run_dir")
    public String getRunDirectory() {
        return _runDirectory;
    }
    /**
     * @param runDirectory The runDirectory to set.
     */
    public void setRunDirectory(String runDirectory) {
        _runDirectory = runDirectory;
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
     * @return Returns the trace level.
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
     * @return Returns the restart.
     */
    @Column(name="restart")
    public boolean isRestart() {
        return _restart;
    }
    /**
     * @param restart The restart to set.
     */
    public void setRestart(boolean restart) {
        _restart = restart;
    }
    
    /**
     * @return Returns the autoStart.
     */
    @Column(name="auto_start")
    public boolean isAutoStart() {
        return _autoStart;
    }
    
    /**
     * @param autoStart The autoStart to set.
     */
    public void setAutoStart(boolean autoStart) {
        _autoStart = autoStart;
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
     * @return Returns the type.
     */
    @Column(name="task_typ")
    public String getType() {
        return _type;
    }
    
    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        _type = type;
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
        _groupName = groupName;
    }
    
    /**
     * @return Returns the environment.
     */
    @ElementCollection
    @JoinTable(name="task_env_definition", joinColumns={@JoinColumn(name="task_id")})
    @MapKeyColumn(name="name")
    @Column(name="value")
    @Fetch(FetchMode.JOIN)
    public Map<String, String> getEnvironment() {
        return _environment;
    }

    /**
     * @param environment The environment to set.
     */
    public void setEnvironment(Map<String, String> environment) {
        _environment = environment;
    }

    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_taskId == null) ? 0 : 
            _taskId.hashCode());
        return result;
    }

    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TaskDefinition other = (TaskDefinition) obj;
        if (_taskId == null || other._taskId == null) {
            return false;
        }
        else if (!_taskId.equals(other._taskId)) return false;
        return true;
    }

    // @see java.lang.Object#toString()
	
	@Override
	public String toString() {
		return _taskId;
	}

	//
    // Implementation
    //
    private String _taskId;
    private RoleDefinition _role;
    private String _name;
    private String _cmdLine;
    private String _runDirectory;
    private String _logFile;
    private String _traceLevel;
    private String _type;
    private String _groupName;
    private boolean _restart;
    private boolean _autoStart;
    private Integer _startDelay;
    private Map<String, String> _environment;
}
