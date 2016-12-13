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

/*
 * MD5C.C - RSA Data Security, Inc., MD5 message-digest algorithm
 *
 * Copyright (C) 2016, RSA Data Security, Inc. Created 2016. All
 * rights reserved.
 *
 * License to copy and use this software is granted provided that it
 * is identified as the "RSA Data Security, Inc. MD5 Message-Digest
 * Algorithm" in all material mentioning or referencing this software
 * or this function.
 *
 * License is also granted to make and use derivative works provided
 * that such works are identified as "derived from the RSA Data
 * Security, Inc. MD5 Message-Digest Algorithm" in all material
 * mentioning or referencing the derived work.
 *
 * Security, Inc. makes no representations concerning either
 * the merchantability of this software or the suitability of this
 * software for any particular purpose. It is provided "as is"
 * without express or implied warranty of any kind.
 *
 * These notices must be retained in any copies of any part of this
 * documentation and/or software.
 *
 *
 * This code is the same as the code published by RSA Inc.  It has been
 * edited for clarity and style only.
 */

/*******************************************************************************
 ** Following license applies to functions to64 and misMD5Password. Both have 
 ** been modified slightly to fit our calling/style conventions.
 **
 ** The original C source license reads:
 **
 ** ----------------------------------------------------------------------------
 ** "THE BEER-WARE LICENSE" (Revision 42):
 ** <phk@login.dknet.dk> wrote this file.  As long as you retain this notice you
 ** can do whatever you want with this stuff. If we meet some day, and you think
 ** this stuff is worth it, you can buy me a beer in return.   Poul-Henning Kamp
 ** ----------------------------------------------------------------------------
 **
 **
 ******************************************************************************/

#define MOCA_ALL_SOURCE

#include <moca.h>

#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <time.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>

#define MD5_INDICATOR "$1$"
#ifdef WIN32
#define snprintf _snprintf 
#define strdup  _strdup 
#endif
typedef unsigned long bits32_t;

/* MD5 context. */
typedef struct MD5Context
{
    bits32_t       state[4];	/* state (ABCD) */
    bits32_t       count[2];	/* number of bits, modulo 2^64 (lsb first) */
    unsigned char   buffer[64];	/* input buffer */
} MD5_CTX;

static void     MD5Init(MD5_CTX *);
static void     MD5Update(MD5_CTX *, const unsigned char *, unsigned int);
static void     MD5Final(unsigned char[16], MD5_CTX *);
static void     MD5Transform(bits32_t[4], const unsigned char[64]);

/* translate from integer to character for md5crypt */
static char itoa64[] = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

#ifdef i386
#define Encode memcpy
#define Decode memcpy
#else /* i386 */

/*
 * Encodes input (bits32_t) into output (unsigned char). Assumes len is
 * a multiple of 4.
 */

static void Encode(unsigned char *output, bits32_t * input, unsigned int len)
{
    unsigned int    i, j;

    for (i = 0, j = 0; j < len; i++, j += 4)
    {
	output[j] = (unsigned char) (input[i] & 0xff);
	output[j + 1] = (unsigned char) ((input[i] >> 8) & 0xff);
	output[j + 2] = (unsigned char) ((input[i] >> 16) & 0xff);
	output[j + 3] = (unsigned char) ((input[i] >> 24) & 0xff);
    }
}

/*
 * Decodes input (unsigned char) into output (bits32_t). Assumes len is
 * a multiple of 4.
 */

static void Decode(bits32_t * output, const unsigned char *input,
		   unsigned int len)
{
    unsigned int    i, j;

    for (i = 0, j = 0; j < len; i++, j += 4)
	output[i] = ((bits32_t) input[j]) | (((bits32_t) input[j + 1]) << 8) |
	    (((bits32_t) input[j + 2]) << 16) | (((bits32_t) input[j + 3]) << 24);
}
#endif /* i386 */

static unsigned char PADDING[64] =
{
    0x80, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
};

