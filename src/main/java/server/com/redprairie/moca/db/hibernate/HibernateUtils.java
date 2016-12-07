/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2015
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

package com.redprairie.moca.db.hibernate;

import java.util.Locale;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2015 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author j1014071
 */
public class HibernateUtils {

    public static final String ORDER_BY = "order by";
    public static final String DISTINCT = "distinct";

    /**
     * Pad {@literal distinct} and {@literal order by} keywords with spaces on both sides.
     * This function is needed because Hibernate SQL Server limit handler logic 
     * assumes sql is formatted in a certain way
     * 
     * @param sql
     * @return
     */
    public static String formatSQLForHibernate(String sql) {

        for(String keyword: new String [] { DISTINCT, ORDER_BY }){
            int startIndex = indexOfKeyWord(sql, keyword, 0, 0);
            sql = padKeyword(sql, startIndex, keyword.length());
        }
        
        return sql;
    }


    /**
     *  Pads keyword with spaces as needed
     *  
     * @param source
     * @param startIndex
     * @param length
     * @return
     */
    public static String padKeyword(String source, int startIndex, int length) {
        if(startIndex >= 0){
            int endIndex = startIndex + length;
            String prefix = source.substring(0, startIndex);
            String keyword = source.substring(startIndex, endIndex);
            String suffix = source.substring(endIndex);
            
            if(!prefix.endsWith(" ")){
                prefix = prefix + " ";
            }
            
            if(!suffix.startsWith(" ")){
                suffix = " " + suffix;
            }
            
            source = prefix + keyword + suffix;
        }
        return source;
    }

    
    /**
     * Performs search for a keyword that is nested n-level deep inside parenthesis
     * NOTE: the code assumes all embedded literals have been bound
     *  
     * @param source
     * @param search
     * @param fromIndex
     * @param depth
     * @return
     */
    public static int  indexOfKeyWord(String source, String search, int fromIndex, int depth) {
        
        String [] delimiters = new String[]{" ", "\n", "\r", "\t"};
        
        for(String startDelimiter: delimiters){
            for(String endDelimiter: delimiters){
                int index = indexOf( source, startDelimiter + search + endDelimiter, fromIndex, true, depth);
                // Add 1 to the result, since index points at the start delimiter
                if(index != -1) return index+1;
            }
        }
        
        return -1;
    }
    
    /**
     * Performs search for a string that is nested n-level deep inside parenthesis
     * NOTE: the code assumes all embedded literals have been bound
     *
     * @param source the string to search.
     * @param search the substring to search for.
     * @param fromIndex the index from which to start the search.
     * @param depth the parenthesis depth level (with {@literal 0} outside of parentheses)
     * @param ignorecase is search case-insensitive 
     *
     * @return the index of the first occurrence of the specified substring, starting at the specified index, or {@literal -1} if there is no such occurrence.
     */
    public static int indexOf(String source, String search, int startIndex, boolean ignorecase, int depth) {
        
        if(ignorecase){
            source = source.toLowerCase(Locale.ROOT);
            search = search.toLowerCase(Locale.ROOT);
        }
        
        final int len = source.length();
        final int searchlen = search.length();
        int pos = -1;
        int currentDepth = 0;
        int currentPos = startIndex;
        do {
            pos = source.indexOf( search, currentPos );
            if ( pos != -1 ) {
                for ( int iter = currentPos; iter < pos; iter++ ) {
                    final char c = source.charAt( iter );
                    if ( c == '(' ) {
                        currentDepth = currentDepth + 1;
                    }
                    else if ( c == ')' ) {
                        currentDepth = currentDepth - 1;
                    }
                }
                currentPos = pos + searchlen;
            }
        } while ( currentPos < len && currentDepth != depth && pos != -1 );
        return currentDepth == depth ? pos : -1;
    }
}
