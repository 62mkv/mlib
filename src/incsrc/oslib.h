/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file for oslib.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2016
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

#ifndef OSLIB_H
#define OSLIB_H

/*
 *  O/S-Specific Definitions
 */

#ifdef WIN32

   /*
    *  Win32 hides the specifics of the SOCKET data type,
    *  so we need includes for this definition.
    */
   #include <winsock2.h>
   #include <windows.h>
   #include <direct.h>
   #include <io.h>

   typedef SOCKET SOCKET_FD;
   typedef HANDLE PIPE_FD;
   typedef HANDLE OS_THREAD;
   typedef HANDLE OS_MUTEX;

   #define osChdir     _chdir
   #define osPopen     _popen
   #define osPclose    _pclose
   #define osSnprintf  _snprintf
   #define osVsnprintf _vsnprintf
   #define osUnlink    _unlink

   #define OS_DEFAULT_EDITOR "notepad"
   #define OS_NULL_DEVICE    "NUL"

#else /* Unix */

   /*
    * HP-UX and LINUX place the type definitions for the POSIX threads
    * types in a different header file that other flavors of UNIX.
    */

   #include <unistd.h>
#if defined HPUX || defined LINUX
   #include <pthread.h>
#else
   #include <sys/types.h>
#endif

   typedef int             SOCKET_FD;
   typedef int             PIPE_FD;
   typedef pthread_t       OS_THREAD;
   typedef pthread_mutex_t OS_MUTEX;

   #define SOCKET_ERROR   -1
   #define INVALID_SOCKET -1

   #define osChdir     chdir
   #define osPopen     popen
   #define osPclose    pclose
   #define osSnprintf  snprintf
   #define osVsnprintf vsnprintf
   #define osUnlink    unlink

   #define OS_DEFAULT_EDITOR "vi"
   #define OS_NULL_DEVICE    "/dev/null"

#endif

/*
 *  O/S Panic Macro Definition
 */

#ifndef OS_PANIC
#define OS_PANIC (osPanic(__FILE__, __LINE__))
#endif

/*
 *  Mapping Mode Definitions for osMapFile( )
 */

#define OS_MAPMODE_R	0x01
#define OS_MAPMODE_W 	0x02
#define OS_MAPMODE_RW	(OS_MAPMODE_R|OS_MAPMODE_W)
#define OS_MAPMODE_RO	OS_MAPMODE_R

/*
 *  Flag Definitions for osSockRecv( ) / osSockSend( )
 */

#define OS_SOCK_PEEK    1
#define OS_SOCK_OOB     2
#define OS_SOCK_NOWAIT  4

/*
 *  Definitions for osTCPListen( )
 */

#define OS_MAX_BACKLOG	0
#define OS_SOMAXCONN    20

/*
 *  Definitions for osRecvFile( ) / osSendFile( )
 */

#define OS_SF_SENDFILE  1
#define OS_SF_NOFILE    2
#define OS_SF_SHUTDOWN  3
#define OS_SF_CLOSETCP  4
#define OS_SF_ERROR     5

/*
 *  Socket Error Code Definitions
 */

#define OS_ECONNRESET	1
#define OS_EPIPE	2
#define OS_ENOTCONN	3
#define OS_EINTR	4
#define OS_EERROR	5
#define OS_EAGAIN	6

/*
 *  Flags Definitions for osMBXRecv( ) / osMBXSend( )
 */

#define OS_MBX_NOWAIT   1

/*
 *  Log Level Definitions for osLogEvent( )
 */

#define OS_EVT_ERROR    1
#define OS_EVT_INFORM   2
#define OS_EVT_WARNING  3

/*
 *  Miscellaneous Definitions
 */

#define OS_MBXNAME_LEN 32
#define OS_QUEUEID_LEN 10

/*
 *  File Type Definitions osFileInfo( )
 */

#define OS_FILETYPE_NULL '?'
#define OS_FILETYPE_DIR  'D'
#define OS_FILETYPE_FILE 'F'

/*
 *  Flags Definitions for osFindFile( )
 */

#define OS_FF_DATESORT   0
#define OS_FF_FULLPATH   1
#define OS_FF_NAMESORT   2
#define OS_FF_NOSORT     4

/*
 *  Process Priority Definitions for osProcessPriority( )
 */

