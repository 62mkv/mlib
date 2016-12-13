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

package com.redprairie.webservices.config;

import java.util.ArrayList;
import java.util.List;

/**
 * populate and hold the details like command, input arguments, output result
 * Fields,result class name and specify that multi row record or single record
 * for a Service
 * 
 * <b><pre>
 *  Copyright (c) 2016 Sam Corporation
 *  All rights reserved.
 * </pre></b>
 * 
 * @author Mohanesha.C
 * @version $Revision$
 */
public class Operation {

    /**
     * @return Returns the command.
     */
    public String getCommand() {
        return _command;
    }

    /**
     * @param command The command to set.
     */
    public void setCommand(String command) {
        this._command = command;
    }

    /**
     * @return Returns the multiRowResult.
     */
    public boolean isMultiRowResult() {
        return _multiRowResult;
    }

    /**
     * @param multiRowResult The multiRowResult to set.
     */
    public void setMultiRowResult(boolean multiRowResult) {
        this._multiRowResult = multiRowResult;
    }

    /**
     * @return Returns the OpeartionArgument[]
     */
    public OperationArgument[] getOperationArguments() {
        return _operationArguments.toArray(new OperationArgument[_operationArguments.size()]);
    }

    /**
     * @param operationArgument to add to this operationArguments .
     */
    public void addOperationArgument(OperationArgument operationArgument) {
        _operationArguments.add(operationArgument);
    }

 
    /**
     * @return Returns the resultClassName.
     */
    public String getResultClassName() {
        return _resultClassName;
    }

    /**
     * @param resultClassName The resultClassName to set.
     */
    public void setResultClassName(String resultClassName) {
        this._resultClassName = resultClassName;
    }

    /**
     * @return Returns the ResultField[].
     */
    public ResultField[] getResultFields() {
        return _resultFields.toArray(new ResultField[_resultFields.size()]);
    }

    /**
     * @param resultField to add to this resultField .
     */
    public void addResultField(ResultField resultField) {
        _resultFields.add(resultField);
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

    // -----------------------------
    // implementation:
    // -----------------------------

    private String _name = null;
    private String _command = null;
    private boolean _multiRowResult = false;
    private List<OperationArgument> _operationArguments = new ArrayList<OperationArgument>();
    private String _resultClassName = null;
    private List<ResultField> _resultFields = new ArrayList<ResultField>();

}
