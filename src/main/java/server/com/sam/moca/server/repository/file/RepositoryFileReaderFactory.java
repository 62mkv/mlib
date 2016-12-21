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

package com.sam.moca.server.repository.file;

import com.sam.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents;

/**
 * This class is an interface to be used for implementing alternate
 * <code>RepositoryFileReaderFactorys</code> for returning instances of 
 * <code>CommandReader</code>, <code>LevelReader</code>,
 * and <code>TriggerReader</code>
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author klehrke
 * @see CommandReader
 * @see LevelReader
 * @see TriggerReader
 */
public interface RepositoryFileReaderFactory {
    
    /***
     * Should return an instance of a CommandReader
     * 
     * @param events
     * @return CommandReader
     */
    public  CommandReader getCommandReader(RepositoryReaderEvents events);
    
    /***
     * Should return an instance of a LevelReader
     * 
     * @param events
     * @return LevelReader
     */
    public  LevelReader getLevelReader(RepositoryReaderEvents events);
    
    /***
     * Should return an instance of a TriggerReader
     * 
     * @param events
     * @return TriggerReader
     */
    public  TriggerReader getTriggerReader(RepositoryReaderEvents events);
}
