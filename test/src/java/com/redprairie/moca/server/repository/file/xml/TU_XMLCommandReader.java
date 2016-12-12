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
import java.util.List;

import junit.framework.TestCase;

import com.redprairie.moca.server.SecurityLevel;
import com.redprairie.moca.server.repository.ArgType;
import com.redprairie.moca.server.repository.ArgumentInfo;
import com.redprairie.moca.server.repository.CFunctionCommand;
import com.redprairie.moca.server.repository.Command;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.LocalSyntaxCommand;
import com.redprairie.moca.server.repository.file.RepositoryReadException;
import com.redprairie.moca.server.repository.file.StubRepositoryReaderEvents;

/**
 * TODO Class Description
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author cjolly
 * @version $Revision$
 */
public class TU_XMLCommandReader extends TestCase {

    public void testCompleteCCommand() throws Exception {
        ComponentLevel testLevel = new ComponentLevel("foo");
        testLevel.setLibrary("foolib");
        Command cmd = readCommandFromResource("test/console_command.mcmd", testLevel);
        
        assertTrue(cmd instanceof CFunctionCommand);
        assertEquals("console command", cmd.getName());
        assertEquals("Execute a console command.", cmd.getDescription());
        assertEquals("mocaConsoleCommand", ((CFunctionCommand)cmd).getFunction());
        assertEquals(SecurityLevel.ADMIN, cmd.getSecurityLevel());

        assertEquals(testLevel, cmd.getLevel());
        List<ArgumentInfo> args = cmd.getArguments();
        assertEquals(5, args.size());
        assertEquals("command", args.get(0).getName());
        assertEquals("text", args.get(0).getAlias());
        assertEquals(ArgType.STRING, args.get(0).getDatatype());
        assertEquals("host", args.get(1).getName());
        assertEquals(ArgType.STRING, args.get(1).getDatatype());
        assertEquals("port", args.get(2).getName());
        assertEquals(ArgType.INTEGER, args.get(2).getDatatype());
        assertEquals("password", args.get(3).getName());
        assertEquals(ArgType.STRING, args.get(3).getDatatype());
        assertEquals("section", args.get(4).getName());
        assertEquals(ArgType.STRING, args.get(4).getDatatype());
    }
    
    public void testCompleteLocalSyntaxCommand() throws Exception {
        ComponentLevel testLevel = new ComponentLevel("foo");
        Command cmd = readCommandFromResource("test/sample_local_syntax.mcmd", testLevel);
        
        assertTrue(cmd instanceof LocalSyntaxCommand);
        assertEquals("sample local syntax", cmd.getName());
        assertEquals("LS Command", cmd.getDescription());
        assertEquals("\ntest test test\n", ((LocalSyntaxCommand)cmd).getSyntax());
        assertEquals(testLevel, cmd.getLevel());
    }
    
    public void testInsecureLocalSyntaxCommand() throws Exception {
        ComponentLevel testLevel = new ComponentLevel("foo");
        Command cmd = readCommandFromResource("test/insecure_local_syntax.mcmd", testLevel);
        
        assertTrue(cmd instanceof LocalSyntaxCommand);
        assertEquals("sample local syntax too", cmd.getName());
        assertEquals("LS Command Too", cmd.getDescription());
        assertEquals("\ntest test test\n", ((LocalSyntaxCommand)cmd).getSyntax());
        assertEquals(SecurityLevel.OPEN, cmd.getSecurityLevel());
        assertEquals(testLevel, cmd.getLevel());
    }
    
    private Command readCommandFromResource(String resource, ComponentLevel level) throws IOException, RepositoryReadException {
        InputStream in = TU_XMLCommandReader.class.getResourceAsStream(resource);
        try {
            XMLCommandReader reader = new XMLCommandReader(new StubRepositoryReaderEvents());
            Command command = reader.read(in, level);
            return command;
        }
        finally {
            if (in != null) in.close();
        }
    }
}
