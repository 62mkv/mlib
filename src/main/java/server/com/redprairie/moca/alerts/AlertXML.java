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

package com.redprairie.moca.alerts;


/**
 * @author grady
 * @version $Revision$
 */
public class AlertXML {

    // Constants
    public static final String MESSAGE_VERSION = "2";
    
    public static final String ROOT = "ems-alert";
    public static final String SOURCE_URL = "source-url";
    public static final String IN_TRANS = "in-trans";
    public static final String DATE_TIME = "date-time";
    public static final String VERSION = "version";
    public static final String VERSION_XPATH = "//ems-alert/version";    
    
    public static final String EVENT_DATA = "event-data";
    public static final String EVENT_NAME = "event-name";
    public static final String EVENT_TYPE = "event-type";
    public static final String SOURCE_SYSTEM = "source-system";
    public static final String KEY_VALUE = "key-val";
    public static final String PRIORITY = "priority";
    public static final String ADD_MSG_FLG = "add-evt-frmt-msg-flg";
    public static final String PRIMER = "primer-flg";
    public static final String DUP_VALUE = "dup-val";
    public static final String CLIENT_ID = "client-id";
    public static final String STORED_TIMEZONE = "stored-tz";
    public static final String EVENT_DEF = "event-def";
    
    public static final String QUALIFIERS = "qualifiers";
    public static final String QUALIFIER = "qualifier";
    public static final String QUAL_ATTR_NAME = "name";
    
    // Constants for an event definition
    public static final String ESCALATION_EVENT = "escalation-event";
    public static final String ESCALATION_TIME = "escalation-time";
    public static final String EVENT_TIME = "event-time";
    public static final String ACKNOWLEDGE_FLAG = "acknowledge-flg";
    public static final String PRIME_LOCK_FLAG = "primer-lock-flg";
    
    // Messages definition
    public static final String MESSAGES = "messages";
    
    public static final String MESSAGE = "message";
    public static final String MESSAGE_ATTR_LOCALE = "locale-id";
    public static final String DESCRIPTION = "description";
    public static final String SHORT_DESCRIPTION = "short-descr";
    public static final String SUBJECT = "subject";
    public static final String TEXT_MESSAGE = "text-message";
    public static final String HTML_MESSAGE = "html-message";
    
    public static final String GROUPS = "groups";
    public static final String GROUP = "group";
    public static final String GROUP_ATTR_NAME = "name";
    
    public static final String ATTACHMENTS = "attachments";
    public static final String ATTACHMENT_SERVER = "att-app-srv";
    public static final String ATTACHMENT_PATH = "att-path";
    public static final String ATTACHMENT_FILENAME = "att-file";
    public static final String ATTACHMENT_REMOVE_FILE = "att-rm-file";
}
