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
import com.redprairie.moca.server.TypeMismatchException;
import com.redprairie.moca.server.db.DBType;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * This class tests to make sure that instr function will work correctly.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author gvaneyck
 * @version $Revision$
 */
public class TU_DbDateFunction extends AbstractMocaTestCase {

    public void testBasicDbDateCall() throws MocaException {
        String dbType = _moca.getDb().getDbType();
        
        MocaResults res = _moca.executeCommand("publish data" +
                        "  where date = dbdate ('12345678901234')");
        
        RowIterator rowIter = res.getRows();
        
        assertTrue(rowIter.next());
        
        if (dbType.equals(DBType.MSSQL.toString())) {
            assertEquals("1234-56-78 90:12:34", rowIter.getString("date"));
        }
        else {
            assertEquals("12345678901234", rowIter.getString("date"));
        }
    }
    
    public void testDbDateInvalidDataType() throws MocaException {
        try {
            _moca.executeCommand("publish data" +
                            "  where date = dbdate (null)");
            fail("expected exception");
        }
        catch (TypeMismatchException e) {
            //Normal
        }
    }
    
    public void testDbDateInvalidString() throws MocaException {
        try {
            _moca.executeCommand("publish data" +
                            "  where date = dbdate ('1234567890123')");
            fail("expected exception");
        }
        catch (TypeMismatchException e) {
            //Normal
        }
    }
}
