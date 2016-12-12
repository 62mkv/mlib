using NUnit.Framework;
using RedPrairie.MOCA.Client.ObjectMapping;
using System.Collections.Generic;
using System;

namespace RedPrairie.MOCA.Client.Tests.ObjectMapping
{
    /// <summary>
    /// Contains tests for the object mapping items
    /// </summary>
    [TestFixture]
    public class ObjectMappingTests
    {
        [Test]
        public void TestReflectionColumnMapper()
        {
            ReflectionColumnMapper mapper = new ReflectionColumnMapper();
            MappingData data = mapper.GetMappingInformation(typeof(TestReflectionClass));

            Assert.AreEqual(typeof(TestReflectionClass), data.ClassType);
            Assert.AreEqual(data.KeyProperty, null);
            Assert.AreEqual(typeof(List<>), data.CollectionType);
            Assert.AreEqual(data.UnmappedColumnsProperty, null);
            Assert.AreEqual(4, data.PropertyCount);

            //Check Properties
            ColumnMap column = data.GetColumnMap("TestInt");
            Assert.IsNotNull(column);
            Assert.AreEqual(typeof(int), column.DataType);
            Assert.AreEqual("TestInt", column.PropertyName);
            Assert.AreEqual("TestInt", column.ColumnName);

            column = data.GetColumnMap("TestString");
            Assert.IsNotNull(column);
            Assert.AreEqual(typeof(string), column.DataType);
            Assert.AreEqual("TestString", column.PropertyName);
            Assert.AreEqual("TestString", column.ColumnName);

            column = data.GetColumnMap("TestDateTime");
            Assert.IsNotNull(column);
            Assert.AreEqual(typeof(DateTime), column.DataType);
            Assert.AreEqual("TestDateTime", column.PropertyName);
            Assert.AreEqual("TestDateTime", column.ColumnName);

            column = data.GetColumnMap("X");
            Assert.IsNotNull(column);
            Assert.AreEqual(typeof(string), column.DataType);
            Assert.AreEqual("X", column.PropertyName);
            Assert.AreEqual("X", column.ColumnName);

        }
    }
}