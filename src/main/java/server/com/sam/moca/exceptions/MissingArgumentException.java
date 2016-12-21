/**
 * 
 */
package com.sam.moca.exceptions;

import com.sam.moca.MocaException;

/**
 * @author dpiessen
 * 
 */
public class MissingArgumentException extends MocaException {

    private static final int CODE = 802;
    public static final String MSG = "Missing argument: ^argdsc^ (^argid^)";

    /**
     * Creates a new {@link MissingArgumentException} (802 code).
     * 
     * @param argumentName The name of the argument that is missing.
     */
    public MissingArgumentException(String argumentName) {
        super(CODE, MSG);
        addArg("argid", argumentName);
        addLookupArg("argdsc", argumentName);
    }

    /**
     * Creates a new {@link MissingArgumentException} (802 code).
     * 
     * @param argumentName The name of the argument that is missing.
     * @param t The inner exception that caused the issue.
     */
    public MissingArgumentException(String argumentName, Throwable t) {
        super(CODE, MSG, t);
        addArg("argid", argumentName);
        addLookupArg("argdsc", argumentName);
    }
    private static final long serialVersionUID = 5045795650871039452L;
}
