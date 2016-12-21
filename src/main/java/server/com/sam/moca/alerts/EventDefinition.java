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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;

import com.sam.moca.alerts.util.AlertUtils;
import com.sam.moca.client.XMLResultsEncoder;


/**
 * @author grady
 * @version $Revision$
 */
public class EventDefinition {
    
    public static final String DEFAULT_PRIME_KEY_VAL = "PRIMER";
    
    /**
     * Inner builder class to help build an immutable EventDefinition.
     * 
     * @author Brandon Grady
     */
    public static class EventDefBuilder {
        /**
         * Constructor requires eventName, description, subject, and message.
         * 
         * @param eventName
         * @param description
         * @param subject
         * @param message
         */
        public EventDefBuilder(String eventName, 
                String description, String subject, String message) {
            
            this.eventName = eventName;
            this.description = description;
            this.subject = subject;
            this.message = message;
        }
        
        public EventDefBuilder eventType(String val) {
            eventType = val;
            return this;
        }

        public EventDefBuilder priority(int val) {
            priority = val;
            return this;
        }

        public EventDefBuilder localeId(String val) {
            if (val != null && !val.equals("")) {
                localeId = val;
            }
            return this;
        }
        
        public EventDefBuilder escalationEvent(String val) {
            escalationEvent = val;
            return this;
        }
        
        public EventDefBuilder escalationTime(int val) {
            escalationTime = val;
            return this;
        }
        
        public EventDefBuilder eventTime(int val) {
            eventTime = val;
            return this;
        }
        
        public EventDefBuilder acknowledgeFlag(boolean val) {
            acknowledgeFlag = val;
            return this;
        }
        
        public EventDefBuilder primeLockFlag(boolean val) {
            primeLockFlag = val;
            return this;
        }
        
        public EventDefBuilder shortDescription(String val) {
            if (val != null) {
                shortDescription = val;
            }
            return this;
        }
        
        public EventDefBuilder htmlMessage(String val) {
            if (val != null) {
                htmlMessage = val;
            }
            return this;
        }
        
        public EventDefinition build() {
            return new EventDefinition(this);
        }
        
        // Required parameters
        private final String eventName;
        private final String description;
        private final String subject;
        private final String message;
        
        // Optional parameters
        private String eventType = "";
        private int priority = Alert.PRIORITY_INFORMATIONAL;
        private int add_msg_flg = 0;
        private String localeId = AlertUtils.getDefaultLocale();
        private String escalationEvent = "";
        private int escalationTime = 0;
        private int eventTime = 0;
        private boolean acknowledgeFlag = false;
        private boolean primeLockFlag = false;
        private String shortDescription = "";
        private String htmlMessage = "";
    }
    
    /**
     * Constructor requires that you use the builder class.
     * 
     * @param builder
     */
    private EventDefinition(EventDefBuilder builder) {
        _eventName = builder.eventName;
        _description = builder.description;
        _subject = builder.subject;
        _message = builder.message;

        _eventType = builder.eventType;
        _priority = builder.priority;
        _localeId = builder.localeId;
        _escalationEvent = builder.escalationEvent;
        _escalationTime = builder.escalationTime;
        _eventTime = builder.eventTime;
        _acknowledgeFlag = builder.acknowledgeFlag;
        _primeLockFlag = builder.primeLockFlag;
        _add_msg_flg = builder.add_msg_flg;
        _shortDescription = builder.shortDescription;
        _htmlMessage = builder.htmlMessage;
    }

    /**
     * By default, do NOT include the root entity of this XML.
     * @return XML String
     * @throws IOException 
     */
    public String asXML() throws IOException {
        return asXML(false);
    }
    
