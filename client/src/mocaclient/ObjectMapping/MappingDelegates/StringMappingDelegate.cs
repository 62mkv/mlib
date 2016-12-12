using System;
using RedPrairie.MOCA.Client.Encoding;

namespace RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates
{
    /// <summary>
    /// A string mapping delegate class
    /// </summary>
    public class StringMappingDelegate : MappingDelegate
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="StringMappingDelegate"/> class.
        /// </summary>
        /// <param name="map">The property info for the mapper.</param>
        public StringMappingDelegate(ColumnMap map):base(map)
        {
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
            if (type.Class.IsAssignableFrom(typeof(DateTime)))
            {
                DateTime dateTime;
                EncodingBase.TryParseDateTime(data, out dateTime);
                SetPropertyValue(target, dateTime.ToString());
            }
            else
            {
                SetPropertyValue(target, data);
            }
        }
    }
}