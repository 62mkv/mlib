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

package com.redprairie.moca.server.expression;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * This class tests to make sure that instr function will work correctly.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_InstrFunction extends AbstractMocaTestCase {

    public void testBasicInstrCall() throws MocaException {
        MocaResults res = _moca.executeCommand("publish data" +
        		"  where colon_pos = instr ('xxxx:9', ':')");
        
        RowIterator rowIter = res.getRows();
        
        assertTrue(rowIter.next());
        
        assertEquals(5, rowIter.getInt("colon_pos"));
    }
    
    public void testBasicInstrCallNotPresent() throws MocaException {
        MocaResults res = _moca.executeCommand("publish data" +
                        "  where colon_pos = instr ('xxxx:9', ';')");
        
        RowIterator rowIter = res.getRows();
        
        assertTrue(rowIter.next());
        
        assertEquals(0, rowIter.getInt("colon_pos"));
    }
}