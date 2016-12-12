using System;
using System.Collections.Generic;
using RedPrairie.MOCA.Client.Interfaces;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// This class is a combination of <see cref="DirectConnection"/> and <see cref="WebConnection"/>.
    /// It determines which connection to use based on the host name.
    /// </summary>
    internal class SmartConnection: IMocaConnection
    {
        #region Private Fields
        private IMocaConnection baseConnection;
        private bool disposed;
        private string _host;
        private int _port;
        #endregion

        #region Constructor / Deconstructor

        /// <summary> 
        /// Open a connetion to the MOCA service running on the given host and
        /// port.  The optional Environment structure represents a set of name/value
        /// pairs that represent client-side context.
        /// </summary>
        /// <param name="host">the host to connect to. </param>
        /// <param name="port">the port to connect to. </param>
        /// <param name="env">a mapping of names to values that represents the MOCA
        /// environment.  The passed variables are available to any called
        /// server-side components.  If this argument is passed as null, no
        /// environment entries will be passed.  </param>
        /// <throws>  <see cref="MocaException"/> if an error occurs connecting to the remote host.</throws>
        public SmartConnection(String host, int port, Dictionary<string, string> env)
        {
            SetupConnection(host, port, env);
        }

        /// <summary>
        /// Open a connetion to the MOCA service running on the given host and
        /// port.  No client-side context (environment) will be passed along.
        ///  </summary>
        /// <param name="host">the host to connect to. </param>
        /// <param name="port">the port to connect to.  </param>
        /// <throws>  <see cref="MocaException"/> if an error occurs connecting to the remote host.</throws>
        public SmartConnection(string host, int port)
            : this(host, port, null)
        {
        }

        /// <summary>
        /// Releases unmanaged resources and performs other cleanup operations before the
        /// <see cref="SmartConnection"/> is reclaimed by garbage collection.
        /// </summary>
        ~SmartConnection()
        {
            Dispose(false);
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
            get { return baseConnection.AutoCommit; }
            set { baseConnection.AutoCommit = value; }
        }

        /// <summary>
        /// Gets or Sets the application ID.
        /// </summary>
        public string ApplicationID
        {
            get { return baseConnection.ApplicationID; }
            set { baseConnection.ApplicationID = value; }
        }

        /// <summary>
        /// Gets or Sets the ID of the connection
        /// </summary>
        public string ConnectionID
        {
            get { return baseConnection.ConnectionID; }
            set { baseConnection.ConnectionID = value; }
        }

        /// <summary>
        /// Gets wither the connection is connected
        /// </summary>
        public bool Connected
        {
            get { return baseConnection.Connected; }
        }

        /// <summary> 
        /// Sets up the environment for this connection.  The environment String
        /// is used to pass client-specific information to server-side components.
        /// Certain environment entries are reserved for MOCA, and certain one
        /// are reserved for well-known application use.  
        /// </summary>
        public Dictionary<string, string> Environment
        {
            get { return baseConnection.Environment; }
            set { baseConnection.Environment = value; }
        }

        /// <summary>
        /// Gets a value indicating whether the connection supports arguments.
        /// </summary>
        /// <value><c>true</c> if the connection supports arguments; otherwise, <c>false</c>.</value>
        public bool SupportsArguments
        {
            get { return baseConnection.SupportsArguments; }
        }

        /// <summary>
        /// Gets the <see cref="ServerInfo"/> object of the simple connection.
        /// </summary>
        public ServerInfo ServerInfo
        {
            get { return baseConnection.ServerInfo; }
        }

        /// <summary>
        /// Gets a value indicating whether tracing is active.
        /// </summary>
        /// <value><c>true</c> if tracing is active; otherwise, <c>false</c>.</value>
        public bool TracingActive
        {
            get { return baseConnection.TracingActive; }
        }

        /// <summary>
        /// Gets the Server Key.
        /// </summary>
        public string ServerKey
        {
            get { return baseConnection.ServerKey; }
        }

        /// <summary>
        /// Begins the execution of a server command. This uses a callback
        /// to handle the results.
        /// </summary>
        /// Execution will occur on the target system that this connection is
        /// <param name="callBack">Uses <see cref="ExecuteCallBack"/> object to do the callback</param>
        public void BeginExecuteCommand(InternalCallBackEventArgs callBack)
        {
            baseConnection.BeginExecuteCommand(callBack);
        }

        /// <summary>
        /// Establishes a connection with the server
        /// </summary>
        public void Connect()
        {
            baseConnection.Connect();
        }

        /// <summary> 
        /// Closes the connection.  Any attempt to execute further commands on
        /// a connection that has been closed will fail.
        /// </summary>
        public void Close()
        {
            baseConnection.Close();
        }

        /// <summary>
        /// Disables server tracing for the connection.
        /// </summary>
        public void DisableTracing()
        {
            baseConnection.DisableTracing();
        }

        /// <summary>
        /// Enables tracing on the connection.
        /// </summary>
        public void EnableTracing()
        {
            baseConnection.EnableTracing();
        }

        /// <summary>
        /// Enables tracing on the connection.
        /// </summary>
        /// <param name="filename">The filename to write the trace to.</param>
        /// <param name="traceLevels">The trace levels to use.</param>
        public void EnableTracing(string filename, string traceLevels)
        {
            baseConnection.EnableTracing(filename, traceLevels);
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
            return baseConnection.Execute(command);
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
            return baseConnection.Execute(command, applicationID);
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
            return baseConnection.Execute(command, applicationID, mappingData, resolver);
        }

        /// <summary>
        /// Initializes a connection object with new information
        /// </summary>
        /// <param name="host">The host name</param>
        /// <param name="port">The port number</param>
        /// <param name="environment">The environment hash</param>
        public void Initialize(string host, int port, Dictionary<string, string> environment)
        {
            bool needWebConnection = ConnectionUtils.IsWebConnection(host);
            bool hasWebConnection = (baseConnection is WebConnection);

            if (baseConnection == null ||
                needWebConnection != hasWebConnection)
            {
                baseConnection.Dispose();

                if (needWebConnection == false && port <= 0)
                {
                    // We may not have a port in the newer versions.
                    ConnectionUtils.GetHostPortFromUrl(host, out host, out port);
                }
                
                baseConnection = needWebConnection
                                     ? new WebConnection(host, port, environment)
                                     : new DirectConnection(host, port, environment);
            } 
            baseConnection.Initialize(host, port, environment);
        }

        /// <summary>
        /// Reconnects with the server
        /// </summary>
        public void Reconnect()
        {
            baseConnection.Reconnect();
        }

        /// <summary>
        /// Updates an environment setting.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <param name="value">The value to replace it.</param>
        public void UpdateEnvironmentSetting(string key, string value)
        {
            baseConnection.UpdateEnvironmentSetting(key, value);
        }

        #endregion

        #region IDisposable Methods

        /// <summary>
        /// Disposes of the connection
        /// </summary>
        public void Dispose()
        {
            Dispose(true);
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
        /// <param name="disposing">If disposing equals false, the method has been called by the
        /// runtime from inside the finalizer and you should not reference
        /// other objects. Only unmanaged resources can be disposed.</param>
        protected virtual void Dispose(bool disposing)
        {
            // Check to see if Dispose has already been called.
            if (!disposed)
            {
                // If disposing equals true, dispose all managed
                // and unmanaged resources.
                if (disposing)
                {
                    if (baseConnection != null)
                        baseConnection.Dispose();
                }

                // Disposing has been done.
                disposed = true;
            }
        }

        #endregion

        #region Private Methods
        private void SetupConnection(string host, int port, Dictionary<string, string> env)
        {
            _host = host;
            _port = port;
            baseConnection = ConnectionUtils.IsWebConnection(host)
                                 ? new WebConnection(host, port, env)
                                 : new DirectConnection(host, port, env); 
        }
        #endregion
    }
}
