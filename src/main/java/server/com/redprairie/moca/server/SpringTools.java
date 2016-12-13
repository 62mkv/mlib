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

package com.redprairie.moca.server;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.util.SimpleFilenameFilter;

/**
 * This class contains some utility functions that can be used when dealing with
 * spring containers.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author wburns
 * @version $Revision$
 */
public class SpringTools {
    
    /**
     * This will retrieve the last bean of the specified type from the 
     * Bean Factory.  The returned value will be null if there is no bean of
     * that type in the factory.
     * @param <T>
     * @param beanFactory The bean factory to get beans from
     * @param type The class that we want the bean of
     * @return The last bean as found in the bean factory of the 
     *         specified type.  This will be null if no type exists
     */
    @SuppressWarnings("unchecked")
    public static <T>T getLastBeanOfType(ListableBeanFactory beanFactory, 
            Class<T> type) {
        T returnValue = null;
        String[] beanNames = beanFactory.getBeanNamesForType(type);

        if (beanNames.length != 0) {
            // Take the last bean name from the factory
            // This approach is faster then getting all beans of the type
            // and checking the last one of the map
            //
            // We can safely cast this since all the beans of this name should be
            // a type or subtype of T.
            returnValue = (T)beanFactory.getBean(beanNames[beanNames.length - 1]);
        }
        
        if (returnValue == null) {
            _logger.debug(MocaUtils.concat("No bean found for type: ", type));
        }
        else {
            _logger.debug(MocaUtils.concat("Found ", returnValue, 
                    " for bean type of ", type));
        }
        
        return returnValue; 
    }
    
    /**
     * This will first get all the data files of the specific name 
     * (ignoring case) and then will get the last bean of that type found in all
     * of those data files searching in the order they were found on
     * the data directory of the prod-dir setting.  If none is found this will 
     * return null.
     * @param <T> The type or interface to search for
     * @param ctx The SystemContext to check on
     * @param fileName The name of the file ignoring case
     * @param type The type or interface type to search for
     * @return The last object found defined in the configuration files or null
     *         if none was found.
     */
    public static <T>T getLastBeanStoredInDataFiles(SystemContext ctx, 
            final String fileName, Class<T> type) {
        
        ApplicationContext context = getContextUsingDataDirectories(ctx, fileName);

        if (context != null) {
            return getLastBeanOfType(context, type);
        }
        else {
            _logger.debug(MocaUtils.concat("No bean found for type: ", type));
            return null;
        }
    }
    
    /**
     * This method will return an ApplicationContext by first finding all xml
     * files that match the provided name by checking the data directories of
     * all of the products defined in the prod-dirs registry variable.  It will
     * then construct the context using those files as the beans to have.  If
     * no files are found for matches then null is returned.
     * @param fileName The file name to match against.
     * @return An application context if files were found, if none are found null
     *         is returned.
     */
    public static ConfigurableApplicationContext getContextUsingDataDirectories(
            final String fileName) {
        SystemContext ctx = ServerUtils.globalContext();
        
        return getContextUsingDataDirectories(ctx, fileName);
    }
    
    /**
     * This method will return a ConfigurableApplicationContext by first 
     * finding all xml files that match the provided name by checking the data 
     * directories of all of the products defined in the prod-dirs registry 
     * variable.  It will then construct the context using those files as the 
     * beans to have.  If no files are found for matches then null is returned.
     * @param fileName The file name to match against.
     * @return An application context if files were found, if none are found null
     *         is returned.
     */
    static ConfigurableApplicationContext getContextUsingDataDirectories(
            SystemContext ctx, final String fileName) {
        
        ConfigurableApplicationContext context = 
            (ConfigurableApplicationContext) ctx.getAttribute("appctx." + fileName);
        
        if (context == null) {
            
            String[] fileStrings = getFileNamesFromDataDirectories(ctx, 
                    new SimpleFilenameFilter(fileName));
            
            // If we have some files then create the context
            if (fileStrings != null) {
                context = new FileSystemXmlApplicationContext(
                        fileStrings);
                
                ctx.putAttribute("appctx." + fileName, context);
            }
        }
        
        return context;
    }
    
