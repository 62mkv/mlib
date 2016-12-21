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
package com.sam.moca.db;

import com.sam.moca.EditableResults;
import com.sam.moca.MocaException;
import com.sam.moca.server.db.BindList;

/**
 * A hook used to modify MOCA SQL queries.
 */
public interface QueryHook {
    /**
     * The query advisor interface. This interface is used by the MOCA SQL
     * adapter to allow modification of incoming queries as well as results. The
     * same advisor instance will be used for modifying the query as well as its
     * results, so if the implementation wants to retain information between
     * calls, it can.
     * 
     * All of the methods of this interface are allowed to throw MOCAException
     * (not SQLException), to indicate an error that occurred with the query or
     * results. This can include application-level errors, and the correct error
     * code will be returned to the caller.
     */
    public interface QueryAdvisor {
        /**
         * Called before execution of a query. This method is actually called
         * before SQL translation, which means it will be in the "MOCA" database
         * dialect (simplified Oracle SQL).
         * 
         * @param sql the query as passed to the MOCA SQL engine. This query may
         *            contain parameters (bind variables). This parameter will
         *            never be null.
         * @param bindList a list of bind variables to be used in this query.
         *         This parameter might be null.
         * @return a query to be used instead of the passed-in query. This
         *         method must not return <code>null</code>.
         * @throws MocaException if an error occurs.
         */
        public String adviseQuery(String sql, BindList bindList) throws MocaException;

        /**
         * Called after columns have been defined on the results object. The
         * <code>res</code> parameter is a mutable results object, and should be
         * modified in place. It is assumed that no columns are being removed
         * from the results, and that reordering of columns will not be done.
         * 
         * @param res the results object to be operated on.
         * @throws MocaException if an error occurs.
         */
        public void adviseMetadata(EditableResults res) throws MocaException;

        /**
         * Called after each row is populated on the results object. The
         * <code>res</code> parameter is a mutable results object, and should be
         * modified in place. When the results object is passed to this method,
         * its current row is pointing to the first row in the
         * results, edit row is pointing at the last row, and the position of
         * either row does not matter when this operation
         * completes. In particular, it must be possible to call
         * <code>addRow</code> on <code>res</code> after completion of this
         * method.
         * 
         * @param res the results object to e operated.
         * @throws MocaException if an error occurs
         */
        public void adviseRowData(EditableResults res) throws MocaException;
    }

    /**
     * Returns an instance of a query advisor. The advisor returned is only used
     * for the execution of a single query and is not retained for additional
     * calls.
     * 
     * @return an instance of the QueryAdvisor interface. This method must not
     *         return <code>null</code>.
     */
    public QueryAdvisor getQueryAdvisor();
}
