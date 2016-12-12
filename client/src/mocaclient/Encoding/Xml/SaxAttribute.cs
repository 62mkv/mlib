namespace RedPrairie.MOCA.Client.Encoding.Xml
{
    /// <summary>
    /// Represents a Sax attribute
    /// </summary>
    internal class SaxAttribute
    {
        /// <summary>
        /// Gets or sets the attribute name
        /// </summary>
        public string Name { get; set; }

        /// <summary>
        /// Gets or sets the attribute namespace
        /// </summary>
        public string NamespaceURI { get; set; }
        /// <summary>
        /// Gets or sets the attribute value
        /// </summary>
        /// <value>The value.</value>
        public string Value { get; set; }
    }
}