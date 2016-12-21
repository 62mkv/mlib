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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sam.moca.MocaContext;
import com.sam.moca.MocaRegistry;
import com.sam.moca.client.XMLResultsEncoder;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.util.MocaUtils;

/**
 * Data structure for an alert.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 *
 * @author Brandon Grady
 * @version $Revision$
 */
public class Alert {

    // Constants
    public static final int PRIORITY_DEFAULT = 0;
    public static final int PRIORITY_INFORMATIONAL = 1;
    public static final int PRIORITY_ACTIONABLE = 2;
    public static final int PRIORITY_CRITICAL = 3;
    public static final String DEFAULT_TIMEZONE = "----";
    
    /**
     * Inner builder class to help build an Immutable Alert instance.
     * 
     * @author Brandon Grady
     */
    public static class AlertBuilder {
        /**
         * Constructor requires eventName and sourceSystem.
         * 
         * @param eventName
         * @param sourceSystem
         */
        public AlertBuilder(String eventName, String sourceSystem) {
            this.eventName = eventName;
            this.sourceSystem = sourceSystem;
        }
        
        // Optional Parameters
        public AlertBuilder priority(int val) {
            priority = val;
            return this;
        }
        
        public AlertBuilder addMsgFlg(int val) {
            add_msg_flg = val;
            return this;
        }
        
        public AlertBuilder primerFlag(boolean val) {
            primerFlag = val;
            return this;
        }
        
        public AlertBuilder inTransFlag(boolean val) {
            inTransFlag = val;
            return this;
        }
        
        public AlertBuilder keyValue(String val) {
            keyValue = val;
            return this;
        }
        
        public AlertBuilder duplicateValue(String val) {
            duplicateValue = val;
            return this;
        }
        
        public AlertBuilder clientId(String val) {
            clientId = val;
            return this;
        }
        
        public AlertBuilder storedTimezone(String val) {
            if (val != null && !val.equals("")) {
                storedTimezone = val;
            }
            return this;
        }
        
        public AlertBuilder attachmentServer(String val) {
            attachmentServer = val;
            return this;
        }

        public AlertBuilder attachmentFile(String val) {
            attachmentFile = val;
            return this;
        }
        
        public AlertBuilder attachmentRemoveFile(boolean val) {
            attachmentRemove = val;
            return this;
        }
        
        public AlertBuilder attachmentPath(String val) {
            attachmentPath = val;
            return this;
        }
        
        public Alert build() {
            return new Alert(this);
        }
        
        // Required parameters
        private final String eventName;
        private final String sourceSystem;
        
        // Optional parameters
        private int priority = PRIORITY_DEFAULT;
        private int add_msg_flg = 0;
        private boolean primerFlag = false;
        private boolean inTransFlag = true;
        private String keyValue = "";
        private String duplicateValue = "";
        private String clientId = "";
        private String storedTimezone = DEFAULT_TIMEZONE;
        private String attachmentServer = "";
        private String attachmentFile = "";
        private String attachmentPath = "";
        private Boolean attachmentRemove = false;
    }
    
    /**
     * Constructor - NOTE that it is private.  The acceptable way to 
     * instantiate an alert is by using the builder class provided
     * above.
     * 
     * For example, the following will build a critical alert for the event
     * known as "eventName":
     * 
     * Alert alert = new Alert.AlertBuilder("eventName", "sourceSystem").
     *               priority(Alert.PRIORITY_CRITICAL).build();
     * 
     * @param builder
     */
    private Alert(AlertBuilder builder) {
        _url = ServerUtils.globalContext().getConfigurationElement(MocaRegistry.REGKEY_SERVER_URL);
        _dateTime = new Date();
        
        synchronized (Alert.class) {
            _fileName = MocaUtils.formatDate(_dateTime) + "_" + _dateTime.getTime() 
                    + "_" + _sessionSequence + AlertFile.XML_EXT;
            
            _sessionSequence++; // Increment the sequence for this session.
        }
        
        // Set the values passed in by the builder
        _eventName = builder.eventName;
        _sourceSystem = builder.sourceSystem;
        _priority = builder.priority;
        _keyValue = builder.keyValue;
        _primerFlag = builder.primerFlag;
        _inTransFlag = builder.inTransFlag;
        _duplicateValue = builder.duplicateValue;
        _clientId = builder.clientId;
        _storedTimezone = builder.storedTimezone;
        _attachmentServer = builder.attachmentServer;
        _attachmentFilename = builder.attachmentFile;
        _attachmentPath = builder.attachmentPath;
        _attachmentRemove = builder.attachmentRemove;
        _add_msg_flg = builder.add_msg_flg;

        // If the primer flag is set, key value must be PRIMER
        evaluateKeyValue();
        
    }
    
