/**
 * Exception thrown when 
 */
package com.redprairie.moca.server.exec;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;

public class ParallelExecutionException extends MocaException {
    private static final long serialVersionUID = -4443856103110530558L;

    public ParallelExecutionException(int code, MocaResults allResults) {
        super(code, "Parallel Command Failure");
        setResults(allResults);
    }
}