    /**
     * This will return an array of strings containing an entry for every file
     * that matches the name in the data directories.  This string will be
     * all ready for spring to be consumed in that the order will already be
     * inversed and such.
     * @param fileName The name of the file to find in the data directories
     * @return An array will all the file names or null if no files were found
     */
    public static String[] getFileNamesFromDataDirectories(final String fileName) {
        SystemContext ctx = ServerUtils.globalContext();
        
        return getFileNamesFromDataDirectories(ctx, 
                new SimpleFilenameFilter(fileName));
    }
    
    /**
     * This will return an array of strings containing an entry for every file
     * that matches the name in the data directories.  This string will be
     * all ready for spring to be consumed in that the order will already be
     * inversed and such.  This will use the specified SystemContext.
     * @param ctx The system context to look into
     * @param fileName The name of the file to match
     * @return An array containing all the strings for the file names or null
     *         if no matching files were found.
     */
    static String[] getFileNamesFromDataDirectories(SystemContext ctx, 
            FilenameFilter filter) {
        // Find any files defined in our configuration to see if they provided
        // an object of the type
        // We need it reversed since it is spring.
        File[] files = ctx.getDataFiles(filter, true);
        
        // If no files were found use the default of not looking up anything
        if (files.length > 0) {
            String[] fileStrings = new String[files.length];
            
            // Now loop through them prepending the file: before.
            for (int i = 0; i < files.length; ++i) {
                fileStrings[i] = "file:" + files[i].getAbsolutePath();
            }
            
            return fileStrings;
        }
        
        return null;
    }
    
    /**
     * Helper method to make an application context given a map of beans
     * @param namedSingletons Map of named singletons to add to the returned
     *        application context
     * @return The context with the given beans part of it
     */
    public static ApplicationContext getContextForPreinstantiatedSingletons(
        Map<String, ?> namedSingletons) {
        DefaultListableBeanFactory parentBeanFactory = 
                new DefaultListableBeanFactory();
        
        for (Entry<String, ?> entry : namedSingletons.entrySet()) {
            parentBeanFactory.registerSingleton(entry.getKey(), entry.getValue());
        }
        ConfigurableApplicationContext parent = new GenericApplicationContext(
            parentBeanFactory);
        parent.refresh();
        
        return parent;
    }
    
    /**
     * This is a utility method to help creating an 
     * {@link AnnotationConfigApplicationContext} with a parent context.  This
     * is needed because spring for whatever reason doesn't think the parent
     * constructor isn't needed.
     * <p>
     * https://jira.springsource.org/browse/SPR-7791
     * @param parent The parent application context to set on the context
     * @param classes The classes to use
     * @return The constructed context with the parent and used classes
     */
    public static AnnotationConfigApplicationContext getContextWithParent(
        ApplicationContext parent, Class<?>... classes) {
        // We have to use default constructor then refresh, since
        // spring guys never thought to add parent to constructor
        AnnotationConfigApplicationContext appCtx = 
                new AnnotationConfigApplicationContext();
        appCtx.setParent(parent);
        appCtx.register(classes);
        appCtx.refresh();
        
        return appCtx;
    }
    
    /**
     * This is a utility method to help add a Map of Singleton Neans to a parent
     * Application Context.
     * 
     * @param parent
     * @param beans
     * @return A new Application Context made from the Parent Context and the given Singletons.
     */
    public static ApplicationContext getContextWithParent(ApplicationContext parent, Map<String,?> beans) {
        GenericApplicationContext context = new GenericApplicationContext(parent);
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        
        // Iteratively add the beans to the new Application Context's Bean Factory.
        for (Entry<String,?> entry : beans.entrySet()) {
            beanFactory.registerSingleton(entry.getKey(), entry.getValue());
        }
        
        context.refresh();
        return context;
    }
    
    private static final Logger _logger = LogManager.getLogger(SpringTools.class);
}
