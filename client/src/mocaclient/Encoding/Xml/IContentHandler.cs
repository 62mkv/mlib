using System.Collections.Generic;
using RedPrairie.MOCA.Exceptions;

/// <summary>
/// A content handler interface for the SAX parser.
/// </summary>
internal interface IContentHandler
{
    /// <summary>
    /// Gets the session id.
    /// </summary>
    /// <value>The session id.</value>
    string SessionId { get; }

    /// <summary>
    /// A method on the handler that is called when document processing starts.
    /// </summary>
    void StartDocument();

    /// <summary>
    /// A method on the handler that is called when document processing ends.
    /// </summary>
    void EndDocument();

    /// <summary>
    /// A method on the handler that is called when processing instructions exist.
    /// </summary>
    /// <param name="target">The instruction target.</param>
    /// <param name="data">The instruction data.</param>
    void ProcessingInstruction(string target, string data);

    /// <summary>
    /// A method on the handler that is called when prefix mapping starts.
    /// </summary>
    /// <param name="prefix">The prefix.</param>
    /// <param name="uri">The URI of the prefix.</param>
    void StartPrefixMapping(string prefix, string uri);

    /// <summary>
    /// A method on the handler that is called when prefix mapping ends.
    /// </summary>
    /// <param name="prefix">The prefix.</param>
    void EndPrefixMapping(string prefix);

    /// <summary>
    /// A method on the handler that is called when a start element occurs.
    /// </summary>
    /// <param name="namespaceURI">The namespace URI of the element.</param>
    /// <param name="localName">The local name of the element.</param>
    /// <param name="rawName">The raw name of the element.</param>
    /// <param name="attributes">A <see cref="Dictionary{TKey,TValue}"/> of attributes.</param>
    void StartElement(string namespaceURI, string localName, string rawName,
                      Dictionary<string, string> attributes);

    /// <summary>
    /// A method on the handler that is called when an end element occurs.
    /// </summary>
    /// <param name="namespaceURI">The namespace URI of the element.</param>
    /// <param name="localName">The local name of the element.</param>
    /// <param name="rawName">The raw name of the element.</param>
    void EndElement(string namespaceURI, string localName, string rawName);

    /// <summary>
    /// A method on the handler that is called when characters within an element are read.
    /// </summary>
    /// <param name="buffer">The character buffer.</param>
    void Characters(string buffer);

    /// <summary>
    /// A method on the handler that is called when ignorable whitespace occurs.
    /// </summary>
    /// <param name="buffer">The character buffer.</param>
    void IgnorableWhitespace(string buffer);

    /// <summary>
    /// A method on the handler that is called when the entity is skipped.
    /// </summary>
    /// <param name="entityName">The entity name.</param>
    void SkippedEntity(string entityName);

    /// <summary>
    /// Gets the results of the parsing.
    /// </summary>
    /// <returns>An object of the results.</returns>
    /// <exception cref="MocaException">Thrown if an error occurs or is the result of the command.</exception>
    object GetResults();
}