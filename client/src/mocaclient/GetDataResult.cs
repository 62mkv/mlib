using System;
using System.Collections;
using RedPrairie.MOCA.Exceptions;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// A generic data results class used for getting data back from
    /// anynchronous commands
    /// </summary>
    /// <typeparam name="TData">The type of the data.</typeparam>
    public class GetDataResult<TData> : ExecuteCommandResultBase<TData>
        where TData : class, IEnumerable
    {
        #region Private Fields

        private readonly TData data;

        #endregion

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="GetDataResult{TData}"/> class.
        /// </summary>
        /// <param name="statusCode">The status code.</param>
        /// <param name="data">The data set of the result.</param>
        public GetDataResult(int statusCode, TData data)
        {
            base.statusCode = statusCode;
            this.data = data;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="GetDataResult{TData}"/> class.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="statusCode">The status code.</param>
        /// <param name="data">The data.</param>
        public GetDataResult(string command, int statusCode, TData data) 
            : this(statusCode, data)
        {
            this.command = command;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="GetDataResult{TData}"/> class.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="statusCode">The status code.</param>
        /// <param name="data">The data.</param>
        /// <param name="exception">The exception.</param>
        public GetDataResult(string command, int statusCode, TData data, MocaException exception)
            : this(command, statusCode, data)
        {
            this.exception = exception;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="GetDataResult{TData}"/> class.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="exception">The exception.</param>
        public GetDataResult(string command, MocaException exception)
            : this(command, exception.ErrorCode, null)
        {
            this.exception = exception;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="GetDataResult{TData}"/> class.
        /// </summary>
        /// <param name="exception">The exception.</param>
        public GetDataResult(Exception exception) : base(exception)
        {
        }

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets the data results of the execution.
        /// </summary>
        /// <value>The data results.</value>
        public override TData Data
        {
            get { return data; }
        }

        /// <summary>
        /// Gets if the command returned successfully and affected or returned rows
        /// </summary>
        public override bool HasData
        {
            get { return data != null && data.GetEnumerator().Current != null; }
        } 
        #endregion
    }
}