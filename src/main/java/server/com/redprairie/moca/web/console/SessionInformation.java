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

package com.redprairie.moca.web.console;

import java.util.Date;
import java.util.Map;

import com.redprairie.moca.MocaConstants;
import com.redprairie.moca.advice.ServerContextAdministrationBean;
import com.redprairie.moca.advice.SessionAdministrationBean;
import com.redprairie.moca.server.exec.SessionType;

public class SessionInformation {
    public SessionInformation(String sessionName, String threadId,
            SessionAdministrationBean bean, boolean actualSession) {
        _name = sessionName;
        _threadId = threadId;
        _lastCommand = bean.getLastCommand();
        _lastCommandTime = bean.getLastCommandTime();
        _lastSQL = bean.getLastSqlStatement();
        _lastSQLTime = bean.getLastSqlStatementTime();
        _lastScript = bean.getLastScript();
        _lastScriptTime = bean.getLastScriptTime();
        _connectedIpAddress = bean.getConnectedIpAddress();
        _startedTime = bean.getStartedTime();
        _traceName = bean.getTraceFile();
        _isSession = actualSession;
        _sessionType = bean.getSessionType();
        _environment = null;
    }
    
    public SessionInformation(String sessionName, String threadId,
        ServerContextAdministrationBean bean, boolean actualSession) {
        _name = sessionName;
        _threadId = threadId;
        _lastCommand = bean.getLastCommand();
        _lastCommandTime = bean.getLastCommandTime();
        _lastSQL = bean.getLastSqlStatement();
        _lastSQLTime = bean.getLastSqlStatementTime();
        _lastScript = bean.getLastScript();
        _lastScriptTime = bean.getLastScriptTime();
        _connectedIpAddress = bean.getConnectedIpAddress();
        _startedTime = bean.getStartedTime();
        _traceName = bean.getTraceFile();
        _isSession = actualSession;
        _sessionType = bean.getSessionType();
        
        _environment = bean.getRequestEnvironment();
        _environment.remove(MocaConstants.WEB_CLIENT_ADDR);
        _environment.remove("WEB_SESSIONID");
    }

    private String _replaceHtmlEntities(String input) {
        // String output = input.replaceAll("[{]", "\\{").replaceAll("[}]",
        // "\\}");
        // return output;
        return input;
    }

    /**
     * @return Returns the name.
     */
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
     * @return Returns the lastCommand.
     */
    public String getLastCommand() {
        return _replaceHtmlEntities(_lastCommand);
    }

    /**
     * @param lastCommand The lastCommand to set.
     */
    public void setLastCommand(String lastCommand) {
        _lastCommand = lastCommand;
    }

    /**
     * @return Returns the lastCommandTime.
     */
    public Date getLastCommandTime() {
        if (_lastCommandTime != null) {
            return new Date(_lastCommandTime.getTime());
        }
        return null;
    }

    /**
     * @param lastCommandTime The lastCommandTime to set.
     */
    public void setLastCommandTime(Date lastCommandTime) {
        _lastCommandTime = new Date(lastCommandTime.getTime());
    }

    /**
     * @return Returns the lastScript.
     */
    public String getLastScript() {
        return _replaceHtmlEntities(_lastScript);
    }

    /**
     * @param lastScript The lastScript to set.
     */
    public void setLastScript(String lastScript) {
        _lastScript = lastScript;
    }

    /**
     * @return Returns the lastScriptTime.
     */
    public Date getLastScriptTime() {
        if (_lastScriptTime != null) {
            return new Date(_lastScriptTime.getTime());
        }
        return null;
    }

    /**
     * @param lastScriptTime The lastScriptTime to set.
     */
    public void setLastScriptTime(Date lastScriptTime) {
        _lastScriptTime = new Date(lastScriptTime.getTime());
    }

    /**
     * @return Returns the lastSQL.
     */
    public String getLastSQL() {
        return _replaceHtmlEntities(_lastSQL);
    }

    /**
     * @param lastSQL The lastSQL to set.
     */
    public void setLastSQL(String lastSQL) {
        _lastSQL = lastSQL;
    }

    /**
     * @return Returns the lastSQLTime.
     */
    public Date getLastSQLTime() {
        if (_lastSQLTime != null) {
            return new Date(_lastSQLTime.getTime());
        }
        return null;
    }

    /**
     * @param lastSQLTime The lastSQLTime to set.
     */
    public void setLastSQLTime(Date lastSQLTime) {
        _lastSQLTime = new Date(lastSQLTime.getTime());
    }

    /**
     * @return Returns the status.
     */
    public String getStatus() {
        return String.valueOf(_status);
    }

    /**
     * @param status The status to set.
     */
    public void setStatus(String status) {
        _status = status;
    }

    /**
     * @return Returns the threadId.
     */
    public String getThreadId() {
        return _threadId;
    }

    /**
     * @param threadId The threadId to set.
     */
    public void setThreadId(String threadId) {
        _threadId = threadId;
    }

    /**
     * @return Returns the connectedIpAddress.
     */
    public String getConnectedIpAddress() {
        return _connectedIpAddress;
    }

    /**
     * @param connectedIpAddress The connectedIpAddress to set.
     */
    public void setConnectedIpAddress(String connectedIpAddress) {
        _connectedIpAddress = connectedIpAddress;
    }
    
    /**
     * @return Returns the commandPath.
     */
    public synchronized String getCommandPath() {
        return _commandPath;
    }

    /**
     * @param commandPath The commandPath to set.
     */
    public synchronized void setCommandPath(String commandPath) {
        _commandPath = commandPath;
    }
    
    /**
     * @return Returns the startedTime.
     */
    public Date getStartedTime() {
        return new Date(_startedTime.getTime());
    }
    
    /**
     * @return traceName The name of the trace file if it is enabled or null
     *         if it is not enabled
     */
    public String getTraceName() {
        return _traceName;
    }
    
    /**
     * @return isSession Whether this is a session or thread
     */
    public boolean isSession() {
        return _isSession;
    }
    
    /**
     * @return sessionType The session type for this session
     */
    public String getSessionType() {
        return String.valueOf(_sessionType);
    }
    
    public Map<String, String> getEnvironment() {
        return _environment;
    }

    private String _name;
    private String _threadId;
    private String _lastCommand;
    private Date _lastCommandTime;
    private String _lastScript;
    private Date _lastScriptTime;
    private String _lastSQL;
    private Date _lastSQLTime;
    private String _status = "";
    private String _connectedIpAddress;
    private String _commandPath;
    private final Date _startedTime;
    private final String _traceName;
    private final boolean _isSession;
    private final SessionType _sessionType;
    private final Map<String, String> _environment;
}
