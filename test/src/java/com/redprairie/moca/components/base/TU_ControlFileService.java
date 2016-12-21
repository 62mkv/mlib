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

package com.sam.moca.components.base;

import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.RowIterator;
import com.sam.moca.util.AbstractMocaTestCase;

/**
 * This is to test the commands related to control file usage
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class TU_ControlFileService extends AbstractMocaTestCase {
    /**
     * This will test the formatting of a load control file
     */
    public void testFormatControlFileLoad() {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                        "format control file " +
            		"  where type = 'L' " +
            		"    and table_name = 'moca_dataset'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        // These next asserts all test the various output
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("[ select count(*) row_count from moca_dataset where", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("    ds_name = '@ds_name@' and ds_desc = '@ds_desc@' and ds_dir = '@ds_dir@' and ds_seq = @ds_seq@ ] | if (@row_count > 0) {", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("       [ update moca_dataset set", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("          ds_name = '@ds_name@'", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals(",          ds_desc = '@ds_desc@'", rowIter.getString("FORMATED_DATA"));

        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals(",          ds_dir = '@ds_dir@'", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals(",          ds_seq = @ds_seq@", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("             where  ds_name = '@ds_name@' and ds_desc = '@ds_desc@' and ds_dir = '@ds_dir@' and ds_seq = @ds_seq@ ] }", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("             else { [ insert into moca_dataset", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("                      (ds_name, ds_desc, ds_dir, ds_seq)", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("                      VALUES", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("                      ('@ds_name@', '@ds_desc@', '@ds_dir@', @ds_seq@) ] }", rowIter.getString("FORMATED_DATA"));
        
        assertFalse(rowIter.next());
    }
    
    /**
     * This tests to make sure that when formatting a control file with unload
     * that it returns correctly
     */
    public void testFormatControlFileUnload() {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                        "format control file " +
                        "  where type = 'U' " +
                        "    and table_name = 'moca_dataset'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        // These next asserts all test the various output
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("[ select", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("'@base_filename@'||'.'||lower('@format_mode@') file_name,", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("   '@format_mode@' dump_mode,", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("   '@append_mode@' dump_append", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("   from dual ] | dump data where dump_command =", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("    '[ select", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("    ds_name,", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("    ds_desc,", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("    ds_dir,", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("    ds_seq,", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("    ''@format_mode@'' format_mode,", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("    ''[ select", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        moca_dataset.*", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        from moca_dataset where", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        ds_name  = ''''''||ds_name||''''''", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        and", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        ds_desc  = ''''''||ds_desc||''''''", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        and", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        ds_dir  = ''''''||ds_dir||''''''", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        and", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        ds_seq  = to_char(''||to_char(ds_seq)||'' )", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        ] '' command from moca_dataset where", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        ds_name like nvl(''@ds_name@'',''%'')", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        and", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        ds_desc like nvl(''@ds_desc@'',''%'')", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        and", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        ds_dir like nvl(''@ds_dir@'',''%'')", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        and", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        ds_seq like nvl(''@ds_seq@'',''%'')", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("        ] | { if( ''@format_mode@'' = ''XML'') {", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("                 format data where format_mode = ''XMLSTAG'' and tag = ''moca_dataset''  &", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("                 format data &", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("                 format data where format_mode = ''XMLETAG'' and tag = ''moca_dataset''", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("                 } else { format data } } '", rowIter.getString("FORMATED_DATA"));
        
        assertFalse(rowIter.next());
    }
    
    /**
     * This tests to make sure that format control file works correctly with
     * data type
     */
    public void testFormatControlFileDataWithNoCommand() {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                        "format control file " +
                        "  where type = 'D' " +
                        "    and table_name = 'moca_dataset'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        // These next asserts all test the various output
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("base_filename,format_mode,append_mode,ds_name,ds_desc,ds_dir,ds_seq", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("%UNLOAD_DIR%/moca_dataset/moca_dataset,CSV,F,%,%,%,%", rowIter.getString("FORMATED_DATA"));
        
        assertFalse(rowIter.next());
    }
    
    /**
     * This tests to make sure that format control file works correctly with
     * data type
     */
    public void testFormatControlFileData() {
        MocaResults res = null;
        try {
            res = _moca.executeCommand(
                        "format control file " +
                        "  where type = 'D' " +
                        "    and table_name = 'moca_dataset'" +
                        "    and where_command = '[select * from moca_dataset]'");
        }
        catch (MocaException e) {
            e.printStackTrace();
            fail("Unexpected MOCA Exception :" + e);
        }
        
        RowIterator rowIter = res.getRows();
        
        // These next asserts all test the various output
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("base_filename,format_mode,append_mode,ds_name,ds_desc,ds_dir,ds_seq", rowIter.getString("FORMATED_DATA"));
        
        assertTrue(rowIter.next());
        assertEquals("DATA", rowIter.getString("rowtype"));
        assertEquals("%UNLOAD_DIR%/moca_dataset/moca_dataset,CSV,F,MOCA-Base,MOCA Base Data,$MOCADIR/db/data/load/base,1", rowIter.getString("FORMATED_DATA"));
        
        assertFalse(rowIter.next());
    }
}
