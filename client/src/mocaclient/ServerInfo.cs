using System.Collections.Generic;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// This class holds the basic server connection information
    /// and the current state of it's tracing options.
    /// </summary>
    public class ServerInfo
    {
        /// <summary>
        /// the instance name of the server
        /// </summary>
        protected string name;
        /// <summary>
        /// the hostname of the server
        /// </summary>
        protected string host;
        /// <summary>
        /// the port of the server
        /// </summary>
        protected int port;
        /// <summary>
        /// The user id to automatically log in
        /// </summary>
        protected string userID;
        /// <summary>
        /// The password of the user to automatically log in
        /// </summary>
        protected string password;
        /// <summary>
        /// The environment string to use
        /// </summary>
        protected string environment;
        /// <summary>
        /// The default trace file name to use
        /// </summary>
        protected string traceFileName;
        /// <summary>
        /// The default trace levels
        /// </summary>
        protected string traceLevels;
        /// <summary>
        /// The current trace file name
        /// </summary>
        protected string currentTraceFileName;
        /// <summary>
        /// The current trace performance file name
        /// </summary>
        protected string currentTracePrfFileName;
        /// <summary>
        ///  The current trace levels
        /// </summary>
        protected string currentTraceLevels;
        /// <summary>
        /// The server type
        /// </summary>
        protected ServerTypes serverType = ServerTypes.Other;

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="ServerInfo"/> class.
        /// </summary>
        /// <param name="name">The name.</param>
        public ServerInfo(string name)
            : this(name, "", -1, "", "", "", "", "", "", "", "", ServerTypes.Other)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ServerInfo"/> class.
        /// </summary>
        /// <param name="name">The name.</param>
        /// <param name="host">The host.</param>
        public ServerInfo(string name, string host)
            : this(name, host, -1, "", "", "", "", "", "", "", "", ServerTypes.Other)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ServerInfo"/> class.
        /// </summary>
        /// <param name="name">The name.</param>
        /// <param name="host">The host.</param>
        /// <param name="port">The port.</param>
        public ServerInfo(string name, string host, int port)
            : this(name, host, port, "", "", "", "", "", "", "", "", ServerTypes.Other)
        {
        }

         /// <summary>
        /// Initializes a new instance of the <see cref="ServerInfo"/> class.
        /// </summary>
        /// <param name="name">The name.</param>
        /// <param name="host">The host.</param>
        /// <param name="environment">The environment.</param>
        public ServerInfo(string name, string host, string environment)
            : this(name, host, -1, "", "", environment, "", "", "", "", "", ServerTypes.Other)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ServerInfo"/> class.
        /// </summary>
        /// <param name="name">The name.</param>
        /// <param name="host">The host.</param>
        /// <param name="port">The port.</param>
        /// <param name="environment">The environment.</param>
        public ServerInfo(string name, string host, int port, string environment)
            : this(name, host, port, "", "", environment, "", "", "", "", "", ServerTypes.Other)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ServerInfo"/> class.
        /// </summary>
        /// <param name="name">The name.</param>
        /// <param name="host">The host.</param>
        /// <param name="port">The port.</param>
        /// <param name="userID">The user ID.</param>
        /// <param name="password">The password.</param>
        /// <param name="environment">The environment.</param>
        /// <param name="traceFileName">Name of the _trace file.</param>
        /// <param name="traceLevels">The trace levels.</param>
        /// <param name="currentTraceFileName">Name of the current trace file.</param>
        /// <param name="currentTraceLevels">The _current trace levels.</param>
        /// <param name="currentTracePrfFileName">Name of the current trace profile file.</param>
        /// <param name="serverType">Type of the server.</param>
        public ServerInfo(string name, string host, int port, string userID, string password, 
                          string environment, string traceFileName, string traceLevels, 
                          string currentTraceFileName, string currentTraceLevels, 
                          string currentTracePrfFileName, ServerTypes serverType)
        {
            this.name = name;
            this.host = host;
            this.port = port;
            this.userID = userID;
            this.password = password;
            this.environment = environment;
            this.traceFileName = traceFileName;
            this.traceLevels = traceLevels;
            this.currentTraceFileName = currentTraceFileName;
            this.currentTraceLevels = currentTraceLevels;
            this.currentTracePrfFileName = currentTracePrfFileName;
            this.serverType = serverType;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ServerInfo"/> class. Copy constructor.
        /// </summary>
        /// <param name="copy">A current <see cref="ServerInfo"/> class to copy</param>
        public ServerInfo(ServerInfo copy)
        {
            serverType = copy.ServerType;
            host = copy.Host;
            name = copy.Name;
            port = copy.Port;
            password = copy.Password;
            userID = copy.UserID;
            environment = copy.environment;
            traceFileName = copy.TraceFileName;
            traceLevels = copy.TraceLevels;
            currentTraceFileName = copy.CurrentTraceFileName;
            currentTracePrfFileName = copy.currentTracePrfFileName;
            currentTraceLevels = copy.CurrentTraceLevels;
        }

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets or sets the name.
        /// </summary>
        /// <value></value>
        public string Name
        {
            get { return name; }
            set { name = value; }
        }

        /// <summary>
        /// Gets or sets the host.
        /// </summary>
        /// <value></value>
        public string Host
        {
            get { return host; }
            set { host = value; }
        }

        /// <summary>
        /// Gets or sets the port.
        /// </summary>
        /// <value></value>
        public int Port
        {
            get { return port; }
            set { port = value; }
        }

        /// <summary>
        /// Gets or sets the user ID.
        /// </summary>
        /// <value></value>
        public string UserID
        {
            get { return userID; }
            set { userID = value; }
        }

        /// <summary>
        /// Gets or sets the password.
        /// </summary>
        /// <value></value>
        public string Password
        {
            get { return password; }
            set { password = value; }
        }

        /// <summary>
        /// Gets or sets the environment.
        /// </summary>
        /// <value></value>
        public virtual string Environment
        {
            get { return environment; }
            set { environment = value; }
        }

        /// <summary>
        /// Gets the environment hash.
        /// </summary>
        public virtual Dictionary<string, string> EnvironmentHash
        {
            get { return ConnectionUtils.ParseEnvironmentString(environment); }
        }
        
        /// <summary>
        /// Gets the name of the trace file.
        /// </summary>
        /// <value></value>
        public string TraceFileName
        {
            get { return traceFileName; }
        }

        /// <summary>
        /// Gets the trace levels.
        /// </summary>
        /// <value></value>
        public string TraceLevels
        {
            get { return traceLevels; }
        }

        /// <summary>
        /// Gets or sets the name of the current trace file.
        /// </summary>
        public string CurrentTraceFileName
        {
            get { return currentTraceFileName; }
            set { currentTraceFileName = value; }
        }

        /// <summary>
        /// Gets or sets the name of the current trace profile file.
        /// </summary>
        public string CurrentTracePrfFileName
        {
            get { return currentTracePrfFileName; }
            set { currentTracePrfFileName = value; }
        }

        /// <summary>
        /// Gets or sets the current trace levels.
        /// </summary>
        public string CurrentTraceLevels
        {
            get { return currentTraceLevels; }
            set { currentTraceLevels = value; }
        }

        /// <summary>
        /// Gets the type of the server.
        /// </summary>
        public ServerTypes ServerType
        {
            get { return serverType; }
        }
                
        #endregion

        #region Internal Methods
        /// <summary>
        /// Sets the type of the server.
        /// </summary>
        /// <param name="newServerType">New type of the server.</param>
        internal void SetServerType(ServerTypes newServerType)
        {
            serverType = newServerType;
        }

        /// <summary>
        /// Builds the environment string.
        /// </summary>
        /// <param name="envHash">The environment dictionary.</param>
        /// <returns>A string of the current environment</returns>
        protected static string BuildEnvironmentString(Dictionary<string, string> envHash)
        {
            return ConnectionUtils.BuildEnvironmentString(envHash);
        }
        #endregion
    }

    /// <summary>
    /// An enumeration of the available server types
    /// </summary>
    public enum ServerTypes
    {
        /// <summary>
        /// An undefined server type
        /// </summary>
        Other,
        /// <summary>
        /// A development server
        /// </summary>
        Development,
        /// <summary>
        /// A test server
        /// </summary>
        Test,
        /// <summary>
        /// A production server
        /// </summary>
        Production,
        /// <summary>
        /// An archive server
        /// </summary>
        Archive
    }
}