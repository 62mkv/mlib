/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.server.legacy.socket;

import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.server.legacy.RemoteNativeProcess;
import com.redprairie.moca.util.NonMocaDaemonThreadFactory;

public class RemoteNativeProcessWrapper extends LocalNativeProcessWrapper {
    
    public RemoteNativeProcessWrapper(RemoteNativeProcess process, Process actualProcess,
            String procId, Socket crashSocket) {
        super(process, procId);
        _actualProcess = actualProcess;
        _crashSocket = crashSocket;
    }

    // @see com.redprairie.moca.server.legacy.NativeProcess#close()
    @Override
    public void close() throws RemoteException {
        ExecutorService service = Executors.newSingleThreadExecutor(
                new NonMocaDaemonThreadFactory("Native Shutdown Thread", false));
        
        try {
            // Closing this should cause the native process to go down nicely
            _crashSocket.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        Future<Void> future = service.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                _actualProcess.waitFor();
                return null;
            }
            
        });
        
        try {
            // If no exception is thrown we don't have to destroy the
            // process, since it was successful.
            future.get(3, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
            _logger.debug("Interruption while waiting for native process to shutdown, taking it down");
            _actualProcess.destroy();
            throw new MocaInterruptedException(e);
        }
        catch (ExecutionException e) {
            _logger.debug("Exception while waiting for native process to shutdown, taking it down", e.getCause());
            _actualProcess.destroy();
            Throwable cause = e.getCause();
            if (cause instanceof RemoteException) {
                throw (RemoteException)cause;
            }
            else if (cause instanceof Error) {
                throw (Error)cause;
            }
            else {
                // If any other problem occurred, we know it wasn't declared
                // so convert it to a runtime exception
                throw new RuntimeException(cause);
            }
        }
        catch (TimeoutException e) {
            _logger.debug("Timeout while waiting for native process to shutdown, taking it down");
            _actualProcess.destroy();
        }
        finally {
            service.shutdownNow();
        }
    }

    protected final Process _actualProcess;
    protected final Socket _crashSocket;
}
