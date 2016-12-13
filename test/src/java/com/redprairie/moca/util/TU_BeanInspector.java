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

package com.redprairie.moca.util;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Unit test for <code>BeanInspector</code>
 *
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
public class TU_BeanInspector extends TestCase {
    public void testGetProperties() throws Exception {
        BeanInspector inspector = BeanInspector.inspect(_TestClass.class);
        BeanProperty[] properties = inspector.getProperties();
        assertEquals(3, properties.length);
        Arrays.sort(properties);
        assertEquals("doohicky", properties[0].getName());
        assertEquals("thingOne", properties[1].getName());
        assertEquals("thingTwo", properties[2].getName());
    }

    public void testGetPropertiesOnSubclass() throws Exception {
        BeanInspector inspector = BeanInspector.inspect(_TestSubclass.class);
        BeanProperty[] properties = inspector.getProperties();
        assertEquals(4, properties.length);
        Arrays.sort(properties);
        assertEquals("doohicky", properties[0].getName());
        assertEquals("thingOne", properties[1].getName());
        assertEquals("thingTwo", properties[2].getName());
        assertEquals("unknown", properties[3].getName());
    }

    public void testGetPropertiesOnSubclassReference() throws Exception {
        BeanInspector inspector = BeanInspector.inspect(new _TestSubclass());
        BeanProperty[] properties = inspector.getProperties();
        assertEquals(4, properties.length);
        Arrays.sort(properties);
        assertEquals("doohicky", properties[0].getName());
        assertEquals("thingOne", properties[1].getName());
        assertEquals("thingTwo", properties[2].getName());
        assertEquals("unknown", properties[3].getName());
    }
    
    public void  testGetProperty() throws Exception {
        _TestSubclass bean = new _TestSubclass();
        BeanInspector inspector = BeanInspector.inspect(bean.getClass());
        BeanProperty property = inspector.getProperty("doohicky");
        Object value = property.getValue(bean);
        Class<?> type = property.getType();
        assertEquals(Boolean.TRUE, value);
        assertEquals(Boolean.TYPE, type);
    }

    public void testGetPropertiesOnArray() throws Exception {
        _TestClass[] testArray = new _TestClass[0];
        BeanInspector inspector = BeanInspector.inspect(testArray);
        BeanProperty[] properties = inspector.getProperties();
        assertEquals(3, properties.length);
        Arrays.sort(properties);
        assertEquals("doohicky", properties[0].getName());
        assertEquals("thingOne", properties[1].getName());
        assertEquals("thingTwo", properties[2].getName());
    }

    public void testGetPropertiesOnSubclassArray() throws Exception {
        _TestClass[] testArray = new _TestSubclass[0];
        BeanInspector inspector = BeanInspector.inspect(testArray);
        BeanProperty[] properties = inspector.getProperties();
        assertEquals(4, properties.length);
        Arrays.sort(properties);
        assertEquals("doohicky", properties[0].getName());
        assertEquals("thingOne", properties[1].getName());
        assertEquals("thingTwo", properties[2].getName());
        assertEquals("unknown", properties[3].getName());
    }
    public void testGetPropertiesOnArrayWithSubclassElements() throws Exception {
        _TestClass[] testArray = new _TestClass[] {new _TestSubclass()};
        BeanInspector inspector = BeanInspector.inspect(testArray);
        BeanProperty[] properties = inspector.getProperties();
        assertEquals(3, properties.length);
        Arrays.sort(properties);
        assertEquals("doohicky", properties[0].getName());
        assertEquals("thingOne", properties[1].getName());
        assertEquals("thingTwo", properties[2].getName());
    }

    public void testSetProperties() throws Exception {
        BeanInspector inspector = BeanInspector.inspect(_TestSubclass2.class);
        BeanProperty[] properties = inspector.getProperties();
        assertEquals(4, properties.length);
        Arrays.sort(properties);
        assertEquals("doohicky", properties[0].getName());
        assertEquals("thingOne", properties[1].getName());
        assertEquals("thingTwo", properties[2].getName());
        assertEquals("unknown", properties[3].getName());
    }


}

class _TestClass {
    public String getThingOne() {
        return "Thing one";
    }
    
    public int getThingTwo() {
        return 2;
    }
    
    public boolean isDoohicky() {
        return false;
    }
}

class _TestSubclass extends _TestClass {
    public Object getUnknown() {
        return _unknown;
    }
    public boolean isDoohicky() {
        return _doohickey;
    }
    
    protected Object _unknown = "BLAH";
    protected boolean _doohickey = true;
}

class _TestSubclass2 extends _TestClass {
    public void setUnknown(Object unknown) {
        _unknown = unknown;
    }
    public void setDoohicky(boolean doohickey) {
        _doohickey = doohickey;
    }
    protected Object _unknown = "BLAH";
    protected boolean _doohickey = true;
}
