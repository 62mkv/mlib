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

package com.redprairie.util;

/**
 * A class to facilitate argument checking.  The intended usage of this class
 * is to check method preconditions in the form of expected argument values.
 * If an argument precondition fails, <code>IllegalArgumentException</code>, a
 * runtime exception is thrown.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public class ArgCheck {

    /**
     * Tests whether the specified argument is null.  A default
     * message is used if an exception is thrown.
     * 
     * @param arg the argument to test
     * @throws IllegalArgumentException if the argument is null
     */
    public static void notNull(Object arg) {
        notNull(arg, "argument is null");
    }
    
    /**
     * Tests whether the specified argument is null.
     * 
     * @param arg the argument to test
     * @param msg the message to include in any thrown exception.
     * @throws IllegalArgumentException if the argument is null
     */
    public static void notNull(Object arg, String msg) {
        if (arg == null) fail(msg);
    }
    
    /**
     * Tests whether the specified condition is true. A default
     * message is used if an exception is thrown.
     * 
     * @param condition the condition to evaluate
     * @throws IllegalArgumentException if the condition is false.
     */
    public static void isTrue(boolean condition) {
        isTrue(condition, "condition is false");
    }
    
    /**
     * Tests whether the specified condition is true.
     * 
     * @param condition the condition to evaluate
     * @param msg the message to include in any thrown exception.
     * @throws IllegalArgumentException if the condition is false.
     */
    public static void isTrue(boolean condition, String msg) {
        if (!condition) fail(msg);
    }
    
    /**
     * Tests whether the specified condition is false. A default
     * message is used if an exception is thrown.
     * 
     * @param condition the condition to evaluate
     * @throws IllegalArgumentException if the condition is true.
     */
    public static void isFalse(boolean condition) {
        isFalse(condition, "condition is true");
    }
    
    /**
     * Tests whether the specified condition is false.
     * 
     * @param condition the condition to evaluate
     * @param msg the message to include in any thrown exception.
     * @throws IllegalArgumentException if the condition is true.
     */
    public static void isFalse(boolean condition, String msg) {
        if (condition) fail(msg);
    }
    
    /**
     * Unconditionally throws an exception.
     * 
     * @param msg the message to include in the thrown exception.
     * @throws IllegalArgumentException unconditionally when this method is
     * called.
     */
    public static void fail(String msg) {
        throw new IllegalArgumentException(msg);
    }
    
    /**
     * Checks if the given String value is a boolean representation which means it
     * must be "true" or "false" (case insensitive) and returns the boolean it evaluated to.
     * If any other value is provided an IllegalArgumentException is thrown. This is an
     * alternative to Boolean.valueOf or Boolean.parseBoolean as those methods
     * will return false for invalid values such as "foo".
     * @param value The String value to check if it's a boolean representation
     * @param msg The failure message
     * @return The boolean the String evaluated to
     */
    public static boolean toBoolean(String value, String msg) {
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        else if ("false".equalsIgnoreCase(value)){
            return false;
        }
        else {
            throw new IllegalArgumentException(msg);
        }
    }
    
    //
    // Implementation
    //
    
    // Private constructor to avoid instantiation
    private ArgCheck() {
    }
}
