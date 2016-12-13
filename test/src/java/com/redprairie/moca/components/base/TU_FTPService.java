/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2013
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

import org.junit.Test;
import org.mockito.Mockito;

import com.jscape.inet.ftp.Ftp;
import com.jscape.inet.ftp.FtpException;
import com.jscape.inet.ftp.FtpFile;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.SimpleResults;

import static org.junit.Assert.*;

/**
 * Tests for FTPService
 * 
 * Copyright (c) 2013 Sam Corporation
 * All Rights Reserved
 * 
 * @author rrupp
 */
public class TU_FTPService {
    
    @Test
    public void testFtpPut() throws FTPServiceException, FtpException {
        // Also test environment variable replacement in both local/remote directories
        String envLesDir = "C:/local/trunk/les";
        String envRemoteDir = "C:/remote/trunk/les";
        String remoteName = "test-rename.txt";
        String origLocalName = "test.txt";
        FTPService service = new FTPService();
        MocaContext ctx = Mockito.mock(MocaContext.class);
        Mockito.when(ctx.getSystemVariable("LESDIR")).thenReturn(envLesDir);
        Mockito.when(ctx.getSystemVariable("MY_REMOTE_DIR")).thenReturn(envRemoteDir);
        Ftp ftp = Mockito.mock(Ftp.class);
        service.putFile(ctx, ftp, null, null, null, null, "$MY_REMOTE_DIR/log", remoteName,
        		"$LESDIR/log", origLocalName, null);
        
        // Verify correct directories were set and upload occurred
        Mockito.verify(ftp).setLocalDir(new File(envLesDir + "/log"));
        Mockito.verify(ftp).setDir(envRemoteDir + "/log");
        Mockito.verify(ftp).upload(origLocalName, remoteName);
    }
    
    @Test
    public void testFtpGet() throws FTPServiceException, FtpException {
        String envLesDir = "C:/local/trunk/les";
        String envRemoteDir = "C:/remote/trunk/les";
        String remoteName = "test.txt";
        FTPService service = new FTPService();
        MocaContext ctx = Mockito.mock(MocaContext.class);
        Mockito.when(ctx.getSystemVariable("LESDIR")).thenReturn(envLesDir);
        Mockito.when(ctx.getSystemVariable("MY_REMOTE_DIR")).thenReturn(envRemoteDir);
        Ftp ftp = Mockito.mock(Ftp.class);
        // Don't rename the file locally
        service.getFile(ctx, ftp, null, null, null, null,
        		"$LESDIR/log", null, "$MY_REMOTE_DIR/log", remoteName, null);
        
        // Verify correct directories were set and download occurred
        Mockito.verify(ftp).setLocalDir(new File(envLesDir + "/log"));
        Mockito.verify(ftp).setDir(envRemoteDir + "/log");
        Mockito.verify(ftp).download("test.txt");
    }
    
