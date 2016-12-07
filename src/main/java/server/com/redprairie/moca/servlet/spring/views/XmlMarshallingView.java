/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.redprairie.moca.servlet.spring.views;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;

import org.springframework.oxm.Marshaller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.xml.MarshallingView;

/**
 * Custom xml marshalling view that allows multi value model.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class XmlMarshallingView extends MarshallingView {
    /**
     * @param marshaller
     */
    public XmlMarshallingView(Marshaller marshaller) {
        super(marshaller);
    }

    // @see org.springframework.web.servlet.view.xml.MarshallingView#setModelKey(java.lang.String)
    @Override
    public void setModelKey(String modelKey) {
        _modelKeys.add(modelKey);
    }
    
    /**
     * Set whether to serialize models containing a single attribute as a map or whether to
     * extract the single value from the model and serialize it directly.
     * <p>The effect of setting this flag is similar to using {@code MappingJacksonHttpMessageConverter}
     * with an {@code @ResponseBody} request-handling method.
     * <p>Default is {@code false}.
     */
    public void setExtractValueFromSingleKeyModel(boolean extractValueFromSingleKeyModel) {
        _extractValueFromSingleKeyModel = extractValueFromSingleKeyModel;
    }
    
    // @see org.springframework.web.servlet.view.xml.MarshallingView#locateToBeMarshalled(java.util.Map)
    @Override
    protected Object locateToBeMarshalled(Map<String, Object> model)
            throws ServletException {
        Map<String, Object> result = new HashMap<String, Object>(model.size());
        Set<String> renderedAttributes = (!CollectionUtils.isEmpty(_modelKeys) ? 
                _modelKeys : model.keySet());
        for (Map.Entry<String, Object> entry : model.entrySet()) {
                if (!(entry.getValue() instanceof BindingResult) && 
                        renderedAttributes.contains(entry.getKey())) {
                        result.put(entry.getKey(), entry.getValue());
                }
        }
        return (_extractValueFromSingleKeyModel && result.size() == 1 ? 
                result.values().iterator().next() : result);
    }
    
    protected boolean _extractValueFromSingleKeyModel = false;
    
    protected Set<String> _modelKeys;
}
