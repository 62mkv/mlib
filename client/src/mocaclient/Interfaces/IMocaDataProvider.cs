using System;
using System.Collections.Generic;
using System.Data;
using System.Diagnostics;
using RedPrairie.MOCA.Exceptions;

namespace RedPrairie.MOCA.Client.Interfaces
{
    /// <summary>
    /// An interface to the moca data provider system to abstract the
    /// implimenting class.
    /// </summary>
    public interface IMocaDataProvider
    {
        /// <summary>
        /// Occurs when digital signature challenge is returned from the server.
        /// It is suggested that the ConfirmDigitalSignature
        /// </summary>
        event EventHandler<DigitalSignatureEventArgs> DigitalSignatureChallenge;

        /// <summary>
        /// Occurs when a command is executed.
        /// </summary>
        event EventHandler<ExecutionEventArgs> OnExecutionEvent;

        /// <summary>
        /// Occurs when user logs in or out
        /// </summary>
        event EventHandler<UserEventArgs> OnUserEvent;

        /// <summary>
        /// Occurs when a connection related event occurs (connect, disconnect, etc.).
        /// </summary>
        event EventHandler<ConnectionEventArgs> OnConnectionEvent;

        /// <summary>
        /// Occurs when a server environemt variable has changed.
        /// </summary>
        event EventHandler<ServerEvnChangeArgs> OnServerEvnChange;

        /// <summary>
        /// Gets or Sets wither auto commit mode is enabled for the connection
        /// </summary>
        bool AutoCommit { get; set; }

        /// <summary>
        /// Gets a value indicating whether this <see cref="FullConnection"/> is connected.
        /// </summary>
        /// <value><c>true</c> if connected; otherwise, <c>false</c>.</value>
        bool Connected { get; }

        /// <summary>
        /// Gets the name of the connection.
        /// </summary>
        /// <value>The name of the connection.</value>
        string ConnectionName { [DebuggerStepThrough]
        get; }

        /// <summary>
        /// Gets the default server.
        /// </summary>
        /// <value></value>
        string DefaultServer { [DebuggerStepThrough]
        get; }

        /// <summary>
        /// Gets the environment keys.
        /// </summary>
        /// <value></value>
        ICollection<string> EnvironmentKeys { [DebuggerStepThrough]
        get; }

        /// <summary>
        /// Gets the invalid servers.
        /// </summary>
        /// <value></value>
        Dictionary<string, Exception> InvalidServers { [DebuggerStepThrough]
        get; }

        /// <summary>
        /// Gets the known servers.
        /// </summary>
        /// <value></value>
        ICollection<ServerInfo> KnownServers { [DebuggerStepThrough]
        get; }

        /// <summary>
        /// Gets or sets the locale ID.
        /// </summary>
        /// <value></value>
        string LocaleID { [DebuggerStepThrough]
        get; //set the connections locale
            set; }

        /// <summary>
        /// Gets a value indicating whether the connection is logged in.
        /// </summary>
        /// <value><c>true</c> if logged in; otherwise, <c>false</c>.</value>
        bool LoggedIn { [DebuggerStepThrough]
        get; }

        /// <summary>
        /// Gets the login info.
        /// </summary>
        /// <value></value>
        LoginInfo LoginInfo { [DebuggerStepThrough]
        get; }

        /// <summary>
        /// Gets the server info.
        /// </summary>
        /// <value></value>
        ServerInfo ServerInfo { [DebuggerStepThrough]
        get; }

        /// <summary>
        /// Gets the name of the server.
        /// </summary>
        /// <value>The name of the server.</value>
        string ServerName { [DebuggerStepThrough]
        get; }

        /// <summary>
        /// Gets a value indicating whether tracing is active.
        /// </summary>
        /// <value><c>true</c> if tracing is active; otherwise, <c>false</c>.</value>
        bool TracingActive { [DebuggerStepThrough]
        get; }

        /// <summary>
        /// Gets the trace file path.
        /// </summary>
        /// <value></value>
        string TraceFilePath { [DebuggerStepThrough]
        get; }

        /// <summary>
        /// Gets the trace profile path.
        /// </summary>
        /// <value></value>
        string TraceProfilePath { [DebuggerStepThrough]
        get; }

        /// <summary>
        /// Gets the user ID.
        /// </summary>
        /// <value></value>
        string UserID { [DebuggerStepThrough]
        get; }

