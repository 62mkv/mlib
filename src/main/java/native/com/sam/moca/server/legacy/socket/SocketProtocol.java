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

package com.sam.moca.server.legacy.socket;

/**
 * Constants and values used for the streaming MOCA native protocol.
 * 
 * Copyright (c) 2016 Sam Corporation All Rights Reserved
 * 
 * @author dinksett
 */
class SocketProtocol {
   
    static final byte REQUEST_INDICATOR = 0x40;
    static final byte RESPONSE_INDICATOR = 0x41;
    static final byte SHUTDOWN_INDICATOR = 0x42;
    
    static final byte RESPONSE_TYPE_NORMAL = 0x7d;
    static final byte RESPONSE_TYPE_EXCEPTION = 0x7e;
}
