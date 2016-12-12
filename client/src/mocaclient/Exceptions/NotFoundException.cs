using System;
using System.Data;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Exceptions
{
    /// <summary> 
    /// Represents that a query did not return any data, or that an updat
    /// did not affect any rows.
    /// </summary>
    [Serializable]
    public class NotFoundException : MocaException
    {
        private const int DB_CODE = MocaErrors.eDB_NO_ROWS_AFFECTED;
        //private const int SERVER_CODE = MocaErrors.eSRV_NO_ROWS_AFFECTED;

        /// <summary> 
        /// Creates a default NoDataException
        /// </summary>
        public NotFoundException() : this(DB_CODE)
        {
        }

        /// <summary> 
        /// Creates a NoDataException with the given results.  The results
        /// are used to provide column information to the caller.
        /// </summary>
        /// <param name="results">a results object to be used to provide
        /// column information to the caller. </param>
        public NotFoundException(DataSet results) : this(DB_CODE, results)
        {
        }

        /// <summary> 
        /// Creates a NoDataException with the given code.
        /// </summary>
        /// <param name="code">what MOCA code value to use for this exception.  This
        /// argument must be either DB_CODE or SERVER_CODE.
        /// </param>
        public NotFoundException(int code) : this(code, null)
        {
        }

        /// <summary> 
        /// Creates a NoDataException with the given code and results.  The results
        /// are used to provide column information to the caller.
        /// </summary>
        /// <param name="code">what MOCA code value to use for this exception.  This
        /// argument must be either DB_CODE or SERVER_CODE. </param>
        /// <param name="results">a results object to be used to provide
        /// column information to the caller. </param>
        public NotFoundException(int code, DataSet results) : base(code, "no rows affected")
        {
            _results = results;
        }
    }
}