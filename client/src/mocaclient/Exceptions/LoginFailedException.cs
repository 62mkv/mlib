using System;

namespace RedPrairie.MOCA.Exceptions
{
    /// <summary>
    /// An exception class that encapsulates a login failure error (523).
    /// </summary>
    [Serializable]
    public class LoginFailedException : MocaException
    {
        private const int CODE = 523;

        /// <summary>
        /// Initializes a new instance of the <see cref="LoginFailedException"/> class.
        /// </summary>
        /// <param name="message">The login failure message.</param>
        public LoginFailedException(string message) : base(CODE, message)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="LoginFailedException"/> class.
        /// </summary>
        /// <param name="message">The login failure message.</param>
        /// <param name="innerException">The inner exception.</param>
        public LoginFailedException(string message, Exception innerException) : base(CODE, message, innerException)
        {
        }
    }
}