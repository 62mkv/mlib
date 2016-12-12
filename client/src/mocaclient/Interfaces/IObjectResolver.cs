using System;

namespace RedPrairie.MOCA.Client.ObjectMapping
{
    /// <summary>
    /// An interface class the represents the resolver.
    /// </summary>
    public interface IObjectResolver
    {
        /// <summary>
        /// Resolve an instance of the default requested type from the container.
        /// </summary>
        /// <param name="t"><see cref="Type"/> of object to get from the container.</param>
        /// <returns>The retrieved object.</returns>
        object Resolve(Type t);
    }
}
