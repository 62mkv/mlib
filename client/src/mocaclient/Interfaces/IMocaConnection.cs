using System;
using System.Collections.Generic;
using RedPrairie.MOCA.Client.ObjectMapping;

namespace RedPrairie.MOCA.Client.Interfaces
{
    /// <summary>
    /// This interface defines the events, properties and methods that the derived
    /// classes must have to be a valid MOCA connection.
    /// </summary>
    internal interface IMocaConnection : IDisposable
    {
        /// <summary> 
        /// Set the autoCommit flag.  Normally, each command execution comprises a
        /// single transaction.  If the autocommit flag is set to false, multiple
        /// commands will be executed within the same transactional context. 
        /// Warning: disabling autoCommit can affect the health of the server
        /// framework, and should only be used for very short periods of time.
        /// </summary>
        bool AutoCommit { get; set; }

        /// <summary>
        /// Gets or Sets the application ID.
        /// </summary>
        string ApplicationID { get; set; }

        /// <summary>
        /// Gets or Sets the ID of the connection
        /// </summary>
        string ConnectionID { get; set; }

        /// <summary>
        /// Gets wither the connection is connected
        /// </summary>
        bool Connected { get; }

        /// <summary> 
        /// Sets up the environment for this connection.  The environment String
        /// is used to pass client-specific information to server-side components.
        /// Certain environment entries are reserved for MOCA, and certain one
        /// are reserved for well-known application use.  
        /// </summary>
        Dictionary<string, string> Environment { get; set; }

        /// <summary>
        /// Gets a value indicating whether the connection supports arguments.
        /// </summary>
        /// <value><c>true</c> if the connection supports arguments; otherwise, <c>false</c>.</value>
        bool SupportsArguments { get; }

        /// <summary>
        /// Gets the <see cref="ServerInfo"/> object of the simple connection.
        /// </summary>
        ServerInfo ServerInfo { get; }

        /// <summary>
        /// Gets a value indicating whether tracing is active.
        /// </summary>
        /// <value><c>true</c> if tracing is active; otherwise, <c>false</c>.</value>
        bool TracingActive { get; }

        /// <summary>
        /// Gets the Server Key.
        /// </summary>
        string ServerKey { get; }

        /// <summary>
        /// Begins the execution of a server command. This uses a callback
        /// to handle the results.
        /// </summary>
        /// <param name="callBack">Uses <see cref="ExecuteCallBack"/> object to do the callback</param>
        void BeginExecuteCommand(InternalCallBackEventArgs callBack);

        /// <summary>
        /// Establishes a connection with the server
        /// </summary>
        void Connect();

        /// <summary> 
        /// Closes the connection.  Any attempt to execute further commands on
        /// a connection that has been closed will fail.
        /// </summary>
        void Close();

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
        ExecuteCommandResult Execute(Command command);

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
        ExecuteCommandResult Execute(Command command, string applicationID);

        /// <summary> 
        /// Execute a command, returning results from that command in an object specified by the
        /// data specified in the <paramref name="mappingData"/> object.
        /// </summary>
        /// <param name="command">the text of the MOCA command sequence to execute.
        /// Execution will occur on the target system that this connection is
        /// associated with. 
        /// </param>
        /// <param name="applicationID">The ID of the application that called the method</param>
        /// <param name="mappingData">The object mapping data that provides information.</param>
        /// <param name="resolver">The object resolver that creates objects for the collection.</param>
        /// <returns> a MocaResults object reprsenting the results of the command
        /// execution. </returns>
        /// <throws>  
        /// MocaException if an error occurs, either in communication with 
        /// the server, or upon execution of the command.
        /// </throws>
        object Execute(Command command, string applicationID, MappingData mappingData, IObjectResolver resolver);

        /// <summary>
        /// Initializes a connection object with new information
        /// </summary>
        /// <param name="host">The host name</param>
        /// <param name="port">The port number</param>
        /// <param name="environment">The environment hash</param>
        void Initialize(string host, int port, Dictionary<string, string> environment);

        /// <summary>
        /// Reconnects with the server
        /// </summary>
        void Reconnect();

        /// <summary>
        /// Updates an environment setting.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <param name="value">The value to replace it.</param>
        void UpdateEnvironmentSetting(string key, string value);

    }
}