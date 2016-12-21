/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.sam.moca.cache.infinispan.externalizers;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

import org.infinispan.io.UnsignedNumeric;
import org.infinispan.marshall.AdvancedExternalizer;
import org.infinispan.util.Util;

import com.sam.moca.server.InstanceUrl;

/**
 * Infinispan specific externalizer used for InstanceUrl object to reduce
 * serialized form.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class InstanceUrlExternalizer implements AdvancedExternalizer<InstanceUrl> {
    private static final long serialVersionUID = -2429684163156492702L;

    // @see org.infinispan.marshall.Externalizer#writeObject(java.io.ObjectOutput, java.lang.Object)
    @Override
    public void writeObject(ObjectOutput output, InstanceUrl object)
            throws IOException {
        output.writeBoolean(object.isSslProtocol());
        output.writeUTF(object.getHostName());
        UnsignedNumeric.writeUnsignedInt(output, object.getPort());
        String endPoint = object.getEndPoint();
        if (endPoint != null) {
            output.writeBoolean(true);
            output.writeUTF(endPoint);
        }
        else {
            output.writeBoolean(false);
        }
    }
    
    // @see org.infinispan.marshall.Externalizer#readObject(java.io.ObjectInput)
    @Override
    public InstanceUrl readObject(ObjectInput input) throws IOException,
            ClassNotFoundException {
        boolean ssl = input.readBoolean();
        String hostName = input.readUTF();
        int port = UnsignedNumeric.readUnsignedInt(input);
        boolean notNull = input.readBoolean();
        if (notNull) {
            return new InstanceUrl(ssl, hostName, port, input.readUTF());
        }
        else {
            return new InstanceUrl(ssl, hostName, port);
        }
    }
    
    // @see org.infinispan.marshall.AdvancedExternalizer#getTypeClasses()
    @SuppressWarnings("unchecked")
    @Override
    public Set<Class<? extends InstanceUrl>> getTypeClasses() {
        return Util.<Class<? extends InstanceUrl>>asSet(InstanceUrl.class);
    }
    
    // @see org.infinispan.marshall.AdvancedExternalizer#getId()
    @Override
    public Integer getId() {
        return null;
    }
}
