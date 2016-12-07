/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2007
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

package com.redprairie.moca.server.exec;

import groovy.lang.Binding;
import groovy.lang.GString;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaResults;
import com.redprairie.moca.MocaRuntimeException;
import com.redprairie.moca.MocaType;
import com.redprairie.moca.MocaValue;
import com.redprairie.moca.SimpleResults;
import com.redprairie.moca.exceptions.MocaDBException;
import com.redprairie.moca.server.repository.Command;
import com.redprairie.util.ClassUtils;
import com.redprairie.util.FixedSizeCache;

/**
 * Adapter to handle execution of the Groovy scripting language.  This mechanism provides tight
 * integration with the script engine and the MOCA execution stack.
 * 
 * <b><pre>
 * Copyright (c) 2007 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 * @param <T>
 */
public class GroovyScriptAdapter implements ScriptAdapter {
    
    /**
     * Default Constructor that doesn't look up outside Groovy scripts. 
     */
    public GroovyScriptAdapter() {
        this(null);
    }
    
    /**
     * Constructor that creates an instance of this class that looks up named scripts in a set of folders.
     * @param paths a set of directories in which to look for scripts to execute.
     */
    public GroovyScriptAdapter(Iterable<String> paths) {
        List<URL> pathURLs = new ArrayList<URL>();
        if (paths != null) {
            for (String dir : paths) {
                File f = new File(dir);
                if (f.exists()) {
                    try {
                        pathURLs.add(f.toURI().toURL());
                    }
                    catch (MalformedURLException e) {
                        _logger.debug("Exception while attempting to use file " +
                        		"for groovy script: " + e.getMessage());
                    }
                }
            }
        }
        _paths = pathURLs.toArray(new URL[pathURLs.size()]);
    }
    
    public class GroovyCompiledScript implements CompiledScript {
        
        GroovyCompiledScript(Class<? extends Script> compiledClass) {
            _compiled = compiledClass;
        }
        
        @Override
        public Object evaluate(MocaContext moca) throws MocaException {
            return runScript(_compiled, moca, false);
        }
        
        @Override
        public MocaResults execute(MocaContext moca) throws MocaException {
            return (MocaResults) runScript(_compiled, moca, true);
        }
        
        private final Class<? extends Script> _compiled;
    }
    
    @Override
    public CompiledScript compile(String script, Command runningCommand) throws MocaException {
        Class<? extends Script> compiledClass = compileScript(script, runningCommand);
        return new GroovyCompiledScript(compiledClass);
    }
    
    //
    // Implementation
    //
    
    static class _MocaBindings extends Binding {
        
        _MocaBindings(MocaContext moca) {
            super(new LinkedHashMap<String, Object>());
            _moca = moca;
        }
        
        // @see javax.script.SimpleBindings#get(java.lang.Object)
        @Override
        public Object getVariable(String name) {
            MocaValue mocaValue;
            if (getVariables().containsKey(name)) {
                return super.getVariable(name);
            }
            else if ((mocaValue = _moca.getStackVariable(name)) != null) {
                Object value = mocaValue.getValue();
                super.setVariable(name, value);
                return value;
            }
            else if (Character.isLowerCase(name.charAt(0))) {
                return null;
            }
            else {
                // Let the superclass decide how to deal with a missing
                // variable.
                return super.getVariable(name);
            }
        }
        
        // @see javax.script.SimpleBindings#put(java.lang.String, java.lang.Object)
        @Override
        public void setVariable(String name, Object value) {
            super.setVariable(name, value);
            _locals.add(name);
        }
        
        MocaContext _moca;
        Set<String> _locals = new HashSet<String>();
    }
    
