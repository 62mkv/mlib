using System;
using System.Collections.Generic;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// ConnectionBase is an abstract class that provides common fields 
    /// and properties to the different connection classes
    /// </summary>
    internal abstract class ConnectionBase
    {
        #region Protected Fields

        /// <summary>
        /// The connection host name
        /// </summary>
        protected string host;
        /// <summary>
        /// The connection port number
        /// </summary>
        protected int port;
        /// <summary>
        /// The Dictionary containing environment arguments
        /// </summary>
        protected Dictionary<string, string> env;
        /// <summary>
        /// The parsed environment string
        /// </summary>
        protected string environmentString;
        /// <summary>
        /// The internal connection ID
        /// </summary>
        protected string connectionID;

        #endregion

        #region Constructor
        /// <summary>
        /// Initializes a new instance of the <see cref="ConnectionBase"/> class.
        /// </summary>
        protected ConnectionBase()
        {
            connectionID = Guid.NewGuid().ToString();
        }
        #endregion

        #region Public Properties

        /// <summary>
        /// Gets or Sets the ID of the application that is calling the execute method
        /// </summary>
        public string ApplicationID
        {
            get
            {
                return env.ContainsKey(Constants.MocaApplicationID)
                           ? env[Constants.MocaApplicationID]
                           : string.Empty;
            }
            set { UpdateEnvironmentSetting(Constants.MocaApplicationID, value); }
        }

        /// <summary>
        /// Gets or sets the connection ID.
        /// </summary>
        /// <value></value>
        public string ConnectionID
        {
            get { return connectionID; }
            set { connectionID = value; }
        }

        #endregion

        #region Protected Methods

        /// <summary>
        /// Updates the environment string.
        /// </summary>
        protected virtual void UpdateEnvironmentString()
        {
            environmentString = ConnectionUtils.BuildEnvironmentString(env);
        }

        #endregion

        #region Public Methods
        
        /// <summary>
        /// Closes this instance.
        /// </summary>
        public virtual void Close()
        {
            env.Clear();
            UpdateEnvironmentString();
        }


        /// <summary>
        /// Updates an environment setting.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <param name="value">The value to replace it.</param>
        public virtual void UpdateEnvironmentSetting(string key, string value)
        {
            bool hasChanged = false;
            if (env.ContainsKey(key))
            {
                if (env[key] != value)
                {
                    if (String.IsNullOrEmpty(value))
                        env.Remove(key);
                    else
                        env[key] = value;

                    hasChanged = true;
                }
            }
            else if (!String.IsNullOrEmpty(value))
            {
                env.Add(key, value);
                hasChanged = true;
            }

            if (hasChanged)
                UpdateEnvironmentString();
        }

        #endregion
    }
}