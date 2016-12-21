/*
 *  $URL$
 *  $Revision$
 *  $Author: sprakash$
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

package com.sam.moca.components.base;

import junit.framework.TestCase;

import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.server.ServerUtils;
import com.sam.moca.util.MocaUtils;

/**
 * Unit test for BitArithmetic
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author $Author:
 * @version $Revision$
 */
public class TU_BitArithmetic extends TestCase {
    
    @Override
    protected void setUp() throws Exception {
        ServerUtils.setupDaemonContext(getClass().getName(), true);
    }

    public void testSetBit() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("set bit where bit = 4 and bitset = 0");
        
        // Should be one row with two columns returned.
        assertTrue(res.next());
        assertEquals(8, res.getInt("bitset"));
        assertEquals(4, res.getInt("bit"));

        // Should be no more rows
        assertFalse(res.next());
    }

    public void testClearBit() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("clear bit where bit = 4 and bitset = 8");
        
        // Should be one row with two columns returned.
        assertTrue(res.next());
        assertEquals(0, res.getInt("bitset"));
        assertEquals(4, res.getInt("bit"));

        // Should be no more rows
        assertFalse(res.next());
    }

    public void testClearAllBits() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("clear all bits");
        
        // Should be one row with two columns returned.
        assertTrue(res.next());
        assertEquals(0, res.getInt("bitset"));

        // Should be no more rows
        assertFalse(res.next());
    }

    public void testSetAllBits() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("set all bits");
        
        // Should be one row with two columns returned.
        assertTrue(res.next());
        assertEquals(-1, res.getInt("bitset"));

        // Should be no more rows
        assertFalse(res.next());
    }
    
    public void testSetBitsFromBitmask() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("set bits from bitmask where bitmask = 4 and bitset = 0");
        
        // Should be one row with two columns returned.
        assertTrue(res.next());
        assertEquals(4, res.getInt("bitset"));
        assertEquals(4, res.getInt("bitmask"));

        // Should be no more rows
        assertFalse(res.next());
    }

    public void testBitIsSet() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("bit is set where bit = 4 and bitset = 8");
        
        // Should be one row with two columns returned.
        assertTrue(res.next());
        assertEquals(8, res.getInt("bitset"));
        assertEquals(4, res.getInt("bit"));
        assertEquals(1, res.getInt("result"));

        // Should be no more rows
        assertFalse(res.next());
        
        res = ctx.executeCommand("bit is set where bit = 4 and bitset = 7");
        
        // Should be one row with two columns returned.
        assertTrue(res.next());
        assertEquals(7, res.getInt("bitset"));
        assertEquals(4, res.getInt("bit"));
        assertEquals(0, res.getInt("result"));

        // Should be no more rows
        assertFalse(res.next());
    }

    public void testBitIsClear() throws MocaException {
        MocaContext ctx = MocaUtils.currentContext();
        MocaResults res = ctx.executeCommand("bit is clear where bit = 4 and bitset = 8");
        
        // Should be one row with two columns returned.
        assertTrue(res.next());
        assertEquals(8, res.getInt("bitset"));
        assertEquals(4, res.getInt("bit"));
        assertEquals(0, res.getInt("result"));

        // Should be no more rows
        assertFalse(res.next());
        
        res = ctx.executeCommand("bit is clear where bit = 4 and bitset = 7");
        
        // Should be one row with two columns returned.
        assertTrue(res.next());
        assertEquals(7, res.getInt("bitset"));
        assertEquals(4, res.getInt("bit"));
        assertEquals(1, res.getInt("result"));

        // Should be no more rows
        assertFalse(res.next());
    }
    
    public void testDumpBitset() throws MocaException {
    	MocaContext ctx = MocaUtils.currentContext();
    	MocaResults res = ctx.executeCommand("dump bitset where bitset = 8");
    	
    	// Should be one row with one column returned
    	assertTrue(res.next());
    	assertEquals("00000000 00000000 00000000 00001000", res.getString("representation"));
    	
    	// Should be no more rows
    	assertFalse(res.next());
    }

}
