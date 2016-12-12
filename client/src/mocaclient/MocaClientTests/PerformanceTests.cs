using System;
using System.Collections.Generic;
using System.Data;
using System.Text;
using NUnit.Framework;
using RedPrairie.MOCA.Exceptions;
using RedPrairie.MOCA.Util;

namespace RedPrairie.MOCA.Client.Tests.Performance
{
    // ReSharper disable UnusedPrivateMember
    /// <summary>
    /// Runs performance tests on the direct connection
    /// </summary>
    [TestFixture]
    public class PerformanceTests
    {
        /// <summary>
        /// Gets the hostname to use
        /// </summary>
        public static string HOST = "localhost";
        /// <summary>
        /// Gets the port to use
        /// </summary>
        public static int PORT = 4500;

        private readonly List<KeyValuePair<string, string>> commandRun = new List<KeyValuePair<string, string>>(5);
        private readonly List<PerformResult> results = new List<PerformResult>();
        /// <summary>
        /// Exposes the connection used to test performance
        /// </summary>
        public FullConnection managedConnection = new FullConnection(HOST, PORT);
        private readonly Random generator = new Random();

        private class PerformResult
        {
            public PerformResult()
            {
                TotalLegacyRuns = 0;
                TotalManagedRuns = 0;
                LegacyTime = new TimeSpan(0);
                ManagedTime = new TimeSpan(0);
            }

            public string TestName;
            public TimeSpan LegacyTime;
            public TimeSpan ManagedTime;

            public int TotalLegacyRuns;
            public int TotalManagedRuns;

            public override string ToString()
            {
                return string.Format("{0, -30} {1, -10} {2, -10} {3}",
                                     TestName,
                                     LegacyTime.Milliseconds,
                                     ManagedTime.Milliseconds,
                                     ((((decimal) LegacyTime.Milliseconds - (decimal) ManagedTime.Milliseconds)
                                       /LegacyTime.Milliseconds)*100).ToString("G3"));
            }

            public string ToAverageString()
            {
                decimal legacyAverage = ((decimal) LegacyTime.Milliseconds/TotalLegacyRuns);
                decimal managedAverage = ((decimal) ManagedTime.Milliseconds/TotalManagedRuns);


                return string.Format("{0, -30} {1, -6}  {2, -10} {3, -11} {4}",
                                     TestName,
                                     TotalManagedRuns,
                                     legacyAverage,
                                     managedAverage,
                                     (((legacyAverage - managedAverage)/legacyAverage)*100).ToString("G3"));
            }
        }

        /// <summary>
        /// Sets up this instance.
        /// </summary>
        [SetUp]
        public void Setup()
        {
            //commandRun.Add(new KeyValuePair<string, string>("Large Upstream Command:", CreateUpstreamCommand()));
            //commandRun.Add(new KeyValuePair<string, string>("Large Argument / Cols Command:", CreateLargeArgs()));
            //commandRun.Add(new KeyValuePair<string, string>("Large Command:", CreateLargeCommand()));
            //commandRun.Add(new KeyValuePair<string, string>("Large Results:", CreateLargeResults()));
            //commandRun.Add(new KeyValuePair<string, string>("Large Columns:", CreateWideCommand()));
            commandRun.Add(new KeyValuePair<string, string>("Real Table:", CreateResultsReal()));
        }

        /// <summary>
        /// Runs the performance tests.
        /// </summary>
        [Test]
        public void RunPerformanceTests()
        {
            try
            {
                try
                {
                    managedConnection.Connect();
                    managedConnection.Login("SUPER", "super");
                }
                catch (ConnectionFailedException)
                {
                    return;
                }

                foreach (KeyValuePair<string, string> command in commandRun)
                {
                    PerformResult result = new PerformResult();
                    RunTest(command, result);

                    results.Add(result);
                }

                Console.WriteLine("Performance Results");
                Console.WriteLine("Test Name\t\t\tLegacy\tManaged\t %");
                foreach (PerformResult performResult in results)
                {
                    Console.WriteLine(performResult);
                }
            }
            finally
            {
                managedConnection.Close();
            }
        }

