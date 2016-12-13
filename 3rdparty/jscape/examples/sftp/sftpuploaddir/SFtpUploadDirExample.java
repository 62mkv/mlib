/*
 * @(#)SFtpUploadDirExample.java
 *
 * Copyright (c) 2016 JScape
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

public class SFtpUploadDirExample extends SftpAdapter {
    
    // perform directory upload
    public void doUpload(String ftpHostname, String ftpUsername, String ftpPassword, File directory) throws SftpException {
    	
		// create new SshParameters instance
		SshParameters params = new SshParameters(ftpHostname,ftpUsername,ftpPassword);    		
		    	
		// create new Ftp instance
		Sftp ftp = new Sftp(params);
       
        // register to capture FTP related events
        ftp.addSftpListener(this);
        
		// establish secure connection        
        ftp.connect();        
        
        // upload directory
        ftp.uploadDir(directory);
        
        // disconnect
        ftp.disconnect();
    }        
    
    // captures download event
    public void upload(SftpUploadEvent evt) {
        System.out.println("Uploaded file: " + evt.getFilename());
    }

    
    // captures connect event
    public void connected(SftpConnectedEvent evt) {
        System.out.println("Connected to server: " + evt.getHostname());
    }
    
    // captures disconnect event
    public void disconnected(SftpDisconnectedEvent evt) {
        System.out.println("Disconnected from server: " + evt.getHostname());
    }
    
    
    public static void main(String[] args) {
    	String sshHostname;
    	String sshUsername;
    	String sshPassword;
        String ftpHostname;
        String ftpUsername;
        String ftpPassword;
        File directory;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter FTP hostname (e.g. ftp.myserver.com): ");
            ftpHostname = reader.readLine();
            System.out.print("Enter FTP username (e.g. jsmith): ");
            ftpUsername = reader.readLine();
            System.out.print("Enter FTP password (e.g. secret): ");
            ftpPassword = reader.readLine();
            System.out.print("Enter local directory to upload (e.g. c:/test): ");
            directory = new File(reader.readLine().trim());
            SFtpUploadDirExample example = new SFtpUploadDirExample();          
            
            // do upload
            example.doUpload(ftpHostname, ftpUsername, ftpPassword, directory);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
