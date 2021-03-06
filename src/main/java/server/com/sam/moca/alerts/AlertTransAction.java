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
package com.sam.moca.alerts;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.TransactionHook;

/**
 * This is a helper class that will write an alert file once the 
 * transaction is completed successfully.  This only occurs for
 * alerts that are written "In transactions," which happens to be
 * the majority of the alerts raised.
 * 
 * @author grady
 * @version $Revision$
 */
public class AlertTransAction implements TransactionHook {

    AlertTransAction(AlertFile aFile) {
        alertFile = aFile;
    }
    
    /**
     * @return the alertFile
     */
    public AlertFile getAlertFile() {
        return alertFile;
    }
    
    /**
     * @param alertFile the alertFile to set
     */
    public void setAlertFile(AlertFile alertFile) {
        this.alertFile = alertFile;
    }

    /* (non-Javadoc)
     * @see com.sam.moca.TransactionHook#afterCompletion(com.sam.moca.MocaContext, boolean)
     */
    @Override
    public void afterCompletion(MocaContext ctx, boolean committed)
            throws MocaException {
        // Do nothing.
    }

    /* (non-Javadoc)
     * @see com.sam.moca.TransactionHook#beforeCommit(com.sam.moca.MocaContext)
     */
    @Override
    public void beforeCommit(MocaContext ctx) throws MocaException {
        
        _logger.debug("Writing EMS file based on successful transaction "
                + "completion - filename: " + alertFile.getXmlFile().getName());
        alertFile.writeXML(true); // TODO, figure out if we want to fail the transaction just because an alert failed.
    }

    // Implementation
    private static final Logger _logger = LogManager.getLogger(AlertTransAction.class);
    private AlertFile alertFile;
}
