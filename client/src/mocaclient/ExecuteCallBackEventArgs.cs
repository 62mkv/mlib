using System;
using System.Data;
using RedPrairie.MOCA.Exceptions;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// Delegate function to handle the results of a Asynchronous MOCA call
    /// </summary>
    /// <param name="e">The results of the command as <see cref="ExecuteCallBackEventArgs"/></param>
    public delegate void ExecuteCallBack(ExecuteCallBackEventArgs e);

    /// <summary>
    /// An <see cref="EventArgs"/> class that is used to return the results of an execute command
    /// </summary>
    public class ExecuteCallBackEventArgs : EventArgs
    {
        #region Private Fields
        private readonly ExecuteCommandResult result;
        #endregion

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="ExecuteCallBackEventArgs"/> class.
        /// </summary>
        /// <param name="result">The <see cref="ExecuteCommandResult"/> result.</param>
        public ExecuteCallBackEventArgs(ExecuteCommandResult result)
        {
            this.result = result;
        }

        #endregion

        #region Properties

        /// <summary>
        /// Gets the <see cref="DataSet"/> result set, or null if none exists
        /// </summary>
        public DataSet Data
        {
            get { return result.Data; }
        }

        /// <summary>
        /// Gets the first table of the <see cref="DataSet"/> results 
        /// </summary>
        /// <remarks>
        /// If MOCA impliments multiple tables in a result set, this would
        /// return the first table in the array
        /// </remarks>
        public DataTable TableData
        {
            get { return result.TableData; }
        }

        /// <summary>
        /// Gets the first table <see cref="DataView"/> of the <see cref="DataSet"/> results 
        /// </summary>
        /// <remarks>
        /// If MOCA impliments multiple tables in a result set, this would
        /// return a <see cref="DataView"/> the first table in the array
        /// </remarks>
        public DataView ViewData
        {
            get { return result.ViewData; }
        }

        /// <summary>
        /// Gets the server status code of the command
        /// </summary>
        public int StatusCode
        {
            get { return result.StatusCode; }
        }

        /// <summary>
        /// Gets if the command returned without error.
        /// </summary>
        public bool IsOK
        {
            get { return result.IsOK; }
        }

        /// <summary>
        /// Gets if the command returned successfully and affected or returned rows
        /// </summary>
        public bool HasData
        {
            get { return result.HasData; }
        }

        /// <summary>
        /// Gets the command that was executed
        /// </summary>
        public string Command
        {
            get { return result.Command; }
        }

        /// <summary>
        /// Gets the text of an error if it occurs
        /// </summary>
        public MocaException Error
        {
            get { return result.Error; }
        }

        #endregion
    }
}