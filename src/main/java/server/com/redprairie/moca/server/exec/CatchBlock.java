/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20167
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

package com.redprairie.moca.server.exec;

import com.redprairie.moca.server.expression.Expression;

public class CatchBlock {
    public void setTest(Expression test) {
        _test  = test;
    }
    
    public void setBlock(CommandSequence block) {
        _block = block;
    }
    
    public Expression getTest() {
        return _test;
    }
    
    /**
     * @return Returns the block.
     */
    public CommandSequence getBlock() {
        return _block;
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        tmp.append("catch(");
        tmp.append(_test);
        tmp.append(')');
        if (_block != null) {
            tmp.append(_block);
        }
        else {
            tmp.append("(catch-only)");
        }
        return tmp.toString();
    }
    
    private Expression _test;
    private CommandSequence _block;
}
