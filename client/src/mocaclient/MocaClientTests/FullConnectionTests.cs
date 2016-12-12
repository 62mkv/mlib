using System;
using System.Data;
using System.Reflection;
using NUnit.Framework;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client.Tests
{
    /// <summary>
    /// Test Full Connection properties
    /// </summary>
    [TestFixture]
    public class FullConnectionTests
    {
        private const string HOST = "http://localhost:4500/service";
        private const int PORT = 0;

        /// <summary>
        /// Tests the setting of environment variables.
        /// </summary>
        [Test]
        public void TestFullConnectionEnvironmentVariables()
        {
            const string expected = "BAR";
            const string localeID = "US_ENGLISH";

            Assert.IsTrue(
                !FullConnection.CanChangeEnvironmentVariable(Constants.EnvironmentKeyLocaleID));
            Assert.IsTrue(
                !FullConnection.CanChangeEnvironmentVariable(
                     Constants.EnvironmentKeyLocaleID.ToLower()));
            Assert.IsTrue(FullConnection.CanChangeEnvironmentVariable("FOO"));

            FullConnection connection = new FullConnection("", HOST, PORT, "");
            connection.LocaleID = localeID;

            //Should not work
            connection.SetEnvironmentVariable(Constants.EnvironmentKeyLocaleID, "ACBD");

            //Validate Property
            Assert.AreEqual(localeID, connection.LocaleID);

            //Validate Env Hash
            Assert.AreEqual(localeID,
                            connection.GetEnvironmentVariable(Constants.EnvironmentKeyLocaleID));

            //Should work
            connection.SetEnvironmentVariable("FOO", expected);

            //Validate
            Assert.AreEqual(expected,
                            connection.GetEnvironmentVariable("FOO"));
        }


        /// <summary>
        /// Tests the setting of environment variables being case insensitive for the keys.
        /// </summary>
        [Test]
        public void TestFullConnectionEnvironmentVariablesCaseInsensitive()
        {
            const string expected = "BAR";
            FullConnection connection = new FullConnection("", HOST, PORT, "");

            connection.SetEnvironmentVariable("CAPS_FOO", expected);
            string getValue = connection.GetEnvironmentVariable("caps_foo");

            Assert.IsTrue(FullConnection.CanChangeEnvironmentVariable("CAPS_FOO"));
            Assert.AreEqual(expected, getValue);
        }

        /// <summary>
        /// Tests the full connection using an invalid strategy.
        /// </summary>
        [Test]
        public void TestFullConnectionInvalidStrategy()
        {
            try
            {
                FullConnection connection = new FullConnection("", HOST, PORT, "", "string");

                Assert.Fail(connection.ToString());
            }
            catch (TypeLoadException)
            {
                return;
            }

            Assert.Fail("Invalid FullConnection Strategy did not fail.");
        }

        /// <summary>
        /// Tests the full connection with a passed locale ID.
        /// </summary>
        [Test]
        public void TestFullConnectionLocaleID()
        {
            const string localeID = "US_ENGLISH";

            FullConnection connection = new FullConnection("", HOST, PORT, "");
            connection.LocaleID = localeID;

            //Validate Property
            Assert.AreEqual(localeID, connection.LocaleID);

            //Validate Env Hash
            Assert.AreEqual(localeID,
                            connection.GetEnvironmentVariable(Constants.EnvironmentKeyLocaleID));
        }

        /// <summary>
        /// Tests the full connection with a mock login.
        /// </summary>
        [Test]
        public void TestFullConnectionLogin()
        {
            const string localeID = "US_ENGLISH";

            FullConnection connection = new FullConnection(HOST, PORT, "");
            try
            {
                connection.Connect();
                connection.Login("SUPER", "super");
            }
            catch (ConnectionFailedException)
            {
                return;
            }
            

            Assert.IsTrue(connection.Connected);
            Assert.IsTrue(connection.LoggedIn);
            Assert.IsNotNull(connection.LoginInfo);

            Assert.AreEqual("SUPER", connection.UserID);
            Assert.AreEqual(localeID, connection.LocaleID);

            //Validate Env Hash
            Assert.AreEqual(localeID,
                            connection.GetEnvironmentVariable(Constants.EnvironmentKeyLocaleID));

            connection.LogOut();
            Assert.IsTrue(!connection.LoggedIn);
            Assert.IsNull(connection.UserID);
            Assert.IsNull(connection.LocaleID);
            Assert.IsEmpty(connection.WarehouseID);
        }

        /// <summary>
        /// Tests the full connection with a mock login and locale override.
        /// </summary>
        [Test]
        public void TestFullConnectionWithLocaleOverride()
        {
            const string localeID = "US_ENGLISH";

            FullConnection connection = new FullConnection(HOST, PORT, "");
            connection.LocaleID = localeID;
            try
            {
                connection.Connect();
                connection.Login("SUPER", "super");
            }
            catch (ConnectionFailedException)
            {
                return;
            }

            Assert.IsTrue(connection.Connected);
            Assert.IsTrue(connection.LoggedIn);
            Assert.IsNotNull(connection.LoginInfo);

            Assert.AreEqual("SUPER", connection.UserID);
            Assert.AreEqual(localeID, connection.LocaleID);
            Assert.AreEqual(localeID, connection.LoginInfo.LocaleID);

            //Validate Env Hash
            Assert.AreEqual(localeID,
                            connection.GetEnvironmentVariable(Constants.EnvironmentKeyLocaleID));

            connection.LogOut();
            Assert.IsTrue(!connection.LoggedIn);
        }

        /// <summary>
        /// Tests the full connection with a multi thread strategy.
        /// </summary>
        [Test]
        public void TestFullConnectionMultiThreadStrategy()
        {
            FullConnection connection = new FullConnection("", HOST, PORT, "",
                                                           "MultiThreadQueueConnection");

            Assert.IsNotNull(connection);
        }

        /// <summary>
        /// Tests the full connection properties.
        /// </summary>
        [Test]
        public void TestFullConnectionProperties()
        {
            // Ensure none of the properties throw a Null Reference Exception
            FullConnection connection = new FullConnection();

            foreach (
                PropertyInfo property in
                    connection.GetType().GetProperties(BindingFlags.GetProperty))
            {
                Assert.IsNotNull(property.GetValue(connection, null),
                                 string.Format("{0} was null on defualt get", property.Name));
            }
        }

        /// <summary>
        /// Tests the full connection set of warehouse ID (WH_ID).
        /// </summary>
        [Test]
        public void TestFullConnectionWarehouseID()
        {
            const string warehouseID = "WMD1";

            FullConnection connection = new FullConnection();
            connection.WarehouseID = warehouseID;

            //Validate Property
            Assert.AreEqual(warehouseID, connection.WarehouseID);

            //Validate Env Hash
            Assert.AreEqual(warehouseID,
                            connection.GetEnvironmentVariable(Constants.EnvironmentKeyWarehouse));
        }

        /// <summary>
        /// Tests the full data table output with a direct connection.
        /// </summary>
        [Test]
        public void TestFullDataTableOutput()
        {
            FullConnection connection = new FullConnection("", HOST, PORT, "");
            try
            {
                connection.Connect();
                connection.Login("SUPER", "super");

                const string cmd = "publish data where x=1";
                DataTable results;
                int result = connection.Execute(cmd, out results);

                //Validate Results
                Assert.AreEqual(0, result);
                Assert.IsNotNull(results);

                //Validate that table name is the command
                Assert.AreEqual(cmd, results.TableName);


                //Validate DataView as well
                DataView dvResults;
                result = connection.Execute(cmd, out dvResults);

                //Validate Results
                Assert.AreEqual(0, result);
                Assert.IsNotNull(dvResults);
                Assert.IsNotNull(dvResults.Table);

                //Validate that table name is the command
                Assert.AreEqual(cmd, dvResults.Table.TableName);
            }
            catch (ConnectionFailedException)
            {
                return;
            }
            finally
            {
                connection.Close();
            }
        }
    }
}