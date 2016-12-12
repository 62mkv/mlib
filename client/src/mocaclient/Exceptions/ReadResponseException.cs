using System;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Exceptions
{
    /// <summary>
    /// An exception class that encapsulates a MOCA read response error (204).
    /// </summary>
    [Serializable]
    public class ReadResponseException : MocaException
    {
        private const int CODE = MocaErrors.eMCC_RECV_ERROR;

        /// <summary>
        /// Initializes a new instance of the <see cref="ReadResponseException"/> class.
        /// </summary>
        public ReadResponseException() : this(null)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ReadResponseException"/> class.
        /// </summary>
        /// <param name="innerException">The inner exception.</param>
        public ReadResponseException(Exception innerException) :
            base(CODE, "MOCA socket communication failure", innerException)
        {
        }
    }
    /// <summary>
    /// An exception class that encapsulates a MOCA unique constraint error (-1).
    /// </summary>
    public class UniqueConstraintException : MocaException
    {
        private const int CODE = MocaErrors.eLENGTH_ERROR;

        /// <summary>
        /// Initializes a new instance of the <see cref="UniqueConstraintException"/> class.
        /// </summary>
        public UniqueConstraintException()
            : this(null)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="UniqueConstraintException"/> class.
        /// </summary>
        /// <param name="message">The message.</param>
        public UniqueConstraintException(string message)
            : base(CODE, message)
        {
        }
    }
}