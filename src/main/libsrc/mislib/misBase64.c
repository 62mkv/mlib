static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: 
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

#include <stddef.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>

static char getChar(unsigned int sixBit)
{
    if (sixBit >= 0 && sixBit <= 25)
	return (char) ('A' + sixBit);
    if (sixBit >= 26 && sixBit <= 51)
	return (char) ('a' + (sixBit - 26));
    if (sixBit >= 52 && sixBit <= 61)
	return (char) ('0' + (sixBit - 52));
    if (sixBit == 62)
	return '+';
    if (sixBit == 63)
	return '/';
    return '?';
}

static int getValue(char c)
{
    if (c >= 'A' && c <= 'Z')
	return c - 'A';
    if (c >= 'a' && c <= 'z')
	return c - 'a' + 26;
    if (c >= '0' && c <= '9')
	return c - '0' + 52;
    if (c == '+')
	return 62;
    if (c == '/')
	return 63;
    if (c == '=')
	return 0;
    return -1;
}

static int nextValue(char **p)
{
    int value = 0;
    if (!**p) return 0;
    
    while (**p && (value = getValue(*((*p)++))) == -1)
        ;

    return value;
}

static char *encodeBlock(char *raw, long raw_len, long offset)
{
    static char base64[5];
    unsigned int block = 0;
    int slack = raw_len - offset - 1;
    int end = (slack >= 2) ? 2 : slack;
    int i;

    memset(base64, '\0', sizeof base64);

    for (i = 0; i <= end; i++)
    {
	unsigned char b = raw[offset + i];
	int neuter = (b < 0) ? b + 256 : b;
	block += neuter << (8 * (2 - i));
    }

    for (i = 0; i < 4; i++)
    {
	unsigned int sixbit;

	sixbit = (block >> (6 * (3 - i))) & 0x3f;
	base64[i] = getChar(sixbit);
    }

    if (slack < 1)
	base64[2] = '=';
    if (slack < 2)
	base64[3] = '=';

    return base64;
}


long misBase64Encode(char *i_raw, long i_raw_len, char **o_str)
{
    long ii;
    MIS_DYNBUF *buf;

    *o_str = NULL;

    buf = misDynBufInit(i_raw_len);

    for (ii = 0; ii < i_raw_len; ii += 3)
	misDynBufAppendString(buf, encodeBlock(i_raw, i_raw_len, ii));

    *o_str = misDynBufClose(buf);

    return eOK;
}

long misBase64Decode(char *i_Base64, unsigned char **o_raw, long *o_len)
{
    int pad = 0;
    int i, j;
    int orig_len = strlen(i_Base64);
    int data_len;
    long length;
    int rawIndex = 0;
    unsigned int block;
    char *p;

    for (i = orig_len - 1; i_Base64[i] == '='; i--)
	pad++;

    data_len = orig_len;

    for (p = i_Base64; *p; p++)
        if (getValue(*p) == -1)
            data_len--;

    length = data_len * 6 / 8 - pad;
    *o_raw = calloc(1, length * sizeof(char) + 1);

    for (p = i_Base64; *p;)
    {
	block = (nextValue(&p) << 18) + 
		(nextValue(&p) << 12) + 
		(nextValue(&p) << 6)  + 
		(nextValue(&p));
	for (j = 0; j < 3 && rawIndex + j < length; j++)
            (*o_raw)[rawIndex + j] = 
		(unsigned char) ((block >> (8 * (2 - j))) & 0xff);

	rawIndex += 3;
    }

    /* -1 because we have a trailing null character on source */
    *o_len = length;
    return eOK;
}

#if 0
  /*
   * I am leaving the original java source here, so that if there
   * is a problem with how it was translated into C, this may be used.
   * This was taken from
   * ftp://ftp.ora.com/pub/examples/java/crypto/files/oreilly/jonathan/util/
   */

public static String encode(byte[]raw)
{
    StringBuffer encoded = new StringBuffer();
    for (int i = 0; i < raw.length; i += 3)
    {
	encoded.append(encodeBlock(raw, i));
    }
    return encoded.toString();
}

protected static char[] encodeBlock(byte[]raw, int offset)
{
    int block = 0;
    int slack = raw.length - offset - 1;
    int end = (slack >= 2) ? 2 : slack;
    for (int i = 0; i <= end; i++)
    {
	byte b = raw[offset + i];
	int neuter = (b < 0) ? b + 256 : b;
	block += neuter << (8 * (2 - i));
    }
    char[] base64 = new char[4];
    for (int i = 0; i < 4; i++)
    {
	int sixbit = (block >>> (6 * (3 - i))) & 0x3f;
	base64[i] = getChar(sixbit);
    }
    if (slack < 1)
	base64[2] = '=';
    if (slack < 2)
	base64[3] = '=';
    return base64;
}

protected static char getChar(int sixBit)
{
    if (sixBit >= 0 && sixBit <= 25)
	return (char) ('A' + sixBit);
    if (sixBit >= 26 && sixBit <= 51)
	return (char) ('a' + (sixBit - 26));
    if (sixBit >= 52 && sixBit <= 61)
	return (char) ('0' + (sixBit - 52));
    if (sixBit == 62)
	return '+';
    if (sixBit == 63)
	return '/';
    return '?';
}

public static byte[] decode(String base64)
{
    int pad = 0;
    for (int i = base64.length() - 1; base64.charAt(i) == '='; i--)
	pad++;
    int length = base64.length() * 6 / 8 - pad;
    byte[]raw = new byte[length];
    int rawIndex = 0;
    for (int i = 0; i < base64.length(); i += 4)
    {
	int block = (getValue(base64.charAt(i)) << 18) + 
		    (getValue(base64.charAt(i + 1)) << 12) + 
		    (getValue(base64.charAt(i + 2)) << 6)  + 
		    (getValue(base64.charAt(i + 3)));
	for (int j = 0; j < 3 && rawIndex + j < raw.length; j++)
	    raw[rawIndex + j] = (byte) ((block >> (8 * (2 - j))) & 0xff);
	rawIndex += 3;
    }
    return raw;
}

protected static int getValue(char c)
{
    if (c >= 'A' && c <= 'Z')
	return c - 'A';
    if (c >= 'a' && c <= 'z')
	return c - 'a' + 26;
    if (c >= '0' && c <= '9')
	return c - '0' + 52;
    if (c == '+')
	return 62;
    if (c == '/')
	return 63;
    if (c == '=')
	return 0;
    return -1;
}
#endif
