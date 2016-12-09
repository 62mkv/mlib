static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: MOCA Server Object library
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

// MOCAServerAccess.cpp : Implementation of CMOCAServerAccess
#include "stdafx.h"
#include "MOCASrvObj.h"
#include "MOCAServerAccess.h"
#include <oslib.h>
#include "Utils.h"
#include "../jnilib/jniprivate.h"

//
// AllocateOleString - Returns a BSTR when passed a char *
//
BSTR util_AllocOleString(char* pStr)
{

    int needToDetach, wideSize;
    UINT extCodePage;
    wchar_t *wideBuf;

    jni_GetEnv(&needToDetach);

    extCodePage = DEFAULT_CODEPAGE;
    if (osIsExtCodePageSet())
    {
        extCodePage = osGetExternalCodePage();
    }

    if (!pStr)
	pStr = "";

    /* Determine what the max length of the wide-character string would be. */
    wideSize = 2 * (strlen(pStr) + 1);

    /* Allocate space for the wide-character string. */
    wideBuf = (wchar_t *) malloc(wideSize);

    /* Convert the given string to a wide-character string. */
    wideSize = MultiByteToWideChar(extCodePage, 0, 
              pStr, -1, wideBuf, wideSize);
    
    _bstr_t newBstr(wideBuf);

    if (needToDetach) jni_ReleaseEnv();

    return newBstr.copy();

}

//
// GetMultiByte - Returns a char * when passed a BSTR.
//
char *util_GetMultiByte(BSTR bstrIn)
{
    int needToDetach;
    UINT extCodePage= DEFAULT_CODEPAGE;

    jni_GetEnv(&needToDetach);

    if (osIsExtCodePageSet())
    {
        extCodePage = osGetExternalCodePage();
    }
    int length = SysStringLen(bstrIn);
    int count = WideCharToMultiByte(extCodePage, 0, 
                                    bstrIn, length, NULL, 0, NULL, NULL);
    char *pStr = (char *)calloc(count+1, sizeof(char));
    WideCharToMultiByte(extCodePage, 0, 
                        bstrIn, length, pStr, count, NULL, NULL);

    if (needToDetach) jni_ReleaseEnv();

    return pStr;
}


