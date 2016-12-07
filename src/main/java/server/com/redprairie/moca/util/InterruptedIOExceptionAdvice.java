/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.util;

import java.io.InterruptedIOException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.redprairie.moca.MocaInterruptedException;

/**
 * This class is a simple advice to put around any object so that any 
 * InterruptedIOException will be turned into a MocaInterruptedException.
 * This will also check for interrupt status of thread after so many calls
 * and will throw a MocaInterruptedException as well.
 * This advice is thread safe and can be shared across proxies or when advising
 * an object that is called concurrently from multiple threads.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class InterruptedIOExceptionAdvice implements MethodInterceptor {

    // @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        // First check if the thread is interrupted.
        if (Thread.interrupted()) {
            throw new MocaInterruptedException();
        }
        try {
            return methodInvocation.proceed();
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
    }
}
