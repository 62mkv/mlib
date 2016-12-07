/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2014
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

package com.redprairie.moca.cluster;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.util.MocaUtils;

/**
 * A simple serializable callable that can be used with the clustered asynchronous
 * executor.
 * 
 * Copyright (c) 2014 RedPrairie Corporation
 * All Rights Reserved
 * 
 */
public class ClusterCommandCallable implements Callable<MocaResults>, Serializable {
    
    public ClusterCommandCallable(String command, MocaArgument... args) {
        _command = command;
        _args = args == null ? new ArrayList<MocaArgument>() : Arrays.asList(args);
    }

    // @see java.util.concurrent.Callable#call()
    @Override
    public MocaResults call() throws Exception {
        return MocaUtils.currentContext().executeCommand(_command, _args.toArray(new MocaArgument[_args.size()]));
    }
    
    private final String _command;
    private final List<MocaArgument> _args;
    
    private static final long serialVersionUID = -7035411705274065060L;

}
