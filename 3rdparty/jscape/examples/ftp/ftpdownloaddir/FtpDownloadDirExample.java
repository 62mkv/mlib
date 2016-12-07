/*
 * @(#)FtpDownloadDirExample.java
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

public class FtpDownloadDirExample extends FtpAdapter {    
    
    // perform directory download
    public void doDownload(String hostname, String username, String password, String directory) throws FtpException {
        Ftp ftp = new Ftp();
        
        //capture Ftp related events
        ftp.addFtpListener(this);
        ftp.setHostname(hostname);
        ftp.setUsername(username);
        ftp.setPassword(password);
        ftp.connect();
        ftp.setBinary();
        ftp.downloadDir(directory);
        ftp.disconnect();
    }
    
    // captures download event
    public void download(FtpDownloadEvent evt) {
        System.out.println("Downloaded file: " + evt.getFilename());
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
        String directory;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter Ftp hostname (e.g. ftp.yourhost.com): ");
            hostname = reader.readLine().trim();
            System.out.print("Enter username (e.g. anonymous): ");
            username = reader.readLine().trim();
            System.out.print("Enter password (e.g. user@here.com): ");
            password = reader.readLine().trim();
            System.out.print("Enter remote directory to download: (e.g. test) ");
            directory = reader.readLine().trim();
            FtpDownloadDirExample example = new FtpDownloadDirExample();
            example.doDownload(hostname,username,password,directory);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
