/*
 * @(#)FtpMuploadExample.java
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

public class FtpMuploadExample extends FtpAdapter {
    private String hostname;
    private String username;
    private String password;
    
    // perform multiple file upload
    public void doUpload(String hostname, String username, String password) throws FtpException {
        Ftp ftp = new Ftp(hostname,username,password);
        
        //capture Ftp related events
        ftp.addFtpListener(this);
        ftp.connect();
        ftp.setBinary();
        ftp.mupload(".*\\.gif");
        ftp.disconnect();
    }
    
    // captures upload event
    public void upload(FtpUploadEvent evt) {
        System.out.println("Uploaded file: " + evt.getFilename());
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
            hostname = reader.readLine().trim();
            System.out.print("Enter username (e.g. anonymous): ");
            username = reader.readLine().trim();
            System.out.print("Enter password (e.g. user@here.com): ");
            password = reader.readLine().trim();
            System.out.println("Using upload filter .*\\.gif");
            FtpMuploadExample example = new FtpMuploadExample();
            example.doUpload(hostname,username,password);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
