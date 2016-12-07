/**
 * Exception thrown when 
 */
package com.redprairie.moca.exceptions;

import com.redprairie.moca.MocaException;

public class AuthorizationException extends MocaException {
    private static final long serialVersionUID = -4237725399495875521L;
    public static final int CODE = 544;

    public AuthorizationException(String command) {
        super(CODE, "Command ^command^: not authorized");
        addArg("command", command);
    }
}
