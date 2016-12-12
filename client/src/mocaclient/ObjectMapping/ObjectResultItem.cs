using System.Collections.Generic;
using RedPrairie.MOCA.Client.Encoding.Xml;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;

namespace RedPrairie.MOCA.Client.ObjectMapping
{
    /// <summary>
    /// A <see cref="ResultItem{TPayload}"/> class
    /// that is used for object mapping.
    /// </summary>
    public class ObjectResultItem : ResultItem<object>
    {
        /// <summary>
        /// Gets or sets the current object.
        /// </summary>
        /// <value>The current object.</value>
        public object CurrentObject { get; set; }

        /// <summary>
        /// Gets or sets the mapping data.
        /// </summary>
        /// <value>The mapping data.</value>
        public List<MappingDelegate> MappingData { get; set; }
    }
}