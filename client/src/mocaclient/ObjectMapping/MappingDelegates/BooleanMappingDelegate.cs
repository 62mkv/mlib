namespace RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates
{
    /// <summary>
    /// A boolean mapping delegate class
    /// </summary>
    public class BooleanMappingDelegate : MappingDelegate
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="StringMappingDelegate"/> class.
        /// </summary>
        /// <param name="map">The property info for the mapper.</param>
        public BooleanMappingDelegate(ColumnMap map)
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
            if (type.Equals(MocaType.STRING))
            {
                data = data.ToLowerInvariant().Trim();
                bool value = (!data.Equals("0") && !data.Equals("false") && !data.Equals("f"));
                SetPropertyValue(target, value);
            }
            else
            {
                bool value = (!data.Equals("0"));
                SetPropertyValue(target, value);
            }
        }
    }
}