/* F, G, H and I are basic MD5 functions. */
#define F(x, y, z) (((x) & (y)) | ((~x) & (z)))
#define G(x, y, z) (((x) & (z)) | ((y) & (~z)))
#define H(x, y, z) ((x) ^ (y) ^ (z))
#define I(x, y, z) ((y) ^ ((x) | (~z)))

/* ROTATE_LEFT rotates x left n bits. */
#define ROTATE_LEFT(x, n) (((x) << (n)) | ((x) >> (32-(n))))

/*
 * FF, GG, HH, and II transformations for rounds 1, 2, 3, and 4.
 * Rotation is separate from addition to prevent recomputation.
 */
#define FF(a, b, c, d, x, s, ac) { \
	(a) += F ((b), (c), (d)) + (x) + (bits32_t)(ac); \
	(a) = ROTATE_LEFT ((a), (s)); \
	(a) += (b); \
	}
#define GG(a, b, c, d, x, s, ac) { \
	(a) += G ((b), (c), (d)) + (x) + (bits32_t)(ac); \
	(a) = ROTATE_LEFT ((a), (s)); \
	(a) += (b); \
	}
#define HH(a, b, c, d, x, s, ac) { \
	(a) += H ((b), (c), (d)) + (x) + (bits32_t)(ac); \
	(a) = ROTATE_LEFT ((a), (s)); \
	(a) += (b); \
	}
#define II(a, b, c, d, x, s, ac) { \
	(a) += I ((b), (c), (d)) + (x) + (bits32_t)(ac); \
	(a) = ROTATE_LEFT ((a), (s)); \
	(a) += (b); \
	}

/* MD5 initialization. Begins an MD5 operation, writing a new context. */

static void MD5Init(MD5_CTX * context)
{

    context->count[0] = context->count[1] = 0;

    /* Load magic initialization constants.  */
    context->state[0] = 0x67452301;
    context->state[1] = 0xefcdab89;
    context->state[2] = 0x98badcfe;
    context->state[3] = 0x10325476;
}

/* 
 * MD5 block update operation. Continues an MD5 message-digest
 * operation, processing another message block, and updating the
 * context.
 */

static void MD5Update(MD5_CTX * context, const unsigned char *input,
		      unsigned int inputLen)
{
    unsigned int    i, index, partLen;

    /* Compute number of bytes mod 64 */
    index = (unsigned int) ((context->count[0] >> 3) & 0x3F);

    /* Update number of bits */
    if ((context->count[0] += ((bits32_t) inputLen << 3))
	< ((bits32_t) inputLen << 3))
	context->count[1]++;
    context->count[1] += ((bits32_t) inputLen >> 29);

    partLen = 64 - index;

    /* Transform as many times as possible. */
    if (inputLen >= partLen)
    {
	memcpy((void *) &context->buffer[index], (void *) input,
	       partLen);
	MD5Transform(context->state, context->buffer);

	for (i = partLen; i + 63 < inputLen; i += 64)
	    MD5Transform(context->state, &input[i]);

	index = 0;
    }
    else
	i = 0;

    /* Buffer remaining input */
    memcpy((void *) &context->buffer[index], (void *) &input[i],
	   inputLen - i);
}

/*
 * MD5 finalization. Ends an MD5 message-digest operation, writing the
 * the message digest and zeroizing the context.
 */

static void MD5Final(unsigned char digest[16], MD5_CTX * context)
{
    unsigned char   bits[8];
    unsigned int    index, padLen;

    /* Save number of bits */
    Encode(bits, context->count, 8);

    /* Pad out to 56 mod 64. */
    index = (unsigned int) ((context->count[0] >> 3) & 0x3f);
    padLen = (index < 56) ? (56 - index) : (120 - index);
    MD5Update(context, PADDING, padLen);

    /* Append length (before padding) */
    MD5Update(context, bits, 8);

    /* Store state in digest */
    Encode(digest, context->state, 16);

    /* Zeroize sensitive information. */
    memset((void *) context, 0, sizeof(*context));
}

