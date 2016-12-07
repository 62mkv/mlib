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

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include <common.h>
#include <mocagendef.h>
#include <mocaerr.h>
#include <mislib.h>
#include <sqllib.h>

#define VAR_CHARSET "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_"

static void CopyQuoted(char **outstr, char *string)
{
    int origlen, len;
    char *s,*d;

    origlen = strlen(*outstr);

    for (len = 2, s = string; *s; s++,len++)
    {
        if (*s == '\'') 
	    len++;
    }

    *outstr = realloc(*outstr, origlen + len + 1);

    d = (*outstr)+origlen;
    s = string;
    *d++ = '\'';

    while (*s)
    {
        *d++ = *s;
        if (*s == '\'')
            *d++ = '\'';
        s++;
    }

    *d++ = '\'';
    *d++ = '\0';
}

char *sqlUnbind(char *stmt, mocaBindList *BindList)
{
    char *p;
    char *copied;
    char *newstr;
    char varname[100];
    char numstr[100];
    int nchars;
    mocaBindList *bp;

    copied = p = stmt;
    newstr = NULL;

    while (p && (p = strpbrk(p, ":'\"")))
    {
        switch(*p)
        {
        case '"':
        case '\'':
            p = strchr(p+1, *p);
            if (p)
                p++;
            break;
        case ':':
            misDynStrncat(&newstr, copied, p - copied);
            nchars = strspn(p+1, VAR_CHARSET);
            strncpy(varname, p+1, nchars);
            varname[nchars] = '\0';
            p += nchars+1;
            copied = p;

            for (bp=BindList;bp;bp=bp->next)
            {
                if (0 == misCiStrcmp(varname, bp->name))
                {
                    if (bp->nullind)
                    {
                        misDynStrcat(&newstr, "NULL");
                    }
        
                    else
                    {
                        switch(bp->dtype)
                        {
                        case COMTYP_LONG:
                        case COMTYP_BOOLEAN:
                        case COMTYP_INT:
                            sprintf(numstr, "%ld", *(long *)bp->data);
                            misDynStrcat(&newstr, numstr);
                            break;
                        case COMTYP_FLOAT:
                            sprintf(numstr, MOCA_FLT_FMT, *(double *)bp->data);
                            misDynStrcat(&newstr, numstr);
                            break;
                        case COMTYP_DATTIM:
                        case COMTYP_STRING:
                            /* handle quotes within strings */
                            CopyQuoted(&newstr, bp->data);
                            break;
                        case COMTYP_CHARPTR:
                        case COMTYP_LONGPTR:
                        case COMTYP_FLOATPTR:
                        default:
                            misDynStrcat(&newstr, ":");
                            misDynStrcat(&newstr, varname);
                            break;
                        }
                    }
                    break;
                }
            }
            break;
        }
    }

    misDynStrcat(&newstr, copied);

    return newstr;
}
