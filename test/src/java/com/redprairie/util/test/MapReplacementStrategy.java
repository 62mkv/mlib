package com.redprairie.util.test;

import java.util.Map;

import com.redprairie.util.StringReplacer.ReplacementStrategy;

/**
 * An implementation of ReplacementStrategy that uses a java.util.Map
 * to hold the replacement mappings.
 * 
 * $URL$
 * $Revision$
 * $Author$
 */

public class MapReplacementStrategy
        implements ReplacementStrategy {
    public MapReplacementStrategy(Map<String, ? extends Object> map) {
        _map = map;
    }
    
    public String lookup(String key) {
        Object value = _map.get(key);
        if (value == null) return null;
        return value.toString();
    }
    
    // Implementation
    private final Map<String, ? extends Object> _map;
}