/* MD5 basic transformation. Transforms state based on block. */

static void MD5Transform(bits32_t state[4], const unsigned char block[64])
{
    bits32_t       a = state[0], b = state[1], c = state[2], d = state[3],
                    x[16];

    Decode(x, block, 64);

    /* Round 1 */
#define S11 7
#define S12 12
#define S13 17
#define S14 22
    FF(a, b, c, d, x[0], S11, 0xd76aa478);	/* 1 */
    FF(d, a, b, c, x[1], S12, 0xe8c7b756);	/* 2 */
    FF(c, d, a, b, x[2], S13, 0x242070db);	/* 3 */
    FF(b, c, d, a, x[3], S14, 0xc1bdceee);	/* 4 */
    FF(a, b, c, d, x[4], S11, 0xf57c0faf);	/* 5 */
    FF(d, a, b, c, x[5], S12, 0x4787c62a);	/* 6 */
    FF(c, d, a, b, x[6], S13, 0xa8304613);	/* 7 */
    FF(b, c, d, a, x[7], S14, 0xfd469501);	/* 8 */
    FF(a, b, c, d, x[8], S11, 0x698098d8);	/* 9 */
    FF(d, a, b, c, x[9], S12, 0x8b44f7af);	/* 10 */
    FF(c, d, a, b, x[10], S13, 0xffff5bb1);	/* 11 */
    FF(b, c, d, a, x[11], S14, 0x895cd7be);	/* 12 */
    FF(a, b, c, d, x[12], S11, 0x6b901122);	/* 13 */
    FF(d, a, b, c, x[13], S12, 0xfd987193);	/* 14 */
    FF(c, d, a, b, x[14], S13, 0xa679438e);	/* 15 */
    FF(b, c, d, a, x[15], S14, 0x49b40821);	/* 16 */

    /* Round 2 */
#define S21 5
#define S22 9
#define S23 14
#define S24 20
    GG(a, b, c, d, x[1], S21, 0xf61e2562);	/* 17 */
    GG(d, a, b, c, x[6], S22, 0xc040b340);	/* 18 */
    GG(c, d, a, b, x[11], S23, 0x265e5a51);	/* 19 */
    GG(b, c, d, a, x[0], S24, 0xe9b6c7aa);	/* 20 */
    GG(a, b, c, d, x[5], S21, 0xd62f105d);	/* 21 */
    GG(d, a, b, c, x[10], S22, 0x2441453);	/* 22 */
    GG(c, d, a, b, x[15], S23, 0xd8a1e681);	/* 23 */
    GG(b, c, d, a, x[4], S24, 0xe7d3fbc8);	/* 24 */
    GG(a, b, c, d, x[9], S21, 0x21e1cde6);	/* 25 */
    GG(d, a, b, c, x[14], S22, 0xc33707d6);	/* 26 */
    GG(c, d, a, b, x[3], S23, 0xf4d50d87);	/* 27 */
    GG(b, c, d, a, x[8], S24, 0x455a14ed);	/* 28 */
    GG(a, b, c, d, x[13], S21, 0xa9e3e905);	/* 29 */
    GG(d, a, b, c, x[2], S22, 0xfcefa3f8);	/* 30 */
    GG(c, d, a, b, x[7], S23, 0x676f02d9);	/* 31 */
    GG(b, c, d, a, x[12], S24, 0x8d2a4c8a);	/* 32 */

    /* Round 3 */
#define S31 4
#define S32 11
#define S33 16
#define S34 23
    HH(a, b, c, d, x[5], S31, 0xfffa3942);	/* 33 */
    HH(d, a, b, c, x[8], S32, 0x8771f681);	/* 34 */
    HH(c, d, a, b, x[11], S33, 0x6d9d6122);	/* 35 */
    HH(b, c, d, a, x[14], S34, 0xfde5380c);	/* 36 */
    HH(a, b, c, d, x[1], S31, 0xa4beea44);	/* 37 */
    HH(d, a, b, c, x[4], S32, 0x4bdecfa9);	/* 38 */
    HH(c, d, a, b, x[7], S33, 0xf6bb4b60);	/* 39 */
    HH(b, c, d, a, x[10], S34, 0xbebfbc70);	/* 40 */
    HH(a, b, c, d, x[13], S31, 0x289b7ec6);	/* 41 */
    HH(d, a, b, c, x[0], S32, 0xeaa127fa);	/* 42 */
    HH(c, d, a, b, x[3], S33, 0xd4ef3085);	/* 43 */
    HH(b, c, d, a, x[6], S34, 0x4881d05);	/* 44 */
    HH(a, b, c, d, x[9], S31, 0xd9d4d039);	/* 45 */
    HH(d, a, b, c, x[12], S32, 0xe6db99e5);	/* 46 */
    HH(c, d, a, b, x[15], S33, 0x1fa27cf8);	/* 47 */
    HH(b, c, d, a, x[2], S34, 0xc4ac5665);	/* 48 */

    /* Round 4 */
#define S41 6
#define S42 10
#define S43 15
#define S44 21
    II(a, b, c, d, x[0], S41, 0xf4292244);	/* 49 */
    II(d, a, b, c, x[7], S42, 0x432aff97);	/* 50 */
    II(c, d, a, b, x[14], S43, 0xab9423a7);	/* 51 */
    II(b, c, d, a, x[5], S44, 0xfc93a039);	/* 52 */
    II(a, b, c, d, x[12], S41, 0x655b59c3);	/* 53 */
    II(d, a, b, c, x[3], S42, 0x8f0ccc92);	/* 54 */
    II(c, d, a, b, x[10], S43, 0xffeff47d);	/* 55 */
    II(b, c, d, a, x[1], S44, 0x85845dd1);	/* 56 */
    II(a, b, c, d, x[8], S41, 0x6fa87e4f);	/* 57 */
    II(d, a, b, c, x[15], S42, 0xfe2ce6e0);	/* 58 */
    II(c, d, a, b, x[6], S43, 0xa3014314);	/* 59 */
    II(b, c, d, a, x[13], S44, 0x4e0811a1);	/* 60 */
    II(a, b, c, d, x[4], S41, 0xf7537e82);	/* 61 */
    II(d, a, b, c, x[11], S42, 0xbd3af235);	/* 62 */
    II(c, d, a, b, x[2], S43, 0x2ad7d2bb);	/* 63 */
    II(b, c, d, a, x[9], S44, 0xeb86d391);	/* 64 */

    state[0] += a;
    state[1] += b;
    state[2] += c;
    state[3] += d;

    /* Zeroize sensitive information. */
    memset((void *) x, 0, sizeof(x));
}

