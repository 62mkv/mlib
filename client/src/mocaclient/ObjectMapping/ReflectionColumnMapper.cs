using System;
using System.Collections.Generic;
using System.Reflection;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;

namespace RedPrairie.MOCA.Client.ObjectMapping
{
    /// <summary>
    /// The default column mapper that simply reflects the columns
    /// and assumes all public properties are available
    /// </summary>
    public class ReflectionColumnMapper
    {
        #region Private Fields
        private readonly Dictionary<Type, MappingData> cache = new Dictionary<Type, MappingData>(10);
        #endregion

        #region Public Methods
        /// <summary>
        /// Gets the data from the system, first from an internal cache.
        /// </summary>
        /// <param name="type">The type of the collection.</param>
        /// <returns>A <see cref="MappingData"/> class.</returns>
        public MappingData GetData(Type type)
        {
            if (CacheContainsType(type))
            {
                MappingData data = DetermineCollectionType(type);
                AddMappingToCache(type, data);
            }

            return GetItemFromCache(type);
        }

        /// <summary>
        /// Gets the mapping information using reflection.
        /// </summary>
        /// <param name="type">The obejct type to map to.</param>
        /// <returns>A <see cref="MappingData"/> class.</returns>
        public MappingData GetMappingInformation(Type type)
        {
            MappingData data = new MappingData(type);

            GetPropertyData(type, data);

            return data;
        }

        /// <summary>
        /// Gets the mapping information using reflection.
        /// </summary>
        /// <param name="type">The obejct type to map to.</param>
        /// <param name="collectionType">The type of the collection.</param>
        /// <param name="keyProperty">The key property if it is a dictionary class.</param>
        /// <returns>A <see cref="MappingData"/> class.</returns>
        public MappingData GetMappingInformation(Type type, Type collectionType, string keyProperty)
        {
            MappingData data = new MappingData(type, collectionType, keyProperty);

            GetPropertyData(type, data);

            return data;
        }

        #endregion

        #region Protected Methods
        /// <summary>
        /// Adds the mapping to the cache.
        /// </summary>
        /// <param name="type">The type of the mapping.</param>
        /// <param name="data">The mapping data object.</param>
        protected virtual void AddMappingToCache(Type type, MappingData data)
        {
            //If the cache is above the max size, clean out an item
            if (cache.Count >= 10)
            {
                cache.Remove(cache.Keys.GetEnumerator().Current);
            }
            
            cache.Add(type, data);
        }

        /// <summary>
        /// Determines if the cache contains the given type mapping
        /// </summary>
        /// <param name="type">The type mapping to get.</param>
        /// <returns><c>true</c> if the cache contains the type; otherwise <c>false</c>.</returns>
        protected virtual bool CacheContainsType(Type type)
        {
            return !cache.ContainsKey(type);
        }

        /// <summary>
        /// Creates a new column map based on the property information.
        /// </summary>
        /// <param name="info">The property information.</param>
        /// <param name="mappingData">The mapping data so attributes can be added if necessary.</param>
        /// <returns></returns>
        protected virtual ColumnMap CreateColumnMap(PropertyInfo info, MappingData mappingData)
        {
            if (info.CanWrite)
            {
                ColumnMap columnMap = new ColumnMap(info.Name, info.PropertyType, info.Name);
                columnMap.PropertyInfo = info;
                return columnMap;
            }

            return null;
        }

        /// <summary>
        /// Determines the type of the collection. Then creates the <see cref="MappingData"/>
        /// collection.
        /// </summary>
        /// <param name="type">The object and potentially collection type.</param>
        /// <returns>A <see cref="MappingData"/> class.</returns>
        protected MappingData DetermineCollectionType(Type type)
        {
            MappingData data;
            if (type.GetInterface("IDictionary") != null)
            {
                Type[] args = type.GetGenericArguments();
                
                if (args == null || args.Length != 2)
                {
                    throw new ArgumentException(string.Format(
                            "The Dictionary type defined has an invalid number of generic arguments Type:{0}",
                            type));
                }

                data = GetMappingInformation(args[1], type, GetKeyProperty(args[1]));
            }
            else if (type.GetInterface("IList") != null)
            {
                Type[] args = type.GetGenericArguments();

                if (args == null || args.Length != 1)
                {
                    throw new ArgumentException(string.Format(
                            "The List type defined has an invalid number of generic arguments Type:{0}",
                            type));
                }

                data = GetMappingInformation(args[0], type, null);
            }
            else
            {
                data = GetMappingInformation(type);
            }

            return data;
        }

        /// <summary>
        /// Gets the item from the cache.
        /// </summary>
        /// <param name="type">The type key of the mapping.</param>
        /// <returns>A <see cref="MappingData"/> object</returns>
        protected virtual MappingData GetItemFromCache(Type type)
        {
            return cache[type];
        }

        /// <summary>
        /// Gets the key property for the dictionary.
        /// </summary>
        /// <param name="classType">The type of the class.</param>
        /// <returns>A key property string</returns>
        protected virtual string GetKeyProperty(Type classType)
        {
            return "Key";
        }

        /// <summary>
        /// Gets the property data.
        /// </summary>
        /// <param name="type">The data type of the object class.</param>
        /// <param name="mappingData">The mapping data object.</param>
        protected virtual void GetPropertyData(Type type, MappingData mappingData)
        {
            PropertyInfo[] propertyInfo = type.GetProperties();

            foreach (PropertyInfo info in propertyInfo)
            {
                ColumnMap map = CreateColumnMap(info, mappingData);

                if (map == null) continue;

                if (map.ColumnMapper == null)
                {
                    map.ColumnMapper = GetMapper(map);
                }

                mappingData.AddColumnMap(map);
            }
        }

        #endregion

        #region Static Methods
        /// <summary>
        /// Gets the correct column mapper based on the Moca Type.
        /// </summary>
        /// <param name="map">The column map.</param>
        /// <returns>A <see cref="MappingDelegate"/> class.</returns>
        public static MappingDelegate GetMapper(ColumnMap map)
        {
            if (map.DataType.IsEnum)
            {
                return new EnumMappingDelegate(map);
            }

            MocaType type = MocaType.LookupClass(map.DataType);

            if (type.Equals(MocaType.STRING) || type.Equals(MocaType.STRING_REF))
            {
                return new StringMappingDelegate(map);
            }
            if (type.Equals(MocaType.INTEGER) || type.Equals(MocaType.INTEGER_REF))
            {
                return new IntegerMappingDelegate(map);
            }
            if (type.Equals(MocaType.DOUBLE) || type.Equals(MocaType.DOUBLE_REF))
            {
                return new DoubleMappingDelegate(map);
            }
            if (type.Equals(MocaType.BOOLEAN))
            {
                return new BooleanMappingDelegate(map);
            }
            if (type.Equals(MocaType.DATETIME))
            {
                return new DateMappingDelegate(map);
            }
            if (type.Equals(MocaType.BINARY))
            {
                return new BinaryMappingDelegate(map);
            }

            return new StringMappingDelegate(map);
        }
        #endregion
    }
}
