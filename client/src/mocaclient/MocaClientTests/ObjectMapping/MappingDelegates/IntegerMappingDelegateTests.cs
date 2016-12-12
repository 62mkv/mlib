using NUnit.Framework;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;

namespace RedPrairie.MOCA.Client.Tests.ObjectMapping
{
    /// <summary>
    /// Unit tests for the <see cref="IntegerMappingDelegate"> class.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>06/16/2008</date>
    [TestFixture]
    public class IntegerMappingDelegateTests
    {
        /// <summary>
        /// Tests the IntegerMappingTarget class with default data
        /// </summary>
        [Test]
        public void GeneralIntegerMappingDelegateTest()
        {
            IntegerMappingTarget obj = new IntegerMappingTarget();
            IntegerMappingDelegate del = new IntegerMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof (IntegerMappingTarget)));

            del.SetValue(obj, MocaType.STRING, "12345");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(12345, obj.Data);
        }

        /// <summary>
        /// Tests the  SetNull meothod of the IntegerMappingDelegate class.
        /// </summary>
        [Test]
        public void NullIntegerMappingDelegateTest()
        {
            //Set the object to "-1" to ensure it isn't reset
            IntegerMappingTarget obj = new IntegerMappingTarget();
            obj.Data = -1;
            IntegerMappingDelegate del = new IntegerMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof (IntegerMappingTarget)));

            del.SetNull(obj);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(-1, obj.Data);
        }

        /// <summary>
        /// Tests the  SetNull meothod of the IntegerMappingDelegate class when the column
        /// has a default value defined.
        /// </summary>
        [Test]
        public void DefaultIntegerMappingDelegateTest()
        {
            IntegerMappingTarget obj = new IntegerMappingTarget();
            ColumnMap map = MappingUtils.CreateColumnMap("Data", typeof (IntegerMappingTarget));
            map.DefaultValue = -239;
            IntegerMappingDelegate del = new IntegerMappingDelegate(map);

            del.SetNull(obj);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(-239, obj.Data);
        }

        /// <summary>
        /// A test class for setting the string target
        /// </summary>
        private class IntegerMappingTarget
        {
            private int data;

            public int Data
            {
                get { return data; }
                set { data = value; }
            }
        }
    }
}