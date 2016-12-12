using System;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Exceptions
{
    /// <summary>
    /// An exception class that encapsulates a login failure error (523).
    /// </summary>
    [Serializable]
    public class LockOutException : MocaException
    {
        private const int CODE = MocaErrors.eMCS_ACCOUNT_LOCK_OUT;

        /// <summary>
        /// Initializes a new instance of the <see cref="LoginFailedException"/> class.
        /// </summary>
        /// <param name="message">The login failure message.</param>
        public LockOutException(string message) : base(CODE, message)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="LoginFailedException"/> class.
        /// </summary>
        /// <param name="message">The login failure message.</param>
        /// <param name="innerException">The inner exception.</param>
        public LockOutException(string message, Exception innerException)
            : base(CODE, message, innerException)
        {
        }
    }
}