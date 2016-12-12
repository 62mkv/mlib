/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.applications.createctl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.applications.msql.Msql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This class is to test the various methods available to the CreateControlFile
 * class and make sure its different permutations call the correct commands.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_CreateControlFile {

    /**
     * Test method for {@link com.redprairie.moca.applications.createctl.CreateControlFile#loadControlFiles(java.io.File, java.lang.String[])}.
     * @throws IOException 
     * @throws MocaException 
     */
    @Test
    public void testLoadControlFilesWorkedNoDirectory() throws IOException, MocaException {
        Msql mockMsql = Mockito.mock(Msql.class);
        
        CreateControlFile ctlFile = new CreateControlFile(mockMsql);
        
        String[] tables = new String[2];
        
        tables[0] = "tableA";
        tables[1] = "table2";
        
        Map<String, MocaException> exceptions = ctlFile.loadControlFiles(null, 
                tables);
        
        assertEquals("We should have not encountered an exception", 0, 
                exceptions.size());
        
        // We should have called to MSQL twice with the following commands
        Mockito.verify(mockMsql).executeCommand(
                "dump data where file_name = 'tablea.ctl' and dump_command = 'format control file where type = ''LOAD'' and table_name = ''tablea'' '");
        
        Mockito.verify(mockMsql).executeCommand(
                "dump data where file_name = 'table2.ctl' and dump_command = 'format control file where type = ''LOAD'' and table_name = ''table2'' '");
    }
    
    /**
     * Test method for {@link com.redprairie.moca.applications.createctl.CreateControlFile#loadControlFiles(java.io.File, java.lang.String[])}.
     * @throws IOException 
     * @throws MocaException 
     */
    @Test
    public void testLoadControlFilesWorkedDirectory() throws IOException, MocaException {
        Msql mockMsql = Mockito.mock(Msql.class);
        
        CreateControlFile ctlFile = new CreateControlFile(mockMsql);
        
        String[] tables = new String[2];
        
        tables[0] = "tableA";
        tables[1] = "table2";
        
        File directory = Mockito.mock(File.class);
        
        Mockito.when(directory.getCanonicalPath()).thenReturn("Some Directory");
        
        Map<String, MocaException> exceptions = ctlFile.loadControlFiles(directory, 
                tables);
        
        assertEquals("We should have not encountered an exception", 0, 
                exceptions.size());
        
        // We should have called to MSQL twice with the following commands
        Mockito.verify(mockMsql).executeCommand("dump data where file_name = '"
                + directory.getCanonicalPath() + "/tablea.ctl' and dump_command = 'format control file where type = ''LOAD'' and table_name = ''tablea'' '");
        
        Mockito.verify(mockMsql).executeCommand("dump data where file_name = '"
                + directory.getCanonicalPath() + "/table2.ctl' and dump_command = 'format control file where type = ''LOAD'' and table_name = ''table2'' '");
    }
    
    /**
     * Test method for {@link com.redprairie.moca.applications.createctl.CreateControlFile#loadControlFiles(java.io.File, java.lang.String[])}.
     * @throws IOException 
     * @throws MocaException 
     */
    @Test
    public void testLoadControlFilesOneException() throws IOException, MocaException {
        Msql mockMsql = Mockito.mock(Msql.class);
        
        CreateControlFile ctlFile = new CreateControlFile(mockMsql);
        
        MocaException mockException = Mockito.mock(MocaException.class);
        
        // The first table will return null where as the second will return an
        // exception
        Mockito.when(mockMsql.executeCommand(Mockito.contains("tablea")))
                .thenReturn(null);
        Mockito.when(mockMsql.executeCommand(Mockito.contains("table2")))
                .thenThrow(mockException);
        
        String[] tables = new String[2];
        
        tables[0] = "tableA";
        tables[1] = "table2";
        
        Map<String, MocaException> exceptions = ctlFile.loadControlFiles(null, 
                tables);
        
        assertEquals("We should have gotten an exception", 1, 
                exceptions.size());
        
        assertTrue(exceptions.containsKey(tables[1]));
        
        assertEquals(mockException, exceptions.get(tables[1]));
        
        // We should have called to MSQL twice with the following commands
        // The last should have thrown an exception which we checked already.
        Mockito.verify(mockMsql).executeCommand(
                "dump data where file_name = 'tablea.ctl' and dump_command = 'format control file where type = ''LOAD'' and table_name = ''tablea'' '");
        
        Mockito.verify(mockMsql).executeCommand(
                "dump data where file_name = 'table2.ctl' and dump_command = 'format control file where type = ''LOAD'' and table_name = ''table2'' '");
    }
    
    /**
     * Test method for {@link com.redprairie.moca.applications.createctl.CreateControlFile#loadControlFiles(java.io.File, java.lang.String[])}.
     * @throws IOException 
     * @throws MocaException 
     */
    @Test
    public void testLoadControlFilesAllException() throws IOException, MocaException {
        Msql mockMsql = Mockito.mock(Msql.class);
        
        CreateControlFile ctlFile = new CreateControlFile(mockMsql);
        
        MocaException mockException = Mockito.mock(MocaException.class);
        
        // We always fail
        Mockito.when(mockMsql.executeCommand(Mockito.anyString()))
                .thenThrow(mockException);
        
        final String[] tables = new String[3];
        
        tables[0] = "tableA";
        tables[1] = "table2";
        tables[2] = "tableX23aI";
        
        Map<String, MocaException> exceptions = ctlFile.loadControlFiles(null, 
                tables);
        
        assertEquals("We should have gotten an exception for all", tables.length, 
                exceptions.size());
        
        assertEquals(mockException, exceptions.get(tables[0]));
        assertEquals(mockException, exceptions.get(tables[1]));
        assertEquals(mockException, exceptions.get(tables[2]));
        
        // We should have called to MSQL thrice with the following commands
        // All should have thrown an exception which we checked already.
        Mockito.verify(mockMsql).executeCommand(
                "dump data where file_name = 'tablea.ctl' and dump_command = 'format control file where type = ''LOAD'' and table_name = ''tablea'' '");
        
        Mockito.verify(mockMsql).executeCommand(
                "dump data where file_name = 'table2.ctl' and dump_command = 'format control file where type = ''LOAD'' and table_name = ''table2'' '");
        
        Mockito.verify(mockMsql).executeCommand(
                "dump data where file_name = 'tablex23ai.ctl' and dump_command = 'format control file where type = ''LOAD'' and table_name = ''tablex23ai'' '");
    }

    /**
     * Test method for {@link com.redprairie.moca.applications.createctl.CreateControlFile#unloadControlFiles(java.io.File, java.lang.String, boolean, java.lang.String[])}.
     * @throws IOException 
     * @throws MocaException 
     */
    @Test
    public void testUnloadControlFilesWorkedNoArgs() throws IOException, MocaException {
        Msql mockMsql = Mockito.mock(Msql.class);
        
        CreateControlFile ctlFile = new CreateControlFile(mockMsql);
        
        String[] tables = new String[2];
        
        tables[0] = "tableA";
        tables[1] = "table2";
        
        Map<String, MocaException> exceptions = ctlFile.unloadControlFiles(null, 
                null, false, tables);
        
        assertEquals("We should have not encountered an exception", 0, 
                exceptions.size());
        
        // We should have called to MSQL twice with the following commands
        Mockito.verify(mockMsql).executeCommand(
                "dump data where file_name = 'tablea.ctl' and dump_command = 'format control file where type = ''UNLOAD'' and table_name = ''tablea'' '");
        
        Mockito.verify(mockMsql).executeCommand(
                "dump data where file_name = 'table2.ctl' and dump_command = 'format control file where type = ''UNLOAD'' and table_name = ''table2'' '");
    }
    
    /**
     * Test method for {@link com.redprairie.moca.applications.createctl.CreateControlFile#unloadControlFiles(java.io.File, java.lang.String, boolean, java.lang.String[])}.
     * @throws IOException 
     * @throws MocaException 
     */
    @Test
    public void testUnloadControlFilesWorkedDirectory() throws IOException, MocaException {
        Msql mockMsql = Mockito.mock(Msql.class);
        
        CreateControlFile ctlFile = new CreateControlFile(mockMsql);
        
        String[] tables = new String[2];
        
        tables[0] = "tableA";
        tables[1] = "table2";
        
        File directory = Mockito.mock(File.class);
        
        Mockito.when(directory.getCanonicalPath()).thenReturn("Some Directory");
        
        Map<String, MocaException> exceptions = ctlFile.unloadControlFiles(
                directory, null, false, tables);
        
        assertEquals("We should have not encountered an exception", 0, 
                exceptions.size());
        
        // We should have called to MSQL twice with the following commands
        Mockito.verify(mockMsql).executeCommand("dump data where file_name = '"
                + directory.getCanonicalPath() + "/tablea.ctl' and dump_command = 'format control file where type = ''UNLOAD'' and table_name = ''tablea'' '");
        
        Mockito.verify(mockMsql).executeCommand("dump data where file_name = '"
                + directory.getCanonicalPath() + "/table2.ctl' and dump_command = 'format control file where type = ''UNLOAD'' and table_name = ''table2'' '");
    }
    
    /**
     * Test method for {@link com.redprairie.moca.applications.createctl.CreateControlFile#unloadControlFiles(java.io.File, java.lang.String, boolean, java.lang.String[])}.
     * @throws IOException 
     * @throws MocaException 
     */
    @Test
    public void testUnloadControlFilesOneException() throws IOException, MocaException {
        Msql mockMsql = Mockito.mock(Msql.class);
        
        CreateControlFile ctlFile = new CreateControlFile(mockMsql);
        
        MocaException mockException = Mockito.mock(MocaException.class);
        
        // The first table will return null where as the second will return an
        // exception
        Mockito.when(mockMsql.executeCommand(Mockito.contains("tablea")))
                .thenReturn(null);
        Mockito.when(mockMsql.executeCommand(Mockito.contains("table2")))
                .thenThrow(mockException);
        
        String[] tables = new String[2];
        
        tables[0] = "tableA";
        tables[1] = "table2";
        
        Map<String, MocaException> exceptions = ctlFile.unloadControlFiles(null,
                null, false, tables);
        
        assertEquals("We should have gotten an exception", 1, 
                exceptions.size());
        
        assertTrue(exceptions.containsKey(tables[1]));
        
        assertEquals(mockException, exceptions.get(tables[1]));
        
        // We should have called to MSQL twice with the following commands
        // The last should have thrown an exception which we checked already.
        Mockito.verify(mockMsql).executeCommand(
                "dump data where file_name = 'tablea.ctl' and dump_command = 'format control file where type = ''UNLOAD'' and table_name = ''tablea'' '");
        
        Mockito.verify(mockMsql).executeCommand(
                "dump data where file_name = 'table2.ctl' and dump_command = 'format control file where type = ''UNLOAD'' and table_name = ''table2'' '");
    }
    
    /**
     * Test method for {@link com.redprairie.moca.applications.createctl.CreateControlFile#unloadControlFiles(java.io.File, java.lang.String, boolean, java.lang.String[])}.
     * @throws IOException 
     * @throws MocaException 
     */
    @Test
    public void testUnloadControlFilesAllException() throws IOException, MocaException {
        Msql mockMsql = Mockito.mock(Msql.class);
        
        CreateControlFile ctlFile = new CreateControlFile(mockMsql);
        
        MocaException mockException = Mockito.mock(MocaException.class);
        
        // We always fail
        Mockito.when(mockMsql.executeCommand(Mockito.anyString()))
                .thenThrow(mockException);
        
        final String[] tables = new String[3];
        
        tables[0] = "tableA";
        tables[1] = "table2";
        tables[2] = "tableX23aI";
        
        File directory = Mockito.mock(File.class);
        
        Mockito.when(directory.getCanonicalPath()).thenReturn("Some Directory");
        
        Map<String, MocaException> exceptions = ctlFile.unloadControlFiles(
                directory, null, false, tables);
        
        assertEquals("We should have gotten an exception for all", tables.length, 
                exceptions.size());
        
        assertEquals(mockException, exceptions.get(tables[0]));
        assertEquals(mockException, exceptions.get(tables[1]));
        assertEquals(mockException, exceptions.get(tables[2]));
        
        // We should have called to MSQL thrice with the following commands
        // All should have thrown an exception which we checked already.
        Mockito.verify(mockMsql).executeCommand("dump data where file_name = '"
                + directory.getCanonicalPath() + "/tablea.ctl' and dump_command = 'format control file where type = ''UNLOAD'' and table_name = ''tablea'' '");
        
        Mockito.verify(mockMsql).executeCommand("dump data where file_name = '"
                + directory.getCanonicalPath() + "/table2.ctl' and dump_command = 'format control file where type = ''UNLOAD'' and table_name = ''table2'' '");
        
        Mockito.verify(mockMsql).executeCommand("dump data where file_name = '"
                + directory.getCanonicalPath() + "/tablex23ai.ctl' and dump_command = 'format control file where type = ''UNLOAD'' and table_name = ''tablex23ai'' '");
    }
    
    /**
     * Test method for {@link com.redprairie.moca.applications.createctl.CreateControlFile#unloadControlFiles(java.io.File, java.lang.String, boolean, java.lang.String[])}.
     * @throws IOException 
     * @throws MocaException 
     */
    @Test
    public void testUnloadControlFilesAllOptionsSomeExceptions() throws IOException, MocaException {
        Msql mockMsql = Mockito.mock(Msql.class);
        
        CreateControlFile ctlFile = new CreateControlFile(mockMsql);
        
        MocaException mockException = Mockito.mock(MocaException.class);
        
        // We always fail if it has table2 in it
        Mockito.when(mockMsql.executeCommand(Mockito.anyString()))
                .thenReturn(null);
        Mockito.when(mockMsql.executeCommand(Mockito.contains("table2")))
                .thenThrow(mockException);
        
        final String[] tables = new String[4];
        
        tables[0] = "tableA";
        tables[1] = "table2"; // should throw exception
        tables[2] = "tableX23aI";
        tables[3] = "table25"; // should throw exception
        
        File directory = Mockito.mock(File.class);        
        Mockito.when(directory.getCanonicalPath()).thenReturn("Some Directory");
        
        String whereClause = "select foo, bar from table where foo = 'bar'";
        
        Map<String, MocaException> exceptions = ctlFile.unloadControlFiles(
                directory, whereClause, true, tables);
        
        assertEquals(2, exceptions.size());
        
        assertEquals(mockException, exceptions.get(tables[1]));
        assertEquals(mockException, exceptions.get(tables[3]));
        
        // This should have worked and then called the dump command
        Mockito.verify(mockMsql).executeCommand("dump data where file_name = '"
                + directory.getCanonicalPath() + "/tablea.ctl' and dump_command = 'format control file where type = ''UNLOAD'' and table_name = ''tablea'' '");
        Mockito.verify(mockMsql).executeCommand("dump data where file_name = '"
                + directory.getCanonicalPath() + "/tablea/tablea.csv' and dump_command = 'format control file where type = ''DATA'' and table_name = ''tablea'' and where_command = ''select foo, bar from table where foo = ''''bar'''''' '");
        
        // This should have thrown an exception when it was invoked
        Mockito.verify(mockMsql).executeCommand("dump data where file_name = '"
                + directory.getCanonicalPath() + "/table2.ctl' and dump_command = 'format control file where type = ''UNLOAD'' and table_name = ''table2'' '");
        
        Mockito.verify(mockMsql).executeCommand("dump data where file_name = '"
                + directory.getCanonicalPath() + "/tablex23ai.ctl' and dump_command = 'format control file where type = ''UNLOAD'' and table_name = ''tablex23ai'' '");
        Mockito.verify(mockMsql).executeCommand("dump data where file_name = '"
                + directory.getCanonicalPath() + "/tablex23ai/tablex23ai.csv' and dump_command = 'format control file where type = ''DATA'' and table_name = ''tablex23ai'' and where_command = ''select foo, bar from table where foo = ''''bar'''''' '");
        
        // This should have thrown an exception when it was invoked
        Mockito.verify(mockMsql).executeCommand("dump data where file_name = '"
                + directory.getCanonicalPath() + "/table25.ctl' and dump_command = 'format control file where type = ''UNLOAD'' and table_name = ''table25'' '");
    }
}
