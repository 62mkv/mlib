/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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
import java.util.Set;

import org.infinispan.marshall.AdvancedExternalizer;
import org.infinispan.util.Util;

import com.sam.moca.cache.Maybe;

/**
 * Infinispan specific externalizer used for Maybe object to reduce
 * serialized form.
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@SuppressWarnings("rawtypes")
public class MaybeExternalizer implements AdvancedExternalizer<Maybe> {
    private static final long serialVersionUID = -6837432627638429628L;

    // @see org.infinispan.marshall.Externalizer#writeObject(java.io.ObjectOutput, java.lang.Object)
    @Override
    public void writeObject(ObjectOutput output, Maybe object) throws IOException {
        output.writeObject(object.getValue());
    }

    // @see org.infinispan.marshall.Externalizer#readObject(java.io.ObjectInput)
    @Override
    public Maybe<?> readObject(ObjectInput input) throws IOException,
            ClassNotFoundException {
        Object obj = input.readObject();
        return new Maybe<Object>(obj);
    }

    // @see org.infinispan.marshall.AdvancedExternalizer#getTypeClasses()
    @SuppressWarnings("unchecked")
    @Override
    public Set<Class<? extends Maybe>> getTypeClasses() {
        return Util.<Class<? extends Maybe>>asSet(Maybe.class);
    }

    // @see org.infinispan.marshall.AdvancedExternalizer#getId()
    @Override
    public Integer getId() {
        return null;
    }
}
