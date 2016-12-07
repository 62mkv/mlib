/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
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

package com.redprairie.moca.server.db.translate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.server.db.translate.filter.AutoBindFilter;
import com.redprairie.moca.server.db.translate.filter.CommentFilter;
import com.redprairie.moca.server.db.translate.filter.CommentHintFilter;
import com.redprairie.moca.server.db.translate.filter.FunctionFilter;
import com.redprairie.moca.server.db.translate.filter.NoBindFilter;
import com.redprairie.moca.server.db.translate.filter.SimpleFunctionFilter;
import com.redprairie.moca.server.db.translate.filter.SimpleWordFilter;
import com.redprairie.moca.server.db.translate.filter.TranslationFilter;
import com.redprairie.moca.server.db.translate.filter.functions.DecodeHandler;
import com.redprairie.moca.server.db.translate.filter.functions.FunctionHandler;
import com.redprairie.moca.server.db.translate.filter.h2.functions.ToDateHandler;
import com.redprairie.moca.server.db.translate.filter.h2.functions.ToNumberHandler;
import com.redprairie.moca.server.db.translate.filter.mssql.MSEmptyStringFilter;

/**
 * Oracle-specific translator. This class performs only minimal SQL translation,
 * automatically turning SQL constants into bind variables. It also sets up the
 * appropriate JDBC pool listener to ensure that JDBC connections are
 * appropriately handled.
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class H2Dialect extends AbstractFilterTranslator {

    /**
     * H2 Translator
     */
    public H2Dialect() {
        super(H2_FILTERS, H2_NOCONV_FILTERS, com.redprairie.moca.server.db.DBType.H2);
    }

    @Override
    public String getSequenceValue(String sequence, Connection conn)
            throws SQLException {
        // In Oracle, the sequence name must correspond to an actual sequence
        // object.
        // So, we prepare a callable statement to get the sequence value
        Statement stmt = null;
        ResultSet res = null;
        try {
            stmt = conn.createStatement();
            res = stmt.executeQuery("select " + sequence + ".nextval from dual");

            res.next();
            return res.getString(1);
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException e) {
                    _logger.debug("There was an issue closing statement", e);
                }
            }
        }
    }

    // @see com.redprairie.moca.db.translate.SQLTranslator#translateSQLException(java.sql.SQLException)
    public SQLException translateSQLException(SQLException e) {
        // TODO translate common H2database error to Oracle 
        return e;
    }
    
    // @see com.redprairie.moca.server.db.translate.SQLDialect#getLimitHandler()
    @Override
    public LimitHandler getLimitHandler() {
        // TODO Add an actual LimitHandler for H2 (Hibernate already has this we can extend upon)
        return NoopLimitHandler.getInstance();
    }

    private static final Map<String, String> H2_NAMES = new HashMap<String, String>();
    static {
        H2_NAMES.put("nvl", "ifnull");  // nvl(dt,...) does not work if column (dt) value is null
        H2_NAMES.put("moca_util.date_diff_days", "date_diff_days");
        H2_NAMES.put("moca_util.isnumeric", "isnumeric");
        H2_NAMES.put("instrb", "instr");
        H2_NAMES.put("substrb", "substr");
        H2_NAMES.put("ceil", "ceiling");
        H2_NAMES.put("lengthb", "length");
        H2_NAMES.put("ln", "log");
    }
    
    private static final Map<String, FunctionHandler> H2_FUNCTIONS = new HashMap<String, FunctionHandler>();
    static {
        H2_FUNCTIONS.put("decode", new DecodeHandler());
        H2_FUNCTIONS.put("to_number", new ToNumberHandler());
        H2_FUNCTIONS.put("to_date", new ToDateHandler());
    }
    
    private static final Map<String, String> H2_WORDS = new HashMap<String, String>();
    static {
        H2_WORDS.put("sysdate", "now(0)");
    }
    
    private static final TranslationFilter[] H2_FILTERS = new TranslationFilter[] {
        new CommentHintFilter(),
        new AutoBindFilter(),
        new NoBindFilter(false),
        new CommentFilter(),
        new MSEmptyStringFilter(),  // TODO may write own version
        new SimpleFunctionFilter(H2_NAMES),
        new SimpleWordFilter(H2_WORDS),
        new FunctionFilter(H2_FUNCTIONS),
    };
    
    private static final TranslationFilter[] H2_NOCONV_FILTERS = new TranslationFilter[] {
        new AutoBindFilter(),
        new NoBindFilter(false)
    };
    

    private static final Logger _logger = LogManager.getLogger(H2Dialect.class);

}