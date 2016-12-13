/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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

package com.redprairie.moca.components.base;

import java.io.File;
import java.util.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jscape.inet.ftp.Ftp;
import com.jscape.inet.ftp.FtpException;
import com.jscape.inet.ftp.FtpFile;
import com.redprairie.moca.EditableResults;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaType;
import com.redprairie.util.ArgCheck;

/**
 * Class containing FTP file transfer components. This class is an adapter
 * between MOCA and JScape's ftp factory product.
 * 
 * @see <a href="http://www.jscape.com">jscape home page</a>
 * 
 * Copyright (c) 2012 Sam Corporation All Rights Reserved
 * 
 * @author klehrke
 */
public class FTPService {

    /**
     * Puts a file from the local filesystem into a remote FTP server. 
     * You must either pass the ftp connection to the component or
     * pass the ftp credentials.
     * @param moca the MOCA context.  This argument cannot be null.
     * @param ftp the ftp connection.
     * @param host the hostname or IP address of the FTP server. 
     * @param port the port on which the FTP server is listening it is 21 by default.
     * @param user the username to use for authentication. 
     * @param password the password to use for authentication.
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
     * @throws FTPServiceException if an FTP error occurs.
     */
    public String putFile(MocaContext moca, Ftp ftp, String host, Integer port,
                          String user, String password, String destDir,
                          String destFile, String sourceDir, String sourceFile,
                          String mode) throws FTPServiceException {
        
        boolean checkAuth = _checkCredentials(host, port, user, password);
        ArgCheck.isTrue(ftp != null || checkAuth, 
                "You must either pass the ftp connection or proper credentials");
        boolean close = false;
        Ftp connection = ftp;
        try {
            if (connection == null) {
                connection = new Ftp(host, user, password, port);
                close = true;
            }
            _connect(moca, connection, sourceDir, destDir, DEFAULT_TIMEOUT, mode);
            if (destFile != null) {
                _logger.debug("Uploading File " + sourceFile + " as "
                        + destFile);
                connection.upload(sourceFile, destFile);
                return "Uploaded " + sourceFile + " to " + connection.getHostname()
                        + " as " + destFile + ".";
            }
            else {
                _logger.debug("Uploading File " + sourceFile);
                connection.upload(sourceFile);
                return "Uploaded " + sourceFile + " to " + connection.getHostname();
            }
        }
        catch (FtpException e) {
            throw new FTPServiceException("error uploading file: " + e, e);
        }
        finally {
            if (close) {
                connection.disconnect();
            }
        }
    }

    /**
     * Gets a file from a remote FTP server into the local filesystem.
     * You must either pass the ftp connection to the component or pass
     * the ftp credentials.
     * @param moca the MOCA context.  This argument cannot be null.
     * @param the ftp connection.
     * @param host the hostname or IP address of the FTP server.
     * @param port the port on which the FTP server is listening which is 21 by default.
     * @param user the username to use for authentication. 
     * @param password the password to use for authentication.
     * @param destDir the directory on the local filesystem that the file will
     * be transferred into.  If this argument is null, the running process's
     * current working directory will be used.  
     * If null, the  default directory for the authenticated user will be used.
     * @param destFile the filename to be used on the local filesystem for the
     * new file.  If null, the base name of the source file will be used.
     * @param srcDir the directory on the remote server, where the source
     * file can be found.  If this argument is null, the
     * <code>sourceFile</code> argument must either specify an absolute path or
     * a file in default directory for the authenticated user.
     * @param sourceFile the name of the file to get from the remote host. 
     * This argument cannot be null.
     * @param mode the mode of the file. This can be either "A" (Ascii) or "I"
     * (bInary?).  If it's anything else or <code>null</code>, the default
     * behavior is to try to auto-select a mode based on the file type being
     * sent.
     * @throws FTPServiceException if an FTP error occurs.
     */
    public String getFile(MocaContext moca, Ftp ftp, String host, Integer port,
                          String user, String password, String destDir,
                          String destFile, String srcDir, String sourceFile,
                          String mode) throws FTPServiceException {
        boolean checkAuth = _checkCredentials(host, port, user, password);
        ArgCheck.isTrue(ftp != null || checkAuth, 
                "You must either pass the ftp connection or proper credentials");
        boolean close = false;
        Ftp connection = ftp;
        try {
            if (connection == null) {
                connection = new Ftp(host, user, password, port);
                close = true;
            }
            _connect(moca, connection, destDir, srcDir, DEFAULT_TIMEOUT,
                mode);
            if (destFile != null) {
                _logger.debug("Downloading File " + sourceFile + " as "
                        + destFile);
                connection.download(destFile, sourceFile);
                return "Downloaded " + sourceFile + " from "
                        + connection.getHostname() + " as " + destFile + ".";
            }
            else {
                _logger.debug("Downloading File " + sourceFile);
                connection.download(sourceFile);
                return "Downloaded " + sourceFile + " from "
                        + connection.getHostname() + ".";
            }
        }
        catch (FtpException e) {
            throw new FTPServiceException("error downloading file: " + e, e);
        }
        finally {
            if (close) {
                connection.disconnect();
            }
        }
    }

