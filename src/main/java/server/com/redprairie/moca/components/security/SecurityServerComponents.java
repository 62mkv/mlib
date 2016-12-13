/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20167
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

import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.security.LDAPClient;

/**
 * MOCA components to communicate with an LDAP server.
 * 
 * <b><pre>
 * Copyright (c) 20167 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author mlange
 * @version $Revision$
 */
public class SecurityServerComponents {

    public MocaResults getSecurityServerInformation(MocaContext moca)
            throws SecurityServerException {

        String ldapUrl = null;
        String ldapBindDn = null;
        String ldapBindPassword = null;
        String ldapAuthType = null;
        String ldapReferrals = null;
        String ldapUidAttr = null;
        String ldapRoleAttr = null;
        
        // Get all the registry values.
        ldapUrl = moca.getRegistryValue(MocaRegistry.REGKEY_SECURITY_LDAP_URL);
        if (ldapUrl == null) {
            throw new SecurityServerException(MocaRegistry.REGKEY_SECURITY_LDAP_URL);
        }
        
        ldapBindDn = moca.getRegistryValue(MocaRegistry.REGKEY_SECURITY_LDAP_BIND_DN);
        ldapBindPassword = moca.getRegistryValue(MocaRegistry.REGKEY_SECURITY_LDAP_BIND_PASSWORD, false);
        if (ldapBindDn == null || ldapBindPassword == null) {
            throw new SecurityServerException(MocaRegistry.REGKEY_SECURITY_LDAP_BIND_PASSWORD);
        }

        // The authentication type isn't required.
        ldapAuthType = moca.getRegistryValue(MocaRegistry.REGKEY_SECURITY_LDAP_AUTH_TYPE);
        if (ldapAuthType == null) {
        	ldapAuthType = LDAPClient.AUTHENTICATION_TYPE_SIMPLE;
        }
        
        // The referrals behavior isn't required.
        ldapReferrals = moca.getRegistryValue(MocaRegistry.REGKEY_SECURITY_LDAP_REFERRALS);
        if (ldapReferrals == null) {
        	ldapReferrals = LDAPClient.REFERRALS_FOLLOW;
        }
        
        ldapUidAttr = moca.getRegistryValue(MocaRegistry.REGKEY_SECURITY_LDAP_UID_ATTR);
        if (ldapUidAttr == null) {
            throw new SecurityServerException(MocaRegistry.REGKEY_SECURITY_LDAP_UID_ATTR);
        }

        ldapRoleAttr = moca.getRegistryValue(MocaRegistry.REGKEY_SECURITY_LDAP_ROLE_ATTR);
        if (ldapRoleAttr == null) {
            throw new SecurityServerException(MocaRegistry.REGKEY_SECURITY_LDAP_ROLE_ATTR);
        }
        
        // Build the result set.
        EditableResults res = moca.newResults();
        res.addColumn("ldap_url", MocaType.STRING);
        res.addColumn("ldap_bind_dn", MocaType.STRING);
        res.addColumn("ldap_bind_password", MocaType.STRING);
        res.addColumn("ldap_auth_type", MocaType.STRING);
        res.addColumn("ldap_uid_attr", MocaType.STRING);
        res.addColumn("ldap_role_attr", MocaType.STRING);

        res.addRow();

        res.setValue("ldap_url", ldapUrl);
        res.setValue("ldap_bind_dn", ldapBindDn);
        res.setValue("ldap_bind_password", ldapBindPassword);
        res.setValue("ldap_auth_type", ldapAuthType);
        res.setValue("ldap_uid_attr", ldapUidAttr);
        res.setValue("ldap_role_attr", ldapRoleAttr);

        return res;
    }
}
