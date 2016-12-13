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

package com.redprairie.moca.dao;

import java.util.List;

/**
 * Interface for defining the API to the DAO implementations that have an
 * unknown primary key or composite key.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * @param <T>
 * @author wburns
 */
public interface UnknownKeyDAO<T> {
    
    /**
     * Read all entries from the session.
     * @return Set<T> List of elements from the session.
     */
    public List<T> readAll();
    
    /**
     * Save or update an object off to the database.
     * @param <T> Object to save to the in the current session.
     */
    public void save(T object);

    /**
     * Delete the object from the session.. 
     * @param <T> object The object to delete from the session. 
     * @return void
     */
    public void delete(T object);
    
    /**
     * Flush the current context to the database.
     * @return void
     */
    public void flush();
    
    /**
     * Create a new criteria object.
     * @return DAOCriteria<T> The newly created criteria object.
     */
    public DAOCriteria<T> createEmptyCriteria();
}
