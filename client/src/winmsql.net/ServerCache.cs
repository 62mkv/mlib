using System;
using System.Drawing;
using System.Collections;
using System.Collections.Generic;
using RedPrairie.Configuration;
using RedPrairie.MOCA.Client;

namespace RedPrairie.MCS.WinMSQL
{
    /// <summary>
    /// 
    /// </summary>
    public class ServerCache
    {
        #region Private Variables

        private Dictionary<string, ServerInfo> _configuredServers =
            new Dictionary<string, ServerInfo>();
        private Dictionary<string, ServerInfo> _manualServers =
            new Dictionary<string, ServerInfo>();

        #endregion

        #region Delegates

        /// <summary>
        /// This delegate is used to subscribe to cache updates
        /// </summary>
        /// <param name="sender">This is the cache that is getting updated</param>
        /// <param name="e">This is not currently used</param>
        public delegate void CacheUpdatedHandler(object sender, EventArgs e);

        #endregion

        #region Events

        /// <summary>
        /// This event is fired everytime the cache is updated
        /// </summary>
        public event CacheUpdatedHandler CacheUpdated;

        #endregion

        #region Constructors

        /// <summary>
        /// The constructor reads the cache out of the configuration
        /// file.
        /// </summary>
        public ServerCache()
        {
            ReadConfiguredLoginInfo();
            ReadManualLoginInfo();
        }

        #endregion

        #region Public Methods

        /// <summary>
        /// This method constructs a list of strings containing
        /// all of the servers held by the cache.
        /// </summary>
        /// <param name="serverSeparator">This is the label that separates configured and manual servers</param>
        /// <returns>A complete list of servers held by this cache</returns>
        public ArrayList GetServerHistory(string serverSeparator)
        {
            ArrayList labels = new ArrayList();

            foreach (string key in _configuredServers.Keys)
            {
                labels.Add(key);
            }
            labels.Sort();

            labels.Add(serverSeparator);

            foreach (string key in _manualServers.Keys)
            {
                labels.Add(key);
            }

            return labels;
        }

        /// <summary>
        /// This method gets the server information held by the cache.
        /// Configured servers are hashed by their server name, while
        /// manual servers are hashed by their URL.
        /// </summary>
        /// <param name="label">This is the key that the server is hashed under.</param>
        /// <returns>The server information requested, or null if no server was found</returns>
        public ServerInfo GetLoginInfo(string label)
        {
            // Check to see if we have a configured server with that label
            if (_configuredServers.ContainsKey(label))
            {
                return _configuredServers[label];
            }
            else if (_manualServers.ContainsKey(label))
            {
                return _manualServers[label];
            }
            else
            {
                return null;
            }
        }

        /// <summary>
        /// This method is used for adding or updating login information.
        /// If the label matches a configured server, then that configured
        /// server is updated; otherwise, it updates or adds a manual server.
        /// 
        /// After the update is complete, the CacheUpdated event is triggered.
        /// </summary>
        /// <param name="info"></param>
        public void UpdateLoginInfo(ServerInfo info)
        {
            if (_configuredServers.ContainsKey(info.Name))
            {
                _configuredServers[info.Name] = info;
            }
            else if (_manualServers.ContainsKey(info.Host))
            {
                _manualServers[info.Host] = info;
            }
            else
            {
                _manualServers.Add(info.Host, info);
            }

            CacheUpdated(this, new EventArgs());
        }

        /// <summary>
        /// This method loops over all of the manual connections and saves them
        /// to the user configuration file.
        /// </summary>
        public void WriteManualLoginInfo()
        {
            int counter = 1;
            foreach (ServerInfo srv in _manualServers.Values)
            {
                ConfigurationSettings.SetValue(ConfigType.User, srv.Host, "URLHistory" + counter++);
            }
            Configuration.ConfigurationSettings.Save(Configuration.ConfigType.User);
        }

        #endregion

        #region Private Methods

        /// <summary>
        /// This method gets all of the defined MOCA servers in the configuration,
        /// loops over them, and creates corresponding ServerInfo objects for them.
        /// </summary>
        private void ReadConfiguredLoginInfo()
        {
            string[] servers = ConfigurationSettings.GetSubKeyNames("Name", Configuration.Defines.MOCA_SERVERS);
            if (servers != null)
            {
                foreach (string serverName in servers)
                {
                    ServerInfo info = new ServerInfo(serverName);
                    if (_configuredServers.ContainsKey(serverName))
                    {
                        _configuredServers[serverName] = info;
                    }
                    else
                    {
                        _configuredServers.Add(serverName, info);
                    }
                }
            }
        }

        /// <summary>
        /// This method loops over all of the manual connections stored in
        /// the configuration and creates a ServerInfo object for each one.
        /// </summary>
        private void ReadManualLoginInfo()
        {
            int ii = 1;
            string url = null;
            do
            {
                string key = "URLHistory" + ii++;
                url = Configuration.ConfigurationSettings.GetValue(Configuration.ConfigType.User, key);
                if (url != null)
                {
                    ServerInfo login = new ServerInfo(url, url, winMSQLTab.DEFAULT_ENVIRONMENT);
                    if (_manualServers.ContainsKey(url))
                    {
                        // Overwrite existing connection...
                        _manualServers[url] = login;
                    }
                    else
                    {
                        // Or add a new connection...
                        _manualServers.Add(url, login);
                    }
                }
            } while (url != null);
        }

        #endregion
    }
}
