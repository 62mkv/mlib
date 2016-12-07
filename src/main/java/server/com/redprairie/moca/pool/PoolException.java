/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.pool;

import com.redprairie.moca.MocaException;

/**
 * General pool exception that can be thrown from the various pool methods.
 * Implementations of validators should extend this class normally.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class PoolException extends MocaException {
    private static final long serialVersionUID = -5711037422079833495L;

    /**
     * @param errorCode
     * @param message
     */
    public PoolException(int errorCode, String message) {
        super(errorCode, message);
    }
    /**
     * @param errorCode
     * @param message
     * @param t
     */
    public PoolException(int errorCode, String message, Throwable t) {
        super(errorCode, message, t);
    }
}
