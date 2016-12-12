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

package com.redprairie.moca.applications.msql;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import org.mockito.InOrder;
import org.mockito.Mockito;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.applications.msql.MsqlEventHandler.TraceType;
import com.redprairie.moca.client.MocaConnection;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * This test class is to test the various behaviors of msql.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_Msql extends AbstractMocaTestCase {
    
    public void testResultSetPrinting() {
        EditableResults results = _moca.newResults();
        
        results.addColumn("i", MocaType.INTEGER);
        results.addColumn("worked", MocaType.STRING);
        results.addColumn("something", MocaType.STRING);
        
        Msql msql = new Msql(true, false);
        
        MsqlEventHandler handler = Mockito.mock(MsqlEventHandler.class);
        msql.addEventHandler(handler);
        
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < 10; ++i) {
            results.addRow();
            results.setIntValue("i", i);
            results.setStringValue("worked", "fdafafafafa");
            results.setStringValue("something", "otherthingsssss");
            
            values.append(i + "    fdafafafafa  otherthingsssss\n");
        }
        
        msql.traceResults(results);
        
        InOrder order = Mockito.inOrder(handler);
        order.verify(handler).traceEvent(
            Mockito.eq("i    worked       something      \n"),
            Mockito.any(TraceType.class));
        order.verify(handler).traceEvent(
            Mockito.eq("---  -----------  ---------------\n"),
            Mockito.any(TraceType.class));
        
        order.verify(handler).traceEvent(
            Mockito.eq(values.toString()),
            Mockito.any(TraceType.class));
    }
    
    public void testEncodingFileSupport() throws IOException {
        String toExecute = "publish data where foo = 'ளுஸ்ரீ'";
        String encoding = "X-UTF-32BE-BOM";
        
        File tempFile = File.createTempFile("TU_Msql", "test");
        
        Writer writer = new OutputStreamWriter(new FileOutputStream(tempFile), 
            encoding);
        try {
            writer.write(toExecute);
            writer.write("\n/");
        }
        finally {
            try {
                writer.close();
            }
            catch (IOException e) {
                System.err.println("There was a problem closing file: " + tempFile);
                e.printStackTrace();
            }
        }
        
        MsqlEventHandler handler = Mockito.mock(MsqlEventHandler.class);
        
        Msql msql = new Msql(false, true);
        msql.setCharset(Charset.forName(encoding));
        
        msql.addEventHandler(handler);
        
        Reader reader = new StringReader("@" + tempFile.getAbsolutePath());
        
        testErrorReturned(msql, reader);
        
        reader = new StringReader("@" + tempFile.getAbsolutePath());
        
        testErrorReturned(msql, reader);
        
        Mockito.verify(handler, Mockito.times(2)).traceEvent(Mockito.contains(
            toExecute), Mockito.any(TraceType.class));
    }
    
    /**
     * This tests if a user does something like an edit which contains
     * a forward slash in a string
     * @throws MocaException
     * @throws IOException
     */
    public void testSlashInComments() throws MocaException, IOException {
        MocaConnection connection = Mockito.mock(MocaConnection.class);
        Msql msql = new Msql(connection, false, true);
        
        StringReader reader = new StringReader("publish data where foo = '\n" +
                        "/'\n" +
                        "/");
        testErrorReturned(msql, reader);
        
        Mockito.verify(connection).executeCommand("publish data where foo = '\n/'");
    }
    
    /**
     * We never supported this in C, so for compatibility and also since it
     * would be much more difficult to fix
     * @throws MocaException
     * @throws IOException
     */
    public void testSlashInCommentsByItself() throws MocaException, IOException {
        MocaConnection connection = Mockito.mock(MocaConnection.class);
        Msql msql = new Msql(connection, false, true);
        
        StringReader reader = new StringReader("publish data where foo = '\n" +
                        "/\n" +
                        "'\n" +
                        "/");
        testErrorReturned(msql, reader);
        
        Mockito.verify(connection).executeCommand("publish data where foo = '");
        Mockito.verify(connection).executeCommand("'");
    }
    
    /**
     * Non Interactive should throw the not found error
     * @throws MocaException
     * @throws IOException
     */
    public void testErrorOnNotFoundNonInteractive() throws MocaException, IOException {
        MocaConnection connection = Mockito.mock(MocaConnection.class);
        NotFoundException excp = Mockito.mock(NotFoundException.class);
        
        Mockito.when(connection.executeCommand("throw not found")).thenThrow(excp);
        Msql msql = new Msql(connection, false, false);
        
        StringReader reader = new StringReader("throw not found\n" +
                        "/");
        testErrorReturned(msql, reader, excp);
    }
    
    /**
     * Interactive shouldn't throw the error but just print a message
     * @throws MocaException
     * @throws IOException
     */
    public void testNoErrorOnNotFoundInteractive() throws MocaException, IOException {
        MocaConnection connection = Mockito.mock(MocaConnection.class);
        NotFoundException excp = Mockito.mock(NotFoundException.class);
        
        Mockito.when(connection.executeCommand("throw not found")).thenThrow(excp);
        Msql msql = new Msql(connection, false, true);
        
        StringReader reader = new StringReader("throw not found\n" +
                        "/");
        testErrorReturned(msql, reader);
    }
    
    /**
     * This will execute the given reader on msql, then verify the exceptions
     * returned are what occurred.  If none are provided then the test will
     * fail if an exception is raised
     * @param msql
     * @param expected
     * @throws IOException 
     */
    private static void testErrorReturned(Msql msql, Reader reader, 
        MocaException... expected) throws IOException {
        
        List<MocaException> excps = msql.executeCommands(reader);
        
        assertEquals(expected.length, excps.size());
        
        for (int i = 0; i < expected.length; ++i) {
            assertEquals("Exception at index " + i + " doesn't match", 
                expected[i], excps.get(i));
        }
    }
}
