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

package com.redprairie.moca.server.repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.redprairie.moca.server.exec.CommandDescriptor;
import com.redprairie.moca.server.exec.CommandNotFoundException;
import com.redprairie.moca.server.exec.ExecutableComponent;
import com.redprairie.moca.server.exec.builtin.CommitBuiltin;
import com.redprairie.moca.server.exec.builtin.DummyBuiltin;
import com.redprairie.moca.server.exec.builtin.RollbackBuiltin;
import com.redprairie.moca.server.repository.file.RepositoryReadException;

/**
 * Added interface for this, look at interface for javadoc
 *
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 *
 * @author wburns
 */
public class MocaCommandRepository implements Serializable, CommandRepository {

    /**
     * Adds a level to the repository.
     * @param lvl
     */
    public void addLevel(ComponentLevel lvl) {
        _levels.put(lvl.getName().toLowerCase(), lvl);
    }

    /**
     * Adds a command object to the repository.
     * @param cmd
     */
    public void addCommand(Command cmd) {
        ComponentLevel commandLevel = cmd.getLevel();
        ComponentLevel repositoryLevel = _levels.get(commandLevel.getName().toLowerCase());
        if (repositoryLevel == null ) {
             throw new IllegalArgumentException("Invalid Command -- level not present: [" + commandLevel.getName() + "]");
        }

        String commandName = normalizeName(cmd.getName());

        List<Command> cmdlist = _commands.get(commandName);
        if (cmdlist == null) {
            cmdlist = new ArrayList<Command>(1);
            _commands.put(commandName, cmdlist);
        }
        cmdlist.add(cmd);
        Collections.sort(cmdlist, new Comparator<Command>() {
            // @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
            @Override
            public int compare(Command o1, Command o2) {
                return -(o1.getLevel().getSortseq() - o2.getLevel().getSortseq());
            }
        });
    }

    /**
     * Adds a trigger using the trigger name as a key. If a trigger with
     * the same name and level already exist it is replaced by the input
     * Trigger and returned.
     * @param ntrig
     * @return Trigger that was replaced or null if the input Trigger
     *         was an addition
     */
    public void addTrigger(Trigger ntrig) {
        String commandName = normalizeName(ntrig.getCommand());

        List<Trigger> tlist = _triggers.get(commandName);

        if (tlist == null) {
            tlist = new ArrayList<Trigger>(1);
            _triggers.put(commandName, tlist);
        }

        tlist.add(ntrig);
        Collections.sort(tlist, new Comparator<Trigger>() {
            // @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
            @Override
            public int compare(Trigger o1, Trigger o2) {
                return o1.getFireSequence() - o2.getFireSequence();
            }
        });
    }

