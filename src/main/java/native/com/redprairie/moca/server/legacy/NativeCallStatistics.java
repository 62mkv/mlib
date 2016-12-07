/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.server.legacy;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.util.DaemonThreadFactory;

/**
 * Utility performance logging for native process statistics
 * 
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author dinksett
 */
public class NativeCallStatistics {
    public NativeCallStatistics(Writer out) {
        _out = out;
    }
    
    synchronized
    public void logCall(String type, String detail, long start, long end) {
        _Data d = new _Data();
        d.type = type;
        d.detail = detail;
        d.time = (end - start);
        _calls.add(d);
        if (_calls.size() > 1000) {
            dumpLog(null);
        }
    }
    
    synchronized
    public void dumpLog(final String message) {
        final Collection<_Data> calls = _calls;
        _calls = new ArrayList<_Data>();
        
        _logWriter.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (_Data d : calls) {
                        _out.append(d.type);
                        _out.append('|');
                        _out.append(d.detail);
                        _out.append('|');
                        _out.append(String.valueOf(d.time));
                        _out.append('\n');
                    }
                    
                    if (message != null) {
                        _out.append(message);
                        _out.append("\n");
                    }
                    _out.flush();
                }
                catch (InterruptedIOException e) {
                    throw new MocaInterruptedException(e);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private static class _Data {
        private String type;
        private String detail;
        private long time;
    }
    
    private Collection<_Data> _calls = new ArrayList<_Data>();
    private final Writer _out;
    private ExecutorService _logWriter = Executors.newSingleThreadScheduledExecutor(
        new DaemonThreadFactory("NativeLogWriter", false));
}
