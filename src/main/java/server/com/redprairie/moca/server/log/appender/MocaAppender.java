/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.server.log.appender;

/**
 * Interface describing contract needed for an appender to work as an appender
 * for use as client trace enabled source.
 * 
 * Copyright (c) 2013 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public interface MocaAppender {
    /**
     * Attempts to close the open appender to free resources such as file
     * pointers.
     * @param appenderName
     */
    public void closeAppender(String appenderName);
}
