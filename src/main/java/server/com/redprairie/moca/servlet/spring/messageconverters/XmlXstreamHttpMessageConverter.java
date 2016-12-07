/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.servlet.spring.messageconverters;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.stereotype.Component;

/**
 * A basic HttpMessageConverter for converting objects to xml using XStream.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
@Component(value="xStreamHttpMessageConverter")
public class XmlXstreamHttpMessageConverter extends
        MarshallingHttpMessageConverter {
    
    @Autowired
    public XmlXstreamHttpMessageConverter(XStreamMarshaller marshall) {
        super(marshall);
        setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_XML,
            MediaType.TEXT_XML, new MediaType("application", "*+xml")));
    }
}
