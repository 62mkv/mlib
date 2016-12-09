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

#include <string.h>
#include <ctype.h>

#include <mislib.h>

char *misAsciiToHex (char *ascii_str,
                     long number_of_bytes)
{
    long        ii;
    static char resultant_string_value[2001];
    
    memset(resultant_string_value,0,sizeof(resultant_string_value));
    
    if (number_of_bytes > 1000)
        number_of_bytes = 1000;

    for (ii = 1; ii <= number_of_bytes; ii++) {
        sprintf(&resultant_string_value[(ii-1)*2],
                "%.2X",(short)ascii_str[ii-1]);
    }
    
    return(resultant_string_value);
}
