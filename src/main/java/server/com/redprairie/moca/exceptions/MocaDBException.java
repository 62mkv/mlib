/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.exceptions;

import java.sql.SQLException;

import com.redprairie.moca.MocaException;

/**
 * Represents an unexpected database exception condition.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
public class MocaDBException extends MocaException {
    
    public static final int UNKNOWN_ERROR_CODE = -2;
    
    /**
     * Creates a MOCA database Exception with the given message.
     * @param message the default MOCA error message to be used to fill in 
     */
    public MocaDBException(String message) {
        super(UNKNOWN_ERROR_CODE, message);
    }

    /**
     * Creates a MOCA database Exception with the given code and message.
     * @param code the MOCA error code to be return to the caller
     * @param message the default MOCA error message to be used to fill in 
     */
    public MocaDBException(int code, String message) {
        super(code, message);
    }

    /**
     * Creates a MOCA database exception object, taking the error code and
     * message from the given SQL Exception.
     * @param cause the SQL exception that caused the error condition.
     */
    public MocaDBException(SQLException cause) {
        super(massageErrorCode(cause.getErrorCode()), cause.getMessage(), cause);
    }
    
    /**
     * Creates a MOCA database exception object, using the provided error code
     * and message but having a cause of the SQLException
     * @param cause the SQL exception that caused the error condition.
     */
    public MocaDBException(int code, String message, SQLException cause) {
        super(code, message, cause);
    }
    
    // Return a special code if no error code is returned.
    private static int massageErrorCode(int code) {
        if (code == 0) {
            return UNKNOWN_ERROR_CODE;
        }
        else if (code > 0) {
            return -code;
        }
        else {
            return code;
        }
    }

    private static final long serialVersionUID = 3258132448989821238L;
}
