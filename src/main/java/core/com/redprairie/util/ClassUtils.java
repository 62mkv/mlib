/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Utilities for instantiating classes.
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public final class ClassUtils {

    /**
     * Returns an instance of the named class, enforcing that it is an instance
     * of the given superclass.  The current thread's context classloader is
     * used to load the named class.  The named class is instantiated with it's
     * default (no-arg) public constructor.
     * 
     * @param name the fully-qualified name of the class to load.
     * @param superclass a Class object that represents either a class that
     * the named class must extend, or an interface that the named class must
     * implement.  If this argument is <code>null</code>, then no check is
     * performed.
     * @return a new instance of the named class.
     * @throws IllegalArgumentException if the named class is not found, if it
     * isn't an instance of the given superclass, or if there was some problem
     * instantiating the class.
     */
    public static <T> T instantiateClass(String name, Class<T> superclass) {
        return (T) instantiateClass(name, superclass, new Class<?>[0], new Object[0], null);
    }
    
    /**
     * Returns an instance of the named class, enforcing that it is an instance
     * of the given superclass.
     *   
     * @param name the fully-qualified name of the class to load.
     * @param superclass a Class object that represents either a class that
     * the named class must extend, or an interface that the named class must
     * implement.  If this argument is <code>null</code>, then no check is
     * performed.
     * @param constructorTypes the argument types of the constructor to use to
     * instantiate the named class.
     * @param constructorArgs the arguments to pass to the constructor.
     * @param loader The classloader to use.  If this argument is null, the
     * current thread's context classloader is used.
     * @return a new instance of the named class.
     * @throws IllegalArgumentException if the named class is not found, if it
     * isn't an instance of the given superclass, or if there was some problem
     * instantiating the class.
     */
    public static <T> T instantiateClass(String name, Class<T> superclass,
                                   Class<?>[] constructorTypes,
                                   Object[] constructorArgs,
                                   ClassLoader loader) {

        Class<T> cls = loadClass(name, superclass, loader);
        try {
            Constructor<T> constructor = cls.getConstructor(constructorTypes);
            return constructor.newInstance(constructorArgs);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("constructor not found on class " + name, e);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("cannot access constructor on class " + name, e);
        }
        catch (InstantiationException e) {
            throw new IllegalArgumentException("cannot instantiate class " + name, e);
        }
        catch (InvocationTargetException e) {
            throw new IllegalArgumentException("constructor on class " + name +
                                               " threw exception" + e.getCause(),
                                               e.getCause());
        }
    }
    
    /**
     * Returns a class object corresponding to the given fully-qualified class
     * name.  
     * @param name the fully-qualified name of the class to load.
     * @param superclass a Class object that represents either a class that
     * the named class must extend, or an interface that the named class must
     * implement.  If this argument is <code>null</code>, then no check is
     * performed.
     * @param loader The classloader to use.  If this argument is null, the
     * current thread's context classloader is used.
     * @return a Class object representing the named class.
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(String name, Class<T> superclass, ClassLoader loader) {
        if (loader == null) {
            loader = getClassLoader();
        }
        
        Class<?> cls;
        try {
            cls = loader.loadClass(name);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class " + name + " not found", e);
        }
        
        if (superclass != null) {
            if (!superclass.isAssignableFrom(cls)) {
                throw new IllegalArgumentException("class " + name +
                        " does not extend/implement " + superclass.getName());
            }
        }
        
        return (Class<T>)cls;
    }
    
    /**
     * Get all the public interfaces implemented by a class.
     * @param o an object.
     * @return an array of Class objects representing all the
     * public interfaces implemented directly by the class or
     * its superclasses.
     */
    public static Class<?>[] getInterfaces(Class<?> cls) {
        Collection<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            interfaces.addAll(Arrays.asList(c.getInterfaces()));
        }
        return interfaces.toArray(new Class[interfaces.size()]);
    }
    
    public static ClassLoader getClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back to this
            // class' classloader...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
        }
        return cl;
    }
    

    //
    // Implementation
    //
    
    // private constructor to prevent instantiation
    private ClassUtils() {
    }
}
