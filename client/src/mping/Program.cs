using System;
using System.Diagnostics;
using System.Text;
using System.Threading;
using RedPrairie.MOCA.Client;
using RedPrairie.MOCA.Client.Interfaces;
using RedPrairie.MOCA.Exceptions;

namespace mping
{
    /// <summary>
    ///   The main section of the application for running.
    /// </summary>
    internal class Program
    {
        /// <summary>
        ///  The entry method for the program.
        /// </summary>
        /// <param name = "args">The command line arguments.</param>
        private static void Main(string[] args)
        {
            // Get options
            var opts = new CommandLineArguments(args);

            if (String.IsNullOrEmpty(opts.DefaultArgument) || 
                opts.ContainsArgument("h") || opts.ContainsArgument("?"))
            {
                ShowUsage();
                return;
            }

            var size = 32;
            if (opts.ContainsArgument("s") && Int32.TryParse(opts.GetArgument("s"), out size))
            {
                size = (size > 0) ? size : 32;
            }

            var delay = 1.0;
            if (opts.ContainsArgument("i") && Double.TryParse(opts.GetArgument("i"), out delay))
            {
                delay = (delay > 0) ? delay : 1.0;
            }

            var count = Int32.MaxValue;
            if (opts.ContainsArgument("c") && Int32.TryParse(opts.GetArgument("c"), out count))
            {
                count = (count > 0) ? count : Int32.MaxValue;
            }

            // Options are set, load up test data
            var commandPayload = GetCommandPayload(size);

            var serverConnection = opts.DefaultArgument;

            // Print out banner
            Console.WriteLine("mping {0}: {1} data bytes", serverConnection, size);
            Console.WriteLine();
            Console.TreatControlCAsInput = true;
            
            try
            {
                IMocaDataProvider conn = new FullConnection(serverConnection, 0);
                conn.Connect();

                // Throw away the first ping time.
                PingTime(conn, string.Empty);

                for (var i = 0; i < count; i++)
                {
                    if (i != 0) Thread.Sleep((int) (delay*1000.0));

                    var time = PingTime(conn, commandPayload);
                    if (time < 0)
                    {
                        Console.WriteLine("Server Not Responding");
                    }
                    else
                    {
                        Stats.RecordPingTime(time);
                        Console.WriteLine("Time = {0:f2} ms", time);
                    }

                    if (!Console.KeyAvailable) 
                        continue;

                    var key = Console.ReadKey(true);
                    if (((key.Modifiers & ConsoleModifiers.Control) != 0) && 
                        key.Key == ConsoleKey.C)
                    {
                        break;
                    }
                }
            }
            catch (ConnectionFailedException)
            {
                Console.WriteLine("Error connecting to server: {0}", serverConnection);
                return;
            }

            Stats.ShowStats();
        }

        /// <summary>
        /// Gets the command payload.
        /// </summary>
        /// <param name="size">The size.</param>
        /// <returns>A random string that represents the command payload.</returns>
        private static string GetCommandPayload(int size)
        {
            var characterSet =
                "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890~`!@#$%^&*()_-+={[}]|\\:;\"<>,.?/".ToCharArray();
            var buf = new StringBuilder(size);
            var r = new Random();
            for (var i = 0; i < size; i++)
            {
                buf.Append(characterSet[r.Next(characterSet.Length)]);
            }

            return buf.ToString();
        }

        /// <summary>
        ///   Shows the usage.
        /// </summary>
        public static void ShowUsage()
        {
            Console.WriteLine("Usage: mping [ -c <count> ] [ -i <interval> ] [ -s <size> ]\n <server uri>");
        }

        /// <summary>
        ///   Pings the server recording the time it takes to run.
        /// </summary>
        /// <param name = "conn">The connection.</param>
        /// <param name = "element">The element to set the payload size.</param>
        /// <returns>A duration of time to ping or -1 if something errors.</returns>
        private static double PingTime(IMocaDataProvider conn, String element)
        {
            var stopwatch = Stopwatch.StartNew();

            try
            {
                conn.Execute(string.Format("ping where blah = '{0}'", element));
            }
            catch (MocaException)
            {
                return -1L;
            }
            finally
            {
                stopwatch.Stop();
            }

            return stopwatch.Elapsed.TotalMilliseconds;
        }

        #region Nested type: Stats

        /// <summary>
        /// A static class that stores the statistics
        /// </summary>
        private static class Stats
        {
            private static int _count;
            private static double _max;
            private static double _min = double.MaxValue;
            private static double _total;

            /// <summary>
            /// Shows the statistics on exit.
            /// </summary>
            public static void ShowStats()
            {
                Console.WriteLine();
                Console.WriteLine("{0:n0} commands\n", _count);
                Console.WriteLine("round-trip min/avg/max = {0:f2}/{1:f2}/{2:f2} ms",
                                  _min, _total/_count, _max);
            }

            /// <summary>
            /// Records the ping time.
            /// </summary>
            /// <param name="nanos">The nanos.</param>
            public static void RecordPingTime(double nanos)
            {
                _count++;
                _total += nanos;
                if (nanos < _min) _min = nanos;
                if (nanos > _max) _max = nanos;
            }

            /// <summary>
            /// Gets the ms time.
            /// </summary>
            /// <param name="value">The value.</param>
            /// <returns></returns>
            private static double GetMsTime(double value)
            {
                return value / 1000000.0;
            }
        }

        #endregion
    }
}