    private Object runScript(Class<? extends Script> compiled, MocaContext moca, boolean returnResults) throws MocaException {
        Object result = null;
        try {
            // For thread safety, create a new instance of our script.
            Script scriptInstance = compiled.newInstance();
            
            // We use the Bindings interface to connect the running script with the MOCA context.
            // Any requests for identifiers will look in MOCA if the identifier has not been created
            // in the script context.
            _MocaBindings vars = new _MocaBindings(moca);
            
            // Put the MocaContext instance into the list of available variables.
            vars.setVariable("moca", moca);
            
            _logger.debug( "Executing Compiled Script");
            scriptInstance.setBinding(vars);
            result = scriptInstance.run();
            _logger.debug( "Script Execution Complete");
            
            if (result instanceof GString) {
                result = result.toString();
            }
    
            if (returnResults) {
                return extractResults(result, vars);
            }
            else {
                return result;
            }
        }
        catch (Exception e) {
            // Groovy doesn't wrap exceptions with their own wrappers.  They also don't declare all
            // checked exceptions on their method signatures.  The upshot of that is that any exception,
            // checked or runtime, could get thrown out of the Groovy methods.
            
            // It is possible for any Results object to be contained in a MOCA exception.  We must
            // make sure that one never gets back to the caller that is not an instance of WrappedResults
            
            // If it was a MocaInterruptedException just let it go up.
            if (e instanceof MocaInterruptedException) {
                throw (MocaInterruptedException)e;
            }
            // First check to make sure that none of the exceptions in the
            // are interrupt related
            Throwable temp = e;
            
            while (temp != null) {
                // If one was interrupted then throw a moca interrupt exception
                // holding onto the original exception.
                if (temp instanceof InterruptedException) {
                    throw new MocaInterruptedException(e);
                }
                temp = temp.getCause();
            }
            
            if (e instanceof MocaException) {
                MocaException cause = (MocaException) e;
                throw cause;
            }
            else if (e instanceof MocaRuntimeException) {
                MocaRuntimeException cause = (MocaRuntimeException) e;
                throw new MocaException(cause);
            }
            else if (e instanceof SQLException) {
                throw new MocaDBException((SQLException)e);
            }
            else {
                throw new MocaScriptException(e);
            }
        }
    }


    /**
     * @param script
     * @param running
     * @return
     * @throws IOException
     */
    private Class<? extends Script> compileScript(String script, Command running) throws MocaScriptException {
        Class<? extends Script> compiled;
        
        if (!script.isEmpty() && script.charAt(0) == '@') {
            // We need to look up scripts in the script cache.
            Class<?> loadedClass;
            try {
                loadedClass = getGroovyEngine().loadScriptByName(script.substring(1));
            }
            catch (ResourceException e) {
                throw new MocaScriptException(e);
            }
            catch (ScriptException e) {
                throw new MocaScriptException(e);
            }
            catch (IOException e) {
                throw new MocaScriptException(e);
            }
            
            if (!Script.class.isAssignableFrom(loadedClass)) {
                throw new IllegalArgumentException("Not a script");
            }

            compiled = loadedClass.asSubclass(Script.class);
            return compiled;
        }
        
        synchronized (_scriptCache) {
            compiled = _scriptCache.get(script);
            if (compiled == null) {
                _logger.debug("Compiling Script");
                String className = _determineClassName(running);
                // Now we do the double check idiom on the _shell to make
                // sure we properly lazy init it and are still thread safe
                // The _shell variable has to be volatile as well.
                try {
                    GroovyClassLoader shell = getGroovyEngine().getGroovyClassLoader();
                    Class<?> temp = shell.parseClass(SCRIPT_PREFIX + script, className);

                    if (!Script.class.isAssignableFrom(temp)) {
                        throw new MocaScriptException(new IllegalArgumentException("Not a script"));
                    }
                    compiled = temp.asSubclass(Script.class);
                }
                catch (IOException e) {
                    throw new MocaScriptException(e);
                }
                catch (GroovyRuntimeException e) {
                    throw new MocaScriptException(e);
                }

                _scriptCache.put(script, compiled);
            }
        }
        return compiled;
    }

