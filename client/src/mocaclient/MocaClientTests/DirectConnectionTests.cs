using System.Collections.Generic;
using NUnit.Framework;
using RedPrairie.MOCA.Client.Interfaces;

namespace RedPrairie.MOCA.Client.Tests
{
    /// <summary>
    /// An override class to test a direct connection to the server
    /// </summary>
    [TestFixture]
    //[Ignore]
    public class DirectConnectionTests : AbstractConnectionFixture
    {
        /// <summary>
        /// Gets a connection object from the inheriting test class.
        /// </summary>
        /// <param name="env">The enviroment to initialize the connection with.</param>
        /// <returns>
        /// An <see cref="IMocaConnection"/> based object
        /// </returns>
        protected override IMocaDataProvider GetConnection(string env)
        {
            FullConnection conn = new FullConnection(null, DEFAULT_HOST, DEFAULT_PORT, env, "DirectConnection");
            conn.Connect();
            return conn;
        }
    }
}