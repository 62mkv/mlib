/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.task;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TestTask implements Runnable {
    public static void main(String[] args) {
        TestTask task = new TestTask();
        task.run();
    }
    
    // @see java.lang.Runnable#run()
    @Override
    public void run() {
        try {
            Thread.sleep(10000);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        throw new NullPointerException();
    }

}
