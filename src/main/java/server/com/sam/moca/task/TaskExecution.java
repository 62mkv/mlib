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

package com.sam.moca.task;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.sam.moca.server.InstanceUrl;

/**
 * This class is a bean representing a task execution.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@Entity(name="com.sam.moca.task.TaskExecution")
@Table(name="task_definition_exec")
public class TaskExecution implements Serializable {
    private static final long serialVersionUID = -5239728152647372370L;
    
    public TaskExecution(TaskDefinition task, InstanceUrl nodeUrl) {
        _taskDef = task;
        setStartDate(new Date());
        _nodeUrl = nodeUrl.toString();
    }
    
    public TaskExecution(TaskDefinition task, Date startDate, InstanceUrl nodeUrl) {
        _taskDef = task;
        setStartDate(startDate);
        _nodeUrl = nodeUrl.toString();
    }
    
    @SuppressWarnings("unused")
    private TaskExecution() {
    }
    
    /**
     * @return Returns the taskDef.
     */
    @Id
    @OneToOne
    @JoinColumn(name="task_id")
    public TaskDefinition getTaskDef() {
        return _taskDef;
    }

    /**
     * @param taskDef The taskDef to set.
     */
    void setTaskDef(TaskDefinition taskDef) {
        _taskDef = taskDef;
    }

    /**
     * @return Returns the startDate.
     */
    @Id
    @Column(name="start_dte")
    public Date getStartDate() {
        return new Date(_startDate.getTime());
    }

    /**
     * @param startDate The startDate to set.
     */
    void setStartDate(Date startDate) {
        // Bad hack to get oracle to work since we don't store milliseconds
        if (startDate != null) {
            long time = startDate.getTime();
            _startDate = new Date(time - time % 1000);
        }
        else {
            _startDate = null;
        }
    }

    /**
     * @return Returns the nodeUrl.
     */
    @Id
    @Column(name="node_url")
    public String getNodeUrl() {
        return _nodeUrl;
    }
    /**
     * @param nodeUrl The nodeUrl to set.
     */
    void setNodeUrl(String nodeUrl) {
        _nodeUrl = nodeUrl;
    }
    /**
     * @return Returns the endDate.
     */
    @Column(name="end_dte")
    public Date getEndDate() {
        return _endDate;
    }
    /**
     * @param endDate The endDate to set.
     */
    public void setEndDate(Date endDate) {
        // Bad hack to get oracle to work since we don't store milliseconds
        if (endDate != null) {
            long time = endDate.getTime();
            _endDate = new Date(time - time % 1000);
        }
        else {
            _endDate = null;
        }
    }
    /**
     * @return Returns the status.
     */
    @Column(name="status")
    public String getStatus() {
        return _status;
    }
    /**
     * @param status The status to set.
     */
    public void setStatus(String status) {
        _status = status;
    }
    
    /**
     * @return Returns the message.
     */
    @Column(name="start_cause")
    String getStartCause() {
        return _startCause;
    }

    /**
     * @param startCause The message to set.
     */
    void setStartCause(String startCause) {
        _startCause = startCause;
    }

    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_taskDef == null) ? 0 : _taskDef.hashCode());
        result = prime * result
                + ((_startDate == null) ? 0 : _startDate.hashCode());
        return result;
    }
    
    // @see java.lang.Object#equals(java.lang.Object)
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        TaskExecution other = (TaskExecution) obj;
        if (_taskDef == null) {
            if (other._taskDef != null) return false;
        }
        else if (!_taskDef.equals(other._taskDef)) return false;
        if (_startDate == null) {
            if (other._startDate != null) return false;
        }
        else if (!_startDate.equals(other._startDate)) return false;
        return true;
    }
    
    private TaskDefinition _taskDef;
    private String _nodeUrl;
    private Date _startDate;
    private Date _endDate;
    private String _status;
    private String _startCause;
}
