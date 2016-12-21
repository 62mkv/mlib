/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.sam.moca.server.repository;

import java.util.List;

import com.sam.moca.server.exec.CommandNotFoundException;
import com.sam.moca.server.exec.ExecutableComponent;

/**
 * This class acts as the in memory repository for moca commands and
 * command triggers.
 * Assumptions about the Command Repository:
 *
 * - No two levels can have the same sequence number.
 * - No two levels can have the same name ( regardless of case).
 * - Triggers are associated with a command name.  All triggers associated with a command name will fire at the appropriate time
 *   no matter which Levels command is executed. ( e.g if Level 1 has trigger A on command Z and
 *   Level 2 has trigger B on Z of Level2 then if command Z of level 1 is executed then triggers A
 *   & B are also run. )
 * - Command names are not unique, but are unique to a Level, that is if level 1 has a command A
 *   then level 2 can have a command A but level 1 cannot have a second command A.
 * - No two triggers can have the same fire sequence number and associated command.
 * -
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 *
 * @author cjolly
 * @version $Revision$
 */
public interface CommandRepository {

    /**
     * Returns a Level object based on its name.
     * @param levelname
     * @return Level object or null;
     */
    public ComponentLevel getLevelByName(String levelname);

    /**
     * Returns a list of Commands that match the command name. The list
     * is sorted by the sequence number of the command. If the command
     * name is not found in the repository a null is returned.
     * @param name
     * @return List<Command> sorted by sequence number.
     */
    public List<Command> getCommandByName(String name);

    /**
     * Returns a list of all ComponentLevels in the repository.  Levels are
     * returned in ascending order of sequence number.
     * @return
     */
    public List<ComponentLevel> getLevels();

    /**
     * Retrieves the Triggers that will fire on a command. The list of
     * Triggers is sorted by Level sequence number + Trigger fire sequence number.
     *
     * @param cmdName
     * @return List<Trigger> or null if cmdName has no triggers or the
     *         command name is not found.
     */
    public List<Trigger> getTriggerByCommandName(String cmdName);

    /**
     * Returns the number of Levels in the repository
     * @return number of levels in the repository
     */
    public int getLevelCount();

    /**
     * Returns the number of Triggers in the repository
     * @return number of triggers in the repository
     */
    public int getTriggerCount();

    /**
     * Returns the number of Commands in the repository
     * @return number of commands in the repository
     */
    public int getCommandCount();

    /**
     * Returns a list of all the commands in the system.
     * @return
     */
    public List<Command> getAllCommands();

    /**
     * Returns a list of all the triggers in the system.
     * @return All the triggers available in the system
     */
    public List<Trigger> getAllTriggers();

    public ExecutableComponent resolveCommand(String verbNounClause,
        boolean override, Command current) throws CommandNotFoundException;

}