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

package com.redprairie.moca.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.NotFoundException;

/**
 * A class to decode an XML document into a MocaResults object.  This class can interpret
 * both moca-results XML and moca-response XML.  The moca-response XML contains exception
 * information as well as session ID information in addition to the results objects.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class XMLResultsDecoder implements ResponseDecoder {
    
    public XMLResultsDecoder(InputStream in) {
        _in = new InputSource(in);
    }
    
    public XMLResultsDecoder(Reader in) {
        _in = new InputSource(in);
    }
    
    /**
     * Decodes the XML results into a MocaResults object.  If the incoming XML
     * stream represents a moca-response XML (i.e. it includes status code and
     * error message information), those fields will be ignored, and the returned
     * results may be empty or <code>null</code>, depending on the status returned.
     * @return the MocaResults object represented by the XML stream.
     * @throws ProtocolException
     */
    public MocaResults decode() throws ProtocolException {
        XMLResultsHandler handler = new XMLResultsHandler();
        try {
            SAXParser parser = factory.newSAXParser();
            factory.setValidating(false);
            parser.parse(_in, handler);
            
            MocaResults results = handler.getResults();

            return results;
        }
        catch (SAXException e) {
            throw new ProtocolException("Error Interpreting XML Results", e);
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new ProtocolException("Error Reading XML Results", e);
        }
        catch (ParserConfigurationException e) {
            throw new ProtocolException("Error Loading XML Parser", e);
        }

    }
    
    /**
     * Decodes the XML results into a MocaResults object.  If the incoming XML
     * stream represents a moca-response XML (i.e. it includes status code and
     * error message information), those fields will be turned into an instance
     * of <code>MocaException</code> and thrown from this method.  Otherwise, a
     * <code>MocaResults</code> object will be returned.
     * @return the MocaResults object represented by the XML stream.
     * @throws ProtocolException
     */
    public MocaResults decodeResponse() throws MocaException, ProtocolException {
        XMLResultsHandler handler = new XMLResultsHandler();
        try {
            SAXParser saxParser = factory.newSAXParser();
            factory.setValidating(true);
            saxParser.parse(_in, handler);
            
            int statusCode = handler.getStatus();
            String message = handler.getMessage();
            MocaResults results = handler.getResults();
            _sessionID = handler.getSessionId();
            
            if (statusCode == 0) {
                return results;
            }
            else {
                if (message == null) {
                    message = "";
                }
                
                if (statusCode == NotFoundException.DB_CODE || statusCode == NotFoundException.SERVER_CODE) {
                    throw new NotFoundException(statusCode, results);
                }
                else {
                    throw new ServerExecutionException(statusCode, message, results);
                }
            }
        }
        catch (SAXException e) {
            throw new ProtocolException("Error Interpreting XML Response", e);
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new ProtocolException("Error Reading XML Response", e);
        }
        catch (ParserConfigurationException e) {
            throw new ProtocolException("Error Loading XML Parser", e);
        }

    }
    
    /**
     * Returns the session ID seen when parsing the response.  This may return <code>null</code>
     * if no session ID was returned from the <code>decodeResponse</code> method, or if the
     * <code>decodeResponse</code> method was never called.
     * 
     * @return Returns the sessionID.
     */
    public String getSessionId() {
        return _sessionID;
    }
    
    //
    // Implementation
    //
    private final static SAXParserFactory factory = SAXParserFactory.newInstance();
    
    private final InputSource _in;
    private String _sessionID;
}
