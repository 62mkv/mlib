/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.coverage.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;

import com.redprairie.moca.coverage.parse.CoverageMocaParser;
import com.redprairie.moca.coverage.repository.MocaClassData.ClassType;
import com.redprairie.moca.server.parse.MocaParseException;
import com.redprairie.moca.server.repository.Command;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.ComponentLibraryFilter;
import com.redprairie.moca.server.repository.file.RepositoryReadException;
import com.redprairie.moca.server.repository.file.xml.XMLCommandReader;
import com.redprairie.moca.server.repository.file.xml.XMLUtils;
import com.redprairie.moca.util.MocaUtils;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class XMLCoverageCommandReader extends XMLCommandReader {
    /**
     * 
     */
    public XMLCoverageCommandReader(ProjectData data, 
        ComponentLibraryFilter includeFilter, 
        ComponentLibraryFilter excludeFilter) {
        _projectData = data;
        _includeFilter = includeFilter;
        _excludeFilter = excludeFilter;
    }
    
    // @see com.redprairie.moca.server.repository.file.xml.XMLCommandReader#readLocalSyntaxCommand(java.lang.String, org.w3c.dom.Element, com.redprairie.moca.server.repository.ComponentLevel)
    @Override
    protected Command readLocalSyntaxCommand(String name, Element node,
        ComponentLevel level) throws RepositoryReadException {
        boolean canInstrument = true;
        // If include is provided it has to pass it
        if (_includeFilter != null) {
            canInstrument = _includeFilter.accept(level);
        }
        // If we want to instrument and exclude is provided see if it is excluded
        if (canInstrument && _excludeFilter != null) {
            canInstrument = !_excludeFilter.accept(level);
        }
        if (!canInstrument) {
            _logger.debug(MocaUtils.concat("Command was not instrumented: ", 
                name, " due to component level: " + level + " not be included "));
            return super.readLocalSyntaxCommand(name, node, level);
        }
        String commandDir = level.getCmdDir();
        String replacedCommandDir = commandDir.substring(commandDir.lastIndexOf(
            File.separatorChar) + 1);
        MocaClassData classData = new MocaClassData(replacedCommandDir + "." + name, 
            ClassType.COMMAND);
        String fileName = node.getOwnerDocument().getDocumentURI();
        
        LineNumberReader lr = null;
        int lineOffset = 0;
        // TODO: hack way of getting line numbers
        try {
            lr = new LineNumberReader(new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(new URI(fileName))), "UTF-8")));
            String line;
            while ((line = lr.readLine()) != null) {
                if (line.matches(".*<local-syntax>.*")) {
                    lineOffset = lr.getLineNumber();
                    if (line.matches(".*<!\\[CDATA\\[.*")) {
                        break;
                    }
                    else {
                        String nextLine = lr.readLine();
                        if (nextLine != null && nextLine.matches(".*<!\\[CDATA\\[.*")) {
                            lineOffset++;
                        }
                        break;
                    }
                }
            }
        }
        catch (UnsupportedEncodingException e) {
            throw new RepositoryReadException("Encoding is not supported.", e);
        }
        catch (IOException e) {
            throw new RepositoryReadException("There was a problem reading file: " + fileName, e);
        }
        catch (URISyntaxException e) {
            throw new RepositoryReadException("There was a problem reading file: " + fileName, e);
        }
        finally {
            if (lr != null) {
                try {
                    lr.close();
                }
                catch (IOException e) {
                    _logger.debug("Problem closing file: " + fileName, e);
                }
            }
        }
        String replacedFileName = fileName.replaceAll("(.*[\\/])+", "");
        classData.setSourceFileName(replacedFileName);
        String syntax = XMLUtils.readCDataValue(node, "local-syntax");

        if (syntax == null || syntax.length() == 0) {
            throw new RepositoryReadException("Missing syntax for Local Syntax command");
        }
        
        try {
            new CoverageMocaParser(syntax, classData, true, lineOffset).parse();
        }
        catch (MocaParseException e) {
            throw new RepositoryReadException("Local Syntax parse error: " + e, e);
        }
        
        classData.setContainsInstrumentationInfo();
        
        _projectData.addClassData(classData);

        LocalSyntaxCoverageCommand result = new LocalSyntaxCoverageCommand(
            name, level, classData, lineOffset);
        result.setSyntax(syntax);

        return result;
    }
    
    private final ProjectData _projectData;
    private final ComponentLibraryFilter _includeFilter;
    private final ComponentLibraryFilter _excludeFilter;
    private static final Logger _logger = LogManager.getLogger(XMLCoverageCommandReader.class);
}
