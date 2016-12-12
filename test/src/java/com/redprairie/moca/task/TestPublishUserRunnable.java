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

package com.redprairie.moca.task;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.util.MocaUtils;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TestPublishUserRunnable implements Runnable {

    // @see java.lang.Runnable#run()
    @Override
    public void run() {
        MocaContext moca = MocaUtils.currentContext();
        try {
            try {
                MocaResults res = moca.executeCommand(
                        "publish data where user = @@USR_ID");
                TU_TaskManager._exchanger.exchange(res);
            }
            catch (MocaException e) {
                TU_TaskManager._exchanger.exchange(e);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("We were interruptted when we shouldn't have been!");
        }
    }

}
