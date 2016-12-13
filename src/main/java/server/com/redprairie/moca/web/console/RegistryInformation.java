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

package com.redprairie.moca.web.console;

import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SystemContext;

public class RegistryInformation {

    private RegistryInformation() {
    }

    public static String getRegistryContents() {
        return ContentsHolder._contents;
    }
    
    private static class ContentsHolder {
        static {
            SystemContext ctxt = ServerUtils.globalContext();
            _contents = ctxt.toString();
        }
        
        private final static String _contents;
    }
}