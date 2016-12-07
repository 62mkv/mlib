/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.server.dispatch;

import java.util.Map;

/**
 * A hook that is invoked when a command is dispatched.  This can occur due to a client request or a recurring job.
 * 
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public interface DispatchActivityHook {
    /**
     * Called when a command is finished executing. Note that this method is
     * called outside of a normal MOCA transaction. Any database changes that
     * happen inside this hook must be done with commands that run inside their
     * own transaction.
     * 
     * @param duration
     * @param command
     * @param env
     * @param errorCode
     * @param rowCount
     */
    public void activity(long duration, String command, Map<String, String> env, int errorCode, int rowCount);
}
