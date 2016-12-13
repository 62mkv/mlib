/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.server.repository.docs;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.redprairie.moca.server.repository.ArgumentInfo;
import com.redprairie.moca.server.repository.Command;
import com.redprairie.moca.server.repository.CommandRepository;
import com.redprairie.moca.server.repository.ComponentLevel;
import com.redprairie.moca.server.repository.LocalSyntaxCommand;
import com.redprairie.moca.server.repository.MocaCommandRepository;
import com.redprairie.moca.server.repository.Trigger;

/**
 * TODO Class Description
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class DocWriter {
    
    public DocWriter(File xslDir, File outputDir, CommandRepository repository) throws CommandDocumentationException {
        _outputDir = outputDir;
        
        
        TransformerFactory fact = TransformerFactory.newInstance();
        try {
            _indexSheet = fact.newTransformer(new StreamSource(new File(xslDir, "index.xsl")));
            _levelSheet = fact.newTransformer(new StreamSource(new File(xslDir, "component-level.xsl")));
            _commandSheet = fact.newTransformer(new StreamSource(new File(xslDir, "command.xsl")));
            _triggerSheet = fact.newTransformer(new StreamSource(new File(xslDir, "trigger.xsl")));
        }
        catch (TransformerConfigurationException e) {
            throw new CommandDocumentationException(e);
        }
        
        _repository = repository;
        _commandsByLevel = new LinkedHashMap<ComponentLevel, List<Command>>();

        List<Command> allCommands = new ArrayList<Command>(_repository.getAllCommands());
        
        Collections.sort(allCommands, new Comparator<Command>() {        
            @Override
            public int compare(Command o1, Command o2) {
                return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
            }
        });
        
        for (Command cmd : allCommands) {
            List<Command> levelCommands = _commandsByLevel.get(cmd.getLevel());
            if (levelCommands == null) {
                levelCommands = new ArrayList<Command>();
                _commandsByLevel.put(cmd.getLevel(), levelCommands);
            }
            levelCommands.add(cmd);
        }
    }
    
    public void createIndexPage() throws CommandDocumentationException {
        StringWriter indexData = new StringWriter();
        try {
            XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(indexData);
            out.writeStartDocument();
            out.writeStartElement("documentation");
            for (ComponentLevel level : _repository.getLevels()) {
                out.writeStartElement("component-level");
                out.writeStartElement("name");
                out.writeCharacters(level.getName());
                out.writeEndElement();

                String description = level.getDescription();
                if (description != null) {
                    out.writeStartElement("description");
                    out.writeCharacters(description);
                    out.writeEndElement();
                }

                String uri = getLevelPath(null, level);

                out.writeStartElement("uri");
                out.writeCharacters(uri);
                out.writeEndElement();

                out.writeEndElement();
                
            }
            out.writeEndElement();

            out.writeEndDocument();
            
            out.close();
            
            String xml = indexData.toString();
            
            File indexFile = new File(_outputDir, "index.html");
            if (!indexFile.getParentFile().exists()) {
                if (!indexFile.getParentFile().mkdirs()) {
                    throw new CommandDocumentationException(
                            "Unable to create command doc directories for file " + 
                            indexFile.getAbsolutePath());
                }
            }

            _indexSheet.transform(new StreamSource(new StringReader(xml)), new StreamResult(indexFile));
            _indexSheet.reset();
        }
        catch (XMLStreamException e) {
            throw new CommandDocumentationException("Unable to write index file ", e);
        }
        catch (TransformerException e) {
            throw new CommandDocumentationException("Unable to write index file ", e);
        }
    }

    public void createComponentLevelPages() throws CommandDocumentationException {
        for (ComponentLevel level : _repository.getLevels()) {
            StringWriter levelData = new StringWriter();
            try {
                XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(levelData);
                out.writeStartDocument();
                out.writeStartElement("documentation");
                out.writeStartElement("name");
                out.writeCharacters(level.getName());
                out.writeEndElement();

                String description = level.getDescription();
                if (description != null) {
                    out.writeStartElement("description");
                    out.writeCharacters(description);
                    out.writeEndElement();
                }
                
                List<Command> levelCommands = _commandsByLevel.get(level);
                if (levelCommands != null) {
                    for (Command cmd : _commandsByLevel.get(level)) {
    
                        out.writeStartElement("command");
                        out.writeStartElement("name");
                        out.writeCharacters(cmd.getName());
                        out.writeEndElement();
                        
                        
                        out.writeStartElement("uri");
                        out.writeCharacters(getCommandPath("../..", cmd));
                        out.writeEndElement();
    
                        out.writeEndElement();
                    }
                }
                out.writeEndElement();

                out.writeEndDocument();
                
                out.close();
                
                String xml = levelData.toString();
                
                File levelHtmlFile = getLevelHtmlFile(level);
                if (!levelHtmlFile.getParentFile().exists()) {
                    if (!levelHtmlFile.getParentFile().mkdirs()) {
                        throw new CommandDocumentationException(
                                "Unable to create command doc directories for file " + 
                                levelHtmlFile.getAbsolutePath());
                    }
                }
                
                _levelSheet.transform(new StreamSource(new StringReader(xml)), new StreamResult(levelHtmlFile));
                _levelSheet.reset();
            }
            catch (XMLStreamException e) {
                throw new CommandDocumentationException("Unable to write index file ", e);
            }
            catch (TransformerException e) {
                throw new CommandDocumentationException("Unable to write index file ", e);
            }
            
        }
    }
    
    public void createCommandPages() throws CommandDocumentationException {
        List<Command> allCommands = new ArrayList<Command>(_repository.getAllCommands());
        
        for (Command command : allCommands) {
            StringWriter commandData = new StringWriter();
            try {
                XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(commandData);
                out.writeStartDocument();
                out.writeStartElement("documentation");

                // Certain elements are part of the command definition
                out.writeStartElement("name");
                out.writeCharacters(command.getName());
                out.writeEndElement();
                
                out.writeStartElement("description");
                out.writeCharacters(command.getDescription());
                out.writeEndElement();
                
                ComponentLevel level = command.getLevel();
                out.writeStartElement("component-level");
                out.writeStartElement("name");
                out.writeCharacters(level.getName());
                out.writeEndElement();
                out.writeStartElement("uri");
                out.writeCharacters(getLevelPath("../..", level));
                out.writeEndElement();
                out.writeEndElement();
                
                // Determine two more pieces of information:
                // 1. The command that overrides this command
                List<Command> thisCommand = _repository.getCommandByName(MocaCommandRepository.normalizeName(command.getName()));
                Command override = null;
                for (Command c : thisCommand) {
                    if (c.equals(command)) {
                        break;
                    }
                    else {
                        override = c;
                    }
                }
                
                if (override != null) {
                    writeCommandReference(out, "overridden-by", override);
                }
                
                // 2. Any triggers that might exist on this command
                List<Trigger> triggers = _repository.getTriggerByCommandName(MocaCommandRepository.normalizeName(command.getName()));
                if (triggers != null && triggers.size() != 0) {
                    for (Trigger trig : triggers) {
                        writeTriggerReference(out, trig);
                    }
                }
                
                switch (command.getTransactionType()) {
                case REQUIRES_NEW:
                    out.writeStartElement("transaction");
                    out.writeCharacters("Requires New");
                    out.writeEndElement();
                    break;
                default:
                    break;
                }
                
                // The command documentation part holds some of the rest.
                CommandDocumentation doc = command.getDocumentation();
                
                if (doc != null) {
                    out.writeStartElement("remarks");
                    out.writeCharacters(doc.getRemarks());
                    out.writeEndElement();
                    
                    List<String> examples = doc.getExamples();
                    if (examples != null) {
                        for (String example : examples) {
                            out.writeStartElement("example");
                            out.writeCharacters(example);
                            out.writeEndElement();
                        }
                    }
                    
                    List<CommandDocumentation.Reference> calledByList = doc.getCalledBy();
                    if (calledByList != null) {
                        for (CommandDocumentation.Reference calledBy : calledByList) {
                            writeReference(out, calledBy, "called-by");
                        }
                    }
                    
                    List<CommandDocumentation.Reference> seeAlsoList = doc.getSeeAlso();
                    if (calledByList != null) {
                        for (CommandDocumentation.Reference seeAlso : seeAlsoList) {
                            writeReference(out, seeAlso, "seealso");
                        }
                    }
                    
                    out.writeStartElement("retrows");
                    out.writeCharacters(doc.getReturnRows());
                    out.writeEndElement();
                    
                    List <CommandDocumentation.Column> cols = doc.getColumns();
                    if (cols != null) {
                        for (CommandDocumentation.Column col : cols) {
                            out.writeStartElement("retcol");
                            out.writeAttribute("name", col.getName());
                            out.writeAttribute("type", col.getType());
                            out.writeCharacters(col.getDescription());
                            out.writeEndElement();
                        }
                    }
                    
                    List<CommandDocumentation.Err> errs = doc.getErrors();
                    if (errs != null) {
                        for (CommandDocumentation.Err err : errs) {
                            out.writeStartElement("exception");
                            out.writeAttribute("value", err.getValue());
                            out.writeCharacters(err.getDescription());
                            out.writeEndElement();
                        }
                    }
                }
                
                // Arguments
                List<ArgumentInfo> args = command.getArguments();
                writeArguments(out, args);
                
                // Local Syntax -- include if there is any
                if (command instanceof LocalSyntaxCommand) {
                    String syntax = ((LocalSyntaxCommand) command).getSyntax();
                    out.writeStartElement("local-syntax");
                    out.writeCharacters(syntax);
                    out.writeEndElement();
                }
                
                // Check for Overriding Command
                Command lastCommand = null;
                for (Command namedCommand : _repository.getCommandByName(MocaCommandRepository.normalizeName(command.getName()))) {
                    if (namedCommand.equals(command) && lastCommand != null) {
                        writeCommandReference(out, "overridden-by", lastCommand);
                        break;
                    }
                    lastCommand = namedCommand;
                }
                
                out.writeEndElement();

                out.writeEndDocument();
                
                out.close();
                
                String xml = commandData.toString();
                
                File commandHtmlFile = getCommandHtmlFile(command);
                if (!commandHtmlFile.getParentFile().exists()) {
                    if (!commandHtmlFile.getParentFile().mkdirs()) {
                        throw new CommandDocumentationException(
                                "Unable to create command doc directories for file " + 
                                commandHtmlFile.getAbsolutePath());
                    }
                }
                
                _commandSheet.transform(new StreamSource(new StringReader(xml)), new StreamResult(commandHtmlFile));
                _commandSheet.reset();
            }
            catch (XMLStreamException e) {
                throw new CommandDocumentationException("Unable to write command doc file ", e);
            }
            catch (TransformerException e) {
                throw new CommandDocumentationException("Unable to write command doc file ", e);
            }
        }
    }

    
    public void createTriggerPages() throws CommandDocumentationException {
        List<Trigger>  allTriggers = new ArrayList<Trigger>(_repository.getAllTriggers());
        
        for (Trigger trigger : allTriggers) {
            StringWriter triggerData = new StringWriter();
            try {
                XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(triggerData);
                out.writeStartDocument();
                out.writeStartElement("documentation");
                
                out.writeStartElement("name");
                out.writeCharacters(trigger.getName());
                out.writeEndElement();
                
                out.writeStartElement("sequence");
                out.writeCharacters(String.valueOf(trigger.getFireSequence()));
                out.writeEndElement();
                
                boolean disabled = trigger.isDisabled();
                
                out.writeStartElement("enabled");
                out.writeCharacters(disabled ? "no" : "yes");
                out.writeEndElement();
                
                if (!disabled) {
                    out.writeStartElement("action");
                    out.writeCData(trigger.getSyntax());
                    out.writeEndElement();
                }

                writeArguments(out, trigger.getArguments());

                List<Command> allCommands = _repository.getCommandByName(trigger.getCommand());
                
                if (allCommands != null) {
                    Command command = allCommands.get(0);
                    out.writeStartElement("command");
                    out.writeStartElement("component-level");
                    out.writeCharacters(command.getLevel().getName());
                    out.writeEndElement();
                    out.writeStartElement("name");
                    out.writeCharacters(command.getName());
                    out.writeEndElement();
                    out.writeStartElement("uri");
                    out.writeCharacters(getCommandPath("..", command));
                    out.writeEndElement();
                    out.writeEndElement();
                }
                
                out.writeEndElement();
                
                out.writeEndDocument();
                
                out.close();
                
                String xml = triggerData.toString();
                
                File triggerHtmlFile = getTriggerHtmlFile(trigger);
                if (!triggerHtmlFile.getParentFile().exists()) {
                    if (!triggerHtmlFile.getParentFile().mkdirs()) {
                        throw new CommandDocumentationException(
                                "Unable to create command doc directories for file " + 
                                triggerHtmlFile.getAbsolutePath());
                    }
                }
                
                _triggerSheet.transform(new StreamSource(new StringReader(xml)), new StreamResult(triggerHtmlFile));
                _triggerSheet.reset();
            }
            catch (XMLStreamException e) {
                throw new CommandDocumentationException("Unable to write command doc file ", e);
            }
            catch (TransformerException e) {
                throw new CommandDocumentationException("Unable to write command doc file ", e);
            }
 
        }
    }
        
    private void writeReference(XMLStreamWriter out, CommandDocumentation.Reference ref, String tag)
            throws XMLStreamException {
        String name = ref.getCommand();
        if (name == null) return;

        List<Command> matchingCommands = _repository.getCommandByName(name);
        if (matchingCommands != null) {
            for (Command command : matchingCommands) {
                writeCommandReference(out, tag, command);
            }
        }
        else {
            out.writeStartElement(tag);
            out.writeStartElement("command");
            out.writeCharacters(name);
            out.writeEndElement();
            out.writeEndElement();
        }
    }

    /**
     * @param out
     * @param tag
     * @param command
     * @throws XMLStreamException
     */
    private void writeCommandReference(XMLStreamWriter out, String tag, Command command) throws XMLStreamException {
        out.writeStartElement(tag);
        out.writeStartElement("component-level");
        out.writeCharacters(command.getLevel().getName());
        out.writeEndElement();
        out.writeStartElement("command");
        out.writeCharacters(command.getName());
        out.writeEndElement();
        out.writeStartElement("uri");
        out.writeCharacters(getCommandPath("../..", command));
        out.writeEndElement();
        out.writeEndElement();
    }
    
    /**
     * @param out
     * @param tag
     * @param command
     * @throws XMLStreamException
     */
    private void writeTriggerReference(XMLStreamWriter out, Trigger trigger) throws XMLStreamException {
        out.writeStartElement("trigger");
        out.writeStartElement("name");
        out.writeCharacters(trigger.getName());
        out.writeEndElement();
        out.writeStartElement("uri");
        out.writeCharacters(getTriggerPath("../..", trigger));
        out.writeEndElement();
        out.writeEndElement();
    }
    
    private void writeArguments(XMLStreamWriter out, List<ArgumentInfo> args) throws XMLStreamException {
        if (args != null) {
            for (ArgumentInfo arg : args) {
                out.writeStartElement("argument");
                out.writeAttribute("name", arg.getName());
                if (arg.getAlias() != null) {
                    out.writeAttribute("alias", arg.getAlias());
                }
                
                if (arg.getDatatype() != null) {
                    out.writeAttribute("type", arg.getDatatype().toString());
                }
                
                out.writeAttribute("required", arg.isRequired() ? "yes" : "no");
                if (arg.getDefaultValue() != null) {
                    out.writeAttribute("default", arg.getDefaultValue());
                }
                
                if (arg.getComment() != null) {
                    out.writeCharacters(arg.getComment());
                }
                out.writeEndElement();
            }
        }
    }

    private String getLevelPath(String prefix, ComponentLevel level) {
        StringBuilder path = new StringBuilder();
        if (prefix != null) {
            path.append(prefix);
            path.append('/');
        }
        
        String levelName = level.getName().trim().toLowerCase();
        path.append("commands/").append(levelName).append('/').append(levelName).append("-lvl.html");
        return path.toString();
    }

    private String getCommandPath(String prefix, Command command) {
        StringBuilder path = new StringBuilder();
        if (prefix != null) {
            path.append(prefix);
            path.append('/');
        }
        
        String levelName = command.getLevel().getName().trim().toLowerCase();
        String commandName = MocaCommandRepository.normalizeName(command.getName()).replace(' ', '_');

        path.append("commands/").append(levelName).append('/').append(commandName).append(".html");
        return path.toString();
    }
    
    private String getTriggerPath(String prefix, Trigger trigger) {
        StringBuilder path = new StringBuilder();
        if (prefix != null) {
            path.append(prefix);
            path.append('/');
        }
        
        String name = MocaCommandRepository.normalizeName(trigger.getName()).replace(' ', '_');
        String commandName = MocaCommandRepository.normalizeName(trigger.getCommand()).replace(' ', '_');
        
        path.append("commands/").append(commandName).append('-').append(name).append("-trg.html");
        return path.toString();
    }
    
    private File getCommandHtmlFile(Command command) {
        String levelName = command.getLevel().getName().trim().toLowerCase();
        String commandName = MocaCommandRepository.normalizeName(command.getName()).replace(' ', '_');
        return new File(new File(new File(_outputDir, "commands"), levelName), commandName + ".html");
    }
    
    private File getLevelHtmlFile(ComponentLevel level) {
        String levelName = level.getName().trim().toLowerCase();
        return new File(new File(new File(_outputDir, "commands"), levelName), levelName + "-lvl.html");
    }

    private File getTriggerHtmlFile(Trigger trigger) {
        String name = MocaCommandRepository.normalizeName(trigger.getName()).replace(' ', '_');
        String commandName = MocaCommandRepository.normalizeName(trigger.getCommand()).replace(' ', '_');
        
        return new File(new File(_outputDir, "commands"), commandName + "-" + name + "-trg.html");
    }
    
    private final Transformer _indexSheet;
    private final Transformer _levelSheet;
    private final Transformer _commandSheet;
    private final Transformer _triggerSheet;
    private final CommandRepository _repository;
    private final Map<ComponentLevel, List<Command>> _commandsByLevel;
    private final File _outputDir;
}
