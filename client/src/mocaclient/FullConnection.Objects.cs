using System.Collections;
using RedPrairie.MOCA.Client.Interfaces;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Exceptions;

namespace RedPrairie.MOCA.Client
{
    public partial class FullConnection : IDataObjectProvider
    {
        #region Private Fields
        private readonly ReflectionColumnMapper mapper = new ReflectionColumnMapper();
        #endregion

        #region Implementation of IDataObjectProvider

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
        public virtual void BeginGetData<TCollection>(string command, GetDataCallBack<TCollection> callBack) where TCollection : class, IEnumerable
        {
            BeginGetData(command, null, callBack);
        }

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
        public virtual void BeginGetData<TCollection>(string command, string applicationID, GetDataCallBack<TCollection> callBack) where TCollection : class, IEnumerable
        {
            BeginGetData(command, applicationID, callBack, null, null);
        }

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
        public virtual TCollection GetData<TCollection>(string command)
            where TCollection : class, IEnumerable
        {
            return GetData<TCollection>(command, "");
        }

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
        public virtual TCollection GetData<TCollection>(string command, string applicationID)
            where TCollection : class, IEnumerable
        {
            return GetData<TCollection>(command, applicationID, null, null);
        }

        #endregion

        #region Protected Methods

        /// <summary>
        /// Begins the command execution, and returns a collection of data.
        /// </summary>
        /// <typeparam name="TCollection">The type of the collection.</typeparam>
        /// <param name="command">The command to execute to retrieve the data.</param>
        /// <param name="applicationID">The application ID that is executing the command.</param>
        /// <param name="callBack">The call back delegate method.</param>
        /// <param name="mappingData">
        /// The mapping data used to transform the data into a collection.
        /// </param>
        /// <param name="resolver">
        /// The object resolver used to create new data objects. Needs to impliment the <see cref="IObjectResolver"/> interface.
        /// </param>
        /// <exception cref="MocaException">
        /// Thrown if an error occurs, either in communication with
        /// the server, or upon execution of the command.
        /// </exception>
        protected void BeginGetData<TCollection>(string command, string applicationID,
                                                      GetDataCallBack<TCollection> callBack,
                                                      MappingData mappingData, IObjectResolver resolver)
            where TCollection : class, IEnumerable
        {
            if (mappingData == null)
            {
                mappingData = mapper.GetData(typeof(TCollection));
            }
            if (resolver == null)
            {
                resolver = new DefaultObjectResolver();
            }

            IInternalCommand cmd =
                new InternalObjectCommand<TCollection>(command,
                    applicationID, mappingData, resolver, callBack);

            _connection.BeginExecuteCommand(new InternalCallBackEventArgs(cmd,
                                           InternalCommandCompleteCallBack));
        }


        /// <summary>
        /// Executes the specified command and retieves the data in a collection form
        /// specified by <typeparamref name="TCollection"/>.
        /// </summary>
        /// <typeparam name="TCollection">The generic collection type to be retrieved</typeparam>
        /// <param name="command">The command to execute to retrieve the data.</param>
        /// <param name="applicationID">The application ID that is excecuting the command.</param>
        /// <param name="mappingData">The mapping data used to transform the data into a collection.</param>
        /// <param name="resolver">The object resolver used to create new data objects. Needs to impliment the <see cref="IObjectResolver"/> interface.</param>
        /// <returns>
        /// A collection of objects specified by <typeparamref name="TCollection"/>.
        /// </returns>
        /// <exception cref="MocaException">
        /// Thrown if an error occurs, either in communication with
        /// the server, or upon execution of the command.
        /// </exception>
        protected TCollection GetData<TCollection>(string command, string applicationID,
                                                MappingData mappingData, IObjectResolver resolver)
            where TCollection : class
        {
            if (mappingData == null)
            {
                mappingData = mapper.GetData(typeof(TCollection));
            }
            if (resolver == null)
            {
                resolver = new DefaultObjectResolver();
            }

            try
            {
                //Get the data
                TCollection data =
                    _connection.Execute(new Command(command), applicationID, mappingData, resolver) as TCollection; 

                //Need to call the digital signature check to remove the key but no error
                CheckForDigitalSignatureChallenge(null);

                return data;
            }
            catch (MocaException ex)
            {
                //Need to call the digital signature check to remove the key but no error
                if (CheckForDigitalSignatureChallenge(ex))
                {
                    return GetData<TCollection>(command, applicationID, mappingData, resolver);
                }

                //Check failed or it's not a digital signature error so throw the exception
                throw;
            }
        }

        #endregion
    }
}