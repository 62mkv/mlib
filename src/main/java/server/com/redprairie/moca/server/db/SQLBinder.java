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

package com.redprairie.moca.server.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.redprairie.moca.MocaType;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.ArgCheck;

/**
 * Class that interprets a SQL statement with named bind variable indicators
 * interspersed throughout, in Oracle syntax, and produces two things: a new
 * SQL statement, with variable indicators replaced with JDBC-style positional
 * variable indicators ('?'), and a list of names, indicating the names of the
 * positional parameters.
 * 
 * For example, if this class is instantiated via
 * <pre><code>
 * SQLBinder b = new SQLBinder("select * from table1 where cola = :a_value");
 * </code></pre>
 * then <code>b.getBoundStatement()</code> would return
 * <code>"select * from table1 where cola = ?"</code> and
 * <code>b.getNames()</code> would return a string array of size 1, containing
 * the single value <code>"a_value"</code>.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
public class SQLBinder {

    /**
     * Creates an instance of this class with the given SQL statement.
     * @param sqlStatement the SQL statement to parse.  This argument cannot
     * be <code>null</code>.  This argument cannot already contain any
     * positional parameters.
     * @throws SQLBinderException if the SQL statement already contains
     * positional parameters, or if there is a zero-length bind variable.
     * @throws IllegalArgumentException if the SQL Statement is <code>null</code>.
     */
    public SQLBinder(String sqlStatement, BindList bindList) {
        ArgCheck.notNull(sqlStatement);
        
        List<String> foundNames = new ArrayList<String>();
        List<Integer> foundPosition = new ArrayList<Integer>();
        boolean inQuote = false;
        boolean inComment = false;
        boolean lineComment = false;
        StringBuilder tmp = new StringBuilder();
        int length = sqlStatement.length();
        for (int i = 0; i < length; ) {
            char c = sqlStatement.charAt(i);
            if (c == '\'' && !inComment && !lineComment) {
                inQuote = !inQuote;
            }
            
            if (!inComment && !lineComment && !inQuote && c == '/' && ((i + 1) < length) && sqlStatement.charAt(i+1) == '*') {
                inComment = true;
                tmp.append("/*");
                i+=2;
            }
            else if (!inComment && !lineComment && !inQuote && c == '-' && ((i + 1) < length) && sqlStatement.charAt(i+1) == '-') {
                lineComment = true;
                tmp.append("--");
                i+=2;
            }
            else if (inComment && c == '*' && ((i + 1) < length) && sqlStatement.charAt(i+1) == '/') {
                inComment = false;
                tmp.append("*/");
                i+=2;
            }
            else if (lineComment && (c == '\n' || c == '\r')) {
                lineComment = false;
                tmp.append(c);
                i++;
            }
            else if (!inQuote && !inComment && !lineComment && c == ':') {
                int p = i + 1;
                
                // Special case: two colons is a valid SQL Server syntax
                if (p < length && sqlStatement.charAt(p) == ':') {
                    tmp.append("::");
                    p++;
                }
                else {
                    StringBuilder id = new StringBuilder();
                    for (; p < length; p++) {
                        c = sqlStatement.charAt(p);
                        if ((c >= 'a' && c <= 'z') ||
                            (c >= 'A' && c <= 'Z') ||
                            (c >= '0' && c <= '9') ||
                            c == '_') {
                            id.append(c);
                        }
                        else {
                            break;
                        }
                    }
                    
                    if (bindList != null && !bindList.contains(id.toString())) {
                        tmp.append(":");
                        tmp.append(id);
                    }
                    else {
                        foundNames.add(id.toString());
                        foundPosition.add(tmp.length());
                        tmp.append('?');
                    }
                }
                i = p;
            }
            else {
                tmp.append(c);
                i++;
            }
        }
        _boundStatement = tmp.toString();
        _originalStatement = sqlStatement;
        _names = foundNames;
        _positions = foundPosition.toArray(new Integer[foundPosition.size()]);
        _bindList = bindList;
    }
    
    /**
     * Returns a list of names corresponding to the positional parameters
     * found.
     * @return a list, consisting of the names of all positional parameters
     * found while parsing the statement.
     */
    public List<String> getNames() {
        return Collections.unmodifiableList(_names);
    }
    
    /**
     * Returns a new SQL statement with all named variable parameters replaced
     * with JDBC positional parameter indicators ("?").
     * @return the new SQL statement. 
     */
    public String getBoundStatement() {
        return _boundStatement;
    }
    
    /**
     * Returns the original SQL statement with all named variable parameters intact.
     * 
     * @return Returns the originalStatement.
     */
    public String getOriginalStatement() {
        return _originalStatement;
    }
    
    /**
     * Returns the string version of what this statement would look like with
     * bound variables replaced with literals.  This method is useful for
     * logging and testing purposes.
     * 
     * @return the passed-in string
     */
    public String getUnboundStatement() {
        StringBuilder tmp = new StringBuilder();
        int start = 0;
        
        for (int i = 0; i < _positions.length; i++) {
            int p = _positions[i];
            tmp.append(_boundStatement.substring(start, p));
            MocaType type = _bindList.getType(_names.get(i));
            Object value = _bindList.getValue(_names.get(i));
            
            if (type == MocaType.STRING_REF || type == MocaType.DOUBLE_REF || type == MocaType.INTEGER_REF) {
                tmp.append('?');
                
                // Since this is a reference field, there may be in/out values.  Print those as well. 
                tmp.append(" /* ");
                if (type == MocaType.STRING_REF && value != null) {
                    tmp.append('\'');
                    tmp.append(String.valueOf(value).replaceAll("'", "''"));
                    tmp.append('\'');
                }
                else {
                    tmp.append(value);
                }
                tmp.append(" */");
            }
            else if (value == null) {
                tmp.append("NULL");
            }
            else if (type == MocaType.STRING || type == MocaType.DATETIME) {
                if (type == MocaType.DATETIME && value instanceof Date) {
                    value = MocaUtils.formatDate((Date)value);
                }
                tmp.append('\'');
                tmp.append(String.valueOf(value).replaceAll("'", "''"));
                tmp.append('\'');
            }
            else {
                tmp.append(value);
            }
            
            start = p + 1;
        }
        
        tmp.append(_boundStatement.substring(start));
        
        return tmp.toString();
    }
    
    //
    // Implementation
    //
    private final List<String> _names;
    private final Integer[] _positions;
    private final String _boundStatement;
    private final String _originalStatement;
    private final BindList _bindList;
}
