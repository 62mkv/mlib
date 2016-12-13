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

package com.redprairie.moca.components.coverage;

import java.io.File;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.coverage.repository.CoberturaCommandRepository;
import com.redprairie.moca.server.exec.DefaultServerContext;
import com.redprairie.moca.server.repository.CommandRepository;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class CodeCoverageService {
    public MocaResults createCodeCoverageOutput(MocaContext moca, String directory) throws MocaException {
        CommandRepository repo = DefaultServerContext.getRepository(moca);
        
        ProjectData data = null;
        if (repo instanceof CoberturaCommandRepository) {
            data = ((CoberturaCommandRepository)repo).getProjectData();
        }
        if (data == null) {
            throw new MocaException(12345, "Repository was not instrumented with code coverage classes!");
        }
        
        File dir = new File(directory);
        
        File file = new File(dir, "moca.ser");
        
        CoverageDataFileHandler.saveCoverageData(data, file);
        
        EditableResults retRes = moca.newResults();
        
        retRes.addColumn("file", MocaType.STRING);
        
        retRes.addRow();
        
        retRes.setStringValue(0, file.getAbsolutePath());
        
        return retRes;
    }
}
