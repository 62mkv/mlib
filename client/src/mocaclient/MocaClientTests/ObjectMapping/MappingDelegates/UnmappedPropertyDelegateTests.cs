using System;
using System.Collections.Generic;
using System.Reflection;
using NUnit.Framework;
using RedPrairie.MOCA.Client.ObjectMapping;
using RedPrairie.MOCA.Client.ObjectMapping.MappingDelegates;

namespace RedPrairie.MOCA.Client.Tests.ObjectMapping
{
    /// <summary>
    /// Unit tests for the <see cref="UnmappedPropertyMapper"> class. This test
    /// class contains more knowledge than it should about how to determine
    /// the property but in this case we really only care that it can set and change
    /// values in the dictionary.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>06/16/2008</date>
    [TestFixture]
    public class UnmappedPropertyMappingDelegateTests
    {
        private const string columnName = "good_column";
        /// <summary>
        /// Tests the UnmappedPropertyMappingTarget class with default data
        /// </summary>
        [Test]
        public void GeneralUnmappedPropertyMappingDelegateTest()
        {
            UnmappedPropertyMappingTarget obj = new UnmappedPropertyMappingTarget();
            MappingDelegate del = GetMapper("Data");
            
            del.SetValue(obj, MocaType.STRING, "1");

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(1, obj.Data.Count);
            Assert.IsTrue(obj.Data.ContainsKey(columnName));
            Assert.AreEqual("1", obj.Data[columnName]);
        }

        /// <summary>
        /// Tests the UnmappedPropertyMappingTarget class with default data with a value
        /// already set in the dictionary
        /// </summary>
        [Test]
        public void DuplicateUnmappedPropertyMappingDelegateTest()
        {
            const string newValue = "different";
            UnmappedPropertyMappingTarget obj = new UnmappedPropertyMappingTarget();
            obj.Data.Add(columnName, "something");

            MappingDelegate del = GetMapper("Data");

            del.SetValue(obj, MocaType.STRING, newValue);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(1, obj.Data.Count);
            Assert.IsTrue(obj.Data.ContainsKey(columnName));
            Assert.AreEqual(newValue, obj.Data[columnName]);
        }

        /// <summary>
        /// Tests the UnmappedPropertyMappingTarget class with null data
        /// </summary>
        [Test]
        public void NullValueUnmappedPropertyMappingDelegateTest()
        {
            UnmappedPropertyMappingTarget obj = new UnmappedPropertyMappingTarget();
            MappingDelegate del = GetMapper("Data");

            del.SetNull(obj);

            Assert.IsNotNull(obj.Data);
            Assert.AreEqual(1, obj.Data.Count);
            Assert.IsTrue(obj.Data.ContainsKey(columnName));
            Assert.IsNull(obj.Data[columnName]);
        }

        /// <summary>
        /// Tests the UnmappedPropertyMappingTarget class with null data class
        /// </summary>
        [Test]
        public void NullCollectionUnmappedPropertyMappingDelegateTest()
        {
            UnmappedPropertyMappingTarget obj = new UnmappedPropertyMappingTarget();
            MappingDelegate del = GetMapper("NullData");

            del.SetValue(obj, MocaType.STRING, "1");

            Assert.IsNull(obj.NullData);
        }

        /// <summary>
        /// Tests the UnmappedPropertyMappingTarget class with an incorrect data type
        /// </summary>
        [Test]
        public void IncorrectCollectionUnmappedPropertyMappingDelegateTest()
        {
            MappingDelegate del = GetMapper("WrongType");
            Assert.IsNull(del);
        }

        /// <summary>
        /// Tests the UnmappedPropertyMappingTarget class with a non-existant property
        /// </summary>
        [Test]
        public void NonExistantUnmappedPropertyMappingDelegateTest()
        {
            MappingDelegate del = GetMapper("Foo");
            Assert.IsNull(del);
        }

        /// <summary>
        /// Gets the mapper. This is the section that duplicates the internal
        /// knowledge area. 
        /// </summary>
        /// <returns>A base <see cref="MappingDelegate"/> class. </returns>
        private static MappingDelegate GetMapper(string propertyName)
        {
            Type classType = typeof (UnmappedPropertyMappingTarget);
            PropertyInfo info = classType.GetProperty(propertyName);

            if (info != null && info.CanRead && 
                info.PropertyType.GetInterface("IDictionary") != null)
            {
                UnmappedPropertyMapper mapper = new UnmappedPropertyMapper(null, info);

                ColumnMap map = new ColumnMap(columnName, typeof(string), columnName);

                //The ususal process is to clone the mapper
                return mapper.Clone(map);
            }

            return null;
        }

        /// <summary>
        /// A test class for setting the string target
        /// </summary>
        private class UnmappedPropertyMappingTarget
        {
            private readonly Dictionary<string, object> data = 
                new Dictionary<string, object>(StringComparer.CurrentCultureIgnoreCase);

            public Dictionary<string, object> Data
            {
                get { return data; }
            }

            public Dictionary<string, object> NullData
            {
                get { return null; }
            }

            public List<object> WrongType
            {
                get { return new List<object>(); }
            }
        }
    }
}