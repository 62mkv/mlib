using System;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// A command result base that returns the data of a command
    /// </summary>
    public abstract class ExecuteCommandResultBase<T>
    {
        /// <summary>
        /// The command that was executed
        /// </summary>
        protected string command = string.Empty; //The command that was executed
        
        /// <summary>
        /// The exception that the results could throw
        /// </summary>
        protected MocaException exception; //The text of the error if one occured
        
        /// <summary>
        /// The status code of the results
        /// </summary>
        protected int statusCode; //The result code of the command

        /// <summary>
        /// Initializes a new instance of the <see cref="T:System.Object" /> class.
        /// </summary>
        protected ExecuteCommandResultBase()
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ExecuteCommandResult"/> class.
        /// </summary>
        /// <param name="exception">The exception.</param>
        protected ExecuteCommandResultBase(Exception exception)
        {
            this.exception = new MocaException(-99999, exception.Message, exception);
        }

        /// <summary>
        /// Gets the server status code of the command
        /// </summary>
        public int StatusCode
        {
            get { return statusCode; }
        }

        /// <summary>
        /// Gets the data results of the execution.
        /// </summary>
        /// <value>The data results.</value>
        public abstract T Data { get; }

        /// <summary>
        /// Gets if the command returned successfully and affected or returned rows
        /// </summary>
        public abstract bool HasData { get; }

        /// <summary>
        /// Gets if the command returned without error. Uses <see cref="MocaErrors"/> 
        /// to determine if an error has occured
        /// </summary>
        public virtual bool IsOK
        {
            get { return MocaErrors.StatusReturnsResults(statusCode); }
        }

        /// <summary>
        /// Gets the command that was executed
        /// </summary>
        public string Command
        {
            get { return command; }
        }

        /// <summary>
        /// Gets the text of an error if it occurs.
        /// stat code will be -99999 if it is a .NET error
        /// </summary>
        public MocaException Error
        {
            get { return exception; }
        }
    }
}