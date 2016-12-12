using System;
using System.Data;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client.Encoding.Xml
{
    /// <summary>
    /// An inherited version of the <see cref="ContentHandlerBase{TResultItem, TPayload}"/>
    /// that is used to process results into a <see cref="DataSet"/> of results.
    /// </summary>
    internal class DataTableContentHandler: ContentHandlerBase<ResultItem<DataTable>, DataTable>
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="DataTableContentHandler"/> class.
        /// </summary>
        /// <param name="command">The command.</param>
        public DataTableContentHandler(string command) : base(command)
        {
        }

        /// <summary>
        /// Gets the results of the parsing.
        /// </summary>
        /// <returns>An object of the results.</returns>
        /// <exception cref="MocaException">Thrown if an error occurs or is the result of the command.</exception>
        public override object GetResults()
        {
            var item = GetCurrentItem();

            //Set the table name
            item.Payload.TableName = Command;
            
            //Throw Error if not an ok result
            if (!item.HeaderData.Status.Equals(MocaErrors.eOK))
                throw new MocaException(item.HeaderData.Status, 
                                        item.HeaderData.Message, 
                                        item.Payload);

            
            //Disable constraints at the table level by default
            //This is done due to MSAP issues in editing.
            var dataSet = new DataSet { EnforceConstraints = false };
            dataSet.Tables.Add(GetCurrentItem().Payload);

            return dataSet;
        }

        /// <summary>
        /// Called when a sub results set is complete.
        /// </summary>
        /// <param name="parent">The parent item.</param>
        /// <param name="subItem">The sub item.</param>
        protected override void OnSubResultsEnd(ResultItem<DataTable> parent, ResultItem<DataTable> subItem)
        {
            parent.DataArray[parent.CurrentColumn] = subItem.Payload;
        }

        /// <summary>
        /// Called when a new row is started.
        /// </summary>
        protected override void OnRowStart(ResultItem<DataTable> currentItem)
        {
            currentItem.CurrentColumn = 0;
            currentItem.DataArray = new object[currentItem.HeaderData.ColumnCount];
        }

        /// <summary>
        /// Called when a new row is ended.
        /// </summary>
        protected override void OnRowEnd(ResultItem<DataTable> currentItem)
        {
            currentItem.Payload.Rows.Add(currentItem.DataArray);
        }

        /// <summary>
        /// Called when a new row collection is started.
        /// </summary>
        protected override void OnRowCollectionStart(ResultItem<DataTable> currentItem)
        {
            currentItem.Payload.Columns.AddRange(currentItem.HeaderData.Columns.ToArray());
            currentItem.HeaderData.ColumnCount = currentItem.Payload.Columns.Count;

            currentItem.Payload.BeginLoadData();
        }

        /// <summary>
        /// Called when a new row collection is ended.
        /// </summary>
        protected override void OnRowCollectionEnd(ResultItem<DataTable> currentItem)
        {
            currentItem.Payload.EndLoadData();
            currentItem.Payload.AcceptChanges();
        }

        /// <summary>
        /// Called when a field has completed processing.
        /// </summary>
        /// <param name="currentItem">The current item.</param>
        /// <param name="fieldData">The field data.</param>
        /// <exception cref="ProtocolException"><c>ProtocolException</c>.</exception>
        protected override void OnFieldEnd(ResultItem<DataTable> currentItem, string fieldData)
        {
            var dataArray = currentItem.DataArray;
            var c = currentItem.CurrentColumn;
            var type = MocaType.LookupClass(currentItem.HeaderData.Columns[c].DataType);

            if (type.Equals(MocaType.BINARY))
            {
                dataArray[c] = Convert.FromBase64String(fieldData);
                return;
            }

            if (fieldData.Length == 0)
            {
                dataArray[c] = null;
            }
            else
            {
                if (type.Equals(MocaType.STRING))
                {
                    dataArray[c] = fieldData;
                }
                else if (type.Equals(MocaType.INTEGER))
                {
                    try
                    {
                        dataArray[c] = Int32.Parse(fieldData, GlobalNumberFormatInfo);
                    }
                    catch (FormatException e)
                    {
                        throw new ProtocolException("error parsing integer: " + fieldData, e);
                    }
                }
                else if (type.Equals(MocaType.DOUBLE))
                {
                    try
                    {
                        dataArray[c] = Double.Parse(fieldData, GlobalNumberFormatInfo);
                    }
                    catch (FormatException e)
                    {
                        throw new ProtocolException("error parsing double: " + fieldData, e);
                    }
                }
                else if (type.Equals(MocaType.BOOLEAN))
                {
                    dataArray[c] = (!fieldData.Equals("0"));
                }
                else if (type.Equals(MocaType.DATETIME))
                {
                    dataArray[c] = ConvertDateTime(fieldData);
                }
                else
                {
                    //Just put it in as a string;
                    dataArray[c] = fieldData;
                }
            }
        }
    }
}