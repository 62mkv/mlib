using System.Collections.Generic;
using NUnit.Framework;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;

namespace RedPrairie.MOCA.Client.Tests.ObjectMapping
{
    /// <summary>
    /// Unit tests for the <see cref="EnumMappingDelegate"> class.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>06/16/2008</date>
    [TestFixture]
    public class EnumMappingDelegateTests
    {
        /// <summary>
        /// Tests the EnumMappingTarget class with default data
        /// </summary>
        [Test]
        public void StringValueEnumMappingDelegateTest()
        {
            EnumMappingTarget obj = new EnumMappingTarget();
            EnumMappingDelegate del = new EnumMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof (EnumMappingTarget)));

            del.SetValue(obj, MocaType.STRING, "1");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(TestType.First, obj.Data);
        }

        /// <summary>
        /// Tests the EnumMappingTarget class with a string of the value name
        /// </summary>
        [Test]
        public void StringEnumMappingDelegateTest()
        {
            EnumMappingTarget obj = new EnumMappingTarget();
            EnumMappingDelegate del = new EnumMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(EnumMappingTarget)));

            del.SetValue(obj, MocaType.STRING, "second");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(TestType.Second, obj.Data);


            del.SetValue(obj, MocaType.STRING, "SECOND");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(TestType.Second, obj.Data);
        }

        /// <summary>
        /// Tests the EnumMappingTarget class with a a custom hash table defined
        /// </summary>
        [Test]
        public void CustomMapEnumMappingDelegateTest()
        {
            Dictionary<string, object> customMap = new Dictionary<string, object>();
            customMap.Add("S", TestType.Second);

            EnumMappingTarget obj = new EnumMappingTarget();
            EnumMappingDelegate del = new EnumMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(EnumMappingTarget)), customMap);

            del.SetValue(obj, MocaType.STRING, "S");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(TestType.Second, obj.Data);
        }

        /// <summary>
        /// Tests the EnumMappingTarget class with default data as an int type
        /// </summary>
        [Test]
        public void StringIntEnumMappingDelegateTest()
        {
            EnumMappingTarget obj = new EnumMappingTarget();
            EnumMappingDelegate del = new EnumMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(EnumMappingTarget)));

            del.SetValue(obj, MocaType.INTEGER, "1");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(TestType.First, obj.Data);
        }


        /// <summary>
        /// Tests the EnumMappingDelegate class with an invalid value.
        /// </summary>
        [Test]
        public void NotFoundStringMappingDelegateTest()
        {
            //Set the object to "Second" to ensure it isn't reset
            EnumMappingTarget obj = new EnumMappingTarget();
            obj.Data = TestType.Second;
            EnumMappingDelegate del = new EnumMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(EnumMappingTarget)));

            del.SetValue(obj, MocaType.STRING, "blslslskd");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(TestType.Second, obj.Data);
        }

        /// <summary>
        /// Tests the EnumMappingDelegate class with an invalid value.
        /// </summary>
        [Test]
        public void NotFoundIntMappingDelegateTest()
        {
            //Set the object to "Second" to ensure it isn't reset
            EnumMappingTarget obj = new EnumMappingTarget();
            obj.Data = TestType.Second;
            EnumMappingDelegate del = new EnumMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(EnumMappingTarget)));

            del.SetValue(obj, MocaType.INTEGER, "99");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(TestType.Second, obj.Data);
        }

        /// <summary>
        /// Tests the  SetNull meothod of the EnumMappingTarget class.
        /// </summary>
        [Test]
        public void NullEnumMappingDelegateTest()
        {
            //Set the object to "Second" to ensure it isn't reset
            EnumMappingTarget obj = new EnumMappingTarget();
            obj.Data = TestType.Second;
            EnumMappingDelegate del = new EnumMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof (EnumMappingTarget)));

            del.SetNull(obj);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(TestType.Second, obj.Data);
        }

        /// <summary>
        /// Tests the  SetNull meothod of the EnumMappingDelegate class when the column
        /// has a default value defined.
        /// </summary>
        [Test]
        public void DefaultEnumMappingDelegateTest()
        {
            EnumMappingTarget obj = new EnumMappingTarget();
            ColumnMap map = MappingUtils.CreateColumnMap("Data", typeof (EnumMappingTarget));
            map.DefaultValue = TestType.Third;
            EnumMappingDelegate del = new EnumMappingDelegate(map);

            del.SetNull(obj);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(TestType.Third, obj.Data);
        }

        /// <summary>
        /// A test class for setting the string target
        /// </summary>
        private class EnumMappingTarget
        {
            private TestType data = TestType.First;

            public TestType Data
            {
                get { return data; }
                set { data = value; }
            }
        }

        private enum TestType
        {
            First = 1,
            Second = 2,
            Third = 3
        }
    }
}