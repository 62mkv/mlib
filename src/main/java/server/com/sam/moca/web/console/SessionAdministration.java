/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.sam.moca.web.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.sam.moca.advice.ServerContextAdministrationBean;
import com.sam.moca.advice.SessionAdministrationBean;
import com.sam.moca.advice.SessionAdministrationManagerBean;
import com.sam.moca.server.ServerUtils;

/**
 * This class defines the various session console request implementations.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SessionAdministration {
    
    private static final Logger LOG = LogManager.getLogger(SessionAdministration.class);
    
    public static List<SessionInformation> getSessionInformation() {
        List<SessionInformation> results = new ArrayList<SessionInformation>();
        SessionAdministrationManagerBean manager = 
            ServerUtils.globalAttribute(SessionAdministrationManagerBean.class);
        
        SessionAdministrationBean[] sessions = manager.getSessions();

        for (SessionAdministrationBean session : sessions) {
            String name = session.getSessionName();
            
            // We skip all the console threads
            if (name.equals("ConsoleRequest")) {
                continue;
            }
            
            Map<Long, ServerContextAdministrationBean> threadSessions = 
                manager.getSessionBeans(session);
            
            if(threadSessions == null) {
                LOG.warn("Session does not have any thread sessions available."
                        + " This could be due to not closing a transaction properly."
                        + " Session name : [ " + session.getSessionName()
                        + " ], LastCommand : [ " + session.getLastCommand() + " ]");
            } 
            else {
                boolean isActive = false;
                for (Entry<Long, ServerContextAdministrationBean> entry : 
                    threadSessions.entrySet()) {
                    
                    String threadId = String.valueOf(entry.getKey());
                    
                    isActive = true;
                    
                    ServerContextAdministrationBean bean = entry.getValue();
                    
                    SessionInformation sessionInformation = new SessionInformation(
                        name, threadId, bean, false);
                    sessionInformation.setStatus(bean.getStatus().toString());
                    // CommandPath can be null when a Server thread is inactive.
                    // This is most likely from an RMI thread.
                    Object commandPath = bean.getCurrentCommandPath();
                    if (commandPath != null) {
                        sessionInformation.setCommandPath(commandPath.toString());
                    }
                    
                    // add the session state to the result set
                    results.add(sessionInformation);
                }
                
                // If it wasn't active and we show all sessions that show that
                // one as well
                if (!isActive) {
                
                    SessionInformation sessionInformation = new SessionInformation(
                        name, null, session, true);
                    
                    // add the session state to the result set
                    results.add(sessionInformation);
                }
            }
        }
        return results;
    }
}
