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

package com.sam.moca.alerts;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.sam.moca.MocaException;
import com.sam.moca.alerts.util.AlertUtils;
import com.sam.moca.components.ems.AlertReader;
import com.sam.moca.util.AbstractMocaTestCase;

/**
 * Unit test for EMS Alerts
 * 
 * <b>
 * 
 * <pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre>
 * 
 * </b>
 * 
 * @author grady
 * @version $Revision$
 */
public class TU_Alert extends AbstractMocaTestCase {
    @SuppressWarnings("deprecation")
    public void testAlertCreation() throws Exception {
        Alert alert = new Alert.AlertBuilder("SomeEvent", "TEST-SYSTEM").
            primerFlag(false).priority(Alert.PRIORITY_INFORMATIONAL).
            duplicateValue("dupval").build();
        
        Date dt = new Date();
        assertEquals(dt.getDate(), alert.getDateTime().getDate());
        assertEquals(dt.getMonth(), alert.getDateTime().getMonth());
        assertEquals(dt.getYear(), alert.getDateTime().getYear());

        assertNotNull(alert.getUrl());
        assertTrue(alert.getUrl().length() > 0);
        
        assertTrue(alert.isInTransFlag());      // This should be the default
        assertFalse(alert.isPrimerFlag());      // This should be FALSE by default
        assertNull(alert.getEventDefinition()); // This should be NULL by default
        
        String xml = alert.asXML();
        
        assertTrue(!xml.equals(""));
        
        // Parse the XML and spot check some nodes.
        Document document = DocumentHelper.parseText(xml);
        
        Node version = document.selectSingleNode("//ems-alert/version");
        assertEquals("2", version.getText());
        
        Node inTrans = document.selectSingleNode("//ems-alert/in-trans");
        assertEquals("1", inTrans.getText());
        
        Node srcSys = document.selectSingleNode("//ems-alert/event-data/source-system");
        assertEquals("TEST-SYSTEM", srcSys.getText());
        
        // Before writing, reset the inTransFlag so that it writes 
        // immediately rather than waiting until the end of the transaction
        alert.setInTransFlag(false);

        alert.write();
        
        File alertFile = new File(AlertUtils.getSpoolDir(), alert.getFileName());
        assertNotNull(alertFile);
        
        // Clean everything up
        AlertFile af = new AlertFile(alertFile);
        assertTrue(af.getXmlContents().length() > 0);
        
        af.complete(true);
        
        // See if this went to the success directory
        File successFile = new File(AlertUtils.getProcessedDir(), alert.getFileName());
        assertTrue(successFile.exists());
        
        // Clean up the file
        if (successFile.exists()) {
            assertTrue(successFile.delete());
        }
    }

