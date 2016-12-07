/*
 * @(#)FtpsUploadDirExample.java
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
import com.jscape.inet.ftps.*;
import java.io.*;
import java.util.Enumeration;

public class FtpsUploadDirExample extends FtpAdapter {
    
    // perform directory upload
    public void doUpload(String hostname, String username, String password, File directory) throws FtpException {
        Ftps ftp = new Ftps();
        
        //capture Ftp related events
        ftp.addFtpListener(this);
        ftp.setHostname(hostname);
        ftp.setUsername(username);
        ftp.setPassword(password);
        ftp.connect();
        ftp.setBinary();
        ftp.uploadDir(directory);
        ftp.disconnect();
    }    
    
    // captures download event
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
        File directory;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter Ftp hostname (e.g. ftp.yourhost.com): ");
            hostname = reader.readLine().trim();
            System.out.print("Enter username (e.g. anonymous): ");
            username = reader.readLine().trim();
            System.out.print("Enter password (e.g. user@here.com): ");
            password = reader.readLine().trim();
            System.out.print("Enter local directory to upload (e.g. c:/test): ");
            directory = new File(reader.readLine().trim());
            FtpsUploadDirExample example = new FtpsUploadDirExample();
            example.doUpload(hostname,username,password,directory);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
