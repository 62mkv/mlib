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

package com.redprairie.moca.components.xml;

import java.net.URL;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * This class is to test various xml commands in the mocaxml cmdsrc directory.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_XmlService extends AbstractMocaTestCase {

    /**
     * This test is to make sure that when executing get xml info we actually
     * get some information back
     * @throws MocaException
     */
    public void testGetXmlInformation() throws MocaException {
        MocaResults result = _moca.executeCommand("get xml info");
        
        RowIterator rowIter = result.getRows();
        
        assertTrue(rowIter.next());
        
        assertNotNull(rowIter.getString("mxml_lib_info"));
        assertNotNull(rowIter.getString("mxml_dom_info"));
        assertNotNull(rowIter.getString("mxml_xslt_info"));
    }
    
    public void testConvertXmlToResultSet() throws MocaException {
        URL xmlFile = TU_XmlService.class.getResource("resources/sample.xml");
        
        MocaResults res = _moca.executeCommand("parse xml", 
                new MocaArgument("pathname", xmlFile.getPath()));
        
        RowIterator rowIter = res.getRows();
        
        assertTrue(rowIter.next());
        
        Object pointer = rowIter.getValue("mxml_ctxt");
        
        assertFalse(rowIter.next());
        
        res = _moca.executeCommand("convert xml to result set", 
                new MocaArgument("mxml_ctxt", pointer));
        
        rowIter = res.getRows();
        
        assertTrue(rowIter.next());
        
        assertEquals(MocaType.STRING, res.getColumnType("scard_host_port"));
        assertEquals(MocaType.STRING, res.getColumnType("scard_host_nam"));
        assertEquals(MocaType.STRING, res.getColumnType("scard_ena_flg"));
        assertEquals(MocaType.STRING, res.getColumnType("lmvocollect_ena_flg"));
        assertNull(rowIter.getValue("scard_host_port"));
        assertNull(rowIter.getValue("scard_host_nam"));
        assertEquals("T", rowIter.getString("scard_ena_flg"));
        assertEquals("T", rowIter.getString("lmvocollect_ena_flg"));
    }
}
