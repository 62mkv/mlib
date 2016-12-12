using System;
using System.Collections.Generic;
using System.IO;
using NUnit.Framework;
using RedPrairie.MOCA.Client.Encoding;
using RedPrairie.MOCA.Client.ObjectMapping;

namespace RedPrairie.MOCA.Client.Tests.ObjectMapping
{
    /// <summary>
    /// Unit tests for the <see cref="ObjectMappingDecoder"> class.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>06/16/2008</date>
    [TestFixture]
    public class ObjectMappingDecoderTests
    {
        /// <summary>
        /// Tests creation of a new instance of the ObjectMappingDecoder class
        /// </summary>
        [Test]
        public void DefaultObjectMappingDecoderTest()
        {
            ObjectMappingDecoder decoder = CreateDecoder(typeof(TestReflectionClass), null);
            List<TestReflectionClass> data = decoder.DecodeCollection() as List<TestReflectionClass>;

            Assert.IsNotNull(data);
            Assert.AreEqual(1, data.Count);

            TestReflectionClass item = data[0];
            Assert.IsNotNull(item);
            Assert.AreEqual("abcdefg", item.TestString);
            Assert.AreEqual(1234, item.TestInt);
            Assert.AreEqual(new DateTime(2008, 1, 1), item.TestDateTime);
        }

        /// <summary>
        /// Tests creation of a new instance of the ObjectMappingDecoder class
        /// </summary>
        [Test]
        public void UnmappedPropertyObjectMappingDecoderTest()
        {
            ObjectMappingDecoder decoder = CreateDecoder(typeof(TestReflectionClass), "ExtraData");
            List<TestReflectionClass> data = decoder.DecodeCollection() as List<TestReflectionClass>;

            Assert.IsNotNull(data);
            Assert.AreEqual(1, data.Count);

            TestReflectionClass item = data[0];
            Assert.IsNotNull(item);
            Assert.AreEqual("abcdefg", item.TestString);
            Assert.AreEqual(1234, item.TestInt);
            Assert.AreEqual(new DateTime(2008, 1, 1), item.TestDateTime);

            //Check extra data
            Assert.AreEqual(1, item.ExtraData.Count);
            Assert.AreEqual("nothing", item.ExtraData["NotFound"]);
        }

        /// <summary>
        /// Tests creation of a new instance of the ObjectMappingDecoder class
        /// </summary>
        [Test]
        public void DictionaryMappingObjectMappingDecoderTest()
        {
            ObjectMappingDecoder decoder = CreateDictionaryDecoder(typeof(TestReflectionClass), "ExtraData");
            Dictionary<string, TestReflectionClass> data = decoder.DecodeCollection() as Dictionary<string, TestReflectionClass>;

            Assert.IsNotNull(data);
            Assert.AreEqual(1, data.Count);

            TestReflectionClass item = data["abcdefg"];
            Assert.IsNotNull(item);
            Assert.AreEqual("abcdefg", item.TestString);
            Assert.AreEqual(1234, item.TestInt);
            Assert.AreEqual(new DateTime(2008, 1, 1), item.TestDateTime);

            //Check extra data
            Assert.AreEqual(1, item.ExtraData.Count);
            Assert.AreEqual("nothing", item.ExtraData["NotFound"]);
        }

        /// <summary>
        /// Creates a <see cref="ObjectMappingDecoder"/> decoder based on the
        /// specified <paramref name="mappingClass"/>.
        /// </summary>
        /// <param name="mappingClass">The mapping class type.</param>
        /// <param name="unmappedPropertyName">Name of the unmapped property.</param>
        /// <returns>
        /// A new <see cref="ObjectMappingDecoder"/>.
        /// </returns>
        private static ObjectMappingDecoder CreateDecoder(Type mappingClass, string unmappedPropertyName)
        {
            //Create the stream
            MemoryStream dataStream = new MemoryStream(EncodingBase.GetEncoding().GetBytes(DATA));

            //Create the mapper
            ReflectionColumnMapper mapper = new ReflectionColumnMapper();
            MappingData mappingData = mapper.GetMappingInformation(mappingClass);

            if (!String.IsNullOrEmpty(unmappedPropertyName))
            {
                mappingData.UnmappedColumnsProperty = unmappedPropertyName;                  
            }

            IObjectResolver resolver = new DefaultObjectResolver();

            return new ObjectMappingDecoder(dataStream, null, mappingData, resolver);
        }

        /// <summary>
        /// Creates a <see cref="ObjectMappingDecoder"/> decoder based on the
        /// specified <paramref name="mappingClass"/>.
        /// </summary>
        /// <param name="mappingClass">The mapping class type.</param>
        /// <param name="unmappedPropertyName">Name of the unmapped property.</param>
        /// <returns>
        /// A new <see cref="ObjectMappingDecoder"/>.
        /// </returns>
        private static ObjectMappingDecoder CreateDictionaryDecoder(Type mappingClass, string unmappedPropertyName)
        {
            //Create the stream
            MemoryStream dataStream = new MemoryStream(EncodingBase.GetEncoding().GetBytes(DATA));

            //Create the mapper
            ReflectionColumnMapper mapper = new ReflectionColumnMapper();
            MappingData mappingData = mapper.GetMappingInformation(mappingClass);

            if (!String.IsNullOrEmpty(unmappedPropertyName))
            {
                mappingData.UnmappedColumnsProperty = unmappedPropertyName;
            }

            mappingData.CollectionType = typeof (Dictionary<,>);
            mappingData.KeyProperty = "TestString";

            IObjectResolver resolver = new DefaultObjectResolver();

            return new ObjectMappingDecoder(dataStream, null, mappingData, resolver);
        }

        private const string DATA =
            "V104^160^-1^0^0^^1^4^sids^~TestString~7~7~TestString~TestString~TestInt~4~4~TestInt~TestInt~TestDateTime~14~14"+
            "~TestDateTime~TestDateTime~NotFound~7~7~NotFound~NotFound~^45^S7^abcdefgI4^1234D14^20080101000000S7^nothing";
    }
}