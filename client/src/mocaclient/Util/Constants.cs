namespace RedPrairie.MOCA.Util
{
    /// <summary>
    /// Contains constant values for the client to leverage
    /// </summary>
    public class Constants
    {
        //Flag Constants
        /// <summary>
        /// Flag constant for disabling auto-commit
        /// </summary>
        public const int FLAG_NOCOMMIT = 0x01;

        /// <summary>
        /// Flag constant for enabling ASCII communication
        /// </summary>
        public const int FLAG_ASCII_COMM = 0x02;

        /// <summary>
        /// Flag constant for enabling licence checks
        /// </summary>
        public const int FLAG_LICENSE_CHECK = 0x04;
        
        /// <summary>
        /// Flag constant for enabling keep alive on the connection
        /// </summary>
        public const int FLAG_KEEPALIVE = 0x08;

        /// <summary>
        /// Flag constant for indicating this is a remote server call
        /// </summary>
        public const int FLAG_REMOTE = 0x10;

        /// Protocol version constants
        /// <summary>
        /// Constant for the earliest protocol version created
        /// </summary>
        public const int PROTOCOL_MIN_VERSION = 100;

        /// <summary>
        /// Constant for the earliest latest version created
        /// </summary>
        public const int PROTOCOL_MAX_VERSION = 104;

        //Data Table extended constants

        /// <summary>
        /// Constant for the column's actual width property key embedded in a .NET column's 
        /// ExtendedProperty collection.
        /// </summary>
        public const string MaxActualWidthProperty = "MaxActualWidthProperty";

        /// <summary>
        /// Constant for the column's short description property key embedded in a .NET column's 
        /// ExtendedProperty collection.
        /// </summary>
        public const string ShortDescProperty = "ShortDescProperty";
        
        /// <summary>
        /// Constant for the column's long description property key embedded in a .NET column's 
        /// ExtendedProperty collection.
        /// </summary>
        public const string LongDescProperty = "LongDescProperty";

        //Protocol Parsing constants
        /// <summary>
        /// Constant for the protocol's data field delimiter
        /// </summary>
        public const char DELIMITER = '^';

        /// <summary>
        /// Constant for the protocol's data column field delimiter
        /// </summary>
        public const char COLUMN_DELIMITER = '~';

        //Server Environment Vairable Constants

        /// <summary>
        /// MOCA environment string constant for application id (APPL_ID)
        /// </summary>
        public const string MocaApplicationID = "MOCA_APPL_ID";
        
        /// <summary>
        /// MOCA constant for the default trace flag switches
        /// </summary>
        public const string DefaultTraceFlags = "WMXASR";

        /// <summary>
        /// MOCA environment string constant for warehouse id (WH_ID)
        /// </summary>
        public const string EnvironmentKeyWarehouse = "WH_ID";

        /// <summary>
        /// MOCA environment string constant for a digital signature key (SIG_KEY)
        /// </summary>
        public const string EnvironmentKeySignatureKey = "SIG_KEY";

        /// <summary>
        /// MOCA environment string constant for the session key
        /// </summary>
        public const string EnvironmentKeySessionKey = "SESSION_KEY";

        /// <summary>
        /// MOCA environment string constant for user id (USR_ID)
        /// </summary>
        public const string EnvironmentKeyUserID = "USR_ID";

        /// <summary>
        /// MOCA environment string constant for locale id (LOCALE_ID)
        /// </summary>
        public const string EnvironmentKeyLocaleID = "LOCALE_ID";

        /// <summary>
        /// Constant indicating that the data is global to the system ('LES' constant)
        /// </summary>
        public const string GlobalDataID = "LES";
    }
}
