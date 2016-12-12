/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2015
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

package com.redprairie.moca.db.hibernate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static com.redprairie.moca.db.hibernate.HibernateUtils.*;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2015 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author j1014071
 */
public class TU_HibernateUtils {

    
    @Test
    // Our current logic (similar to Hibernate) assumes all literals are used as bound variables at this point,
    // so that embedded literals are handled incorrectly. This test is here to document current
    // incorrect behavior of HibernateUtils.formatSQLForHibernate() call
    public void formatDistinctLiteral(){
        // Verifying that "distinct" inside a literal does change after formatting
        
        for(String sql: new String[]{
                "select '\n"
                + "distinct\n"
                + "'\n"
                + "from dual",
        }){
            String result = HibernateUtils.formatSQLForHibernate(sql);
            // Distinct should have changed (current Hibernate behavior)
            assertTrue(result.indexOf(" distinct ") >= 0);
        }
    }
    
    
    @Test
    public void formatDistinctKeyword(){
        // Verifying that "distinct" keyword is surrounded by spaces after formatting
        
        for(String sql: new String[]{
                "select distinct\n"
                + "from dual",
                "select\n"
                + "distinct\n"
                + "from dual",
        }){
            String result = HibernateUtils.formatSQLForHibernate(sql);
            assertTrue("ERROR: incorrect formatting of \"distinct\" keyword for sql:\n"
                + "\n[\n" + sql + "\n]\n"  
                + "\nFormattedResult:\n"
                + "\n[\n" + result + "\n]\n"  
                , 
                result.indexOf(" distinct ") >= 0);
        }
    }

    @Test
    public void formatOrderByKeyword(){
        // Verify that "order by" is surrounded by spaces after formatting
        
        for(String sql: new String[]{
                "select distinct\n"
                + " nvl(trlr.carcod, appt.carcod) carcod,\n"
                       + "appt.appt_id,\n"
                       + "max(rcvtrk.trknum) trknum,\n"
                       + "count(distinct rcvtrk.trknum) trknum_cnt\n"
                       + "from appt\n"
                       + "order by\n"
                       + "carcod"
        }){
            String result = HibernateUtils.formatSQLForHibernate(sql);
            assertTrue("ERROR: incorrect formatting of \"order by\" keyword for sql:\n"
                    + "\n[\n" + sql + "\n]\n"  
                    + "\nFormattedResult:\n"
                    + "\n[\n" + result + "\n]\n"  
                    , 
                    result.indexOf(" order by ") >= 0);
            
        }
    }
    
    @Test
    public void padKeyWord(){

        String source = "select\n"
                + "distinct\n"
                + "x from dual\n"
                + "order by\n"
                + "x";
        
        
        for(String keyword : new String[] { DISTINCT, ORDER_BY}){
            int index = HibernateUtils.indexOfKeyWord(source, keyword, 0, 0);
            String padded = HibernateUtils.padKeyword(source, index, keyword.length());
            assertTrue(padded.indexOf(" " + keyword + " ") >= 0);
        }
    }
    
    @Test
    public void indexOfKeyWord(){

        String source = "select 'distinct' x from (select distinct x from (select distinct 'x' x from dual)))";
        //               012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
        //               0         1         2         3         4         5         6         6         7
        
        assertEquals(-1, HibernateUtils.indexOfKeyWord(source, DISTINCT, 0, 0));
        assertEquals(33, HibernateUtils.indexOfKeyWord(source, DISTINCT, 0, 1));
        
    }
    
    @Test
    public void indexOf(){
        
        String source = "select distinct x from (select distinct x from (select distinct 'x' x from dual)))";
        //               012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
        //               0         1         2         3         4         5         6         6         7
        
        // case insensitive
        assertEquals(7, HibernateUtils.indexOf(source, DISTINCT, 0, true, 0));
        assertEquals(31, HibernateUtils.indexOf(source, DISTINCT, 0, true, 1));
        assertEquals(55, HibernateUtils.indexOf(source, DISTINCT, 0, true, 2));
        
        source = "select DISTINCT x from (select DisTINct x from (select dIsTincT 'x' x from dual)))";
        //               012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
        //               0         1         2         3         4         5         6         6         7
        
        // case insensitive
        assertEquals(7, HibernateUtils.indexOf(source, DISTINCT, 0, true, 0));
        assertEquals(31, HibernateUtils.indexOf(source, DISTINCT, 0, true, 1));
        assertEquals(55, HibernateUtils.indexOf(source, DISTINCT, 0, true, 2));

        // case sensistive
        assertEquals(-1, HibernateUtils.indexOf(source, DISTINCT, 0, false, 0));
        assertEquals(-1, HibernateUtils.indexOf(source, DISTINCT, 0, false, 1));
        assertEquals(-1, HibernateUtils.indexOf(source, DISTINCT, 0, false, 2));
    }
    
    
}
