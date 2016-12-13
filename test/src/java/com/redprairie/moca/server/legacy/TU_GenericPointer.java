/*
 *  $URL$
 *  $Revision$
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

package com.redprairie.moca.server.legacy;

import org.junit.Test;

import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.TypeMismatchException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * This class tests some of the functionalilty of the NativeReturnStruct class.  Notably, it tests the serialization
 * of that class.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_GenericPointer {
    @Test 
    public void testSimpleValues() throws Exception {
        testLongValue(0L);
        testLongValue(1L);
        testLongValue(-1L);
        testLongValue(0xdeadbeefL);
        testLongValue(-0xdeadbeefL);
        testLongValue(0xffffffffdeadbeefL);
    }
    
    private void testLongValue(long l) {
        GenericPointer p = new GenericPointer(l);
        String stringValue = p.toString();
        
        try {
            GenericPointer newValue = (GenericPointer) ServerUtils.copyArg(new MocaValue(MocaType.STRING, stringValue), MocaType.GENERIC);
            assertEquals("value: " + l + "(" + p + ")", newValue, p);
        }
        catch (TypeMismatchException e) {
            fail("value: " + l + "(" + p + "): " + e);
        }
    }
}
