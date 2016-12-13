/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.probes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.probes.PollingProbe;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.LocalSessionContext;
import com.redprairie.moca.server.exec.RequestContext;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.exec.SessionType;
import com.redprairie.moca.server.session.SessionToken;

/**
 * Extends Monitoring & Diagnostics PollingProbe which allows
 * scheduling of probing logic on an interval. This abstract class
 * will handle making the MocaContext and MadFactory available to
 * implementing subclasses. Additionally, transaction logic will then be handled
 * by this abstract class. Subclasses should implement the executeWithContext
 * method to perform their desired probing logic. Note - while similar to a job
 * this should only be used for probing related logic. 
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public abstract class MocaPollingProbe extends PollingProbe {
    
    /**
     * Executed on an interval specified by the @PollingInterval annotation. The
     * MocaContext and MadFactory are provided for the implementing class. Transaction
     * management (commit/rollback) is already handled by the parent class (MocaPollingProbe).
     * This method should perform the probing related logic as desired.
     * 
     * @param moca The MocaContext
     * @param mad The MadFactory
     * @throws MocaException
     */
    protected abstract void executeWithContext(MocaContext moca, MadFactory mad) throws MocaException;
    

    // @see com.redprairie.mad.probes.PollingProbe#execute(org.quartz.JobExecutionContext)
    @Override
    public final void execute(JobExecutionContext jobExec) throws JobExecutionException {
        execute(setupProbeContext(this.getClass()), getFactory());
    }

    // Calls the concrete subclasses executeWithContext method providing the
    // MocaContext and MadFactory that were setup - handles commit/rollback.
    // This is package private for testing purposes to mock out the ServerContext/MadFactory
    void execute(ServerContext serverContext, MadFactory mFact) {
        try {
            executeWithContext(serverContext.getComponentContext(), getFactory());
            serverContext.commit();
        }
        catch (MocaInterruptedException e) {
            _logger.info("Polling probe interrupted  -- aborting");
            throw e;
        }
        catch (MocaException e) {
            _logger.warn("A MocaException occurred while executing the polling probe.", e);
        }
        catch (RuntimeException e) {
            // Just print from our polling probe and rethrow, Quartz will include the full stack trace
            _logger.error("A RuntimeException occurred while executing the polling probe.");
            throw e;
        }
        finally {
            // Close will handle the rollback if needed
            serverContext.close();
            // Dissociate the session then
            ServerUtils.setCurrentContext(null);
        }
    }
    
    // Sets up the server context for the probe
    private ServerContext setupProbeContext(Class<?> clazz) {
        ServerContextFactory factory = ServerUtils
                .globalAttribute(ServerContextFactory.class);
        
        RequestContext request = new RequestContext();
        SessionContext session = new LocalSessionContext("probe-" + clazz.getName(), 
            SessionType.PROBE);

        // Authenticate our session
        session.setSessionToken(new SessionToken(clazz.getName()));

        ServerContext serverContext = factory.newContext(request, session);
        ServerUtils.setCurrentContext(serverContext);
        
        return serverContext;
    }  
    
    /**
     * Logger to be used by implementing subclasses
     */
    protected final Logger _logger = LogManager.getLogger(this.getClass());
}
