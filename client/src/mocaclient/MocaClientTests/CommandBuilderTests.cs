using NUnit.Framework;
using RedPrairie.MOCA.Client.Crypto;
using RedPrairie.MOCA.Client.Encoding;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client.Tests.Base
{
    /// <summary>
    /// Tests the command builder to ensure the protocol is parsed correctly
    /// </summary>
    [Category("BuildTests")]
    [TestFixture]
    public class CommandBuilderTests : EncodingBase
    {
        internal const string COMMAND_STRING = "publish data where a=1";
        internal const string ENV_STRING = "LOCALE_ID=US_ENGLISH";
        internal const string ENV_STRING2 = "LOCALE_ID=US_ENGLISH:MOCA_APPL_ID=WINMSQL";


        /// <summary>
        /// Tests a protocol version 104 command.
        /// </summary>
        [Test]
        public void TestV104Command()
        {
            string compareString =
                string.Format("V104^69^2^^^62^^^~0~~0~~-1^LOCALE_ID=US_ENGLISH^001026^{0}", COMMAND_STRING);

            CommandBuilder builder = new CommandBuilder(ENV_STRING);

            //Test if version 100 is passed
            TestPermutation(builder, COMMAND_STRING, 104, compareString);
        }

        /// <summary>
        /// Tests Tests a protocol version 104 command with RPBF encryption.
        /// </summary>
        [Test]
        public void TestV104RPBFCommand()
        {
            string compareString =
                "V104^73^6^rpbf^^62^_\\}4{x7vw':RAALQ]WL]Q+BKF_U[QWLhvY_I@D]GUSGUA_]K_]\0" + (char) 15;


            CommandBuilder builder = new CommandBuilder(new RPBFEncryptionStrategy(), ENV_STRING);

            //Test if version 100 is passed
            TestPermutation(builder, COMMAND_STRING, 104, compareString);
        }

        /// <summary>
        /// Tests a protocol version 104 command encrypted with blowfish.
        /// </summary>
        [Test]
        public void TestV104BlowfishCommand()
        {
            byte[] data = {
                              0x56, 0x31, 0x30, 0x34, 0x5e, 0x31, 0x30, 0x34,
                              0x5e, 0x31, 0x30, 0x5e, 0x62, 0x6c, 0x6f, 0x77,
                              0x66, 0x69, 0x73, 0x68, 0x5e, 0x5e, 0x38, 0x38,
                              0x5e, 0xa6, 0x87, 0x22, 0xa8, 0xdf, 0xaf, 0x39,
                              0x98, 0xaf, 0x23, 0x6f, 0xf2, 0x92, 0x74, 0xbb,
                              0x82, 0xab, 0x64, 0xa9, 0x64, 0x22, 0x99, 0x7f,
                              0x0b, 0xbb, 0x8e, 0x2a, 0x5f, 0x62, 0x35, 0x8f,
                              0x4a, 0x51, 0x0b, 0x07, 0x51, 0x5a, 0x92, 0x4b,
                              0xd0, 0x11, 0x45, 0x2c, 0x8a, 0x37, 0x13, 0xdc,
                              0x2e, 0x9b, 0x3b, 0x61, 0x01, 0x7f, 0x28, 0xce,
                              0x2c, 0xfc, 0xd2, 0x06, 0xbf, 0x99, 0x3b, 0x4a,
                              0x46, 0x0b, 0xe1, 0x65, 0xf0, 0xe1, 0x5c, 0xab,
                              0x3f, 0xb2, 0x48, 0x1b, 0xdd, 0xb4, 0x7f, 0x5e,
                              0x57, 0x81, 0xd3, 0xd8, 0x19, 0xd0, 0x2d, 0xf6,
                              0xfc
                          };


            CommandBuilder builder = new CommandBuilder(new BlowfishEncryptionStrategy(), ENV_STRING2);

            //Test if version 100 is passed
            TestPermutation(builder, COMMAND_STRING, 104, data);
        }


        /// <summary>
        /// Tests a protocol version 103 command.
        /// </summary>
        [Test]
        public void TestV103Command()
        {
            CommandBuilder builder = new CommandBuilder(ENV_STRING);
            string compareString = "V103^65^^~0~~0~~-1^LOCALE_ID=US_ENGLISH^002050^get encryption information";

            TestPermutation(builder, "get encryption information", 103, compareString);
        }
        /// <summary>
        /// Tests a protocol version 102-101 command.
        /// </summary>
        [Test]
        public void TestV101_V102Command()
        {
            CommandBuilder builder = new CommandBuilder(ENV_STRING);
            string compareString = string.Format("V101^000033^^LOCALE_ID=US_ENGLISH^001026^{0}", COMMAND_STRING);

            //Test if version 101 is passed
            TestPermutation(builder, COMMAND_STRING, 101, compareString);

            //Test if version 102 is passed
            compareString = string.Format("V102^000033^^LOCALE_ID=US_ENGLISH^001026^{0}", COMMAND_STRING);
            TestPermutation(builder, COMMAND_STRING, 102, compareString);
        }
        /// <summary>
        /// Tests a protocol version 100 command.
        /// </summary>
        [Test]
        public void TestV100Command()
        {
            CommandBuilder builder = new CommandBuilder(ENV_STRING);
            string compareString = string.Format("000032^LOCALE_ID=US_ENGLISH^001026^{0}", COMMAND_STRING);

            //Test if none is passed
            TestPermutation(builder, COMMAND_STRING, 0, compareString);

            //Test if version 100 is passed
            TestPermutation(builder, COMMAND_STRING, 100, compareString);
        }


        /// <summary>
        /// Tests the permutation of the command built.
        /// </summary>
        /// <param name="builder">The command builder to test with.</param>
        /// <param name="commandName">The command to run</param>
        /// <param name="protocolVersion">The version of the protocol</param>
        /// <param name="compareString">The string to compare the result to.</param>
        private static void TestPermutation(CommandBuilder builder, string commandName, int protocolVersion,
                                            string compareString)
        {
            byte[] command = builder.CreateCommand(commandName,
                                                   protocolVersion, Constants.FLAG_ASCII_COMM);
            byte[] compare = ToByteArray(compareString);

            Assert.AreEqual(compare, command,
                            "Command Bytes do not match:\n Expected Length:{2}\n Actual Length:{3}\n Expected: {0}\n Actual:   {1}",
                            ToCharArray(compare), ToCharArray(command), compare.Length, command.Length);
        }

        /// <summary>
        /// Tests the permutation of the command built.
        /// </summary>
        /// <param name="builder">The command builder to test with.</param>
        /// <param name="commandName">The command to run</param>
        /// <param name="protocolVersion">The version of the protocol</param>
        /// <param name="compareArray">The byte[] to compare the result to.</param>
        private static void TestPermutation(CommandBuilder builder, string commandName, int protocolVersion,
                                            byte[] compareArray)
        {
            byte[] command = builder.CreateCommand(commandName,
                                                   protocolVersion, Constants.FLAG_ASCII_COMM);

            Assert.AreEqual(compareArray, command,
                            "Command Bytes do not match:\n Expected Length:{2}\n Actual Length:{3}\n Expected: {0}\n Actual:   {1}",
                            ToCharArray(compareArray), ToCharArray(command), compareArray.Length, command.Length);
        }
    }
}