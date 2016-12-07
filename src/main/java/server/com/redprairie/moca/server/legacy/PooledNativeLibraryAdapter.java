package com.redprairie.moca.server.legacy;

import java.rmi.RemoteException;
import java.util.concurrent.atomic.AtomicInteger;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadMetrics;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaLibInfo;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaRuntimeException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.server.log.TraceState;
import com.redprairie.moca.server.log.TraceUtils;
import com.redprairie.moca.server.log.exceptions.LoggingException;
import com.redprairie.moca.util.MocaUtils;

/**
 * A NativeLibraryAdapter implementation that 
 */
public class PooledNativeLibraryAdapter implements NativeLibraryAdapter {
    
    public PooledNativeLibraryAdapter(NativeProcess process, 
        MocaServerAdapter serverAdapter) {
        _process = process;
        _serverAdapter = serverAdapter;
    }

    @Override
    public MocaResults callFunction(String libraryName, String functionName,
                                    MocaType[] argTypes, Object[] args,
                                    boolean simpleFunction) throws MocaException {
        boolean traceEnabled = traceEnabled();
        _executeCount.incrementAndGet();
        try {
            NativeReturnStruct retStruct = _process.callFunction(_serverAdapter, 
                    libraryName, functionName, argTypes, args, simpleFunction, 
                    traceEnabled);
            
            // If there was an error we have to change it into an actual
            // Java exception here
            if (retStruct.getErrorCode() != 0) {
                // Technically we should never be able to return this from a
                // command, but I guess we can allow for C to send an interrupt
                // like signal by returning a result set with matching error
                // code.
                if (retStruct.getErrorCode() == MocaInterruptedException.CODE) {
                    throw handleInterruption(new MocaInterruptedException(), 
                                             libraryName + "." + functionName);
                }
                throw new CommandInvocationException(
                        retStruct.getErrorCode(), retStruct.getMessage(), retStruct.isMessageResolved(),
                        castResults(retStruct.getResults()),
                        retStruct.getArgs());
            }
            
            return castResults(retStruct.getResults());
        }
        catch (MocaInterruptedException e) {
            throw handleInterruption(e, libraryName + "." + functionName);
        }
        catch (RemoteException e) {
            MocaContext ctx = MocaUtils.currentContext();
            String taskId = ctx.getSystemVariable("MOCA_TASK_ID");
            
            MadFactory mFact = MadMetrics.getFactory();
            if (taskId == null) {
                mFact.sendNotification("moca.native-process-crash", "C function " + libraryName + "." + functionName + " crashed");
            } else {
                mFact.sendNotification("moca.native-process-crash", "C function " + libraryName + "." + functionName + " crashed.  (Task: " + taskId + ")");
            }
            
            throw new MocaNativeCommunicationException("C function " + libraryName + "." + functionName, e);
        }
        finally {
            _executeCount.decrementAndGet();
        }
    }
    
    protected MocaRuntimeException handleInterruption(MocaRuntimeException e,
                                                      String called) {
        try {
            _process.close();
        }
        catch (RemoteException e1) {
            MocaNativeProcessPool._logger
                .debug("May have experienced problem shutting down native "
                        + "process after interrupt, it could be already down.");
        }
        return e;
    }
    
    /**
     * Determines if the native process should have tracing enabled.
     * @return Whether or not tracing has been determined to be enabled for
     *         this thread.
     */
    private static boolean traceEnabled() {
        boolean traceEnabled;
        try {
            TraceState state = TraceUtils.getTraceState();
            
            if (state.getTraceLevel() != null && 
                    !state.getTraceLevel().isEmpty()) {
                traceEnabled = true;
            }
            else {
                traceEnabled = false;
            }
        }
        catch (LoggingException e) {
            // If error then just assume no tracing
            traceEnabled = false;
        }
        
        // If the session trace is not enabled then check the global trace
        if (!traceEnabled) {
            if (TraceUtils.getGlobalTraceLevel() > 0) {
                traceEnabled = true;
            }
        }
        
        return traceEnabled;
    }
    
    // @see com.redprairie.moca.server.legacy.NativeLibraryAdapter#initializeLibrary(java.lang.String, java.lang.String)
    @Override
    public MocaLibInfo initializeLibrary(String libraryName) throws MocaException {
        try {
            return _process.initializeLibrary(libraryName);
        }
        catch (RemoteException e) {
            throw new MocaNativeCommunicationException("C library Init: " + libraryName, e);
        }
    }
    
