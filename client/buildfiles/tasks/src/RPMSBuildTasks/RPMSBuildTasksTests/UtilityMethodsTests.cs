using System;
using System.Security.Cryptography;
using System.Text;
using NUnit.Framework;
using RedPrairie.MSBuild.Tasks;

namespace RPMSBuildTasks.Tests
{
    /// <summary>
    /// Unit tests for the <see cref="Utils"> class.
    /// </summary>
    /// <author>Piessens, Daniel</author>
    /// <date>03/14/2008</date>
    [TestFixture]
    public class UtilityMethodsTests
    {
        [Test]
        public void TestGetNumberFromString()
        {
            int defaultValue = 1;
            int correctValue = 2007;

            Assert.AreEqual(correctValue, Utils.GetNumberFromString("2007", defaultValue));
            Assert.AreEqual(correctValue, Utils.GetNumberFromString(" 2007 ", defaultValue));
            Assert.AreEqual(defaultValue, Utils.GetNumberFromString(null, defaultValue));
            Assert.AreEqual(defaultValue, Utils.GetNumberFromString("", defaultValue));
            Assert.AreEqual(defaultValue, Utils.GetNumberFromString("*", defaultValue));
            Assert.AreEqual(defaultValue, Utils.GetNumberFromString("1a1", defaultValue));
        }

        [Test]
        public void TestGetFileVersion()
        {
            Version defaultVersion = new Version(1, 0, 0, 0);

            Assert.AreEqual(defaultVersion, Utils.GetFileVersion("Foo"));
            Assert.AreEqual(defaultVersion, Utils.GetFileVersion("2008"));
            Assert.AreEqual(defaultVersion, Utils.GetFileVersion("2008.1"));
            Assert.AreEqual(new Version(2008, 2, 1, 0), Utils.GetFileVersion("2008.2.1"));
            Assert.AreEqual(new Version(2008, 2, 0, 0), Utils.GetFileVersion("2008.2.0a13"));
            Assert.AreEqual(new Version(2008, 2, 1, 0), Utils.GetFileVersion("2008.2.1.0"));
            Assert.AreEqual(new Version(2008, 2, 1, 0), Utils.GetFileVersion("2008.2.1.0a13"));
            Assert.AreEqual(defaultVersion, Utils.GetFileVersion("1.*.*"));
        }

        public void TestHashCode()
        {
            string fileName = "RedPrairie.MCS.McsDefines.dll";

            Assert.AreEqual(36, ComputeFileID("DevExpress.ExtraGrid3.dll").Length);
            Assert.AreEqual(36, ComputeFileID("RedPrairie.MCS.McsDefines.dll").Length);

            string firstRun = ComputeFileID(fileName);
            string secondRun = ComputeFileID(fileName);
            Console.WriteLine(firstRun);
            Assert.AreEqual(firstRun, secondRun);
        }

        private string ComputeFileID(string fileName)
        {
            MD5CryptoServiceProvider hashProvider = new MD5CryptoServiceProvider();
            byte[] hash = hashProvider.ComputeHash(Encoding.UTF8.GetBytes(fileName));
            StringBuilder result = new StringBuilder(36);

            for (int i = 0; i < 16; i++)
            {

                if (i > 2 && i < 12 && i % 2 == 0)
                {
                    result.Append("-");
                }

                result.AppendFormat("{0:x2}", hash[i]);
            }
            
            return result.ToString();
        }
    }
}
