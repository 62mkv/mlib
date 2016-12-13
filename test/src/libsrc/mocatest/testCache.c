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

LIBEXPORT RETURN_STRUCT *testCacheCreate()
{
    RETURN_STRUCT *results;
    MISCACHE *cache;

    cache = misCacheInit(10, NULL);

    results = srvResults(eOK, "cache", COMTYP_GENERIC, sizeof cache, cache, NULL);

    return results;
}

LIBEXPORT RETURN_STRUCT *testCacheClose(void **i_cache)
{
    RETURN_STRUCT *results;
    MISCACHE *cache = i_cache ? *i_cache : NULL;

    misCacheFree(cache);

    results = srvResults(eOK, NULL);

    return results;
}

LIBEXPORT RETURN_STRUCT *testCacheAdd(void **i_cache, char *i_name, char *i_value)
{
    RETURN_STRUCT *results;
    MISCACHE *cache = i_cache ? *i_cache : NULL;
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

LIBEXPORT RETURN_STRUCT *testCacheRemove(void **i_cache, char *i_name)
{
    RETURN_STRUCT *results;
    MISCACHE *cache = i_cache ? *i_cache : NULL;

    misCacheDelete(cache, i_name);

    results = srvResults(eOK, NULL);

    return results;
}

LIBEXPORT RETURN_STRUCT *testCacheGet(void **i_cache, char *i_name)
{
    RETURN_STRUCT *results;
    MISCACHE *cache = i_cache ? *i_cache : NULL;
    char *value;

    value = misCacheGet(cache, i_name);

    results = srvResults(eOK, "value", COMTYP_CHAR,
        value ? (strlen(value) + 1) : 0, value, NULL);

    return results;
}

LIBEXPORT RETURN_STRUCT *testCacheInitiate(void)
{
    RETURN_STRUCT *initResults = NULL;
    RETURN_STRUCT *results = NULL;
    MISCACHE *cache;
    long status;

    cache = misCacheInit(10, NULL);

    misCachePut(cache, "hello", strcpy(malloc(10), "zzz"));
    misCachePut(cache, "testkey", strcpy(malloc(10), "yyy"));

    status = srvInitiateCommandFormat( &initResults,
	    "test cache get where cache = '%p' and key = 'testkey' |" 
	    "[[ moca.setTransactionAttribute('initiateTest', value)]]",
	    cache);

    if (status != eOK)
    {
        return initResults;
    }

    misCacheFree(cache);

    results = srvResults(eOK, "foo", COMTYP_CHAR, 100, "zzz", 
			      NULL);

    srvFreeMemory(SRVRET_STRUCT, initResults);
    return results;
}
