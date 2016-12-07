/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.servlet.xstream.converters;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;

/**
 * This is a converter that can be used to basically remove a wrapped value
 * from an Unmodifiable map.  The deserialization may or may not work properly
 * for nested elements and also the type of map is lost so we assume 
 * LinkedHashMap
 * 
 * Copyright (c) 2012 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
public class UnmodifiableMapConverter extends MapConverter {
    /**
     * @param mapper
     */
    public UnmodifiableMapConverter(final Mapper mapper) {
        super(mapper);
    }

    @SuppressWarnings("rawtypes")
    public boolean canConvert(final Class type) {
        return Collections.unmodifiableMap(Collections.emptyMap()).getClass() == type;
    }

    public Object unmarshal(final HierarchicalStreamReader reader,
        final UnmarshallingContext context) {
        Map<Object, Object> map = new LinkedHashMap<Object, Object>();
        populateMap(reader, context, map);
        return Collections.unmodifiableMap(map);
    }
}
