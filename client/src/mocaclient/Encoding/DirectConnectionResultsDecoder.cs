using System;
using System.Data;
using System.IO;
using System.Security.Cryptography;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client.Encoding
{
    /// <summary>
    /// The decoder class for a direct connection to the server
    /// </summary>
    public class DirectConnectionResultsDecoder : ResultsDecoderBase
    {
        #region Protected Fields

        /// <summary>
        /// The cryptography strategy used to decrypt the results
        /// </summary>
        protected SymmetricAlgorithm encryptionStrategy;

        /// <summary>
        /// The protocol version used to decode the results
        /// </summary>
        protected int protocolVersion;

        #endregion

        #region Constructor

        /// <summary>
        /// Initializes a new instance of the <see cref="DirectConnectionResultsDecoder"/> class.
        /// </summary>
        /// <param name="dataStream">The data stream.</param>
        /// <param name="encryptionStrategy">The encryption strategy.</param>
        public DirectConnectionResultsDecoder(Stream dataStream, SymmetricAlgorithm encryptionStrategy)
            : base(dataStream)
        {
            this.encryptionStrategy = encryptionStrategy;
        }

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets the protocol for the last command
        /// </summary>
        public int ProtocolVersion
        {
            get { return protocolVersion; }
        }

        #endregion

        #region Method Overrides

        /// <summary>
        /// Parses the stream for the version
        /// </summary>
        /// <returns>An integer of the protocol version</returns>
        protected override int ParseVersion()
        {
            protocolVersion = Constants.PROTOCOL_MIN_VERSION;

            int data = dataStream.ReadByte();

            if (data <= 0)
            {
                throw new ConnectionFailedException("Could not read result from server, connection failed");
            }

            /* Determine what the protocol version is. */
            while (data != 'V' || data < 0)
            {
                data = dataStream.ReadByte();
            }

            if (data != 'V')
            {
                throw new ConnectionFailedException("Could not read result from server, connection failed");
            }

            string tmpData = NextField();

            if (!Int32.TryParse(tmpData, out protocolVersion))
                throw new ProtocolException(
                    string.Format("Protocol Version '{0}' not recognized", tmpData));

            /* Validate the protocol version. */
            if (protocolVersion < Constants.PROTOCOL_MIN_VERSION ||
                protocolVersion > Constants.PROTOCOL_MAX_VERSION)
                throw new ProtocolException(
                    string.Format("Protocol Version {0} not supported", protocolVersion));

            return protocolVersion;

        }

        /// <summary>
        /// Parses the header.
        /// </summary>
        /// <param name="protoVersion">The proto version.</param>
        /// <returns></returns>
        protected override HeaderData ParseHeader(int protoVersion)
        {
            switch (protoVersion)
            {
                case 102:
                    return ParseV102Header();
                case 103:
                    return ParseV103Header();
                case 104:
                    return ParseV104Header();
                case 101:
                default:
                    return ParseBaseHeader();
            }
        }

        /// <summary>
        /// Parses the columns out to the proper data
        /// </summary>
        /// <param name="headerData">The <see cref="HeaderData"/> to add the item to</param>
        /// <param name="columnInfo">A string of the column meta data</param>
        protected override void ParseColumns(ref HeaderData headerData, string columnInfo)
        {
            switch (headerData.Protocol)
            {
                default:
                    ParseBaseColumns(ref headerData, columnInfo);
                    break;
            }
        }

        /// <summary>
        /// Gets a stream that contains the row data.
        /// </summary>
        protected override Stream GetRowStream(HeaderData headerData, Stream inputStream)
        {
            switch (headerData.Protocol)
            {
                case 104:
                    /* first field here contains the row length */
                    int nLength = ParseInt(NextField(inputStream));
                    return GetCryptoStream(nLength);

                default:
                    return base.GetRowStream(headerData, inputStream);
            }
        }

        #endregion

        #region Private Methods

        #region Header Parsing

        /// <summary>
        /// Parses the Header for version 101 protocol
        /// </summary>
        /// <returns>The <see cref="HeaderData"/> of the response</returns>
        private HeaderData ParseBaseHeader()
        {
            HeaderData headerData = new HeaderData();
            headerData.Protocol = 101;
            headerData.DataLength = ParseInt(NextField());
            headerData.Status = ParseInt(NextField());
            headerData.RowCount = ParseInt(NextField());
            headerData.ColumnCount = ParseInt(NextField());
            headerData.DataTypeString = NextField();
            string columnMetadata = NextField();

            ParseColumns(ref headerData, columnMetadata);

            return headerData;
        }

        /// <summary>
        /// Parses the Header for version 102 and 103 protocol
        /// </summary>
        /// <returns>The <see cref="HeaderData"/> of the response</returns>
        private HeaderData ParseV102Header()
        {
            HeaderData headerData = new HeaderData();
            headerData.Protocol = 102;
            headerData.DataLength = ParseInt(NextField());
            headerData.Status = ParseInt(NextField());
            headerData.MessageLength = ParseInt(NextField());
            headerData.Message = ReadBytesAsString(headerData.MessageLength);
            SkipBytes(1);
            headerData.RowCount = ParseInt(NextField());
            headerData.ColumnCount = ParseInt(NextField());
            headerData.DataTypeString = NextField();
            string columnMetadata = NextField();

            ParseColumns(ref headerData, columnMetadata);

            return headerData;
        }

        /// <summary>
        /// Parses the Header for version 103 protocol
        /// </summary>
        /// <returns>The <see cref="HeaderData"/> of the response</returns>
        private HeaderData ParseV103Header()
        {
            HeaderData headerData = new HeaderData();
            headerData.Protocol = 103;
            headerData.DataLength = ParseInt(NextField());
            headerData.CommandCount = ParseInt(NextField());
            headerData.Status = ParseInt(NextField());
            headerData.MessageLength = ParseInt(NextField());
            headerData.Message = ReadBytesAsString(headerData.MessageLength);
            SkipBytes(1);
            headerData.RowCount = ParseInt(NextField());
            headerData.ColumnCount = ParseInt(NextField());
            headerData.DataTypeString = NextField();
            string columnMetadata = NextField();

            ParseColumns(ref headerData, columnMetadata);

            return headerData;
        }

        /// <summary>
        /// Parses the Header for version 104 protocol
        /// </summary>
        /// <returns>The <see cref="HeaderData"/> of the response</returns>
        private HeaderData ParseV104Header()
        {
            HeaderData headerData = new HeaderData();
            headerData.Protocol = 104;
            headerData.HeaderLength = ParseInt(NextField());
            
            //holds the base stream of data when the encryption wrapper is created
            Stream baseStream = dataStream;
            dataStream = GetCryptoStream(headerData.HeaderLength);
            
            headerData.CommandCount = ParseInt(NextField());
            headerData.Status = ParseInt(NextField());
            headerData.MessageLength = ParseInt(NextField());
            headerData.Message = ReadBytesAsString(headerData.MessageLength);
            SkipBytes(1);
            headerData.RowCount = ParseInt(NextField());
            headerData.ColumnCount = ParseInt(NextField());
            headerData.DataTypeString = NextField();
            string columnMetadata = NextField();

            dataStream = baseStream;

            ParseColumns(ref headerData, columnMetadata);

            return headerData;
        }

        #endregion

        #region Column Parsing

        /// <summary>
        /// Parses the Header Columns for version 104 protocol
        /// </summary>
        /// <returns>The <see cref="HeaderData"/> of the response</returns>
        private static void ParseBaseColumns(ref HeaderData headerData, string columnInfo)
        {
            string[] metadataFields = columnInfo.Split('~');

            for (int c = 0, m = 1; c < headerData.ColumnCount; c++)
            {
                char typeCode = headerData.DataTypeString[c];
                MocaType type = MocaType.Lookup(Char.ToUpper(typeCode));
                //bool isNullable = Char.IsLower(typeCode);
                string name = metadataFields[m++];
                int definedMaxLength = ParseInt(metadataFields[m++]);
                int actualMaxLength = ParseInt(metadataFields[m++]);
                string shortDesc = metadataFields[m++];
                string longDesc = metadataFields[m++];

                DataColumn col = new DataColumn(name, type.Class);
                //col.AllowDBNull = isNullable; - Remove for now due to MCS issues

                /* This was removed due to unicode support and removal of the
                 * defined column length value support in MOCA server
                if (type.Equals(MocaType.STRING) && definedMaxLength > 0)
                   col.MaxLength = Math.Max(definedMaxLength, actualMaxLength);*/
                col.MaxLength = -1;

                col.ExtendedProperties.Add(Constants.MaxActualWidthProperty, actualMaxLength);
                col.ExtendedProperties.Add(Constants.ShortDescProperty, shortDesc);
                col.ExtendedProperties.Add(Constants.LongDescProperty, longDesc);

                headerData.Columns.Add(col);
            }
        }

        #endregion

        #region Stream Handling
        /// <summary>
        /// Gets the crypto stream when an encryption strategy is present.
        /// The input is buffered into a <see cref="MemoryStream"/> to allow
        /// for the cryptograph to work correctly.
        /// </summary>
        /// <param name="length">The length of the stream to read</param>
        /// <returns>A new <see cref="Stream"/> that is potentially wrapped with the decryptor</returns>
        private Stream GetCryptoStream(int length)
        {
            MemoryStream newStream = ReadBytesAsStream(length);

            if (encryptionStrategy != null)
            {
                return new CryptoStream(newStream, encryptionStrategy.CreateDecryptor(), CryptoStreamMode.Read);
            }
            return newStream;
        }
        #endregion

        #endregion
    }
}
