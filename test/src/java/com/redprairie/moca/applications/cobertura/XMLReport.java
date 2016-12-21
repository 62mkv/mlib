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

package com.sam.moca.applications.cobertura;

import org.apache.logging.log4j.LogManager;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.JumpData;
import net.sourceforge.cobertura.coveragedata.LineData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import net.sourceforge.cobertura.coveragedata.SwitchData;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.Header;
import net.sourceforge.cobertura.util.IOUtil;
import net.sourceforge.cobertura.util.StringUtil;

import org.apache.logging.log4j.Logger;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class XMLReport {

    private static final Logger logger = LogManager.getLogger(XMLReport.class);

    protected final static String coverageDTD = "coverage-04.dtd";

    private final PrintWriter pw;
    private final FileFinder finder;
    private final ComplexityCalculator complexity;
    private int indent = 0;

    public XMLReport(ProjectData projectData, File destinationDir,
                    FileFinder finder, ComplexityCalculator complexity) throws IOException
    {
            this.complexity = complexity;
            this.finder = finder;

            File file = new File(destinationDir, "coverage.xml");
            pw = IOUtil.getPrintWriter(file);

            try
            {
                    println("<?xml version=\"1.0\"?>");
                    println("<!DOCTYPE coverage SYSTEM \"http://cobertura.sourceforge.net/xml/"
                                    + coverageDTD + "\">");
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

                    increaseIndentation();
                    dumpSources();
                    dumpPackages(projectData);
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

    void indent()
    {
            for (int i = 0; i < indent; i++)
            {
                    pw.print("\t");
            }
    }

    void println(String ln)
    {
            indent();
            pw.println(ln);
    }

    @SuppressWarnings("rawtypes")
    private void dumpSources()
    {
            println("<sources>");
            increaseIndentation();
            for (Iterator it = finder.getSourceDirectoryList().iterator(); it.hasNext(); ) {
                    String dir = (String) it.next();
                    dumpSource(dir);
            }
            decreaseIndentation();
            println("</sources>");
    }

    private void dumpSource(String sourceDirectory)
    {
            println("<source>" + sourceDirectory + "</source>");
    }

    @SuppressWarnings("rawtypes")
    private void dumpPackages(ProjectData projectData)
    {
            println("<packages>");
            increaseIndentation();

            Iterator it = projectData.getPackages().iterator();
            while (it.hasNext())
            {
                    dumpPackage((PackageData)it.next());
            }

            decreaseIndentation();
            println("</packages>");
    }

    private void dumpPackage(PackageData packageData)
    {
            logger.debug("Dumping package " + packageData.getName());

            println("<package name=\"" + packageData.getName()
                            + "\" line-rate=\"" + packageData.getLineCoverageRate()
                            + "\" branch-rate=\"" + packageData.getBranchCoverageRate()
                            + "\" complexity=\"" + complexity.getCCNForPackage(packageData) + "\"" + ">");
            increaseIndentation();
            dumpSourceFiles(packageData);
            decreaseIndentation();
            println("</package>");
    }

    @SuppressWarnings("rawtypes")
    private void dumpSourceFiles(PackageData packageData)
    {
            println("<classes>");
            increaseIndentation();

            Iterator it = packageData.getSourceFiles().iterator();
            while (it.hasNext())
            {
                    dumpClasses((SourceFileData)it.next());
            }

            decreaseIndentation();
            println("</classes>");
    }

    @SuppressWarnings("rawtypes")
    private void dumpClasses(SourceFileData sourceFileData)
    {
            Iterator it = sourceFileData.getClasses().iterator();
            while (it.hasNext())
            {
                    dumpClass((ClassData)it.next());
            }
    }

    private void dumpClass(ClassData classData)
    {
            logger.debug("Dumping class " + classData.getName());

            println("<class name=\"" + classData.getName() + "\" filename=\""
                            + classData.getSourceFileName() + "\" line-rate=\""
                            + classData.getLineCoverageRate() + "\" branch-rate=\""
                            + classData.getBranchCoverageRate() + "\" complexity=\""
                            + complexity.getCCNForClass(classData) + "\"" + ">");
            increaseIndentation();

            dumpMethods(classData);
            dumpLines(classData);

            decreaseIndentation();
            println("</class>");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void dumpMethods(ClassData classData)
    {
            println("<methods>");
            increaseIndentation();

            SortedSet sortedMethods = new TreeSet();
            sortedMethods.addAll(classData.getMethodNamesAndDescriptors());
            Iterator iter = sortedMethods.iterator();
            while (iter.hasNext())
            {
                    dumpMethod(classData, (String)iter.next());
            }

            decreaseIndentation();
            println("</methods>");
    }

    private void dumpMethod(ClassData classData, String nameAndSig)
    {
            String name = nameAndSig.substring(0, nameAndSig.indexOf('('));
            String signature = nameAndSig.substring(nameAndSig.indexOf('('));
            double lineRate = classData.getLineCoverageRate(nameAndSig);
            double branchRate = classData.getBranchCoverageRate(nameAndSig);

            println("<method name=\"" + xmlEscape(name) + "\" signature=\""
                            + xmlEscape(signature) + "\" line-rate=\"" + lineRate
                            + "\" branch-rate=\"" + branchRate + "\">");
            increaseIndentation();
            dumpLines(classData, nameAndSig);
            decreaseIndentation();
            println("</method>");
    }

    private static String xmlEscape(String str)
    {
            str = StringUtil.replaceAll(str, "<", "&lt;");
            str = StringUtil.replaceAll(str, ">", "&gt;");
            return str;
    }

    private void dumpLines(ClassData classData)
    {
            dumpLines(classData.getLines());
    }

    private void dumpLines(ClassData classData, String methodNameAndSig)
    {
            dumpLines(classData.getLines(methodNameAndSig));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void dumpLines(Collection lines)
    {
            println("<lines>");
            increaseIndentation();

            SortedSet sortedLines = new TreeSet();
            sortedLines.addAll(lines);
            Iterator iter = sortedLines.iterator();
            while (iter.hasNext())
            {
                    dumpLine((LineData)iter.next());
            }

            decreaseIndentation();
            println("</lines>");
    }

    private void dumpLine(LineData lineData)
    {
            int lineNumber = lineData.getLineNumber();
            long hitCount = lineData.getHits();
            boolean hasBranch = lineData.hasBranch();
            String conditionCoverage = lineData.getConditionCoverage();

            String lineInfo = "<line number=\"" + lineNumber + "\" hits=\"" + hitCount
                            + "\" branch=\"" + hasBranch + "\"";
            if (hasBranch)
            {
                    println(lineInfo + " condition-coverage=\"" + conditionCoverage + "\">");
                    dumpConditions(lineData);
                    println("</line>");
            } else
            {
                    println(lineInfo + "/>");
            }
    }

    private void dumpConditions(LineData lineData)
    {
            increaseIndentation();
            println("<conditions>");

            for (int i = 0; i < lineData.getConditionSize(); i++)
            {
                    Object conditionData = lineData.getConditionData(i);
                    String coverage = lineData.getConditionCoverage(i);
                    dumpCondition(conditionData, coverage);
            }

            println("</conditions>");
            decreaseIndentation();
    }

    private void dumpCondition(Object conditionData, String coverage)
    {
            increaseIndentation();
            StringBuffer buffer = new StringBuffer("<condition");
            if (conditionData instanceof JumpData)
            {
                    JumpData jumpData = (JumpData) conditionData;
                    buffer.append(" number=\"").append(jumpData.getConditionNumber()).append("\"");
                    buffer.append(" type=\"").append("jump").append("\"");
                    buffer.append(" coverage=\"").append(coverage).append("\"");
            }
            else
            {
                    SwitchData switchData = (SwitchData) conditionData;
                    buffer.append(" number=\"").append(switchData.getSwitchNumber()).append("\"");
                    buffer.append(" type=\"").append("switch").append("\"");
                    buffer.append(" coverage=\"").append(coverage).append("\"");
            }
            buffer.append("/>");
            println(buffer.toString());
            decreaseIndentation();
    }
}