    /**
     * This method will try to consolidate any triggers that are defined
     * multiple times that share the same name and command.  These triggers will
     * then either be merged into a single one or an error is returned stating
     * the cause.
     * @return The list of errors encountered.
     */
    public List<RepositoryReadException> consolidateTriggers() {
        List<RepositoryReadException> exceptions =
            new ArrayList<RepositoryReadException>();
        Set<Entry<String, List<Trigger>>> entries = _triggers.entrySet();

        for (Entry<String, List<Trigger>> entry : entries) {

            String commandName = entry.getKey();
            List<Trigger> tlist = entry.getValue();
            boolean changed = false;

            ListIterator<Trigger> iter = tlist.listIterator();
            Set<Trigger> triggersForRemoval = new HashSet<Trigger>();

            // We want to iterate through all of the triggers to find any
            // that have matching name and command.
            while (iter.hasNext()) {
                Trigger existing = iter.next();
                String existingName = normalizeName(existing.getName());

                // If the trigger we are now on was found to be required to
                // be removed then do so and continue with next one.b
                if (triggersForRemoval.remove(existing)) {
                    iter.remove();
                    continue;
                }

                // If there are more triggers after this one, then check them
                // to see if we find any matches.
                if (iter.hasNext()) {
                    ListIterator<Trigger> iter2 = tlist.listIterator(iter.nextIndex());

                    while (iter2.hasNext()) {

                        Trigger trigger2 = iter2.next();

                        String triggerName = normalizeName(trigger2.getName());

                        if (existingName.equalsIgnoreCase(triggerName)) {
                            /*
                             * Make sure we don't have a duplicate trigger.
                             * - If there is ever multiple triggers with the same key and local syntax,
                             *   then that's an error
                             * - Triggers with the same key and level are duplicates
                             * - A trigger with local syntax sets all info attributes except
                             *   enable/disable and sortseq
                             * - Higher level triggers override settings for enable/disable and sortseq
                             */
                            if (existing.getSyntax() != null && trigger2.getSyntax() != null) {
                                exceptions.add(new RepositoryReadException("Duplicate trigger \""
                                        + triggerName + "\" on \"" + commandName
                                        + "\" found with local syntax"));
                                continue;
                            }

                            int prevSeq = existing.getSortSequence();
                            int newSeq = trigger2.getSortSequence();

                            if (prevSeq == newSeq) {
                                exceptions.add(new RepositoryReadException("Duplicate trigger \"" +
                                        triggerName + "\" on \"" + commandName +
                                        "\" found at same level (" + prevSeq + ")"));
                                continue;
                            }

                            // We have to mark to remove this trigger then.
                            triggersForRemoval.add(trigger2);

                            if (existing instanceof TriggerOverridable) {
                                ((TriggerOverridable)existing).overrideTrigger(trigger2);
                            }
                            else {
                                exceptions.add(new RepositoryReadException("Trigger \"" +
                                        triggerName + "\" on \"" + commandName +
                                        "\" could not be overridden due to not " +
                                        "implementing TriggerOverridable"));
                                continue;
                            }
                        }
                    }
                }

                // If the trigger resulted in no syntax and was not disabled
                // then we have a problem.
                if (existing.getSyntax() == null && !existing.isDisabled()) {
                    exceptions.add(new RepositoryReadException("Resultant Trigger \"" +
                            existing.getName() + "\" on \"" + commandName +
                            "\" found enabled but has no syntax."));
                }
            }

            if (changed) {
                // Lastly we have to sort the triggers again, since the fire sequence
                // could have been updated.
                Collections.sort(tlist, new Comparator<Trigger>() {
                    // @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
                    @Override
                    public int compare(Trigger o1, Trigger o2) {
                        return o1.getFireSequence() - o2.getFireSequence();
                    }
                });
            }
        }

        return exceptions;
    }

    // @see com.redprairie.moca.server.repository.CommandRepository1#getLevelByName(java.lang.String)

    @Override
    public ComponentLevel getLevelByName(String levelname) {
        return _levels.get(levelname.toLowerCase());
    }

    // @see com.redprairie.moca.server.repository.CommandRepository1#getCommandByName(java.lang.String)

    @Override
    public List<Command> getCommandByName(String name) {
        return _commands.get(name);
    }

    // @see com.redprairie.moca.server.repository.CommandRepository1#getLevels()

    @Override
    public List<ComponentLevel> getLevels() {
        List<ComponentLevel> tmp = new ArrayList<ComponentLevel>(_levels.values());
        Collections.sort(tmp, new Comparator<ComponentLevel>() {
            // @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)

            @Override
            public int compare(ComponentLevel o1, ComponentLevel o2) {
                int sortDiff = o1.getSortseq() - o2.getSortseq();
                return sortDiff != 0 ? sortDiff : o1.getName().compareTo(o2.getName());
            }
        });
        return tmp;
    }

    // @see com.redprairie.moca.server.repository.CommandRepository1#getTriggerByCommandName(java.lang.String)

    @Override
    public List<Trigger> getTriggerByCommandName(String cmdName) {
        return _triggers.get(cmdName);
    }

    // @see com.redprairie.moca.server.repository.CommandRepository1#getLevelCount()

    @Override
    public int getLevelCount() {
        return _levels.size();
    }

    // @see com.redprairie.moca.server.repository.CommandRepository1#getTriggerCount()

    @Override
    public int getTriggerCount() {
        int total = 0;
        for (List<Trigger> t: _triggers.values()) {
            total += t.size();
        }
        return total;
    }

