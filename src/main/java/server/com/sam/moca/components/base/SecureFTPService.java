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

package com.sam.moca.components.base;

import java.io.File;
import java.util.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jscape.inet.sftp.Sftp;
import com.jscape.inet.sftp.SftpException;
import com.jscape.inet.sftp.SftpFile;
import com.jscape.inet.ssh.util.SshParameters;
import com.sam.moca.EditableResults;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.exceptions.MissingArgumentException;

/**
 * Class containing SFTP (SSH-FTP) file transfer components.  This class is
 * an adapter between MOCA and JScape's secure ftp factory product.
 * 
 * @see <a href="http://www.jscape.com">jscape home page</a>
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class SecureFTPService {
    
    /**
     * Puts a file from the local filesystem into a remote SFTP server.
     * @param moca the MOCA context.  This argument cannot be null.
     * @param host the hostname or IP address of the SFTP server. This argument 
     * cannot be null.
     * @param port the port on which the SFTP server is listening.
     * @param user the username to use for authentication. This argument 
     * cannot be null.
     * @param password the password to use for authentication.  This argument 
     * cannot be null.
     * @param destDir the directory on the server that the file will be
     * transferred into.  If null, the default directory for the authenticated
     * user will be used.
     * @param destFile the filename to be used on the remote server for the
     * new file.  If null, the name of the source file will be used.
     * @param sourceDir the directory on the calling host, where the source
     * file is to be located.  If this argument is null, the
     * <code>sourceFile</code> argument must either specify an absolute path or
     * a file in the running process' current working directory.
     * @param sourceFile the name of the file to send to the remote host.  This
     * argument cannot be null.
     * @param mode the mode of the file. This can be either "A" (Ascii) or "I"
     * (bInary?).  If it's anything else or <code>null</code>, the default
     * behavior is to try to auto-select a mode based on the file type being
     * sent. 
     * @throws SecureFTPServiceException if an SFTP error occurs.
     */
    public String putFile(MocaContext moca,
            String host, int port, String user, String password,
            String privateKeyFilename, String privateKeyPassphrase,
            String destDir, String destFile,
            String sourceDir, String sourceFile, String mode)
            throws MissingArgumentException, SecureFTPServiceException  {
        SecureFTPConnection sftpConnection = buildSftpConnection(host, port,
                user, password, privateKeyFilename, privateKeyPassphrase);
        Sftp sftp = sftpConnection.getSftp();
        SshParameters sshParams = sftpConnection.getParameters();
        try {
            _connect(moca, sftp, sshParams, sourceDir, destDir, mode);

            if (destFile != null) {
                _logger.debug("Uploading File " + sourceFile + " as " + destFile);
                sftp.upload(sourceFile, destFile);
                return "Uploaded " + sourceFile + " to " + host + " as " + destFile + ".";
            }
            else {
                _logger.debug("Uploading File " + sourceFile);
                sftp.upload(sourceFile);
                return "Uploaded " + sourceFile + " to " + host;
            }
        }
        catch (SftpException e) {
            throw new SecureFTPServiceException("error uploading file: " + e, e);
        }
        finally {
            if (sftp.isConnected()) sftp.disconnect();
        }
    }

    /**
     * Removes a file from a remote SFTP server.
     * @param moca the MOCA context.  This argument cannot be null.
     * @param host the hostname or IP address of the SFTP server. This argument 
     * cannot be null.
     * @param port the port on which the SFTP server is listening.
     * @param user the username to use for authentication. This argument 
     * cannot be null.
     * @param password the password to use for authentication.  This argument 
     * cannot be null.
     * @param destDir the directory on the server that the file will be
     * removed from.  If null, the default directory for the authenticated
     * user will be used.
     * @param destFile the filename to be removed from the remote server.
     * This argument cannot be null.
     * @throws SecureFTPServiceException if an SFTP error occurs.
     */
    public String removeFile(MocaContext moca,
            String host, int port, String user, String password,
            String privateKeyFilename, String privateKeyPassphrase,
            String destDir, String destFile)
            throws MissingArgumentException, SecureFTPServiceException {
        
        SecureFTPConnection sftpConnection = buildSftpConnection(host, port,
                user, password, privateKeyFilename, privateKeyPassphrase);
        Sftp sftp = sftpConnection.getSftp();
        SshParameters sshParams = sftpConnection.getParameters();
        try {
            _connect(moca, sftp, sshParams, null, destDir, null);

            _logger.debug("Removing file " + destFile);
            sftp.deleteFile(destFile);
            return "Deleted " + destFile + " from " + host + ".";
        }
        catch (SftpException e) {
            throw new SecureFTPServiceException("error deleting file: " + e, e);
        }
        finally {
            if (sftp.isConnected()) sftp.disconnect();
        }
    }

    /**
     * Gets a file from a remote SFTP server into the local filesystem.
     * @param moca the MOCA context.  This argument cannot be null.
     * @param host the hostname or IP address of the SFTP server. This argument 
     * cannot be null.
     * @param port the port on which the SFTP server is listening.
     * @param user the username to use for authentication. This argument 
     * cannot be null.
     * @param password the password to use for authentication.  This argument 
     * cannot be null.
     * @param destDir the directory on the local filesystem that the file will
     * be transferred into.  If this argument is null, the running process's
     * current working directory will be used.  
     * If null, the default directory for the authenticated user will be used.
     * @param destFile the filename to be used on the local filesystem for the
     * new file.  If null, the base name of the source file will be used.
     * @param sourceDir the directory on the remote server, where the source
     * file can be found.  If this argument is null, the
     * <code>sourceFile</code> argument must either specify an absolute path or
     * a file in default directory for the authenticated user.
     * @param sourceFile the name of the file to get from the remote host. 
     * This argument cannot be null.
     * @param mode the mode of the file. This can be either "A" (Ascii) or "I"
     * (bInary?).  If it's anything else or <code>null</code>, the default
     * behavior is to try to auto-select a mode based on the file type being
     * sent.
     * @throws SecureFTPServiceException if an SFTP error occurs.
     */
    public String getFile(MocaContext moca,
            String host, int port, String user, String password,
            String privateKeyFilename, String privateKeyPassphrase,
            String destDir, String destFile,
            String sourceDir, String sourceFile, String mode)
            throws MissingArgumentException, SecureFTPServiceException {
        
        SecureFTPConnection sftpConnection = buildSftpConnection(host, port,
                user, password, privateKeyFilename, privateKeyPassphrase);
        Sftp sftp = sftpConnection.getSftp();
        SshParameters sshParams = sftpConnection.getParameters();
        try {
            _connect(moca, sftp, sshParams, destDir, sourceDir, mode);

            if (destFile != null) {
                _logger.debug("Downloading File " + sourceFile + " as " + destFile);
                sftp.download(destFile, sourceFile);
                return "Downloaded " + sourceFile + " from " + host + " as " + destFile + ".";
            }
            else {
                _logger.debug("Downloading File " + sourceFile);
                sftp.download(sourceFile);
                return "Downloaded " + sourceFile + " from " + host + ".";
            }
        }
        catch (SftpException e) {
            throw new SecureFTPServiceException("error downloading file:" + e, e);
        }
        finally {
            if (sftp.isConnected()) sftp.disconnect();
        }
    }
    /**
     * Renames a file on a remote SFTP server.
     * @param moca the MOCA context.  This argument cannot be null.
     * @param host the hostname or IP address of the FTP server. This argument 
     * cannot be null.
     * @param port the port on which the SFTP server is listening which is 22 
     * by default.
     * @param user the username to use for authentication. This argument 
     * cannot be null.
     * @param password the password to use for authentication.  This argument 
     * cannot be null.
     * @param destDir the directory on the server that the file will be
     * removed from.  If null, the default directory for the authenticated
     * user will be used.
     * @param oldName the existing name of the file on the remote server. 
     * This argument cannot be null.
     * @param newName the new name of the file. This argument cannot be null.
     * @throws SecureFTPServiceException if an SFTP error occurs.
     */
    public String rename(MocaContext moca,
            String host, int port, String user, String password,
            String privateKeyFilename, String privateKeyPassphrase,
            String destDir,String oldName,String newName)
                throws MissingArgumentException, SecureFTPServiceException {

        SecureFTPConnection sftpConnection = buildSftpConnection(host, port,
                user, password, privateKeyFilename, privateKeyPassphrase);
        Sftp sftp = sftpConnection.getSftp();
        SshParameters sshParams = sftpConnection.getParameters();
        try {
            _connect(moca, sftp, sshParams, null, destDir, null);

            _logger.debug("Renaming file " + oldName+ " to " + newName+ ".");
            sftp.renameFile(oldName, newName);
            return "Renamed " + oldName + " to " + newName+ ".";
        }
        catch (SftpException e) {
            throw new SecureFTPServiceException("error renaming file: " + e, e);
        }
        finally {
            if (sftp.isConnected()) sftp.disconnect();
        }
    }
    
    /**
     * List a directory on a remote SFTP server.
     * @param moca the MOCA context.  This argument cannot be null.
     * @param host the hostname or IP address of the SFTP server. This argument 
     * cannot be null.
     * @param port the port on which the SFTP server is listening which is 22
     * by default.
     * @param user the username to use for authentication. This argument 
     * cannot be null.
     * @param password the password to use for authentication.  This argument 
     * cannot be null.
     * @param directory the directory on the server to list.  If one is not specified the 
     * current working directory will be used.
     * @throws SFTPServiceException if an SFTP error occurs.
     */
    @SuppressWarnings("unchecked")
    public MocaResults list(MocaContext moca, String host, int port,
                            String user, String password,
                            String privateKeyFilename,
                            String privateKeyPassphrase, String directory,
                            String filename) throws MissingArgumentException, SecureFTPServiceException {

        EditableResults res = moca.newResults();
        res.addColumn("filename", MocaType.STRING);

        SecureFTPConnection sftpConnection = buildSftpConnection(host, port,
                user, password, privateKeyFilename, privateKeyPassphrase);
        Sftp sftp = sftpConnection.getSftp();
        SshParameters sshParams = sftpConnection.getParameters();
        try {
            _logger.debug("Listing directory " + directory);
            _connect(moca, sftp, sshParams, null, directory, null);

            Enumeration<SftpFile> files = null;
            if (filename != null && filename.length() > 0) {
                // Replace the ? with a dot, a dot with backslash dot and a star
                // with dot star to be compliant with regular expression for
                // our wildcards
                String regex = filename.replace(".", "\\.").replace('?', '.').replace("*", ".*");
                files = (Enumeration<SftpFile>)sftp.getDirListing(regex);
            }
            else {
                files = (Enumeration<SftpFile>)sftp.getDirListing();
            }

            while (files.hasMoreElements()) {
                SftpFile file = (SftpFile) files.nextElement();

                res.addRow();
                res.setStringValue("filename", file.getFilename().trim());
            }
        }
        catch (SftpException e) {
            throw new SecureFTPServiceException("error listing directory: "
                    + e.getMessage(), e);
        }
        finally {
            if (sftp.isConnected()) sftp.disconnect();
        }

        return res;
    }
    
    //
    // Implementation
    //
    
    // Used to stub this out for testing
    protected SecureFTPConnection buildSftpConnection(String hostname,
            int port, String username, String password,
            String privateKeyFilename, String privateKeyPassphrase) throws MissingArgumentException {
        SshParameters sshParams = _getSshParameters(hostname, port, username,
                password, privateKeyFilename, privateKeyPassphrase);
        Sftp sftp =  new Sftp(sshParams);
        
        return new SecureFTPConnection(sftp, sshParams);
    }
    
    private void _connect(MocaContext moca, Sftp sftp, SshParameters params,
            String localDir, String remoteDir, String mode)
            throws SftpException, SecureFTPServiceException {
        try {
            sftp.connect();
        }
        catch (SftpException e) {
            throw new SecureFTPServiceException("Problem connecting to " + 
                params.getSshHostname() + ":" + params.getSshPort(), e);
        }

        _logger.debug("Connected to SFTP server");

        _logger.debug("Transfer mode: " + mode);
        
        if (mode != null && mode.length() > 0) {
            char modeChar = Character.toUpperCase(mode.charAt(0));
            
            if (modeChar == 'I') {
                _logger.debug("Forcing binary transfer mode");
                sftp.setAuto(false);
                sftp.setBinary();
            }
            else if (modeChar == 'A') {
                _logger.debug("Forcing ASCII transfer mode");
                sftp.setAuto(false);
                sftp.setAscii();
            }
        }
        
        _changeDir(sftp, new FTPDirectories(moca, localDir, remoteDir));
    }
    
    /**
     * Handles setting up local/remote directories for FTP
     * @param sftp The secure FTP connection
     * @param dirs The local/remote directories
     * @throws SftpException
     */
    private void _changeDir(Sftp sftp, FTPDirectories dirs) throws SftpException {
        if (dirs.getLocalDirectory() != null) {
            sftp.setLocalDir(new File(dirs.getLocalDirectory()));
        }

        _logger.debug("Local Directory: " + sftp.getLocalDir());
        
        if (dirs.getRemoteDirectory() != null) {
            sftp.setDir(dirs.getRemoteDirectory());
        }
        
        _logger.debug("Remote Directory: " + sftp.getDir());
    }
    
    /*
     * This method is a little screwy in that it deals with all the different combinations of optional arguments
     * and returns an appropriate SshParameters instance given what it's passed. 
     */
    private static SshParameters _getSshParameters(String hostname, int port,
                                                   String username,
                                                   String password,
                                                   String privateKeyFilename,
                                                   String privateKeyPassphrase) throws MissingArgumentException {

        // The combinations below exist when we've been given a PK file.
        if (privateKeyFilename != null && privateKeyFilename.length() > 0) {
            File privateKeyFile = new File(privateKeyFilename);
            
            if (privateKeyPassphrase != null && privateKeyPassphrase.length() > 0) {                
                if (password != null && password.length() > 0) {
                    return new SshParameters(hostname, port, username, password, privateKeyFile, privateKeyPassphrase);
                } 
                else {            
                    return new SshParameters(hostname, port, username, privateKeyPassphrase, privateKeyFile);
                }
            } 
            else { 
                return new SshParameters(hostname, port, username, privateKeyFile);
            }
        }
        
        // If we weren't given a PK file then a password is required.
        else {
            
            if (password == null || password.length() == 0) {
                throw new MissingArgumentException("password");
            }
         
            return new SshParameters(hostname, port, username, password);
        }
    }
    
    // This is only really necessary because on the version of Jscape we're
    // using after you construct a Sftp object you can't access the SshParameters that
    // were used to build it. Let's use this to wrap them together.
    protected static class SecureFTPConnection {
        
        public SecureFTPConnection(Sftp sftp, SshParameters params) {
            _sftp = sftp;
            _params = params;
        }
        
        public Sftp getSftp() {
            return _sftp;
        }
        
        public SshParameters getParameters() {
            return _params;
        }
        
        private final Sftp _sftp;
        private final SshParameters _params;
    }
    
    private static final Logger _logger = LogManager.getLogger(SecureFTPService.class);
}