/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.redprairie.moca.server.db.translate;

import java.util.List;

import com.redprairie.moca.server.db.BindList;

/**
 * A noop implementation of a LimitHandler used
 * for dialects that do not yet implemented limit
 * handling functionality.
 * 
 * Copyright (c) 2013 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
class NoopLimitHandler implements LimitHandler {
    
    /**
     * Gets the instance of the noop handler, this component is
     * stateless so we don't need multiple instances
     * @return The NoopLimitHandler
     */
    static NoopLimitHandler getInstance() {
        return INSTANCE;
    }
    
    private NoopLimitHandler() {}

    // @see com.redprairie.moca.server.db.translate.LimitHandler#addLimit(java.lang.String, int, int, com.redprairie.moca.server.db.BindList, boolean)
    @Override
    public String addLimit(String sql, int startRow, int rowLimit,
            BindList args, boolean findTotal) {
        return sql;
    }
    
    @Override
    public void rebindLimitVariables(int startRow, int rowLimit, BindList args) {
    }

    // @see com.redprairie.moca.server.db.translate.LimitHandler#getExcludedColumns()
    @Override
    public List<String> getExcludedColumns() {
        return null;
    }

    // @see com.redprairie.moca.server.db.translate.LimitHandler#getTotalColumnName()
    @Override
    public String getTotalColumnName() {
        return null;
    }

    // @see com.redprairie.moca.server.db.translate.LimitHandler#handleStartRow(int)
    @Override
    public int handleStartRow(int startRow) {
        return startRow;
    }
    
    // @see com.redprairie.moca.server.db.translate.LimitHandler#supportsLimit()
    @Override
    public boolean supportsLimit() {
        return false;
    }

    // @see com.redprairie.moca.server.db.translate.LimitHandler#supportsTotalCount()
    @Override
    public boolean supportsTotalCount() {
        return false;
    }

    private static final NoopLimitHandler INSTANCE = new NoopLimitHandler();
}
