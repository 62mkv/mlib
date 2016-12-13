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

package com.redprairie.moca;

/**
 * This class holds various constants related to how MOCA runs
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class MocaConstants {
    public final static String COPYRIGHT_STRING = "Copyright (c) 2016 " +
    		"Mlib Corporation.  All rights reserved.";
    
    public final static String WEB_CLIENT_ADDR = "WEB_CLIENT_ADDR";
    public final static String ENV_DEVICE_ID = "DEVCOD";
    public static final String SUSPENDED_TX = "SUSPEND-TX";
    
    public final static Object HIDDEN = Values.HIDDEN;
    
    enum Values {
        HIDDEN("<hidden>");
        
        private Values(String value) {
            _value = value;
        }
        
        @Override
        public String toString() {
            return _value;
        }
        
        private final String _value;
    }
}
