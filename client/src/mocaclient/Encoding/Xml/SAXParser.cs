using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Xml;
using RedPrairie.MOCA.Exceptions;

namespace RedPrairie.MOCA.Client.Encoding.Xml
{
    /// <summary>
    /// The SaxParser class build a SAX push model from the pull model found
    /// in the <see cref="XmlTextReader"/>.
    /// </summary>
    internal static class SaxParser
    {
        /// <summary>
        /// Parses the specified URL.
        /// </summary>
        /// <param name="stream">The input stream to read.</param>
        /// <param name="handler">The content handler.</param>
        /// <exception cref="ReadResponseException">Thrown if a processing error occurs.</exception>
        public static void Parse(Stream stream, IContentHandler handler)
        {
            var nsstack = new Stack();
            XmlTextReader reader = null;
            try
            {
                reader = new XmlTextReader(stream);
                var nsuri = reader.NameTable.Add("http://www.w3.org/2000/xmlns/");
                handler.StartDocument();
                while (reader.Read())
                {
                    string prefix;
                   
                    switch (reader.NodeType)
                    {
                        case XmlNodeType.Element:
                            nsstack.Push(null);
                            var atts = new Dictionary<string, string>();
                            while (reader.MoveToNextAttribute())
                            {
                                if (reader.NamespaceURI.Equals(nsuri))
                                {
                                    prefix = string.Empty;
                                    if (reader.Prefix == "xmlns")
                                    {
                                        prefix = reader.LocalName;
                                    }
                                    nsstack.Push(prefix);
                                    handler.StartPrefixMapping(prefix, reader.Value);
                                }
                                else
                                {
                                    atts.Add(reader.Name, reader.Value);
                                }
                            }
                            reader.MoveToElement();
                            handler.StartElement(reader.NamespaceURI,
                                                  reader.LocalName, reader.Name, atts);
                            if (reader.IsEmptyElement)
                            {
                                handler.EndElement(reader.NamespaceURI,
                                                    reader.LocalName, reader.Name);
                            }
                            break;
                        case XmlNodeType.EndElement:
                            handler.EndElement(reader.NamespaceURI,
                                                reader.LocalName, reader.Name);
                            prefix = (string)nsstack.Pop();
                            while (prefix != null)
                            {
                                handler.EndPrefixMapping(prefix);
                                prefix = (string)nsstack.Pop();
                            }
                            break;
                        case XmlNodeType.Text:
                            handler.Characters(reader.ReadString());
                            //After read your are automatically put on the next tag so you
                            //have to call the proper case from here or it won't work correctly.
                            if (reader.NodeType == XmlNodeType.Element)
                            {
                                goto case XmlNodeType.Element;
                            }
                            if (reader.NodeType == XmlNodeType.EndElement)
                            {
                                goto case XmlNodeType.EndElement;
                            }
                            break;
                        case XmlNodeType.ProcessingInstruction:
                            handler.ProcessingInstruction(reader.Name, reader.Value);
                            break;
                        case XmlNodeType.Whitespace:
                            handler.IgnorableWhitespace(reader.Value);
                            break;
                        case XmlNodeType.Entity:
                            handler.SkippedEntity(reader.Name);
                            break;
                    }
                }
                handler.EndDocument();
            }
            catch (XmlException ex)
            {
                throw new ReadResponseException(ex);
            }
            catch (MocaException)
            {
                throw;
            }
            catch (Exception ex)
            {
                throw new ReadResponseException(ex);
            }
            finally
            {
                if (reader != null && reader.ReadState != ReadState.Closed)
                {
                    reader.Close();
                }
            }
        }
    }
}