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

package com.redprairie.moca.mad.reporters;

import java.util.Formatter;

import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.servlet.WebSessionManager;
import com.redprairie.moca.web.console.SessionAdministration;
import com.redprairie.moca.web.console.SessionInformation;

/**
 * A class which formats summary information for the set of Sessions and each
 * individual Session presently registered in MOCA into a readable text report..
 * 
 * Copyright (c) 2012 Sam Corporation All Rights Reserved
 * 
 * @author eknapp
 */
public class SessionReporter {

    /**
     * Generates a formatted report on the current sessions.
     * @return A formatted report regarding the current sessions.
     */
    public static String generateSessionReport() {
        StringBuilder buffer = new StringBuilder();
        Formatter formatter = new Formatter(buffer);
        formatSessionSummaryInfo(formatter);
        buffer.append("\n");
        for (SessionInformation session : SessionAdministration.getSessionInformation()) {
            formatSessionInformation(formatter, session);
            buffer.append("\n");
        }
        return buffer.toString();
    }
    
    private static void formatSessionSummaryInfo(Formatter formatter) {
        String format = "%1$-30s%2$-10s%n";
        WebSessionManager manager = getSessionManager();
        format(formatter, format, "Session Count:", manager.getSessionCount());
        format(formatter, format, "Max Session Count:", manager.getMaxSessions());
        format(formatter, format, "Peak Session Count:", manager.getPeakSessionCount());
    }

    private static WebSessionManager getSessionManager() {
        return (WebSessionManager) ServerUtils.globalContext().getAttribute(
            WebSessionManager.class.getName());
    }

    private static void formatSessionInformation(Formatter formatter,
                                                 SessionInformation info) {
        String format = "%1$-30s%2$-100s%n";
        format(formatter, format, "Session-ID:", info.getName());
        format(formatter, format, "Thread-ID:", info.getThreadId());
        format(formatter, format, "Session-Type:", info.getSessionType());
        format(formatter, format, "Session-Status:", info.getStatus());
        format(formatter, format, "Start-Time:", info.getStartedTime());
        format(formatter, format, "Last-SQL:", info.getLastSQL());
        format(formatter, format, "Last-SQL-Time:", info.getLastSQLTime());
        format(formatter, format, "Last-Command:", info.getLastCommand());
        format(formatter, format, "Last-Command-Time:", info.getLastCommandTime());
        format(formatter, format, "Last-Script:", info.getLastScript());
        format(formatter, format, "Last-Script-Time:", info.getLastScriptTime());
        format(formatter, format, "Trace-File:", info.getTraceName());
    }

    private static void format(Formatter formatter, String format, String title,
                               Object argument) {
        formatter.format(format, title, formatArgument(argument));
    }

    private static String formatArgument(Object argument) {
        return argument == null ? "" : argument.toString();
    }

}
