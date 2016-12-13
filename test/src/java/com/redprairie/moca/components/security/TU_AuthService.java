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

package com.redprairie.moca.components.security;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.server.SystemConfigurationException;
import com.redprairie.moca.server.exec.SystemContext;


public class TU_AuthService {
    
    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected=SystemConfigurationException.class)
    public void testAdminPasswordWithNoUserSpecified() throws SystemConfigurationException {
        mockUserPass(null, "|H|iohefoihef");
        AuthService.validateConsoleAdminConfiguration(_mockSys);
    }
    
    @Test(expected=SystemConfigurationException.class)
    public void testAdminUserWithNoPasswordSpecified() throws SystemConfigurationException {
        mockUserPass("my-admin-user", null);
        AuthService.validateConsoleAdminConfiguration(_mockSys);
    }
    
    @Test(expected=SystemConfigurationException.class)
    public void testAdminPasswordNotHashed() throws SystemConfigurationException {
        mockUserPass("my-admin-user", "cleartextisbad");
        AuthService.validateConsoleAdminConfiguration(_mockSys);
    }
    
    @Test
    public void testValidConfiguration() throws SystemConfigurationException {
        mockUserPass("my-admin-user", "|H|iohefoihef");
        AuthService.validateConsoleAdminConfiguration(_mockSys);
    }
    
    private void mockUserPass(String user, String password) {
        Mockito.when(_mockSys.getConfigurationElement(
                Mockito.eq(MocaRegistry.REGKEY_SECURITY_ADMIN_USER), Mockito.anyString())).thenReturn(user);
        Mockito.when(_mockSys.getConfigurationElement(MocaRegistry.REGKEY_SECURITY_ADMIN_PASSWORD)).thenReturn(password);
    }
    
    @Mock
    private SystemContext _mockSys;
}
