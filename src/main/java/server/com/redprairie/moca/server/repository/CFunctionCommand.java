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
 * CFunctionCommand
 * A Subclass of the abstract Command class, this
 * class overrides methods to customize it for 
 * commands of the C-Function Type.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author cjolly
 * @version $Revision$
 */
public class CFunctionCommand extends Command {
    /**
     * 
     */
    public CFunctionCommand(String name, ComponentLevel level, CommandType type) {
        super(name, level);
        _type = type;
        
    }
    
    // @see com.redprairie.moca.server.repository.Command#getFunction()
    public String getFunction() {
         return _function;
    }

    // @see com.redprairie.moca.server.repository.Command#setFunction(java.lang.String)
    public void setFunction(String function) {
        this._function = function;
    }
    
    @Override
    public CommandType getType() {
        return _type;
    }
    
    // @see
    // com.redprairie.moca.server.repository.Command#execute(com.redprairie.moca.MocaContext)
    @Override
    protected MocaResults executeWithContext(ServerContext ctx) throws MocaException {
        Object[] args = getArgs(ctx);
        NativeLibraryAdapter libAdapter = ctx.getNativeLibraryAdapter();
        try {
            _logger.debug(MocaUtils.concat("Calling C function ", _function));
            MocaResults res = libAdapter.callFunction(getLevel().getName(),
                _function, getArgTypes(), args, 
                _type == CommandType.SIMPLE_C_FUNCTION);
            _logger.debug(MocaUtils.concat("C function ", _function, " returned OK "));
            return res;
        }
        catch (MocaInterruptedException e) {
            _logger.debug(MocaUtils.concat("C function ", _function, " returned status: ", e.getErrorCode()), e);
            throw e;
        }
        catch (MocaRuntimeException e) {
            _logger.debug(MocaUtils.concat("C function ", _function, " returned status: ", e.getErrorCode()), e);
            throw new MocaException(e);
        }
        catch (MocaException e) {
            _logger.debug(MocaUtils.concat("C function ", _function, " returned status: ", e.getErrorCode()), e);
            throw e;
        }
        catch (RuntimeException e) {
            _logger.debug(MocaUtils.concat("C function ", _function, " failed with exception: ", e), e);
            throw new UnexpectedException(e);
        }
    }

    private static transient Logger _logger = LogManager.getLogger(CFunctionCommand.class);
    private String _function;
    private final CommandType _type;
    private static final long serialVersionUID = 604901739598367707L;
}
