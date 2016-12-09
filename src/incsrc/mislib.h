/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Public header file for mislib.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2002
 *  RedPrairie Corporation
 *  All Rights Reserved
 *
 *  This software is furnished under a corporate license for use on a
 *  single computer system and can be copied (with inclusion of the
 *  above copyright) only for use on such a system.
 *
 *  The information in this document is subject to change without notice
 *  and should not be construed as a commitment by RedPrairie Corporation.
 *
 *  RedPrairie Corporation assumes no responsibility for the use of the
 *  software described in this document on equipment which has not been
 *  supplied or approved by RedPrairie Corporation.
 *
 *  $Copyright-End$
 *
 *#END*************************************************************************/

#ifndef MISLIB_H
#define MISLIB_H

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <oslib.h>

/*
 *  Debugging Malloc Definitions
 */

#if defined(MOCA_DEBUG_MALLOC) && !defined(MOCA_DEBUG_MALLOC_IGNORE)
# define free(x)      misDebugFree   ("free",   __FILE__,__LINE__, x, #x)
# define calloc(x,y)  misDebugCalloc ("calloc", __FILE__,__LINE__, x, y, #x, #y)
# define malloc(x)    misDebugMalloc ("malloc", __FILE__,__LINE__, x, #x, NULL)
# define realloc(x,y) misDebugRealloc("realloc",__FILE__,__LINE__, x, y, #x, #y)
#endif

/*
 *  Tracing Level Definitions
 */

#define T_FLOW    (1<<0)
#define T_SQL     (1<<1)
#define T_MGR     (1<<2)
#define T_SERVER  (1<<3)
#define T_SRVARGS (1<<4)
#define T_PERF    (1<<5)
#define T_PID     (1<<6)

/*
 *  Error Stack Operation Definitions
 */

#define ES_START  (1<<0)
#define ES_ADD    (1<<1)
#define ES_DUMP   (1<<2)

/*
 *  Function Mapping Definitions
 */

/*
 *  This macro determines whether a character is
 *     the 2nd, 3rd or 4th byte of a utf-8 character 
 *  UTF-8 Characters look like this:
 *  Unicode             Bytes    Byte 1     Byte 2     Byte 3     Byte 4
 *  U+0000 .. U+007F    1 byte	 0xxx xxxx
 *  U+0080 .. U+07FF	2 bytes	 110x xxxx  10xx xxxx	 	 
 *  U+0800 .. U+FFFF    3 bytes	 1110 xxxx  10xx xxxx  10xx xxxx	 
 *  U+10000 .. U+10FFFF 4 bytes	 1111 0xxx  10xx xxxx  10xx xxxx  10xx xxxx
 *
 *  If a character is the 2nd through 4th bytes, it will always start with 
 *  the bits 10.  10 = 0x80.    11 = 0xC0.  If a bitwise AND is done on the
 *  byte with 0xC0 and the result is 0x80, the byte starts with 10.
 */
#define misCharIsExt(in) (((in) & 0xC0) == 0x80)

/*
 *  Socket Error Definitions
 */

#define MIS_SOCK_EOF     -1
#define MIS_SOCK_ERROR   -2
#define MIS_SOCK_TIMEOUT -3

/*
 *  Tracing Definitions
 */

#define TRACE_COMM_LEN      20

#define DEFAULT_TRACE_FILE  "$LESDIR/log/redprairie.log"

/*
 *  Execution Status Definitions
 */

#define MOCASTAT_INCOMING_CMD 1
#define MOCASTAT_EXEC_CMD     2
#define MOCASTAT_EXEC_SQL     3
#define MOCASTAT_COMMENT      4
#define MOCASTAT_ENVIRONMENT  5
#define MOCASTAT_STATE        6
#define MOCASTAT_KEEPALIVE    7

/*
 *  Socket Type Definition
 */

typedef struct mis_Socket misSocket;

/*
 *  Hash Type Definition
 */

typedef struct mis_Hash MISHASH;
typedef struct mis_Cache MISCACHE;

/*
 *  Function Pointer Type Definition
 */

typedef void (*MSFPTR)();

/*
 *  HTTP Request Types
 */

typedef enum misHTTPRequestType
{
    HTTP_REQUEST_GET,
    HTTP_REQUEST_HEAD,
    HTTP_REQUEST_POST,
    HTTP_REQUEST_PUT,
    HTTP_REQUEST_OPTIONS,
    HTTP_REQUEST_TRACE
} misHTTPRequestType;

/*
 * Keepalive List Definition
 */