/*
 *******************************************************************
 * End of MD5 library code. What follows are our public functions to
 * exploit the MD5 algorithm.
 *******************************************************************
 */
static void b64encode(unsigned char *in, unsigned int len,
		      char *out, unsigned int size, int bits)
{
    register unsigned int bitpos, n, lo, hi;
    static char     b64string[] = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_.";

    if (bits > 6)
	return;

    for (bitpos = 0, n = 0; bitpos < (len * 8) && n < (size - 1); bitpos += bits, n++)
    {
	lo = in[bitpos / 8] >> (bitpos % 8);
	hi = (bitpos+ bits - 1 < (len * 8))?
	         in[(bitpos + bits - 1) / 8] << (8 - bitpos % 8):
	         0;
	out[n] = b64string[(lo | hi) & ((1<<bits)-1)];
    }

    if (n < size)
	out[n] = '\0';
}

/*
 * misMD5Data - The public face on these routines.  buf must be a character
 * array of at least 23 bytes.
 */
char *misMD5Data(void *data, unsigned int len, char *buf, unsigned int size,
		 int bits)
{
    MD5_CTX         ctx;
    unsigned char   digest[17]; /* Only 16 used for MD5Final */

    MD5Init(&ctx);
    MD5Update(&ctx, data, len);
    MD5Final(digest, &ctx);

    /*
     * If the caller passes us a "bits" value of 8 we deal with it in a
     * special way and don't base64-encode the message digest.  This also
     * means that callers need to deal with the results copied into the
     * given buffer as raw bytes rather than base64-encoded bytes, so they
     * can't assume they are getting a "normal" string back and do a printf( )
     * of it or anything like that.
     */
    if (bits == 8)
    {
        memset(buf, 0, size);
        memcpy(buf, digest, MIN(size, 16));
    }
    else
    {
        /*
         * For the convenience of b64encode, we allocate one extra byte of
         * data.
         */
        digest[16]=0;
        b64encode(digest, 16, buf, size, bits);
    }

    return buf;
}

