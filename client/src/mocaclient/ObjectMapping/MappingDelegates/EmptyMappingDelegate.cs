
namespace RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates
{
    /// <summary>
    /// An empty mapping delgate class that doesn't contain any setting code
    /// </summary>
    internal class EmptyMappingDelegate : MappingDelegate
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="EmptyMappingDelegate"/> class.
        /// </summary>
        public EmptyMappingDelegate() : base(null)
        {
        }

        /// <summary>
        /// Clones the specified column map.
        /// </summary>
        /// <param name="columnMap">The column map.</param>
        /// <returns></returns>
        public override MappingDelegate Clone(ColumnMap columnMap)
        {
            return new EmptyMappingDelegate();
        }

        /// <summary>
        /// Sets the defined value as null based on the <paramref name="target"/> object.
        /// </summary>
        /// <param name="target">The target object to set the value into.</param>
        public override void SetNull(object target)
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
        }

        /// <summary>
        /// Sets the (<paramref name="data"/>) in the <paramref name="target"/> object.
        /// This overload is only used for binary and result data types.
        /// </summary>
        /// <param name="target">The target object to set the value into.</param>
        /// <param name="data">The data in a binary array form.</param>
        public override void SetValue(object target, byte[] data)
        {
        }
    }
}