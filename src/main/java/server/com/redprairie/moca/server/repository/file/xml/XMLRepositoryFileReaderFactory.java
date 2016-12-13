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

package com.redprairie.moca.server.repository.file.xml;

import com.redprairie.moca.server.repository.file.CommandReader;
import com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents;
import com.redprairie.moca.server.repository.file.LevelReader;
import com.redprairie.moca.server.repository.file.RepositoryFileReaderFactory;
import com.redprairie.moca.server.repository.file.TriggerReader;

/**
 * This class is an XML implementation of <code>RepositoryFileReaderFactory</code>.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 * @see RepositoryFileReaderFactory
 */
public class XMLRepositoryFileReaderFactory implements RepositoryFileReaderFactory{
    
    /**
     * This method returns a <code>CommandReader</code> that's implemented using
     * a <code>XMLCommandReader</code>.
     * 
     * @see XMLCommandReader
     * @see RespositoryReaderEvents
     */
    public CommandReader getCommandReader(RepositoryReaderEvents events){
        return new XMLCommandReader(events);
    }
    
    /**
     * 
     * This method returns a <code>CommandReader</code> that's implemented using
     * a <code>XMLLevelReader</code>.
     * 
     * @see XMLLevelReader
     * @see RespositoryReaderEvents
     * 
     */
    public LevelReader getLevelReader(RepositoryReaderEvents events){
        return new XMLLevelReader(events);
    }
    
    /**
     * 
     * This method returns a <code>CommandReader</code> that's implemented using
     * a <code>XMLTriggerReader</code>.
     * 
     * @see XMLTriggerReader
     * @see RespositoryReaderEvents
     * 
     */
    public TriggerReader getTriggerReader(RepositoryReaderEvents events) {
        return new XMLTriggerReader(events);
    }
}
