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

import java.io.PipedReader;
import java.io.PipedWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;


/**
 * This class tests to make sure that json encoder decoder can be used together.
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class TU_JSONResultsEncoder extends TU_AbstractResultsEncoder {

    // @see com.redprairie.moca.client.TU_AbstractResultsEncoder#_makeCopy(com.redprairie.moca.MocaResults)
    @Override
    protected MocaResults _makeCopy(MocaResults res) throws Exception {
        PipedWriter sink = new PipedWriter();
        final PipedReader source = new PipedReader(sink);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        try{
            Future<MocaResults> thread = executor.submit(
                new Callable<MocaResults>() {
                    public MocaResults call() throws Exception {
                        return JSONResultsDecoder.decode(source);
                    }
                });

            // write the results to our side of the pipe
            JSONResultsEncoder.writeResults(res, sink);

            // Now wait until the other thread is done reading.
            return thread.get();
        } finally {
            executor.shutdown();
        }
    }
    
    public void testControlCharacterInString() throws Exception {
        EditableResults res = new SimpleResults();
        res.addColumn("StringWithControl", MocaType.STRING);
        res.addRow();
        
        String value = "something";
        Charset charset = Charset.forName("UTF-8");
        
        byte[] bytes = value.getBytes(charset);
        
        byte[] myBytes = new byte[bytes.length * 3 + 4];
        
        System.arraycopy(bytes, 0, myBytes, 0, bytes.length);
        myBytes[bytes.length] = 0x13;
        myBytes[bytes.length + 1] = 0x10;
        System.arraycopy(bytes, 0, myBytes, bytes.length + 2, bytes.length);
        myBytes[bytes.length * 2 + 2] = 0x13;
        myBytes[bytes.length * 2 + 3] = 0x10;
        System.arraycopy(bytes, 0, myBytes, bytes.length * 2 + 4, bytes.length);
        // Here we put the string value with the control characters in the
        // result set so we can encode/decode it.
        res.setStringValue(0, charset.decode(ByteBuffer.wrap(myBytes)).toString());
        _compareResults(res, _makeCopy(res));
    }
    
    public void testConcurrencyOfDates() throws InterruptedException, ExecutionException {
        
        ExecutorService service = Executors.newFixedThreadPool(20);
        CompletionService<Void> completion = new ExecutorCompletionService<Void>(service);
        try {
            int amount = 500;
            for (int i = 0; i < amount; ++i) {
                completion.submit(new Callable<Void>() {

                    @Override
                    public Void call() throws Exception {
                        final EditableResults res = new SimpleResults();
                        res.addColumn("Date1", MocaType.DATETIME);
                        res.addColumn("Date2", MocaType.DATETIME);
                        res.addColumn("Date3", MocaType.DATETIME);
                        res.addColumn("Date4", MocaType.DATETIME);
                        res.addColumn("Date5", MocaType.DATETIME);
                        res.addRow();
                        
                        res.setDateValue(0, new Date());
                        res.setDateValue(1, new Date());
                        res.setDateValue(2, new Date());
                        res.setDateValue(3, new Date());
                        res.setDateValue(4, new Date());
                        
                        _compareResults(res, _makeCopy(res));
                        
                        return null;
                    }
                    
                });
            }
            
            for (int i = 0; i < amount; ++i) {
                completion.take().get();
            }
            
        }
        finally {
            service.shutdown();
        }
    }
}
