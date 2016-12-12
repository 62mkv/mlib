using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using RedPrairie.MOCA.Client.Encoding.Xml;
using RedPrairie.MOCA.Client.Interfaces;

namespace RedPrairie.MOCA.Client.Tests
{
    /// <summary>
    /// A test fixture for testing the <see cref="XmlRequestEncoder"/> class.
    /// </summary>
    [TestFixture]
    public class XmlRequestEncoderFixture
    {
        /// <summary>
        /// Tests the encode simple request.
        /// </summary>
        [Test]
        public void TestEncodeSimpleRequest()
        {
            var buffer = new StringBuilder();
            XmlRequestEncoder.EncodeRequest("foo bar", null, null, null, null, true, buffer);
            Assert.AreEqual("<moca-request autocommit=\"True\"><query>foo bar</query></moca-request>", buffer.ToString());
        }

        /// <summary>
        /// Tests the encode environment string. Ensures that session key is not added
        /// and that items are escaped properly.
        /// </summary>
        [Test]
        public void TestEncodeEnvironmentString()
        {
            var env = new Dictionary<String, String>
                          {
                              {"a", "AAAA Value"},
                              {"b", "b value"},
                              {"c", ""},
                              {"xml", "<something='&z'>"},
                              {"SESSION_KEY", "1kscn12"}
                          };
            var envString = XmlRequestEncoder.BuildXmlEnvironmentString(env);

            Assert.AreEqual("<var name=\"A\" value=\"AAAA Value\"/>" + 
                            "<var name=\"B\" value=\"b value\"/>" + 
                            "<var name=\"C\" value=\"\"/>" + 
                            "<var name=\"XML\" value=\"&lt;something=&apos;&amp;z&apos;&gt;\"/>" +
                            "<var name=\"SESSION_KEY\" value=\"1kscn12\"/>", 
                            envString);
        }

        /// <summary>
        /// Tests the encode decode complex request.
        /// </summary>
        [Test]
        public void TestEncodeDecodeComplexRequest()
        {
            var buffer = new StringBuilder();

            var env = new Dictionary<String, String> { { "a", "AAAA Value" }, { "b", "b value" }, { "c", "" } };
            var envString = XmlRequestEncoder.BuildXmlEnvironmentString(env);

            XmlRequestEncoder.EncodeRequest("do something where x = @+xxx and @*", "session-00001", envString, null, null, false, buffer);
            Assert.AreEqual("<moca-request autocommit=\"False\"><session id=\"session-00001\"/>" + 
                            "<environment><var name=\"A\" value=\"AAAA Value\"/><var name=\"B\" value=\"b value\"/>" + 
                            "<var name=\"C\" value=\"\"/></environment>"+ 
                            "<query>do something where x = @+xxx and @*</query></moca-request>", 
                            buffer.ToString());
        }

        /// <summary>
        /// Tests the encode decode complex request with arguments.
        /// </summary>
        [Test]
        public void TestEncodeDecodeComplexRequestWithArguments()
        {
            var buffer = new StringBuilder();
            const string foo = null;
            var args = new List<IMocaArgument>
                           {
                               new MocaArgument<string>("arg1", null),
                               new MocaArgument<string>("arg2", "Hello"),
                               new MocaArgument<int>("arg3", 3, MocaOperator.GreaterThan),
                               new MocaArgument<DateTime>("arg4", new DateTime(2009, 11, 23, 12, 22, 22), MocaOperator.LessThanEqual),
                               new MocaArgument<string>("arg5", foo),
                           };

            var env = new Dictionary<String, String> { { "a", "AAAA Value" }, { "b", "b value" }, { "c", "" } };
            var envString = XmlRequestEncoder.BuildXmlEnvironmentString(env);

            XmlRequestEncoder.EncodeRequest("do something where x = @+xxx and @*", "session-00001", envString, null, args, false, buffer);
            Assert.AreEqual("<moca-request autocommit=\"False\"><session id=\"session-00001\"/>" +
                            "<environment><var name=\"A\" value=\"AAAA Value\"/><var name=\"B\" value=\"b value\"/>" +
                            "<var name=\"C\" value=\"\"/></environment>" +
                            "<args>"+
                            "<field name=\"arg1\" type=\"STRING\" oper=\"EQ\" null=\"true\"/>" +
                            "<field name=\"arg2\" type=\"STRING\" oper=\"EQ\">Hello</field>" +
                            "<field name=\"arg3\" type=\"INTEGER\" oper=\"GT\">3</field>" +
                            "<field name=\"arg4\" type=\"DATETIME\" oper=\"LE\">20091123122222</field>" +
                            "<field name=\"arg5\" type=\"STRING\" oper=\"EQ\" null=\"true\"/>" +
                            "</args>" +
                            "<query>do something where x = @+xxx and @*</query></moca-request>",
                            buffer.ToString());
        }
    }
}