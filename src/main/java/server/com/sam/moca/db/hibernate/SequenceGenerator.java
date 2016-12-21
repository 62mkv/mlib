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

package com.sam.moca.db.hibernate;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;

import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaResults;
import com.sam.moca.util.MocaUtils;

/**
 * Hibernate ID Generator using the "generate next number" command.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class SequenceGenerator implements IdentifierGenerator {

    // @see org.hibernate.id.IdentifierGenerator#generate(org.hibernate.engine.SessionImplementor, java.lang.Object)

    public Serializable generate(SessionImplementor arg0, Object arg1)
            throws HibernateException {
        MocaContext moca = MocaUtils.currentContext();
        MocaResults res = null;

        try {
            res = moca.executeCommand(
                    "generate next number where numcod = '" + _numcod + "'");
            if (res.next()) {
                return res.getString("nxtnum");
            }
            throw new HibernateException("sequence generation returned no data.");
        }
        catch (MocaException e) {
            throw new HibernateException("error generating sequence", e);
        }
        finally {
            if (res != null) {
                res.close();
            }
        }
    }
    
    public void setNumcod(String numcod) {
        _numcod = numcod;
    }
    
    private String _numcod;

}