#ifdef UNIX
# define MOCA_PRIORITY_LOW     10
# define MOCA_PRIORITY_NORMAL  0
# define MOCA_PRIORITY_HIGH    -10
#else
# define MOCA_PRIORITY_LOW     IDLE_PRIORITY_CLASS
# define MOCA_PRIORITY_NORMAL  NORMAL_PRIORITY_CLASS
# define MOCA_PRIORITY_HIGH    HIGH_PRIORITY_CLASS
#endif

/*
 *  Access Mode Definitions for osAccess( )
 */

#ifdef UNIX
# define OS_ACCESS_EXISTS     F_OK
# define OS_ACCESS_WRITE      W_OK
# define OS_ACCESS_READ       R_OK
#else
# define OS_ACCESS_EXISTS     0
# define OS_ACCESS_WRITE      2
# define OS_ACCESS_READ       4
#endif

/*
 * File Encoding Definitions
 */
#define BOM_UTF8              "\xEF\xBB\xBF"
#define BOM_UTF16_LE          "\xFF\xFE"
#define BOM_UTF16_BE          "\xFE\xFF"
#define BOM_UTF32_LE          "\xFF\xFE\x00\x00"
#define BOM_UTF32_BE          "\x00\x00\xFE\xFF"

#define ENCODING_LITTLE_ENDIAN   0
#define ENCODING_BIG_ENDIAN      1

#define ENCODING_ANSI         0
#define ENCODING_UTF8         8
#define ENCODING_UTF16       16
#define ENCODING_UTF16_BE    17
#define ENCODING_UTF32       32
#define ENCODING_UTF32_BE    33

/*
 *  Time Type Definition
 */

typedef struct
{
   long sec;
   unsigned short msec;
} OS_TIME;

/*
 *  INI File Type Definition
 */

typedef struct osIniEntry_s OS_INI_FILE;

/*
 *  Find File Context Type Definition for osFindFile( )
 */

typedef void *OS_FF_CONTEXT;

/*
 *  TCP Address Type Definition for osRecvFile( ) / osSendFile( )
 */

typedef struct
{
    char ip[16];
    unsigned short port;
} OS_TCP_ADDR;

/*
 *  Mailbox & Mailbox List Type Definitions
 */

#ifndef WIN32

typedef struct
{
    int  pipefd;
    long max_msg;
    char mbxname[OS_MBXNAME_LEN+1];
    long subproc_pid;
    int queue_id;
    long msgsize;
} OSMBX;

typedef struct
{
    char mbxname[OS_MBXNAME_LEN+1];
    char queue_id[OS_QUEUEID_LEN+1];
} OS_MBX_LIST;

OS_MBX_LIST *osMBXGetList(void);

#else

typedef struct
{
    char path[256];
    long max_msg;
    HANDLE hPipe;
} OSMBX;

#endif

/*
 *  Function Pointer Type Definition
 */

typedef void (*OSFPTR)();

/*
 *  External Global Variable Definitions for osGetopt( )
 */

extern LIBIMPORT char *osOptarg;
extern LIBIMPORT int osOptind, osOpterr, osOptopt;

/*
 *  Function Prototypes
 */

