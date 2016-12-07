namespace RedPrairie.MOCA.NET
{
    using System;
    using System.Runtime.InteropServices;

    public class Error
    {
        public const int eOK                          = 0;
        public const int eFILE_OPENING_ERROR          = 1;
        public const int eERROR                       = 2;
        public const int eINVALID_ARGS                = 3;
        public const int eNODEFAULT_PRINTER           = 4;
        public const int eNO_MEMORY                   = 5;
        public const int eNO_MORE_FILENAMES           = 7;
        public const int eINVALID_MSGTYPE             = 8;
        public const int eNO_VALIDATE                 = 9;
        public const int eINVALID_OPERATION           = 10;
        public const int eSTARTUP_FAILURE             = 11;
        public const int ePERMISSION_DENIED           = 12;
        public const int ePARSE_ERROR                 = 13;
        public const int eNOT_IMPLEMENTED             = 14;

        public const int eSQL_BASE                    = 100;
        public const int eSQL_TOO_LONG                = 101;
        public const int eSQL_ABORTED                 = 102;
        
        public const int eMCC_BASE                    = 200;
        public const int eMCC_FAILED_TO_RELEASE       = 201;
        public const int eMCC_FAILED_TO_CONNECT       = 202;
        public const int eMCC_SEND_ERROR              = 203;
        public const int eMCC_RECV_ERROR              = 204;
        public const int eMCC_PROTO_NOT_SUPPORTED     = 205;
        public const int eMCC_HTTP_ERROR              = 206;
        public const int eMCC_PROTOCOL_ERROR          = 207;

        public const int eOS_BASE                     = 300;
        public const int eOS_TIMEOUT                  = 301;
        public const int eOS_EGAIN                    = 302;
        public const int eOS_AUTH_PAM                 = 303;
        public const int eOS_AUTH_READ                = 304;
        public const int eOS_AUTH_SEND                = 305;
        public const int eOS_AUTH_FAILED              = 306;

        public const int eMIS_BASE                    = 350;
        public const int eMIS_HTTP_ERROR              = 351;
        public const int eMIS_HTTP_FAILED_TO_CONNECT  = 352;
        public const int eMIS_HTTP_NOT_HTTP_RESPONSE  = 353;

        public const int eEVT_BASE                    = 400;
        public const int eEVT_INVALID_PARAMS          = 401;
        public const int eEVT_MBX_FAILURE             = 402;
        public const int eEVT_SYSERR                  = 403;
        public const int eEVT_ABORT                   = 404;

        public const int eSRV_BASE                    = 500;
        public const int eSRV_INVALID_COMMAND         = 501;
        public const int eSRV_UNEXPECTED_ERROR        = 502;
        public const int eSRV_PROTOCOL_ERROR          = 503;
        public const int eSRV_TYPE_MISMATCH           = 504;
        public const int eSRV_PARSE_ERROR             = 505;
        public const int eSRV_LICENSE_INVALID         = 506;
        public const int eSRV_INSUFF_ARGUMENTS        = 507;
        public const int eSRV_LICENSE_EXPIRED         = 508;
        public const int eSRV_LICENSE_NOTFOUND        = 509;
        public const int eSRV_NO_ROWS_AFFECTED        = 510;
        public const int eSRV_DB_ERROR                = 511;
        public const int eSRV_DUPLICATE               = 512;
        public const int eSRV_LICENSE_PRODUCT_INVALID = 513;
        public const int eSRV_MISSING_WHERE           = 514;
        public const int eSRV_NULL_RETURNED           = 515;
        public const int eSRV_INVALID_FUNCTION        = 516;
        public const int eSRV_REMOTE_SYSTEM_REQUIRED  = 517;
        public const int eSRV_REMOTE_CONNECT_FAILURE  = 518;
        public const int eSRV_INVALID_OBJECT          = 519;
        public const int eSRV_AUTOMATION_ERROR        = 520;
        public const int eSRV_DIVIDE_BY_ZERO          = 521;
        public const int eSRV_DISCONNECTED            = 522;
        public const int eSRV_AUTHENTICATE            = 523;
        public const int eSRV_INVALID_VERSION         = 524;
        public const int eSRV_SYSTEM_LIST_INVALID     = 525;
        public const int eSRV_UNHANDLED_TYPE          = 526;

        public const int eMXML_BASE                   = 600; 
        public const int eMXML_NO_PARSER              = 601;
        public const int eMXML_NO_TMP_FILENAME        = 602;
        public const int eMXML_NO_START_PTR           = 603;

        public const int eMOCA_BASE                   = 800;
        public const int eMOCA_INVALID_PASSWORD       = 801;
        public const int eMOCA_MISSING_ARG            = 802;
        public const int eMOCA_SMTP_COM_FAILURE       = 803;
        public const int eMOCA_FTP_COM_FAILURE        = 804;
        public const int eMOCA_SOAP_UNKNOWN_ERROR     = 805;

        public const int eMOCA_LAST_ONE_DONT_REMOVE   = 999;

        public const int eDB_NORMAL                   = 0;
        public const int eDB_UNIQUE_CONS_VIO          = -1;
        public const int eDB_LOCK_TIMEOUT             = -54;
        public const int eDB_DEADLOCK                 = -60;
        public const int eDB_NOT_A_GROUPBY_EXPR       = -979;
        public const int eDB_MAX_CURSORS              = -1000;
        public const int eDB_INVALID_CURSOR           = -1001;
        public const int eDB_NOT_LOGGED_IN            = -1012;
        public const int eDB_INTERRUPTED              = -1013;
        public const int eDB_NO_ROWS_AFFECTED         = -1403;
        public const int eDB_EOF_ON_COMMCHANNEL       = -3113;
        public const int eDB_NOT_CONNECTED            = -3114;
        public const int eDB_INVALID_NLS_PARAMETER    = -12705;

        public const int eDB_ADO_NOT_CONNECTED        = -2147467259;
    }
}
