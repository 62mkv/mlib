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

package com.redprairie.moca.test;

import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.exceptions.UniqueConstraintException;

import com.redprairie.mad.client.MadFactory;

/**
 * Test class to hold testing web service endpoints.
 * 
 * Copyright (c) 2012 RedPrairie Corporation All Rights Reserved
 * 
 * @author klehrke
 */
@Controller
public class TestService {

    @RequestMapping(value = "var/{varToken}", method = RequestMethod.GET)
    public @ResponseBody
    String getVar(MocaContext moca, @PathVariable String varToken) throws MocaException {
        return moca.getSystemVariable(varToken);
    }

    @RequestMapping(value = "getObjectNotFound/{varToken}", method = RequestMethod.GET)
    public @ResponseBody
    String getObjectNotFound(@PathVariable String varToken) throws MocaException {
        throw new NotFoundException(NotFoundException.SERVER_CODE);
    }

    @RequestMapping(value = "getObjectConstraint/{varToken}", method = RequestMethod.GET)
    public @ResponseBody
    String getObjectConstraint(@PathVariable String varToken) throws MocaException {
        throw new UniqueConstraintException(varToken);
    }

    @RequestMapping(value = "getTestDate", method = RequestMethod.POST)
    public @ResponseBody
    Date getObjectConstraint(@RequestBody Date varToken) {
        return varToken;
    }

    @RequestMapping(value="getMocaException/{varToken}", method=RequestMethod.GET)
    public @ResponseBody void getMocaException(@PathVariable int varToken) throws MocaException {
        throw new MocaException(varToken);
    }
    
    @RequestMapping(value="noop", method=RequestMethod.GET)
    public @ResponseBody void noop() throws MocaException {
    }

    @RequestMapping(value = "getMadFactory", method = RequestMethod.GET)
    public @ResponseBody
    MadFactory getMadFactory(MadFactory factory) {
        return factory;
    }
}
