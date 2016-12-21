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

package com.sam.moca.server.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import javax.transaction.xa.XAResource;

import com.sam.moca.TransactionHook;

public interface MocaTransaction {

    public Object removeAttribute(String name);

    public Object getAttribute(String name);

    public void setAttribute(String name, Object value);

    public void addHook(TransactionHook hook);

    public Collection<TransactionHook> hooks();

    public void clearAll();

    public Connection getConnection() throws SQLException;

    public boolean isOpen();

    public void commit() throws SQLException;

    public void rollback() throws SQLException;

    public void setSavepoint(String name) throws SQLException;

    public void rollbackToSavepoint(String name) throws SQLException;

    public void suspend() throws SQLException;
    
    public void resume() throws SQLException;
    
    public void addXAResource(XAResource resource) throws SQLException;
}