        /// <summary>
        /// Tests the large number of commands.
        /// </summary>
        [Test]
        public void TestLargeNumberOfCommands()
        {
            try
            {
                managedConnection.Connect();
                managedConnection.Login("SUPER", "super");
            }
            catch (ConnectionFailedException)
            {
                return;
            }
            try
            {
                decimal managedAverage = 0;

                for (int i = 0; i < 10000; i++)
                {
                    DateTime startTime = DateTime.Now;
                    managedConnection.Execute("publish data where x = 'ABCDEFG'");
                    DateTime stopTime = DateTime.Now;

                    managedAverage += stopTime.Subtract(startTime).Milliseconds;
                }

                Console.WriteLine("Large Number of Commands Test");
                Console.WriteLine("Averages  \tLegacy\tManaged");
                Console.WriteLine("10,000 Calls\t{0}\t{1}",
                                  "0",
                                  (managedAverage/10000).ToString("G3"));
            }
            finally
            {
                managedConnection.Close();
            }
        }

        /// <summary>
        /// Averages the performance tests.
        /// </summary>
        [Test]
        public void AveragePerformanceTests()
        {
            const int totalRuns = 10;
            try
            {
                managedConnection.Connect();
                managedConnection.Login("SUPER", "super");
            }
            catch (ConnectionFailedException)
            {
                return;
            }

            //Create Results Hash
            Dictionary<string, PerformResult> hashResults = new Dictionary<string, PerformResult>();
            foreach (KeyValuePair<string, string> valuePair in commandRun)
            {
                hashResults.Add(valuePair.Key, new PerformResult());
            }

            try
            {
                List<KeyValuePair<string, string>> randomQueue = CreateRandomList(commandRun);

                for (int i = 0; i < totalRuns; i++)
                {
                    foreach (KeyValuePair<string, string> command in randomQueue)
                    {
                        PerformResult result = hashResults[command.Key];

                        if (RunTest(command, result))
                        {
                            results.Add(result);
                        }
                    }

                    if (i < 100)
                        Console.WriteLine("Finished Run {0}", i);
                }

                Console.WriteLine("Commands Test ({0} calls - randomized)", totalRuns);
                Console.WriteLine("{0, -30} {1, -6}  {2, -10} {3, -11} {4}", "Test Name", "Runs", "Table (ms)", "Object (ms)", "% Gain");
                foreach (PerformResult performResult in hashResults.Values)
                {
                    Console.WriteLine(performResult.ToAverageString());
                }
            }
            finally
            {
                managedConnection.Close();
            }
        }

        [Test]
        public void TestObjectResults()
        {
            PerformResult result = new PerformResult();
            result.TestName = "Object Test";

            try
            {
                managedConnection.Connect();
                managedConnection.Login("SUPER", "super");
            }
            catch (ConnectionFailedException)
            {
                return;
            }

            try
            {
                RunObjectTest(CreateResultsReal(), result);
            }
            finally
            {
                managedConnection.Close();
            }
        }

        private List<KeyValuePair<string, string>> CreateRandomList(IEnumerable<KeyValuePair<string, string>> commands)
        {
            List<KeyValuePair<string, string>> copy = new List<KeyValuePair<string, string>>(commands);
            List<KeyValuePair<string, string>> result = new List<KeyValuePair<string, string>>();

            while (copy.Count > 0)
            {
                int index = generator.Next(0, copy.Count - 1);

                result.Add(copy[index]);
                copy.RemoveAt(index);
            }

            return result;
        }


        private bool RunTest(KeyValuePair<string, string> command, PerformResult result)
        {
            result.TestName = command.Key;
            result.TotalLegacyRuns++;
            result.TotalManagedRuns++;
            result.ManagedTime = RunObjectTest(command.Value, result);
            result.LegacyTime = RunManagedTest(command.Value); // RunManagedTestWithConvert(command.Value);

            return true;
        }

        private TimeSpan RunObjectTest(string command, PerformResult result)
        {
            DateTime startTime = DateTime.Now;
            try
            {
                List<MLSData> data = managedConnection.GetData<List<MLSData>>(command);

                if (data == null)
                {
                    Console.WriteLine("Data from object test '{0}' returned null", result.TestName);
                    return TimeSpan.Zero;
                }
            }
            catch (MocaException e)
            {
                Console.WriteLine("Test '{0}' threw an Exception: {1}", result.TestName, e);
                return TimeSpan.Zero;
            }
            DateTime stopTime = DateTime.Now;

            
            return stopTime.Subtract(startTime);
        }

        private TimeSpan RunManagedTest(string command)
        {
            DateTime startTime = DateTime.Now;
            ExecuteCommandResult result = managedConnection.ExecuteResults(command);
            DateTime stopTime = DateTime.Now;

            if (result.StatusCode != 0) throw new MocaException(result.StatusCode);

            return stopTime.Subtract(startTime);
        }

        
        private TimeSpan RunManagedTestWithConvert(string command)
        
