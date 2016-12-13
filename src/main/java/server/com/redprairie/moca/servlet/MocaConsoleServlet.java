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

package com.redprairie.moca.servlet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaConstants;
import com.redprairie.moca.MocaException;
import com.redprairie.moca.MocaRegistry;
import com.redprairie.moca.client.NotAuthorizedException;
import com.redprairie.moca.server.ServerContextFactory;
import com.redprairie.moca.server.ServerUtils;
import com.redprairie.moca.server.exec.LocalSessionContext;
import com.redprairie.moca.server.exec.RequestContext;
import com.redprairie.moca.server.exec.ServerContext;
import com.redprairie.moca.server.exec.SessionContext;
import com.redprairie.moca.server.exec.SessionType;
import com.redprairie.moca.server.exec.SystemContext;
import com.redprairie.moca.server.profile.CommandUsage;
import com.redprairie.moca.server.profile.CommandUsageStatistics;
import com.redprairie.moca.server.profile.ProfileUtils;
import com.redprairie.moca.server.session.SessionToken;
import com.redprairie.moca.servlet.support.SupportZip;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.moca.web.AbstractModel;
import com.redprairie.moca.web.WebResults;
import com.redprairie.moca.web.console.Authentication;
import com.redprairie.moca.web.console.Authentication.Role;
import com.redprairie.moca.web.console.ConsoleModel;
import com.redprairie.moca.web.console.MocaClusterAdministration;

/**
 * MOCA Console web servlet.
 * 
 * <b><pre>
 * Copyright (c) 2016 Sam Corporation
 * All Rights Reserved
 * </pre></b>
 * 
 * @author Matt Horner
 * @version $Revision$
 */
public class MocaConsoleServlet extends HttpServlet {
    public MocaConsoleServlet(ServerContextFactory factory, boolean hasDatabase) { 
        _logger.debug("Starting Console Servlet container");
        _factory = factory;
        _hasDatabase = hasDatabase;
    }
    
    // @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        _logger.debug("Received POST request from " + request.getLocalName());

        // Adding a header to avoid Compatibility issues with IE9.
        response.addHeader("X-UA-Compatible", "IE=edge");

