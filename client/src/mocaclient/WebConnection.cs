using System;
using System.Collections.Generic;
using System.Data;
using System.IO;
using System.IO.Compression;
using System.Net;
using System.Net.Cache;
using System.Text;
using System.Threading;
using RedPrairie.MOCA.Client.Encoding.Xml;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client
{
    ///<summary>
    /// WebConnection is an override of the <see cref="DirectConnection"/>
    /// class specifically targeted at https based connections to a MOCA server.
    ///</summary>
    internal class WebConnection : DirectConnection
    {
        #region Private Fields
        private Uri _baseUri;
        private bool _connected;
        private string _sessionId;
        private CookieContainer _cookies = new CookieContainer();

        private static readonly object _lockObject = new object();

        #endregion

        #region Constructors / Deconstructors

        /// <summary>
        /// Initializes a new instance of the <see cref="WebConnection"/> class.
        /// </summary>
        /// <param name="host">The host to connect to.</param>
        /// <param name="port">The port to connect to.</param>
        public WebConnection(string host, int port)
            : this(host, port, null)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="WebConnection"/> class.
        /// </summary>
        /// <param name="host">The host to connect to.</param>
        /// <param name="port">The port to connect to.</param>
        /// <param name="env">A mapping of names to values that represents the MOCA
        /// environment.  The passed variables are available to any called
        /// server-side components.  If this argument is passed as null, no
        /// environment entries will be passed.</param>
        /// <throws>
        /// <see cref="MocaException"/> if an error occurs connecting to the remote host.</throws>
        public WebConnection(string host, int port, Dictionary<string, string> env)
        {
            if (host == null)
                throw new ArgumentException("Host cannot be null");

            base.host = host;
            base.port = port;

            Environment = env;

            _serverInfo = new RedPrairie.MOCA.Client.ServerInfo(
                "", host, port, ConnectionUtils.BuildEnvironmentString(env));
        }


        /// <summary>
        /// Releases unmanaged resources and performs other cleanup operations before the
        /// <see cref="WebConnection"/> is reclaimed by garbage collection.
        /// </summary>
        ~WebConnection()
        {
            DisposeInternal();
        }

        #endregion

        #region IMocaConnection Member Overrides

        /// <summary>
        /// Gets if the connection is connected
        /// </summary>
        /// <value></value>
        public override bool Connected
        {
            get { return _connected; }
        }

        /// <summary>
        /// Gets a value indicating whether the connection supports arguments.
        /// </summary>
        /// <value><c>true</c> if the connection supports arguments; otherwise, <c>false</c>.</value>
        public override bool SupportsArguments
        {
            get { return true; }
        }

        /// <summary>
        /// Checks to see if the connection to the server has closed
        /// </summary>
        /// <exception cref="IOException">This is thrown if the socket has closed</exception>
        protected override void CheckForServerClose()
        {
            //do nothing for now
        }

        /// <summary>
        /// Establishes a connection with the server
        /// </summary>
        public override void Connect()
        {
            try
            {
                InternalExecute(new Command("ping"), 30000);
            }
            catch (Exception e)
            {
                throw new ConnectionFailedException("Unable to connect.", e);
            }
            
            _connected = true;
        }

        /// <summary> 
        /// Closes the connection.  Any attempt to execute further commands on
        /// a connection that has been closed will fail.
        /// </summary>
        public override void Close()
        {
            if (_connected)
            {
                //Send empty command
                try
                {
                    var request = SendWebRequest(new Command(null));
                    GetResponse(request);
                }
                catch (ProtocolException)
                {
                }
            }

            _baseUri = null;
            _sessionId = null;
            _connected = false;
            base.Close();
        }

        #endregion

        #region Protected Method Overrides

        /// <summary>
        /// The internal method that handles the actual execution of the command.
        /// </summary>
        /// <param name="command">The command that is sent.</param>
        /// <returns>A <see cref="DataSet"/> of the results</returns>
        /// <exception cref="ProtocolException">An error occurred connecting to the resource</exception>
        protected override DataSet InternalExecute(Command command)
        {
            var request = SendWebRequest(command);
            var response = GetResponse(request);
            return ParseResults(response, command.CommandText);
        }

        /// <summary>
        /// The internal method that handles the actual execution of the command.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="mappingData">The mapping data.</param>
        /// <param name="resolver">The resolver.</param>
        /// <returns>An object of the collection</returns>
        protected override object InternalObjectExecute(Command command, MappingData mappingData, IObjectResolver resolver)
        {
            var request = SendWebRequest(command);
            var response = GetResponse(request);
            return ParseResults(response, command.CommandText, mappingData, resolver);
        }

        /// <summary>
        /// The internal method that handles the actual start of an asynchronous execution.
        /// </summary>
        /// <param name="callBack">The callback method that uses the results</param>
        protected override void InternalAsyncExecute(InternalCallBackEventArgs callBack)
        {
            var thread = new Thread(WebRequestCompleted);
            thread.Start(callBack);
        }

        /// <summary>
        /// Updates the environment string
        /// </summary>
        protected override void UpdateEnvironmentString()
        {
            environmentString = XmlRequestEncoder.BuildXmlEnvironmentString(env);
        }

        #endregion

        #region Private Methods

        /// <summary>
        /// Builds the appropriate command string to send the server.
        /// </summary>
        /// <param name="command">The command to send</param>
        /// <returns>A built <see cref="Uri"/></returns>
        private byte[] BuildQueryString(Command command)
        {
            var queryBuilder = new StringBuilder();

            string session;
            lock (_lockObject)
            {
                session = _sessionId;
            }

            XmlRequestEncoder.EncodeRequest(command.CommandText, session, environmentString, command.Context, command.Arguments, AutoCommit, queryBuilder);

            return System.Text.Encoding.UTF8.GetBytes(queryBuilder.ToString());
        }

        /// <summary>
        /// Checks to see if the session id is populated.
        /// </summary>
        /// <param name="contentHandler">The content handler.</param>
        private void CheckSessionId(IContentHandler contentHandler)
        {
            if ((String.IsNullOrEmpty(contentHandler.SessionId) &&
                 String.IsNullOrEmpty(_sessionId)) ||
                _sessionId == contentHandler.SessionId)
                return;

            lock (_lockObject)
            {
                _sessionId = contentHandler.SessionId;
            }
            CreateBaseUri();
        }

        /// <summary>
        /// Creates the base URI for all requests.
        /// </summary>
        /// <returns>A <see cref="UriBuilder"/> to use with the request.</returns>
        private Uri CreateBaseUri()
        {
            var builder = host.StartsWith("http")
                                     ? new UriBuilder(host)
                                     : new UriBuilder { Host = host };

            if (port > 0)
            {
                builder.Port = port;
            }

            if (String.IsNullOrEmpty(builder.Path) || builder.Path == "/")
            {
                builder.Path += (builder.Path == "/") ? string.Empty : "/";
                builder.Path += "service";
            }

            if (!String.IsNullOrEmpty(_sessionId))
            {
                lock (_lockObject)
                {
                    builder.Query = string.Format("msession={0}", _sessionId);
                }
            }

            _baseUri = builder.Uri;

            return _baseUri;
        }

        /// <summary>
        /// Creates the web request.
        /// </summary>
        /// <param name="timeout">The timeout.</param>
        /// <returns>A new <see cref="WebRequest"/>.</returns>
        private WebRequest CreateWebRequest(int timeout)
        {
            var baseUri = _baseUri ?? CreateBaseUri();

            var webRequest = (HttpWebRequest)WebRequest.Create(baseUri);
            webRequest.CookieContainer = _cookies;
            webRequest.ContentType = "application/moca-xml";
            webRequest.Method = "POST";
            webRequest.CachePolicy = new RequestCachePolicy(RequestCacheLevel.BypassCache);
            webRequest.UseDefaultCredentials = false;
            webRequest.Timeout = (timeout != 0) ? timeout : Timeout.Infinite;
            webRequest.Headers.Add("Response-Encoder", "xml");
            webRequest.Headers.Add("Accept-Encoding", "gzip");

            if (webRequest is HttpWebRequest)
            {
                //This is needed due to the interaction between the nagle
                //algorithm, expect 100-continue and request timeout on Windows server.

                var httpRequest = ((HttpWebRequest) webRequest);
                httpRequest.ServicePoint.Expect100Continue = false;
                httpRequest.ServicePoint.UseNagleAlgorithm = false;
            }

            return webRequest;
        }

        /// <summary>
        /// Gets the response stream.
        /// </summary>
        /// <param name="request">The request.</param>
        /// <returns>A response stream.</returns>
        /// <exception cref="ProtocolException">An error occurred connecting to the resource</exception>
        public WebResponse GetResponse(WebRequest request)
        {
            try
            {
                var response = request.GetResponse();

                if (response == null)
                    throw new ProtocolException("An error occurred connecting to the resource");

                return response;
            }
            catch (WebException ex)
            {
                throw new ProtocolException("A HTTP error occurred while sending the request.", ex);
            }
        }

        /// <summary>
        /// The internal method that handles the actual execution of the command.
        /// </summary>
        /// <param name="command">The command that is sent.</param>
        /// <param name="timeout">The request timeout.</param>
        /// <returns>A <see cref="DataSet"/> of the results</returns>
        /// <exception cref="ProtocolException">An error occurred connecting to the resource</exception>
        private void InternalExecute(Command command, int timeout)
        {
            var request = SendWebRequest(command, timeout);
            var response = GetResponse(request);
            ParseResults(response, command.CommandText);
        }

        /// <summary>
        /// Parses the results of the incoming data stream.
        /// </summary>
        /// <param name="response">The response.</param>
        /// <param name="command">The command being called.</param>
        /// <returns>
        /// A <see cref="DataSet"/> or null if an error occurred.
        /// </returns>
        /// <exception cref="MocaException">If the execution fails</exception>
        private DataSet ParseResults(WebResponse response, string command)
        {
            return ParseResults(response, command, null, null) as DataSet;
        }

        /// <summary>
        /// Parses the results.
        /// </summary>
        /// <param name="response">The response.</param>
        /// <param name="command">The command.</param>
        /// <param name="mappingData">The mapping data.</param>
        /// <param name="resolver">The resolver.</param>
        /// <returns>An object representing the results.</returns>
        /// <exception cref="ProtocolException">Response content type was incorrect.</exception>
        /// <exception cref="MocaException"><c>MocaException</c>.</exception>
        /// <exception cref="ReadResponseException"><c>ReadResponseException</c>.</exception>
        private object ParseResults(WebResponse response, string command, MappingData mappingData, IObjectResolver resolver)
        {
            if (!response.ContentType.StartsWith("application/xml"))
            {
                throw new ProtocolException("Response content type was incorrect");
            }

            var contentHandler = (mappingData == null && resolver == null)
                                     ? (IContentHandler)new DataTableContentHandler(command)
                                     : new ObjectMapperContentHandler(command, mappingData, resolver);

            //Parse the results
            try
            {
                using (var stream = GetResponseStream(response))
                {
                    SaxParser.Parse(stream, contentHandler);

                    //update the session ID
                    CheckSessionId(contentHandler);
                    return contentHandler.GetResults();
                }
            }
            finally
            {
                response.Close();
            }
        }

        /// <summary>
        /// Sends the web request.
        /// </summary>
        /// <param name="command">The command to send.</param>
        /// <returns>A formed web request.</returns>
        /// <exception cref="ProtocolException">Unexpected web response from server</exception>
        private WebRequest SendWebRequest(Command command)
        {
            return SendWebRequest(command, 0);
        }

        /// <summary>
        /// Creates the appropriate response stream.
        /// </summary>
        /// <param name="response">A WebResponse obect.</param>
        /// <returns>Either the raw response stream or a gzip (compression) decododing response stream</returns>
        private Stream GetResponseStream(WebResponse response)
        {
            HttpWebResponse httpResponse = (HttpWebResponse)response;
            Stream stream = response.GetResponseStream();
            if (httpResponse.ContentEncoding.Contains("gzip"))
                stream = new GZipStream(stream, CompressionMode.Decompress);

            return stream;
        }

        /// <summary>
        /// Sends the web request.
        /// </summary>
        /// <param name="command">The command to send.</param>
        /// <param name="timeout">The request timeout.</param>
        /// <returns>A formed web request.</returns>
        /// <exception cref="ProtocolException">Unexpected web response from server</exception>
        private WebRequest SendWebRequest(Command command, int timeout)
        {
            try
            {
                var webRequest = CreateWebRequest(timeout);
                var bytes = BuildQueryString(command);

                webRequest.ContentLength = bytes.Length;
                var os = webRequest.GetRequestStream();
                os.Write(bytes, 0, bytes.Length);
                os.Close();

                return webRequest;
            }
            catch (SystemException ex)
            {
                throw new ProtocolException("Protocol failure", ex);
            }
        }

        /// <summary>
        /// Webs the request completed.
        /// </summary>
        /// <param name="item">The item.</param>
        private void WebRequestCompleted(object item)
        {
            var callBack = (InternalCallBackEventArgs)item;

            try
            {
                var request = SendWebRequest(callBack.Command.Command);
                var response = GetResponse(request);

                callBack.Command.Result = (callBack.Command.IsDataSet)
                                         ? ParseResults(response, callBack.Command.Command.CommandText)
                                         : ParseResults(response,
                                                        callBack.Command.Command.CommandText,
                                                        callBack.Command.MappingData,
                                                        callBack.Command.ObjectResolver);

            }
            catch (MocaException ex)
            {
                callBack.Command.Exception = ex;
                callBack.Command.Result = ex.Results;
            }

            callBack.InvokeCommandCallback(this);
        }

        #endregion
    }
}