        {
            DateTime startTime = DateTime.Now;
            ExecuteCommandResult result = managedConnection.ExecuteResults(command);
            
            if (result.HasData && result.Command.StartsWith("[SELECT * FROM les_mls_cat"))
            {
                List<MLSData> list = new List<MLSData>();
                foreach (DataRow row in result.TableData.Rows)
                {
                    MLSData mls = new MLSData();
                    mls.Appl_id = ConnectionUtils.GetStringValue(row, "appl_id");
                    mls.Cust_lvl = ConnectionUtils.GetIntValue(row, "cust_lvl");
                    mls.Frm_id = ConnectionUtils.GetStringValue(row, "frm_id");
                    mls.Grp_nam = ConnectionUtils.GetStringValue(row, "grp_nam");
                    mls.Locale_id = ConnectionUtils.GetStringValue(row, "locale_id");
                    mls.Mls_id = ConnectionUtils.GetStringValue(row, "mls_id");
                    mls.Mls_text = ConnectionUtils.GetStringValue(row, "mls_text");
                    mls.Prod_id = ConnectionUtils.GetStringValue(row, "prod_id");
                    mls.Srt_seq = ConnectionUtils.GetIntValue(row, "srtseq");

                    list.Add(mls);
                }

            }

            
            DateTime stopTime = DateTime.Now;

            if (result.StatusCode != 0) throw new MocaException(result.StatusCode);

            return stopTime.Subtract(startTime);
        }

        private static string CreateUpstreamCommand()
        {
            StringBuilder command = new StringBuilder();
            command.Append("publish data where ");

            for (int i = 0; i < 10000; i++)
            {
                if (i > 0) command.Append(" and ");
                command.Append("x" + i);
                command.Append(" = ");
                command.Append(i);
            }

            command.Append(" | noop");

            return command.ToString();
        }

        private static string CreateWideCommand()
        {
            StringBuilder command = new StringBuilder();
            command.Append("publish data where ");

            for (int i = 0; i < 10000; i++)
            {
                if (i > 0) command.Append(" and ");
                command.Append("x" + i);
                command.Append(" = ");
                command.Append(i);
            }

            return command.ToString();
        }

        private static string CreateLargeArgs()
        {
            StringBuilder command = new StringBuilder();
            command.Append("publish data where ");

            for (int i = 0; i < 10000; i++)
            {
                if (i > 0) command.Append(" and ");
                command.Append("x" + i);
                command.Append(" = ");
                command.Append(i);
            }

            return command.ToString();
        }

        private static string CreateLargeCommand()
        {
            char[] characterSet =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890~`!@#$%^&*()_-+={[}]|\\:;\"<>,.?/".
                    ToCharArray();
            StringBuilder element = new StringBuilder();
            Random r = new Random();
            for (int i = 0; i < 5000000; i++)
            {
                element.Append(characterSet[r.Next(characterSet.Length)]);
            }

            return string.Format("publish data where x = '{0}'", element);
        }

        private static string CreateLargeResults()
        {
            return "do loop where count = 1000 | publish data where x='Hello' and y = @i";
        }

        private static string CreateResultsReal()
        {
            return "[SELECT * FROM les_mls_cat UNION SELECT * FROM les_mls_cat]";
        }
    }

    public class MLSData
    {
        private int x;
        private string mls_id;
        private string locale_id;
        private string prod_id;
        private string appl_id;
        private string frm_id;
        private string vartn;
        private int srt_seq;
        private int cust_lvl;
        private string mls_text;
        private string grp_nam;

        public int X
        {
            get { return x; }
            set { x = value; }
        }

        public string Mls_id
        {
            get { return mls_id; }
            set { mls_id = value; }
        }

        public string Locale_id
        {
            get { return locale_id; }
            set { locale_id = value; }
        }

        public string Prod_id
        {
            get { return prod_id; }
            set { prod_id = value; }
        }

        public string Appl_id
        {
            get { return appl_id; }
            set { appl_id = value; }
        }

        public string Frm_id
        {
            get { return frm_id; }
            set { frm_id = value; }
        }

        public string Vartn
        {
            get { return vartn; }
            set { vartn = value; }
        }

        public int Srt_seq
        {
            get { return srt_seq; }
            set { srt_seq = value; }
        }

        public int Cust_lvl
        {
            get { return cust_lvl; }
            set { cust_lvl = value; }
        }

        public string Mls_text
        {
            get { return mls_text; }
            set { mls_text = value; }
        }

        public string Grp_nam
        {
            get { return grp_nam; }
            set { grp_nam = value; }
        }
    }
    // ReSharper restore UnusedPrivateMember
}