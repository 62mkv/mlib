using System;
using System.Collections.Generic;
using System.Text.RegularExpressions;

namespace DevTreeConverter
{
    /// <summary>
    /// Preferences for the application
    /// </summary>
    public class CommandLineArguments
    {
        private readonly Regex parser = new Regex("/([A-Za-z])(\\\".+\\\"|\\S*)");
        private readonly Dictionary<string, string> args = new Dictionary<string, string>(StringComparer.InvariantCultureIgnoreCase);

        public CommandLineArguments(IEnumerable<string> arguments)
        {
            ParseArguments(arguments);
        }

        #region Public Methods
        public bool ContainsArgument(string argumentName)
        {
            return args.ContainsKey(argumentName);
        }

        public string GetArgument(string argumentName)
        {
            if (args.ContainsKey(argumentName))
                return args[argumentName];
            else
                return null;
        }
        #endregion

        #region Private Methods
        private void ParseArguments(IEnumerable<string> arguments)
        {
            if (arguments == null)
                return;
            
            string lastArg = null;
            args.Clear();
            foreach (string argument in arguments)
            {
                MatchCollection matchCol = parser.Matches(argument);

                if (matchCol.Count > 0)
                {
                    foreach (Match match in matchCol)
                    {
                        if (match.Groups.Count == 3)
                        {
                            args.Add(match.Groups[1].Value, match.Groups[2].Value);
                            lastArg = match.Groups[1].Value;
                        }
                    }
                }
                else
                {
                    if (lastArg != null && String.IsNullOrEmpty(args[lastArg]))
                    {
                        args[lastArg] = argument;
                        lastArg = null;
                    }
                }
            }
        }

        #endregion
    }
}
