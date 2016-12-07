/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

package com.redprairie.moca.server.expression.function;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.cache.CacheManager;
import com.redprairie.moca.exceptions.MocaDBException;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.exec.ServerContext;

/**
 * A low-level implementation of the TO_CHAR function, that defers to
 * a command if the conversion fails.  This is here because of existing
 * usage of the to_char library function that can't be eliminated or 
 * replicated without using specific database queries.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 * @version $Revision$
 */
public class ToCharFunction extends BaseFunction {
    
    @Override
    protected MocaValue invoke(ServerContext ctx, List<MocaValue> args) throws MocaException {
        if (args.size() != 1 && args.size() != 2) {
            throw new FunctionArgumentException("expected one or two argument");
        }

        MocaValue arg = args.get(0);
        
        // Simple case -- no arguments: directly convert to string value.
        if (args.size() == 1) {
            String s = arg.asString();
            return new MocaValue(MocaType.STRING, s);
        }
        
        // More complex case -- format string.
        String format = args.get(1).asString();
        
        if (format == null) {
            throw new FunctionArgumentException("format cannot be null");
        }
        
        // If null, the result will be null
        if (arg.isNull()) {
            return new MocaValue(MocaType.STRING, null);
        }
        
        // If the format is numeric only, we need to apply a numeric formatter.
        if (numericFormatPattern.matcher(format).matches()) {
            String numericFormat = format.replace('9', '#');
            
            // Anything with 0
            if (numericFormat.indexOf('0') >= 0) {
                boolean sawZero = false;
                int len = numericFormat.length();
                StringBuilder translated = new StringBuilder(len);
                for (int i = 0; i < len; i++) {
                    char c = numericFormat.charAt(i);
                    if (c == '0') {
                        sawZero = true;
                    }
                    else if (c == '#' && sawZero) {
                        c = '0';
                    }
                    else if (c == '.') {
                        sawZero = false;
                    }
                    translated.append(c);
                }
                numericFormat = translated.toString();
            }

            double sourceValue = arg.asDouble();

            // Handle implied decimal place capability.
            if (numericFormat.indexOf('V') != -1) {
                int places = numericFormat.length() - numericFormat.indexOf('V') - 1;
                numericFormat = numericFormat.replace("V", "");
                if (places > 0) {
                    sourceValue *= Math.pow(10.0, places);
                }
            }

            if(numericFormat.indexOf('S') != -1) {
                numericFormat = numericFormat.replace('S', '+') + ';' + numericFormat.replace('S', '-');
            }
            
            try {
                String s = new DecimalFormat(numericFormat, new DecimalFormatSymbols(Locale.ROOT)).format(sourceValue);
                // Perform padding, as needed.
                if (s.length() < format.length()) {
                    int paddingRequired = format.length() - s.length();
                    StringBuilder tmp = new StringBuilder(format.length());
                    for (int i = 0; i < paddingRequired; i++) {
                        tmp.append(' ');
                    }
                    tmp.append(s);
                    s = tmp.toString();
                }
                return new MocaValue(MocaType.STRING, s);
            }
            catch (IllegalArgumentException e) {
                // Ignore
            }
        }
        else {
            ConcurrentMap<String, SimpleDateFormat> formatCache = 
                CacheManager.getCache("date_format", null);
            
            SimpleDateFormat sdf = formatCache.get(format.toUpperCase());
            // Make sure before anything that we have a valid date first
            Date date = arg.asDate();
            if (date == null) {
                throw new FunctionArgumentException("Invalid date: " + arg.asString());
            }
            // If there was a valid date format and the date resolves correctly
            // then format it.
            if (sdf != null) {
                sdf = (SimpleDateFormat) sdf.clone();
                return new MocaValue(MocaType.STRING, sdf.format(date));
            }
        }

        // Fallback mode -- gather results from the database, but only if using 
        // Oracle.
        if (ctx.getDbType() != DBType.ORACLE) {
            throw new FunctionArgumentException("unable to parse format");
        }
        
        BindList sqlArgs = new BindList();
        sqlArgs.add("value", args.get(0).getType(), args.get(0).getValue());
        sqlArgs.add("format", MocaType.STRING, format);
        sqlArgs.add("result", MocaType.STRING_REF, "");
        
        try {
            // Do this differently if we think this is a numeric format
            if (format.indexOf('0') >= 0 || format.indexOf('9') >= 0) {
                ctx.executeSQL("begin :result := to_char(to_number(:value),:format); end;",
                    sqlArgs, false, true);
            }
            else {
                ctx.executeSQL("begin :result := to_char(to_date(:value, 'YYYYMMDDHH24MISS'),:format); end;",
                    sqlArgs, false, true);

            }
        }
        catch (SQLException e) {
            throw new MocaDBException(e);
        }
        
        return new MocaValue(MocaType.STRING, sqlArgs.getValue("result"));
    }
    
    static final Pattern numericFormatPattern = Pattern.compile("[09,.SV]*");
}
