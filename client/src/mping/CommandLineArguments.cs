using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;

namespace mping
{
    /// <summary>
    /// A class that parses and provides command line arguments.
    /// </summary>
    public class CommandLineArguments
    {
        #region Private Fields
        private readonly Regex _parser = new Regex("^(-|/)([A-Za-z?])(\\\".+\\\"|\\S*)");
        private readonly Dictionary<string, string> _args = new Dictionary<string, string>(StringComparer.InvariantCultureIgnoreCase);
        private const string DEFAULT_ARG = "DEFAULT_ARG";
        #endregion

        #region Constructor

        /// <summary>
        /// Initializes a new instance of the <see cref="CommandLineArguments"/> class.
        /// </summary>
        /// <param name="arguments">The system command line arguments.</param>
        public CommandLineArguments(IEnumerable<string> arguments)
        {
            ParseArguments(arguments);
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="CommandLineArguments"/> class.
        /// </summary>
        /// <param name="arguments">The system command line arguments.</param>
        /// <param name="applicationConfigurationFile">The application configuration file.</param>
        /// <param name="customConfigurationFile">The custom configuration file.</param>
        /// <param name="modulePath">The directory path to lookup modules in.</param>
        public CommandLineArguments(ICollection<string> arguments, string applicationConfigurationFile, string customConfigurationFile, string modulePath)
            : this(arguments)
        {
            ApplicationConfigurationFile = applicationConfigurationFile;
            CustomConfigurationFile = customConfigurationFile;
            ModulePath = modulePath;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="CommandLineArguments"/> class.
        /// </summary>
        /// <param name="arguments">The system command line arguments.</param>
        /// <param name="applicationConfigurationFile">The application configuration file.</param>
        /// <param name="customConfigurationFile">The custom configuration file.</param>
        /// <param name="modulePath">The directory path to lookup modules in.</param>
        /// <param name="applicationFileName">Name of the application file.</param>
        public CommandLineArguments(ICollection<string> arguments, string applicationConfigurationFile, string customConfigurationFile, string modulePath, string applicationFileName)
            : this(arguments, applicationConfigurationFile, customConfigurationFile, modulePath)
        {
            ApplicationFileName = applicationFileName;
        }

        #endregion

        #region Public Properties

        /// <summary>
        /// Gets the file name of the running application.
        /// </summary>
        /// <value>The file name of the application file.</value>
        public string ApplicationFileName { get; private set; }

        /// <summary>
        /// Gets the application configuration file.
        /// </summary>
        /// <value>The application configuration file.</value>
        public string ApplicationConfigurationFile { get; private set; }

        /// <summary>
        /// Gets the argument count.
        /// </summary>
        /// <value>The argument count.</value>
        public int ArgumentCount
        {
            get { return _args.Count; }
        }

        /// <summary>
        /// Gets the argument names that were passed to the file.
        /// </summary>
        /// <value>The argument names.</value>
        public string[] ArgumentNames
        {
            get { return _args.Keys.ToArray(); }
        }

        /// <summary>
        /// Gets the custom configuration file if it exists.
        /// </summary>
        /// <value>The application configuration file.</value>
        public string CustomConfigurationFile { get; private set; }

        /// <summary>
        /// Gets the default argument if it exists.
        /// </summary>
        /// <value>The default argument.</value>
        public string DefaultArgument
        {
            get
            {
                return _args.ContainsKey(DEFAULT_ARG) ? _args[DEFAULT_ARG] : null;
            }
        }

        /// <summary>
        /// Gets the module search path.
        /// </summary>
        /// <value>The module search path.</value>
        public string ModulePath { get; private set; }

        #endregion

        #region Public Methods
        /// <summary>
        /// Determines whether the specified argument name exists in the argument collection.
        /// </summary>
        /// <param name="argumentName">Name of the argument.</param>
        /// <returns>
        /// 	<c>true</c> if the specified argument name exists; otherwise, <c>false</c>.
        /// </returns>
        public bool ContainsArgument(string argumentName)
        {
            return _args.ContainsKey(argumentName);
        }

        /// <summary>
        /// Gets the specified argument.
        /// </summary>
        /// <param name="argumentName">The name of the argument.</param>
        /// <returns>The argument value</returns>
        public string GetArgument(string argumentName)
        {
            if (_args.ContainsKey(argumentName))
                return _args[argumentName];

            return null;
        }

        #endregion

        #region Private Methods
        /// <summary>
        /// Parses the argument collection based on the regular expression parser.
        /// </summary>
        /// <param name="arguments">The arguments.</param>
        private void ParseArguments(IEnumerable<string> arguments)
        {
            string lastArg = null;
            _args.Clear();
            
            foreach (var argument in arguments)
            {
                var matchCol = _parser.Matches(argument);

                if (matchCol.Count > 0)
                {
                    foreach (Match match in matchCol)
                    {
                        if (match.Groups.Count != 4) continue;
                        
                        var key = match.Groups[2].Value;
                        if (_args.ContainsKey(key))
                            continue;

                        var value = match.Groups[3].Value;
                        _args.Add(key, value);
                        
                        lastArg = !String.IsNullOrEmpty(value) ? null : key;
                    }
                }
                else
                {
                    if (lastArg != null)
                    {
                        if (String.IsNullOrEmpty(_args[lastArg]))
                            _args[lastArg] = argument;

                        lastArg = null;
                    }
                    else
                    {
                        _args.Add(DEFAULT_ARG, argument);
                    }
                }
            }
        }

        #endregion
    }
}