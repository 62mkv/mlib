/*
 *  $URL$
 *  $Author$
 *  $Date$
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

package com.redprairie.moca.server.exec;

/**
 * This is an enum that describes the various session types that the
 * system can generate.
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public enum SessionType {
    CLIENT,
    WEBSERVICE,
    CLIENT_LEGACY,
    TASK,
    JOB,
    ASYNC,
    CONSOLE,
    PROBE,
    /**
     * This is a miscellaneous catch all that applies to threads that are 
     * spawned outside the context of any of the other types that is not
     * controlled by MOCA.  One such example is an RMI exported object.
     */
    SERVER
}