    /**
     * Renames a file on a remote FTP server.  You must either pass the 
     * ftp connection to the component or pass the ftp credentials.
     * @param moca the MOCA context.  This argument cannot be null.
     * @param the ftp connection.
     * @param host the hostname or IP address of the FTP server. 
     * @param port the port on which the FTP server is listening which is 21 
     * by default.
     * @param user the username to use for authentication.
     * @param password the password to use for authentication.
     * @param destDir the directory on the server that the file will be
     * removed from.  If null, the default directory for the authenticated
     * user will be used.
     * @param oldName the existing name of the file on the remote server. This
     *  argument cannot be null.
     * @param newName the new name of the file. This argument cannot be null.
     * @throws FTPServiceException if an FTP error occurs.
     */
    public String rename(MocaContext moca, Ftp ftp, String host, Integer port,
                         String user, String password, String destDir,
                         String oldName, String newName)
            throws FTPServiceException {
        boolean checkAuth = _checkCredentials(host, port, user, password);
        ArgCheck.isTrue(ftp != null || checkAuth, 
                "You must either pass the ftp connection or proper ftp credentials");
        boolean close = false;
        Ftp connection = ftp;
        try {
            if (connection == null) {
                connection = new Ftp(host, user, password, port);
                close = true;
            }
            _connect(moca, connection, null, destDir, DEFAULT_TIMEOUT,
                null);
            _logger.debug("Renaming file " + oldName + " to " + newName + ".");
            connection.renameFile(oldName, newName);
            return "Renamed " + oldName + " to " + newName + ".";
        }
        catch (FtpException e) {
            throw new FTPServiceException("error renaming file: " + e, e);
        }
        finally {
            if (close) {
                connection.disconnect();
            }
        }
    }

    /**
     * List a directory on a remote FTP server. You must either pass the 
     * ftp connection to the component or pass the ftp credentials.
     * @param moca the MOCA context.  This argument cannot be null.
     * @param the ftp connection.
     * @param host the hostname or IP address of the FTP server.
     * @param port the port on which the FTP server is listening which is 21 
     * by default.
     * @param user the username to use for authentication.
     * @param password the password to use for authentication.
     * @param directory the directory on the server to list.  If one is not specified the 
     * current working directory will be used.
     * @throws FTPServiceException if an FTP error occurs.
     */
    @SuppressWarnings("unchecked")
    public MocaResults list(MocaContext moca, Ftp ftp, String host, Integer port,
                            String user, String password, String directory,
                            String filename) throws FTPServiceException {
        boolean checkAuth = _checkCredentials(host, port, user, password);
        ArgCheck.isTrue(ftp != null || checkAuth, 
                "You must either pass the ftp connection or proper credentials");
        EditableResults res = moca.newResults();
        res.addColumn("filename", MocaType.STRING);
        
        boolean close = false;
        Ftp connection = ftp;
        try {
            if (connection == null) {
                connection = new Ftp(host, user, password, port);
                close = true;
            }
            _connect(moca, connection, null, directory, DEFAULT_TIMEOUT, null);
            _logger.debug("Listing directory " + connection.getDir());
            Enumeration<FtpFile> files = null;
            if (filename != null && filename.length() > 0) {
                // Replace the ? with a dot, a dot with backslash dot and a star
                // with dot star to be compliant with regular expression for
                // our wildcards
                String regex = filename.replace(".", "\\.").replace('?', '.')
                    .replace("*", ".*");
                files = (Enumeration<FtpFile>) connection.getDirListingRegex(regex);
            }
            else {
                files = (Enumeration<FtpFile>) connection.getDirListing();
            }
            while (files.hasMoreElements()) {
                FtpFile file = (FtpFile) files.nextElement();

                res.addRow();
                res.setStringValue("filename", file.getFilename().trim());
            }
        }
        catch (FtpException e) {
            throw new FTPServiceException("error listing directory: " + e, e);
        }
        finally {
            if (close) {
                connection.disconnect();
            }
        }

        return res;
    }

