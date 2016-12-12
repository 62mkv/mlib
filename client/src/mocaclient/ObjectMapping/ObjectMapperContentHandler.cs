using System.Data;
using System.Web;
using RedPrairie.MOCA.Client.Encoding.Xml;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client.ObjectMapping
{
    /// <summary>
    /// A <see cref="IContentHandler"/> class that uses object mapping
    /// to create a command result.
    /// </summary>
    internal class ObjectMapperContentHandler: ContentHandlerBase<ObjectResultItem, object>
    {
        private readonly ObjectMapper _objectMapper;

        /// <summary>
        /// Initializes a new instance of the <see cref="ObjectMapperContentHandler"/> class.
        /// </summary>
        /// <param name="command">The command.</param>
        /// <param name="mappingData">The mapping data.</param>
        /// <param name="resolver">The resolver.</param>
        public ObjectMapperContentHandler(string command, MappingData mappingData, IObjectResolver resolver) 
            :base(command)
        {
            _objectMapper = new ObjectMapper(mappingData, resolver);
        }

        /// <summary>
        /// Gets the results of the parsing.
        /// </summary>
        /// <returns>An object of the results.</returns>
        /// <exception cref="MocaException">Thrown if an error occurs or is the result of the command.</exception>
        public override object GetResults()
        {
            var item = GetCurrentItem();

            //Throw Error if not an ok result
            if (!item.HeaderData.Status.Equals(MocaErrors.eOK))
            {
                var table = new DataTable(Command);
                table.Columns.AddRange(item.HeaderData.Columns.ToArray());
                
                throw new MocaException(item.HeaderData.Status, item.HeaderData.Message, table);
            }

            return item.Payload;
        }

        /// <summary>
        /// Called when a new row is started.
        /// </summary>
        /// <param name="currentItem">The current item.</param>
        protected override void OnRowStart(ObjectResultItem currentItem)
        {
            //Create the object
            currentItem.CurrentObject = _objectMapper.CreateObject();
            currentItem.CurrentColumn = 0;
        }

        /// <summary>
        /// Called when a new row is ended.
        /// </summary>
        /// <param name="currentItem">The current item.</param>
        protected override void OnRowEnd(ObjectResultItem currentItem)
        {
            _objectMapper.AddObjectToCollection(currentItem.Payload, currentItem.CurrentObject);
        }

        /// <summary>
        /// Called when a new row collection is started.
        /// </summary>
        /// <param name="currentItem">The current item.</param>
        protected override void OnRowCollectionStart(ObjectResultItem currentItem)
        {
            //Create the type converting delegates based on the column data
            currentItem.MappingData = _objectMapper.CreateMappingDelegates(currentItem.HeaderData);
            currentItem.Payload = _objectMapper.CreateCollectionObject();
        }

        /// <summary>
        /// Called when a field has completed processing.
        /// </summary>
        /// <param name="currentItem">The current item.</param>
        /// <param name="fieldData">The field data.</param>
        protected override void OnFieldEnd(ObjectResultItem currentItem, string fieldData)
        {
            var column = currentItem.CurrentColumn;
            var item = currentItem.CurrentObject;

            //Exit if the column index is invalid
            if (column < 0 || column >= currentItem.MappingData.Count)
            {
                return;
            }

            var map = currentItem.MappingData[column];
            if (string.IsNullOrEmpty(fieldData))
            {
                map.SetNull(item);
                return;
            }

            var type = currentItem.HeaderData.Columns[column].DataType;
            if (type.Equals(typeof(byte[])))
            {
                map.SetValue(item, ToByteArray(HttpUtility.HtmlDecode(fieldData)));
            }

            else
            {
                map.SetValue(item, MocaType.LookupClass(type), fieldData);
            }
        }
    }
}