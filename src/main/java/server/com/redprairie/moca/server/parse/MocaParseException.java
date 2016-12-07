/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2007
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

package com.redprairie.moca.server.parse;

import com.redprairie.moca.MocaException;

public class MocaParseException extends MocaException {

    public MocaParseException(int line, int linePos, String detail) {
        super(505, "Syntax error at line ^line^.^byte^: ^text^");
        addArg("line", Integer.valueOf(line));
        addArg("byte", Integer.valueOf(linePos));
        addArg("text", detail);
    }
    
    private static final long serialVersionUID = -7029640392742041267L;
}
