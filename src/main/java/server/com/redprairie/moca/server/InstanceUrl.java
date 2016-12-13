/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Class that describes a valid MOCA instance url
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class InstanceUrl implements Serializable {
    
    private static final long serialVersionUID = 8833550951416512915L;
    
    /**
     * This method will return an instance url given the provided character
     * sequence.  This assumes the character sequence is in the following format:
     * <p>
     * http(s)://&lt;hostname&gt;(:&lt;port&gt;)(/&lt;endpoint&gt;)
     * <p>
     * Parts in parenthesis are optional.  Parts in 
     * @param charSequence
     * @return
     * @throws MalformedURLException 
     */
    public static InstanceUrl fromChars(String charSequence) {
        URL uri;
        try {
            uri = new URL(charSequence);
        }
        catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
        String protocol = uri.getProtocol();
        boolean ssl;
        if (protocol.equals("http")) {
            ssl = false;
        }
        else if (protocol.equals("https")) {
            ssl = true;
        }
        else {
            throw new IllegalArgumentException(
                "Protocols supported are only http and https, got " + protocol);
        }
        String hostName = uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            port = 80;
        }
        
        String path = uri.getPath();
        InstanceUrl url;
        // Path can be just / which we want to ignore
        if (path != null && path.length() > 1) {
            url = new InstanceUrl(ssl, hostName, port, path.substring(1));
        }
        else {
            url = new InstanceUrl(ssl, hostName, port);
        }
        
        return url;
    }
    
    /**
     * @param sslProtocol
     * @param _hostName
     * @param _port
     * @param _endPoint
     */
    public InstanceUrl(boolean sslProtocol, String hostName, int port,
            String endPoint) {
        this._sslProtocol = sslProtocol;
        this._hostName = hostName;
        this._port = port;
        this._endPoint = endPoint;
    }
    
    /**
     * @param sslProtocol
     * @param _hostName
     * @param _port
     */
    public InstanceUrl(boolean sslProtocol, String hostName, int port) {
        this._sslProtocol = sslProtocol;
        this._hostName = hostName;
        this._port = port;
        this._endPoint = null;
    }
    
    /**
     * @return Returns the sslProtocol.
     */
    public boolean isSslProtocol() {
        return _sslProtocol;
    }

    /**
     * @return Returns the hostName.
     */
    public String getHostName() {
        return _hostName;
    }

    /**
     * @return Returns the port.
     */
    public int getPort() {
        return _port;
    }

    /**
     * @return Returns the endPoint.
     */
    public String getEndPoint() {
        return _endPoint;
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        if (_endPoint == null)
            return (_sslProtocol ? "https://" : "http://") + _hostName + ":" + _port;
        else
            return (_sslProtocol ? "https://" : "http://") + _hostName + ":" + _port + "/" + _endPoint;
    } 
    
    // @see java.lang.Object#hashCode()   
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_endPoint == null) ? 0 : _endPoint.hashCode());
        result = prime * result + ((_hostName == null) ? 0 : _hostName.hashCode());
        result = prime * result + _port;
        result = prime * result + (_sslProtocol ? 1231 : 1237);
        
        return result;
    }

    // @see java.lang.Object#equals(java.lang.Object)  
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        InstanceUrl other = (InstanceUrl) obj;
        
        if (_endPoint == null) {
            if (other._endPoint != null) return false;
        }
        else {
            if (!_endPoint.equals(other._endPoint)) return false;
        }     
        if (_hostName == null) {
            if (other._hostName != null) return false;
        }
        else {
            if (!_hostName.equals(other._hostName)) return false;
        }
        if (_port != other._port) return false;
        if (_sslProtocol != other._sslProtocol) return false;
        
        return true;
    }

    private final boolean _sslProtocol;
    private final String _hostName;
    private final int _port;
    private final String _endPoint;
}
