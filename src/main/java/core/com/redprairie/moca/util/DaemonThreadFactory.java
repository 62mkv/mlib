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

package com.redprairie.moca.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A ThreadFactory that marks the spawned threads as daemon.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public class DaemonThreadFactory implements ThreadFactory {
    public DaemonThreadFactory(String prefix, boolean increment) {
        _prefix = prefix;
        if (increment) {
            _count = new AtomicInteger(0);
        }
        else {
            _count = null;
        }
    }
    
    public DaemonThreadFactory(String prefix) {
        this(prefix, true);
    }
    
    @Override
    public Thread newThread(Runnable r) {
        Thread t = createThread(r);
        t.setDaemon(true);
        return t;
    }
    
    protected String getNextName() {
        String name;
        if (_count == null) {
            name = _prefix;
        }
        else {
            name = _prefix + "-" + _count.getAndAdd(1);
        }
        
        return name;
    }
    
    protected Thread createThread(Runnable runnable) {
        return new Thread(runnable, getNextName());
    }
    
    private final String _prefix;
    private final AtomicInteger _count;
}