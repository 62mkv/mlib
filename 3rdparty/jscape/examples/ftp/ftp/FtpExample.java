/*
 * @(#)FtpExample.java
 *
 * Copyright (c) 2001-2002 JScape
 * 1147 S. 53rd Pl., Mesa, Arizona, 85206, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * JScape. ("Confidential Information").  You shall not disclose such
 * Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with JScape.
 */

import com.jscape.inet.ftp.*;
import java.io.*;
import java.util.Enumeration;

public class FtpExample extends FtpAdapter {
    private String hostname;
    private String username;
    private String password;
    
    public FtpExample(String hostname, String username, String password) {
        this.hostname = hostname;
        this.username = username;
        this.password = password;
    }
    
    // print out directory listing
    public void getListing() throws FtpException {
        Ftp ftp = new Ftp(hostname,username,password);
        
        //capture Ftp related events
        ftp.addFtpListener(this);
        ftp.connect();
        
        String results = ftp.getDirListingAsString();
        System.out.println(results);
        ftp.disconnect();
    }
    
    // captures connect event
    public void connected(FtpConnectedEvent evt) {
        System.out.println("Connected to server: " + evt.getHostname());
    }
    
    // captures disconnect event
    public void disconnected(FtpDisconnectedEvent evt) {
        System.out.println("Disconnected from server: " + evt.getHostname());
    }
    
    
    public static void main(String[] args) {
        String hostname;
        String username;
        String password;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter Ftp hostname (e.g. ftp.yourhost.com): ");
            hostname = reader.readLine();
            System.out.print("Enter username (e.g. anonymous): ");
            username = reader.readLine();
            System.out.print("Enter password (e.g. user@here.com): ");
            password = reader.readLine();
            FtpExample example = new FtpExample(hostname,username,password);
            example.getListing();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}

