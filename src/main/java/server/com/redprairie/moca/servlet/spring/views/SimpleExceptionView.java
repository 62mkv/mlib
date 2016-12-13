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

package com.redprairie.moca.servlet.spring.views;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.view.AbstractView;

/**
 * This is a custom view that can be used for Exceptions, that has a single
 * element in the mode that describes the reason or error message.
 * 
 * The reason is returned in plain text so it is easier to grab for clients.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SimpleExceptionView extends AbstractView {
    
    private final int _errorStatus;
    private final Integer _headerStatus;
    
    public SimpleExceptionView() {
        this(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    
    public SimpleExceptionView(int errorStatus) {
        this(errorStatus, null);
    }
    
    /***
     * The constructor to give the error status and the MOCA status.
     * 
     * @param errorStatus
     * @param mocaStatus
     */
    public SimpleExceptionView(int errorStatus, Integer mocaStatus) {
        super();
        _errorStatus = errorStatus;
        _headerStatus = mocaStatus;
    }
    
    // @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    @Override
    protected void renderMergedOutputModel(Map<String, Object> model,
        HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        
        //Were we given the MOCA exception status? If so, add a header, otherwise don't.
        //The alternative would be to set it as the default value (or error status)
        //so we're aware a generic exception was given to us without a MOCA status 
        //available.
        if(_headerStatus != null){
            response.setHeader("moca-status", String.valueOf(_headerStatus));
        }
        
        Object message = model.get("message");
        if (message != null) {
            response.sendError(_errorStatus, 
                message.toString());
        }
        else {
            throw new RuntimeException("MocaExceptionView requires a message " +
                    "value in the view ");
        }

    }
}
