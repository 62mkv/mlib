namespace RedPrairie.MOCA.Util
{
    /// <summary>
    /// Constants that define MOCA Errors as well as a 
    /// method to validate if the commmand returned without errors
    /// </summary>
    public class MocaErrors
    {

        /// <summary>
        /// A constant for MOCA error code -1 - Record already exists.
        /// </summary>
        public const int eFLAG_ERROR = -1;

        /// <summary>
        /// A constant for MOCA error code -1 - Record already exists.
        /// </summary>
        public const int eLENGTH_ERROR = -1;

        /// <summary>
        /// A constant for MOCA error code 0 - Success.
        /// </summary>
        public const int eOK = 0;

        /// <summary>
        /// A constant for MOCA error code 1 - Cannot open file.
        /// </summary>
        public const int eFILE_OPENING_ERROR = 1;

        /// <summary>
        /// A constant for MOCA error code 2 - General error
        /// </summary>
        public const int eERROR = 2;

        /// <summary>
        /// A constant for MOCA error code 3 - Invalid arguments
        /// </summary>
        public const int eINVALID_ARGS = 3;

        /// <summary>
        /// A constant for MOCA error code 4 - No default printer
        /// </summary>
        public const int eNODEFAULT_PRINTER = 4;

        /// <summary>
        /// A constant for MOCA error code 5 - Out of Memory
        /// </summary>
        public const int eNO_MEMORY = 5;

        /// <summary>
        /// A constant for MOCA error code 7 - No more filenames
        /// </summary>
        public const int eNO_MORE_FILENAMES = 7;

        /// <summary>
        /// A constant for MOCA error code 8 - Invalid message type
        /// </summary>
        public const int eINVALID_MSGTYPE = 8;

        /// <summary>
        /// A constant for MOCA error code 9 - Validation failure
        /// </summary>
        public const int eNO_VALIDATE = 9;

        /// <summary>
        /// A constant for MOCA error code 10 - Invalid operation
        /// </summary>
        public const int eINVALID_OPERATION = 10;

        /// <summary>
        /// A constant for MOCA error code 11 - Startup failure
        /// </summary>
        public const int eSTARTUP_FAILURE = 11;

        /// <summary>
        /// A constant for MOCA error code 12 - Permission denied
        /// </summary>
        public const int ePERMISSION_DENIED = 12;

        /// <summary>
        /// A constant for MOCA error code 13 - Parse error
        /// </summary>
        public const int ePARSE_ERROR = 13;

        /// <summary>
        /// A constant for MOCA error code 14 - Command Not Implimented
        /// </summary>
        public const int eNOT_IMPLEMENTED = 14;


        /// <summary>
        /// A constant for MOCA error code 100 - General SQL library error
        /// </summary>
        public const int eSQL_BASE = 100; /* sqllib */

        /// <summary>
        /// A constant for MOCA error code 101 - SQL command is to long
        /// </summary>
        public const int eSQL_TOO_LONG = 101;

        /// <summary>
        /// A constant for MOCA error code 102 - SQL command was aborted
        /// </summary>
        public const int eSQL_ABORTED = 102;


        /// <summary>
        /// A constant for MOCA error code 200 - General MCCLIB error
        /// </summary>
        public const int eMCC_BASE = 200; /* mcclib */

        /// <summary>
        /// A constant for MOCA error code 201 - Unable to release MCC
        /// </summary>
        public const int eMCC_FAILED_TO_RELEASE = 201;

        /// <summary>
        /// A constant for MOCA error code 202 - Failed to connect to server
        /// </summary>
        public const int eMCC_FAILED_TO_CONNECT = 202;

        /// <summary>
        /// A constant for MOCA error code 203 - Eror sending data to server
        /// </summary>
        public const int eMCC_SEND_ERROR = 203;

        /// <summary>
        /// A constant for MOCA error code 204 - Error receiving results from server
        /// </summary>
        public const int eMCC_RECV_ERROR = 204;

        /// <summary>
        /// A constant for MOCA error code 205 - Protocol version unsupported
        /// </summary>
        public const int eMCC_PROTO_NOT_SUPPORTED = 205;

        /// <summary>
        /// A constant for MOCA error code 206 - Error from web server
        /// </summary>
        public const int eMCC_HTTP_ERROR = 206;

        /// <summary>
        /// A constant for MOCA error code 207 - Communication protocol failure
        /// </summary>
        public const int eMCC_PROTOCOL_ERROR = 207;

        /// <summary>
        /// A constant for MOCA error code 208 - Cryptography negotiation failure
        /// </summary>
        public const int eMCC_CRYPT_DISCOVERY = 208;


        /// <summary>
        /// A constant for MOCA error code 300 - General OSLIB error
        /// </summary>
        public const int eOS_BASE = 300; /* oslib */

        /// <summary>
        /// A constant for MOCA error code 301 - A timeout occurred
        /// </summary>
        public const int eOS_TIMEOUT = 301;

        /// <summary>
        /// A constant for MOCA error code 302 - Resource is temporarily not available
        /// </summary>
        public const int eOS_EGAIN = 302;

        /// <summary>
        /// A constant for MOCA error code 303 - PAM authentication error
        /// </summary>
        public const int eOS_AUTH_PAM = 303;

        /// <summary>
        /// A constant for MOCA error code 304 - Authentication request read failed
        /// </summary>
        public const int eOS_AUTH_READ = 304;

        /// <summary>
        /// A constant for MOCA error code 305 - Authentication request send failed
        /// </summary>
        public const int eOS_AUTH_SEND = 305;

        /// <summary>
        /// A constant for MOCA error code 306  - Authentication failed
        /// </summary>
        public const int eOS_AUTH_FAILED = 306;


        /// <summary>
        /// A constant for MOCA error code 310 - Mailbox file read failure
        /// </summary>
        public const int eOS_MBX_READ_FILE = 310;

        /// <summary>
        /// A constant for MOCA error code 311 - Mailbox file write failure
        /// </summary>
        public const int eOS_MBX_WRITE_FILE = 311;

        /// <summary>
        /// A constant for MOCA error code 312 - Mailbox file create failure
        /// </summary>
        public const int eOS_MBX_CREATE_FILE = 312;

        /// <summary>
        /// A constant for MOCA error code 313 - Mailbox mail slot create failure
        /// </summary>
        public const int eOS_MBX_CREATE_MAILSLOT = 313;

        /// <summary>
        /// A constant for MOCA error code 314 - Mailbox file map failure
        /// </summary>
        public const int eOS_MBX_MAP_FILE = 314;

        /// <summary>
        /// A constant for MOCA error code 315 - Mailbox not found
        /// </summary>
        public const int eOS_MBX_NOT_FOUND = 315;

        /// <summary>
        /// A constant for MOCA error code 316 - Mailbox already exists
        /// </summary>
        public const int eOS_MBX_EXISTS = 316;

        /// <summary>
        /// A constant for MOCA error code 317 - Mailbox out of space
        /// </summary>
        public const int eOS_MBX_NOMEM = 317;

        /// <summary>
        /// A constant for MOCA error code 318 - Mailbox read failure
        /// </summary>
        public const int eOS_MBX_READ = 318;

        /// <summary>
        /// A constant for MOCA error code 319 - Mailbox message control system issue
        /// </summary>
        public const int eOS_MBX_MSGCTL = 319;

        /// <summary>
        /// A constant for MOCA error code 320 - Mailbox retrieve failure
        /// </summary>
        public const int eOS_MBX_MSGGET = 320;

        /// <summary>
        /// A constant for MOCA error code 321 - Mailbox send failure
        /// </summary>
        public const int eOS_MBX_MSGSND = 321;

        /// <summary>
        /// A constant for MOCA error code 322 - Mailbox protocol issue
        /// </summary>
        public const int eOS_MBX_PROTOCOL = 322;

        /// <summary>
        /// A constant for MOCA error code 323 - Mailbox invalid queue ID
        /// </summary>
        public const int eOS_MBX_QUEUE_ID = 323;


        /// <summary>
        /// A constant for MOCA error code 350 - General MISLIB error
        /// </summary>
        public const int eMIS_BASE = 350; /* mislib */

        /// <summary>
        /// A constant for MOCA error code 351 - An HTTP error occurred
        /// </summary>
        public const int eMIS_HTTP_ERROR = 351;

        /// <summary>
        /// A constant for MOCA error code 352 - Could not connect to HTTP endpoint
        /// </summary>
        public const int eMIS_HTTP_FAILED_TO_CONNECT = 352;

        /// <summary>
        /// A constant for MOCA error code 353 - A non-HTTP response was received
        /// </summary>
        public const int eMIS_HTTP_NOT_HTTP_RESPONSE = 353;


        /// <summary>
        /// A constant for MOCA error code 400 - General EVTLIB error
        /// </summary>
        public const int eEVT_BASE = 400; /* evtlib */

        /// <summary>
        /// A constant for MOCA error code 401 - Invalid argument
        /// </summary>
        public const int eEVT_INVALID_PARAMS = 401;

        /// <summary>
        /// A constant for MOCA error code 402 - Failed to create mailbox
        /// </summary>
        public const int eEVT_MBX_FAILURE = 402;

        /// <summary>
        /// A constant for MOCA error code 403 - System error
        /// </summary>
        public const int eEVT_SYSERR = 403;

        /// <summary>
        /// A constant for MOCA error code 404 - Event abort
        /// </summary>
        public const int eEVT_ABORT = 404;


        /// <summary>
        /// A constant for MOCA error code 500 - General SERVLIB error
        /// </summary>
        public const int eSRV_BASE = 500; /* srvlib */

        /// <summary>
        /// A constant for MOCA error code 501 - Invalid command
        /// </summary>
        public const int eSRV_INVALID_COMMAND = 501;

        /// <summary>
        /// A constant for MOCA error code 502 - Unexpected server error
        /// </summary>
        public const int eSRV_UNEXPECTED_ERROR = 502;

        /// <summary>
        /// A constant for MOCA error code 503 - Protocol error
        /// </summary>
        public const int eSRV_PROTOCOL_ERROR = 503;

        /// <summary>
        /// A constant for MOCA error code 504 - Datatype mismatch in results
        /// </summary>
        public const int eSRV_TYPE_MISMATCH = 504;

        /// <summary>
        /// A constant for MOCA error code 505 - Parse error in command
        /// </summary>
        public const int eSRV_PARSE_ERROR = 505;

        /// <summary>
        /// A constant for MOCA error code 506 - Server license is invalid
        /// </summary>
        public const int eSRV_LICENSE_INVALID = 506;

        /// <summary>
        /// A constant for MOCA error code 507 - Required argument is missing
        /// </summary>
        public const int eSRV_INSUFF_ARGUMENTS = 507;

        /// <summary>
        /// A constant for MOCA error code 508 - Server license has expired
        /// </summary>
        public const int eSRV_LICENSE_EXPIRED = 508;

        /// <summary>
        /// A constant for MOCA error code 509 - Server license not found
        /// </summary>
        public const int eSRV_LICENSE_NOTFOUND = 509;

        /// <summary>
        /// A constant for MOCA error code 510 - No data found.
        /// </summary>
        public const int eSRV_NO_ROWS_AFFECTED = 510;

        /// <summary>
        /// A constant for MOCA error code 511 - Database error
        /// </summary>
        public const int eSRV_DB_ERROR = 511;

        /// <summary>
        /// A constant for MOCA error code 512 - Database constraint violation
        /// </summary>
        public const int eSRV_DUPLICATE = 512;

        /// <summary>
        /// A constant for MOCA error code 513 - Product not licensed
        /// </summary>
        public const int eSRV_LICENSE_PRODUCT_INVALID = 513;

        /// <summary>
        /// A constant for MOCA error code 514 - Update or delete statement must include a WHERE clause
        /// </summary>
        public const int eSRV_MISSING_WHERE = 514;

        /// <summary>
        /// A constant for MOCA error code 515 - NULL result returned form command
        /// </summary>
        public const int eSRV_NULL_RETURNED = 515;

        /// <summary>
        /// A constant for MOCA error code 516 - Invalid function
        /// </summary>
        public const int eSRV_INVALID_FUNCTION = 516;

        /// <summary>
        /// A constant for MOCA error code 517 - Remote system name required
        /// </summary>
        public const int eSRV_REMOTE_SYSTEM_REQUIRED = 517;

        /// <summary>
        /// A constant for MOCA error code 518 - Unable to connect to remote system
        /// </summary>
        public const int eSRV_REMOTE_CONNECT_FAILURE = 518;

        /// <summary>
        /// A constant for MOCA error code 519 - Invalid object
        /// </summary>
        public const int eSRV_INVALID_OBJECT = 519;

        /// <summary>
        /// A constant for MOCA error code 520 - Automation error
        /// </summary>
        public const int eSRV_AUTOMATION_ERROR = 520;

        /// <summary>
        /// A constant for MOCA error code 521 - Division by zero
        /// </summary>
        public const int eSRV_DIVIDE_BY_ZERO = 521;

        /// <summary>
        /// A constant for MOCA error code 522 - Client disconnected
        /// </summary>
        public const int eSRV_DISCONNECTED = 522;

        /// <summary>
        /// A constant for MOCA error code 523 - Client must be logged in and is not
        /// </summary>
        public const int eSRV_AUTHENTICATE = 523;

        /// <summary>
        /// A constant for MOCA error code 524 - Protocol version not supported
        /// </summary>
        public const int eSRV_INVALID_VERSION = 524;

        /// <summary>
        /// A constant for MOCA error code 525 - Invalid system list
        /// </summary>
        public const int eSRV_SYSTEM_LIST_INVALID = 525;

        /// <summary>
        /// A constant for MOCA error code 526 - Unhandled data type
        /// </summary>
        public const int eSRV_UNHANDLED_TYPE = 526;

        /// <summary>
        /// A constant for MOCA error code 527 - Remote command setup failure
        /// </summary>
        public const int eSRV_REMOTE_PREPARE = 527;

        /// <summary>
        /// A constant for MOCA error code 528 - Remote command commit failure
        /// </summary>
        public const int eSRV_REMOTE_COMMIT = 528;

        /// <summary>
        /// A constant for MOCA error code 529 - Unknown adapter
        /// </summary>
        public const int eSRV_UNKNOWN_ADAPTER_CODE = 529;


        /// <summary>
        /// A constant for MOCA error code 600 - General XML Library error
        /// </summary>
        public const int eMXML_BASE = 600; /* mxmllib */

        /// <summary>
        /// A constant for MOCA error code 601 - Could not initialize XML engine
        /// </summary>
        public const int eMXML_INITIALIZE = 601;

        /// <summary>
        /// A constant for MOCA error code 602 - No XML parser found
        /// </summary>
        public const int eMXML_NO_PARSER = 602;

        /// <summary>
        /// A constant for MOCA error code 603 - No XML temp file name
        /// </summary>
        public const int eMXML_NO_TMP_FILENAME = 603;

        /// <summary>
        /// A constant for MOCA error code 604 - XML not supported
        /// </summary>
        public const int eMXML_NOT_SUPPORTED = 604;

        /// <summary>
        /// A constant for MOCA error code 605 - XML parse error
        /// </summary>
        public const int eMXML_PARSE_ERROR = 605;

        /// <summary>
        /// A constant for MOCA error code 606 - XML Write error
        /// </summary>
        public const int eMXML_WRITE_ERROR = 606;

        /// <summary>
        /// A constant for MOCA error code 610 - XML document create error
        /// </summary>
        public const int eMXML_CREATE_DOCUMENT = 610;

        /// <summary>
        /// A constant for MOCA error code 611 - XML element create error
        /// </summary>
        public const int eMXML_CREATE_ELEMENT = 611;

        /// <summary>
        /// A constant for MOCA error code 612 - XML document fragment create error
        /// </summary>
        public const int eMXML_CREATE_DOCFRAG = 612;

        /// <summary>
        /// A constant for MOCA error code 613 - XML text node create error
        /// </summary>
        public const int eMXML_CREATE_TEXT_NODE = 613;

        /// <summary>
        /// A constant for MOCA error code 614 - XML comment create error
        /// </summary>
        public const int eMXML_CREATE_COMMENT = 614;

        /// <summary>
        /// A constant for MOCA error code 615 - XML CDATA create error
        /// </summary>
        public const int eMXML_CREATE_CDATA_SECTION = 615;

        /// <summary>
        /// A constant for MOCA error code 616 - XML PI create error
        /// </summary>
        public const int eMXML_CREATE_PI = 616;

        /// <summary>
        /// A constant for MOCA error code 617 - XML attribute create error
        /// </summary>
        public const int eMXML_CREATE_ATTR = 617;

        /// <summary>
        /// A constant for MOCA error code 620 - XML could not get node type
        /// </summary>
        public const int eMXML_GET_NODE_TYPE = 620;

        /// <summary>
        /// A constant for MOCA error code 630- XML unknown node type
        /// </summary>
        public const int eMXML_UNKNOWN_NODE_TYPE = 630;


        /// <summary>
        /// A constant for MOCA error code 640 - XML cannot insert node before previous
        /// </summary>
        public const int eMXML_INSERT_BEFORE = 640;

        /// <summary>
        /// A constant for MOCA error code 641 - XML append child error
        /// </summary>
        public const int eMXML_APPEND_CHILD = 641;

        /// <summary>
        /// A constant for MOCA error code 642 - XML remove child error
        /// </summary>
        public const int eMXML_REMOVE_CHILD = 642;

        /// <summary>
        /// A constant for MOCA error code 643 - XML replace child error
        /// </summary>
        public const int eMXML_REPLACE_CHILD = 643;

        /// <summary>
        /// A constant for MOCA error code 644 - XML node clone error
        /// </summary>
        public const int eMXML_CLONE_NODE = 644;


        /// <summary>
        /// A constant for MOCA error code 650 - XML element has no child nodes
        /// </summary>
        public const int eMXML_HAS_CHILD_NODES = 650;


        /// <summary>
        /// A constant for MOCA error code 660 - XML could not get node name
        /// </summary>
        public const int eMXML_GET_NODE_NAME = 660;

        /// <summary>
        /// A constant for MOCA error code 661
        /// </summary>
        public const int eMXML_GET_NODE_VALUE = 661;

        /// <summary>
        /// A constant for MOCA error code 662 - XML could not get node value
        /// </summary>
        public const int eMXML_SET_NODE_VALUE = 662;

        /// <summary>
        /// A constant for MOCA error code 663 - XML could not get parent node
        /// </summary>
        public const int eMXML_GET_PARENT_NODE = 663;

        /// <summary>
        /// A constant for MOCA error code 664 - XML could not get child nodes
        /// </summary>
        public const int eMXML_GET_CHILD_NODES = 664;

        /// <summary>
        /// A constant for MOCA error code 665 - XML could not get first child
        /// </summary>
        public const int eMXML_GET_FIRST_CHILD = 665;

        /// <summary>
        /// A constant for MOCA error code 666 - XML could not get last child
        /// </summary>
        public const int eMXML_GET_LAST_CHILD = 666;

        /// <summary>
        /// A constant for MOCA error code 667 - XML could not get previous sibling
        /// </summary>
        public const int eMXML_GET_PREVIOUS_SIBLING = 667;

        /// <summary>
        /// A constant for MOCA error code 668 - XML could not get next sibling
        /// </summary>
        public const int eMXML_GET_NEXT_SIBLING = 668;

        /// <summary>
        /// A constant for MOCA error code 669 - XML could not get attributes
        /// </summary>
        public const int eMXML_GET_ATTRS = 669;


        /// <summary>
        /// A constant for MOCA error code 670 - XML could not get attribute
        /// </summary>
        public const int eMXML_GET_ATTR = 670;

        /// <summary>
        /// A constant for MOCA error code 671 - XML could not set attribute
        /// </summary>
        public const int eMXML_SET_ATTR = 671;

        /// <summary>
        /// A constant for MOCA error code 672 - XML could not remove attribute
        /// </summary>
        public const int eMXML_REMOVE_ATTR = 672;

        /// <summary>
        /// A constant for MOCA error code 673 - XML could not get attribute name
        /// </summary>
        public const int eMXML_GET_ATTR_NAME = 673;

        /// <summary>
        /// A constant for MOCA error code 674 - XML could not get attribute value
        /// </summary>
        public const int eMXML_GET_ATTR_VALUE = 674;

        /// <summary>
        /// A constant for MOCA error code 675 - XML could not set attribute value
        /// </summary>
        public const int eMXML_SET_ATTR_VALUE = 675;

        /// <summary>
        /// A constant for MOCA error code 676 - XML no get attribute specified
        /// </summary>
        public const int eMXML_GET_ATTR_SPECIFIED = 676;

        /// <summary>
        /// A constant for MOCA error code 677 - XML could not get attribute node
        /// </summary>
        public const int eMXML_GET_ATTR_NODE = 677;

        /// <summary>
        /// A constant for MOCA error code 678 - XML could not set attribute node
        /// </summary>
        public const int eMXML_SET_ATTR_NODE = 678;

        /// <summary>
        /// A constant for MOCA error code 679 - XML could not remove attribute node
        /// </summary>
        public const int eMXML_REMOVE_ATTR_NODE = 679;

        /// <summary>
        /// A constant for MOCA error code 680 - XML could not get elements
        /// </summary>
        public const int eMXML_GET_ELEMENTS = 680;

        /// <summary>
        /// A constant for MOCA error code 681 - XML could not get tag name
        /// </summary>
        public const int eMXML_GET_TAG_NAME = 681;

        /// <summary>
        /// A constant for MOCA error code 682 - XML could not get list length
        /// </summary>
        public const int eMXML_GET_LIST_LENGTH = 682;

        /// <summary>
        /// A constant for MOCA error code 683 - XML could not get item
        /// </summary>
        public const int eMXML_GET_ITEM = 683;

        /// <summary>
        /// A constant for MOCA error code 684 - XML could not get named item
        /// </summary>
        public const int eMXML_GET_NAMED_ITEM = 684;

        /// <summary>
        /// A constant for MOCA error code 685 - XML could not set named item
        /// </summary>
        public const int eMXML_SET_NAMED_ITEM = 685;

        /// <summary>
        /// A constant for MOCA error code 686 - XML could not remove named item
        /// </summary>
        public const int eMXML_REMOVE_NAMED_ITEM = 686;


        /// <summary>
        /// A constant for MOCA error code 687 - XML could not get PI target
        /// </summary>
        public const int eMXML_GET_PI_TARGET = 687;

        /// <summary>
        /// A constant for MOCA error code 688 - XML could not get PI data
        /// </summary>
        public const int eMXML_GET_PI_DATA = 688;

        /// <summary>
        /// A constant for MOCA error code 689 - XML could not set PI data
        /// </summary>
        public const int eMXML_SET_PI_DATA = 689;

        /// <summary>
        /// A constant for MOCA error code 690 - XML could not get CDATA
        /// </summary>
        public const int eMXML_GET_CDATA = 690;

        /// <summary>
        /// A constant for MOCA error code 691 - XML could not set CDATA
        /// </summary>
        public const int eMXML_SET_CDATA = 691;

        /// <summary>
        /// A constant for MOCA error code 692 - XML could not get CDATA length
        /// </summary>
        public const int eMXML_GET_CDATA_LENGTH = 692;

        /// <summary>
        /// A constant for MOCA error code 693 - XML could not get document element
        /// </summary>
        public const int eMXML_GET_DOCUMENT_ELEMENT = 693;


        /// <summary>
        /// A constant for MOCA error code 700 - General CMDLIB error
        /// </summary>
        public const int eCMD_BASE = 700; /* cmdlib */

        /// <summary>
        /// A constant for MOCA error code 701 - Repository not loaded
        /// </summary>
        public const int eCMD_REPOSITORY_NOT_LOADED = 701;

        /// <summary>
        /// A constant for MOCA error code 702 - Too many command name matches
        /// </summary>
        public const int eCMD_TOO_MANY_MATCHES = 702;


        /// <summary>
        /// A constant for MOCA error code 710 - Missing command repository
        /// </summary>
        public const int eCMD_MISSING_REPOSITORY = 710;

        /// <summary>
        /// A constant for MOCA error code 711 - Missing command level
        /// </summary>
        public const int eCMD_MISSING_LEVEL = 711;

        /// <summary>
        /// A constant for MOCA error code 712 - Missing command
        /// </summary>
        public const int eCMD_MISSING_COMMAND = 712;

        /// <summary>
        /// A constant for MOCA error code 713 - Missing trigger
        /// </summary>
        public const int eCMD_MISSING_TRIGGER = 713;

        /// <summary>
        /// A constant for MOCA error code 714 - Missing argument
        /// </summary>
        public const int eCMD_MISSING_ARGUMENT = 714;

        /// <summary>
        /// A constant for MOCA error code 715 - Missing command type
        /// </summary>
        public const int eCMD_MISSING_TYPE = 715;

        /// <summary>
        /// A constant for MOCA error code 716 - Missing data type
        /// </summary>
        public const int eCMD_MISSING_DATATYPE = 716;

        /// <summary>
        /// A constant for MOCA error code 717 - Missing Java namespace
        /// </summary>
        public const int eCMD_MISSING_NAMESPACE = 717;

        /// <summary>
        /// A constant for MOCA error code 718 - Missing .NET ProgID
        /// </summary>
        public const int eCMD_MISSING_PROGID = 718;

        /// <summary>
        /// A constant for MOCA error code 719 - Missing Local Syntax command
        /// </summary>
        public const int eCMD_MISSING_SYNTAX = 719;

        /// <summary>
        /// A constant for MOCA error code 720 - Missing C Function
        /// </summary>
        public const int eCMD_MISSING_FUNCTION = 720;

        /// <summary>
        /// A constant for MOCA error code 721 - Missing Method
        /// </summary>
        public const int eCMD_MISSING_METHOD = 721;

        /// <summary>
        /// A constant for MOCA error code 722 - Missing command directory
        /// </summary>
        public const int eCMD_MISSING_DIRECTORY = 722;

        /// <summary>
        /// A constant for MOCA error code 723 - Missing command node
        /// </summary>
        public const int eCMD_MISSING_NODE = 723;

        /// <summary>
        /// A constant for MOCA error code 724 - Missing value
        /// </summary>
        public const int eCMD_MISSING_VALUE = 724;

        /// <summary>
        /// A constant for MOCA error code 725 - Missing attribute
        /// </summary>
        public const int eCMD_MISSING_ATTRIBUTE = 725;


        /// <summary>
        /// A constant for MOCA error code 730 - Duplicate command level found
        /// </summary>
        public const int eCMD_DUPLICATE_LEVEL = 730;

        /// <summary>
        /// A constant for MOCA error code 731 - Duplicate command found at the same level
        /// </summary>
        public const int eCMD_DUPLICATE_COMMAND = 731;

        /// <summary>
        /// A constant for MOCA error code 732 - Duplicate trigger found at the same level
        /// </summary>
        public const int eCMD_DUPLICATE_TRIGGER = 732;

        /// <summary>
        /// A constant for MOCA error code 733 - Duplicate argument in command
        /// </summary>
        public const int eCMD_DUPLICATE_ARGUMENT = 733;


        /// <summary>
        /// A constant for MOCA error code 740 - Unknown command level
        /// </summary>
        public const int eCMD_UNKNOWN_LEVEL = 740;

        /// <summary>
        /// A constant for MOCA error code 741 - Unknown command
        /// </summary>
        public const int eCMD_UNKNOWN_COMMAND = 741;

        /// <summary>
        /// A constant for MOCA error code 742 - Unknown trigger
        /// </summary>
        public const int eCMD_UNKNOWN_TRIGGER = 742;

        /// <summary>
        /// A constant for MOCA error code 743 - Unknown Argument
        /// </summary>
        public const int eCMD_UNKNOWN_ARGUMENT = 743;

        /// <summary>
        /// A constant for MOCA error code 744 - Unknown Type
        /// </summary>
        public const int eCMD_UNKNOWN_TYPE = 744;

        /// <summary>
        /// A constant for MOCA error code 745 - Unknown data type
        /// </summary>
        public const int eCMD_UNKNOWN_DATATYPE = 745;

        /// <summary>
        /// A constant for MOCA error code 746 - Unknown file format
        /// </summary>
        public const int eCMD_UNKNOWN_FILE_FORMAT = 746;


        /// <summary>
        /// A constant for MOCA error code 800 - General MOCABASE error
        /// </summary>
        public const int eMOCA_BASE = 800; /* mocabase */

        /// <summary>
        /// A constant for MOCA error code 801 - Invalid password
        /// </summary>
        public const int eMOCA_INVALID_PASSWORD = 801;

        /// <summary>
        /// A constant for MOCA error code 802 - Missing argument
        /// </summary>
        public const int eMOCA_MISSING_ARG = 802;

        /// <summary>
        /// A constant for MOCA error code 803 - SMTP connection failure
        /// </summary>
        public const int eMOCA_SMTP_COM_FAILURE = 803;

        /// <summary>
        /// A constant for MOCA error code 804 - FTP connection failure
        /// </summary>
        public const int eMOCA_FTP_COM_FAILURE = 804;

        /// <summary>
        /// A constant for MOCA error code 805 - SOAP message error
        /// </summary>
        public const int eMOCA_SOAP_UNKNOWN_ERROR = 805;

        /// <summary>
        /// A constant for MCS error code 831 - A password confirmation was required and not provided.
        /// </summary>
        public const int eMCS_DIGITAL_SIGNATURE_CHALLENGE = 831; 
 
        /// <summary>
        /// A constant for MCS error code 832 - 
        /// A password confirmation was required and you do not have credentials to perform the operation.
        /// </summary>
        public const int eMCS_DIGITAL_SIGNATURE_CHALLENGE_WITH_OVERRIDE = 832;

        /// <summary>
        /// A constant for MOCA error code 850 - EMS host missing
        /// </summary>
        public const int eEMS_MISSING_HOST = 850; /* mocaems */

        /// <summary>
        /// A constant for MOCA error code 851 - EMS port mising
        /// </summary>
        public const int eEMS_MISSING_PORT = 851;

        /// <summary>
        /// A constant for MOCA error code 852 - EMS event name missing
        /// </summary>
        public const int eEMS_MISSING_EVENT_NAME = 852;

        /// <summary>
        /// A constant for MOCA error code 853 - EMS source system missing
        /// </summary>
        public const int eEMS_MISSING_SOURCE_SYSTEM = 853;

        /// <summary>
        /// A constant for MOCA error code 854 - EMS key value missing
        /// </summary>
        public const int eEMS_MISSING_KEY_VALUE = 854;

        /// <summary>
        /// A constant for MOCA error code 855 - EMS invalid event name
        /// </summary>
        public const int eEMS_INVALID_EVENT_NAME = 855;

        /// <summary>
        /// A constant for MOCA error code 856 - EMS bad XML
        /// </summary>
        public const int eEMS_BAD_XML = 856;

        /// <summary>
        /// A constant for MOCA error code 857 - EMS prime lock
        /// </summary>
        public const int eEMS_PRIME_LOCK_EVENT = 857;

        /// <summary>
        /// A constant for MOCA error code 858 - EMS could not create file
        /// </summary>
        public const int eEMS_FILE_CREATE = 858;

        /// <summary>
        /// A constant for MOCA error code 859 - EMS could not remove file
        /// </summary>
        public const int eEMS_FILE_REMOVE = 859;

        /// <summary>
        /// A constant for MOCA error code 860 - EMS could not generate xml
        /// </summary>
        public const int eEMS_GENERATE_XML = 860;

        /// <summary>
        /// A constant for MOCA error code 861 - EMS could log event
        /// </summary>
        public const int eEMS_LOG_EVENT = 861;


        /// <summary>
        /// A constant for MOCA error code 999 - Last MOCA event (not used)
        /// </summary>
        public const int eMOCA_LAST_ONE_DONT_REMOVE = 999;

        /// <summary>
        /// A constant for MOCA error code 1000 - User login is invalid.
        /// </summary>
        public const int eMOCA_INVALID_LOGIN = 1000;

        /// <summary>
        /// A constant for MOCA error code 1020 - User account locked out.
        /// </summary>
        public const int eMCS_ACCOUNT_LOCK_OUT = 1020;

        /*  
         *  These are errors returned from Oracle.
         *
         *  The enumerated values are only defined 
         *  so that programs may check them.  When
         *  a routine returns with an Oracle error; 
         *  a negative return code is returned and
         *  a global error string is filled in; if
         *  possible.
         *
         *  Enumerated values should be added to
         *  this list as needed.  The values should
         *  match those documented in the "Oracle
         *  Server Messages and Codes Manual".
         */

        /// <summary>
        /// A constant for MOCA error code 0 - Success.
        /// </summary>
        public const int eDB_NORMAL = 0; /* Oracle */

        /// <summary>
        /// A constant for MOCA error code -1 - Database constraint violation.
        /// </summary>
        public const int eDB_UNIQUE_CONS_VIO = -1;

        /// <summary>
        /// A constant for MOCA error code -54 - Database Lock timeout
        /// </summary>
        public const int eDB_LOCK_TIMEOUT = -54;

        /// <summary>
        /// A constant for MOCA error code -60 - Database deadlock 
        /// </summary>
        public const int eDB_DEADLOCK = -60;

        /// <summary>
        /// A constant for MOCA error code -979 - Invalid GROUP BY Expression
        /// </summary>
        public const int eDB_NOT_A_GROUPBY_EXPR = -979;

        /// <summary>
        /// A constant for MOCA error code -1000 - Maximum Cursors exceeded
        /// </summary>
        public const int eDB_MAX_CURSORS = -1000;

        /// <summary>
        /// A constant for MOCA error code -1001 - Invalid Database cursor
        /// </summary>
        public const int eDB_INVALID_CURSOR = -1001;

        /// <summary>
        /// A constant for MOCA error code -1012 - Could not log into database
        /// </summary>
        public const int eDB_NOT_LOGGED_IN = -1012;

        /// <summary>
        /// A constant for MOCA error code -1013 - Database connection interrupted
        /// </summary>
        public const int eDB_INTERRUPTED = -1013;

        /// <summary>
        /// A constant for MOCA error code -1403 - No rows returned from query or command
        /// </summary>
        public const int eDB_NO_ROWS_AFFECTED = -1403;

        /// <summary>
        /// A constant for MOCA error code -3113 - EOF on database communication channel
        /// </summary>
        public const int eDB_EOF_ON_COMMCHANNEL = -3113;

        /// <summary>
        /// A constant for MOCA error code -3114 - Could not create database connection
        /// </summary>
        public const int eDB_NOT_CONNECTED = -3114;

        /// <summary>
        /// A constant for MOCA error code -12705 - Invalid database NLS parameter
        /// </summary>
        public const int eDB_INVALID_NLS_PARAMETER = -12705;

        /// <summary>
        /// A constant for MOCA error code -28500 - Could not connect to Oracle database
        /// </summary>
        public const int eDB_NON_ORACLE_CONN_ERROR = -28500;

        /* ADO -> SQL Server Disconnect Error */

        /// <summary>
        /// A constant for MOCA error code -2147467259 - ADO could not create DB connection
        /// </summary>
        public const int eDB_ADO_NOT_CONNECTED = -2147467259;

        /// <summary>
        /// Checks the result status to verify that the command
        /// either returned an <see cref="eOK"/> or 
        /// <see cref="eDB_NO_ROWS_AFFECTED"/> or 
        /// <see cref="eSRV_NO_ROWS_AFFECTED"/> status.
        /// </summary>
        /// <param name="status">The status to check</param>
        /// <returns></returns>
        public static bool StatusReturnsResults(int status)
        {
            return (status == eDB_NO_ROWS_AFFECTED ||
                    status == eSRV_NO_ROWS_AFFECTED ||
                    status == eOK);
        }
    }
}
