using System;
using System.Collections;
using System.Collections.Generic;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;

namespace RedPrairie.MOCA.Client.ObjectMapping
{
    /// <summary>
    /// An object mapping class that takes in the various values and maps them accordingly.
    /// This is abstracted from the various decoders to help web and TCP parsing.
    /// </summary>
    public class ObjectMapper
    {
        #region Private Fields
        private readonly MappingData _mappingData;
        private readonly IObjectResolver _resolver;
        #endregion

        #region Constructor
        /// <summary>
        /// Initializes a new instance of the <see cref="ObjectMapper"/> class.
        /// </summary>
        /// <param name="mappingData">The mapping data used to decode the columns.</param>
        /// <param name="resolver">The resolver used to create objects.</param>
        /// <exception cref="ArgumentNullException">
        /// Thrown if <paramref name="resolver"/> is <c>null</c>.
        /// </exception>
        public ObjectMapper(MappingData mappingData, IObjectResolver resolver)
        {
            _mappingData = mappingData;

            if (resolver != null)
            {
                _resolver = resolver;
            }
            else
            {
                throw new ArgumentNullException("resolver");
            }
        }
        #endregion

        /// <summary>
        /// Adds the object to the collection.
        /// </summary>
        /// <param name="collection">The collection.</param>
        /// <param name="item">The item.</param>
        public void AddObjectToCollection(object collection, object item)
        {
            if (_mappingData.IsDictionaryCollection)
            {
                SetDictionaryValue(collection, item);
            }
            else
            {
                ((IList)collection).Add(item);
            }
        }

        /// <summary>
        /// Creates the appropriate collection object.
        /// </summary>
        /// <returns>A new collection object if successful</returns>
        /// <exception cref="ArgumentException">Key Property for mapping class is invalid.</exception>
        /// <exception cref="NullReferenceException">Thrown if the collection cannot be created.</exception>
        public object CreateCollectionObject()
        {
            if (_mappingData.CollectionType.ContainsGenericParameters)
            {
                var genericArgCount = _mappingData.CollectionType.GetGenericArguments().Length;
                if (genericArgCount == 1)
                {
                    _mappingData.CollectionType =
                        _mappingData.CollectionType.MakeGenericType(_mappingData.ClassType);
                }
                else if (genericArgCount == 2 && !String.IsNullOrEmpty(_mappingData.KeyProperty))
                {
                    var info = _mappingData.ClassType.GetProperty(_mappingData.KeyProperty);

                    if (info != null)
                    {
                        _mappingData.CollectionType =
                            _mappingData.CollectionType.MakeGenericType(info.PropertyType, _mappingData.ClassType);
                    }
                    else
                    {
                        throw new ArgumentException("Key Property for mapping class is invalid");
                    }
                }
                else
                {
                    throw new ArgumentException("Defined type for collection cannot be mapped");
                }
            }


            var collection = _resolver.Resolve(_mappingData.CollectionType);

            if (collection == null)
            {
                throw new NullReferenceException(
                    string.Format("Collection of type {0} could not be created",
                                  _mappingData.CollectionType));
            }

            return collection;
        }

        /// <summary>
        /// Creates the mapping delegates.
        /// </summary>
        /// <param name="headerData">The header data.</param>
        public List<MappingDelegate> CreateMappingDelegates(HeaderData headerData)
        {
            var mappingDelegates = new List<MappingDelegate>();
            foreach (var column in headerData.Columns)
            {
                MappingDelegate mapDelegate = null;
                ColumnMap map = _mappingData.GetColumnMap(column.ColumnName);

                if (map != null && map.ColumnMapper != null)
                {
                    mapDelegate = map.ColumnMapper;
                }

                //Can't find it set it to the default mapper
                if (mapDelegate == null)
                {
                    if (map == null)
                    {
                        map = new ColumnMap(column.ColumnName, column.DataType, column.ColumnName);
                    }

                    mapDelegate = _mappingData.UnmappedPropertyDelegate.Clone(map);
                }

                mappingDelegates.Add(mapDelegate);
            }

            return mappingDelegates;
        }

        /// <summary>
        /// Creates the object to hold the data.
        /// </summary>
        /// <returns>A new object.</returns>
        public object CreateObject()
        {
            return _resolver.Resolve(_mappingData.ClassType);
        }

        /// <summary>
        /// Sets the dictionary value into the dictionary.
        /// </summary>
        /// <param name="collection">The collection.</param>
        /// <param name="item">The item.</param>
        private void SetDictionaryValue(object collection, object item)
        {
            var dictionary = collection as IDictionary;
            var key =
                _mappingData.GetColumnMap(_mappingData.KeyProperty).PropertyInfo.GetValue(item, null);

            if (dictionary != null && key != null)
            {
                if (dictionary.Contains(key))
                {
                    dictionary[key] = item;
                }
                else
                {
                    dictionary.Add(key, item);
                }
            }
        }
    }
}