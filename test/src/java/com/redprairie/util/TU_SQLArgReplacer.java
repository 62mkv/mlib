/*
 *  $URL$
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

package com.redprairie.util;

import junit.framework.TestCase;

import com.redprairie.moca.MocaOperator;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.server.db.BindList;
import com.redprairie.moca.server.exec.ArgumentSource;
import com.redprairie.util.test.MockArgumentSource;

/**
 * Unit tests for SQLArgReplacer class.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author  Derek Inksetter
 * @version $Revision$
 */
public class TU_SQLArgReplacer extends TestCase {
    
    public void testNoArgsWithWildcard() {
        MockArgumentSource mock = new MockArgumentSource();
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where @*", mock);
        
        assertEquals("select * from foo where 1 = 1",
                     scanner.getSQLString());
        
        assertTrue(scanner.getBindList().isEmpty());
    }
    
    public void testSimpleVariableSubstitution() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("foo", MocaOperator.EQ, "aaa", true);
        mock.addArg("bar", MocaOperator.EQ, Integer.valueOf(137), true);
        mock.addArg("baz", MocaOperator.EQ, Double.valueOf(-3.2), true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("insert into foo values (@foo, @bar, @baz)",
            mock);
        
        assertEquals("insert into foo values (:var_foo, :var_bar, :var_baz)",
                     scanner.getSQLString());
        
        assertEquals("aaa", scanner.getBindList().getValue("var_foo"));
        assertEquals(137, scanner.getBindList().getValue("var_bar"));
        assertEquals(-3.2, scanner.getBindList().getValue("var_baz"));
    }

    public void testSystemVariableSubstitution() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("foo", MocaOperator.EQ, "aaa", true);
        mock.addArg("bar", MocaOperator.EQ, Integer.valueOf(137), true);
        mock.addArg("baz", MocaOperator.EQ, Double.valueOf(-3.2), true);
        
