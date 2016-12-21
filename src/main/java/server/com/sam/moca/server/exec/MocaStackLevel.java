/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.moca.server.exec;

/**
 * This class is not thread safe and should only be contained within a
 * {@link DefaultServerContext} since that object is also not thread safe.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class MocaStackLevel implements StackLevel {

    // @see com.sam.moca.server.exec.StackLevel#getLevel()
    @Override
    public int getLevel() {
        return _stackLevel;
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        if (_string == null) {
            StringBuilder sb = new StringBuilder();
            sb.append(_stackLevel < 0 ? 0 : _stackLevel);
            
            _string = sb.toString();
        }
        return _string;
    }

    void incrementLevel() {
        _stackLevel++;
        _string = null;
    }
    
    void decrementLevel() {
        _stackLevel--;
        _string = null;
    }

    private int _stackLevel = -1;
    private String _string;
}
