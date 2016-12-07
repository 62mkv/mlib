/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

package com.redprairie.moca.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.redprairie.moca.MocaArgument;
import com.redprairie.moca.MocaOperator;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.util.DateUtils;

/**
 * Normalized context for MOCA requests comes in the form:
 * VVVVV...?O?T[x]?xxxxx~VVVVV...?O?T[x]?xxxxx|CCCCC...?O?T[x]?xxxxx~...
 * ...where VVVVV... is a variable name, O is an encoding of an operator,
 * T is a data type, [x] is an optional 'x' that indicates NULL, and
 * xxxxx is a variable value, encoded as a string. Certain characters
 * are quoted.
 *
 * The second part of the string, after "|", is the "current" command's
 * where clause.
 */
public class NormalizedContextDecoder {
    
    public NormalizedContextDecoder(String text) {
        this(text, Charset.forName("UTF-8"));
    }
    
    public NormalizedContextDecoder(String text, Charset charset) {
        _text = text;
        decoder = charset.newDecoder();
    }
    
    public void decode() throws ProtocolException {
        String[] majorSections = _text.split("\\|", -1);
        if (majorSections.length != 2) {
            throw new ProtocolException("invalid context string: " + _text);
        }

        try {
            _context = decodeArgList(majorSections[0]);
            _args = decodeArgList(majorSections[1]);
        }
        catch (CharacterCodingException e) {
            throw new ProtocolException("error parsing context string: " + e, e);
        }
    }
    
    /**
     * @return Returns the args.
     */
    public List<MocaArgument> getArgs() {
        return _args;
    }
    
    /**
     * @return Returns the context.
     */
    public List<MocaArgument> getContext() {
        return _context;
    }
    
    //
    // Implementation
    //
    
    private List<MocaArgument> decodeArgList(String list) throws CharacterCodingException, ProtocolException {
        List<MocaArgument> results = new ArrayList<MocaArgument>();
        String[] args = list.split("\\~", -1);
        for (String arg : args) {
            if (!(arg.equals(""))) {
                String[] fields = arg.split("\\?", -1);
                if (fields.length != 4) {
                    throw new ProtocolException("can't decode arguments -- missing fields.");
                }
               
                String name = decodeString(fields[0]);
                MocaOperator oper = operMap.get(Integer.valueOf(fields[1]));
                MocaType dataType = MocaType.lookup(fields[2].charAt(0));
               
                if (fields[2].length() == 2 && fields[2].charAt(1) == 'x') {
                    // Ignore the last field
                    results.add(new MocaArgument(name, oper, dataType, null));
                }
                else {
                    Object value = null;
                    switch (dataType) {
                    case OBJECT:
                    case GENERIC:
                    case UNKNOWN:
                        // No value
                        break;
                    case BINARY:
                        // We don't support binary as a context value
                        value = new byte[0];
                        break;
                    case RESULTS:
                        String newString = fields[3];
                        final byte[] bytes = newString.getBytes(decoder.charset());
                        final InputStream stream = new InputStream() {

                                // @see java.io.InputStream#read()
                                @Override
                                public synchronized int read() {
                                    if (i >= bytes.length) {
                                        return -1;
                                    }
                                    int b = bytes[i++];
                                    try {
                                        b = decodeByte(b, this);
                                    }
                                    catch (IOException e) {
                                        b = -1;
                                    }
                                    return b;
                                }
                                
                                private int i = 0;
                        };
                        try {
                            value = new FlatResultsDecoder(stream, 
                                decoder.charset().name()).decode();
                        }
                        catch (IOException e) {
                            throw new ProtocolException("Problem when decoding results", e);
                        }
                        break;
                    case BOOLEAN:
                        value = Boolean.valueOf(fields[3].equals("1"));
                        break;
                    case DATETIME:
                        value = DateUtils.parseDate(fields[3]);
                        break;
                    case INTEGER:
                        value = Integer.valueOf(fields[3]);
                        break;
                    case DOUBLE:
                        value = Double.valueOf(fields[3]);
                        break;
                    case STRING:
                        value = decodeString(fields[3]);
                        break;
                    }
                    results.add(new MocaArgument(name, oper, dataType, value));
                }
            }
        }
        return results;
    }
    
    private final String decodeString(CharSequence orig) throws CharacterCodingException {
        StringBuilder out = new StringBuilder();
        
        int len = orig.length();
        
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        for (int i = 0; i < len; i++) {
            char c = orig.charAt(i);
            if (c == '%') {
                byteBuffer.write(Integer.parseInt(orig.subSequence(i + 1, i + 3).toString(), 16));
                i += 2;
            }
            else {
                if (byteBuffer.size() > 0) {
                    try {
                    out.append(decoder.decode(ByteBuffer.wrap(byteBuffer.toByteArray())));
                    }
                    catch (MalformedInputException e) {
                        throw e;
                    }
                    byteBuffer.reset();
                }

                out.append(c);
            }
        }

        if (byteBuffer.size() > 0) {
            out.append(decoder.decode(ByteBuffer.wrap(byteBuffer.toByteArray())));
            byteBuffer.reset();
        }

        return out.toString();
    }
    
    private final int decodeByte(int b, InputStream in) throws IOException {
        if (b == '%') {
            byte[] byteValue = new byte[2];
            byteValue[0] = (byte)in.read();
            byteValue[1] = (byte)in.read();
            String value = new String(byteValue, decoder.charset());
            b = Integer.valueOf(value, 16);
        }

        return b;
    }
    
    private final String _text;
    
    private static Map<Integer, MocaOperator> operMap = new HashMap<Integer, MocaOperator>();
    static {
        operMap.put(1, MocaOperator.NOTNULL);
        operMap.put(2, MocaOperator.ISNULL);
        operMap.put(3, MocaOperator.EQ);
        operMap.put(4, MocaOperator.NE);
        operMap.put(5, MocaOperator.LT);
        operMap.put(6, MocaOperator.LE);
        operMap.put(7, MocaOperator.GT);
        operMap.put(8, MocaOperator.GE);
        operMap.put(9, MocaOperator.LIKE);
        operMap.put(10, MocaOperator.RAWCLAUSE);
        operMap.put(11, MocaOperator.REFALL);
        operMap.put(12, MocaOperator.REFONE);
        operMap.put(13, MocaOperator.REFLIKE);
        operMap.put(14, MocaOperator.NOTLIKE);
        operMap.put(15, MocaOperator.NAMEDCLAUSE);
    }
    

    private final CharsetDecoder decoder;
    private List<MocaArgument> _args;
    private List<MocaArgument> _context;
}
