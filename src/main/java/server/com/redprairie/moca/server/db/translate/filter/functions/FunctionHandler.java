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

package com.redprairie.moca.server.db.translate.filter.functions;

import java.util.List;

import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.db.translate.SQLElement;
import com.redprairie.moca.server.db.translate.TranslationException;

/**
 * Interface for translating SQL functions into native database dialects. This
 * interface will be invoked for each instance of a function that is found.
 * 
 * It is important to note that the handler represented by an instance of this
 * interface need not deal with recursion, unless it is necessary for data
 * handling. Recursive calls to identical functions will result in multiple
 * invocations of this handler.
 * 
 * For example, the SQL fragment
 * <blockquote>
 * <pre>
 * decode(x, 'a', decode(y, 'a', 'b', y), x)
 * </pre>
 * </blockquote>
 * 
 * will, if <code>decode</code> is a function handled by this handler, result
 * in the handler's <code>translate</code> method being called twice, once
 * with <code>[ y 'a' 'b' x ]</code> as parameters, and once with
 * <code>[ x 'a' <result-of-first-call> x ]</code> as paramters. This handler
 * is called recursively, so it does not need to act recursively.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public interface FunctionHandler {
    /**
     * Translate a function call, given a function name and arguments.
     * @param name the name of this function, as it appears in SQL statements.
     * @param args the arguments to this function.  Each element in the List
     * is an argument to the function, and it consists of (potentially) any
     * number of SQL elements.
     * @param bindArgs the list of bind variables to this statement.
     * @return a collection of SQL elements that make up the translated
     * SQL statement.
     */
    public List<SQLElement> translate(String name, List<List<SQLElement>> args, BindList bindArgs)
            throws TranslationException;
}
