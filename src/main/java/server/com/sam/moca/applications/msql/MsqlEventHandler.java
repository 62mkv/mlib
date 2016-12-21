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

package com.sam.moca.applications.msql;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public interface MsqlEventHandler {
    
    public static enum TraceType {
        INTERACTIVE,
        NONINTERACTIVE,
        ALWAYS
    }
    
    /**
     * @param message
     */
    public void traceEvent(String message, TraceType type);

    /**
     * This is called for each line of the file and will have information
     * of the next line's multi line indention and the current line value
     * @param event
     */
    public void notifyLine(String line, int multiLineCount);
    
    /**
     * These are only raised if MSET performance value is set to ON
     * @param command
     */
    public void notifyCommandExecution(String command, long duration);
    
    /**
     * This event is called if a user inputs something to change the prompt
     * that is displayed
     * @param prompt The prompt that user wanted it to change to
     */
    public void updatePrompt(String prompt);
}
