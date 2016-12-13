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

package com.redprairie.moca.server.expression.function;

import java.util.List;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.server.TypeMismatchException;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.server.exec.ServerContext;

/**
 * Convert the given argument, which must be a string in the "YYYYMMDDHHMISS"
 * format, to a string in the correct format for the database.
 * 
 * Oracle      YYYYMMDDHHMISS
 * SQL Server  YYYY-MM-DD HH:MI:SS
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author gvaneyck
 * @version $Revision$
 */
public class DbDateFunction extends BaseFunction {

    @Override
    protected MocaValue invoke(ServerContext ctx, List<MocaValue> args) throws MocaException {
        if (args.size() != 1) {
            throw new FunctionArgumentException("expected single argument");
        }
        
        MocaValue argValue = args.get(0);
        if (argValue.isNull()) {
            throw new TypeMismatchException(
                    "string in YYYYMMDDHHMISS format for dbdate");
        }
        
        String value = argValue.asString();
        if (value.length() != 14) {
            throw new TypeMismatchException(
                    "string in YYYYMMDDHHMISS format for dbdate");
        }

        if (ctx.getDbType() == DBType.MSSQL) {
            StringBuilder sb = new StringBuilder();
            sb.append(value.substring(0, 4));
            sb.append("-");
            sb.append(value.substring(4, 6));
            sb.append("-");
            sb.append(value.substring(6, 8));
            sb.append(" ");
            sb.append(value.substring(8, 10));
            sb.append(":");
            sb.append(value.substring(10, 12));
            sb.append(":");
            sb.append(value.substring(12));
            return new MocaValue(MocaType.STRING, sb.toString());
        }
        else {
            return new MocaValue(MocaType.STRING, value);
        }
    }
}
