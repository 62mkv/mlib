using System;
using System.Data;
using System.IO;
using NUnit.Framework;
using RedPrairie.MOCA.Client.Encoding;

namespace RedPrairie.MOCA.Client.Tests
{
    /// <summary>
    /// This is a test utility base class that allows access to common
    /// test table creation functions.
    /// </summary>
    public class TestUtilBase
    {
        /// <summary>
        /// Constructs a default set of columns
        /// </summary>
        /// <param name="res">The <see cref="DataTable"/> to add the columns to</param>
        /// <param name="allowNull">if set to <c>true</c> set AllowDBNull to true.</param>
        public static void DefaultColumns(DataTable res, bool allowNull)
        {
            res.Columns.Add("aaaaaaa", MocaType.STRING.Class);
            res.Columns["aaaaaaa"].MaxLength = -1;

            res.Columns.Add("bbbbbbb", MocaType.BOOLEAN.Class);
            res.Columns.Add("ccccccc", MocaType.DOUBLE.Class);
            res.Columns.Add("ddddddd", MocaType.INTEGER.Class);
            res.Columns.Add("eeeeeee", MocaType.BINARY.Class);
            res.Columns["eeeeeee"].AllowDBNull = allowNull;

            res.Columns.Add("fffffff", MocaType.DATETIME.Class);
            res.Columns.Add("ggggggg", MocaType.RESULTS.Class);
        }

        /// <summary>
        /// Constructs a default set of columns
        /// </summary>
        /// <param name="res">The <see cref="DataTable"/> to add the columns to</param>
        public static void DefaultColumns(DataTable res)
        {
            DefaultColumns(res, false);
        }

        /// <summary>
        /// Simulates a command sent and received by using <see cref="ResultsEncoder.EncodeResults"/>
        /// to encode the data then <see cref="ResultsDecoderBase"/> to decode it.
        /// </summary>
        /// <param name="res">The <see cref="DataTable"/> to copy.</param>
        public static DataSet MakeCopy(DataSet res)
        {
            byte[] encoded = ResultsEncoder.EncodeResults(res.Tables[0], null);
            // ResultsEncoder exhausts the result set, so we need to reset it.
            DataSet copy = new ResultsDecoderBase(new MemoryStream(encoded)).Decode("");
            return copy;
        }

        /// <summary>
        /// Compares the tables to make sure the values are identical.
        /// </summary>
        /// <param name="orig">The original.</param>
        /// <param name="copy">The copy.</param>
        public static void CompareResults(DataTable orig, DataTable copy)
        {
            if (orig == null)
            {
                Assert.IsNull(copy);
                return;
            }


            Assert.AreEqual(orig.Rows.Count, copy.Rows.Count);
            Assert.AreEqual(orig.Columns.Count, copy.Columns.Count);
            int columns = orig.Columns.Count;

            for (int c = 0; c < columns; c++)
            {
                Assert.AreEqual(orig.Columns[c].ColumnName, copy.Columns[c].ColumnName);
                Assert.IsTrue(orig.Columns[c].DataType.Equals(copy.Columns[c].DataType));
                Assert.AreEqual(orig.Columns[c].AllowDBNull, copy.Columns[c].AllowDBNull);
                Assert.AreEqual(orig.Columns[c].MaxLength, copy.Columns[c].MaxLength);
            }

            Assert.AreEqual(orig.Rows.Count, copy.Rows.Count);

            for (int row = 0; row < orig.Rows.Count; row++)
            {
                DataRow rowOrig = orig.Rows[row];
                DataRow rowCopy = copy.Rows[row];


                for (int c = 0; c < columns; c++)
                {
                    MocaType type = MocaType.LookupClass(orig.Columns[c].DataType);
                    if (rowOrig.IsNull(c))
                    {
                        Assert.IsTrue(rowCopy.IsNull(c));
                    }
                    else if (type.Equals(MocaType.RESULTS))
                    {
                        CompareResults((DataTable) rowOrig[c], (DataTable) rowCopy[c]);
                    }
                    else if (type.Equals(MocaType.BINARY))
                    {
                        byte[] origData = (byte[]) rowOrig[c];
                        byte[] copyData = (byte[]) rowCopy[c];
                        Assert.IsTrue(CheckArrayEquality(origData, copyData));
                    }
                    else if (type.Equals(MocaType.UNKNOWN))
                    {
                        Assert.AreEqual(rowOrig[c], rowCopy[c]);
                    }
                    else if (type.Equals(MocaType.DATETIME))
                    {
                        DateTime origDate = (DateTime) rowOrig[c];
                        DateTime copyDate = (DateTime) rowCopy[c];

                        Assert.AreEqual(origDate.Date, copyDate.Date);
                        Assert.AreEqual(origDate.Hour, copyDate.Hour);
                        Assert.AreEqual(origDate.Minute, copyDate.Minute);
                        Assert.AreEqual(origDate.Second, copyDate.Second);
                    }
                    else
                    {
                        Assert.AreEqual(rowOrig[c], rowCopy[c]);
                    }
                }
            }
        }

        /// <summary>
        /// Compares the entire members of one array whith the other one.
        /// </summary>
        /// <param name="array1">The array to be compared.</param>
        /// <param name="array2">The array to be compared with.</param>
        /// <returns>True if both arrays are equals otherwise it returns false.</returns>
        /// <remarks>Two arrays are equal if they contains the same elements in the same order.</remarks>
        public static bool CheckArrayEquality(Array array1, Array array2)
        {
            bool result = false;
            if ((array1 == null) && (array2 == null))
                result = true;
            else if ((array1 != null) && (array2 != null))
            {
                if (array1.Length == array2.Length)
                {
                    int length = array1.Length;
                    result = true;
                    for (int index = 0; index < length; index++)
                    {
                        if (!(array1.GetValue(index).Equals(array2.GetValue(index))))
                        {
                            result = false;
                            break;
                        }
                    }
                }
            }
            return result;
        }
    }
}
