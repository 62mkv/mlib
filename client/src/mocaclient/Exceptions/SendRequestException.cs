using System;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Exceptions
{
    /// <summary>
    /// An exception class that encapsulates a MOCA command request error (203).
    /// </summary>
    [Serializable]
    public class SendRequestException : MocaException
    {
        private const int CODE = MocaErrors.eMCC_SEND_ERROR;

        /// <summary>
        /// Initializes a new instance of the <see cref="SendRequestException"/> class.
        /// </summary>
        public SendRequestException() : this(null)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="SendRequestException"/> class.
        /// </summary>
        /// <param name="innerException">The inner exception that raised the error.</param>
        public SendRequestException(Exception innerException) :
            base(CODE, "MOCA socket communication failure", innerException)
        {
        }
    }
}