    /**
     * @return XML string to be passed to EMS
     * @throws IOException 
     */
    public String asXML() throws IOException {
        StringWriter out = new StringWriter();
        
        out.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        
        out.append("<" + AlertXML.ROOT + ">\n");
        
        // Version
        out.append("<" + AlertXML.VERSION + ">");
        XMLResultsEncoder.writeEscapedString(AlertXML.MESSAGE_VERSION, out);
        out.append("</" + AlertXML.VERSION + ">\n");
        
        // Source URL
        out.append("<" + AlertXML.SOURCE_URL + ">");
        XMLResultsEncoder.writeEscapedString(_url, out);
        out.append("</" + AlertXML.SOURCE_URL + ">\n");

        // In Trans
        out.append("<" + AlertXML.IN_TRANS + ">");
        out.append(_inTransFlag ? "1" : "0");
        out.append("</" + AlertXML.IN_TRANS + ">\n");
        
        // Date/Time
        out.append("<" + AlertXML.DATE_TIME + ">");
        out.append(MocaUtils.formatDate(_dateTime));
        out.append("</" + AlertXML.DATE_TIME + ">\n");
        
        // Event Data
        out.append("<" + AlertXML.EVENT_DATA + ">\n");
        
        // Event Name
        out.append("<" + AlertXML.EVENT_NAME + ">");
        XMLResultsEncoder.writeEscapedString(_eventName, out);
        out.append("</" + AlertXML.EVENT_NAME + ">\n");
        
        // Source System
        out.append("<" + AlertXML.SOURCE_SYSTEM + ">");
        XMLResultsEncoder.writeEscapedString(_sourceSystem, out);
        out.append("</" + AlertXML.SOURCE_SYSTEM + ">\n");
        
        // Key Value
        out.append("<" + AlertXML.KEY_VALUE + ">");
        XMLResultsEncoder.writeEscapedString(_keyValue, out);
        out.append("</" + AlertXML.KEY_VALUE + ">\n");
        
        // Priority
        out.append("<" + AlertXML.PRIORITY + ">");
        out.append(Integer.toString(_priority));
        out.append("</" + AlertXML.PRIORITY + ">\n");
        
        // Primer Flag
        out.append("<" + AlertXML.PRIMER + ">");
        out.append(_primerFlag ? "1" : "0");
        out.append("</" + AlertXML.PRIMER + ">\n");
        
        // Duplicate Value
        out.append("<" + AlertXML.DUP_VALUE + ">");
        XMLResultsEncoder.writeEscapedString(_duplicateValue, out);
        out.append("</" + AlertXML.DUP_VALUE + ">\n");
        
        // Client
        out.append("<" + AlertXML.CLIENT_ID + ">");
        XMLResultsEncoder.writeEscapedString(_clientId, out);
        out.append("</" + AlertXML.CLIENT_ID + ">\n");
        
        // Stored Timezone
        out.append("<" + AlertXML.STORED_TIMEZONE + ">");
        XMLResultsEncoder.writeEscapedString(_storedTimezone, out);
        out.append("</" + AlertXML.STORED_TIMEZONE + ">\n");
        
        // Event Definition
        out.append("<" + AlertXML.EVENT_DEF + ">\n");
        if (_eventDefinition != null) {
            out.append(_eventDefinition.asXML());
        }
        out.append("</" + AlertXML.EVENT_DEF + ">\n");
        
        // Qualifiers
        out.append("<" + AlertXML.QUALIFIERS + ">\n");
        Iterator <EventQualifier> iter = _eventQualifiers.iterator();
        while (iter.hasNext()) {
            EventQualifier eq = iter.next();
            out.append(eq.asXML());
        }
        out.append("</" + AlertXML.QUALIFIERS + ">\n");
        
        out.append("<" + AlertXML.ATTACHMENTS + ">\n");
        
        out.append("<" + AlertXML.ATTACHMENT_SERVER + ">");
        XMLResultsEncoder.writeEscapedString(_attachmentServer, out);
        out.append("</" + AlertXML.ATTACHMENT_SERVER + ">\n");
        
        out.append("<" + AlertXML.ATTACHMENT_PATH + ">\n");
        XMLResultsEncoder.writeEscapedString(_attachmentPath, out);
        out.append("</" + AlertXML.ATTACHMENT_PATH + ">\n");
        
        out.append("<" + AlertXML.ATTACHMENT_FILENAME + ">\n");
        XMLResultsEncoder.writeEscapedString(_attachmentFilename, out);
        out.append("</" + AlertXML.ATTACHMENT_FILENAME + ">\n");
        
        out.append("<" + AlertXML.ATTACHMENT_REMOVE_FILE + ">\n");
        XMLResultsEncoder.writeEscapedString((_attachmentRemove ? "1": "0"), out);
        out.append("</" + AlertXML.ATTACHMENT_REMOVE_FILE + ">\n");
        
        out.append("</" + AlertXML.ATTACHMENTS + ">\n");
        
        // End the open entities
        out.append("</" + AlertXML.EVENT_DATA + ">\n");
        
        out.append("</" + AlertXML.ROOT + ">\n");
        
        return out.toString();
    }
    
