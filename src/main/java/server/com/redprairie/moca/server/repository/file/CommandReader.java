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
import java.io.InputStream;

import com.redprairie.moca.server.repository.Command;
import com.redprairie.moca.server.repository.ComponentLevel;

/**
 * This Interface is used to create implementations for reading Moca command files.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 * @see Command
 */
public interface CommandReader {

    /***
     * 
     * Returns a command read from a <code>File</code> from a specific <code>ComponentLevel</code>.
     * 
     * 
     * @param file
     * @param level
     * @return Command
     * @throws RepositoryReadException
     * @see ComponentLevel
     */
    public Command read(File file, ComponentLevel level) throws RepositoryReadException;
    
    /***
     * 
     * Returns a command read from an <code>InputStream</code> from a specific <code>ComponentLevel</code>.
     * 
     * 
     * @param stream
     * @param level
     * @return Command
     * @throws RepositoryReadException
     * @see ComponentLevel
     */
    public Command read(InputStream stream, ComponentLevel level) throws RepositoryReadException;
    
}
