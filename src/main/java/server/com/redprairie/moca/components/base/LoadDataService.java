/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.components.base;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.applications.mload.Mload;
import com.redprairie.moca.client.LoginFailedException;
import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class LoadDataService {
    /**
     * This will execute mload for the given arguments.
     * @param moca
     * @param dataDirectory
     * @param dataFile
     * @param controlFile
     * @param useHeader
     * @param delimiter
     * @return
     * @throws FileNotFoundException
     * @throws LoginFailedException
     * @throws IOException
     * @throws OptionsException
     * @throws MocaException
     * @throws ParseException
     */
    public MocaResults loadDataFile(MocaContext moca, String dataDirectory, 
            String dataFile, String controlFile, boolean useHeader, 
            String delimiter) throws FileNotFoundException, LoginFailedException, 
            IOException, OptionsException, MocaException, ParseException {
        EditableResults retRes = moca.newResults();
        List<String> list = new ArrayList<String>(5);
        
        if (useHeader) {
            list.add("-H");
        }
        
        if (dataDirectory != null && dataDirectory.trim().length() > 0) {
            list.add("-D" + dataDirectory);
        }
        
        if (dataFile != null && dataFile.trim().length() > 0) {
            list.add("-d" + dataFile);
        }
        
        if (controlFile != null && controlFile.trim().length() > 0) {
            list.add("-c" + controlFile);
        }
        
        if (delimiter != null && delimiter.trim().length() > 0) {
            list.add("-f" + delimiter);
        }
        
        Options opts = Options.parse(Mload.COMMAND_LINE_OPTIONS, list.toArray(
                new String[list.size()]));
        
        StringBuilder sb = new StringBuilder();
        
        Mload mload = new Mload(opts);
        mload.setStreams(sb, sb);
        
        int errorCount = mload.load();
        
        retRes.addColumn("err_cnt", MocaType.INTEGER);
        retRes.addColumn("result", MocaType.STRING);
        
        retRes.addRow();
        
        retRes.setIntValue(0, errorCount);
        retRes.setStringValue(1, sb.toString());
        
        if (errorCount > 0) {
            throw new LoadDataException(retRes);
        }
        
        return retRes;
    }
}
