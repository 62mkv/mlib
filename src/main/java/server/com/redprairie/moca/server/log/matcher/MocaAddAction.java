/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
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

package com.redprairie.moca.server.log.matcher;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import ch.elca.el4j.services.xmlmerge.Action;

/**
 * Custom merge action that handles adding BOTH elements -
 * with the original before the patch.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 */
public class MocaAddAction implements Action {

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "unchecked", "rawtypes"})
    public void perform(Element originalElement, Element patchElement, Element outputParentElement) {
        if (patchElement == null && originalElement == null) {
            // do nothing
        }
        else if (patchElement == null && originalElement != null) {
            outputParentElement.addContent((Element) originalElement.clone());
        }
        else if (patchElement != null && originalElement == null) {
            outputParentElement.addContent((Element) patchElement.clone());
        }
        else {
            final List outputContent = outputParentElement.getContent();
            
            List toAdd = new ArrayList();
            toAdd.add(patchElement);
            toAdd.add(originalElement.clone());
            outputContent.addAll(toAdd);
        }
    }
}
