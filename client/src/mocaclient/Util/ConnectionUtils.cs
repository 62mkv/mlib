using System;
using System.Collections.Generic;
using System.Data;
using System.Globalization;
using System.Text;
using System.Web;

namespace RedPrairie.MOCA.Util
{
    /// <summary>
    /// A utility class for assisting with common MOCA functions.
    /// </summary>
    public class ConnectionUtils
    {
        /// <summary>
        /// Parses the environment string to a Dictionary
        /// </summary>
        /// <param name="envString">The string to parse</param>
        /// <returns>A dictionary of the results</returns>
        public static Dictionary<string, string> ParseEnvironmentString(string envString)
        {
            Dictionary<string, string> env = new Dictionary<string, string>();

            if (envString != null)
            {
                string[] elements = envString.Split(':');
                for (int i = 0; i < elements.Length; i++)
                {
                    string[] tmp = elements[i].Split(new char[] { '=' }, 2);
                    if (tmp.Length == 2)
                    {
                        if (!env.ContainsKey(tmp[0]))
                            env.Add(tmp[0], tmp[1]);
                        else
                            env[tmp[0]] = tmp[1];
                    }
                }
            }

            return env;
        }

        /// <summary>
        /// Builds and environment string
        /// </summary>
        /// <param name="env">The environment variable hash to parse</param>
        /// <returns>A parsed environment string</returns>
        public static string BuildEnvironmentString(Dictionary<string, string> env)
        {
            StringBuilder buf = new StringBuilder();

            if (env != null)
            {
                foreach (KeyValuePair<string, string> keyValuePair in env)
                {
                    buf.AppendFormat("{0}{1}={2}",
                                     (buf.Length > 0) ? ":" : "",
                                     keyValuePair.Key.ToUpper(),
                                     keyValuePair.Value);
                }
            }

            return buf.ToString();
        }

        /// <summary>
        /// Builds an Environment string without a SESSION_ID Field;
        /// </summary>
        /// <param name="env">The environment variable hash to parse</param>
        /// <returns>A parsed environment string</returns>
        public static string BuildWebEnvironmentString(Dictionary<string, string> env)
        {
            Dictionary<string, string> filterEnv = new Dictionary<string, string>(env);

            if (filterEnv.ContainsKey(Constants.EnvironmentKeySessionKey))
                filterEnv.Remove(Constants.EnvironmentKeySessionKey);

            return HttpUtility.UrlEncode(BuildEnvironmentString(filterEnv));
        }
       
        /// <summary>
        /// Merges the environment sets using the base as not re-defineable unless
        /// it is not part of the un-editable collection
        /// </summary>
        /// <param name="baseEnv">The base environment variable set.</param>
        /// <param name="mergeEnv">The merging environment variable set.</param>
        /// <returns>A merged environment variable set</returns>
        public static Dictionary<string, string> MergeEnvironmentSets(Dictionary<string, string> baseEnv, Dictionary<string, string> mergeEnv)
        {
            Dictionary<string, string> newItems = new Dictionary<string, string>(baseEnv, StringComparer.InvariantCultureIgnoreCase);
            
            foreach (KeyValuePair<string, string> mergePair in mergeEnv)
            {
                if (!newItems.ContainsKey(mergePair.Key))
                {
                    newItems.Add(mergePair.Key, mergePair.Value);
                }
                else if (newItems.ContainsKey(mergePair.Key) && 
                         CanChangeEnvironmentVariable(mergePair.Key))
                {
                    newItems[mergePair.Key] = mergePair.Value;
                }
            }

            return newItems;
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
            return string.Compare(variableKey, Constants.EnvironmentKeyWarehouse, true) != 0 &&
                   string.Compare(variableKey, Constants.EnvironmentKeyUserID, true) != 0 &&
                   string.Compare(variableKey, Constants.EnvironmentKeySessionKey, true) != 0 &&
                   string.Compare(variableKey, Constants.EnvironmentKeyLocaleID, true) != 0 &&
                   string.Compare(variableKey, Constants.EnvironmentKeySignatureKey, true) != 0;
        }

        /// <summary>
        /// Gets the string value of the specified cell.
        /// </summary>
        /// <param name="row">The row to get the value from</param>
        /// <param name="columnName">Name of the column to get</param>
        /// <returns>An empty string if null otherwise the value</returns>
        public static string GetStringValue(DataRow row, string columnName)
        {
            return GetStringValue(row, columnName, string.Empty);
        }

        /// <summary>
        /// Gets the string value of the specified cell.
        /// </summary>
        /// <param name="row">The row to get the value from</param>
        /// <param name="columnName">Name of the column to get</param>
        /// <param name="defaultValue">The default value.</param>
        /// <returns>An empty string if null otherwise the value</returns>
        public static string GetStringValue(DataRow row, string columnName, string defaultValue)
        {
            return (row != null && 
                    row.Table.Columns.IndexOf(columnName) != -1 &&
                    row[columnName] != null)
                       ? row[columnName].ToString()
                       : defaultValue;
        }

