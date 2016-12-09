namespace RedPrairie.MOCA.NET
{
    using System;
    using System.Runtime.InteropServices;

    public class Command
    {
        [ DllImport("MOCA.dll") ]
        private static extern int srvInitiateCommand
        (
            string command,
            ref IntPtr res
        );

        [ DllImport("MOCA.dll") ]
        private static extern int srvInitiateInline
        (
            string command,
            ref IntPtr res
        );

        public static int Execute(string fmt, params object[] args)
        {
            string command;

            IntPtr res = IntPtr.Zero;

            command = String.Format(fmt, args);

            return srvInitiateCommand(command, ref res);
        }

        public static int ExecuteInline(string fmt, params object[] args)
        {
            string command;

            IntPtr res = IntPtr.Zero;

            command = String.Format(fmt, args);

            return srvInitiateInline(command, ref res);
        }
    }
}
