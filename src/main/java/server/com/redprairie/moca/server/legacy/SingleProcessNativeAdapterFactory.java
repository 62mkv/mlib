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

package com.redprairie.moca.server.legacy;

import java.util.concurrent.TimeUnit;

import com.redprairie.moca.Builder;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.legacy.socket.LocalNativeProcessWrapper;
import com.redprairie.moca.server.repository.CommandRepository;

/**
 * Native Factory that only ever creates a single native process.  This native
 * process is always internal and tied to the same process.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class SingleProcessNativeAdapterFactory extends AbstractNativeAdapterFactory {
    /**
     * Constructor to keep the pool in
     */
    public SingleProcessNativeAdapterFactory(NativeProcessPoolBuilder poolBuilder, 
            int processTimeout, CommandRepository repos) throws MocaException {
        super(poolBuilder.pooltimeout(processTimeout, TimeUnit.SECONDS), repos);
    }

    // @see com.redprairie.moca.server.legacy.NativeProcessPool#getServerAdapter(com.redprairie.moca.server.exec.ServerContext)
    @Override
    protected MocaServerAdapter getServerAdapter(ServerContext ctx) throws MocaException {
        return new ContextMocaServerAdapter(ctx);
    }

    // @see com.redprairie.moca.server.legacy.NativeProcessPool#start()
    @Override
    protected void setup() {
        // There is nothing to setup here
    }
    
    protected Builder<? extends NativeProcess> builder() {
        return new Builder<NativeProcess>() {
            @Override
            public NativeProcess build() {
                String processID = getNextID();
                return new LocalNativeProcessWrapper(
                    new InternalNativeProcess(processID), processID);
            }
            
        };
    }

    // @see com.redprairie.moca.server.legacy.NativeAdapterFactory#close()
    @Override
    public void close() {
        // This impl has nothing to worry about or close
    }
}
