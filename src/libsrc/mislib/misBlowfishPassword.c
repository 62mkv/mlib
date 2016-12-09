static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL: https://athena.redprairie.com/svn/prod/moca/trunk/src/libsrc/mislib/mi
sBlowfishPassword.c $
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to deal with passwords that we encode/decode.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2008
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
 *#END************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>

#define BLOWFISH_ID          "|B|"
#define BLOWFISH_KEY_LENGTH  16
#define B64_BITS             8

static char *sGetDateTime(void)
{
    static char buffer[30];

    struct tm *times;

    OS_TIME ostime;

    /* Get the current seconds and milliseconds. */
    osGetTime(&ostime);

    /* Convert our time_t into a struct tm. */
    times = localtime(&ostime.sec);

    /* Convert our struct tm to a formatted string. */
    strftime(buffer, sizeof(buffer) - 1, "%Y/%m/%d %H:%M:%S", times);

    return buffer;
}

static void sPrintBytes(char *label, char *data, long size)
{
    printf("%s: ", label);

    while (size--)
        printf("%u ", (unsigned char) *data++);

    printf("\n");
}

/*
 * Encoding Algorithm
 *
 *     password p1 = "foo"
 *     date d1 = "2008/03/25 11:22:33"
 *     blowfish key k1 = MD5(d1)
 *     blowfish text b1 = BlowfishEncode(key = k1, text = p1)
 *     concatentated text c1 = "<k1><b1>"
 *     base64 text b64 = B64Encode(c1)
 *     password e1 = "|B|<b64>"
 */

char *misBlowfishEncodePassword(char *p1)
{
    long b1_length,
         c1_length;

    char *b1  = NULL,     /* Blowfish text                             */
         *c1  = NULL,     /* Concatenated blowfish key + blowfish text */
         *d1  = NULL,     /* Date/time string                          */
         *e1  = NULL,     /* Encoded password                          */
         *k1  = NULL,     /* Blowfish key                              */
         *b64 = NULL;     /* Base 64 encoded text                      */

    MIS_BLOWFISH_KEY *key;

    /* Get the current date/time <d1>. */
    d1 = sGetDateTime( );

    /* Create Blowfish key <k1>. */
    k1 = malloc(BLOWFISH_KEY_LENGTH);
    misMD5Data(d1, strlen(d1), k1, BLOWFISH_KEY_LENGTH, B64_BITS);

    /* Create the Blowfish text <b1>. */
    key = misBlowfishInitKey(k1, BLOWFISH_KEY_LENGTH);
    b1 = misEncryptBlowfish(p1, strlen(p1), key, &b1_length);

    /* Create the concatenated text <c1>. */
    c1_length = BLOWFISH_KEY_LENGTH + b1_length;
    c1 = malloc(c1_length);
    memcpy(c1, k1, BLOWFISH_KEY_LENGTH);
    memcpy(c1 + BLOWFISH_KEY_LENGTH, b1, b1_length);

    /* Create the base64-encoded text <b64>. */
    misBase64Encode(c1, c1_length, &b64);

    /* Create the encoded password <e1>. */
    misDynSprintf(&e1, "%s%s", BLOWFISH_ID, b64);

    /* Free up all dynamically allocated memory. */
    free(b1);
    free(b64);
    free(c1);
    free(k1);
    free(key);

    return e1;
}

/*
 * Decoding Algorithm
 *
 *     encoded password e1 = "|B|<b64>"
 *     base-64 text b64 = e1 + 3 characters
 *     concatenated text c1 = B64Decode(b64)
 *     blowfish key k1 = First BLOWFISH_KEY_LENGTH bytes of c1
 *     blowfish text b1 = Remaining bytes of c1
 *     blowfish text b1 = BlowfishDecode(key = k1, text = c1)
 *     password p1 = BlowfishDecode(b1)
 */

char *misBlowfishDecodePassword(char *e1)
{
    long b1_length,
         c1_length,
         p1_length;

    char *b1  = NULL,     /* Blowfish text                             */
         *c1  = NULL,     /* Concatenated blowfish key + blowfish text */
         *d1  = NULL,     /* Date/time string                          */
         *k1  = NULL,     /* Blowfish key                              */
         *p1  = NULL,     /* Decoded password                          */
         *b64 = NULL;     /* Base 64 encoded text                      */

    MIS_BLOWFISH_KEY *key;

    /* Get the base64-encoded text <b64>. */
    b64 = e1 + strlen(BLOWFISH_ID);

    /* Get the concatenated text <c1>. */
    misBase64Decode(b64, (unsigned char **) &c1, &c1_length);

    /* Get the Blowfish key <k1>. */
    k1 = calloc(1, BLOWFISH_KEY_LENGTH);
    memcpy(k1, c1, BLOWFISH_KEY_LENGTH);

    /* Get the Blowfish text <b1>. */
    key = misBlowfishInitKey(k1, BLOWFISH_KEY_LENGTH);
    b1 = c1 + BLOWFISH_KEY_LENGTH;
    b1_length = c1_length - BLOWFISH_KEY_LENGTH;

    /* Get the password <p1>. */
    p1 = misDecryptBlowfish(b1, b1_length, key, &p1_length);

    /* 
     * NULL-terminate the password. We do this conditionally because 
     * we used to encode the NULL-terminator and changed it for later
     * releases, but we still want to make sure we can handle it.
     */
    p1 = realloc(p1, p1_length + 1);
    p1[p1_length] = '\0';

    /* Free up all dynamically allocated memory. */
    free(c1);
    free(k1);
    free(key);

    return p1;
}

short misIsBlowfishEncodedPassword(char *ciphertext)
{
    /* Make sure we were given a password to check. */
    if (!ciphertext)
        return 0;

    return (strncmp(ciphertext, BLOWFISH_ID, strlen(BLOWFISH_ID)) == 0) ? 1 : 0;
}


short misMatchBlowfishEncodedPassword(char *password, char *ciphertext)
{
    short status = eERROR;
    char *result = NULL;

    /* Make sure we were given the password and ciphertext to check. */
    if (!password || !ciphertext)
        return eERROR;

    /* Don't bother if the ciphertext isn't even encoded. */
    if (misIsBlowfishEncodedPassword(ciphertext))
    {
        result = misBlowfishDecodePassword(ciphertext);
        if (strcmp(password, result) == 0)
            status = eOK;

        free(result);
    }
    else
    {
        if (strcmp(password, ciphertext) == 0)
            status = eOK;
    }

    return status;
}