    // @see com.redprairie.moca.server.legacy.NativeLibraryAdapter#initializeCOM(java.lang.String)
    @Override
    public MocaLibInfo initializeCOM(String progID) throws MocaException {
        try {
            return _process.initializeCOMLibrary(progID);
        }
        catch (RemoteException e) {
            throw new MocaNativeCommunicationException("COM library init: " + progID, e);
        }
    }
    
    // @see com.redprairie.moca.server.legacy.NativeLibraryAdapter#callCOMMethod(java.lang.String, java.lang.String, com.redprairie.moca.MocaType[], java.lang.Object[])
    
    @Override
    public MocaResults callCOMMethod(String progID, String methodName, MocaType[] argTypes, Object[] args)
            throws MocaException {
        boolean traceEnabled = traceEnabled();
        _executeCount.incrementAndGet();
        try {
            NativeReturnStruct retStruct = _process.callCOMMethod(_serverAdapter, 
                    progID, methodName, argTypes, args, traceEnabled);
            
            // If there was an error we have to change it into an actual
            // Java exception here
            if (retStruct.getErrorCode() != 0) {
                // Technically we should never be able to return this from a
                // command, but I guess we can allow for C to send an interrupt
                // like signal by returning a result set with matching error
                // code.
                if (retStruct.getErrorCode() == MocaInterruptedException.CODE) {
                    throw handleInterruption(new MocaInterruptedException(),
                        progID + "." + methodName);
                }
                throw new CommandInvocationException(
                        retStruct.getErrorCode(), retStruct.getMessage(), retStruct.isMessageResolved(), 
                        castResults(retStruct.getResults()), retStruct.getArgs());
            }
            
            return castResults(retStruct.getResults());
        }
        catch (MocaInterruptedException e) {
            throw handleInterruption(e, progID + "." + methodName);
        }
        catch (RemoteException e) {
            throw new MocaNativeCommunicationException("COM method " + progID + "." + methodName, e);
        }
        finally {
            _executeCount.decrementAndGet();
        }
    }

    @Override
    public void release() throws MocaException {
        if (_process != null) {
            try {
                _process.release(_serverAdapter);
            }
            catch (RemoteException e) {
            }
            
            try {
                _process.close();
            }
            catch (RemoteException e) {
                throw new MocaNativeCommunicationException("Error " +
                		"communicating with Native Process Pool", e);
            }
            _process = null;
        }
    }
    
    @Override
    public void preCommit() throws MocaException {
        try {
            _process.preCommit(_serverAdapter);
        }
        catch (RemoteException e) {
            throw new MocaNativeCommunicationException("pre-commit", e);
        }
    }
    
    @Override
    public void postTransaction(boolean commit) {
        try {
            _process.postTransaction(_serverAdapter, commit);
        }
        catch (RemoteException e) {
            MocaNativeProcessPool._logger.debug("Problem encountered while " +
                    "calling post transaction hooks on native process");
        }
    }
    
    @Override
    public boolean isKeepaliveSet() {
        try {
            return _process.isKeepaliveSet();
        }
        catch (RemoteException e) {
            MocaNativeProcessPool._logger.debug("Problem encountered while " +
                    "calling isKeepalive on native process");
        }
        return false;
    }
    
    // @see com.redprairie.moca.server.legacy.NativeLibraryAdapter#setEnvironmentVariable(java.lang.String, java.lang.String)
    @Override
    public void setEnvironmentVariable(String name, String value) {
        try {
            _process.setEnvironment(name, value);
        }
        catch (RemoteException e) {
            MocaNativeProcessPool._logger.debug("Problem encountered while " +
                    "calling setEnvironmentVariable on native process");
        }
    }
    
    // @see com.redprairie.moca.server.legacy.NativeLibraryAdapter#currentlyActive()
    @Override
    public boolean currentlyActive() {
        return _executeCount.get() > 0;
    }
    
    //
    // Implementation
    //
    private SimpleResults castResults(MocaResults res) {
        if (res == null || res instanceof SimpleResults) {
            return (SimpleResults) res;
        }
        else {
            SimpleResults out = new SimpleResults();
            MocaUtils.copyResults(out, res);
            res.close();
            return out;
        }
    }

    private AtomicInteger _executeCount = new AtomicInteger(0);
    private NativeProcess _process;
    private final MocaServerAdapter _serverAdapter;
}
