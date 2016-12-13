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

import com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class StubRepositoryReaderEvents implements RepositoryReaderEvents {

    // @see com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents#finishedLoading()

    @Override
    public void finishedLoading() {
    }

    // @see com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents#reportError(java.lang.String, java.lang.Throwable)

    @Override
    public void reportError(String string, Throwable e) {
    }

    // @see com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents#reportWarning(java.lang.String)

    @Override
    public void reportWarning(String msg) {
    }

    // @see com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents#setCommandCount(int)

    @Override
    public void setCommandCount(int length) {
    }

    // @see com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents#setCurrentCommand(java.io.File)

    @Override
    public void setCurrentCommand(File commandFile) {
    }

    // @see com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents#setCurrentDirectory(java.io.File)

    @Override
    public void setCurrentDirectory(File directory) {
    }

    // @see com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents#setCurrentLevel(java.io.File)

    @Override
    public void setCurrentLevel(File levelFile) {
    }

    // @see com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents#setCurrentTrigger(java.io.File)

    @Override
    public void setCurrentTrigger(File triggerFile) {
    }

    // @see com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents#setDirectoryCount(int)

    @Override
    public void setDirectoryCount(int length) {
    }

    // @see com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents#setLevelCount(int)

    @Override
    public void setLevelCount(int length) {
    }

    // @see com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents#setTriggerCount(int)

    @Override
    public void setTriggerCount(int length) {
    }

    // @see com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents#reportTriggerOverrideError(java.lang.String)
    
    @Override
    public void reportTriggerOverrideError(String msg) {
    }
}
