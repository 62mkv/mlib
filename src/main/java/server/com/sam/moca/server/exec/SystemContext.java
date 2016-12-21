/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.sam.moca.server.exec;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;

/**
 * Interface representing the overall MOCA system.  This contains configuration information, as well as
 * global facilities, such as default logging, monitoring, etc.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public interface SystemContext {
    public boolean isVariableMapped(String name);
    
    public String getVariable(String name);
    
    public String getConfigurationElement(String key);
    
    public String getConfigurationElement(String key, boolean expand);
    
    public String getConfigurationElement(String key, String defaultValue);
    
    public String getConfigurationElement(String key, String defaultValue, boolean expand);
    
    public void overrideConfigurationElement(String key, String value);

    Map<String, String> getConfigurationSection(String section, boolean expand);
    
    /**
     * This will return the first file found from the filter checking the
     * data directories in the inverse order of how they are listed.
     * 
     * That is the first file in a data directory that passes the 
     * filter starting from the last data directory to the first.
     * @param filter The filter to find the file based off of
     * @return The first file found or null if no file is found
     * @throws IllegalArgumentException This is thrown if the prod-dirs registry
     *         setting is not defined.
     */
    public File getDataFile(FilenameFilter filter) throws IllegalArgumentException;
    
    /**
     * This will return the first file found from the filter checking the
     * data directories in the inverse order of how they are listed.
     * 
     * That is the first file in a data directory that passes the 
     * filter starting from the last data directory to the first.
     * 
     * This is similar to {@link SystemContext#getDataFile(FilenameFilter)}
     * except that you can optionally specify whether or not you want the 
     * directories searched in the reverse order.
     * @param filter The filter to find the file based off of
     * @return The first file found or null if no file is found
     * @param reverseOrder Whether or not to return the files in inverse order
     * @throws IllegalArgumentException This is thrown if the prod-dirs registry
     *         setting is not defined.
     */
    public File getDataFile(FilenameFilter filter, boolean reverseOrder) throws IllegalArgumentException;
    
    /**
     * This will return all files contained in the data directories.
     * These files will be in order in which they are found in.  That is it
     * will take each data directory in the order of the path setting
     * finding all files in each and then returned in the order in which 
     * {@link File#listFiles(FilenameFilter)} returns them.
     * @see File#listFiles(FilenameFilter)
     * @param filter The filter to base the files off of
     * @return The files found.  This array will be empty if no files are found
     * @throws IllegalArgumentException This is thrown if the prod-dirs registry
     *         setting is not defined.
     */
    public File[] getDataFiles(FilenameFilter filter) throws IllegalArgumentException;
    
    /**
     * This will return all files contained in the data directories.
     * This is similar to {@link SystemContext#getDataFiles(FilenameFilter)}
     * except that you can optionally specify whether or not you want the files
     * in reverse order that they are normally returned.
     * @see File#listFiles(FilenameFilter)
     * @param filter The filter to base the files off of
     * @param reverseOrder Whether or not to return the files in inverse order
     * @return The files found.  This array will be empty if no files are found
     * @throws IllegalArgumentException This is thrown if the prod-dirs registry
     *         setting is not defined.
     */
    public File[] getDataFiles(FilenameFilter filter, boolean reverseOrder) throws IllegalArgumentException;
    
    /**
     * Returns a system attribute.  These are arbitrary objects, stored in the
     * system context by the framework.  Generally, these should be used to
     * hold configuration information.
     * @param name the name of the attribute.
     * @return the value for the named attribute, or null if the attribute is
     * not present in the system context.
     */
    public Object getAttribute(String name);
    
    /**
     * Adds a system attribute.  These are arbitrary objects, stored in the
     * system context by the framework.  Generally, these should be used to
     * hold configuration information.  If the attribute is already in the
     * context, its value is overwritten with the new value.
     * @param name the name of the attribute
     * @param value the value for the attribute.
     */
    public void putAttribute(String name, Object value);
   
    /**
     * Removes a system attribute.  These are arbitrary objects, stored in the
     * system context by the framework.  Generally, these should be used to
     * hold configuration information.
     * @param name the name of the attribute.  If the named attribute is
     * present, its value is returned.
     * @return the former value of the attribute, or <code>null</code> if the
     * attribute is not set in this context.
     */
    public Object removeAttribute(String name);
    
    /**
     * Normally this method should not be called except when you know that this
     * SystemContext will be eligible for garbage collection.  This is because
     * a SystemContext will usually have circular dependencies and this is to
     * clean those up.
     */
    public void clearAttributes();

    /**
     * Provides a string representation of the registry.
     * @return a string representation of the registry as it exists in 
     * memory.  This is _not_ a string representation of the raw registry file
     * on disk.
     */
    public String toString();
}
