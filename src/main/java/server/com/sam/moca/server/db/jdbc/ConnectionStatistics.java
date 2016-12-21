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

package com.sam.moca.server.db.jdbc;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import com.sam.moca.server.profile.CommandPath;
import com.sam.util.Pair;

/**
 * Database connection statistics
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class ConnectionStatistics {
    
    public ConnectionStatistics(String connectionIdentifier) {
        _id = connectionIdentifier;
    }
    
    /**
     * @return Returns the _thread.
     */
    synchronized 
    public Thread getThread() {
        return _thread;
    }
    /**
     * @param thread The _thread to set.
     */
    synchronized 
    public void setThread(Thread thread) {
        this._thread = thread;
    }
    
    /**
     * @param sql
     */
    synchronized 
    public void setLastSQL(String sql) {
        _lastSQL = sql;
        _lastSQLTime = new Date();
        _sqlCount.incrementAndGet();
    }
    
    /**
     * @return A pair object holding the last sql executed as the first value
     *         and the date when the sql was executed as the second value.
     */
    synchronized 
    public Pair<String, Date> getLastSQL() {
        Date date = null;
        if (_lastSQLTime != null) {
            date = new Date(_lastSQLTime.getTime());
        }
        return new Pair<String, Date>(_lastSQL, date);
    }
    /**
     * @return The amount of times a sql statement was executed for this
     *         transaction
     */
    public int getSQLStatementCount() {
        return _sqlCount.get();
    }
    
    public String getConnectionIdentifier() {
        return _id;
    }
    
    synchronized
    public void setLastCommandPath(CommandPath commandPath) {
        _lastCommandPath = commandPath;
    }
    
    synchronized
    public CommandPath getLastCommandPath() {
        return _lastCommandPath;
    }
    
    private final String _id;
    
    private final AtomicInteger _sqlCount = new AtomicInteger();
    private Thread _thread;
    private String _lastSQL;
    private Date _lastSQLTime;
    private CommandPath _lastCommandPath;
}
