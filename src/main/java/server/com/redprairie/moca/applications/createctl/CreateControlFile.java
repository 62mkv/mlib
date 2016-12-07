/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.applications.createctl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.applications.msql.Msql;

/**
 * This class is the Java representation of create control file.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class CreateControlFile {

    /**
     * @param _msql
     */
    public CreateControlFile(Msql msql) {
        super();
        _msql = msql;
    }
    
    public Map<String, MocaException> loadControlFiles(File outputDirectory,
            String[] tables) throws IOException {
        return loadControlFiles(true, outputDirectory, null, 
                false, tables);
    }
    
    public Map<String, MocaException> unloadControlFiles(File outputDirectory,
            String whereClause, boolean createDataFile, String[] tables) 
            throws IOException {
        return loadControlFiles(false, outputDirectory, whereClause, 
                createDataFile, tables);
    }
    
    private Map<String, MocaException> loadControlFiles(boolean load, 
            File outputDirectory, String whereClause, boolean createDataFile,
            String[] tables) throws IOException {
        String directoryName = null;
        if (outputDirectory != null) {
            directoryName = outputDirectory.getCanonicalPath();
        }
        Map<String, MocaException> exceptions = new HashMap<String, MocaException>();
        
        for (String table : tables) {
            String lowerCaseTable = table.toLowerCase();
            try {
                StringBuilder builder = new StringBuilder("dump data where file_name = '");
                if (directoryName != null) {
                    builder.append(directoryName);
                    builder.append("/");
                }
                builder.append(lowerCaseTable);
                builder.append(".ctl' and dump_command = 'format control file where type = ''");
                builder.append(load ? "LOAD" : "UNLOAD");
                builder.append("'' and table_name = ''");
                builder.append(lowerCaseTable);
                builder.append("'' '");
                _msql.executeCommand(builder.toString());

            
                if (!load && createDataFile) {
                    StringBuilder dataBuilder = new StringBuilder("dump data where file_name = '");
                    if (directoryName != null) {
                        dataBuilder.append(directoryName);
                        dataBuilder.append("/");
                    }
                    dataBuilder.append(lowerCaseTable);
                    dataBuilder.append('/');
                    dataBuilder.append(lowerCaseTable);
                    dataBuilder.append(".csv' and dump_command = 'format control file where type = ''DATA'' and table_name = ''");
                    dataBuilder.append(lowerCaseTable);
                    dataBuilder.append("''");
                    if (whereClause != null) {
                        dataBuilder.append(" and where_command = ''");
                        
                        for (int i = 0; i < whereClause.length(); ++i) {
                            char character = whereClause.charAt(i);
                            dataBuilder.append(character);
                            if (character == '\'') {
                                // We have to convert a single quote into 4, so
                                // we append 3 more
                                dataBuilder.append("'''");
                            }
                        }
                        
                        dataBuilder.append("''");
                    }
                    dataBuilder.append(" '");
                
                    _msql.executeCommand(dataBuilder.toString());
                }
            }
            catch (MocaException e) {
                exceptions.put(table, e);
            }
        }
        
        return exceptions;
    }
    
    
    private final Msql _msql;
}