    /**
     * @throws IOException
     */
    private GroovyScriptEngine getGroovyEngine() throws IOException {
        
        // If we've compiled a certain number of classes, let's start over.  A number of those classes are 
        if (compileCount.getAndIncrement() >= COMPILE_LIMIT) {
            synchronized(this) {
                if (compileCount.get() >= COMPILE_LIMIT) {
                    _gse = null;
                    compileCount.set(0);
                }
            }
        }
        
        if (_gse == null) {
            synchronized(this) {
                if (_gse == null) {
                    _logger.debug("Loading Groovy class loader");
                    URL url = GroovyScriptAdapter.class.getResource("resources/moca.groovyrc");
                    _gse = new GroovyScriptEngine(_paths, ClassUtils.getClassLoader());
                    GroovyClassLoader shell = _gse.getGroovyClassLoader();
                    GroovyCodeSource source =new GroovyCodeSource(url);
                    try {
                        Class<?> tmp = shell.parseClass(source);
                        Script tmpScript = (Script)tmp.newInstance();
                        tmpScript.run();
                    }
                    catch (InstantiationException e) {
                        throw new IllegalArgumentException(
                                "Groovy Init Error: " + e, e);
                    }
                    catch (IllegalAccessException e) {
                        throw new IllegalArgumentException(
                                "Groovy Init Error: " + e, e);
                    }
                }
            }
        }
        
        return _gse;
    }

    
    /**
     * @param scriptResult
     * @param vars
     * @return
     */
    @SuppressWarnings("unchecked")
    private MocaResults extractResults(Object scriptResult, _MocaBindings vars) {
        // Now look at the result.  There are three possibilities:
        // 1. MocaResults returned from the script.  This trumps everything else, and will
        //    override any other results that might otherwise have been produced.
        // 2. Variables produced in the script context.  Any variables that are created within
        //    the context of the script are made available downstream.  There is an exception for
        //    naming convention (_variable) which can inhibit the publishing of a given variable.
        // 3. If no variables are available, the results processing occurs much like in Java
        //    components.
        if (scriptResult instanceof MocaResults) {
            _logger.debug( "results returned, overriding script output");
        }
        else if (scriptResult instanceof Map) {
            Map<?, ?> resMap = (Map<?, ?>) scriptResult;
            SimpleResults out = new SimpleResults();
            out.addRow();
            for (Map.Entry<?, ?> entry : resMap.entrySet()) {
                String key = String.valueOf(entry.getKey());
                Object value = entry.getValue();
                MocaType colType = MocaType.forValue(value); 
                out.addColumn(key, colType);
                out.setValue(key, value);
            }
            scriptResult = out;
        }
        else {
            // If the script variables are used, they are flat, so we only need a single row.
            SimpleResults out = new SimpleResults();
            out.addRow();
            
            int columnsAdded = 0;
            
            // Look through the variables that got defined through the course of our 
            // script execution.  pull them out and push them into a results object.
            for(Object eObject : vars.getVariables().entrySet()) {
                Map.Entry<String, Object> e = (Map.Entry<String, Object>) eObject;
                String name = e.getKey();
                
                if (name.equals("moca") || name.startsWith("_") || !vars._locals.contains(name)) {
                    continue;
                }
   
                Object value = e.getValue();
                if (value != null) {
                    if (value instanceof GString) {
                        out.addColumn(name, MocaType.STRING);
                        out.setValue(name, value.toString());
                    }
                    else {
                        out.addColumn(name, MocaType.lookupClass(value.getClass()));
                        out.setValue(name, value);
                    }
                    columnsAdded++;
                }
                else {
                    out.addColumn(name, MocaType.STRING);
                    out.setNull(name);
                    columnsAdded++;
                }
            }
   
            // If no columns were added, process results as for Java components.
            if (columnsAdded == 0) {
                out.close();
            }
            else {
                scriptResult = out;
            }
        }
        
        if (scriptResult == null) {
            return new SimpleResults();
        }
        else {
            return ResultMapper.createResults(scriptResult);
        }
    }
    
    private String _determineClassName(Command cmd) {
        if (cmd == null) {
            return String.format("unknown/AdHocGroovyScript%05d.moca", _scriptCount++);
        }
        
        StringBuilder out = new StringBuilder();

        String pkgName = cmd.getLevel().getPackage();
        if (pkgName != null) {
            out.append(pkgName);
        }
        else {
            out.append(cmd.getLevel().getName().toLowerCase());
        }
        out.append('/');
        
        String name = cmd.getName();
        
        for (String word : name.split(" ")) {
            int wordLength = word.length();
            for (int i = 0; i < wordLength; i++) {
                out.append((i == 0) ? Character.toUpperCase(word.charAt(i)) : Character.toLowerCase(word.charAt(i)));
            }
        }
        
        out.append(String.format("%05d.moca", _scriptCount++));

        return out.toString();
    }

    private final Map<String, Class<? extends Script>> _scriptCache = new FixedSizeCache<String, Class<? extends Script>>(CACHE_SIZE);
    private volatile GroovyScriptEngine _gse;
    private final URL[] _paths;
    private int _scriptCount = 0;
    private static final Logger _logger = LogManager.getLogger(GroovyScriptAdapter.class);
    private final static String SCRIPT_PREFIX = "import com.redprairie.moca.*; import com.redprairie.moca.util.*; import com.redprairie.moca.exceptions.*;";
    private final AtomicInteger compileCount = new AtomicInteger(0);
    private final static int COMPILE_LIMIT = 400;
    private final static int CACHE_SIZE = 200;
}
