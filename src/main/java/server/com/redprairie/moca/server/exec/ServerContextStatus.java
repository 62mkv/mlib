/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

import com.redprairie.moca.server.repository.CommandType;

/**
 * This enumeration holds the various states that a Server Context can
 * be in at at given time.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public enum ServerContextStatus {
    INACTIVE,
    IN_ENGINE,
    JAVA_EXECUTION,
    C_EXECUTION,
    COM_EXECUTION,
    SQL_EXECUTION,
    SCRIPT_EXECUTION,
    REMOTE_EXECUTION,
    LOCAL_SYNTAX_EXECUTION;
    
    public static ServerContextStatus getStatusForCommandType(CommandType type) {
        switch (type) {
        case C_FUNCTION:
        case SIMPLE_C_FUNCTION:
            return ServerContextStatus.C_EXECUTION;
        case COM_METHOD:
            return ServerContextStatus.COM_EXECUTION;
        case JAVA_METHOD:
            return ServerContextStatus.JAVA_EXECUTION;
        case LOCAL_SYNTAX:
            return ServerContextStatus.LOCAL_SYNTAX_EXECUTION;
        default:
            return null;
        }
    }
}
