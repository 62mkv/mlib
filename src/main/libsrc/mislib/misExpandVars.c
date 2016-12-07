static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Support expanding environment variables of the following form:
 *
 *                   $MY_NAME
 *                   ${MY_NAME}
 *                   %MY_NAME%
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2002-2003
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

#include <mocaerr.h>
#include <mocagendef.h>
#include <oslib.h>

#define VAR_CHARSET "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_"

char *misExpandVars(char *dest, char *src, long destSize, char *(*func)(char *))
{
    int nameSize;

    char *name,
         *srcPtr,
         *destPtr,
         *valuePtr,
         *nameStartPtr,
         *nameEndPtr;

    /* Use our own environment variable lookup function if one wasn't given. */
    if (!func)
        func = osGetVar;

    /* Set pointers to the beginning of the source and destination strings. */
    srcPtr  = src;
    destPtr = dest;

    /* Cycle through every character in the destination string we can. */
    while ((destPtr - dest) < destSize)
    {
        /* Handle environment variables in the form ${MY_NAME}. */
        if (*srcPtr == '$' && *(srcPtr+1) == '{')
        {
            /* Skip past the '$' and '{' characters. */
            nameStartPtr = srcPtr + 2;

            /* Get the end of the environment variable name. */
            nameEndPtr = strchr(nameStartPtr, '}');

            /*
             * For this to really be an environment variable, it has to both
             * begin with a '{' character and end with a '}' character.
             */
            if (nameEndPtr)
            {
                /* Get the size of the environment variable name. */
                nameSize = nameEndPtr - nameStartPtr;

                /* Reset the environment variable name. */
                name = NULL;

                /* Make a copy of the environment variable name. */
                misDynStrncpy(&name, nameStartPtr, nameSize);

                /* Get the value of this environment variable. */
                valuePtr = (*func)(name);

                while (valuePtr && *valuePtr && ((destPtr - dest) < destSize))
                {
                    /* Make a copy of this character. */
                    *destPtr = *valuePtr;

                    /* Move on to the next character. */
                    destPtr++, valuePtr++;
                }

                /* Free memory we dynamically allocated. */
                free(name);

                /* Skip to the end of the environment variable. */
                    srcPtr += nameSize + 3;
            }

            /* This wasn't really an environment variable. */
            else
            {
                /* Make a copy of this character. */
                *destPtr = *srcPtr;

                /* Move on to the next character. */
                destPtr++, srcPtr++;
            }
        }

        /* Handle environment variables in the form $MY_NAME. */
        else if (*srcPtr == '$')
        {
            /* Skip past the '$' character. */
            nameStartPtr = srcPtr + 1;

            /* Get the size of the environment variable name. */
            nameSize = strspn(nameStartPtr, VAR_CHARSET);

            /* Get the end of the environment variable name. */
            nameEndPtr = nameStartPtr + nameSize;

            /* Reset the environment variable name. */
            name = NULL;

            /* Make a copy of the environment variable name. */
            misDynStrncpy(&name, nameStartPtr, nameSize);

            /* Get the value of this environment variable. */
            valuePtr = (*func)(name);

            while (valuePtr && *valuePtr && ((destPtr - dest) < destSize))
            {
                /* Make a copy of this character. */
                *destPtr = *valuePtr;

                /* Move on to the next character. */
                destPtr++, valuePtr++;
            }

            /* Free memory we dynamically allocated. */
            free(name);

            /* Skip to the end of the environment variable. */
            srcPtr += nameSize + 1;
        }

        /* Handle environment variables in the form %MY_NAME%. */
        else if (*srcPtr == '%')
        {
            /* Get the start of the environment variable name. */
            nameStartPtr = srcPtr + 1;

            /* Get the end of the environment variable name. */
            nameEndPtr = strchr(nameStartPtr, '%');

            /*
             * For this to really be an environment variable, it has to both
             * begin and end with a '%' character.
             */
            if (nameEndPtr)
            {
                /* Get the size of the environment variable name. */
                nameSize = nameEndPtr - nameStartPtr;

                /* Reset the environment variable name. */
                name = NULL;

                /* Make a copy of the environment variable name. */
                misDynStrncpy(&name, nameStartPtr, nameSize);

                /* Get the value of this environment variable. */
                valuePtr = (*func)(name);

                /* Copy the value into the destination string. */
                while (valuePtr && *valuePtr && ((destPtr - dest) < destSize))
                {
                    /* Make a copy of this character. */
                    *destPtr = *valuePtr;

                    /* Move on to the next character. */
                    destPtr++, valuePtr++;
                }

                /* Free memory we dynamically allocated. */
                free(name);

                /* Skip to the end of the environment variable. */
                srcPtr = nameEndPtr + 1;
            }

            /* This wasn't really an environment variable. */
            else
            {
                /* Make a copy of this character. */
                *destPtr = *srcPtr;

                /* Move on to the next character. */
                destPtr++, srcPtr++;
            }
        }

        /* Found just a regular character. */
        else
        {
            /* Make a copy of this character. */
            *destPtr = *srcPtr;

            /* We can stop if we've hit a null character. */
            if (! *destPtr)
            break;

            /* Move on to the next character. */
            destPtr++, srcPtr++;
        }
    }

    /* Terminate the destination string if there's room. */
    if ((destPtr - dest) >= destSize)
        dest[destSize - 1] = '\0';

    return dest;
}

