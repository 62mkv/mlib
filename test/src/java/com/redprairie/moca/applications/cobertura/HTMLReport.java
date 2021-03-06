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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import net.sourceforge.cobertura.coveragedata.ClassData;
import net.sourceforge.cobertura.coveragedata.CoverageData;
import net.sourceforge.cobertura.coveragedata.LineData;
import net.sourceforge.cobertura.coveragedata.PackageData;
import net.sourceforge.cobertura.coveragedata.ProjectData;
import net.sourceforge.cobertura.coveragedata.SourceFileData;
import net.sourceforge.cobertura.reporting.html.JavaToHtml;
import net.sourceforge.cobertura.reporting.html.SourceFileDataBaseNameComparator;
import net.sourceforge.cobertura.reporting.html.files.CopyFiles;
import net.sourceforge.cobertura.util.FileFinder;
import net.sourceforge.cobertura.util.Header;
import net.sourceforge.cobertura.util.IOUtil;
import net.sourceforge.cobertura.util.Source;
import net.sourceforge.cobertura.util.StringUtil;

import org.apache.logging.log4j.Logger;

import com.sam.moca.coverage.repository.MocaClassData;
import com.sam.moca.coverage.repository.MocaClassData.ClassType;

/**
 * This is an html report copied from cobertura but customized to have
 * information that we want.
 * 
 * Copyright (c) 2011 Sam Corporation All Rights Reserved
 * 
 * @author wburns
 */
public class HTMLReport {

    private static final Logger LOGGER = LogManager.getLogger(HTMLReport.class);
    
    private File destinationDir;
    private FileFinder finder;
    private ComplexityCalculator complexity;
    private ProjectData projectData;
    private String encoding;

    /**
     * Create a coverage report
     * 
     * @param encoding
     */
    public HTMLReport(ProjectData projectData, File outputDir,
            FileFinder finder, ComplexityCalculator complexity, String encoding)
            throws Exception {
        this.destinationDir = outputDir;
        this.finder = finder;
        this.complexity = complexity;
        this.projectData = projectData;
        this.encoding = encoding;

        CopyFiles.copy(outputDir);
        generatePackageList();
        generateSourceFileLists();
        generateOverviews();
        generateSourceFiles();
    }

    private String generatePackageName(PackageData packageData) {
        if (packageData.getName().equals("")) return "(default)";
        return packageData.getName();
    }

