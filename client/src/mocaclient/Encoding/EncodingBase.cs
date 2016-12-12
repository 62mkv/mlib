using System;
using System.Globalization;

namespace RedPrairie.MOCA.Client.Encoding
{
    /// <summary>
    /// This class contains encoding formats and methods that involve MOCA
    /// specific encoding / control
    /// </summary>
    public class EncodingBase
    {

        /// <summary>
        /// Initializes the <see cref="EncodingBase"/> class.
        /// </summary>
        static EncodingBase()
        {
            globalNumberFormatInfo.NumberDecimalSeparator = ".";
        }

        private static System.Text.Encoding currentEncoding = System.Text.Encoding.Default;
        private readonly static NumberFormatInfo globalNumberFormatInfo = new NumberFormatInfo();
        /// <summary>
        /// The date time MOCA string.
        /// </summary>
        public const string DATE_TIME_FORMAT = "yyyyMMddHHmmss";

        /// <summary>
        /// Gets the global number format information to parse doubles.
        /// </summary>
        /// <value>The global number format information.</value>
        public static NumberFormatInfo GlobalNumberFormatInfo
        {
            get { return globalNumberFormatInfo; }
        }

        /// <summary>
        /// Converts a MOCA DateTime to a .NET <see cref="DateTime"/> object
        /// </summary>
        /// <param name="data">The MOCA string to convert</param>
        /// <returns>A <see cref="DateTime"/> object or null</returns>
        protected static DateTime ConvertDateTime(string data)
        {
            DateTime d2;
            TryParseDateTime(data, out d2);
            return d2;
        }

        /// <summary>
        /// Converts an array of sbytes to an array of chars
        /// </summary>
        /// <param name="byteArray">The array of sbytes to convert</param>
        /// <returns>The new array of chars</returns>
        protected static string ToCharArray(byte[] byteArray)
        {
            return currentEncoding.GetString(byteArray);
        }

        /// <summary>
        /// Converts a string to an array of bytes
        /// </summary>
        /// <param name="sourceString">The string to be converted</param>
        /// <returns>The new array of bytes</returns>
        protected static byte[] ToByteArray(string sourceString)
        {
            return currentEncoding.GetBytes(sourceString);
        }

        /// <summary>
        /// Gets the byte length of the data once encoded
        /// </summary>
        /// <param name="data">The data to get the length of</param>
        /// <param name="padValues">If true the value is 0 padded to 6 digits</param>
        /// <returns>The length of the data as an int</returns>
        protected static string GetEncodedLength(string data, bool padValues)
        {
            return GetEncodedLength(currentEncoding.GetByteCount(data), padValues);
        }

        /// <summary>
        /// Gets the byte length of the data once encoded
        /// </summary>
        /// <param name="byteCount">The byte count.</param>
        /// <param name="padValues">If true the value is 0 padded to 6 digits</param>
        /// <returns>A string padded correctly for encoding</returns>
        protected static string GetEncodedLength(int byteCount, bool padValues)
        {
            return byteCount.ToString(padValues ? "x6" : "D");
        }

        /// <summary>
        /// Pads the hex value.
        /// </summary>
        /// <param name="value">The value to pad</param>
        /// <returns></returns>
        protected static string PadHexValue(int value)
        {
            return value.ToString("D6");
        }

        /// <summary>
        /// Gets the encoded byte count of the string
        /// </summary>
        /// <param name="data">The string to be encoded</param>
        /// <returns>The number of bytes</returns>
        protected static int GetByteCount(string data)
        {
            return currentEncoding.GetByteCount(data);
        }

        /// <summary>
        /// Gets the supported encoding type.
        /// </summary>
        /// <returns>The <see cref="System.Text.Encoding"/> used.</returns>
        public static System.Text.Encoding GetEncoding()
        {
            return currentEncoding;
        }

        /// <summary>
        /// Sets the current encoding.
        /// </summary>
        /// <param name="newEncodingName">
        /// The new encoding format, following the ISO standard
        /// </param>
        /// <returns>
        /// <c>true</c> if the new encoding was sucessfully parsed; otherwise <c>false
        /// </c></returns>
        public static bool SetEncoding(string newEncodingName)
        {
            if (!String.IsNullOrEmpty(newEncodingName))
            {
                try
                {
                    System.Text.Encoding newEncoding =
                        System.Text.Encoding.GetEncoding(newEncodingName);

                    if (newEncoding != null)
                    {
                        currentEncoding = newEncoding;
                        return true;
                    }
                }
                catch (ArgumentException)
                {
                }
            }

            return false;
        }

        /// <summary>
        /// Tries to parse the parse the given data to a date time.
        /// </summary>
        /// <param name="data">The data.</param>
        /// <param name="dateTime">The date time.</param>
        /// <returns><c>true</c> if successful; otherwise <c>false</c>.</returns>
        public static bool TryParseDateTime(string data, out DateTime dateTime)
        {
            DateTimeFormatInfo dfi = new DateTimeFormatInfo();
            dfi.FullDateTimePattern = DATE_TIME_FORMAT;

            try
            {
                dateTime = DateTime.ParseExact(data, "F", dfi);
                return true;
            }
            catch (FormatException)
            {
            }
            catch (ArgumentException)
            {
            }

            dateTime = new DateTime();
            return false;
        }
    }
}
