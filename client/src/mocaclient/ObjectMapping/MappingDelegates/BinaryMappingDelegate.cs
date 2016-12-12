using RedPrairie.MOCA.Client.Encoding;

namespace RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates
{
    /// <summary>
    /// A binary data type mapper
    /// </summary>
    public class BinaryMappingDelegate : MappingDelegate
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="BinaryMappingDelegate"/> class.
        /// </summary>
        /// <param name="map">The property info for the mapper.</param>
        public BinaryMappingDelegate(ColumnMap map):base(map)
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
            byte[] value = EncodingBase.GetEncoding().GetBytes(data);
            SetPropertyValue(target, value);
        }

        /// <summary>
        /// Sets the (<paramref name="data"/>) in the <paramref name="target"/> object.
        /// This overload is only used for binary and result data types.
        /// </summary>
        /// <param name="target">The target object to set the value into.</param>
        /// <param name="data">The data in a binary array form.</param>
        public override void SetValue(object target, byte[] data)
        {
            SetPropertyValue(target, data);
        }
    }
}