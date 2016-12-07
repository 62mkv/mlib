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

package com.redprairie.moca.util;

/**
 * TODO Class Description
 * 
 * @author dinksett
 * @version $Revision$
 */
public class FormatString {
    public FormatString(String fmt, Object... args) {
        _fmt = fmt;
        _args = args;
    }
    
    @Override
    public String toString() {
        if (_string == null) {
            synchronized(this) {
                if (_string == null) {
                    _string = String.format(_fmt, _args);
                }
            }
        }
        return _string;
    }
    
    private volatile String _string;
    private final String _fmt;
    private final Object[] _args;
}
