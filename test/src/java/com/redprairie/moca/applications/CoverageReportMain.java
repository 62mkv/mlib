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

package com.redprairie.moca.applications;

import java.io.File;

import net.sourceforge.cobertura.coveragedata.CoverageDataFileHandler;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.util.CommandLineBuilder;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.Header;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.applications.cobertura.CoberturaComplexityCalculator;
import com.redprairie.moca.applications.cobertura.ComplexityCalculator;
import com.redprairie.moca.applications.cobertura.HTMLReport;
import com.redprairie.moca.applications.cobertura.MocaComplexityCalculator;
import com.redprairie.moca.applications.cobertura.SummaryXMLReport;
import com.redprairie.moca.applications.cobertura.XMLReport;
import com.redprairie.moca.coverage.repository.MocaProjectData;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation All Rights Reserved
 * 
 * @author wburns
 */
public class CoverageReportMain {
    private static final Logger LOGGER = LogManager.getLogger(CoverageReportMain.class.getName());

    private String format = "html";
    private File dataFile = null;
    private File destinationDir = null;
    private String encoding = "UTF-8";

    private void parseArguments(String[] args) throws Exception {
        FileFinder finder = new FileFinder();
        String baseDir = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--basedir")) {
                baseDir = args[++i];
            }
            else if (args[i].equals("--datafile")) {
                setDataFile(args[++i]);
            }
            else if (args[i].equals("--destination")) {
                setDestination(args[++i]);
            }
            else if (args[i].equals("--format")) {
                setFormat(args[++i]);
            }
            else if (args[i].equals("--encoding")) {
                setEncoding(args[++i]);
            }
            else if (args[i].equals("-h")) {
                System.out
                    .println("Usage: mbuildcoveragereport [--datafile <datafile>] [--destination <destination>] [-h] [--format <format>] [--encoding <encoding>] <src directories>");
                return;
            }
            else {
                if (baseDir == null) {
                    finder.addSourceDirectory(args[i]);
                }
                else {
                    finder.addSourceFile(baseDir, args[i]);
                }
            }
        }

        if (dataFile == null)
            dataFile = CoverageDataFileHandler.getDefaultDataFile();

        if (destinationDir == null) {
            System.err.println("Error: destination directory must be set");
            System.exit(1);
        }

        if (format == null) {
            System.err.println("Error: format must be set");
            System.exit(1);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("format is " + format + " encoding is " + encoding);
            LOGGER.debug("dataFile is " + dataFile.getAbsolutePath());
            LOGGER.debug("destinationDir is "
                    + destinationDir.getAbsolutePath());
        }

        ProjectData projectData = CoverageDataFileHandler
            .loadCoverageData(dataFile);

        if (projectData == null) {
            System.err.println("Error: Unable to read from data file "
                    + dataFile.getAbsolutePath());
            System.exit(1);
        }

        ComplexityCalculator complexity;
        if (projectData instanceof MocaProjectData) {
            complexity = new MocaComplexityCalculator();
        }
        else {
            complexity = new CoberturaComplexityCalculator(finder);
        }

        if (format.equalsIgnoreCase("html")) {
            new HTMLReport(projectData, destinationDir, finder, complexity,
                encoding);
        }
        else if (format.equalsIgnoreCase("xml")) {
            new XMLReport(projectData, destinationDir, finder, complexity);
        }
        else if (format.equalsIgnoreCase("summaryXml")) {
            new SummaryXMLReport(projectData, destinationDir, finder,
                complexity);
        }
    }

    private void setFormat(String value) {
        format = value;
        if (!format.equalsIgnoreCase("html") && !format.equalsIgnoreCase("xml")
                && !format.equalsIgnoreCase("summaryXml")) {
            System.err
                .println(""
                        + "Error: format \""
                        + format
                        + "\" is invalid. Must be either html or xml or summaryXml");
            System.exit(1);
        }
    }

    private void setDataFile(String value) {
        dataFile = new File(value);
        if (!dataFile.exists()) {
            System.err.println("Error: data file " + dataFile.getAbsolutePath()
                    + " does not exist");
            System.exit(1);
        }
        if (!dataFile.isFile()) {
            System.err.println("Error: data file " + dataFile.getAbsolutePath()
                    + " must be a regular file");
            System.exit(1);
        }
    }

    private void setDestination(String value) {
        destinationDir = new File(value);
        if (destinationDir.exists()) {
            if (!destinationDir.isDirectory()) {
                System.err.println("Error: destination directory " + destinationDir
                        + " already exists but is not a directory");
                System.exit(1);
            }
        }
        else if (!destinationDir.mkdirs()) {
            System.err.println("Error: destination directory " + destinationDir
                    + " could not be created");
            System.exit(1);
        }
    }

    private void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public static void main(String[] args) throws Exception {
        Header.print(System.out);

        long startTime = System.currentTimeMillis();

        CoverageReportMain main = new CoverageReportMain();

        try {
            args = CommandLineBuilder.preprocessCommandLineArguments(args);
        }
        catch (Exception ex) {
            System.err.println("Error: Cannot process arguments: "
                    + ex.getMessage());
            System.exit(1);
        }

        main.parseArguments(args);

        long stopTime = System.currentTimeMillis();
        System.out.println("Report time: " + (stopTime - startTime) + "ms");
    }

}
