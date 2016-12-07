/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

import java.lang.reflect.Field;

/**
 * Constants and methods used in tracing from MOCA components.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public class MocaTrace {
    public static final int FLOW    = (1<<0);
    public static final int SQL     = (1<<1);
    public static final int MGR     = (1<<2);
    public static final int SERVER  = (1<<3);
    public static final int SRVARGS = (1<<4);
    public static final int PERF    = (1<<5);
    
    private static final int ALL;
    
    public static int getAllLevels() {
        return ALL;
    }
    
    static {
        int allLevels = 0;
        // We want to make sure that every trace level is disabled
        Field[] traceValues = MocaTrace.class.getFields();
        
        for (Field traceValue : traceValues) {
            int level = 0;
            try {
                level = traceValue.getInt(null);
            }
            catch (IllegalArgumentException e) {
                // Just ignore it if so, this doesn't really make any sense
            }
            catch (IllegalAccessException e) {
                // Just ignore it if so, this doesn't really make any sense
            }
            allLevels |= level; 
        }
        ALL = allLevels;
    }
}
