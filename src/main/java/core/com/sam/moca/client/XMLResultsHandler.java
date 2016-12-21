/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 20168
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

package com.sam.moca.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.LinkedList;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sam.moca.EditableResults;
import com.sam.moca.MocaInterruptedException;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.SimpleResults;
import com.sam.moca.util.DateUtils;
import com.sam.util.Base64;

/**
 * SAX Parser Handler for processing XML and returning a result set.  This SAX handler can handle a MOCA result, as
 * well as any XML-encoded MOCA result set.
 * 
 * <b><pre>
 * Copyright (c) 20168 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
class XMLResultsHandler extends DefaultHandler {
    
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes)
            throws SAXException {
        // Check to make sure we haven't been interrupted
        if (Thread.interrupted()) {
            throw new MocaInterruptedException();
        }
        if (name.equals("moca-results")) {
            XMLResultsHandler._ParserStack element = new _ParserStack();
            element.output = new SimpleResults();
            _stack.addFirst(element);
            if (_stack.size() == 1) {
                _finalResults = element.output;
            }
        }
        else if (name.equals("status")) {
            _state = ParserState.STATUS; 
        }
        else if (name.equals("session-id")) {
            _state = ParserState.SESSIONID;
        }
        else if (name.equals("message")) {
            _state = ParserState.MESSAGE;
            _message = new StringBuilder();
        }
        else if (name.equals("column")) {
            String colName = attributes.getValue("name");
            String colType = attributes.getValue("type");
            String length = attributes.getValue("length");
            String nullable = attributes.getValue("nullable");
            _stack.getFirst().output.addColumn(colName, MocaType.lookup(colType.charAt(0)),
                                               Integer.parseInt(length),
                                               Boolean.parseBoolean(nullable));
        }
        else if (name.equals("row")) {
            _stack.getFirst().output.addRow();
            _stack.getFirst().currentColumn = 0;
        }
        else if (name.equals("field")) {
            _state = ParserState.FIELD;
            String isNullAttr = attributes.getValue("null");
            
            int col = _stack.getFirst().currentColumn;
            EditableResults res = _stack.getFirst().output;
            _fieldIsNull = (isNullAttr != null) ? Boolean.valueOf(isNullAttr) : false;
            
            MocaType type = res.getColumnType(col);
            
            // Binary fields require special handling. Binary data (actually any
            // data) can come in as multiple chunks. We spin up a thread to read
            // the other end of a pipe and pass it to a base-64 decoder, which
            // then writes to a byte array. This avoids the need to have the entire
            // binary field as a base-64 string in memory before decoding it.
            if (type == MocaType.BINARY) {
                if (_binaryHandler != null) {
                    throw new SAXException("Already parsing a binary field -- invalid XML");
                }
                _base64Pipe = new PipedWriter();
                _binaryHandler = new _Base64Decoder(_base64Pipe);
                _binaryHandler.start();
            }
            else {
                _fieldData = new StringBuilder();
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String stringData = new String(ch, start, length);

        switch (_state) {
        case STATUS:
            _status = Integer.parseInt(stringData);
            break;
        case MESSAGE:
            _message.append(stringData);
            break;
        case SESSIONID:
            _sessionId = stringData;
            break;
        case FIELD:
            int col = _stack.getFirst().currentColumn;
            EditableResults res = _stack.getFirst().output;
            MocaType type = res.getColumnType(col);
            if (type == MocaType.BINARY) {
                try {
                    _base64Pipe.write(ch, start, length);
                }
                catch (IOException e) {
                    throw new SAXException("Unable to read base64 data", e);
                }
                break;
            }
            else {
                _fieldData.append(ch, start, length);
            }
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        if (name.equals("moca-results")) {
            XMLResultsHandler._ParserStack element = _stack.removeFirst();
            MocaResults parseOutput = element.output;
            
            if (_stack.size() > 0) {
                element = _stack.getFirst();
                element.output.setResultsValue(element.currentColumn, parseOutput);
            }
        }
        else if (name.equals("status")) {
            _state = ParserState.NONE; 
        }
        else if (name.equals("session-id")) {
            _state = ParserState.NONE; 
        }
        else if (name.equals("message")) {
            _state = ParserState.NONE; 
        }
        else if (name.equals("field")) {
            _state = ParserState.NONE;
            
            XMLResultsHandler._ParserStack element = _stack.getFirst();
            EditableResults res = element.output;
            int col = element.currentColumn;
            MocaType type = res.getColumnType(col);
            
            if (type == MocaType.BINARY) {
                if (_binaryHandler != null) {
                    try {
                        try {
                            _base64Pipe.close();
                        }
                        catch (IOException e) {
                            throw new SAXException("Unable to finish base64 handling", e);
                        }
                        byte[] rawData = _binaryHandler.close();
                        
                        if (!_fieldIsNull) {
                            element.output.setBinaryValue(element.currentColumn, rawData);
                        }
                    }
                    finally {
                        _base64Pipe = null;
                        _binaryHandler = null;
                    }
                }
            }
            else {
                if (_fieldData.length() != 0 && !_fieldIsNull) {
                    switch(type)  {
                    case STRING:
                        res.setStringValue(col, _fieldData.toString());
                        break;
                    case INTEGER:
                        res.setIntValue(col, Integer.parseInt(_fieldData.toString()));
                        break;
                    case DOUBLE:
                        res.setDoubleValue(col, Double.parseDouble(_fieldData.toString()));
                        break;
                    case BOOLEAN:
                        res.setBooleanValue(col, _fieldData.charAt(0) == '1');
                        break;
                    case DATETIME:
                        res.setDateValue(col, DateUtils.parseDate(_fieldData.toString()));
                        break;
                    }
                }
            }
            
            _stack.getFirst().currentColumn++;
        }
    }
    
    // @see org.xml.sax.helpers.DefaultHandler#resolveEntity(java.lang.String, java.lang.String)
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException,
            SAXException {
        try {
            if (systemId.endsWith("moca-response.dtd")) {
                InputStream in = getClass().getResourceAsStream("resources/moca-response.dtd");
                return new InputSource(in);
            }
            else if (systemId.endsWith("moca-results.dtd")) {
                InputStream in = getClass().getResourceAsStream("resources/moca-results.dtd");
                return new InputSource(in);
            }
        }
        catch (Exception e) {
            // do nothing
        }
        return null;
    }
    
    public String getMessage() {
        return _message == null ? null : _message.toString();
    }

    public String getSessionId() {
        return _sessionId;
    }

    public int getStatus() {
        return _status;
    }

    public MocaResults getResults() {
        return _finalResults;
    }

    //
    // Implementation
    //
    private static enum ParserState {
        NONE, STATUS, MESSAGE, SESSIONID, FIELD 
    }

    private static class _ParserStack {
        private EditableResults output;
        private int currentColumn;
    }

    // This class is used to make a simple background thread to efficiently read base64 data
    // without allocating a large chunk of data to hold a base64 string.
    private static  class _Base64Decoder extends Thread {
        
        public _Base64Decoder(PipedWriter writer) throws SAXException {
            _binaryOutput = new ByteArrayOutputStream();
            try {
                _reader = new PipedReader(writer);
            }
            catch (IOException e) {
                throw new SAXException(e);
            }
        }
        
        // @see java.lang.Runnable#run()
        @Override
        public void run() {
            try {
                Base64.decode(_reader, _binaryOutput);
            }
            catch (IOException e) {
                _caught = e;
            }
            finally {
                try {
                    _reader.close();
                }
                catch (IOException ignore) {
                    // Ignore
                }
            }
        }
        
        public byte[] close() {
            try {
                // wait for the reading thread to die
                this.join();
                
                // Return the byte array
                return _binaryOutput.toByteArray(); 
            }
            catch (InterruptedException e) {
                // This is called by the server thread so we want to interrupt
                // back at them.
                throw new MocaInterruptedException(e);
            }
        }
        
        private PipedReader _reader;
        private ByteArrayOutputStream _binaryOutput;
        @SuppressWarnings("unused")
        private IOException _caught;
    }
    
    private StringBuilder _message;
    private String _sessionId;
    private int _status;
    private MocaResults _finalResults;

    private LinkedList<XMLResultsHandler._ParserStack> _stack = new LinkedList<XMLResultsHandler._ParserStack>();
    private XMLResultsHandler.ParserState _state = ParserState.NONE;
    private _Base64Decoder _binaryHandler;
    private PipedWriter _base64Pipe;
    private StringBuilder _fieldData;
    private boolean _fieldIsNull;
 }