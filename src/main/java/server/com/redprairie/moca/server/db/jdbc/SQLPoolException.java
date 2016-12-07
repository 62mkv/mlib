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

package com.redprairie.moca.server.db.jdbc;

import java.sql.SQLException;

import com.redprairie.moca.pool.PoolException;

/**
 * PoolException that should be used when a SQLException is encountered.
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SQLPoolException extends PoolException {
    private static final long serialVersionUID = 5198268399545270160L;
    public static final int UNKNOWN_ERROR_CODE = -2;
    
    public SQLPoolException(String message) {
        super(UNKNOWN_ERROR_CODE, message);
    }
    
    public SQLPoolException(SQLException e) {
        super(massageErrorCode(e.getErrorCode()), e.getMessage(), e);
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
}
