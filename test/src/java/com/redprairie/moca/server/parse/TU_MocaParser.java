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

package com.redprairie.moca.server.parse;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import com.redprairie.moca.server.exec.CommandSequence;
import com.redprairie.moca.server.exec.CommandStream;
/**
 * Tests the behavior of the MOCA parser.  This set of tests relies on the behavior of the 
 * toString method on the parser output classes (CommandSequence, etc).  There's not a good way,
 * otherwise, to get at the internals of the parsed commands.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class TU_MocaParser extends TestCase {
    
    public void testEmptyCommand() throws MocaParseException {
        try {
            CommandSequence parsed = new MocaParser("").parse();
            fail("Expected parse exception, got [" + parsed + "]");
        }
        catch (MocaParseException e) {
            // Normal
        }
    }
    
    public void testUnclosedBrackets() throws MocaParseException {
        try {
            CommandSequence parsed = new MocaParser("[select * from foo").parse();
            fail("Expected parse exception, got [" + parsed + "]");
        }
        catch (MocaParseException e) {
            // Normal
        }
    }
    
    public void testUnclosedScriptBrackets() throws MocaParseException {
        try {
            CommandSequence parsed = new MocaParser("[[ foo = 'bar']A").parse();
            fail("Expected parse exception, got [" + parsed + "]");
        }
        catch (MocaParseException e) {
            // Normal
        }
    }
    
    public void testSimpleCommand() throws MocaParseException {
        CommandSequence parsed = new MocaParser("foo").parse();
        assertNotNull(parsed);
        List<CommandStream> streams = parsed.getStreams();
        assertEquals(1, streams.size());
        assertEquals("{foo}", parsed.toString());
    }

    public void testSimpleVerbNoun() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data").parse();
        assertNotNull(parsed);
        List<CommandStream> streams = parsed.getStreams();
        assertEquals(1, streams.size());
        assertEquals("{publish data}", parsed.toString());
    }
    
    public void testCompoundCommand() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data; noop").parse();
        assertNotNull(parsed);
        List<CommandStream> streams = parsed.getStreams();
        assertEquals(2, streams.size());
        assertEquals("{publish data; noop}", parsed.toString());
    }

    public void testCommandWithWhereClause() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data where x = 'abcd'").parse();
        assertNotNull(parsed);
        List<CommandStream> streams = parsed.getStreams();
        assertEquals(1, streams.size());
        assertEquals("{publish data WHERE x EQ (TYPE: STRING, VALUE: abcd)}", parsed.toString());
    }

    public void testSQLCommandWithWhereClause() throws MocaParseException {
        CommandSequence parsed = new MocaParser("[select * from foo where @*] where x = 'abcd'").parse();
        assertNotNull(parsed);
        List<CommandStream> streams = parsed.getStreams();
        assertEquals(1, streams.size());
        assertEquals("{(SQL: select * from foo where @*) WHERE x EQ (TYPE: STRING, VALUE: abcd)}", parsed.toString());
    }

    public void testBasicSQLCommand() throws MocaParseException {
        CommandSequence parsed = new MocaParser("[select * from foo]").parse();
        assertNotNull(parsed);
        List<CommandStream> streams = parsed.getStreams();
        assertEquals(1, streams.size());
        assertEquals("{(SQL: select * from foo)}", parsed.toString());
    }

    public void testElaborateSQLCommand() throws MocaParseException {
        CommandSequence parsed = new MocaParser("[/*NOCONV*/ BEGIN;" +
                                            " SELECT 'abc', 'abc''s \"value\" so they say]]]]' [test][test][tests]from foo;end]").parse();
        assertNotNull(parsed);
        List<CommandStream> streams = parsed.getStreams();
        assertEquals(1, streams.size());
        assertEquals("{(SQL: /*NOCONV*/ BEGIN;" +
                     " SELECT 'abc', 'abc''s \"value\" so they say]]]]' [test][test][tests]from foo;end)}", parsed.toString());
    }

    public void testBasicGroovyCommand() throws MocaParseException {
        CommandSequence parsed = new MocaParser("[[a = 100\nb = 'Testing' + \"TestingAgain\"]]").parse();
        assertNotNull(parsed);
        List<CommandStream> streams = parsed.getStreams();
        assertEquals(1, streams.size());
        assertEquals("{(SCRIPT: a = 100\nb = 'Testing' + \"TestingAgain\")}", parsed.toString());
    }
    
    public void testNumericValue() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data where abcd = -1403").parse();
        assertNotNull(parsed);
        assertEquals("{publish data WHERE abcd EQ (TYPE: INTEGER, VALUE: -1403)}", parsed.toString());
    }
    
    
    public void testLargeNumericValue() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data where abcd = 1234567890123").parse();
        assertNotNull(parsed);
        assertEquals("{publish data WHERE abcd EQ (TYPE: DOUBLE, VALUE: 1.234567890123E12)}", parsed.toString());
    }

    
    public void testVeryLargeNumericValue() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data where abcd = -12345678901234567890123").parse();
        assertNotNull(parsed);
        assertEquals("{publish data WHERE abcd EQ (TYPE: DOUBLE, VALUE: -1.2345678901234568E22)}", parsed.toString());
    }

    public void testNullValue() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data where abcd = -1403 and def = null").parse();
        assertNotNull(parsed);
        assertEquals("{publish data WHERE abcd EQ (TYPE: INTEGER, VALUE: -1403) AND def EQ (TYPE: STRING, VALUE: null)}", parsed.toString());
    }
    
    public void testSimpleSyntaxWithFunction() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data where abcd = nvl(@var, 'default')").parse();
        assertNotNull(parsed);
        assertEquals("{publish data WHERE abcd EQ nvl(@var,(TYPE: STRING, VALUE: default))}", parsed.toString());
    }
    
    public void testResultsRedirect() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data where x = 'hello' and y = 'goodbye' >> res | [[ res ]]").parse();
        assertNotNull(parsed);
        assertEquals("{publish data WHERE x EQ (TYPE: STRING, VALUE: hello) AND y EQ (TYPE: STRING, VALUE: goodbye) >> res | (SCRIPT:  res )}", parsed.toString());
    }
    
    public void testTryCatchWithSingleCatchValue() throws MocaParseException {
        CommandSequence parsed = new MocaParser("try {publish data where x = 'hello'} catch(-1403) {noop}").parse();
        assertNotNull(parsed);
        assertEquals("{try {{publish data WHERE x EQ (TYPE: STRING, VALUE: hello)}}catch((TYPE: INTEGER, VALUE: -1403)){noop}}", parsed.toString());
    }

    public void testCatchWithSingleCatchValue() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data where x = 'hello' catch(-1403)").parse();
        assertNotNull(parsed);
        assertEquals("{try {publish data WHERE x EQ (TYPE: STRING, VALUE: hello)}catch((TYPE: INTEGER, VALUE: -1403))(catch-only)}", parsed.toString());
    }

    public void testCatchWithWildcardCatchValue() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data where x = 'hello' catch(@?)").parse();
        assertNotNull(parsed);
        assertEquals("{try {publish data WHERE x EQ (TYPE: STRING, VALUE: hello)}catch(@?)(catch-only)}", parsed.toString());
    }

    public void testTryFinally() throws MocaParseException {
        CommandSequence parsed = new MocaParser("try {publish data where x = 'hello'} finally {noop}").parse();
        assertNotNull(parsed);
        assertEquals("{try {{publish data WHERE x EQ (TYPE: STRING, VALUE: hello)}}finally{noop}}", parsed.toString());
    }

    public void testCommandWithBracketWhereClause() throws MocaParseException {
        CommandSequence parsed = new MocaParser("list thing where [x = 100]").parse();
        assertNotNull(parsed);
        assertEquals("{list thing WHERE where RAWCLAUSE (TYPE: STRING, VALUE: x = 100)}", parsed.toString());
    }

    public void testConditionalCommand() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data | if (@var = 'foo') do something").parse();
        assertNotNull(parsed);
        assertEquals("{publish data | IF (@var=(TYPE: STRING, VALUE: foo)) do something}", parsed.toString());
    }

    public void testScriptExpression() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data where x = [[abc]]").parse();
        assertNotNull(parsed);
        assertEquals("{publish data WHERE x EQ [[abc]]}", parsed.toString());
    }

    public void testRemoteCommand() throws MocaParseException {
        CommandSequence parsed = new MocaParser("remote('host:9999'){publish data where x='hello'}|other").parse();
        assertNotNull(parsed);
        assertEquals("{REMOTE((TYPE: STRING, VALUE: host:9999)){publish data where x='hello'} | other}", parsed.toString());
    }

    public void testRemoteCommandNoBraces() throws MocaParseException {
        CommandSequence parsed = new MocaParser("remote('host:9999') foo where x like 'y%' and y [ < 100]|other").parse();
        assertNotNull(parsed);
        assertEquals("{REMOTE((TYPE: STRING, VALUE: host:9999)) foo where x like 'y%' and y [ < 100] | other}", parsed.toString());
    }

    public void testVariableReference() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data where x = @y | if (@x = @z) filter data").parse();
        assertNotNull(parsed);
        assertEquals("{publish data WHERE x EQ @y | IF (@x=@z) filter data}", parsed.toString());
    }

    public void testVariableDirective() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data where x = @-y | if (@x#onstack and @x like @y#keep) filter data").parse();
        assertNotNull(parsed);
        assertEquals("{publish data WHERE x EQ @+y | IF (@x#onstack AND @x LIKE @y#keep) filter data}", parsed.toString());
    }

    public void testVariableReferenceWithKeywords() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data where x = @where and y = @like and @+z^where").parse();
        assertNotNull(parsed);
        assertEquals("{publish data WHERE x EQ @where AND y EQ @like AND z^where REFONE null}", parsed.toString());
    }

    public void testConcatenationInRemoteExpression() throws MocaParseException {
        CommandSequence parsed = new MocaParser("remote(@host||\":\"||@port) do something where @*").parse();
        assertNotNull(parsed);
        assertEquals("{REMOTE(@host || (TYPE: STRING, VALUE: :) || @port) do something where @*}", parsed.toString());
    }

    public void testBadVariableDirective() throws MocaParseException {
        try {
            CommandSequence parsed = new MocaParser("publish data where x = @-y | if (@x#unstuck and @x like @y#keeep) filter data").parse();
            fail("expected parse exception, got " + parsed);
        }
        catch (MocaParseException e) {
            // Normal
        }
    }

    public void testCommentInCommandSyntax() throws MocaParseException {
        CommandSequence parsed = new MocaParser("if (@x = 'AAA') /* Test */ {foo}").parse();
        assertNotNull(parsed);
        assertEquals("{IF (@x=(TYPE: STRING, VALUE: AAA)) {foo}}", parsed.toString());
    }

    public void testNotLikeInExpression() throws MocaParseException {
        CommandSequence parsed = new MocaParser("if (@x not like '%x') noop").parse();
        assertNotNull(parsed);
    }

    public void testQuotesInSQLCommentCStyle() throws MocaParseException {
        CommandSequence parsed = new MocaParser("[select x from foo /* ' \" */]").parse();
        assertNotNull(parsed);
        assertEquals("{(SQL: select x from foo /* ' \" */)}", parsed.toString());
    }
    
    public void testQuotesInGroovyCommentCStyle() throws MocaParseException {
        CommandSequence parsed = new MocaParser("[[var=2 /* ' \" */]]").parse();
        assertNotNull(parsed);
        assertEquals("{(SCRIPT: var=2 /* ' \" */)}", parsed.toString());
    }
    
    public void testQuotesInGroovyCommentOneLine() throws MocaParseException {
        CommandSequence parsed = new MocaParser("[[\n" + 
                "var=2 // ' \"\n" + 
                "]]").parse();
        assertNotNull(parsed);
        assertEquals("{(SCRIPT: \n" + 
                "var=2 // ' \"\n" + 
                ")}", parsed.toString());
    }
    
    public void testEndingSQLBracketInComment() throws MocaParseException {
        CommandSequence parsed = new MocaParser("[select x from foo -- comment]").parse();
        assertEquals("{(SQL: select x from foo -- comment)}", parsed.toString());
    }
    
    public void testEndingGroovyBracketsInComment() throws MocaParseException {
        try {
            CommandSequence parsed = new MocaParser("[[var=2 // comment]]").parse();
            fail("Expected parse exception, got " + parsed);
        }
        catch (MocaParseException e) {
            // Normal
        }
    }

    public void testAlternateScript() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data | :HQL [[from Foo]]").parse();
        assertNotNull(parsed);
        assertEquals("{publish data | (SCRIPT(hql): from Foo)}", parsed.toString());
    }

    public void testAlternateSql() throws MocaParseException {
        CommandSequence parsed = new MocaParser("publish data | :HQL [from Foo]").parse();
        assertNotNull(parsed);
        assertEquals("{publish data | (SQL(hql): from Foo)}", parsed.toString());
    }

    public void testOverridenCommand() throws MocaParseException {
        CommandSequence parsed = new MocaParser("^convert list where @*").parse();
        assertNotNull(parsed);
        assertEquals("{^convert list WHERE _ALL_ARGS_ REFALL null}", parsed.toString());
    }

    public void testCommandReferences() throws MocaParseException {
        MocaParser parser = new MocaParser("test foo where x = null and @*");
        parser.parse();
        
        Collection<CommandReference> references = parser.getCommandReferences();
        assertNotNull(references);
        assertEquals(1, references.size());
        Iterator<CommandReference> i = references.iterator();
        CommandReference ref = i.next();
        assertEquals("test foo", ref.getVerbNounClause());
        assertEquals(false, ref.isOverride());
    }

    public void testMultipleCommandReferences() throws MocaParseException {
        MocaParser parser = new MocaParser("test foo where x = null and @*; ^test bar where y = to_blah(xyz) | test foo");
        parser.parse();
        
        Collection<CommandReference> references = parser.getCommandReferences();
        assertNotNull(references);
        assertEquals(4, references.size());
        Iterator<CommandReference> i = references.iterator();
        CommandReference ref = i.next();
        assertEquals("test foo", ref.getVerbNounClause());
        assertEquals(false, ref.isOverride());
        ref = i.next();
        assertEquals("test bar", ref.getVerbNounClause());
        assertEquals(true, ref.isOverride());
        ref = i.next();
        assertEquals("xyz", ref.getVerbNounClause());
        assertEquals(false, ref.isOverride());
        ref = i.next();
        assertEquals("to_blah", ref.getVerbNounClause());
        assertEquals(false, ref.isOverride());
    }

    public void testSuppressedReferenceChecking() throws MocaParseException {
        MocaParser parser = new MocaParser("@SuppressWarnings(noref) test foo where x = null and @* | test bar & @SuppressWarnings test blah where x = xyz; ^test bar");
        parser.parse();
        
        Collection<CommandReference> references = parser.getCommandReferences();
        assertNotNull(references);
        assertEquals(2, references.size());
        Iterator<CommandReference> i = references.iterator();
        CommandReference ref = i.next();
        assertEquals("test bar", ref.getVerbNounClause());
        assertEquals(false, ref.isOverride());
        ref = i.next();
        assertEquals("test bar", ref.getVerbNounClause());
        assertEquals(true, ref.isOverride());
    }

    public void testSuppressedReferenceCheckingNested() throws MocaParseException {
        MocaParser parser = new MocaParser("test foo where x = null and @* | @SuppressWarnings { " +
        		"test bar;" +
        		"test baz & @SuppressWarnings(\"noref\") test blah where x = xyz |" +
        		"test more" +
        		"} | pubish data");
        parser.parse();
        
        Collection<CommandReference> references = parser.getCommandReferences();
        assertNotNull(references);
        assertEquals(2, references.size());
        Iterator<CommandReference> i = references.iterator();
        CommandReference ref = i.next();
        assertEquals("test foo", ref.getVerbNounClause());
        assertEquals(false, ref.isOverride());
        ref = i.next();
        assertEquals("pubish data", ref.getVerbNounClause());
        assertEquals(false, ref.isOverride());
    }
    
    public void testDepthWarning() throws MocaParseException {
        MocaParser parser = new MocaParser("test foo | test foo | test foo | test foo | test foo | test foo |" +
                                            "test foo | test foo | test foo | test foo | test foo | test foo |" +
                                            "test foo | test foo | test foo | test foo | test foo | test foo |" +
                                            "test foo | test foo | test foo | test foo | test foo | test foo |" +
                                            "test foo | test foo | test foo | test foo | test foo | test foo |" +
                                            "test foo | test foo | test foo | test foo | test foo | test foo", 32);
        parser.parse();
        
        Collection<MocaSyntaxWarning> warnings = parser.getWarnings();
        assertNotNull(warnings);
        assertEquals(1, warnings.size());
        Iterator<MocaSyntaxWarning> i = warnings.iterator();
        MocaSyntaxWarning ref = i.next();
        String type = ref.getType();
        assertEquals("depth", type);
    }

}
