static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to support performing an HTTP request as well
 *               as responding to an HTTP request.
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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <errno.h>
#include <time.h>

#ifdef WIN32
# include <fcntl.h>
# include <io.h>
#endif

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>


/*
 *  Static Variables
 */

static char hexdigit[] = "0123456789abcdef";
static char escapeCharacter[256];
static int  initialized;


/*
 *  FUNCTION: sAddToList
 *
 *  PURPOSE:  Add the given string to the given list.
 *
 *  NOTE(S):  This function is used to maintain both parameter and
 *            header lists, since we implement them both the same.
 *
 *  RETURNS:  eOK
 *            Some error code
 */

static long sAddToList(char *str, char **list[])
{
    long ii,
         size;

    /* Determine the current size of the given list. */
    for (ii = 0, size = 0; *list && (*list)[ii]; ii++, size++)
        ;

    /* Increment the size for this new one. */
    size++;

    /* Allocate space for a new pointer in the list. */
    *list = realloc(*list, sizeof(char **) * (size + 1));
    if (! *list)
        OS_PANIC;

    (*list)[size] = NULL;

    /* Allocate space for the actual string. */
    (*list)[size - 1] = calloc(1, strlen(str) + 1);
    if (! (*list)[size - 1])
        OS_PANIC;

    /* Copy the string into the list. */
    strcpy((*list)[size - 1], str);

    /* Trim white-space from both sides of the string. */
    misTrimLR((*list)[size - 1]);

    return eOK;
}


/*
 *  FUNCTION: sGetListValue
 *
 *  PURPOSE:  Get the value for the given named item from the given list.
 *
 *  NOTE(S):  This function is used to maintain both parameter and
 *            header lists, since we implement them both the same.
 *
 *  RETURNS:  Pointer to a list value.
 *            NULL
 */

static char *sGetListValue(char *name, char *list[])
{
    int ii;

    /* Cycle through each element in the list. */
    for (ii = 0; list && list[ii] && list[ii+1]; ii += 2)
    {
        /* Check for a matching argument. */
        if (misCiStrcmp(name, list[ii]) == 0)
        {
            return list[ii + 1];
        }
    }

    return NULL;
}


/*
 *  FUNCTION: sFreeList
 *
 *  PURPOSE:  Free memory associated with the given list.
 *
 *  NOTE(S):  This function is used to maintain both parameter and
 *            header lists, since we implement them both the same.
 *
 *  RETURNS:  void
 */

static void sFreeList(char *list[])
{
    int ii;

    /* Cycle through each node in the list. */
    for (ii = 0; list && list[ii]; ii++)
        free(list[ii]);

    /* Free the list itself. */
    free(list);

    return;
}


/*
 *  FUNCTION: sGetParamsFromString
 *
 *  PURPOSE:  Populate the given argument count and vector from the
 *            parameters in the given string.  The given string is
 *            formatted as follows:
 *
 *                name=value& ... &name=value
 *
 *  RETURNS:  eOK
 *            Some error code
 */

static long sGetParamsFromString(char *str, char **params[])
{
    long status;

    char *name,
         *value,
         *param,
         *decoded;

    /* Cycle through each parameter in the given string. */
    while ((param = misStrsep(&str, "&")) != NULL)
    {
        /* Get the parameter name. */
        name = strtok(param, "=");
        if (! name)
            return eERROR;

        /* Get the parameter value. */
        value = strtok(NULL,  "'\0'");

        /* We massage the value just in case one wasn't given. */
        value = value ? value : "";

        /* URL decode the value. */
        decoded = misHTTPDynURLDecode(value);

        /* Add the parameter name to the parameters list. */
        status = sAddToList(name, params);
        if (status != eOK)
            return status;

        /* Add the parameter value to the parameters list. */
        status = sAddToList(decoded, params);
        if (status != eOK)
            return status;
    }

    return eOK;
}


