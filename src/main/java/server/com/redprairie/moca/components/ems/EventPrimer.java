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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.alerts.Alert;
import com.redprairie.moca.alerts.EMSException;
import com.redprairie.moca.alerts.EventDefinition;
import com.redprairie.moca.alerts.EventGroup;
import com.redprairie.moca.alerts.EventQualifier;
import com.redprairie.moca.alerts.FileCreationException;

/**
 * Component class that is responsible for creating a prime message 
 * that will ultimately be transported to EMS.  Prime messages describe 
 * an alert for later raising.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 */
public class EventPrimer {

    /**
     * Facilitates a prime message being created.
     * 
     * @param moca the MOCA context. This argument cannot be null.
     * @param eventName The name of the event
     * @param sourceSystem The source system.
     * @param eventMessage The message that will be created.
     * @param htmlMessage The HTML formatted message that will be created.
     * @param subject The subject line of the e-mail.
     * @param qualifiers Comma-separated list of qualifiers
     * @param groups Comma-separated list of groups
     * @param priority Priority of the event
     * @param acknowledgeFlag Acknowledgement flag
     * @param eventType Event type.
     * @param primeLockFlag Prime Lock Flag
     * @param eventTime Event Time
     * @param escalationTime Event Escalation Time
     * @param escalationEvent Event to use for escalation
     * @param description The description of the event
     * @param localeId The locale to use
     * @param storedTimezone The timezone of the source system.
     * @throws EMSException 
     */
    public void primeEvent(MocaContext moca, 
            String eventName,
            String sourceSystem,
            String eventMessage,
            String htmlMessage,
            String subject,
            String qualifiers,
            String groups,
            Integer priority,
            Integer acknowledgeFlag,
            String eventType,
            Integer primeLockFlag,
            Integer eventTime,
            Integer escalationTime,
            String escalationEvent,
            String description,
            String localeId,
            String storedTimezone,
            String shortDesc,
            Integer add_msg_flg) throws EMSException {
                
        // Create the EventDefinition object
        EventDefinition ed = new EventDefinition.EventDefBuilder(eventName, 
                description, subject, eventMessage).
                htmlMessage(htmlMessage).eventType(eventType).
                escalationEvent(escalationEvent).localeId(localeId).
                build();

        // Modify the definition as needed based on what was passed.
        if (priority != null) {
            ed.setPriority(priority.intValue());
        }
        
        if (acknowledgeFlag != null) {
            ed.setAcknowledgeFlag(acknowledgeFlag.intValue() == 1 ? true : false);
        }
        
        if (primeLockFlag != null) {
            ed.setPrimeLockFlag(primeLockFlag.intValue() == 1 ? true : false);
        }
        
        if (eventTime != null) {
            ed.setEventTime(eventTime.intValue());
        }

        if (escalationTime != null) {
            ed.setEscalationTime(escalationTime.intValue()); 
        }

        // Modify the definition as needed based on what was passed.
        if (add_msg_flg != null) {
            ed.setAddMsgFlg(add_msg_flg.intValue());
        }
        
        int ii;
        
        // Handle the qualifiers
        if (qualifiers != null && !qualifiers.isEmpty()) {
            String[] quals = qualifiers.split(",");
            
            for (ii = 0; ii < quals.length; ii++) {
                EventQualifier eq = new EventQualifier();
                
                if (quals[ii].indexOf(':') >= 0) {
                    String[] pair = quals[ii].split(":");
                    eq.setName(pair[0].trim());
                    eq.setType(EventQualifier.translateType(pair[1].trim()));
                }
                else {
                    eq.setName(quals[ii].trim());
                }
                
                // Add the qualifier to the event definition
                ed.addEventQualifier(eq);
            }
        }
        
        // Handle the groups
        if (groups != null && !groups.equals("")) {
            String[] grps = groups.split(",");
            
            for (ii = 0; ii < grps.length; ii++) {
                EventGroup eg = new EventGroup();
                eg.setName(grps[ii]);
                
                // Add the group the event definition
                ed.addEventGroup(eg);
            }
        }
        
        // Create the Alert object
        Alert alert = new Alert.AlertBuilder(eventName, sourceSystem).
            primerFlag(true).storedTimezone(storedTimezone).
            build();
        
        // Set the event definition on the alert
        alert.setEventDefinition(ed);
        
        // Call the .write method on the Alert.
        try {
            alert.write();
        }
        catch (IOException e) {
            _logger.debug("IOException encountered while trying to write file: " + e, e);
            throw new FileCreationException();
        }
        
        return;
    }
    
    // -----------------------------
    // Implementation:
    // -----------------------------
    private static final Logger _logger = LogManager.getLogger(EventPrimer.class);
}
