using System;
using System.Collections.Generic;
using System.Reflection;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;

namespace RedPrairie.MOCA.Client.ObjectMapping
{
    /// <summary>
    /// This class will contain the cached mapping data for this including a 
    /// modified ColumnData collection that will hold information about each column, 
    /// the destination type and any information about the type of collection to return
    /// (list, keyed etc.).
    /// </summary>
    public class MappingData
    {
        #region Private Fields
        private readonly Type classType;
        private Type collectionType;
        private string keyProperty;
        private string unmappedColumnsProperty;
        private MappingDelegate unmappedPropertyDelegate;

        private readonly Dictionary<string, ColumnMap> columnMapCollection =
            new Dictionary<string, ColumnMap>(StringComparer.InvariantCultureIgnoreCase);
        #endregion

        #region Constructor

        /// <summary>
        /// Initializes a new instance of the <see cref="MappingData"/> class.
        /// Sets the collection type to a List.
        /// </summary>
        /// <param name="classType">Type of the class.</param>
        public MappingData(Type classType)
        {
            this.classType = classType;
            collectionType = typeof(List<>);
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="MappingData"/> class.
        /// </summary>
        /// <param name="classType">Type of the class.</param>
        /// <param name="collectionType">Type of the collection.</param>
        /// <param name="keyProperty">The key property.</param>
        public MappingData(Type classType, Type collectionType, string keyProperty)
        {
            this.classType = classType;
            this.collectionType = collectionType;
            this.keyProperty = keyProperty;
        }

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets the type of the class to load data into.
        /// </summary>
        /// <value>The type of the class.</value>
        public Type ClassType
        {
            get { return classType; }
        }

        /// <summary>
        /// Gets or sets the type of the collection.
        /// </summary>
        /// <value>The type of the collection.</value>
        public Type CollectionType
        {
            get { return collectionType; }
            set { collectionType = value; }
        }

        /// <summary>
        /// Gets a value indicating whether this instance is dictionary collection.
        /// </summary>
        /// <value>
        /// 	<c>true</c> if this instance is dictionary collection; otherwise, <c>false</c>.
        /// </value>
        public bool IsDictionaryCollection
        {
            get
            {
                return !String.IsNullOrEmpty(keyProperty) &&
                       collectionType.GetInterface("IDictionary") != null;
            }
        }

        /// <summary>
        /// Gets or sets the key property.
        /// </summary>
        /// <value>The key property.</value>
        public string KeyProperty
        {
            get { return keyProperty; }
            set { keyProperty = value; }
        }

        /// <summary>
        /// Gets the property count.
        /// </summary>
        /// <value>The property count.</value>
        public int PropertyCount
        {
            get { return columnMapCollection.Count; }
        }

        /// <summary>
        /// Gets or sets the unmapped columns property.
        /// </summary>
        /// <value>The unmapped columns property.</value>
        public string UnmappedColumnsProperty
        {
            get { return unmappedColumnsProperty; }
            set { unmappedColumnsProperty = value; }
        }

        /// <summary>
        /// Gets the unmapped property delegate.
        /// </summary>
        /// <value>The unmapped property delegate.</value>
        public MappingDelegate UnmappedPropertyDelegate
        {
            get
            {
                if (unmappedPropertyDelegate == null)
                {
                    unmappedPropertyDelegate = GetUnmappedPropertiesDelegate();
                }

                return unmappedPropertyDelegate;
            }
        }

        #endregion

        #region Public Methods
        /// <summary>
        /// Adds the column to the meta data. Existing columns are replaced.
        /// </summary>
        /// <param name="columnMap">The column map.</param>
        public void AddColumnMap(ColumnMap columnMap)
        {
            if (!columnMapCollection.ContainsKey(columnMap.ColumnName))
            {
                columnMapCollection.Add(columnMap.ColumnName, columnMap);
            }
            else
            {
                columnMapCollection[columnMap.ColumnName] = columnMap;
            }
        }

        /// <summary>
        /// Gets the <see cref="ColumnMap"/> data for the given <paramref name="columnName"/>.
        /// </summary>
        /// <param name="columnName">Name of the column.</param>
        /// <returns>A <see cref="ColumnMap"/> if found, otherwise <c>null</c>.</returns>
        public ColumnMap GetColumnMap(string columnName)
        {
            return columnMapCollection.ContainsKey(columnName)
                        ? columnMapCollection[columnName]
                        : null;
        }
        #endregion

        #region Private Methods
        /// <summary>
        /// Gets the unmapped properties delegate.
        /// </summary>
        /// <returns>A <see cref="MappingDelegate"/> based on the configuration</returns>
        private MappingDelegate GetUnmappedPropertiesDelegate()
        {
            if (!String.IsNullOrEmpty(unmappedColumnsProperty))
            {
                PropertyInfo info = classType.GetProperty(unmappedColumnsProperty);

                if (info != null && info.CanRead &&
                    info.PropertyType.GetInterface("IDictionary") != null)
                {
                    return new UnmappedPropertyMapper(null, info);
                }
            }

            return new EmptyMappingDelegate();
        }
        #endregion
    }

}