/*
 *  FUNCTION: sSetupNoEscape
 *
 *  PURPOSE:  Populate the "escape character" array, which is just a list of
 *            characters that we need to escape.
 *
 *  RETURNS:  void
 */

static void sInitializeEscapeCharacters(void)
{
    int ii;

    /* First populate the whole ascii set as needing to be escaped. */
    for (ii = 0; ii < sizeof(escapeCharacter); ii++)
        escapeCharacter[ii] = 1;

    /* We don't escape alpha-numerics. */
    for (ii='0'; ii<='9'; ii++) escapeCharacter[ii] = 0;
    for (ii='a'; ii<='z'; ii++) escapeCharacter[ii] = 0;
    for (ii='A'; ii<='Z'; ii++) escapeCharacter[ii] = 0;

    /* We don't escape the following characters. */
    escapeCharacter['-']  = 0;
    escapeCharacter['_']  = 0;
    escapeCharacter['.']  = 0;
    escapeCharacter['!']  = 0;
    escapeCharacter['~']  = 0;
    escapeCharacter['*']  = 0;
    escapeCharacter['\''] = 0;
    escapeCharacter['(']  = 0;
    escapeCharacter[')']  = 0;

    initialized = 1;

    return;
}


/*
 *  FUNCTION: sHexcode
 *
 *  PURPOSE:  Get the decimal value of this hex code.
 *
 *  RETURNS:  Decimal value of the given hex code.
 */

static int sHexcode(char c)
{
    if      (c >= '0' && c <= '9') return c - '0';
    else if (c >= 'a' && c <= 'f') return c - 'a' + 10;
    else if (c >= 'A' && c <= 'F') return c - 'A' + 10;
    else                           return 0;
}


/*
 *  FUNCTION: misHTTPDynURLDecode
 *
 *  PURPOSE:  URL decode the given string, dynamically allocating space for
 *            the decoded string.
 *
 *  RETURNS:  Pointer to the decoded string.
 *            NULL
 */

char *misHTTPDynURLDecode(char *str)
{
    char *ptr,
         *dest;

    /* Initialize our output string. */
    dest = NULL;

    /* Cycle through the given string. */
    while ((ptr = strpbrk(str, "+%")) != NULL)
    {
        /* Copy in the string so far. */
        misDynStrncat(&dest, str, ptr - str);

        /* Replace '+' characters with a ' ' character. */
        if (*ptr == '+')
        {
            misDynCharcat(&dest, ' ');
    
            /* Advance past this character in the string. */
            str = ptr + 1;
        }
    
        /* Replace hex encodings with their character representation. */
        else if (*ptr == '%')
        {
            long decval;
    
            /* Advance the pointer past the '%' character. */
            ptr++;

            /* Determine the decimal value of this hex number. */
            decval  = sHexcode(*ptr++) * 16;
            decval += sHexcode(*ptr++);
    
            /* Append the character to the string. */
            misDynCharcat(&dest, (char) decval);
    
            /* Advance past this encoded character in the string. */
            str = ptr;
        }
    }

    /* Copy in the rest of the string. */
    misDynStrcat(&dest, str);

    return dest;
}


/*
 *  FUNCTION: misHTTPDynURLEncode
 *
 *  PURPOSE:  URL encode the given string, dynamically allocating space for
 *            the encoded string.
 *
 *  RETURNS:  Pointer to the encoded string.
 *            NULL
 */

