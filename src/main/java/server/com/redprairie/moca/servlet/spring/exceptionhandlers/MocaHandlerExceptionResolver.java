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

package com.redprairie.moca.servlet.spring.exceptionhandlers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.components.crud.PrimaryKeyExistsException;
import com.redprairie.moca.exceptions.UniqueConstraintException;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.dispatch.ErrorMessageBuilder;
import com.redprairie.moca.server.dispatch.MessageResolver;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.servlet.spring.views.SimpleExceptionView;
import com.redprairie.moca.util.MocaUtils;

/**
 * This is a very simple exception handler resolver that converts the exception
 * to a ModelAndView where the view just prints out the localized message
 * in plain text.
 * 
 * Copyright (c) 2012 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@Component(value="mocaExceptionResolver")
public class MocaHandlerExceptionResolver implements HandlerExceptionResolver {

    // @see org.springframework.web.servlet.HandlerExceptionResolver#resolveException(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
        HttpServletResponse response, Object handler, Exception ex) {

        if (ex instanceof MocaException) {
            MocaException me = (MocaException) ex;
            if (me instanceof NotFoundException) {
                _logger.debug(MocaUtils.concat(
                    "Exception raised from WS request ", request.getPathInfo(), " : ", me));
            }
            else {
                _logger.debug(MocaUtils.concat(
                    "Exception raised from WS request ", request.getPathInfo(), " : ", me), me);
            }
            
            ServerContext context = ServerUtils.getCurrentContext();
            MessageResolver resolver = context.getMessageResolver();
            ErrorMessageBuilder builder = new ErrorMessageBuilder(
                me, resolver);
            String convertedMessage = builder.getMessage();
            
            int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            
            if (ex instanceof NotFoundException) {
                status = HttpServletResponse.SC_NOT_FOUND;
            }
            else if (ex instanceof UniqueConstraintException
                    || ex instanceof PrimaryKeyExistsException) {
                status = HttpServletResponse.SC_CONFLICT;
            }
            
            return new ModelAndView(
                new SimpleExceptionView(status, me.getErrorCode()), "message",
                convertedMessage);
        }
        
        return null;
    }
    
    private static final Logger _logger = LogManager.getLogger(
            MocaHandlerExceptionResolver.class);
}
