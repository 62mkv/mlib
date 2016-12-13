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

import java.util.Comparator;

/**
 * An instance of java.util.Comparator for use in sorting arrays and
 * Collections of objects that conform to the JavaBeans conventions for 
 * properties.  When the <code>compare</code> method is called, the first
 * property named in the constructor is compared, in order, by calling the
 * appropriate getter method on each object, then calling <code>compare</code>
 * on that object.  If that comparison does not compare equal, then the result
 * of that comparison is returned immediately.  Otherwise, the next property is
 * inspected. 
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
public class BeanComparator<T> implements Comparator<T> {
    
    /**
     * Creates an instance of this class that will compare the named
     * properties.  Each property must return a value that is an instance
     * of Comparable.
     * @param fields the set of properties to be used in bean comparison. 
     */
    public BeanComparator(String[] fields) {
        this(fields, new Comparator[fields.length]);
    }

    /**
     * Creates an instance of this class that will compare the named
     * properties.  Each field is compared with the corresponding comparator
     * in <code>fieldComparators</code>.  Both arrays must be of the same
     * length.
     * @param fields the set of properties to be used in bean comparison.
     * @param fieldComparators instances of <code>Comparator</code> to be used
     * for the specific field comparison. If a comparator for a field is
     * <code>null</code>, then the field is assumed to be of a type that
     * implements Comparable.  If a field does not implement
     * <code>Comparable</code>, and no comparator is given, the field is
     * silently ignored.
     */
    public BeanComparator(String[] fields, Comparator<?>[] fieldComparators) {
        _fields = (String[])fields.clone();
        _comparators = (Comparator<?>[])fieldComparators.clone();
    }

    // javadoc inherited from superclass/interface
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int compare(T o1, T o2) {
        // Let's assume that these objects are of the same class.
        BeanInspector inspector = BeanInspector.inspect(o1);
        
        for (int i = 0; i < _fields.length; i++) {
            Object field1 = null;
            Object field2 = null;
            try {
                BeanProperty property = inspector.getProperty(_fields[i]) ;
                field1 = property.getValue(o1);
                field2 = property.getValue(o2);
            }
            catch (BeanPropertyException e) {
                // An exception reading a bean property means we treat the
                // property as null
            }
            catch (IllegalArgumentException e){
                // An exception reading a bean property means we treat the
                // property as null
            }
            
            int result = 0;
            if (_comparators[i] != null) {
                result = _comparators[i].compare(field1, field2);
            }
            else if (field1 instanceof Comparable) {
                result = ((Comparable)field1).compareTo(field2);
            }
            if (result != 0) return result;
        }
        return 0;
    }
    
    //
    // Implementation
    //
    private final String[] _fields;
    
    @SuppressWarnings("rawtypes")
    private final Comparator[] _comparators;
}
