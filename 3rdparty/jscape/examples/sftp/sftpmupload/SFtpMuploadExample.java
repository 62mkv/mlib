/*
 * @(#)SFtpMuploadExample.java
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


import com.jscape.inet.sftp.*;
import com.jscape.inet.sftp.events.*;
import com.jscape.inet.ssh.util.SshParameters;
import java.io.*;
import java.util.Enumeration;

public class SFtpMuploadExample extends SftpAdapter {
    
    // perform multiple file upload
    public void doUpload(String ftpHostname, String ftpUsername, String ftpPassword) throws SftpException {

		// create new SshParameters instance
		SshParameters params = new SshParameters(ftpHostname,ftpUsername,ftpPassword);    		
		    	
		// create new Ftp instance
		Sftp ftp = new Sftp(params);
                
        // register to capture FTP related events
        ftp.addSftpListener(this);
        
        // establish secure connection
        ftp.connect();        
        
        // upload all files in local directory matching filter
        ftp.mupload(".*\\.gif");
        
        // disconnect
        ftp.disconnect();
    }    
    
    // captures upload event
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

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter FTP hostname (e.g. ftp.myserver.com): ");
            ftpHostname = reader.readLine();
            System.out.print("Enter FTP username (e.g. jsmith): ");
            ftpUsername = reader.readLine();
            System.out.print("Enter FTP password (e.g. secret): ");
            ftpPassword = reader.readLine();
            SFtpMuploadExample example = new SFtpMuploadExample();                       
            
            // do upload
            example.doUpload(ftpHostname, ftpUsername, ftpPassword);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}
