using System;
using System.Data;
using System.Diagnostics;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Exceptions;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// A stuct that holds the command to be run,
    /// previous application ID, and callback for
    /// async threading purposes
    /// </summary>
    internal class InternalCommand : IInternalCommand
    {
        #region Private Fields
        private string applicationID;
        private readonly ExecuteCallBack executeCallBack;
        private MocaException exception; //The text of the error if one occured
        private string prevApplicationID;
        private object result;
        #endregion

        #region Constructors
        
        /// <summary>
        /// Initializes a new instance of the <see cref="InternalCommand"/> object.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="applicationID">The application ID.</param>
        /// <param name="executeCallBack">The callback to call when command is complete.</param>
        public InternalCommand(Command command, string applicationID, ExecuteCallBack executeCallBack)
        {
            this.Command = command;
            this.applicationID = applicationID;
            this.executeCallBack = executeCallBack;
        }

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets the application ID.
        /// </summary>
        /// <value>The application ID.</value>
        public string ApplicationID
        {
            [DebuggerStepThrough]
            get { return applicationID; }
            set { applicationID = value; }
        }

        /// <summary>
        /// Gets or sets the command to execute
        /// </summary>
        public Command Command { get; set; }

        /// <summary>
        /// Gets the command delegate.
        /// </summary>
        /// <value>The command delegate.</value>
        public Delegate CommandDelegate
        {
            get { return executeCallBack; }
        }       

        /// <summary>
        /// Gets a value indicating whether this instance is data set.
        /// </summary>
        /// <value>
        /// 	<c>true</c> if this instance is data set; otherwise, <c>false</c>.
        /// </value>
        public bool IsDataSet
        {
            get { return true; }
        }

        /// <summary>
        /// Gets or sets the exception.
        /// </summary>
        /// <value>The exception.</value>
        public MocaException Exception
        {
            get { return exception; }
            set { exception = value; }
        }

        /// <summary>
        /// The callback to call when the command is complete
        /// </summary>
        public ExecuteCallBack ExecuteCallBack
        {
            get { return executeCallBack; }
        }

        /// <summary>
        /// Gets the mapping data.
        /// </summary>
        /// <value>The mapping data.</value>
        public MappingData MappingData
        {
            get { return null; }
        }

        /// <summary>
        /// Gets the object resolver.
        /// </summary>
        /// <value>The object resolver.</value>
        public IObjectResolver ObjectResolver
        {
            get { return null; }
        }

        /// <summary>
        /// Gets or sets the application id that existed before the execution
        /// </summary>
        public string PrevApplicationID
        {
            get { return prevApplicationID; }
            set { prevApplicationID = value; }
        }

        /// <summary>
        /// Gets or sets the results.
        /// </summary>
        /// <value>The results.</value>
        public object Result
        {
            get { return result; }
            set { result = value; }
        }

        /// <summary>
        /// Gets the status code.
        /// </summary>
        /// <value>The status code.</value>
        public int StatusCode
        {
            get
            {
                return (exception != null) ? exception.ErrorCode : 0;
            }
        }

        /// <summary>
        /// Gets a value indicating whether  the command manipulates application ID.
        /// </summary>
        /// <value><c>true</c> if the command manipulates application ID; otherwise, <c>false</c>.</value>
        public bool UsesApplicationID
        {
            get { return prevApplicationID != null; }
        }
        #endregion

        #region Public Methods
        /// <summary>
        /// Gets the delegate parameters for the callback.
        /// </summary>
        /// <returns>An object array of the parameters</returns>
        public object[] GetDelegateParameters()
        {
            ExecuteCommandResult execResult =
                new ExecuteCommandResult(Command.CommandText, StatusCode, result as DataSet, exception);

            return new object[] { new ExecuteCallBackEventArgs(execResult) };
        }
        #endregion
    }
}