    public void testEventPrime() throws Exception {
        Alert alert = new Alert.AlertBuilder("SomeEvent", "TEST-SYSTEM").
            priority(Alert.PRIORITY_INFORMATIONAL).primerFlag(true).
            inTransFlag(false).duplicateValue("dupval").build();
        
        alert.setEventName("SomeEvent");
        alert.setPriority(Alert.PRIORITY_INFORMATIONAL);
        alert.setPrimerFlag(true);
        alert.setInTransFlag(false);
        alert.setDuplicateValue("dupval");
        alert.setSourceSystem("TEST-SYSTEM");
        
        EventDefinition ed = new EventDefinition.EventDefBuilder("SomeEvent",
                "SomeDescription", "SomeSubject", "SomeMessage").
                htmlMessage("<h1>SomeHTMLMessage</h1>").
                build();
        
        alert.setEventDefinition(ed);
        
        assertEquals(EventDefinition.DEFAULT_PRIME_KEY_VAL, alert.getKeyValue());
        
        String xml = alert.asXML();
        
        assertTrue(!xml.equals(""));
        
        // Parse the XML and spot check some nodes.
        Document document = DocumentHelper.parseText(xml);
        
        Node primerFlag = document.selectSingleNode("//ems-alert/event-data/primer-flg");
        assertEquals("1", primerFlag.getText());
        
        Node keyValue = document.selectSingleNode("//ems-alert/event-data/key-val");
        assertEquals(EventDefinition.DEFAULT_PRIME_KEY_VAL, keyValue.getText());
        
        Node timeZone = document.selectSingleNode("//ems-alert/event-data/stored-tz");
        assertEquals("----", timeZone.getText());
        
        // Test the messages and stuff
        Element locale = (Element) document.selectSingleNode("//ems-alert/event-data/event-def/messages/message");
        assertEquals("US_ENGLISH", locale.attributeValue("locale-id"));
        

        Node description = document.selectSingleNode("//ems-alert/event-data/event-def/messages/message/description");
        assertEquals("SomeDescription", description.getText());
        
        Node subject = document.selectSingleNode("//ems-alert/event-data/event-def/messages/message/subject");
        assertEquals("SomeSubject", subject.getText());
        
        Node htmlMessage = document.selectSingleNode("//ems-alert/event-data/event-def/messages/message/html-message");
        assertEquals("&lt;h1&gt;SomeHTMLMessage&lt;/h1&gt;", htmlMessage.getText());
        
        Node textMessage = document.selectSingleNode("//ems-alert/event-data/event-def/messages/message/text-message");
        assertEquals("SomeMessage", textMessage.getText());
        
        // Write and clean this up.
        alert.write();
        
        File alertFile = new File(AlertUtils.getSpoolDir(), alert.getFileName());
        assertNotNull(alertFile);
        
        // Clean everything up
        AlertFile af = new AlertFile(alertFile);
        assertTrue(af.getXmlContents().length() > 0);
        
        af.complete(true);
        
        // See if this went to the success directory
        File successFile = new File(AlertUtils.getProcessedDir(), alert.getFileName());
        assertTrue(successFile.exists());
        
        // Clean up the file
        if (successFile.exists()) {
            assertTrue(successFile.delete());
        }
    }

    public void testDeliverAlert() throws IOException, FileCreationException, FileReadException {
        // Build a new Priming Alert
        Alert primeAlert = new Alert.AlertBuilder("SomeEvent", "TEST-SYSTEM").
            primerFlag(false).priority(Alert.PRIORITY_INFORMATIONAL).
            duplicateValue("deliverAlert").build();
        
        // Write the priming alert to file.
        primeAlert.setInTransFlag(false);
        primeAlert.write();

        // Build a new Alert
        Alert alert = new Alert.AlertBuilder("SomeEvent", "TEST-SYSTEM").
            primerFlag(false).priority(Alert.PRIORITY_INFORMATIONAL).
            duplicateValue("deliverAlert").build();
        
        // Write the alert to file.
        alert.setInTransFlag(false);
        alert.write();
        
        try {
            // Simulate successful priming by moving the priming file to the
            // processed directory.
            AlertReader reader = new AlertReader(AlertUtils.getSpoolDir() +
                "/" + primeAlert.getFileName());
            assertNotNull(reader);
            AlertFile[] files = reader.getAlertFiles();
            assertNotNull(files);
            assertTrue("Expected 1 file, got " + files.length + " files",
                files.length == 1);
            files[0].complete(true);

            // Clean up the prime file
            File primeFile = new File(AlertUtils.getProcessedDir(), primeAlert.getFileName());
            assertTrue(primeFile.exists());
            assertTrue(primeFile.delete());

            // Simulate successful alert processing by moving the alert XML file
            // to the processed directory.
            reader = new AlertReader(AlertUtils.getSpoolDir() +
                "/" + alert.getFileName());
            assertNotNull(reader);
            files = reader.getAlertFiles();
            assertNotNull(files);
            assertTrue("Expected 1 file, got " + files.length + " files",
                files.length == 1);
            files[0].complete(true);

            // Clean up the alert file
            File alertFile = new File(AlertUtils.getProcessedDir(), alert.getFileName());
            assertTrue(alertFile.exists());
            assertTrue(alertFile.delete());
        }
        catch (MocaException e) {
                assertTrue("Moca exception occurred while transporting alert to EMS host." + e, false);
        }
    }
}