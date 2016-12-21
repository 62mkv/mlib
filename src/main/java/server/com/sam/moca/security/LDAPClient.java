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

package com.sam.moca.security;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import com.sam.util.ArgCheck;

/**
 * Utility functions to support authentication and authorization against an LDAP
 * server.
 * 
 * <b>
 * 
 * <pre>
 *   Copyright (c) 20167 Sam Corporation
 *   All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author mlange
 * @version $Revision$
 */
public class LDAPClient {
    /**
     * Establish an anonymous connection to an LDAP server.
     * 
     * @param url the url to use for connecting to the LDAP server.
     * @throws IllegalArgumentException if an illegal argument was provided.
     */
    public LDAPClient(String url) {
        this(url, null, null);
    }

    /**
     * Establish a connection to an LDAP server.
     * 
     * @param url the url to use for connecting to the LDAP server.
     * @param bindDN the distinguished name to use for connecting to the LDAP
     *            server.
     * @param bindPassword the password to use for connecting to the LDAP
     *            server.
     * @throws IllegalArgumentException if an illegal argument was provided.
     */
    public LDAPClient(String url, String bindDN, String bindPassword) {
        this(url, bindDN, bindPassword, null, null);
    }

    /**
     * Establish a connection to an LDAP server.
     * 
     * @param url the url to use for connecting to the LDAP server.
     * @param bindDN the distinguished name to use for connecting to the LDAP
     *            server.
     * @param bindPassword the password to use for connecting to the LDAP
     *            server.
     * @param authType the authentication type to use for authenticating users.
     *            (e.g. "simple", "digest-md5")
     * @param referrals the rule to use when encountering referrals. (e.g.
     *            "ignore", "follow")
     * @throws IllegalArgumentException if an illegal argument was provided.
     */
    public LDAPClient(String url, String bindDN, String bindPassword, String authType, String referrals) {
        
    	// Validate arguments.
        ArgCheck.notNull(url, "url cannot be null");

        // Set class variables
        setURL(url);
        setReferrals(referrals);
        setAuthenticationType(authType);

        // Set up the environment for creating the initial context.
        _env = new Hashtable<String, String>();
        _env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_FACTORY);
        _env.put(Context.REFERRAL, _referrals);
        _env.put(Context.PROVIDER_URL, _url);

