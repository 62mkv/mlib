/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.mad.custom;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.redprairie.mad.protocol.MadMessageCustom;

/**
 * Tests ExecutorStatusHandler
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public class TU_ExecutorStatusHandler {
    @Test
    public void testParsing() throws IOException {
        ExecutorStatusHandler handler = new ExecutorStatusHandler();
        
        ExecutorStatusMessage msg = new ExecutorStatusMessage(
            ExecutorStatusHandler.CUSTOM_MESSAGE_TYPE, "sessionId", "status");
        
        byte[] body = handler.assemble(msg);
        
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(body));
        
        MadMessageCustom customMsg = handler.construct(dis.readInt(), dis);
        
        Assert.assertTrue(customMsg instanceof ExecutorStatusMessage);
       
        ExecutorStatusMessage parsedMsg = (ExecutorStatusMessage) customMsg;
        
        Assert.assertEquals(msg.getSessionId(), parsedMsg.getSessionId());
        Assert.assertEquals(msg.getStatus(), parsedMsg.getStatus());
    }
}
