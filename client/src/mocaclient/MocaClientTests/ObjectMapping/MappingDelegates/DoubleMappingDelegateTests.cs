using NUnit.Framework;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;

namespace RedPrairie.MOCA.Client.Tests.ObjectMapping
{
    /// <summary>
    /// Unit tests for the <see cref="DoubleMappingDelegate"> class.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>06/16/2008</date>
    [TestFixture]
    public class DoubleMappingDelegateTests
    {
        /// <summary>
        /// Tests the DoubleMappingTarget class with default data
        /// </summary>
        [Test]
        public void GeneralDoubleMappingDelegateTest()
        {
            DoubleMappingTarget obj = new DoubleMappingTarget();
            DoubleMappingDelegate del = new DoubleMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof (DoubleMappingTarget)));

            del.SetValue(obj, MocaType.STRING, "5.0");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(5.0, obj.Data);
        }

        /// <summary>
        /// Tests the DoubleMappingTarget class with default data
        /// </summary>
        [Test]
        public void GeneralDoubleMappingDelegateTestNoDecimalPoint()
        {
            DoubleMappingTarget obj = new DoubleMappingTarget();
            DoubleMappingDelegate del = new DoubleMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(DoubleMappingTarget)));

            del.SetValue(obj, MocaType.STRING, "12345678");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(12345678.0, obj.Data);
        }

        /// <summary>
        /// Tests the  SetNull meothod of the DoubleMappingTarget class.
        /// </summary>
        [Test]
        public void NullDoubleMappingDelegateTest()
        {
            //Set the object to "999.1" to ensure it isn't reset
            DoubleMappingTarget obj = new DoubleMappingTarget();
            obj.Data = 999.1;
            DoubleMappingDelegate del = new DoubleMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof (DoubleMappingTarget)));

            del.SetNull(obj);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(999.1, obj.Data);
        }

        /// <summary>
        /// Tests the  SetNull meothod of the DoubleMappingDelegate class when the column
        /// has a default value defined.
        /// </summary>
        [Test]
        public void DefaultDoubleMappingDelegateTest()
        {
            DoubleMappingTarget obj = new DoubleMappingTarget();
            ColumnMap map = MappingUtils.CreateColumnMap("Data", typeof (DoubleMappingTarget));
            map.DefaultValue = 123.123;
            DoubleMappingDelegate del = new DoubleMappingDelegate(map);

            del.SetNull(obj);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(123.123, obj.Data);
        }

        /// <summary>
        /// A test class for setting the string target
        /// </summary>
        private class DoubleMappingTarget
        {
            private double data;

            public double Data
            {
                get { return data; }
                set { data = value; }
            }
        }
    }
}