    /**
     * @param includeRoot - set to TRUE to wrap the XML in a event-def entity.
     * @return XML String
     * @throws IOException 
     */
    public String asXML(boolean includeRoot) throws IOException {
        StringWriter out = new StringWriter();

        if (includeRoot) {
            out.append("<" + AlertXML.EVENT_DEF + ">\n");
        }
        
        // Event Name
        out.append("<" + AlertXML.EVENT_NAME + ">");
        XMLResultsEncoder.writeEscapedString(_eventName, out);
        out.append("</" + AlertXML.EVENT_NAME + ">\n");

        // Event Type
        out.append("<" + AlertXML.EVENT_TYPE + ">");
        XMLResultsEncoder.writeEscapedString(_eventType, out);
        out.append("</" + AlertXML.EVENT_TYPE + ">\n");

        // Priority
        out.append("<" + AlertXML.PRIORITY + ">");
        out.append(Integer.toString(_priority));
        out.append("</" + AlertXML.PRIORITY + ">\n");
        
        // Escalation Event
        out.append("<" + AlertXML.ESCALATION_EVENT + ">");
        XMLResultsEncoder.writeEscapedString(_escalationEvent, out);
        out.append("</" + AlertXML.ESCALATION_EVENT + ">\n");
        
        // Escalation Time
        out.append("<" + AlertXML.ESCALATION_TIME + ">");
        out.append(Integer.toString(_escalationTime));
        out.append("</" + AlertXML.ESCALATION_TIME + ">\n");

        // Event Time
        out.append("<" + AlertXML.EVENT_TIME + ">");
        out.append(Integer.toString(_eventTime));
        out.append("</" + AlertXML.EVENT_TIME + ">\n");
        
        // Acknowledge Flag
        out.append("<" + AlertXML.ACKNOWLEDGE_FLAG + ">");
        out.append(_acknowledgeFlag ? "1" : "0");
        out.append("</" + AlertXML.ACKNOWLEDGE_FLAG + ">\n");

        // Prime Lock Flag
        out.append("<" + AlertXML.PRIME_LOCK_FLAG + ">");
        out.append(_primeLockFlag ? "1" : "0");
        out.append("</" + AlertXML.PRIME_LOCK_FLAG + ">\n");
        
       // Add Event Format Message Flag
        out.append("<" + AlertXML.ADD_MSG_FLG + ">");
        out.append(Integer.toString(_add_msg_flg));
        out.append("</" + AlertXML.ADD_MSG_FLG + ">\n");
        
        // Messages
        // The XML supports sending many locale-based messages at the same
        // time.  This is where that functionality should be plugged in 
        // if/when it is implemented
        
        out.append("<" + AlertXML.MESSAGES + ">\n");

        out.append("<" + AlertXML.MESSAGE + " " + AlertXML.MESSAGE_ATTR_LOCALE 
                + "=\"" + _localeId + "\">\n");

        // All message-type fields will be sent in CDATA blocks.
        
        // Description
        out.append("<" + AlertXML.DESCRIPTION + ">");
        out.append("<![CDATA[");
        XMLResultsEncoder.writeEscapedString(_description, out);
        out.append("]]>");
        out.append("</" + AlertXML.DESCRIPTION + ">\n");

        // Short Description
        out.append("<" + AlertXML.SHORT_DESCRIPTION + ">");
        out.append("<![CDATA[");
        XMLResultsEncoder.writeEscapedString(_shortDescription, out);
        out.append("]]>");
        out.append("</" + AlertXML.SHORT_DESCRIPTION + ">\n");
        
        // Subject
        out.append("<" + AlertXML.SUBJECT + ">");
        out.append("<![CDATA[");
        XMLResultsEncoder.writeEscapedString(_subject, out);
        out.append("]]>");
        out.append("</" + AlertXML.SUBJECT + ">\n");

        // Text Message
        out.append("<" + AlertXML.TEXT_MESSAGE + ">");
        out.append("<![CDATA[");
        XMLResultsEncoder.writeEscapedString(_message, out);
        out.append("]]>");
        out.append("</" + AlertXML.TEXT_MESSAGE + ">\n");

        // HTML Message
        out.append("<" + AlertXML.HTML_MESSAGE + ">");
        out.append("<![CDATA[");
        XMLResultsEncoder.writeEscapedString(_htmlMessage, out);
        out.append("]]>");
        out.append("</" + AlertXML.HTML_MESSAGE + ">\n");
        
        out.append("</" + AlertXML.MESSAGE + ">\n");
        out.append("</" + AlertXML.MESSAGES + ">\n");

        // Qualifiers
        out.append("<" + AlertXML.QUALIFIERS + ">\n");
        
        Iterator<EventQualifier> qIter = _eventQualifiers.iterator();
        while (qIter.hasNext()) {
            EventQualifier eq = qIter.next();
            out.append(eq.asXML());
        }
        
        out.append("</" + AlertXML.QUALIFIERS + ">\n");
        
        // Groups
        out.append("<" + AlertXML.GROUPS + ">\n");
        
        Iterator<EventGroup> gIter = _eventGroups.iterator();
        while (gIter.hasNext()) {
            EventGroup eg = gIter.next();
            out.append(eg.asXML());
        }

        out.append("</" + AlertXML.GROUPS + ">\n");

        if (includeRoot) {
            out.append("</" + AlertXML.EVENT_DEF + ">");
        }
        
        return out.toString();
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
     * @return the eventType
     */
    public String getEventType() {
        return _eventType;
    }
    
    /**
     * @param eventType the eventType to set
     */
    public void setEventType(String eventType) {
        _eventType = eventType;
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
     * @return add_msg_flg
     */
    public int getAddMsgFlg() {
        return _add_msg_flg;
    }

    /**
     * @param addMsgFlg, the add_msg_flg to set
     */
    public void setAddMsgFlg(int addMsgFlg) {
        _add_msg_flg = addMsgFlg;
    }
    
    /**
     * @return the localeId
     */
    public String getLocaleId() {
        return _localeId;
    }
    
    /**
     * @param localeId the localeId to set
     */
    public void setLocaleId(String localeId) {
        _localeId = localeId;
    }
    
    /**
     * @return the escalationEvent
     */
    public String getEscalationEvent() {
        return _escalationEvent;
    }
    
    /**
     * @param escalationEvent the escalationEvent to set
     */
    public void setEscalationEvent(String escalationEvent) {
        _escalationEvent = escalationEvent;
    }
    
    /**
     * @return the escalationTime
     */
    public int getEscalationTime() {
        return _escalationTime;
    }
    
    /**
     * @param escalationTime the escalationTime to set
     */
    public void setEscalationTime(int escalationTime) {
        _escalationTime = escalationTime;
    }
    
    /**
     * @return the eventTime
     */
    public int getEventTime() {
        return _eventTime;
    }
    
    /**
     * @param eventTime the eventTime to set
     */
    public void setEventTime(int eventTime) {
        _eventTime = eventTime;
    }
    /**
     * @return the acknowledgeFlag
     */
    public boolean isAcknowledgeFlag() {
        return _acknowledgeFlag;
    }
    
    /**
     * @param acknowledgeFlag the acknowledgeFlag to set
     */
    public void setAcknowledgeFlag(boolean acknowledgeFlag) {
        _acknowledgeFlag = acknowledgeFlag;
    }
    
    /**
     * @return the primeLockFlag
     */
    public boolean isPrimeLockFlag() {
        return _primeLockFlag;
    }
    
    /**
     * @param primeLockFlag the primeLockFlag to set
     */
    public void setPrimeLockFlag(boolean primeLockFlag) {
        _primeLockFlag = primeLockFlag;
    }
    
    /**
     * @return the description
     */
    public String getDescription() {
        return _description;
    }
    
    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        _description = description;
    }
    
    /**
     * @return the shortDescription
     */
    public String getShortDescription() {
        return _shortDescription;
    }
    
    /**
     * @param shortDescription the shortDescription to set
     */
    public void setShortDescription(String shortDescription) {
        _shortDescription = shortDescription;
    }
    
    /**
     * @return the subject
     */
    public String getSubject() {
        return _subject;
    }
    
    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        _subject = subject;
    }
    