struct mis_Keepalive
{
    char id[25];
    int counter;
    void (*rollback)(void);
};

/*
 *  Function Prototypes
 */

#if defined (__cplusplus)
extern "C" {
#endif

/* mislib.c */
void  misSetDebug(char *status);
int   misDebug(void);
void  misSetLibDebug(char *status);
int   misLibDebug(void);

long  misCiStrcmp(char *s1, char *s2);
long  misCiStrncmp(char *s1, char *s2, long len);
long  misCiStrncmpN(char *s1, char *s2, long len);
long  misStrncmpN(char *s1, char *s2, long len);
char *misStrncpyN(char *dest, char *src, long len);
char *misStrncatN(char *dest, char *src, long len);

char *misCiStrstr(char *s1, char *s2);
char *misCiStrReplace(char *s1, char *s2, char *s3);
char *misCiStrReplaceAll(char *s1, char *s2, char *s3);

char *misStrReplace(char *s1, char *s2, char *s3);
char *misStrReplaceAll(char *s1, char *s2, char *s3);

char *misReplaceChars(char *input_string, char *search_string,
                      char *replace_string);

/* UTF8 Note:  This function will work unless we need to do unicode capable
 * searching for strings.  Collation rules may define that the sequence of
 * a set of particular characters may not matter in regards to equality.
 * At this time we'll leave it, but as we look more indepth at collation
 * rules, this may have to be touched */
char *misStrtok(char *haystack, char *needle);

long  misTrimStrncmp(char *s1, char *s2, long len);
long  misTrimStrncmpN(char *s1, char *s2, long len);

char *misTrim(char *str);
char *misTrimR(char *str);
char *misTrimLR(char *str);

char *misToUpper(char *str);
char *misToLower(char *str);

/* misAsciiToHex.c */
char *misAsciiToHex(char *str, long len);

/* misBanner.c */
char *misGetStartBanner(char *appname);
char *misGetVersionBanner(char *appName);
char *misGetVersion(void);

/* misBase64.c */
long misBase64Encode(char *i_raw, long i_raw_len, char **o_str);
long misBase64Decode(char *i_Base64, unsigned char **o_raw, long *o_len );

/* misBaseConv.c */
long misBaseConv(const char *inum, int ibase, char *onum, int obase, int size);
long misDecToBin(char *decString, char *binString, long binLength);
long misBinToDec(char *binString, char *decString, long decLength);
long misBinToHex(char *binString, char *hexString, long hexLength);
long misHexToBin(char *hexString, char *binString, long binLength);

/* misBlowfishPassword.c */
char *misBlowfishEncodePassword(char *cleartext);
char *misBlowfishDecodePassword(char *ciphertext);
short misIsBlowfishEncodedPassword(char *ciphertext);
short misMatchBlowfishEncodedPassword(char *cleartext, char *ciphertext);

/* misBuildInsertList.c */
long misBuildInsertList(char *colnam, char *colval,
                        long maxlen, char **columnList, char **valueList);
long misBuildInsertListN(char *colnam, char *colval,
                        long maxlen, char **columnList, char **valueList);

/* misBuildInsertListType.c */
long misBuildInsertListType(char *colnam, char dataType, void *colval,
                        long maxlen, char **columnList, char **valueList);
long misBuildInsertListTypeN(char *colnam, char dataType, void *colval,
                        long maxlen, char **columnList, char **valueList);

/* misBuildInsertListDBKW.c */
long misBuildInsertListDBKW(char *colnam, char *colval,
                        long maxlen, char **columnList, char **valueList);
long misBuildInsertListDBKWN(char *colnam, char *colval,
                        long maxlen, char **columnList, char **valueList);

/* misBuildUpdateList.c */
long misBuildUpdateList(char *colnam, char *colval, long maxlen,
                        char **updateList);
long misBuildUpdateListN(char *colnam, char *colval, long maxlen,
                        char **updateList);

/* misBuildUpdateListType.c */
long misBuildUpdateListType(char *colnam, char dataType,
                        void *colval, long maxlen,
                        char **updateList);
long misBuildUpdateListTypeN(char *colnam, char dataType,
                        void *colval, long maxlen,
                        char **updateList);

/* misBuildUpdateListDBKW.c */
long misBuildUpdateListDBKW(char *colnam, char *colval, long maxlen,
                        char **updateList);
long misBuildUpdateListDBKWN(char *colnam, char *colval, long maxlen,
                        char **updateList);


/* misBuildWhereList.c */
long misBuildWhereList(char *colnam, char *colval, long maxlen,
            char **whereList);
long misBuildWhereListN(char *colnam, char *colval, long n_maxlen,
            char **whereList);

/* misCompress.c */
char *misZVersion(void);
char *misZError(int error);

long misZCompress(char **dest, long *destlen, char *src, long *status);
long misZUncompress(char **dest, long *destlen, char *src, long srclen,
                long *status );

/* misDatetime.c */
char *misFormatDate(char *format);
char *misFormatTime(char *format);
char *misFormatDateTime(char *date_format, char *time_format);
char *misFormatMOCADate(char *moca_dt, char *format);
char *misFormatMOCATime(char *moca_dt, char *format);
char *misFormatMOCADateTime(char *moca_dt, char *date_format, char *time_format);

long  misValidateDatetime(char *yyyymmddhhmiss, int fullyValidateYear);
long  misValidateDatetimeFormat(char *fmt);

/* misDT.c */
typedef struct {
    int days;
    int seconds;
} MIS_DTVALUE;

long misDTFormat(char *date, long size, MIS_DTVALUE *val);
long misDTParse(char *date, MIS_DTVALUE *val);
long misDTAddDays(MIS_DTVALUE *val, double days);
double misDTCompare(MIS_DTVALUE *l, MIS_DTVALUE *r);

/* misDMalloc.c */
void  misDebugReport(char *file, const int line);
void *misDebugMalloc(char *call, char *file, int line, size_t size, ...);
void *misDebugCalloc(char *call, char *file, int line, int nel,
                 size_t size, ...);
void  misDebugFree(char *call, char *file, int line, void *addr, ...);
void *misDebugRealloc(char *call, char *file, int line, void *ptr,
                  size_t size, ...);

void misFlagCachedMemory(MSFPTR function, void *addr);
void misRemoveCachedMemoryEntry(void *addr);
void misReleaseCachedMemory(void);

/* misDynStr.c */
char *misDynCharcat(char **s1, const char s2);
char *misDynStrncat(char **s1, const char *s2, size_t count);
char *misDynStrncatN(char **s1, const char *s2, size_t count);
char *misDynStrncpy(char **s1, const char *s2, size_t count);
char *misDynStrncpyN(char **s1, const char *s2, size_t count);
char *misDynStrcat(char **s1, const char *s2);
char *misDynStrcpy(char **s1, const char *s2);
long  misDynSprintf(char **buffer, char *fmt, ...);
char *misDynTrimncpyN(char **s1, const char *s2, long n_count);
char *misDynTrimncpy(char **s1, const char *s2, long n_count);
void  misFree(void *);

/* misDynBuf.c */
typedef struct mis__DynBuf MIS_DYNBUF;
MIS_DYNBUF *misDynBufInit(int size);
void misDynBufAppendString(MIS_DYNBUF *buf, const char *str);
char *misDynBufGetString(MIS_DYNBUF *buf);
void misDynBufFree(MIS_DYNBUF *buf);
char *misDynBufClose(MIS_DYNBUF *buf);
void misDynBufAppendBytes(MIS_DYNBUF *buf, void *ptr, int len);
void misDynBufAppendChar(MIS_DYNBUF *buf, char c);
int misDynBufGetSize(MIS_DYNBUF *buf);

/* misEncrypt.c */
long misEncrypt(char *data, long length);
long misDecrypt(char *data, long length);

/* misEncryptBlowfish.c */
typedef struct mis__BlowfishKey MIS_BLOWFISH_KEY;
char *misEncryptBlowfish(char *data, long length, MIS_BLOWFISH_KEY *key, long *resultlen);
char *misDecryptBlowfish(char *data, long length, MIS_BLOWFISH_KEY *key, long *resultlen);
MIS_BLOWFISH_KEY *misBlowfishInitKey(char *key, int keylen);

/* misEncryptionRPBF.c */
void misEncryptRPBF(char *string, long inlength, long *length);
void misDecryptRPBF(char *string, long inlength, long *outlength);

/* misExpandVars.c */
char *misExpandVars(char *out, char *in, long size, char *(*)(char *));
char *misDynExpandVars(char *in, char *(*)(char *));

/* misFileExists.c */
long misFileExists(char *filename);

/* misFindFile.c */
long misFindFile(char *filespec, char **returnedFilename, long *context);
long misEndFindFile(long *context);

/* misFixFilePath.c */
char *misFixFilePath(char *path);

/* misHash.c */
MISHASH *misHashInit(int slots);
MISHASH *misCiHashInit(int slots);

void misHashFree(MISHASH *table);

unsigned long  misHash(char *str);
unsigned long  misCiHash(char *str);

long  misHashPut(MISHASH *table, char *key, void *payload);
void *misHashGet(MISHASH *table, char *key);
void *misHashDelete(MISHASH *table, char *key);
char *misHashEnum(MISHASH *table, void **payload);

/* misCache.c */
MISCACHE *misCacheInit(int size, void (*freefunc)(void *));
void misCacheFree(MISCACHE *table);
long  misCachePut(MISCACHE *table, char *key, void *payload);
void *misCacheGet(MISCACHE *table, char *key);
void *misCacheDelete(MISCACHE *table, char *key);
unsigned int misCacheHits(MISCACHE *cache);
unsigned int misCacheMisses(MISCACHE *cache);
int misCacheSize(MISCACHE *cache);
void misCacheResetStats(MISCACHE *cache);

/* misHexToAscii.c */
char *misHexToAscii(char *hex);

/* misHTTP.c */
char *misHTTPDynURLEncode(char *str);
char *misHTTPDynURLDecode(char *str);
int   misHTTPURLEncode(char *in, char *out, long outsize);
int   misHTTPURLDecode(char *in, char *out, long outsize);

long misHTTPGetMessageBody(char **msg);

long  misHTTPGetParams(char **params[]);
char *misHTTPGetParamValue(char *name, char *params[]);
void  misHTTPFreeParams(char *params[]);

long  misHTTPAddHeader(char *name, char *value, char **headers[]);

char *misHTTPGetHeaderValue(char *name, char *headers[]);
void  misHTTPFreeHeaders(char *headers[]);

long misHTTPPost(char *url, char *requestHeaders[], char *requestBody,
             long *responseStatus, char **responseReasonPhrase,
         char **responseHeaders[], char **responseMessageBody);

/* misLog.c */
void misLogDebug(char *Format, ...);
void misLogInfo(char *Format, ...);
void misLogWarning(char *Format, ...);
void misLogError(char *Format, ...);
void misLogUpdate(char *Format, ...);
void misStackError(char *fmt, ...);
void misLogErrorStack(void);
void misClearErrorStack(void);

/* misMD5.c */
char *misMD5Data(void *data, unsigned int len, char *buf, unsigned int size,
             int bits);
char *misMD5Password(const char *password, const char *salt);
short misIsMD5Password(char *ciphertext);
short misMatchMD5Password(char *password, char *ciphertext);

/* misParseArgument.c */
char *misParseArgument(char **ptr);

/* misQuoteString.c */
char *misQuoteStringN(char *orig, long n_maxlen, char quote);
char *misQuoteString(char *orig, long n_maxlen, char quote);

/* misSocket.c */
misSocket *misSockOpen(char *host, unsigned short port);
misSocket *misSockOpenTimeout(char *host,
                  unsigned short port,
                  long timeout);
misSocket *misSockOpenWithBind(char *host, unsigned short port,misSocket *sock);
misSocket *misSockOpenFD(int fd);
void       misSockClose(misSocket *sock);
long       misSockCheck(misSocket *sock);
void       misSockTimeout(misSocket *sock, long timeout);

int misSockPutc(misSocket *sock, int c);
int misSockPuts(char *string, misSocket *sock, char *eol);
int misSockWrite(misSocket *sock, char *buffer, long size);
int misSockFlush(misSocket *sock);

int   misSockGetc(misSocket *sock);
char *misSockGets(char *buffer, long size, misSocket *sock, char eol);
char *misSockRead(misSocket *sock, char *buffer, long size);

int misSockFD(misSocket *sock);
int misSockEOF(misSocket *sock);
int misSockError(misSocket *sock);

/* misSprintfLen.c */
long misSprintfLen(char *fmt, va_list args);

/* misStatus.c */
void misSetStatusFile(char *filename);
void misSetStatusPos(int filepos);
int  misGetStatusPos(void);
void misUpdateStatus(int field, ...);
long misGetStatus(int slot, int field, void **value, void *timestamp);
long misCreateStatusFile(int slots);

/* misStrsep.c */
char *misStrsep(char **stringp, char *delim);

/* misSysmon.c */
void misEnterProcess(char *AppName);
void misExitProcess(int ExitCode);

/* misTrace.c */
void  misTrc(int level, char *format, ...);
void  misVTrc(int level, char *format, va_list arg);

void  misSetTraceLevelFromArg(char *arg);
int   misGetTraceLevelsString(char **lvls[], char *args[]);
char *misGetTraceOptionsString(void);
void  misSetTraceFile(char *pathname, char *mode);
char *misGetTraceFile(void);
FILE *misGetTraceFilePointer(void);

int   misGetTraceLevel(void);
void  misSetTraceLevel(int bitmask, int onoff);
void  misResetTraceLevel(void);

/* misTrim.c */
long   misTrimLenN(char *str, long n_max_len);
long   misTrimByteLenN(char *str, long n_max_len);
long   misTrimLen(char *str, long b_max_len);
short  misTrimIsNull(char *str, long max_len);
short  misTrimIsNullN(char *str, long max_len);
char  *misTrimncpyN(char *out, char *in, long n_len, long outsize);
char  *misTrimncpy(char *out, char *in, long n_len, long outsize);
char  *misTrimLRcpyN(char *out, char *in, long len);
char  *misTrimLRcpy(char *out, char *in, long len);

/* UTF 8 functional methods (osUTF8.c) */
int  utf8NextCharLen(char *s);
int utf8NextChar(char **s);
long utf8ByteLen(char *in, long n_char_cnt);
long utf8CharLen(char *in);


#if defined (__cplusplus)
}
#endif

