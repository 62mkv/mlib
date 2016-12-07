/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.probes.asyncexec;

import java.util.List;

/**
 * Abstract class for common dumpExecutors method
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public abstract class AbstractExecutors implements AsynchronousExecutorsMXBean {
    // @see com.redprairie.moca.probes.asyncexec.AsynchronousExecutorsMXBean#dumpExecutors()
    @Override
    public String dumpExecutors() {
        List<AsynchronousExecution> executors = getExecutors();
        StringBuilder builder = new StringBuilder();
        
        for (AsynchronousExecution execution : executors) {
            builder.append("Thread ID:\t");
            builder.append(execution.getThreadId());
            builder.append("\n");
            builder.append("Callable:\t");
            builder.append(execution.getCallable());
            builder.append("\n");
            builder.append("Status:\t");
            builder.append(execution.getStatus());
            builder.append("\n");
            builder.append("Status Age:\t");
            builder.append(execution.getStatusAgeMs());
            builder.append(" ms\n\n");
        }
        
        return builder.toString();
    }
}
