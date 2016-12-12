
using System;
using System.Collections.Generic;
using System.Data;
using RedPrairie.MOCA.Client;
using RedPrairie.MOCA.Client.Interfaces;
using RedPrairie.MOCA.Exceptions;
using System.Text;
using System.Security.Cryptography;

namespace RedPrairie.MOCA.Util
{
    /// <summary>
    /// Contains the login utility methods which involve methods that
    /// should remain internal to the system.
    /// </summary>
    internal class LoginUtils
    {
        /// <summary>
        /// Logins the specified connection.
        /// </summary>
        /// <param name="conn">The connection to execute on</param>
        /// <param name="user">The user name to login</param>
        /// <param name="password">The password of the user</param>
        /// <param name="isSingleSignon">indicates if the login is SSO</param>
        /// <param name="localeOverride">A locale override to override the results, null if not needed</param>
        /// <returns>A <see cref="DataTable"/> of user settings</returns>
        /// <exception cref="LoginFailedException">If the login fails</exception>
        public static DataTable Login(IMocaConnection conn, string user,
                                      string password, bool isSingleSignon, string localeOverride)
        {
            return Login(conn, user, password, isSingleSignon, localeOverride, false);
        }

        /// <summary>
        /// Logins the specified connection.
        /// </summary>
        /// <param name="conn">The connection to execute on</param>
        /// <param name="user">The user name to login</param>
        /// <param name="password">The password of the user</param>
        /// <param name="isSingleSignon">indicates if the login is SSO</param>
        /// <param name="localeOverride">A locale override to override the results, null if not needed</param>
        /// <param name="mergeEnvironments">if set to <c>true</c> new login settings are merged into the existing environment.
        /// Otherwise, they are replaced with a new set.</param>
        /// <returns>
        /// A <see cref="DataTable"/> of user settings
        /// </returns>
        /// <exception cref="LoginFailedException">If the login fails</exception>
        public static DataTable Login(IMocaConnection conn, string user, 
                                      string password, bool isSingleSignon, string localeOverride, bool mergeEnvironments)
        {
            return Login(conn, user, password, isSingleSignon, localeOverride, mergeEnvironments, "");
        }

        /// <summary>
        /// Logins the specified connection.
        /// </summary>
        /// <param name="conn">The connection to execute on</param>
        /// <param name="user">The user name to login</param>
        /// <param name="password">The password of the user</param>
        /// <param name="isSingleSignon">indicates if the login is SSO</param>
        /// <param name="localeOverride">A locale override to override the results, null if not needed</param>
        /// <param name="mergeEnvironments">if set to <c>true</c> new login settings are merged into the existing environment.
        /// Otherwise, they are replaced with a new set.</param>
        /// <param name="clientKey">The Client Key.</param>
        /// <returns>
        /// A <see cref="DataTable"/> of user settings
        /// </returns>
        /// <exception cref="LoginFailedException">If the login fails</exception>
        public static DataTable Login(IMocaConnection conn, string user, 
                                      string password, bool isSingleSignon, 
                                      string localeOverride, bool mergeEnvironments,
                                      string clientKey)
        {
            string safeUser = user.Replace("'", "''").ToUpper();
            string safePassword = password.Replace("'", "''");

            Command command;
            if (conn.SupportsArguments)
            {
                //Use safe parameters if possible
                string loginCommand =
                string.Format("login user where usr_id = @usr_id and usr_pswd = @usr_pswd {0}",
                              (isSingleSignon ? "and single_signon_flg = 1" : ""));

                List<IMocaArgument> args = new List<IMocaArgument>
                                          {
                                              new MocaArgument<string>("usr_id", safeUser),
                                              new MocaArgument<string>("usr_pswd", safePassword),
                                              
                                          };
                if (clientKey != null && clientKey.Length != 0)
                {
                    string clientKeyArg = GenerateClientKey(clientKey, conn.ServerKey);
                    args.Add(new MocaArgument<string>("client_key", clientKeyArg));
                }

                command = new Command(loginCommand, null, args);
            }
            else
            {
                //Method for legacy servers.
                command = new Command(
                     string.Format("login user where usr_id = '{0}' and usr_pswd = '{1}' {2}",
                              safeUser, 
                              safePassword,
                              (isSingleSignon ? "and single_signon_flg = 1" : "")));
            }

            try
            {
                ExecuteCommandResult res = conn.Execute(command);
                if (res.HasData)
                {
                    string locale = conn.Environment.ContainsKey(Constants.EnvironmentKeyLocaleID)
                                        ? conn.Environment[Constants.EnvironmentKeyLocaleID]
                                        : "US_ENGLISH";

                    locale = ConnectionUtils.GetStringValue(res.TableData.Rows[0], "locale_id", locale);
                    string key = ConnectionUtils.GetStringValue(res.TableData.Rows[0], "session_key");

                    // Set Override if necessary
                    if (!String.IsNullOrEmpty(localeOverride))
                        locale = localeOverride;

                    if (res.TableData != null && res.TableData.Rows.Count > 0)
                        user = ConnectionUtils.GetStringValue(res.TableData.Rows[0], "usr_id");

                    Dictionary<string, string> env = new Dictionary<string, string>();
                    env.Add(Constants.EnvironmentKeyLocaleID, locale);
                    env.Add(Constants.EnvironmentKeyUserID, user.ToUpper().Trim());
                    env.Add(Constants.EnvironmentKeySessionKey, key);

                    conn.Environment = (mergeEnvironments) ? ConnectionUtils.MergeEnvironmentSets(env, conn.Environment) : env;

                    return res.TableData;
                }
                
                throw new LoginFailedException("No results");
            }
            catch (MocaException e)
            {
                if (e.ErrorCode == MocaErrors.eMCS_ACCOUNT_LOCK_OUT)
                    throw new LockOutException(e.Message);
                
                throw new LoginFailedException(e.Message, e);
            }
        }

