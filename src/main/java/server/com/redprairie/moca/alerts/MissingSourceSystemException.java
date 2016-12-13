/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.redprairie.moca.alerts;

/**
 * Missing Source System Exception occurs when an XML file is passed to EMS
 * that does not have an source-system node.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Brandon Grady
 * @version $Revision$
 */
public class MissingSourceSystemException extends EMSFailureException {

    public static final int CODE = 853;

    /**
     * Source System is missing from the EMS message.
     */
    public MissingSourceSystemException() {
        super(CODE, msg);
    }
    
    // -----------------------------
    // Implementation:
    // -----------------------------
    private static final long serialVersionUID = -7373652159274570587L;
    private static final String msg = "XML passed without a Source System.";
    
}
