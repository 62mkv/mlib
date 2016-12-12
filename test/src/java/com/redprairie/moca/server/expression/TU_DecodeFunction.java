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
 * This class is to test to make sure that decode works properly for varying
 * data types
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_DecodeFunction extends AbstractMocaTestCase {

    public void testTrueEqualsOne() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "[[" +
                "    something = true" +
                "]]" +
                "|" +
                "publish data " +
                "  where istrue = decode(@something, 1, 'yes', 0, 'no', 'neither')");
        
        RowIterator rowIter = results.getRows();
        
        assertTrue("There should be 1 row.", rowIter.next());
        
        assertEquals("yes", rowIter.getString("istrue"));
        
        assertFalse("There should be only 1 row.", rowIter.next());
    }
    
    public void testFalseEqualsZero() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "[[" +
                "    something = false" +
                "]]" +
                "|" +
                "publish data " +
                "  where istrue = decode(@something, 1, 'yes', 0, 'no', 'neither')");
        
        RowIterator rowIter = results.getRows();
        
        assertTrue("There should be 1 row.", rowIter.next());
        
        assertEquals("no", rowIter.getString("istrue"));
        
        assertFalse("There should be only 1 row.", rowIter.next());
    }
    
    public void testOneEqualsTrue() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "[[" +
                "    something = 1" +
                "]]" +
                "|" +
                "publish data " +
                "  where istrue = decode(@something, true, 'yes', false, 'no', 'neither')");
        
        RowIterator rowIter = results.getRows();
        
        assertTrue("There should be 1 row.", rowIter.next());
        
        assertEquals("yes", rowIter.getString("istrue"));
        
        assertFalse("There should be only 1 row.", rowIter.next());
    }
    
    public void testZeroEqualsFalse() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "[[" +
                "    something = 0" +
                "]]" +
                "|" +
                "publish data " +
                "  where istrue = decode(@something, true, 'yes', false, 'no', 'neither')");
        
        RowIterator rowIter = results.getRows();
        
        assertTrue("There should be 1 row.", rowIter.next());
        
        assertEquals("no", rowIter.getString("istrue"));
        
        assertFalse("There should be only 1 row.", rowIter.next());
    }
    
    public void testNullReturnsLastValue() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "[[" +
                "    something = null" +
                "]]" +
                "|" +
                "publish data " +
                "  where istrue = decode(@something, true, 'yes', false, 'no', 'neither')");
        
        RowIterator rowIter = results.getRows();
        
        assertTrue("There should be 1 row.", rowIter.next());
        
        assertEquals("neither", rowIter.getString("istrue"));
        
        assertFalse("There should be only 1 row.", rowIter.next());
    }

    public void testNullInterimValue() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "[[" +
                "    something = null" +
                "]]" +
                "|" +
                "publish data " +
                "  where isnull = decode(@something, null, 'yes', false, 'no', 'neither')");
        
        RowIterator rowIter = results.getRows();
        
        assertTrue("There should be 1 row.", rowIter.next());
        
        assertEquals("yes", rowIter.getString("isnull"));
        
        assertFalse("There should be only 1 row.", rowIter.next());
    }
}
