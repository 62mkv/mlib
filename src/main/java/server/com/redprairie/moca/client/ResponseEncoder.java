/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.redprairie.moca.client;

import java.io.IOException;

import javax.servlet.ServletException;

import com.redprairie.moca.MocaResults;

public interface ResponseEncoder {

    /**
     * Add a column to the current result set.
     * 
     * @param res the result set to send to the client.
     * @param message a message associated with the status of the command
     *            executed.
     * @param writer a writer to write the encoded response to.
     */
    public void writeResponse(MocaResults res, String message, int status) 
        throws ServletException, IOException;
}
