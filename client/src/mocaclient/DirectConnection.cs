using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Threading;
using RedPrairie.MOCA.Client.Crypto;
using RedPrairie.MOCA.Client.Encoding;
using RedPrairie.MOCA.Client.Interfaces;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// This class impliments a connection to the moca server using .NET
    /// managed network streams.
    /// </summary>
    /// <url>$URL$</url>
    /// <revision>$Revision$</revision>      
    /// <author>$Author$</author>        
    internal class DirectConnection : ConnectionBase, IMocaConnection
    {
        #region Private Fields

        /// <summary>
        /// The protocol of the last successful command
        /// </summary>
        protected int lastCommandProtocol = 104;
        /// <summary>
        /// The flags for the connection
        /// </summary>
        protected int flags = Constants.FLAG_ASCII_COMM;
        /// <summary>
        /// The TCP client object
        /// </summary>
        protected TcpClient socketClient;
        /// <summary>
        /// The outgoing command stream to the server
        /// </summary>
        protected Stream outputStream;
        /// <summary>
        /// The input stream of results from the server
        /// </summary>
        protected Stream inputStream;
        /// <summary>
        /// Indicates if the object has been disposed
        /// </summary>
        protected bool disposed;

        /// <summary>
        /// Holds the <see cref="ServerInfo"/> object for the current connection
        /// </summary>
        protected ServerInfo _serverInfo;

        /// <summary>
        /// A value indicating whether tracing is active.
        /// </summary>
        protected bool _tracingActive;

        /// <summary>
        /// The encryption strategy for the connection
        /// </summary>
        protected SymmetricAlgorithm encryptionStrategy;
        /// <summary>
        /// The <see cref="CommandBuilder"/> used to construct a byte stream for the correct server protocol
        /// </summary>
        protected CommandBuilder commandBuilder;

        /// <summary>
        /// The thread used for an async execution
        /// </summary>
        protected Thread workerThread;

        /// <summary>
        /// The Server Key.
        /// </summary>
        protected string serverKey = "";
        
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
        public DirectConnection(String host, int port, Dictionary<string, string> env)
            : this()
        {
            if (host == null)
                throw new ArgumentException("Host cannot be null");

            Environment = env;

            this.host = host;
            this.port = port;

            _serverInfo = new RedPrairie.MOCA.Client.ServerInfo(
                "", host, port, ConnectionUtils.BuildEnvironmentString(env));
        }

        /// <summary>
        /// Open a connetion to the MOCA service running on the given host and
        /// port.  No client-side context (environment) will be passed along.
        ///  </summary>
        /// <param name="host">the host to connect to. </param>
        /// <param name="port">the port to connect to.  </param>
        /// <throws>  <see cref="MocaException"/> if an error occurs connecting to the remote host.</throws>
        public DirectConnection(string host, int port)
            : this(host, port, null)
        {
        }

        /// <summary>
        /// Parameterless constructor for inherited classes that use different methods
        /// </summary>
        protected DirectConnection()
        {
            commandBuilder = new CommandBuilder(encryptionStrategy);
        }

        /// <summary>
        /// Releases unmanaged resources and performs other cleanup operations before the
        /// <see cref="DirectConnection"/> is reclaimed by garbage collection.
        /// </summary>
        ~DirectConnection()
        {
            DisposeInternal();
        }

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets or sets if auto commit is enabled
        /// </summary>
        public virtual bool AutoCommit
        {
            get { return ((flags & Constants.FLAG_NOCOMMIT) == 0); }

            set
            {
                if (!value)
                {
                    flags |= Constants.FLAG_NOCOMMIT;
                }
                else
                {
                    flags &= ~Constants.FLAG_NOCOMMIT;
                }
            }
        }

        /// <summary>
        /// Gets if the connection is connected
        /// </summary>
        public virtual bool Connected
        {
            get
            {
                return (socketClient != null)
                       ? socketClient.Connected
                       : false;
            }
        }

        /// <summary>
        /// Gets the Environment dictionary
        /// </summary>
        public Dictionary<string, string> Environment
        {
            get { return env; }
            set
            {
                if (value == null)
                {
                    if (env == null)
                        env = new Dictionary<string, string>(StringComparer.InvariantCultureIgnoreCase);
                }
                else
                {
                    env = new Dictionary<string, string>(value, StringComparer.InvariantCultureIgnoreCase);
                    UpdateEnvironmentString();
                }
            }
        }

        /// <summary>
        /// Gets the Host of the connection 
        /// </summary>
        public string Host
        {
            get { return host; }
        }

        /// <summary>
        /// Gets if worker thread for this class is currently running
        /// </summary>
        public bool IsRunningCommand
        {
            get
            {
                if (workerThread == null)
                    return false;

                return ((workerThread.ThreadState == ThreadState.Running) ||
                        (workerThread.ThreadState == ThreadState.Background));
            }
        }

        /// <summary>
        /// Gets the managed thread ID
        /// </summary>
        public string ManagedThreadID
        {
            get { return (workerThread != null) ? workerThread.ManagedThreadId.ToString() : ""; }
        }

        /// <summary>
        /// Gets the port of the connection
        /// </summary>
        public int Port
        {
            get { return port; }
        }

        /// <summary>
        /// Gets the Current State of the Thread
        /// </summary>
        public ThreadState ThreadState
        {
            get { return (workerThread != null) ? workerThread.ThreadState : ThreadState.Stopped; }
        }

        /// <summary>
        /// Gets the Current State of the Thread
        /// </summary>
        public bool ThreadAlive
        {
            get { return (workerThread != null) ? workerThread.IsAlive : false; }
        }

        /// <summary>
        /// Gets a value indicating whether the connection supports arguments.
        /// </summary>
        /// <value><c>true</c> if the connection supports arguments; otherwise, <c>false</c>.</value>
        public virtual bool SupportsArguments
        {
            get { return false; }
        }

        /// <summary>
        /// Gets the <see cref="ServerInfo"/> object of the simple connection.
        /// </summary>
        public ServerInfo ServerInfo
        {
            get { return _serverInfo; }
        }

        /// <summary>
        /// Gets a value indicating whether tracing is active.
        /// </summary>
        /// <value><c>true</c> if tracing is active; otherwise, <c>false</c>.</value>
        public bool TracingActive
        {
            get { return _tracingActive; }
            set { _tracingActive = value; }
        }

        /// <summary>
        /// Gets the Server Key.
        /// </summary>
        public string ServerKey
        {
            get { return serverKey; }
        }

        #endregion

        #region Public Methods

        /// <summary>
        /// Begins the execution of a server command. This uses a callback
        /// to handle the results.
        /// </summary>
        /// <param name="callBack">Uses <see cref="ExecuteCallBack"/> object to do the callback</param>
        public void BeginExecuteCommand(InternalCallBackEventArgs callBack)
        {
            if (callBack.Command.ApplicationID != null)
            {
                callBack.Command.PrevApplicationID = ApplicationID;
                ApplicationID = callBack.Command.ApplicationID;
            }

            InternalAsyncExecute(callBack);
        }

        /// <summary>
        /// Disables server tracing for the connection.
        /// </summary>
        public void DisableTracing()
        {
            const string strCmd = "set trace where activate = 0 ";
            ExecuteCommandResult result = Execute(new Command(strCmd));

            if (!result.IsOK)
            {
                throw new MocaException(result.StatusCode,
                                        string.Format("Error enabling tracing: {0}",
                                                      result.StatusCode));
            }

            _tracingActive = false;
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

            ExecuteCommandResult result = Execute(new Command(strCmd));

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
            Execute(new Command(strCmd));

            // Now remove both the original trace files, ignore the return status errors
            strCmd =
                string.Format("remove file where filename = '{0}'", _serverInfo.CurrentTraceFileName);
            Execute(new Command(strCmd));
            strCmd =
                string.Format("remove file where filename = '{0}'",
                              _serverInfo.CurrentTracePrfFileName);
            Execute(new Command(strCmd));

            // Reopen the trace file in append mode
            strCmd = string.Format(
                "set trace " +
                " where activate = 1 " +
                " and level = \"{0}\" " +
                " and filename = \"{1}\" " +
                " and mode = \"a+\" ",
                traceLevels, filename);

            result = Execute(new Command(strCmd));

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
        }

        /// <summary>
        /// Closes the connection if it was open
        /// </summary>
        public override void Close()
        {
            try
            {
                if (socketClient != null && socketClient.Connected)
                    socketClient.Close();

                //Kill thread if necessary
                if (workerThread != null &&
                    workerThread.ThreadState == ThreadState.Running)
                {
                    workerThread.Abort();

                    while (workerThread.ThreadState != ThreadState.Stopped)
                    {
                    }
                }
            }
            catch (ThreadAbortException)
            {
            }
            catch (IOException)
            {
            }

            base.Close();
        }

        /// <summary>
        /// Establishes a socket connection to the server
        /// </summary>
        public virtual void Connect()
        {
            try
            {
                socketClient = new TcpClient(host, port);

                outputStream = new BufferedStream(socketClient.GetStream());
                inputStream = new BufferedStream(socketClient.GetStream());

                string encryptionType = null;
                try
                {
                    ExecuteCommandResult res = Execute(new Command("get encryption information"));

                    if (res.Error != null && res.Error is ConnectionFailedException)
                    {
                        throw res.Error;
                    }

                    if (res.HasData)
                    {
                        encryptionType = res.TableData.Rows[0]["name"].ToString();

                        if (res.TableData.Columns.Contains("charset"))
                            EncodingBase.SetEncoding(res.TableData.Rows[0]["charset"].ToString());

                        if (res.TableData.Columns.Contains("server_key"))
                        {
                            serverKey = res.TableData.Rows[0]["server_key"].ToString();
                        }
                    }
                }
                catch (MocaException e)
                {
                    throw new ConnectionFailedException("Unable To Connect", e);
                }
                encryptionStrategy = GetEncryptionStrategy(encryptionType);
                commandBuilder.EncryptionStrategy = encryptionStrategy;
            }
            catch (SocketException e)
            {
                throw new ConnectionFailedException("Unable to Connect", e);
            }
            catch (IOException e)
            {
                throw new ConnectionFailedException("Unable to Connect", e);
            }
        }

        /// <summary>
        /// Executes a command to the server and retrieves the results.
        /// </summary>
        /// <param name="command"></param>
        /// <returns></returns>
        public ExecuteCommandResult Execute(Command command)
        {
            try
            {
                DataSet dataSet = InternalExecute(command);
                return new ExecuteCommandResult(command.CommandText, MocaErrors.eOK, dataSet);
            }
            catch (MocaException e)
            {
                return new ExecuteCommandResult(command.CommandText, e);
            }
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
            string prevApplicationID = null;
            if (applicationID != null)
            {
                prevApplicationID = ApplicationID;
                ApplicationID = applicationID;
            }

            ExecuteCommandResult result = Execute(command);

            if (applicationID != null)
            {
                ApplicationID = prevApplicationID;
            }

            return result;
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
            string prevApplicationID = null;
            if (!String.IsNullOrEmpty(applicationID))
            {
                prevApplicationID = ApplicationID;
                ApplicationID = applicationID;
            }

            object data = InternalObjectExecute(command, mappingData, resolver);

            if (!String.IsNullOrEmpty(prevApplicationID))
            {
                ApplicationID = prevApplicationID;
            }

            return data;
        }

        /// <summary>
        /// Initializes the connection with new information.
        /// </summary>
        /// <param name="newHost">The new host.</param>
        /// <param name="newPort">The new port.</param>
        /// <param name="newEnv">The new env.</param>
        public void Initialize(string newHost, int newPort, Dictionary<string, string> newEnv)
        {
            Close();

            Environment = newEnv;
            host = newHost;
            port = newPort;
        }

        /// <summary>
        /// Disconnects and Reconnects to the server
        /// </summary>
        public virtual void Reconnect()
        {
            try
            {
                if (outputStream != null)
                    outputStream.Close();

                if (inputStream != null)
                    inputStream.Close();

                if (socketClient != null && socketClient.Connected)
                    socketClient.Close();
            }
            catch (IOException)
            {
            }

            Connect();
        }

        #endregion

        #region Protected Methods

        /// <summary>
        /// Checks to see if the connection to the server has closed
        /// </summary>
        /// <exception cref="IOException">This is thrown if the socket has closed</exception>
        protected virtual void CheckForServerClose()
        {
            try
            {
                inputStream.Flush();
                if (socketClient == null || !socketClient.Connected ||
                    socketClient.Client.Poll(0, SelectMode.SelectRead))
                    throw new IOException("Connection Closed");
            }
            catch (SystemException)
            {
                throw new IOException("Connection Closed");
            }
        }

        /// <summary>
        /// Determines which envryption strategy to use  on the connection
        /// </summary>
        /// <param name="name">The encryption name from the server</param>
        /// <returns>A new <see cref="SymmetricAlgorithm"/> class</returns>
        protected static SymmetricAlgorithm GetEncryptionStrategy(string name)
        {
            if (name != null && name.ToUpper().Equals("RPBF"))
            {
                return new RPBFEncryptionStrategy();
            }
            if (name != null && name.Equals("blowfish"))
            {
                return new BlowfishEncryptionStrategy();
            }

            return null;
        }

        /// <summary>
        /// The internal method that handles the actual execution of the command.
        /// </summary>
        /// <param name="command">The command that is sent.</param>
        /// <returns>A <see cref="DataSet"/> of the results"/></returns>
        protected virtual DataSet InternalExecute(Command command)
        {
            SendDataToServer(command);

            try
            {
                return ReadResponse(command.CommandText);
            }
            catch (IOException e)
            {
                throw new ReadResponseException(e);
            }
            catch (ProtocolException e)
            {
                if (lastCommandProtocol > Constants.PROTOCOL_MIN_VERSION)
                {
                    lastCommandProtocol--;
                    return InternalExecute(command);
                }

                throw new ProtocolException("Could not find protocol", e);
            }
        }

        /// <summary>
        /// The internal method that handles the actual execution of the command.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="mappingData">The mapping data.</param>
        /// <param name="resolver">The resolver.</param>
        /// <returns>An object of the collection</returns>
        protected virtual object InternalObjectExecute(Command command, MappingData mappingData, IObjectResolver resolver)
        {
            SendDataToServer(command);

            try
            {
                return ReadResponse(mappingData, resolver);
            }
            catch (IOException e)
            {
                throw new ReadResponseException(e);
            }
            catch (ProtocolException e)
            {
                if (lastCommandProtocol > Constants.PROTOCOL_MIN_VERSION)
                {
                    lastCommandProtocol--;
                    return ReadResponse(mappingData, resolver);
                }

                throw new ProtocolException("Could not find protocol", e);
            }
        }

        /// <summary>
        /// The internal method that handles the actual start of an asynchronous execution.
        /// </summary>
        /// <param name="callBack">The callback method that uses the results</param>
        protected virtual void InternalAsyncExecute(InternalCallBackEventArgs callBack)
        {
            while (IsRunningCommand)
                Thread.Sleep(0);

            workerThread = new Thread(InternalAsyncExecute);
            workerThread.IsBackground = true;
            workerThread.Name = string.Format("MOCA Connection Command: {0}", callBack.Command.Command);
            workerThread.Start(callBack);
        }

        /// <summary>
        /// The internal method that handles the actual start of an asynchronous execution.
        /// </summary>
        /// <param name="internalCommand">The <see cref="InternalCommand"/> that is sent as an object for threading</param>
        protected virtual void InternalAsyncExecute(object internalCommand)
        {
            InternalCallBackEventArgs internalCmd = (InternalCallBackEventArgs)internalCommand;

            //Execute the command
            try
            {
                internalCmd.Command.Result = (internalCmd.Command.IsDataSet)
                                             ? InternalExecute(internalCmd.Command.Command)
                                             : InternalObjectExecute(internalCmd.Command.Command,
                                             internalCmd.Command.MappingData, internalCmd.Command.ObjectResolver);
            }
            catch (MocaException e)
            {
                internalCmd.Command.Exception = e;
                internalCmd.Command.Result = e.Results;
            }

            //Reset the application ID if needed
            if (internalCmd.Command.PrevApplicationID != null)
                ApplicationID = internalCmd.Command.PrevApplicationID;

            internalCmd.InvokeCommandCallback(this);
        }

        /// <summary>
        /// Reads the response from the input stream and parses
        /// according to the protocol version.
        /// </summary>
        /// <param name="command">The command that created the response. Used for setting the table name.</param>
        /// <returns>A <see cref="DataSet"/> of the results.</returns>
        protected DataSet ReadResponse(string command)
        {
            DirectConnectionResultsDecoder decoder = new DirectConnectionResultsDecoder(inputStream, encryptionStrategy);

            DataSet dsResult = decoder.Decode(command);
            lastCommandProtocol = decoder.ProtocolVersion;

            return dsResult;
        }

        /// <summary>
        /// Reads the response from the input stream and parses
        /// according to the protocol version.
        /// </summary>
        /// <param name="mappingData">The mapping data for the class mapping.</param>
        /// <param name="resolver">The object resolver.</param>
        /// <returns>An object of the results collection.</returns>
        protected object ReadResponse(MappingData mappingData, IObjectResolver resolver)
        {
            ObjectMappingDecoder decoder = new ObjectMappingDecoder(inputStream, encryptionStrategy,
                                                                    mappingData, resolver);

            object dsResult = decoder.DecodeCollection();
            lastCommandProtocol = decoder.ProtocolVersion;

            return dsResult;
        }

        /// <summary>
        /// Transmits a command to the server
        /// </summary>
        /// <param name="command">The command to send</param>
        protected void SendCommand(string command)
        {
            inputStream.Flush();

            byte[] data = commandBuilder.CreateCommand(command, lastCommandProtocol, flags);

            outputStream.Write(data, 0, data.Length);
            outputStream.Flush();
        }

        /// <summary>
        /// Sends the data to server.
        /// </summary>
        /// <param name="command">The command.</param>
        protected void SendDataToServer(Command command)
        {
            try
            {
                CheckForServerClose();
                SendCommand(command.CommandText);
            }
            catch (IOException e)
            {
                Reconnect();
                try
                {
                    CheckForServerClose();
                    SendCommand(command.CommandText);
                }
                catch (IOException)
                {
                    throw new SendRequestException(e);
                }
            }
        }

        /// <summary>
        /// Updates the environment string
        /// </summary>
        protected override void UpdateEnvironmentString()
        {
            base.UpdateEnvironmentString();
            commandBuilder.EnvironmentEncoding = environmentString;
        }

        #region IDisposable Methods

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
            if (disposed)
                return;

            //Dispose of all resources
            Close();


            // Disposing has been done.
            disposed = true;
        }

        #endregion

        #endregion
    }
}
