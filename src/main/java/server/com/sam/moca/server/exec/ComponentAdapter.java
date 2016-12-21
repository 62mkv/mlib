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

package com.sam.moca.server.exec;

import org.apache.logging.log4j.LogManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;

import org.apache.logging.log4j.Logger;

import com.redprairie.mad.client.MadFactory;
import com.redprairie.mad.client.MadMetrics;
import com.sam.moca.EditableResults;
import com.sam.moca.MocaContext;
import com.sam.moca.MocaException;
import com.sam.moca.MocaInterruptedException;
import com.sam.moca.MocaResults;
import com.sam.moca.MocaRuntimeException;
import com.sam.moca.MocaType;
import com.sam.moca.NotFoundException;
import com.sam.moca.SimpleResults;
import com.sam.moca.exceptions.MocaDBException;
import com.sam.moca.exceptions.UnexpectedException;
import com.sam.util.ClassUtils;

/**
 * An adapter class to be used with MOCA's JNI layer.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All rights reserved.
 * </pre></b>
 *
 * @author Derek Inksetter
 * @version $Revision$
 */
public class ComponentAdapter {
    
    public static ComponentAdapter lookupMethod(String pkg, String className,
            String methodName, MocaType[] argTypes) throws MocaException {

        // Translate the argument codes into MocaType object.
        // Append package to class name, if present

        if (pkg != null && pkg.length() != 0) {
            className = pkg + '.' + className;
        }
        
        ClassLoader loader = ClassUtils.getClassLoader();
        
        Class<?> cls;
        try {
            cls = loader.loadClass(className);
        }
        catch (ClassNotFoundException e) {
            throw new ComponentNotFoundException(className);
        }
        
        Method[] methods = cls.getMethods();
        Method method = null;
        boolean passContext = false;
        boolean passFactory = false;
        
        outer:
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)) {
                Class<?>[] declaredArgTypes = methods[i].getParameterTypes();
                
                // int to track the index of the declared parameters
                int d = 0;
                // int to track the index of the given parameters
                int g = 0;

                // Set the index of the declared arguments to ignore the number
                // of injectable arguments for now (MocaContext or MadFactory).
                int injectableArgs = declaredArgTypes.length - argTypes.length;
                d = injectableArgs;
                
                // You can have at most two injectable args, MocaContext and MadFactory.
                if (injectableArgs > 2 || injectableArgs < 0) {
                    continue;
                }

                // Check that the non-injectable arguments match up.
                for (; d < declaredArgTypes.length && g < argTypes.length; d++, g++) {
                    if (!_isCompatible(declaredArgTypes[d], argTypes[g].getValueClass())) {
                        continue outer;
                    }
                }

                // If there is one injectable argument the first parameter must
                // be either a MocaContext or MadFactory.
                if (injectableArgs == 1) {
                    if (declaredArgTypes[0].equals(MocaContext.class)) {
                        passContext = true;
                    }
                    else if (declaredArgTypes[0].equals(MadFactory.class)) {
                        passFactory = true;
                    }
                    else {
                        continue;
                    }
                }
                // If there are two injectable argument the two arguments in
                // order must be a MocaContext and a MadFactory.
                else if (injectableArgs == 2) {
                    if (declaredArgTypes[0].equals(MocaContext.class)
                            && declaredArgTypes[1].equals(MadFactory.class)) {
                        passContext = true;
                        passFactory = true;
                    }
                    else {
                        continue;
                    }
                }

                // We have found theoretically a method match.
                method = methods[i];
                break;          
            }
        }
        
        if (method == null) {
            throw new ComponentNotFoundException(className, methodName);
        }
        
        ComponentAdapter def = new ComponentAdapter(cls, method);
        
        def._passContext = passContext;
        def._passFactory = passFactory;

        // Don't create an instance of our component if they're using
        // a static (class) method.
        if (Modifier.isStatic(method.getModifiers())) {
            def._constructor = null;
        }
        else {
            Constructor<?>[] constructors = cls.getConstructors();
            Constructor<?> defaultConstructor = null;
            Constructor<?> contextConstructor = null;
            for (int i = 0; i < constructors.length; i++) {
                Class<?>[] parameters = constructors[i].getParameterTypes();
                if (parameters.length == 0) {
                    defaultConstructor = constructors[i];
                }
                else if (parameters.length == 1 &&
                         parameters[0].equals(MocaContext.class)) {
                    contextConstructor = constructors[i];
                }
            }
            if (contextConstructor != null) {
                def._constructor = contextConstructor;
                def._passContextToConstructor = true;
            }
            else if (defaultConstructor != null) {
                def._constructor = defaultConstructor;
                def._passContextToConstructor = false;
            }
            else {
                throw new ComponentNotFoundException(className);
            }
        }
        
        return def;
    }
    
    /**
     * Executes a method on a class.  The class is instantiated and the method
     * is called on the resulting object.  There are a number of ways in which
     * a component's  methods can be called.
     * 
     * @param args The arguments to pass.
     * @return an instance of MocaResults that represents the output of the
     * given component invocation.
     * @throws MocaException if the component threw an exception.
     */
    public MocaResults executeMethod(Object[] args, MocaContext ctx) throws MocaException {
        
        try {
            if (_passContext || _passFactory) {
                int injectableArgs = 0;
                if (_passContext) injectableArgs++;
                if (_passFactory) injectableArgs++;
                    
                Object[] newArgs = new Object[args.length + injectableArgs];
                System.arraycopy(args, 0, newArgs, injectableArgs, args.length);
                args = newArgs;
                args[0] = _passContext ? ctx : MadMetrics.getFactory();
                if (injectableArgs == 2) {
                    args[1] = MadMetrics.getFactory();
                }
            }
            
            Object component;
            
            if (_constructor == null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Static method on " + _cls.getName() + ", no instance created");
                }
                component = null;
            }
            else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Creating instance of " + _cls.getName());
                }
                try {
                    Object[] constructorArgs;
                    if (_passContextToConstructor) {
                        constructorArgs = new Object[] {ctx};
                    }
                    else {
                        constructorArgs = new Object[0];
                    }
                    component = _constructor.newInstance(constructorArgs);
                }
                catch (InstantiationException e) {
                    throw new UnexpectedException(e);
                }
            }
            
            if (LOG.isDebugEnabled())
                LOG.debug("Invoking method: " + _method);
            
            Object result = _method.invoke(component, args);
            
            if (result == null) {
                // If the method is declared as a void, then return
                // an empty results object.  Otherwise, return null, which
                // will trigger an error response from MOCA.
                Class<?> returnType = _method.getReturnType();
                if (returnType.equals(Void.TYPE)) {
                    return new SimpleResults();
                }
                else {
                    EditableResults res = new SimpleResults();
                    res.addColumn("result", MocaType.lookupClass(returnType));
                    res.addRow();
                    res.setNull(0);
                    return res;
                }
            }
            else {
                return ResultMapper.createResults(result);
            }
        }
        catch (InvocationTargetException e) {
            Throwable thrown = e.getTargetException();
            
            if (thrown instanceof NotFoundException) {
                LOG.debug("Exception thrown from component: " + thrown);
            }
            else {
                LOG.debug("Exception thrown from component", thrown);
            }

            if (thrown instanceof MocaException) {
                MocaException cause = (MocaException) thrown;
                throw cause;
            }
            else if (thrown instanceof MocaInterruptedException) {
                // We want this to propagate up.
                throw (MocaInterruptedException)thrown;
            }
            else if (thrown instanceof MocaRuntimeException) {
                MocaRuntimeException cause = (MocaRuntimeException) thrown;
                throw new MocaException(cause);
            }
            else if (thrown instanceof SQLException) {
                throw new MocaDBException((SQLException)thrown);
            }
            else if (thrown instanceof InterruptedException) {
                throw new MocaInterruptedException((InterruptedException)thrown);
            }
            else {
                throw new UnexpectedException(thrown);
            }
        }
        catch (IllegalAccessException e) {
            LOG.debug("Unexpected error", e);
            throw new UnexpectedException(e);
        }
        catch (RuntimeException e) {
            LOG.debug("Unexpected error", e);
            throw new UnexpectedException(e);
        }
    }
    
    //
    // Implementation
    //
    
    private ComponentAdapter(Class<?> cls, Method method) {
        _cls = cls;
        _method = method;
    }
    
    private static boolean _isCompatible(Class<?> declared, Class<?> passed) {
        // Note, in the case of primitives, passed will always be a wrapper
        // type. Declared may be a wrapper or the TYPE class.
        if (declared.equals(passed)) return true;
        if (declared.isAssignableFrom(passed)) return true;
        if (passed == Object.class && !declared.isPrimitive()) return true;
        if (!declared.isPrimitive()) return false;
        if ((declared.equals(Integer.TYPE) && passed.equals(Integer.class)) ||
            (declared.equals(Double.TYPE) && passed.equals(Double.class)) ||
            (declared.equals(Long.TYPE) && passed.equals(Long.class)) ||
            (declared.equals(Boolean.TYPE) && passed.equals(Boolean.class)) ||
            (declared.equals(Character.TYPE) && passed.equals(Character.class))) {
            return true;
        }
        return false;
    }
    
    private static final Logger LOG = LogManager.getLogger(ComponentAdapter.class);

    private Method _method;
    private Constructor<?> _constructor;
    private boolean _passContext;
    private boolean _passContextToConstructor;
    private boolean _passFactory;
    private Class<?> _cls;
}