        // If we don't have a password we'll assume an anonymous bind.
        if (bindDN != null && bindPassword != null) {
            _env.put(Context.SECURITY_AUTHENTICATION, _authType);
            _env.put(Context.SECURITY_PRINCIPAL, bindDN);
            _env.put(Context.SECURITY_CREDENTIALS, bindPassword);
        }
        else {
            throw new IllegalArgumentException("Anonymous bind not supported.");
        }
    }

    /**
     * Bind a connection to an LDAP server.
     * 
     * @throws LDAPClientException if a connection to the LDAP server could not be closed.
     */
    public void bind() throws LDAPClientBindException {

        // Establish a connection to the LDAP server.
        try {
            _ctx = new InitialDirContext(_env);
        }
        catch (NamingException e) {
            throw new LDAPClientBindException("Could not bind to LDAP server: "
                    + e, e);
        }
    }

    /**
     * Close a connection to an LDAP server.
     * 
     * @throws LDAPClientException if a connection to the LDAP server could
     *             not be closed.
     */
    public void close() throws LDAPClientException {

        try {
            _ctx.close();
        }
        catch (NamingException e) {
            throw new LDAPClientException("Could not close LDAP context: " + e,
                e);
        }

        return;
    }

    /**
     * Authenticate a user against an LDAP server.
     * 
     * @param bindDN the distinguished name of the user to authenticate.
     * @param bindPassword the password for the user to authenticate.
     * @throws LDAPClientAuthenticationException if the user could not be
     *             authenticated.
     */
    public void authenticateUser(String bindDN, String bindPassword)
            throws LDAPClientAuthenticationException {

        // Validate arguments.
        ArgCheck.notNull(bindDN, "bindDN cannot be null");
        ArgCheck.notNull(bindPassword, "bindPassword cannot be null");

        // Authenticate the user.
        try {
            // Set up the environment for creating the initial context.
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_FACTORY);
            env.put(Context.REFERRAL, _referrals);
            env.put(Context.PROVIDER_URL, _url);
            env.put(Context.SECURITY_AUTHENTICATION, _authType);
            env.put(Context.SECURITY_PRINCIPAL, bindDN);
            env.put(Context.SECURITY_CREDENTIALS, bindPassword);

            DirContext ctx = new InitialDirContext(env);
            ctx.close();
        }
        catch (NamingException e) {
            throw new LDAPClientAuthenticationException(
                "Could not authenticate user: " + e, e);
        }
    }

    /**
     * Find the distinguished names that contain attribute values that match the
     * given search filter starting from the given search base.
     * 
     * @param searchBase the search base to use.
     * @param searchFilter the search filter to use.
     * @return String[] - an array of distinguished names that contain entries
     *         that match the search criteria.
     * @throws LDAPClientSearchException if the search failed.
     */
    public String[] searchResultDNs(String searchBase, String searchFilter)
            throws LDAPClientSearchException {
        List<String> dnlist = new ArrayList<String>();

        // Validate arguments.
        ArgCheck.notNull(searchFilter, "searchFilter cannot be null");

        // We need to provide a search base.
        if (searchBase == null) searchBase = "";

        try {
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<?> e = _ctx.search(searchBase, searchFilter, ctls);

            // Cycle through each search result.
            while (e.hasMore()) {
                // Get this search result.
                SearchResult result = (SearchResult) e.next();

                // Get the dn this search result matched against.
                String dn = result.getNameInNamespace();

                // Add this dn to the dn list.
                dnlist.add(dn);
            }
        }
        catch (NamingException e) {
            throw new LDAPClientSearchException("Could not execute search: "
                    + e, e);
        }

        return dnlist.toArray(new String[dnlist.size()]);
    }

    /**
     * Find the entries that match the given search filter starting from the
     * given search base.
     * 
     * @param searchBase the search base to use.
     * @param searchFilter the search filter to use.
     * @return Attributes - an instance of <code>Attributes</code>
     *         representing the matching attributes.
     * @throws LDAPClientSearchException if the search failed.
     */
    public Attributes search(String searchBase, String searchFilter)
            throws LDAPClientSearchException {
        Attributes attrsToReturn = new BasicAttributes();

        // Validate arguments.
        ArgCheck.notNull(searchFilter, "searchFilter cannot be null");

        // We need to provide a search base.
        if (searchBase == null) searchBase = "";

        try {
            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            NamingEnumeration<?> e = _ctx.search(searchBase, searchFilter, ctls);

            // Cycle through each search result.
            while (e.hasMore()) {
                // Get this search result.
                SearchResult result = (SearchResult) e.next();

                // Get the attributes from this search result.
                Attributes attrs = result.getAttributes();

                // Append these attributes to the list of attributes to return.
                attrsToReturn = appendAttributes(attrsToReturn, attrs);
            }
        }
        catch (NamingException e) {
            throw new LDAPClientSearchException("Could not execute search: "
                    + e, e);
        }

        return attrsToReturn;
    }

    /**
     * Find the attribute of the given attribute name that matches the given
     * search filter starting from the given search base.
     * 
     * @param searchBase the search base to use.
     * @param searchFilter the search filter to use.
     * @param attrName the attribute name to search for.
     * @return an instance of <code>Attributes</code> representing the
     *         matching attributes.
     * @throws LDAPClientSearchException if the search failed.
     */
    public Attributes search(String searchBase, String searchFilter,
                             String attrName) throws LDAPClientSearchException {
        Attributes attrsToReturn = new BasicAttributes();

        // Validate arguments.
        ArgCheck.notNull(searchFilter, "searchFilter cannot be null");
        ArgCheck.notNull(attrName, "attrName cannot be null");

        // We need to provide a search base.
        if (searchBase == null) searchBase = "";

        try {
            String[] attrNames = new String[] { attrName };

            SearchControls ctls = new SearchControls();
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctls.setReturningAttributes(attrNames);

            NamingEnumeration<?> resultEnum = _ctx.search(searchBase,
                searchFilter, ctls);

            // Cycle through each search result.
            while (resultEnum.hasMore()) {
                // Get this search result.
                SearchResult result = (SearchResult) resultEnum.next();

                // Get the attributes from this search result.
                Attributes attrs = result.getAttributes();

                // Append these attributes to the list of attributes to return.
                attrsToReturn = appendAttributes(attrsToReturn, attrs);
            }
        }
        catch (NamingException e) {
            throw new LDAPClientSearchException("Could not execute search: "
                    + e, e);
        }

        return attrsToReturn;
    }

    /**
     * Get information associated with this LDAP server.
     * <p>
     * This method is meant to be called for debugging purposes only.
     * 
     * @return an instance of <code>Attributes</code> representing the
     *         information associated with this LDAP server.
     * @throws LDAPClientException if an exception is raised while communicating
     *             with the LDAP server.
     */
    public Attributes getInformation() throws LDAPClientException {
        Attributes attrs = null;

        // Get some important attributes directly from the LDAP server.
        try {
            // This is the list of attribute names we want to get.
            String[] attrNames = { "dnsHostName", "serverName",
                    "rootDomainNamingContext", "defaultNamingContext",
                    "ldapServiceName", "supportedLDAPVersion",
                    "supportedSASLMechanisms" };

            // Get the values associated with the above attribute names.
            attrs = _ctx.getAttributes(_url, attrNames);
        }
        catch (NamingException e) {
            throw new LDAPClientException("Could not get LDAP attributes: "
                    + e.toString(true), e);
        }

        // Add some of our own attributes to the list.
        attrs.put("url", _url);

        return attrs;
    }

    /**
     * Print to stdout the name/value pairs for the given attributes.
     * <p>
     * This method is meant to be called for debugging purposes only.
     * 
     * @param attrs the attributes to print.
     * @throws NamingException if an exception is raised while printing the
     *             attributes.
     */

    public void printAttributes(Attributes attrs) throws NamingException {

        NamingEnumeration<? extends Attribute> attrEnum = attrs.getAll();

        while (attrEnum.hasMore()) {
            Attribute attr = attrEnum.next();

            String attrName = attr.getID();

            NamingEnumeration<?> attrValueEnum = attr.getAll();

            while (attrValueEnum.hasMore()) {
                String attrValue = (String) attrValueEnum.next();

                System.out.println(attrName + " = " + attrValue);
            }
        }
    }

    /**
     * Set the authentication type for this LDAP connection.
     * 
     * @param authType the authentication mechanism for this LDAP connection.
     *            (e.g. "simple", "digest-md5")
     * @throws IllegalArgumentException if an illegal argument was provided.
     */

    public void setAuthenticationType(String authType) {

        // Deal with setting the default mechanism.
        if (authType == null) {
            _authType = AUTHENTICATION_TYPE_SIMPLE;
            return;
        }

        // Set the mechanism.
        if (authType.compareToIgnoreCase(AUTHENTICATION_TYPE_SIMPLE) == 0) {
            _authType = AUTHENTICATION_TYPE_SIMPLE;
        }
        else if (authType.compareToIgnoreCase(AUTHENTICATION_TYPE_DIGEST_MD5) == 0) {
            _authType = AUTHENTICATION_TYPE_DIGEST_MD5;
        }
        else {
            throw new IllegalArgumentException(
                "Invalid authentication mechanism: " + authType);
        }
    }

    /**
     * Set the referrals behavior for this LDAP connection.
     * 
     * @param referrals the referrals rule to use for this LDAP connection.
     *            (e.g. "ignore", "follow")
     * @throws IllegalArgumentException if an illegal argument was provided.
     */

    public void setReferrals(String referrals) {

        // Deal with setting the default behavior.
        if (referrals == null) {
            _referrals = REFERRALS_FOLLOW;
            return;
        }

        // Set the behavior.
        if (referrals.compareToIgnoreCase(REFERRALS_IGNORE) == 0) {
            _referrals = REFERRALS_IGNORE;
        }
        else if (referrals.compareToIgnoreCase(REFERRALS_FOLLOW) == 0) {
            _referrals = REFERRALS_FOLLOW;
        }
        else {
            throw new IllegalArgumentException("Invalid referrals rule: "
                    + referrals);
        }
    }

    private void setURL(String url) {
        _url = url.replaceAll(" ", "%20");
    }

    public Attributes appendAttributes(Attributes attrsToReturn,
                                       Attributes attrs) throws NamingException {

        NamingEnumeration<? extends Attribute> attrEnum = attrs.getAll();

        while (attrEnum.hasMore()) {
            Attribute attr = attrEnum.next();

            String attrName = attr.getID();

            Attribute matchingAttr = attrsToReturn.get(attrName);
            if (matchingAttr == null) {
                attrsToReturn.put(attr);
            }
            else {

                NamingEnumeration<?> attrValueEnum = attr.getAll();

                while (attrValueEnum.hasMore()) {
                    String attrValue = (String) attrValueEnum.next();
                    matchingAttr.add(attrValue);
                }
            }
        }

        return attrsToReturn;
    }

    private DirContext _ctx;
    private Hashtable<String, String> _env;

    private String _url;
    private String _authType;
    private String _referrals;

    private static final String LDAP_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";

    public static final String REFERRALS_IGNORE = "ignore";
    public static final String REFERRALS_FOLLOW = "follow";

    public static final String AUTHENTICATION_TYPE_SIMPLE = "SIMPLE";
    public static final String AUTHENTICATION_TYPE_DIGEST_MD5 = "DIGEST-MD5";
}
