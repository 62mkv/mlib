/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.redprairie.moca.server.legacy;

import java.util.Date;

import com.redprairie.moca.server.profile.CommandPath;


/**
 * A class representing the native (C/C++/COM) aspects of MOCA.  This is the interface
 * exposed by the RMI server acting as a worker process in the MOCA application server.
 * This interface is not directly used by callers, but is generally wrapped by a decorator
 * interface that handles pool interactions (when necessary).
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public interface NativeProcess extends RemoteNativeProcess {

    public abstract String lastCall();

    public abstract Date lastCallDate();

    public Date dateCreated();

    public Thread getAssociatedThread();

    public CommandPath getLastCommandPath();

    public String getId();
}
