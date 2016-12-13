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

package com.redprairie.moca.server.db;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.exceptions.MocaDBException;
import com.redprairie.moca.server.db.translate.TranslationException;
import com.redprairie.moca.util.AbstractMocaTestCase;

/**
 * This tests to ensure that a date will bind as a string
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TI_StringBindingAsDate extends AbstractMocaTestCase {

    /**
     * This test is to make sure that the date binds as a string so that to_date
     * will not fail
     * @throws MocaException
     */
    public void testDateIsBindingAsStringToDatabase() throws MocaException {
        MocaResults results = _moca.executeCommand(
                "publish data where date = sysdate " +
        	"|" +
        	"[select to_date(@date) foo from dual]");
        
        assertEquals(MocaType.DATETIME,results.getColumnType("foo"));
        
        RowIterator rowIter = results.getRows();
        
        assertTrue("There should be 1 row", rowIter.next());
        
        assertNotNull("We should have a date object", 
                rowIter.getDateTime("foo"));
    }

    public void testDateWithBadFormat() throws MocaException {
        try {
            MocaResults results = _moca.executeCommand(
                    "publish data where date = '2009-04-03 23:59:59'" +
                    "|" +
                    "[select to_date(@date:date) foo from dual]");
            results.next();
            fail("Expected Exception, got " + results.getString("foo"));
        }
        catch (MocaDBException e) {
            // Normal -- depending on the DB, we may get a rejection of the date passed in.
        }
        catch (TranslationException e) {
            // Normal -- depending on the db, we could get a failure to translate the call to to_date
        }
    }
}