    /***
     * Connects to the ftp server.
     * 
     * @param moca
     * @param host
     * @param port
     * @param user
     * @param proxyHost
     * @param proxyPort
     * @param proxyUser
     * @param proxypass
     * @param proxyType
     * @param password
     * @param localDir
     * @param remoteDir
     * @param mode
     * @return the ftp connection
     * @throws FTPServiceException
     * @throws FtpException
     */
    public MocaResults connectFTP(MocaContext moca, String host, int port,
                                  String user, String password,
                                  String proxyHost, Integer proxyPort,
                                  String proxyUser, String proxyPass,
                                  String proxyType, int connTimeout,
                                  String localDir, String remoteDir, String mode)
            throws FTPServiceException {

        Ftp ftp = new Ftp(host, user, password, port);
        try {
            _addProxy(ftp, proxyHost, proxyPort, proxyUser, proxyPass, proxyType);
            _connect(moca, ftp, localDir, remoteDir, connTimeout , mode);
        }
        catch (FtpException e) {
            throw new FTPServiceException("Could not connect to ftp server", e);
        }
        EditableResults res = moca.newResults();
        res.addColumn("ftp", MocaType.OBJECT);
        res.addRow();
        res.setValue("ftp", ftp);
        return res;
    }

    /***
     * This disconnects us from the FTP client.
     * 
     * @param ftp - the ftp object.
     * @throws FTPServiceException
     * @throws FtpException
     */
    public void disconnectFTP(Ftp ftp) throws FTPServiceException {
        ArgCheck.notNull(ftp);
        ftp.disconnect();
    }
    
    
    /**
     * Removes a file from a remote FTP server. You must either pass the 
     * ftp connection to the component or pass the ftp credentials.
     * @param moca the MOCA context.  This argument cannot be null.
     * @param the ftp connection.
     * @param host the hostname or IP address of the FTP server.
     * @param port the port on which the FTP server is listening which is 21 by default.
     * @param user the username to use for authentication.
     * @param password the password to use for authentication.
     * @param targetDir the directory on the server that the file will be
     * removed from.  If null, the default directory for the authenticated
     * user will be used.
     * @param targetFile the filename to be removed from the remote server.
     * This argument cannot be null.
     * @throws FTPServiceException if an FTP error occurs.
     */
    public String removeFile(MocaContext moca, Ftp ftp, 
                             String host, Integer port, String user, String password,
                             String targetDir,
                             String targetFile) throws FTPServiceException {
        boolean checkAuth = _checkCredentials(host, port, user, password);
        ArgCheck.isTrue(ftp != null || checkAuth, 
                "You must either pass the ftp connection or proper credentials");
        boolean close = false;
        Ftp connection = ftp;
        try {
            if (connection == null) {
                connection = new Ftp(host, user, password, port);
                close = true;
            }

            _connect(moca, connection, null, targetDir, DEFAULT_TIMEOUT, null);
            _logger.debug( "Removing file " + targetFile);
            connection.deleteFile(targetFile);
            return "Deleted " + targetFile + " from " + connection.getHostname() + ".";
        }
        catch (FtpException e) {
            throw new FTPServiceException("error deleting file: " + e, e);
        }
        finally {
            if (close) {
                connection.disconnect();
            }
        }
    }