    /**
     * When the alert is ready to go, write it.  The object will handle whether to write it
     * immediately or to tie into the transaction in order to write when a commit or rollback occurs.
     * 
     * @return void
     * @throws Exception 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public void write() throws FileCreationException, IOException {
        // Create the AlertFile and assign the XML of this alert to it.
        AlertFile aFile;
        try {
            aFile = new AlertFile(_fileName, true); // Will append the path
            aFile.setXmlContents(asXML());
        }
        catch (Exception e) {
            _logger.debug("Exception caught while trying to write XML file: " 
                    + e.getMessage());
            throw new FileCreationException();
        }
        
        if (_inTransFlag) {
            // If this is in transaction - we don't want to write it until 
            // after a successful commit
            MocaContext ctxt = MocaUtils.currentContext();
            AlertTransAction ata = new AlertTransAction(aFile);
            ctxt.addTransactionHook(ata);
        }
        else {
            aFile.writeXML(true);
        }
    }
    
    /**
     * @return the url
     */
    public String getUrl() {
        return _url;
    }
    
    /**
     * @return the dateTime
     */
    public Date getDateTime() {
        return new Date(_dateTime.getTime());
    }
    
    /**
     * @return the eventName
     */
    public String getEventName() {
        return _eventName;
    }
    
    /**
     * @param eventName the eventName to set
     */
    public void setEventName(String eventName) {
        _eventName = eventName;
    }
    
