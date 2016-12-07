namespace RedPrairie.MOCA.NET
{
    using System;
    using System.Runtime.InteropServices;

    public class SQL
    {
        [ DllImport("MOCA.dll") ]
        private static extern int dbExecStr
        (
            string sql,
            IntPtr res
        );

        public static int Execute(string fmt, params object[] args)
        {
            string sql;

            IntPtr res = IntPtr.Zero;

            sql = String.Format(fmt, args);

            return dbExecStr(sql, res);
        }
    }
}
