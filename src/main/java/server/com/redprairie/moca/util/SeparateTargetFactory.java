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

package com.redprairie.moca.util;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopConfigException;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.framework.DefaultAopProxyFactory;

/**
 * This class is needed since each of our target source was being shared
 * between the proxies and we copy the support to make sure we don't violate
 * each one on a new call.

 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class SeparateTargetFactory extends DefaultAopProxyFactory {
    private static final long serialVersionUID = 7283919713505740220L;

    // @see org.springframework.aop.framework.DefaultAopProxyFactory#createAopProxy(org.springframework.aop.framework.AdvisedSupport)
    @Override
    public AopProxy createAopProxy(AdvisedSupport config)
            throws AopConfigException {
        // Since the default proxy creator keeps reusing the same advised
        // support we have to copy everything as a new config so that each
        // object will be different
        AdvisedSupport newSupport = new AdvisedSupport(
                config.getProxiedInterfaces());
        newSupport.setTargetSource(config.getTargetSource());
        newSupport.addAdvisors(config.getAdvisors());
        // This only copies frozen stuff
        newSupport.copyFrom(config);
        
        return super.createAopProxy(newSupport);
    }
}
