/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.sam.moca;

/**
 * Results returned from a query that uses the
 * #limit hint to specify row limiting/pagination should be used.
 * This result set optionally will have an additional total
 * row count that is the number of rows that would be returned
 * had row limiting NOT been used. For example, if I have a query that
 * would return 100 rows but is limited to rows 25-50 then {@link #getRowCount()} 
 * would return 25 but {@link #getTotalRowCount()} would return 100 which is the number
 * of rows had the limit not been applied
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public interface PagedResults extends EditableResults {
    
    /**
     * Gets the total number of possible rows had row limiting
     * not been used.
     * @return The total number of rows
     */
    public int getTotalRowCount();

}
