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

package com.redprairie.moca.server.profile;

import org.junit.Test;

import com.redprairie.moca.server.repository.Command;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.LocalSyntaxCommand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class TU_CommandPath {

    @Test
    public void testSimpleEquality() {
        CommandPath path1 = CommandPath.forCommand(null, commandA);
        CommandPath path2 = CommandPath.forCommand(null, commandA);
        
        assertEquals(path1, path2);
        assertEquals(path1.hashCode(), path2.hashCode());
    }
    
    @Test
    public void testSimpleInequality() {
        CommandPath path1 = CommandPath.forCommand(null, commandA);
        CommandPath path2 = CommandPath.forCommand(null, commandB);
        
        assertFalse(path1.equals(path2));
    }
    
    @Test
    public void testLongIdenticalPaths() {
        CommandPath path1 = CommandPath.forCommand(CommandPath.forCommand(CommandPath.forCommand(null, commandA), commandB), commandFooBar);
        CommandPath path2 = CommandPath.forCommand(CommandPath.forCommand(CommandPath.forCommand(null, commandA), commandB), commandFooBar);
        
        assertEquals(path1, path2);
        assertEquals(path1.hashCode(), path2.hashCode());
    }

    
    @Test
    public void testDifferentPathLengths() {
        CommandPath path1 = CommandPath.forCommand(null, commandA);
        CommandPath path2 = CommandPath.forCommand(CommandPath.forCommand(CommandPath.forCommand(null, commandFooBar), commandB), commandA);
        
        assertFalse(path1.equals(path2));
        assertFalse(path2.equals(path1));
    }
    
    private static final ComponentLevel testLevel = new ComponentLevel("lvl");
    private static final Command commandA = new LocalSyntaxCommand("command a", testLevel);
    private static final Command commandB = new LocalSyntaxCommand("command b", testLevel);
    private static final Command commandFooBar = new LocalSyntaxCommand("foo bar", testLevel);

}
