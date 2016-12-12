using System;
using NUnit.Framework;
using RedPrairie.MOCA.Client.Interfaces;

namespace RedPrairie.MOCA.Client.Tests.Other
{
    /// <summary>
    /// An override class to test the web connection
    /// </summary>
    [TestFixture]
    [Ignore]
    public class WebConnectionTests : AbstractConnectionFixture
    {
        private const string URL = "http://polecat64:7100";
        
        public override void TestFailedConnection()
        {
            //This test does not apply to the web
        }

        /// <summary>
        /// Gets a connection object from the inheriting test class.
        /// </summary>
        /// <param name="env">The enviroment to initialize the connection with.</param>
        /// <returns>
        /// An <see cref="IMocaConnection"/> based object
        /// </returns>
        protected override IMocaDataProvider GetConnection(string env)
        {
            var conn = new FullConnection(null, URL, 0, env, "WebConnection");
            conn.Connect();
            return conn;
        }

        /// <summary>
        /// Tests that control characters are processed correctly.
        /// </summary>
        [Test]
        public void TestControlCharacters()
        {
            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                ExecuteCommandResult res = conn.ExecuteResults(string.Format("[select '{0}' as x from dual]", (char)2));
                Assert.IsTrue(res.HasData);
                Assert.AreEqual(new String(new[]{(char)65533}), res.TableData.Rows[0]["x"].ToString());
            }
            finally
            {
                conn.Close();
            }
        }

        /// <summary>
        /// Tests the whole connection for correct closing and disposal in a tight loop.
        /// </summary>
        [Test]
        public void TestWebConnectionTightLoop()
        {
            var firstTry = true;
            for (var i = 0; i < 5; i++)
            {
                System.Diagnostics.Debug.WriteLine(string.Format("Running pass {0}", i + 1));
                var conn = new FullConnection(URL, 0);

                try
                {
                    conn.Connect();
                    if (!conn.Login("super", "super"))
                    {
                        return;
                    }
                }
                catch (Exception)
                {
                    //Server is not available so skip test.
                    if (firstTry)
                        return;

                    throw;
                }

                firstTry = false;
                try
                {
                    ExecuteCommandResult res = conn.ExecuteResults(string.Format("[select '{0}' as x from dual]", (char)2));
                    Assert.IsTrue(res.HasData);
                    Assert.AreEqual(new String(new[] { (char)65533 }), res.TableData.Rows[0]["x"].ToString());
                }
                finally
                {
                    conn.Close();
                } 
            }
        }

        /// <summary>
        /// Tests that whitespace characters are processed correctly.
        /// </summary>
        [Test]
        public void TestWhitespaceCharacters()
        {
            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                ExecuteCommandResult res = conn.ExecuteResults("[select chr(9) as x from dual]");
                Assert.IsTrue(res.HasData);
                Assert.AreEqual("\t", res.TableData.Rows[0]["x"].ToString());
            }
            finally
            {
                conn.Close();
            }
        }

        /// <summary>
        /// Tests that new line characters are processed correctly.
        /// </summary>
        [Test]
        public void TestNewlineCharacters()
        {
            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                const string command = "[[ java.lang.StringBuilder builder = new java.lang.StringBuilder();" + 
                                       "builder.append(\"Hello\");" +
                                       "builder.append(System.getProperty(\"line.separator\"));" +
                                       "builder.append(\"World!\");" +
                                       "x = builder.toString(); ]]";

                ExecuteCommandResult res = conn.ExecuteResults("[[ x = System.getProperty(\"line.separator\");]]");
                ExecuteCommandResult res2 = conn.ExecuteResults(command);
                ExecuteCommandResult res3 = conn.ExecuteResults("publish data where x = 'This\r\nis a test ~blah~'");
                ExecuteCommandResult res4 = conn.ExecuteResults("publish data where x = '\r\n'");
                
                Assert.IsTrue(res.HasData);
                Assert.AreEqual("\r\n", res.TableData.Rows[0]["x"].ToString());

                Assert.IsTrue(res2.HasData);
                Assert.AreEqual("Hello\r\nWorld!", res2.TableData.Rows[0]["x"].ToString());

                Assert.IsTrue(res3.HasData);
                Assert.AreEqual("This\r\nis a test ~blah~", res3.TableData.Rows[0]["x"].ToString());

                Assert.IsTrue(res4.HasData);
                Assert.AreEqual("\r\n", res4.TableData.Rows[0]["x"].ToString());
            }
            finally
            {
                conn.Close();
            }
        }
    }
}