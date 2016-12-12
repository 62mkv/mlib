using System;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Exceptions
{
    /// <summary> 
    /// Used to indicate a failure in the MOCA protocol.
    /// </summary>
    [Serializable]
    public class ProtocolException : MocaException
    {
        private const int CODE = MocaErrors.eMCC_PROTOCOL_ERROR;

        /// <param name="message">A message that represents describes the error</param>
        public ProtocolException(string message) : base(CODE, message)
        {
        }

        /// <param name="message">A message that represents describes the error</param>
        /// <param name="innerException">The exception that caused the error</param>
        public ProtocolException(string message, Exception innerException) :
            base(CODE, message, innerException)
        {
        }
    }
}