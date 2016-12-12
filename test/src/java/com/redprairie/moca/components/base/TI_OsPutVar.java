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

package com.redprairie.moca.components.base;

import org.junit.Test;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.util.AbstractMocaTestCase;


/**
 * A test class to test the C command put os variable
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class TI_OsPutVar extends AbstractMocaTestCase {

    @Test
    public void testOsPutVarSimple() throws MocaException {
        String firstValue = "foo";
        
       _moca.executeCommand("test set variable where name = @var and value = @usrid",
            new MocaArgument("var", "usrid"), new MocaArgument("usrid", firstValue));
        assertEquals(firstValue, _moca.getSystemVariable("usrid"));
    }
    
    @Test
    public void testOsPutVarTwoPuts() throws MocaException {
        String firstValue = "foo";
        String secondValue = "foobar";
        
       _moca.executeCommand("test set variable where name = @var and value = @usrid",
            new MocaArgument("var", "usrid"), new MocaArgument("usrid", firstValue));
        
        _moca.executeCommand("test set variable where name = @var and value = @usrid",
            new MocaArgument("var", "usrid"), new MocaArgument("usrid", secondValue));
        assertEquals(secondValue, _moca.getSystemVariable("usrid"));
    }

}
