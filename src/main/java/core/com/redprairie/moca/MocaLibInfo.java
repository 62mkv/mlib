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

package com.redprairie.moca;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.Serializable;
import java.util.Properties;

/**
 * Class that holds library information for MOCA component libraries.  An
 * instance of this class is returned from a component library initialization
 * method. 
 * 
 * <b><pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MocaLibInfo implements Serializable {
    private static final long serialVersionUID = -8381178677774087809L;
    /**
     * Creates an instance of this class passing a version and product name.
     * The version and product information is displayed by the "list library
     * versions" command.
     * @param version a version string.
     * @param product a product string.  This is typically the name of
     * your product.
     * This argument can be <code>null</code>.
     */
    public MocaLibInfo(String version, String product) {
        _version = version;
        _product = product;
    }
    
    /**
     * Creates an instance of this class passing product name. The
     * version information is derived from a properties file
     * <code>build.properties</code>located in the sub-package
     * <code>resources</code>. Subclasses of this class should have keep
     * version information in that file, under the property
     * <code>releaseVersion</code>.
     * 
     * @param product
     *            a product string. This is typically the name of
     *            your product. This argument can be <code>null</code>.
     */
    protected MocaLibInfo(String product) {
        _version = getReleaseVersion();
        _product = product;
    }
    
    /**
     * @return the product.
     */
    public String getProduct() {
        return _product;
    }

    /**
     * @return the version.
     */
    public String getVersion() {
        return _version;
    }
    
    //
    // Implementation
    //
    
    private String getReleaseVersion() {
        
        InputStream in = getClass().getResourceAsStream("resources/build.properties");

        Properties buildProperties = new Properties();
        if (in != null) {
            try {
                buildProperties.load(in);
            }
            catch (InterruptedIOException e) {
                throw new MocaInterruptedException(e);
            }
            catch (IOException e) {
                // Use default if unable to load properties
            }
            finally {
                try { in.close(); } catch (IOException ignore) { }
            }
        }
        return buildProperties.getProperty("releaseVersion");
    }

    private final String _version;
    private final String _product;
}
