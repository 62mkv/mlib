
namespace RedPrairie.MOCA.Client.Encoding.Xml
{
    /// <summary>
    /// A result data class used for processing.
    /// </summary>
    /// <typeparam name="TPayload">The type of the payload.</typeparam>
    public class ResultItem<TPayload> where TPayload: class, new()
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="ResultItem&lt;TPayload&gt;"/> class.
        /// </summary>
        public ResultItem()
        {
            HeaderData = new HeaderData();
            Payload = new TPayload();
            CurrentColumn = 0;
        }

        /// <summary>
        /// Gets or sets the current column index.
        /// </summary>
        /// <value>The current column.</value>
        public int CurrentColumn { get; set; }

        /// <summary>
        /// Gets or sets the data array that can be used to store current row values.
        /// </summary>
        /// <value>The data array.</value>
        public object[] DataArray { get; set; }

        /// <summary>
        /// Gets or sets the header data.
        /// </summary>
        /// <value>The header data.</value>
        public HeaderData HeaderData { get; private set; }

        /// <summary>
        /// Gets or sets the payload.
        /// </summary>
        /// <value>The payload.</value>
        public TPayload Payload { get; set; }
       
    }
}