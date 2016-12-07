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

package com.redprairie.moca.crud;

import com.redprairie.moca.MocaException;

/**
 * This exception is used when an invalid date format is used
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class DateInvalidException extends MocaException {
    private static final long serialVersionUID = 96933585253682085L;

    /**
     * @param errorCode
     * @param message
     * @param t
     */
    public DateInvalidException(String dateName, String dateValue, 
            IllegalArgumentException e) {
        super(2961, "Invalid date value (^dteval^) for ^dtenam^", e);
        super.addArg("dtenam", dateName);
        super.addArg("dteval", dateValue);
    }

}
