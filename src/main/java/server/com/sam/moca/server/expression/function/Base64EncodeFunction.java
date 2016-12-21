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

package com.sam.moca.server.expression.function;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.sam.moca.MocaException;
import com.sam.moca.MocaType;
import com.sam.moca.MocaValue;
import com.sam.moca.server.exec.ServerContext;
import com.sam.util.Base64;

/**
 * Encodes a binary field (byte array) into a base-64 encoded string value.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author derek
 * @version $Revision$
 */
public class Base64EncodeFunction extends BaseFunction {

    @Override
    protected MocaValue invoke(ServerContext ctx, List<MocaValue> args) throws MocaException {
        if (args.size() != 1) {
            throw new FunctionArgumentException("expected single argument");
        }

        MocaValue argValue = args.get(0);

        if (argValue.isNull()) {
            return new MocaValue(MocaType.STRING, null);
        }

        if (argValue.getType() != MocaType.BINARY) {
            throw new FunctionArgumentException("expected binary argument");
        }
        
        StringBuilder out = new StringBuilder();
        try {
            Base64.encode(new ByteArrayInputStream((byte[])argValue.getValue()), out);
        }
        catch (IOException e) {
            throw new FunctionArgumentException("Encoding Exception: " + e, e);
        }
        
        return new MocaValue(MocaType.STRING, out.toString());
    }
}
