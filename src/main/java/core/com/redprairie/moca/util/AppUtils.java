/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.Date;
import java.util.Properties;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.redprairie.moca.MocaConstants;
import com.redprairie.moca.MocaInterruptedException;

/**
 * Utility functions for applications.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author gvaneyck
 * @version $Revision$
 */
public class AppUtils {

    private AppUtils() {
        // We'll only load the version data once
        setupVersionData();
    }
    
    /**
     * Returns the same banner for Java applications as misGetStartBanner.
     * @param appName the name of the application.
     */
    public static String getStartBanner(String appName)
    {
        String banner;
        
        DateTimeFormatter outDate = DateTimeFormat.forPattern("EEE MMM d H:mm:ss yyyy");

        // Build the banner to return to the caller
        banner = String.format("%n%s %s - %s%n%n%s%n%n", appName,
            INSTANCE._fullVersion, outDate.print(new Date().getTime()),
            MocaConstants.COPYRIGHT_STRING);

        return banner;
    }

    /**
     * Returns the same version banner for Java applications as
     * misGetVersionBanner.
     * @param appName the name of the application.
     */
    public static String getVersionBanner(String appName)
    {
        return String.format("%n%s %s%n%n", appName, INSTANCE._fullVersion);
    }
    
    /**
     * Returns the full build version
     * @return The full build version
     */
    public static String getFullVersion() {
        return INSTANCE._fullVersion;
    }
    
    /**
     * Gets the major and minor revision of the application
     * server for example 2012.2. This will be null if only
     * a build date is present.
     * @return The major minor build version or null if only the build date is present.
     */
    public static String getMajorMinorRevision() {
        return INSTANCE._majorMinorRevision;
    }
    
    //
    // Implementation
    //
    
    private void setupVersionData()
    {
        InputStream in = AppUtils.class.getResourceAsStream(
                "/com/redprairie/moca/resources/build.properties");

        Properties buildProperties = new Properties();
        if (in != null) {
            try {
                buildProperties.load(in);
            }
            catch (InterruptedIOException e) {
                throw new MocaInterruptedException(e);
            }
            catch (IOException e) {
                // Use default if unable to load properties
            }
            finally {
                try { in.close(); } catch (IOException ignore) { }
            }
        }
        
        String releaseVersion = buildProperties.getProperty("releaseVersion");
        try {
            DateTimeFormatter inDate = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter outDate = DateTimeFormat.forPattern("MMM d yyyy");
            
            _fullVersion = "Build date: " + outDate.print(inDate.parseDateTime(releaseVersion));
        }
        catch (IllegalArgumentException e) {
            // We built with release=Y (probably), just return the version
            String[] splitVersion = releaseVersion.split("\\.");
            // Safety check
            if (splitVersion.length > 2) {
                _majorMinorRevision = splitVersion[0] + "." + splitVersion[1];
            }
            _fullVersion = releaseVersion;
        }
    }
    
    private String _fullVersion;
    private String _majorMinorRevision;
    private final static AppUtils INSTANCE = new AppUtils();
}
