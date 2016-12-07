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
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.exceptions.MocaDBException;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.exec.ServerContext;

/**
 * A low-level implementation of the TO_NUMBER function, that defers to
 * a command if the conversion fails.  This is here because of existing
 * usage of the to_number library function that can't be eliminated or 
 * replicated without using specific database queries.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 * @version $Revision$
 */
public class ToNumberFunction extends BaseFunction {
    
    @Override
    protected MocaValue invoke(ServerContext ctx, List<MocaValue> args) throws MocaException {
        
        // Simple case -- no arguments: directly convert to a numeric value.
        if (args.size() == 1) {
            MocaValue arg = args.get(0);
            if (arg.isNull()) {
                return new MocaValue(MocaType.DOUBLE, null);
            }
            else {
                if (arg.getType() == MocaType.INTEGER || arg.getType() == MocaType.DOUBLE) {
                    double d = arg.asDouble();
                    return new MocaValue(MocaType.DOUBLE, d);
                }
                else {
                    String s = arg.asString();
                    NumberFormat nf = NumberFormat.getInstance(Locale.US);
                    
                    ParsePosition pos = new ParsePosition(0);
                    Number parseResult = nf.parse(s.trim(), pos);

                    if (parseResult == null) {
                        parseResult = Double.valueOf(0.0);
                    }
                    
                    return new MocaValue(MocaType.DOUBLE, parseResult.doubleValue());
                }
            }
        }
        
        if (args.size() != 2) {
            throw new FunctionArgumentException("wrong number of arguments");
        }
        
        MocaValue arg = args.get(0);
        if (arg.isNull()) {
            return new MocaValue(MocaType.DOUBLE, null);
        }
        if (args.get(1).isNull()) {
            throw new FunctionArgumentException("argument format cannot be null");
        }

        String numericFormat = args.get(1).asString();
        
        Exception savedException = null;
        if (_formatPattern.matcher(numericFormat).matches()) {
            // Replace 9 with # for most cases.
            numericFormat = numericFormat.replace('9', '#');

            // For parsing purposes, zero is the same as any other digit.
            numericFormat = numericFormat.replace('0', '#');

            int places = 0;
            
            // Handle implied decimal place capability.
            if (numericFormat.indexOf('V') != -1) {
                places = numericFormat.length() - numericFormat.indexOf('V') - 1;
                numericFormat = numericFormat.replace("V", "");
            }

            if(numericFormat.indexOf('S') != -1) {
                numericFormat = numericFormat.replace('S', '+') + ';' + numericFormat.replace('S', '-');
            }
            
            try {
                DecimalFormat df = new DecimalFormat(numericFormat, new DecimalFormatSymbols(Locale.ROOT));
                ParsePosition pos = new ParsePosition(0);
                Number parseResult = df.parse(arg.asString().trim(), pos);

                if (parseResult == null) {
                    parseResult = Double.valueOf(0.0);
                }
                
                if (places > 0) {
                    parseResult = parseResult.doubleValue() / Math.pow(10.0, places);
                }
                
                return new MocaValue(MocaType.DOUBLE, parseResult.doubleValue());
            }
            catch (IllegalArgumentException e) {
                // Invalid format -- continue using DB
                savedException = e;
            }
        }

        // Fallback mode -- gather results from the database, but only if using 
        // Oracle.
        if (ctx.getDbType() != DBType.ORACLE) {
            throw new FunctionArgumentException("unable to parse number", savedException);
        }
     
        BindList sqlArgs = new BindList();
        sqlArgs.add("value", MocaType.STRING, args.get(0).asString());
        sqlArgs.add("format", MocaType.STRING, args.get(1).asString());
        sqlArgs.add("result", MocaType.DOUBLE_REF, 0.0);
        
        try {
            ctx.executeSQL("begin :result := to_number (:value,:format); end;",
                           sqlArgs, false, true);
        }
        catch (SQLException e) {
            throw new MocaDBException(e);
        }
        
        return new MocaValue(MocaType.DOUBLE, sqlArgs.getValue("result"));
    }
    
    private static final Pattern _formatPattern = Pattern.compile("^[09]*[.SV]?[09]*$"); 
}
