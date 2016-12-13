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

package com.redprairie.moca.components.repository;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.server.SecurityLevel;
import com.redprairie.moca.server.exec.DefaultServerContext;
import com.redprairie.moca.server.repository.ArgType;
import com.redprairie.moca.server.repository.ArgumentInfo;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.JavaCommand;
import com.redprairie.moca.server.repository.MocaCommandRepository;
import com.redprairie.moca.server.repository.MocaTrigger;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * This class is to test the RepositoryService class as well as the moca
 * commands in the mocarepository component level.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_RepositoryService extends AbstractMocaTestCase {

    /**
     * This test is to confirm that calling list active commands will work
     * correctly
     * @throws MocaException This is thrown if any unexpected Moca exception is
     *         encountered while executing the command
     */
    public void testRetrievingAllCommands() throws MocaException {
        MocaResults res = _moca.executeCommand("list active commands");
        
        // There should be at least 1 available command
        assertTrue("There are no available commands?", 
                res.getRowCount() > 0);
    }
    
    /**
     * This tests to make sure that if we setup the repository with a command
     * that it will retrieve that and give us back the values correctly
     */
    public void testRetrieveSingleCommandWithInjectedRepository() {
        ComponentLevel javaComponentLevel = new ComponentLevel("MOCAjava");
        javaComponentLevel.setSortseq(1234);
        JavaCommand javaCommand = new JavaCommand("java command", 
                javaComponentLevel);
        javaCommand.setClassName("SomeClass");
        javaCommand.setMethod("someMethod");
        javaCommand.setSecurityLevel(SecurityLevel.PUBLIC);
        
        // Now we create our repository and throw the command and component
        // level into it
        MocaCommandRepository repos = new MocaCommandRepository();
        repos.addLevel(javaComponentLevel);
        repos.addCommand(javaCommand);           
            
        // Create a new DefaultServerContext but only provide the repository
        DefaultServerContext serverContext = new DefaultServerContext(null,
                null, null, null, null, repos, null, null, null, null, null);
        
        // Get the linked moca context from the serverContext
        MocaContext mocaContext = serverContext.getComponentContext();
        
        MocaResults res = DefaultServerContext.listCommands(mocaContext, null);
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("There should be 1 row", rowIter.next());
        
        // The values we supplied should all match now
        assertEquals("MOCAjava", rowIter.getString("cmplvl"));
        assertEquals(1234, rowIter.getInt("cmplvlseq"));
        assertEquals("java command", rowIter.getString("command"));
        assertEquals("SomeClass", rowIter.getString("class"));
        assertEquals("someMethod", rowIter.getString("functn"));
        assertEquals("J", rowIter.getString("cmdtyp"));
        assertEquals("Java Method", rowIter.getString("type"));
        assertEquals("PUBLIC", rowIter.getString("security"));
        
        assertFalse("There should only be 1 row", rowIter.next());
    }
    
    /**
     * This test is to confirm that calling list active command arguments will 
     * work correctly
     * @throws MocaException This is thrown if any unexpected Moca exception is
     *         encountered while executing the command
     */
    public void testRetrievingAllCommandArguments() throws MocaException {
        MocaResults res = _moca.executeCommand("list active command arguments");
        
        // There should be at least 1 available command argument
        assertTrue("There are no available command arguments?", 
                res.getRowCount() > 0);
    }
    
    /**
     * This tests to make sure that if we setup the repository with a command
     * that it will retrieve that and give us back the values correctly
     */
    public void testRetrieveSingleCommandArgumentsWithInjectedRepository() {
        ComponentLevel javaComponentLevel = new ComponentLevel("MOCAjava");
        JavaCommand javaCommand = new JavaCommand("java command", 
                javaComponentLevel);
        
        ArgumentInfo argument1 = new ArgumentInfo("foo", "arg1", ArgType.STRING, 
                "default", false);
        ArgumentInfo argument2 = new ArgumentInfo("bar", null, ArgType.FLOAT, 
                null, true);
        javaCommand.addArgument(argument1);
        javaCommand.addArgument(argument2);
        
        // Now we create our repository and throw the command and component
        // level into it
        MocaCommandRepository repos = new MocaCommandRepository();
        repos.addLevel(javaComponentLevel);
        repos.addCommand(javaCommand);
        
        // Create a new DefaultServerContext but only provide the repository
        DefaultServerContext serverContext = new DefaultServerContext(null,
                null, null, null, null, repos, null, null, null, null, 
                null);
        
        // Get the linked moca context from the serverContext
        MocaContext mocaContext = serverContext.getComponentContext();
        
        MocaResults res = DefaultServerContext.listCommandArguments(
                mocaContext, null);
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("There should be 2 rows", rowIter.next());
        
        // The values we supplied should all match now
        assertEquals("MOCAjava", rowIter.getString("cmplvl"));
        assertEquals("java command", rowIter.getString("command"));
        assertEquals("foo", rowIter.getString("argnam"));
        assertEquals("arg1", rowIter.getString("altnam"));
        assertEquals(ArgType.STRING.toString(), rowIter.getString("argtyp"));
        assertEquals("default", rowIter.getString("fixval"));
        assertEquals(0, rowIter.getInt("argidx"));
        assertFalse(rowIter.getBoolean("argreq"));
        
        assertTrue("There should be 2 rows", rowIter.next());
        
        // The values we supplied should all match now
        assertEquals("MOCAjava", rowIter.getString("cmplvl"));
        assertEquals("java command", rowIter.getString("command"));
        assertEquals("bar", rowIter.getString("argnam"));
        assertNull(rowIter.getString("altnam"));
        assertEquals(ArgType.FLOAT.toString(), rowIter.getString("argtyp"));
        assertNull(rowIter.getString("fixval"));
        assertEquals(1, rowIter.getInt("argidx"));
        assertTrue(rowIter.getBoolean("argreq"));
        
        assertFalse("There should only be 2 rows", rowIter.next());
    }
    
    /**
     * This test is to confirm that calling list active triggers will work
     * correctly
     * @throws MocaException This is thrown if any unexpected Moca exception is
     *         encountered while executing the command
     */
    public void testRetrievingAllTriggers() throws MocaException {
        try {
            _moca.executeCommand("list active triggers");
        }
        catch (NotFoundException ignore) {
            // This can happen since MOCA has not triggers
        }
        
        // We would like to verify there was at least a row returned, but 
        // unfortunately MOCA doesn't have any triggers in it
    }
    
    /**
     * This tests to make sure that if we setup the repository with a trigger
     * that it will retrieve that and give us back the values correctly
     */
    public void testRetrieveSingleTriggerWithInjectedRepository() {
        ComponentLevel javaComponentLevel = new ComponentLevel("MOCAjava");
        MocaTrigger trigger = new MocaTrigger("trigger", "some command");
        trigger.setFireSequence(10);
        String syntax = "publish data where foo = 'bar'";
        trigger.setSyntax(syntax);
        
        // Now we create our repository and throw the command and component
        // level into it
        MocaCommandRepository repos = new MocaCommandRepository();
        repos.addLevel(javaComponentLevel);
        repos.addTrigger(trigger);
        
        // Create a new DefaultServerContext but only provide the repository
        DefaultServerContext serverContext = new DefaultServerContext(null,
                null, null, null, null, repos, null, null, null, null, 
                null);
        
        // Get the linked moca context from the serverContext
        MocaContext mocaContext = serverContext.getComponentContext();
        
        MocaResults res = DefaultServerContext.listTriggers(
                mocaContext, null);
        
        RowIterator rowIter = res.getRows();
        
        assertTrue("There should be 1 row1", rowIter.next());
        
        // The values we supplied should all match now
        assertEquals("some command", rowIter.getString("command"));
        assertEquals(10, rowIter.getInt("trgseq"));
        assertEquals(syntax, rowIter.getString("syntax"));
        
        assertFalse("There should only be 1 row", rowIter.next());
    }
    
    /**
     * This test is to make sure that when no commands are returned we get a
     * NotFoundException.
     * @throws MocaException 
     */
    public void testNotFoundExceptionWhenNoCommandsAreReturned() throws MocaException {
        String shouldNotExist = "A19fak3";
        try {
            _moca.executeCommand("list active commands" +
            		         "  where command = 'A19fak3'");
            fail("The command : " + shouldNotExist + " was assumed to not exist");
        }
        catch (NotFoundException e) {
            // Should go in here
        }
    }
    
    /**
     * This test is to make sure that when no arguments are returned we get a
     * NotFoundException.
     * @throws MocaException 
     */
    public void testNotFoundExceptionWhenNoArgumentsAreReturned() throws MocaException {
        String shouldNotExist = "A19fak3";
        try {
            _moca.executeCommand("list active command arguments" +
                                 "  where command = 'A19fak3'");
            fail("The command : " + shouldNotExist + " was assumed to not exist");
        }
        catch (NotFoundException e) {
            // Should go in here
        }
    }
    
    /**
     * This test is to make sure that when no triggers are returned we get a
     * NotFoundException.
     * @throws MocaException 
     */
    public void testNotFoundExceptionWhenNoTriggersAreReturned() throws MocaException {
        String shouldNotExist = "A19fak3";
        try {
            _moca.executeCommand("list active triggers" +
                                 "  where command = 'A19fak3'");
            fail("The command : " + shouldNotExist + " was assumed to not exist");
        }
        catch (NotFoundException e) {
            // Should go in here
        }
    }
    
    /**
     * This test is to make sure that we correctly receive the list of active component levels
     * *
     * @throws MocaException 
     */
    public void testListActiveComponentLevels() throws MocaException {
        MocaResults res = null;
        res = _moca.executeCommand("list active levels");
        
        assertTrue("We should have at least 1 active component level.",res.getRowCount() > 0);
        
    }
    
    
    /**
     * This test is to make sure that we correctly validate
     * the trigger file is in a valid format.
     * 
     * @throws MocaException, IOException 
     */
    public void testValidateValidCommand() throws MocaException, IOException{
        String data = readResourceFile("validcommand.mcmd");
        _moca.executeCommand(
            "validate command where data=@data and cmplvl=@cmplvl",
                new MocaArgument("data", data), 
                    new MocaArgument("cmplvl", "mocabase"));
    }
    
    /**
     * This test is to make sure that we correctly throw a MocaException when
     * the command file is a invalid format.
     * *
     * @throws IOException 
     */
    public void testValidateInvalidCommand() throws IOException{
        String data = readResourceFile("invalidcommand.mcmd");
        try{
            _moca.executeCommand(
                "validate command where data=@data and cmplvl=@cmplvl",
                    new MocaArgument("data", data), 
                        new MocaArgument("cmplvl", "mocabase"));
            fail("Should not validate an invalid command.");
        }catch(MocaException e){
            //Should go here since the command is invalid.
        }
    }
    
    /**
     * This test is to make sure that we correctly validate
     * the trigger file is in a valid format.
     * 
     * @throws MocaException, IOException 
     */
    public void testValidateValidTrigger() throws MocaException, IOException{
        String data = readResourceFile("validtrigger.mtrg");
        _moca.executeCommand(
            "validate trigger where data=@data and cmplvl=@cmplvl",
                new MocaArgument("data", data), 
                    new MocaArgument("cmplvl", "mocabase"));
    }
    
    /**
     * This test is to make sure that we correctly throw a MocaException when
     * the trigger file is an invalid format.
     * 
     * @throws IOException 
     */
    public void testValidateInvalidTrigger() throws IOException{
        String data = readResourceFile("invalidtrigger.mtrg");
        try{
            _moca.executeCommand(
                "validate trigger where data=@data and cmplvl=@cmplvl",
                    new MocaArgument("data", data), 
                        new MocaArgument("cmplvl", "mocabase"));
            fail("Should not validate an invalid trigger.");
        }catch(MocaException e){
            //Should go here since the trigger is invalid.
        }
    }
    
    /***
     * Private method to readResource files from the test directory.
     * 
     * @param filename
     * @return
     * @throws IOException
     */
    private String readResourceFile(String filename) throws IOException{
        URL url = getClass().getResource("test/" + filename);
        StringBuilder data = new StringBuilder();
        BufferedReader bufReader = null;
        try {
            bufReader = new BufferedReader(new InputStreamReader(
                new FileInputStream(url.getFile()), "UTF-8"));
            String input;
            // Now we loop through the buffer
            while ((input = bufReader.readLine()) != null) {
                data.append(input.trim() + "\n");
            }
        }
        finally {
            if (bufReader != null) {
                bufReader.close();      
            }
        }
        return data.toString();

    }
    
}
