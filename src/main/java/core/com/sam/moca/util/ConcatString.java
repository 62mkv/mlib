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

package com.sam.moca.util;

/**
 * TODO Class Description
 * 
 * @author dinksett
 * @version $Revision$
 */
public class ConcatString {
    public ConcatString(Object... fields) {
        _fields = fields;
    }
    
    @Override
    public String toString() {
        if (_string == null) {
            synchronized(this) {
                if (_string == null) {
                    StringBuilder buf = new StringBuilder();
                    for (Object f : _fields) {
                        buf.append(f);
                    }
                    _string = buf.toString();
                }
            }
        }
        
        return _string;
    }
    
    private final Object[] _fields;
    private volatile String _string;
}
