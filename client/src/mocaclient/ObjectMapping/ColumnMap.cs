using System;
using System.Reflection;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;

namespace RedPrairie.MOCA.Client.ObjectMapping
{
    /// <summary>
    /// This class holds the metadata about the columns including 
    /// the mapping IL delegate name, type and name. 
    /// </summary>
    public class ColumnMap
    {
        #region Private Fields
        private MappingDelegate columnMapper;
        private string columnName;
        private readonly Type dataType;
        private object defaultValue = null;
        private PropertyInfo propertyInfo;
        private string propertyName;
        #endregion

        #region Constructor

        /// <summary>
        /// Initializes a new instance of the <see cref="ColumnMap"/> class.
        /// </summary>
        /// <param name="columnName">The table column name.</param>
        /// <param name="dataType">The type of the column.</param>
        /// <param name="propertyName">The name of the property to map to.</param>
        public ColumnMap(string columnName, Type dataType, string propertyName)
        {
            this.columnName = columnName;
            this.dataType = dataType;
            this.propertyName = propertyName;
        }

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets or sets the column map.
        /// </summary>
        /// <value>The column map.</value>
        public MappingDelegate ColumnMapper
        {
            get { return columnMapper; }
            set { columnMapper = value; }
        }

        /// <summary>
        /// Gets the name of the column.
        /// </summary>
        /// <value>The name of the column.</value>
        public string ColumnName
        {
            get { return columnName; }
        }

        /// <summary>
        /// Gets the data type of the column.
        /// </summary>
        /// <value>The type of the column.</value>
        public Type DataType
        {
            get { return dataType; }
        }


        /// <summary>
        /// Gets or sets the default value.
        /// </summary>
        /// <value>The default value.</value>
        public object DefaultValue
        {
            get { return defaultValue; }
            set { defaultValue = value; }
        }

        /// <summary>
        /// Gets or sets the property info.
        /// </summary>
        /// <value>The property info.</value>
        public PropertyInfo PropertyInfo
        {
            get { return propertyInfo; }
            set { propertyInfo = value; }
        }

        /// <summary>
        /// Gets the name of the property.
        /// </summary>
        /// <value>The name of the property.</value>
        public string PropertyName
        {
            get { return propertyName; }
        }

        #endregion
    }
}
