/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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

package com.redprairie.moca.server.dispatch;

/**
 * A class to resolve message codes into their locale-specific, user-readable
 * forms.
 * 
 * TODO Some implementation of this class must either reference the locale or be
 * created with a locale reference in its constructor.
 * 
 * <b><pre>
 * Copyright (c) 2009 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public interface MessageResolver {
    /**
     * Looks up the given key in the message catalog.
     * 
     * @param key the message key to look up in the message catalog.
     * @return a locale-specific message for the given message key.
     */
    public String getMessage(String key);
}
