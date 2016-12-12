using System;
using System.ComponentModel;
using System.Data;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// Enumerates the different types of execution events
    /// </summary>
    public enum ExecutionEvent
    {
        /// <summary>
        /// The event is being raised before a command is executed
        /// </summary>
        PreExecution = 0,
        /// <summary>
        /// The event is being raised after a command is executed
        /// </summary>
        ExecutionComplete
    }

    /// <summary>
    /// Enumerates the different types of user based events
    /// </summary>
    public enum UserEventState
    {
        /// <summary>
        /// A user has logged in successfully.
        /// </summary>
        UserLoggedIn = 0,
        /// <summary>
        /// A user has logged out successfully.
        /// </summary>
        UserLoggedOut,
        /// <summary>
        /// A user is logging in, but it is not yet complete.
        /// </summary>
        UserLoggingIn,
        /// <summary>
        /// A user is logging out, but it is not yet complete.
        /// </summary>
        UserLoggingOut
    }

    /// <summary>
    /// Enumerates the different types of connection based events
    /// </summary>
    public enum ConnectionEventState
    {
        /// <summary>
        /// The connection has connected
        /// </summary>
        Connected = 0,
        /// <summary>
        /// The connection has disconnected
        /// </summary>
        Disconnected,
        /// <summary>
        /// Tracing has been enabled for the connection
        /// </summary>
        TracingEnabled,
        /// <summary>
        /// Tracing has been disabled for the connection
        /// </summary>
        TracingDisabled
    }

    #region Event Argument Class Definitions

    /// <summary>
    /// An event argument class for a command execution event
    /// </summary>
    public class ExecutionEventArgs : EventArgs
    {
        private string _command;
        private ExecutionEvent _event;
        private int _status;
        private DataSet _data;
        private string _errorText;

        /// <summary>
        /// Initializes a new instance of the <see cref="ExecutionEventArgs"/> class.
        /// </summary>
        /// <param name="executionEvent">The execution event.</param>
        /// <param name="command">The command.</param>
        public ExecutionEventArgs(ExecutionEvent executionEvent,
                                  ref string command)
        {
            _command = command;
            _status = 0;
            _event = executionEvent;
            _data = null;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ExecutionEventArgs"/> class.
        /// </summary>
        /// <param name="executionEvent">The execution event.</param>
        /// <param name="command">The command.</param>
        /// <param name="status">The status of the command.</param>
        /// <param name="data">The data.</param>
        public ExecutionEventArgs(ExecutionEvent executionEvent,
                                  ref string command,
                                  int status,
                                  DataSet data)
        {
            _command = command;
            _event = executionEvent;
            _status = status;
            _data = data;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ExecutionEventArgs"/> class.
        /// </summary>
        /// <param name="executionEvent">The execution event.</param>
        /// <param name="command">The command.</param>
        /// <param name="status">The status.</param>
        /// <param name="data">The data.</param>
        /// <param name="errorText">The error text if it failed.</param>
        public ExecutionEventArgs(ExecutionEvent executionEvent,
                                  ref string command,
                                  int status,
                                  DataSet data,
                                  string errorText)
        {
            _command = command;
            _event = executionEvent;
            _status = status;
            _data = data;
            _errorText = errorText;
        }

        /// <summary>
        /// Gets or sets the command.
        /// </summary>
        /// <value>The command.</value>
        public string Command
        {
            get { return _command; }
            set { _command = value; }
        }

        /// <summary>
        /// Gets the execution event type.
        /// </summary>
        /// <value>The event type.</value>
        public ExecutionEvent Event
        {
            get { return _event; }
        }

        /// <summary>
        /// Gets the status of the command.
        /// </summary>
        /// <value>The status code.</value>
        public int Status
        {
            get { return _status; }
        }

        /// <summary>
        /// Gets the data returned by the command.
        /// </summary>
        /// <value>The data from the command.</value>
        public DataSet Data
        {
            get { return _data; }
        }

        /// <summary>
        /// Gets the error text if the command failed.
        /// </summary>
        /// <value>The error text.</value>
        public string ErrorText
        {
            get { return _errorText; }
        }
    }

    /// <summary>
    /// An event argument class for a user based event
    /// </summary>
    public class UserEventArgs : EventArgs
    {
        private LoginInfo _loginInfo;
        private UserEventState _state;

        /// <summary>
        /// Initializes a new instance of the <see cref="UserEventArgs"/> class.
        /// </summary>
        /// <param name="state">The event type.</param>
        /// <param name="info">The login information.</param>
        public UserEventArgs(UserEventState state, LoginInfo info)
        {
            _loginInfo = info;
            _state = state;
        }

        /// <summary>
        /// Gets the type of login event.
        /// </summary>
        /// <value>The state.</value>
        public UserEventState State
        {
            get { return _state; }
        }

        /// <summary>
        /// Gets the login information for the user.
        /// </summary>
        /// <value>The login info.</value>
        public LoginInfo LoginInfo
        {
            get { return _loginInfo; }
        }
    }

    /// <summary>
    /// An event argument class for a connection based event
    /// </summary>
    public class ConnectionEventArgs : EventArgs
    {
        private ServerInfo _serverInfo;
        private ConnectionEventState _state;

        /// <summary>
        /// Initializes a new instance of the <see cref="ConnectionEventArgs"/> class.
        /// </summary>
        /// <param name="state">The event type.</param>
        /// <param name="info">The connection information.</param>
        public ConnectionEventArgs(ConnectionEventState state, ServerInfo info)
        {
            _state = state;
            _serverInfo = info;
        }

        /// <summary>
        /// Gets The event type.
        /// </summary>
        /// <value>The state.</value>
        public ConnectionEventState State
        {
            get { return _state; }
        }

        /// <summary>
        /// Gets the connect info.
        /// </summary>
        /// <value>The connect information.</value>
        public ServerInfo ConnectInfo
        {
            get { return _serverInfo; }
        }
    }

    /// <summary>
    /// Event arguments for when an environment variable has changed
    /// </summary>
    public class ServerEvnChangeArgs : EventArgs
    {
        private string key;
        private string value;

        /// <summary>
        /// Initializes a new instance of the <see cref="ServerEvnChangeArgs"/> class.
        /// </summary>
        /// <param name="key">The environment variable key.</param>
        /// <param name="value">The environment variable value.</param>
        public ServerEvnChangeArgs(string key, string value)
        {
            this.key = key;
            this.value = value;
        }


        /// <summary>
        /// Gets or sets the environment variable key.
        /// </summary>
        /// <value>The key.</value>
        public string Key
        {
            get { return key; }
            set { key = value; }
        }

        /// <summary>
        /// Gets or sets the environment variable value.
        /// </summary>
        /// <value>The value.</value>
        public string Value
        {
            get { return value; }
            set { this.value = value; }
        }
    }

    /// <summary>
    /// An event argument class for a digital signature challenge
    /// </summary>
    public class DigitalSignatureEventArgs : CancelEventArgs
    {
        private readonly bool overrideRequired = false;
        private readonly string message = null;

        /// <summary>
        /// Initializes a new instance of the <see cref="DigitalSignatureEventArgs"/> class.
        /// </summary>
        /// <param name="overrideRequired">if set to <c>true</c>  a user override is required.</param>
        /// <param name="message">The custom error message.</param>
        public DigitalSignatureEventArgs(bool overrideRequired, string message)
        {
            this.overrideRequired = overrideRequired;
            this.message = message;
        }

        /// <summary>
        /// Gets a value indicating whether a user override is required.
        /// </summary>
        /// <value><c>true</c> if a user override is required; otherwise, <c>false</c>.</value>
        public bool OverrideRequired
        {
            get { return overrideRequired; }
        }

        /// <summary>
        /// Gets the custom error message.
        /// </summary>
        /// <value>The error message.</value>
        public string ErrorMessage
        {
            get { return message; }
        }
    }

    /// <summary>
    /// An event argument class for a session timeout
    /// </summary>
    public class ServerTimeoutEventArgs : CancelEventArgs
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="ServerTimeoutEventArgs"/> class.
        /// </summary>
        public ServerTimeoutEventArgs()
        {
        }

   }

    #endregion
}