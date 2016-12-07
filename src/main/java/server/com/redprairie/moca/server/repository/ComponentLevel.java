/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

package com.redprairie.moca.server.repository;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaLibInfo;
import com.redprairie.moca.exceptions.UnexpectedException;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.legacy.NativeLibraryAdapter;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.ArgCheck;
import com.redprairie.util.ClassUtils;

/**
 *  Class to encapsulate the Level definition. The primary purpose of
 *  a level is to determine the order of lookup of a Command ( if a command
 *  if defined in multiple levels) and to associate Commands and Triggers with
 *  its sequence number.
 * <pre>
 * Copyright (c) 2008 RedPrairie Corporation
 * All Rights Reserved
 * </pre>
 *
 * </b>
 *
 * @author cjolly
 * @version $Revision$
 */

public class ComponentLevel implements Serializable {

    /**
     * Default constructor for a Level
     */
    public ComponentLevel(String name) {
        ArgCheck.notNull(name);
        _name = name;
    }

    /**
     *
     * @return the sortsequence for this level
     */
    public int getSortseq() {
        return _sortseq;
    }

    /**
     * Set the sort sequence for this Level
     * @param sortseq
     */
    public void setSortseq(int sortseq) {
        _sortseq = sortseq;
    }

    /**
     * Returns the name of this level.
     * @return the name of this Level
     */
    public String getName() {
        return _name;
    }

    /**
     * Get the description of this Level.
     * @return
     */
    public String getDescription() {
        return _description;
    }

    /**
     * Set the description of this level
     * @param description
     */
    public void setDescription(String description) {
        _description = description;
    }

    /**
     * get the library
     * @return
     */
    public String getLibrary() {
        return _library;
    }

    /**
     * Set the library
     * @param library
     */
    public void setLibrary(String library) {
        _library = library;
    }

    /**
     * return the program id
     * @return
     */
    public String getProgid() {
        return _progid;
    }

    /**
     * set the program id
     * @param progid
     */
    public void setProgid(String progid) {
        _progid = progid;
    }

    /**
     * return the package
     * @return
     */
    public String getPackage() {
        return _package;
    }

    /**
     * set the package name.
     *
     * @param pkg
     */
    public void setPackage(String pkg) {
        _package = pkg;
    }

    /**
     * get the Version.
     * @return version string.
     */
    synchronized
    public String getVersion() {
        if (_runtimeInfo != null) {
            return _runtimeInfo.getVersion();
        }
        return null;
    }
    
    /**
     * get the Version.
     * @return version string.
     */
    synchronized
    public String getProduct() {
        if (_runtimeInfo != null) {
            return _runtimeInfo.getProduct();
        }
        return null;
    }

    /**
     * if the Level is editable
     * @return boolean
     */
    public boolean isEditable() {
        return _editable;
    }

    /**
     * set the editable property of the Level
     * @param readonly
     */
    public void setEditable(boolean editable) {
        _editable = editable;
    }
    
    /**
     * return the command directory
     * @return
     */
    public String getCmdDir() {
        return _cmdDir;
    }

    /**
     * set the command directory.
     * @param cmdDir
     */
    public void setCmdDir(String cmdDir) {
    	_cmdDir = cmdDir;
    }
    
    /**
     * Initializes the MOCA library, throwing exceptions if initialization failed.
     * @param ctx
     * @throws MocaException
     */
    synchronized
    public void initialize(ServerContext ctx) throws MocaException {
        // Don't Initialize the C library, just grab the license and version information from the loaded library.
        // That information will have already been gathered when the library is first loaded.
        
        if (_library != null) {
            _logger.debug(MocaUtils.concat("Initializing C library ", _library));
            NativeLibraryAdapter nativeAdapter = ctx.getNativeLibraryAdapter();
            _runtimeInfo = nativeAdapter.initializeLibrary(_name);
            _logger.debug(MocaUtils.concat("Done initializing C library ", _library));
        }
        
        if (_progid != null) {
            _logger.debug(MocaUtils.concat("Initializing COM Library ", _progid));
            NativeLibraryAdapter nativeAdapter = ctx.getNativeLibraryAdapter();
            _runtimeInfo = nativeAdapter.initializeCOM(_name);
            _logger.debug(MocaUtils.concat("Done initializing COM library ", _progid));
        }
        
        //
        // Initialize the Java library
        //
        if (_package != null) {
            _logger.debug(MocaUtils.concat("Initializing Java package ", _package));
            Class<?> initClass = null;
            try {
                initClass = ClassUtils.loadClass(_package + ".ComponentLibrary", null, null);
            }
            catch (IllegalArgumentException e) {
                _logger.debug(MocaUtils.concat("Done initializing Java package ", 
                        _package, " - ComponentLibrary not present"));
            }
            if (initClass != null) {
                Method initMethod = null;
                try {
                    initMethod = initClass.getMethod("initialize", new Class[0]);
                }
                catch (NoSuchMethodException e1) {
                    _logger.debug(MocaUtils.concat("Done initializing Java package ", 
                            _package, " - initialize method on ComponentLibrary not present"));
                    // OK.  No better way to figure out if a method doesn't exist.
                }

            
                if (initMethod != null) {
                    try {
                        Object initInstance = initClass.newInstance();
                        _runtimeInfo = (MocaLibInfo) initMethod.invoke(initInstance, new Object[0]);
                    }
                    catch (InvocationTargetException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof MocaException) {
                            throw (MocaException) cause;
                        }
                        else {
                            throw new UnexpectedException(e); 
                        }
                    }
                    catch (IllegalAccessException e) {
                        throw new UnexpectedException(e); 
                    }
                    catch (InstantiationException e) {
                        throw new UnexpectedException(e);
                    }
                }
            }
            _logger.debug(MocaUtils.concat("Done initializing Java package ", _package));
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ComponentLevel)) {
            return false;
        }
        
        ComponentLevel other = (ComponentLevel)obj;
        
        if (!other._name.equals(_name)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        return _name.hashCode();
    }
    
    //
    // Implementation
    //
    private static final long serialVersionUID = 1L;
    private final String _name;
    private String _description;
    private String _library;
    private String _progid;
    private String _package;
    private String _cmdDir;
    private int _sortseq;
    private boolean _editable;
    private transient MocaLibInfo _runtimeInfo;
    private static transient Logger _logger = LogManager.getLogger(ComponentLevel.class);
}
