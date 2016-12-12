using System;
using System.Data;
using System.Net;
using System.Text;
using NUnit.Framework;
using RedPrairie.MOCA.Client.Interfaces;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client.Tests
{
    /// <summary>
    /// An abstract connection test fixture that contains a common
    /// base of test to use on the different connection classes to ensure
    /// uniform consistency
    /// </summary>
    public class AbstractConnectionFixture : TestUtilBase
    {
        /// <summary>
        /// The default host to connect to (<c>localhost</c>)
        /// </summary>
        public static string DEFAULT_HOST = IPAddress.Loopback.ToString();
        /// <summary>
        /// The default port to connect to (4500)
        /// </summary>
        public static int DEFAULT_PORT = 4500;

        /// <summary>
        /// Tests that a failed connection throws an exception.
        /// </summary>
        [Test]
        public virtual void TestFailedConnection()
        {
            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                conn.Close();

                try
                {
                    conn.Connect("1.1.1.1", 4500, null);
                }
                catch (Exception e)
                {
                    Assert.IsInstanceOfType(typeof (ConnectionFailedException), e);
                    return;
                }
                finally
                {
                    conn.Close();
                }

                Assert.Fail(
                    "Connection should have thrown a ConnectionFailedException on bad host/port");
            }
            finally
            {
                conn.Close();
            } 
        }

        /// <summary>
        /// Tests the execution of a general command.
        /// </summary>
        [Test]
        public void TestExecuteCommand()
        {
            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                ExecuteCommandResult res = conn.ExecuteResults("publish data where x='Hello'");
                Assert.IsTrue(res.HasData);
                Assert.AreEqual("Hello", res.TableData.Rows[0]["x"].ToString());
            }
            finally
            {
                conn.Close();
            }
        }

        /// <summary>
        /// Tests the connection to ensure result set with many rows returns successfully.
        /// </summary>
        [Test]
        public void TestLargeResults()
        {
            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                ExecuteCommandResult res = conn.ExecuteResults("do loop where count = 1000 | " +
                                                        "publish data where x='Hello' and y = @i");
                for (int i = 0; i < 1000; i++)
                {
                    Assert.IsTrue(res.HasData);
                    Assert.AreEqual("Hello", res.TableData.Rows[i]["x"].ToString());
                    Assert.AreEqual(i, res.TableData.Rows[i]["y"]);
                }
            }
            finally
            {
                conn.Close();
            }
        }

        /// <summary>
        /// Tests the different data types in a result set.
        /// </summary>
        [Test]
        public void TestDifferentDataTypes()
        {
            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                DateTime testDate = new DateTime(2004, 1, 1, 22, 2, 3, 0);

                for (int x = 0; x < 10; x++)
                {
                    ExecuteCommandResult res = conn.ExecuteResults("do loop where count = 10 | " +
                                                            "publish data " +
                                                            " where s='string value' and i = 1000 " +
                                                            "   and l = (1024 * 1024 * 1024) and f = 3.1415 " +
                                                            "   and d = date('20040101220203')");
                    for (int i = 0; i < 10; i++)
                    {
                        Assert.IsTrue(res.HasData);
                        Assert.AreEqual(10, res.TableData.Rows.Count);
                        Assert.AreEqual("string value", res.TableData.Rows[0]["s"].ToString());
                        Assert.AreEqual(1000, (Int32)res.TableData.Rows[0]["i"]);
                        Assert.AreEqual(1024 * 1024 * 1024, (Int32)res.TableData.Rows[0]["l"]);
                        Assert.AreEqual(3.1415, (Double)res.TableData.Rows[0]["f"]);
                        Assert.AreEqual(testDate, (DateTime)res.TableData.Rows[0]["d"]);
                    }
                }
            }
            finally
            {
                conn.Close();
            }
        }

        /// <summary>
        /// Tests the nested results (one <see cref="DataTable"/> inside another <see cref="DataTable"/>).
        /// </summary>
        [Test]
        public virtual void TestNestedResults()
        {
            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                for (int x = 0; x < 10; x++)
                {
                    ExecuteCommandResult res = conn.ExecuteResults(
                        "{do loop where count = 10 |" +
                        " publish data where i = @i and x = 'This is part ' || @i " +
                        " and y = sysdate and z = 89324.123 } >> res");
                    Assert.IsTrue(res.HasData);
                    Assert.AreEqual(1, res.TableData.Rows.Count);
                    Assert.IsNotNull(res.TableData.Rows[0]["res"]);
                    DataTable testRes = (DataTable)res.TableData.Rows[0]["res"];
                    for (int j = 0; j < 10; j++)
                    {
                        Assert.AreEqual(j, (Int32)testRes.Rows[j]["i"]);
                        Assert.AreEqual("This is part " + j, testRes.Rows[j]["x"]);
                        Assert.AreEqual(MocaType.DATETIME.Class, testRes.Columns["y"].DataType);
                        Assert.AreEqual(89324.123, Math.Round((Double)testRes.Rows[j]["z"], 3));
                    }
                }
            }
            finally
            {
                conn.Close();
            }
        }

        /// <summary>
        /// Tests that a -1403 result returns a <see cref="NotFoundException"/>
        /// </summary>
        [Test]
        public void TestInitiateCommandWithNotFound()
        {
            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                ExecuteCommandResult res = conn.ExecuteResults("set return status where status = -1403");
                if (res.HasData)
                    Assert.Fail("Expected NotFoundException");
            }
            catch (NotFoundException)
            {
                // Normal
            }
            finally
            {
                conn.Close();
            }
        }

        /// <summary>
        /// Tests that IsOK is set to false and the status code matches on a command that
        /// does not return and eOK.
        /// </summary>
        [Test]
        public void TestInitiateCommandWithError()
        {
            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                ExecuteCommandResult res = conn.ExecuteResults("set return status where status = 9231");
                if (res.IsOK)
                    Assert.Fail("Expected IsOK property on ExecuteCommandResult to be false");
                else
                    Assert.AreEqual(9231, res.StatusCode);
            }
            catch (MocaException)
            {
            }
            finally
            {
                conn.Close();
            }
        }

        /// <summary>
        /// Tests the environment strings to ensure they are getting passed.
        /// </summary>
        [Test]
        public void TestEnvironment()
        {
            const string env = "TEST1=test 1 value:TEST2=test 2 value";
            IMocaDataProvider conn;
            try
            {
                conn = GetConnection(env);
                conn.Login("SUPER", "super");
            }
            catch (Exception)
            {
                return;
            }

            try
            {
                ExecuteCommandResult res = conn.ExecuteResults("publish data where x=@@test1 and y = @@TEST2");
                Assert.IsTrue(res.HasData);
                Assert.AreEqual("test 1 value", res.TableData.Rows[0]["x"]);
                Assert.AreEqual("test 2 value", res.TableData.Rows[0]["y"]);
            }
            finally
            {
                conn.Close();
            }
        }

        /// <summary>
        /// Tests a command that passes a large argument in and returns it.
        /// </summary>
        [Test]
        public void TestLargeCommand()
        {
            char[] characterSet =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890~`!@#$%^&*()_-+={[}]|\\:;\"<>,.?/".
                    ToCharArray();
            StringBuilder element = new StringBuilder();
            Random r = new Random();
            for (int i = 0; i < 5000000; i++)
            {
                element.Append(characterSet[r.Next(characterSet.Length)]);
            }

            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                ExecuteCommandResult res = conn.ExecuteResults("publish data where x = '" + element + "'");
                Assert.IsTrue(res.HasData);
                Assert.AreEqual(element.ToString(), res.TableData.Rows[0]["x"]);
            }
            finally
            {
                conn.Close();
            }
        }

        /// <summary>
        /// Tests a command with a large number of arguments.
        /// </summary>
        [Test]
        public void TestLargeNumberOfArguments()
        {
            StringBuilder command = new StringBuilder();
            command.Append("publish data where ");

            for (int i = 0; i < 10000; i++)
            {
                if (i > 0) command.Append(" and ");
                command.Append("x" + i);
                command.Append(" = ");
                command.Append(i);
            }

            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                ExecuteCommandResult res = conn.ExecuteResults(command.ToString());
                Assert.AreEqual(10000, res.TableData.Columns.Count);
                for (int i = 0; i < 10000; i++)
                {
                    Assert.AreEqual("x" + i, res.TableData.Columns[i].ColumnName.ToLower());
                }

                Assert.IsTrue(res.HasData);

                for (int i = 0; i < 10000; i++)
                {
                    Assert.AreEqual(i, (Int32)res.TableData.Rows[0]["x" + i]);
                }
            }
            finally
            {
                conn.Close();
            }
        }

        /// <summary>
        /// Tests a command with a large number of arguments but returns no data.
        /// </summary>
        [Test]
        public void TestLargeNumberOfArgumentsUpstreamOnly()
        {
            StringBuilder command = new StringBuilder();
            command.Append("publish data where ");

            for (int i = 0; i < 10000; i++)
            {
                if (i > 0) command.Append(" and ");
                command.Append("x" + i);
                command.Append(" = ");
                command.Append(i);
            }

            command.Append(" | set return status where status = 0");

            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                ExecuteCommandResult res = conn.ExecuteResults(command.ToString());
                Assert.AreEqual(res.StatusCode, 0);
            }
            finally
            {
                conn.Close();
            }
        }

        /// <summary>
        /// Tests a large binary download.
        /// </summary>
        [Test]
        public void TestLargeBinaryDownload()
        {
            StringBuilder command = new StringBuilder();
            command.Append("[[ foo = new byte[50];");
            command.Append(" for (i in 0 .. foo.length -1)  {");
            command.Append(" foo[i] = (byte)0x01;");
            command.Append(" }");
            command.Append(" ]]");

            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                ExecuteCommandResult res = conn.ExecuteResults(command.ToString());
                Assert.AreEqual(0, res.StatusCode);
                Assert.AreEqual(res.TableData.Columns.Count, 1);
                Assert.IsInstanceOfType(typeof(byte[]), res.TableData.Rows[0][0]);
                Assert.AreEqual(50, ((byte[]) res.TableData.Rows[0][0]).Length);
            }
            finally
            {
                conn.Close();
            }
        }

        /// <summary>
        /// Tests a large number of commands being run in succession.
        /// </summary>
        [Test]
        public void TestLargeNumberOfCommands()
        {
            IMocaDataProvider conn = GetConnection();
            if (conn == null) return;
            try
            {
                for (int i = 0; i < 10000; i++)
                {
                    ExecuteCommandResult res = conn.ExecuteResults("publish data where x = 'ABCDEFG'");
                    Assert.AreEqual(1, res.TableData.Columns.Count);
                    Assert.IsTrue(res.HasData);
                    Assert.AreEqual("ABCDEFG", res.TableData.Rows[0]["x"].ToString());
                }
            }
            finally
            {
                conn.Close();
            }
        }

        /// <summary>
        /// Gets a connection object from the inheriting test class.
        /// </summary>
        /// <returns>An <see cref="IMocaDataProvider"/> based object</returns>
        /// <exception cref="NotImplementedException">Thrown if the method has not been overridden</exception>
        protected IMocaDataProvider GetConnection()
        {
            try
            {
                IMocaDataProvider conn = GetConnection(null);
                return conn.Login("SUPER", "super") ? conn : null;
            }
            catch (Exception)
            {
                return null;
            }
        }

        /// <summary>
        /// Gets a connection object from the inheriting test class.
        /// </summary>
        /// <param name="env">The environment to initialize the connection with.</param>
        /// <returns>An <see cref="IMocaDataProvider"/> based object</returns>
        /// <exception cref="NotImplementedException">Thrown if the method has not been overridden</exception>
        protected virtual IMocaDataProvider GetConnection(string env)
        {
            throw new NotImplementedException("IMocaDataProvider GetConnection(Dictionary<string, string> env)");
        }
    }
}