using System;
using System.Collections.Generic;
using RedPrairie.MOCA.Client.Interfaces;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Exceptions;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// This class implements a connection that is multi-threaded and queues
    /// asynchronous commands when all available connections are busy.
    /// </summary>
    internal class MultiThreadQueueConnection : ConnectionBase, IMocaConnection
    {
        #region Internal Fields

        //The max number of connections allowed - 0 is unlimited
        private int _maxConnections = 5;

        //The Connection queue
        private readonly Dictionary<string, IMocaConnection> _connectionPool =
            new Dictionary<string, IMocaConnection>(StringComparer.InvariantCultureIgnoreCase);

        //The connections that are currently available
        private readonly Queue<string> _availableConnections = new Queue<string>();

        //The queue of commands waiting to be run
        private readonly Queue<InternalCallBackEventArgs> _commandQueue = new Queue<InternalCallBackEventArgs>();

        //The lock object to maintain synchronization.
        private readonly object _lock;

        //A dedicated connection for normal execute commands to avoid deadlocks.
        private readonly SmartConnection _singleConnection;

        //Helps with garbage collection
        private bool _disposed;

        //The parent connection which would be used for asynchronous traces
        private FullConnection _fullConnection;

        #endregion

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="MultiThreadQueueConnection"/> class.
        /// </summary>
        public MultiThreadQueueConnection()
            : this("localhost", 4500, null)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="MultiThreadQueueConnection"/> class.
        /// </summary>
        /// <param name="host">The host.</param>
        /// <param name="port">The port.</param>
        public MultiThreadQueueConnection(string host, int port)
            : this(host, port, null)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="MultiThreadQueueConnection"/> class.
        /// </summary>
        /// <param name="host">The host.</param>
        /// <param name="port">The port.</param>
        /// <param name="environment">The environment.</param>
        public MultiThreadQueueConnection(string host, int port, Dictionary<string, string> environment)
        {
            _singleConnection = new SmartConnection(host, port, environment);
            _lock = new object();
            
            this.host = host;
            this.port = port;
            Environment = environment;
        }

        /// <summary>
        /// Releases unmanaged resources and performs other cleanup operations before the
        /// <see cref="MultiThreadQueueConnection"/> is reclaimed by garbage collection.
        /// </summary>
        ~MultiThreadQueueConnection()
        {
            DisposeInternal();
        }

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets the <see cref="IMocaConnection"/> objects of all trace enabled asynchronous connections.
        /// </summary>
        public Dictionary<string, IMocaConnection> AsyncConnections
        {
            get
            {
                lock (_lock)
                {
                    return _connectionPool;
                }
            }
        }

        /// <summary>
        /// Gets the number of inactive queue threads
        /// </summary>
        public int AvailableQueueThreads
        {
            get
            {
                lock (_lock)
                {
                    return _availableConnections.Count; 
                }
            }
        }

        /// <summary>
        /// Gets if the Manager is running in single connection mode
        /// </summary>
        public bool IsSingleMode
        {
            get { return _maxConnections == 1; }
        }

        /// <summary>
        /// Gets / Sets the max number of connections. 0 is unlimited The default
        /// value is 5.
        /// </summary>
        public int MaxConnections
        {
            get { return _maxConnections; }
            set { _maxConnections = (value < 0) ? 0 : value; }
        }

        /// <summary>
        /// Gets the number of active connection pool threads
        /// </summary>
        public int PoolConnectionCount
        {
            get
            {
                lock (_lock)
                {
                    return _connectionPool.Count; 
                }
            }
        }

        /// <summary>
        /// Gets the number of queued commands to be executed
        /// </summary>
        public int QueuedCommands
        {
            get
            {
                lock (_lock)
                {
                    return _commandQueue.Count; 
                }
            }
        }

        #endregion

        #region IMocaConnection Members

        /// <summary> 
        /// Set the autoCommit flag.  Normally, each command execution comprises a
        /// single transaction.  If the autocommit flag is set to false, multiple
        /// commands will be executed within the same transactional context. 
        /// Warning: disabling autoCommit can affect the health of the server
        /// framework, and should only be used for very short periods of time.
        /// </summary>
        public bool AutoCommit
        {
            get
            {
                bool isAutoCommit = _singleConnection.AutoCommit;

                foreach (KeyValuePair<string, IMocaConnection> pair in _connectionPool)
                {
                    isAutoCommit &= pair.Value.AutoCommit;
                }

                return isAutoCommit;
            }
            set
            {
                ManipulateConnectionPool(conn => conn.AutoCommit = value);
            }
        }
        
        /// <summary>
        /// Gets wither the connection is connected. In this case it ensures all
        /// connections are connected
        /// </summary>
        public bool Connected
        {
            get
            {
                bool isConnected = _singleConnection.Connected;

                foreach (KeyValuePair<string, IMocaConnection> pair in _connectionPool)
                {
                    isConnected &= pair.Value.Connected;
                }

                return isConnected;
            }
        }

        /// <summary> 
        /// Sets up the environment for this connection.  The environment String
        /// is used to pass client-specific information to server-side components.
        /// Certain environment entries are reserved for MOCA, and certain one
        /// are reserved for well-known application use.  
        /// </summary>
        public Dictionary<string, string> Environment
        {
            get { return env; }
            set
            {
                env = value;
                ManipulateConnectionPool(conn => conn.Environment = env);
            }
        }

        /// <summary>
        /// Gets a value indicating whether the connection supports arguments.
        /// </summary>
        /// <value><c>true</c> if the connection supports arguments; otherwise, <c>false</c>.</value>
        public bool SupportsArguments
        {
            get { return (_singleConnection != null) ? _singleConnection.SupportsArguments : false; }
        }

        /// <summary>
        /// Gets the <see cref="ServerInfo"/> object of the simple connection.
        /// </summary>
        public ServerInfo ServerInfo
        {
            get { return _singleConnection.ServerInfo; }
        }

        /// <summary>
        /// Gets a value indicating whether tracing is active.
        /// </summary>
        /// <value><c>true</c> if tracing is active; otherwise, <c>false</c>.</value>
        public bool TracingActive
        {
            get { return _singleConnection.TracingActive; }
        }

        /// <summary>
        /// Gets the Server Key.
        /// </summary>
        public string ServerKey
        {
            get { return _singleConnection.ServerKey; }
        }

        /// <summary>
        /// Begins the execution of a server command. This uses a callback
        /// to handle the results.
        /// </summary>
        /// <param name="callBack">Uses <see cref="ExecuteCallBack"/> object to do the callback</param>
        public void BeginExecuteCommand(InternalCallBackEventArgs callBack)
        {
            callBack.AddCallBack(OnAsyncCommandExecutionComplete);
            IMocaConnection conn = GetConnection();
            
            if (conn == null)
            {
                lock (_lock)
                {
                    _commandQueue.Enqueue(callBack); 
                }
            }
            else
            {
                conn.BeginExecuteCommand(callBack);
            }
        }

        /// <summary> 
        /// Closes the connection.  Any attempt to execute further commands on
        /// a connection that has been closed will fail.
        /// </summary>
        public override void Close()
        {
            ClearConnectionPool(false);
            base.Close();
        }

        /// <summary>
        /// Disables server tracing for the connection.
        /// </summary>
        public void DisableTracing()
        {
            _singleConnection.DisableTracing();
        }

        /// <summary>
        /// Enables tracing on the connection.
        /// </summary>
        public void EnableTracing()
        {
            _singleConnection.EnableTracing();
        }

        /// <summary>
        /// Enables tracing on the connection.
        /// </summary>
        /// <param name="filename">The filename to write the trace to.</param>
        /// <param name="traceLevels">The trace levels to use.</param>
        public void EnableTracing(string filename, string traceLevels)
        {
            _singleConnection.EnableTracing(filename, traceLevels);
        }

        /// <summary>
        /// Establishes a connection with the server
        /// </summary>
        public void Connect()
        {
            ManipulateConnectionPool(conn => conn.Connect());
        }

        /// <summary> 
        /// Execute a command, returning results from that command in a <see cref="ExecuteCommandResult"/>
        /// object.
        /// </summary>
        /// <param name="command">the text of the MOCA command sequence to execute.
        /// Execution will occur on the target system that this connection is
        /// associated with. 
        /// </param>
        /// <returns> a MocaResults object reprsenting the results of the command
        /// execution. </returns>
        /// <throws>  
        /// MocaException if an error occurs, either in communication with 
        /// the server, or upon execution of the command.
        /// </throws>
        public ExecuteCommandResult Execute(Command command)
        {
            return _singleConnection.Execute(command);
        }

        /// <summary> 
        /// Execute a command, returning results from that command in a <see cref="ExecuteCommandResult"/>
        /// object.
        /// </summary>
        /// <param name="command">the text of the MOCA command sequence to execute.
        /// Execution will occur on the target system that this connection is
        /// associated with. 
        /// </param>
        /// <param name="applicationID">The ID of the application that called the method</param>
        /// <returns> a MocaResults object reprsenting the results of the command
        /// execution. </returns>
        /// <throws>  
        /// MocaException if an error occurs, either in communication with 
        /// the server, or upon execution of the command.
        /// </throws>
        public ExecuteCommandResult Execute(Command command, string applicationID)
        {
            return _singleConnection.Execute(command, applicationID);
        }

        /// <summary>
        /// Execute a command, returning results from that command in an object specified by the
        /// data specified in the <paramref name="mappingData"/> object.
        /// </summary>
        /// <param name="command">the text of the MOCA command sequence to execute.
        /// Execution will occur on the target system that this connection is
        /// associated with.</param>
        /// <param name="applicationID">The ID of the application that called the method</param>
        /// <param name="mappingData">The object mapping data that provides information.</param>
        /// <param name="resolver">The object resolver that creates objects for the collection.</param>
        /// <returns>
        /// a MocaResults object reprsenting the results of the command
        /// execution.
        /// </returns>
        /// <throws>
        /// MocaException if an error occurs, either in communication with
        /// the server, or upon execution of the command.
        /// </throws>
        public object Execute(Command command, string applicationID, MappingData mappingData, IObjectResolver resolver)
        {
            return _singleConnection.Execute(command, applicationID, mappingData, resolver);
        }

        /// <summary>
        /// Initializes the connection with new information.
        /// </summary>
        /// <param name="newHost">The new host.</param>
        /// <param name="newPort">The new port.</param>
        /// <param name="newEnv">The new env.</param>
        public void Initialize(string newHost, int newPort, Dictionary<string, string> newEnv)
        {
            host = newHost;
            port = newPort;
            Environment = newEnv;

            ClearConnectionPool(false);
            ManipulateConnectionPool(conn => conn.Initialize(newHost, newPort, newEnv));
        }

        /// <summary>
        /// Reconnects with the server
        /// </summary>
        public void Reconnect()
        {
            ManipulateConnectionPool(conn => conn.Reconnect());
        }

        /// <summary>
        /// Updates an environment setting.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <param name="value">The value to replace it.</param>
        public override void UpdateEnvironmentSetting(string key, string value)
        {
            base.UpdateEnvironmentSetting(key, value);
            ManipulateConnectionPool(conn => conn.UpdateEnvironmentSetting(key, value));
        }

        /// <summary>
        /// Occurs when a connection related event occurs (connect, disconnect, trace enabled/disabled, etc.).
        /// </summary>
        /// <param name="sender">The sender.</param>
        /// <param name="e">The connection status.</param>
        internal void multiThreadConnection_ConnectionEventHandler(object sender, ConnectionEventArgs e)
        {
            switch (e.State)
            {
                case ConnectionEventState.TracingEnabled:
                    // bind the _fullConnection here
                    if (_fullConnection == null && sender != null && sender is FullConnection)
                        _fullConnection = (sender as FullConnection);
                    // reset all of the trace info when enable trace.
                    if (_connectionPool != null && _connectionPool.Count > 0)
                    {
                        foreach (KeyValuePair<string, IMocaConnection> conn in _connectionPool)
                        {
                            conn.Value.ServerInfo.CurrentTracePrfFileName = string.Empty;
                            conn.Value.ServerInfo.CurrentTraceFileName = string.Empty;
                            conn.Value.ServerInfo.CurrentTraceLevels = string.Empty;
                        }
                    }
                    break;
                case ConnectionEventState.TracingDisabled:
                    // bind the _fullConnection here
                    if (_fullConnection == null && sender != null && sender is FullConnection)
                        _fullConnection = (sender as FullConnection);
                    // disables server tracing for the asynchronous connections.
                    if (_connectionPool != null && _connectionPool.Count > 0)
                    {
                        // loop all the activated connections
                        foreach (KeyValuePair<string, IMocaConnection> conn in _connectionPool)
                        {
                            if (conn.Value.Connected && conn.Value.TracingActive)
                            {
                                conn.Value.DisableTracing();
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        #endregion

        #region IDisposable Members

        /// <summary>
        /// Disposes of the connection
        /// </summary>
        public void Dispose()
        {
            DisposeInternal();
            // This object will be cleaned up by the Dispose method.
            // Therefore, you should call GC.SupressFinalize to
            // take this object off the finalization queue
            // and prevent finalization code for this object
            // from executing a second time.
            GC.SuppressFinalize(this);
        }

        /// <summary>
        /// Dispose(bool disposing) executes in two distinct scenarios.
        /// If disposing equals true, the method has been called directly
        /// or indirectly by a user's code. Managed and unmanaged resources
        /// can be disposed.
        /// </summary>
        protected virtual void DisposeInternal()
        {
            // Check to see if Dispose has already been called.
            if (_disposed) 
                return;

            ClearConnectionPool(true);

            // Disposing has been done.
            _disposed = true;
        }

        #endregion

        #region Private Methods

        /// <summary>
        /// Clears the connection pool.
        /// </summary>
        /// <param name="dispose">if set to <c>true</c> dispose of the connections, otherwise close them.</param>
        private void ClearConnectionPool(bool dispose)
        {
            lock (_lock)
            {
                ManipulateConnectionPool(conn => conn.Close());
                
                if (dispose)
                {
                    _singleConnection.Dispose();
                }

                //Dispose of the pool as it will be cleared either way.
                foreach (var connection in _connectionPool)
                {
                    connection.Value.Dispose();
                }

                while (_commandQueue.Count > 0)
                {
                    var item = _commandQueue.Dequeue();
                    item.Command.Exception = new ConnectionFailedException("Connection aborted.");
                    item.InvokeCommandCallback(this);
                }
                
                _availableConnections.Clear();
                _connectionPool.Clear();
            }
        }

        /// <summary>
        /// Gets a connection from the connection pool. 
        /// Will create a new connection if the system isn't past it's thread count.
        /// </summary>
        ///  <returns>A <see cref="IMocaConnection"/> object or null if the pool is maxed</returns>
        private IMocaConnection GetConnection()
        {
            //Always try to grab a connection from the queue if available
            lock (_lock)
            {
                if (_availableConnections.Count > 0)
                {
                    //There's an available connection, use that
                    string queueConnectionID = _availableConnections.Dequeue();
                    
                    System.Diagnostics.Debug.Print("Available Connections:{0}, Connection:{1}",
                                                    _availableConnections.Count, queueConnectionID);

                    IMocaConnection conn = _connectionPool[queueConnectionID];
                    // enable trace for async call
                    if (_fullConnection != null && _fullConnection.TracingActive && !conn.TracingActive)
                    {
                        EnableTraceForAsyncCall(conn, _fullConnection);
                    }
                    return conn;
                }
                
                if (_connectionPool.Count < _maxConnections || _maxConnections == 0)
                {
                    //There's space in the pool, get a new connection
                    System.Diagnostics.Debug.Print("New Connection Created");
                    IMocaConnection conn = CreateConnection();
                    // enable trace for async call
                    if (_fullConnection != null && _fullConnection.TracingActive)
                    {
                        EnableTraceForAsyncCall(conn, _fullConnection);
                    }
                    return conn;
                }
                
                return null;
            }
        }

        /// <summary>
        /// Enable trace for asynchronous call.
        /// </summary>
        /// <param name="conn">The asynchronous connection.</param>
        /// <param name="fullConn">The parent connection.</param>
        private void EnableTraceForAsyncCall(IMocaConnection conn, FullConnection fullConn)
        {
            if (conn == null) return;

            if (_fullConnection != null && _fullConnection.ServerInfo != null)
            {
                conn.ServerInfo.CurrentTraceLevels = _fullConnection.ServerInfo.CurrentTraceLevels;

                string traceFileName =
                    GetAsyncTraceFileName(conn, _fullConnection.ServerInfo.CurrentTraceFileName);

                conn.ServerInfo.CurrentTraceFileName = traceFileName;
            }
            //_asyncTraceConnections.Add(conn.ConnectionID, conn);

            conn.EnableTracing(conn.ServerInfo.CurrentTraceFileName, conn.ServerInfo.CurrentTraceLevels);
        }

        /// <summary>
        /// Get the asynchronous trace file name based on the ConnectionID.
        /// "-Asynchronous_" and the first 8 characters of the ConnectionID
        /// would be appended to the the normal trace file name.
        /// For example, normal file name is set as 'SUPER.log'
        /// and the ConnectionID is 6964feb7-b77b-4e72-a7c9-b6adb146c048,
        /// then the final trace file name would be "SUPER-Asynchronous_6964feb7.log".
        /// </summary>
        /// <param name="conn">The current connection.</param>
        /// <param name="parentTraceFileName">The parent <see cref="FullConnection"/> object.</param>
        /// <returns>The asynchronous trace file name.</returns>
        private string GetAsyncTraceFileName(IMocaConnection conn, string parentTraceFileName)
        {
            if (conn == null) return parentTraceFileName;

            string traceFileName = parentTraceFileName;

            if (string.IsNullOrEmpty(traceFileName))
            {
                traceFileName = conn.ConnectionID;
            }
            else
            {
                traceFileName = System.IO.Path.GetFileNameWithoutExtension(traceFileName)
                    + "-Asynchronous_" + conn.ConnectionID.Substring(0, 8) 
                    + System.IO.Path.GetExtension(traceFileName);
            }

            return traceFileName;
        }

        /// <summary>
        /// Manipulates the connection pool.
        /// </summary>
        /// <param name="action">The action.</param>
        private void ManipulateConnectionPool(Action<IMocaConnection> action)
        {
            lock (_lock)
            {
                action(_singleConnection);

                foreach (KeyValuePair<string, IMocaConnection> pair in _connectionPool)
                {
                    action(pair.Value);
                } 
            }
        }

        /// <summary>
        /// Creates a new connection in the pool
        /// </summary>
        /// <returns>A new <see cref="IMocaConnection"/></returns>
        private IMocaConnection CreateConnection()
        {
            IMocaConnection newConnection = new SmartConnection(host, port, env);
            newConnection.Connect();
            
            lock (_lock)
            {
                newConnection.AutoCommit = _singleConnection.AutoCommit;
                _connectionPool.Add(newConnection.ConnectionID, newConnection); 
            }
            
            return newConnection;
        }

        /// <summary>
        /// Called when the async command execution is complete.
        /// </summary>
        /// <param name="conn">The sender moca connection.</param>
        /// <param name="e">The <see cref="InternalCallBackEventArgs"/> instance containing the event data.</param>
        private void OnAsyncCommandExecutionComplete(IMocaConnection conn, InternalCallBackEventArgs e)
        {
            lock (_lock)
            {
                //shut down this connection if the max number has been lowered
                if (_maxConnections != 0 && _maxConnections < _connectionPool.Count)
                {
                    string id = conn.ConnectionID;
                    conn.Dispose();
                    _connectionPool.Remove(id);
                }
                //Get the next command if necessary
                else if (_commandQueue.Count > 0)
                {
                    InternalCallBackEventArgs item = _commandQueue.Dequeue();
                    conn.BeginExecuteCommand(item);
                }
                else
                {
                    _availableConnections.Enqueue(conn.ConnectionID);
                }
            }

            e.InvokeCommandCallback(this);
        }
        #endregion
    }
}
