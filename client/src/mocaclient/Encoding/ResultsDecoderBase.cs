using System;
using System.Data;
using System.IO;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client.Encoding
{
    /// <summary>
    /// A class to decode a stream of bytes into a <see cref="DataSet"/> or
    /// <see cref="DataTable"/> object.  The encoding of the byte stream 
    /// must be the same encoding as produced by the MOCA C function sqlEncodeResults.
    /// An overriding class can modify the parsing implimentations to adapt to protocol
    /// changes.
    /// </summary>
    public class ResultsDecoderBase : EncodingBase
    {
        #region Private Fields

        /// <summary>
        /// The data stream to read and parse
        /// </summary>
        protected Stream dataStream;

        #endregion

        #region Constructor

        /// <summary>
        /// Initializes a new instance of the <see cref="ResultsDecoderBase"/> class.
        /// </summary>
        /// <param name="dataStream">The data stream to decode the results from.</param>
        public ResultsDecoderBase(Stream dataStream)
        {
            this.dataStream = dataStream;
        }
        #endregion

        #region Public Methods

        /// <summary>
        /// Decodes the binary data to a <see cref="DataSet"/> object
        /// </summary>
        /// <param name="tableName">The name of the table that will be created.</param>
        /// <returns>A new <see cref="DataSet"/> object. If the table parse failed,
        /// the set has a single empty <see cref="DataTable"/>.</returns>
        public virtual DataSet Decode(string tableName)
        {
            DataSet dataSet = new DataSet();
            
            //Disable constraints at the table level by default
            //This is done due to MSAP issues in editing.
            dataSet.EnforceConstraints = false;
            
            DataTable dataTable = DecodeTable();
            dataTable.TableName = tableName;

            dataSet.Tables.Add(dataTable);

            return dataSet;
        }

        /// <summary>
        /// Decodes the binary data to a <see cref="DataTable"/> object
        /// </summary>
        /// <returns>A new <see cref="DataSet"/> object or null if failed</returns>
        public virtual DataTable DecodeTable()
        {
            DataTable dataTable = new DataTable();

            HeaderData headerData = ParseHeader(ParseVersion());

            //This is a second check since the column parsing could fail
            if (headerData.ColumnCount != 0 && headerData.Columns.Count != 0)
            {
                //Check for duplicates
                headerData.CheckDuplicateColumns();

                dataTable = GetDataTable(headerData);
            }

            //Throw Error if not an ok result
            if (!headerData.Status.Equals(MocaErrors.eOK))
                throw new MocaException(headerData.Status, headerData.Message, dataTable);

            return dataTable;
        }

        

        #endregion

        #region Protected Virtual Methods

        /// <summary>
        /// Parses the stream for the version
        /// </summary>
        /// <returns>An integer of the protocol version</returns>
        protected virtual int ParseVersion()
        {
            return -1;
        }

        /// <summary>
        /// Parses the header and column data from the incoming stream
        /// </summary>
        /// <param name="protocolVersion">The version to parse</param>
        /// <returns>The <see cref="HeaderData"/> from the results</returns>
        protected virtual HeaderData ParseHeader(int protocolVersion)
        {
            HeaderData headerData = new HeaderData();
            headerData.Protocol = protocolVersion;

            string columnInfo = null;

            do
            {
                // Read the stanza prefix [xxxxx=]some-value
                byte[] prefix = new byte[5];
                int nbytes = dataStream.Read(prefix, 0, prefix.Length);

                if (nbytes == -1)
                {
                    // End of stream
                    return null;
                }
                if (nbytes != 5)
                {
                    throw new ProtocolException(
                        string.Format("error reading stream: unrecognized stanza: {0}", (prefix)));
                }
                int eq = dataStream.ReadByte();
                if (eq != '=')
                {
                    throw new ProtocolException(string.Format("error reading stream: unrecognized character: {0}", eq));
                }

                string key = ToCharArray(prefix);
                if (key.Equals("RDATA"))
                {
                    break;
                }

                // Read to the delimiter character
                string tmp = NextField();

                if (key.Equals("NROWS"))
                {
                    try
                    {
                        headerData.RowCount = Int32.Parse(tmp);
                    }
                    catch (FormatException e)
                    {
                        throw new ProtocolException("error reading stream: illegal number of rows: " + tmp, e);
                    }
                }
                else if (key.Equals("NCOLS"))
                {
                    try
                    {
                        headerData.ColumnCount = Int32.Parse(tmp);
                    }
                    catch (FormatException e)
                    {
                        throw new ProtocolException("error reading stream: illegal number of columns: " + tmp, e);
                    }
                }
                else if (key.Equals("DTYPE"))
                {
                    headerData.DataTypeString = tmp;
                }
                else if (key.Equals("CINFO"))
                {
                    columnInfo = tmp;
                }
                else if (key.Equals("EMESG"))
                {
                    headerData.Message = tmp;
                }
            } while (true);

            //Ensure we have all meta-data
            if (columnInfo == null || headerData.DataTypeString == null)
                return headerData;

            ParseColumns(ref headerData, columnInfo);

            return headerData;
        }

        /// <summary>
        /// Parses the columns out to the proper data
        /// </summary>
        /// <param name="headerData">The <see cref="HeaderData"/> to add the item to</param>
        /// <param name="columnInfo">A string of the column meta data</param>
        protected virtual void ParseColumns(ref HeaderData headerData, string columnInfo)
        {
            // Set up the column header information
            string[] metadataFields = columnInfo.Split('~');

            for (int c = 0, m = 0; c < headerData.ColumnCount; c++)
            {
                char typeCode = headerData.DataTypeString[c];
                MocaType type = MocaType.Lookup(Char.ToUpper(typeCode));
                bool isNullable = Char.IsLower(typeCode);
                string name = metadataFields[m++];
                int definedMaxLength = Int32.Parse(metadataFields[m++]);
                int actualMaxLength = Int32.Parse(metadataFields[m++]);

                DataColumn col = new DataColumn(name, type.Class);
                col.AllowDBNull = isNullable;
                
                /* This was removed due to unicode support and removal of the
                 * defined column length value support in MOCA server
                if (type.Equals(MocaType.STRING))
                    col.MaxLength = definedMaxLength; */
                col.MaxLength = -1;

                col.ExtendedProperties.Add(Constants.MaxActualWidthProperty, actualMaxLength);

                headerData.Columns.Add(col);
            }
        }

        /// <summary>
        /// Gets the table's row data
        /// </summary>
        /// <param name="dataTable">The <see cref="DataTable"/> that the data is loaded into</param>
        /// <param name="headerData">The <see cref="HeaderData"/> that defines the parsing</param>
        protected virtual void GetData(ref DataTable dataTable, HeaderData headerData)
        {
            for (int r = 0; r < headerData.RowCount; r++)
            {
                Stream baseStream = dataStream;
                dataStream = GetRowStream(headerData, dataStream);
                  
                object[] dataArray = new object[headerData.ColumnCount];

                for (int c = 0; c < headerData.ColumnCount; c++)
                {
                    char typeCode = (char) dataStream.ReadByte();
                    MocaType type = MocaType.Lookup(typeCode);
                    string tmp = NextField();
                    int dataLength;
                    Int32.TryParse(tmp, out dataLength);
                    
                    if (dataLength == 0)
                    {
                        dataArray[c] = null;
                    }
                    else
                    {
                        if (type.Equals(MocaType.BINARY))
                        {
                            if (dataLength >= 8)
                            {
                                byte[] readData = ReadBytes(dataLength);
                                byte[] data = new byte[readData.Length - 8];

                                // The first 8 bytes are encoding the length of the data.
                                // We've already got that, so there's really no need to
                                // interpret it.
                                Array.Copy(readData, 8, data, 0, readData.Length - 8);

                                dataArray[c] = data;
                            }
                            else
                                dataArray[c] = null;
                        }
                        else if (type.Equals(MocaType.RESULTS))
                        {
                            byte[] data = ReadBytes(dataLength);
                            Stream subStream = new MemoryStream(data);
                            dataArray[c] = new ResultsDecoderBase(subStream).DecodeTable();
                        }
                        else
                        {
                            string data = ReadBytesAsString(dataLength);
                            if (type.Equals(MocaType.STRING) || type.Equals(MocaType.STRING_REF))
                            {
                                dataArray[c] = data;
                            }
                            else if (type.Equals(MocaType.INTEGER) || type.Equals(MocaType.INTEGER_REF))
                            {
                                try
                                {
                                    dataArray[c] = Int32.Parse(data, GlobalNumberFormatInfo);
                                }
                                catch (FormatException e)
                                {
                                    throw new ProtocolException("error parsing integer: " + data, e);
                                }
                            }
                            else if (type.Equals(MocaType.DOUBLE) || type.Equals(MocaType.DOUBLE_REF))
                            {
                                try
                                {
                                    dataArray[c] = Double.Parse(data, GlobalNumberFormatInfo);
                                }
                                catch (FormatException e)
                                {
                                    throw new ProtocolException("error parsing double: " + data, e);
                                }
                            }
                            else if (type.Equals(MocaType.BOOLEAN))
                            {
                                dataArray[c] = (!data.Equals("0"));
                            }
                            else if (type.Equals(MocaType.DATETIME))
                            {
                                dataArray[c] = ConvertDateTime(data);
                            }
                            else
                            {
                                //Just put it in as a string;
                                dataArray[c] = data;
                            }
                        }
                    }
                }

                dataTable.Rows.Add(dataArray);
                dataStream = baseStream;
            }
        }

        /// <summary>
        /// Executes any misc. stream operations
        /// </summary>
        protected virtual Stream GetRowStream(HeaderData headerData, Stream inputStream)
        {
            return inputStream;
        }

        #endregion

        #region Protected Methods

        /// <summary>
        /// Gets the data table.
        /// </summary>
        /// <param name="headerData">The header data.</param>
        /// <returns>A <see cref="DataTable"/> with the results </returns>
        protected DataTable GetDataTable(HeaderData headerData)
        {
            DataTable dataTable = new DataTable();
            dataTable.Columns.AddRange(headerData.Columns.ToArray());

            // Next read the row data
            dataTable.BeginLoadData();
            GetData(ref dataTable, headerData);
            dataTable.EndLoadData();
            dataTable.AcceptChanges();
            return dataTable;
        }

        /// <summary>
        /// Gets the string representation of the next field
        /// </summary>
        /// <returns>a string of the next field</returns>
        protected string NextField()
        {
            return NextField(null);
        }

        /// <summary>
        /// Gets the string representation of the next field
        /// </summary>
        /// <param name="stream">The stream that is read</param>
        /// <returns>a string of the next field</returns>
        protected string NextField(Stream stream)
        {
            Stream readStream = (stream == null) ? dataStream : stream;
            MemoryStream str = new MemoryStream();
            while (true)
            {
                int c = readStream.ReadByte();
                if (c == -1 || c == Constants.DELIMITER)
                {
                    break;
                }
                str.WriteByte((Byte) c);
            }

            return ToCharArray(str.ToArray());
        }

        /// <summary>
        /// Reads a set of bytes in from the input stream
        /// </summary>
        /// <param name="length">The number of bytes to read</param>
        /// <returns>A byte array of the results</returns>
        protected byte[] ReadBytes(int length)
        {
            return ReadBytesAsStream(length).ToArray();
        }

        /// <summary>
        /// Reads the underlying stream of bytes and reutrns the length
        /// as a new <see cref="MemoryStream"/>.
        /// </summary>
        /// <param name="length">The length of bytes to read.</param>
        /// <returns>A new <see cref="MemoryStream"/> of the data.</returns>
        protected MemoryStream ReadBytesAsStream(int length)
        {
            MemoryStream tmpStream = new MemoryStream(length);

            int counter = 0;
            while (counter < length)
            {
                int readByte = dataStream.ReadByte();

                //Check to see if we've reached the end of the stream
                if (readByte != -1)
                {
                    tmpStream.WriteByte((byte)readByte);
                    counter++;
                }
                else
                {
                    break;
                }
            }
            tmpStream.Position = 0;
            return tmpStream;
        }

        /// <summary>
        /// Reads in a number of bytes as a string
        /// </summary>
        /// <param name="length">The number of bytes to read</param>
        /// <returns>A string of the result</returns>
        protected string ReadBytesAsString(int length)
        {
            return ToCharArray(ReadBytes(length));
        }

        /// <summary>
        /// Advances the incoming stream a number of bytes
        /// </summary>
        /// <param name="length">The number of bytes to advance</param>
        protected void SkipBytes(int length)
        {
            if (dataStream.CanSeek)
                dataStream.Position += length;
            else
            {
                for (int i = 0; i < length; i++)
                {
                    dataStream.ReadByte();
                }
            }
        }

        /// <summary>
        /// Parses the string as an integer returning a default of 0.
        /// </summary>
        /// <param name="value">The string value to parse.</param>
        /// <returns>the integer value of the string or 0</returns>
        protected static int ParseInt(string value)
        {
            int output;
            if (String.IsNullOrEmpty(value) ||
                !Int32.TryParse(value, out output))
            {
                return 0;
            }
            
            return output;
        }

        #endregion
    }
}
