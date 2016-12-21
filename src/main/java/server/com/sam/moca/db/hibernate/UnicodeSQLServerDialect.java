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

import java.sql.Types;

import org.hibernate.dialect.SQLServerDialect;

/**
 * This is a custom dialect class to allow hibernate to know how to convert
 * multi byte type columns to their java equivalent.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class UnicodeSQLServerDialect extends SQLServerDialect {
    public UnicodeSQLServerDialect() {
        super();
        registerHibernateType(Types.NVARCHAR, "string");
        registerHibernateType(Types.NCHAR, "character");
        registerHibernateType(Types.NCLOB, "clob");
    }
}
