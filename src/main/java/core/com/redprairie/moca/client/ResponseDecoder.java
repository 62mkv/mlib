/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;

/**
 * This interface defines the methods required for a class to implement to be
 * able to decode a given response from a moca server.  An instance of any
 * implementing classes should not be created directly but retrieved from the
 * {@link ResponseDecoderFactory#getResponseDecoder(java.net.HttpURLConnection)}
 * method call.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 * @see ResponseDecoderFactory
 */
public interface ResponseDecoder {

    public MocaResults decodeResponse() throws MocaException, ProtocolException;

    public String getSessionId();

}