        /// <summary>
        /// Gets or sets the warehouse ID.
        /// </summary>
        /// <value></value>
        string WarehouseID { [DebuggerStepThrough]
        get; set; }

        /// <summary>
        /// Gets the <see cref="ServerInfo"/> objects of all trace enabled asynchronous connections.
        /// </summary>
        Dictionary<string, ServerInfo> AsyncTraceServerInfo { [DebuggerStepThrough]
        get; }

        /// <summary>
        /// Closes the current connection if one is open.
        /// </summary>
        void Close();

        /// <summary>
        /// Confirms the digital signature for the user and populates it on the list.
        /// </summary>
        /// <param name="username">The username.</param>
        /// <param name="password">The password.</param>
        /// <exception cref="LoginFailedException">Thrown when the user's name or password is invalid.</exception>
        /// <exception cref="LockOutException">Thrown when the account has been locked.</exception>
        /// <returns><c>true</c> if the signature confirmation is successful; otherwise <c>false</c>.</returns>
        void ConfirmDigitalSignature(string username, string password);

        /// <summary>
        /// Connects to the Server using settings from the constructor
        /// </summary>
        void Connect();

        /// <summary>
        /// Connects the specified server name.
        /// </summary>
        /// <param name="serverName">Name of the server instance from the config</param>
        void Connect(string serverName);

        /// <summary>
        /// Connects the specified host.
        /// </summary>
        /// <param name="host">The host.</param>
        /// <param name="port">The port number.</param>
        void Connect(string host, int port);

        /// <summary>
        /// Connects the specified server name.
        /// </summary>
        /// <param name="serverName">Name of the server instance from the config</param>
        /// <param name="host">The host.</param>
        /// <param name="port">The port number.</param>
        void Connect(string serverName, string host, int port);

        /// <summary>
        /// Connects the specified host.
        /// </summary>
        /// <param name="host">The host.</param>
        /// <param name="port">The port number.</param>
        /// <param name="environment">The environment string to connect with.</param>
        void Connect(string host, int port, string environment);

        /// <summary>
        /// Connects the specified server name.
        /// </summary>
        /// <param name="serverName">Name of the server instance from the config</param>
        /// <param name="host">The host.</param>
        /// <param name="port">The port number.</param>
        /// <param name="environment">The environment string to connect with.</param>
        /// <param name="manualServer">if set to <c>true</c> the connection does not have a defined configuration.</param>
        void Connect(string serverName, string host, int port, string environment,
                     bool manualServer);

        /// <summary>
        /// Creates a new server info class.
        /// </summary>
        /// <param name="name">The local server name.</param>
        /// <param name="host">The host name.</param>
        /// <param name="port">The port number.</param>
        /// <returns>A new <see cref="FullConnection.ServerInfo"/> class</returns>
        ServerInfo CreateNewServerInfo(string name, string host, int port);

        /// <summary>
        /// Disables server tracing for the connection.
        /// </summary>
        void DisableTracing();

        /// <summary>
        /// Enables tracing on the connection.
        /// </summary>
        void EnableTracing();

        /// <summary>
        /// Enables tracing on the connection.
        /// </summary>
        /// <param name="filename">The filename to write the trace to.</param>
        /// <param name="traceLevels">The trace levels to use.</param>
        void EnableTracing(string filename, string traceLevels);

        /// <summary>
        /// Indicates if an environments variable exists.
        /// </summary>
        /// <param name="variableName">Name of the variable.</param>
        /// <returns><c>true</c> if it exists; otherwise <c>false</c>.</returns>
        bool EnvironmentVariableExists(string variableName);

        /// <summary>
        /// Executes the specified MOCA command.
        /// </summary>
        /// <param name="command">The command to execute</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        int Execute(string command);

        /// <summary>
        /// Executes the specified command.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="applicationID">The application ID.</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        int Execute(string command, string applicationID);

        /// <summary>
        /// Executes the specified command.
        /// </summary>
        /// <param name="Command">The command.</param>
        /// <param name="data">The <see cref="DataSet"/> of data that was returned</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        int Execute(string Command, ref DataSet data);

        /// <summary>
        /// Executes the specified command.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="applicationID">The application ID.</param>
        /// <param name="data">The <see cref="DataSet"/> of data that was returned</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        int Execute(string command, string applicationID, ref DataSet data);

