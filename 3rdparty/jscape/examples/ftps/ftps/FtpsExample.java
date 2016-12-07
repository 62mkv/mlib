/*
 * @(#)FtpsExample.java
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

public class FtpsExample extends FtpAdapter {
    private String hostname;
    private String username;
    private String password;
    private String connectionType;
    private int port;
    
    public FtpsExample(String hostname, String username, String password, String connectionType, int port) {
        this.hostname = hostname;
        this.username = username;
        this.password = password;
	this.connectionType = connectionType;
	this.port = port;
    }
    
    // print out directory listing
    public void getListing() throws FtpException {
        Ftps ftp = new Ftps(hostname,username,password);
	if(connectionType.equals("ftps-implicit")) {
	  ftp.setConnectionType(Ftps.IMPLICIT_SSL);
	} else if(connectionType.equals("ftps-explicit")) {
	  ftp.setConnectionType(Ftps.AUTH_TLS);
	} else {
	  throw new FtpException("Invalid connection type " + connectionType);
	}
        
        //capture Ftp related events
        ftp.addFtpListener(this);
	ftp.setPort(port);
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
	String connectionType;
	int port;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter Ftps hostname (e.g. ftp.yourhost.com): ");
            hostname = reader.readLine();
            System.out.print("Enter Ftps port (e.g. 21): ");
            port = Integer.parseInt(reader.readLine());
            System.out.print("Enter connection type (e.g. ftps-explicit, ftps-implicit): ");
            connectionType = reader.readLine();
            System.out.print("Enter username (e.g. anonymous): ");
            username = reader.readLine();
            System.out.print("Enter password (e.g. user@here.com): ");
            password = reader.readLine();
            FtpsExample example = new FtpsExample(hostname,username,password,connectionType,port);
            example.getListing();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}

