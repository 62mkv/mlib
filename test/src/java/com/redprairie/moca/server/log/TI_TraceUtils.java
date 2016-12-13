/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2015
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

package com.redprairie.moca.server.log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.redprairie.moca.server.log.exceptions.LoggingException;
import com.redprairie.moca.util.AbstractMocaJunit4TestCase;

import static org.junit.Assert.*;

/**
 * Tests for {@link TraceUtils}.
 * 
 * Copyright (c) 2015 JDA Software
 * All Rights Reserved
 */
public class TI_TraceUtils extends AbstractMocaJunit4TestCase {
    
    /**
     * Tests that when append=false the file should be overwritten.
     * @throws IOException
     * @throws LoggingException
     * @throws InterruptedException
     */
    @Test
    public void testAppendOff() throws IOException, LoggingException, InterruptedException {
        File file = File.createTempFile("TU_TraceUtils", "append-off.log");
        assertTrue(file.delete());
        TraceUtils.enableSessionTracing(file.getAbsolutePath(), false, "*");
        _logger.info("a log message");
        awaitFile(file, 10, TimeUnit.SECONDS);
        TraceUtils.disableSessionTracing();
        TraceUtils.enableSessionTracing(file.getAbsolutePath(), false, "*");
        assertFalse("File should have been deleted as we're not appending",
            file.exists());
        _logger.info("we should have a new log file created");
        awaitFile(file, 10, TimeUnit.SECONDS);
        TraceUtils.disableSessionTracing();
    }
    
    /**
     * Tests that when append=true the file should be added to.
     * @throws IOException
     * @throws LoggingException
     * @throws InterruptedException
     */
    @Test
    public void testAppendOn() throws IOException, LoggingException, InterruptedException {
        File file = File.createTempFile("TU_TraceUtils", "append-on.log");
        assertTrue(file.delete());
        TraceUtils.enableSessionTracing(file.getAbsolutePath(), true, "*");
        _logger.info("a log message");
        awaitFile(file, 10, TimeUnit.SECONDS);
        Path filePath = FileSystems.getDefault().getPath(file.getAbsolutePath(), "");
        long creationTime = 
                Files.readAttributes(filePath, BasicFileAttributes.class).creationTime().toMillis();
        TraceUtils.disableSessionTracing();
        Thread.sleep(1000);
        TraceUtils.enableSessionTracing(file.getAbsolutePath(), true, "*");
        _logger.info("another log message");
        
        // Creation time should be the same because the file hasn't been overwritten
        assertEquals(creationTime,
            Files.readAttributes(filePath, BasicFileAttributes.class).creationTime().toMillis());
        TraceUtils.disableSessionTracing();
    }
    
    // Used to account for asynchronous logging
    private void awaitFile(File file, long time, TimeUnit unit) throws InterruptedException {
        long endtime = System.currentTimeMillis() + unit.toMillis(time);
        boolean exists = false;
        while (endtime > System.currentTimeMillis()) {
            if (file.exists()) {
                exists = true;
                break;
            }
            Thread.sleep(100);
        }
        
        assertTrue("Expected file to exist and it didn't in time", exists);
    }

    private static final Logger _logger = LogManager.getLogger(TI_TraceUtils.class);
}
