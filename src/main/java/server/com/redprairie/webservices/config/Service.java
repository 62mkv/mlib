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

package com.redprairie.webservices.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * populate and hold the details of service,pakage, class name and collection of
 * operation for Service config
 * 
 * <b>
 * 
 * <pre>
 *  Copyright (c) 2005 RedPrairie Corporation
 *  All rights reserved.
 * </pre>
 * 
 * </b>
 * 
 * @author Mohanesha.C
 * @version $Revision$
 */
public class Service {

    /**
     * @return Returns the _className.
     */
    public String getClassName() {
        return _className;
    }

    /**
     * @param className The className to set.
     */
    public void setClassName(String className) {
        this._className = className;
    }

    /**
     * @return Returns the _name.
     */
    public String getName() {
        return _name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * @return Returns the Operation[] .
     */
    public Operation[] getOperations() {
        return _operations.values().toArray(
                new Operation[_operations.values().size()]);
    }

    /**
     * @param operation The operation to add to collection.
     */
    public void addOperation(Operation operation) {
        _operations.put(operation.getName(), operation);
    }
    
    public void addAllOperations(Operation[] operations) {
        for (Operation o : operations) {
            addOperation(o);
        }
    }

    /**
     * @return Returns the _packageName.
     */
    public String getPackageName() {
        return _packageName;
    }

    /**
     * @param packageName The packageName to set.
     */
    public void setPackageName(String packageName) {
        this._packageName = packageName;
    }
    
    /**
     * @param generate the generate to set
     */
    public void setGenerate(boolean generate) {
        _generate = generate;
    }
    
    /**
     * @return the generate
     */
    public boolean isGenerate() {
        return _generate;
    }

    // -----------------------------
    // implementation:
    // -----------------------------

    private String _name = null;
    private String _className = null;
    private String _packageName = null;
    private Map<String, Operation> _operations = new LinkedHashMap<String, Operation>();
    private boolean _generate = true;
}
