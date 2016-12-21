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

package com.sam.moca.server.legacy.socket;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.sam.moca.MocaException;
import com.sam.moca.MocaInterruptedException;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaRuntimeException;
import com.sam.moca.server.legacy.NativeReturnStruct;

/**
 * The handler side of a two-way proxy mechanism for remote services.  An instance of this class is used to handle
 * requests coming from a proxy interface, implementing a remote service over some kind of communication channel (most
 * likely a socket or pair of pipes).  Certain arguments (those of a particular type) to invoked methods can be
 * replaced with an alternative object, usually another CallbackProxy communicating on the same channel.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 * @version $Revision$
 */
class CallbackHandler<T> {
    
    public static <T,P> CallbackHandler<T> newHandler(
            ObjectOutputStream out, ObjectInputStream in, Class<T> cls,
            Class<P> proxyClass, T target) {

        CallbackHandler<T> handler = new CallbackHandler<T>(out, in, target, cls, null);
        P proxy = CallbackProxy.newProxy(out, in, proxyClass, cls, handler);
        handler.setCallback(proxy);
        return handler;
    }
    
    protected CallbackHandler(ObjectOutputStream out, ObjectInputStream in, T target, Class<T> cls, Object callback) {
        _out = out;
        _in = in;
        _target = target;
        _type = cls;
        _callback = callback;
    }
    
    protected CallbackHandler(ObjectOutputStream out, ObjectInputStream in, T target, Class<T> cls) {
        this(out, in, target, cls, null);
    }
    
    protected void setTarget(T target) {
        _target = target;
    }
    
    protected T getTarget() {
        return _target;
    }
    
    protected void setCallback(Object callback) {
        _callback = callback;
    }
    
    protected Object getCallback() {
        return _callback;
    }
    
    public void dispatch() throws IOException {
        byte responseType;
        Object result;
        try {
            String method = (String) _in.readUTF();
            int argLength = _in.readInt();
            Object[] args = null;
            if (argLength >= 0) {
                args = new Object[argLength];
                for (int i = 0; i < argLength; i++) {
                    if (_in.readBoolean()) {
                        args[i] = _callback;
                    }
                    else {
                        args[i] = _in.readObject();
                    }
                }
            }
            
            Method m = lookupMethod(method);
            
            result = m.invoke(_target, args);
            responseType = SocketProtocol.RESPONSE_TYPE_NORMAL;
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unexpected exception: " + e);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Unexpected exception: " + e);
        }
        catch (InvocationTargetException e) {
            responseType = SocketProtocol.RESPONSE_TYPE_EXCEPTION;
            result = e.getCause();
            // If it is an error just let it go up.
            if (result instanceof Error) {
                throw (Error)result;
            }
            // We don't let the interrupted exception go back to the caller
            else if (result instanceof MocaInterruptedException) {
                throw (MocaInterruptedException)result;
            }
            
            // First check to make sure that none of the exceptions in the
            // are interrupt related
            Throwable temp = e.getCause();
            
            while (temp != null) {
                // If one was interrupted then throw a moca interrupt exception
                // holding onto the original exception.
                if (temp instanceof InterruptedException || 
                        temp instanceof InterruptedIOException) {
                    throw new MocaInterruptedException(e);
                }
                temp = temp.getCause();
            }
            
            // If it isn't a MocaException then we should print it out, else
            // people will just see a 502 error and no tracing.
            if (!(result instanceof MocaException || 
                    result instanceof MocaRuntimeException)) {
                ((Throwable)result).printStackTrace();
            }
        }
        
        // Just in case if we are interrupted and the read didn't come out we check just in case.
        if(Thread.interrupted()){
            throw new MocaInterruptedException();
        }

        // Now, unless something has really gone wrong, we should have the result
        // or the exception here.
        _out.reset();
        _out.writeByte(SocketProtocol.RESPONSE_INDICATOR);
        _out.writeByte(responseType);
        _out.writeUnshared(result);
        _out.flush();
        
        if (result instanceof NativeReturnStruct) {
            MocaResults res = ((NativeReturnStruct) result).getResults();
            if (res != null) res.close();
        }
    }
    
    public void dispatchLoop() throws IOException {
        _out.flush();
        while (true) {
            byte indicator = _in.readByte();

            // The handler side of the proxy/handler pair only responds to requests.
            if (indicator == SocketProtocol.REQUEST_INDICATOR) {
                dispatch();
            }
            else if (indicator == SocketProtocol.SHUTDOWN_INDICATOR) {
                break;
            }
            else {
                throw new IOException("Unexpected indicator: " + indicator);
            }
        }
    }

    private Method lookupMethod(String name) {
        
        Method found = _methods.get(name);
        if (found != null) {
            return found;
        }
        
        for (Method m : _type.getMethods()) {
            if (m.getName().equals(name)) {
                _methods.put(name, m);
                return m;
            }
        }
        
        return null;
    }

    private final ObjectInputStream _in;
    private final ObjectOutputStream _out;
    private final Map<String, Method> _methods = new HashMap<String, Method>();
    private final Class<T> _type;
    private T _target;
    private Object _callback;
}
