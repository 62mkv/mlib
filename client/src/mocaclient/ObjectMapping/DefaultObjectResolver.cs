using System;

namespace RedPrairie.MOCA.Client.ObjectMapping
{
    /// <summary>
    /// A default implimentation of the obejct resolver;
    /// </summary>
    public class DefaultObjectResolver : IObjectResolver
    {
        #region IObjectResolver Members

        /// <summary>
        /// Resolve an instance of the default requested type from the container.
        /// </summary>
        /// <param name="t"><see cref="Type"/> of object to get from the container.</param>
        /// <returns>The retrieved object.</returns>
        public object Resolve(Type t)
        {
            try
            {
                Type constructedType = t;
                if (t.ContainsGenericParameters)
                {
                    Type[] typeParams = t.GetGenericArguments();
                    constructedType = t.MakeGenericType(typeParams);
                }

                return Activator.CreateInstance(constructedType);
            }
            catch (SystemException e)
            {
                System.Diagnostics.Debug.Write(e.Message);
                return null;
            }
        }

        #endregion
    }
}