/*
 * The following methods take the max_bytes argument and divides it by 4 so that
 * it represents a character length.  This is used when the values being passed
 * to these methods are a UTF-8 byte length (characters * 4).
 */

#define misCiStrncmpChars(s1, s2, max_bytes) \
    misCiStrncmpN(s1, s2, (max_bytes /4))
#define misStrncmpChars(s1, s2, max_bytes) \
    misStrncmpN(s1, s2, (max_bytes /4))
#define misStrncpyChars(s1, s2, max_bytes) \
    misStrncpyN(s1, s2, (max_bytes / 4))
#define misStrncatChars(dest, src, max_bytes) \
    misStrncatN(dest, src, (max_bytes/4))

#define misBuildInsertListChars(colnam, colval, max_bytes, columnList, valueList) \
    misBuildInsertListN(colnam, colval, (max_bytes/4), columnList, valueList)
#define misBuildInsertListTypeChars(colnam, dataType, colval, max_bytes, columnList, valueList) \
    misBuildInsertListTypeN(colnam, dataType, colval, (max_bytes/4), columnList, valueList)
#define misBuildInsertListDBKWChars(colnam, colval, max_bytes, columnList, valueList) \
    misBuildInsertListDBKWN(colnam, colval, (max_bytes/4), columnList, valueList)
