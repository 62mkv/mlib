/*
 *  $URL$
 *  $Revision$
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

package com.sam.moca.web.console;

import java.lang.management.ThreadInfo;
import java.util.Map;

import com.sam.moca.advice.ServerContextAdministrationBean;
import com.sam.moca.advice.SessionAdministrationBean;
import com.sam.moca.advice.SessionAdministrationManagerBean;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.web.AbstractModel;

public class ServerThreadInformation extends AbstractModel {

    private ServerThreadInformation() {
    }

    /**
     * Retrieve the details related to the session.
     * 
     * @param Map<String,String[]> parameters The parameters passed on the
     *            request.
     * @return
     */
    public static ThreadInfo getThreadInfo(String sessionName, String threadId)
            throws Exception {

        SessionAdministrationManagerBean manager = ServerUtils.globalAttribute(
            SessionAdministrationManagerBean.class);

        SessionAdministrationBean[] sessions = manager.getSessions();
        
        for (SessionAdministrationBean session : sessions) {
            if (sessionName.equals(session.getSessionName())) {
                Map<Long, ServerContextAdministrationBean> sessionThreads = 
                    manager.getSessionBeans(session);
                
                ServerContextAdministrationBean bean = sessionThreads.get(
                    Long.valueOf(threadId));
                
                if (bean != null) {
                    ThreadInfo[] threads = bean.getSessionThreads();
                    if (threads != null && threads.length > 0) {
                        return threads[0];
                    }
                }
                break;
            }
        }
        
        throw new Exception("The selected session thread no longer exists.");
    }
}