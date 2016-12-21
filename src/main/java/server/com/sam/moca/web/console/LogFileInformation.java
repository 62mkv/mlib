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

package com.sam.moca.web.console;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.sam.moca.EditableResults;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.SimpleResults;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.exec.SystemContext;
    
public class LogFileInformation {

    private LogFileInformation() {
    }

    public static MocaResults getLogFiles() throws IOException {
        SystemContext sysContext = ServerUtils.globalContext();
        String lesdir = sysContext.getVariable("LESDIR");
        
        String logdir = lesdir + "/log";
        File directory = new File(logdir);
        List<File> files = getFilesForDirectory(directory);
                   
        // Create a new result set.
        EditableResults res = new SimpleResults();
        res.addColumn("filename", MocaType.STRING);
        res.addColumn("pathname", MocaType.STRING);
        res.addColumn("modified", MocaType.DATETIME);
        res.addColumn("size", MocaType.DOUBLE);
        
        // Iterate through each file in the directory.
        for (File file : files ){
            // We only care about files that are readable by us.
            if (!file.isFile() || !file.canRead()) {
                continue;
            }
            
            // Add this file to the result set.
            res.addRow();
            res.setStringValue("filename", file.getName()); 
            res.setStringValue("pathname", file.getPath().substring(logdir.length() + 1));
            res.setDateValue("modified", new Date(file.lastModified()));
            res.setDoubleValue("size", file.length());
        }
        
        return res;
    }
    
    static private List<File> getFilesForDirectory(File directory) throws FileNotFoundException {
        List<File> result = new ArrayList<File>();
        File[] fileArray = directory.listFiles();   
        List<File> files = Arrays.asList(fileArray);
        
        // First add the files in just this directory.
        for (File file : files) {
            result.add(file); 
        }
        
        // Then add the file in any sub-directories.
        for (File file : files) {
            if (file.isDirectory()) {
                List<File> children = getFilesForDirectory(file);
                result.addAll(children);
            }
        }
      
        return result;
    }
}