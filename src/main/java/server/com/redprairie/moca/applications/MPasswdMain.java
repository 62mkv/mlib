/*
 *  $URL$
 *  $Revision$
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

package com.redprairie.moca.applications;

import java.io.Console;

import org.eclipse.jetty.util.security.Password;

import com.redprairie.moca.util.BlowfishUtils;
import com.redprairie.moca.util.MD5Utils;
import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;

/**
 * This class is the mainline for the mpasswd executable.  This is used to 
 * generate a new password for the given type.  The available types are 
 * database user, database admin, web container, and zabbix api password.  After generating the
 * password it will be put to standard out so that a user can copy and paste
 * the password into their configuration.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class MPasswdMain {
    static void showUsage() {
        System.out.println(
                "Usage: mpasswd [-cdawlzhv] [-n password]\n" +
                "\t-c             Print the admin console password\n" +
                "\t-d             Print the database user's password\n" +
                "\t-a             Print the database administrator's password\n" +
                "\t-l             Print the ldap bind password\n" +
                "\t-w             Print the web container's password\n" +
                "\t-z             Print the zabbix api password\n" +
                "\t-n <password>  New password\n" +
                "\t-h             Show help\n" +
                "\t-v             Show version information\n");
    }
    
    public static void main(String[] args) {
        Options opts = null;
        try {
            opts = Options.parse("zldawch?vn:", args);
        }
        catch (OptionsException e) {
            System.err.println("Invalid option: " + e.getMessage());
            showUsage();
            System.exit(1);
        }

        boolean printAdminConsolePass = opts.isSet('c');
        boolean printDatabaseUserPass = opts.isSet('d');
        boolean printDatabaseAdminPass = opts.isSet('a');
        boolean printWebContainerPass = opts.isSet('w');
        boolean printLdapPass = opts.isSet('l');
        boolean printZabbixPass = opts.isSet('z');
        
        if (opts.isSet('h') || opts.isSet('?') || (!printDatabaseAdminPass && 
                !printDatabaseUserPass && !printWebContainerPass && 
                !printAdminConsolePass && !printLdapPass && !printZabbixPass)) {
            showUsage();
            return;
        }
        
        String newClearTextPassword = null;
        
        if (opts.isSet('n')) {
            newClearTextPassword = opts.getArgument('n');
        }
        
        // If they didn't give us a password then we have to prompt for it
        if (newClearTextPassword == null || 
                newClearTextPassword.trim().isEmpty()) {
            String firstAttempt;
            Console console = System.console();
            firstAttempt = new String(console.readPassword("%s", 
                    "New Password: "));
            
            newClearTextPassword = new String(console.readPassword("%s", 
                "Confirm New Password: "));
            
            // Make sure the passwords match
            if (!firstAttempt.equals(newClearTextPassword)) {
                System.err.println("\nPasswords do not match");
                System.exit(1);
            }
        }
        
        // If they want a database password then they are both the same thing
        if (printDatabaseUserPass || printDatabaseAdminPass || printLdapPass || printZabbixPass) {
            String newEncodedPassword = null;
            try {
                newEncodedPassword = BlowfishUtils.encodePassword(
                        newClearTextPassword);
            }
            catch (Exception e) {
                System.out.println("There was a problem encoding the password!");
                e.printStackTrace();
                System.exit(1);
            }
            
            if (printDatabaseUserPass) {
                System.out.println("New database user password : " + 
                        newEncodedPassword);
            }
            
            if (printDatabaseAdminPass) {
                System.out.println("New database admin password : " + 
                        newEncodedPassword);
            }
            
            if (printLdapPass) {
                System.out.println("New ldap bind password : " + 
                        newEncodedPassword);
            }
            
            if (printZabbixPass) {
                System.out.println("New zabbix api password : " + 
                    newEncodedPassword);
            }
        }
        
        if (printAdminConsolePass) {
            System.out.println("New Admin Console Password : " + 
                    MD5Utils._hashPipe + 
                    MD5Utils.hashPassword(newClearTextPassword, null));
        }
        
        if (printWebContainerPass) {
            System.out.println("New Jetty container password : " + 
                    Password.obfuscate(newClearTextPassword));
        }
    }
}
