/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20167
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

package com.redprairie.moca.server.exec;

import java.util.ArrayList;
import java.util.List;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaOperator;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.NotFoundException;
import com.redprairie.moca.server.expression.Expression;

public class MocaCommandUnit implements CommandUnit {
    public void setSql(String sql) {
        _sql = sql;
    }

    public void setSqlProfileHint(String sqlProfileHint) {
        _sqlProfileHint = sqlProfileHint;
    }
    
    public void setScript(String script) {
        _script = script;
    }
    
    public void setOverride(boolean override) {
        _override = override;
    }
    
    /**
     * @param language The language to set.
     */
    public void setLanguage(String language) {
        _language = (language == null) ? null : language.toLowerCase();
    }
    
    public void setVerbNounClause(String verbNounClause) {
        _verbNounClause = verbNounClause.toLowerCase();
    }
    
    public void setArgList(List<CommandArg> argList) {
        _argList = argList;
    }
    
    public MocaResults execute(ServerContext exec) throws MocaException {
        for (CommandArg arg : _argList) {
            String argName = arg.getName();
            MocaOperator argOper = arg.getOperator();
            
            // Three special cases: REFONE -- @+var, REFLIKE -- @%var, REFALL -- @*
            if (argOper == MocaOperator.REFONE) {
            	// Only add the named argument if there's a variable with that name on the stack.
            	MocaArgument resultArg = exec.getVariableAsArgument(argName, true, false);
            	
            	if (resultArg != null) {
            		String targetName = arg.getTargetName();
            		if (targetName == null) targetName = argName;
                	exec.addArg(targetName, resultArg.getOper(), new MocaValue(resultArg.getType(), resultArg.getValue()));
            	}
            }
            else if (argOper == MocaOperator.REFLIKE) {
            	// Override the operator to a LIKE operator, but only if the variable is present.
            	MocaArgument resultArg = exec.getVariableAsArgument(argName, true, false);
            	if (resultArg != null) {
            		MocaOperator operatorToUse = resultArg.getOper();
            		
            		// We only use LIKE if the value contains % or _
            		String valueAsString = resultArg.getValue() == null ? null : resultArg.getValue().toString();
                	if (valueAsString != null &&
                			(valueAsString.contains("%") || valueAsString.contains("_"))) {
                		operatorToUse = MocaOperator.LIKE;
                	}
                	
                	exec.addArg(argName, operatorToUse, new MocaValue(resultArg.getType(), resultArg.getValue()));
            	}
            }
            else if (argOper == MocaOperator.REFALL) {
            	// Add all arguments.
            	MocaArgument[] allArgs = exec.getCommandArgs(false, false);
            	for (MocaArgument resultArg : allArgs) {
            		exec.addArg(resultArg.getName(),
            				    resultArg.getOper(),
            				    new MocaValue(resultArg.getType(), resultArg.getValue()));
            	}
            }
            else {
	        Expression argExpr = arg.getValue();
	        
	        MocaValue argValue;
	        if (argExpr == null) {
	            argValue = new MocaValue(MocaType.STRING, null);
	        }
	        else {
	            argValue = argExpr.evaluate(exec);
	        }
	        
                exec.addArg(argName, argOper, argValue);
            }
        }
        
        MocaResults results;
        try {
            if (_sql != null) {
                if (_language != null && !_language.isEmpty()) {
                    CompiledScript compiled;
                    synchronized(this) {
                        if (_compiled == null) {
                            _compiled = exec.compileScript(_sql, _language);
                        }
                        compiled = _compiled;
                    }
                    
                    results = exec.executeScript(compiled);
                }
                else {
                    if (_sqlProfileHint != null) {
                        results = exec
                            .executeSQLWithVars(_sql, _sqlProfileHint);
                    }
                    else {
                        results = exec.executeSQLWithVars(_sql);
                    }
                }
            }
            else if (_script != null) {
                CompiledScript compiled;
                synchronized(this) {
                    if (_compiled == null) {
                        _compiled = exec.compileScript(_script, _language);
                    }
                    compiled = _compiled;
                }
                
                results = exec.executeScript(compiled);
            }
            else {
                results = exec.executeNamedCommand(_verbNounClause, _override);
            }
            exec.setError(null);
        }
        catch (MocaException e) {
            if (!(e instanceof NotFoundException) &&
                    e.getErrorCode() == NotFoundException.DB_CODE ||
                    e.getErrorCode() == NotFoundException.SERVER_CODE) {
                throw new NotFoundException(e.getErrorCode(), e.getResults());
            }
            else {
                throw e;
            }
        }
        
        // For convenience, reset the results after execution.
        results.reset();
        
        return results;
    }
    
    // @see java.lang.Object#toString()
    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        if (_sql != null) {
            tmp.append("(SQL");
            if (_language != null) {
                tmp.append('(');
                tmp.append(_language);
                tmp.append(')');
            }
            tmp.append(": ");
            tmp.append(_sql);
            tmp.append(')');
        }
        else if (_script != null) {
            tmp.append("(SCRIPT");
            if (_language != null) {
                tmp.append('(');
                tmp.append(_language);
                tmp.append(')');
            }
            tmp.append(": ");
            tmp.append(_script);
            tmp.append(')');
        }
        else {
            if (_override) {
                tmp.append('^');
            }
            tmp.append(_verbNounClause);
        }
        if (_argList != null) {
            boolean firstOne = true;
            for (CommandArg arg : _argList) {
                if (firstOne) {
                    tmp.append(" WHERE ");
                    firstOne = false;
                }
                else {
                    tmp.append(" AND ");
                }
                tmp.append(arg);
            }
        }
        return tmp.toString();
    }
    
    /**
     * @return Returns the sql.
     */
    @Override
    public String getSql() {
        return _sql;
    }

    /**
     * @return Returns the script.
     */
    @Override
    public String getScript() {
        return _script;
    }

    /**
     * @return Returns the override.
     */
    @Override
    public boolean isOverride() {
        return _override;
    }

    /**
     * @return Returns the verbNounClause.
     */
    @Override
    public String getVerbNounClause() {
        return _verbNounClause;
    }

    /**
     * @return Returns the language.
     */
    @Override
    public String getLanguage() {
        return _language;
    }

    /**
     * @return Returns the argList.
     */
    @Override
    public List<CommandArg> getArgList() {
        return _argList;
    }

    // Privates
    private String _sql;
    private String _sqlProfileHint;
    private String _script;
    private boolean _override;
    private String _verbNounClause;
    private String _language;
    private List<CommandArg> _argList = new ArrayList<CommandArg>();
    private transient CompiledScript _compiled;
}
