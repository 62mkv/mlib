/**
 * 
 */
package com.redprairie.moca.web.console;

import com.redprairie.moca.MocaException;

/**
 * @author mlange
 */
public class ConsoleException extends MocaException {

    private static final int CODE = 16;

    /**
     * Creates a default ConsoleException.
     */
    public ConsoleException(String msg) {
        super(CODE, msg);
    }
    
    private static final long serialVersionUID = -2459677833262053680L;
}

