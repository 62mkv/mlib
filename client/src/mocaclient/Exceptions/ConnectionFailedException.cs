using System;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Exceptions
{
    /// <summary>
    /// An exception class that encapsulates a MOCA connection failure error (202).
    /// </summary>
    [Serializable]
    public class ConnectionFailedException : MocaException
    {
        private const int CODE = MocaErrors.eMCC_FAILED_TO_CONNECT;

        /// <summary>
        /// Initializes a new instance of the <see cref="ConnectionFailedException"/> class.
        /// </summary>
        /// <param name="message">The message.</param>
        public ConnectionFailedException(String message) : base(CODE, message)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ConnectionFailedException"/> class.
        /// </summary>
        /// <param name="message">The message.</param>
        /// <param name="innerException">The inner exception.</param>
        public ConnectionFailedException(String message, Exception innerException)
            : base(CODE, message, innerException)
        {
        }
    }
}