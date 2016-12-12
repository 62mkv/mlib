/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.repository.docs;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import com.redprairie.moca.server.repository.Command;
import com.redprairie.moca.server.repository.CommandRepository;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.TransactionType;
import com.redprairie.moca.server.repository.Trigger;

/**
 * This test unit is to test out the <code>DocWriter</code> functionality.
 * Because of the inherited behavior of the <code>DocWriter</code>, the only reasonable test
 * is to verify the calls on the <code>XMLStreamWriter</code>.
 * 
 * Copyright (c) 2010 RedPrairie Corporation All Rights Reserved
 * 
 * @author klehrke
 * @see XMLStreamWriter
 * @see DocWriter
 */
public class TU_DocWriter {

    @BeforeClass
    public static void setUp() {
        // General Setup
        String clazz = MyTransformerFactoryImpl.class.getName();
        System.setProperty("javax.xml.transform.TransformerFactory", clazz);

        clazz = MyXMLOutputFactoryImpl.class.getName();
        System.setProperty("javax.xml.stream.XMLOutputFactory", clazz);
    }

    /***
     * Unit Test to test writing the command pages.
     * 
     * @throws CommandDocumentationException
     * @throws XMLStreamException
     */
    @Test
    public void testDocWriterCommandPages()
            throws CommandDocumentationException, XMLStreamException {

        CommandRepository repos = Mockito.mock(CommandRepository.class);
        MyXMLOutputFactoryImpl.out = Mockito.mock(XMLStreamWriter.class);
        // Create mocked commands
        Command command1 = Mockito.mock(Command.class);
        Mockito.when(command1.getName()).thenReturn("TestCommand1");

        Command command2 = Mockito.mock(Command.class);
        Mockito.when(command2.getName()).thenReturn("TestCommand2");

        Mockito.when(command1.getTransactionType()).thenReturn(
            TransactionType.REQUIRES_NEW);
        Mockito.when(command2.getTransactionType()).thenReturn(
            TransactionType.NONE);

        // Setup mocked ComponentLevel
        ComponentLevel lvl = Mockito.mock(ComponentLevel.class);
        Mockito.when(lvl.getDescription()).thenReturn("Description");
        Mockito.when(lvl.getName()).thenReturn("TestLevel");

        Mockito.when(command1.getLevel()).thenReturn(lvl);
        Mockito.when(command2.getLevel()).thenReturn(lvl);

        // Create the list off commands
        List<Command> commandList1 = new ArrayList<Command>();
        commandList1.add(command1);

        Mockito.when(repos.getCommandByName("TestCommand1")).thenReturn(
            commandList1);

        commandList1 = new ArrayList<Command>();
        commandList1.add(command2);
        Mockito.when(repos.getCommandByName("TestCommand2")).thenReturn(
            commandList1);

        List<Command> listCommands = new ArrayList<Command>();
        listCommands.add(command1);
        listCommands.add(command2);

        List<Trigger> listTriggers = new ArrayList<Trigger>();

        Trigger trig2 = Mockito.mock(Trigger.class);

        Mockito.when(trig2.getName()).thenReturn("TestTrigger2");
        Mockito.when(trig2.getFireSequence()).thenReturn(6500);
        Mockito.when(trig2.isDisabled()).thenReturn(false);
        Mockito.when(trig2.getCommand()).thenReturn("TestCommand2");
        Mockito.when(trig2.getSyntax()).thenReturn("publish something");

        listTriggers.add(trig2);

        Mockito.when(repos.getAllTriggers()).thenReturn(listTriggers);

        Mockito.when(repos.getAllCommands()).thenReturn(listCommands);
        
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        DocWriter docwriter = new DocWriter(
                new File(tmpDir, "docs/xsl"),
                new File(tmpDir, "temp/Doc"), repos);
        docwriter.createCommandPages();
        XMLStreamWriter out = MyXMLOutputFactoryImpl.out;

        InOrder inOrder = Mockito.inOrder(out);
        // Command 1
        {
            inOrder.verify(out).writeStartDocument();
            inOrder.verify(out).writeStartElement("documentation");

            // Certain elements are part of the command definition
            inOrder.verify(out).writeStartElement("name");
            inOrder.verify(out).writeCharacters(command1.getName());
            inOrder.verify(out).writeEndElement();

            inOrder.verify(out).writeStartElement("description");
            inOrder.verify(out).writeCharacters(command1.getDescription());
            inOrder.verify(out).writeEndElement();

            ComponentLevel level = command1.getLevel();
            inOrder.verify(out).writeStartElement("component-level");
            inOrder.verify(out).writeStartElement("name");
            inOrder.verify(out).writeCharacters(level.getName());
            inOrder.verify(out).writeEndElement();
            inOrder.verify(out).writeStartElement("uri");
            inOrder.verify(out).writeCharacters(Mockito.anyString());
            inOrder.verify(out, Mockito.times(2)).writeEndElement();

            inOrder.verify(out).writeStartElement("transaction");
            inOrder.verify(out).writeCharacters("Requires New");
            inOrder.verify(out, Mockito.times(2)).writeEndElement();
        }

        // Command 2 with trigger
        {
            inOrder.verify(out).writeStartDocument();
            inOrder.verify(out).writeStartElement("documentation");

            // Certain elements are part of the command definition
            inOrder.verify(out).writeStartElement("name");
            inOrder.verify(out).writeCharacters(command2.getName());
            inOrder.verify(out).writeEndElement();

            inOrder.verify(out).writeStartElement("description");
            inOrder.verify(out).writeCharacters(command2.getDescription());
            inOrder.verify(out).writeEndElement();

            ComponentLevel level = command2.getLevel();
            inOrder.verify(out).writeStartElement("component-level");
            inOrder.verify(out).writeStartElement("name");
            inOrder.verify(out).writeCharacters(level.getName());
            inOrder.verify(out).writeEndElement();

            List<Trigger> t = repos.getAllTriggers();
            out.writeStartElement("trigger");
            out.writeStartElement("name");
            out.writeCharacters(t.get(0).getName());
            out.writeEndElement();
            out.writeStartElement("uri");
            out.writeCharacters(Mockito.anyString());
            out.writeEndElement();
            out.writeEndElement();

            inOrder.verify(out).writeStartElement("uri");
            inOrder.verify(out).writeCharacters(Mockito.anyString());
            inOrder.verify(out, Mockito.times(3)).writeEndElement();
        }

        inOrder.verify(out).writeEndDocument();

        inOrder.verify(out).close();
    }

