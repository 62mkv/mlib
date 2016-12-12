using System;
using System.Data;
using System.IO;
using NUnit.Framework;
using RedPrairie.MOCA.Client.Crypto;
using RedPrairie.MOCA.Client.Encoding;

namespace RedPrairie.MOCA.Client.Tests.Base
{
    /// <summary> 
    /// Tests that ResultsEncoder and ResultsDecoderBase work together in all legal
    /// cases.
    /// </summary>
    [Category("BuildTests")]
    [TestFixture]
    public class ResultsEncoderTests:TestUtilBase
    {
        /// <summary>
        /// Tests the encoder with empty results.
        /// </summary>
        [Test]
        public virtual void EncoderTestEmptyResults()
        {
            DataSet res = new DataSet();
            res.Tables.Add();
            CompareResults(res.Tables[0], MakeCopy(res).Tables[0]);
        }

        /// <summary>
        /// Tests the encoder to ensure metadata with no length works.
        /// </summary>
        [Test]
        public virtual void EncoderTestMetadataOnlyNoLengths()
        {
            DataSet dataSet = new DataSet();
            DataTable res = dataSet.Tables.Add();

            res.Columns.Add("aaaaaaa", MocaType.STRING.Class);
            res.Columns.Add("bbbbbbb", MocaType.BOOLEAN.Class);
            res.Columns.Add("ccccccc", MocaType.DOUBLE.Class);
            res.Columns.Add("ddddddd", MocaType.INTEGER.Class);
            res.Columns.Add("eeeeeee", MocaType.BINARY.Class);
            res.Columns.Add("fffffff", MocaType.DATETIME.Class);
            res.Columns.Add("ggggggg", MocaType.RESULTS.Class);

            CompareResults(dataSet.Tables[0], MakeCopy(dataSet).Tables[0]);
        }

        /// <summary>
        /// Tests the encoder to ensure meta data encoding works correctly.
        /// </summary>
        [Test]
        public virtual void EncoderTestMetadataOnly()
        {
            DataSet res = new DataSet();
            DataTable resTable = res.Tables.Add();
            DefaultColumns(resTable);

            CompareResults(res.Tables[0], MakeCopy(res).Tables[0]);
        }

        /// <summary>
        /// Tests the encoder null data.
        /// </summary>
        [Test]
        public virtual void EncoderTestNullData()
        {
            DataSet res = new DataSet();
            DataTable resTable = res.Tables.Add();
            DefaultColumns(resTable, true);

            resTable.Rows.Add(new object[resTable.Columns.Count]);

            CompareResults(res.Tables[0], MakeCopy(res).Tables[0]);
        }

        /// <summary>
        /// Tests the encoder with a single data row.
        /// </summary>
        [Test]
        public virtual void EncoderTestSingleDataRow()
        {
            DataSet res = new DataSet();
            DataTable resTable = res.Tables.Add();
            DefaultColumns(resTable);

            object[] items = new object[7];

            items[resTable.Columns.IndexOf("aaaaaaa")] = "Hello, World";
            items[resTable.Columns.IndexOf("bbbbbbb")] = true;
            items[resTable.Columns.IndexOf("ccccccc")] = 68.231;
            items[resTable.Columns.IndexOf("ddddddd")] = 123456789;
            byte[] someData = new byte[5000];
            new Random().NextBytes(someData);
            items[resTable.Columns.IndexOf("eeeeeee")] = someData;
            DateTime tempAux = DateTime.Now;
            items[resTable.Columns.IndexOf("fffffff")] = tempAux;
            DataTable sub = new DataTable();
            DefaultColumns(sub);
            items[resTable.Columns.IndexOf("ggggggg")] = sub;
            resTable.Rows.Add(items);

            CompareResults(res.Tables[0], MakeCopy(res).Tables[0]);
        }

        /// <summary>
        /// Tests the encoder with mulitple data rows.
        /// </summary>
        [Test]
        public virtual void EncoderTestMulitpleDataRows()
        {
            DataSet res = new DataSet();
            DataTable resTable = res.Tables.Add();
            DefaultColumns(resTable);
            for (int i = 0; i < 20; i++)
            {
                object[] items = new object[7];
                items[resTable.Columns.IndexOf("aaaaaaa")] = "Hello, World";
                items[resTable.Columns.IndexOf("bbbbbbb")] = true;
                ;
                items[resTable.Columns.IndexOf("ccccccc")] = 68.231;
                items[resTable.Columns.IndexOf("ddddddd")] = 123456789;
                byte[] someData = new byte[5000];
                new Random().NextBytes(someData);
                items[resTable.Columns.IndexOf("eeeeeee")] = someData;
                DateTime tempAux = DateTime.Now;
                items[resTable.Columns.IndexOf("fffffff")] = tempAux;
                DataTable sub = new DataTable();
                DefaultColumns(sub);
                items[resTable.Columns.IndexOf("ggggggg")] = sub;
                resTable.Rows.Add(items);
            }

            CompareResults(res.Tables[0], MakeCopy(res).Tables[0]);
        }

        /// <summary>
        /// Tests the decoder results decryption with RPBF.
        /// </summary>
        [Test]
        public void DecoderTestResultsDecryptionRPBF()
        {
            //Create Fake stream
            string data = "V104^28^66;=[6Y8WT:R<P|Nojm!k#i`gbeB8^R7]L`jkg";
            MemoryStream stream = new MemoryStream(EncodingBase.GetEncoding().GetBytes(data));

            //Create Decoder
            DirectConnectionResultsDecoder decoder =
                new DirectConnectionResultsDecoder(stream, new RPBFEncryptionStrategy());

            DataTable table = decoder.DecodeTable();

            Assert.AreEqual(1, table.Columns.Count);
            Assert.AreEqual(1, table.Rows.Count);
            Assert.AreEqual("Hello", table.Rows[0]["x"].ToString());
        }
        
        /// <summary>
        /// Tests the decoder results decryption with Blowfish.
        /// </summary>
        [Test]
        public void DecoderTestResultsDecryptionBlowfish()
        {
            //Create Fake stream
            byte[] data = {
                              0x56, 0x31, 0x30, 0x34, 0x5e, 0x33, 0x32, 0x5e,
                              0x82, 0xdd, 0x9a, 0x40, 0x78, 0x3b, 0x69, 0xea,
                              0x4d, 0xd7, 0x7c, 0x8f, 0xce, 0x6f, 0x06, 0xe6,
                              0x8a, 0xe9, 0x2a, 0xbe, 0x05, 0x50, 0x9f, 0x83,
                              0xac, 0x76, 0x11, 0x08, 0x67, 0x4c, 0x11, 0x55,
                              0x31, 0x36, 0x5e, 0xeb, 0x6d, 0x12, 0xea, 0x51,
                              0xb4, 0xc4, 0xb1, 0x34, 0x8c, 0xbc, 0x5f, 0x31,
                              0x33, 0xc8, 0x4b
                          };

            MemoryStream stream = new MemoryStream(data);

            //Create Decoder
            DirectConnectionResultsDecoder decoder =
                new DirectConnectionResultsDecoder(stream, new BlowfishEncryptionStrategy());

            DataTable table = decoder.DecodeTable();

            Assert.AreEqual(1, table.Columns.Count);
            Assert.AreEqual(1, table.Rows.Count);
            Assert.AreEqual("Hello", table.Rows[0]["x"].ToString());
        }

    }
}