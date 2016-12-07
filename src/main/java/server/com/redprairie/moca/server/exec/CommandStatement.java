/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2007
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

package com.redprairie.moca.server.exec;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.server.expression.Expression;
import com.redprairie.moca.util.MocaUtils;

public class CommandStatement implements ExecutableComponent {
    
    public void setIfTest(Expression ifTest) {
        _ifTest = ifTest;
    }

    public void setIfBlock(CommandStatement ifBlock) {
        _ifBlock = ifBlock;
    }

    public void setElseBlock(CommandStatement elseBlock) {
        _elseBlock = elseBlock;
    }

    public void setMainBlock(CommandBlock mainBlock) {
        _mainBlock = mainBlock;
    }

    public void setCatchBlocks(List<CatchBlock> catchBlocks) {
        _catchBlocks = catchBlocks;
    }

    public void setFinallyBlock(CommandSequence finallyBlock) {
        _finallyBlock = finallyBlock;
    }

    public void setRedirect(String redirect) {
        _redirect = redirect;
    }

    public MocaResults execute(ServerContext exec) throws MocaException {

        // Pick up the main statement execution results.
        try {
            MocaResults result = _executeStatement(exec);
    
            // If they've redirected the output of this block to a variable, capture
            // that
            // variable here.
            if (_redirect == null) {
                return result;
            }
            else {
                EditableResults output = new SimpleResults();
                output.addColumn(_redirect, MocaType.RESULTS);
                output.addRow();
                output.setResultsValue(0, result);
                return output;
            }
        }
        finally {
            // "Fix" the error state into this stack level.
            exec.fixErrorState();
        }
    }

    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        if (_ifTest != null) {
            tmp.append("IF (");
            tmp.append(_ifTest);
            tmp.append(") ");
            tmp.append(_ifBlock);
            if (_elseBlock != null) {
                tmp.append(" ELSE ");
                tmp.append(_elseBlock);
            }
        }

        else {
            if ((_catchBlocks != null && _catchBlocks.size() != 0)
                    || _finallyBlock != null) {
                tmp.append("try {");
                tmp.append(_mainBlock);
                tmp.append('}');
                if (_catchBlocks != null) {
                    for (CatchBlock cblock : _catchBlocks) {
                        tmp.append(cblock);
                    }
                }
                if (_finallyBlock != null) {
                    tmp.append("finally");
                    tmp.append(_finallyBlock);
                }
            }
            else {
                tmp.append(_mainBlock);
            }
        }

        if (_redirect != null) {
            tmp.append(" >> ");
            tmp.append(_redirect);
        }

        return tmp.toString();
    }

    // Private members

    // Execute the main part of the statement. If a redirection is expected,
    // handle it above.
    private MocaResults _executeStatement(ServerContext exec)
            throws MocaException {
        exec.clearArgs();
        if (_ifTest != null) {
            _logger.debug(MocaUtils.concat("Evaluating conditional test: ", _ifTest));
            MocaValue testValue = _ifTest.evaluate(exec);
            if (testValue.asBoolean()) {
                _logger.debug("If-test passed - executing if block");
                return _ifBlock.execute(exec);
            }
            else if (_elseBlock != null) {
                _logger.debug("If-test failed - executing else block");
                return _elseBlock.execute(exec);
            }
            else {
                _logger.debug("If-test failed - no else block to execute");
            }

            return exec.newResults();
        }

        try {
            MocaResults res = _mainBlock.execute(exec);
            return res;
        }
        catch (MocaException e) {

            // Push current error code up to context. Even if an exception is
            // caught, the original error status must be
            // kept for future use within the context.
            exec.setError(e);

            int errorCode = e.getErrorCode();

            if (_catchBlocks != null) {
                // If we're set up to catch errors, do so.
                for (CatchBlock catchBlock : _catchBlocks) {
                    Expression catchExpr = catchBlock.getTest();
                    _logger.debug(MocaUtils.concat("Evaluating try-catch expression... ", catchExpr));

                    // As a special case, we allow catch (@?) to mean catch
                    // anything. Check to see if the error code matches.
                    MocaValue catchValue = catchExpr.evaluate(exec);
                    boolean matches = (catchValue.asInt() == errorCode);
                    
                    if (matches) {
                        _logger.debug("Catch condition met - executing catch block...");
                        // We execute the catch block, if it exists. If it's a
                        // simple catch expression, then
                        // we return the error results of the exception that got
                        // thrown.
                        CommandSequence block = catchBlock.getBlock();

                        // If there's a block of code to execute on this catch
                        // expression, return the result of executing that
                        // block.
                        if (block != null) {

                            // Create a new stack frame in the current execution engine.
                            exec.pushStack();

                            try {
                                return block.execute(exec);
                            }
                            catch (MocaException e2) {
                                exec.setError(e2);
                                throw e2;
                            }
                            finally {
                                exec.popStack(true);
                            }
                        }
                        else {
                            // Because we caught the exception, this statement
                            // has no return value,
                            // but the exception might have some information for
                            // us.
                            MocaResults res = e.getResults();

                            if (res == null) res = exec.newResults();

                            return res;
                        }
                    }
                }
                _logger.debug("No catch expression matched throwing exception");
            }

            // If we got through the entire catch expression set, throw the
            // original exception out.
            throw e;
        }
        finally {
            // A finally block can't affect the results, but it can affect the
            // error state. An exception thrown from a finally block
            // will set the error state of the stack level.
            if (_finallyBlock != null) {
                _logger.debug("Executing finally block...");
                try {
                    _finallyBlock.execute(exec);
                }
                catch (MocaException e) {
                    exec.setError(e);
                    throw e;
                }
            }
        }
    }
    
    /**
     * @return Returns the ifTest.
     */
    public Expression getIfTest() {
        return _ifTest;
    }

    /**
     * @return Returns the catchBlocks.
     */
    public List<CatchBlock> getCatchBlocks() {
        return _catchBlocks;
    }

    /**
     * @return Returns the finallyBlock.
     */
    public CommandSequence getFinallyBlock() {
        return _finallyBlock;
    }

    private static final Logger _logger = 
        LogManager.getLogger(CommandStatement.class);

    private CommandBlock _mainBlock;
    private Expression _ifTest;

    private CommandStatement _ifBlock;
    private CommandStatement _elseBlock;

    private List<CatchBlock> _catchBlocks;
    private CommandSequence _finallyBlock;

    private String _redirect;
}
