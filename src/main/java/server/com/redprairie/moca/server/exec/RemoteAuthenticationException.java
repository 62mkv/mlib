/**
 * Exception thrown when 
 */
package com.redprairie.moca.server.exec;

import com.redprairie.moca.MocaException;

public class RemoteAuthenticationException extends MocaException {
    private static final long serialVersionUID = -4443856103110530558L;
    public static final int CODE = 541;

    public RemoteAuthenticationException(String message) {
        super(CODE, message);
    }
    
    @Override
    public boolean isMessageResolved() {
        return true;
    }
}