/* 
 *   Following functions implement the MD5 password encryption used by 
 *   utilities like passwd and crypt. 
*/
static char *GenSeed(char *buffer, short len)
{
    static int inited = 0;

    int i = 0;
    int pos = 0;

    if (!inited)
    {
	inited++;
	srand(time(NULL));
    }

    for (i =0; i < len; i++) 
    {
        buffer[pos++] = itoa64[rand() % (sizeof itoa64 - 1)];
    }

    buffer[i] = '\0';

    return buffer;
}

static void to64(char *s, unsigned int v, int n)
{
    while (--n >= 0) 
    {
        *s++ = itoa64[v&0x3f];
	v >>= 6;
    }
}

/* 
 * misMD5Password 
 * Generates a MD5encryption of an input password and returns
 * the seed (or generated seed if none provided) in the format
 * "$1$<seed>$<ciphertext>". This is the same algorithm used by later
 * crypt libraries that support md5. The $1$ at the begining of the
 * password indicates that md5 was used to encrypt the plain text.
 * 
 * example : 
 * char *seed  = "$1$01234567$"; or seed=NULL or seed="2" or seed="23234433"
 * char * passwd = "password"
 * char * encrypt = misMD5Password(passwd, seed);
 * printf("%s converted to %s", passwd, encrypt);
 * free(encrypt);
 * ------------ 
 * example comparing passwords:
 * ...
 * char *passwdFromFile="$1$01234567$b5lh2mHyD2PdJjFfALlEz1";
 * char *inputtxt="password";
 * char *ciphertext =  misMD5Password(passwd, passwdFromFile);
 * if (strcmp(ciphertext, passwdFromFile) == 0) {
 *     authenticated=TRUE;
 * }
 * free(ciphertext);
 * ...
 */
        