char *misHTTPDynURLEncode(char *src)
{
    unsigned char *dest;

    /* Initialize our escape character array if necessary. */
    if (! initialized) sInitializeEscapeCharacters();

    /* Initialize our output string. */
    dest = NULL;

    /* Cycle through the source string. */
    while (src && *src)
    {
        /* Replace ' ' characters with a '+' character. */
        if (*src == ' ')
        {
            misDynCharcat((char **) &dest, '+');
        }

        /* Replace special characters with their hex encodings. */
        else if (escapeCharacter[*src])
        {
            misDynCharcat((char **) &dest, '%');
            misDynCharcat((char **) &dest, (char) hexdigit[(*src) / 16]);
            misDynCharcat((char **) &dest, (char) hexdigit[(*src) % 16]);
        }
    
        /* Just copy characters that don't need to be escaped. */
        else
        {
            misDynCharcat((char **) &dest, *src);
        }
    
        /* Advance to the next character. */
        src++;
    }

    /* Null-terminate the string. */
    misDynCharcat((char **) &dest, '\0');

    return (char *) dest;
}


int misHTTPURLEncode(char *src, char *dest, long destlen)
{
    int nchars = 0;

    unsigned char *s,
                  *d;

    /* Initialize our escape character array if necessary. */
    if (! initialized) sInitializeEscapeCharacters();

    /* Cycle through the source string. */
    for (s = (unsigned char *) src, d = (unsigned char *) dest; *s; s++)
    {
        /* Replace ' ' characters with a '+' character. */
        if (*s == ' ')
        {
            *d++ = '+';
            nchars++;
        }
    
        /* Replace special characters with their hex encodings. */
        else if (escapeCharacter[*s])
        {
            if ((d - (unsigned char *) dest) >= (destlen - 3))
            break;
            *d++ = '%';
            *d++ = hexdigit[(*s) / 16];
            *d++ = hexdigit[(*s) % 16];
            nchars += 3;
        }
    
        /* Just copy characters that don't need to be escaped. */
        else
        {
            if ((d - (unsigned char *) dest) >= (destlen - 1))
            break;
            *d++ = *s;
            nchars++;
        }
    }

    /* Null-terminate the string. */
    *d = '\0';

    return nchars;
}


int misHTTPURLDecode(char *src, char *dest, long destlen)
{
    int result,
        nchars = 0;

    char *s,
         *d;

    /* Cycle through the source string. */
    for (s = src, d = dest; *s && (d - dest) < (destlen - 1); d++)
    {
        /* Unescape character encodings beginning with a '%'. */
        if (*s == '%')
        {
            s++;
            result  = sHexcode(*s++) * 16;
            result += sHexcode(*s++);
            *d = result;
        }
    
        /* Just copy characters that don't need to be escaped. */
        else
        {
            *d = *s++;
        }
        nchars++;
    }

    /* Null-terminate the string. */
    *d = '\0';

    return nchars;
}


/*
 *  FUNCTION: misHTTPGetMessageBody
 *
 *  PURPOSE:  Get the message body from stdin.
 *
 *  RETURNS:  eOK
 *            Some error code
 */

long misHTTPGetMessageBody(char **msg)
{
    long len,
         status;

    char *lenstr,
         *buffer;

    /* Get the length of the message body. */
    lenstr = getenv("CONTENT_LENGTH");
    if (! lenstr)
        return eERROR;

    /* Convert the length to something we can use. */
    len = lenstr ? atol(lenstr) : 0;

    /* Allocate space for the message body. */
    buffer = calloc(1, atoi(lenstr) + 1);
    if (! buffer)
        OS_PANIC;

#ifdef WIN32
    _setmode(_fileno(stdin), _O_BINARY);
#endif

    /* Get the message body from stdin. */
    status = fread(buffer, 1, len, stdin);
    if (status != len)
    {
        free(buffer);
        return errno;
    }

#ifdef WIN32
    _setmode(_fileno(stdin), _O_TEXT);
#endif

    misTrc(T_FLOW, "Read %ld bytes", len);
    misTrc(T_FLOW, "Data: [%s]", buffer);

    /* Point the caller's pointer to our buffer. */
    *msg = buffer;

    return eOK;
}


/*
 *  FUNCTION: misHTTPGetParams
 *
 *  PURPOSE:  Populate the given array of pointers with the parameters passed
 *            to stdin.
 *
 *  RETURNS:  eOK
 *            Some error code
 */

