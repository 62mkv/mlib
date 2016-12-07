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

package com.redprairie.moca;

/**
 * This interface defines what hook methods there are when tying into the
 * moca restart call from the admin console.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public interface MocaServerHook {
    /**
     * This method will be invoked as a callback whenever the administrative 
     * command to restart the moca server has been called.  The argument of
     * whether or not it is cleanly restarted is passed as an argument.  In a
     * clean shut down attempts should be made to wait for resources to clean
     * up as well.
     * @param clean If this is true, then attempts to cleanly restart the server
     */
    public void onRestart(boolean clean);
}
