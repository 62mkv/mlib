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

package com.sam.moca.components.base;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.sam.moca.EditableResults;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.NotFoundException;

/**
 * This class handles all the session variable.
 * 
 * This class is not thread safe and assumes that a moca context is shared
 * among only 1 thread.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class SessionVariableService {
    
    /**
     * This method saves a session variable.
     * @param moca The moca context to save the session variable into
     * @param name The name of the variable
     * @param value The value of the variable
     */
    public void saveSessionVariable(MocaContext moca, String name, String value) {
        Map<String, String> sessionVariables = getSessionVariables(moca);
        
        sessionVariables.put(name, value);
    }
    
    /**
     * This will retrieve the session variable from the context
     * @param moca The moca context to get the session variable from
     * @param name The name of the variable
     * @return a result set containing the value
     * @throws NotFoundException This is thrown if a session variable is not
     *         found.
     */
    public MocaResults getSessionVariable(MocaContext moca, String name) 
            throws NotFoundException {
        Map<String, String> sessionVariables = getSessionVariables(moca);
        
        Object value = sessionVariables.get(name);
        
        EditableResults retRes = moca.newResults();
        
        retRes.addColumn("value", MocaType.STRING);
        
        if (value == null) {
            throw new NotFoundException(retRes);
        }
        
        retRes.addRow();
        
        retRes.setValue("value", value);
        
        return retRes;
    }
    
    /**
     * This will list all existing session variables
     * @param moca The moca context to get the session variables from
     * @return a result set containing all the session variables
     */
    public MocaResults listSessionVariables(MocaContext moca) {
        Map<String, String> sessionVariables = getSessionVariables(moca);
        
        EditableResults retRes = moca.newResults();
        
        retRes.addColumn("name", MocaType.STRING);
        retRes.addColumn("value", MocaType.STRING);
        
        for (Entry<String, String> entry : sessionVariables.entrySet()) {
            retRes.addRow();
            retRes.setStringValue("name", entry.getKey());
            retRes.setStringValue("value", entry.getValue());
        }
        
        return retRes;
    }
    
    /**
     * This method is just to get the SessionVariable map.  It tries to retrieve
     * it from the moca transaction attributes and if not found initialize it
     * @param moca The moca context to get the variables from
     * @return a map that has the session variables in it
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> getSessionVariables(MocaContext moca) {
        Map<String, String> sessionVariables = 
            (Map<String, String>)moca.getTransactionAttribute(SESSION_VARIABLE_ATTRIBUTE);
        
        if (sessionVariables == null) {
            sessionVariables = new LinkedHashMap<String, String>();
            
            moca.setTransactionAttribute(SESSION_VARIABLE_ATTRIBUTE, 
                    sessionVariables);
        }
        
        return sessionVariables;
    }
    
    private final static String SESSION_VARIABLE_ATTRIBUTE = 
        SessionVariableService.class.getCanonicalName();
}
