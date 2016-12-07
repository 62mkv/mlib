/*
 * @(#)SFtpExample.java
 *
 * Copyright (c) 2001-2004 JScape
 * 1147 S. 53rd Pl., Mesa, Arizona, 85206, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * JScape. ("Confidential Information").  You shall not disclose such
 * Confidential Information and shall use it only in accordance with
 * the terms of the license agreement you entered into with JScape.
 */

import com.jscape.inet.sftp.*;
import com.jscape.inet.sftp.events.*;
import com.jscape.inet.ssh.util.SshParameters;
import java.io.*;
import java.util.Enumeration;

public class SFtpExample extends SftpAdapter {
    private String ftpHostname;
    private String ftpUsername;
    private String ftpPassword;
    
    /**
     * Creates a new SFtpExample instance.
     * @param ftpHostname the FTP hostname
     * @param ftpUsername the FTP username
     * @param ftpPassword the FTP password
     */
    public SFtpExample(String ftpHostname, String ftpUsername, String ftpPassword) {
        this.ftpHostname = ftpHostname;
        this.ftpUsername = ftpUsername;
        this.ftpPassword = ftpPassword;
    }   
    
    
    /**
     * Prints a directory listing from FTP server
     * @throws SftpException
     */
    public void getListing() throws SftpException {
    	
    	SshParameters params = new SshParameters(ftpHostname,ftpUsername,ftpPassword);
    	
    	// create Sftp instance
        Sftp ftp = new Sftp(params);
        
        //capture FTP related events
        ftp.addSftpListener(this);
        
        // establish secure FTP connection
        ftp.connect();
        
        // get directory listing
        String results = ftp.getDirListingAsString();
        System.out.println(results);
        
        // disconnect
        ftp.disconnect();
    }
    
	/**
	 * Captures FtpConnectedEvent event
	 */
    public void connected(SftpConnectedEvent evt) {
        System.out.println("Connected to server: " + evt.getHostname());
    }
    
    /**
     * Captures FtpDisconnectedEvent event
     */
    public void disconnected(SftpDisconnectedEvent evt) {
        System.out.println("Disconnected from server: " + evt.getHostname());
    }
    
    
    public static void main(String[] args) {
        String ftpHostname;
        String ftpUsername;
        String ftpPassword;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter FTP hostname (e.g. ftp.myserver.com): ");
            ftpHostname = reader.readLine();
            System.out.print("Enter FTP username (e.g. jsmith): ");
            ftpUsername = reader.readLine();
            System.out.print("Enter FTP password (e.g. secret): ");
	    	ftpPassword = reader.readLine();
	    	            
            SFtpExample example = new SFtpExample(ftpHostname, ftpUsername, ftpPassword);
            	
	    // print directory listing           	
            example.getListing();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}