        /// <summary>
        /// Gets the int value of the specified cell.
        /// </summary>
        /// <param name="row">The row to get the value from</param>
        /// <param name="columnName">Name of the column to get</param>
        /// <return>An int value</return>
        public static int GetIntValue(DataRow row, string columnName)
        {
            return GetIntValue(row, columnName, 0);
        }

        /// <summary>
        /// Gets the int value of the specified cell.
        /// </summary>
        /// <param name="row">The row to get the value from</param>
        /// <param name="columnName">Name of the column to get</param>
        /// <param name="defaultValue">The default value.</param>
        /// <returns></returns>
        /// <return>An int value</return>
        public static int GetIntValue(DataRow row, string columnName, int defaultValue)
        {
            if (row != null && row.Table.Columns.IndexOf(columnName) != -1 &&
                row[columnName] != null)
            {
                int parsedVal;

                return (Int32.TryParse(row[columnName].ToString(), out parsedVal))
                        ? parsedVal : defaultValue;
            }
            
            return defaultValue;
        }

        /// <summary>
        /// Gets the boolean value of the specified cell.
        /// </summary>
        /// <param name="row">The row to get the value from</param>
        /// <param name="columnName">Name of the column to get</param>
        /// <returns>An boolean value</returns>
        public static bool GetBooleanValue(DataRow row, string columnName)
        {
            return GetBooleanValue(row, columnName, false);
        }

        /// <summary>
        /// Gets the boolean value of the specified cell.
        /// </summary>
        /// <param name="row">The row to get the value from</param>
        /// <param name="columnName">Name of the column to get</param>
        /// <param name="defaultValue">The default value.</param>
        /// <returns>An boolean value</returns>
        public static bool GetBooleanValue(DataRow row, string columnName, bool defaultValue)
        {
            if (row != null && row.Table.Columns.IndexOf(columnName) != -1 &&
               row[columnName] != null && !(row[columnName] is DBNull))
            {
                var dt = row.Table.Columns[columnName].DataType;
                if (dt == typeof(bool) || dt == typeof(Boolean))
                {
                    return (bool)row[columnName];
                }

                if (dt == typeof(int))
                {
                    return (int)row[columnName] == 0 ? false : true;
                }

                if (dt == typeof(String) || dt == typeof(string))
                {
                    var sValue = (string)row[columnName];

                    int intVal;
                    if(int.TryParse(sValue, NumberStyles.Integer, 
                        CultureInfo.InvariantCulture, out intVal))
                    {
                        return intVal == 0 ? false : true;
                    }

                    if(sValue == "T" || sValue == "t")
                    {
                        return true;
                    }

                    if(sValue == "F" || sValue == "f")
                    {
                        return false;
                    }

                    bool parsedValue;
                    if(bool.TryParse(sValue, out parsedValue))
                    {
                        return parsedValue;
                    }
                }
            }

            return defaultValue;
        }

        /// <summary>
        /// Gets the date value of the specified cell.
        /// </summary>
        /// <param name="row">The row to get the value from</param>
        /// <param name="columnName">Name of the column to get</param>
        /// <returns>A <see cref="DateTime"/> or <see cref="DateTime.MinValue"/> if errored</returns>
        public static DateTime GetDateValue(DataRow row, string columnName)
        {
            if (row != null && row.Table.Columns.IndexOf(columnName) != -1 &&
                row[columnName] != null)
            {
                DateTime parsedVal;

                return (DateTime.TryParse(row[columnName].ToString(), out parsedVal))
                        ? parsedVal : DateTime.MinValue;
            }
            
            return DateTime.MinValue;
        }

        /// <summary>
        /// Determines whether a web connection should be made to the specified host.
        /// </summary>
        /// <param name="host">The host name.</param>
        /// <returns>
        /// 	<c>true</c> if a web connection should be created otherwise, <c>false</c>.
        /// </returns>
        public static bool IsWebConnection(string host)
        {
            // Default assumption is now a web server, due to migration to NG
            if (String.IsNullOrEmpty(host)) return true;

            return (host.StartsWith("https://") || host.StartsWith("http://"));
        }

        /// <summary>
        /// Parses a URL to retrieve the host/port combination.  This is used when 
        /// trying to connect to a legacy system via a DirectConnection where the
        /// url is entered as host:port
        /// </summary>
        /// <param name="url">URL string in the format of host:port</param>
        /// <param name="host">Host String parsed</param>
        /// <param name="port">Port number parsed</param>
        public static void GetHostPortFromUrl(string url, out string host, out int port)
        {
            string [] split = url.Split(new char[] {':'});
            if (split.Length != 2)
                throw new ArgumentException(string.Format ("Invalid host/port pairing: {0}", url));
            
            if (split[0].CompareTo("https") == 0 ||
                split[0].CompareTo("http") == 0)
                throw new ArgumentException(string.Format ("Invalid host/port pairing: {0}", url));
            host = split[0];
            int.TryParse (split[1], out port);

        }
        
        
    }
}