using System;
using System.Reflection;
using System.Collections.Generic;
using RedPrairie.MOCA.Client.Encoding;

namespace RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates
{
    /// <summary>
    /// A class for mapping unknown properties into the specified target collection
    /// </summary>
    public class UnmappedPropertyMapper:MappingDelegate
    {
        #region Private Fields
        private readonly PropertyInfo info; 
        #endregion

        #region Constructor
        /// <summary>
        /// Initializes a new instance of the <see cref="UnmappedPropertyMapper"/> class.
        /// </summary>
        /// <param name="map">The column map.</param>
        /// <param name="info">The method info for the dictionary.</param>
        public UnmappedPropertyMapper(ColumnMap map, PropertyInfo info)
            : base(map)
        {
            this.info = info;
        } 
        #endregion

        #region Public Methods

        /// <summary>
        /// Clones the specified column map.
        /// </summary>
        /// <param name="columnMap">The column map.</param>
        /// <returns></returns>
        public override MappingDelegate Clone(ColumnMap columnMap)
        {
            return new UnmappedPropertyMapper(columnMap, info);
        }

        /// <summary>
        /// Sets the defined value as null based on the <paramref name="target"/> object.
        /// </summary>
        /// <param name="target">The target object to set the value into.</param>
        public override void SetNull(object target)
        {
            SetDictionaryValue(target, null);
        }

        /// <summary>
        /// Sets the value (<paramref name="data"/>) in the <paramref name="target"/> object.
        /// </summary>
        /// <param name="target">The target object to set the value into.</param>
        /// <param name="type">The type of the incoming data.</param>
        /// <param name="data">The data to convert it into.</param>
        public override void SetValue(object target, MocaType type, string data)
        {
            //If we have a date/time let's get it to .NET if we can
            if (type.Equals(MocaType.DATETIME))
            {
                DateTime dateTime;
                EncodingBase.TryParseDateTime(data, out dateTime);
                SetDictionaryValue(target, dateTime);
            }
            else
            {
                SetDictionaryValue(target, data);
            }
        }

        /// <summary>
        /// Sets the (<paramref name="data"/>) in the <paramref name="target"/> object.
        /// This overload is only used for binary and result data types.
        /// </summary>
        /// <param name="target">The target object to set the value into.</param>
        /// <param name="data">The data in a binary array form.</param>
        public override void SetValue(object target, byte[] data)
        {
            SetDictionaryValue(target, data);
        } 
        #endregion

        #region Private Methods
        /// <summary>
        /// Gets the dictionary from the target value.
        /// </summary>
        /// <returns>A dictionary or null if it failed</returns>
        private IDictionary<string, object> GetDictionary(object target)
        {
            try
            {
                return info.GetValue(target, null) as IDictionary<string, object>;
            }
            catch (SystemException)
            {
            }

            return null;
        }

        /// <summary>
        /// Sets the specified value into the dictionary.
        /// </summary>
        /// <param name="target">The target.</param>
        /// <param name="value">The value.</param>
        private void SetDictionaryValue(object target, object value)
        {
            IDictionary<string, object> dictionary = GetDictionary(target);

            if (dictionary != null && map != null)
            {
                if (dictionary.ContainsKey(map.ColumnName))
                {
                    dictionary[map.ColumnName] = value;
                }
                else
                {
                    dictionary.Add(map.ColumnName, value);
                }
            }
            //Log failure
        } 
        #endregion
        
    }
}
