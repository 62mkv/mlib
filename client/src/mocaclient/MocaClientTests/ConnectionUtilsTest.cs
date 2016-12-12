using System.Collections.Generic;
using System.Data;
using NUnit.Framework;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client.Tests.Base
{
    /// <summary>
    /// Tests the ConnectionUtils Class
    /// </summary>
    [Category("BuildTests")]
    [TestFixture]
    public class ConnectionUtilsTest
    {
        /// <summary>
        /// Tests the environment string parsing.
        /// </summary>
        [Test]
        public void TestParseEnvironmentString()
        {
            string test1 = "a=foo:b=bar";
            Dictionary<string, string> result = ConnectionUtils.ParseEnvironmentString(test1);

            Assert.IsTrue(result.ContainsKey("a"));
            Assert.AreEqual("foo", result["a"]);

            Assert.IsTrue(result.ContainsKey("b"));
            Assert.AreEqual("bar", result["b"]);
        }

        /// <summary>
        /// Tests the environment string building.
        /// </summary>
        [Test]
        public void TestBuildEnvironmentString()
        {
            Dictionary<string, string> test = new Dictionary<string, string>();
            test.Add("a", "foo");
            test.Add("b", "bar");

            string result = ConnectionUtils.BuildEnvironmentString(test);
            Assert.AreEqual("A=foo:B=bar", result);
        }

        /// <summary>
        /// Tests the environment string building for a web connection.
        /// </summary>
        [Test]
        public void TestBuildWebEnvironmentString()
        {
            Dictionary<string, string> test = new Dictionary<string, string>();
            test.Add("a", "foo");
            test.Add("b", "bar");
            test.Add("SESSION_KEY", "12345");

            string result = ConnectionUtils.BuildWebEnvironmentString(test);
            Assert.AreEqual("A%3dfoo%3aB%3dbar", result);
        }

        /// <summary>
        /// Tests the boolean parsing method
        /// </summary>
        [Test]
        public void GetBooleanValueTest_ValueIsBoolean()
        {
            var dataset = new DataTable();
            dataset.Columns.Add("column", typeof(bool));
            var row = dataset.NewRow();

            row["column"] = true;
            Assert.IsTrue(ConnectionUtils.GetBooleanValue(row, "column", true));

            row["column"] = false;
            Assert.IsFalse(ConnectionUtils.GetBooleanValue(row, "column", true));
        }

        /// <summary>
        /// Tests the boolean parsing method
        /// </summary>
        [Test]
        public void GetBooleanValueTest_ValueIsInteger()
        {
            var dataset = new DataTable();
            dataset.Columns.Add("column", typeof(int));
            var row = dataset.NewRow();

            row["column"] = 1;
            Assert.IsTrue(ConnectionUtils.GetBooleanValue(row, "column", true));

            row["column"] = 0;
            Assert.IsFalse(ConnectionUtils.GetBooleanValue(row, "column", true));

            row["column"] = 2;
            Assert.IsTrue(ConnectionUtils.GetBooleanValue(row, "column", true));

            row["column"] = -1;
            Assert.IsTrue(ConnectionUtils.GetBooleanValue(row, "column", true));
        }

        /// <summary>
        /// Tests the boolean parsing method
        /// </summary>
        [Test]
        public void GetBooleanValueTest_ValueIsString_TorF()
        {
            var dataset = new DataTable();
            dataset.Columns.Add("column", typeof(string));
            var row = dataset.NewRow();

            row["column"] = "T";
            Assert.IsTrue(ConnectionUtils.GetBooleanValue(row, "column", true));

            row["column"] = "F";
            Assert.IsFalse(ConnectionUtils.GetBooleanValue(row, "column", true));

            row["column"] = "some other string";
            Assert.IsFalse(ConnectionUtils.GetBooleanValue(row, "column", false));

            row["column"] = "some other string";
            Assert.IsTrue(ConnectionUtils.GetBooleanValue(row, "column", true));
        }

        /// <summary>
        /// Tests the boolean parsing method
        /// </summary>
        [Test]
        public void GetBooleanValueTest_ValueIsString_TrueOrFalse()
        {
            var dataset = new DataTable();
            dataset.Columns.Add("column", typeof(string));
            var row = dataset.NewRow();

            row["column"] = "True";
            Assert.IsTrue(ConnectionUtils.GetBooleanValue(row, "column", true));

            row["column"] = "False";
            Assert.IsFalse(ConnectionUtils.GetBooleanValue(row, "column", true));

            row["column"] = "some other string";
            Assert.IsTrue(ConnectionUtils.GetBooleanValue(row, "column", true));

            row["column"] = "some other string";
            Assert.IsFalse(ConnectionUtils.GetBooleanValue(row, "column", false));
        }

        /// <summary>
        /// Tests the boolean parsing method
        /// </summary>
        [Test]
        public void GetBooleanValueTest_ValueIsString_Integer()
        {
            var dataset = new DataTable();
            dataset.Columns.Add("column", typeof(string));
            var row = dataset.NewRow();

            row["column"] = "1";
            Assert.IsTrue(ConnectionUtils.GetBooleanValue(row, "column", true));

            row["column"] = "0";
            Assert.IsFalse(ConnectionUtils.GetBooleanValue(row, "column", true));

            row["column"] = "2";
            Assert.IsTrue(ConnectionUtils.GetBooleanValue(row, "column", true));

            row["column"] = "-1";
            Assert.IsTrue(ConnectionUtils.GetBooleanValue(row, "column", true));
        }

        /// <summary>
        /// Tests the boolean parsing method
        /// </summary>
        [Test]
        public void GetBooleanValueTest_ColumnDoesNotExist()
        {
            var dataset = new DataTable();
            var row = dataset.NewRow();
            Assert.IsTrue(ConnectionUtils.GetBooleanValue(row, "column", true));
            Assert.IsFalse(ConnectionUtils.GetBooleanValue(row, "column", false));
        }

        /// <summary>
        /// Tests the boolean parsing method
        /// </summary>
        [Test]
        public void GetBooleanValueTest_ValueIsNull()
        {
            var dataset = new DataTable();
            dataset.Columns.Add("column", typeof(string));
            var row = dataset.NewRow();
            Assert.IsTrue(ConnectionUtils.GetBooleanValue(row, "column", true));
            Assert.IsFalse(ConnectionUtils.GetBooleanValue(row, "column", false));
        }

        /// <summary>
        /// Tests the boolean parsing method
        /// </summary>
        [Test]
        public void GetBooleanValueTest_ValueIsInvalidType()
        {
            var dataset = new DataTable();
            dataset.Columns.Add("column", typeof(object));
            var row = dataset.NewRow();
            Assert.IsTrue(ConnectionUtils.GetBooleanValue(row, "column", true));
            Assert.IsFalse(ConnectionUtils.GetBooleanValue(row, "column", false));
        }
    }
}