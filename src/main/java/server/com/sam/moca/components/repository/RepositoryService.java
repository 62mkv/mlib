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

package com.sam.moca.components.repository;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.NotFoundException;
import com.sam.moca.components.crud.InvalidValueException;
import com.sam.moca.exceptions.InvalidArgumentException;
import com.sam.moca.server.exec.DefaultServerContext;
import com.sam.moca.server.repository.CommandArgumentFilter;
import com.sam.moca.server.repository.CommandFilter;
import com.sam.moca.server.repository.CommandRepository;
import com.sam.moca.server.repository.ComponentLevel;
import com.sam.moca.server.repository.TriggerFilter;
import com.sam.moca.server.repository.file.CommandReader;
import com.sam.moca.server.repository.file.RepositoryReadException;
import com.sam.moca.server.repository.file.TriggerReader;
import com.sam.moca.server.repository.file.xml.XMLCommandReader;
import com.sam.moca.server.repository.file.xml.XMLTriggerReader;
import com.sam.moca.util.MocaUtils;

/**
 * This class defines the java based components for the mocarepository
 * component level.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class RepositoryService {
	/**
     * This command will list all active levels in the system.  You can
     * optionally narrow the list down by providing a specific component
     * level.
     * @param moca The moca context
     * @param componentLevel The component level to narrow results down if 
     *                       desired
     * @return A MocaResults containing the information for all the commands
     *         that pass the criteria
     * @throws NotFoundException This is thrown if no commands are 
     *         found for the filter
     */
    public MocaResults listActiveLevels(MocaContext moca, 
            final String componentLevel) 
            throws NotFoundException, InvalidArgumentException {
    	
        MocaResults retRes = DefaultServerContext.listLevels(moca, 
        		componentLevel);
        
        retRes = MocaUtils.filterResults(retRes, moca);
        
        // If there were no rows found then we should throw up a not found
        // exception back to the caller
        if (retRes.getRowCount() == 0) {
            throw new NotFoundException(retRes);
        }
        
        return retRes;
    }
    
    /**
     * This command will list all active commands in the system.  You can
     * optionally narrow the list down by providing a component level or
     * command name.
     * @param moca The moca context
     * @param componentLevel The component level to narrow results down if 
     *                       desired
     * @param commandName The command name to narrow results down if desired
     * @return A MocaResults containing the information for all the commands
     *         that pass the criteria
     * @throws NotFoundException This is thrown if no commands are 
     *         found for the filter
     */
    public MocaResults listActiveCommands(MocaContext moca, 
            final String componentLevel, final String commandName) 
            throws NotFoundException, InvalidArgumentException {
        MocaResults retRes = DefaultServerContext.listCommands(moca, 
                new CommandFilter() {

            @Override
            public boolean accept(String commandNameToCheck, 
                    String componentLevelToCheck) {
                boolean accept = true;
                // If command is provided and it doesn't match then it is false
                if (commandName != null && 
                        !commandName.equalsIgnoreCase(commandNameToCheck)) {
                    accept = false;
                }
                // If component level is provided and it doesn't match then it 
                // is false
                if (componentLevel != null && 
                        !componentLevel.equalsIgnoreCase(componentLevelToCheck)) {
                    accept = false;
                }
                return accept;
            }
            
        });
        
        retRes = MocaUtils.filterResults(retRes, moca);
        
        // If there were no rows found then we should throw up a not found
        // exception back to the caller
        if (retRes.getRowCount() == 0) {
            throw new NotFoundException(retRes);
        }
        
        return retRes;
    }
    
    /**
     * This command will list all active command arguments in the system.  You 
     * can optionally narrow the list down by providing a component level,
     * command name, or argument name.
     * @param moca The Moca Context
     * @param componentLevel The component level to narrow results down if 
     *                       desired
     * @param commandName The command name to narrow results down if desired
     * @param argumentName The argument name to narrow results down if desired
     * @return A MocaResults containing the information for all the command
     *         arguments that pass the criteria
     * @throws NotFoundException This is thrown if no command arguments are 
     *         found for the filter
     */
    public MocaResults listActiveCommandArguments(MocaContext moca, 
            final String componentLevel, final String commandName, 
            final String argumentName) throws NotFoundException, InvalidArgumentException {
        MocaResults retRes = DefaultServerContext.listCommandArguments(moca, 
                new CommandArgumentFilter() {

            @Override
            public boolean accept(String componentLevelToCheck, String commandNameToCheck,
                    String argumentNameToCheck) {
                boolean accept = true;
                // If command is provided and it doesn't match then it is false
                if (commandName != null && 
                        !commandName.equalsIgnoreCase(commandNameToCheck)) {
                    accept = false;
                }
                // If component level is provided and it doesn't match then it 
                // is false
                if (componentLevel != null && 
                        !componentLevel.equalsIgnoreCase(componentLevelToCheck)) {
                    accept = false;
                }
                
                if (argumentName != null && 
                        !argumentName.equalsIgnoreCase(argumentNameToCheck)) {
                    accept = false;
                }
                return accept;
            }
            
        });
        
        retRes = MocaUtils.filterResults(retRes, moca);

        // If there were no rows found then we should throw up a not found
        // exception back to the caller
        if (retRes.getRowCount() == 0) {
            throw new NotFoundException(retRes);
        }
        
        return retRes;
    }
    
    /**
     * This command will list all active triggers in the system.  You can 
     * optionally narrow the list down by providing a command name.
     * @param moca The Moca Context
     * @param commandName The command name to narrow results down if desired
     * @return A MocaResults containing the information for all the triggers 
     *         that pass the criteria
     * @throws NotFoundException This is thrown if no triggers are 
     *         found for the filter
     */
    public MocaResults listActiveTriggers(MocaContext moca, 
            final String commandName) throws NotFoundException, InvalidArgumentException {
        MocaResults retRes = DefaultServerContext.listTriggers(moca, 
                new TriggerFilter() {

            @Override
            public boolean accept(String commandNameToCheck) {
                // If the command name is null or it matches the target command 
                //  name then we accept it
                return (commandName == null || 
                        commandName.equalsIgnoreCase(commandNameToCheck));
            }
            
        });
        
        retRes = MocaUtils.filterResults(retRes, moca);

       // If there were no rows found then we should throw up a not found
        // exception back to the caller
        if (retRes.getRowCount() == 0) {
            throw new NotFoundException(retRes);
        }
        
        return retRes;
    }
    
    /**
     * This command validates a command.
     * @param moca The Moca Context
     * @param data The xml data for a command
     * @param componentLevel The level that the command exists on
     * @throws MocaException 
     */
    public void validateCommand(MocaContext moca, final String data,
                                String componentLevel) throws MocaException {
        CommandRepository repos = DefaultServerContext.getRepository(moca);
        
        ComponentLevel targetLevel = null;
        for (ComponentLevel level : repos.getLevels())
        {
            if (level.getName().equalsIgnoreCase(componentLevel))
            {
                targetLevel = level;
                break;
            }
        }
        
        if (targetLevel == null)
            throw new InvalidValueException(componentLevel, "cmd_lvl");

        InputStream stream = new ByteArrayInputStream(data.getBytes(Charset.forName("UTF-8")));
        CommandReader reader = new XMLCommandReader();
        try {
            reader.read(stream, targetLevel);
        }
        catch (RepositoryReadException e) {
            throw new InvalidValueException("xml", "data ");
        }
        
        // Valid exit since the command parsed correctly
    }
    
    /**
     * This command validates a trigger.
     * @param moca The Moca Context
     * @param data The xml data for a trigger
     * @param componentLevel The level that the command exists on
     * @throws MocaException 
     */
    public void validateTrigger(MocaContext moca, final String data,
                                String componentLevel) throws MocaException {
        CommandRepository repos = DefaultServerContext.getRepository(moca);
        
        ComponentLevel targetLevel = null;
        for (ComponentLevel level : repos.getLevels())
        {
            if (level.getName().equalsIgnoreCase(componentLevel))
            {
                targetLevel = level;
                break;
            }
        }
        
        if (targetLevel == null)
            throw new InvalidValueException(componentLevel, "cmd_lvl");

        InputStream stream = new ByteArrayInputStream(data.getBytes(Charset.forName("UTF-8")));
        TriggerReader reader = new XMLTriggerReader();
        try {
            reader.read(stream, targetLevel);
        }
        catch (RepositoryReadException e) {
            throw new InvalidValueException("xml", "data ");
        }
        
        // Valid exit since the command parsed correctly
    }
}
