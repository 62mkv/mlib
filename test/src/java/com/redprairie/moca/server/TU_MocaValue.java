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

package com.redprairie.moca.server;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

import junit.framework.Assert;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;


/**
 * This class is used to test some of the logic for MocaValue
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_MocaValue {
   
    @Test
    public void testAsBoolean() throws MocaException {
        MocaValue nullValue = new MocaValue(MocaType.OBJECT, null);
        MocaValue emptyString = new MocaValue(MocaType.STRING, "");
        MocaValue stringValue = new MocaValue(MocaType.STRING, "foo");
        MocaValue trueValue = new MocaValue(MocaType.BOOLEAN, true);
        MocaValue falseValue = new MocaValue(MocaType.BOOLEAN, false);
        MocaValue intNonzeroValue = new MocaValue(MocaType.INTEGER, 1);
        MocaValue intZeroValue = new MocaValue(MocaType.INTEGER, 0);
        MocaValue doubleNonzeroValue = new MocaValue(MocaType.DOUBLE, 1.1);
        MocaValue doubleZeroValue = new MocaValue(MocaType.DOUBLE, 0.0);
        
        Assert.assertEquals(false, nullValue.asBoolean());
        Assert.assertEquals(false, emptyString.asBoolean());
        Assert.assertEquals(true, stringValue.asBoolean());
        Assert.assertEquals(true, trueValue.asBoolean());
        Assert.assertEquals(false, falseValue.asBoolean());
        Assert.assertEquals(true, intNonzeroValue.asBoolean());
        Assert.assertEquals(false, intZeroValue.asBoolean());
        Assert.assertEquals(true, doubleNonzeroValue.asBoolean());
        Assert.assertEquals(false, doubleZeroValue.asBoolean());
    }
    
    @Test
    public void testAsString() throws MocaException, ParseException {
        DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        MocaValue nullValue = new MocaValue(MocaType.OBJECT, null);
        MocaValue emptyString = new MocaValue(MocaType.STRING, "");
        MocaValue nullString = new MocaValue(MocaType.STRING, null);
        MocaValue nullInt = new MocaValue(MocaType.INTEGER, null);
        MocaValue dateValue = new MocaValue(MocaType.DATETIME, dateFormat.parseDateTime("19700101000000").toDate());
        MocaValue stringValue = new MocaValue(MocaType.STRING, "foo");
        MocaValue trueValue = new MocaValue(MocaType.BOOLEAN, true);
        MocaValue falseValue = new MocaValue(MocaType.BOOLEAN, false);
        MocaValue intValue = new MocaValue(MocaType.INTEGER, 123);
        MocaValue doubleValue1 = new MocaValue(MocaType.DOUBLE, 1.1);
        MocaValue doubleValue2 = new MocaValue(MocaType.DOUBLE, 123.0);
        MocaValue doubleValue3 = new MocaValue(MocaType.DOUBLE, new BigDecimal("123.123"));
        
        Assert.assertEquals(null, nullValue.asString());
        Assert.assertEquals(null, nullInt.asString());
        Assert.assertEquals(null, nullString.asString());
        Assert.assertEquals("", emptyString.asString());
        Assert.assertEquals("19700101000000", dateValue.asString());
        Assert.assertEquals("foo", stringValue.asString());
        Assert.assertEquals("1", trueValue.asString());
        Assert.assertEquals("0", falseValue.asString());
        Assert.assertEquals("123", intValue.asString());
        Assert.assertEquals("1.1", doubleValue1.asString());
        Assert.assertEquals("123", doubleValue2.asString());
        Assert.assertEquals("123.123", doubleValue3.asString());
    }
    
    @Test
    public void testAsInt() throws MocaException {
        /*
         * Boolean
         */
        MocaValue booleanTrueValue = new MocaValue(MocaType.BOOLEAN, true);
        MocaValue booleanFalseValue = new MocaValue(MocaType.BOOLEAN, false);

        Assert.assertEquals(1, booleanTrueValue.asInt());
        Assert.assertEquals(0, booleanFalseValue.asInt());

        /*
         * Integer
         */
        MocaValue intValue = new MocaValue(MocaType.INTEGER, 1);

        Assert.assertEquals(1, intValue.asInt());

        /*
         * Double
         */
        MocaValue doubleValue = new MocaValue(MocaType.DOUBLE, 1.0);

        Assert.assertEquals(1, doubleValue.asInt());

        /*
         * String
         */
        MocaValue trimmedStringIntValue = new MocaValue(MocaType.STRING, "1");
        MocaValue untrimmedStringIntValue = new MocaValue(MocaType.STRING, " 1 ");
        MocaValue trimmedStringDoubleValue = new MocaValue(MocaType.STRING, "1.0");
        MocaValue untrimmedStringDoubleValue = new MocaValue(MocaType.STRING, " 1.0 ");

        Assert.assertEquals(1, trimmedStringIntValue.asInt());
        Assert.assertEquals(1, untrimmedStringIntValue.asInt());
        Assert.assertEquals(1, trimmedStringDoubleValue.asInt());
        Assert.assertEquals(1, untrimmedStringDoubleValue.asInt());

        /*
         * NULL
         */
        MocaValue nullValue = new MocaValue(MocaType.STRING, null); 
        
        Assert.assertEquals(0, nullValue.asInt());
    }
    
    @Test
    public void testAsDouble() throws MocaException {
        MocaValue nullValue = new MocaValue(MocaType.OBJECT, null);
        MocaValue emptyString = new MocaValue(MocaType.STRING, "");
        MocaValue nonValue = new MocaValue(MocaType.DATETIME, new Date());
        MocaValue stringValue = new MocaValue(MocaType.STRING, "1.1");
        MocaValue trueValue = new MocaValue(MocaType.BOOLEAN, true);
        MocaValue falseValue = new MocaValue(MocaType.BOOLEAN, false);
        MocaValue intValue = new MocaValue(MocaType.INTEGER, 123);
        MocaValue doubleValue1 = new MocaValue(MocaType.DOUBLE, 2.2);
        MocaValue doubleValue2 = new MocaValue(MocaType.DOUBLE, new BigDecimal("123.123"));
        
        Assert.assertEquals(0.0, nullValue.asDouble());
        Assert.assertEquals(0.0, emptyString.asDouble());
        Assert.assertEquals(0.0, nonValue.asDouble());
        Assert.assertEquals(1.1, stringValue.asDouble());
        Assert.assertEquals(1.0, trueValue.asDouble());
        Assert.assertEquals(0.0, falseValue.asDouble());
        Assert.assertEquals(123.0, intValue.asDouble());
        Assert.assertEquals(2.2, doubleValue1.asDouble());
        Assert.assertEquals(123.123, doubleValue2.asDouble());
    }

    @Test
    public void testAsDate() throws MocaException, ParseException {
        DateTimeFormatter dateFormat = DateTimeFormat.forPattern("yyyyMMddHHmmss");
        Date epoch = dateFormat.parseDateTime("19700101000000").toDate();
        
        MocaValue nullValue = new MocaValue(MocaType.OBJECT, null);
        MocaValue emptyString = new MocaValue(MocaType.STRING, "");
        MocaValue dateValue = new MocaValue(MocaType.DATETIME, epoch);
        MocaValue stringValue = new MocaValue(MocaType.STRING, "19700101000000");
        MocaValue trueValue = new MocaValue(MocaType.BOOLEAN, true);
        MocaValue intValue = new MocaValue(MocaType.INTEGER, 123);
        
        Assert.assertEquals(null, nullValue.asDate());
        Assert.assertEquals(null, emptyString.asDate());
        Assert.assertEquals(epoch, dateValue.asDate());
        Assert.assertEquals(epoch, stringValue.asDate());
        Assert.assertEquals(null, trueValue.asDate());
        Assert.assertEquals(null, intValue.asDate());
    }
    
    @Test
    public void testCompareToWithNulls() {
        MocaValue nullObject = new MocaValue(MocaType.OBJECT, null);
        MocaValue nullString = new MocaValue(MocaType.STRING, null);
        MocaValue nullInt = new MocaValue(MocaType.INTEGER, null);
        MocaValue nullDouble = new MocaValue(MocaType.DOUBLE, null);
        MocaValue nullBinary = new MocaValue(MocaType.BINARY, null);
        MocaValue nullBoolean = new MocaValue(MocaType.BOOLEAN, null);
        MocaValue nullResults = new MocaValue(MocaType.RESULTS, null);
        MocaValue nullGeneric = new MocaValue(MocaType.GENERIC, null);
        MocaValue nullDate = new MocaValue(MocaType.DATETIME, null);
        
        MocaValue emptyString = new MocaValue(MocaType.STRING, "");
        
        Assert.assertEquals(0, emptyString.compareTo(nullObject));
        Assert.assertEquals(0, emptyString.compareTo(nullString));
        Assert.assertEquals(0, emptyString.compareTo(nullInt));
        Assert.assertEquals(0, emptyString.compareTo(nullDouble));
        Assert.assertEquals(0, emptyString.compareTo(nullBinary));
        Assert.assertEquals(0, emptyString.compareTo(nullBoolean));
        Assert.assertEquals(0, emptyString.compareTo(nullResults));
        Assert.assertEquals(0, emptyString.compareTo(nullGeneric));
        Assert.assertEquals(0, emptyString.compareTo(nullDate));

        Assert.assertEquals(0, nullObject.compareTo(emptyString));
        Assert.assertEquals(0, nullString.compareTo(emptyString));
        Assert.assertEquals(0, nullInt.compareTo(emptyString));
        Assert.assertEquals(0, nullDouble.compareTo(emptyString));
        Assert.assertEquals(0, nullBinary.compareTo(emptyString));
        Assert.assertEquals(0, nullBoolean.compareTo(emptyString));
        Assert.assertEquals(0, nullResults.compareTo(emptyString));
        Assert.assertEquals(0, nullGeneric.compareTo(emptyString));
        Assert.assertEquals(0, nullDate.compareTo(emptyString));
    }
    @Test
    public void testCompareToWithStringsAndOtherTypes() {
       Assert.assertEquals(0, new MocaValue(MocaType.STRING, "10").compareTo(new MocaValue(MocaType.INTEGER, 10)));
       Assert.assertEquals(0, new MocaValue(MocaType.STRING, "10").compareTo(new MocaValue(MocaType.DOUBLE, 10.0)));
       Assert.assertEquals(0, new MocaValue(MocaType.STRING, "1").compareTo(new MocaValue(MocaType.BOOLEAN, true)));
       Assert.assertEquals(0, new MocaValue(MocaType.INTEGER, 1).compareTo(new MocaValue(MocaType.BOOLEAN, true)));
       Assert.assertTrue(new MocaValue(MocaType.STRING, "10").compareTo(new MocaValue(MocaType.INTEGER, 2)) < 0);
       Assert.assertTrue(new MocaValue(MocaType.STRING, "10").compareTo(new MocaValue(MocaType.DOUBLE, 2.1)) < 0);
       Assert.assertTrue(new MocaValue(MocaType.STRING, "").compareTo(new MocaValue(MocaType.BOOLEAN, true)) < 0);

       Assert.assertEquals(0, new MocaValue(MocaType.INTEGER, 10).compareTo(new MocaValue(MocaType.STRING, "10")));
       Assert.assertEquals(0, new MocaValue(MocaType.DOUBLE, 10.0).compareTo(new MocaValue(MocaType.STRING, "10")));
       Assert.assertEquals(0, new MocaValue(MocaType.BOOLEAN, true).compareTo(new MocaValue(MocaType.STRING, "1")));
       Assert.assertEquals(0, new MocaValue(MocaType.INTEGER, 1).compareTo(new MocaValue(MocaType.BOOLEAN, true)));
       Assert.assertTrue(new MocaValue(MocaType.INTEGER, 2).compareTo(new MocaValue(MocaType.STRING, "10")) > 0);
       Assert.assertTrue(new MocaValue(MocaType.DOUBLE, 2.1).compareTo(new MocaValue(MocaType.STRING, "10")) > 0);
       Assert.assertTrue(new MocaValue(MocaType.BOOLEAN, true).compareTo(new MocaValue(MocaType.STRING, "")) > 0);
    }
    
}
