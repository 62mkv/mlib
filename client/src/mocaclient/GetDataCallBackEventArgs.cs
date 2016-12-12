using System;
using System.Collections;
using RedPrairie.MOCA.Exceptions;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// Delegate function to handle the results of a asynchronous 
    /// call to the GetData methods.
    /// </summary>
    /// <param name="e">The results of the command as <see cref="GetDataCallBackEventArgs{T}"/></param>
    public delegate void GetDataCallBack<T>(GetDataCallBackEventArgs<T> e) where T : class, IEnumerable;

    /// <summary>
    /// An <see cref="EventArgs"/> class that is used to return the results of a
    /// GetData call
    /// </summary>
    public class GetDataCallBackEventArgs<T>:EventArgs 
        where T : class, IEnumerable
    {
        #region Private Fields
        private readonly GetDataResult<T> result;
        #endregion

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="ExecuteCallBackEventArgs"/> class.
        /// </summary>
        /// <param name="result">The <see cref="ExecuteCommandResult"/> result.</param>
        internal GetDataCallBackEventArgs(GetDataResult<T> result)
        {
            this.result = result;
        }

        #endregion

        #region Properties

        /// <summary>
        /// Gets the resulting data of the command
        /// </summary>
        public T Data
        {
            get { return result.Data; }
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
