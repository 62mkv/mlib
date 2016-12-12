using System;

namespace RedPrairie.MOCA.Exceptions
{
    /// <summary> 
    /// Package-private exception class that acts as the default MOCA exception in
    /// cases where a more specific exception cannot be established.
    /// </summary>
    [Serializable]
    internal class ServerExecutionException : MocaException
    {
        /// <param name="code">the error code returned from the server.</param>
        /// <param name="message">the default message returned from the server.</param>
        public ServerExecutionException(int code, string message) : base(code, message)
        {
        }
    }
}