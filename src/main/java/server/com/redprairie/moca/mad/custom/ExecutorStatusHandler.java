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

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.redprairie.mad.protocol.CustomHandler;
import com.redprairie.mad.protocol.MadMessageCustom;
import com.redprairie.moca.mad.async.AsynchronousExecutorStatus;

/**
 * Handles messages for setting the status of an AsynchronousExecutor 
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author klucas
 */
public class ExecutorStatusHandler implements CustomHandler {
    public static final int CUSTOM_MESSAGE_TYPE = 1001;

    // @see com.redprairie.mad.protocol.CustomHandler#assemble(com.redprairie.mad.protocol.MadMessageCustom)
    @Override
    public byte[] assemble(MadMessageCustom customMsg) throws IOException {
        if (customMsg instanceof ExecutorStatusMessage) {
             ExecutorStatusMessage msg = (ExecutorStatusMessage) customMsg;
             
             ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(byteStream);
             
             out.writeInt(msg.getCustomType());
             out.writeUTF(msg.getSessionId());
             out.writeUTF(msg.getStatus());
            
             return byteStream.toByteArray();
        }
        
        throw new IOException();
    }

    // @see com.redprairie.mad.protocol.CustomHandler#construct(int, java.io.DataInputStream)
    @Override
    public MadMessageCustom construct(int customType, DataInputStream dis) throws IOException {
        String sessionId = dis.readUTF();
        String status = dis.readUTF();
        
        return new ExecutorStatusMessage(customType, sessionId, status);
    }

    // @see com.redprairie.mad.protocol.CustomHandler#process(com.redprairie.mad.protocol.MadMessageCustom)
    @Override
    public void process(MadMessageCustom customMsg) {
        if (customMsg instanceof ExecutorStatusMessage) {
            ExecutorStatusMessage msg = (ExecutorStatusMessage)customMsg;
            
            AsynchronousExecutorStatus.set(msg.getStatus(), msg.getSessionId());
        }
    }
}
