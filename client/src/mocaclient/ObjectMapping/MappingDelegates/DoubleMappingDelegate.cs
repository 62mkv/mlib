using System;
using System.Globalization;
using RedPrairie.MOCA.Client.Encoding;

namespace RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates
{
    /// <summary>
    /// A double mapping delegate class
    /// </summary>
    public class DoubleMappingDelegate : MappingDelegate
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="StringMappingDelegate"/> class.
        /// </summary>
        /// <param name="map">The property info for the mapper.</param>
        public DoubleMappingDelegate(ColumnMap map)
            : base(map)
        {
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
            double value;
            if (Double.TryParse(data, NumberStyles.Any, 
                EncodingBase.GlobalNumberFormatInfo, out value))
            {
                SetPropertyValue(target, value);
            }
            else
            {
                SetNull(target);
            }
        }
    }
}