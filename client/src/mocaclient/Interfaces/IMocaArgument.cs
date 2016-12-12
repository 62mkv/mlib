namespace RedPrairie.MOCA.Client.Interfaces
{
    /// <summary>
    /// An interface that defines an argument passed into MOCA.
    /// </summary>
    public interface IMocaArgument
    {
        /// <summary>
        /// Gets the argument name.
        /// </summary>
        /// <value>The argument name.</value>
        string Name { get; }

        /// <summary>
        /// Gets the operator.
        /// </summary>
        /// <value>The operator.</value>
        MocaOperator Operator { get; }

        /// <summary>
        /// Gets the type.
        /// </summary>
        /// <value>The type.</value>
        MocaType Type { get; }

        /// <summary>
        /// Gets the value.
        /// </summary>
        /// <value>The value.</value>
        object Value { get; }

    }
}