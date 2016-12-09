using System;
using System.Drawing;
using System.Collections;
using System.Collections.Generic;
using RedPrairie.Configuration;
using RedPrairie.MOCA.Client;

namespace RedPrairie.MCS.WinMSQL
{
    /// <summary>
    /// This class holds server-specific information.
    /// It extends RedPrairie.MOCA.Client.ServerInfo.
    /// </summary>
    public class ServerInfo : MOCA.Client.ServerInfo
    {
        #region Private Variables

        private Color tabColor = Color.Empty;

        #endregion

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="ServerInfo"/> class.
        /// This calls the RedPrairie.MOCA.Client.ServerInfo constructor.
        /// This constructor defaults the port to 0.
        /// </summary>
        /// <param name="name">The name of the server</param>
        /// <param name="host">The URL for the server</param>
        /// <param name="environment">The environment variable string for the server</param>
        public ServerInfo(string name, string host, string environment)
            : base(name, host, 0, environment)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ServerInfo"/> class.
        /// This calls the RedPrairie.MOCA.Client.ServerInfo constructor.
        /// After calling the base constructor, this constructor attempts
        /// to pull server information from the configuration.
        /// </summary>
        /// <param name="name">The name of the server</param>
        public ServerInfo(string name)
            : base(name)
        {
            string server_key = Configuration.Defines.MOCA_SERVER(name);

            host = ConfigurationSettings.GetValue(Configuration.Defines.MOCA_SERVERS,
                                                  server_key, Configuration.Defines.HOST_NAME);

            try
            {
                if (!string.IsNullOrEmpty(host) && !host.StartsWith("http"))
                {
                    //this means we have a legacy instance
                    string portString = ConfigurationSettings.GetValue(Configuration.Defines.MOCA_SERVERS,
                        server_key, Configuration.Defines.PORT_NUMBER);
                    int legacyPort = 0;
                    int.TryParse(portString, out legacyPort);
                    host = string.Format("{0}:{1}", host, legacyPort);
                    port = 0;
                }
            }
            catch (RedPrairie.Exceptions.RPException)
            {
            }
            catch (Exception)
            {
            }

            try
            {
                // We don't care if it's not there.
                UserID = ConfigurationSettings.GetValue(Configuration.Defines.MOCA_SERVERS,
                                                        server_key, Configuration.Defines.USER_ID);
                Password = ConfigurationSettings.GetValue(Configuration.Defines.MOCA_SERVERS,
                                                          server_key, Configuration.Defines.PASSWORD);
            }
            catch (RedPrairie.Exceptions.RPException)
            {
            }

            // Read the Trace information if it's there.
            traceFileName = ConfigurationSettings.GetValue(Configuration.Defines.MOCA_SERVERS,
                                                            server_key,
                                                            Configuration.Defines.TraceFileName);
            traceLevels = ConfigurationSettings.GetValue(Configuration.Defines.MOCA_SERVERS,
                                                          server_key,
                                                          Configuration.Defines.TraceLevels);

            Dictionary<string, string> environmentHash = new Dictionary<string, string>();

            // At this time we are using SERVER_ENVIRONMENT, which is common to all the connections
            // instead of client environment, which is connection specific.  We do that to preserve
            // backward compatibility.
            string[] env_settings = ConfigurationSettings.GetValueNames("Name",
                Configuration.Defines.MCS_SETTINGS, Configuration.Defines.SERVER_ENVIRONMENT);

            if (env_settings != null && env_settings.Length > 0)
            {
                foreach (string Key in env_settings)
                {
                    string path = Configuration.Defines.ENV_VARIABLE(Key);

                    string Value = ConfigurationSettings.GetValue(Configuration.Defines.MCS_SETTINGS,
                        Configuration.Defines.SERVER_ENVIRONMENT, path);

                    if (Key.StartsWith("COLOR"))
                    {
                        tabColor = Color.FromName(Value);
                    }
                    else if (environmentHash.ContainsKey(Key))
                    {
                        environmentHash[Key] = Value;
                    }
                    else
                    {
                        environmentHash.Add(Key, Value);
                    }
                }
            }

            // We've already added the SERVER_ENVIRONMENT variables, so lets now add the
            // CLIENT_ENVIRONMNET variables.
            env_settings = ConfigurationSettings.GetValueNames("Name",
                Configuration.Defines.MOCA_SERVERS, Configuration.Defines.MOCA_SERVER(name),
                Configuration.Defines.CLIENT_ENVIRONMENT);

            if (env_settings != null)
            {
                foreach (string envVarName in env_settings)
                {
                    string envVarValue = ConfigurationSettings.GetValue(
                        Configuration.Defines.MOCA_SERVERS, Configuration.Defines.MOCA_SERVER(name),
                        Configuration.Defines.CLIENT_ENVIRONMENT,
                        Configuration.Defines.ENV_VARIABLE(envVarName));

                    if (envVarName.StartsWith("COLOR"))
                    {
                        tabColor = Color.FromName(envVarValue);
                    }
                    else if (environmentHash.ContainsKey(envVarName))
                    {
                        environmentHash[envVarName] = envVarValue;
                    }
                    else
                    {
                        environmentHash.Add(envVarName, envVarValue);
                    }
                }
            }
            environment = BuildEnvironmentString(environmentHash);
        }

        #endregion

        #region Properties

        /// <summary>
        /// This is the color of the tab.
        /// If the tab is not set to a color, then it defaults to Color.Empty.
        /// </summary>
        public Color TabColor
        {
            get
            {
                return tabColor;
            }
        }

        #endregion
    }
}
