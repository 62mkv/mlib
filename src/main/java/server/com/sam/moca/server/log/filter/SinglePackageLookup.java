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

package com.sam.moca.server.log.filter;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SinglePackageLookup implements PackageLookup {
    
    public SinglePackageLookup(String presentPackage) {
        this.presentPackage = presentPackage;
    }

    // @see com.sam.moca.server.log.filter.PackageLookup#isPackagePresent(java.lang.String)
    @Override
    public boolean isPackagePresent(String packageName) {
        return packageName.startsWith(presentPackage);
    }

    private final String presentPackage;
}
