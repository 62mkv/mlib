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

static MISCACHE *cache;

static void freeCache(void) {

    misCacheFree(cache);
    cache = NULL;
}

LIBEXPORT RETURN_STRUCT *testKeepaliveCacheCreate()
{
    RETURN_STRUCT *results;

    cache = misCacheInit(10, NULL);

    results = srvResults(eOK, "cache", COMTYP_GENERIC, sizeof cache, cache, NULL);

    srvRequestKeepalive("keepalive_test", freeCache);

    return results;
}

LIBEXPORT RETURN_STRUCT *testKeepaliveCacheClose()
{
    RETURN_STRUCT *results;

    misCacheFree(cache);
    srvReleaseKeepalive("keepalive_test");

    results = srvResults(eOK, NULL);

    return results;
}

LIBEXPORT RETURN_STRUCT *testKeepaliveCacheAdd(char *i_name, char *i_value)
{
    RETURN_STRUCT *results;
    char *value = NULL;

    if (i_value)
    {
	value = malloc(strlen(i_value) + 1);
	strcpy(value, i_value);
    }

    misCachePut(cache, i_name, value);

    results = srvResults(eOK, NULL);

    return results;
}

LIBEXPORT RETURN_STRUCT *testKeepaliveCacheRemove(void **i_cache, char *i_name)
{
    RETURN_STRUCT *results;

    if (cache) misCacheDelete(cache, i_name);

    results = srvResults(eOK, NULL);

    return results;
}

LIBEXPORT RETURN_STRUCT *testKeepaliveCacheGet(char *i_name)
{
    RETURN_STRUCT *results;
    char *value = NULL;

    if (cache) value = misCacheGet(cache, i_name);

    results = srvResults(eOK, "value", COMTYP_CHAR,
        value ? (strlen(value) + 1) : 0, value, NULL);

    return results;
}
