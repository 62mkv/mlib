/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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
package com.redprairie.moca.db;

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.server.db.BindList;

/**
 * Implementation of QueryHook that does nothing. All queries are returned
 * unchanged, and all results are left alone as well.
 * 
 * @author derek
 */
public class NullQueryHook implements QueryHook {

    public QueryAdvisor getQueryAdvisor() {
        return _instance;
    }

    private static class Advisor implements QueryAdvisor {

        public String adviseQuery(String sql, BindList bindList) {
            return sql;
        }

        public void adviseMetadata(EditableResults res) {
            return;
        }

        public void adviseRowData(EditableResults res) {
            return;
        }
    }

    //
    // Implementation
    //
    private static final Advisor _instance = new Advisor();
}