        String queryStr = request.getQueryString();
        if (queryStr != null && queryStr.equals("m=login")) {
            WebResults<String> results = login(request, response);
            response.getWriter().append(results.toJsonString());
        }
        else if (queryStr != null && queryStr.equals("m=logout")) {
            WebResults<String> results = logout(request, response);
            response.getWriter().append(results.toJsonString());
        }
        else {
            dispatchRequest(request, response);
        }
    }
    
    // @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        _logger.debug("Received GET request from " + request.getLocalName());

        // Adding a header to avoid Compatibility issues with IE9.
        response.addHeader("X-UA-Compatible", "IE=edge");
        
        if (request.getServletPath().equals("/console/login.do")) {
            displayLoginPage(response);
        }
        else if (request.getServletPath().equals("/console/console.do")) {
            displayConsolePage(response);
        }
        else if (request.getServletPath().equals("/console/download")) {
            downloadLogFile(request, response);
        }
        else if (request.getServletPath().equals("/console/profile")) {
            downloadProfile(request, response);
        }
        else if (request.getServletPath().equals("/console/support")) {
            downloadSupport(request, response);
        }
        else {
            dispatchRequest(request, response);
        }
    }
    
    private String _readResourceFile(String fileLocation) throws IOException {
        SystemContext context = ServerUtils.globalContext();

        File page = new File(context.getVariable("MOCADIR") + fileLocation);
        StringBuilder pageContents = new StringBuilder();

        if (page.canRead()) {
            _logger.debug(String.format("Reading file contents %s from disk and caching", fileLocation));
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(page), "UTF-8"));
                
                String line = null;
                while ((line = reader.readLine()) != null) {
                    pageContents.append(line).append("\n");
                }
            }
            finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
        else {
            _logger.warn("Unable to read page contents: " + page);
        }
        return pageContents.toString();
    }
    
    /**
     * @param response
     * @throws IOException 
     */
    private void displayConsolePage(HttpServletResponse response) throws IOException {
        _logger.debug("Rendering console page...");
        
        String consolePageLocation = "/web/console/console.html";
        
        if (_consolePage == null) {
            String consolePage = _readResourceFile(consolePageLocation);
            _consolePage = consolePage;
        }      
        
        response.getWriter().write(_consolePage);        
    }

    /**
     * @param response
     * @throws MocaException 
     */
    private void displayLoginPage(HttpServletResponse response) throws IOException {
        _logger.debug("Rendering console login page");
        
        String loginPageLocation = "/web/console/login.html";
        
        if (_loginPage == null) {
            String loginPage = _readResourceFile(loginPageLocation);
            _loginPage = loginPage;
        }
        
        
        response.getWriter().write(_loginPage);
    }
    
    // @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        _admin = ServerUtils.globalAttribute(MocaClusterAdministration.class);
        
        _model = new ConsoleModel(_admin);
    }
    
    /**
     * Dispatch the mode of the request onto the proper handler.
     * @param request The HTTP request object. 
     * @param response The HTTP response object.
     * @throws IOException
     */
    private void dispatchRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String mode = request.getParameter("m");
        _logger.debug("Received console dispatch request: " + mode);

        // check the request and see if it's a valid dynamic content request,
        // if it is not, then redirect to the static content host.
        if (mode == null) {
            _logger.debug("Received empty mode on request, redirecting to static content");
            response.sendRedirect("/console/console.do");
            return; // return without further processing the modeless state.
        }
        
        Map<String, String> envMap = new HashMap<String, String>();
        envMap.put(MocaConstants.WEB_CLIENT_ADDR, request.getRemoteAddr());
        
        RequestContext requestContext = new RequestContext(envMap);
        SessionContext sessionContext = new LocalSessionContext("ConsoleRequest", 
            SessionType.CONSOLE);
        
        WebResults<?> results = new WebResults<Object>();
        SessionToken token = (SessionToken) request.getAttribute("moca.AuthToken");
        sessionContext.setSessionToken(token);
        
        ServerContext context = _factory.newContext(requestContext, sessionContext);
        
        // Set up the new context on this thread.
        ServerUtils.setCurrentContext(context);
    
        Map<String, String[]> parameters = request.getParameterMap();
        
        // Dispatch the request on to the model/method handling the request.
        Map<String, String> sessionHandlers = _model.publishHandlers();
        if (sessionHandlers.containsKey(mode)) {
            
            Role role = Authentication.getUserRole(token);
            if (role != null) {
                response.addHeader("CONSOLE-ROLE", role.toString());
            }
            String handler = sessionHandlers.get(mode);
            
            try {
                
                //Check for read only! Otherwise they have to have admin at this point.
                if(role == Role.CONSOLE_READ && !handler.startsWith("get")){
                    results.handleException(new SecurityException(handler + " is not allowed with READ only permissions."));
                } else {            
                    Method method = _model.getClass().getMethod(handler, Map.class);
                    results = (WebResults<?>) method.invoke(_model, parameters);
                }
            }
            catch (IllegalArgumentException e) {
                e.printStackTrace();
                results.handleException(e);
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
                results.handleException(e);
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
                results.handleException(e);
            }
            catch (SecurityException e) {
                e.printStackTrace();
                results.handleException(e);
            }
            catch (NoSuchMethodException e) {
                e.printStackTrace();
                results.handleException(e);
            }
            finally {
                context.close();
                ServerUtils.setCurrentContext(null);
            }
            response.getWriter().append(results.toJsonString());
        }  
    }

    private WebResults<String> login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        _logger.debug("Received console login request");

        // Get the password that the user provided.
        String username = request.getParameter("loginName");
        String password = request.getParameter("password");

        String sessionId = null;
        WebResults<String> results = new WebResults<String>();
        try {
            sessionId = Authentication.loginFromHttpRequest(request, ServerUtils.globalContext(),
                _factory, username, password, Authentication.RequestType.CONSOLE);
        }
        catch (NotAuthorizedException e) {
            _logger.debug("User has no console privileges.");
            results.add("NoAccess");
            return results;
        }
        catch (MocaException e) {
            _logger.warn("Unable to authenticate the user [{}] for console access, ended in error: {}", username, e.getMessage());
            results.add("NotAuthenticated");
            return results;
        }

        // If we've authenticated and we have the necessary 
        // role, we will set up the cookie used for the
        // remainder of the console requests.
        response.addHeader("CONSOLE-ROLE", Authentication.getUserRole(sessionId).toString());
        Authentication.setupCookie(sessionId, request, response);
        results.add("Authenticated");
        _logger.debug("Authentication of console session succeeded");

        return results;
    }

    private WebResults<String> logout(HttpServletRequest request,
                                      HttpServletResponse response)
            throws IOException {
        _logger.debug("Received console logout request");

        Authentication.handleLogout(request, response);
        WebResults<String> results = new WebResults<String>();
        results.add("NotAuthenticated");

        return results;
    }
    
    /**
     * Download a log file to the browser.
     * @param request The HTTP request object. 
     * @param response The HTTP response object.
     * @throws IOException
     */
    private void downloadLogFile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileList = request.getParameter("filename");

        _logger.debug("Received console download request: " + fileList);

        // If an invalid logfile name was passed, throw up.
        if (fileList == null) {
            throw new IOException("invalid request");
        }

        // Determine the log directory.  All file requests must be relative to the log directory.
        SystemContext sysContext = ServerUtils.globalContext();
        String lesDir = sysContext.getVariable("LESDIR");
        File logDir = new File(lesDir, "log");

        List<File> logFiles = new ArrayList<File>();
        
        // Go through the files and make sure they exist, and are relative to the current directory
        for (String filename : fileList.split(",")) {
            // Don't allow clandestine directory navigation using relative directories (e.g. ../src/...)
            if (filename.contains("..")) {
                throw new IOException("Invalid request: " + filename);
            }
            
            File logFile = new File(logDir, filename);
            
            if (logFile.exists() && !logFile.isDirectory()) {
                logFiles.add(logFile);
            }
        }
        
        // If an invalid logfile name was passed, throw up.
        if (logFiles.size() == 0) {
            throw new IOException("No valid files selected");
        }

        // Two modes: We can send a single file (using protocol compression), or
        // we can send a zip file (using zipfile compression. Either way, we
        // really want to compress log files as much as possible.
        else if (logFiles.size() == 1) {
            File file = logFiles.get(0);

            response.setContentType("text/plain");
            response.setHeader("Content-Disposition", "attachment;filename=\"" + file.getName() + "\"");
            response.setStatus(200);

            // If the browser support gzip or compression, use those tools to compress the file contents.
            String encodings = request.getHeader("Accept-Encoding");
            OutputStream out = null;
            FileInputStream inputStream = null;
            try {
                if (encodings != null && encodings.indexOf("gzip") != -1) {
                    // Go with GZIP
                    response.setHeader("Content-Encoding", "gzip");
                    out = new GZIPOutputStream(response.getOutputStream());
                }
                else if (encodings != null && encodings.indexOf("compress") != -1) {
                    // Go with ZIP
                    response.setHeader("Content-Encoding", "x-compress");
                    out = new ZipOutputStream(response.getOutputStream());
                    ((ZipOutputStream) out).putNextEntry(new ZipEntry("dummy name"));
                }
                else {
                    // No compression
                    out = response.getOutputStream();
                }
    
                // Copy the file contents to the output stream.
                inputStream = new FileInputStream(file);
                int count;
                byte[] bytes = new byte[1024];

                while ((count = inputStream.read(bytes)) != -1) {
                    out.write(bytes, 0, count);
                }
            }
            finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                }
                catch (IOException e) {
                    _logger.warn("Error closing zip output stream", e);
                }
                
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
                catch (IOException e) {
                    _logger.warn("Error closing file input stream", e);
                }
            }
        }
        else {
            // Multiple files: create a single zip file.
            String zipName = "logfiles.zip";
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment;filename=\"" + zipName + "\"");
            response.setStatus(200);

            ServletOutputStream outputStream = response.getOutputStream();

            // Use the default compression.
            ZipOutputStream zip = new ZipOutputStream(outputStream);
            try {
                for (File file : logFiles) {
                    FileInputStream inputStream = new FileInputStream(file);
    
                    try {
                        int count;
                        byte[] bytes = new byte[1024];
    
                        zip.putNextEntry(new ZipEntry(file.getName()));
    
                        while ((count = inputStream.read(bytes)) != -1) {
                            zip.write(bytes, 0, count);
                        }
    
                        zip.closeEntry();
                    }
                    finally {
                        inputStream.close();
                    }
                }
            }
            finally {
                zip.close();   
            }
        }
    }
    
    /**
     * Download a log file to the browser.
     * @param request The HTTP request object. 
     * @param response The HTTP response object.
     * @throws IOException
     */
    private void downloadProfile(HttpServletRequest request, HttpServletResponse response) throws IOException {
        _logger.debug("Received console profile download request");

        
        SystemContext context = ServerUtils.globalContext();
        ServerContextFactory factory = (ServerContextFactory)context.getAttribute(
                ServerContextFactory.class.getName());
        CommandUsage usage = factory.getCommandUsage();

        Collection<CommandUsageStatistics> stats = usage.getStats();
        
        if (stats == null) {
            response.setStatus(404);
        }
        else {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment;filename=\"profile-server.csv\"");
            response.setStatus(200);

            // If the browser support gzip or compression, use those tools to compress the file contents.
            String encodings = request.getHeader("Accept-Encoding");
            BufferedWriter out = null;
            try {
                if (encodings != null && encodings.indexOf("gzip") != -1) {
                    // Go with GZIP
                    response.setHeader("Content-Encoding", "gzip");
                    out = new BufferedWriter(new OutputStreamWriter(
                        new GZIPOutputStream(response.getOutputStream()),
                        Charset.forName("UTF-8")));
                }
                else if (encodings != null && encodings.indexOf("compress") != -1) {
                    // Go with ZIP
                    response.setHeader("Content-Encoding", "x-compress");
                    ZipOutputStream zip = new ZipOutputStream(response.getOutputStream());
                    zip.putNextEntry(new ZipEntry("dummy name"));
                    out = new BufferedWriter(new OutputStreamWriter(zip, Charset.forName("UTF-8")));
                }
                else {
                    // No compression
                    out = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), Charset.forName("UTF-8")));
                }
                
                ProfileUtils.writeUsage(stats, out);
            }
            finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }
    
    private void downloadSupport(HttpServletRequest request, 
        HttpServletResponse response) throws IOException {
        
        SystemContext system = ServerUtils.globalContext();
        
        Map<String, String> envMap = new HashMap<String, String>();
        envMap.put(MocaConstants.WEB_CLIENT_ADDR, request.getRemoteAddr());
        
        RequestContext requestContext = new RequestContext(envMap);
        SessionContext sessionContext = new LocalSessionContext("ConsoleRequest", 
            SessionType.CONSOLE);
        
        // Authenticate our session
        SessionToken sessionToken =  (SessionToken) request.getAttribute("moca.AuthToken");
        sessionContext.setSessionToken(sessionToken);
        
        ServerContext context = _factory.newContext(requestContext, sessionContext);
        
        // Set up the new context on this thread.
        ServerUtils.setCurrentContext(context);
        // Build the support zip file name
        String zipName = "support-" + system.getVariable("MOCA_ENVNAME") + "-"
                + MocaUtils.formatDate(new Date()) + ".zip";
        
        // Create a zip file that contains all the support stuff.
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + zipName + "\"");
        response.setStatus(200);

        ServletOutputStream outputStream = response.getOutputStream();
        
        // Continue on with the Moca Specific Support Files.
        try {
            SupportZip zip = new SupportZip(outputStream, _hasDatabase);
            zip.generateSupportZip();
        }
        catch(IOException e) {
            _logger.warn(e);
            e.printStackTrace();
            throw e;
        }
        finally {
            context.close();
            ServerUtils.setCurrentContext(null);
        }
    }
    
    private transient MocaClusterAdministration _admin;
    private transient AbstractModel _model;
    private static String _loginPage;
    private static String _consolePage;
    private final ServerContextFactory _factory;
    private final boolean _hasDatabase;
    private static final Logger _logger = LogManager.getLogger(MocaConsoleServlet.class);
    private static final long serialVersionUID = -3035357309479478299L;
}