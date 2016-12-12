/*
 *  $URL: https://athena.redprairie.com/svn/prod/devtools/trunk/bootstrap/eclipse/codetemplates.xml $
 *  $Author: mlange $
 *  $Date: 2009-06-18 22:49:22 -0500 (Thu, 18 Jun 2009) $
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2015
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

package com.redprairie.moca.server.log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import com.redprairie.moca.util.AbstractMocaJunit4TestCase;
import org.jboss.logging.Logger;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Validates that JBoss Logging works correctly
 * in conjunction with Log4j
 * Copyright (c) 2015 JDA Corporation
 * All Rights Reserved
 */
public class TU_JBossLogging extends AbstractMocaJunit4TestCase {

    static {
        // this is also set in build.xml to make sure that the first
        // test that runs with jboss logging will set up correctly
        System.setProperty("org.jboss.logging.provider", "log4j");
    }

    @Override
    protected void mocaSetUp() throws Exception {
        LoggingConfigurator.configure(true);
    }

//    /**
//     * This is a regression test that was failing because when we
//     * were using JBoss logging that communicates with the Log4j 1.2 API
//     * when we enable session tracing we're putting the "*" value in the
//     * ThreadContext for "moca-trace-level". This is done then via a Log4j2
//     * filter where if that * value is present we skip the actual logger configs.
//     * However, the JBoss logging API does two things to check if the log level is enabled,
//     * first it checks whether debug is enabled which actually is true because it goes
//     * jboss logging --> log4j 1.2 bridge ---> log4j2 ---> {@link PackageFilter} which returns
//     * true because * is set and the com.redprairie package is included. However, the JBoss
//     * logging check also check that the effectiveLevel of the logger is >= DEBUG, the effective
//     * level for the logger will actually be "INFO" because we're not modifying the logger level
//     * here but rather having this work due to the use of our filter {@link PackageFilter}.
//     * This is resolved by updating JBoss logging to use the log4j2 API directly.
//     */
//    @Test
//    public void testJBossLoggingWithSessionLevelTracing() {
//        Logger log = Logger.getLogger(TU_JBossLogging.class);
//        assertFalse(log.isDebugEnabled());
//        assertFalse(log.isTraceEnabled());
//        _moca.setTraceLevel("*");
//        assertTrue("Should have had debug log level enabled", log.isDebugEnabled());
//        assertTrue("Should have had trace log level enabled", log.isTraceEnabled());
//    }
//    this doesn't work anymore because we are back to using the bridge

//    /**
//     * This is a regression test that was actually failing due to an issue with the
//     * log4j 1.2 to 2.x bridge where {@link Category#getEffectiveLevel()} didn't
//     * translate the log level "ALL" and instead assigned it to "OFF" so this would fail
//     * even though our logger level was ALL. This was fixed in log4j but we're actually
//     * fixing it by just updating JBoss logging to use Log4j2 natively rather than using
//     * the log4j 1.2 bridge.
//     */
//    @Test
//    public void testJBossLoggingLevelAll() {
//        Logger log = Logger.getLogger("com.redprairie.moca.test.jboss.logging");
//        assertTrue("Should have had debug log level enabled", log.isDebugEnabled());
//        assertTrue("Should have had trace log level enabled", log.isTraceEnabled());
//    }
//    this doesn't work anymore because we are back to using the bridge

    @Test
    public void testActualLogging() throws IOException {
        final String MESSAGE = "DOESLOGGINGWORK";
        final File log = new File(new File(System.getenv("LESDIR"), "log"), "TU_JBossLogging.testActualLogging.log");
        log.deleteOnExit();
        try {
            Logger l = Logger.getLogger("com.redprairie.moca.server.log.TU_JBossLogging.testActualLogging");
            l.error(MESSAGE);
            final List<String> strings = Files.readAllLines(log.toPath(), StandardCharsets.UTF_8);
            boolean found = false;
            for (String s: strings) {
                if (s.contains(MESSAGE)) found = true;
            }
            if (!found) fail("JBoss Logging didn't work!");
        }
        finally {
            log.delete();
        }
    }
}
