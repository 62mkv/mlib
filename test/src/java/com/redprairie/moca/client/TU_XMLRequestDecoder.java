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

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * TODO Class Description
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_XMLRequestDecoder {

    @Test
    public void testSimpleRequest() throws Exception {
        XMLRequestDecoder decoder = new XMLRequestDecoder(TU_XMLRequestDecoder.class.getResourceAsStream("resources/request01.xml"));
        decoder.decode();
        String query = decoder.getQuery();
        System.out.println(query);
        assertNotNull(query);
    }
}
