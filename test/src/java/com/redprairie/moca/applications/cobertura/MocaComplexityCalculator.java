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

package com.redprairie.moca.applications.cobertura;

import java.util.Collection;
import java.util.SortedSet;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;

import com.redprairie.moca.coverage.repository.MocaClassData;
import com.redprairie.moca.coverage.repository.MocaProjectData;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaComplexityCalculator implements ComplexityCalculator {

    // @see com.redprairie.moca.applications.cobertura.ComplexityCalculator#getCCNForProject(net.sourceforge.cobertura.coveragedata.ProjectData)
    @Override
    public double getCCNForProject(ProjectData projectData) {
        if (!(projectData instanceof MocaProjectData)) {
            return 0;
        }
        // This cast should be safe since we are using a MocaProjectData
        @SuppressWarnings("unchecked")
        Collection<MocaClassData> classDatas = (Collection<MocaClassData>)projectData.getClasses();
        return findAverage(classDatas);
    }

    // @see com.redprairie.moca.applications.cobertura.ComplexityCalculator#getCCNForPackage(net.sourceforge.cobertura.coveragedata.PackageData)
    @Override
    public double getCCNForPackage(PackageData packageData) {
        @SuppressWarnings("unchecked")
        SortedSet<MocaClassData> classDatas = (SortedSet<MocaClassData>)packageData.getClasses();
        return findAverage(classDatas);
    }

    // @see com.redprairie.moca.applications.cobertura.ComplexityCalculator#getCCNForSourceFile(net.sourceforge.cobertura.coveragedata.SourceFileData)
    @Override
    public double getCCNForSourceFile(SourceFileData sourceFile) {
        @SuppressWarnings("unchecked")
        SortedSet<MocaClassData> classDatas = (SortedSet<MocaClassData>)sourceFile.getClasses();
        return findAverage(classDatas);
    }

    // @see com.redprairie.moca.applications.cobertura.ComplexityCalculator#getCCNForClass(net.sourceforge.cobertura.coveragedata.ClassData)
    @Override
    public double getCCNForClass(ClassData classData) {
        if (!(classData instanceof MocaClassData)) {
            return 0;
        }
        return ((MocaClassData)classData).getComplexity();
    }
    
    private double findAverage(Collection<MocaClassData> classDatas) {
        double total = 0;
        
        // This cast should be safe since we are using a MocaProjectData
        int classAmount = classDatas.size();
        
        for (MocaClassData classData : classDatas) {
            total += classData.getComplexity();
        }
        
        return total / classAmount;
    }
}
