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

package com.sam.moca.server.legacy;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import com.sam.moca.EditableResults;
import com.sam.moca.MocaException;
import com.sam.moca.MocaException.Args;
import com.sam.moca.MocaInterruptedException;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaType;
import com.sam.moca.RowIterator;
import com.sam.moca.SimpleResults;
import com.sam.moca.server.db.BindList;

/**
 * This class contains all the information about a command execution status as
 * is done in C.  That is that it will contain a result set, error code, message,
 * and arguments.  This should be used when communicating between Java and C.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class NativeReturnStruct implements Externalizable {
    private static final long serialVersionUID = 629319920576214738L;
    
    public static void setResultsClass(Class<? extends EditableResults> cls) {
        resultsImplClass = cls;
    }
    
    /**
     * @return Returns the resultsImplClass.
     */
    public static Class<? extends EditableResults> getResultsClass() {
        return resultsImplClass;
    }
    
    /**
     * This constructor is used when there is an exception in execution when
     * invoked from the C side
     * @param e The exception that occurred
     */
    public NativeReturnStruct(MocaException e) {
        this(e.getErrorCode(), e.getResults(), e.getMessage(), e.isMessageResolved(), e.getArgList(), null);
    }
    
    /**
     * This constructor is used when a successful execution is done to return
     * to the C side
     * @param res the result set to return
     */
    public NativeReturnStruct(MocaResults res) {
        this(0, res, null, false, null, null);
    }
    
    public NativeReturnStruct(MocaResults res, BindList bind) {
        this(0, res, null, false, null, bind);
    }
    
    /**
     * This constructor is to allow a user to set all the values within it
     * directly
     * @param errorCode
     * @param results
     * @param message
     * @param args
     */
    public NativeReturnStruct(int errorCode, MocaResults results, String message, boolean messageResolved, 
                              Args[] args, BindList bindList) {
        _errorCode = errorCode;
        _results = results;
        _message = message;
        _messageResolved = messageResolved;
        _args = args;
        
        if (bindList != null && bindList.hasReferences()) {
            _bindList = bindList;
        }
        else {
            _bindList = null;
        }
    }
    
    // Only to be used for Externalizable serialization
    public NativeReturnStruct() {
        
    }

    /**
     * @return Returns the _args.
     */
    public Args[] getArgs() {
        return _args;
    }

    /**
     * @return Returns the _results.
     */
    public MocaResults getResults() {
        return _results;
    }

    /**
     * @return Returns the _message.
     */
    public String getMessage() {
        return _message;
    }

    /**
     * @return Returns the _errorCode.
     */
    public int getErrorCode() {
        return _errorCode;
    }
    
    public BindList getBindList() {
        return _bindList;
    }
    
    public boolean isMessageResolved() {
        return _messageResolved;
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        _errorCode = in.readInt();
        _message = (String) in.readObject();
        _messageResolved = in.readBoolean();
        _args = (Args[]) in.readObject();
        _bindList = (BindList) in.readObject();
        _results = readResults(in, resultsImplClass);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(_errorCode);
        out.writeObject(_message);
        out.writeBoolean(_messageResolved);
        out.writeObject(_args);
        out.writeObject(_bindList);
        writeResults(out, _results);
    }

    //
    // Implementation
    //
    
    private static MocaResults readResults(DataInput stream, Class<? extends EditableResults> resultsImplClass)
        throws IOException {
        
        int cols = stream.readInt();
        
        // If we got a -1 that means the result set was null so just return
        // immediately
        if (cols == -1) {
            return null;
        }
        
        EditableResults tmp;
        try {
            tmp = resultsImplClass.newInstance();
        }
        catch (IllegalAccessException e) {
            throw new IOException("Unexpected instantiation error", e);
        }
        catch (InstantiationException e) {
            throw new IOException("Unexpected instantiation error", e);
        }
        
        MocaType colTypes[] = new MocaType[cols];
        
        for (int i = 0; i < cols; i++) {
            String name = stream.readUTF();
            colTypes[i] = MocaType.lookup(stream.readChar());
            int maxLength = stream.readInt();
            boolean nullable = stream.readBoolean();
            
            tmp.addColumn(name, colTypes[i], maxLength, nullable);
        }
        
        int rows = stream.readInt();
        
        for (int r = 0; r < rows; r++) {
            tmp.addRow();
            for (int i = 0; i < cols; i++) {
                byte dataIndicator = stream.readByte();
                boolean isNull = dataIndicator == NULL_DATA;
                if (!isNull) {
                    switch (colTypes[i]) {
                    case STRING:
                        if (dataIndicator == NORMAL_DATA) {
                            tmp.setStringValue(i, stream.readUTF());
                        }
                        else {
                            int numStrings = stream.readInt();
                            StringBuilder buf = new StringBuilder(numStrings * LARGE_STRING_THRESHOLD);
                            for (int s = 0; s < numStrings; s++) {
                                buf.append(stream.readUTF());
                            }
                            tmp.setStringValue(i, buf.toString());
                        }
                        
                        break;
                    case INTEGER:
                        tmp.setIntValue(i, stream.readInt());
                        break;
                    case DOUBLE:
                        tmp.setDoubleValue(i, stream.readDouble());
                        break;
                    case BOOLEAN:
                        tmp.setBooleanValue(i, stream.readBoolean());
                        break;
                    case BINARY:
                        int length = stream.readInt();
                        byte[] data = new byte[length];
                        stream.readFully(data);
                        tmp.setBinaryValue(i, data);
                        break;
                    case DATETIME:
                        tmp.setDateValue(i, new Date(stream.readLong()));
                        break;
                    case RESULTS:
                        tmp.setResultsValue(i, readResults(stream, resultsImplClass));
                        break;
                    case GENERIC:
                        tmp.setValue(i, new GenericPointer(stream.readLong()));
                        break;
                    default:
                        // Nothing
                        break;
                    }
                }
                else {
                    tmp.setNull(i);
                }
            }
        }
        
        return tmp;
    }

    private static void writeResults(DataOutput stream, MocaResults results) throws IOException {
        if (results == null) {
            stream.writeInt(-1);
            return;
        }
        
        // Write metadata
        int columnCount =  results.getColumnCount();
        stream.writeInt(columnCount);
        
        MocaType colTypes[] = new MocaType[columnCount];
        for (int i = 0; i < columnCount; i++) {
            stream.writeUTF(results.getColumnName(i));
            colTypes[i] = results.getColumnType(i);
            stream.writeChar(colTypes[i].getTypeCode());
            stream.writeInt(results.getMaxLength(i));
            stream.writeBoolean(results.isNullable(i));
        }
        
        int rowCount = results.getRowCount();
        
        stream.writeInt(rowCount);
        
        RowIterator row = results.getRows();
        while (row.next()) {
            for (int i = 0; i < columnCount; i++) {
                MocaType colType = colTypes[i];
                if (row.isNull(i)) {
                    stream.writeByte(NULL_DATA);
                }
                else if (colType == MocaType.DATETIME) {
                    // Dates can come through where isNull() returns false, but the value
                    // comes back as null.
                    Date date = row.getDateTime(i);
                    
                    if (date == null) { 
                       stream.writeByte(NULL_DATA);
                    }
                    else {
                        stream.writeByte(NORMAL_DATA);
                        stream.writeLong(date.getTime());
                    }
                }
                else {
                    switch (colType) {
                    case STRING:
                        String tmpString = row.getString(i);
                        
                        // If the string is smaller than a certain threshold,
                        // send it in one call. otherwise, send it as a series
                        // of separate strings. This is to deal with the
                        // DataOutput.writeUTF() limitation of 0xFFFF bytes.
                        int len = tmpString.length();
                        if (len <= LARGE_STRING_THRESHOLD) {
                            stream.writeByte(NORMAL_DATA);
                            stream.writeUTF(tmpString);
                        }
                        else {
                            stream.writeByte(LARGE_DATA);
                            int numStrings = (len - 1)/LARGE_STRING_THRESHOLD + 1;  
                            stream.writeInt(numStrings);
                            for (int s = 0; s < numStrings; s++) {
                                // We want to check in case of large strings to 
                                // responsive to interrupts
                                if (Thread.interrupted()) {
                                    throw new MocaInterruptedException();
                                }
                                int begin = s * LARGE_STRING_THRESHOLD;
                                int end = (s + 1) * LARGE_STRING_THRESHOLD;
                                if (end > len) end = len;
                                stream.writeUTF(tmpString.substring(begin, end));
                            }
                        }
                        break;
                    case RESULTS:
                        stream.writeByte(NORMAL_DATA);
                        writeResults(stream, row.getResults(i));
                        break;
                    case INTEGER:
                        stream.writeByte(NORMAL_DATA);
                        stream.writeInt(row.getInt(i));
                        break;
                    case DOUBLE:
                        stream.writeByte(NORMAL_DATA);
                        stream.writeDouble(row.getDouble(i));
                        break;
                    case BOOLEAN:
                        stream.writeByte(NORMAL_DATA);
                        stream.writeBoolean(row.getBoolean(i));
                        break;
                    case BINARY:
                        stream.writeByte(NORMAL_DATA);
                        byte[] data = (byte[]) row.getValue(i);
                        stream.writeInt(data.length);
                        stream.write(data);
                        break;
                    case GENERIC:
                        stream.writeByte(NORMAL_DATA);
                        stream.writeLong(((GenericPointer)row.getValue(i)).getValue());
                        break;
                    case OBJECT:
                        stream.writeByte(NORMAL_DATA);
                        // Nothing
                        break;
                    default:
                        break;
                    }
                }
            }
        }
    }

    // Data indicators -- used for NULL, normal, and large data.
    private static final byte NORMAL_DATA = 0;
    private static final byte NULL_DATA = 1;
    private static final byte LARGE_DATA = 2;
    
    // Our threshold for normal vs large string handling.
    private static final int LARGE_STRING_THRESHOLD = 10240;

    private Args[] _args;
    private transient MocaResults _results;
    private String _message;
    private BindList _bindList;
    private int _errorCode;
    private boolean _messageResolved;
    private static Class<? extends EditableResults> resultsImplClass = SimpleResults.class;
}