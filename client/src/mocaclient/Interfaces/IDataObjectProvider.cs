using RedPrairie.MOCA.Exceptions;

namespace RedPrairie.MOCA.Client.Interfaces
{
    /// <summary>
    /// An object data provider from MOCA that returns
    /// a collection of objects rather than the normal
    /// table structure.
    /// </summary>
    interface IDataObjectProvider
    {
        /// <summary>
        /// Begins the command execution, and returns a collection of data.
        /// </summary>
        /// <typeparam name="TCollection">The type of the collection.</typeparam>
        /// <param name="command">The command.</param>
        /// <param name="callBack">The call back delegate method.</param>
        /// <exception cref="MocaException">
        /// Thrown if an error occurs, either in communication with
        /// the server, or upon execution of the command.
        /// </exception>
        void BeginGetData<TCollection>(string command, GetDataCallBack<TCollection> callBack)
            where TCollection : class, System.Collections.IEnumerable;

        /// <summary>
        /// Begins the command execution, and returns a collection of data.
        /// </summary>
        /// <typeparam name="TCollection">The type of the collection.</typeparam>
        /// <param name="command">The command.</param>
        /// <param name="applicationID">The application ID.</param>
        /// <param name="callBack">The call back delegate method.</param>
        /// <exception cref="MocaException">
        /// Thrown if an error occurs, either in communication with
        /// the server, or upon execution of the command.
        /// </exception>
        void BeginGetData<TCollection>(string command, string applicationID, GetDataCallBack<TCollection> callBack)
            where TCollection : class, System.Collections.IEnumerable;
        
        /// <summary>
        /// Executes the specified command and retieves the data in a collection form
        /// specified by <typeparamref name="TCollection"/>.
        /// </summary>
        /// <typeparam name="TCollection">The generic collection type to be retrieved</typeparam>
        /// <param name="command">The command to execute to retrieve the data.</param>
        /// <returns>A collection of objects specified by <typeparamref name="TCollection"/>.</returns>
        /// <exception cref="MocaException">
        /// Thrown if an error occurs, either in communication with
        /// the server, or upon execution of the command.
        /// </exception>
        TCollection GetData<TCollection>(string command)
            where TCollection : class, System.Collections.IEnumerable;
        
        /// <summary>
        /// Executes the specified command and retieves the data in a collection form
        /// specified by <typeparamref name="TCollection"/>.
        /// </summary>
        /// <typeparam name="TCollection">The generic collection type to be retrieved</typeparam>
        /// <param name="command">The command to execute to retrieve the data.</param>
        /// <param name="applicationID">The application ID that is excecuting the command.</param>
        /// <returns>
        /// A collection of objects specified by <typeparamref name="TCollection"/>.
        /// </returns>
        /// <exception cref="MocaException">
        /// Thrown if an error occurs, either in communication with
        /// the server, or upon execution of the command.
        /// </exception>
        TCollection GetData<TCollection>(string command, string applicationID)
            where TCollection : class, System.Collections.IEnumerable;

    }
}
