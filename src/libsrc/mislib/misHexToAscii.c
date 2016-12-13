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

#include <moca.h>

#include <stdio.h>
#include <string.h>
#include <ctype.h>

#include <mislib.h>

char *misHexToAscii(char *hex_str)
{
    long        ii;
    static char resultant_string_value1[1001];

    
    memset(resultant_string_value1,0,sizeof(resultant_string_value1));
    
    ii = 0;
    while (hex_str[ii]!='\0' && hex_str[ii]!=' ') {
        resultant_string_value1[ii/2] =
            (char)((isdigit(hex_str[ii]) ? hex_str[ii]-'0': hex_str[ii]-'7')*16
                   +(isdigit(hex_str[ii+1])?hex_str[ii+1]-'0':hex_str[ii+1]-'7'));
        ii+=2;
    }
    
    return(resultant_string_value1);
}
