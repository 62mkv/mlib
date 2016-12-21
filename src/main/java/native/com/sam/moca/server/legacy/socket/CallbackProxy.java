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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.channels.ClosedByInterruptException;
import java.rmi.RemoteException;

import com.sam.moca.MocaInterruptedException;
import com.sam.util.AbstractInvocationHandler;
import com.sam.util.ClassUtils;

/**
 * The interface side of a two-way proxy mechanism for remote services.  An instance of this class is used
 * to provide an interface to a caller, implemented as a service over some kind of communication channel (most likely
 * a socket or pair of pipes).  Unlike other RPC mechanisms, when awaiting a reply to a remote call, the channel is
 * watched for an incoming request.  That request is handed off to another proxy handler for a different interface,
 * assumed to be using the same communication channels. This proxy and its corresponding Callback handler are
 * idempotent -- meaning multiple calls can be nested with no ill effects.
 * 
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * 
 * @author derek
 * @version $Revision$
 */
public class CallbackProxy <T> extends AbstractInvocationHandler {
    
    /**
     * Creates a new callback-enabled proxy and builds a callback handler for the given target object.
     * Note that this factory method hides the actual handler for the callback inside the proxy.  It
     * therefore cannot be used outside of this proxy.  To allow that usage, use the factory method
     * that takes a callback handler.
     */
    public static <T, P> T newProxy(ObjectOutputStream out, ObjectInputStream in,
                                    Class<T> cls, Class<P> callbackClass, 
                                    P target) {
        CallbackHandler<P> handler = new CallbackHandler<P>(out, in, target, callbackClass);
        return newProxy(out, in, cls, callbackClass, handler); 
    }

    /**
     * Creates a new callback-enabled proxy and uses the given callback handler.  With this factory
     * method, it is possible to allow for the callback handler to be the primary actor on the thread.
     */
    @SuppressWarnings("unchecked")
    public static <T, P> T newProxy(ObjectOutputStream out, ObjectInputStream in,
                                    Class<T> cls, Class<P> callbackClass,
                                    CallbackHandler<P> callback) {
        T proxy = (T) Proxy.newProxyInstance(ClassUtils.getClassLoader(),
            new Class<?>[] {cls}, 
            new CallbackProxy<P>(out, in, callbackClass, callback));
        
        return proxy;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Object proxyInvoke(Object proxy, Method method, Object[] args) throws Throwable {
        T previousTarget = null;
        boolean setTarget = false;
        try {
            _out.reset();
            
            String methodName = method.getName();
            if (methodName.equals("shutdown")) {
                // We send the shutdown now
                shutdown(false);
                // Shutdown doesn't have any return values
                return null;
            }
            
            _out.writeByte(SocketProtocol.REQUEST_INDICATOR);
            _out.writeUTF(methodName);
            if (args == null) {
                _out.writeInt(-1);
            }
            else {
                _out.writeInt(args.length);
                for (Object arg : args) {
                    if (arg != null && _callbackType != null && 
                        _callbackType.isAssignableFrom(arg.getClass())) {
                        previousTarget = _callback.getTarget();
                        _callback.setTarget((T)arg);
                        setTarget = true;
                        _out.writeBoolean(true);
                    }
                    else {
                        _out.writeBoolean(false);
                        _out.writeObject(arg);
                    }
                }
            }
            
            byte responseType = getResponseType();
            
            if (responseType == SocketProtocol.RESPONSE_TYPE_NORMAL) {
                return _in.readObject();
            }
            else if (responseType == SocketProtocol.RESPONSE_TYPE_EXCEPTION) {
                throw (Exception) _in.readObject();
            }
            else {
                throw new IllegalArgumentException("Unable to handle response: " + responseType);
            }
        }
        catch (ClosedByInterruptException e) {
            // We  have to clear the interrupt status since we know which
            // thread was interrupted, ours.  This is because no other threads
            // use this socket and ClosedByInterruptException leaves the
            // thread whose interrupt closed it still in an interrupt state
            Thread.interrupted();
            throw new MocaInterruptedException(e);
        }
        catch (InterruptedIOException e) {
            throw new MocaInterruptedException(e);
        }
        catch (IOException e) {
            throw new RemoteException("Unable to communicate with proxy handler: " + e, e);
        }
        catch (ClassNotFoundException e) {
            throw new RemoteException("Unable to read results from proxy handler: " + e, e);
        }
        finally {
            if (setTarget) {
                _callback.setTarget(previousTarget);
            }
        }
    }
        
    protected CallbackProxy(ObjectOutputStream out, ObjectInputStream in, Class<T> callbackClass, CallbackHandler<T> callback) {
        _in = in;
        _out = out;
        _callbackType = callbackClass;
        _callback = callback;
    }
    
    private void shutdown(boolean clean) throws IOException {
        if (clean) {
            _out.writeByte(SocketProtocol.SHUTDOWN_INDICATOR);
            _out.flush();
        }
        // Either way we now close the streams which should also
        // force the other side of the socket to error if it didn't
        // shutdown correctly from our indicator
        // We have to close the output first.  If we close input first
        // then the other side may close the output before we can.
        _out.close();
        _in.close();        
    }
    
    private byte getResponseType() throws IOException {
        _out.flush();
        while (true) {
            byte indicator = _in.readByte();
            
            // Some jvm platforms the above readByte isn't interruptable so we
            // check interrupt state first
            if (Thread.interrupted()) {
            	throw new MocaInterruptedException();
            }

            // If a request is made while we're awaiting a reply we need to handle it.
            if (indicator == SocketProtocol.REQUEST_INDICATOR && _callback != null) {
                _callback.dispatch();
            }
            else if (indicator == SocketProtocol.RESPONSE_INDICATOR) {
                return _in.readByte();
            }
            else {
                throw new RemoteException("Unexpected response: " + indicator);
            }
        }
    }

    // @see java.lang.Object#finalize()
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            _in.close();
        }
        catch (IOException ignore) {
            // We don't care if this object is being garbage collected.
        }
        
        try {
            _out.close();
        }
        catch (IOException ignore) {
            // We don't care if this object is being garbage collected.
        }
    }

    private final ObjectInputStream _in;
    private final ObjectOutputStream _out;
    private final CallbackHandler<T> _callback;
    private final Class<T> _callbackType;
}
