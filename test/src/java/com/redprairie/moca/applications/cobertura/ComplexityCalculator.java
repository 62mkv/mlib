/*
 *  $URL$
 *  $Author$
 *  $Date$
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

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public interface ComplexityCalculator {

    /**
     * Computes CCN for all sources contained in the project.
     * CCN for whole project is an average CCN for source files.
     * All source files for which CCN cannot be computed are ignored.
     * 
     * @param projectData project to compute CCN for
     * @throws NullPointerException if projectData is null
     * @return CCN for project or 0 if no source files were found
     */
    public abstract double getCCNForProject(ProjectData projectData);

    /**
     * Computes CCN for all sources contained in the specified package.
     * All source files that cannot be mapped to existing files are ignored.
     * 
     * @param packageData package to compute CCN for

import org.apache.logging.log4j.LogManager;     * @throws NullPointerException if <code>packageData</code> is <code>null</code>
     * @return CCN for the specified package or 0 if no source files were found

import org.apache.logging.log4j.LogManager;     */
    public abstract double getCCNForPackage(PackageData packageData);

    /**
     * Computes CCN for single source file.
     * 
     * @param sourceFile source file to compute CCN for
     * @throws NullPointerException if <code>sourceFile</code> is <code>null</code>
     * @return CCN for the specified source file, 0 if cannot map <code>sourceFile</code> to existing file
     */
    public abstract double getCCNForSourceFile(SourceFileData sourceFile);

    /**
     * Computes CCN for source file the specified class belongs to.
     * 
     * @param classData package to compute CCN for

import org.apache.logging.log4j.LogManager;     * @return CCN for source file the specified class belongs to
     * @throws NullPointerException if <code>classData</code> is <code>null</code>
     */
    public abstract double getCCNForClass(ClassData classData);

}