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

package com.redprairie.moca.client;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.redprairie.moca.MocaResults;

/**
 * Tests that ResultsEncoder and ResultsDecoder work together in all legal
 * cases.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2005 RedPrairie Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_XMLResultsEncoder extends TU_AbstractResultsEncoder {

    protected MocaResults _makeCopy(MocaResults res) throws Exception {
        PipedOutputStream sink = new PipedOutputStream();
        final PipedInputStream source = new PipedInputStream(sink);
        OutputStreamWriter out = new OutputStreamWriter(sink, "UTF-8");

        Future<MocaResults> thread = Executors.newSingleThreadExecutor().submit(
            new Callable<MocaResults>() {
                public MocaResults call() throws Exception {
                    return new XMLResultsDecoder(source).decode();
                }
            });

        // write the results to our side of the pipe
        XMLResultsEncoder.writeResults(res, out);
        
        try {
            out.close();
        }
        catch (IOException e) {
            // Ignore
        }

        // Now wait until the other thread is done reading.
        return thread.get();
    }

}
