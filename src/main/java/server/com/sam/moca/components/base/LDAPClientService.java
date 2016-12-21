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

package com.sam.moca.components.base;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import com.sam.moca.EditableResults;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaTrace;
import com.sam.moca.MocaType;
import com.sam.moca.security.LDAPClient;
import com.sam.moca.security.LDAPClientAuthenticationException;
import com.sam.moca.security.LDAPClientBindException;
import com.sam.moca.security.LDAPClientException;
import com.sam.moca.security.LDAPClientSearchException;
import com.sam.util.ArgCheck;

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
public class LDAPClientService {

    public MocaResults connectToLDAPServer(MocaContext moca, String url,
                                           String bindDN, String bindPassword,
                                           String authType, String referrals)
            throws IllegalArgumentException, LDAPClientBindException {

        LDAPClient client = null;

        moca.trace(MocaTrace.FLOW, "Attempting to connect to LDAP server...");

        // The given URL could be a semi-colon-separated list of multiple URLs.
        String[] urlList = url.split(";");
        
        // Cycle through each URL in the URL list.
        for (int ii = 0; ii < urlList.length; ii++) {
            // Trim spaces from each side of the URL.
            String thisUrl = urlList[ii].trim();
            
            try { 
                // Construct an LDAP client.
                client = new LDAPClient(thisUrl, bindDN, bindPassword, authType, referrals);
        
                // Bind to the LDAP server.
                client.bind();
                
                // We connected, so let's break out.
                break;
            }
            catch (LDAPClientBindException e) {
                // We swallow the exception unless this is the last URL we can try.
                if (ii == urlList.length-1)
                    throw e;

                moca.logWarning("Could not bind to LDAP server (" + thisUrl + ")");
                moca.logWarning("Trying the next configured LDAP server...");
            }
        }

        // Build the result set.
        EditableResults res = moca.newResults();
        res.addColumn("ldap_client", MocaType.OBJECT);
        res.addRow();
        res.setValue("ldap_client", client);

        return res;
    }

    public void closeLDAPServerConnection(MocaContext moca, LDAPClient client)
            throws LDAPClientException {

        // Validate arguments.
        ArgCheck.notNull(client, "client cannot be null");

        // Authenticate the user.
        client.close();
    }

    public void authenticateLDAPUserUsingDN(MocaContext moca,
                                            LDAPClient client, String dn,
                                            String password)
            throws LDAPClientException, LDAPClientAuthenticationException {

        // Validate arguments.
        ArgCheck.notNull(client, "client can not be null");
        ArgCheck.notNull(dn, "dn can not be null");
        ArgCheck.notNull(password, "password can not be null");

        moca.trace(MocaTrace.FLOW,
            "Attempting to authenticate LDAP user using DN...");

        // Authenticate the user.
        client.authenticateUser(dn, password);
    }

    public void authenticateLDAPUserUsingUID(MocaContext moca,
                                             LDAPClient client,
                                             String uidAttrName, String uid,
                                             String password)
            throws LDAPClientServiceException, LDAPClientException,
            LDAPClientAuthenticationException, LDAPClientSearchException {

        // Validate arguments.
        ArgCheck.notNull(client, "client cannot be null");
        ArgCheck.notNull(uidAttrName, "UID attribute name can not be null");
        ArgCheck.notNull(uid, "UID can not be null");
        ArgCheck.notNull(password, "Password can not be null");

        moca.trace(MocaTrace.FLOW,
            "Attempting to authenticate LDAP user using UID...");

        // Search for the dn for this user.
        String searchFilter = "(" + uidAttrName + "=" + uid + ")";
        String[] dnlist = client.searchResultDNs(null, searchFilter);

        // We should find one and only one matching user id.
        if (dnlist.length == 0)
            throw new LDAPClientServiceException("Could not find matching UID");
        if (dnlist.length > 1)
            throw new LDAPClientServiceException(
                "More than one matching UID was found");

        // Get the dn for this user.
        String dn = dnlist[0];

        moca.trace(MocaTrace.FLOW, "Attempting to authenticate user " + dn);

        // Authenticate the user.
        client.authenticateUser(dn, password);
    }

    public MocaResults findLDAPAttributes(MocaContext moca, LDAPClient client,
                                          String searchBase, String searchFilter)
            throws LDAPClientException, LDAPClientSearchException {

        // Validate arguments.
        ArgCheck.notNull(client, "client cannot be null");

        // Create the result set.
        EditableResults res = moca.newResults();
        res.addColumn("name", MocaType.STRING);
        res.addColumn("value", MocaType.STRING);

        // Get the list of attributes from the LDAP server.
        Attributes attrs = client.search(searchBase, searchFilter);

        try {
            NamingEnumeration<?> attrEnum = attrs.getAll();

            while (attrEnum.hasMore()) {

                Attribute attr = (Attribute) attrEnum.next();

                NamingEnumeration<?> attrValueEnum = attr.getAll();

                while (attrValueEnum.hasMore()) {
                    // Get this name/value pair.
                    String name = attr.getID();
                    String value = (String) attrValueEnum.next();

                    // Add them to the result set.
                    res.addRow();
                    res.setStringValue("name", name);
                    res.setStringValue("value", value);
                }
            }
        }
        catch (NamingException e) {
            throw new LDAPClientException(
                "unable to get server attributes" + e, e);
        }

        return res;
    }

    public MocaResults findLDAPAttributeValues(MocaContext moca,
                                               LDAPClient client,
                                               String searchBase,
                                               String searchFilter,
                                               String attrName)
            throws LDAPClientException, LDAPClientSearchException {

        // Validate arguments.
        ArgCheck.notNull(client, "client cannot be null");

        // Create the result set.
        EditableResults res = moca.newResults();
        res.addColumn("name", MocaType.STRING);
        res.addColumn("value", MocaType.STRING);

        // Get the list of attributes from the LDAP server.
        Attributes attrs = client.search(searchBase, searchFilter, attrName);

        try {
            NamingEnumeration<?> attrEnum = attrs.getAll();

            while (attrEnum.hasMore()) {
                Attribute attr = (Attribute) attrEnum.next();

                NamingEnumeration<?> attrValueEnum = attr.getAll();

                while (attrValueEnum.hasMore()) {
                    // Get this name/value pair.
                    String name = attr.getID();
                    String value = (String) attrValueEnum.next();

                    // Add them to the result set.
                    res.addRow();
                    res.setStringValue("name", name);
                    res.setStringValue("value", value);
                }
            }
        }
        catch (NamingException e) {
            throw new LDAPClientException("unable to get server attributes", e);
        }

        return res;
    }

    public MocaResults getLDAPServerInformation(MocaContext moca,
                                                LDAPClient client)
            throws LDAPClientException {

        // Validate arguments.
        ArgCheck.notNull(client, "client cannot be null");

        EditableResults res = moca.newResults();
        res.addColumn("name", MocaType.STRING);
        res.addColumn("value", MocaType.STRING);

        // Get the list of attributes from the LDAP server.
        try {
            Attributes attrs = client.getInformation();

            for (NamingEnumeration<?> ae = attrs.getAll(); ae.hasMore();) {
                Attribute attr = (Attribute) ae.next();
                NamingEnumeration<?> ve = attr.getAll();

                while (ve.hasMore()) {
                    String name = attr.getID();
                    String value = (String) ve.next();

                    res.addRow();
                    res.setStringValue("name", name);
                    res.setStringValue("value", value);
                }
            }
        }
        catch (NamingException e) {
            throw new LDAPClientException("unable to get server attributes", e);
        }

        return res;
    }
}
