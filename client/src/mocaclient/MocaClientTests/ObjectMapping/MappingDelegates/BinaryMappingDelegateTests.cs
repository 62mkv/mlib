using NUnit.Framework;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;

namespace RedPrairie.MOCA.Client.Tests.ObjectMapping
{
    /// <summary>
    /// Unit tests for the <see cref="BinaryMappingDelegate"> class.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>06/16/2008</date>
    [TestFixture]
    public class BinaryMappingDelegateTests
    {
        /// <summary>
        /// Tests the BinaryMappingTarget class with default data
        /// </summary>
        [Test]
        public void GeneralBinaryMappingDelegateTest()
        {
            BinaryMappingTarget obj = new BinaryMappingTarget();
            BinaryMappingDelegate del = new BinaryMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof (BinaryMappingTarget)));

            del.SetValue(obj, MocaType.STRING, "1234");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(new byte[] { 49, 50, 51, 52 }, obj.Data);
        }

        /// <summary>
        /// Tests the BinaryMappingTarget class with default data in the proper
        /// SetValue method for binary values
        /// </summary>
        [Test]
        public void BinMethodBinaryMappingDelegateTest()
        {
            byte[] data = new byte[] {49, 50, 51, 52};
            BinaryMappingTarget obj = new BinaryMappingTarget();
            BinaryMappingDelegate del = new BinaryMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(BinaryMappingTarget)));

            del.SetValue(obj, data);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(data, obj.Data);
        }

        /// <summary>
        /// Tests the  SetNull meothod of the BinaryMappingTarget class.
        /// </summary>
        [Test]
        public void NullBinaryMappingDelegateTest()
        {
            //Set the object to "true" to ensure it isn't reset
            BinaryMappingTarget obj = new BinaryMappingTarget();
            BinaryMappingDelegate del = new BinaryMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof (BinaryMappingTarget)));

            del.SetNull(obj);

            Assert.IsNull(obj.Data);
        }

        /// <summary>
        /// Tests the  SetNull meothod of the BinaryMappingDelegate class when the column
        /// has a default value defined.
        /// </summary>
        [Test]
        public void DefaultBinaryMappingDelegateTest()
        {
            BinaryMappingTarget obj = new BinaryMappingTarget();
            ColumnMap map = MappingUtils.CreateColumnMap("Data", typeof (BinaryMappingTarget));
            map.DefaultValue = new byte[] {22 , 33};
            BinaryMappingDelegate del = new BinaryMappingDelegate(map);

            del.SetNull(obj);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(new byte[] {22 , 33}, obj.Data);
        }

        /// <summary>
        /// A test class for setting the string target
        /// </summary>
        private class BinaryMappingTarget
        {
            private byte[] data;

            public byte[] Data
            {
                get { return data; }
                set { data = value; }
            }
        }
    }
}