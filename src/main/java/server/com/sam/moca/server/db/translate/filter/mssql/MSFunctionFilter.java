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

package com.sam.moca.server.db.translate.filter.mssql;

import java.util.HashMap;
import java.util.Map;

import com.sam.moca.server.db.translate.filter.FunctionFilter;
import com.sam.moca.server.db.translate.filter.functions.FunctionHandler;
import com.sam.moca.server.db.translate.filter.functions.RoundTruncHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.AddMonthsHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.ChrHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.ConcatHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.DateDiffDaysHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.InstrHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.LpadHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.MSDecodeHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.ModHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.MonthsBetweenHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.ReplaceHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.RpadHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.SubstrHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.ToCharHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.ToDateHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.ToNumberHandler;
import com.sam.moca.server.db.translate.filter.mssql.functions.TruncHandler;

/**
 * SQL Translation filter for translating arbitrary functions into other SQL.
 * This is useful when a SQL construct does not have a straightforward analog
 * in another SQL dialect.  A good example of this is the decode function in
 * Oracle, which needs to be translated into a long case expression.
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MSFunctionFilter extends FunctionFilter {
    
    public MSFunctionFilter() {
        super(_HANDLERS);
    }
    
    private static final Map<String, FunctionHandler> _HANDLERS = new HashMap<String, FunctionHandler>();
    /*
     */

    static {
        _HANDLERS.put("mod", new ModHandler());
        _HANDLERS.put("chr", new ChrHandler());
        _HANDLERS.put("concat", new ConcatHandler());
        _HANDLERS.put("lpad", new LpadHandler());
        _HANDLERS.put("rpad", new RpadHandler());
        _HANDLERS.put("replace", new ReplaceHandler());
        _HANDLERS.put("substr", new SubstrHandler());
        _HANDLERS.put("substrb", new SubstrHandler());
        _HANDLERS.put("instr", new InstrHandler());
        _HANDLERS.put("instrb", new InstrHandler());
        _HANDLERS.put("add_months", new AddMonthsHandler());
        _HANDLERS.put("months_between", new MonthsBetweenHandler());
        _HANDLERS.put("round", new RoundTruncHandler());
        _HANDLERS.put("trunc", new TruncHandler());
        _HANDLERS.put("to_char", new ToCharHandler());
        _HANDLERS.put("to_date", new ToDateHandler());
        _HANDLERS.put("to_number", new ToNumberHandler());
        _HANDLERS.put("decode", new MSDecodeHandler());
        _HANDLERS.put("moca_util.date_diff_days", new DateDiffDaysHandler());
    }}