    @Test
    public void testFtpGetWithRename() throws FTPServiceException, FtpException {
        String envLesDir = "C:/local/trunk/les";
        String envRemoteDir = "C:/remote/trunk/les";
        String remoteName = "test.txt";
        String localRenameFile = "test-rename-local.txt";
        FTPService service = new FTPService();
        MocaContext ctx = Mockito.mock(MocaContext.class);
        Mockito.when(ctx.getSystemVariable("LESDIR")).thenReturn(envLesDir);
        Mockito.when(ctx.getSystemVariable("MY_REMOTE_DIR")).thenReturn(envRemoteDir);
        Ftp ftp = Mockito.mock(Ftp.class);
        service.getFile(ctx, ftp, null, null, null, null, "$LESDIR/log", localRenameFile,
        		"$MY_REMOTE_DIR/log", remoteName, null);
        
        // Verify correct directories were set and download occurred
        Mockito.verify(ftp).setLocalDir(new File(envLesDir + "/log"));
        Mockito.verify(ftp).setDir(envRemoteDir + "/log");
        Mockito.verify(ftp).download(localRenameFile, remoteName);
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testFtpDirList() throws FTPServiceException, FtpException {
        String remoteDir = "C:/remote/dev/les/log";
        FTPService service = new FTPService();
        String filename = "test.txt";
        MocaContext ctx = Mockito.mock(MocaContext.class);
        Mockito.when(ctx.newResults()).thenReturn(new SimpleResults());
        Mockito.when(ctx.getSystemVariable("MY_REMOTE_DIR")).thenReturn(remoteDir);
        Ftp ftp = Mockito.mock(Ftp.class);
        Enumeration<FtpFile> files = Mockito.mock(Enumeration.class);
        FtpFile mockFile = Mockito.mock(FtpFile.class);
        Mockito.when(mockFile.getFilename()).thenReturn(filename);
        Mockito.when(ftp.getDirListing()).thenReturn(files);
        // Mock that there's only one element
        Mockito.when(files.hasMoreElements()).thenReturn(true, false);
        Mockito.when(files.nextElement()).thenReturn(mockFile);
        MocaResults res = service.list(ctx, ftp, null, null, null, null, "%MY_REMOTE_DIR%", null);
        
        // Verify correct directories were set and download occurred
        // Local directory shouldn't need to be set in this case
        Mockito.verify(ftp, Mockito.times(0)).setLocalDir(Mockito.any(File.class));
        Mockito.verify(ftp).setDir(remoteDir);
        Mockito.verify(ftp).getDirListing();
        
        // Verify results
        assertEquals(1, res.getRowCount());
        assertTrue(res.next());
        assertEquals(filename, res.getString("filename"));
    }
    
    @Test
    public void testFtpRenameFile() throws FTPServiceException, FtpException {
        String envLesDir = "C:/local/trunk/les";
        String envRemoteDir = "C:/remote/trunk/les";
        String oldName = "test-old.txt";
        String newName = "test-new.txt";
        FTPService service = new FTPService();
        MocaContext ctx = Mockito.mock(MocaContext.class);
        Mockito.when(ctx.getSystemVariable("LESDIR")).thenReturn(envLesDir);
        Mockito.when(ctx.getSystemVariable("MY_REMOTE_DIR")).thenReturn(envRemoteDir);
        Ftp ftp = Mockito.mock(Ftp.class);
        service.rename(ctx, ftp, null, null, null, null, envRemoteDir, oldName, newName);
        
        // Verify correct directories were set and rename occurred
        // Local directory shouldn't need to be set in this case
        Mockito.verify(ftp, Mockito.times(0)).setLocalDir(Mockito.any(File.class));
        Mockito.verify(ftp).setDir(envRemoteDir);
        Mockito.verify(ftp).renameFile(oldName, newName);
    }
    
    @Test
    public void testFtpRemoveFile() throws FTPServiceException, FtpException {
        String envLesDir = "C:/local/trunk/les";
        String envRemoteDir = "C:/remote/trunk/les";
        String fileName = "test.txt";
        FTPService service = new FTPService();
        MocaContext ctx = Mockito.mock(MocaContext.class);
        Mockito.when(ctx.getSystemVariable("LESDIR")).thenReturn(envLesDir);
        Mockito.when(ctx.getSystemVariable("MY_REMOTE_DIR")).thenReturn(envRemoteDir);
        Ftp ftp = Mockito.mock(Ftp.class);
        service.removeFile(ctx, ftp, null, null, null, null, envRemoteDir, fileName);
        
        // Verify correct directories were set and rename occurred
        // Local directory shouldn't need to be set in this case
        Mockito.verify(ftp, Mockito.times(0)).setLocalDir(Mockito.any(File.class));
        Mockito.verify(ftp).setDir(envRemoteDir);
        Mockito.verify(ftp).deleteFile(fileName);
    }
    
    // Test that either a FTP connection or the credentials are passed in
    @Test
    public void testInvalidArgs() throws FTPServiceException {
        FTPService service = new FTPService();
        MocaContext ctx = Mockito.mock(MocaContext.class);
        try {
            service.getFile(ctx, null, null, null, null, null, null, null, null, null, null);
            fail("Should have received IllegalArgumentException due to no credentials or connection.");
        }
        catch (IllegalArgumentException expected) {}
        
        try {
            service.putFile(ctx, null, null, null, null, null, null, null, null, null, null);
            fail("Should have received IllegalArgumentException due to no credentials or connection.");
        }
        catch (IllegalArgumentException expected) {}
        
        try {
            service.list(ctx, null, null, null, null, null, null, null);
            fail("Should have received IllegalArgumentException due to no credentials or connection.");
        }
        catch (IllegalArgumentException expected) {}
        
        try {
            service.rename(ctx, null, null, null, null, null, null, null, null);
            fail("Should have received IllegalArgumentException due to no credentials or connection.");
        }
        catch (IllegalArgumentException expected) {}
    }
}
