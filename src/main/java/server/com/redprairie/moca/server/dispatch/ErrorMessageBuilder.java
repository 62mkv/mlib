/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.redprairie.moca.server.dispatch;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaException.Args;
import com.redprairie.util.StringReplacer;
import com.redprairie.util.StringReplacer.ReplacementStrategy;

/**
 * Class to build error messages associated with {@link MocaException} objects.  A
 * MocaException contains an error code, a default error message, and error message
 * arguments, which can be used to replace placeholders in either the looked-up message
 * or the default message.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ErrorMessageBuilder {

    /**
     * Constructor that takes a MocaException object and a message resolver and
     * holds on to the required fields from each. Messages are not resolved at
     * the time of object construction, but instead are done with the getMessage
     * method is called.
     * 
     * @param e the MocaException object encapsulating the error condition. The
     *            error code, default message and argument list are retrieved
     *            from the exception object.
     * @param lookup the message resolver used to look up error messages at
     *            runtime.
     */
    public ErrorMessageBuilder(MocaException e, MessageResolver lookup) {
        _errorCode = e.getErrorCode();
        _defaultMessage = e.getMessage();
        
        for (MocaException.Args arg : e.getArgList()) {
            _args.put(arg.getName().toLowerCase(), arg);
        }
        _lookup = lookup;
    }
    
    public ErrorMessageBuilder(int errorCode, String defaultMessage, 
            Args[] args, MessageResolver lookup) {
        _errorCode = errorCode;
        _defaultMessage = defaultMessage;
        
        for (MocaException.Args arg : args) {
            _args.put(arg.getName().toLowerCase(), arg);
        }
        _lookup = lookup;
    }

    /**
     * Resolves the main error message using the lookup mechanism. If any lookup
     * returns <code>null</code>, an appropriate default value will be used. In
     * the case of error code lookup returning <code>null</code>, the default
     * error message will be used instead of the looked-up message. If a
     * lookup-type message argument returns <code>null</code> upon lookup, the
     * raw value of the lookup field is inserted instead.
     * 
     * @return the translated and arg-replaced error message associated with the
     *         exception.
     */
    public String getMessage() {

        /**
         * Look up 
         */
        String message = _lookup.getMessage("err" + _errorCode);
        
        if (message == null) message = _defaultMessage;
        
        // Use a replacement strategy to look up replacement values using
        // our Args list.
        ReplacementStrategy strat = new ReplacementStrategy() {
            public String lookup(String key) {
                // ^!^: The default message itself
                if (key.equals("!") || key.equals("*")) {
                    return _defaultMessage;
                }
                
                // ^?^: The error code, as a string 
                if (key.equals("?")) {
                    return String.valueOf(_errorCode);
                }

                // Otherwise, deal with the arguments
                Args arg = _args.get(key.toLowerCase());
                if (arg == null) {
                    return null;
                }
                
                Object value = arg.getValue();
                if (value == null) {
                    return null;
                }
                
                if (arg.isLookup()) {
                    return _lookup.getMessage(String.valueOf(value));
                }
                else {
                	if (value instanceof Double) {
                		// DecimalFormat is not thread safe so we can either 
                		// clone or just create a new one.  Since this code
                		// should not be hit that much we just create a new one
                		// to reduce code complexity
                		DecimalFormat doubleFormatter = new DecimalFormat("0.#");
                    	doubleFormatter.setMaximumFractionDigits(15);
                    	
                    	return doubleFormatter.format(value);
                	}
                    return String.valueOf(value);
                }
            }
        };
        
        return new StringReplacer('^', strat).translate(message);
    }

    //
    // Implementation
    //
    private final String _defaultMessage;
    private final int _errorCode;
    private final Map<String, MocaException.Args> _args = new HashMap<String, MocaException.Args>();
    private final MessageResolver _lookup;
}
