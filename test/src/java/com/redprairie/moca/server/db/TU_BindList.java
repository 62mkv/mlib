/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2006
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

package com.redprairie.moca.server.db;

import java.sql.Timestamp;
import java.util.Date;

import junit.framework.TestCase;

import com.redprairie.moca.MocaType;
import com.redprairie.moca.util.MocaUtils;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_BindList extends TestCase {
    public void testCaseInsensitivity() {
        BindList bind = new BindList();
        bind.add("v_double_value", MocaType.DOUBLE.getTypeCode(), 3.14);
        bind.add("v_string_value", MocaType.STRING.getTypeCode(), "Test String 1");
        bind.add("v_string_value2", MocaType.STRING.getTypeCode(), "Test String 2");
        bind.add("v_string_value3", MocaType.STRING.getTypeCode(), "Test String 3", 3000);
        assertEquals(MocaType.DOUBLE, bind.getType("v_DOUBLE_VALUE"));
        assertEquals(MocaType.DOUBLE, bind.getType("v_double_value"));
        assertEquals(3.14, ((Double)bind.getValue("v_double_value")).doubleValue(), 0.0);
        assertEquals(3.14, ((Double)bind.getValue("v_DOUBLE_VALUE")).doubleValue(), 0.0);
        assertEquals(MocaType.STRING, bind.getType("v_string_value"));
        assertEquals(MocaType.STRING, bind.getType("v_String_Value"));
        assertEquals("Test String 1", (String)bind.getValue("v_string_value"));
        assertEquals("Test String 1", (String)bind.getValue("v_String_Value"));
        assertEquals(MocaType.STRING, bind.getType("v_string_value2"));
        assertEquals(MocaType.STRING, bind.getType("v_STRING_ValuE2"));
        assertEquals("Test String 2", (String)bind.getValue("v_string_value2"));
        assertEquals("Test String 2", (String)bind.getValue("v_STRING_ValuE2"));
        assertEquals(3000, bind.getSize("v_string_value3"));
        assertEquals(3000, bind.getSize("V_STRiNG_ValUe3"));
    }

    public void testMissingVariable() {
        BindList bind = new BindList();
        bind.add("v_double_value", MocaType.DOUBLE.getTypeCode(), 3.14);
        bind.add("v_string_value", MocaType.STRING.getTypeCode(), "Test String 1");
        bind.add("v_string_value2", MocaType.STRING.getTypeCode(), "Test String 2");
        assertNull(bind.getType("xxx"));
        assertNull(bind.getValue("xxx"));
    }
    
    public void testIsEmpty() {
        BindList bind = new BindList();
        assertTrue(bind.isEmpty());
        bind.add("_null", MocaType.STRING, null);
        assertFalse(bind.isEmpty());
    }
    
    public void testGetSize() {
        BindList bind = new BindList();
        bind.add("d1", MocaType.DOUBLE.getTypeCode(), 3.14);
        bind.add("s1", MocaType.STRING.getTypeCode(), "abcdefg");
        bind.add("s2", MocaType.STRING.getTypeCode(), "abcdefg", 200);
        bind.add("s3", MocaType.STRING_REF.getTypeCode(), "abcdefg", 200);
        bind.add("d2", MocaType.DATETIME.getTypeCode(), "20060101000002");
        
        assertEquals(7, bind.getSize("s1"));
        assertEquals(200, bind.getSize("s2"));
        assertEquals(200, bind.getSize("s3"));
        assertEquals(14, bind.getSize("d2"));
    }

    public void testGetDateTimeValue() {
        BindList bind = new BindList();
        Date orig = MocaUtils.parseDate("20060101000002");
        bind.add("d1", MocaType.DATETIME.getTypeCode(), orig);
        bind.add("d2", MocaType.DATETIME.getTypeCode(), "20060101000002");
        
        assertEquals(MocaType.DATETIME, bind.getType("d1"));
        assertEquals(MocaType.DATETIME, bind.getType("d2"));
        Object d1 = bind.getValue("d1");
        assertTrue(d1 instanceof Timestamp);
        assertEquals(orig.getTime(), ((Timestamp)d1).getTime());

        assertEquals("20060101000002", bind.getValue("d2"));
        assertEquals("20060101000002", MocaUtils.formatDate((Date)d1));
    }

}
