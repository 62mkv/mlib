using System.Collections.Generic;
using RedPrairie.MOCA.Client.Interfaces;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// A class that contains the contents of a MOCA command.
    /// </summary>
    public class Command
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="Command"/> class.
        /// </summary>
        /// <param name="commandText">The command text.</param>
        public Command(string commandText)
        {
            CommandText = commandText;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="Command"/> class.
        /// </summary>
        /// <param name="commandText">The command text.</param>
        /// <param name="arguments">The arguments.</param>
        public Command(string commandText, IEnumerable<IMocaArgument> arguments)
               :this(commandText)
        {
            Arguments = arguments;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="Command"/> class.
        /// </summary>
        /// <param name="commandText">The command text.</param>
        /// <param name="arguments">The arguments.</param>
        /// <param name="context">The context.</param>
        public Command(string commandText, IEnumerable<IMocaArgument> arguments, IEnumerable<IMocaArgument> context)
            : this(commandText)
        {
            Arguments = arguments;
            Context = context;
        }

        /// <summary>
        /// Gets the arguments.
        /// </summary>
        /// <value>The arguments.</value>
        public IEnumerable<IMocaArgument> Arguments { get; private set; }

        /// <summary>
        /// Gets the command text.
        /// </summary>
        /// <value>The command text.</value>
        public string CommandText { get; private set; }

        /// <summary>
        /// Gets or sets the context.
        /// </summary>
        /// <value>The context.</value>
        public IEnumerable<IMocaArgument> Context { get; private set; }
    }
}