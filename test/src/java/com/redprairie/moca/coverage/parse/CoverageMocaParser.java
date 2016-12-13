/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.coverage.parse;

import java.util.HashMap;
import java.util.Map;

import com.redprairie.moca.coverage.repository.CoberturaBranchCoverage;
import com.redprairie.moca.coverage.repository.CoberturaLineCoverage;
import com.redprairie.moca.coverage.repository.CoverageExpression;
import com.redprairie.moca.coverage.repository.CoverageMocaCommandUnit;
import com.redprairie.moca.coverage.repository.MocaClassData;
import com.redprairie.moca.server.exec.CommandSequence;
import com.redprairie.moca.server.exec.CommandUnit;
import com.redprairie.moca.server.expression.Expression;
import com.redprairie.moca.server.parse.MocaParseException;
import com.redprairie.moca.server.parse.MocaParser;

/**
 * Moca Parser that has built in local syntax instrumentation
 * 
 * Copyright (c) 2011 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class CoverageMocaParser extends MocaParser {

    /**
     * @param command
     */
    public CoverageMocaParser(CharSequence command, MocaClassData data, 
        boolean shouldAddLines, int lineOffset) {
        super(command);
        _data = data;
        _shouldAddLines = shouldAddLines;
        _lineOffset = lineOffset;
        _branchUsageCount = new HashMap<Integer, Integer>();
        _catchUsageCount = new HashMap<Integer, Integer>();
    }
    
    // @see com.redprairie.moca.server.parse.MocaParser#buildCommand()
    @Override
    protected CommandUnit buildCommand() throws MocaParseException {
        int startLine = _scan.getLine() + _lineOffset;
        CommandUnit command = super.buildCommand();
        String sqlOrGroovy;
        if ((sqlOrGroovy = command.getSql()) != null || 
                (sqlOrGroovy = command.getScript()) != null) {
            for (int i = 0; i < sqlOrGroovy.length(); i++) {
                char sqlOrGroovyChar = sqlOrGroovy.charAt(i);
                if (sqlOrGroovyChar == '\n') {
                    startLine--;
                }
            }
        }
        // We don't really have methods at all
        if (_shouldAddLines) {
            _data.addLine(startLine, "foo", "()");
        }
        
        return new CoverageMocaCommandUnit(command, new CoberturaLineCoverage(
            _data, startLine));
    }
    
    // @see com.redprairie.moca.server.parse.MocaParser#buildIfExpression()
    protected Expression buildIfExpression() throws MocaParseException {
        int startLine = _scan.getLine() + _lineOffset;
        Integer branchCountForLine = _branchUsageCount.get(startLine);
        if (branchCountForLine == null) {
            branchCountForLine = 0;
        }
        else {
            branchCountForLine++;
            _branchUsageCount.put(startLine, branchCountForLine);
        }
        if (_shouldAddLines) {
            _data.addLine(startLine, "foo", "()");
            _data.addLineJump(startLine, branchCountForLine);
        }
        Expression expression = super.buildIfExpression();
        return new CoverageExpression(expression, new CoberturaBranchCoverage(
            _data, startLine, branchCountForLine));
    }
    
    // @see com.redprairie.moca.server.parse.MocaParser#buildCatchExpression()
    @Override
    protected Expression buildCatchExpression() throws MocaParseException {
        int startLine = _scan.getLine() + _lineOffset;
        Integer catchCountForLine = _catchUsageCount.get(startLine);
        if (catchCountForLine == null) {
            catchCountForLine = 0;
        }
        else {
            catchCountForLine++;
            _catchUsageCount.put(startLine, catchCountForLine);
        }
        if (_shouldAddLines) {
            _data.addLine(startLine, "foo", "()");
            _data.addLineJump(startLine, catchCountForLine);
        }
        Expression expression = super.buildCatchExpression();
        return new CoverageExpression(expression, new CoberturaBranchCoverage(
            _data, startLine, catchCountForLine));
    }
    
    // @see com.redprairie.moca.server.parse.MocaParser#parse()
    @Override
    public CommandSequence parse() throws MocaParseException {
        CommandSequence seq =  super.parse();
        if (_shouldAddLines) {
            _data.setComplexity(_maxStackLevel);
        }
        return seq;
    }
    
    private final MocaClassData _data;
    private final boolean _shouldAddLines;
    private final int _lineOffset;
    private final Map<Integer, Integer> _branchUsageCount;
    private final Map<Integer, Integer> _catchUsageCount;
}
