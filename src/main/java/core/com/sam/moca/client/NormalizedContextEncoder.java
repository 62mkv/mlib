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

package com.sam.moca.client;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.sam.moca.MocaArgument;
import com.sam.moca.MocaOperator;
import com.sam.moca.MocaResults;
import com.sam.moca.util.DateUtils;

/**
 * Produces a normalized context string from a list of MocaArgument values.  This 
 * 
 * Copyright (c) 2010 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class NormalizedContextEncoder {
    public NormalizedContextEncoder(String charset) {
        _charset = charset;
    }
    
    /**
     * Produce a string of the form:
     *
     * <pre>
     * VVVVV...?O?T[x]?xxxxx~VVVVV...?O?T[x]?xxxxx|CCCCC...?O?T[x]?xxxxx~...
     * </pre>
     * 
     * ...where VVVVV... is a variable name, O is an encoding of an operator,
     * T is a data type, [x] is an optional 'x' that indicates NULL, and
     * xxxxx is a variable value, encoded as a string. Certain characters
     * are quoted.
     *
     * The second part of the string, after "|", is the "current" command's
     * where clause.
     */
    public void encodeContext(MocaArgument[] args, final Appendable out) throws IOException {
        if (args == null) return;
        
        boolean isFirst = true;
        
        for (MocaArgument arg : args) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                out.append('~');
            }

            encode(arg.getName(), out);
            out.append('?');
            out.append(String.valueOf(operMap.get(arg.getOper())));
            out.append('?');
            out.append(arg.getType().getTypeCode());
            Object value = arg.getValue();
            if (value == null) {
                out.append('x');
                out.append('?');
            }
            else {
                out.append('?');
                switch (arg.getType()) {
                case BINARY:
                case OBJECT:
                case GENERIC:
                case UNKNOWN:
                    // No value
                    break;
                case RESULTS:
                    OutputStream stream = new OutputStream() {
                        
                        // @see java.io.OutputStream#write(int)
                        @Override
                        public void write(int b) throws IOException {
                            encode(b, out);
                        }
                    };
                    FlatResultsEncoder.writeResults((MocaResults)value, null, stream, 
                        _charset);
                    break;
                case BOOLEAN:
                    out.append((Boolean)value ? "1" : "0");
                    break;
                case DATETIME:
                    out.append(DateUtils.formatDate((Date)value));
                    break;
                default:
                    encode(String.valueOf(value), out);
                    break;
                }
            }
        }
    }
    
    private void encode(CharSequence value, Appendable out) throws IOException {
        int len = value.length();
        for (int i = 0; i < len; i++) {
            char c = value.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '+' || c == '-' || c == '.' || c == '_') {
                out.append(c);
            }
            else if (c >= 0 && c < 128) {
                out.append(String.format("%%%02x", (int)c));
            }
            else {
                byte[] tmp = new String(new char[]{c}).getBytes(_charset);
                for (byte b : tmp) {
                    out.append(String.format("%%%02x", b));
                }
            }
        }
    }
    private void encode(int b, Appendable out) throws IOException {
        // If numeric, or alpha (ASCII ONLY), or certain special characters, we expect the encoding to consist of
        // only one byte.  In cases where it won't then we need to encode the byte value as a hex code.
        if ((b >= '0' && b <= '9') || (b >= 'A' && b <= 'Z') || (b >= 'a' && b <= 'z') || b == '+' || b == '-' || b == '.' || b == '_') {
            out.append((char)b);
        }
        else {
            // We have to make the byte an unsigned byte
            int bInt = b & 0xff;
            out.append(String.format("%%%02x", bInt));
        }
    }
    private static Map<MocaOperator, Integer> operMap = new HashMap<MocaOperator, Integer>();
    static {
        operMap.put(MocaOperator.NOTNULL, 1);
        operMap.put(MocaOperator.ISNULL, 2);
        operMap.put(MocaOperator.EQ, 3);
        operMap.put(MocaOperator.NE, 4);
        operMap.put(MocaOperator.LT, 5);
        operMap.put(MocaOperator.LE, 6);
        operMap.put(MocaOperator.GT, 7);
        operMap.put(MocaOperator.GE, 8);
        operMap.put(MocaOperator.LIKE, 9);
        operMap.put(MocaOperator.RAWCLAUSE, 10);
        operMap.put(MocaOperator.REFALL, 11);
        operMap.put(MocaOperator.REFONE, 12);
        operMap.put(MocaOperator.REFLIKE, 13);
        operMap.put(MocaOperator.NOTLIKE, 14);
        operMap.put(MocaOperator.NAMEDCLAUSE, 15);
    }

    private final String _charset;
}
