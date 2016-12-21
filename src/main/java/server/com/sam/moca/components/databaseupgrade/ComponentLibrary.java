/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20167
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

package com.sam.moca.components.databaseupgrade;

import com.sam.moca.MocaLibInfo;
import com.sam.moca.server.MocaServerLibInfo;

/**
 * MOCA component library initialization component. 
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author sehlke
 * @version $Revision$
 */
public class ComponentLibrary {

    public MocaLibInfo initialize() {
        return _LIBINFO;
    }
    
    private static final MocaLibInfo _LIBINFO = new MocaServerLibInfo();
}