    /**
     * @return the priority
     */
    public int getPriority() {
        return _priority;
    }
    
    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        _priority = priority;
    }
    
    /**
     * @return the sourceSystem
     */
    public String getSourceSystem() {
        return _sourceSystem;
    }

    /**
     * @param sourceSystem the sourceSystem to set
     */
    public void setSourceSystem(String sourceSystem) {
        _sourceSystem = sourceSystem;
    }

    /**
     * @return the keyValue
     */
    public String getKeyValue() {
        return _keyValue;
    }

    /**
     * @param keyValue the keyValue to set
     */
    public void setKeyValue(String keyValue) {
        _keyValue = keyValue;
    }

    /**
     * @return the primerFlag
     */
    public boolean isPrimerFlag() {
        return _primerFlag;
    }
    
    /**
     * @param primerFlag the primerFlag to set
     */
    public void setPrimerFlag(boolean primerFlag) {
        _primerFlag = primerFlag;
        
        evaluateKeyValue();
    }
    
    /**
     * @return the inTransFlag
     */
    public boolean isInTransFlag() {
        return _inTransFlag;
    }
    
    /**
     * @param inTransFlag the inTransFlag to set
     */
    public void setInTransFlag(boolean inTransFlag) {
        _inTransFlag = inTransFlag;
    }
    
    /**
     * @return the duplicateValue
     */
    public String getDuplicateValue() {
        return _duplicateValue;
    }
    
    /**
     * @param duplicateValue the duplicateValue to set
     */
    public void setDuplicateValue(String duplicateValue) {
        _duplicateValue = duplicateValue;
    }
    
    /**
     * @return the clientId
     */
    public String getClientId() {
        return _clientId;
    }
    
    /**
     * @param clientId the clientId to set
     */
    public void setClientId(String clientId) {
        _clientId = clientId;
    }
    
    /**
     * @return the storedTimezone
     */
    public String getStoredTimezone() {
        return _storedTimezone;
    }
    
    /**
     * @param storedTimezone the storedTimezone to set
     */
    public void setStoredTimezone(String storedTimezone) {
        _storedTimezone = storedTimezone;
    }
    
    public String getFileName() {
        return _fileName;
    }

    public String getAttachmentFilename() {
        return _attachmentFilename;
    }

    public void setAttachmentFilename(String attachmentFilename) {
        _attachmentFilename = attachmentFilename;
    }

    public Boolean getAttachmentRemove() {
        return _attachmentRemove;
    }

    public void setAttachmentRemove(Boolean attachmentRemove) {
        _attachmentRemove = attachmentRemove;
    }

    public String getAttachmentPath() {
        return _attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        _attachmentPath = attachmentPath;
    }

    public String getAttachmentServer() {
        return _attachmentServer;
    }

    public void setAttachmentServer(String attachmentServer) {
        _attachmentServer = attachmentServer;
    }

    /**
     * @return the eventDefinition
     */
    public EventDefinition getEventDefinition() {
        return _eventDefinition;
    }
    
    /**
     * @param eventDefinition the eventDefinition to set
     */
    public void setEventDefinition(EventDefinition eventDefinition) {
        _eventDefinition = eventDefinition;
    }
    
    /**
     * @return the eventQualifiers
     */
    public ArrayList<EventQualifier> getEventQualifiers() {
        return new ArrayList<EventQualifier>(_eventQualifiers);
    }
    
    public void addEventQualifier(EventQualifier eq) {
        _eventQualifiers.add(eq);
    }
    
    /**
     * @param eventQualifiers the eventQualifiers to set
     */
    public void setEventQualifiers(ArrayList<EventQualifier> eventQualifiers) {
        _eventQualifiers = eventQualifiers;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (other == null) {
            return false;
        }
        
        if (!(other instanceof Alert)) {
            return false;
        }
        
        final Alert alert = (Alert) other;
        
        return _url.equals(alert._url)
               && _dateTime.equals(alert._dateTime)
               && _eventName.equals(alert._eventName)
               && _priority == alert._priority
               && _primerFlag == alert._primerFlag
               && _inTransFlag == alert._inTransFlag
               && _sourceSystem.equals(alert._sourceSystem)
               && _keyValue.equals(alert._keyValue)
               && _duplicateValue.equals(alert._duplicateValue)
               && _clientId.equals(alert._clientId)
               && _storedTimezone.equals(alert._storedTimezone);
    }
    
    @Override
    public int hashCode() {
        return _url.hashCode() + _dateTime.hashCode() + _eventName.hashCode()
               + _priority + (_primerFlag ? 1 : 0)
               + (_inTransFlag ? 1 : 0) + _sourceSystem.hashCode()
               + _keyValue.hashCode() + _duplicateValue.hashCode()
               + _clientId.hashCode() + _storedTimezone.hashCode();
    }
    
    @Override
    public String toString() {
        return "Alert(moca): " + _eventName + " - From: " + _sourceSystem;
    }
    
    // Implementation
    private static final Logger _logger = LogManager.getLogger(Alert.class);

    private String _url;
    private Date _dateTime;
    private String _eventName = "";
    private int _priority = PRIORITY_INFORMATIONAL;
    private boolean _primerFlag = false;
    private boolean _inTransFlag = true;
    private String _sourceSystem = "";
    private String _keyValue = "";
    private String _duplicateValue = "";
    private String _clientId = "";
    private String _storedTimezone = DEFAULT_TIMEZONE;
    private String _fileName = "";
    private String _attachmentFilename = "";
    private Boolean _attachmentRemove = false;
    @SuppressWarnings("unused")
    private int _add_msg_flg = 0;
    private String _attachmentPath = "";
    private String _attachmentServer = "";
    private EventDefinition _eventDefinition;
    private ArrayList<EventQualifier> _eventQualifiers = new ArrayList<EventQualifier>();
    
    private static int _sessionSequence = 0;
    
    // Re-evaluates the key value based on the primer flag;
    private void evaluateKeyValue() {
        if (_primerFlag) {
            _keyValue = EventDefinition.DEFAULT_PRIME_KEY_VAL;
        }
    }
}