    /***
     * 
     * Unit Test to test creating trigger pages.
     * 
     * @throws CommandDocumentationException
     * @throws XMLStreamException
     */
    @Test
    public void testDocWriterCreateTriggerPages()
            throws CommandDocumentationException, XMLStreamException {

        // Setup stuff
        CommandRepository repos = Mockito.mock(CommandRepository.class);
        MyXMLOutputFactoryImpl.out = Mockito.mock(XMLStreamWriter.class);
        
        ComponentLevel lvl = Mockito.mock(ComponentLevel.class);
        Mockito.when(lvl.getDescription()).thenReturn("Description");
        Mockito.when(lvl.getName()).thenReturn("TestLevel");

        List<Command> listCommands = new ArrayList<Command>();

        Command command1 = Mockito.mock(Command.class);
        Mockito.when(command1.getName()).thenReturn("TestCommand1");
        
        List<Trigger> listTriggers = new ArrayList<Trigger>();
        
        //Trigger 1
        Trigger trig1 = Mockito.mock(Trigger.class);
        Mockito.when(trig1.getName()).thenReturn("TestTrigger1");
        Mockito.when(trig1.getFireSequence()).thenReturn(9000);
        Mockito.when(trig1.isDisabled()).thenReturn(true);
        Mockito.when(trig1.getCommand()).thenReturn("TestCommand1");
        Mockito.when(trig1.getSyntax()).thenReturn("");

        //Trigger 2
        Trigger trig2 = Mockito.mock(Trigger.class);
        Mockito.when(trig2.getName()).thenReturn("TestTrigger2");
        Mockito.when(trig2.getFireSequence()).thenReturn(6500);
        Mockito.when(trig2.isDisabled()).thenReturn(false);
        Mockito.when(trig2.getCommand()).thenReturn("TestCommand1");
        Mockito.when(trig2.getSyntax()).thenReturn("publish something");

        listTriggers.add(trig1);
        listTriggers.add(trig2);

        Mockito.when(repos.getAllTriggers()).thenReturn(listTriggers);

        List<Command> commandList1 = new ArrayList<Command>();
        commandList1.add(command1);

        Mockito.when(repos.getCommandByName("TestCommand1")).thenReturn(
            commandList1);

        Mockito.when(command1.getLevel()).thenReturn(lvl);

        listCommands.add(command1);

        // Actual call
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        DocWriter docwriter = new DocWriter(
                new File(tmpDir, "docs/xsl"),
                new File(tmpDir, "temp/Doc"), repos);
        docwriter.createTriggerPages();

        XMLStreamWriter out = MyXMLOutputFactoryImpl.out;

        // Verification
        InOrder inOrder = Mockito.inOrder(out);

        // Trigger 1
        {
            inOrder.verify(out).writeStartDocument();
            inOrder.verify(out).writeStartElement("documentation");

            inOrder.verify(out).writeStartElement("name");
            inOrder.verify(out).writeCharacters("TestTrigger1");
            inOrder.verify(out).writeEndElement();
            inOrder.verify(out).writeStartElement("sequence");
            inOrder.verify(out).writeCharacters(String.valueOf(9000));
            inOrder.verify(out).writeEndElement();

            inOrder.verify(out).writeStartElement("enabled");
            inOrder.verify(out).writeCharacters("no");
            inOrder.verify(out).writeEndElement();

            inOrder.verify(out).writeStartElement("command");
            inOrder.verify(out).writeStartElement("component-level");
            inOrder.verify(out).writeCharacters("TestLevel");
            inOrder.verify(out).writeEndElement();
            inOrder.verify(out).writeStartElement("name");
            inOrder.verify(out).writeCharacters("TestCommand1");
            inOrder.verify(out).writeEndElement();
            inOrder.verify(out).writeStartElement("uri");
            // TODO: we can verify this better later
            inOrder.verify(out).writeCharacters(Mockito.anyString());
            inOrder.verify(out, Mockito.times(3)).writeEndElement();

            inOrder.verify(out).writeEndDocument();

            inOrder.verify(out).close();
        }

        // Trigger 2
        {
            inOrder.verify(out).writeStartDocument();
            inOrder.verify(out).writeStartElement("documentation");

            inOrder.verify(out).writeStartElement("name");
            inOrder.verify(out).writeCharacters("TestTrigger2");
            inOrder.verify(out).writeEndElement();
            inOrder.verify(out).writeStartElement("sequence");
            inOrder.verify(out).writeCharacters(String.valueOf(6500));
            inOrder.verify(out).writeEndElement();

            inOrder.verify(out).writeStartElement("enabled");
            inOrder.verify(out).writeCharacters("yes");
            inOrder.verify(out).writeEndElement();

            inOrder.verify(out).writeStartElement("action");
            inOrder.verify(out).writeCData("publish something");
            inOrder.verify(out).writeEndElement();

            inOrder.verify(out).writeStartElement("command");
            inOrder.verify(out).writeStartElement("component-level");
            inOrder.verify(out).writeCharacters("TestLevel");
            inOrder.verify(out).writeEndElement();
            inOrder.verify(out).writeStartElement("name");
            inOrder.verify(out).writeCharacters("TestCommand1");
            inOrder.verify(out).writeEndElement();
            inOrder.verify(out).writeStartElement("uri");
            // TODO: we can verify this better later
            inOrder.verify(out).writeCharacters(Mockito.anyString());
            inOrder.verify(out, Mockito.times(3)).writeEndElement();

            inOrder.verify(out).writeEndDocument();

            inOrder.verify(out).close();
        }

    }

