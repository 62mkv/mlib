using System;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics;
using RedPrairie.MOCA.Client.Interfaces;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// Provides the equivelant of a MCSConnection with delegates for items
    /// that would be specific to MCS
    /// </summary>
    public partial class FullConnection : IMocaDataProvider
    {
        #region Events

        /// <summary>
        /// Occurs when digital signature challenge is returned from the server.
        /// It is suggested that the ConfirmDigitalSignature
        /// </summary>
        public event EventHandler<DigitalSignatureEventArgs> DigitalSignatureChallenge;

        /// <summary>
        /// Occurs when the server session or remote execution times out
        /// </summary>
        public event EventHandler<ServerTimeoutEventArgs> ServerTimeoutLogin;

        /// <summary>
        /// Occurs when a command is executed.
        /// </summary>
        public event EventHandler<ExecutionEventArgs> OnExecutionEvent;

        /// <summary>
        /// Occurs when user logs in or out
        /// </summary>
        public event EventHandler<UserEventArgs> OnUserEvent;

        /// <summary>
        /// Occurs when a connection related event occurs (connect, disconnect, etc.).
        /// </summary>
        public event EventHandler<ConnectionEventArgs> OnConnectionEvent;

        /// <summary>
        /// Occurs when a server environemt variable has changed.
        /// </summary>
        public event EventHandler<ServerEvnChangeArgs> OnServerEvnChange;

        #endregion

        #region Private Attributes

        /// <summary>
        /// The actual implimenting connection class
        /// </summary>
        private readonly IMocaConnection _connection;

        /// <summary>
        /// Indicates if the user is logged in
        /// </summary>
        protected bool _loggedIn;

        /// <summary>
        /// Holds login information
        /// </summary>
        protected LoginInfo _loginInfo;

        /// <summary>
        /// Holds a dictionary of known servers by their instance name
        /// </summary>
        protected Dictionary<string, ServerInfo> _knownServers;

        /// <summary>
        /// Holds a dictionary of invalid servers by their instance name
        /// </summary>
        protected Dictionary<string, Exception> _invalidServers =
            new Dictionary<string, Exception>();

        /// <summary>
        /// Holds the instance name of the default server
        /// </summary>
        protected string _defaultServer;

        /// <summary>
        /// Holds the <see cref="ServerInfo"/> object for the current connection
        /// </summary>
        protected ServerInfo _serverInfo;

        /// <summary>
        /// Indicates that tracing is active for the connection
        /// </summary>
        protected bool _tracingActive;

        #endregion

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="FullConnection"/> class.
        /// </summary>
        public FullConnection()
            : this("", "", 0, "")
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="FullConnection"/> class.
        /// </summary>
        /// <param name="host">The host.</param>
        /// <param name="port">The port.</param>
        public FullConnection(string host, int port)
            : this("", host, port, "")
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="FullConnection"/> class.
        /// </summary>
        /// <param name="host">The host.</param>
        /// <param name="port">The port.</param>
        /// <param name="environment">The environment string.</param>
        public FullConnection(string host, int port, string environment)
            : this("", host, port, environment)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="FullConnection"/> class.
        /// </summary>
        /// <param name="connectionName">Name of the connection in the config to load</param>
        /// <param name="host">The host.</param>
        /// <param name="port">The port.</param>
        public FullConnection(string connectionName, string host, int port)
            : this(connectionName, host, port, "")
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="FullConnection"/> class.
        /// </summary>
        /// <param name="connectionName">Name of the connection in the config to load</param>
        /// <param name="host">The host.</param>
        /// <param name="port">The port.</param>
        /// <param name="environment">The environment.</param>
        public FullConnection(string connectionName, string host, int port, string environment)
            : this(connectionName, host, port, environment, "SmartConnection")
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="FullConnection"/> class.
        /// </summary>
        /// <param name="connectionName">Name of the connection in the config to load</param>
        /// <param name="host">The host.</param>
        /// <param name="port">The port.</param>
        /// <param name="environment">The environment string.</param>
        /// <param name="connectionTypeName">Type of the connection to create. Must implement 
        /// <see cref="IMocaConnection"/> and contain a construtor with the signature (string host, int port, string env)</param>
        public FullConnection(string connectionName, string host, int port, string environment,
                              string connectionTypeName)
        {
            _knownServers = new Dictionary<string, ServerInfo>();
            _serverInfo = CreateNewServerInfo(connectionName, host, environment);
            
            _loginInfo = new LoginInfo();

            Exception exception = null;
            bool isLoaded = true;
            try
            {
                if (!connectionTypeName.Contains(",") && !connectionTypeName.StartsWith("RedPrairie"))
                {
                    connectionTypeName = string.Format("RedPrairie.MOCA.Client.{0}", connectionTypeName);
                }
                Type connectionType = Type.GetType(connectionTypeName);
                _connection = (IMocaConnection)Activator.CreateInstance(connectionType,
                                                                       host, port, ParseEnvironmentString(true));
                // subscribe the trace active event for asynchronous calls
                if (_connection is MultiThreadQueueConnection)
                {
                    OnConnectionEvent += (_connection as MultiThreadQueueConnection).multiThreadConnection_ConnectionEventHandler;
                }
            }
            catch (Exception ex)
            {
                isLoaded = false;
                exception = ex;
            }

            if (!isLoaded || _connection == null)
                throw new TypeLoadException(
                    string.Format("Could not create IMocaConnection from '{0}'\n" +
                                  "needs constructor with (string host, int port, string env)",
                                  connectionTypeName), exception);

            _connection.ConnectionID = connectionName;
        }

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets or Sets wither auto commit mode is enabled for the connection
        /// </summary>
        public bool AutoCommit
        {
            get { return _connection.AutoCommit; }
            set { _connection.AutoCommit = value; }
        }

        /// <summary>
        /// Gets a value indicating whether this <see cref="FullConnection"/> is connected.
        /// </summary>
        /// <value><c>true</c> if connected; otherwise, <c>false</c>.</value>
        public bool Connected
        {
            get { return _connection.Connected; }
        }

        /// <summary>
        /// Gets the name of the connection.
        /// </summary>
        /// <value>The name of the connection.</value>
        public string ConnectionName
        {
            [DebuggerStepThrough]
            get { return _connection.ConnectionID; }
        }

        /// <summary>
        /// Gets the default server.
        /// </summary>
        /// <value></value>
        public string DefaultServer
        {
            [DebuggerStepThrough]
            get { return _defaultServer; }
        }

        /// <summary>
        /// Gets the environment keys.
        /// </summary>
        /// <value></value>
        public ICollection<string> EnvironmentKeys
        {
            [DebuggerStepThrough]
            get { return _connection.Environment.Keys; }
        }

        /// <summary>
        /// Gets the invalid servers.
        /// </summary>
        /// <value></value>
        public Dictionary<string, Exception> InvalidServers
        {
            [DebuggerStepThrough]
            get { return _invalidServers; }
        }

        /// <summary>
        /// Gets the known servers.
        /// </summary>
        /// <value></value>
        public ICollection<ServerInfo> KnownServers
        {
            [DebuggerStepThrough]
            get { return _knownServers.Values; }
        }

        /// <summary>
        /// Gets or sets the locale ID.
        /// </summary>
        /// <value></value>
        public string LocaleID
        {
            [DebuggerStepThrough]
            get { return _loginInfo.LocaleID; }
            //set the connections locale
            set
            {
                _loginInfo.LocaleID = value;
                _connection.UpdateEnvironmentSetting(Constants.EnvironmentKeyLocaleID, value);
            }
        }

        /// <summary>
        /// Gets a value indicating whether the connection is logged in.
        /// </summary>
        /// <value><c>true</c> if logged in; otherwise, <c>false</c>.</value>
        public bool LoggedIn
        {
            [DebuggerStepThrough]
            get { return _loggedIn; }
        }

        /// <summary>
        /// Gets the login info.
        /// </summary>
        /// <value></value>
        public LoginInfo LoginInfo
        {
            [DebuggerStepThrough]
            get { return _loginInfo; }
        }

        /// <summary>
        /// Gets the server info.
        /// </summary>
        /// <value></value>
        public ServerInfo ServerInfo
        {
            [DebuggerStepThrough]
            get { return _serverInfo; }
        }

        /// <summary>
        /// Gets the name of the server.
        /// </summary>
        /// <value>The name of the server.</value>
        public string ServerName
        {
            [DebuggerStepThrough]
            get { return _serverInfo.Name; }
        }

        /// <summary>
        /// Gets a value indicating whether tracing is active.
        /// </summary>
        /// <value><c>true</c> if tracing is active; otherwise, <c>false</c>.</value>
        public bool TracingActive
        {
            [DebuggerStepThrough]
            get { return _tracingActive; }
        }

        /// <summary>
        /// Gets the trace file path.
        /// </summary>
        /// <value></value>
        public string TraceFilePath
        {
            [DebuggerStepThrough]
            get { return _serverInfo.CurrentTraceFileName; }
        }

        /// <summary>
        /// Gets the trace profile path.
        /// </summary>
        /// <value></value>
        public string TraceProfilePath
        {
            [DebuggerStepThrough]
            get { return _serverInfo.CurrentTracePrfFileName; }
        }

        /// <summary>
        /// Gets the user ID.
        /// </summary>
        /// <value></value>
        public string UserID
        {
            [DebuggerStepThrough]
            get { return _loginInfo.UserID; }
        }

        /// <summary>
        /// Gets or sets the warehouse ID.
        /// </summary>
        /// <value></value>
        public string WarehouseID
        {
            [DebuggerStepThrough]
            get
            {
                if (EnvironmentVariableExists(Constants.EnvironmentKeyWarehouse))
                {
                    return _connection.Environment[Constants.EnvironmentKeyWarehouse];
                }

                return string.Empty;
            }
            set
            {
                _connection.UpdateEnvironmentSetting(Constants.EnvironmentKeyWarehouse, value);

                if (OnServerEvnChange != null)
                    OnServerEvnChange(this,
                                      new ServerEvnChangeArgs(Constants.EnvironmentKeyWarehouse,
                                                              value));
            }
        }

        /// <summary>
        /// Gets the <see cref="ServerInfo"/> objects of all trace enabled asynchronous connections.
        /// </summary>
        public Dictionary<string, ServerInfo> AsyncTraceServerInfo
        {
            [DebuggerStepThrough]
            get
            {
                if (IsMultiThreadQueueConnection(_connection))
                {
                    var activatedTraceServerInfo =
                        new Dictionary<string, ServerInfo>(StringComparer.InvariantCultureIgnoreCase);

                    var multiConnection =
                        _connection as MultiThreadQueueConnection;

                    if (multiConnection.AsyncConnections.Count > 0)
                    {
                        // loop all the activated connections
                        foreach (KeyValuePair<string, IMocaConnection> conn in 
                                            multiConnection.AsyncConnections)
                        {
                            activatedTraceServerInfo.Add(conn.Value.ConnectionID, conn.Value.ServerInfo);
                        }
                    }

                    return activatedTraceServerInfo;
                }

                return null;
            }
        }

        #endregion

        #region Public Methods

        /// <summary>
        /// Closes the connection.
        /// </summary>
        public void Close()
        {
            if (_connection == null || !_connection.Connected) 
                return;
            
            if (_loggedIn)
            {
                LogOut();
            }

            if (TracingActive)
            {
                DisableTracing();
            }

            // unsubscribe the OnConnectionEvent event for asynchronous calls
            if (_connection is MultiThreadQueueConnection)
            {
                OnConnectionEvent -= (_connection as MultiThreadQueueConnection).multiThreadConnection_ConnectionEventHandler;
            }

            _connection.Close();
            GC.Collect(1);
        }

        /// <summary>
        /// Determines whether the specified environment variable key can be changed.
        /// </summary>
        /// <param name="variableKey">The variable key.</param>
        /// <returns>
        /// <c>true</c> if the specified environment key can change; otherwise, <c>false</c>.
        /// </returns>
        public static bool CanChangeEnvironmentVariable(string variableKey)
        {
            return ConnectionUtils.CanChangeEnvironmentVariable(variableKey);
        }

        /// <summary>
        /// Confirms the digital signature for the user and populates it on the list.
        /// </summary>
        /// <param name="username">The username.</param>
        /// <param name="password">The password.</param>
        /// <exception cref="LoginFailedException">Thrown when the user's name or password is invalid.</exception>
        /// <exception cref="LockOutException">Thrown when the account has been locked.</exception>
        /// <returns><c>true</c> if the signature confirmation is successful; otherwise <c>false</c>.</returns>
        public void ConfirmDigitalSignature(string username, string password)
        {
            LoginUtils.ConfirmDigitalSignature(_connection, username, password);
        }
        
        /// <summary>
        /// Execute a reauthentication login.
        /// </summary>
        /// <param name="username">The username.</param>
        /// <param name="password">The password.</param>
        /// <param name="isSingleSignon">Single sign-on?</param>
        /// <exception cref="LoginFailedException">Thrown when the user's name or password is invalid.</exception>
        /// <exception cref="LockOutException">Thrown when the account has been locked.</exception>
        /// <returns><c>true</c> if the reauthentication is successful; otherwise <c>false</c>.</returns>
        public bool ConfirmReAuthentication(string username, string password, bool isSingleSignon)
        {
            try
            {
                LoginUtils.Login(_connection, username, password, isSingleSignon, _loginInfo.LocaleID, true);
            }
            catch (LoginFailedException)
            {
                return false;
            }
            return true;
        }

        #region Connect Overload Methods

        /// <summary>
        /// Connects to the Server using settings from the constructor
        /// </summary>
        public void Connect()
        {
            _connection.Connect();
        }

        /// <summary>
        /// Connects the specified server name.
        /// </summary>
        /// <param name="serverName">Name of the server instance from the config</param>
        public void Connect(string serverName)
        {
            Connect(serverName, "", 0, "", false);
        }
        /// <summary>
        /// Connects the specified host.
        /// </summary>
        /// <param name="host">The host.</param>
        /// <param name="port">The port number.</param>
        public void Connect(string host, int port)
        {
            Connect("", host, port, "", false);
        }

        /// <summary>
        /// Connects the specified server name.
        /// </summary>
        /// <param name="serverName">Name of the server instance from the config</param>
        /// <param name="host">The host.</param>
        /// <param name="port">The port number.</param>
        public void Connect(string serverName, string host, int port)
        {
            Connect(serverName, host, port, "", false);
        }

        /// <summary>
        /// Connects the specified host.
        /// </summary>
        /// <param name="host">The host.</param>
        /// <param name="port">The port number.</param>
        /// <param name="environment">The environment string to connect with.</param>
        public void Connect(string host, int port, string environment)
        {
            Connect("", host, port, environment, false);
        }

        /// <summary>
        /// Connects the specified server name.
        /// </summary>
        /// <param name="serverName">Name of the server instance from the config</param>
        /// <param name="host">The host.</param>
        /// <param name="port">The port number.</param>
        /// <param name="environment">The environment string to connect with.</param>
        /// <param name="manualServer">if set to <c>true</c> the connection does not have a defined configuration.</param>
        public void Connect(string serverName, string host, int port, string environment,
                            bool manualServer)
        {
            ServerInfo info;

            if (!String.IsNullOrEmpty(serverName) && manualServer == false)
            {
                // Try to load servers
                if (_knownServers.Count == 0)
                    LoadKnownServers();


                // Get the server info.
                try
                {
                    info = GetServerInfo(serverName);
                    // Host and port may have been over ridden. In that case, create a new info structure
                    if ((host != null && info.Host != host) || (port >= 0 && info.Port != port))
                    {
                        //Try to match it to another known server or create a new set
                        info = GetServerInfo(host, port);
                    }
                }
                catch (Exception e)
                {
                    throw new ConnectionFailedException(
                        string.Format("Server Configuration for {0} not found in registry",
                                      serverName), e);
                }

                host = String.IsNullOrEmpty(host) ? info.Host : host;
                port = (port < 0) ? info.Port : port;
            }
            else if (port <=0 )
            {
                try
                {
                    info = CreateNewServerInfo(host, host, environment);                        
                }
                catch (ConnectionFailedException rpe)
                {
                    throw new ConnectionFailedException(
                        string.Format(
                            "Invalid Server Information provided.  Host = {0}", host
                            ), rpe);
                }
            }
            else
            {
                try
                {
                    info = CreateNewServerInfo(string.Format("{0}:{1}", host, port), host, port, environment);
                }
                catch (ConnectionFailedException rpe)
                {
                    throw new ConnectionFailedException(
                        string.Format(
                            "Invalid Server Information provided.  Host = {0}, Port = {1}", host,
                            port), rpe);
                }
            }


            //Close the connection if it is open
            if (_connection.Connected)
            {
                _connection.Close();
            }

            _connection.Initialize(host, port,
                                  ConnectionUtils.ParseEnvironmentString(
                                      !String.IsNullOrEmpty(environment)
                                          ? environment
                                          : info.Environment));
            //Connect
            _connection.Connect();

            info.Host = host;
            info.Port = port;
            _serverInfo = info;

            if (OnConnectionEvent != null)
            {
                OnConnectionEvent(this,
                                  new ConnectionEventArgs(ConnectionEventState.Connected, info));
            }
        }

        #endregion

        /// <summary>
        /// Creates a new server info class.
        /// </summary>
        /// <param name="name">The local server name.</param>
        /// <param name="host">The host name.</param>
        /// <param name="port">The port number.</param>
        /// <returns>A new <see cref="ServerInfo"/> class</returns>
        public virtual ServerInfo CreateNewServerInfo(string name, string host, int port)
        {
            return new ServerInfo(name, host, port);
        }

        /// <summary>
        /// Creates a new server info class.
        /// </summary>
        /// <param name="name">The local server name.</param>
        /// <param name="host">The host name.</param>
        /// <param name="port">The port number.</param>
        /// <param name="env">An environment string.</param>
        /// <returns>A new <see cref="ServerInfo"/> class</returns>
        public virtual ServerInfo CreateNewServerInfo(string name, string host, int port, String env)
        {
            return new ServerInfo(name, host, port, env);
        }

        /// <summary>
        /// Creates a new server info class.
        /// </summary>
        /// <param name="name">The local server name.</param>
        /// <param name="host">The host name.</param>        
        /// <param name="env">An Environment String.</param>
        /// <returns>A new <see cref="ServerInfo"/> class</returns>
        public virtual ServerInfo CreateNewServerInfo(string name, string host, String env)
        {
            return new ServerInfo(name, host, env);
        }

        /// <summary>
        /// Disables server tracing for the connection.
        /// </summary>
        public void DisableTracing()
        {
            const string strCmd = "set trace where activate = 0 ";
            ExecuteCommandResult result = _connection.Execute(new Command(strCmd));

            if (!result.IsOK)
            {
                throw new MocaException(result.StatusCode,
                                        string.Format("Error disabling server tracing: {0}",
                                                      result.StatusCode));
            }

            _tracingActive = false;
            if (OnConnectionEvent != null)
            {
                OnConnectionEvent(this,
                                  new ConnectionEventArgs(ConnectionEventState.TracingDisabled,
                                                          _serverInfo));
            }
        }

        /// <summary>
        /// Enables tracing on the connection.
        /// </summary>
        public void EnableTracing()
        {
            EnableTracing(_serverInfo.TraceFileName, _serverInfo.TraceLevels);
        }

        /// <summary>
        /// Enables tracing on the connection.
        /// </summary>
        /// <param name="filename">The filename to write the trace to.</param>
        /// <param name="traceLevels">The trace levels to use.</param>
        public void EnableTracing(string filename, string traceLevels)
        {
            if (_tracingActive) return;

            // Get the current trace file full path
            string strCmd = string.Format(
                "set trace " +
                " where activate = 1 " +
                " and level = \"{0}\" " +
                " and filename = \"{1}\" " +
                " and mode = \"w\" ",
                traceLevels, filename);

            ExecuteCommandResult result = _connection.Execute(new Command(strCmd));

            if (!result.IsOK)
            {
                throw new MocaException(result.StatusCode,
                                        string.Format("Error enabling tracing: {0}",
                                                      result.StatusCode));
            }

            _serverInfo.CurrentTraceFileName =
                ConnectionUtils.GetStringValue(result.TableData.Rows[0], "filename");
            _serverInfo.CurrentTracePrfFileName =
                ConnectionUtils.GetStringValue(result.TableData.Rows[0], "prf_filename");

            // Disable trace, otherwise won't be able to remove the file.
            strCmd = "set trace where activate = 0 ";
            _connection.Execute(new Command(strCmd));

            // Now remove both the original trace files, ignore the return status errors
            strCmd =
                string.Format("remove file where filename = '{0}'", _serverInfo.CurrentTraceFileName);
            _connection.Execute(new Command(strCmd));
            strCmd =
                string.Format("remove file where filename = '{0}'",
                              _serverInfo.CurrentTracePrfFileName);
            _connection.Execute(new Command(strCmd));

            // Reopen the trace file in append mode
            strCmd = string.Format(
                "set trace " +
                " where activate = 1 " +
                " and level = \"{0}\" " +
                " and filename = \"{1}\" " +
                " and mode = \"a+\" ",
                traceLevels, filename);

            result = _connection.Execute(new Command(strCmd));

            if (!result.IsOK)
            {
                throw new MocaException(result.StatusCode,
                                        string.Format("Error enabling tracing: {0}",
                                                      result.StatusCode));
            }

            _serverInfo.CurrentTraceFileName =
                ConnectionUtils.GetStringValue(result.TableData.Rows[0], "filename");
            _serverInfo.CurrentTracePrfFileName =
                ConnectionUtils.GetStringValue(result.TableData.Rows[0], "prf_filename");
            _serverInfo.CurrentTraceFileName = filename;
            _serverInfo.CurrentTraceLevels = traceLevels;

            _tracingActive = true;

            if (OnConnectionEvent != null)
            {
                OnConnectionEvent(this,
                                  new ConnectionEventArgs(ConnectionEventState.TracingEnabled,
                                                          _serverInfo));
            }
        }

        /// <summary>
        /// Indicates if an environments variable exists.
        /// </summary>
        /// <param name="variableName">Name of the variable.</param>
        /// <returns><c>true</c> if it exists; otherwise <c>false</c>.</returns>
        public bool EnvironmentVariableExists(string variableName)
        {
            return _connection.Environment.ContainsKey(variableName);
        }

        #region Execute Overload Methods

        /// <summary>
        /// Executes the specified MOCA command.
        /// </summary>
        /// <param name="command">The command to execute</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        public int Execute(string command)
        {
            return Execute(command, null);
        }

        /// <summary>
        /// Executes the specified command.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="applicationID">The application ID.</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        public int Execute(string command, string applicationID)
        {
            ExecuteCommandResult result = InternalExecuteResults(command, applicationID);            
            return result.StatusCode;
        }

        /// <summary>
        /// Executes the specified command.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="data">The <see cref="DataSet"/> of data that was returned</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        public int Execute(string command, ref DataSet data)
        {
            return Execute(command, null, ref data);
        }

        /// <summary>
        /// Executes the specified command.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="applicationID">The application ID.</param>
        /// <param name="data">The <see cref="DataSet"/> of data that was returned</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        public int Execute(string command, string applicationID, ref DataSet data)
        {
            ExecuteCommandResult result = InternalExecuteResults(command, applicationID);
            data = result.Data;
            return result.StatusCode;
        }

        /// <summary>
        /// Executes the specified command.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="data">The <see cref="DataSet"/> of data that was returned</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        public int Execute(string command, out DataTable data)
        {
            return Execute(command, null, out data);
        }

        /// <summary>
        /// Executes the specified command.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="applicationID">The application ID.</param>
        /// <param name="data">The <see cref="DataSet"/> of data that was returned</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        public int Execute(string command, string applicationID, out DataTable data)
        {
            ExecuteCommandResult result = InternalExecuteResults(command, applicationID);
            data = result.TableData;
            data.TableName = result.TableData.TableName;
            return result.StatusCode;
        }

        /// <summary>
        /// Executes the specified command.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="dataView">The <see cref="DataView"/> of data that was returned</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        public int Execute(string command, out DataView dataView)
        {
            return Execute(command, null, out dataView);
        }

        /// <summary>
        /// Executes the specified command.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="applicationID">The application ID.</param>
        /// <param name="dataView">The <see cref="DataView"/> of data that was returned</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        public int Execute(string command, string applicationID, out DataView dataView)
        {
            ExecuteCommandResult result = InternalExecuteResults(command, applicationID);
            dataView = result.ViewData;
            return result.StatusCode;
        }

        /// <summary>
        /// Begins the Execution of a command. The async call cannot raise
        /// the pre and post execute events
        /// </summary>
        /// <param name="command">The command to execute</param>
        /// <param name="callBack">The call back once complete</param>
        public void BeginExecute(string command, ExecuteCallBack callBack)
        {
            BeginExecute(command, null, callBack);
        }

        /// <summary>
        /// Begins the Execution of a command. The async call cannot raise
        /// the pre and post execute events
        /// </summary>
        /// <param name="command">The command to execute</param>
        /// <param name="applicationID">The application ID</param>
        /// <param name="callBack">The call back once complete</param>
        public void BeginExecute(string command, string applicationID, ExecuteCallBack callBack)
        {
            _connection.BeginExecuteCommand(new InternalCallBackEventArgs(
                            new InternalCommand(new Command(command), applicationID, callBack),
                            InternalCommandCompleteCallBack));
        }

        /// <summary>
        /// Executes the specified MOCA command using <see cref="ExecuteCommandResult"/> to return the data.
        /// </summary>
        /// <param name="command">The command to execute</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        public ExecuteCommandResult ExecuteResults(string command)
        {
            return ExecuteResults(command, null);
        }

        /// <summary>
        /// Executes the specified command using <see cref="ExecuteCommandResult"/> to return the data.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="applicationID">The application ID.</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        public ExecuteCommandResult ExecuteResults(string command, string applicationID)
        {
            return InternalExecuteResults(command, applicationID);
        }

        #endregion

        /// <summary>
        /// Gets the environment variable value for the specified key.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <returns>A string of the result or an empty string</returns>
        public string GetEnvironmentVariable(string key)
        {
            if (EnvironmentVariableExists(key))
                return _connection.Environment[key];
            
            return string.Empty;
        }

        /// <summary>
        /// Gets the <see cref="ServerInfo"/> for a server.
        /// </summary>
        /// <param name="serverName">Name of the server.</param>
        /// <returns>The <see cref="ServerInfo"/> otherwise null</returns>
        public ServerInfo GetServerInfo(string serverName)
        {
            if (_knownServers.ContainsKey(serverName))
                return _knownServers[serverName];
            
            return CreateNewServerInfo(serverName, "", 0);
        }

        /// <summary>
        /// Gets the <see cref="ServerInfo"/> for a server by host and port.
        /// </summary>
        /// <param name="host">The hostname of the server.</param>
        /// <param name="port">The port of the server.</param>
        /// <returns>The <see cref="ServerInfo"/>; otherwise <c>null</c>.</returns>
        public ServerInfo GetServerInfo(string host, int port)
        {
            foreach (KeyValuePair<string, ServerInfo> server in _knownServers)
            {
                if (server.Value.Host == host && server.Value.Port == port)
                    return server.Value;
            }
            string name = ConnectionUtils.IsWebConnection(host) ? host : string.Format("{0}:{1}", host, port);
            return CreateNewServerInfo(name, host, port);
            
        }

        /// <summary>
        /// Loads the known servers from a configuration. The base class simply
        /// clears out the holders for these variables.
        /// </summary>
        public virtual void LoadKnownServers()
        {
            // Clear our existing server list (if any)
            _knownServers.Clear();
            _invalidServers.Clear();
            _defaultServer = "";
        }

        /// <summary>
        /// Logins the specified username to the server.
        /// </summary>
        /// <param name="username">The username.</param>
        /// <param name="password">The password.</param>
        /// <returns><c>true</c> if successful, <c>false</c> otherwise</returns>
        public bool Login(string username, string password)
        {
            return Login(username, password, false);
        }
        
        /// <summary>
        /// Logins the specified username to the server.
        /// </summary>
        /// <param name="username">The username.</param>
        /// <param name="password">The password.</param>
        /// <param name="clientKey">The client key.</param>
        /// <returns><c>true</c> if successful, <c>false</c> otherwise</returns>
        public bool Login(string username, string password, string clientKey)
        {
            return Login(username, password, false, clientKey);
        }

        /// <summary>
        /// Logs the currently logged in user out.
        /// </summary>
        public void LogOut()
        {
            if (OnUserEvent != null)
                OnUserEvent(this, new UserEventArgs(UserEventState.UserLoggingOut, _loginInfo));

            LoginUtils.Logout(_connection);
           
            _loggedIn = false;
            _loginInfo = new LoginInfo();
            
            if (OnUserEvent != null)
                OnUserEvent(this, new UserEventArgs(UserEventState.UserLoggedOut, _loginInfo));
        }

        /// <summary>
        /// Sets the environment variable value for the specified key.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <param name="value">The value.</param>
        public void SetEnvironmentVariable(string key, string value)
        {
            if (CanChangeEnvironmentVariable(key))
            {
                _connection.UpdateEnvironmentSetting(key, value);

                if (OnServerEvnChange != null)
                    OnServerEvnChange(this, new ServerEvnChangeArgs(key, value));
            }
        }

        /// <summary>
        /// Logins the specified username to the server using the SingleSignon delegate
        /// </summary>
        /// <param name="username">The username to verify</param>
        /// <returns><c>true</c> if successful, <c>false</c> otherwise</returns>
        public bool SingleSignonLogin(string username)
        {
            return Login(username, "", true);
        }

        #endregion

        #region Private Methods

        /// <summary>
        /// Checks for a digital signature challenge and raises the appropriate events.
        /// </summary>
        /// <param name="e">The potential exception of the command. This may be <c>null</c> if the command completed successfully.</param>
        /// <returns><c>true</c> if the command needs to be re-executed; otherwise <c>false</c>.</returns>
        private bool CheckForDigitalSignatureChallenge(MocaException e)
        {
            if (e != null &&
                (e.ErrorCode == MocaErrors.eMCS_DIGITAL_SIGNATURE_CHALLENGE ||
                 e.ErrorCode == MocaErrors.eMCS_DIGITAL_SIGNATURE_CHALLENGE_WITH_OVERRIDE) &&
                 DigitalSignatureChallenge != null)
            {
                //Check for custom error message
                string customError = null;

                if (e.Results != null && e.Results.Tables.Count > 0 &&
                    e.Results.Tables[0].Columns.Count > 0 && e.Results.Tables[0].Rows.Count > 0)
                    customError = e.Results.Tables[0].Rows[0][0].ToString();
                
                //Raise the event for the client to handle
                DigitalSignatureEventArgs args = new DigitalSignatureEventArgs(e.ErrorCode ==
                                                  MocaErrors.eMCS_DIGITAL_SIGNATURE_CHALLENGE_WITH_OVERRIDE,
                                                  customError);
                args.Cancel = false;
                DigitalSignatureChallenge(this, args);

                //If the event is successful and there is a key in the environment, re-run the command.
                if (!args.Cancel && EnvironmentVariableExists(Constants.EnvironmentKeySignatureKey))
                {
                    return true;
                }
            }
            else if (EnvironmentVariableExists(Constants.EnvironmentKeySignatureKey))
            {
                string signatureKey = GetEnvironmentVariable(Constants.EnvironmentKeySignatureKey);
                if (signatureKey.EndsWith("#"))
                    _connection.UpdateEnvironmentSetting(Constants.EnvironmentKeySignatureKey, null);
            }

            return false;
        }

        /// <summary>
        /// Checks Server session or remote call timeout.
        /// </summary>
        /// <param name="e">The potential exception of the command. This may be <c>null</c> if the command completed successfully.</param>
        /// <returns><c>true</c> if the command needs to be re-executed; otherwise <c>false</c>.</returns>
        private bool CheckForServerTimeout(MocaException e)
        {
            if (e != null &&
                e.ErrorCode == MocaErrors.eSRV_AUTHENTICATE &&
                 ServerTimeoutLogin != null)
            {
                //Raise the event for the client to handle
                ServerTimeoutEventArgs args = new ServerTimeoutEventArgs();
                args.Cancel = false;
                ServerTimeoutLogin(this, args);

                //If the event is successful and there is a key in the environment, re-run the command.
                if (!args.Cancel)
                {
                    return true;
                }
            }

            return false;
        }

        /// <summary>
        /// The internal command execution method that raises the events and handles the appropriate scenarios
        /// </summary>
        /// <param name="command">The command to exute.</param>
        /// <param name="applicationID">The application ID.</param>
        /// <returns></returns>
        private ExecuteCommandResult InternalExecuteResults(string command, string applicationID)
        {
            return InternalExecuteResults(command, applicationID, true);
        }

        /// <summary>
        /// The internal command execution method that raises the events and handles the appropriate scenarios
        /// </summary>
        /// <param name="command">The command to exute.</param>
        /// <param name="applicationID">The application ID.</param>
        /// <param name="raiseEvents">if set to <c>true</c> raise command execution events.</param>
        /// <returns></returns>
        private ExecuteCommandResult InternalExecuteResults(string command, string applicationID, bool raiseEvents)
        {
            if (raiseEvents)
                RaisePreExecution(ref command);

            ExecuteCommandResult result = _connection.Execute(new Command(command), applicationID);

            if (CheckForDigitalSignatureChallenge(result.Error))
            {
                result = InternalExecuteResults(command, applicationID, false);
            }

            if (CheckForServerTimeout(result.Error))
            {
                result = InternalExecuteResults(command, applicationID, false);
            }

            if (raiseEvents)
                RaiseExecutionComplete(command, result.StatusCode, result.Data,
                                   result.Error != null ? result.Error.Message : string.Empty);

            return result;
        }

        /// <summary>
        /// An internal caller that is run on a command completion before the actual caller is reuturned
        /// </summary>
        /// <param name="sender">The sender.</param>
        /// <param name="e">The command execution results.</param>
        private void InternalCommandCompleteCallBack(IMocaConnection sender, InternalCallBackEventArgs e)
        {
            if (CheckForDigitalSignatureChallenge(e.Command.Exception))
            {
                //re-execute the command after adding this post hook on the call stack
                e.AddCallBack(InternalCommandCompleteCallBack);
                _connection.BeginExecuteCommand(e);
                return;
            }

            if (CheckForServerTimeout(e.Command.Exception))
            {
                //re-execute the command after adding this post hook on the call stack
                _connection.BeginExecuteCommand(e);
                return;
            }

            e.InvokeCommandCallback(sender);
        }

        /// <summary>
        /// The Internal Login method
        /// </summary>
        /// <param name="username">The username.</param>
        /// <param name="password">The password.</param>
        /// <param name="singleSignon">if set to <c>true</c> [single signon].</param>
        /// <returns></returns>
        private bool Login(string username, string password, bool singleSignon)
        {
            return Login(username, password, singleSignon, "");
        }

        /// <summary>
        /// The Internal Login method
        /// </summary>
        /// <param name="username">The username.</param>
        /// <param name="password">The password.</param>
        /// <param name="singleSignon">if set to <c>true</c> [single signon].</param>
        /// <param name="clientKey">The cleint key</param>
        /// <returns></returns>
        private bool Login(string username, string password, bool singleSignon, string clientKey)
        {

            if (_connection.Connected == false)
                throw new ConnectionFailedException("Connection not active.  Cannot log in.");

            if (OnUserEvent != null)
                OnUserEvent(this, new UserEventArgs(UserEventState.UserLoggingIn, _loginInfo));

            try
            {
                DataTable dtLogin =
                    LoginUtils.Login(_connection, username, password, singleSignon,
                                          _loginInfo.LocaleID, true, clientKey);

                //update login info
                _loginInfo = new LoginInfo(dtLogin, _loginInfo.LocaleID);

                //update server info
                _serverInfo.UserID = _loginInfo.UserID;
                try
                {
                    ServerTypes serverType = (ServerTypes) Enum.Parse(typeof (ServerTypes),
                                                                      ConnectionUtils.GetStringValue
                                                                          (dtLogin.Rows[0],
                                                                           "srv_typ"), true);
                    _serverInfo.SetServerType(serverType);
                }
                catch (ArgumentException)
                {
                }


                if (OnUserEvent != null)
                    OnUserEvent(this, new UserEventArgs(UserEventState.UserLoggedIn, _loginInfo));
            }
            catch (LoginFailedException)
            {
                return false;
            }

            _loggedIn = true;
            return true;
        }

        /// <summary>
        /// Parses the environment string.
        /// </summary>
        /// <param name="clear">if set to <c>true</c> the base info is cleared.</param>
        /// <returns>A new Dictionary of variables</returns>
        private Dictionary<string, string> ParseEnvironmentString(bool clear)
        {
            // Order of variables for this:
            // 1. Use current connection variables unless clear is set
            // 1. use Server Env variables unless it already exists and is defined as not overridable
            // 2. Write over server with connection variables


            Dictionary<string, string> baseEnv = (_connection != null)
                                                     ? new Dictionary<string, string>(
                                                           _connection.Environment, StringComparer.InvariantCultureIgnoreCase)
                                                     : new Dictionary<string, string>(StringComparer.InvariantCultureIgnoreCase);

            Dictionary<string, string> srvEnv = _serverInfo.EnvironmentHash;

            if (clear)
                baseEnv.Clear();

            baseEnv = ConnectionUtils.MergeEnvironmentSets(baseEnv, srvEnv);

            if (_connection != null)
                baseEnv = ConnectionUtils.MergeEnvironmentSets(baseEnv, _connection.Environment);

            return baseEnv;
        }

        /// <summary>
        /// Check if the current connection(<see cref="_connection"/>) is <see cref="MultiThreadQueueConnection"/> object.
        /// </summary>
        /// <param name="connection">The current connection(<see cref="_connection"/>)</param>
        /// <returns><c>true</c> if is <see cref="MultiThreadQueueConnection"/> object; otherwise, <c>false</c>.</returns>
        private bool IsMultiThreadQueueConnection(IMocaConnection connection)
        {
            if (_connection != null && _connection is MultiThreadQueueConnection)
            {
                return true;
            }
            return false;
        }

        /// <summary>
        /// Raises the pre execution event for a command.
        /// </summary>
        /// <param name="command">The command.</param>
        protected void RaisePreExecution(ref string command)
        {
            if (OnExecutionEvent != null)
            {
                OnExecutionEvent(
                    this,
                    new ExecutionEventArgs(
                        ExecutionEvent.PreExecution,
                        ref command));
            }
        }

        /// <summary>
        /// Raises the execution complete event for a command.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="status">The status.</param>
        /// <param name="data">The data.</param>
        /// <param name="errorText">Any error that was the result of the command</param>
        protected void RaiseExecutionComplete(string command, int status, DataSet data,
                                              string errorText)
        {
            if (OnExecutionEvent != null)
            {
                OnExecutionEvent(
                    this,
                    new ExecutionEventArgs(
                        ExecutionEvent.ExecutionComplete,
                        ref command,
                        status,
                        data, errorText));
            }
        }

        #endregion
    }
}
