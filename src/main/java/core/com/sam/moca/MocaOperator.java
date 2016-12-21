/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20167
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

public enum MocaOperator {
    NOTNULL("IS NOT NULL"), ISNULL("IS NULL"), EQ("="), NE("!="), LT("<"), LE("<="), GT(">"), GE(">="),
    LIKE("LIKE"), NOTLIKE("NOT LIKE"), RAWCLAUSE("[RAW CLAUSE]"), NAMEDCLAUSE("[NAMED RAW CLAUSE]"),
    REFONE(null), REFALL(null), REFLIKE(null);
    
    public String getSQLForm() {
        return _form;
    }
    private  MocaOperator(String form) {
        _form = form;
    }
    private String _form;
}
