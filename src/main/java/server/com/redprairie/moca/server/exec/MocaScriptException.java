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

package com.redprairie.moca.server.exec;

import com.redprairie.moca.MocaException;

/**
 * Thrown from a Groovy script if a NON-MOCA error occurs, such as a runtime exception, or some error in compiling
 * or executing the script itself.
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MocaScriptException extends MocaException {
    private static final long serialVersionUID = -115685067415511227L;
    public static final int CODE = 531;
    
    MocaScriptException(Throwable t) {
        super(CODE, "Groovy Script Exception: ^DETAIL^", t);
        addArg("detail", t.toString());
    }
}
