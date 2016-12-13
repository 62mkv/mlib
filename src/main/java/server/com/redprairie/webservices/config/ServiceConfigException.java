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

package com.redprairie.webservices.config;

/**
 * RPMechanicalException extends RPException 
 * it is used to throw mechanical errors like parse,
 * Xml format and file/directory not found 
 * <b>
 * 
 * <pre>
 *  Copyright (c) 2016 Sam Corporation
 *  All rights reserved.
 * </pre>
 * 
 * </b>
 * 
 * @author Mohanesha.C
 * @version $Revision$
 */
public class ServiceConfigException extends Exception {

    private static final long serialVersionUID = -7083584493548324024L;

    public ServiceConfigException(String message) {
        super(message);        
    }

    public ServiceConfigException(String message, Throwable cause) {
        super(message, cause);        
    }

}
