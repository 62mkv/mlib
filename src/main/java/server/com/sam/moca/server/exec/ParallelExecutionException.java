/**
 * Exception thrown when 
 */
package com.sam.moca.server.exec;

import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;

public class ParallelExecutionException extends MocaException {
    private static final long serialVersionUID = -4443856103110530558L;

    public ParallelExecutionException(int code, MocaResults allResults) {
        super(code, "Parallel Command Failure");
        setResults(allResults);
    }
}