#define misBuildUpdateListChars(colnam, colval, max_bytes, updateList) \
    misBuildUpdateListN(colnam, colval, (max_bytes/4), updateList)
#define misBuildUpdateListTypeChars(colnam, dataType, colval, max_bytes, updateList) \
    misBuildUpdateListTypeN(colnam, dataType, colval, (max_bytes/4), updateList)
#define misBuildUpdateListDBKWChars(colnam, colval, max_bytes, updateList) \
    misBuildUpdateListDBKWN(colnam, colval, (max_bytes/4), updateList)
#define misBuildWhereListChars(colnam, colval, max_bytes, whereList) \
    misBuildWhereListN(colnam, colval, (max_bytes/4), whereList)
#define misDynStrncatChars(s1, s2, max_bytes) \
    misDynStrncatN(s1, s2, (max_bytes/4))
#define misDynStrncpyChars(s1, s2, max_bytes) \
    misDynStrncpyN(s1, s2, (max_bytes/4))
#define misDynTrimncpyChars(s1, s2, max_bytes) \
    misDynTrimncpyN(s1, s2, (max_bytes/4))
#define misTrimLenChars(str, max_bytes) \
    misTrimLenN(str, (max_bytes/4))
#define misTrimByteLenChars(str, max_bytes) \
    misTrimByteLenN(str, (max_bytes/4))
#define misTrimcpy(out, in, len)  \
    misTrimncpy((out), (in), (len), (len) + 1)
#define misTrimcpyChars(out, in, max_bytes) \
    misTrimncpyN(out, in, (max_bytes/4), (max_bytes)+1)
#define misTrimcpyN(out, in, len) \
    misTrimncpyN((out), (in), (len), (len) + 1)
#define misTrimIsNullChars(str, max_bytes) \
    misTrimIsNullN(str, (max_bytes / 4))
#define misTrimLRcpyChars(out, in, max_bytes) \
    misTrimLRcpyN(out, in, (max_bytes/4))
#define misTrimStrncmpChars(s1, s2, max_bytes) \
    misTrimStrncmpN(s1, s2, (max_bytes/4))
#define misQuoteStringChars(orig, max_bytes, quote) \
    misQuoteStringN(orig, (max_bytes/4), quote)

#endif