    /***
     * 
     * Unit test to test writing the index page.
     * 
     * @throws CommandDocumentationException
     * @throws XMLStreamException
     */
    @Test
    public void testDocWriterIndexPage() throws CommandDocumentationException,
            XMLStreamException {
        CommandRepository repos = Mockito.mock(CommandRepository.class);
        
        MyXMLOutputFactoryImpl.out = Mockito.mock(XMLStreamWriter.class);
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        DocWriter docwriter = new DocWriter(
                new File(tmpDir, "docs/xsl"),
                new File(tmpDir, "temp/Doc"), repos);
        docwriter.createIndexPage();

        XMLStreamWriter out = MyXMLOutputFactoryImpl.out;
        InOrder inOrder = Mockito.inOrder(out);

        inOrder.verify(out).writeStartDocument();
        inOrder.verify(out).writeStartElement("documentation");
        for (int i = 0; i < repos.getLevels().size(); ++i) {
            ComponentLevel level = repos.getLevels().get(i);
            inOrder.verify(out).writeStartElement("component-level");
            inOrder.verify(out).writeStartElement("name");
            inOrder.verify(out).writeCharacters(level.getName());
            inOrder.verify(out).writeEndElement();

            String description = level.getDescription();
            if (description != null) {
                inOrder.verify(out).writeStartElement("description");
                inOrder.verify(out).writeCharacters(description);
                inOrder.verify(out).writeEndElement();
            }

            inOrder.verify(out).writeStartElement("uri");
            inOrder.verify(out).writeCharacters(Mockito.anyString());

            inOrder.verify(out,
                Mockito.times(i + 1 == repos.getLevels().size() ? 3 : 2))
                .writeEndElement();

        }

        inOrder.verify(out).writeEndDocument();

        inOrder.verify(out).close();

    }

