/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 */

package com.redprairie.moca.components.base;

import java.io.File;
import java.util.Enumeration;

import org.junit.Test;
import org.mockito.Mockito;

import com.jscape.inet.sftp.Sftp;
import com.jscape.inet.sftp.SftpException;
import com.jscape.inet.sftp.SftpFile;
import com.jscape.inet.ssh.util.SshParameters;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.exceptions.MissingArgumentException;

import static org.junit.Assert.*;

/**
 * Tests for SecureFTPService
 * 
 * Copyright (c) 2013 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_SecureFTPService {
    
    @Test
    public void testSftpPut() throws MissingArgumentException, SecureFTPServiceException, SftpException {
        // Also test environment variable replacement in both local/remote directories
        String envLesDir = "C:/local/trunk/les";
        String envRemoteDir = "C:/remote/trunk/les";
        
        final Sftp sftp = Mockito.mock(Sftp.class);
        final SshParameters params = Mockito.mock(SshParameters.class);
        SecureFTPService service = new SecureFTPService() {
            @Override
            protected SecureFTPConnection buildSftpConnection(String hostname,
                    int port, String username, String password,
                    String privateKeyFilename, String privateKeyPassphrase) 
                            throws MissingArgumentException {
                return new SecureFTPConnection(sftp, params);
            }
        };
        
        MocaContext ctx = Mockito.mock(MocaContext.class);
        Mockito.when(ctx.getSystemVariable("LESDIR")).thenReturn(envLesDir);
        Mockito.when(ctx.getSystemVariable("MY_REMOTE_DIR")).thenReturn(envRemoteDir);
        
        service.putFile(ctx, "remotehost", 9999, "SUPER", "SUPER", "privatefile", "privatepass",
                "$MY_REMOTE_DIR/log", "test-rename.txt", "$LESDIR/log", "test.txt", null);
        
        // Verify correct directories were set and upload occurred
        Mockito.verify(sftp).setLocalDir(new File(envLesDir + "/log"));
        Mockito.verify(sftp).setDir(envRemoteDir + "/log");
        Mockito.verify(sftp).upload("test.txt", "test-rename.txt");
    }
    
    @Test
    public void testSftpGet() throws MissingArgumentException,
            SecureFTPServiceException, SftpException {
        String envLesDir = "C:/local/trunk/les";
        String envRemoteDir = "C:/remote/trunk/les";
        final Sftp sftp = Mockito.mock(Sftp.class);
        final SshParameters params = Mockito.mock(SshParameters.class);
        SecureFTPService service = new SecureFTPService() {
            @Override
            protected SecureFTPConnection buildSftpConnection(String hostname,
                    int port, String username, String password,
                    String privateKeyFilename, String privateKeyPassphrase) 
                            throws MissingArgumentException {
                return new SecureFTPConnection(sftp, params);
            }
        };
        
        MocaContext ctx = Mockito.mock(MocaContext.class);
        Mockito.when(ctx.getSystemVariable("LESDIR")).thenReturn(envLesDir);
        Mockito.when(ctx.getSystemVariable("MY_REMOTE_DIR")).thenReturn(envRemoteDir);
        // Don't rename the file locally
        service.getFile(ctx, "remotehost", 9999, "SUPER", "SUPER", "privatefile", "privatepass",
                "$LESDIR/log", null, "$MY_REMOTE_DIR/log", "test.txt", null);
        
        // Verify correct directories were set and download occurred
        Mockito.verify(sftp).setLocalDir(new File(envLesDir + "/log"));
        Mockito.verify(sftp).setDir(envRemoteDir + "/log");
        Mockito.verify(sftp).download("test.txt");
    }
    
    @Test
    public void testSftpGetWithRename() throws MissingArgumentException,
            SecureFTPServiceException, SftpException {
        String envLesDir = "C:/local/trunk/les";
        String envRemoteDir = "C:/remote/trunk/les";
        String localRenameFile = "test-rename-local.txt";
        final Sftp sftp = Mockito.mock(Sftp.class);
        final SshParameters params = Mockito.mock(SshParameters.class);
        SecureFTPService service = new SecureFTPService() {
            @Override
            protected SecureFTPConnection buildSftpConnection(String hostname,
                    int port, String username, String password,
                    String privateKeyFilename, String privateKeyPassphrase) 
                            throws MissingArgumentException {
                return new SecureFTPConnection(sftp, params);
            }
        };
        MocaContext ctx = Mockito.mock(MocaContext.class);
        Mockito.when(ctx.getSystemVariable("LESDIR")).thenReturn(envLesDir);
        Mockito.when(ctx.getSystemVariable("MY_REMOTE_DIR")).thenReturn(envRemoteDir);
        service.getFile(ctx, "remotehost", 9999, "SUPER", "SUPER", "privatefile", "privatepass",
                "$LESDIR/log", localRenameFile, "$MY_REMOTE_DIR/log", "test.txt", null);
        
        // Verify correct directories were set and download occurred
        Mockito.verify(sftp).setLocalDir(new File(envLesDir + "/log"));
        Mockito.verify(sftp).setDir(envRemoteDir + "/log");
        Mockito.verify(sftp).download(localRenameFile, "test.txt");
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testSftpDirList() throws MissingArgumentException,
            SecureFTPServiceException, SftpException {
        String remoteDir = "C:/remote/dev/les/log";
        final Sftp sftp = Mockito.mock(Sftp.class);
        final SshParameters params = Mockito.mock(SshParameters.class);
        SecureFTPService service = new SecureFTPService() {
            @Override
            protected SecureFTPConnection buildSftpConnection(String hostname,
                    int port, String username, String password,
                    String privateKeyFilename, String privateKeyPassphrase) 
                            throws MissingArgumentException {
                return new SecureFTPConnection(sftp, params);
            }
        };
        MocaContext ctx = Mockito.mock(MocaContext.class);
        Mockito.when(ctx.newResults()).thenReturn(new SimpleResults());
        Mockito.when(ctx.getSystemVariable("MY_REMOTE_DIR")).thenReturn(remoteDir);
        Enumeration<SftpFile> files = Mockito.mock(Enumeration.class);
        SftpFile mockFile = Mockito.mock(SftpFile.class);
        Mockito.when(mockFile.getFilename()).thenReturn("test.txt");
        Mockito.when(sftp.getDirListing()).thenReturn(files);
        // Mock that there's only one element
        Mockito.when(files.hasMoreElements()).thenReturn(true, false);
        Mockito.when(files.nextElement()).thenReturn(mockFile);
        MocaResults res = service.list(ctx, "remotehost", 9999, "SUPER", "SUPER", "privatefile", "privatepass",
                 "$MY_REMOTE_DIR", null);
        
        
        // Verify correct directories were set and download occurred
        // Local directory shouldn't need to be set in this case
        Mockito.verify(sftp, Mockito.times(0)).setLocalDir(Mockito.any(File.class));
        Mockito.verify(sftp).setDir(remoteDir);
        Mockito.verify(sftp).getDirListing();
        
        // Verify results
        assertEquals(1, res.getRowCount());
        assertTrue(res.next());
        assertEquals("test.txt", res.getString("filename"));
    }
    
    @Test
    public void testSftpRenameFile() throws MissingArgumentException,
            SecureFTPServiceException, SftpException {
        String envLesDir = "C:/local/trunk/les";
        String envRemoteDir = "C:/remote/trunk/les";
        String oldName = "test-old.txt";
        String newName = "test-new.txt";
        
        final Sftp sftp = Mockito.mock(Sftp.class);
        final SshParameters params = Mockito.mock(SshParameters.class);
        SecureFTPService service = new SecureFTPService() {
            @Override
            protected SecureFTPConnection buildSftpConnection(String hostname,
                    int port, String username, String password,
                    String privateKeyFilename, String privateKeyPassphrase) 
                            throws MissingArgumentException {
                return new SecureFTPConnection(sftp, params);
            }
        };
        MocaContext ctx = Mockito.mock(MocaContext.class);
        Mockito.when(ctx.getSystemVariable("LESDIR")).thenReturn(envLesDir);
        Mockito.when(ctx.getSystemVariable("MY_REMOTE_DIR")).thenReturn(envRemoteDir);
        service.rename(ctx, "remotehost", 9999, "SUPER", "SUPER", "privatefile", "privatepass",
                envRemoteDir, oldName, newName);
        
        // Verify correct directories were set and rename occurred
        // Local directory shouldn't need to be set in this case
        Mockito.verify(sftp, Mockito.times(0)).setLocalDir(Mockito.any(File.class));
        Mockito.verify(sftp).setDir(envRemoteDir);
        Mockito.verify(sftp).renameFile(oldName, newName);
    }
    
    @Test
    public void testSftpRemoveFile() throws MissingArgumentException,
            SecureFTPServiceException, SftpException {
        String envLesDir = "C:/local/trunk/les";
        String envRemoteDir = "C:/remote/trunk/les";
        String fileName = "test.txt";
        final Sftp sftp = Mockito.mock(Sftp.class);
        final SshParameters params = Mockito.mock(SshParameters.class);
        SecureFTPService service = new SecureFTPService() {
            @Override
            protected SecureFTPConnection buildSftpConnection(String hostname,
                    int port, String username, String password,
                    String privateKeyFilename, String privateKeyPassphrase) 
                            throws MissingArgumentException {
                return new SecureFTPConnection(sftp, params);
            }
        };
        MocaContext ctx = Mockito.mock(MocaContext.class);
        Mockito.when(ctx.getSystemVariable("LESDIR")).thenReturn(envLesDir);
        Mockito.when(ctx.getSystemVariable("MY_REMOTE_DIR")).thenReturn(envRemoteDir);
        service.removeFile(ctx, "remotehost", 9999, "SUPER", "SUPER", "privatefile", "privatepass",
                   envRemoteDir, fileName);
        
        // Verify correct directories were set and rename occurred
        // Local directory shouldn't need to be set in this case
        Mockito.verify(sftp, Mockito.times(0)).setLocalDir(Mockito.any(File.class));
        Mockito.verify(sftp).setDir(envRemoteDir);
        Mockito.verify(sftp).deleteFile(fileName);
    }
    
    @Test
    public void testNoPasswordOrKeyGiven() throws SecureFTPServiceException {
        SecureFTPService service = new SecureFTPService();
        MocaContext ctx = Mockito.mock(MocaContext.class);
        Mockito.when(ctx.newResults()).thenReturn(new SimpleResults());
        try {
            service.putFile(ctx, "remotehost", 9999, "SUPER", null, null, null,
                    "$MY_REMOTE_DIR/log", "test-rename.txt", "$LESDIR/log", "test.txt", null);
            fail("Should have receivied a MissingArgumentException due to key or password not provided.");
        }
        catch (MissingArgumentException expected) {
            assertEquals("password", expected.getArgList()[0].getValue());
        }
        
        try {
            service.getFile(ctx, "remotehost", 9999, "SUPER", null, null, null,
                    "$LESDIR/log", "newfile.txt", "$MY_REMOTE_DIR/log", "test.txt", null);
            fail("Should have receivied a MissingArgumentException due to key or password not provided.");
        }
        catch (MissingArgumentException expected) {
            assertEquals("password", expected.getArgList()[0].getValue());
        }
        
        try {
            service.list(ctx, "remotehost", 9999, "SUPER", null, null, null,
                        "$MY_REMOTE_DIR", null);
            fail("Should have receivied a MissingArgumentException due to key or password not provided.");
        }
        catch (MissingArgumentException expected) {
            assertEquals("password", expected.getArgList()[0].getValue());
        }
        
        
        try {
            service.rename(ctx, "remotehost", 9999, "SUPER", null, null, null,
                      "C:/remote", "old.txt", "new.txt");
            fail("Should have receivied a MissingArgumentException due to key or password not provided.");
        }
        catch (MissingArgumentException expected) {
            assertEquals("password", expected.getArgList()[0].getValue());
        }
        
        try {
            service.removeFile(ctx, "remotehost", 9999, "SUPER", null, null, null,
                     "C:/remote", "delete.txt");
            fail("Should have receivied a MissingArgumentException due to key or password not provided.");
        }
        catch (MissingArgumentException expected) {
            assertEquals("password", expected.getArgList()[0].getValue());
        }
        
        
    }
}
