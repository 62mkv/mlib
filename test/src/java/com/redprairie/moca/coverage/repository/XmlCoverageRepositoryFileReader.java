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

package com.redprairie.moca.coverage.repository;

import net.sourceforge.cobertura.coveragedata.ProjectData;

import com.redprairie.moca.server.repository.ComponentLibraryFilter;
import com.redprairie.moca.server.repository.file.CommandReader;
import com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents;
import com.redprairie.moca.server.repository.file.TriggerReader;
import com.redprairie.moca.server.repository.file.xml.XMLRepositoryFileReaderFactory;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class XmlCoverageRepositoryFileReader extends
        XMLRepositoryFileReaderFactory {
    /**
     * 
     */
    public XmlCoverageRepositoryFileReader(ProjectData data, 
        ComponentLibraryFilter includeFilter, 
        ComponentLibraryFilter excludeFilter) {
        _projectData = data;
        _includeFilter = includeFilter;
        _excludeFilter = excludeFilter;
    }
    
    // @see com.redprairie.moca.server.repository.file.xml.XMLRepositoryFileReaderFactory#getCommandReader(com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents)
    @Override
    public CommandReader getCommandReader(RepositoryReaderEvents events) {
        return new XMLCoverageCommandReader(_projectData, _includeFilter, 
            _excludeFilter);
    }
    
    // @see com.redprairie.moca.server.repository.file.xml.XMLRepositoryFileReaderFactory#getTriggerReader(com.redprairie.moca.server.repository.file.CommandRepositoryReader.RepositoryReaderEvents)
    @Override
    public TriggerReader getTriggerReader(RepositoryReaderEvents events) {
        return new XMLCoverageTriggerReader(_projectData, _includeFilter, 
            _excludeFilter);
    }
    
    private final ProjectData _projectData;
    private final ComponentLibraryFilter _includeFilter;
    private final ComponentLibraryFilter _excludeFilter;
}