    @SuppressWarnings("rawtypes")
    private void generatePackageList() throws IOException {
        File file = new File(destinationDir, "frame-packages.html");
        PrintWriter out = null;

        try {
            out = IOUtil.getPrintWriter(file);

            out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
            out.println("           \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");

            out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">");
            out.println("<head>");
            out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
            out.println("<title>Coverage Report</title>");
            out.println("<link title=\"Style\" type=\"text/css\" rel=\"stylesheet\" href=\"css/main.css\" />");
            out.println("</head>");
            out.println("<body>");
            out.println("<h5>Component Levels</h5>");
            out.println("<table width=\"100%\">");
            out.println("<tr>");
            out.println("<td nowrap=\"nowrap\"><a href=\"frame-summary.html\" onclick='parent.sourceFileList.location.href=\"frame-sourcefiles.html\"' target=\"summary\">All</a></td>");
            out.println("</tr>");

            Iterator iter = projectData.getPackages().iterator();
            while (iter.hasNext()) {
                PackageData packageData = (PackageData) iter.next();
                String url1 = "frame-summary-" + packageData.getName()
                        + ".html";
                String url2 = "frame-sourcefiles-" + packageData.getName()
                        + ".html";
                out.println("<tr>");
                out.println("<td nowrap=\"nowrap\"><a href=\"" + url1
                        + "\" onclick='parent.sourceFileList.location.href=\""
                        + url2 + "\"' target=\"summary\">"
                        + generatePackageName(packageData) + "</a></td>");
                out.println("</tr>");
            }
            out.println("</table>");
            out.println("</body>");
            out.println("</html>");
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void generateSourceFileLists() throws IOException {
        generateSourceFileList(null);
        Iterator iter = projectData.getPackages().iterator();
        while (iter.hasNext()) {
            PackageData packageData = (PackageData) iter.next();
            generateSourceFileList(packageData);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void generateSourceFileList(PackageData packageData)
            throws IOException {
        String filename;
        Collection<ClassData> classFiles;
        if (packageData == null) {
            filename = "frame-sourcefiles.html";
            classFiles = projectData.getClasses();
        }
        else {
            filename = "frame-sourcefiles-" + packageData.getName() + ".html";
            classFiles = packageData.getClasses();
        }

        Collection<SourceFileData> commandFiles = convertClassDataToSourceFileData(
            classFiles, ClassType.COMMAND);
        Collection<SourceFileData> triggerFiles = convertClassDataToSourceFileData(
            classFiles, ClassType.TRIGGER);

        // sourceFiles may be sorted, but if so it's sorted by
        // the full path to the file, and we only want to sort
        // based on the file's basename.
        Vector sortedSourceFiles = new Vector();
        sortedSourceFiles.addAll(commandFiles);
        Collections.sort(sortedSourceFiles,
            new SourceFileDataBaseNameComparator());

        // sourceFiles may be sorted, but if so it's sorted by
        // the full path to the file, and we only want to sort
        // based on the file's basename.
        Vector sortedTriggerFiles = new Vector();
        sortedTriggerFiles.addAll(triggerFiles);
        Collections.sort(sortedSourceFiles,
            new SourceFileDataBaseNameComparator());

        File file = new File(destinationDir, filename);
        PrintWriter out = null;
        try {
            out = IOUtil.getPrintWriter(file);

            out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
            out.println("           \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");

            out.println("<html>");
            out.println("<head>");
            out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
            out.println("<title>Coverage Report Commands</title>");
            out.println("<link title=\"Style\" type=\"text/css\" rel=\"stylesheet\" href=\"css/main.css\"/>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h5>");
            out.println(packageData == null ? "All Component Levels"
                    : generatePackageName(packageData));
            out.println("</h5>");
            if (!sortedSourceFiles.isEmpty()) {
                out.println("<div class=\"separator\">&nbsp;</div>");
                out.println("<h5>Commands</h5>");
                out.println("<table width=\"100%\">");
                out.println("<tbody>");

                for (Iterator iter = sortedSourceFiles.iterator(); iter
                    .hasNext();) {
                    SourceFileData sourceFileData = (SourceFileData) iter
                        .next();
                    out.println("<tr>");
                    String percentCovered;
                    if (sourceFileData.getNumberOfValidLines() > 0)
                        percentCovered = getPercentValue(sourceFileData
                            .getLineCoverageRate());
                    else
                        percentCovered = "N/A";
                    out.println("<td nowrap=\"nowrap\"><a target=\"summary\" href=\""
                            + sourceFileData.getNormalizedName()
                            + ".html\">"
                            + sourceFileData.getBaseName()
                            + "</a> <i>("
                            + percentCovered + ")</i></td>");
                    out.println("</tr>");
                }
                out.println("</tbody>");
                out.println("</table>");
            }

            if (!sortedTriggerFiles.isEmpty()) {
                out.println("<div class=\"separator\">&nbsp;</div>");
                out.println("<h5>Triggers</h5>");
                out.println("<table width=\"100%\">");
                out.println("<tbody>");

                for (Iterator iter = sortedTriggerFiles.iterator(); iter
                    .hasNext();) {
                    SourceFileData sourceFileData = (SourceFileData) iter
                        .next();
                    out.println("<tr>");
                    String percentCovered;
                    if (sourceFileData.getNumberOfValidLines() > 0)
                        percentCovered = getPercentValue(sourceFileData
                            .getLineCoverageRate());
                    else
                        percentCovered = "N/A";
                    out.println("<td nowrap=\"nowrap\"><a target=\"summary\" href=\""
                            + sourceFileData.getNormalizedName()
                            + ".html\">"
                            + sourceFileData.getBaseName()
                            + "</a> <i>("
                            + percentCovered + ")</i></td>");
                    out.println("</tr>");
                }
                out.println("</tbody>");
                out.println("</table>");
            }

            out.println("</body>");
            out.println("</html>");
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private void generateOverviews() throws IOException {
        generateOverview(null);
        Iterator iter = projectData.getPackages().iterator();
        while (iter.hasNext()) {
            PackageData packageData = (PackageData) iter.next();
            generateOverview(packageData);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void generateOverview(PackageData packageData) throws IOException {
        Iterator iter;

        String filename;
        if (packageData == null) {
            filename = "frame-summary.html";
        }
        else {
            filename = "frame-summary-" + packageData.getName() + ".html";
        }
        File file = new File(destinationDir, filename);
        PrintWriter out = null;

        try {
            out = IOUtil.getPrintWriter(file);
            ;

            out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
            out.println("           \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");

            out.println("<html>");
            out.println("<head>");
            out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
            out.println("<title>Coverage Report</title>");
            out.println("<link title=\"Style\" type=\"text/css\" rel=\"stylesheet\" href=\"css/main.css\"/>");
            out.println("<link title=\"Style\" type=\"text/css\" rel=\"stylesheet\" href=\"css/sortabletable.css\"/>");
            out.println("<script type=\"text/javascript\" src=\"js/popup.js\"></script>");
            out.println("<script type=\"text/javascript\" src=\"js/sortabletable.js\"></script>");
            out.println("<script type=\"text/javascript\" src=\"js/customsorttypes.js\"></script>");
            out.println("</head>");
            out.println("<body>");

            out.print("<h5>Coverage Report - ");
            out.print(packageData == null ? "All Component Levels"
                    : generatePackageName(packageData));
            out.println("</h5>");
            out.println("<div class=\"separator\">&nbsp;</div>");
            out.println("<table class=\"report\" id=\"packageResults\">");
            out.println(generateTableHeader("Component Levels", true));
            out.println("<tbody>");

            SortedSet packages;
            if (packageData == null) {
                // Output a summary line for all packages
                out.println(generateTableRowForTotal());

                // Get packages
                packages = projectData.getPackages();
            }
            else {
                // Get subpackages
                packages = projectData.getSubPackages(packageData.getName());
            }

            // Output a line for each package or subpackage
            iter = packages.iterator();
            while (iter.hasNext()) {
                PackageData subPackageData = (PackageData) iter.next();
                out.println(generateTableRowForPackage(subPackageData));
            }

            out.println("</tbody>");
            out.println("</table>");
            out.println("<script type=\"text/javascript\">");
            out.println("var packageTable = new SortableTable(document.getElementById(\"packageResults\"),");
            out.println("    [\"String\", \"Number\", \"Number\", \"Percentage\", \"Percentage\", \"FormattedNumber\"]);");
            out.println("packageTable.sort(0);");
            out.println("</script>");

            // Get the list of source files in this package
            Collection<SourceFileData> commandFiles;
            Collection<SourceFileData> triggerFiles;
            if (packageData == null) {
                PackageData defaultPackage = (PackageData) projectData.getChild("");
                if (defaultPackage != null) {
                    commandFiles = convertClassDataToSourceFileData(
                        defaultPackage.getClasses(), ClassType.COMMAND);
                    triggerFiles = convertClassDataToSourceFileData(
                        defaultPackage.getClasses(), ClassType.TRIGGER);
                }
                else {
                    commandFiles = new TreeSet<SourceFileData>();
                    triggerFiles = new TreeSet<SourceFileData>();
                }
            }
            else {
                commandFiles = convertClassDataToSourceFileData(
                    packageData.getClasses(), ClassType.COMMAND);
                triggerFiles = convertClassDataToSourceFileData(
                    packageData.getClasses(), ClassType.TRIGGER);
            }

            // Output a line for each source file
            if (commandFiles.size() > 0) {
                out.println("<div class=\"separator\">&nbsp;</div>");
                out.println("<table class=\"report\" id=\"commandResults\">");
                out.println(generateTableHeader(
                    "Commands in this Component Level", false));
                out.println("<tbody>");

                for (SourceFileData sourceFileData : commandFiles) {
                    out.println(generateTableRowsForSourceFile(sourceFileData));
                }

                out.println("</tbody>");
                out.println("</table>");
                out.println("<script type=\"text/javascript\">");
                out.println("var classTable = new SortableTable(document.getElementById(\"commandResults\"),");
                out.println("    [\"String\", \"Percentage\", \"Percentage\", \"FormattedNumber\"]);");
                out.println("classTable.sort(0);");
                out.println("</script>");
            }

            if (triggerFiles.size() > 0) {
                // Put triggers after commands
                out.println("<div class=\"separator\">&nbsp;</div>");
                out.println("<table class=\"report\" id=\"triggerResults\">");
                out.println(generateTableHeader(
                    "Triggers in this Component Level", false));
                out.println("<tbody>");

                for (SourceFileData sourceFileData : triggerFiles) {
                    out.println(generateTableRowsForSourceFile(sourceFileData));
                }

                out.println("</tbody>");
                out.println("</table>");
                out.println("<script type=\"text/javascript\">");
                out.println("var classTable = new SortableTable(document.getElementById(\"triggerResults\"),");
                out.println("    [\"String\", \"Percentage\", \"Percentage\", \"FormattedNumber\"]);");
                out.println("classTable.sort(0);");
                out.println("</script>");
            }

            out.println(generateFooter());

            out.println("</body>");
            out.println("</html>");
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private Collection<SourceFileData> convertClassDataToSourceFileData(
        Collection<ClassData> classDatas, ClassType type) {
        Collection<SourceFileData> retColl = new TreeSet<SourceFileData>();
        for (ClassData classData : classDatas) {
            if (type != null && classData instanceof MocaClassData) {
                if (type != ((MocaClassData) classData).getType()) {
                    continue;
                }
            }
            SourceFileData sourceFileData = new SourceFileData(
                classData.getSourceFileName());
            sourceFileData.addClassData(classData);
            retColl.add(sourceFileData);
        }
        return retColl;
    }

    @SuppressWarnings("rawtypes")
    private void generateSourceFiles() {
        Iterator iter = projectData.getSourceFiles().iterator();
        while (iter.hasNext()) {
            SourceFileData sourceFileData = (SourceFileData) iter.next();
            try {
                generateSourceFile(sourceFileData);
            }
            catch (IOException e) {
                LOGGER.info("Could not generate HTML file for source file "
                        + sourceFileData.getName() + ": "
                        + e.getLocalizedMessage());
            }
        }
    }

    private void generateSourceFile(SourceFileData sourceFileData)
            throws IOException {
        if (!sourceFileData.containsInstrumentationInfo()) {
            LOGGER.info("Data file does not contain instrumentation "
                    + "information for the file " + sourceFileData.getName()
                    + ".  Ensure this class was instrumented, and this "
                    + "data file contains the instrumentation information.");
        }

        String filename = sourceFileData.getNormalizedName() + ".html";
        File file = new File(destinationDir, filename);
        PrintWriter out = null;

        try {
            out = IOUtil.getPrintWriter(file);

            out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
            out.println("           \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");

            out.println("<html>");
            out.println("<head>");
            out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>");
            out.println("<title>Coverage Report</title>");
            out.println("<link title=\"Style\" type=\"text/css\" rel=\"stylesheet\" href=\"css/main.css\"/>");
            out.println("<script type=\"text/javascript\" src=\"js/popup.js\"></script>");
            out.println("</head>");
            out.println("<body>");
            out.print("<h5>Coverage Report - ");
            String classPackageName = sourceFileData.getPackageName();
            if ((classPackageName != null) && classPackageName.length() > 0) {
                out.print(classPackageName + ".");
            }
            out.print(sourceFileData.getBaseName());
            out.println("</h5>");

            // Output the coverage summary for this class
            out.println("<div class=\"separator\">&nbsp;</div>");
            out.println("<table class=\"report\">");
            out.println(generateTableHeader("Name", false));
            out.println(generateTableRowsForSourceFile(sourceFileData));
            out.println("</table>");

            // Output this class's source code with syntax and coverage
            // highlighting
            out.println("<div class=\"separator\">&nbsp;</div>");
            out.println(generateHtmlizedJavaSource(sourceFileData));

            out.println(generateFooter());

            out.println("</body>");
            out.println("</html>");
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private String generateBranchInfo(LineData lineData, String content) {
        boolean hasBranch = (lineData != null) ? lineData.hasBranch() : false;
        if (hasBranch) {
            StringBuffer ret = new StringBuffer();
            ret.append("<a title=\"Line ").append(lineData.getLineNumber())
                .append(": Conditional coverage ")
                .append(lineData.getConditionCoverage());
            if (lineData.getConditionSize() > 1) {
                ret.append(" [each condition: ");
                for (int i = 0; i < lineData.getConditionSize(); i++) {
                    if (i > 0) ret.append(", ");
                    ret.append(lineData.getConditionCoverage(i));
                }
                ret.append("]");
            }
            ret.append(".\">").append(content).append("</a>");
            return ret.toString();
        }
        else {
            return content;
        }
    }

    private String generateHtmlizedJavaSource(SourceFileData sourceFileData) {
        Source source = finder.getSource(sourceFileData.getName());

        if (source == null) {
            return "<p>Unable to locate " + sourceFileData.getName()
                    + ".  Have you specified the source directory?</p>";
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(
                source.getInputStream(), encoding));
        }
        catch (UnsupportedEncodingException e) {
            return "<p>Unable to open " + source.getOriginDesc()
                    + ": The encoding '" + encoding
                    + "' is not supported by your JVM.</p>";
        }
        catch (Throwable t) {
            return "<p>Unable to open " + source.getOriginDesc() + ": "
                    + t.getLocalizedMessage() + "</p>";
        }

        StringBuffer ret = new StringBuffer();
        ret.append("<table cellspacing=\"0\" cellpadding=\"0\" class=\"src\">\n");
        try {
            String lineStr;
            JavaToHtml javaToHtml = new JavaToHtml();
            int lineNumber = 1;
            while ((lineStr = br.readLine()) != null) {
                ret.append("<tr>");
                if (sourceFileData.isValidSourceLineNumber(lineNumber)) {
                    LineData lineData = sourceFileData
                        .getLineCoverage(lineNumber);
                    ret.append("  <td class=\"numLineCover\">&nbsp;"
                            + lineNumber + "</td>");
                    if (lineData.isCovered()) {
                        ret.append("  <td class=\"nbHitsCovered\">"
                                + generateBranchInfo(
                                    lineData,
                                    "&nbsp;"
                                            + lineData.getHits() ) + "</td>");
                        ret.append("  <td class=\"src\"><pre class=\"src\">&nbsp;"
                                + generateBranchInfo(lineData,
                                    javaToHtml.process(lineStr))
                                + "</pre></td>");
                    }
                    else {
                        ret.append("  <td class=\"nbHitsUncovered\">"
                                + generateBranchInfo(
                                    lineData,
                                    "&nbsp;"
                                            + lineData.getHits()) + "</td>");
                        ret.append("  <td class=\"src\"><pre class=\"src\"><span class=\"srcUncovered\">&nbsp;"
                                + generateBranchInfo(lineData,
                                    javaToHtml.process(lineStr))
                                + "</span></pre></td>");
                    }
                }
                else {
                    ret.append("  <td class=\"numLine\">&nbsp;" + lineNumber
                            + "</td>");
                    ret.append("  <td class=\"nbHits\">&nbsp;</td>\n");
                    ret.append("  <td class=\"src\"><pre class=\"src\">&nbsp;"
                            + javaToHtml.process(lineStr) + "</pre></td>");
                }
                ret.append("</tr>\n");
                lineNumber++;
            }
        }
        catch (IOException e) {
            ret.append("<tr><td>Error reading " + source.getOriginDesc() + ": "
                    + e.getLocalizedMessage() + "</td></tr>\n");
        }
        finally {
            try {
                br.close();
                source.close();
            }
            catch (IOException e) {
            }
        }

        ret.append("</table>\n");

        return ret.toString();
    }

    private static String generateFooter() {
        return "<div class=\"footer\">Report generated by "
                + "Sam <a href=\"http://cobertura.sourceforge.net/\" target=\"_top\">Cobertura</a> "
                + Header.version() + " on "
                + DateFormat.getInstance().format(new Date()) + ".</div>";
    }

    private static String generateTableHeader(String title,
        boolean showColumnForNumberOfCommands) {
        StringBuffer ret = new StringBuffer();
        ret.append("<thead>");
        ret.append("<tr>");
        ret.append("  <td class=\"heading\">" + title + "</td>");
        if (showColumnForNumberOfCommands) {
            ret.append("  <td class=\"heading\"># Commands</td>");
            ret.append("  <td class=\"heading\"># Triggers</td>");
        }
        ret.append("  <td class=\"heading\">"
                + generateHelpURL("Line Coverage",
                    "The percent of lines executed by this test run.")
                + "</td>");
        ret.append("  <td class=\"heading\">"
                + generateHelpURL("Branch Coverage",
                    "The percent of branches executed by this test run.")
                + "</td>");
        ret.append("  <td class=\"heading\">"
                + generateHelpURL(
                    "Pipe Depth",
                    "The longest command stream (continuous number of commands separated by pipes).  Ideally this should not be longer than 20.")
                + "</td>");
        ret.append("</tr>");
        ret.append("</thead>");
        return ret.toString();
    }

    private static String generateHelpURL(String text, String description) {
        StringBuffer ret = new StringBuffer();
        boolean popupTooltips = false;
        if (popupTooltips) {
            ret.append("<a class=\"hastooltip\" href=\"help.html\" onclick=\"popupwindow('help.html'); return false;\">");
            ret.append(text);
            ret.append("<span>" + description + "</span>");
            ret.append("</a>");
        }
        else {
            ret.append("<a class=\"dfn\" href=\"help.html\" onclick=\"popupwindow('help.html'); return false;\">");
            ret.append(text);
            ret.append("</a>");
        }
        return ret.toString();
    }

    @SuppressWarnings("unchecked")
    private String generateTableRowForTotal() {
        StringBuffer ret = new StringBuffer();
        double ccn = complexity.getCCNForProject(projectData);

        ret.append("  <tr>");
        ret.append("<td><b>All Component Levels</b></td>");
        // TODO: need to get correct count for commands
        ret.append("<td class=\"value\">" + convertClassDataToSourceFileData(
            projectData.getClasses(), ClassType.COMMAND).size()
                + "</td>");
        // TODO: need to get correct count for triggers
        ret.append("<td class=\"value\">" + convertClassDataToSourceFileData(
            projectData.getClasses(), ClassType.TRIGGER).size()
            + "</td>");
        ret.append(generateTableColumnsFromData(projectData, ccn));
        ret.append("</tr>");
        return ret.toString();
    }

    @SuppressWarnings("unchecked")
    private String generateTableRowForPackage(PackageData packageData) {
        StringBuffer ret = new StringBuffer();
        String url1 = "frame-summary-" + packageData.getName() + ".html";
        String url2 = "frame-sourcefiles-" + packageData.getName() + ".html";
        double ccn = complexity.getCCNForPackage(packageData);

        ret.append("  <tr>");
        ret.append("<td><a href=\"" + url1
                + "\" onclick='parent.sourceFileList.location.href=\"" + url2
                + "\"'>" + generatePackageName(packageData) + "</a></td>");
        // TODO: need to get correct count for commands
        ret.append("<td class=\"value\">" + convertClassDataToSourceFileData(
            packageData.getClasses(), ClassType.COMMAND).size() + "</td>");
        // TODO: need to get correct count for triggers
        ret.append("<td class=\"value\">" + convertClassDataToSourceFileData(
            packageData.getClasses(), ClassType.TRIGGER).size() + "</td>");
        ret.append(generateTableColumnsFromData(packageData, ccn));
        ret.append("</tr>");
        return ret.toString();
    }

    @SuppressWarnings("rawtypes")
    private String generateTableRowsForSourceFile(SourceFileData sourceFileData) {
        StringBuffer ret = new StringBuffer();
        String sourceFileName = sourceFileData.getNormalizedName();
        double ccn = complexity.getCCNForSourceFile(sourceFileData);

        Iterator iter = sourceFileData.getClasses().iterator();
        while (iter.hasNext()) {
            ClassData classData = (ClassData) iter.next();
            ret.append(generateTableRowForClass(classData, sourceFileName, ccn));
        }

        return ret.toString();
    }

    private String generateTableRowForClass(ClassData classData,
        String sourceFileName, double ccn) {
        StringBuilder ret = new StringBuilder();

        ret.append("  <tr>");
        ret.append("<td><a href=\"" + sourceFileName + ".html\">"
                + classData.getBaseName() + "</a></td>");
        ret.append(generateTableColumnsFromData(classData, ccn));
        ret.append("</tr>\n");
        return ret.toString();
    }

    /**
     * Return a string containing three HTML table cells. The first cell
     * contains a graph showing the line coverage, the second cell contains a
     * graph showing the branch coverage, and the third cell contains the code
     * complexity.
     * 
     * @param ccn The code complexity to display. This should be greater than 1.
     * @return A string containing the HTML for three table cells.
     */
    private static String generateTableColumnsFromData(
        CoverageData coverageData, double ccn) {
        int numLinesCovered = coverageData.getNumberOfCoveredLines();
        int numLinesValid = coverageData.getNumberOfValidLines();
        int numBranchesCovered = coverageData.getNumberOfCoveredBranches();
        int numBranchesValid = coverageData.getNumberOfValidBranches();

        // The "hidden" CSS class is used below to write the ccn without
        // any formatting so that the table column can be sorted correctly
        return "<td>" + generatePercentResult(numLinesCovered, numLinesValid)
                + "</td><td>"
                + generatePercentResult(numBranchesCovered, numBranchesValid)
                + "</td><td class=\"value\"><span class=\"hidden\">" + ccn
                + ";</span>" + getDoubleValue(ccn) + "</td>";
    }

    /**
     * This is crazy complicated, and took me a while to figure out, but it
     * works. It creates a dandy little percentage meter, from 0 to 100.
     * 
     * @param dividend The number of covered lines or branches.
     * @param divisor The number of valid lines or branches.
     * @return A percentage meter.
     */
    private static String generatePercentResult(int dividend, int divisor) {
        StringBuffer sb = new StringBuffer();

        sb.append("<table cellpadding=\"0px\" cellspacing=\"0px\" class=\"percentgraph\"><tr class=\"percentgraph\"><td align=\"right\" class=\"percentgraph\" width=\"40\">");
        if (divisor > 0)
            sb.append(getPercentValue((double) dividend / divisor));
        else
            sb.append(generateHelpURL(
                "N/A",
                "Line coverage and branch coverage will appear as \"Not Applicable\" when Cobertura can not find line number information in the .class file.  This happens for stub and skeleton classes, interfaces, or when the class was not compiled with \"debug=true.\""));
        sb.append("</td><td class=\"percentgraph\"><div class=\"percentgraph\">");
        if (divisor > 0) {
            sb.append("<div class=\"greenbar\" style=\"width:"
                    + (dividend * 100 / divisor) + "px\">");
            sb.append("<span class=\"text\">");
            sb.append(dividend);
            sb.append("/");
            sb.append(divisor);
        }
        else {
            sb.append("<div class=\"na\" style=\"width:100px\">");
            sb.append("<span class=\"text\">");
            sb.append(generateHelpURL(
                "N/A",
                "Line coverage and branch coverage will appear as \"Not Applicable\" when Cobertura can not find line number information in the .class file.  This happens for stub and skeleton classes, interfaces, or when the class was not compiled with \"debug=true.\""));
        }
        sb.append("</span></div></div></td></tr></table>");

        return sb.toString();
    }

    private static String getDoubleValue(double value) {
        return new DecimalFormat().format(value);
    }

    private static String getPercentValue(double value) {
        return StringUtil.getPercentValue(value);
    }

}
