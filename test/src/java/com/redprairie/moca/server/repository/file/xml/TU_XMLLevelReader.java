/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

package com.redprairie.moca.server.repository.file.xml;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.file.RepositoryReadException;
import com.redprairie.moca.server.repository.file.StubRepositoryReaderEvents;
/**
 * Unit test for <code>LevelFile</code> class.
 * 
 * <b><pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author cjolly
 * @version $Revision$
 */
public class TU_XMLLevelReader extends TestCase {

    /**
     * Test read/write for Level Files.
     * Test method for {@link com.redprairie.moca.server.repository.file.xml.XMLLevelReader#write(com.redprairie.moca.server.repository.ComponentLevel, java.io.File)}.
     */
    public void testSimpleCompleteLevel() throws Exception {
        ComponentLevel testLevel = readLevelFromResource("test/mocabase.mlvl");
        assertEquals("MOCAbase", testLevel.getName());
        assertEquals(0, testLevel.getSortseq());
        assertEquals("MOCAbase", testLevel.getLibrary());
        assertEquals("com.redprairie.moca.components.base", testLevel.getPackage());
        assertNull(testLevel.getProgid());
        assertFalse(testLevel.isEditable());
        assertEquals("MOCA Base Components", testLevel.getDescription());
    }
    
    public void testMinimalCompleteLevel() throws Exception {
        ComponentLevel testLevel = readLevelFromResource("test/minimal.mlvl");
        assertEquals("foo", testLevel.getName());
        assertEquals(0, testLevel.getSortseq());
        assertNull(testLevel.getLibrary());
        assertNull(testLevel.getPackage());
        assertNull(testLevel.getProgid());
        assertFalse(testLevel.isEditable());
        assertNull(testLevel.getDescription());
    }
   
    public void testIncompleteLevel() throws Exception {
        try {
            readLevelFromResource("test/incomplete.mlvl");
            fail("Expected XML error loading level file");
        }
        catch (RepositoryReadException e) {
            // Normal
        }
    }
   
    private ComponentLevel readLevelFromResource(String resource) throws IOException, RepositoryReadException {
        InputStream in = TU_XMLLevelReader.class.getResourceAsStream(resource);
        try {
            XMLLevelReader reader = new XMLLevelReader(new StubRepositoryReaderEvents());
            ComponentLevel testLevel = reader.read(in);
            return testLevel;
        }
        finally {
            in.close();
        }
    }
}
