namespace RedPrairie.MOCA.NET
{
    using System;
    using System.Runtime.InteropServices;

    public class Transaction
    {
        [ DllImport("MOCA.dll") ]
        private static extern int srvCommit( );

        [ DllImport("MOCA.dll") ]
        private static extern int srvRollback( );

        public static int Commit( )
        {
            return srvCommit( );
        }

        public static int Rollback( )
        {
            return srvRollback( );
        }
    }
}
