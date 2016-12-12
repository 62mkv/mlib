using System;
using NUnit.Framework;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;

namespace RedPrairie.MOCA.Client.Tests.ObjectMapping
{
    /// <summary>
    /// Unit tests for the <see cref="DateMappingDelegate"> class.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>06/16/2008</date>
    [TestFixture]
    public class DateMappingDelegateTests
    {
        private static readonly DateTime DEFAULT_DATE = new DateTime(2008, 6, 16);
        private const string DEFAULT_DATE_STRING = "20080616000000";

        /// <summary>
        /// Tests the DateMappingTarget class with default data
        /// </summary>
        [Test]
        public void GeneralDateMappingDelegateTest()
        {
            DateMappingTarget obj = new DateMappingTarget();
            DateMappingDelegate del = new DateMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof (DateMappingTarget)));

            del.SetValue(obj, MocaType.DATETIME, DEFAULT_DATE_STRING);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(DEFAULT_DATE, obj.Data);
        }

        /// <summary>
        /// Tests the DateMappingTarget class with a .NET formatted date time string
        /// </summary>
        [Test]
        public void DotNetDateMappingDelegateTest()
        {
            DateMappingTarget obj = new DateMappingTarget();
            DateMappingDelegate del = new DateMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(DateMappingTarget)));

            del.SetValue(obj, MocaType.DATETIME, "06/16/2008 12:00:00 AM");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(DEFAULT_DATE, obj.Data);
        }

        /// <summary>
        /// Tests the DateMappingTarget class with an invalid string
        /// </summary>
        [Test]
        public void InvalidDateMappingDelegateTest()
        {
            DateMappingTarget obj = new DateMappingTarget();
            DateMappingDelegate del = new DateMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(DateMappingTarget)));

            del.SetValue(obj, MocaType.DATETIME, "blslslskdjhf");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(new DateTime(), obj.Data);
        }

        /// <summary>
        /// Tests the  SetNull meothod of the DateMappingTarget class.
        /// </summary>
        [Test]
        public void NullDateMappingDelegateTest()
        {
            DateTime defaultValue = new DateTime(2005, 2, 1);
            //Set the object to "defaultValue" to ensure it isn't reset
            DateMappingTarget obj = new DateMappingTarget();
            obj.Data = defaultValue;

            DateMappingDelegate del = new DateMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof (DateMappingTarget)));

            del.SetNull(obj);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(defaultValue, obj.Data);
        }

        /// <summary>
        /// Tests the  SetNull meothod of the DateMappingDelegate class when the column
        /// has a default value defined.
        /// </summary>
        [Test]
        public void DefaultDateMappingDelegateTest()
        {
            DateTime defaultValue = new DateTime(2005, 2, 2);
            DateMappingTarget obj = new DateMappingTarget();
            ColumnMap map = MappingUtils.CreateColumnMap("Data", typeof (DateMappingTarget));
            map.DefaultValue = defaultValue;
            DateMappingDelegate del = new DateMappingDelegate(map);

            del.SetNull(obj);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(defaultValue, obj.Data);
        }

        /// <summary>
        /// A test class for setting the string target
        /// </summary>
        private class DateMappingTarget
        {
            private DateTime data;

            public DateTime Data
            {
                get { return data; }
                set { data = value; }
            }
        }
    }
}