    //
    // Implementation
    //
    /***
     * A method to manually change the
     * remote and local directory on the ftp 
     * connection.
     * 
     * 
     * @param ftp
     * @param dirs The local and remote directories with resolved variables
     * @throws FtpException
     */
    private void _changeDir(Ftp ftp, FTPDirectories dirs) throws FtpException{
        
        // Set the local directory if it was provided and isn't empty.
        if (dirs.getLocalDirectory() != null && !dirs.getLocalDirectory().isEmpty()) {
            ftp.setLocalDir(new File(dirs.getLocalDirectory()));
        }

        _logger.debug("Local Directory: " + ftp.getLocalDir());

        // Set the remote directory if it was provided and isn't empty.
        if (dirs.getRemoteDirectory() != null && !dirs.getRemoteDirectory().isEmpty()) {
            ftp.setDir(dirs.getRemoteDirectory());
        }

        _logger.debug("Remote Directory: " + ftp.getDir());
        
    }
    
    /***
     * 
     * Changes the mode of transfer. 
     * 
     * Available modes are:
     *  I - Binary mode
     *  A - Ascii mode
     * 
     * @param ftp
     * @param mode
     * @throws FtpException
     */
    private void _changeMode(Ftp ftp, String mode) throws FtpException{
        if (mode != null && mode.length() > 0) {
            char modeChar = Character.toUpperCase(mode.charAt(0));

            if (modeChar == 'I') {
                _logger.debug("Forcing binary transfer mode");
                ftp.setAuto(false);
                ftp.setBinary();
            }
            else if (modeChar == 'A') {
                _logger.debug("Forcing ASCII transfer mode");
                ftp.setAuto(false);
                ftp.setAscii();
            }
        }
    }

    /***
     * 
     * Sets the proxy information on the ftp connection.
     * 
     * @param ftp
     * @param proxyHost
     * @param proxyPort
     * @param proxyUser
     * @param proxyPass
     * @param proxyType
     */
    private void _addProxy(Ftp ftp, String proxyHost, Integer proxyPort,
                   String proxyUser, String proxyPass, String proxyType) {
        // Check if we're using proxy
        // Proxy type supports HTTP, SOCKS5
        
        if (proxyHost != null && !proxyHost.isEmpty()) {
            if (_logger.isDebugEnabled()) {
                String debugInfo = String.format(
                        "Proxy Use: host=[%s], port=[%d], user=[%s], password=[%s], type=[%s]",
                        proxyHost, proxyPort, proxyUser, proxyPass, proxyType);
                _logger.debug(debugInfo);
            }
            ftp.setProxyHost(proxyHost, proxyPort);
            if (proxyType != null && !proxyType.isEmpty()) {
                ftp.setProxyType(proxyType);
            }
            if (proxyUser != null && !proxyUser.isEmpty()) {
                ftp.setProxyAuthentication(proxyUser, proxyPass);
            }
        }
    }
    
    /***
     * Connects to the ftp server.  This also sets the
     * the local and remote directory.
     * 
     * @param moca
     * @param ftp
     * @param localDir
     * @param remoteDir
     * @param connTimeout
     * @param mode
     * @throws FtpException
     * @throws FTPServiceException
     */
    private void _connect(MocaContext moca, Ftp ftp, String localDir,
                          String remoteDir, int connTimeout, String mode) throws FtpException,
            FTPServiceException {
        try {
            if (!ftp.isConnected()) {
                ftp.setTimeout(connTimeout);
                ftp.connect();
            }
        }
        catch (FtpException e) {
            throw new FTPServiceException("Problem connecting to "
                    + ftp.getHostname() + ":" + ftp.getPort(), e);
        }

        _logger.debug("Connected to FTP server");
        _logger.debug("Transfer mode: " + mode);
        _changeMode(ftp, mode);
        _changeDir(ftp, new FTPDirectories(moca, localDir, remoteDir));
    }
    
    /***
     * This is a private method to ensure that all the 
     * required arguments for manual authentification requests
     * are there. Since we now take an ftp object as well (already authenticated),
     * we can't set the required argument through the mcmd file.
     * @param hostname
     * @param port
     * @param user
     * @param password
     * @return if all required arguments exist
     */
    private boolean _checkCredentials(String hostname, Integer port,
                                      String user, String password) {
        return hostname != null && port != null && user != null && password != null;
    }

    public static final int DEFAULT_TIMEOUT = 30000;
    private static final Logger _logger = LogManager.getLogger(FTPService.class);
}
