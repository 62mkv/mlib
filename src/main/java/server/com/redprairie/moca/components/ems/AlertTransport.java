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

package com.redprairie.moca.components.ems;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.alerts.AlertFile;
import com.redprairie.moca.alerts.util.AlertUtils;

/**
 * Component class that is responsible for transporting messages to the EMS
 * system.  This component is meant to be called by a daemon scheduler.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 */
public class AlertTransport {

    /**
     * Transports messages to the EMS system.
     * 
     * @param moca the MOCA context. This argument cannot be null.
     * @param fileName A specific file to transport.
     * @throws MocaException 
     */
    public void raiseAlert(MocaContext moca, 
            String fileName,
            Integer removeFile) throws MocaException {
        
        AlertReader reader;
        String xml;
        boolean rmFile = (removeFile != null ? (removeFile == 1) : false);
        
        if (fileName == null || fileName.equals("")) {
            reader = new AlertReader();
        }
        else {
            reader = new AlertReader(fileName);
        }
        
        // Iterate through the files returned and act on them
        AlertFile[] files = reader.getAlertFiles();
        
        // Build the remote command to relay the XML data to the EMS server
        Map<String, Object> args = new HashMap<String, Object>();
        String remoteCommand = 
            "remote('" + AlertUtils.getUrl() + "')" +
            "{process ems alert xml}";
        
        for (AlertFile file : files) {
            _logger.debug("Processing file " + file.getXmlFile().getName());
            xml = file.getXmlContents();
            
            try {
                args.put("xml", xml);
                moca.executeCommand(remoteCommand, args);
                
                _logger.debug("XML passed to EMS successfully.");
                file.complete(true, rmFile);
            }
            catch (MocaException e) {
                _logger.error("Error processing XML in EMS.  Moving file " 
                        + file + " to the BAD directory:" + e, e);
                file.complete(false);
                // TODO: Capture the remote connection exception and 
                // attempt to re-send.
            }
        }
        
        return;
    }
    
    // ---------------------------------
    // Implementation:
    // ---------------------------------
    
    // Constants
    private static final Logger _logger = LogManager.getLogger(AlertTransport.class);
}
