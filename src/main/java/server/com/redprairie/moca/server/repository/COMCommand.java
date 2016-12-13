/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.redprairie.moca.server.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaRuntimeException;
import com.redprairie.moca.exceptions.UnexpectedException;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.legacy.NativeLibraryAdapter;
import com.redprairie.moca.util.MocaUtils;

/**
 * COMCommand - A subclass of Command with overrides to specialize this class to
 * a COMCommand.
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author cjolly
 * @version $Revision$
 */
public class COMCommand extends Command {
    /**
     * 
     */
    public COMCommand(String name, ComponentLevel level) {
        super(name, level);
        _method = null;
    }

    // @see com.redprairie.moca.server.repository.Command#getMethod()
    public String getMethod() {
        return _method;
    }

    // @see
    // com.redprairie.moca.server.repository.Command#setMethod(java.lang.String)
    public void setMethod(String method) {
        _method = method;
    }

    @Override
    public CommandType getType() {
        return CommandType.COM_METHOD;
    }

    // @see
    // com.redprairie.moca.server.repository.Command#execute(com.redprairie.moca.MocaContext)
    @Override    protected MocaResults executeWithContext(ServerContext ctx) throws MocaException {
        Object[] args = getArgs(ctx);
        NativeLibraryAdapter libAdapter = ctx.getNativeLibraryAdapter();
        try {
            _logger.debug(MocaUtils.concat("Calling COM method ", _method));
            MocaResults res = libAdapter.callCOMMethod(getLevel().getProgid(),
                _method, getArgTypes(), args);
            _logger.debug(MocaUtils.concat("COM method ", _method, " returned OK "));
            return res;
        }
        catch (MocaInterruptedException e) {
            _logger.debug(MocaUtils.concat("COM method ", _method, " returned status: ", e.getErrorCode()), e);
            throw e;
        }
        catch (MocaRuntimeException e) {
            _logger.debug(MocaUtils.concat("COM method ", _method, " returned status: ", e.getErrorCode()), e);
            throw new MocaException(e);
        }
        catch (MocaException e) {
            _logger.debug(MocaUtils.concat("COM method ", _method, " returned status: ", e.getErrorCode()), e);
            throw e;
        }
        catch (RuntimeException e) {
            _logger.debug(MocaUtils.concat("COM method ", _method, " threw exception: ", e), e);
            throw new UnexpectedException(e);
        }
    }

    //
    // Implementation
    //
    private String _method;
    private static transient Logger _logger = LogManager.getLogger(COMCommand.class);
    private static final long serialVersionUID = 3258090245118014887L;
}
