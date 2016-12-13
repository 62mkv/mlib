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

package com.redprairie.moca.components.base;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.RowIterator;
import com.redprairie.moca.exceptions.InvalidArgumentException;

/**
 * This class handles moca base commands that involve converting columns to
 * different types of results
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class ConversionService {
    /**
     * This method is to convert a list of rows in a single column into
     * a single string
     * @param moca the moca context this was called from
     * @param resultSet the result set to convert
     * @param columnName the column to get the values from
     * @param separator the separator to put in between the values
     * @return a result set containing the string created from the columns
     */
    public MocaResults convertColumnsToString(MocaContext moca, 
            MocaResults resultSet, String columnName, String separator) {
        
        StringBuilder sb = new StringBuilder();
        boolean firstLoop = true;
        RowIterator rowIterator = resultSet.getRows();
        // Loop through results adding it to the buffer
        while (rowIterator.next()) {
            // If we are not the first loop append the separator else we signal
            // that we have gone through the first loop
            if (!firstLoop) {
                sb.append(separator);
            }
            else  {
                firstLoop = false;
            }
            Object value = rowIterator.getValue(columnName);
            
            if (value != null) sb.append(value);
        }
        
        EditableResults res = moca.newResults();
        
        res.addColumn("result_string", MocaType.STRING);
        res.addRow();
        
        res.setStringValue("result_string", sb.toString());
        
        return res;
    }
    
    /**
     * This method is to convert a list of columns into multiple other columns.
     * The desired column count will dictate how many rows and columns there
     * are.  The algorithm does a top down insertion.
     * @param moca the moca context this was called from
     * @param resultSet the restult set to convert 
     * @param columnName the column name to get the values from
     * @param desiredColumnCount the # of columns to create
     * @return a result set containing the # of columns created and rows
     *         subsequently determined by the column count
     */
    public MocaResults convertColumnsToMultiColumn(MocaContext moca, 
            MocaResults resultSet, String columnName, 
            int desiredColumnCount) {
        
        // If our result set is empty just return empty
        int rowCount = resultSet.getRowCount(); 
        if (rowCount == 0) {
            return moca.newResults(); 
        }
        
        /* 
         * Determine how many items to place in each column.
         *
         * Note:  We will always fill top down - so we first 
         *        figure out our minimum number of rows needed
         *        to display this information... next, we consider
         *        the minimum number of columns needed to display
         *        the information based on the min numbers of rows
         *        - it may be that we actually downsize the column
         *        count to accomodate.  
         *
         *        For example:
         *            4 data points, 3 columns
         *        A pure top down, adhering to column request would look like:
         *                1      3       4
         *                2
         *        Or consider 5, across 4 columns
         *                1      3       4     5
         *                2
         *        Instead, what this routine will do is the following:
         *                1     3
         *                2     4
         *        And:
         *                1     3     5
         *                2     4
         *        Adhering strictly to a column count would dictate that
         *        we, in the above cases, should really switch to a L-R 
         *        fill pattern.
         */
        
        // These are the # of rows and columns we will create
        int requiredRows;
        // If we have more columns requested then rows available use how
        // many rows for column and a single row
        if (desiredColumnCount > rowCount) {
            requiredRows = 1;
        }
        // Else we use the desired number of columns and figure out how
        // many rows to put in
        else {
            requiredRows = rowCount / desiredColumnCount + 
                (rowCount % desiredColumnCount == 0 ? 0 : 1);
        }
        
        MocaType type = resultSet.getColumnType(columnName);
        EditableResults res = moca.newResults();
        
        RowIterator rowIterator = resultSet.getRows();
        int currentRowCount = 0;
        int currentColumnCount = 0;
        
        // Loop through all the rows to get the values in a top down fashion
        while (rowIterator.next()) {
            
            // If we are the first row add another column
            if (currentRowCount == 0) {
                res.addColumn(columnName + "_" + currentColumnCount, type);
            }
            
            // If we are still on the first column add a row
            if (currentColumnCount == 0) {
                res.addRow();
            }
            // Else move to the next row
            else {
                res.next();
            }
            
            res.setValue(currentColumnCount, rowIterator.getValue(columnName));
            
            // If we get to the last required row then reset is -1 since row
            // count is not zero based
            if (currentRowCount == requiredRows - 1) {
                currentRowCount = 0;
                currentColumnCount++;
                res.reset();
            }
            else {
                currentRowCount++;
            }
            
        }
        
        return res;
    }
    
    private enum ConversionTypes {
        LIST("L"),
        SINGLEQUOTES("S"),
        DOUBLEQUOTES("D");
        
        /**
         * String Constructor
         */
        private ConversionTypes(String value) {
            _value = value;
        }
        
        public String toString() {
            return _value;
        }
        
        private final String _value;
    }
    
    /**
     * The command takes a string, a separator, and a conversion type and 
     * parses each token from the given string using the given separator, 
     * building a new list of the given conversion type.  If the given 
     * conversion type is a 'L' (list), the given string is parsed, with each 
     * row in the result set being a token from the string.  If the given 
     * conversion type is a 'S', each token in the given string is 
     * single-quoted.  If the given token is a 'D', each token in the given 
     * string is double-quoted.  If a separator is not passed, a comma is used.
     * @param stringList the list to convert into various type
     * @param type the types of conversions ('L', 'S', 'D') 
     * @param separator the separator to divide the list by
     * @return a result set after it has been converted
     */
    public MocaResults convertList(MocaContext moca, String stringList, 
            String type, String separator) throws InvalidArgumentException {
        
        // If the type is not valid, then throw an exception
        if (!Arrays.asList(new String[]{
                ConversionTypes.LIST.toString(),
                ConversionTypes.DOUBLEQUOTES.toString(),
                ConversionTypes.SINGLEQUOTES.toString()
                }).contains(type)) {
            throw new InvalidArgumentException("conversiontype");
        }
        
        EditableResults res = moca.newResults();
        
        res.addColumn("retstr", MocaType.STRING);
        res.addColumn("count", MocaType.INTEGER);
        
        String[] splitString = stringList.split(Pattern.quote(separator));
        StringBuilder sb = new StringBuilder();
        boolean firstString = true;
        
        char token = ' ';
        // If we are not on the first loop skip the separator
        if (ConversionTypes.SINGLEQUOTES.toString().equals(type)) {
            token = '\'';
        }
        else if (ConversionTypes.DOUBLEQUOTES.toString().equals(
                type)) {
            token = '\"';
        }

        for (int i = 0; i < splitString.length; ++i) {
            // If it is a list then iterate over the string array and add rows
            if (ConversionTypes.LIST.toString().equals(type)) {
                res.addRow();
                res.setStringValue("retstr", splitString[i].trim());
                res.setIntValue("count", i + 1);
            }
            // If we aren't list then append the token to each side
            else {
                if (!firstString) {
                    sb.append(separator);
                }
                sb.append(token);
                sb.append(splitString[i].trim());
                sb.append(token);
                
                firstString = false;
            }
        }
        
        // If we have information in the buffer add the row
        if (sb.length() > 0) {
            res.addRow();
            res.setStringValue("retstr", sb.toString());
            res.setIntValue("count", splitString.length);
        }
        
        return res;
    }
}
