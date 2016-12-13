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
 *  Copyright (c) 20168
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
 *#END************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <oslib.h>
#include <srvlib.h>

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

LIBEXPORT 
RETURN_STRUCT *testBlowfishEncodePassword(char *p1)
{
    char *e1;

    RETURN_STRUCT *results = NULL;
    
    e1 = misBlowfishEncodePassword(p1);
    results = srvResults(eOK, "encoded", COMTYP_CHAR, strlen(e1), e1, NULL); 

    return results;
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

LIBEXPORT RETURN_STRUCT *testBlowfishDecodePassword(char *e1)
{
    char *d1;
    RETURN_STRUCT *results = NULL;

    d1 = misBlowfishDecodePassword(e1);
    results = srvResults(eOK, "decode", COMTYP_CHAR, strlen(d1), d1, NULL);

    return results;
}
