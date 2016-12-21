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

package com.sam.util;

/**
 * A runtime exception that indicates that a method on a proxy or stub has been
 * invoked without a corresponding method on the target class.
 * 
 * @see ProxyStub
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public class NotImplementedException extends RuntimeException {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3258410651268494389L;

    /**
     * @param message
     */
    public NotImplementedException(String message) {
        super(message);
    }

}
