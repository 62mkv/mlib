namespace RedPrairie.MOCA.NET
{
    using System;
    using System.Runtime.InteropServices;

    public class Log
    {
        [ DllImport("MOCA.dll") ]
        private static extern void misLogError
        (
            string message
        );

        [ DllImport("MOCA.dll") ]
        private static extern void misLogInfo
        (
            string message
        );

        [ DllImport("MOCA.dll") ]
        private static extern void misLogWarning
        (
            string message
        );

        public static void Error(string fmt, params object[] args)
        {
            string message;

            message = String.Format(fmt, args);

            misLogError(message);
        }

        public static void Info(string fmt, params object[] args)
        {
            string message;

            message = String.Format(fmt, args);

            misLogInfo(message);
        }

        public static void Warning(string fmt, params object[] args)
        {
            string message;

            message = String.Format(fmt, args);

            misLogWarning(message);
        }
    }
}
