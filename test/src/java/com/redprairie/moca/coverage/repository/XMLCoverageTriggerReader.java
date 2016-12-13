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

package com.redprairie.moca.coverage.repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;

import net.sourceforge.cobertura.coveragedata.ProjectData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.coverage.parse.CoverageMocaParser;
import com.redprairie.moca.coverage.repository.MocaClassData.ClassType;
import com.redprairie.moca.server.parse.MocaParseException;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.ComponentLibraryFilter;
import com.redprairie.moca.server.repository.Trigger;
import com.redprairie.moca.server.repository.file.RepositoryReadException;
import com.redprairie.moca.server.repository.file.xml.XMLTriggerReader;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class XMLCoverageTriggerReader extends XMLTriggerReader {
    public XMLCoverageTriggerReader(ProjectData data, 
        ComponentLibraryFilter includeFilter, 
        ComponentLibraryFilter excludeFilter) {
        _projectData = data;
        _includeFilter = includeFilter;
        _excludeFilter = excludeFilter;
    }
    
    // @see com.redprairie.moca.server.repository.file.xml.XMLTriggerReader#read(java.io.File, com.redprairie.moca.server.repository.ComponentLevel)
    @Override
    public Trigger read(File xmlFile, ComponentLevel level)
            throws RepositoryReadException {
        Trigger trigger = super.read(xmlFile, level);
        return wrapTrigger(trigger, level, xmlFile);
    }
    
    private Trigger wrapTrigger(Trigger trigger, ComponentLevel level, File xmlFile) throws RepositoryReadException {
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
            return trigger;
        }
        String syntax = trigger.getSyntax();
        String commandDir = level.getCmdDir();
        String replacedCommandDir = commandDir.substring(commandDir.lastIndexOf(
            File.separatorChar) + 1);
        MocaClassData classData = new MocaClassData(replacedCommandDir + "." + 
                trigger.getName() + " on " + trigger.getCommand(), ClassType.TRIGGER);
        classData.setSourceFileName(xmlFile.getName());
        
        LineNumberReader lr = null;
        int lineOffset = 0;
        // TODO: hack way of getting line numbers
        try {
            lr = new LineNumberReader(new BufferedReader(new InputStreamReader(
                new FileInputStream(xmlFile), "UTF-8")));
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
            throw new RepositoryReadException("There was a problem reading file: " + xmlFile, e);
        }
        finally {
            if (lr != null) {
                try {
                    lr.close();
                }
                catch (IOException e) {
                    _logger.debug("Problem closing file: " + xmlFile, e);
                }
            }
        }
        if (syntax != null) {
            try {
                new CoverageMocaParser(syntax, classData, true, lineOffset).parse();
            }
            catch (MocaParseException e) {
                throw new RepositoryReadException("Local Syntax parse error: " + e, e);
            }
        }
        
        _projectData.addClassData(classData);
        classData.setContainsInstrumentationInfo();
        
        return new CoverageTrigger(trigger, classData, lineOffset);
    }
    
    private final ProjectData _projectData;
    private final ComponentLibraryFilter _includeFilter;
    private final ComponentLibraryFilter _excludeFilter;
    
    private static final Logger _logger = LogManager.getLogger(XMLCoverageTriggerReader.class);
}