char *misDynExpandVars(char *src, char *(*func)(char *))
{
    int nameSize;

    char *name,
	 *srcPtr, 
	 *valuePtr,
	 *nameStartPtr,
	 *nameEndPtr;

    MIS_DYNBUF *destBuf;

    /* Use our own environment variable lookup function if one wasn't given. */
    if (!func)
	func = osGetVar;

    /* Set pointers to the beginning of the source and destination strings. */
    srcPtr  = src;
    destBuf = misDynBufInit(128);

    /* Cycle through every character in the destination string we can. */
    while (*srcPtr)
    {
	/* Handle environment variables in the form ${MY_NAME}. */
	if (*srcPtr == '$' && *(srcPtr+1) == '{')
	{
	    /* Skip past the '$' and '{' characters. */
	    nameStartPtr = srcPtr + 2;

	    /* Get the end of the environment variable name. */
	    nameEndPtr = strchr(nameStartPtr, '}');

	    /* 
	     * For this to really be an environment variable, it has to both
	     * begin with a '{' character and end with a '}' character. 
	     */
	    if (nameEndPtr)
	    {
	        /* Get the size of the environment variable name. */
	        nameSize = nameEndPtr - nameStartPtr;

		/* Reset the environment variable name. */
		name = NULL;

	        /* Make a copy of the environment variable name. */
	        misDynStrncpy(&name, nameStartPtr, nameSize);

	        /* Get the value of this environment variable. */
	        valuePtr = (*func)(name);

	        while (valuePtr && *valuePtr)
	        {
	            /* Make a copy of this character. */
                    misDynBufAppendChar(destBuf, *valuePtr);
		    valuePtr++;
	        }

	        /* Free memory we dynamically allocated. */
	        free(name);

  	        /* Skip to the end of the environment variable. */
                srcPtr += nameSize + 3;
	    }

	    /* This wasn't really an environment variable. */
	    else
	    {
	        /* Make a copy of this character. */
                misDynBufAppendChar(destBuf, *srcPtr);

	        /* Move on to the next character. */
		srcPtr++;
	    }
	}

	/* Handle environment variables in the form $MY_NAME. */
	else if (*srcPtr == '$')
	{
	    /* Skip past the '$' character. */
	    nameStartPtr = srcPtr + 1;

	    /* Get the size of the environment variable name. */
	    nameSize = strspn(nameStartPtr, VAR_CHARSET);

	    /* Get the end of the environment variable name. */
	    nameEndPtr = nameStartPtr + nameSize;

	    /* Reset the environment variable name. */
	    name = NULL;

	    /* Make a copy of the environment variable name. */
	    misDynStrncpy(&name, nameStartPtr, nameSize);

	    /* Get the value of this environment variable. */
	    valuePtr = (*func)(name);

	    while (valuePtr && *valuePtr)
	    {
	        /* Make a copy of this character. */
                misDynBufAppendChar(destBuf, *valuePtr);
                valuePtr++;
	    }

	    /* Free memory we dynamically allocated. */
	    free(name);

  	    /* Skip to the end of the environment variable. */
            srcPtr += nameSize + 1;
	}

	/* Handle environment variables in the form %MY_NAME%. */
	else if (*srcPtr == '%')
	{
	    /* Get the start of the environment variable name. */
	    nameStartPtr = srcPtr + 1;

	    /* Get the end of the environment variable name. */
	    nameEndPtr = strchr(nameStartPtr, '%');

	    /* 
	     * For this to really be an environment variable, it has to both
	     * begin and end with a '%' character. 
	     */
	    if (nameEndPtr)
	    {
		/* Get the size of the environment variable name. */
		nameSize = nameEndPtr - nameStartPtr;

		/* Reset the environment variable name. */
		name = NULL;

		/* Make a copy of the environment variable name. */
		misDynStrncpy(&name, nameStartPtr, nameSize);

		/* Get the value of this environment variable. */
		valuePtr = (*func)(name);
	
		/* Copy the value into the destination string. */
		while (valuePtr && *valuePtr)
		{
	            /* Make a copy of this character. */
                    misDynBufAppendChar(destBuf, *valuePtr);
                    valuePtr++;
		}

		/* Free memory we dynamically allocated. */
		free(name);

		/* Skip to the end of the environment variable. */
		srcPtr = nameEndPtr + 1;
	    }

	    /* This wasn't really an environment variable. */
	    else
	    {
	        /* Make a copy of this character. */
                misDynBufAppendChar(destBuf, *srcPtr);

	        /* Move on to the next character. */
		srcPtr++;
	    }
	}

	/* Found just a regular character. */
	else
	{
	    /* Make a copy of this character. */
            misDynBufAppendChar(destBuf, *srcPtr);

	    /* Move on to the next character. */
	    srcPtr++;
	}
    }

    return misDynBufClose(destBuf);
}
