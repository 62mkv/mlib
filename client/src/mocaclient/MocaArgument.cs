using RedPrairie.MOCA.Client.Interfaces;

namespace RedPrairie.MOCA.Client
{
    /// <summary>
    /// A MOCA argument class that contains the name, operator and value of a moca argument.
    /// </summary>
    public class MocaArgument<T>:IMocaArgument
    {

        /// <summary>
        /// Initializes a new instance of the <see cref="MocaArgument&lt;T&gt;"/> class.
        /// </summary>
        /// <param name="name">The name.</param>
        /// <param name="value">The value.</param>
        public MocaArgument(string name, T value)
            : this(name, value, MocaOperator.Equal)
        {
        }


        /// <summary>
        /// Initializes a new instance of the <see cref="MocaArgument&lt;T&gt;"/> class.
        /// </summary>
        /// <param name="name">The name.</param>
        /// <param name="value">The value.</param>
        /// <param name="oper">The operator to use in queries.</param>
        public MocaArgument(string name, T value, MocaOperator oper)
        {
            Name = name;
            Operator = oper;
            Value = value;
            Type = MocaType.LookupClass(typeof(T));
        }

        #region Implementation of IMocaArgument

        /// <summary>
        /// Gets the argument name.
        /// </summary>
        /// <value>The argument name.</value>
        public string Name { get; private set; }

        /// <summary>
        /// Gets the operator.
        /// </summary>
        /// <value>The operator.</value>
        public MocaOperator Operator { get; private set; }

        /// <summary>
        /// Gets the type.
        /// </summary>
        /// <value>The type.</value>
        public MocaType Type { get; private set; }

        /// <summary>
        /// Gets the value.
        /// </summary>
        /// <value>The value.</value>
        public object Value { get; private set; }

        #endregion
    }
}