        /// <summary>
        /// Logouts the specified user on the connection.
        /// </summary>
        /// <param name="conn">The connection.</param>
        public static void Logout(IMocaConnection conn)
        {
            //Get the user ID
            string userId;
            conn.Environment.TryGetValue(Constants.EnvironmentKeyUserID, out userId);

            conn.Execute(new Command(string.Format("logout user where usr_id = \"{0}\" ", userId)));

            conn.UpdateEnvironmentSetting(Constants.EnvironmentKeyUserID, null);
            conn.UpdateEnvironmentSetting(Constants.EnvironmentKeySessionKey, null);
            conn.UpdateEnvironmentSetting(Constants.EnvironmentKeyLocaleID, null);
            conn.UpdateEnvironmentSetting(Constants.EnvironmentKeyWarehouse, null);
        }

        /// <summary>
        /// Confirms the digital signature for the user based on their cridentials.
        /// </summary>
        /// <param name="conn">The connection to execute on.</param>
        /// <param name="user">The user name to validate</param>
        /// <param name="password">The password for the user</param>
        /// <exception cref="LoginFailedException">If the confirmation fails</exception>
        public static void ConfirmDigitalSignature(IMocaConnection conn, string user, string password)
        {
            Command loginCommand;
            if (conn.SupportsArguments)
            {
                loginCommand =
                    new Command("confirm digital signature where usr_id = @usr_id and usr_pswd = @usr_pswd",
                                null,
                                new List<IMocaArgument>
                                    {
                                        new MocaArgument<string>("usr_id", user.Replace("'", "''").ToUpper()),
                                        new MocaArgument<string>("usr_pswd", password.Replace("'", "''"))
                                    });
            }
            else
            {
                string safeUser = user.Replace("'", "''").ToUpper();
                string safePassword = password.Replace("'", "''");

                loginCommand = new Command(
                    string.Format("confirm digital signature where usr_id = '{0}' and usr_pswd = '{1}'",
                              safeUser,
                              safePassword));
            }

            try
            {
                ExecuteCommandResult res = conn.Execute(loginCommand);
                if (res.HasData && 
                    res.TableData.Columns.Contains(Constants.EnvironmentKeySignatureKey.ToLower()))
                {
                    string signatureKey = ConnectionUtils.GetStringValue(res.TableData.Rows[0],
                                                         Constants.EnvironmentKeySignatureKey.ToLower());

                    if (!String.IsNullOrEmpty(signatureKey))
                    {
                        conn.UpdateEnvironmentSetting(Constants.EnvironmentKeySignatureKey, signatureKey);
                        return;
                    }
                }

                throw new LoginFailedException("No key value returned from the command");
            }
            catch (MocaException e)
            {
                throw new LoginFailedException(e.Message, e); 
            }
        }

        /// <summary>
        /// Calculate a client key, given a client ID and server ID.
        /// </summary>
        /// <param name="clientKey"></param>
        /// <param name="serverKey"></param>
        /// <returns></returns>
        public static string GenerateClientKey(string clientKey,
                                               string serverKey)
        {
            UTF8Encoding encoding = new UTF8Encoding();

            byte[] clientKeyBytes = encoding.GetBytes(clientKey);
            byte[] serverKeyBytes = encoding.GetBytes(serverKey);

            List<byte> list = new List<byte>();

            list.AddRange(clientKeyBytes);
            list.AddRange(serverKeyBytes);

            SHA1 sha = new SHA1CryptoServiceProvider();

            byte[] hash = sha.ComputeHash(list.ToArray());

            int pound = clientKey.IndexOf("#");
            string keyName;

            if (pound != -1)
            {
                keyName = clientKey.Substring(0, pound);
            }
            else
            {
                keyName = clientKey;
            }

            string key = keyName + '/' + System.Convert.ToBase64String(hash);

            return key;
        }
    }
}
