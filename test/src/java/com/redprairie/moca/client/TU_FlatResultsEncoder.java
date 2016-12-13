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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.redprairie.moca.MocaResults;

/**
 * Tests that ResultsEncoder and ResultsDecoder work together in all legal
 * cases.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_FlatResultsEncoder extends TU_AbstractResultsEncoder {

    public void testNullResults()  throws Exception {
        _compareResults(null, _makeCopy(null));
    }
    

    protected MocaResults _makeCopy(MocaResults res) throws IOException, ProtocolException {
        byte[] encoded = FlatResultsEncoder.encodeResults(res, null, null);
        // ResultsEncoder exhausts the result set, so we need to reset it.
        if (res != null) res.reset();
        MocaResults out = new FlatResultsDecoder(new ByteArrayInputStream(encoded), null).decode();
        return out;
    }
    
}
