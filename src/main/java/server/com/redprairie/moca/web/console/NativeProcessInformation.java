/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.web.console;

import java.util.Collection;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.server.legacy.NativeProcess;
import com.redprairie.moca.server.legacy.NativeProcessPool;
import com.redprairie.moca.server.profile.CommandPath;

/**
 * Get information regarding the current native processes.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class NativeProcessInformation {
    
    public NativeProcessInformation(NativeProcessPool pool) throws ConsoleException {
        // Ensure that we were given a pool.
        if (pool == null) {
            throw new ConsoleException("A valid native process pool was not provided.  Please report this as a bug.");
        }
        
        _pool = pool;
    }
    
    public MocaResults getNativeProcesses() {
        EditableResults res = new SimpleResults();
        
        res.addColumn("moca_prc", MocaType.STRING);
        res.addColumn("created_dt", MocaType.DATETIME);
        res.addColumn("requests", MocaType.INTEGER);
        res.addColumn("last_call", MocaType.STRING);
        res.addColumn("last_call_dt", MocaType.DATETIME);
        // This has to be a string, since we have no long type
        res.addColumn("thread_id", MocaType.STRING);
        res.addColumn("command_path", MocaType.STRING);
        res.addColumn("temp", MocaType.BOOLEAN);

        Collection<NativeProcess> allProcesses = _pool.getAllProcesses();
        
        addNativeProcessesToResults(res, allProcesses, false);
        
        Collection<NativeProcess> temporaryProcesses = _pool.getTemporaryProcesses();
        addNativeProcessesToResults(res, temporaryProcesses, true);
        
        return res;
    }
    
    private void addNativeProcessesToResults(EditableResults res, 
        Iterable<? extends NativeProcess> procs, boolean orphan) {
        for (NativeProcess process : procs) {
            String id = process.getId();
            res.addRow();
            res.setStringValue(0, id);
            res.setDateValue(1, process.dateCreated());
            Integer count = _pool.timesTaken(process);
            res.setIntValue(2, count == null ? 0 : count);
            res.setStringValue(3, process.lastCall());
            res.setDateValue(4, process.lastCallDate());
            Thread thread = process.getAssociatedThread();
            if (thread != null) {
                res.setStringValue(5, Long.toString(thread.getId()));
            }
            CommandPath path = process.getLastCommandPath();
            if (path != null) {
                res.setStringValue(6, path.toString());
            }
            res.setBooleanValue(7, orphan);
        }
    }
    
    public void stopNativeProcess(String mocaProcess) {
        Collection<NativeProcess> allProcesses = _pool.getAllProcesses();
        for (NativeProcess process : allProcesses) {
            boolean matches = process.getId().equals(mocaProcess);
            if (matches) {
                ConsoleModel._logger.info("Native Process [" + mocaProcess + 
                        "] is being shutdown by the console.");
                _pool.shutdownProcess(process);
                break;
            }
        }
    }

    private final NativeProcessPool _pool;
}
