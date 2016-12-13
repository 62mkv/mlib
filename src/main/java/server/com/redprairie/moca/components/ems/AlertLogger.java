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

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.alerts.Alert;
import com.redprairie.moca.alerts.EMSException;
import com.redprairie.moca.alerts.EventQualifier;
import com.redprairie.moca.alerts.FileCreationException;
import com.redprairie.moca.util.MocaUtils;

/**
 * Component class that creates EMS message files (xml) for transport
 * to the EMS system.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 */
public class AlertLogger {

    /**
     * Component call for logging an EMS message.
     * 
     * @param moca the MOCA context. This argument cannot be null.
     * @param eventName The name of the event
     * @param sourceSystem The source system.
     * @param keyValue The key value for this alert
     * @param inTransFlag Should this alert only be recorded if the transaction finishes
     * @throws EMSException 
     */
    public void logAlert(MocaContext moca, 
            String eventName,
            String sourceSystem,
            String keyValue,
            String dupValue,
            String storedTimeZone,
            int priority,
            int inTransFlag,
            String attachmentPath,
            String attachmentServer,
            String attachmentFile,
            int attachmentRemoveFile,
            int addMsgFlg) throws EMSException {
        
        // Create the Alert object
        Alert alert = new Alert.AlertBuilder(eventName, sourceSystem).
            keyValue(keyValue).
            duplicateValue(dupValue).
            storedTimezone(storedTimeZone).
            priority(priority).
            inTransFlag(inTransFlag == 1 ? true : false).
            attachmentPath(attachmentPath).
            attachmentServer(attachmentServer).
            attachmentFile(attachmentFile).
            attachmentRemoveFile(attachmentRemoveFile == 1).
            addMsgFlg(addMsgFlg).
            build();
        
        // Loop through all arguments.  All arguments that are known should be 
        // handled accordingly above.  All unknown should be appended to the Qualifiers
        // list.
        MocaArgument[] args = moca.getArgs(true);

        for (int ii = 0; ii < args.length; ii++) {
            if (!knownArguments.contains(args[ii].getName().toLowerCase())) {
                // This argument is unknown to us, so it is a qualifier
                EventQualifier eq = new EventQualifier();
                eq.setName(args[ii].getName());
                
                // Default the value to an empty string and only overwrite with a non-null value.
                eq.setValue("");
                if (args[ii].getValue() != null) {
                    if (args[ii].getType() == MocaType.DATETIME) {
                        // Need to format the value in MOCA date/time format
                        eq.setValue(MocaUtils.formatDate((Date) args[ii].getValue()));
                        eq.setType(EventQualifier.FLDTYP_DATETIME);
                    }
                    else {
                        eq.setValue(args[ii].getValue().toString());
                    }
                }
                
                alert.addEventQualifier(eq);
            }
        }
        
        // Call the .write method on the Alert.
        try {
            alert.write();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            _logger.debug("IO Exception while writing alert: " + e, e);
            throw new FileCreationException();
        }
    }
    
    // -------------------------------
    // Implementation:
    // -------------------------------
    
    private static final Logger _logger = LogManager.getLogger(AlertLogger.class);
    
    // Place known arguments into this array.  NOTE: This array must stay sorted!!!
    private static Set<String> knownArguments = new HashSet<String>();
    static {
        knownArguments.add("evt_nam");
        knownArguments.add("in_trans"); 
        knownArguments.add("key_val");
        knownArguments.add("dup_val");
        knownArguments.add("src_sys"); 
        knownArguments.add("priority");
        knownArguments.add("stored_tz");
        knownArguments.add("att_app_srv");
        knownArguments.add("att_file");
        knownArguments.add("att_path");
        knownArguments.add("att_rm_file");
        knownArguments.add("add_msg_flg");
    }
}
