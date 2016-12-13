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

package com.redprairie.moca;

import java.util.LinkedHashMap;
import java.util.Map;

import com.redprairie.moca.MocaException.Args;
import com.redprairie.util.StringReplacer;
import com.redprairie.util.StringReplacer.ReplacementStrategy;

/**
 * The base exception class for all MOCA exceptions.  Applications should
 * extend this exception to allow for application-specific exceptions.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public class MocaRuntimeException extends RuntimeException {

    /**
     * @param errorCode the error code associated with this exception.
     * @param message
     */
    public MocaRuntimeException(int errorCode, String message) {
        super(message);
        _errorCode = errorCode;
    }
    
    /**
     * Wraps another exception in a MOCA exception.
     * @param errorCode
     * @param message
     * @param t
     */
    public MocaRuntimeException(int errorCode, String message, Throwable t) {
        super(message, t);
        _errorCode = errorCode;
    }
    
    /**
     * Turns a MOCA runtime (unchecked) exception into a checked exception.
     * @param cause the original cause of the exception.
     */
    public MocaRuntimeException(MocaException cause) {
        this(cause.getErrorCode(), cause.getMessage(), cause);
        setResults(cause.getResults());
        Args[] args = cause.getArgList();
        if (args != null && args.length != 0) {
            _args = new LinkedHashMap<String, Args>();
            for (int i = 0; i < args.length; i++) {
                _args.put(args[i].getName(), args[i]);
            }
        }
        _isMessageResolved = cause.isMessageResolved();
    }
    
    /**
     * Returns the error code associated with this error.  This error code
     * will be returned to callers in other languages (C, local syntax, COM,
     * clients) as an error code.
     * 
     * @return the error code for this error
     */
    public int getErrorCode() {
        return _errorCode;
    }
    
    /**
     * Gets the argument list associated with this exception condition.
     * @return the argument list for error message parameterization.
     */
    public MocaException.Args[] getArgList() {
        if (_args == null) return new Args[0];

        return (Args[]) _args.values().toArray(new Args[_args.size()]);
    }
    
    /**
     * Returns the value of a named exception argument. It is the caller's
     * responsibility to know the type of the given argument by convention,
     * or to determine the type through reflection on the returned object.
     * @param name the name of an argument to look up.
     * @return the argument's value.  If the named argument refers to a "lookup"
     * argument, only the ID to be looked up will be returned (i.e. no catalog
     * lookup will occur).  If the named argument has not been set in this
     * exception, then <code>null</code> is returned.
     */
    public Object getArgValue(String name) {
        if (_args == null) return null;
        Args arg = (Args) _args.get(name);
        if (arg == null) return null;
        return arg.getValue();
    }
    
    /**
     * Overrides the default Exception.toString method to do parameter
     * substitution, based on the arguments given to this exception. 
     */
    public String toString() {
        
        // Use a replacement strategy to look up replacement values using
        // our Args list.
        ReplacementStrategy strat = new ReplacementStrategy() {
            public String lookup(String key) {
                if (_args == null) return null;
                Args arg = (Args) _args.get(key.toLowerCase());
                if (arg == null) return null;
                Object value = arg.getValue();
                if (value == null) return null;
                return String.valueOf(value);
            }
        };
        
        return new StringReplacer('^', strat).translate(super.toString());
    }
    
    /**
     * Get the results from this exception.  Some commands will
     * throw exceptions containing results information. Typically,
     * this usage is restricted to certain well-known subclasses,
     * such as NotFoundException.
     * @return the MocaResults object associated with this exception
     * condition.
     */
    public MocaResults getResults() {
        return _results;
    }
    
    /**
     * Alter the results contained within this exception.  MOCA uses
     * this method to change the results passed to the MOCA runtime
     * in the case of an error. Application code should never call
     * this method.
     */
    public void setResults(MocaResults results) {
        _results = results;
    }
    

    public boolean isMessageResolved() {
        return _isMessageResolved;
    }
    
    //
    // Subclass interface
    //
    
    /**
     * Add an argument to the error message.  This allows for parameterized,
     * localized error messages.
     * @param name the name of the argument to add.
     * @param value the value of the argument to add.
     */
    protected void addArg(String name, Object value) {
        if (_args == null) {
            _args = new LinkedHashMap<String, Args>();
        }
        _args.put(name.toLowerCase(), new MocaException.Args(name, value, false));
    }
    
    /**
     * Add an argument to the error message.  The value is used as the key
     * in an application-specfic lookup.
     * @param name the name of the argument to add.
     * @param value the value of the argument.  This is actually the name of
     * an application-specific lookup key.
     */
    protected void addLookupArg(String name, String value) {
        if (_args == null) {
            _args = new LinkedHashMap<String, Args>();
        }
        _args.put(name.toLowerCase(), new MocaException.Args(name, value, true));
    }

    //
    // Implementation
    //
    private int _errorCode;
    private Map<String, Args> _args;
    private MocaResults _results;
    private boolean _isMessageResolved = false;

    private static final long serialVersionUID = 3257006553294255158L;
}
