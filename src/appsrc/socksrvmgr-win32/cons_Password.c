/*#START***********************************************************************
 *
 *  $URL$
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
#include <stdlib.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>

#include "socksrvmgr.h"

long cons_Password(CONS *c, int argc, char *argv[])
{
    long status = eOK;
    char *ciphertext = NULL;

    if (argc < 1)
    {
	cons_printf(c, "Usage: password <password>\n");
	return eERROR;
    } 

    /* Create an MD5 hash of the given password. */
    ciphertext = misMD5Password(argv[0], param.console_password);

    /* We don't require the passowrd in the registry to be hashed. */
    if (strcmp(argv[0],    param.console_password) == 0 ||
        strcmp(ciphertext, param.console_password) == 0)
    {
        c->privlevel = PRIV_ADMIN;
	status = eOK;
    }
    else
    {
        cons_printf(c, "Invalid password\n");
 	c->privlevel = PRIV_LUSER;
	status = eERROR;
    }

    free(ciphertext);

    return status;
}
