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

package com.redprairie.moca.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Utility functions for converting a date to and from MOCA date string format.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class DateUtils {
    /**
     * Formats a standard Java date into the MOCA date String (YYYYMMDDHHmmss) 
     * format.  If the date object provided is <code>null</code> then 
     * <code>null</code> is returned.
     * 
     * @param date The date object to format
     * @return The formatted string
     */
    public static String formatDate(Date date) {
            
        if (date == null) return null;
        
        DateFormat tmp = (DateFormat)_dateFormat.clone();
        return tmp.format(date);
    }
    
    /**
     * Parses a MOCA date String (YYYYMMDDHHmmss) into a standard Java date object.  This method
     * assumes that the string should be parsed in the current timezone.  If the date is formatted
     * incorrectly, <code>null</code> is returned.
     * 
     * @param dateString The date string to parse
     * @return The date as parsed from the string or null if invalid
     */
    public static Date parseDate(String dateString) {
            
        if (dateString == null) return null;

        try {
            DateFormat tmp = (DateFormat)_dateFormat.clone();
            tmp.setLenient(false);
            return tmp.parse(dateString);
        }
        catch (ParseException e) {
            return null;
        }
    }
    
    private static final DateFormat _dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
}
