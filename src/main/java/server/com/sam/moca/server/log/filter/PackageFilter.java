/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

package com.sam.moca.server.log.filter;

import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

import com.sam.util.ArgCheck;

/**
 * Filter that filters based on package and is also a composite filter
 * using the defined inner filter. The order in which the inner filter
 * and package filter are evaluated is defined via the evaluateInnerFirst
 * constructor arguments. When evaluating the package filter if the package
 * is not present the returned Result is NEUTRAL allowing filtering to
 * take place further down the chain of filters and logger configurations.
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author wburns
 */
@Plugin(name = "PackageFilter", category = "Core", elementType = "filter", printObject = true)
public class PackageFilter extends AbstractFilter {

    private final Filter firstFilter;
    private final Filter secondFilter;
    
    PackageFilter(PackageLookup includePackages, PackageLookup excludePackages, 
        Filter innerFilter, boolean evaluateInnerFirst) {
        this(includePackages, excludePackages,
                innerFilter, evaluateInnerFirst,
                new InternalPackageFilter(includePackages, excludePackages));
        
    }
    
    // Primarily for testing to provide the internal package filter
    PackageFilter(PackageLookup includePackages, PackageLookup excludePackages, 
            Filter innerFilter, boolean evaluateInnerFirst, Filter internalPackageFilter) {
        if (innerFilter == null) {
            throw new NullPointerException("PackageFilter requires a filter");
        }

        if (evaluateInnerFirst) {
            this.firstFilter = innerFilter;
            this.secondFilter = internalPackageFilter;
        }
        else {
            this.firstFilter = internalPackageFilter;
            this.secondFilter = innerFilter;
        }
    }
    
    // @see org.apache.logging.log4j.core.filter.AbstractFilter#filter(org.apache.logging.log4j.core.LogEvent)
    @Override
    public Result filter(LogEvent event) {
        Result result = firstFilter.filter(event);
        if (result == Result.ACCEPT) {
            result = secondFilter.filter(event);
        }

        return result;
    }
    
    // @see org.apache.logging.log4j.core.filter.AbstractFilter#filter(org.apache.logging.log4j.core.Logger, org.apache.logging.log4j.Level, org.apache.logging.log4j.Marker, org.apache.logging.log4j.message.Message, java.lang.Throwable)
    @Override
    public Result filter(Logger logger, Level level, Marker marker,
        Message msg, Throwable t) {
        Result result = firstFilter.filter(logger, level, marker, msg, t);
        if (result == Result.ACCEPT) {
            result = secondFilter.filter(logger, level, marker, msg, t);
        }

        return result;
    }
    
    // @see org.apache.logging.log4j.core.filter.AbstractFilter#filter(org.apache.logging.log4j.core.Logger, org.apache.logging.log4j.Level, org.apache.logging.log4j.Marker, java.lang.Object, java.lang.Throwable)
    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg,
        Throwable t) {
        Result result = firstFilter.filter(logger, level, marker, msg, t);
        if (result == Result.ACCEPT) {
            result = secondFilter.filter(logger, level, marker, msg, t);
        }
        
        return result;
    }
    
    // @see org.apache.logging.log4j.core.filter.AbstractFilter#filter(org.apache.logging.log4j.core.Logger, org.apache.logging.log4j.Level, org.apache.logging.log4j.Marker, java.lang.String, java.lang.Object[])
    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg,
        Object... params) {
        Result result = firstFilter.filter(logger, level, marker, msg, params);
        if (result == Result.ACCEPT) {
            result = secondFilter.filter(logger, level, marker, msg, params);
        }

        return result;
    }
    
    // Handles filtering based on package lookups
    private static class InternalPackageFilter implements Filter {
        
        private final PackageLookup includePackages;
        private final PackageLookup excludePackages;
        
        InternalPackageFilter(PackageLookup includePackages, PackageLookup excludePackages) {
            this.includePackages = includePackages;
            this.excludePackages = excludePackages;
        }

        @Override
        public Result getOnMismatch() {
            return Result.NEUTRAL;
        }

        @Override
        public Result getOnMatch() {
            return Result.ACCEPT;
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker,
                String msg, Object... params) {
            return packageFilter(logger.getName());
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker,
                Object msg, Throwable t) {
            return packageFilter(logger.getName());
        }

        @Override
        public Result filter(Logger logger, Level level, Marker marker,
                Message msg, Throwable t) {
            return packageFilter(logger.getName());
        }

        @Override
        public Result filter(LogEvent event) {
            return packageFilter(event.getLoggerName());
        }
        
        private Result packageFilter(String loggerName) {
            return isPackagePresent(loggerName) ? getOnMatch() : getOnMismatch();
        }
        
        private boolean isPackagePresent(String loggerName) {
            return includePackages.isPackagePresent(loggerName) && 
                    !excludePackages.isPackagePresent(loggerName);
        }
        
    }
    
    @PluginFactory
    public static PackageFilter createFilter(@PluginElement("filters") final Filter[] filters,
        @PluginAttribute("includePackages") String includePackages,
        @PluginAttribute("excludePackages") String excludePackages,
        @PluginAttribute("innerFirst") String innerFirst) {
        if (filters == null || filters.length == 0) {
            throw new IllegalArgumentException("PackageFilter must be provided 1 filter, none were provided.");
        }
        else if (filters.length > 1) {
            throw new IllegalArgumentException("PackageFilter can only be configured with 1 filter, more than one was provided.");
        }
        
        PackageLookup includeLookup = getLookup(true, includePackages);
        PackageLookup excludeLookup = getLookup(false, excludePackages);
        // Default behavior is to evaluate the inner filter first if not specified
        boolean evaluateInnerFirst = true;
        if (innerFirst != null) {
            evaluateInnerFirst = ArgCheck.toBoolean(innerFirst,
                "The innerFirst attribute must be true or false for the PackageFilter, provided value: " + innerFirst);
        }

        return new PackageFilter(includeLookup, 
                excludeLookup, filters[0], evaluateInnerFirst);
    }
    
    private static PackageLookup getLookup(boolean emptyDefault, String packages) {
        if (packages == null || packages.isEmpty()) {
            return new BooleanPackageLookup(emptyDefault);
        }
        String[] packagesArray = packages.split(Pattern.quote(","));
        if (packagesArray.length == 1) {
            return new SinglePackageLookup(packagesArray[0]);
        }
        else {
            PackageTrieNode packageTrie = new PackageTrieNode();
            for (String packageString : packagesArray) {
                packageTrie.addPackage(packageString);
            }
            return packageTrie;
        }
    }
}
