/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.moca.web.console;

import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.NotFoundException;
import com.sam.moca.SimpleResults;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.server.exec.ServerContext;

public class ComponentLibraryInformation {

    private ComponentLibraryInformation() {
    }

    public static MocaResults getComponentLibraries() throws MocaException {
        ServerContext ctxt = ServerUtils.getCurrentContext();

        try {
            return ctxt.executeCommand("list library versions", null, false);
        }
        catch (NotFoundException e) {
            return new SimpleResults();
        }
    }
}