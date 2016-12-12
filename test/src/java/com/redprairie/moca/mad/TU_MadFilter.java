/*
 *  $URL$
 *  $Author$
 *  $Date$
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

package com.redprairie.moca.mad;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;

import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.exec.SystemContext;

/**
 * Tests MadFilter
 * 
 * Copyright (c) 2012 RedPrairie Corporation 
 * All Rights Reserved
 * 
 * @author klucas
 */
public class TU_MadFilter {

    private MadFilter madFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain chain;

    @Mock
    private SystemContext context;

    @Before
    public void testSetup() throws ServletException {
        MockitoAnnotations.initMocks(this);
        madFilter = new MadFilter();
        madFilter.setSystemContext(context);
    }

    @After
    public void testCleanup() {
        madFilter = null;
    }

    @Test
    public void testDoFilterNoPassword() throws IOException, ServletException {
        Mockito.when(request.getHeader(MadFilter.AUTH_HEADER)).thenReturn(
            "Basic OnBhc3N3b3Jk");
        Mockito.when(
            context.getConfigurationElement(MocaRegistry.REGKEY_SECURITY_ADMIN_PASSWORD))
            .thenReturn(null);

        madFilter.doFilter(request, response, chain);

        // If the admin-password isn't set they should never be able to login
        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testDoFilterMD5Password() throws IOException, ServletException {
        Mockito.when(request.getHeader(MadFilter.AUTH_HEADER)).thenReturn(
            "Basic OmJ1YmJlcnM=");
        Mockito.when(
            context.getConfigurationElement(MocaRegistry.REGKEY_SECURITY_ADMIN_PASSWORD))
            .thenReturn("|H|SYZF90S9FB5ONC1S1BBMIR2K52D5G1");

        madFilter.doFilter(request, response, chain);

        Mockito.verify(chain).doFilter(request, response);
    }

    @Test
    public void testDoFilterNoLoginNoPassword() throws IOException, ServletException {
        madFilter.doFilter(request, response, chain);

        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testDoFilterNoLoginMD5Password() throws IOException, ServletException {
        Mockito.when(
            context.getConfigurationElement(MocaRegistry.REGKEY_SECURITY_ADMIN_PASSWORD))
            .thenReturn("|H|SYZF90S9FB5ONC1S1BBMIR2K52D5G1");

        madFilter.doFilter(request, response, chain);

        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testDoFilterUTF8() throws IOException, ServletException {
        /* This is façade. */
        Mockito.when(request.getHeader(MadFilter.AUTH_HEADER)).thenReturn(
            "Basic OmZhw6dhZGU=");
        Mockito.when(
            context.getConfigurationElement(MocaRegistry.REGKEY_SECURITY_ADMIN_PASSWORD))
            .thenReturn("|H|ZDLON7VAP1O9AMIVAFNDHV026PU0A5");

        madFilter.doFilter(request, response, chain);

        Mockito.verify(chain).doFilter(request, response);
    }

    @Test
    public void testNonBasicAuthHeader() throws IOException, ServletException {
        Mockito.when(request.getHeader(MadFilter.AUTH_HEADER)).thenReturn(
            "Non-Basic non-basic-string");

        madFilter.doFilter(request, response, chain);

        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    public void testTooManyAuthHeaderFields() throws IOException, ServletException {
        Mockito.when(request.getHeader(MadFilter.AUTH_HEADER)).thenReturn(
            "Basic string-with-no-colon");

        madFilter.doFilter(request, response, chain);

        Mockito.verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

}