long misHTTPGetParams(char **params[])
{
    long len,
         status;

    char *lenstr,
         *buffer;

    /* Get the length of the message body. */
    lenstr = getenv("CONTENT_LENGTH");
    if (! lenstr)
        return eERROR;

    /* Convert the length to something we can use. */
    len = lenstr ? atol(lenstr) : 0;

    /* Allocate space for the message body. */
    buffer = calloc(1, atoi(lenstr) + 1);
    if (! buffer)
        OS_PANIC;

#ifdef WIN32
    _setmode(_fileno(stdin), _O_BINARY);
#endif

    /* Get the message body from stdin. */
    status = fread(buffer, 1, len, stdin);
    if (status != len)
    {
        free(buffer);
        return errno;
    }

#ifdef WIN32
    _setmode(_fileno(stdin), _O_TEXT);
#endif

    misTrc(T_FLOW, "Read %ld bytes", len);
    misTrc(T_FLOW, "Data: [%s]", buffer);

    /* Populate the argument vector from the string. */
    sGetParamsFromString(buffer, params);

    /* Free up dynamically allocated memory. */
    free(buffer);

    return eOK;
}


/*
 *  FUNCTION: misHTTPGetParamValue
 *
 *  PURPOSE:  Get the value for the given named parameter from the
 *            given argument vector, which should have been populated
 *            by a previous call to misHTTPGetParams( ).
 *
 *  RETURNS:  eOK
 *            Some error code
 */

char *misHTTPGetParamValue(char *name, char *params[])
{
    return sGetListValue(name, params);
}


/*
 *  FUNCTION: misHTTPFreeParams
 *
 *  PURPOSE:  Free memory assocaited with the given parameter list.
 *
 *  RETURNS:  void
 */

void misHTTPFreeParams(char *params[])
{
    sFreeList(params);
}


/*
 *  FUNCTION: misHTTPAddHeader
 *
 *  PURPOSE:  Add the given header name and value to the header list.
 *
 *  RETURNS:  eOK
 *            Some error code
 */

long misHTTPAddHeader(char *name, char *value, char **headers[])
{
    long status;

    /* Add the header name to the header list. */
    status = sAddToList(name, headers);
    if (status != eOK)
        return status;

    /* Add the header value to the header list. */
    status = sAddToList(value, headers);
    if (status != eOK)
        return status;

    return eOK;
}


/*
 *  FUNCTION: misHTTPGetHeaderValue
 *
 *  PURPOSE:  Get the value for the given header name from the given header
 *            list.
 *
 *  RETURNS:  Pointer to the header value.
 *            NULL - The given header name could not be found.
 */

char *misHTTPGetHeaderValue(char *name, char *headers[])
{
    return sGetListValue(name, headers);
}


/*
 *  FUNCTION: misHTTPFreeHeaders
 *
 *  PURPOSE:  Free memory assocaited with the given header list.
 *
 *  RETURNS:  void
 */

void misHTTPFreeHeaders(char *headers[])
{
    sFreeList(headers);
}


/*
 *  FUNCTION: sParseURL
 *
 *  PURPOSE:  Parse the host, port and path from the given URL.
 *
 *  FORMAT:   http://www.myhost.com:80/my/pathname
 *
 *  RETURNS:  void
 */

static void sParseURL(char *url, char **host, unsigned short *port, char **path)
{
    char *ptr;

    /* Initialize our output variables. */
    *host = NULL, *path = NULL;

    /* Skip the scheme if one is given. */
    if (misCiStrncmp(url, "http://", 7) == 0)
        url += 7;

    /* Set the defaults for each of our output variables. */
    misDynStrcpy(host, url);
    misDynStrcpy(path, "/");
    *port = 80;

    /* Find the delimiter for the host portion of the URL. */
    ptr = strpbrk(url, ":/");

    /* Get the host portion of the URL. */
    if (ptr)
        misDynStrncpy(host, url, (ptr - url));

    /* Get the port portion of the URL. */
    if (ptr && *ptr == ':')
    {
        url = ptr + sizeof(char);
        *port = (unsigned short) atoi(url);
        
        /* Find the delimiter for the port portion of the URL. */
        ptr = strpbrk(url, "/");
    }

    /* Get the path portion of the URL. */
    if (ptr && *ptr == '/')
    {
        url = ptr;
        misDynStrcpy(path, url);
    }

    return;
}


