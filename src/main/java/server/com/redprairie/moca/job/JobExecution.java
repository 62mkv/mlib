/*
 *  $URL$
 *  $Author$
 *  $Date$
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

package com.redprairie.moca.job;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.redprairie.moca.server.InstanceUrl;

/**
 * This class is a bean representing a job execution.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@Entity(name="com.redprairie.moca.task.JobExecution")
@Table(name="job_definition_exec")
public class JobExecution implements Serializable {
    private static final long serialVersionUID = -5239728152647372370L;
    
    public JobExecution(JobDefinition job, InstanceUrl nodeUrl) {
        _jobDef = job;
        long time = System.currentTimeMillis();
        _startDate = new Date(time - time % 1000);
        _nodeUrl = nodeUrl.toString();
    }
    
    @SuppressWarnings("unused")
    private JobExecution() {
    }
    
    /**
     * @return Returns the jobDef.
     */
    @Id
    @OneToOne
    @JoinColumn(name="job_id")
    public JobDefinition getJobDef() {
        return _jobDef;
    }

    /**
     * @param jobDef The jobDef to set.
     */
    void setJobDef(JobDefinition jobDef) {
        _jobDef = jobDef;
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
    public Integer getStatus() {
        return _status;
    }
    /**
     * @param status The status to set.
     */
    public void setStatus(Integer status) {
        _status = status;
    }
    
    /**
     * @return Returns the message.
     */
    @Column(name="message")
    String getMessage() {
        return _message;
    }

    /**
     * @param message The message to set.
     */
    void setMessage(String message) {
        _message = message;
    }

    // @see java.lang.Object#hashCode()
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_jobDef == null) ? 0 : _jobDef.hashCode());
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
        JobExecution other = (JobExecution) obj;
        if (_jobDef == null) {
            if (other._jobDef != null) return false;
        }
        else if (!_jobDef.equals(other._jobDef)) return false;
        if (_startDate == null) {
            if (other._startDate != null) return false;
        }
        else if (!_startDate.equals(other._startDate)) return false;
        return true;
    }
    
    private JobDefinition _jobDef;
    private String _nodeUrl;
    private Date _startDate;
    private Date _endDate;
    private Integer _status;
    private String _message;
}
