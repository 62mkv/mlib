/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.redprairie.moca.server.repository.file.xml;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.Trigger;
import com.redprairie.moca.server.repository.file.RepositoryReadException;
import com.redprairie.moca.server.repository.file.StubRepositoryReaderEvents;

/**
 * Test the functionality of the TriggerFile class
 * 
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author cjolly
 * @version $Revision$
 */
public class TU_XMLTriggerReader extends TestCase {

    public void testBasicTrigger() throws Exception {
        Trigger trg = readTriggerFromResource("test/close_receive_truck-process_anniversary_storage_billing.mtrg");
        assertEquals("process_anniversary_storage_billing", trg.getName());
        assertEquals("close receive truck", trg.getCommand());
        assertEquals(4000, trg.getFireSequence());
        assertTrue(trg.getSyntax().trim().startsWith("get installed configuration")); 
        assertTrue(trg.getSyntax().contains("if (@installed=1)"));
    }
   
    public void testIncompleteTrigger() throws Exception {
        try {
            readTriggerFromResource("test/incomplete.mtrg");
            fail("Expected XML error loading trigger file");
        }
        catch (RepositoryReadException e) {
            // Normal
        }
    }
   
    private Trigger readTriggerFromResource(String resource) throws IOException, RepositoryReadException {
        InputStream in = TU_XMLTriggerReader.class.getResourceAsStream(resource);
        try {
            XMLTriggerReader reader = new XMLTriggerReader(new StubRepositoryReaderEvents());
            Trigger trigger = reader.read(in, new ComponentLevel("test"));
            return trigger;
        }
        finally {
            in.close();
        }
    }

}