/*
 *  FUNCTION: sParseResponse
 *
 *  PURPOSE:  Parse a server's response to a HTTP request.
 *
 *  RETURNS:  eOK
 *            Some error code
 */

static long sParseResponse(misSocket *sock,
                       long *responseStatus,
                       char **responseReasonPhrase,
               char **responseHeaders[],
               char **responseBody)
{
    long ii      = 0,
         length  = 0,
         reading = 0;

    char buffer[4096];

    char *ptr;

    /* Intialize the buffer. */
    memset(buffer, 0, sizeof(buffer));

    /* Initialize the content length in case one isn't read from the socket. */
    length = 0;

    /* Intialize the response status, reason phrase, headers and body. */
    *responseStatus       = 400;
    *responseReasonPhrase = NULL;
    *responseHeaders      = NULL;
    *responseBody         = NULL;

    /*
     *  Handle the status line of the response.
     */

    /* Get the status line from the server. */
    if (misSockGets(buffer, sizeof(buffer) - 1, sock, '\n') == NULL)
        return eMIS_HTTP_ERROR;

    misTrc(T_FLOW, "Read: %s", buffer);

    /* Make sure that we we've got a legitimate response status line. */
    if (misCiStrncmp(buffer, "http/", 5) != 0)
        return eMIS_HTTP_NOT_HTTP_RESPONSE;

    /* Skip past the HTTP version. */
    ptr = strtok(buffer, " ");

    /* Parse out the status and make a copy for our caller. */
    ptr = strtok(NULL, " ");
    *responseStatus = atol(ptr);

    /* Parse our the reason phrase and make a copy for our caller. */
    ptr = strtok(NULL, "'\0'");
    misTrim(ptr);
    misDynStrcpy(responseReasonPhrase, ptr ? ptr : "");

    /*
     *  Handle each header of the response.
     */

    /* Cycle through each header. */
    for (ii = 1; ;ii++)
    {
        /* Get a header from the server. */
        if (misSockGets(buffer, sizeof(buffer) - 1, sock, '\n') == NULL)
            return eMIS_HTTP_ERROR;

        misTrc(T_FLOW, "Read: %s", buffer);

        /* We can break out if we've reached the end of our headers. */
        if (strcmp(buffer, "\n") == 0 || strcmp(buffer, "\r\n") == 0)
            break;

        /* The content length is special because we need it below. */
        if (misCiStrncmp(buffer, "Content-Length:", 14) == 0)
        {
            char temp[1024];

            /* Parse the content length from the buffer. */
            sscanf(buffer, "%s %ld", temp, &length);
        }

        /* Parse the header name and add it to the header list. */
        ptr = strtok(buffer, ":");
        sAddToList(ptr, responseHeaders);
    
        /* Parse the header value and add it to the header list. */
        ptr = strtok(NULL, "'\0'");
        sAddToList(ptr, responseHeaders);
    }

    /*
     *  Handle the message body of the response.
     */

    /* Get the message body of the response. */
    reading = 1;
    do
    {
        memset(buffer, 0, sizeof(buffer));
        for (ii = 0; ii < sizeof(buffer) - 1; ii++)
        {
            int c;

            c = misSockGetc(sock);
            if (c == MIS_SOCK_EOF || c == MIS_SOCK_ERROR || c == MIS_SOCK_TIMEOUT)
            {
                reading = 0;
                break;
            }
            buffer[ii] = c;
        }

        misDynStrncat(responseBody, buffer, strlen(buffer));

    } while (reading);

    /* Dump a trace message if the lengths don't match. */
    if (length != 0  && strlen(*responseBody) != length)
    {
        misTrc(T_FLOW, "Received only %ld bytes of response body out of expected %ld bytes - returning error...",
           strlen(*responseBody), length);
        return eMIS_HTTP_ERROR;
    }
    misTrc(T_FLOW, "Read: %s", *responseBody);

    return eOK;
}


