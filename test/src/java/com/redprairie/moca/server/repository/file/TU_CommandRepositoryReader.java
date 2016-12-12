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

package com.redprairie.moca.server.repository.file;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;

import com.redprairie.moca.server.repository.Command;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.MocaTrigger;
import com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents;
import com.redprairie.moca.server.repository.file.xml.XMLRepositoryFileReaderFactory;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * TestUnit class written to test the functionality of the CommandRepositoryReader.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 */
public class TU_CommandRepositoryReader extends AbstractMocaTestCase {
    
    private static final String DIRECTORY_SEPARATOR = File.separator;
    
    /***
     * Unit test to ensure that we can receive an error for invalid information in 
     * readAll.
     * 
     */
    @Test
    public void testReadAllErrorReported(){
        RepositoryReaderEvents events = Mockito.mock(RepositoryReaderEvents.class);
        
        File[] directories = new File[1];
        File rootDirectory = Mockito.mock(File.class);
        directories[0] = rootDirectory;
        
        File[] levelFiles = new File[1];
        File level = new File("TestLevel");
        levelFiles[0] = level;
        
        Mockito.when(rootDirectory.exists()).thenReturn(true);
        Mockito.when(rootDirectory.listFiles((FilenameFilter) Mockito.any())).thenReturn(levelFiles);
        
        
        CommandRepositoryReader cmdRepReader = new CommandRepositoryReader(directories,events, new XMLRepositoryFileReaderFactory());
        cmdRepReader.readAll();
        
        assertEquals(cmdRepReader.getCommandsProcessed(), 0);
        assertEquals(cmdRepReader.getDirectoriesProcessed(), 1);
        assertEquals(cmdRepReader.getErrorCount(), 1);
        assertEquals(cmdRepReader.getLevelsProcessed(), 1);
        assertEquals(cmdRepReader.getTriggersProcessed(), 0);
        
        Mockito.verify(events, Mockito.times(1)).reportError(Mockito.anyString(),(Throwable)Mockito.any());
        Mockito.verify(events, Mockito.times(1)).finishedLoading();
    }
    
    /***
     * Unit test to ensure the accurate outcome of CommandRepositoryReader.readAll().
     * 
     * @throws RepositoryReadException
     * @throws IOException
     */
    @Test
    public void testReadAll() throws RepositoryReadException, IOException{
        //Setup mock files
        String directory = System.getProperty("java.io.tmpdir") + DIRECTORY_SEPARATOR + "temp" + DIRECTORY_SEPARATOR + "Doc";
        File dir = new File(directory);
        File levelDefinition = new File(dir, "TestLevel.mlvl" );
        File levelDir = new File(dir, "TestLevel");
        File levelCommand = new File(levelDir, "Command.mcmd");
        File levelTrigger = new File(levelDir, "Trigger.mtrg");
        
        try{
            dir.mkdirs();
            levelDefinition.createNewFile();
            levelDir.mkdir();
            levelCommand.createNewFile();
            levelTrigger.createNewFile();
            
            //RepositoryReaderEvents
            RepositoryReaderEvents events = Mockito.mock(RepositoryReaderEvents.class);
            
            File[] directories = {dir};
            
            //Setup the Readers
            CommandReader commandReader = Mockito.mock(CommandReader.class);
            
            ComponentLevel componentLevel = Mockito.mock(ComponentLevel.class);
            Mockito.when(componentLevel.getName()).thenReturn("TestLevel");
            
          //Setup the command to be read
            Command cmd = Mockito.mock(Command.class);
            Mockito.when(commandReader.read(Mockito.any(File.class), Mockito.any(ComponentLevel.class))).thenReturn(cmd);
            Mockito.when(cmd.getLevel()).thenReturn(componentLevel);
            Mockito.when(cmd.getName()).thenReturn("TestCommand");
            
            LevelReader levelReader = Mockito.mock(LevelReader.class);
            Mockito.when(levelReader.read(levelDefinition)).thenReturn(componentLevel);
            
            TriggerReader triggerReader = Mockito.mock(TriggerReader.class);
            
          //Set up the trigger
            MocaTrigger trig = Mockito.mock(MocaTrigger.class);
            Mockito.when(trig.getCommand()).thenReturn("TestCommand");
            Mockito.when(trig.getName()).thenReturn("TestTrigger");
            Mockito.when(trig.getFireSequence()).thenReturn(100);
            Mockito.when(trig.getSyntax()).thenReturn("noop");
            
            //Set up the Factories to pass mock objects
            RepositoryFileReaderFactory xmlFact = Mockito.mock(RepositoryFileReaderFactory.class);
            Mockito.when(xmlFact.getCommandReader(events)).thenReturn(commandReader);
            Mockito.when(xmlFact.getLevelReader(events)).thenReturn(levelReader);
            Mockito.when(xmlFact.getTriggerReader(events)).thenReturn(triggerReader);
            
            Mockito.when(triggerReader.read(Mockito.any(File.class), Mockito.any(ComponentLevel.class))).thenReturn(trig);
            
            //Call the methods we want to test
            CommandRepositoryReader cmdRepReader = new CommandRepositoryReader(directories,events, xmlFact);
            cmdRepReader.readAll();
            
            //Verify that we have the amount we suspect
            assertEquals(cmdRepReader.getCommandsProcessed(), 1);
            assertEquals(cmdRepReader.getDirectoriesProcessed(), 1);
            assertEquals(cmdRepReader.getErrorCount(), 0);
            assertEquals(cmdRepReader.getLevelsProcessed(), 1);
            assertEquals(cmdRepReader.getTriggersProcessed(), 1);
            Mockito.verify(events, Mockito.never()).reportError(Mockito.anyString(), (Throwable) Mockito.any());
            Mockito.verify(events, Mockito.times(1)).finishedLoading();
        }
        finally{
            //Cleanup
            levelDefinition.delete();
            levelCommand.delete();
            levelTrigger.delete();
            levelDir.delete();
            dir.delete();
        }
    }
}
