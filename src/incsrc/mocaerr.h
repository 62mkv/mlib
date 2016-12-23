/*#START***********************************************************************D
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file containing MOCA error codes.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016-2010
 *  Sam Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by Sam Corporation.
 *
 *  Sam Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by Sam Corporation.
 *
 *  $Copyright-End$
 *
 *#END*************************************************************************/

#ifndef MOCAERR_H
#define MOCAERR_H

/*
 *  MOCA Error Codes 0-999.
 */

typedef enum MOCAErrors
{
    eFLAG_ERROR                  = -1,
    eLENGTH_ERROR                = -1,
    eOK                          = 0,
    eFILE_OPENING_ERROR          = 1,
    eERROR                       = 2,
    eINVALID_ARGS                = 3,
    eNO_DEFAULT_PRINTER          = 4,
    eNO_MEMORY                   = 5,
    eNO_MORE_FILENAMES           = 7,
    eINVALID_MSGTYPE             = 8,
    eNO_VALIDATE                 = 9,
    eINVALID_OPERATION           = 10,
    eSTARTUP_FAILURE             = 11,
    ePERMISSION_DENIED           = 12,
    ePARSE_ERROR                 = 13,
    eNOT_IMPLEMENTED             = 14,
    eUNKNOWN_CONS_COMMAND        = 15,
    eCONSOLE_EXCEPTION           = 16,
    ePRINT_FILE_ERROR            = 17,

    eSQL_BASE                    = 100,        /* sqllib */
    eSQL_TOO_LONG                = 101,
    eSQL_ABORTED                 = 102,

    eMCC_BASE                    = 200,        /* mcclib */
    eMCC_FAILED_TO_RELEASE       = 201,
    eMCC_FAILED_TO_CONNECT       = 202,
    eMCC_SEND_ERROR              = 203,
    eMCC_RECV_ERROR              = 204,
    eMCC_PROTO_NOT_SUPPORTED     = 205,
    eMCC_HTTP_ERROR              = 206,
    eMCC_PROTOCOL_ERROR          = 207,
    eMCC_CRYPT_DISCOVERY         = 208,
    eMCC_CONSTRUCT_CLIENT        = 213,
    eMCC_CLOSE_CLIENT            = 214,
    eMCC_LOGIN                   = 215,
    eMCC_LOGOUT                  = 216,

    eJNI_FIND_CLASS              = 250,        /* jnilib */
    eJNI_FIND_METHOD             = 251,
    eJNI_FIND_FIELD              = 252,
    eJNI_SERVER_ADAPTER          = 253,
    eJNI_EXCEPTION               = 254,
    eJNI_UNEXPECTED_ERROR        = 255,
    eJNI_UNEXPECTED_EXCEPTION    = 256,
    eJNI_INVALID_RESULTS_OBJECT  = 257,
    eJNI_INVALID_JNIENV          = 258,

    eOS_BASE                     = 300,        /* oslib */
    eOS_TIMEOUT                  = 301,
    eOS_EGAIN                    = 302,
    eOS_AUTH_FAILED              = 303,
    eOS_AUTH_NOT_SUPPORTED       = 304,

    eOS_MBX_READ_FILE            = 310,
    eOS_MBX_WRITE_FILE           = 311,
    eOS_MBX_CREATE_FILE          = 312,
    eOS_MBX_CREATE_MAILSLOT      = 313,
    eOS_MBX_MAP_FILE             = 314,
    eOS_MBX_NOT_FOUND            = 315,
    eOS_MBX_EXISTS               = 316,
    eOS_MBX_NOMEM                = 317,
    eOS_MBX_READ                 = 318,
    eOS_MBX_MSGCTL               = 319,
    eOS_MBX_MSGGET               = 320,
    eOS_MBX_MSGSND               = 321,
    eOS_MBX_PROTOCOL             = 322,
    eOS_MBX_QUEUE_ID             = 323,

    eMIS_BASE                    = 350,        /* mislib */
    eMIS_HTTP_ERROR              = 351,
    eMIS_HTTP_FAILED_TO_CONNECT  = 352,
    eMIS_HTTP_NOT_HTTP_RESPONSE  = 353,
    eMIS_SOCKET_DISCONNECTED     = 354,

    eEVT_BASE                    = 400,        /* evtlib */
    eEVT_INVALID_PARAMS          = 401,
    eEVT_MBX_FAILURE             = 402,
    eEVT_SYSERR                  = 403,
    eEVT_ABORT                   = 404,

    eSRV_BASE                    = 500,	       /* srvlib */
    eSRV_INVALID_COMMAND         = 501,
    eSRV_UNEXPECTED_ERROR        = 502,
    eSRV_PROTOCOL_ERROR          = 503,
    eSRV_TYPE_MISMATCH           = 504,
    eSRV_PARSE_ERROR             = 505,
    eSRV_LICENSE_INVALID         = 506,
    eSRV_INSUFF_ARGUMENTS        = 507,
    eSRV_LICENSE_EXPIRED         = 508,
    eSRV_LICENSE_NOTFOUND        = 509,
    eSRV_NO_ROWS_AFFECTED        = 510,
    eSRV_DB_ERROR                = 511,
    eSRV_DUPLICATE               = 512,
    eSRV_LICENSE_PRODUCT_INVALID = 513,
    eSRV_MISSING_WHERE           = 514,
    eSRV_NULL_RETURNED           = 515,
    eSRV_INVALID_FUNCTION        = 516,
    eSRV_REMOTE_SYSTEM_REQUIRED  = 517,
    eSRV_REMOTE_CONNECT_FAILURE  = 518,
    eSRV_INVALID_OBJECT          = 519,
    eSRV_AUTOMATION_ERROR        = 520,
    eSRV_DIVIDE_BY_ZERO          = 521,
    eSRV_DISCONNECTED            = 522,
    eSRV_AUTHENTICATE            = 523,
    eSRV_INVALID_VERSION         = 524,
    eSRV_SYSTEM_LIST_INVALID     = 525,
    eSRV_UNHANDLED_TYPE          = 526,
    eSRV_REMOTE_PREPARE          = 527,
    eSRV_REMOTE_COMMIT           = 528,
    eSRV_UNKNOWN_ADAPTER_CODE    = 529,
    eSRV_INVALID_METHOD          = 530,
    eSRV_SCRIPT_ERROR            = 531,
    eSRV_ACCUM_HIDDEN            = 532,
    eSRV_ACCUM_MIXED_HIDDEN      = 533,
    eSRV_LOGIN_FAILED            = 534,
    eSRV_JOB_FAILED              = 535,
    eSRV_NATIVE_TIMEOUT          = 536,
    eSRV_NATIVE_COMMUNICATION    = 537,
    eSRV_INVALID_MANAGED_METHOD  = 538,
    eSRV_INTERRUPTED             = 539,
    eSRV_ROW_LIMIT_EXCEEDED      = 540,
    eSRV_REMOTE_AUTHENTICATION   = 541,
    eSRV_SESSION_CLOSED          = 542,
    eSRV_REMOTE_SESSION_CLOSED   = 543,
    eSRV_COMMAND_AUTHORIZATION   = 544,

    eMXML_BASE                   = 600,        /* mxmllib */
    eMXML_INITIALIZE             = 601,
    eMXML_CONSTRUCT              = 602,
    eMXML_CONFIGURE              = 603,
    eMXML_PARSE_ERROR            = 604,
    eMXML_STRING                 = 605,
    eMXML_RAW_STRING             = 606,
    eMXML_WRITE_FILE             = 607,
    eMXML_RAW_WRITE_FILE         = 608,

    eMXML_ADD_ELEMENT            = 610,
    eMXML_ADD_TEXT_NODE          = 611,
    eMXML_ADD_COMMENT            = 612,
    eMXML_ADD_PI                 = 613,
    eMXML_ADD_ATTRIBUTE          = 614,
    eMXML_APPLY_XSLT_FILE        = 615,
    eMXML_APPLY_XSLT_STRING      = 616,

    eMXML_INSERT_BEFORE          = 620,
    eMXML_APPEND_CHILD           = 621,
    eMXML_REMOVE_CHILD           = 622,
    eMXML_REPLACE_CHILD          = 623,
    eMXML_CLONE_NODE             = 624,
    eMXML_HAS_CHILD_NODES        = 625,

    eMXML_CREATE_ATTR            = 630,
    eMXML_CREATE_COMMENT         = 631,
    eMXML_CREATE_DOCUMENT        = 632,
    eMXML_CREATE_DOCFRAG         = 633,
    eMXML_CREATE_ELEMENT         = 634,
    eMXML_CREATE_TEXT_NODE       = 635,
    eMXML_CREATE_CDATA_SECTION   = 636,
    eMXML_CREATE_PI              = 637,


    eMXML_GET_DOCUMENT           = 640,
    eMXML_GET_DOCUMENT_ELEMENT   = 641,
    eMXML_GET_ELEMENTS           = 642,
    eMXML_GET_TAG_NAME           = 643,
    eMXML_GET_NODE_NAME          = 644,
    eMXML_GET_NODE_VALUE         = 645,
    eMXML_GET_NODE_TYPE          = 646,  
    eMXML_GET_PARENT_NODE        = 647,
    eMXML_GET_CHILD_NODES        = 648,
    eMXML_GET_FIRST_CHILD        = 649,
    eMXML_GET_LAST_CHILD         = 650,
    eMXML_GET_PREVIOUS_SIBLING   = 651,
    eMXML_GET_NEXT_SIBLING       = 652,
    eMXML_GET_ATTRS              = 653,
    eMXML_GET_ATTR               = 654,
    eMXML_GET_ATTR_NAME          = 655,
    eMXML_GET_ATTR_VALUE         = 656,
    eMXML_GET_ATTR_NODE          = 657,
    eMXML_GET_ATTR_SPECIFIED     = 658,
    eMXML_GET_LIST_LENGTH        = 659,
    eMXML_GET_ITEM               = 660,
    eMXML_GET_NAMED_ITEM         = 661,
    eMXML_GET_CDATA              = 662,
    eMXML_GET_CDATA_LENGTH       = 663,
    eMXML_GET_PI_TARGET          = 664,
    eMXML_GET_PI_DATA            = 665,

    eMXML_SET_ATTR               = 670,
    eMXML_SET_ATTR_VALUE         = 671,
    eMXML_SET_ATTR_NODE          = 672,
    eMXML_SET_NODE_VALUE         = 673,
    eMXML_SET_NAMED_ITEM         = 674,
    eMXML_SET_CDATA              = 675,
    eMXML_SET_PI_DATA            = 676,

    eMXML_REMOVE_ATTR            = 680,
    eMXML_REMOVE_ATTR_NODE       = 681,
    eMXML_REMOVE_NAMED_ITEM      = 682,

    eCMD_BASE                    = 700,        /* cmdlib */
    eCMD_REPOSITORY_NOT_LOADED   = 701,
    eCMD_TOO_MANY_MATCHES        = 702,

    eCMD_MISSING_REPOSITORY      = 710,
    eCMD_MISSING_LEVEL           = 711,
    eCMD_MISSING_COMMAND         = 712,
    eCMD_MISSING_TRIGGER         = 713,
    eCMD_MISSING_ARGUMENT        = 714,
    eCMD_MISSING_TYPE            = 715,
    eCMD_MISSING_DATATYPE        = 716,
    eCMD_MISSING_NAMESPACE       = 717,
    eCMD_MISSING_PROGID          = 718,
    eCMD_MISSING_SYNTAX          = 719,
    eCMD_MISSING_FUNCTION        = 720,
    eCMD_MISSING_METHOD          = 721,
    eCMD_MISSING_DIRECTORY       = 722,
    eCMD_MISSING_NODE            = 723,
    eCMD_MISSING_VALUE           = 724,
    eCMD_MISSING_ATTRIBUTE       = 725,
    eCMD_MISSING_PACKAGE         = 726,

    eCMD_DUPLICATE_LEVEL         = 730,
    eCMD_DUPLICATE_COMMAND       = 731,
    eCMD_DUPLICATE_TRIGGER       = 732,
    eCMD_DUPLICATE_ARGUMENT      = 733,

    eCMD_UNKNOWN_LEVEL           = 740,
    eCMD_UNKNOWN_COMMAND         = 741,
    eCMD_UNKNOWN_TRIGGER         = 742,
    eCMD_UNKNOWN_ARGUMENT        = 743,
    eCMD_UNKNOWN_TYPE            = 744,
    eCMD_UNKNOWN_DATATYPE        = 745,
    eCMD_UNKNOWN_FILE_FORMAT     = 746,

    eMOCA_LDAP_CLIENT            = 750,       /* com.sam.moca.security */
    eMOCA_LDAP_CLIENT_BIND       = 751, 
    eMOCA_LDAP_CLIENT_AUTH       = 752, 
    eMOCA_LDAP_CLIENT_SEARCH     = 753, 

    eMOCA_BASE                   = 800,        /* com.sam.moca.components */
    eMOCA_INVALID_PASSWORD       = 801,
    eMOCA_MISSING_ARG            = 802,
    eMOCA_SMTP_COM_FAILURE       = 803,
    eMOCA_FTP_COM_FAILURE        = 804,
    eMOCA_SOAP_UNKNOWN_ERROR     = 805,
    eMOCA_INVALID_COLUMN         = 806,
    eMOCA_SFTP_COM_FAILURE       = 807,
    eMOCA_LDAP_FAILURE           = 808,
    eMOCA_COPY_FILE_FAILED       = 809,
    eMOCA_INVALID_ARG            = 810,

    eMOCA_TASK_NOT_FOUND         = 811,
    eMOCA_TASK_INVALID_STATE     = 812,
    eMOCA_LOAD_DATA_FAILED       = 813,

    eMOCA_SERVICE_MANAGER        = 815,        /* com.sam.moca.esrver.service */
    eMOCA_SERVICE_READER         = 816,

    eMOCA_SQL_CONTAINS_REFS      = 820,        /* mocadbutl */

    eMOCA_LDAP_MISSING_CONFIG    = 830,        /* mocasecurity */
    eMOCA_FAILED_FED_SECURITY    = 833,
    eMOCA_FAILED_ADMIN_SECURITY  = 834,

    eEMS_MISSING_HOST            = 850,        /* mocaems */
    eEMS_MISSING_PORT            = 851,
    eEMS_MISSING_EVENT_NAME      = 852,
    eEMS_MISSING_SOURCE_SYSTEM   = 853,
    eEMS_MISSING_KEY_VALUE       = 854,
    eEMS_INVALID_EVENT_NAME      = 855,
    eEMS_BAD_XML                 = 856,
    eEMS_PRIME_LOCK_EVENT        = 857,
    eEMS_FILE_CREATE             = 858,
    eEMS_FILE_REMOVE             = 859,
    eEMS_GENERATE_XML            = 860,
    eEMS_LOG_EVENT               = 861,

    eBPP_NO_SERVER_CONFIGURED    = 900,        /* mocabpp */
    eBPP_SERVER_URL_INVALID      = 901,
    eBPP_SERVICE_UNAVAILABLE     = 902,
    eBPP_UNKNOWN_ENTITY_SET      = 903,
    eBPP_STORAGE_RESULTS_ERROR   = 904,
    eBPP_STORAGE_ERROR           = 905,

    eMOCA_LAST_ONE_DONT_REMOVE   = 999,

    /*  
     *  These are errors returned from Oracle.
     *
     *  The enumerated values are only defined 
     *  so that programs may check them.  When
     *  a routine returns with an Oracle error, 
     *  a negative return code is returned and
     *  a global error string is filled in, if
     *  possible.
     *
     *  Enumerated values should be added to
     *  this list as needed.  The values should
     *  match those documented in the "Oracle
     *  Server Messages and Codes Manual".
     */

    eDB_NORMAL                = 0,             /* Oracle */
    eDB_UNIQUE_CONS_VIO       = -1,
    eDB_LOCK_TIMEOUT          = -54,
    eDB_DEADLOCK              = -60,
    eDB_NOT_A_GROUPBY_EXPR    = -979,
    eDB_MAX_CURSORS           = -1000,
    eDB_INVALID_CURSOR        = -1001,
    eDB_NOT_LOGGED_IN         = -1012,
    eDB_INTERRUPTED           = -1013,
    eDB_NO_ROWS_AFFECTED      = -1403,
    eDB_EOF_ON_COMMCHANNEL    = -3113,
    eDB_NOT_CONNECTED         = -3114,
    eDB_INVALID_NLS_PARAMETER = -12705,
    eDB_NON_ORACLE_CONN_ERROR = -28500,

    eDB_INCORRECT_SYNTAX      = -102,          /* SQL Server */

    /* ADO -> SQL Server Disconnect Error */
    eDB_ADO_NOT_CONNECTED     = -2147467259,

    /* Unexpected JDBC Exception */
    eDB_JDBC_EXCEPTION        = -2

} MOCAErrors;

#endif
