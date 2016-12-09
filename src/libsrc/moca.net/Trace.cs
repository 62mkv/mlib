namespace RedPrairie.MOCA.NET
{
    using System;
    using System.Runtime.InteropServices;

    public class Trace
    {
        public const int T_FLOW    = (1 << 0);
        public const int T_SQL     = (1 << 1);
        public const int T_MGR     = (1 << 2);
        public const int T_SERVER  = (1 << 3);
        public const int T_SRVARGS = (1 << 4);

        [ DllImport("MOCA.dll") ]
        private static extern void misTrc
        (
            int level,
            string message
        );

        private static void MyTrace(int level, string fmt, params object[] args)
        {
            string message;
            
            message = String.Format(fmt, args);

            misTrc(level, message);
        }

        public static void Flow(string fmt, params object[] args)
        {
            MyTrace(T_FLOW, fmt, args);
        }

        public static void SQL(string fmt, params object[] args)
        {
            MyTrace(T_SQL, fmt, args);
        }

        public static void Manager(string fmt, params object[] args)
        {
            MyTrace(T_MGR, fmt, args);
        }

        public static void Server(string fmt, params object[] args)
        {
            MyTrace(T_SERVER, fmt, args);
        }

        public static void Arguments(string fmt, params object[] args)
        {
            MyTrace(T_SRVARGS, fmt, args);
        }
    }
}
