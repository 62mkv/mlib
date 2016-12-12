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

package com.redprairie.moca.server.legacy;

import org.junit.BeforeClass;
import org.junit.Test;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.TU_AbstractEditableResults;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.SystemConfigurationException;

/**
 * This class tests some of the functionalilty of the WrappedResults class
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_WrappedResults extends TU_AbstractEditableResults {
    
    @BeforeClass public static void beforeClass() throws SystemConfigurationException {
        ServerUtils.setupDaemonContext(TU_WrappedResults.class.getName(), true);
    }

    /**
     * This is to verify that has next will work correctly while adding rows
     * and doing next on the same result set
     */
    @Test public void testHasNext() {
        WrappedResults results = new WrappedResults();
        
        results.addColumn("test", MocaType.STRING);
        
        junit.framework.Assert.assertFalse("We shouldn't have any rows yet", 
                results.hasNext());
        
        results.addRow();
        
        results.setStringValue("test", "1");
        
        junit.framework.Assert.assertTrue("We should have a row now", 
                results.hasNext());
        
        junit.framework.Assert.assertTrue("We should be able to iterate to " +
        		"the next row", results.next());
        
        junit.framework.Assert.assertFalse("We shouldn't have another row yet", 
                results.hasNext());
    }
    
    /* (non-Javadoc)
     * @see com.redprairie.moca.TU_AbstractEditableResults#_createNewResults()
     */
    @Override
    protected EditableResults _createNewResults() {
        return new WrappedResults();
    }
}
