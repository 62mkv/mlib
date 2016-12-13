/*
 *  $URL$
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

package com.redprairie.moca.server.log.filter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.message.Message;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class TU_PackageFilter {
    
    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }
    
    // Simple test to make sure that when we specify the inner filter runs first
    // that the package filter gets completely skipped when the inner runs first
    // and returns NEUTRAL/DENY
    @Test
    public void testInnerFilterFirstSkipPackageFilter() {
        PackageFilter pFilter = new PackageFilter(_includeLookup, _excludeLookup,
                _innerFilter, true, _internalPackageFilter);
        mockFilterResults(_innerFilter, Result.NEUTRAL);
        runAllFilterMethods(pFilter, "testing", Result.NEUTRAL);
        verifyMocksCalled(_innerFilter, 1);
        verifyMocksCalled(_internalPackageFilter, 0);
    }
    
    // Tests that inner filter runs first, returns ACCEPT which subsequently runs the package filter
    @Test
    public void testInnerFilterFirstRunsPackageAsInner() {
        PackageFilter pFilter = new PackageFilter(_includeLookup, _excludeLookup,
                _innerFilter, true, _internalPackageFilter);
        mockFilterResults(_innerFilter, Result.ACCEPT);
        mockFilterResults(_internalPackageFilter, Result.NEUTRAL);
        runAllFilterMethods(pFilter, "testing", Result.NEUTRAL);
        verifyMocksCalled(_innerFilter, 1);
        verifyMocksCalled(_internalPackageFilter, 1);
    }
    
    // Tests that package filter runs first, inner filter is skipped due to package filter returning NEUTRAL
    @Test
    public void testPackageRunsFirstSkipsInner() {
        PackageFilter pFilter = new PackageFilter(_includeLookup, _excludeLookup,
                _innerFilter, false, _internalPackageFilter);
        mockFilterResults(_internalPackageFilter, Result.NEUTRAL);
        runAllFilterMethods(pFilter, "testing", Result.NEUTRAL);
        verifyMocksCalled(_internalPackageFilter, 1);
        verifyMocksCalled(_innerFilter, 0);
    }
    
    // Simple test where our logger is a included in the includes packages
    @Test
    public void testActualPackageFilterInclude() {
        mockFilterResults(_innerFilter, Result.ACCEPT);
        PackageFilter pFilter = PackageFilter.createFilter(new Filter[]{_innerFilter},
                "com.redprairie", null, null);
        runAllFilterMethods(pFilter, "com.redprairie.testing", Result.ACCEPT);
        verifyMocksCalled(_innerFilter, 1);
    }
    
    // Simple test that logs with a logger that is not include in our filter
    // The PackageFilter is specified to run the package filter first (rather than the inner filter)
    @Test
    public void testPackageFilterEvalutedFirst() {
        mockFilterResults(_innerFilter, Result.ACCEPT);
        PackageFilter pFilter = PackageFilter.createFilter(new Filter[]{_innerFilter},
                "com.redprairie", null, "false");
        runAllFilterMethods(pFilter, "com.not.included", Result.NEUTRAL);
        verifyMocksCalled(_innerFilter, 0);
    }
    
    // Tests multi package include filter with the inner filter being evaluated first (default)
    @Test
    public void testMultiPackageFilter() {
        mockFilterResults(_innerFilter, Result.ACCEPT);
        PackageFilter pFilter = PackageFilter.createFilter(new Filter[]{_innerFilter},
                "com.redprairie,org.hibernate", null, null);
        runAllFilterMethods(pFilter, "com.redprairie.testing", Result.ACCEPT);
        runAllFilterMethods(pFilter, "org.hibernate.third.party.logger", Result.ACCEPT);
        verifyMocksCalled(_innerFilter, 2);
    }
    
    // Test uses a multipackage filter with the inner filter being evaluted first (default)
    // so the package filter never actually has to execute in this case due to inner denying early
    @Test
    public void testMultiPackageFilterSkipPackageStep() {
        mockFilterResults(_innerFilter, Result.DENY);
        PackageFilter pFilter = PackageFilter.createFilter(new Filter[]{_innerFilter},
                "com.redprairie,org.hibernate", null, null);
        runAllFilterMethods(pFilter, "com.redprairie.testing", Result.DENY);
        verifyMocksCalled(_innerFilter, 1);
    }
    
    // Tests excluding a subset of the loggers while including a parent of it
    @Test
    public void testExcludesFilter() {
        mockFilterResults(_innerFilter, Result.ACCEPT);
        PackageFilter pFilter = PackageFilter.createFilter(new Filter[]{_innerFilter},
                "com.redprairie", "com.redprairie.exclude", null);
        runAllFilterMethods(pFilter, "com.redprairie.exclude.excludedlogger", Result.NEUTRAL);
        runAllFilterMethods(pFilter, "com.redprairie.testing", Result.ACCEPT);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidInnerFirstAttribute() {
        PackageFilter.createFilter(new Filter[]{_innerFilter},
                "com.redprairie", "com.redprairie.exclude", "foo");
    }
    
    @Test(expected=IllegalArgumentException.class) 
    public void testInvalidNumberofInnerFilters() {
        PackageFilter.createFilter(new Filter[]{_innerFilter, _internalPackageFilter},
                "com.redprairie", "com.redprairie.exclude", "foo");
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testNoInnerFilterConfigured() {
        PackageFilter.createFilter(null,
                "com.redprairie", "com.redprairie.exclude", "foo");
    }
    
    private void runAllFilterMethods(PackageFilter filter, String loggerName, Result expected) {
        LogEvent mockEvent = Mockito.mock(LogEvent.class);
        Mockito.when(mockEvent.getLoggerName()).thenReturn(loggerName);
        assertEquals(expected, filter.filter(mockEvent));
        
        Logger mockLogger = Mockito.mock(Logger.class);
        Mockito.when(mockLogger.getName()).thenReturn(loggerName);
        assertEquals(expected,
                filter.filter(mockLogger, null, null, Mockito.mock(Message.class), null));
        assertEquals(expected,
                filter.filter(mockLogger, null, null, "", Mockito.mock(Throwable.class)));
        assertEquals(expected,
                filter.filter(mockLogger, null, null, "","a", "b"));
    }
    
    private void verifyMocksCalled(Filter mockFilter, int count) {
        Mockito.verify(mockFilter, Mockito.times(count)).filter(Mockito.any(LogEvent.class));
        Mockito.verify(mockFilter, Mockito.times(count)).filter(Mockito.any(Logger.class), Mockito.any(Level.class),
                Mockito.any(Marker.class), Mockito.any(Message.class), Mockito.any(Throwable.class));
        Mockito.verify(mockFilter, Mockito.times(count)).filter(Mockito.any(Logger.class), Mockito.any(Level.class),
                Mockito.any(Marker.class), Mockito.any(), Mockito.any(Throwable.class));
        Mockito.verify(mockFilter, Mockito.times(count)).filter(Mockito.any(Logger.class), Mockito.any(Level.class),
                Mockito.any(Marker.class), Mockito.anyString(), Mockito.anyVararg());
    }
    
    private void mockFilterResults(Filter mockFilter, Result wantedResult) {
        Mockito.when(mockFilter.filter(Mockito.any(LogEvent.class))).thenReturn(wantedResult);
        Mockito.when(mockFilter.filter(Mockito.any(Logger.class), Mockito.any(Level.class),
                Mockito.any(Marker.class), Mockito.any(Message.class), Mockito.any(Throwable.class)))
                .thenReturn(wantedResult);
        Mockito.when(mockFilter.filter(Mockito.any(Logger.class), Mockito.any(Level.class),
                Mockito.any(Marker.class), Mockito.any(), Mockito.any(Throwable.class)))
                .thenReturn(wantedResult);
        Mockito.when(mockFilter.filter(Mockito.any(Logger.class), Mockito.any(Level.class),
                Mockito.any(Marker.class), Mockito.anyString(), Mockito.anyVararg()))
                .thenReturn(wantedResult);
    }

    
    @Mock
    private Filter _innerFilter;
    
    @Mock
    private Filter _internalPackageFilter;
    
    @Mock
    private PackageLookup _includeLookup;
    
    @Mock
    private PackageLookup _excludeLookup;
}
