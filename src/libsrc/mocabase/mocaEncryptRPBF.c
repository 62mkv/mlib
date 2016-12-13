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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mislib.h>
#include <sqllib.h>
#include <srvlib.h>

#include "mocabase.h"

LIBEXPORT 
RETURN_STRUCT *mocaEncryptRPBF(long *blocksize_i, char *data_i, long *data_len_i, void **data_ptr_i)
{
    void *data     = NULL;
    char *out_data = NULL;
    long  out_len = 0;
    long  blocksize;
    long  pos;
    long  data_len;

    RETURN_STRUCT *ret;

    /* Validate our arguments. */
    if(data_i)
    {
        data = data_i;
        data_len = (data_len_i)?*data_len_i:strlen(data_i);
    }
    else
    {
        if(!data_ptr_i)
        {
	   return MOCAMissingArg("data_ptr");
        }
        else
        {
           if(!data_len_i)
           {
	       return MOCAMissingArg("data_ptr");
           }
           data = *data_ptr_i;
           data_len = *data_len_i;
        }
    }

    if(blocksize_i && *blocksize_i>0)
    {
        blocksize = *blocksize_i;
    }
    else
    {
        blocksize = data_len;
    }

    /* NOTE: input on output length are the same (nature of RPBF) */
    out_data = calloc(1, data_len + 1); /* added one for null term, but if use pointer it is not needed */
    memcpy(out_data, data, data_len);
    for(pos=0; pos<=data_len && data_len > 0; pos+=blocksize)
    {   long len;
        long cur_blocksize = blocksize;
 
        if(pos+cur_blocksize>data_len)
        {
            cur_blocksize=data_len-pos;
        }
        misEncryptRPBF((char*)out_data+pos, cur_blocksize, &len);
        out_len+=len;
    }

    ret = srvResults(eOK,
           "encrypted_data", COMTYP_BINARY, out_len, out_data,
           NULL
        );
    free(out_data);
    return ret;
}

LIBEXPORT 
RETURN_STRUCT *mocaDecryptRPBF(long *blocksize_i, char *data_i, long *data_len_i, void **data_ptr_i)
{
    void *data     = NULL;
    char *out_data = NULL;
    long  out_len = 0;
    long  blocksize;
    long  pos;
    long  data_len;

    RETURN_STRUCT *ret;

    /* Validate our arguments. */
    if(data_i)
    {
        data = data_i;
        data_len = (data_len_i)?*data_len_i:strlen(data_i);
    }
    else
    {
        if(!data_ptr_i)
        {
	   return MOCAMissingArg("data_ptr");
        }
        else
        {
           if(!data_len_i)
           {
	       return MOCAMissingArg("data_ptr");
           }
           data = *data_ptr_i;
           data_len = *data_len_i;
        }
    }

    if(blocksize_i && *blocksize_i>0)
    {
        blocksize = *blocksize_i;
    }
    else
    {
        blocksize = data_len;
    }

    /* NOTE: input on output length are the same (nature of RPBF) */
    /* added one for null term, but if use pointer it is not needed */
    out_data = calloc(1, data_len + 1);
    memcpy(out_data, data, data_len);
    for(pos=0; pos<=data_len && data_len > 0; pos+=blocksize)
    {   long len;
        long cur_blocksize = blocksize;
 
        if(pos+cur_blocksize>data_len)
        {
            cur_blocksize=data_len-pos;
        }
        misDecryptRPBF((char*)out_data+pos, cur_blocksize, &len);
        out_len+=len;
    }

    ret = srvResults(eOK,
           "decrypted_data", COMTYP_BINARY, out_len, out_data,
           NULL
        );
    free(out_data);
    return ret;
}
