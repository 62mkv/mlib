/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.server;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.legacy.GenericPointer;

import static org.junit.Assert.assertEquals;

/**
 * This class is used to test if ServerUtils is working
 * correctly
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author gvaneyck
 * @version $Revision$
 */
public class TU_ServerUtils {
    
    private static ServerContext previousContext = null;
    
    @BeforeClass public static void beforeTests() throws SystemConfigurationException {
        previousContext = ServerUtils.getCurrentContextNullable();
        // First we null out the current context
        ServerUtils.removeCurrentContext();
    }
    
    @AfterClass public static void afterTests() {
        // Now restore the context back to what it was
        ServerUtils.setCurrentContext(previousContext);
    }

    @Test
    public void testCopyArgWithLargeGenericDouble() throws MocaException {
        Double value = Double.valueOf((long)Integer.MAX_VALUE + 2);
        MocaValue in = new MocaValue(MocaType.DOUBLE, value);
        GenericPointer out = (GenericPointer)ServerUtils.copyArg(in, MocaType.GENERIC);

        assertEquals(value.longValue(), out.getValue());
    }
    
    @Test
    public void testAssociationLeak() {
        ServerContext context = Mockito.mock(ServerContext.class); 
        Mockito.when(context.getSession()).thenReturn(null);
        
        // Now set the current context multiple times.  This should result in
        // only a single thread association.
        for (int i = 0; i < 10000; i++) {
            ServerUtils.setCurrentContext(context);
        }
        
        List<Thread> threads = ServerUtils.getContextThreads(context);
        
        assertEquals("There should only be 1 thread associated", 1, threads.size());
    }
}
