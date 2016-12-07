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

package com.redprairie.moca.servlet.jackson.handlers;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializationProblemHandler;
import org.codehaus.jackson.map.JsonDeserializer;

/**
 * A class to handle the unknown properties not known to the pojo.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class UnknownPropertyProblemHandler extends
        DeserializationProblemHandler {

    // @see org.codehaus.jackson.map.DeserializationProblemHandler#handleUnknownProperty(org.codehaus.jackson.map.DeserializationContext, org.codehaus.jackson.map.JsonDeserializer, java.lang.Object, java.lang.String)
    
    @Override
    public boolean handleUnknownProperty(DeserializationContext ctxt,
                                         JsonDeserializer<?> deserializer,
                                         Object beanOrClass, String propertyName)
            throws IOException, JsonProcessingException {
        
        log.info("Detected unknown property:  " + propertyName);
        return super.handleUnknownProperty(ctxt, deserializer, beanOrClass,
            propertyName);
    }
    
    private static Logger log = LogManager.getLogger(UnknownPropertyProblemHandler.class);

}
