/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.redprairie.moca.util;

import com.redprairie.moca.server.ServerUtils;

/**
 * This class is basically the same as DaemonThreadFactory except that all the
 * thread local values have been stripped.  Therefore threads for use with
 * this factory cannot rely on having a MocaContext available to it.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class NonMocaDaemonThreadFactory extends DaemonThreadFactory {
    
    /**
     * @param prefix
     */
    public NonMocaDaemonThreadFactory(String prefix) {
        super(prefix);
    }
    /**
     * @param prefix
     * @param increment
     */
    public NonMocaDaemonThreadFactory(String prefix, boolean increment) {
        super(prefix, increment);
    }
    
    // @see com.redprairie.moca.util.DaemonThreadFactory#createThread(java.lang.Runnable)
    
    @Override
    protected Thread createThread(Runnable runnable) {
        return new Thread(runnable, getNextName()) {
            // @see java.lang.Thread#run()
            @Override
            public void run() {
                ServerUtils.setCurrentContext(null);
                super.run();
            }
            
        };
    }
}
