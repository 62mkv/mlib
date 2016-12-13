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

package com.redprairie.moca.server.exec;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaValue;

/**
 * A source for looking up arguments from the execution data stack.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public interface ArgumentSource {
    /**
     * Gets a variable from the current execution stack, possibly NOT marking
     * that variable as used. Note, variables are only marked used if they are
     * arguments. Stack results are never "used", only arguments.
     * 
     * @param name the variable to look up.
     * @param markUsed If <code>false</code>, the variable fetched is not marked
     *            used. Otherwise, it is marked used. Used variables will not be
     *            included in argument lists.
     * @return the value of the argument, or <code>null</code> if the variable
     *         is not on the stack.
     */
    public MocaValue getVariable(String name, boolean markUsed);

    /**
     * Looks up a system variable (AKA environment variable) in the current
     * running context. System variables can come from three places. They can be
     * OS environment variables, they can be set up in the global system
     * configuration, and they can be set up in the request information passed
     * to the MOCA engine, which can override the global settings.
     * 
     * @param name the name of the system variable
     * @return the value of the system variable, or <code>null</code> if the
     *         variable is not set.
     */
    public String getSystemVariable(String name);
    
    /**
     * Equivalent to calling <code>getVariableAsArgument</code> with the 
     * <code>alias</code> argument set to <code>null</code>.
     * 
     * @param name the argument name to look for.
     * @param markUsed if <code>true</code>, the argument is marked as used on
     *            the stack
     * @param equalsOnly if true, we should limit the search to arguments passed
     *            with the equals operator (MocaOperator.EQ).  Otherwise, any
     *            argument in the list could be returned.
     * @return a <code>MocaArgument</code> instance corresponding to the named
     *         value. If no value of the given name exists on the stack, either
     *         as an argument or a result column, <code>null</code> is returned.
     */
    public MocaArgument getVariableAsArgument(String name, boolean markUsed, boolean equalsOnly);

    /**
     * Determines if a variable is available on the stack.
     * 
     * @param name the variable to look up.
     * @return <code>true</code> if the variable is on the stack,
     *         <code>false</code> otherwise.
     */
    public boolean isVariableAvailable(String name);
    
    /**
     * Returns the full set of arguments at the current execution stack level.
     * Only arguments not marked as used are returned.
     * 
     * @param getAll if <code>true</code>, all arguments are retrieved.
     *            Otherwise, only unused (unreferenced) arguments are returned.
     * @param useLowLevel if <code>true</code>, use the lowest level of the
     *            execution stack.  Otherwise, only look at the most recent command
     *            definition.
     * @return an array of {@link MocaArgument} elements corresponding to the
     *         currently executing command's argument list (where clause).
     */
    public MocaArgument[] getCommandArgs(boolean getAll, boolean useLowLevel);
}