/*
 *  FUNCTION: misHTTPPost
 *
 *  PURPOSE:  Perform a HTTP post request.
 *
 *  RETURNS:  eOK
 *            Some error code
 */

long misHTTPPost(char *url,
                 char *requestHeaders[],
                 char *requestBody,
                 long *responseStatus,
                 char **responseReasonPhrase,
                 char **responseHeaders[],
                 char **responseBody)
{
    unsigned short port;

    long ii,
         status;

    char *host,
         *path,
          buffer[2048];

    misSocket *sock;

    /* Parse apart the pieces of the URL. */
    sParseURL(url, &host, &port, &path);

    /* Open a socket to the HTTP server. */
    sock = misSockOpen(host, port);
    if (! sock)
        return eMIS_HTTP_FAILED_TO_CONNECT;

    /* Make sure the path won't be too big for our buffer. */
    if (path && strlen(path) > sizeof(buffer) - 20)
        return eERROR;

    /*
     *  Write the request line.
     */
    sprintf(buffer, "POST %s HTTP/1.0", path);
    misSockPuts(buffer, sock, "\r\n");
    misTrc(T_FLOW, "Wrote: %s", buffer);

    /*
     *  Write standard request headers.
     */
    sprintf(buffer, "User-Agent: MOCA/%s", misGetVersion( ));
    misSockPuts(buffer, sock, "\r\n");
    misTrc(T_FLOW, "Wrote: %s", buffer);

    sprintf(buffer, "Host: %s:%d", host, port);
    misSockPuts(buffer, sock, "\r\n");
    misTrc(T_FLOW, "Wrote: %s", buffer);

    sprintf(buffer, "Connection: Keep-Alive");
    misSockPuts(buffer, sock, "\r\n");
    misTrc(T_FLOW, "Wrote: %s", buffer);

    sprintf(buffer, "Cache-Control: no-cache");
    status = misSockPuts(buffer, sock, "\r\n");
    misTrc(T_FLOW, "Wrote: %s", buffer);

    sprintf(buffer, "Content-Length: %ld", strlen(requestBody));
    misSockPuts(buffer, sock, "\r\n");
    misTrc(T_FLOW, "Wrote: %s", buffer);

    /*
     *  Write custom request headers.
     */
    for (ii = 0;
         requestHeaders && requestHeaders[ii] && requestHeaders[ii + 1];
         ii += 2)
    {
        misSockPuts(requestHeaders[ii], sock, "");
        misSockPuts(":", sock, "");
        misSockPuts(requestHeaders[ii + 1], sock, "\r\n");
        misTrc(T_FLOW, "Wrote: %s: %s", requestHeaders[ii],
                                        requestHeaders[ii+1]);
    }

    /* Write the empty line to signify the end of the headers. */
    misSockPuts("", sock, "\r\n");

    /*
     *  Write the request body.
     */
    misSockPuts(requestBody, sock, "\r\n\r\n");
    misTrc(T_FLOW, "Wrote: %s", requestBody);

    /* Flush everything to the socket. */
    misSockFlush(sock);

    /* Parse the response from the server. */
    status = sParseResponse(sock, responseStatus, responseReasonPhrase,
                            responseHeaders, responseBody);

    /* Close the socket to the HTTP server. */
    misSockClose(sock);

    /* Free dynamically allocated memory. */
    free(host);
    free(path);

    return status;
}
