/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.repository.docs;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds documentation information for a command.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class CommandDocumentation {
    public static class Column {
        
        public String getName() {
            return _name;
        }
        
        public String getType() {
            return _type;
        }
        
        public String getDescription() {
            return _description;
        }

        private Column(String name, String type, String description) {
            _name = name;
            _type = type;
            _description = description;
        }
        
        private final String _name;
        private final String _type;
        private final String _description;
    }
    
    public static class Err {
        public String getValue() {
            return _value;
        }
        
        public String getDescription() {
            return _description;
        }

        private Err(String value, String description) {
            _value = value;
            _description = description;
        }
        
        private final String _value;
        private final String _description;
    }
    
    public static class Reference {
        
        public String getCommand() {
            return _command;
        }

        private Reference(String command) {
            _command = command;
        }
        
        private final String _command;
    }
    
    
    public CommandDocumentation() {
        
    }
    
    /**
     * @return Returns the remarks.
     */
    public String getRemarks() {
        return _remarks;
    }
    
    /**
     * @param remarks The remarks to set.
     */
    public void setRemarks(String remarks) {
        _remarks = remarks;
    }
    
    /**
     * @return Returns the example.
     */
    public List<String> getExamples() {
        return _examples;
    }
    
    /**
     * @param example The example to set.
     */
    public void addExample(String example) {
        _examples.add(example);
    }
    
    /**
     * @return Returns the returnRows.
     */
    public String getReturnRows() {
        return _returnRows;
    }
    
    /**
     * @param returnRows The returnRows to set.
     */
    public void setReturnRows(String returnRows) {
        _returnRows = returnRows;
    }
    
    /**
     * @return Returns the columns.
     */
    public List<Column> getColumns() {
        return _columns;
    }
    
    public void addColumn(String name, String type, String description) {
        _columns.add(new Column(name, type, description));
    }
    
    
    /**
     * @return Returns the calledBy.
     */
    public List<Reference> getCalledBy() {
        return _calledBy;
    }
    
    public void addCalledby(String command) {
        _calledBy.add(new Reference(command));
    }

    /**
     * @return Returns the seeAlso.
     */
    public List<Reference> getSeeAlso() {
        return _seeAlso;
    }
    
    public void addSeeAlso(String command) {
        _seeAlso.add(new Reference(command));
    }
    
    /**
     * @return Returns the errors.
     */
    public List<Err> getErrors() {
        return _errors;
    }
    
    public void addError(String value, String description) {
        _errors.add(new Err(value, description));
    }
    
    private String _remarks;
    private String _returnRows;
    private final List<String> _examples = new ArrayList<String>();
    private final List<Column> _columns = new ArrayList<Column>();
    private final List<Err> _errors = new ArrayList<Err>();
    private final List<Reference> _seeAlso = new ArrayList<Reference>();
    private final List<Reference> _calledBy = new ArrayList<Reference>();
}
