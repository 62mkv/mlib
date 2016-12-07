/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

package com.redprairie.moca.alerts;

/**
 * Missing Key Value Exception occurs when an XML file is passed to EMS
 * that does not have an key-value node.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 */
public class MissingKeyValueException extends EMSFailureException {
    
    public static final int CODE = 854;

    /**
     * Key value is missing from the XML message passed to EMS.
     */
    public MissingKeyValueException() {
        super(CODE, msg);
    }
    
    // -------------------------------
    // Implementation:
    // -------------------------------    
    private static final long serialVersionUID = -9147673955398406115L;
    private static final String msg = "XML passed without a Key Value.";
}
