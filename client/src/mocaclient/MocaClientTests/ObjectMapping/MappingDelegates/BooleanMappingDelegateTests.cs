using NUnit.Framework;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;

namespace RedPrairie.MOCA.Client.Tests.ObjectMapping
{
    /// <summary>
    /// Unit tests for the <see cref="BooleanMappingDelegate"> class.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>06/16/2008</date>
    [TestFixture]
    public class BooleanMappingDelegateTests
    {
        /// <summary>
        /// Tests the BooleanMappingTarget class with default data
        /// </summary>
        [Test]
        public void GeneralBooleanMappingDelegateTest()
        {
            BooleanMappingTarget obj = new BooleanMappingTarget();
            BooleanMappingDelegate del = new BooleanMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(BooleanMappingTarget)));

            del.SetValue(obj, MocaType.STRING, "1");

            Assert.IsNotNull(obj.Data);
            bool something = obj.Data;
            Assert.IsTrue(something);
        }

        /// <summary>
        /// Tests the BooleanMappingTarget class with a mixed case string of "false".
        /// </summary>
        [Test]
        public void BooleanMappingDelegateFalseStringTest()
        {
            BooleanMappingTarget obj = new BooleanMappingTarget();
            BooleanMappingDelegate del = new BooleanMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(BooleanMappingTarget)));

            del.SetValue(obj, MocaType.STRING, "FalsE ");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(false, obj.Data);
        }


        /// <summary>
        /// Tests the  SetNull meothod of the BooleanMappingTarget class.
        /// </summary>
        [Test]
        public void NullBooleanMappingDelegateTest()
        {
            //Set the object to "true" to ensure it isn't reset
            BooleanMappingTarget obj = new BooleanMappingTarget();
            obj.Data = true;

            BooleanMappingDelegate del = new BooleanMappingDelegate(
                MappingUtils.CreateColumnMap("Data", typeof(BooleanMappingTarget)));

            del.SetNull(obj);

            Assert.IsTrue(obj.Data);
        }

        /// <summary>
        /// Tests the  SetNull meothod of the BooleanMappingDelegate class when the column
        /// has a default value defined.
        /// </summary>
        [Test]
        public void DefaultBooleanMappingDelegateTest()
        {
            BooleanMappingTarget obj = new BooleanMappingTarget();
            ColumnMap map = MappingUtils.CreateColumnMap("Data", typeof(BooleanMappingTarget));
            map.DefaultValue = false;
            BooleanMappingDelegate del = new BooleanMappingDelegate(map);
            
            del.SetNull(obj);

            Assert.IsFalse(obj.Data);
        }

        /// <summary>
        /// A test class for setting the string target
        /// </summary>
        private class BooleanMappingTarget
        {
            private bool data;

            public bool Data
            {
                get { return data; }
                set { data = value; }
            }
        }
    }
}