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

package com.redprairie.moca.server.log.render;

import java.util.MissingFormatArgumentException;

/**
 * An interface for a sensitive log line in the system that could be disabled
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dpiessen
 * @version $Revision$
 */
public class SecureLogMessage {

    /** Creates a new {@link SecureLogMessage}
     * @param messageFormat The message format. The format should follow a
     * standard {@link String.format} format: i.e. %1$s.
     * @param arguments The arguments that are considered secure.
     */
    public SecureLogMessage(String messageFormat, Object... arguments) {
        this.messageFormat = messageFormat;
        this.arguments = arguments;
    }
    
    /** Creates a new {@link SecureLogMessage}
     * @param Indicates if the message should always be displayed in secure mode
     * @param messageFormat The message format. The format should follow a
     * standard {@link String.format} format: i.e. %1$s.
     * @param arguments The arguments that are considered secure.
     */
    public SecureLogMessage(boolean alwaysSecure, String messageFormat, Object... arguments) {
        this.messageFormat = messageFormat;
        this.arguments = arguments;
    }
    
    /** Gets the insecure version of the message for valid output
     * @return The message with the data provided
     */
    public String getUnsecureMessage() {
        
        if (alwaysSecure) {
            return toString();
        }
                
        try {
            return String.format(messageFormat, arguments);
        }
        catch (MissingFormatArgumentException e) {
            return messageFormat;
        }
    } 
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        Object[] securedArgs = new Object[0];
        if (arguments != null){
            securedArgs = new Object[arguments.length];
            
            for (int i=0; i < securedArgs.length; i++) {
                securedArgs[i] = hiddenString;
            }
        }
        
        try {
            return String.format(messageFormat, securedArgs);
        }
        catch (MissingFormatArgumentException e) {
            return messageFormat;
        }
        
    }

    protected boolean alwaysSecure = false;
    protected String messageFormat = "";
    protected Object[] arguments = null;
    protected String hiddenString = "****";
}
