/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.sam.moca.util;

import java.util.HashMap;
import java.util.Map;

import com.sam.moca.BeanResults;
import com.sam.moca.EditableResults;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.ModifiableResults;
import com.sam.moca.RowIterator;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class ResultUtils {
    /**
     * Copies the data from one <code>MocaResults</code> object into
     * another one.  The source object's current row pointer is reset
     * before copying the source data, and the source object's current
     * row pointer will be at the end of the result after copying.  I.e,
     * {@link MocaResults#reset() source.reset} will be called before copying,
     * but not after.
     * @param res
     * @param source
     */
    public static void copyResults(EditableResults res, MocaResults source) {
        if (source != null) {
            copyColumns(res, source, false);
            source.reset();
            copyRowsByIndex(res, source);
        }
    }
    
    /**
     * Copies the data from one <code>MocaResults</code> object into
     * another one, reusing columns on the target result set if they are
     * already defined.  The source object's current row pointer is reset
     * before copying the source data, and the source object's current
     * row pointer will be at the end of the result after copying.  I.e,
     * {@link MocaResults#reset() source.reset} will be called before copying,
     * but not after.
     * @param res
     * @param source
     */
    public static void mergeResults(EditableResults res, MocaResults source) {
        if (source != null) {
            copyColumns(res, source, true);
            source.reset();
            copyRows(res, source);
        }
    }
    
    /**
     * Copies the data from one <code>MocaResults</code> object into
     * another one.  It is assumed that columns are already defined in the
     * target result object.  Note that row data is copied from the source
     * to the target result set <i>by name</i>.  That means that if there are
     * duplicate columns in the source and target result set, they will not
     * both be copied to the target.  This is probably not what you want, since
     * the target result set may return null for that column.
     * @param res the target result set.
     * @param source the source result set.
     */
    public static void copyRows(ModifiableResults res, MocaResults source) {
        if (source != null) {
            int columns = source.getColumnCount();

            RowIterator sourceRows = source.getRows();
            //
            // Now loop through the results as given
            //
            while (sourceRows.next()) {
                res.addRow();
                for (int i = 0; i < columns; i++) {
                    
                    Object value = sourceRows.getValue(i);
                    String name = source.getColumnName(i);
                    if (res.containsColumn(name)) {
                        res.setValue(name, value);
                    }
                }
            }
        }
    }
    
    /**
     * Copies the data from one <code>MocaResults</code> object into
     * another one.  It is assumed that columns are already defined in the
     * target result object, and that they correspond to the columns in the 
     * source result set, both in type, name and position.
     * @param res the target result set.
     * @param source the source result set.
     */
    public static void copyRowsByIndex(ModifiableResults res, MocaResults source) {
        if (source != null) {
            int columns = source.getColumnCount();
            
            RowIterator rows = source.getRows();

            //
            // Now loop through the results as given
            //
            while (rows.next()) {
                res.addRow();
                for (int i = 0; i < columns; i++) {
                    
                    if (rows.isNull(i)) {
                        res.setNull(i);
                    }
                    else {
                        Object value = rows.getValue(i);
                        res.setValue(i, value);
                    }
                }
            }
        }
    }
    
    /**
     * Copies the data from the current row of one <code>RowIterator</code>
     * object into another one. It is assumed that columns are already defined
     * in the target result object. Note that row data is copied from the source
     * to the target result set <i>by name</i>.  A new row is added to the target result set.
     * 
     * @param res the target result set
     * @param source the source result set
     */
    public static void copyCurrentRow(ModifiableResults res, RowIterator source) {
        if (source != null) {
            int columns = res.getColumnCount();

            res.addRow();
            for (int i = 0; i < columns; i++) {
                Object value = source.getValue(i);
                String name = res.getColumnName(i);
                res.setValue(name, value);
            }
        }
    }
    
    /**
     * Copies the data from the current row of one <code>RowIterator</code>
     * object into another one. It is assumed that columns are already defined
     * in the target result object. A new row is added to the target result set.
     * 
     * @param res the target result set
     * @param source the source result set
     */
    public static void copyCurrentRowByIndex(ModifiableResults res, RowIterator source) {
        if (source != null) {
            int columns = res.getColumnCount();

            res.addRow();
            for (int i = 0; i < columns; i++) {
                Object value = source.getValue(i);
                res.setValue(i, value);
            }
        }
    }
    
    /**
     * Copies the column information (metadata) from one <code>MocaResults</code> object
     * into another one.  No data is copied into the target result object.  Duplicate
     * columns (i.e. ones having the same name) will be copied. 
     * @param res
     * @param source
     */
    public static void copyColumns(EditableResults res, MocaResults source) {
        copyColumns(res, source, false);
    }
    
    /**
     * Copies the column information (metadata) from one <code>MocaResults</code> object
     * into another one.  No data is copied into the target result object.  If the
     * <code>merge</code> parameter is <code>true</code>, duplicate columns are ignored,
     * and the first column returned from the source results is used.
     * @param res the target result set
     * @param source the source result set.
     * @param merge if <code>true</code>, merge duplicate columns.  Otherwise, duplicate
     * columns are retained.
     */
    public static void copyColumns(EditableResults res, MocaResults source, boolean merge) {
        transformColumns(res, source, merge, null);
    }
    
    /**
     * Copies the column information (metadata) from one <code>MocaResults</code> object
     * into another one.  No data is copied into the target result object.  If the
     * <code>merge</code> parameter is <code>true</code>, duplicate columns are ignored,
     * and the first column returned from the source results is used.  The <code>nameMap</code>
     * argument is a mapping of old-to-new column names.  This method can be combined with the
     * <code>copyRowsByIndex</code> method to create a copy of a result set with modified
     * column names.
     * @param res the target result set
     * @param source the source result set.
     * @param merge if <code>true</code>, merge duplicate columns.  Otherwise, duplicate
     * columns are retained.
     * @param nameMap a mapping of source-to-target column names.  Source column
     * names must be in lower case.
     */
    public static void transformColumns(EditableResults res, MocaResults source, boolean merge, Map<String, String> nameMap) {
        if (source != null) {
            
            // If name mapping is turned on, make sure we have lower-case 
            // column names
            Map<String, String> nameMapLower = null;
            if (nameMap != null && !nameMap.isEmpty()) {
                nameMapLower = new HashMap<String, String>();
                for (Map.Entry<String, String> mapping : nameMap.entrySet()) {
                    nameMapLower.put(mapping.getKey().toLowerCase(), mapping.getValue());
                }
            }
            
            int columns = source.getColumnCount();
            for (int i = 0; i < columns; i++) {
                String columnName = source.getColumnName(i);
                
                // Use the mapped column name, if present. 
                if (nameMapLower != null) {
                    String mappedName = nameMapLower.get(columnName.toLowerCase());
                    if (mappedName != null) {
                        columnName = mappedName;
                    }
                }
                
                MocaType type = source.getColumnType(i);
                int maxLength = source.getMaxLength(i);
                if (!merge || !res.containsColumn(columnName)) {
                    res.addColumn(columnName, type, maxLength);
                }
            }
        }
    }
    
    /**
     * Copies the data from one <code>MocaResults</code> object into
     * an object array.  The JavaBeans properties of the given class
     * define the resulting columns.
     * @param cls the class of the classes to be copied.  The class must have
     * a default constructor.
     * @param source the results object to copy into the new objects.
     */
    public static <T> T[] createObjectArray(Class<? extends T> cls, MocaResults source) {
        BeanResults<T> res = new BeanResults<T>(cls);
        copyRows(res, source);
        return res.getData();
    }
}
