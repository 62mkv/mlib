/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.test;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.util.MocaUtils;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class TestTask implements Runnable {

    // @see java.lang.Runnable#run()

    @Override
    public void run() {
        MocaContext moca = MocaUtils.currentContext();
        _log.info("Running My Task");
        
        try {
            for (int i = 0; i < 10; i++) {
                _log.info("Doing Thing " + i);
                moca.executeCommand("publish data where x = '" + i + "'");
                Thread.sleep(2000);
            }
            
            moca.executeCommand("blah blah blah");
        }
        catch (MocaException e) {
            _log.error("Unable to execute command ", e);
        }
        catch (InterruptedException e) {
            _log.warn("Interrupted!");
        }
    }
    
    private static Logger _log = LogManager.getLogger(TestTask.class);
}
