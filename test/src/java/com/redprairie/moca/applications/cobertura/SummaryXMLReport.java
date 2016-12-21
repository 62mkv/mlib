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

package com.sam.moca.applications.cobertura;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.Header;
import net.sourceforge.cobertura.util.IOUtil;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SummaryXMLReport {
    

    private final PrintWriter pw;
    private int indent = 0;

    
    public SummaryXMLReport(ProjectData projectData, File destinationDir,
                    FileFinder finder, ComplexityCalculator complexity) throws IOException
    {
            File file = new File(destinationDir, "coverage-summary.xml");
            pw = IOUtil.getPrintWriter(file);

            try
            {
                    println("<?xml version=\"1.0\"?>");
                    println("<!DOCTYPE coverage SYSTEM \"http://cobertura.sourceforge.net/xml/"
                                    + XMLReport.coverageDTD + "\">");
                    println("");

                    double ccn = complexity.getCCNForProject(projectData);
                    int numLinesCovered = projectData.getNumberOfCoveredLines();
                    int numLinesValid = projectData.getNumberOfValidLines();
                    int numBranchesCovered = projectData.getNumberOfCoveredBranches();
                    int numBranchesValid = projectData.getNumberOfValidBranches();

                    
                    // TODO: Set a schema?
                    //println("<coverage " + sourceDirectories.toString() + " xmlns=\"http://cobertura.sourceforge.net\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://cobertura.sourceforge.net/xml/coverage.xsd\">");
                    println(
                                    "<coverage line-rate=\"" + projectData.getLineCoverageRate()
                                    + "\" branch-rate=\"" + projectData.getBranchCoverageRate()
                                    + "\" lines-covered=\"" + numLinesCovered
                                    + "\" lines-valid=\"" + numLinesValid
                                    + "\" branches-covered=\"" + numBranchesCovered
                                    + "\" branches-valid=\"" + numBranchesValid

                                    + "\" complexity=\"" + ccn

                                    + "\" version=\"" + Header.version()
                                    + "\" timestamp=\"" + new Date().getTime()
                                    + "\">");

                    //the DTD requires a "packages" element
                    increaseIndentation();
                    println("<packages />");
                    decreaseIndentation();
                    
                    println("</coverage>");
            }
            finally
            {
                    pw.close();
            }

    }
    
    void increaseIndentation()
    {
            indent++;
    }

    void decreaseIndentation()
    {
            if (indent > 0)
                    indent--;
    }

    private void println(String ln)
    {
            indent();
            pw.println(ln);
    }

    private void indent()
    {
            for (int i = 0; i < indent; i++)
            {
                    pw.print("\t");
            }
    }
}
