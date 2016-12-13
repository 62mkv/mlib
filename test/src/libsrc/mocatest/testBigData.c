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
 *#END************************************************************************/

#include <moca.h>

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <common.h>
#include <mocaerr.h>
#include <srvlib.h>
#include <sqllib.h>
#include <mislib.h>

LIBEXPORT RETURN_STRUCT *testBigData(int *i_iterations)
{
    int i, iterations;
    RETURN_STRUCT *results;

    iterations = i_iterations?*i_iterations:0;

    results = srvResultsInit(eOK, "i", COMTYP_INT, sizeof(long),
                                  "a", COMTYP_STRING, 100,
                                  "b", COMTYP_STRING, 200,
                                  "c", COMTYP_STRING, 200,
                                  "d", COMTYP_STRING, 200,
                                  "e", COMTYP_STRING, 200,
                                  "f", COMTYP_STRING, 200,
                                  "g", COMTYP_STRING, 200,
                                  "h", COMTYP_STRING, 200,
                                  "i", COMTYP_STRING, 200,
                                  "j", COMTYP_STRING, 200,
                                  "k", COMTYP_STRING, 200,
                                  "l", COMTYP_STRING, 200,
                                  "m", COMTYP_STRING, 200,
                                  "n", COMTYP_STRING, 200,
                                  "o", COMTYP_STRING, 200,
                                  "p", COMTYP_STRING, 200,
                                  "q", COMTYP_STRING, 200,
                                  "r", COMTYP_STRING, 200,
                                  "s", COMTYP_STRING, 200,
                                  "t", COMTYP_STRING, 200,
                                  "u", COMTYP_STRING, 200,
                                  "v", COMTYP_STRING, 200,
                                  "w", COMTYP_STRING, 200,
                                  "x", COMTYP_STRING, 200,
                                  "y", COMTYP_STRING, 200,
                                  "z", COMTYP_STRING, 200,
                                  NULL);

    for (i=0;i<iterations;i++)
    {
        srvResultsAdd(results, i,
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA",
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                "ccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc",
                "ddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd",
                "eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee",
                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                "ggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg",
                "hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh",
                "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
                "jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjj",
                "kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk",
                "lllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll",
                "mmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm",
                "nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn",
                "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo",
                "ppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppppp",
                "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq",
                "rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr",
                "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss",
                "ttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttttt",
                "uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu",
                "vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv",
                "wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww",
                "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                "yyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy",
                "zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz"
                );
    }

    return results;
}
