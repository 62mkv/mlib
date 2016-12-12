using System;
using NUnit.Framework;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;

namespace RedPrairie.MOCA.Client.Tests.ObjectMapping
{
    /// <summary>
    /// Unit tests for the <see cref="StringMappingDelegate"> class.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>06/16/2008</date>
    [TestFixture]
    public class StringMappingDelegateTests
    {
        /// <summary>
        /// Tests the StringMappingDelegate class with default data
        /// </summary>
        [Test]
        public void GeneralStringMappingDelegateTest()
        {
            const string data = "I am SKYNET";
            StringMappingTarget obj = new StringMappingTarget();
            StringMappingDelegate del = new StringMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(StringMappingTarget)));

            del.SetValue(obj, MocaType.STRING, data);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(data, obj.Data);
        }

        /// <summary>
        /// Tests the StringMappingDelegate class with a DateTime column value
        /// </summary>
        [Test]
        public void DateStringMappingDelegateTest()
        {
            StringMappingTarget obj = new StringMappingTarget();
            StringMappingDelegate del = new StringMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(StringMappingTarget)));

            del.SetValue(obj, MocaType.DATETIME, "20080616000000");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(new DateTime(2008, 6, 16).ToString(), obj.Data);
        }

        /// <summary>
        /// Tests the  SetNull meothod of the StringMappingDelegate class.
        /// </summary>
        [Test]
        public void NullStringMappingDelegateTest()
        {
            StringMappingTarget obj = new StringMappingTarget();
            StringMappingDelegate del = new StringMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(StringMappingTarget)));

            del.SetNull(obj);

            Assert.IsNull(obj.Data);
        }

        /// <summary>
        /// Tests the  SetNull meothod of the StringMappingDelegate class when the column
        /// has a default value defined.
        /// </summary>
        [Test]
        public void DefaultStringMappingDelegateTest()
        {
            const string defaultValue = "Foo";
            StringMappingTarget obj = new StringMappingTarget();
            ColumnMap map = MappingUtils.CreateColumnMap("Data", typeof (StringMappingTarget));
            map.DefaultValue = defaultValue;
            StringMappingDelegate del = new StringMappingDelegate(map);
                

            del.SetNull(obj);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(defaultValue, obj.Data);
        }

        /// <summary>
        /// A test class for setting the string target
        /// </summary>
        private class StringMappingTarget
        {
            private string data;

            public string Data
            {
                get { return data; }
                set { data = value; }
            }
        }
    }
}