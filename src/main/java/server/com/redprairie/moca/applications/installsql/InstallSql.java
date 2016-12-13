/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.redprairie.moca.applications.installsql;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.logging.log4j.LogManager;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.applications.msql.Msql;
import com.redprairie.moca.applications.msql.MsqlEventHandler;
import com.redprairie.moca.applications.preprocessor.MocaPreProcessor;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.util.MocaUtils;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class InstallSql {
    
    public InstallSql(Appendable appendable) {
        super();
        _appendable = appendable;
    }
    
    /**
     * This will process all the files using the given preprocessor.  It will
     * then send the processed output to the appendable if in debug or pass
     * the stream to Msql to execute the processed code.  If not in debug
     * a list of exceptions will be returned that were encountered while running
     * Msql for the preprocessed output.
     * @param fileNames The files to preprocess
     * @param preprocessor The preprocessor to use for processing the files
     * @param debug Whether or not we are in debug mode
     * @return The list of exceptions if running not in debug.  Or else this
     *         will be null when running in debug mode
     * @throws IOException This occurs if there is a problem with the 
     *         preprocessor output or writing to the appender
     * @throws MocaException This occurs if there was a problem obtaining the
     *         database connection type
     */
    public List<MocaException> processFiles(List<String> fileNames,
            MocaPreProcessor preprocessor, boolean debug) throws IOException,
            MocaException {
        MocaContext moca = MocaUtils.currentContext();
        
        // Here we set the database type in the preprocessor
        DBType type;
        {
            // First we have to retrieve the database type
            String dbType = moca.getDb().getDbType();
            
            type = DBType.valueOf(dbType);
        }
        
        List<MocaException> exceptions = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(preprocessor.process( 
                    fileNames, type));
            
            if (debug) {
                String line;
                while ((line = reader.readLine()) != null) {
                    _appendable.append(line);
                    _appendable.append('\n');
                }
            }
            else {
                Msql msql = new Msql(true, false);
                msql.setCharset(Charset.forName("UTF-8"));
                msql.setAutoCommit(false);
                msql.addEventHandler(new MsqlEventHandler() {

                    @Override
                    public void notifyLine(String line, int multiLineCount) {
                        
                    }

                    @Override
                    public void traceEvent(String message, TraceType type) {
                        try {
                            if (! type.equals(TraceType.INTERACTIVE))
                                _appendable.append(message);
                        }
                        catch (IOException e) {
                            // TODO need to do something here
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void updatePrompt(String prompt) {
                        
                    }

                    @Override
                    public void notifyCommandExecution(String command, 
                            long duration) {
                        
                    }
                });
                // Execute the commands on msql
                exceptions = msql.executeCommands(reader);
            }
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    LogManager.getLogger(LogManager.ROOT_LOGGER_NAME).warn("There was a problem closing" +
                                "the stream from the preprocessor :" + 
                                e.getMessage());
                }
            }
        }
        return exceptions;
    }
    
    private final Appendable _appendable;
}
