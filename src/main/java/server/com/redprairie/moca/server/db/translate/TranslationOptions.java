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

package com.redprairie.moca.server.db.translate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.redprairie.moca.server.db.BindMode;

/**
 * SQL Translation support class that allows the caller to provide some controls over the translation itself, and
 * allows the translator to indicate to the caller that certain things must happen for the translation to be done
 * correctly.  An instance of this class will be created for each SQL translation operation, and will live through
 * the SQL execution.  This class is not thread safe, and not intended to be used by multiple threads, or for
 * multipl SQL operations.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TranslationOptions {
   
    /**
     * Default constructor that creates an instance of this class with the default settings.
     */
    public TranslationOptions() {
        this(BindMode.AUTO);
    }
    
    /**
     * Constructor that takes a binding mode parameter.
     * @param mode an enumeration of the beginning binding mode to be used for this translation.
     */
    public TranslationOptions(BindMode mode) {
        _bindMode = mode;
    }
    
    /**
     * Adds a statement to be executed before the primary SQL statement.  This is primarily used to execute statements
     * that modify the session in a way that's required for the translation to work as expected. In particular, this
     * is used for issuing lock timeout statements to databases that require them.  Each statement added will be
     * executed in order, and any results returned will be ignored.
     * 
     * @param statement The statement that will be executed before the primary statement.
     */
    public void addPreStatement(String statement) {
        if (_preStatements == null) {
            _preStatements = new ArrayList<String>();
        }
        _preStatements.add(statement);
    }
    
    /**
     * Adds a statement to be executed after the primary SQL statement.  This is primarily used to execute statements
     * that modify the session in a way that's required for the translation to work as expected. In particular, this
     * is used for issuing lock timeout statements to databases that require them.  Each statement added will be
     * executed in order, and the results will be ignored.
     * 
     * @param statement The statement that will be executed before the primary statement.
     */
    public void addPostStatement(String statement) {
        if (_postStatements == null) {
            _postStatements = new ArrayList<String>();
        }
        _postStatements.add(statement);
    }
    
    /**
     * @return Returns the postStatements.
     */
    public Collection<String> getPostStatements() {
        return _postStatements;
    }
    
    /**
     * @return Returns the preStatements.
     */
    public Collection<String> getPreStatements() {
        return _preStatements;
    }
    
    /**
     * Returns true if the translation should do autobinding.
     */
    public boolean isAutoBind() {
        return _bindMode == BindMode.AUTO || _bindMode == BindMode.VAR;
    }

    /**
     * Returns true if the translation should do unbinding
     */
    public boolean doUnbind() {
        return _bindMode == BindMode.UNBIND;
    }
    
    public void addHint(String hint) {
        String[] hintText = hint.split("=", 2);
        String name;
        String value = "";
        if (hintText.length == 2) {
            name = hintText[0];
            value = hintText[1];
        }
        else {
            name = hint;
        }
        _hints.put(name.toLowerCase().trim(), value.trim());
    }
    
    public boolean hasHint(String hint) {
        return _hints.containsKey(hint.toLowerCase());
    }
    
    public String getHintValue(String hint) {
        return _hints.get(hint.toLowerCase());
    }
    
    //
    // Implementation
    //
    private List<String> _preStatements;
    private List<String> _postStatements;
    private BindMode _bindMode;
    private Map<String, String> _hints = new LinkedHashMap<String, String>();
}
