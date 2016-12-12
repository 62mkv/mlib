using System;
using System.Data;
using System.IO;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client.Encoding
{
    /// <summary>
    /// A class to encode a MocaResults object into a stream of bytes.  The
    /// encoding of the byte stream will be the same encoding as produced by
    /// the MOCA C function sqlEncodeResults.
    /// </summary>
    public class ResultsEncoder : EncodingBase
    {
        /// <summary>
        /// Encodes the results.
        /// </summary>
        /// <param name="res">A DataTable <see cref="DataTable"/> to encode</param>
        /// <param name="message">An error message to send if the results errored. Should be null or empty if results are ok.</param>
        /// <returns></returns>
        public static byte[] EncodeResults(DataTable res, String message)
        {
            return EncodeResultsInternal(res, message).ToArray();
        }

        /// <summary>
        /// Encodes the results.
        /// </summary>
        /// <param name="res">A DataTable <see cref="DataTable"/> to encode</param>
        /// <param name="message">An error message to send if the results errored. Should be null or empty if results are ok.</param>
        /// <returns></returns>
        public static byte[] EncodeResultsWithVersion(DataTable res, String message)
        {
            MemoryStream content = EncodeResultsInternal(res, message);
            MemoryStream fullContent = new MemoryStream();

            

            return fullContent.ToArray();
        }

        /// <summary>
        /// Encodes the results as a string.
        /// </summary>
        /// <param name="res">A DataTable <see cref="DataTable"/> to encode</param>
        /// <returns>The encoded results as a string</returns>
        public static string EncodeResultsAsString(DataTable res)
        {
            return EncodeResultsInternal(res, null).ToString();
        }

        /// <summary>
        /// Encodes the results.
        /// </summary>
        /// <param name="res">A DataTable <see cref="DataTable"/> to encode</param>
        /// <param name="message">An error message to send if the results errored. Should be null or empty if results are ok.</param>
        /// <returns></returns>
        private static MemoryStream EncodeResultsInternal(DataTable res, String message)
        {
            MemoryStream _out = new MemoryStream();
            try
            {
                if (!String.IsNullOrEmpty(message))
                {
                    WriteToStream(_out, "EMESG=");
                    WriteToStream(_out, message);
                    WriteToStream(_out, Constants.DELIMITER);
                }

                if (res != null)
                {
                    int rowCount = res.Rows.Count;
                    int columnCount = res.Columns.Count;

                    WriteToStream(_out, "NROWS=");
                    WriteToStream(_out, rowCount.ToString());
                    WriteToStream(_out, Constants.DELIMITER);
                    WriteToStream(_out, "NCOLS=");
                    WriteToStream(_out, columnCount.ToString());
                    WriteToStream(_out, Constants.DELIMITER);
                    WriteToStream(_out, "DTYPE=");

                    foreach (DataColumn column in res.Columns)
                    {
                        MocaType columnType = MocaType.LookupClass(column.DataType);
                        char typeChar = columnType.TypeCode;
                        if (column.AllowDBNull)
                        {
                            typeChar = typeChar.ToString().ToLower()[0];
                        }
                        WriteToStream(_out, typeChar);
                    }
                    WriteToStream(_out, Constants.DELIMITER);
                    WriteToStream(_out, "CINFO=");
                    foreach (DataColumn column in res.Columns)
                    {
                        WriteToStream(_out, column.ColumnName);
                        WriteToStream(_out, Constants.COLUMN_DELIMITER);
                        WriteToStream(_out, column.MaxLength.ToString());
                        WriteToStream(_out, Constants.COLUMN_DELIMITER);
                        WriteToStream(_out, column.MaxLength.ToString());
                        WriteToStream(_out, Constants.COLUMN_DELIMITER);
                    }
                    WriteToStream(_out, Constants.DELIMITER);
                    WriteToStream(_out, "RDATA=");

                    foreach (DataRow row in res.Rows)
                    {
                        foreach (DataColumn column in res.Columns)
                        {
                            MocaType columnType = MocaType.LookupClass(column.DataType);
                            WriteToStream(_out, columnType.TypeCode);

                            if (row[column] == null || row.IsNull(column))
                            {
                                WriteToStream(_out, "0");
                                WriteToStream(_out, Constants.DELIMITER);
                            }
                            else
                            {
                                if (columnType.Equals(MocaType.RESULTS))
                                {
                                    byte[] value = EncodeResults((DataTable)row[column], null);
                                    WriteToStream(_out, value.Length.ToString());
                                    WriteToStream(_out, Constants.DELIMITER);
                                    _out.Write(value, 0, value.Length);
                                }
                                else if (columnType.Equals(MocaType.BINARY))
                                {
                                    byte[] value = (byte[])row[column];
                                    WriteToStream(_out, (value.Length + 8).ToString());
                                    WriteToStream(_out, Constants.DELIMITER);
                                    WriteToStream(_out, value.Length.ToString("x8"));
                                    _out.Write(value, 0, value.Length);
                                }
                                else if (columnType.Equals(MocaType.BOOLEAN))
                                {
                                    WriteToStream(_out, "1");
                                    WriteToStream(_out, Constants.DELIMITER);
                                    bool tmpbool;
                                    WriteToStream(_out, Boolean.TryParse(row[column].ToString(), out tmpbool)
                                                            ? (tmpbool) ? '1' : '0'
                                                            : '0');
                                }
                                else
                                {
                                    string value;
                                    if (columnType.Equals(MocaType.DATETIME))
                                    {
                                        value = ((DateTime)row[column]).ToString("yyyyMMddHHmmss");
                                    }
                                    else if (columnType.Equals(MocaType.DOUBLE))
                                    {
                                        double dbl = (double)row[column];
                                        value = dbl.ToString(GlobalNumberFormatInfo);
                                    }
                                    else if (columnType.Equals(MocaType.INTEGER))
                                    {
                                        int intValue = (int)row[column];
                                        value = intValue.ToString(GlobalNumberFormatInfo);
                                    }
                                    else
                                    {
                                        value = row[column].ToString();
                                    }
                                    WriteToStream(_out, value.Length.ToString());
                                    WriteToStream(_out, Constants.DELIMITER);
                                    WriteToStream(_out, value);
                                }
                            }
                        }
                    }
                }
            }
            catch (IOException e)
            {
                throw new ArgumentException("Unexpected memory problem: " + e, e);
            }
            return _out;
        }

        private static void WriteToStream(MemoryStream stream, char data)
        {
            byte[] byteData = GetEncoding().GetBytes(new char[1] {data});
            stream.Write(byteData, 0, byteData.Length);
        }

        private static void WriteToStream(MemoryStream stream, string data)
        {
            byte[] byteData = ToByteArray(data);
            stream.Write(byteData, 0, byteData.Length);
        }
    }
}