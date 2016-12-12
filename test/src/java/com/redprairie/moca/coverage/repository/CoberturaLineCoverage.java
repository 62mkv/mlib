/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2011
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

package com.redprairie.moca.coverage.repository;

import java.io.Serializable;

import net.sourceforge.cobertura.coveragedata.ClassData;

import com.redprairie.moca.coverage.LineCoverage;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2011 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class CoberturaLineCoverage implements LineCoverage, Serializable {
    private static final long serialVersionUID = -2894081905471445122L;
    /**
     * 
     */
    public CoberturaLineCoverage(ClassData data, int lineNumber) {
        _data = data;
        _lineNumber = lineNumber;
    }

    // @see com.redprairie.moca.coverage.LineCoverage#lineHit()
    @Override
    public void lineHit() {
        _data.touch(_lineNumber, 1);
    }

    private final ClassData _data;
    private final int _lineNumber;
}
