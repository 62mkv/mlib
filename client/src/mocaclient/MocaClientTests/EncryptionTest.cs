using System;
using System.IO;
using System.Security.Cryptography;
using NUnit.Framework;
using RedPrairie.MOCA.Client.Crypto;
using RedPrairie.MOCA.Client.Encoding;

namespace RedPrairie.MOCA.Client.Tests.Base
{
    /// <summary>
    /// Tests the different encryption methods
    /// </summary>
    [Category("BuildTests")]
    [TestFixture]
    public class EncryptionTest
    {
        /// <summary>
        /// Tests the RPBF encryption strategy.
        /// </summary>
        [Test]
        public void TestRPBFEncryptionStrategy()
        {
            TestEncryptionTransform(new RPBFEncryptionStrategy());
        }

        /// <summary>
        /// Tests the blowfish encryption strategy.
        /// </summary>
        [Test]
        public void TestBlowfishEncryptionStrategy()
        {
            TestEncryptionTransform(new BlowfishEncryptionStrategy());
        }

        /// <summary>
        /// Tests the triple DES encryption strategy.
        /// </summary>
        [Test]
        public void TestTripleDESEncryptionStrategy()
        {
            TripleDES tripleDES = TripleDES.Create();
            tripleDES.Mode = CipherMode.ECB;
            tripleDES.Padding = PaddingMode.PKCS7;

            TestEncryptionTransform(tripleDES);
        }


        /// <summary>
        /// Tests the encryption transform.
        /// </summary>
        /// <param name="strategy">The strategy.</param>
        private static void TestEncryptionTransform(SymmetricAlgorithm strategy)
        {
            
            //Console.Write("Identical Write Test: ");
            TestIdenticalWrite(strategy);
            //Console.WriteLine("Passed");
            //Console.Write("Full Encryption Test: ");
            TestFullEncryption(strategy);
            //Console.WriteLine("Passed");
            //Console.Write("Mixed Encryption Test: ");
            TestMixedEncryption(strategy);
            //Console.WriteLine("Passed");
        }

        /// <summary>
        /// Tests the full encryption.
        /// </summary>
        /// <param name="strategy">The strategy.</param>
        private static void TestFullEncryption(SymmetricAlgorithm strategy)
        {
            string testString = "This is a test string with `!2;; and a number of other value";

            MemoryStream stream = new MemoryStream();
            CryptoStream crypt = new CryptoStream(stream, strategy.CreateEncryptor(), CryptoStreamMode.Write);

            byte[] data = EncodingBase.GetEncoding().GetBytes(string.Format("{0}^", testString));

            crypt.Write(data, 0, data.Length);
            crypt.FlushFinalBlock();

            //Read the stream back
            stream.Position = 0;
            CryptoStream deCrypt = new CryptoStream(stream, strategy.CreateDecryptor(), CryptoStreamMode.Read);

            MemoryStream str = new MemoryStream();
            while (true)
            {
                int c = deCrypt.ReadByte();
                if (c == -1 || c == '^')
                {
                    break;
                }
                str.WriteByte((Byte) c);
            }

            string result = EncodingBase.GetEncoding().GetString(str.ToArray());

            stream.Close();
            Assert.AreEqual(testString, result);
        }

        /// <summary>
        /// Tests the identical write to ensure correct padding.
        /// </summary>
        /// <param name="strategy">The strategy.</param>
        private static void TestIdenticalWrite(SymmetricAlgorithm strategy)
        {
            string testString = "This is a test string with `!2;; and a number of other value";

            MemoryStream stream = new MemoryStream();
            CryptoStream crypt = new CryptoStream(stream, strategy.CreateEncryptor(), CryptoStreamMode.Write);

            byte[] data = EncodingBase.GetEncoding().GetBytes(testString);
            crypt.Write(data, 0, data.Length);
            crypt.FlushFinalBlock();
            byte[] output1 = stream.ToArray();

            stream.Close();

            stream = new MemoryStream();
            CryptoStream crypt2 = new CryptoStream(stream, strategy.CreateEncryptor(), CryptoStreamMode.Write);
            crypt2.Write(data, 0, data.Length);
            crypt2.FlushFinalBlock();
            byte[] output2 = stream.ToArray();

            Assert.AreEqual(output1, output2);
        }

        /// <summary>
        /// Tests the mixed encryption stream of encrypted and un-encrypted data.
        /// </summary>
        /// <param name="strategy">The strategy.</param>
        private static void TestMixedEncryption(SymmetricAlgorithm strategy)
        {
            //Tests the mix between reading and writing from the base stream and the crypto
            //assume we know headerinfo and tailinfo
            string headerinfo = "ab12342se";
            string testString = "This is a test string with `!2;; and a number of other value1923812 baba";
            string tailinfo = "tailinfo";
            string compareString = string.Format("{0}{1}{2}", headerinfo, testString, tailinfo);

            MemoryStream stream = new MemoryStream();

            //write the header
            byte[] data = EncodingBase.GetEncoding().GetBytes(headerinfo);

            stream.Write(data, 0, data.Length);

            //Write encrypted content
            CryptoStream crypt = new CryptoStream(stream, strategy.CreateEncryptor(), CryptoStreamMode.Write);
            data = EncodingBase.GetEncoding().GetBytes(testString + '^');
            crypt.Write(data, 0, data.Length);
            crypt.FlushFinalBlock();

            //Write tail
            data = EncodingBase.GetEncoding().GetBytes(tailinfo);
            stream.Write(data, 0, data.Length);

            //Reading content
            stream.Position = 0;

            //Get header
            MemoryStream str = new MemoryStream();

            data = new byte[headerinfo.Length];
            stream.Read(data, 0, headerinfo.Length);
            str.Write(data, 0, headerinfo.Length);

            CryptoStream deCrypt = new CryptoStream(stream, strategy.CreateDecryptor(), CryptoStreamMode.Read);


            while (true)
            {
                int c = deCrypt.ReadByte();
                if (c == '^')
                {
                    break;
                }
                else if (c == -1)
                {
                    Assert.Fail("End of Decrypt stream hit");
                }
                str.WriteByte((Byte) c);
            }

            if ((stream.Length - stream.Position) != tailinfo.Length)
            {
                stream.Position -= tailinfo.Length;
            }

            data = new byte[tailinfo.Length];
            stream.Read(data, 0, tailinfo.Length);
            str.Write(data, 0, tailinfo.Length);

            string result = EncodingBase.GetEncoding().GetString(str.ToArray());

            stream.Close();

            Assert.AreEqual(compareString, result);
        }
    }
}