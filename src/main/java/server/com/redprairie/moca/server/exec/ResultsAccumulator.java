/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

import java.util.EnumSet;

import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.server.TypeMismatchException;
import com.redprairie.moca.util.MocaUtils;

/**
 * Accumulator for results that must be combined.  This type of results combining
 * is the more aggressive combination method that allows columns of differing types
 * to be 
 * @author dinksett
 */
public class ResultsAccumulator {
    
    public ResultsAccumulator() {
        _res = null;
    }
    
    public ResultsAccumulator(MocaResults source) {
        if (source instanceof SimpleResults) {
            _res = (SimpleResults) source;
        }
        else {
            _res = new SimpleResults();
            MocaUtils.copyResults(_res, source);
        }
    }
    
    public void addResults(MocaResults source) throws TypeMismatchException {
        
        // First, if we don't have a results object yet, just use the one passed.
        if (_res == null) {
            if (source instanceof SimpleResults) {
                _res = (SimpleResults) source;
            }
            else {
                _res = new SimpleResults();
                MocaUtils.copyResults(_res, source);
            }
            return;
        }
        
        // OK, now we need to do the actual merge.
        int columns = source.getColumnCount();
        int resColumns = _res.getColumnCount();
        
        if (columns == resColumns) {
            boolean matches = true;
            for (int i = 0; i < columns; i++) {
                if (source.getColumnType(i) != _res.getColumnType(i) || 
                        !source.getColumnName(i).equals(_res.getColumnName(i))) {
                    matches = false;
                    break;
                }
            }
            
            if (matches) {
                RowIterator row = source.getRows();
                
                while (row.next()) {
                    MocaUtils.copyCurrentRowByIndex(_res, row);
                }
                return;
            }
        }
        
        // This is kept to determine if we're coercing
        // a column type.
        MocaType[] coercedType = new MocaType[columns];
        
        for (int i = 0; i < columns; i++) {
            String columnName = source.getColumnName(i);
            MocaType type = source.getColumnType(i);
            int maxLength = source.getMaxLength(i);
            
            // Keep track of which columns we're going to have to do more work
            // with.  Ideally, all columns will match types exactly.  Sometimes,
            // however, we'll need to add by name, and sometimes we'll need to
            // promote the column first, then add by name.
            if (!_res.containsColumn(columnName)) {
                _res.addColumn(columnName, type, maxLength);
                coercedType[i] = type;
            }
            else {
                MocaType toType = _res.getColumnType(columnName);
                int toIndex = _res.getColumnNumber(columnName);
                if (toType != type || toIndex != i) {
                    coercedType[i] = getPromotedType(type, toType);
                    _res.promoteColumn(toIndex, coercedType[i]);
                }
                else {
                    coercedType[i] = null;
                }
            }
        }
        
        RowIterator sourceRows = source.getRows();

        //
        // Now loop through the results as given.
        //
        while (sourceRows.next()) {
            _res.addRow();
            for (int i = 0; i < columns; i++) {
                Object value = sourceRows.getValue(i);
                
                // The presence of a value in this slot means that we'll need
                // to set the value by name, and that there might be a type
                // promotion involved.
                if (coercedType[i] != null) {
                    MocaType type = source.getColumnType(i);
                    
                    Object coercedValue;
                    if (type == coercedType[i]) {
                        coercedValue = value;
                    }
                    else {
                        MocaValue tmp = new MocaValue(type, value);
                        coercedValue = tmp.asType(coercedType[i]);
                    }
                    
                    // We have to set the value by name.
                    String name = source.getColumnName(i);
                    _res.setValue(name, coercedValue);
                }
                else {
                    _res.setValue(i, value);
                }
            }
        }
    }
    
    public MocaResults getResults() {
        return _res;
    }
    
    private MocaType getPromotedType(MocaType orig, MocaType toAdd) throws TypeMismatchException {
        if (!PROMOTABLE_TYPES.contains(orig) || !PROMOTABLE_TYPES.contains(toAdd)) {
            throw new TypeMismatchException(orig.name());
        }
        
        if (toAdd.compareTo(orig) < 0) {
            return orig;
        }
        else {
            return toAdd;
        }
    }
    
    private SimpleResults _res;
    private final static EnumSet<MocaType> PROMOTABLE_TYPES = EnumSet.of(
        MocaType.BOOLEAN,
        MocaType.INTEGER,
        MocaType.DOUBLE,
        MocaType.DATETIME,
        MocaType.STRING);
}
