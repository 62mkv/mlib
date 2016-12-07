/*
 *  $URL$
 *  $Revision$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2005
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

package com.redprairie.moca.db.hibernate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.util.Properties;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.jmx.StatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redprairie.moca.DatabaseTool;
import com.redprairie.moca.MocaContext;
import com.redprairie.moca.MocaInterruptedException;
import com.redprairie.moca.MocaTrace;
import com.redprairie.moca.TransactionHook;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.util.MocaUtils;

/**
 * Tools to enable support of the hibernate object/relational mapping tool
 * for transparent object persistence.  Hibernate uses a MOCA-supplied JDBC
 * connection to perform all database operations.
 * 
 * <b><pre>
 * Copyright (c) 2006 RedPrairie Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author dinksett
 * @version $Revision$
 */
public class HibernateTools {
    public static final String SESSION_ATTRIBUTE_NAME = "com.redprairie.moca.hibernate.Session";

    /**
     * Default session factory -- uses MOCA's connection objects to back the hibernate
     * session. The session created with this method uses the default interceptor.
     * 
     * @param ctx
     * @return
     */
    public static Session getSession(MocaContext ctx) {
        return getSession(ctx, null);
    }
    
    /**
     * Session factory method that allows for an interceptor object to be passed.
     * The session created with this method will use the interceptor as passed
     * if the session was not previously created.
     * @param ctx
     * @param interceptor
     * @return
     */
    public static Session getSession(MocaContext ctx, Interceptor interceptor) {
        Session session = (Session) ctx.getTransactionAttribute(SESSION_ATTRIBUTE_NAME);
        if (session == null) {
            DatabaseTool db = ctx.getDb();
            SessionFactory factory = getSessionFactory(ctx);
            // We cannot use getCurrentSession, due to our use of locally scoped
            // interceptors.  The getCurrentSession method only works with
            // a globally scoped interceptor.
            Connection conn = db.getConnection();
            session = factory
                    .withOptions()
                        .connection(conn)
                        .interceptor(interceptor)
                    .openSession();
            ctx.setTransactionAttribute(SESSION_ATTRIBUTE_NAME, session);
            
            TransactionHook sessionHook = new _SessionFlushHook(session);
            ctx.addTransactionHook(sessionHook);
        }
        
        return session;
    }
    
    public static Session getSession(){
        return getSession(MocaUtils.currentContext());
    }
    
    synchronized
    public static SessionFactory getSessionFactory(final MocaContext ctx) {
        if (_factory == null) {
            Configuration cfg = new Configuration();
            
            // First, load up default properties stored in MOCA 
            Properties props = new Properties();
            try {
                _loadProperties(props, 
                        HibernateTools.class.getResourceAsStream(
                                "resources/hibernate.properties"));
            }
            catch (IOException e) {
                ctx.logWarning("Unable to load hibernate properties: " + e);
                _sqlLogger.debug("Unable to load hibernate properties", e);
            }
            
            // Next, default the database dialect, based on MOCA's idea
            // of the database type.
            String dbType = ctx.getDb().getDbType();
            
            if (dbType.equals("ORACLE")) {
                props.setProperty("hibernate.dialect",
                        "org.hibernate.dialect.Oracle10gDialect");
            }
            else if (dbType.equals("MSSQL")) {
                props.setProperty("hibernate.dialect",
                        "com.redprairie.moca.db.hibernate.UnicodeSQLServerDialect");
            }
            else if (dbType.equals("H2")) {
                props.setProperty("hibernate.dialect",
                        "org.hibernate.dialect.H2Dialect");
            }
            
            SystemContext systemContext = ServerUtils.globalContext();
            
            // We get the data files in reverse.
            File[] files = systemContext.getDataFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    if (name.equals("hibernate.properties")) {
                        return true;
                    }
                    return false;
                }
                
            }, true);

            // For each directory in the list, look for a
            // hibernate.properties file.
            for (File propFile : files) {
                if (propFile.canRead()) {
                    try {
                        ctx.trace(MocaTrace.SQL, "Loading Properties file: " + propFile);
                        _loadProperties(props, new FileInputStream(propFile));
                    }
                    catch (IOException e) {
                        ctx.logWarning("Unable to load properties " + propFile + ": " + e);
                        _sqlLogger.debug("Unable to load hibernate properties", e);
                    }
                    catch (HibernateException e) {
                        ctx.logWarning("Unable to load properties " + propFile + ": " + e);
                        _sqlLogger.debug("Unable to load hibernate properties", e);
                    }
                }
            }

            cfg.setProperties(props);
            
            // We get the data files in reverse.
            files = systemContext.getDataFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    if (name.equals("hibernate.cfg.xml")) {
                        return true;
                    }
                    return false;
                }
                
            }, true);
            
            // Now look for hibernate config files in each mappings directory.
            for (File xmlFile : files) {
                if (xmlFile.canRead()) {
                    try {
                        ctx.trace(MocaTrace.SQL, "Loading config file: " + xmlFile);
                        cfg.configure(xmlFile);
                    }
                    catch (HibernateException e) {
                        ctx.logWarning("Unable to load config file " + xmlFile + ": " + e);
                        _sqlLogger.debug("Unable to load hibernate config", e);
                    }
                }
            }
            
            _factory = cfg.buildSessionFactory();
            
            StatisticsService statsMBean = new StatisticsService();
            statsMBean.setSessionFactory(_factory);

            Exception excp = null;
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            try {
                mBeanServer.registerMBean(statsMBean, new ObjectName("Hibernate:application=Statistics"));
            }
            catch (InstanceAlreadyExistsException e) {
                excp = e;
            }
            catch (MBeanRegistrationException e) {
                excp = e;
            }
            catch (NotCompliantMBeanException e) {
                excp = e;
            }
            catch (MalformedObjectNameException e) {
                excp = e;
            }
            
            if (excp != null) {
                _sqlLogger.warn("Failed to export Hibernate Statistics "
                        + "Service.  Runtime statistics will not be viewable "
                        + "from MBeans!", excp);
            }
        }
        
        return _factory;
        
    }
    
    //
    // Implementation
    //
    
    private static class _SessionFlushHook implements TransactionHook {
        public void beforeCommit(MocaContext ctx) {
            _session.flush();
        }

        public void afterCompletion(MocaContext ctx, boolean committed) {
            _session.close();
            ctx.removeTransactionAttribute(SESSION_ATTRIBUTE_NAME);
        }

        private _SessionFlushHook(Session session) {
            _session = session;
        }

        final Session _session;

    }

    private static void _loadProperties(Properties props, InputStream str) throws IOException {
        if (str != null) {
            try {
                props.load(str);
            }
            catch (InterruptedIOException e) {
                throw new MocaInterruptedException(e);
            }
            finally {
                try { str.close(); }
                catch (IOException ignore) { }
            }
        }
    }

    private static Logger _sqlLogger = LoggerFactory.getLogger(
            "com.redprairie.moca.server.db.Sql");
    private static SessionFactory _factory;
}
