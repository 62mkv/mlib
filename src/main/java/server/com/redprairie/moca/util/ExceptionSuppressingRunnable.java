/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

import org.apache.logging.log4j.Logger;

/**
 * A Runnable wrapper, which can invoke the delegate and suppress any unchecked exceptions.
 * 
 * Copyright (c) 2013 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author jmitchel
 */
public class ExceptionSuppressingRunnable implements Runnable{
    
    public ExceptionSuppressingRunnable(Runnable delegate, Logger logger) {
        _delegate = delegate;
        _logger = logger;
    }
    
    public void run() {
        try {
            _delegate.run();
        }
        catch (RuntimeException e) {
            _logger.error("Suppressing unchecked exception.", e);
        }
    }
    
    private final Runnable _delegate;
    private final Logger _logger;
}
