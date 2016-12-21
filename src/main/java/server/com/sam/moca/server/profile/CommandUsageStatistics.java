/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.sam.moca.server.profile;

import com.sam.moca.MocaColumn;

/**
 * 
 * Statistics gathered through command usage logging.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class CommandUsageStatistics implements Cloneable {
    public CommandUsageStatistics(CommandPathElement cmd, CommandPath path) {
        _levelName = cmd.getGroup();
        _commandName = cmd.getName();
        _commandType = cmd.getType();
        _commandPath = path;
    }
    
    /**
     * This contstructor is only used for reconstructing a CommandUsageStatistics
     * object from persistent storage.
     */
    CommandUsageStatistics(String levelName, String commandName,
            String commandType, String commandPath,
            int count, int totalTime, int minTime, int maxTime) {
        _levelName = levelName;
        _commandName = commandName;
        _commandPath = commandPath;
        _commandType = commandType;
        _count = count;
        _totalTime = (long)totalTime * 1000000L;
        _minTime = (long)minTime * 1000000L;
        _maxTime = (long)maxTime * 1000000L;
        
    }

    @MocaColumn(name="component_level", order=1)
    public String getLevelName() {
        return _levelName;
    }

    @MocaColumn(name="command", order=2)
    public String getCommandName() {
        return _commandName;
    }
    
    @MocaColumn(name="type", order=3)
    public String getCommandType() {
        return _commandType;
    }

    @MocaColumn(name="command_path", order=4)
    public String getPath() {
        return _commandPath == null ? null : _commandPath.toString();
    }

    @MocaColumn(name="execution_count", order=5)
    synchronized
    public int getCount() {
        return _count;
    }

    @MocaColumn(name="min_ms", order=6)
    synchronized
    public int getMinTime() {
        return (int) (_minTime / 1000000L);
    }

    @MocaColumn(name="max_ms", order=7)
    synchronized
    public int getMaxTime() {
        return (int) (_maxTime / 1000000L);
    }
    
    @MocaColumn(name="avg_ms", order=8)
    synchronized
    public double getAvgTime() {
        return (_totalTime / (double) _count) / 1000000.0;
    }
    
    @MocaColumn(name="total_ms", order=9)
    synchronized
    public int getTotalTime() {
        return (int) (_totalTime/1000000L);
    }
    
    @MocaColumn(name="self_ms", order=10)
    synchronized
    public int getSelfTime() {
        return (int) ((_totalTime - _childTime)/1000000L);
    }
    
    @MocaColumn(name="avg_self_ms", order=11)
    synchronized
    public double getAvgSelfTime() {
        return ((_totalTime - _childTime) / (double) _count) / 1000000.0;
    }
    
    synchronized
    public long nanos() {
        return _totalTime;
    }
    
    synchronized
    public void setChildTime(long childTime) {
        _childTime = childTime;
    }
    
    synchronized
    void log(long nanos) {
        _count++;
        _totalTime += nanos;
        if (_minTime == 0 || _minTime > nanos) {
            _minTime = nanos;
        }
        if (_maxTime < nanos) {
            _maxTime = nanos;
        }
    }
    
    // @see java.lang.Object#clone()
    @Override
    synchronized
    protected Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }
    
    //
    // Implementation
    //
    private final String _levelName;
    private final String _commandName;
    private final Object _commandPath;
    private final String _commandType;
    private int _count;
    private long _totalTime;
    private long _minTime;
    private long _maxTime;
    private long _childTime;
}