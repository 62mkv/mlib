using NUnit.Framework;
using RedPrairie.MOCA.Client.Interfaces;

namespace RedPrairie.MOCA.Client.Tests
{
    /// <summary>
    /// Test class to ensure the SmartConnection class works appropriately
    /// </summary>
    [TestFixture]
    public class SmartConnectionTests : AbstractConnectionFixture
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
            FullConnection conn = new FullConnection(null, DEFAULT_HOST, DEFAULT_PORT, env, "SmartConnection");
            conn.Connect();
            return conn;
        }
    }
}