        /// <summary>
        /// Executes the specified command.
        /// </summary>
        /// <param name="Command">The command.</param>
        /// <param name="data">The <see cref="DataSet"/> of data that was returned</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        int Execute(string Command, out DataTable data);

        /// <summary>
        /// Executes the specified command.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="applicationID">The application ID.</param>
        /// <param name="data">The <see cref="DataSet"/> of data that was returned</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        int Execute(string command, string applicationID, out DataTable data);

        /// <summary>
        /// Executes the specified command.
        /// </summary>
        /// <param name="Command">The command.</param>
        /// <param name="dataView">The <see cref="DataView"/> of data that was returned</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        int Execute(string Command, out DataView dataView);

        /// <summary>
        /// Executes the specified command.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="applicationID">The application ID.</param>
        /// <param name="dataView">The <see cref="DataView"/> of data that was returned</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        int Execute(string command, string applicationID, out DataView dataView);

        /// <summary>
        /// Begins the Execution of a command. The async call cannot raise
        /// the pre and post execute events
        /// </summary>
        /// <param name="command">The command to execute</param>
        /// <param name="callBack">The call back once complete</param>
        void BeginExecute(string command, ExecuteCallBack callBack);

        /// <summary>
        /// Begins the Execution of a command. The async call cannot raise
        /// the pre and post execute events
        /// </summary>
        /// <param name="command">The command to execute</param>
        /// <param name="applicationID">The application ID</param>
        /// <param name="callBack">The call back once complete</param>
        void BeginExecute(string command, string applicationID, ExecuteCallBack callBack);

        /// <summary>
        /// Executes the specified MOCA command using <see cref="ExecuteCommandResult"/> to return the data.
        /// </summary>
        /// <param name="command">The command to execute</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        ExecuteCommandResult ExecuteResults(string command);

        /// <summary>
        /// Executes the specified command using <see cref="ExecuteCommandResult"/> to return the data.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="applicationID">The application ID.</param>
        /// <returns>An <c>int</c> indicating the result code</returns>
        ExecuteCommandResult ExecuteResults(string command, string applicationID);

        /// <summary>
        /// Gets the environment variable value for the specified key.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <returns>A string of the result or an empty string</returns>
        string GetEnvironmentVariable(string key);

        /// <summary>
        /// Gets the <see cref="FullConnection.ServerInfo"/> for a server.
        /// </summary>
        /// <param name="serverName">Name of the server.</param>
        /// <returns>The <see cref="FullConnection.ServerInfo"/> otherwise null</returns>
        ServerInfo GetServerInfo(string serverName);

        /// <summary>
        /// Gets the <see cref="FullConnection.ServerInfo"/> for a server by host and port.
        /// </summary>
        /// <param name="host">The hostname of the server.</param>
        /// <param name="port">The port of the server.</param>
        /// <returns>The <see cref="FullConnection.ServerInfo"/>; otherwise <c>null</c>.</returns>
        ServerInfo GetServerInfo(string host, int port);

        /// <summary>
        /// Loads the known servers from a configuration. The base class simply
        /// clears out the holders for these variables.
        /// </summary>
        void LoadKnownServers();

        /// <summary>
        /// Logins the specified username to the server.
        /// </summary>
        /// <param name="username">The username.</param>
        /// <param name="password">The password.</param>
        /// <returns><c>true</c> if successful, <c>false</c> otherwise</returns>
        bool Login(string username, string password);
        
        /// <summary>
        /// Logins the specified username to the server.
        /// </summary>
        /// <param name="username">The username.</param>
        /// <param name="password">The password.</param>
        /// <param name="clientKey">The client key.</param>
        /// <returns><c>true</c> if successful, <c>false</c> otherwise</returns>
        bool Login(string username, string password, string clientKey);

        /// <summary>
        /// Logs the currently logged in user out.
        /// </summary>
        void LogOut();

        /// <summary>
        /// Sets the environment variable value for the specified key.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <param name="value">The value.</param>
        void SetEnvironmentVariable(string key, string value);

        /// <summary>
        /// Logins the specified username to the server using the SingleSignon delegate
        /// </summary>
        /// <param name="username">The username to verify</param>
        /// <returns><c>true</c> if successful, <c>false</c> otherwise</returns>
        bool SingleSignonLogin(string username);
    }
}