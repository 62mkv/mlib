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

package com.sam.moca.servlet.spring.marshallers;

import org.springframework.oxm.xstream.XStreamMarshaller;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * This is an extension of the XStreamMarshaller so that we can get to the
 * MapperWrapper to wrap it with the HibernateMapper.
 * 
 * Copyright (c) 2012 Sam Corporation All Rights Reserved
 * 
 * @author klehrke
 */
public class HibernateMapperXStreamMarshaller extends XStreamMarshaller {

    private final XStream xstream = new XStream() {

        protected MapperWrapper wrapMapper(final MapperWrapper next) {
            return new HibernateMapper(next);
        }
    };

    // @see org.springframework.oxm.xstream.XStreamMarshaller#getXStream()
    @Override
    public XStream getXStream() {
        return xstream;
    }
}