    /**
     * @return the message
     */
    public String getMessage() {
        return _message;
    }
    
    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        _message = message;
    }
    
    /**
     * @return the htmlMessage
     */
    public String getHtmlMessage() {
        return _htmlMessage;
    }
    
    /**
     * @param htmlMessage the htmlMessage to set
     */
    public void setHtmlMessage(String htmlMessage) {
        _htmlMessage = htmlMessage;
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
    
    /**
     * @return the eventGroups
     */
    public ArrayList<EventGroup> getEventGroups() {
        return new ArrayList<EventGroup>(_eventGroups);
    }
    
    public void addEventGroup(EventGroup eg) {
        _eventGroups.add(eg);
    }
    
    /**
     * @param eventGroups the eventGroups to set
     */
    public void setEventGroups(ArrayList<EventGroup> eventGroups) {
        _eventGroups = eventGroups;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        
        if (other == null) {
            return false;
        }
        
        if (!(other instanceof EventDefinition)) {
            return false;
        }
        
        final EventDefinition ed = (EventDefinition) other;
        
        return _eventName.equals(ed._eventName)
               && _eventType.equals(ed._eventType)
               && _priority == ed._priority
               && _localeId.equals(ed._localeId)
               && _escalationEvent.equals(ed._escalationEvent)
               && _escalationTime == ed._escalationTime
               && _eventTime == ed._eventTime
               && _acknowledgeFlag == ed._acknowledgeFlag
               && _primeLockFlag == ed._primeLockFlag
               && _description.equals(ed._description)
               && _shortDescription.equals(ed._shortDescription)
               && _subject.equals(ed._subject)
               && _message.equals(ed._message)
               && _htmlMessage.equals(ed._htmlMessage);
    }
    
    @Override
    public int hashCode() {
        return _eventName.hashCode() + _eventType.hashCode()
             + _priority + _localeId.hashCode() + _escalationEvent.hashCode()
             + _escalationTime + _eventTime + 
             + (_acknowledgeFlag ? 1 : 0) 
             + (_primeLockFlag ? 1 : 0)
             + _description.hashCode() + _shortDescription.hashCode()
             + _subject.hashCode() + _message.hashCode()
             + _htmlMessage.hashCode();
    }
    
    @Override
    public String toString() {
        return "EventDefinition(moca): " + _eventName;
    }
    
    // Implementation
    private String _eventName = "";
    private String _eventType = "";
    private int _priority = Alert.PRIORITY_INFORMATIONAL;
    private String _localeId = AlertUtils.getDefaultLocale();
    private String _escalationEvent = "";
    private int _escalationTime = 0;
    private int _add_msg_flg = 0;
    private int _eventTime = 0;
    private boolean _acknowledgeFlag = false;
    private boolean _primeLockFlag = false;
    private String _description = "";
    private String _shortDescription = "";
    private String _subject = "";
    private String _message = "";
    private String _htmlMessage = "";
    private ArrayList<EventQualifier> _eventQualifiers = new ArrayList<EventQualifier>();
    private ArrayList<EventGroup> _eventGroups = new ArrayList<EventGroup>();

}
