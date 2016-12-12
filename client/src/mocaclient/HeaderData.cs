using System.Collections.Generic;
using System.Data;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// Holds the header data for a results set
    /// </summary>
    public class HeaderData
    {
        #region Private Fields

        private int protocol = 104;
        private int dataLength = 0;
        private int headerLength = 0;
        private int status = 0;
        private int columnCount = 0;
        private int commandCount = 0;
        private int rowCount = 0;
        private string dataTypeString;
        private int traceCounter = 0;
        private string message;
        private int messageLength = 0;
        private int duplicateColumns = -1;
        private int dataStart = 0;

        private List<DataColumn> columns = new List<DataColumn>();

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets or sets the protocol number.
        /// </summary>
        /// <value>The protocol number.</value>
        public int Protocol
        {
            get { return protocol; }
            set { protocol = value; }
        }

        /// <summary>
        /// Gets or sets the length of the data.
        /// </summary>
        /// <value>The length of the data.</value>
        public int DataLength
        {
            get { return dataLength; }
            set { dataLength = value; }
        }

        /// <summary>
        /// Gets or sets the length of the header.
        /// </summary>
        /// <value>The length of the header.</value>
        public int HeaderLength
        {
            get { return headerLength; }
            set { headerLength = value; }
        }

        /// <summary>
        /// Gets or sets the status code.
        /// </summary>
        /// <value>The status code.</value>
        public int Status
        {
            get { return status; }
            set { status = value; }
        }

        /// <summary>
        /// Gets or sets the column count.
        /// </summary>
        /// <value>The column count.</value>
        public int ColumnCount
        {
            get { return columnCount; }
            set { columnCount = value; }
        }


        /// <summary>
        /// Gets or sets the command count.
        /// </summary>
        /// <value>The command count.</value>
        public int CommandCount
        {
            get { return commandCount; }
            set { commandCount = value; }
        }

        /// <summary>
        /// Gets or sets the row count.
        /// </summary>
        /// <value>The row count.</value>
        public int RowCount
        {
            get { return rowCount; }
            set { rowCount = value; }
        }

        /// <summary>
        /// Gets or sets the data type string.
        /// </summary>
        /// <value>The data type string.</value>
        public string DataTypeString
        {
            get { return dataTypeString; }
            set { dataTypeString = value; }
        }

        /// <summary>
        /// Gets or sets the trace counter.
        /// </summary>
        /// <value>The trace counter.</value>
        public int TraceCounter
        {
            get { return traceCounter; }
            set { traceCounter = value; }
        }

        /// <summary>
        /// Gets or sets the error message.
        /// </summary>
        /// <value>The error message.</value>
        public string Message
        {
            get { return message; }
            set { message = value; }
        }

        /// <summary>
        /// Gets or sets the length of the error message.
        /// </summary>
        /// <value>The length of the error message.</value>
        public int MessageLength
        {
            get { return messageLength; }
            set { messageLength = value; }
        }

        /// <summary>
        /// Gets the number of duplicate columns.
        /// </summary>
        /// <value>The number of duplicate columns.</value>
        public int DuplicateColumns
        {
            get
            {
                if (duplicateColumns == -1)
                    CheckDuplicateColumns();

                return duplicateColumns;
            }
        }

        /// <summary>
        /// Gets or sets the start index position of the data.
        /// </summary>
        /// <value>The index in the whole string.</value>
        public int DataStart
        {
            get { return dataStart; }
            set { dataStart = value; }
        }

        /// <summary>
        /// Gets or sets the data columns.
        /// </summary>
        /// <value>The data columns as a generic list of <see cref="DataColumn"/> items.</value>
        public List<DataColumn> Columns
        {
            get { return columns; }
            set { columns = value; }
        }

        #endregion

        #region Public Methods
        /// <summary>
        /// Verifies that there are no duplicates in the column collection.
        /// If duplicates exist they begin to duplicate 
        /// </summary>
        public void CheckDuplicateColumns()
        {
            //Set duplicates = 0 to start
            duplicateColumns = 0;
            Dictionary<string, int> duplicates = new Dictionary<string, int>();

            for (int colCount = 0; colCount < columns.Count; colCount++)
            {
                string colName = columns[colCount].ColumnName;

                //Check for empty string
                if (string.IsNullOrEmpty(colName))
                {
                    colName = "column";
                    
                    if (!duplicates.ContainsKey(colName))
                        duplicates.Add(colName, 1);
                }

                if (duplicates.ContainsKey(colName))
                {
                    int duplicateCount = duplicates[colName];
                    //find next number that is not used
                    while (duplicates.ContainsKey(string.Format("{0}{1}", colName, duplicateCount)))
                    {
                        duplicateCount++;
                        duplicates[colName] = duplicateCount;
                    }
                    columns[colCount].ColumnName = string.Format("{0}{1}", colName, duplicateCount);

                    duplicates[colName] = duplicateCount + 1;
                }
                else
                {
                    duplicates.Add(colName, 1);
                }
            }

        }
        #endregion
    }
}