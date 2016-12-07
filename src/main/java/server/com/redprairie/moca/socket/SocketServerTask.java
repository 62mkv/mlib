/*
 *  $URL$
 *  $Author$
 *  $Date$
 *  
 *  $Copyright-Start$
 *
 *  Copyright (c) 2010
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

package com.redprairie.moca.socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.redprairie.moca.MocaContext;
import com.redprairie.moca.util.MocaUtils;
import com.redprairie.moca.util.Options;
import com.redprairie.moca.util.OptionsException;

/**
 * Task to run a socket server on some port.  The port and processing command are passed as "command line" arguments
 * to the task.
 * 
 * Copyright (c) 2010 RedPrairie Corporation
 * All Rights Reserved
 * 
 * @author derek
 */
public class SocketServerTask implements Runnable {

    public SocketServerTask(String[] args) throws OptionsException {
        Options opt = Options.parse("p:c:P:s", args);

        // Port
        if (!opt.isSet('p')) {
            throw new IllegalArgumentException("Port argument is required");
        }
        // Command
        if (!opt.isSet('c')) {
            throw new IllegalArgumentException("Command argument is required");
        }

        _command = opt.getArgument('c');
        _port = Integer.parseInt(opt.getArgument('p'));
        
        // Pool size
        if (opt.isSet('P')) {
            _poolSize = Integer.parseInt(opt.getArgument('P'));
        }
        else {
            _poolSize = 10;
        }
        
        // Whether child threads should generate unique session IDs
        if (opt.isSet('s')) {
            _useUniqueSessions = true;
        }
        else {
            _useUniqueSessions = false;
        }
    }

    @Override
    public void run() {
        MocaContext moca = MocaUtils.currentContext();
        String prefix = null;
        // If using unique session IDs we'll try to prefix the session
        // ID with the task name or this threads name if somehow we're not in a task
        if (_useUniqueSessions) {
            String taskId = moca.getSystemVariable("MOCA_TASK_ID");
            prefix = taskId != null ? "task-" + taskId : Thread.currentThread().getName();
            _log.debug("This SocketServerTask will generate unique sessions IDs with the prefix: {}", prefix);
        }
        
        SocketProcessorFactory factory = new CommandContextSocketProcessor(_command, _useUniqueSessions, prefix);
        try {
            _server = new GenericProtocolServer(_port, _poolSize);
            _server.start(factory);
        }
        catch (SocketProcessorException e) {
            _log.error("Error running server on port " + _port, e);
        }
    }
    
    /**
     * Gets the port the server is set to run on. If a random free port is specified by
     * using the port 0, this method will return 0 until {@link #run()}
     * is called where the port is then bound to an actual port number.
     * @return The port
     */
    public int getPort() {
        // If the server hasn't been started yet via run() then just get
        // the defined port, otherwise to support a random port (0) being specified
        // we have to query the actual GenericProtocolServer.
        if (_server == null) {
            return _port;
        }
        else {
            return _server.getPort();
        }
    }

    private GenericProtocolServer _server;
    private final int _port;
    private final String _command;
    private final int _poolSize;
    private final boolean _useUniqueSessions;
    private final static Logger _log = LogManager.getLogger(SocketServerTask.class); 
}
