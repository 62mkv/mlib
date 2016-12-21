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

import java.io.File;
import java.util.List;

import com.sam.moca.MocaException;
import com.sam.moca.server.exec.ServerContext;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface Trigger {

    /**
     * Return the name of this Trigger
     * 
     * @return
     */
    public String getName();

    /**
     * Return the command that this Trigger will fire on
     * 
     * @return
     */
    public String getCommand();

    /**
     * Get the description for this Trigger
     * 
     * @return
     */
    public String getDescription();

    /**
     * Gets the local syntax command for this trigger if set; else will return
     * null
     * 
     * @return local syntax command that is this trigger
     */
    public String getSyntax();

    /**
     * Gets the text of the Documentation
     * 
     * @return
     */
    public String getDocumentation();

    /**
     * Returns the fileName of where this Trigger is stored
     * 
     * @return String
     */
    public String getFileName();

    /**
     * Returns the file where this trigger was loaded from. This will include
     * the full pathname of the trigger file.  Note that this method may return
     * <code>null</code> if this object has been serialized.
     * 
     * @return
     */
    public File getFile();

    /**
     * Returns the version of this Trigger
     * 
     * @return version of trigger
     */
    public String getVersion();

    /**
     * Return the fire sequence of this Trigger
     * 
     * @return
     */
    public int getFireSequence();

    public List<ArgumentInfo> getArguments();

    public int getSortSequence();

    /**
     * Execute the trigger.
     * @param ctx
     * @return
     * @throws MocaException
     */
    public abstract void execute(ServerContext ctx) throws MocaException;

    public abstract boolean isDisabled();

}