/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2014
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

package com.redprairie.moca.server.log.matcher;

import ch.elca.el4j.services.xmlmerge.Matcher;
import org.jdom.Element;

/**
 * Matcher that matches elements <b>iff</b> their configured attributes match or both attributes are null.
 * Only one elements' attributes being null does <b>NOT</b> constitute matching.
 * 
 * Copyright (c) 2014 RedPrairie Corporation
 * All Rights Reserved
 */
public class NullableAttributeMatcher implements Matcher {
    
    public NullableAttributeMatcher(String attribute) {
        _attribute = attribute;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean matches(Element originalElement, Element patchElement) {
            return originalElement != null && patchElement != null
                && originalElement.getQualifiedName().equals(patchElement.getQualifiedName())
                && ((originalElement.getAttribute(_attribute) == null && patchElement.getAttribute(_attribute) == null)
                || (originalElement.getAttribute(_attribute) != null && patchElement.getAttribute(_attribute) != null
                    && originalElement.getAttributeValue(_attribute).equals(patchElement.getAttributeValue(_attribute))));
    }

    private final String _attribute;
}