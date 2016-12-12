using System;
using System.Data;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// A class that is used to return the results of an execute command
    /// </summary>
    public class ExecuteCommandResult : ExecuteCommandResultBase<DataSet>
    {
        #region Private Fields

        private readonly DataSet data; //The dataset of the results

        #endregion

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="ExecuteCommandResult"/> class.
        /// </summary>
        /// <param name="statusCode">The status code.</param>
        /// <param name="data">The data set of the result.</param>
        public ExecuteCommandResult(int statusCode, DataSet data)
        {
            base.statusCode = statusCode;
            this.data = data;

            SetTableName();
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ExecuteCommandResult"/> class.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="statusCode">The status code.</param>
        /// <param name="data">The data.</param>
        public ExecuteCommandResult(string command, int statusCode, DataSet data) : this(statusCode, data)
        {
            this.command = command;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ExecuteCommandResult"/> class.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="statusCode">The status code.</param>
        /// <param name="data">The data.</param>
        /// <param name="exception">The exception.</param>
        public ExecuteCommandResult(string command, int statusCode, DataSet data, MocaException exception)
            : this(command, statusCode, data)
        {
            this.exception = exception;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ExecuteCommandResult"/> class.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="exception">The exception.</param>
        public ExecuteCommandResult(string command, MocaException exception)
            : this(command, exception.ErrorCode, exception.Results)
        {
            this.exception = exception;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ExecuteCommandResult"/> class.
        /// </summary>
        /// <param name="exception">The exception.</param>
        public ExecuteCommandResult(Exception exception) : base(exception)
        {
        }

        #endregion

        #region Properties

        /// <summary>
        /// Gets the <see cref="DataSet"/> result set, or null if none exists
        /// </summary>
        public override DataSet Data
        {
            get { return data; }
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
            get
            {
                if (data != null && data.Tables.Count > 0)
                {
                    return data.Tables[0];
                }
                return null;
            }
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
            get
            {
                if (data != null && data.Tables.Count > 0)
                {
                    return data.Tables[0].DefaultView;
                }
                return null;
            }
        }

        /// <summary>
        /// Gets if the command returned successfully and affected or returned rows
        /// </summary>
        public override bool HasData
        {
            get { return (statusCode == MocaErrors.eOK &&
                         data != null &&
                         data.Tables.Count > 0 &&
                         data.Tables[0].Rows.Count > 0); }
        }

        #endregion

        #region Private Methods
        /// <summary>
        /// Sets the name of the table to the command.
        /// </summary>
        private void SetTableName()
        {
            if (data != null && data.Tables.Count > 0 && !String.IsNullOrEmpty(command))
                data.Tables[0].TableName = command;
        }
        #endregion
    }
}