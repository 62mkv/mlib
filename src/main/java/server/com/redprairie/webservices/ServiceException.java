/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2007
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

package com.redprairie.webservices;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRuntimeException;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ServiceException extends Exception {
    private static final long serialVersionUID = 3425672642690620416L;

    public ServiceException(Throwable cause) {
        super(cause);
        if (cause instanceof MocaException) {
            _code = ((MocaException) cause).getErrorCode();
        }
        else if (cause instanceof MocaRuntimeException) {
            _code = ((MocaRuntimeException) cause).getErrorCode();
        }
    }
    
    /**
     * @return the code
     */
    public int getCode() {
        return _code;
    }
    
    private int _code;
}
