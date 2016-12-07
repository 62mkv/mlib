package com.redprairie.moca.server.dispatch;

import com.redprairie.moca.MocaResults;

/**
 * A class describing the results of the command dispatch request.  Most of the
 * way throughout the execution of incoming requests, an error condition is 
 * treated as the exception state of the thread. When dispatching results to
 * the caller, an exception needs to be treated as its component parts: result
 * status, result data set, and error message.
 */
public class DispatchResult {
    
    public DispatchResult(MocaResults results) {
        this(results, 0, null);
    }
    
    public DispatchResult(MocaResults results, int status, String message) {
        _results = results;
        _status = status;
        _message = message;
    }
    
    /**
     * Returns the results of the command execution. Results can exist on
     * error executions, as well as the result of normal executions.
     * 
     * @return the results of the command dispatch, or results attached to
     *         the exception object thrown from an aborted dispatch.
     */
    public MocaResults getResults() {
        return _results;
    }
    
    /**
     * Returns the execution status. Zero is considered to be a "normal"
     * execution status.
     * 
     * @return the execution status of the command dispatch.
     */
    public int getStatus() {
        return _status;
    }
    
    /**
     * Returns the error message. If an exception occurred upon execution,
     * the locale-specific error message is returned. This message is safe
     * to return to the caller.
     * 
     * @return the error message associated with the command dispatch. This
     *         can return <code>null</code> if no error occurred.
     */
    public String getMessage() {
        return _message;
    }
    
    //
    // Implementation
    //
    
    private final MocaResults _results;
    private final int _status;
    private final String _message;
}