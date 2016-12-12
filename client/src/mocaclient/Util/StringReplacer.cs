using System.Collections.Generic;
using System.Text;

namespace RedPrairie.MOCA.Util
{
    /// <summary>
    ///  Interface to allow different mechanisms to replace delimited
    ///  strings.  The <see cref="Lookup"/> method will be called whenever
    /// a delmited string (the key) is found by the StringReplacer.
    /// </summary>
    public interface IReplacementStrategy
    {
        /// <summary>
        /// Looks up the specified key to see if there is a replacement value for it.
        /// </summary>
        /// <param name="key">The key.</param>
        /// <returns>A replacement value or <c>null</c></returns>
        string Lookup(string key);
    }

    /// <summary>
    /// A string replacer class that looks for a string format and
    /// replaces it using a lookup strategy or Dictionary
    /// </summary>
    public class StringReplacer
    {
        #region Private Fields

        private string _prefix;
        private string _suffix;
        private IReplacementStrategy _strategy;

        #endregion

        #region Constructors

        /// <summary>
        /// Initializes a new instance of the <see cref="StringReplacer"/> class.
        /// </summary>
        /// <param name="delim">The delimter character that indicates the start and end of a token.</param>
        /// <param name="mapping">The mapping collection that contains lookup values.</param>
        public StringReplacer(char delim, Dictionary<string, object> mapping)
            : this(delim, new MapReplacementStrategy(mapping))
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="StringReplacer"/> class.
        /// </summary>
        /// <param name="delim">The delimter character that indicates the start and end of a token.</param>
        /// <param name="strategy">The replacement strategy for replacing tokens.</param>
        public StringReplacer(char delim, IReplacementStrategy strategy)
            : this(delim.ToString(), delim.ToString(), strategy)
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="StringReplacer"/> class.
        /// </summary>
        /// <param name="prefix">The prefix string that starts the token.</param>
        /// <param name="suffix">The suffix string that ends the token.</param>
        /// <param name="mapping">The mapping collection that contains lookup values.</param>
        public StringReplacer(string prefix, string suffix, Dictionary<string, object> mapping)
            : this(prefix, suffix, new MapReplacementStrategy(mapping))
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="StringReplacer"/> class.
        /// </summary>
        /// <param name="prefix">The prefix string that starts the token.</param>
        /// <param name="suffix">The suffix string that ends the token.</param>
        /// <param name="strategy">The mapping collection that contains lookup values.</param>
        public StringReplacer(string prefix, string suffix, IReplacementStrategy strategy)
        {
            _prefix = prefix;
            _suffix = suffix;
            _strategy = strategy;
        }

        #endregion

        #region Public Methods

        /// <summary>
        /// Perform the variable substitution on the given string.
        /// </summary>
        /// <param name="input">the string on which to perform substitution.</param>
        /// <returns>the input string with delimited identifiers substituted with
        /// their values, according to the passed-input <see cref="IReplacementStrategy"/>.</returns>
        public string Translate(string input)
        {
            // Don't bother if a null string is passed input.
            if (input == null)
                return null;

            StringBuilder result = new StringBuilder();
            int next = 0;
            do
            {
                // Find the next prefix input our string 
                int prefixPos = input.IndexOf(_prefix, next);
                if (prefixPos < 0)
                {
                    result.Append(input.Substring(next));
                    break;
                }

                // Append the string before the delimiter
                result.Append(input.Substring(next, prefixPos - next));

                // Find the end of this reference
                int suffixPos = input.IndexOf(_suffix, prefixPos + _prefix.Length);

                if (suffixPos < 0)
                {
                    result.Append(input.Substring(next));
                    break;
                }

                // Extract the key and do the Lookup
                string key = input.Substring(prefixPos + _prefix.Length, suffixPos - prefixPos - _prefix.Length);
                string value = _strategy.Lookup(key);

                // If null is returned, leave the parameter intact
                if (value == null)
                {
                    result.Append(input.Substring(prefixPos, suffixPos - prefixPos + _suffix.Length));
                }
                else
                {
                    result.Append(value);
                }

                // Continue with the rest of the string (after the suffix)
                next = suffixPos + _suffix.Length;
            } while (next < input.Length);

            return result.ToString();
        }

        #endregion

        #region Private Methods

        private class MapReplacementStrategy : IReplacementStrategy
        {
            private Dictionary<string, object> _map;

            public MapReplacementStrategy(Dictionary<string, object> map)
            {
                _map = map;
            }

            public string Lookup(string key)
            {
                if (_map.ContainsKey(key))
                    return (_map[key] == null) ? null : _map[key].ToString();
                else
                    return null;
            }
        }

        #endregion
    }
}