using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Exceptions;

namespace RedPrairie.MOCA.Client
{
    internal interface IInternalCommand
    {
        /// <summary>
        /// Gets the application ID.
        /// </summary>
        /// <value>The application ID.</value>
        string ApplicationID { get; set; }

        /// <summary>
        /// Gets or sets the command to execute
        /// </summary>
        Command Command { get; set; }

        /// <summary>
        /// Gets the command delegate.
        /// </summary>
        /// <value>The command delegate.</value>
        System.Delegate CommandDelegate { get; } 

        /// <summary>
        /// Gets a value indicating whether this instance is data set.
        /// </summary>
        /// <value>
        /// 	<c>true</c> if this instance is data set; otherwise, <c>false</c>.
        /// </value>
        bool IsDataSet { get; }

        /// <summary>
        /// Gets or sets the exception.
        /// </summary>
        /// <value>The exception.</value>
        MocaException Exception { get; set; }

        /// <summary>
        /// Gets the mapping data.
        /// </summary>
        /// <value>The mapping data.</value>
        MappingData MappingData { get; }

        /// <summary>
        /// Gets the object resolver.
        /// </summary>
        /// <value>The object resolver.</value>
        IObjectResolver ObjectResolver { get; }

        /// <summary>
        /// Gets or sets the application id that existed before the execution
        /// </summary>
        string PrevApplicationID { get; set; }

        /// <summary>
        /// Gets or sets the results.
        /// </summary>
        /// <value>The results.</value>
        object Result { get; set; }

        /// <summary>
        /// Gets the status code.
        /// </summary>
        /// <value>The status code.</value>
        int StatusCode { get; }

        /// <summary>
        /// Gets a value indicating whether  the command manipulates application ID.
        /// </summary>
        /// <value><c>true</c> if the command manipulates application ID; otherwise, <c>false</c>.</value>
        bool UsesApplicationID { get; }

        /// <summary>
        /// Gets the delegate parameters for the callback.
        /// </summary>
        /// <returns>An object array of the parameters</returns>
        object[] GetDelegateParameters();
    }
}