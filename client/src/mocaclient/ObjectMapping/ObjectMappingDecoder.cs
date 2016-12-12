using System;
using System.Data;
using System.IO;
using System.Security.Cryptography;
using RedPrairie.MOCA.Client.Encoding;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client.ObjectMapping
{
    /// <summary>
    /// A results decoder that maps the output decoding to 
    /// an object collection instead of a <see cref="DataSet"/>.
    /// </summary>
    public class ObjectMappingDecoder: DirectConnectionResultsDecoder
    {
        #region Private Fields
        private readonly ObjectMapper _objectMapper;
        #endregion

        #region Constructor
        /// <summary>
        /// Initializes a new instance of the ObjectMappingDecoder class.
        /// </summary>
        /// <param name="dataStream">The data stream.</param>
        /// <param name="encryptionStrategy">The encryption strategy.</param>
        /// <param name="mappingData">The mapping data used to decode the columns.</param>
        /// <param name="resolver">The resolver used to create objects.</param>
        public ObjectMappingDecoder(Stream dataStream, SymmetricAlgorithm encryptionStrategy,
                                    MappingData mappingData, IObjectResolver resolver)
            : base(dataStream, encryptionStrategy)
        {
            _objectMapper = new ObjectMapper(mappingData, resolver);
        }
        #endregion

        #region Public Methods
        /// <summary>
        /// Decodes the binary data to a <see cref="DataTable"/> object
        /// </summary>
        /// <returns>A new <see cref="DataSet"/> object or null if failed</returns>
        /// <exception cref="MocaException">Thrown if the status is not successful.</exception>
        public virtual object DecodeCollection()
        {
            HeaderData headerData = ParseHeader(ParseVersion());

            //This is a second check since the column parsing could fail
            if (headerData.ColumnCount != 0 && headerData.Columns.Count != 0)
            {
                //Check for duplicates
                headerData.CheckDuplicateColumns();
            }

            //Throw Error if not an ok result, in this case do it before
            //the decode as an exception cannot contain the collection
            if (!headerData.Status.Equals(MocaErrors.eOK))
            {
                throw new MocaException(headerData.Status, headerData.Message, GetDataTable(headerData));
            }

            //Next read the row data and begin converting it to obejcts
            return GetCollectionData(headerData);
        }
        #endregion

        #region Private Methods
        
        /// <summary>
        /// Gets the collection data.
        /// </summary>
        /// <param name="headerData">The header data.</param>
        /// <returns>A collection containing the results</returns>
        private object GetCollectionData(HeaderData headerData)
        {
            //Create the type converting delegates based on the column data
            var columnMaps = _objectMapper.CreateMappingDelegates(headerData);

            var collection = _objectMapper.CreateCollectionObject();
            

            for (int r = 0; r < headerData.RowCount; r++)
            {
                Stream baseStream = dataStream;
                dataStream = GetRowStream(headerData, dataStream);

                //Create the object
                object item = _objectMapper.CreateObject();
                    
                if (item != null)
                {
                    foreach (MappingDelegate map in columnMaps)
                    {
                        //Get the data
                        var typeCode = (char)dataStream.ReadByte();
                        MocaType type = MocaType.Lookup(typeCode);
                        string tmp = NextField();
                        int dataLength;
                        Int32.TryParse(tmp, out dataLength);

                        if (dataLength == 0)
                        {
                            map.SetNull(item);
                        }
                        else if (type.Equals(MocaType.BINARY))
                        {
                            if (dataLength < 8)
                            {
                                map.SetNull(item); 
                            }
                            else
                            {
                                byte[] readData = ReadBytes(dataLength);
                                var data = new byte[readData.Length - 8];

                                // The first 8 bytes are encoding the length of the data.
                                // We've already got that, so there's really no need to
                                // interpret it.
                                Array.Copy(readData, 8, data, 0, readData.Length - 8);

                                map.SetValue(item, data);
                            }
                        }
                        else if (type.Equals(MocaType.RESULTS))
                        {
                            ReadBytes(dataLength);
                            //Not supported at this point
                        }
                        else
                        {
                            map.SetValue(item, type, ReadBytesAsString(dataLength));
                        }
                    }


                    _objectMapper.AddObjectToCollection(collection, item);
                }

                dataStream = baseStream;
            }


            return collection;
        }

        #endregion
    }
}
