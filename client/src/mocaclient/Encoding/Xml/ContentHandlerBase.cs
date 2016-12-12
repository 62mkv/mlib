using System;
using System.Collections.Generic;
using System.Data;
using System.Text;
using RedPrairie.MOCA.Exceptions;

namespace RedPrairie.MOCA.Client.Encoding.Xml
{
    /// <summary>
    /// A content handler implementation for a MOCA results set.
    /// </summary>
    internal abstract class ContentHandlerBase<TResultItem, TPayload> : EncodingBase, IContentHandler 
                 where TPayload : class, new()
                 where TResultItem: ResultItem<TPayload>, new()
    {
        private const string COLUMN_ELEMENT = "column";
        private const string DATA_ELEMENT = "data";
        private const string FIELD_ELEMENT = "field";
        private const string LENGTH_ATTRIBUTE = "length";
        private const string MESSAGE_ELEMENT = "message";
        private const string MOCA_RESULTS_ELEMENT = "moca-results";
        private const string NAME_ATTRIBUTE = "name";
        //private const string NULLABLE_ATTRIBUTE = "nullable";
        private const string ROW_ELEMENT = "row";
        private const string SESSION_ID_ELEMENT = "session-id";
        private const string STATUS_ELEMENT = "status";
        private const string TYPE_ATTRIBUTE = "type";

        /// <summary>
        /// An enumeration for keeping track of the parser state.
        /// </summary>
        private enum ParserState
        {
            /// <summary>
            /// The states of the parser
            /// </summary>
            NONE, STATUS, MESSAGE, SESSIONID, FIELD
        }

        private TResultItem _currentItem = new TResultItem();
        private StringBuilder _fieldData;
        private Stack<TResultItem> _stack;
        private ParserState _state;

        /// <summary>
        /// Initializes a new instance of the <see cref="ContentHandlerBase{TResultItem, TPayload}"/> class.
        /// </summary>
        /// <param name="command">The command.</param>
        protected ContentHandlerBase(string command)
        {
            Command = command;
        }

        #region Implementation of IContentHandler

        /// <summary>
        /// Gets the command.
        /// </summary>
        /// <value>The command.</value>
        public string Command { get; private set; }

        /// <summary>
        /// Gets the session id.
        /// </summary>
        /// <value>The session id.</value>
        public string SessionId { get; private set; }

        #endregion

        /// <summary>
        /// A method on the handler that is called when document processing starts.
        /// </summary>
        public virtual void StartDocument()
        {
            _stack = new Stack<TResultItem>();
        }

        /// <summary>
        /// A method on the handler that is called when document processing ends.
        /// </summary>
        public virtual void EndDocument()
        {

        }

        /// <summary>
        /// A method on the handler that is called when processing instructions exist.
        /// </summary>
        /// <param name="target">The instruction target.</param>
        /// <param name="data">The instruction data.</param>
        public void ProcessingInstruction(string target, String data)
        {

        }

        /// <summary>
        /// A method on the handler that is called when prefix mapping starts.
        /// </summary>
        /// <param name="prefix">The prefix.</param>
        /// <param name="uri">The URI of the prefix.</param>
        public void StartPrefixMapping(string prefix, String uri)
        {
        }

        /// <summary>
        /// A method on the handler that is called when prefix mapping ends.
        /// </summary>
        /// <param name="prefix">The prefix.</param>
        public void EndPrefixMapping(string prefix)
        {
        }

        /// <summary>
        /// A method on the handler that is called when ignorable whitespace occurs.
        /// </summary>
        /// <param name="buffer">The character buffer.</param>
        public void IgnorableWhitespace(string buffer)
        {
            if (_fieldData == null || String.IsNullOrEmpty(buffer))
                return;

            buffer = CheckEndOfLine(buffer);

            _fieldData.Append(buffer);
        }

        /// <summary>
        /// A method on the handler that is called when the entity is skipped.
        /// </summary>
        /// <param name="entityName">The entity name.</param>
        public void SkippedEntity(string entityName)
        {
        }

        /// <summary>
        /// Gets the results of the parsing.
        /// </summary>
        /// <returns>An object of the results.</returns>
        /// <exception cref="MocaException">Thrown if an error occurs or is the result of the command.</exception>
        public abstract object GetResults();

        /// <summary>
        /// Starts the element.
        /// </summary>
        /// <param name="namespaceURI">The namespace URI.</param>
        /// <param name="name">The name.</param>
        /// <param name="rawName">Name of the raw.</param>
        /// <param name="attributes">The attributes.</param>
        public void StartElement(string namespaceURI, string name, string rawName, Dictionary<string, string> attributes)
        {
            switch (name)
            {
                case MOCA_RESULTS_ELEMENT:
                    
                    if (_currentItem != null && _currentItem.HeaderData.ColumnCount > 0)
                    {
                        //If we're in a sub results set, push the parent on the stack
                        var newItem = new TResultItem();
                        _stack.Push(_currentItem);
                        _currentItem = newItem;
                        OnSubResultsStart(newItem);
                    }
                    
                    break;
                case STATUS_ELEMENT:
                    _state = ParserState.STATUS;
                    break;
                case SESSION_ID_ELEMENT:
                    _state = ParserState.SESSIONID;
                    break;
                case MESSAGE_ELEMENT:
                    _state = ParserState.MESSAGE;
                    break;
                case COLUMN_ELEMENT:
                    var colName = attributes[NAME_ATTRIBUTE];
                    var typeCode = attributes[TYPE_ATTRIBUTE][0];
                    var type = MocaType.Lookup(Char.ToUpper(typeCode));
                    
                    var col = new DataColumn(colName, type.Class); 

                    //Disable as MCS has issues with nullable flags
                    //col.AllowDBNull = Boolean.Parse(attributes[NULLABLE_ATTRIBUTE]);

                    /* This was removed due to unicode support and removal of the
                     * defined column length value support in MOCA server
                        int length;
                        if (type.Equals(MocaType.STRING) &&
                            Int32.TryParse(attributes[LENGTH_ATTRIBUTE], out length) &&
                            length > 0)
                        {
                            col.MaxLength = length;
                        }
                    */
                    col.MaxLength = -1;
                    
                    _currentItem.HeaderData.Columns.Add(col);
                    break;
                case DATA_ELEMENT:
                    //Check for duplicate columns here
                    _currentItem.HeaderData.CheckDuplicateColumns();
                    OnRowCollectionStart(_currentItem);
                    break;
                case ROW_ELEMENT:
                    OnRowStart(_currentItem);
                    break;
                case FIELD_ELEMENT:
                    _state = ParserState.FIELD;
                    _fieldData = new StringBuilder();
                    
                    OnFieldStart(_currentItem);
                    break;
                default:
                    break;
            }

        }

        /// <summary>
        /// A method handler that is called when content form an element is read.
        /// </summary>
        /// <param name="buffer">The buffer.</param>
        /// <exception cref="ProtocolException">No status code returned</exception>
        public void Characters(string buffer)
        {
            if (String.IsNullOrEmpty(buffer))
                return;

            buffer = CheckEndOfLine(buffer);

            switch (_state)
            {
                case ParserState.STATUS:
                    int status;
                    if (!Int32.TryParse(buffer, out status))
                        throw new ProtocolException("No status code returned");

                    _currentItem.HeaderData.Status = status;
                    break;
                case ParserState.MESSAGE:
                    _currentItem.HeaderData.Message = buffer;
                    break;
                case ParserState.SESSIONID:
                    SessionId = buffer;
                    break;
                case ParserState.FIELD:
                    //var type = item.HeaderData.Columns[item.CurrentColumn].DataType;
                    //if (type == typeof(byte[]))
                    //{
                    //    try
                    //    {
                    //        //_base64Pipe.write(buffer, start, length);
                    //    }
                    //    catch (IOException e)
                    //    {
                    //        throw new ReadResponseException(e);
                    //    }
                    //    break;
                    //}
                    
                    _fieldData.Append(buffer);
                    break;
            }
        }

        /// <summary>
        /// Ends the element.
        /// </summary>
        /// <param name="uri">The URI.</param>
        /// <param name="localName">Name of the local.</param>
        /// <param name="name">The name.</param>
        /// <exception cref="ProtocolException"><c>ProtocolException</c>.</exception>
        public void EndElement(String uri, String localName, String name)
        {
            switch (name)
            {
                case MOCA_RESULTS_ELEMENT:
                    
                    if (_stack.Count > 0)
                    {
                        var result = _currentItem;
                        _currentItem = _stack.Pop();
                        OnSubResultsEnd(_currentItem, result);
                    }
                    break;

                case STATUS_ELEMENT:
                case MESSAGE_ELEMENT:
                case SESSION_ID_ELEMENT:
                    _state = ParserState.NONE;
                    break;

                case DATA_ELEMENT:
                    OnRowCollectionEnd(_currentItem);
                    break;

                case ROW_ELEMENT:
                    OnRowEnd(_currentItem);
                    break;

                case FIELD_ELEMENT:
                    
                    if (_fieldData != null)
                    {
                        OnFieldEnd(_currentItem, _fieldData.ToString());
                    }

                    _state = ParserState.NONE;
                    _fieldData = null;
                    _currentItem.CurrentColumn++;
                    break;

                default:
                    break;
            }
        }

        /// <summary>
        /// Gets the current item. This is used to provide null checks in case of premature failure.
        /// </summary>
        /// <returns>The current result item.</returns>
        protected TResultItem GetCurrentItem()
        {
            return _currentItem ?? new TResultItem();
        }

        /// <summary>
        /// Called when a sub results set is started.
        /// </summary>
        /// <param name="subItem">The sub item.</param>
        protected virtual void OnSubResultsStart(TResultItem subItem)
        {

        }

        /// <summary>
        /// Called when a sub results set is complete.
        /// </summary>
        /// <param name="parent">The parent item.</param>
        /// <param name="subItem">The sub item.</param>
        protected virtual void OnSubResultsEnd(TResultItem parent, TResultItem subItem)
        {

        }

        /// <summary>
        /// Called when a field has started processing.
        /// </summary>
        /// <param name="currentItem">The current item.</param>
        protected virtual void OnFieldStart(TResultItem currentItem)
        {
        }

        /// <summary>
        /// Called when a field has completed processing.
        /// </summary>
        /// <param name="currentItem">The current item.</param>
        /// <param name="fieldData">The field data.</param>
        protected virtual void OnFieldEnd(TResultItem currentItem, string fieldData)
        {
        }

        /// <summary>
        /// Called when a new row is started.
        /// </summary>
        /// <param name="currentItem">The current item.</param>
        protected virtual void OnRowStart(TResultItem currentItem)
        {
        }

        /// <summary>
        /// Called when a new row is ended.
        /// </summary>
        /// <param name="currentItem">The current item.</param>
        protected virtual void OnRowEnd(TResultItem currentItem)
        {
        }

        /// <summary>
        /// Called when a new row collection is started.
        /// </summary>
        /// <param name="currentItem">The current item.</param>
        protected virtual void OnRowCollectionStart(TResultItem currentItem)
        {
        }
        
        /// <summary>
        /// Called when a new row collection is ended.
        /// </summary>
        /// <param name="currentItem">The current item.</param>
        protected virtual void OnRowCollectionEnd(TResultItem currentItem)
        {
        }

        /// <summary>
        /// Checks the end of line.
        /// </summary>
        /// <param name="buffer">The buffer.</param>
        /// <returns></returns>
        protected static string CheckEndOfLine(string buffer)
        {
            var builder = new StringBuilder(buffer.Length);
            var lastChar = (char)0;
            for (var i = 0; i < buffer.Length; i++)
            {
                var currentChar = buffer[i];
                if (currentChar == '\n' && lastChar != '\r')
                {
                    builder.Append('\r');
                }
                builder.Append(currentChar);
                lastChar = currentChar;
            }

            return builder.ToString();
        }

    }
}
