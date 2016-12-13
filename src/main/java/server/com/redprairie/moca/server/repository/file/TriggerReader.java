/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.repository.file;

import java.io.File;
import java.io.InputStream;

import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.Trigger;

/**
 * This Interface is used to create implementations for reading Moca Trigger files.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 * @see Trigger
 */
public interface TriggerReader {
    
    /***
     * 
     * Returns a <code>Trigger</code> interpreted from a <code>File</code> from
     * a specific <code>ComponentLevel</code>.
     * 
     * @param file
     * @param level
     * @return
     * @throws RepositoryReadException
     * @see ComponentLevel
     */
    public Trigger read(File file, ComponentLevel level) throws RepositoryReadException;
    
    /***
     * 
     * Returns a <code>Trigger</code> interpreted from an <code>InputStream</code> from
     * a specific <code>ComponentLevel</code>.
     * 
     * @param stream
     * @param level
     * @return
     * @throws RepositoryReadException
     * @see ComponentLevel
     */
    public Trigger read(InputStream stream, ComponentLevel level) throws RepositoryReadException;

}