    // @see com.redprairie.moca.server.repository.CommandRepository1#getCommandCount()

    @Override
    public int getCommandCount() {
        int total = 0;
        for (List<Command> t: _commands.values()) {
            total += t.size();
        }
        return total;
    }

    // @see com.redprairie.moca.server.repository.CommandRepository1#getAllCommands()

    @Override
    public List<Command> getAllCommands() {
        List<Command> allCommands = new ArrayList<Command>();
        for(List<Command> commandSet : _commands.values()) {
            allCommands.addAll(commandSet);
        }
        return allCommands;
    }

    // @see com.redprairie.moca.server.repository.CommandRepository1#getAllTriggers()

    @Override
    public List<Trigger> getAllTriggers() {
        List<Trigger> allTriggers = new ArrayList<Trigger>();
        for(List<Trigger> triggerSet : _triggers.values()) {
            allTriggers.addAll(triggerSet);
        }
        return allTriggers;
    }


    // @see com.redprairie.moca.server.repository.CommandRepository1#resolveCommand(java.lang.String, boolean, com.redprairie.moca.server.repository.Command)

    @Override
    public ExecutableComponent resolveCommand(String verbNounClause, boolean override, Command current)
            throws CommandNotFoundException {

        if (override && current == null) {
            throw new CommandNotFoundException("No override command available for " + verbNounClause);
        }

        ExecutableComponent builtin = BUILTIN_COMMANDS.get(verbNounClause);
        if (builtin != null) {
            return builtin;
        }

        List<Command> commandList = getCommandByName(verbNounClause);

        if (commandList == null || commandList.size() == 0) {
            throw new CommandNotFoundException(verbNounClause);
        }

        Command commandDef = null;

        // Check for command override. This means we should be executing a
        // command of this name already.
        if (override) {
            // If we're not executing the named command, the override is not
            // allowed.
            if (!current.getName().equalsIgnoreCase(verbNounClause)) {
                throw new CommandNotFoundException("No override command available for " + verbNounClause);
            }

            // Now find our command in the command list.
            int found = commandList.indexOf(current);

            // This should always
            if (found == -1) {
                throw new CommandNotFoundException("No override command available for " + verbNounClause
                        + ": not executing command");
            }

            if (commandList.size() <= found + 1) {
                throw new CommandNotFoundException("No override command available for " + verbNounClause);
            }

            commandDef = commandList.get(found + 1);
        }
        else {
            commandDef = commandList.get(0);
        }

        List<Trigger> triggers = null;
        if (!override) {
            triggers = getTriggerByCommandName(verbNounClause);
        }

        return new CommandDescriptor(commandDef, triggers);
    }


    /**
     * Normalize the name element.  We could have names that have extra spaces
     * within the command name, or at the beginning or end.
     */
    public static String normalizeName(String name) {
        if (name == null) {
            return null;
        }
        else {
            return name.trim().replaceAll("\\s+", " ").toLowerCase();
        }
    }

    //
    // Implementation
    //

    private static final long serialVersionUID = -2504184706864521675L;
    private Map<String, ComponentLevel> _levels = new HashMap<String, ComponentLevel>();
    private Map<String, List<Command>> _commands = new HashMap<String, List<Command>>();
    private Map<String, List<Trigger>> _triggers = new HashMap<String, List<Trigger>>();

    private static final Map<String, ExecutableComponent> BUILTIN_COMMANDS = new HashMap<String, ExecutableComponent>();
    static {
        BUILTIN_COMMANDS.put("commit", new CommitBuiltin());
        BUILTIN_COMMANDS.put("rollback", new RollbackBuiltin());
        BUILTIN_COMMANDS.put("nodbcommit", new DummyBuiltin("nodbcommit"));
        BUILTIN_COMMANDS.put("nodbrollback", new DummyBuiltin("nodbrollback"));
        BUILTIN_COMMANDS.put("prepare", new DummyBuiltin("prepare")); // TODO FIXME
        BUILTIN_COMMANDS.put("ping", new DummyBuiltin("ping"));
        BUILTIN_COMMANDS.put("noop", new DummyBuiltin("noop"));
    }


}
