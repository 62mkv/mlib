using System;
using System.Collections.Generic;
using NUnit.Framework;
using RedPrairie.MOCA.Client.Encoding;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client.Tests.Base
{
    /// <summary>
    /// Unit tests for StringReplacer utility class.
    /// </summary>
    [TestFixture]
    public class StringReplacerTests
    {
        /// <summary>
        /// Tests the single char delimiter replacement.
        /// </summary>
        [Category("BuildTests")]
        [Test]
        public void TestSingleCharDelimiter()
        {
            Dictionary<string, object> mapping = new Dictionary<string, object>();
            mapping.Add("a", "value of a");
            mapping.Add("b", "value of b");
            mapping.Add("abcdefghijk", "value number 3");
            StringReplacer replacer = new StringReplacer('%', mapping);

            Assert.AreEqual("", replacer.Translate(""));
            Assert.AreEqual("Test", replacer.Translate("Test"));
            Assert.AreEqual("%nothere%", replacer.Translate("%nothere%"));
            Assert.AreEqual("%notclosed", replacer.Translate("%notclosed"));
            Assert.AreEqual("value of a", replacer.Translate("%a%"));
            Assert.AreEqual("value of b", replacer.Translate("%b%"));
            Assert.AreEqual("value of avalue of b", replacer.Translate("%a%%b%"));
            Assert.AreEqual("This is value number 3, mkay?", replacer.Translate("This is %abcdefghijk%, mkay?"));
        }

        /// <summary>
        /// Tests the bytes encoder.
        /// </summary>
        [Test]
        public void TestBytesEncoder()
        {
            EncoderTest coder = new EncoderTest();

            string strTest = "This is a Test String";

            Assert.AreEqual(strTest, coder.GenerateString(coder.GenerateBytes(strTest)));
        }

        /// <summary>
        /// Tests the date time converter.
        /// </summary>
        [Test]
        public void TestDateTimeConverter()
        {
            EncoderTest coder = new EncoderTest();

            DateTime result = new DateTime(2007, 4, 24, 22, 55, 22);
            string strTest = "20070424225522";

            Assert.AreEqual(result, coder.TestDateTimeEncoding(strTest));
        }

        /// <summary>
        /// Tests the conversion of a binary value to a string.
        /// </summary>
        [Test]
        public void TestStringFormatForBinary()
        {
            int value = 5000;

            string strValue = value.ToString("x8");

            Assert.AreEqual(strValue.Length, 8);
        }

        private class EncoderTest : EncodingBase
        {
            public byte[] GenerateBytes(string baseString)
            {
                return ToByteArray(baseString);
            }

            public string GenerateString(byte[] bytes)
            {
                return ToCharArray(bytes);
            }

            public DateTime TestDateTimeEncoding(string dateTime)
            {
                return ConvertDateTime(dateTime);
            }
        }
    }
}