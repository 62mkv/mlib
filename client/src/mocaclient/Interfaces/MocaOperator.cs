namespace RedPrairie.MOCA.Client.Interfaces
{
    /// <summary>
    /// The MOCA operator
    /// </summary>
    public enum MocaOperator
    {
        /// <summary>
        /// The  = operator
        /// </summary>
        Equal,
        /// <summary>
        /// The != operator
        /// </summary>
        NotEqual,
        /// <summary>
        /// The &gt; operator
        /// </summary>
        GreaterThan,
        /// <summary>
        /// The &gt;= operator
        /// </summary>
        GreaterThanEqual,
        /// <summary>
        /// The &lt; operator
        /// </summary>
        LessThan,
        /// <summary>
        /// The &lt;= operator
        /// </summary>
        LessThanEqual,
        /// <summary>
        /// The like operator
        /// </summary>
        Like,
        /// <summary>
        /// The NOT LIKE operator
        /// </summary>
        NotLike,
        /// <summary>
        /// The RAW CLAUSE operator
        /// </summary>
        RawClause,
        /// <summary>
        /// The Named Clause operator
        /// </summary>
        NamedClause
    }
}