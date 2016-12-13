/*
 *  $RCSfile: $
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

package com.redprairie.moca.server.parse;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.redprairie.moca.MocaOperator;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.server.exec.CatchBlock;
import com.redprairie.moca.server.exec.CommandArg;
import com.redprairie.moca.server.exec.CommandBlock;
import com.redprairie.moca.server.exec.CommandBlock.RemoteType;
import com.redprairie.moca.server.exec.CommandGroup;
import com.redprairie.moca.server.exec.CommandSequence;
import com.redprairie.moca.server.exec.CommandStatement;
import com.redprairie.moca.server.exec.CommandStream;
import com.redprairie.moca.server.exec.CommandUnit;
import com.redprairie.moca.server.exec.MocaCommandUnit;
import com.redprairie.moca.server.expression.ErrorCodeExpression;
import com.redprairie.moca.server.expression.ErrorMessageExpression;
import com.redprairie.moca.server.expression.Expression;
import com.redprairie.moca.server.expression.FunctionExpression;
import com.redprairie.moca.server.expression.LiteralExpression;
import com.redprairie.moca.server.expression.ReferenceExpression;
import com.redprairie.moca.server.expression.ScriptExpression;
import com.redprairie.moca.server.expression.operators.AndExpression;
import com.redprairie.moca.server.expression.operators.ConcatExpression;
import com.redprairie.moca.server.expression.operators.IsNullExpression;
import com.redprairie.moca.server.expression.operators.NotExpression;
import com.redprairie.moca.server.expression.operators.NotNullExpression;
import com.redprairie.moca.server.expression.operators.OrExpression;
import com.redprairie.moca.server.expression.operators.arith.DivisionExpression;
import com.redprairie.moca.server.expression.operators.arith.MinusExpression;
import com.redprairie.moca.server.expression.operators.arith.ModExpression;
import com.redprairie.moca.server.expression.operators.arith.MultiplyExpression;
import com.redprairie.moca.server.expression.operators.arith.PlusExpression;
import com.redprairie.moca.server.expression.operators.compare.EqualsExpression;
import com.redprairie.moca.server.expression.operators.compare.GreaterThanExpression;
import com.redprairie.moca.server.expression.operators.compare.GreaterThanOrEqualsExpression;
import com.redprairie.moca.server.expression.operators.compare.LessThanExpression;
import com.redprairie.moca.server.expression.operators.compare.LessThanOrEqualsExpression;
import com.redprairie.moca.server.expression.operators.compare.LikeExpression;
import com.redprairie.moca.server.expression.operators.compare.NotEqualsExpression;
import com.redprairie.moca.server.expression.operators.compare.NotLikeExpression;

/**
 * The core command parser for MOCA command syntax.  This parser reads string input
 * and produces an executable CommandSequence tree.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class MocaParser {

    public MocaParser(CharSequence command) {
        this(command, DEFAULT_DEPTH_WARN_LIMIT);
    }
    
    public MocaParser(CharSequence command, int depthWarningLimit) {
        _scan = new MocaLexer(command);
        _references = new LinkedHashSet<CommandReference>();
        _warnings = new ArrayList<MocaSyntaxWarning>();
        _ignoreReferences = false;
        _depthWarningLimit = depthWarningLimit;
    }
    
    public CommandSequence parse() throws MocaParseException {
        _scan.next();
        
        CommandSequence seq = buildSequence();
        
        if (_scan.tokenType() != MocaTokenType.EOF) {
            throw new MocaParseException(_scan.getLine() + 1, _scan.getLinePos() + 1, "Unexpected token: " + _scan.current());
        }
        
        if (_maxStackLevel > _depthWarningLimit && _depthWarningLimit > 0) {
            addWarning("depth", "pipes nested too deep (" + _maxStackLevel + ")");
        }
        
        return seq;
    }
    
    public Collection<CommandReference> getCommandReferences() {
        return _references;
    }
    
    public Collection<MocaSyntaxWarning> getWarnings() {
        return _warnings;
    }
    
    //
    // Implementation
    //

    protected CommandSequence buildSequence() throws MocaParseException {
        CommandSequence sequence = new CommandSequence();
        
        CommandStream stream = buildStream();
        sequence.addStream(stream);
        
        while (_scan.tokenType() == MocaTokenType.SEMICOLON) {
            // Skip the semicolon
            _scan.next();

            // Special case -- if someone ends a command sequence with a semicolon, check for reasonable
            // end-of-sequence characters.
            if (_scan.tokenType() == MocaTokenType.CLOSE_BRACE || _scan.tokenType() == MocaTokenType.EOF) {
                break;
            }

            stream = buildStream();
            sequence.addStream(stream);
        }
        
        return sequence;
    }
    
    protected CommandStream buildStream() throws MocaParseException {
        CommandStream stream = new CommandStream();

        CommandGroup group = buildGroup();
        stream.addGroup(group);
        
        int startingStackLevel = _stackLevel;
        while (_scan.tokenType() == MocaTokenType.PIPE) {
            // Skip the pipe character
            _scan.next();
            _stackLevel++;
            if (_stackLevel > _maxStackLevel && !_ignoreDepth) {
                _maxStackLevel = _stackLevel;
            }
            group = buildGroup();
            stream.addGroup(group);
        }
        _stackLevel = startingStackLevel;

        return stream;
    }
    
    protected CommandGroup buildGroup() throws MocaParseException {
        
        CommandGroup group = new CommandGroup();

        CommandStatement stmt = buildStatement();
        group.addStatement(stmt);
        
        while (_scan.tokenType() == MocaTokenType.AMPERSAND) {
            // Skip the ampersand character
            _scan.next();
            
            stmt = buildStatement();
            group.addStatement(stmt);
        }

        return group;
    }
    
    protected Expression buildIfExpression() throws MocaParseException {
        _scan.next();
        if (_scan.tokenType() != MocaTokenType.OPEN_PAREN) {
            throw parseException("(");
        }
        
        _scan.next();
        return buildFullExpression();
    }
    
    protected CommandStatement buildStatement() throws MocaParseException {
        CommandStatement stmt = new CommandStatement();
        
        List<StatementAnnotation> annotations = parseAnnotations();
        try {
             
            for (StatementAnnotation a : annotations) {
                a.begin();
            }
            
            if (_scan.tokenType() == MocaTokenType.IF) {
                stmt.setIfTest(buildIfExpression());
    
                if (_scan.tokenType() != MocaTokenType.CLOSE_PAREN) {
                    throw parseException(")");
                }
                _scan.next();
                
                stmt.setIfBlock(buildStatement());
                
                if (_scan.tokenType() == MocaTokenType.ELSE) {
                    _scan.next();
                    stmt.setElseBlock(buildStatement());
                }
            }
            else if (_scan.tokenType() == MocaTokenType.TRY) {
                _scan.next();
                
                // We expect braces here, but we want this to be 
                if (_scan.tokenType() != MocaTokenType.OPEN_BRACE) {
                    throw parseException("{");
                }
                
                // Don't scan the brace.  Let the main block parser do that.
                stmt.setMainBlock(buildBlock());
                stmt.setCatchBlocks(buildCatchBlocks());
                if (_scan.tokenType() == MocaTokenType.FINALLY) {
                    // Scan past the FINALLY keyword
                    _scan.next();
                    stmt.setFinallyBlock(buildSubSequence());
                }
            }
            else {
                stmt.setMainBlock(buildBlock());
                stmt.setCatchBlocks(buildMultiCatchBlock());
            }
            
            if (_scan.tokenType() == MocaTokenType.REDIR_INTO) {
                _scan.next();
                if (_scan.tokenType() != MocaTokenType.VARWORD) {
                    throw parseException("<WORD>");
                }
                stmt.setRedirect(_scan.current().getValue());
                _scan.next();
            }
        }
        finally {
            if (annotations != null) {
                for (StatementAnnotation annotation : annotations) {
                    annotation.end();
                }
            }
        }
        
        return stmt;
    }

    protected List<StatementAnnotation> parseAnnotations()
            throws MocaParseException {
        List<StatementAnnotation> annotations = new ArrayList<StatementAnnotation>();
        while (_scan.tokenType() == MocaTokenType.ATSIGN) {
            _scan.next();
            if (_scan.tokenType() != MocaTokenType.VARWORD) {
                throw parseException("<WORD>");
            }
            
            String name = _scan.current().getValue();
            _scan.next();

            
            if (_scan.tokenType() == MocaTokenType.OPEN_PAREN) {
                List<String> args = new ArrayList<String>();
                _scan.next();
                
                while (_scan.tokenType() != MocaTokenType.CLOSE_PAREN) {
                    if (_scan.tokenType() == MocaTokenType.VARWORD) {
                        args.add(_scan.current().getValue());
                        _scan.next();
                    }
                    else if (_scan.tokenType() == MocaTokenType.SINGLE_STRING ||
                             _scan.tokenType() == MocaTokenType.DOUBLE_STRING) {
                        args.add(dequote(_scan.current().getValue()));
                        _scan.next();
                    }
                    else {
                        throw parseException("WORD or STRING");
                    }
                    
                    if (_scan.tokenType() == MocaTokenType.COMMA) {
                        _scan.next();
                        if (_scan.tokenType() == MocaTokenType.CLOSE_PAREN) {
                            throw parseException("WORD or STRING");
                        }   
                    }
                }
                
                if (_scan.tokenType() != MocaTokenType.CLOSE_PAREN) {
                    throw parseException(")");
                }
                _scan.next();
                
                annotations.add(getAnnotation(name, args));
            }
            else {
                annotations.add(getAnnotation(name, null));
            }
        }
        return annotations;
    }
    
    protected List<CatchBlock> buildCatchBlocks() throws MocaParseException {
        if (_scan.tokenType() != MocaTokenType.CATCH) {
            return null;
        }

        List<CatchBlock> blocks = new ArrayList<CatchBlock>();
        
        while (_scan.tokenType() == MocaTokenType.CATCH) {
            CatchBlock block = new CatchBlock();
            _scan.next();
            if (_scan.tokenType() != MocaTokenType.OPEN_PAREN) {
                throw parseException("(");
            }
            _scan.next();

            block.setTest(buildCatchExpression());

            if (_scan.tokenType() != MocaTokenType.CLOSE_PAREN) {
                throw parseException(")");
            }
            _scan.next();
            
            if (_scan.tokenType() != MocaTokenType.OPEN_BRACE) {
                throw parseException("{");
            }
            
            _scan.next();
            
            // If it is not a close brace then assume it is a sequence
            // If it was a close brace we just leave the sequence empty
            // as in ignoring the exception
            if (_scan.tokenType() != MocaTokenType.CLOSE_BRACE) {
                block.setBlock(buildSequence());    
            }
            
            if (_scan.tokenType() != MocaTokenType.CLOSE_BRACE) {
                throw parseException("}");
            }

            _scan.next();
            
            blocks.add(block);
        }
        return blocks;
    }
    
    protected Expression buildCatchExpression() throws MocaParseException {
        
        return buildExpressionTerm();
    }
    
    protected List<CatchBlock> buildMultiCatchBlock() throws MocaParseException {
        if (_scan.tokenType() != MocaTokenType.CATCH) {
            return null;
        }
        
        List<CatchBlock> blocks = new ArrayList<CatchBlock>();
        _scan.next();
        
        if (_scan.tokenType() != MocaTokenType.OPEN_PAREN) {
            throw parseException("(");
        }
        _scan.next();

        do {
            CatchBlock block = new CatchBlock();
            block.setTest(buildCatchExpression());
            blocks.add(block);
            
            if (_scan.tokenType() != MocaTokenType.COMMA) {
                 break;
            }
            _scan.next();
        } while (true);

        if (_scan.tokenType() != MocaTokenType.CLOSE_PAREN) {
            throw parseException(")");
        }
        
        _scan.next();

        return blocks;
    }
    
    protected CommandSequence buildSubSequence() throws MocaParseException {
        if (_scan.tokenType() != MocaTokenType.OPEN_BRACE) {
            throw parseException("{");
        }

        _scan.next();
        CommandSequence seq = buildSequence(); 
        
        if (_scan.tokenType() != MocaTokenType.CLOSE_BRACE) {
            throw parseException("}");
        }
        _scan.next();
        
        return seq;
    }
    
    protected CommandBlock buildBlock() throws MocaParseException {
        
        CommandBlock block = new CommandBlock();
        
        RemoteType remoteFlavor = null;
        MocaLexer.Reference remoteStart = null;
        boolean referenceState = _ignoreReferences;

        if (_scan.tokenType() == MocaTokenType.REMOTE || 
            _scan.tokenType() == MocaTokenType.PARALLEL ||
            _scan.tokenType() == MocaTokenType.INPARALLEL ) {
            
            MocaTokenType flavorToken = _scan.tokenType();
            
            if (flavorToken == MocaTokenType.REMOTE) {
                remoteFlavor = RemoteType.REMOTE;
            }
            else if (flavorToken == MocaTokenType.PARALLEL) {
                remoteFlavor = RemoteType.PARALLEL;
            }
            else {
                remoteFlavor = RemoteType.INPARALLEL;
            }
            
            // Scan past the keyword
            _scan.next();
            
            // Expect an open parenthesis
            if (_scan.tokenType() != MocaTokenType.OPEN_PAREN) {
                throw parseException("(");
            }
            _scan.next();
            
            block.setRemoteType(remoteFlavor);
            
            block.setRemoteHost(buildExpressionTerm());

            // Expect a close parenthesis
            if (_scan.tokenType() != MocaTokenType.CLOSE_PAREN) {
                throw parseException(")");
            }
            _scan.next();
            
            remoteStart = _scan.markPlace();
            
            // Suppress command references for remote commands
            _ignoreReferences = true;
        }
        
        // At this point, there are two possibilities.  Either we've got a sub-sequence (braces) or
        // this is a real command.
        if (_scan.tokenType() == MocaTokenType.OPEN_BRACE) {
            _scan.next();
            
            block.setSubSequence(buildSequence());
            
            if (_scan.tokenType() != MocaTokenType.CLOSE_BRACE) {
                throw parseException("}");
            }
            _scan.next();
        }
        else {
            block.setCommand(buildCommand());
        }
        
        if (remoteStart != null) {
            block.setRemoteText(_scan.getTextSinceMark(remoteStart));
        }
        
        _ignoreReferences = referenceState;
        return block;
    }
    
    private String _checkForProfileHint(String sql) {
        Matcher profileMatcher = _sqlProfileMatcher.matcher(sql);
        String profileString = null;
        if (profileMatcher.matches()) {
            profileString = profileMatcher.group(1).trim();
        }
        return profileString;
    }
    
    protected CommandUnit buildCommand() throws MocaParseException {
        MocaCommandUnit cmd = new MocaCommandUnit();
        
        if (_scan.tokenType() == MocaTokenType.COLON) {
            _scan.next();
            if (_scan.tokenType() != MocaTokenType.VARWORD) {
                throw parseException("<WORD>");
            }
            
            String languageOverride = _scan.current().getValue();
            cmd.setLanguage(languageOverride);
            _scan.next();
        }
        
        if (_scan.tokenType() == MocaTokenType.BRACKET_STRING_WITH_HINT) {
            cmd.setSqlProfileHint(_checkForProfileHint(_scan.current()
                .getValue()));
        }
          
        if (_scan.tokenType() == MocaTokenType.BRACKET_STRING ||
                _scan.tokenType() == MocaTokenType.BRACKET_STRING_WITH_HINT) {
            String strToken = _scan.current().getValue();
            _scan.next();

            if (strToken.length() >= 4 &&
                strToken.charAt(1) == '[' && strToken.charAt(strToken.length() - 2) == ']') {
                cmd.setScript(strToken.substring(2, strToken.length() - 2));
            }
            else {              
                cmd.setSql(strToken.substring(1, strToken.length() - 1));
            }
        }
        else {
            boolean override = false;
            if (_scan.tokenType() == MocaTokenType.CARET) {
                _scan.next();
                override = true;
            }
            
            cmd.setOverride(override);
            
            if (_scan.tokenType() != MocaTokenType.VARWORD) {
                throw parseException("<WORD>");
            }
            
            String verb = _scan.current().getValue();
            StringBuilder verbNounClause = new StringBuilder(verb);
            _scan.next();
            
            while (isValidNoun(_scan.tokenType())) {
                verbNounClause.append(' ');
                verbNounClause.append(_scan.current().getValue());
                _scan.next();
            }
            
            String ref = verbNounClause.toString().toLowerCase();
            cmd.setVerbNounClause(ref);
            if (!_ignoreReferences) {
                _references.add(new CommandReference(ref, override, _scan.getLine(), _scan.getLinePos() + 1));
            }
        }
        
        // A where clause can appear after any command type (SQL, script, or verb/noun).
        if (_scan.tokenType() == MocaTokenType.WHERE) {
            _scan.next();
            cmd.setArgList(buildArgList());
        }
        
        return cmd;
        
    }
    
    protected boolean isValidNoun(MocaTokenType type) {
        switch (type) {
        case VARWORD:
        case REMOTE:
        case PARALLEL:
        case INPARALLEL:
        case AND:
        case OR:
        case NOT:
        case IS:
        case LIKE:
        case NULL_TOKEN:
            return true;
        default:
            return false;
        }
    }
    
    protected boolean isValidVariableRef(MocaTokenType type) {
        switch (type) {
        case VARWORD:
        case REMOTE:
        case PARALLEL:
        case INPARALLEL:
        case LIKE:
        case WHERE:
            return true;
        default:
            return false;
        }
    }
    
    protected List<CommandArg> buildArgList() throws MocaParseException {
        List<CommandArg> argList = new ArrayList<CommandArg>();
        do {
            CommandArg arg = new CommandArg();
            
            // There are three valid tokens that could occur -- a bracketed string, meaning a
            // "raw" where clause to be appended to SQL statements, a "special" variable reference
            // or a "name OP value" sequence of tokens.
            if (_scan.tokenType() == MocaTokenType.BRACKET_STRING) {
                String strToken = _scan.current().getValue();
                _scan.next();
                arg.setName("where");
                arg.setOperator(MocaOperator.RAWCLAUSE);
                arg.setValue(new LiteralExpression(MocaType.STRING, strToken.substring(1, strToken.length() - 1)));
            }
            else if (_scan.tokenType() == MocaTokenType.ATSIGN) {
                _scan.next();
                
                if (_scan.tokenType() == MocaTokenType.STAR) {
                    _scan.next();
                    arg.setName("_ALL_ARGS_");
                    arg.setOperator(MocaOperator.REFALL);
                }
                else if (_scan.tokenType() == MocaTokenType.PLUS || _scan.tokenType() == MocaTokenType.PERCENT) {
                    // @+var and @%var are parsed identically
                    
                    boolean isLike = (_scan.tokenType() == MocaTokenType.PERCENT);
                    
                    // Skip over the + or %
                    _scan.next();
                    
                    // Expect a word
                    String name = getVarName("NAME"); 
                    _scan.next();

                    // Optionally, handle a caret as a source of the reference
                    if (_scan.tokenType() == MocaTokenType.CARET) {
                        _scan.next();
                        arg.setName(getVarName("NAME"));
                        arg.setTargetName(name);
                        _scan.next();
                    }
                    else {
                        arg.setName(name);
                    }
                    
                    // If @+, use REFONE. If @% use REFLIKE
                    arg.setOperator(isLike ? MocaOperator.REFLIKE : MocaOperator.REFONE);
                }
                else {
                    throw parseException("*, % or +");
                }
            }
            else {
                // Now we have the meat of the where clause.
                arg.setName(getVarName("WORD, wildcard variable reference (@+, @*), or [raw where clause]"));
                _scan.next();
                
                MocaTokenType oper = _scan.tokenType();
                if (oper == MocaTokenType.BRACKET_STRING) {
                    // We know the current item is a value (bracket string), so read it as an
                    // expression
                    String strToken = _scan.current().getValue();
                    _scan.next();
                    arg.setOperator(MocaOperator.NAMEDCLAUSE);
                    arg.setValue(new LiteralExpression(MocaType.STRING, strToken.substring(1, strToken.length() - 1)));
                }
                else if (oper == MocaTokenType.IS) {
                    _scan.next();
                    if (_scan.tokenType() == MocaTokenType.NOT) {
                        _scan.next();
                        arg.setOperator(MocaOperator.NOTNULL);
                    }
                    else {
                        arg.setOperator(MocaOperator.ISNULL);
                    }
                    
                    // As long as we see the word NULL, scan past it.
                    if (_scan.tokenType() != MocaTokenType.NULL_TOKEN) {
                        throw parseException("NULL");
                    }
                    _scan.next();
                }
                else {
                    if (oper == MocaTokenType.EQ) arg.setOperator(MocaOperator.EQ);
                    else if (oper == MocaTokenType.NE) arg.setOperator(MocaOperator.NE);
                    else if (oper == MocaTokenType.GT) arg.setOperator(MocaOperator.GT);
                    else if (oper == MocaTokenType.GE) arg.setOperator(MocaOperator.GE);
                    else if (oper == MocaTokenType.LT) arg.setOperator(MocaOperator.LT);
                    else if (oper == MocaTokenType.LE) arg.setOperator(MocaOperator.LE);
                    else if (oper == MocaTokenType.LIKE) arg.setOperator(MocaOperator.LIKE);
                    else if (oper == MocaTokenType.NOT) {
                        _scan.next();
                        if (_scan.tokenType() != MocaTokenType.LIKE) {
                            throw parseException("LIKE");
                        }
                        arg.setOperator(MocaOperator.NOTLIKE);
                    }
                    else {
                        throw parseException("(=, !=, >, <, >=, <=, IS, or LIKE)");
                    }
                    _scan.next();
                    
                    // Now look for a value for our operator
                    arg.setValue(buildExpressionTerm());
                }
            }
            
            argList.add(arg);
            
            if (_scan.tokenType() != MocaTokenType.AND) {
                break;
            }
            _scan.next();
            
        } while (true);
        
        return argList;
    }
            
    protected Expression buildExpressionTerm() throws MocaParseException {
        Expression expr = buildExpressionFactor();
        do {
            MocaTokenType operatorToken = _scan.tokenType();
            if (operatorToken == MocaTokenType.PLUS) {
                _scan.next();
                expr = new PlusExpression(expr, buildExpressionFactor());
            }
            else if (operatorToken == MocaTokenType.MINUS) {
                _scan.next();
                expr = new MinusExpression(expr, buildExpressionFactor());
            }
            else if (operatorToken == MocaTokenType.DOUBLEPIPE) {
                _scan.next();
                expr = new ConcatExpression(expr, buildExpressionFactor());
            }
            else {
                break;
            }
        } while (true);
        
        return expr;
    }
    
    protected Expression buildLogicalExpression() throws MocaParseException {
        Expression expr = buildExpressionTerm();
        MocaTokenType oper = _scan.tokenType();
        if (oper == MocaTokenType.IS) {
            _scan.next();
            if (_scan.tokenType() == MocaTokenType.NOT) {
                _scan.next();
                expr  = new NotNullExpression(expr);
            }
            else {
                expr = new IsNullExpression(expr);
            }
            
            // As long as we see the word NULL, scan past it.
            if (_scan.tokenType() != MocaTokenType.NULL_TOKEN) {
                throw parseException("NULL");
            }
            _scan.next();
        }
        else {
            if (oper == MocaTokenType.EQ) {
                _scan.next();
                expr  = new EqualsExpression(expr, buildExpressionTerm());
            }
            else if (oper == MocaTokenType.NE) {
                _scan.next();
                expr  = new NotEqualsExpression(expr, buildExpressionTerm());
            }
            else if (oper == MocaTokenType.GT) {
                _scan.next();
                expr  = new GreaterThanExpression(expr, buildExpressionTerm());
            }
            else if (oper == MocaTokenType.GE) {
                _scan.next();
                expr  = new GreaterThanOrEqualsExpression(expr, buildExpressionTerm());
            }
            else if (oper == MocaTokenType.LT) {
                _scan.next();
                expr  = new LessThanExpression(expr, buildExpressionTerm());
            }
            else if (oper == MocaTokenType.LE) {
                _scan.next();
                expr  = new LessThanOrEqualsExpression(expr, buildExpressionTerm());
            }
            else if (oper == MocaTokenType.LIKE) {
                _scan.next();
                expr  = new LikeExpression(expr, buildExpressionTerm());
            }
            else if (oper == MocaTokenType.NOT) {
                _scan.next();
                if (_scan.tokenType() != MocaTokenType.LIKE) {
                    throw parseException("LIKE");
                }
                _scan.next();
                expr  = new NotLikeExpression(expr, buildExpressionTerm());
            }
        }
        
        // If no operator is present, just return the initial expression
        return expr;
    }
    
    protected Expression buildAndExpression() throws MocaParseException {
        Expression expr = buildLogicalExpression();
        while (_scan.tokenType() == MocaTokenType.AND) {
            _scan.next();
            Expression left = expr;
            Expression right = buildLogicalExpression();
            expr = new AndExpression(left, right);
        }
        
        return expr;
    }
    protected Expression buildFullExpression() throws MocaParseException {
        Expression expr = buildAndExpression();
        while (_scan.tokenType() == MocaTokenType.OR) {
            _scan.next();
            Expression left = expr;
            Expression right = buildAndExpression();
            expr = new OrExpression(left, right);
        }
        
        return expr;
    }
    
    protected Expression buildExpressionFactor() throws MocaParseException {
        Expression expr = buildExpressionValue();
        
        do {
            MocaTokenType operatorToken = _scan.tokenType();
            
            if (operatorToken == MocaTokenType.STAR) {
                _scan.next();
                expr = new MultiplyExpression(expr, buildExpressionValue());
            }
            else if (operatorToken == MocaTokenType.SLASH) {
                _scan.next();
                expr = new DivisionExpression(expr, buildExpressionValue());
            }
            else if (operatorToken == MocaTokenType.PERCENT) {
                _scan.next();
                expr = new ModExpression(expr, buildExpressionValue());
            }
            else {
                break;
            }
                
        } while (true);
        
        return expr;
    }
    
    protected Expression buildExpressionValue() throws MocaParseException {
        boolean isNegative = false;
        switch (_scan.tokenType()) {
        case BANG:
            _scan.next();
            return new NotExpression(buildExpressionValue());
        case VARWORD:
            return buildFunctionExpression();
        case OPEN_PAREN:
            _scan.next();
            Expression subExpression = buildFullExpression();
            if (_scan.tokenType() != MocaTokenType.CLOSE_PAREN) {
                throw parseException(")");
            }
            _scan.next();
            return subExpression;
        case MINUS:
            isNegative = true;
            // Pass through
        case PLUS:
            _scan.next();
            if (_scan.tokenType() != MocaTokenType.NUMBER) {
                throw parseException("<NUMBER>");
            }
            // Pass through
        case NUMBER:
            String numericValue = _scan.current().getValue();
            if (isNegative) numericValue = "-" + numericValue;
            Expression numericExpression;
            try {
                if (numericValue.indexOf('.') != -1 ||
                        numericValue.indexOf('e') != -1 ||
                        numericValue.indexOf('E') != -1) {
                    // We've got a floating point value on our hands.
                    numericExpression = new LiteralExpression(MocaType.DOUBLE, new Double(numericValue));
                }
                else {
                    // Deal with large numeric values.
                    BigInteger tmpValue = new BigInteger(numericValue, 10);
                    
                    if (tmpValue.compareTo(BigInteger.valueOf((long)Integer.MAX_VALUE)) > 0 ||
                        tmpValue.compareTo(BigInteger.valueOf((long)Integer.MIN_VALUE)) < 0) {
                        numericExpression = new LiteralExpression(MocaType.DOUBLE,
                            Double.valueOf(tmpValue.doubleValue()));
                    }
                    else {
                        numericExpression = new LiteralExpression(MocaType.INTEGER,
                            Integer.valueOf(tmpValue.intValue()));
                    }
                }
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
                throw parseException("NUMERIC");
            }
            
            _scan.next();
            
            return numericExpression;
        case BRACKET_STRING:
            String bracketValue = _scan.current().getValue();
            _scan.next();
            String rawValue;
            if (bracketValue.length() >= 4 && bracketValue.charAt(1) == '[' &&
                    bracketValue.charAt(bracketValue.length() - 2) == ']') {
                rawValue = bracketValue.substring(2, bracketValue.length() - 2);
                return new ScriptExpression(rawValue);
            }
            else {
                rawValue = bracketValue.substring(1, bracketValue.length() - 1);
                return new LiteralExpression(MocaType.STRING, rawValue);
            }

            
        case SINGLE_STRING:
        case DOUBLE_STRING:
            String unquoted = _scan.current().getValue();
            _scan.next();
            return new LiteralExpression(MocaType.STRING, dequote(unquoted));
            
        case ATSIGN:
            _scan.next();
            
            // Special variables:
            // @?, @!
            if (_scan.tokenType() == MocaTokenType.BANG) {
                _scan.next();
                return new ErrorMessageExpression();
            }
            else if (_scan.tokenType() == MocaTokenType.QUESTION_MARK) {
                _scan.next();
                return new ErrorCodeExpression();
            }
            else {
                boolean isEnvironment = false;
                boolean useAllForms = false;
                if (_scan.tokenType() == MocaTokenType.ATSIGN) {
                    _scan.next();
                    isEnvironment = true;
                }
                else if (_scan.tokenType() == MocaTokenType.MINUS) {
                    _scan.next();
                    useAllForms = true;
                }
                
                String name = getVarName("NAME");
                _scan.next();
                
                //
                // Check for variable directives: @var#onstack or @var#keep
                //
                boolean markUsed = true;
                boolean checkOnly = false;
                if (_scan.tokenType() == MocaTokenType.POUND) {
                    _scan.next();
                    if (_scan.tokenType() != MocaTokenType.VARWORD) {
                        throw parseException("keep or onstack");
                    }
                    
                    if (_scan.current().getValue().equalsIgnoreCase("keep")) {
                        markUsed = false;
                    }
                    else if (_scan.current().getValue().equalsIgnoreCase("onstack")) {
                        checkOnly = true;
                    }
                    else {
                        throw parseException("keep or onstack");
                    }
                    _scan.next();
                }
                
                return new ReferenceExpression(name, isEnvironment, useAllForms, markUsed, checkOnly);
            }
            
        case NULL_TOKEN:
            _scan.next();
            return new LiteralExpression(MocaType.STRING, null);
        }
        
        // Now, we've failed to find an expression.
        throw parseException("expression");
    }
    
    protected Expression buildFunctionExpression() throws MocaParseException {
        // This method only gets called if a word is present.  First, pull off
        // the function name from the current token.
        String functionName = _scan.current().getValue();
        
        List<Expression> functionArgs = new ArrayList<Expression>();
        _scan.next();
        if (_scan.tokenType() == MocaTokenType.OPEN_PAREN) {
            _scan.next();
            while (_scan.tokenType() != MocaTokenType.CLOSE_PAREN) {
                functionArgs.add(buildFullExpression());
                if (_scan.tokenType() == MocaTokenType.COMMA) {
                    _scan.next();
                }
                else if (_scan.tokenType() != MocaTokenType.CLOSE_PAREN) {
                    throw parseException(")");
                }
            }
            _scan.next();
        }
        FunctionExpression expr = FunctionExpression.getBuiltInFunction(functionName, functionArgs);
        
        if (expr == null) {
            expr = FunctionExpression.getCommandFunction(functionName, functionArgs);
            if (!_ignoreReferences) {
                _references.add(new CommandReference(
                    functionName.toLowerCase().replace("__", " "),
                    false, _scan.getLine(), _scan.getLinePos()));
            }
        }
        return expr;
    }
    
    protected String dequote(String orig) {
        char quotechar = orig.charAt(0);
        int startpos = 1;
        int endpos = orig.length() - 1;
        StringBuilder buf = new StringBuilder(orig.length());
        do {
            int quotepos = orig.indexOf(quotechar, startpos);
            if (quotepos > startpos) {
                buf.append(orig, startpos, quotepos);
            }
            
            if (quotepos < endpos) {
                buf.append(quotechar);
                startpos = quotepos + 2;
            }
            else {
                startpos = quotepos;
            }
        } while (startpos < endpos);
        
        return buf.toString();
    }
    
    protected String getVarName(String expect) throws MocaParseException {
        if (isValidVariableRef(_scan.tokenType())) {
            return _scan.current().getValue();
        }

        throw parseException(expect);
    }
    
    protected MocaParseException parseException(String expected) {
        return new MocaParseException(_scan.getLine() + 1, _scan.getLinePos() + 1,
                "Expected: " + expected + ", got " + _scan.current());
    }
    
    protected abstract class StatementAnnotation {
        abstract void begin();
        abstract void end();
    }
    
    protected class SuppressWarningsAnnotation extends StatementAnnotation {
        SuppressWarningsAnnotation(StatementAnnotation... specificWarnings) {
            this(Arrays.asList(specificWarnings));
        }
        
        SuppressWarningsAnnotation(Collection<StatementAnnotation> specificWarnings) {
            _specificWarnings = new ArrayList<StatementAnnotation>(specificWarnings);
        }
        
        @Override
        void begin()  {
            for (StatementAnnotation a : _specificWarnings) {
                a.begin();
            }
        }
        
        @Override
        void end()  {
            for (StatementAnnotation a : _specificWarnings) {
                a.end();
            }
        }
        private final Collection<StatementAnnotation> _specificWarnings;
    }
    
    protected class NoRefWarning extends StatementAnnotation {
        @Override
        void begin() {
            _origState = _ignoreReferences;
            _ignoreReferences = true;
        }
        
        @Override
        void end() {
            _ignoreReferences = _origState;
        }
        
        private boolean _origState;
    }
    
    protected class DepthWarning extends StatementAnnotation {
        @Override
        void begin() {
            _origState = _ignoreDepth;
            _ignoreDepth = true;
        }
        
        @Override
        void end() {
            _ignoreDepth = _origState;
        }
        
        private boolean _origState;
    }
    
    protected StatementAnnotation getAnnotation(String name, List<String> args) throws MocaParseException {
        if (name.equalsIgnoreCase("SuppressWarnings")) {
            if (args == null || args.size() == 0) {
                // All Warnings
                return new SuppressWarningsAnnotation(new NoRefWarning(), new DepthWarning());
            }
            else {
                List<StatementAnnotation> warnings = new ArrayList<StatementAnnotation>();
                for (String s : args) {
                    if (s.equalsIgnoreCase("noref")) {
                        warnings.add(new NoRefWarning());
                    }
                    else if (s.equalsIgnoreCase("depth")) {
                        warnings.add(new DepthWarning());
                    }
                    else {
                        throw parseException("valid warning");
                    }
                }
                return new SuppressWarningsAnnotation(warnings);
            }
        }
        
        throw parseException("SuppressWarnings");
    }
    
    protected void addWarning(String type, String message) {
        _warnings.add(new MocaSyntaxWarning(type, message, _scan.getLine(), _scan.getLinePos() + 1));
    }
    
    protected final MocaLexer _scan;
    private final Set<CommandReference> _references;
    private final List<MocaSyntaxWarning> _warnings;
    private final int _depthWarningLimit;
    private boolean _ignoreReferences;
    private boolean _ignoreDepth;
    private int _stackLevel;
    protected int _maxStackLevel;
    private static final int DEFAULT_DEPTH_WARN_LIMIT = 0;
    private static final Pattern _sqlProfileMatcher = Pattern.compile(
        ".*/\\*#\\s*profile\\s*=\\s*(.*?)\\s*\\*/.*"
        , Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
}
