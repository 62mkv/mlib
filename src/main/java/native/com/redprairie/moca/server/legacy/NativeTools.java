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

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.util.DateUtils;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public class NativeTools {
    public static String getArgName(MocaArgument arg) {
        return arg.getName();
    }
    
    public static int getArgOper(MocaArgument arg) {

        // #define OPR_NONE       0
        // #define OPR_NOTNULL    1
        // #define OPR_ISNULL     2
        // #define OPR_EQ         3
        // #define OPR_NE         4
        // #define OPR_LT         5
        // #define OPR_LE         6
        // #define OPR_GT         7
        // #define OPR_GE         8
        // #define OPR_LIKE       9
        // #define OPR_RAWCLAUSE  10
        // #define OPR_REFALL     11
        // #define OPR_REFONE     12
        // #define OPR_REFLIKE    13
        // #define OPR_NOTLIKE    14
        // #define OPR_NAMECLAUSE 15
        
        switch (arg.getOper()) {
        case NOTNULL:
            return 1;
        case ISNULL:
            return 2;
        case EQ:
            return 3;
        case NE:
            return 4;
        case LT:
            return 5;
        case LE:
            return 6;
        case GT:
            return 7;
        case GE:
            return 8;
        case LIKE:
            return 9;
        case RAWCLAUSE:
            return 10;
        case NOTLIKE:
            return 14;
        case NAMEDCLAUSE:
            return 15;
        default:
            return 0;
        }
    }

    public static char getArgType(MocaArgument arg) {
        return arg.getType().getTypeCode();
    }

    public static Object getArgValue(MocaArgument arg) {
        Object value;
        // If it was a date change it into a string as C can't handle a date 
        // object
        if (arg.getType() == MocaType.DATETIME) {
            value = DateUtils.formatDate((Date)arg.getValue());
        }
        else {
            value = arg.getValue();
        }
        
        if (value instanceof MocaResults && ! (value instanceof WrappedResults)) {
            WrappedResults out = new WrappedResults((MocaResults)value, false);
            value = out;
        }

        return value;
    }


}
