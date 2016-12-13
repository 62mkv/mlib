/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.applications.traceanalyzer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A TraceParser that handles the log4j 1 format used
 * in MOCA from versions 2010.1 through 2012.2.
 * @author rrupp
 */
class Log4jOneTraceParser implements TraceParser {       

    @Override
    public boolean isNewLine(String line) {
        return LINE_REGEX.matcher(line).matches();
    }

    @Override
    public TraceLine parseLine(String line) {
        Matcher matcher = LINE_REGEX.matcher(line);
        if (!matcher.matches()) {
            return null;
        }
        
        int stackLevel = matcher.group(9) != null ? Integer.valueOf(matcher.group(9)) : 0;

        Date date;
        try {
            date = _dateFormatter.parse(matcher.group(4));
        }
        catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return new TraceLine(line, Integer.valueOf(matcher.group(1)), matcher.group(2), matcher.group(3),
            date, matcher.group(4), matcher.group(6), stackLevel, matcher.group(11));
            
    }
    
    // Shows the matching for the regex, numbers denote the group number
    // For thread ID it's left justified with a minimum width of 3 and no max (but we use 6 here)
    // so it will be followed by at least 1 space or up to 3 if the thread ID was only a single digit
    // Format is:                                            <thread_id>(1)    <sessionID>(2) <debugLevel>(3)     <timestamp>(4)        (5)<loggerName>(6)(7)(8)<optional_stackLevel>(9)(10) <logMessage>(11)
    private static final Pattern LINE_REGEX = Pattern.compile("(\\d{1,6}) {1,3}([a-f0-9]{4}) (T|D|I|W|E|F) (\\d{2}:\\d{2}:\\d{2},\\d{3}) (\\()(.{10})(\\))( \\[)?([0-9]{1,3})?(\\])? (.*)");

    // MOCA's defined log4j format: "%-3t %-4.4S %1.1p %d{ABSOLUTE} (%-10.10c{1}) %s%m%n";
    
    // Format for the timestamp in the log file
    static final String DATE_FORMAT = "HH:mm:ss,SSS";
    private final SimpleDateFormat _dateFormatter = new SimpleDateFormat(DATE_FORMAT);
}
