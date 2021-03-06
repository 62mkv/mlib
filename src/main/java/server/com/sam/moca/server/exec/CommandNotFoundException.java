/**
 * 
 */
package com.sam.moca.server.exec;

import com.sam.moca.MocaException;

/**
 * @author Dan
 * 
 */
public class CommandNotFoundException extends MocaException {
    private static final long serialVersionUID = -4443856103110530558L;

    public CommandNotFoundException(String command) {
        super(501, "Command ^COMMAND^ not found");
        addArg("COMMAND", command);
    }
}
