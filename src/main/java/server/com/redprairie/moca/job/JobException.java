/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.job;

import com.redprairie.moca.MocaException;

/**
 * An exception associated with job scheduling or configuration errors.  This
 * exception is typically not thrown in the case of simple job failure, but
 * rather for configuration issues such as a malformed schedule (cron) string,
 * or an invalid set of parameters for timer configuration.
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class JobException extends MocaException {
    public static final int CODE = 535;

    public JobException(String msg) {
            super(CODE, msg);
    }

    public JobException(String msg, Throwable e) {
        super(CODE, msg, e);
    }

    private static final long serialVersionUID = 1L;
}