    /***
     * 
     * Unit Test to test writing out component level pages
     * 
     * @throws CommandDocumentationException
     * @throws XMLStreamException
     */
    @Test
    public void testDocWriterComponentLevelPages()
            throws CommandDocumentationException, XMLStreamException {
        
        //Setup
        CommandRepository repos = Mockito.mock(CommandRepository.class);

        MyXMLOutputFactoryImpl.out = Mockito.mock(XMLStreamWriter.class);
        
        ComponentLevel lvl = Mockito.mock(ComponentLevel.class);
        Mockito.when(lvl.getDescription()).thenReturn("Description");
        Mockito.when(lvl.getName()).thenReturn("TestLevel");

        ComponentLevel lvl2 = Mockito.mock(ComponentLevel.class);
        Mockito.when(lvl2.getDescription()).thenReturn("Description");
        Mockito.when(lvl2.getName()).thenReturn("TestLevel2");

        List<ComponentLevel> listLevels = new ArrayList<ComponentLevel>();

        listLevels.add(lvl);
        listLevels.add(lvl2);

        Mockito.when(repos.getLevels()).thenReturn(listLevels);

        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        DocWriter docwriter = new DocWriter(
                new File(tmpDir, "docs/xsl"),
                new File(tmpDir, "temp/Doc"), repos);
        docwriter.createComponentLevelPages();
        
        XMLStreamWriter localOut = MyXMLOutputFactoryImpl.out;
        
        //Inorder check
        InOrder inOrder = Mockito.inOrder(localOut);

        inOrder.verify(localOut).writeStartDocument();
        inOrder.verify(localOut).writeStartElement("documentation");
        inOrder.verify(localOut).writeStartElement("name");
        inOrder.verify(localOut).writeCharacters(lvl.getName());
        inOrder.verify(localOut).writeEndElement();
        inOrder.verify(localOut).writeStartElement("description");
        inOrder.verify(localOut).writeCharacters(lvl.getDescription());
        inOrder.verify(localOut, Mockito.times(2)).writeEndElement();
        inOrder.verify(localOut).writeEndDocument();
        inOrder.verify(localOut).close();

    }

    /***
     * 
     * Test writing out component level pages
     * 
     * @throws CommandDocumentationException
     * @throws XMLStreamException
     */
    @AfterClass
    public static void tearDown() {
        System.setProperty("javax.xml.transform.TransformerFactory", "");
        System.setProperty("javax.xml.stream.XMLOutputFactory", "");
        removeDirs(new File(System.getProperty("java.io.tmpdir"), "temp"));
    }

    /***
     * Helper to clean up the directories created by the tests
     * 
     * @param dir
     */
    private static void removeDirs(String dir) {
        removeDirs(new File(dir));
    }
    
    private static void removeDirs(File rootDir) {
        File[] sub = rootDir.listFiles();
        for (File f : sub) {
            if (f.list().length != 0) {
                removeDirs(f.getAbsolutePath());
            }
            f.delete();
        }
        rootDir.delete();
    }
    
    public static class MyXMLOutputFactoryImpl extends XMLOutputFactory{
        
        private static  XMLStreamWriter out;
        
