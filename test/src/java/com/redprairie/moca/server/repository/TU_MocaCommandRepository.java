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

package com.redprairie.moca.server.repository;

import java.util.List;

import org.junit.Test;

import com.redprairie.moca.server.repository.file.RepositoryReadException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for CommandRepository.  The Command Repository is the container class that
 * holds the command definitions and keeps track of which commands are overriding
 * which other commands.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_MocaCommandRepository {

    /**
     * Test method for {@link com.redprairie.moca.server.repository.MocaCommandRepository#addLevel(com.redprairie.moca.server.repository.ComponentLevel)}.
     */
    @Test
    public void testLevelSequences() {
        MocaCommandRepository repos = new MocaCommandRepository();
        ComponentLevel fooLevel = new ComponentLevel("foo");
        fooLevel.setSortseq(100);
        repos.addLevel(fooLevel);
        ComponentLevel barLevel = new ComponentLevel("bar");
        barLevel.setSortseq(200);
        repos.addLevel(barLevel);
        ComponentLevel bazLevel = new ComponentLevel("baz");
        bazLevel.setSortseq(150);
        repos.addLevel(bazLevel);
        
        assertEquals(3, repos.getLevelCount());
        List<ComponentLevel> levels = repos.getLevels();
        assertEquals(fooLevel, levels.get(0));
        assertEquals(bazLevel, levels.get(1));
        assertEquals(barLevel, levels.get(2));
    }

    /**
     * Test method for {@link com.redprairie.moca.server.repository.MocaCommandRepository#addCommand(com.redprairie.moca.server.repository.Command)}.
     */
    @Test
    public void testTwoLevelsWithSameName() {
        MocaCommandRepository repos = new MocaCommandRepository();
        ComponentLevel fooLevel = new ComponentLevel("foo");
        fooLevel.setSortseq(100);
        repos.addLevel(fooLevel);
        ComponentLevel fooLevel2 = new ComponentLevel("foo");
        fooLevel2.setSortseq(200);
        repos.addLevel(fooLevel2);
        
        assertEquals(1, repos.getLevelCount());
        List<ComponentLevel> levels = repos.getLevels();
        assertEquals(fooLevel2, levels.get(0));
        
    }

    /**
     * Test method for {@link com.redprairie.moca.server.repository.MocaCommandRepository#addTrigger(com.redprairie.moca.server.repository.MocaTrigger)}.
     */
    @Test
    public void testCommandOverride() {
        MocaCommandRepository repos = new MocaCommandRepository();
        ComponentLevel fooLevel = new ComponentLevel("foo");
        fooLevel.setSortseq(100);
        repos.addLevel(fooLevel);
        ComponentLevel barLevel = new ComponentLevel("bar");
        barLevel.setSortseq(200);
        repos.addLevel(barLevel);
        
        Command baseCommand = new JavaCommand("do thing", fooLevel);
        Command overrideCommand = new LocalSyntaxCommand("do thing", barLevel);
        
        repos.addCommand(baseCommand);
        repos.addCommand(overrideCommand);
        
        List<Command> doThingCommands = repos.getCommandByName("do thing");
        
        assertEquals(2, doThingCommands.size());
        assertEquals(overrideCommand, doThingCommands.get(0));
        assertEquals(baseCommand, doThingCommands.get(1));
    }
    
    @Test
    public void testCommandOverrideDifferentCaseAndWhitespace() {
        MocaCommandRepository repos = new MocaCommandRepository();
        ComponentLevel fooLevel = new ComponentLevel("foo");
        fooLevel.setSortseq(100);
        repos.addLevel(fooLevel);
        ComponentLevel barLevel = new ComponentLevel("bar");
        barLevel.setSortseq(200);
        repos.addLevel(barLevel);
        
        Command baseCommand = new JavaCommand("DO THING", fooLevel);
        Command overrideCommand = new LocalSyntaxCommand("\nDo\nThing\n", barLevel);
        
        repos.addCommand(baseCommand);
        repos.addCommand(overrideCommand);
        
        List<Command> doThingCommands = repos.getCommandByName("do thing");
        assertEquals(2, doThingCommands.size());
        assertEquals(overrideCommand, doThingCommands.get(0));
        assertEquals(baseCommand, doThingCommands.get(1));

        List<Command> upperCaseCommands = repos.getCommandByName(MocaCommandRepository.normalizeName("DO  THING"));
        assertEquals(2, upperCaseCommands.size());
        assertEquals(overrideCommand, upperCaseCommands.get(0));
        assertEquals(baseCommand, upperCaseCommands.get(1));

        List<Command> whitespaceCommands = repos.getCommandByName(MocaCommandRepository.normalizeName("\nDo\t\n   Thing\n"));
        assertEquals(2, whitespaceCommands.size());
        assertEquals(overrideCommand, whitespaceCommands.get(0));
        assertEquals(baseCommand, whitespaceCommands.get(1));
    }
    
    @Test
    public void testTriggerSequence() {
        MocaCommandRepository repos = new MocaCommandRepository();
        ComponentLevel fooLevel = new ComponentLevel("foo");
        fooLevel.setSortseq(100);
        repos.addLevel(fooLevel);
        
        Command baseCommand = new JavaCommand("do thing", fooLevel);
        
        repos.addCommand(baseCommand);
        
        MocaTrigger triggerA = new MocaTrigger("mytrig", "do thing");
        triggerA.setFireSequence(100);
        
        MocaTrigger triggerB = new MocaTrigger("another", "do   thing");
        triggerB.setFireSequence(200);
        
        MocaTrigger triggerC = new MocaTrigger("one more", "\ndo\r\nthing\n\n\n");
        triggerC.setFireSequence(150);
        
        repos.addTrigger(triggerA);
        repos.addTrigger(triggerB);
        repos.addTrigger(triggerC);
        
        List<Trigger> triggers = repos.getTriggerByCommandName("do thing");
        
        assertEquals(3, triggers.size());
        assertEquals(triggerA, triggers.get(0));
        assertEquals(triggerC, triggers.get(1));
        assertEquals(triggerB, triggers.get(2));

        triggers = repos.getTriggerByCommandName(MocaCommandRepository.normalizeName("do\nthing"));
        
        assertEquals(3, triggers.size());
        assertEquals(triggerA, triggers.get(0));
        assertEquals(triggerC, triggers.get(1));
        assertEquals(triggerB, triggers.get(2));
    }
    
    @Test
    public void testTriggerOverrideBothHaveSyntax() {
        MocaCommandRepository repos = new MocaCommandRepository();
        ComponentLevel fooLevel = new ComponentLevel("foo");
        fooLevel.setSortseq(100);
        repos.addLevel(fooLevel);
        
        Command baseCommand = new JavaCommand("do thing", fooLevel);
        
        repos.addCommand(baseCommand);
        
        MocaTrigger triggerA = new MocaTrigger("mytrig", "do thing");
        triggerA.setSyntax("noop");
        triggerA.setFireSequence(50);
        
        MocaTrigger triggerB = new MocaTrigger("mytrig", "do   thing");
        triggerB.setSyntax("publish data");
        triggerB.setFireSequence(100);
        
        repos.addTrigger(triggerA);
        repos.addTrigger(triggerB);
        
        List<RepositoryReadException> exceptions = repos.consolidateTriggers();
        
        assertEquals("We should have gotten an error since both triggers have syntax", 
                1, exceptions.size());
    }
    
    @Test
    public void testTriggerOverrideDisableSameSortSequence() {
        MocaCommandRepository repos = new MocaCommandRepository();
        ComponentLevel fooLevel = new ComponentLevel("foo");
        fooLevel.setSortseq(100);
        repos.addLevel(fooLevel);
        
        Command baseCommand = new JavaCommand("do thing", fooLevel);
        
        repos.addCommand(baseCommand);
        
        MocaTrigger triggerA = new MocaTrigger("my trig ", "do thing");
        triggerA.setSyntax("noop");
        triggerA.setFireSequence(50);
        triggerA.setSortSequence(100);
        
        MocaTrigger triggerB = new MocaTrigger("my   trig", "do   thing");
        triggerB.setDisabled(true);
        triggerB.setSortSequence(100);
        
        repos.addTrigger(triggerA);
        repos.addTrigger(triggerB);
        
        List<RepositoryReadException> exceptions = repos.consolidateTriggers();
        
        assertEquals("We should have gotten an error since both triggers have same sort sequence", 
                1, exceptions.size());
    }
    
    @Test
    public void testTriggerOverrideDisable() {
        MocaCommandRepository repos = new MocaCommandRepository();
        ComponentLevel fooLevel = new ComponentLevel("foo");
        fooLevel.setSortseq(100);
        repos.addLevel(fooLevel);
        
        Command baseCommand = new JavaCommand("do thing", fooLevel);
        
        repos.addCommand(baseCommand);
        
        MocaTrigger triggerA = new MocaTrigger("my trig ", "do thing");
        triggerA.setSyntax("noop");
        triggerA.setFireSequence(50);
        triggerA.setSortSequence(100);
        
        MocaTrigger triggerB = new MocaTrigger("my   trig", "do   thing");
        triggerB.setDisabled(true);
        // Only reason this is set so that it comes after the other, since
        // our triggers are sorted 0 to N for fire-sequence.
        triggerB.setFireSequence(100);
        // Normally this would be tied to a component level sort sequence
        // but that is okay.
        triggerB.setSortSequence(200);
        
        repos.addTrigger(triggerA);
        repos.addTrigger(triggerB);
        
        List<RepositoryReadException> exceptions = repos.consolidateTriggers();
        
        assertEquals("We should be fine since we are disabling the trigger", 
                0, exceptions.size());
        
        List<Trigger> triggers = repos.getAllTriggers();
        
        assertEquals("We should have consolidated down to 1 trigger", 1, 
                triggers.size());
        
        Trigger trigger = triggers.get(0);
        
        // Should be the same as the first trigger we passed in.
        assertTrue(trigger == triggerA);
        
        assertTrue("The trigger should now be disabled.", triggerA.isDisabled());
    }
}
