/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.components.base;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.util.MocaUtils;

/**
 * Builds FTPDirectories and handles resolving any
 * environment variables in them.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
class FTPDirectories {
    
    /**
     * Takes directory paths for the local and remote machine and
     * resolves environment variables appropriately.
     * @param ctx The MocaContext
     * @param localDirectoryPath The local directory path
     * @param remoteDirectoryPath The remote directory path
     */
    public FTPDirectories(MocaContext ctx, String localDirectoryPath, String remoteDirectoryPath) {
        _localDirectory = localDirectoryPath == null ? null :
                MocaUtils.expandEnvironmentVariables(ctx, localDirectoryPath).trim();
        
        _remoteDirectory = remoteDirectoryPath == null ? null :
               MocaUtils.expandEnvironmentVariables(ctx, remoteDirectoryPath).trim();
    }
    
    public String getLocalDirectory() {
        return _localDirectory;
    }
    
    public String getRemoteDirectory() {
        return _remoteDirectory;
    }
    
    private final String _localDirectory;
    private final String _remoteDirectory;

}