        // @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(java.io.Writer)
        @Override
        public XMLStreamWriter createXMLStreamWriter(Writer stream)
                throws XMLStreamException {
            return out;
        }
        // @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(java.io.OutputStream)
        @Override
        public XMLStreamWriter createXMLStreamWriter(OutputStream stream)
                throws XMLStreamException {
            return null;
        }
        // @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(java.io.OutputStream, java.lang.String)
        @Override
        public XMLStreamWriter createXMLStreamWriter(OutputStream stream,
                                                     String encoding)
                throws XMLStreamException {
            return null;
        }
        // @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(javax.xml.transform.Result)
        @Override
        public XMLStreamWriter createXMLStreamWriter(Result result)
                throws XMLStreamException {
            
            return null;
        }
        // @see javax.xml.stream.XMLOutputFactory#createXMLEventWriter(javax.xml.transform.Result)
        @Override
        public XMLEventWriter createXMLEventWriter(Result result)
                throws XMLStreamException {
           
            return null;
        }
        // @see javax.xml.stream.XMLOutputFactory#createXMLEventWriter(java.io.OutputStream)
        @Override
        public XMLEventWriter createXMLEventWriter(OutputStream stream)
                throws XMLStreamException {
            return null;
        }
        // @see javax.xml.stream.XMLOutputFactory#createXMLEventWriter(java.io.OutputStream, java.lang.String)
        @Override
        public XMLEventWriter createXMLEventWriter(OutputStream stream,
                                                   String encoding)
                throws XMLStreamException {
            return null;
        }
        // @see javax.xml.stream.XMLOutputFactory#createXMLEventWriter(java.io.Writer)
        @Override
        public XMLEventWriter createXMLEventWriter(Writer stream)
                throws XMLStreamException {
            return null;
        }
        // @see javax.xml.stream.XMLOutputFactory#setProperty(java.lang.String, java.lang.Object)
        @Override
        public void setProperty(String name, Object value)
                throws IllegalArgumentException {
            
        }
        // @see javax.xml.stream.XMLOutputFactory#getProperty(java.lang.String)
        @Override
        public Object getProperty(String name) throws IllegalArgumentException {
            return null;
        }
        // @see javax.xml.stream.XMLOutputFactory#isPropertySupported(java.lang.String)
        @Override
        public boolean isPropertySupported(String name) {
            return false;
        }
    }
    
    public static class MyTransformerFactoryImpl extends TransformerFactory{
        // @see javax.xml.transform.TransformerFactory#newTransformer(javax.xml.transform.Source)
        @Override
        public Transformer newTransformer(Source source)
                throws TransformerConfigurationException {
            return Mockito.mock(Transformer.class);
        }
        // @see javax.xml.transform.TransformerFactory#newTransformer()
        @Override
        public Transformer newTransformer()
                throws TransformerConfigurationException {
            return null;
        }
        // @see javax.xml.transform.TransformerFactory#newTemplates(javax.xml.transform.Source)
        @Override
        public Templates newTemplates(Source source)
                throws TransformerConfigurationException {
            return null;
        }
        // @see javax.xml.transform.TransformerFactory#getAssociatedStylesheet(javax.xml.transform.Source, java.lang.String, java.lang.String, java.lang.String)
        @Override
        public Source getAssociatedStylesheet(Source source, String media,
                                              String title, String charset)
                throws TransformerConfigurationException {
            return null;
        }
        // @see javax.xml.transform.TransformerFactory#setURIResolver(javax.xml.transform.URIResolver)
        
        @Override
        public void setURIResolver(URIResolver resolver) {
        }
        // @see javax.xml.transform.TransformerFactory#getURIResolver()
        @Override
        public URIResolver getURIResolver() {
            return null;
        }
        // @see javax.xml.transform.TransformerFactory#setFeature(java.lang.String, boolean)
        @Override
        public void setFeature(String name, boolean value)
                throws TransformerConfigurationException {
            
        }
        // @see javax.xml.transform.TransformerFactory#getFeature(java.lang.String)
        @Override
        public boolean getFeature(String name) {
            return false;
        }
        // @see javax.xml.transform.TransformerFactory#setAttribute(java.lang.String, java.lang.Object)
        
        @Override
        public void setAttribute(String name, Object value) {
        }
        // @see javax.xml.transform.TransformerFactory#getAttribute(java.lang.String)
        @Override
        public Object getAttribute(String name) {
            return null;
        }
        // @see javax.xml.transform.TransformerFactory#setErrorListener(javax.xml.transform.ErrorListener)
        @Override
        public void setErrorListener(ErrorListener listener) {
        }
        // @see javax.xml.transform.TransformerFactory#getErrorListener()
        @Override
        public ErrorListener getErrorListener() {
            return null;
        }
    }

}
