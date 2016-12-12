using System;
using System.Collections.Generic;
using System.Diagnostics;

namespace RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates
{
    /// <summary>
    /// An enumeraion mapping delegate class
    /// </summary>
    public class EnumMappingDelegate : MappingDelegate
    {
        private readonly Dictionary<string, object> mappedValues;
        
        /// <summary>
        /// Initializes a new instance of the <see cref="StringMappingDelegate"/> class.
        /// </summary>
        /// <param name="map">The property info for the mapper.</param>
        public EnumMappingDelegate(ColumnMap map)
            : base(map)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="MappingDelegate"/> class.
        /// </summary>
        /// <param name="map">The property info for the mapper.</param>
        /// <param name="mappedValues">The mapped values.</param>
        public EnumMappingDelegate(ColumnMap map, Dictionary<string, object> mappedValues) 
            : this(map)
        {
            this.mappedValues = mappedValues;
        }

        /// <summary>
        /// Gets the mapped values.
        /// </summary>
        /// <value>The mapped values.</value>
        public Dictionary<string, object> MappedValues
        {
            [DebuggerStepThrough]
            get { return mappedValues; }
        }

        /// <summary>
        /// Sets the defined value as null based on the <paramref name="target"/> object.
        /// </summary>
        /// <param name="target">The target object to set the value into.</param>
        public override void SetNull(object target)
        {
            SetDefaultValue(target);
        }

        /// <summary>
        /// Sets the value (<paramref name="data"/>) in the <paramref name="target"/> object.
        /// </summary>
        /// <param name="target">The target object to set the value into.</param>
        /// <param name="type">The type of the incoming data.</param>
        /// <param name="data">The data to convert it into.</param>
        public override void SetValue(object target, MocaType type, string data)
        {
            if (mappedValues != null && mappedValues.ContainsKey(data))
            {
                SetPropertyValue(target, mappedValues[data]);
                return;
            }

            try
            {
                data = data.Trim().Replace(" ", "");
                object value = Enum.Parse(map.DataType, data, true);

                if (value != null && Enum.IsDefined(map.DataType, value))
                {
                    SetPropertyValue(target, value);
                    return;
                }
            }
            catch (ArgumentException)
            {
            }

            SetNull(target);
        }
    }
}