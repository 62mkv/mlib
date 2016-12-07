/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.applications.traceanalyzer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Trace parser for the new JDA cloud ready format!
 */
public class JdaTraceParser implements TraceParser {
    
    @Override
    public boolean isNewLine(String line) {
        return NEW_LINE_REGEX.matcher(line).matches();
    }

    @Override
    public TraceLine parseLine(String line) {
        Matcher matcher = FULL_LINE_REGEX.matcher(line);
        if (!matcher.matches()) {
            return null;
        }
        Date date;
        try {
            date = _dateFormatter.parse(matcher.group(1));
        }
        catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
        return new TraceLine(line, Integer.valueOf(matcher.group(3).trim()), matcher.group(4), matcher.group(2).trim(),
            date, matcher.group(1), matcher.group(5), Integer.valueOf(matcher.group(6)), matcher.group(7));
            
    }
    
    // Detects a new line where the log message may span multiple lines therefore we don't have the ending []
    private static final String NEW_LINE_PATTERN =
  //                  <date>(1)                             <log_level>(2)      <thread_id>(3) <session_id>(4) <logger_name>(5) <stack_level>(6)  <log_message>(7)
 "(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}) (DEBUG|INFO |WARN |ERROR) \\[([0-9 ]{1,5}) ([a-f0-9]{4})\\] (.+?) \\[(\\d{1,3})\\] (.*)";

    //                                               <base_pattern>     <second_end_line_identifier_unused>
    private static final String FULL_TRACE_PATTERN = NEW_LINE_PATTERN + " \\[\\]";

    private static final Pattern NEW_LINE_REGEX = Pattern.compile(NEW_LINE_PATTERN);
    private static final Pattern FULL_LINE_REGEX = Pattern.compile(FULL_TRACE_PATTERN);

    // Format for the timestamp in the log file
    static final String DATE_FORMAT = "yyyy-mm-dd HH:mm:ss,SSS";
    private final SimpleDateFormat _dateFormatter = new SimpleDateFormat(DATE_FORMAT);

}