char *misMD5Password(const char *pw, const char *salt)
{
    static char           passwd[120], *p;
    static char           *sp;
    static char           *ep;
    unsigned char         final[16];
    char                  *saltIndicator=NULL;
 
    /* standard indicator of a MD5 password */
    const char            *magic=MD5_INDICATOR;
    char                  seed[9];
    int                   sl, pl, ii;
    MD5_CTX               ctx, ctx1;
    unsigned long         l;
    char                  *retval = NULL;  
 
    /* Refine the Salt first */
    sp = (char *) salt;
    if (sp != NULL) 
    {
	/* If it starts with the magic string, then skip that */
	if (!strncmp(sp,magic, strlen(magic)))
	    sp += strlen(magic);

	/* It stops at the first '$', max 8 chars */
	for (ep=sp; *ep && *ep != '$' && ep < (sp+8); ep++)
	    continue;

	/* get the length of the true salt */
	sl = ep - sp;
    }
    else
    {
	memset(seed, 0, sizeof(seed));
	sp = GenSeed(seed, sizeof(seed)-1);
        sl = strlen(sp);
    }

    MD5Init(&ctx);
            
    /* The password first, since that is what is most unknown */
    MD5Update(&ctx, (const unsigned char *) pw, strlen(pw));
            
    /* Then our magic string */
    MD5Update(&ctx, (const unsigned char *) magic, strlen(magic));
            
    /* Then the raw salt */
    MD5Update(&ctx, (const unsigned char *) sp, sl);
            
    /* Then just as many characters of the MD5(pw, salt, pw) */
    MD5Init(&ctx1);
    MD5Update(&ctx1, (const unsigned char *) pw, strlen(pw));
    MD5Update(&ctx1, (const unsigned char *) sp, sl);
    MD5Update(&ctx1, (const unsigned char *) pw, strlen(pw));
    MD5Final(final, &ctx1);
            
    for (pl = strlen(pw); pl > 0; pl -= 16) 
    {
	int tl = pl > 16 ? 16 : pl;
	MD5Update(&ctx, final, pl>16 ? 16 : pl);
    }
            
    /* Don't leave anything around in vm they could use. */
    memset(final, 0, sizeof final);
            
    /* Then something really weird... */
    for (ii = strlen(pw); ii; ii >>= 1) 
    {
	if (ii&1)
	    MD5Update(&ctx, final, 1);
	else
	    MD5Update(&ctx, (const unsigned char *) pw, 1);
    }
            
    /* Now make the output string */
    sprintf(passwd, "%s%.*s$", (char *) magic, sl, sp);

    MD5Final(final, &ctx);
            
    /*
     * and now, just to make sure things don't run too fast
     * On a 60 Mhz Pentium this takes 34 msec, so you would
     * need 30 seconds to build a 1000 entry dictionary...
     */
    for (ii=0; ii<1000; ii++) 
    {
	MD5Init(&ctx1);

	if (ii & 1)
	    MD5Update(&ctx1, (const unsigned char *) pw, strlen(pw));
	else
	    MD5Update(&ctx1, final, 16);
                
	if (ii % 3)
	    MD5Update(&ctx1, (const unsigned char *) sp, sl);

	if (ii % 7)
	    MD5Update(&ctx1, (const unsigned char *) pw, strlen(pw));
                
	if (ii & 1)
	    MD5Update(&ctx1, final, 16);
	else
	    MD5Update(&ctx1, (const unsigned char *) pw, strlen(pw));

	MD5Final(final, &ctx1);
    }

    p = passwd + strlen(passwd);
            
    l = (final[0]<<16) | (final[6]<<8) | final[12]; to64(p, l, 4); p += 4;
    l = (final[1]<<16) | (final[7]<<8) | final[13]; to64(p, l, 4); p += 4;
    l = (final[2]<<16) | (final[8]<<8) | final[14]; to64(p, l, 4); p += 4;
    l = (final[3]<<16) | (final[9]<<8) | final[15]; to64(p, l, 4); p += 4;
    l = (final[4]<<16) | (final[10]<<8) | final[5]; to64(p, l, 4); p += 4;
    l = final[11]; to64(p, l, 2); p += 2;
    *p = '\0';
            
    /* Don't leave anything around in vm they could use. */
    memset(final, 0, sizeof final);
    misDynStrcpy(&retval, (const char *) passwd);

    return retval;
}            

short misIsMD5Password(char *passwd) 
{
    char *locate = NULL;

    locate = strstr(passwd, MD5_INDICATOR);

    return (locate == passwd) ? 1 : 0;
}

short misMatchMD5Password(char *password, char *ciphertext)
{
    short status = eERROR;
    char *result = NULL;

    /* Make sure we were given the password and ciphertext to check. */
    if (!password || !ciphertext)
        return eERROR;

    /* Don't bother if the ciphertext isn't even encoded. */
    if (misIsMD5Password(ciphertext))
    {
        result = misMD5Password(password, ciphertext);
        if (strcmp(result, ciphertext) == 0)
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
