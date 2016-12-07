/**
 * Exception thrown when 
 */
package com.redprairie.moca.exceptions;

import com.redprairie.moca.MocaException;

public class AuthenticationException extends MocaException {
    private static final long serialVersionUID = -4443856103110530558L;
    public static final int CODE = 523;

    public AuthenticationException(String command) {
        this(command, "invalid_session");
    }
    
    public AuthenticationException(String command, String reason) {
        super(CODE, "Command ^command^: not authenticated (^reason^)");
        addArg("command", command);
        addLookupArg("reason", reason);
    }
}