#if defined (__cplusplus)
extern "C" {
#endif

/* osAccess.c */
int osAccess(char *pathname, int mode);

/* osAsyncIO.c */
long osStartAsyncIO(SOCKET_FD fd, void (*handler)(int));
long osStopAsyncIO(SOCKET_FD fd);

/* osAtexit.c */
int  osExit(int code);
int  osAtexit(void (*function)(void));
int  osAtexitClean(void (*function)(void));
void os_CleanupAtexit(void);
void os_CleanupAtexitClean(void);

/* osAuthenticateUser.c */
long osAuthenticateUser(char *logname, char *password);

/* osBaseFile.c */
char *osBaseFile(char *pathname);

/* osCodePages.c */
long   osIsExtCodePageSet(void);
long   osWillConvertCP(char *overrideExtCodePage);
size_t osToExtCPArray(char *intStr, char *extStr, size_t extSize,
		      char *overrideExtCodePage);
size_t osToExtCPPtr(char *intStr, char **extStr, char *overrideExtCodePage);
size_t osToIntCPArray(char *extStr, char *intStr, size_t intSize,
		      char *overrideExtCodePage);
size_t osToIntCPPtr(char *extStr, char **intStr, char *overrideExtCodePage);
#ifdef UNIX
char *osGetExternalCodePage(void);
#else
UINT osGetExternalCodePage(void);
#endif
int    osGetFileEncoding(FILE *fp, int stripBOM);

/* osCopyFile.c */
long osCopyFile(char *source, char *destination, long mode);

/* osDirName.c */
char *osDirName(char *pathname);

/* osDirectory.c */
long  osCreateDir(char *directory);
long  osRemoveDir(char *directory);
char *osGetCurrentDir(void);

/* osDumpCore.c */
void osDumpCore(void);

/* osEncodeDescriptor.c */
long osEncodeDescriptor(PIPE_FD descriptor, char *text);
long osDecodeDescriptor(char *text, PIPE_FD *descriptor);

/* osError.c */
long  osErrno(void);
char *osError(void);
char *osStrError(long number);

/* osFileInfo.c */
long osFileInfo(char *filename, char *filetype);
long osFileAccessed(char *filename, char **datetime);
long osFileCreated(char *filename, char **datetime);
long osFileModified(char *filename, char **datetime);
long osFileSize(char *filename, long *size);

/* osFilterTCP.c */
long  osFilterTCP(SOCKET_FD fd);
long  osFilterTCPAdd(char *ruleset, char *ruletext);
long  osFilterTCPClear(char *ruleset);
char *osFilterTCPList(int n);

/* osFindFile.c */
long osFindFile(char *spec, char *filename, OS_FF_CONTEXT *context, int flags);
long osEndFindFile(OS_FF_CONTEXT *context);

/* osFopen.c */
FILE *osFopen(char *path, char *mode);

/* osGetHostSerialNumber.c */
char *osGetHostSerialNumber(void);

/* osGetProcessId.c */
unsigned long osGetProcessId(void);

/* osGetLogin.c */
char *osGetLogin(char * buffer, int buffer_len);

/* osGetTime.c */
OS_TIME *osGetTime(OS_TIME *tb);

/* osGetopt.c */
int osGetopt(int argc, char *argv[], char *opts);

/* osIniFile.c */
long osOpenIniFile(OS_INI_FILE **iniFile, char *pathname);
void osCloseIniFile(OS_INI_FILE *iniFile);
char *osGetIniValue(OS_INI_FILE *iniFile, char *section, char *name);
char *osEnumerateIniSections(OS_INI_FILE *iniFile,
                             void **context,
                             char **section);
char *osEnumerateIniValues(OS_INI_FILE *iniFile,
                           void **context,
                           char *section,
                           char **name,
                           char **value);

/* osInit.c */
void osInit(void);

/* osLaunchJavaApplication.c */
int osLaunchJavaApplication(int is32bit, char *appnane, char *classname, int argc, char *argv[]);
int osLaunchJavaApplicationUseArgs(int is32bit, char *appnane, char *classname, int argc, char *argv[]);

/* osLibrary.c */
void   *osLibraryOpen(char *libname);
void   *osLibraryOpenByMember(char *libname, char *member);
void    osLibraryClose(void *handle);
OSFPTR  osLibraryLookupFunction(void *handle, char *symbol);
char   *osLibraryError(void);

/* osLockMem.c */
long osLockMem(void *mem);
long osUnlockMem(void *mem);

/* osLogEvent.c */
void osLogEvent(int type, char *msg);

/* osMBX.c */
OSMBX *osMBXCreate(char *mbxname, long max_msg);
long   osMBXClose(OSMBX *mbx);
long   osMBXKill(char *mbxname);
long   osMBXSend(char *mbxname, void *buffer, long size, int flags);
long   osMBXRecv(OSMBX *mbx, void *buffer, long size, long *msgsize, int flags);
long   osMBXRecvTimeout(OSMBX *mbx, void *buffer, long size,
			long *msgsize, int timeout);

/* osMapFile.c */
long osCreateMapFile(char *infile, long size);
long osDeleteMapFile(char *infile);
long osMapFile(char *infile, void **mem, long mode);
long osUnmapFile(void *mem);

/* osMutex.c */
long osCreateMutex(OS_MUTEX *mutex);
long osTryLockMutex(OS_MUTEX *mutex);
long osLockMutex(OS_MUTEX *mutex);
long osUnlockMutex(OS_MUTEX *mutex);
long osDestroyMutex(OS_MUTEX *mutex);

/* osPanic.c */
void osPanic(char *filename, long line);

/* osPipe.c */
long osPipe(PIPE_FD fd[2]);

/* osPrintFile.c */
long osPrintFile(char *filename, char *device, int copies, char *options,
		 int raw);

/* osProcessPriority.c */
long osSetProcessPriority(long priority);

/* osRecvFile.c */
#ifdef WIN32
long osRecvFile(PIPE_FD pipefd, long *controlword, SOCKET_FD *fd,
	        OS_TCP_ADDR *tcp_addr, HANDLE hEvent);
#else
long osRecvFile(PIPE_FD pipefd, long *controlword, SOCKET_FD *fd,
	        OS_TCP_ADDR *tcp_addr);
#endif

/* osRegistry.c */
char *osGetReg(char *section, char *name);
char *osGetRegNotExpanded(char *section, char *name);
char *osGetRegistryValue(char *section, char *name);
char *osGetRegistryValueNotExpanded(char *section, char *name);

char *osEnumerateRegistry(char *section, void **context, char **name, 
                          char **value);

/* osSendFile.c */
#ifdef WIN32
long osSendFile(PIPE_FD pipefd, long controlword, SOCKET_FD fd,
	        OS_TCP_ADDR *tcp_addr, DWORD dwPid, HANDLE hEvent );
#else
long osSendFile(PIPE_FD pipefd, long controlword, SOCKET_FD fd,
	        OS_TCP_ADDR *tcp_addr);
#endif

/* osSignal.c */
void osDumpSignalMask(void);

/* osSleep.c */
void osSleep(int sec, int msec);

/* osSockError.c */
char *osSockError(void);
long  osSockErrno(void);

/* osSocket.c */
long osSockInit(void);
long osSockClose(SOCKET_FD fd);
long osSockShutdown(SOCKET_FD fd);
long osSockAccept(SOCKET_FD fd, SOCKET_FD *newfd, int flags);
long osSockAddress(SOCKET_FD fd, char *outstr, long size, unsigned short *port);
long osSockLocalAddress(SOCKET_FD fd,
			char *outstr,
			long size,
			unsigned short *port);
int  osSockSend(SOCKET_FD fd, void *msg, int len, int flags);
int  osSockRecv(SOCKET_FD fd, void *msg, int len, int flags);
int  osSockRecvTimeout(SOCKET_FD fd, void *msg, int len, int flags,int timeout);
long osSockWait(SOCKET_FD fd, int do_read);
long osSockWaitTimeout(SOCKET_FD fd, int timeout);
long osSockBlocking(SOCKET_FD fd, int Blocking);

/* osTCP.c */
long  osTCPConnect(SOCKET_FD *desc, char *host, unsigned short port);
long  osTCPConnectTimeout(SOCKET_FD *desc,
			  char *host,
			  unsigned short port,
			  long timeout);
long  osTCPConnectWithBind(SOCKET_FD *desc,
			   char *host,
			   unsigned short port,
			   SOCKET_FD my_desc);
long  osTCPListen(SOCKET_FD *desc, unsigned short port, int backlog, int reuse);
char *osTCPAddrToName(char *hostip);
char *osTCPNameToAddr(char *hostname);
long  osTCPHostname(char *hostname, long hostsize);
long  osTCPHostAddr(char *ipaddr, long addrsize);
long  osTCPKeepalive(SOCKET_FD fd, int onoff);
long  osTCPNodelay(SOCKET_FD fd, int onoff);

/* osThread.c */
void os_SetInitialThread(void);
long osIsInitialThread(void);
void osSignalInitialThread(int signo);
long osCreateThread(OS_THREAD *thread, void *(*fptr)(void *), void *arg);
long osJoinThread(OS_THREAD thread, void **valuePtr);
void osExitThread(void *valuePtr);
long osThreadIsRunning(OS_THREAD thread);

/* osVar.c */
char *osGetVar(char *name);
void  osPutVar(char *name, char *value, char *tag);
void  osRemoveVar(char *name, char *tag);

#if defined (__cplusplus)
}
#endif

#endif /* OSLIB_H */
