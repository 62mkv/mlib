using System;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client.Encoding
{
    /// <summary>
    /// CommandBuilder constructs the command that will be sent to the server.
    /// </summary>
    public class CommandBuilder : EncodingBase
    {
        #region Private Fields

        private SymmetricAlgorithm encryptionStrategy;
        private string encryptionInfo = string.Empty;
        private string service = string.Empty;
        private string schema = string.Empty;
        private string traceEncoding = "~0~~0~~-1";
        private string environmentEncoding = string.Empty;

        #endregion

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="CommandBuilder"/> class.
        /// </summary>
        public CommandBuilder()
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="CommandBuilder"/> class.
        /// </summary>
        /// <param name="encryptionStrategy">The encryption strategy.</param>
        public CommandBuilder(SymmetricAlgorithm encryptionStrategy)
        {
            this.encryptionStrategy = encryptionStrategy;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="CommandBuilder"/> class.
        /// </summary>
        /// <param name="environmentEncoding">The environment variables encoded
        /// into a single string.</param>
        public CommandBuilder(string environmentEncoding)
        {
            this.environmentEncoding = environmentEncoding;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="CommandBuilder"/> class.
        /// </summary>
        /// <param name="encryptionStrategy">The encryption strategy.</param>
        /// <param name="environmentEncoding">The environment variables encoded
        /// into a single string.</param>
        public CommandBuilder(SymmetricAlgorithm encryptionStrategy, string environmentEncoding)
            : this(encryptionStrategy)
        {
            this.environmentEncoding = environmentEncoding;
        }

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets or sets the encryption strategy.
        /// </summary>
        public SymmetricAlgorithm EncryptionStrategy
        {
            get { return encryptionStrategy; }
            set { encryptionStrategy = value; }
        }

        /// <summary>
        /// Gets or sets the encryption info.
        /// </summary>
        public string EncryptionInfo
        {
            get { return encryptionInfo; }
            set { encryptionInfo = value; }
        }

        /// <summary>
        /// Gets or sets the environment variables encoded
        /// into a single string.
        /// </summary>
        public string EnvironmentEncoding
        {
            get { return environmentEncoding; }
            set { environmentEncoding = value; }
        }

        /// <summary>
        /// Gets or sets the service (not currently used).
        /// </summary>
        public string Service
        {
            get { return service; }
            set { service = value; }
        }

        /// <summary>
        /// Gets or sets the schema (not currently used).
        /// </summary>
        public string Schema
        {
            get { return schema; }
            set { schema = value; }
        }

        /// <summary>
        /// Gets or sets the trace encoding (limited use).
        /// </summary>
        public string TraceEncoding
        {
            get { return traceEncoding; }
            set { traceEncoding = value; }
        }

        #endregion

        #region Public Methods

        /// <summary>
        /// Creates the command that is sent to the server.
        /// </summary>
        /// <param name="command">The string of the text command.</param>
        /// <param name="protocolVersion">The protocol version.</param>
        /// <param name="flags">Any command flags that are needed flags.</param>
        /// <returns>A <see cref="byte"/> array of the encoded command</returns>
        public byte[] CreateCommand(string command, int protocolVersion, int flags)
        {
            MemoryStream outputStream = new MemoryStream();

            //Creates the version
            EncodeToStream(outputStream,
                           (protocolVersion > 100)
                               ? string.Format("V{0}^", protocolVersion)
                               : string.Empty);

            int _flags = ((GetByteCount(command)%18) << 8) | (flags & 0xff);

            switch (protocolVersion)
            {
                case 104:
                    CreateV104Command(ref outputStream, command, _flags);
                    break;
                default:
                    CreateLegacyCommand(ref outputStream, command, _flags, protocolVersion);
                    break;
            }

            return outputStream.ToArray();
        }

        #endregion

        #region Protected Methods

        /// <summary>
        /// Encodes the data then writes it to the target stream
        /// </summary>
        /// <param name="outputStream">The stream</param>
        /// <param name="data">The data to write</param>
        protected static int EncodeToStream(Stream outputStream, string data)
        {
            if (!String.IsNullOrEmpty(data))
            {
                byte[] bufferBytes = ToByteArray(data);
                int length = bufferBytes.Length;
                outputStream.Write(bufferBytes, 0, bufferBytes.Length);

                return length;
            }
            return 0;
        }

        #endregion

        #region Private Methods

        /// <summary>
        /// Creates a command that conforms from Version  - 100 to 103
        /// </summary>
        /// <param name="outputStream">The stream to write data to</param>
        /// <param name="command">The command to parse</param>
        /// <param name="flags">And flag values to send</param>
        /// <param name="protocolVersion">The protocol version to build the stream to</param>
        private void CreateLegacyCommand(ref MemoryStream outputStream,
                                         string command, int flags, int protocolVersion)
        {
            //Creates the header and appends the command
            string payloadData = string.Format("{0}{1}",
                                               GetHeaderData(protocolVersion, flags),
                                               command);


            //Write Length
            string length = GetEncodedLength(payloadData, (protocolVersion < 103));

            EncodeToStream(outputStream,
                           string.Format("{0}{1}", length, Constants.DELIMITER));

            //Write Remainder of Command
            EncodeToStream(outputStream, payloadData);
        }

        /// <summary>
        /// Creates the header data for the packet.
        /// </summary>
        /// <param name="protocolVersion">The version of the protocol to build for</param>
        /// <param name="flags">Any flags to send</param>
        /// <returns>A string representing the header</returns>
        private string GetHeaderData(int protocolVersion, int flags)
        {
            StringBuilder builder = new StringBuilder();

            if (protocolVersion > 100)
                builder.AppendFormat("{0}{1}", service, Constants.DELIMITER);

            if (protocolVersion > 103)
                builder.AppendFormat("{0}{1}", schema, Constants.DELIMITER);

            if (protocolVersion > 102)
                builder.AppendFormat("{0}{1}", traceEncoding, Constants.DELIMITER);

            builder.AppendFormat("{0}{1}", environmentEncoding, Constants.DELIMITER);

            builder.AppendFormat("{0}{1}", PadHexValue(flags), Constants.DELIMITER);

            return builder.ToString();
        }

        /// <summary>
        /// Creates a command for the 104 protocol version.
        /// A seperate method was needed to support encryption.
        /// </summary>
        /// <param name="outputStream">The stream to write data to</param>
        /// <param name="command">The command to parse</param>
        /// <param name="flags">And flag values to send</param>
        private void CreateV104Command(ref MemoryStream outputStream, string command, int flags)
        {
            MemoryStream payloadStream = new MemoryStream();
            MemoryStream tmpStream = new MemoryStream(); //Temporary stream for encryption

            Stream cryptoStream = encryptionStrategy != null
                                      ? new CryptoStream(payloadStream, encryptionStrategy.CreateEncryptor(),
                                                         CryptoStreamMode.Write)
                                      : (Stream) payloadStream;

            //Begin writing potentially encrypted stream
            EncodeToStream(cryptoStream, GetHeaderData(104, flags));
            cryptoStream.Flush();

            //Write payload
            EncodeToStream(cryptoStream, command);

            //Do final crypto tranform if needed
            if (cryptoStream is CryptoStream)
                ((CryptoStream) cryptoStream).FlushFinalBlock();

            //Get the length of the stream
            int restLength = (int) payloadStream.Length;

            //Calculate header length
            byte[] encryptionSegBytes = ToByteArray(string.Format("{0}^{1}^", encryptionStrategy, encryptionInfo));
            int headerLength = encryptionSegBytes.Length;

            //Write Header Length
            EncodeToStream(tmpStream,
                           string.Format("{0}{1}", GetEncodedLength(headerLength, false), Constants.DELIMITER));

            //Write Encryption Info
            tmpStream.Write(encryptionSegBytes, 0, encryptionSegBytes.Length);

            //Write Remaining Length
            EncodeToStream(tmpStream, string.Format("{0}{1}", GetEncodedLength(restLength, false), Constants.DELIMITER));

            //Write Encryption Data
            payloadStream.WriteTo(tmpStream);

            //Write Total Length
            int totalLength = (int) tmpStream.Length;
            EncodeToStream(outputStream,
                           string.Format("{0}{1}", GetEncodedLength(totalLength, false), Constants.DELIMITER));

            //Write Header Information
            tmpStream.WriteTo(outputStream);
            tmpStream.Close();
            payloadStream.Close();
        }

        #endregion
    }
}