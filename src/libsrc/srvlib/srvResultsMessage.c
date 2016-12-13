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

#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <sqllib.h>
#include <oslib.h>
#include <mislib.h>

#include "srvprivate.h"

static char *SubstituteArgs(char *dstring, char *msg, 
                            char *defmsg, long size, SRV_ERROR_ARG *args)
{
    char *s, *d;
    char var[100],tmpstr[100];
    char *varptr, *endptr, *subptr;
    char *tmpptr = NULL;
    long nchars;
    SRV_ERROR_ARG *tmpArg;

    d = dstring;
    s = msg;

    while ((d-dstring)< size)
    {
	if (*s == '^')
	{
	    varptr = s+1;
	    endptr = strchr(varptr, '^');

	    if (endptr)
	    {
		nchars = endptr - varptr;

		s=endptr + 1;

		if (nchars >= sizeof var - 1)
		    nchars = sizeof var - 2;

		strncpy(var, varptr, nchars);
		var[nchars] = '\0';

		subptr = NULL;
		tmpptr = NULL;
                if (strcmp(var, "!") == 0)
                {
                    subptr = dbErrorText();
                }
                else if (strcmp(var, "?") == 0)
                {
                    sprintf(tmpstr, "%ld", dbErrorNumber());
                    subptr = tmpstr;
                }
                else if (strcmp(var, "*") == 0)
                {
                    subptr = defmsg;
                }
                else
                {
                    for (tmpArg = args; tmpArg; tmpArg = tmpArg->next)
                    {
                        if (0 == misCiStrcmp(var, tmpArg->varnam))
                        {
                            switch (tmpArg->type)
                            {
                            case COMTYP_STRING:
                            case COMTYP_DATTIM:
                                if (tmpArg->lookup)
				{
                                    tmpptr = jni_srvTranslateMessage(tmpArg->data.cdata);
				    subptr = tmpptr;
				}

                                if (! subptr)
                                    subptr = tmpArg->data.cdata;
                                break;
                            case COMTYP_BOOLEAN:
                            case COMTYP_INT:
                            case COMTYP_LONG:
                                sprintf(tmpstr, "%ld", tmpArg->data.ldata);
                                subptr = tmpstr;
                                break;
                            case COMTYP_FLOAT:
                                sprintf(tmpstr, MOCA_FLT_FMT, tmpArg->data.fdata);
                                subptr = tmpstr;
                                break;
                            }
                            break;
                        }
                    }
                }

		/* If the substitution string was not found, keep the token */
		if (!subptr)
		{
		    *d++ = '^';
		    subptr = var;
		    strcat(var, "^");
		}		    

		while (*subptr && ((d - dstring) < size))
		{
		    *d = *subptr;
		    d++, subptr++;
		}

		if (tmpptr) free(tmpptr);
	    }
	    else
	    {
		*d = *s;
		d++, s++;
	    }
	}
	else
	{
	    if (!(*d = *s))
		break;
	    d++,s++;
	}
    }

    if ((d - dstring) >= size)
	dstring[size - 1] = '\0';

    return dstring;
}

char *srv_ResultsMessageStatus(RETURN_STRUCT *ret, long status)
{
    static int inloop;
    static char buffer[500];
    char *msg = NULL;
    char *tmpptr = NULL;

    if (inloop)
    {
        sprintf(buffer, "Recursive loop hit while handling error message");
        return buffer;
    }

    buffer[0] = '\0';

    if (!ret)
	return buffer;

    if (ret->ReturnedData &&
	ret->ReturnedData->Message &&
	strlen(ret->ReturnedData->Message))
    {
	strncpy(buffer, ret->ReturnedData->Message, sizeof buffer - 1);
	return buffer;
    }

    /* An Oracle Error */
    if (status < 0 && dbErrorNumber() == status) 
	msg = dbErrorText();

    if (!msg)
    {
        char statusId[20];
	sprintf(statusId, "err%d", status);
        inloop = 1;
        tmpptr = jni_srvTranslateMessage(statusId);
	msg = tmpptr;
        inloop = 0;
    }

    if (!msg)
	msg = ret->Error.DefaultText;

    if (msg)
	SubstituteArgs(buffer, msg, ret->Error.DefaultText,
                       sizeof buffer, ret->Error.Args);
    else
	sprintf(buffer, "No text found for error %ld.", status);

    if (tmpptr) free(tmpptr);
    
    return(buffer);
}

char *srvResultsMessage(RETURN_STRUCT *ret)
{
    return srv_ResultsMessageStatus(ret, ret->Error.Code);
}
