/*
 * Copyright (C) 2011 XStream Committers.
 * All rights reserved.
 *
 * The software in this package is published under the terms of the BSD

import org.apache.logging.log4j.LogManager; * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 * 
 * Created on 19. April 2011 by Joerg Schaible
 */
package com.sam.moca.servlet.xstream.converters;

import org.hibernate.collection.internal.PersistentMap;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.mapper.Mapper;


/**
 * COPIED from XSTREAM HIBERNATE MODULE to SUPPORT HIBERNATE 4
 * A converter for Hibernate's {@link PersistentMap}. The converter will drop any reference to
 * the Hibernate collection and emit at serialization time an equivalent JDK collection instead.
 * 
 * @author J&ouml;rg Schaible
 * @since 1.4
 */
public class HibernatePersistentMapConverter extends MapConverter {

    /**
     * Construct a HibernatePersistentMapConverter.
     * 
     * @param mapper
     * @since 1.4
     */
    public HibernatePersistentMapConverter(final Mapper mapper) {
        super(mapper);
    }

    @SuppressWarnings("rawtypes")
    public boolean canConvert(final Class type) {
        return type == PersistentMap.class;
    }

    public Object unmarshal(final HierarchicalStreamReader reader,
        final UnmarshallingContext context) {
        throw new ConversionException("Cannot deserialize Hibernate collection");
    }
}