        mock.addSystemVar("locale_id", "US_ENGLISH");
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where foo = @foo and bar = @bar and baz = @baz and locale = @@LOCALE_ID",
            ProxyStub.newProxy(ArgumentSource.class, mock));
        
        assertEquals("select * from foo where foo = :var_foo and bar = :var_bar and baz = :var_baz and locale = :var0",
                     scanner.getSQLString());
        
        assertEquals("aaa", scanner.getBindList().getValue("var_foo"));
        assertEquals(137, scanner.getBindList().getValue("var_bar"));
        assertEquals(-3.2, scanner.getBindList().getValue("var_baz"));
        assertEquals("US_ENGLISH", scanner.getBindList().getValue("var0"));
    }

    public void testQualifiedOperatorSubstitution() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("foo", MocaOperator.EQ, "aaa", true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where @+xyz.foo",
            mock);
        
        assertEquals("select * from foo where xyz.foo = :var0",
                     scanner.getSQLString());
        
        assertEquals("aaa", scanner.getBindList().getValue("var0"));
    }

    public void testQualifiedOperatorSubstitutionWithWildcard() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("foo", MocaOperator.EQ, "aaa", true);
        mock.addArg("bar", MocaOperator.LT, "bbb", true);
        mock.addArg("baz", MocaOperator.LIKE, "ccc", true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where @+xyz.foo and @*",
            mock);
        
        assertEquals("select * from foo where xyz.foo = :var0 and bar < :var1 and baz LIKE :var2",
                     scanner.getSQLString());
        
        assertEquals("aaa", scanner.getBindList().getValue("var0"));
        assertEquals("bbb", scanner.getBindList().getValue("var1"));
        assertEquals("ccc", scanner.getBindList().getValue("var2"));
    }

    public void testQualifiedOperatorSubstitutionWithAlternateNameLookup() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("foo", MocaOperator.EQ, "aaa", true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where @+xyz.bar^foo",
            mock);
        
        assertEquals("select * from foo where xyz.bar = :var0",
                     scanner.getSQLString());
        
        assertEquals("aaa", scanner.getBindList().getValue("var0"));

        // Reuse the mock
        scanner = new SQLArgReplacer("select * from foo where @+xyz.bar^fzz and @+foo",
            mock);
        
        assertEquals("select * from foo where 1 = 1 and foo = :var0",
                     scanner.getSQLString());
        
        assertEquals("aaa", scanner.getBindList().getValue("var0"));

    }

    public void testRawSubstitution() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("table_name", MocaOperator.EQ, "orders", true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select count(*) from @table_name:raw",
            mock);
        
        assertEquals("select count(*) from orders",
                     scanner.getSQLString());
        
        assertTrue(scanner.getBindList().isEmpty());
    }
    
    public void testRawBlankSubstitution() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("empty", MocaOperator.EQ, "", true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo @empty:raw",
            mock);
        
        assertEquals("select * from foo ",
                     scanner.getSQLString());
        
        assertTrue(scanner.getBindList().isEmpty());
    }

    public void testRawNullSubstitution() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("nullvalue", MocaOperator.EQ, null, true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where x in (@nullvalue:raw)",
            mock);
        
        assertEquals("select * from foo where x in ()",
                     scanner.getSQLString());
        
        assertTrue(scanner.getBindList().isEmpty());
    }

    public void testNoValueOnStack() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("foo", MocaOperator.EQ, "aaa", true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where bar = @bar",
            mock);
        
        assertEquals("select * from foo where bar = :var_bar",
                     scanner.getSQLString());
        
        assertNull(scanner.getBindList().getValue("var_bar"));
    }

    public void testNoValueOnStackRawSubstitution() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("foo", MocaOperator.EQ, "aaa", true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where bar in (@bar:raw)",
            mock);
        
        assertEquals("select * from foo where bar in ()",
                     scanner.getSQLString());
    }

    public void testWildcardReplacement() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("foo", MocaOperator.EQ, "aaa", true);
        mock.addArg("baz", MocaOperator.LT, Integer.valueOf(137), true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where @*",
            mock);
        
        assertEquals("select * from foo where foo = :var0 and baz < :var1",
                     scanner.getSQLString());
        
        assertEquals("aaa", scanner.getBindList().getValue("var0"));
        assertEquals(137, scanner.getBindList().getValue("var1"));
    }
    
    public void testComplexReplacement() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("abc", MocaOperator.EQ, "aaa", false);
        mock.addArg("def", MocaOperator.NOTNULL, null, false);
        mock.addArg("ghi", MocaOperator.NE, "20081130000402", false);
        mock.addArg("table_name", MocaOperator.EQ, "foo", true);
        mock.addArg("foo", MocaOperator.EQ, "bbb", true);
        mock.addArg("baz", MocaOperator.LT, Integer.valueOf(137), true);
        mock.addArg("bar", MocaOperator.EQ, null, true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select @baz baz, @-baz#keep bazz from @table_name:raw where @+abc and @+blah.def and @+xyz.ghi:date and @+nothere and @*",
            mock);
        
        assertEquals("select :var_baz baz, :var0 bazz from foo where abc = :var1 and blah.def IS NOT NULL and xyz.ghi != TO_DATE(:var2, 'YYYYMMDDHH24MISS') and 1 = 1 and foo = :var3 and baz < :var4 and bar is null",
                     scanner.getSQLString());

        BindList args = scanner.getBindList();
        // First argument is null
        assertNull(args.getValue("var_baz"));
        assertEquals(137, args.getValue("var0"));
        assertEquals(MocaType.INTEGER, args.getType("var0"));
        assertEquals("aaa", args.getValue("var1"));
        assertEquals(MocaType.STRING, args.getType("var1"));
        Object var2Value = args.getValue("var2");
        assertEquals(String.class, var2Value.getClass());
        assertEquals(MocaType.STRING, args.getType("var2"));
        assertEquals("bbb", args.getValue("var3"));
        assertEquals(MocaType.STRING, args.getType("var3"));
        assertEquals(137, args.getValue("var4"));
        assertEquals(MocaType.INTEGER, args.getType("var4"));
    }

    public void testReplacementWithRecursiveReferences() {
        MockArgumentSource mock = new MockArgumentSource();
        mock.addSystemVar("a", "zzz");
        
        mock.addArg("abc", MocaOperator.EQ, "abcabc", false);
        mock.addArg("where", MocaOperator.RAWCLAUSE, "bar = @bar and @+foo", true);
        mock.addArg("foo", MocaOperator.NAMEDCLAUSE, "in (@a, @@a)", true);
        mock.addArg("a", MocaOperator.EQ, "aaa", false);
        mock.addArg("bar", MocaOperator.EQ, "barbarbar", false);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select @abc from foo where @+xxx.abc and @+foo and @* and @+a and @+z",
            mock);
        
        assertEquals("select :var_abc from foo where xxx.abc = :var0 and foo in (:var_a, :var1) and bar = :var_bar and foo in (:var_a, :var2) and a = :var3 and 1 = 1",
                     scanner.getSQLString());

        BindList args = scanner.getBindList();
        // First argument is null
        assertEquals("abcabc", args.getValue("var_abc"));
        assertEquals("abcabc", args.getValue("var0"));
        assertEquals("aaa", args.getValue("var_a"));
        assertEquals("zzz", args.getValue("var1"));
        assertEquals("barbarbar", args.getValue("var_bar"));
        assertEquals("zzz", args.getValue("var2"));
        assertEquals("aaa", args.getValue("var3"));
    }

    public void testWildcardReplacementWithOrderBy() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("foo", MocaOperator.EQ, "aaa", true);
        mock.addArg("baz", MocaOperator.LT, Integer.valueOf(137), true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where @* order by bar",
            mock);
        
        assertEquals("select * from foo where foo = :var0 and baz < :var1 order by bar",
                     scanner.getSQLString());
        
        assertEquals("aaa", scanner.getBindList().getValue("var0"));
        assertEquals(137, scanner.getBindList().getValue("var1"));
    }
    
    public void testNoReplacementForSingleAtSign() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("bar", MocaOperator.LT, "bar", true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo @ sysA where @*",
            mock);
        
        assertEquals("select * from foo @ sysA where bar < :var0",
                     scanner.getSQLString());
        
        assertEquals("bar", scanner.getBindList().getValue("var0"));
    }
    
    public void testWildcardReplacementWithNull() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("bar", MocaOperator.EQ, null, true);
        mock.addArg("baz", MocaOperator.NE, null, true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where @*",
            mock);
        
        assertEquals("select * from foo where bar is null and baz is not null",
                     scanner.getSQLString());
        
        assertTrue("The bind list should be empty", scanner.getBindList().isEmpty());
    }
   
    public void testWildcardReplacementWithEmptyString() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("bar", MocaOperator.EQ, "", true);
        mock.addArg("baz", MocaOperator.NE, "", true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where @*",
            mock);
        
        assertEquals("select * from foo where bar is null and baz is not null",
                     scanner.getSQLString());
        
        assertTrue("The bind list should be empty", scanner.getBindList().isEmpty());
    }
   
    public void testAtPlusSubstitutionWithNull() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("bar", MocaOperator.EQ, null, true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where @+bar",
            mock);
        
        assertEquals("select * from foo where bar is null",
                     scanner.getSQLString());
        
        assertTrue("The bind list should be empty", scanner.getBindList().isEmpty());
    }
    
    public void testAtPlusSubstitutionAndAliasWithNull() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("bar", MocaOperator.EQ, "bar", true);
        mock.addArg("foobar", MocaOperator.EQ, null, true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where @+bar^foobar",
            mock);
        
        assertEquals("select * from foo where bar is null",
                     scanner.getSQLString());
        
        assertTrue("The bind list should be empty", scanner.getBindList().isEmpty());
    }
    
    public void testAtMinusSubstitutionAndNotOnStack() {
        MockArgumentSource mock = new MockArgumentSource();
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where bar = @-bar",
            mock);
        
        assertEquals("select * from foo where bar = :var0",
                     scanner.getSQLString());
        assertTrue(scanner.getBindList().contains("var0"));
        assertEquals(null, scanner.getBindList().getValue("var0"));
    }
    
    public void testEmptyStringValueOnStack() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("foo", MocaOperator.EQ, "", true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from bar where foo = @foo",
            mock);
        
        assertEquals("select * from bar where foo = :var_foo",
                     scanner.getSQLString());
        
        assertNull(scanner.getBindList().getValue("var_foo"));
    }
        
    public void testCommentWithSingleQuote() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("bar", MocaOperator.EQ, "XYZ", true);
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo /* ABC's comment */ where bar = @bar",
            mock);
        
        assertEquals("select * from foo /* ABC's comment */ where bar = :var_bar",
            scanner.getSQLString());
        assertEquals("XYZ", scanner.getBindList().getValue("var_bar"));
    }
        
    public void testLikeReplacement() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("car_move_id", MocaOperator.EQ, "cmv%", true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where @%car_move_id",
            mock);
        
        assertEquals("select * from foo where car_move_id LIKE :var0",
                     scanner.getSQLString());
        
        assertEquals("cmv%", scanner.getBindList().getValue("var0"));
    }
    
    /**
     * When using @%<variable> we should only actually use a LIKE operator
     * if the actual value passed is using a wildcard (otherwise it serves no purpose/results in overhead).
     */
    public void testLikeNotReplacedWithoutWildcard() {
        MockArgumentSource mock = new MockArgumentSource();
        
        mock.addArg("car_move_id", MocaOperator.EQ, "cmv", true);
        
        SQLArgReplacer scanner = new SQLArgReplacer("select * from foo where @%car_move_id",
            mock);
        
        assertEquals("select * from foo where car_move_id = :var0",
                     scanner.getSQLString());
        
        assertEquals("cmv", scanner.getBindList().getValue("var0"));
    }
}
