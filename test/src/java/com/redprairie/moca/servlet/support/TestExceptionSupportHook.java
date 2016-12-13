/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.redprairie.moca.servlet.support;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaResults;

/**
 * A sample test Support Hook for testing.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author j1014843
 */
public class TestExceptionSupportHook implements SupportHook{

    // @see com.redprairie.moca.servlet.support.SupportHook#getName()
    
    @Override
    public String getName() {
        return "moca_test_exception";
    }

    // @see com.redprairie.moca.servlet.support.SupportHook#getType()
    
    @Override
    public SupportType getType() {
        return SupportType.MOCA_RESULTS;
    }

    // @see com.redprairie.moca.servlet.support.SupportHook#getMocaResults()
    
    @Override
    public MocaResults getMocaResults(MocaContext context) throws Exception {
        throw new Exception("Something bad happened");
    }

    // @see com.redprairie.moca.servlet.support.SupportHook#getString()
    
    @Override
    public String getString(MocaContext context) throws Exception {
        return null;
    }
}
