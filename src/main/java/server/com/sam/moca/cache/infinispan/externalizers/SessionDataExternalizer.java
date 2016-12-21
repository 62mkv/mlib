/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.infinispan.io.UnsignedNumeric;
import org.infinispan.marshall.AdvancedExternalizer;
import org.infinispan.util.Util;

import com.sam.moca.server.session.SessionData;
import com.sam.moca.web.console.Authentication.Role;

/**
 * Infinispan specific externalizer used for SessionData object to reduce
 * serialized form.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SessionDataExternalizer implements AdvancedExternalizer<SessionData> {
    private static final long serialVersionUID = -3704229959439344260L;

    // @see org.infinispan.marshall.Externalizer#readObject(java.io.ObjectInput)
    @Override
    public SessionData readObject(ObjectInput in) throws IOException,
            ClassNotFoundException {
        long time = UnsignedNumeric.readUnsignedLong(in);
        Date createdDate = new Date(time);
        String sessionId = in.readUTF();
        Role role = null;
        if (in.readBoolean()) {
             role = (Role) in.readObject();
        }
        String userId = in.readUTF();
        
        int mapSize = UnsignedNumeric.readUnsignedInt(in);
        Map<String, String> env = new HashMap<String, String>(mapSize);
        for (int i = 0; i < mapSize; ++i) {
            String key = in.readUTF();
            boolean notNullString = in.readBoolean();
            String value = null;
            if (notNullString) {
                value = in.readUTF();
            }
            env.put(key, value);
        }
        return new SessionData(userId, role,
                sessionId, createdDate, env);
    }

    // @see org.infinispan.marshall.Externalizer#writeObject(java.io.ObjectOutput, java.lang.Object)
    @Override
    public void writeObject(ObjectOutput out, SessionData obj)
            throws IOException {
        // Created date is always non null
        UnsignedNumeric.writeUnsignedLong(out, obj.getCreatedDate().getTime());
        out.writeUTF(obj.getSessionId());
        boolean hasRole = obj.getRole() != null;
        out.writeBoolean(hasRole);
        if (hasRole) {
            out.writeObject(obj.getRole());
        }
        out.writeUTF(obj.getUserId());
        
        Map<String, String> env = obj.getEnvironment();
        UnsignedNumeric.writeUnsignedInt(out, env.size());
        
        for (Entry<String, String> entry : env.entrySet()) {
            out.writeUTF(entry.getKey());
            
            String value = entry.getValue();
            if (value != null) {
                out.writeBoolean(true);
                out.writeUTF(value);
            }
            else {
                out.writeBoolean(false);
            }
        }
    }

    // @see org.infinispan.marshall.AdvancedExternalizer#getTypeClasses()
    @SuppressWarnings("unchecked")
    @Override
    public Set<Class<? extends SessionData>> getTypeClasses() {
        return Util.<Class<? extends SessionData>>asSet(SessionData.class);
    }

    // @see org.infinispan.marshall.AdvancedExternalizer#getId()
    @Override
    public Integer getId() {
        return null;
    }
}
