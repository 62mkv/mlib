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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.cache.CacheManager;
import com.redprairie.moca.exceptions.MocaDBException;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.util.MocaUtils;

/**
 * A low-level implementation of the TO_DATE function, that defers to
 * a command if the conversion fails.  This is here because of existing
 * usage of the to_date library function that can't be eliminated or 
 * replicated without using specific database queries.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 * @version $Revision$
 */
public class ToDateFunction extends BaseFunction {
    
    @Override
    protected MocaValue invoke(ServerContext ctx, List<MocaValue> args) throws MocaException {
        if (args.size() != 1 && args.size() != 2) {
            throw new FunctionArgumentException("expected one or two argument");
        }

        MocaValue arg = args.get(0);
        
        // Simple case -- no arguments: directly convert to date/time value.
        if (args.size() == 1) {
            Date d = null;

            if (arg.getType() != MocaType.DATETIME) {
                String strValue = arg.asString();

                if (strValue != null && strValue.length() == 8) {
                    try {
                        d = ((SimpleDateFormat)shortDateFormat.clone()).parse(strValue);
                    }
                    catch (ParseException e) {
                        throw new FunctionArgumentException("Invalid date: " + arg.asString());
                    }
                }
            }
            
            if (d == null) {
                if (!arg.isNull()) {
                    d = MocaUtils.parseDate(arg.asString());
                    if (d == null) {
                        throw new FunctionArgumentException("Invalid date: " + arg.asString());
                    }
                }
            }
            
            return new MocaValue(MocaType.DATETIME, d);
        }
        
        // More complex case -- format string.
        String format = args.get(1).asString();
        if (format == null) {
            throw new FunctionArgumentException("format cannot be null");
        }
        
        // Null input means null result
        if (arg.isNull()) {
            return new MocaValue(MocaType.DATETIME, null);
        }
        
        String input = arg.asString().trim();

        // Make sure we're looking up translated formats using the upper-case patterns.
        format = format.toUpperCase();

        ConcurrentMap<String, SimpleDateFormat> formatCache = 
            CacheManager.getCache("date_format", null);
        SimpleDateFormat sdf = formatCache.get(format);
        
        // We may have to tweak the date formatter or the input.
        if (sdf != null) {
            // Expect the length of the format and input to match.
            int formatLength = format.length();
            if (format.contains("HH24") || format.contains("HH12")) {
                formatLength -= 2;
            }
            
            // If the input is too long, truncate the input before parsing it.
            if (input.length() > formatLength) {
                input = input.substring(0, formatLength);
            }
            // If the input is too short, we'll need to try a truncated format.
            else if (input.length() < formatLength) {
                sdf = formatCache.get(format.substring(0, input.length()));
            }
        }
        
        if (sdf != null) {
            sdf = (SimpleDateFormat) sdf.clone();
            sdf.setLenient(false);
            
            try {
                Date result = sdf.parse(input);
                return new MocaValue(MocaType.DATETIME, result); 
            }
            catch (ParseException e) {
                // If some error occurred in parsing, defer to the db. Maybe it
                // will have better luck.
            }
        }
        
        // Fallback mode -- gather results from the database, but only if using 
        // Oracle.
        if (ctx.getDbType() != DBType.ORACLE) {
            throw new FunctionArgumentException("unable to parse date");
        }
     
        BindList sqlArgs = new BindList();
        sqlArgs.add("value", MocaType.STRING, args.get(0).asString());
        sqlArgs.add("format", MocaType.STRING, args.get(1).asString());
        
        try {
            MocaResults res = ctx.executeSQL("select to_date (:value,:format) result from dual",
                           sqlArgs, false, false);
            res.next();
            return new MocaValue(MocaType.DATETIME, res.getDateTime("result"));
        }
        catch (SQLException e) {
            throw new MocaDBException(e);
        }
    }
    
    private static final SimpleDateFormat shortDateFormat = new SimpleDateFormat("yyyyMMdd");
    
    static {
        shortDateFormat.setLenient(false);
    }
}
