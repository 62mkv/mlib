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
#include "Utils.h"
#include "../jnilib/jniprivate.h"

/////////////////////////////////////////////////////////////////////////////
// CMOCAServerAccess

_COM_SMARTPTR_TYPEDEF(IMOCAResults, IID_IMOCAResults);

//
// MOCAServerAccess::SQLExecute - Execute a SQL statement in the current 
// server context.
//
STDMETHODIMP CMOCAServerAccess::SQLExecute(BSTR SqlCommand, IMOCAResults **Results)
{
    int needToDetach;
    long sqlStatus;
    char *strSqlCommand;
    mocaDataRes *sqlRes = NULL;
    RETURN_STRUCT *srvRes = NULL;

    jni_GetEnv(&needToDetach);

    strSqlCommand = util_GetMultiByte(SqlCommand);

    //
    // Execute the incoming SQL statement.
    // We only want to capture results if the results pointer was passed.
    //
    sqlStatus = sqlExecStr(strSqlCommand, &sqlRes);

    // We keep a Server Results Structure, so we need
    // to build one from the execution results.
    srvRes = srvAddSQLResults(sqlRes, sqlStatus);

    // Use a MOCAResults smart pointer
    IMOCAResultsPtr spResults;
    spResults.CreateInstance(CLSID_MOCAResults);

    // Put our results pointer (as a long) into the MOCAResults
    // object.
    spResults->put_Results((long) srvRes);
    
    // We're passing this back, so let's add a reference to this object
    // (that way, it won't get deleted).
    spResults.AddRef();

    // Pass it back.
    *Results = spResults;

    // Free up our temporary multibyte string.
    free(strSqlCommand);

    if (needToDetach) jni_ReleaseEnv();

    return S_OK;
}

//
// MOCAServerAccess::Initiate - Execute a MOCA command in the current 
// server context.
//
STDMETHODIMP CMOCAServerAccess::Initiate(BSTR ServerCommand, IMOCAResults **Results)
{
    int needToDetach;
    char *strServerCommand;
    RETURN_STRUCT *srvRes = NULL;
    long srvStatus;

    jni_GetEnv(&needToDetach);

    strServerCommand = util_GetMultiByte(ServerCommand);

    srvStatus = srvInitiateInline(strServerCommand, &srvRes);

    // Use a MOCAResults smart pointer
    IMOCAResultsPtr spResults;
    spResults.CreateInstance(CLSID_MOCAResults);

    // Put our results pointer (as a long) into the MOCAResults
    // object.
    spResults->put_Results((long) srvRes);
    
    // We're passing this back, so let's add a reference to this object
    // (that way, it won't get deleted).
    spResults.AddRef();

    // Pass it back.
    *Results = spResults;

    // Free up our temporary multibyte string.
    free(strServerCommand);

    if (needToDetach) jni_ReleaseEnv();

    return S_OK;
}

//
// MOCAServerAccess::Commit - Commit the outstanding MOCA transaction.
//
STDMETHODIMP CMOCAServerAccess::Commit(long *Status)
{
    int needToDetach;

    jni_GetEnv(&needToDetach);

    // Commit our outstanding transaction.
    *Status = srvCommit();

    if (needToDetach) jni_ReleaseEnv();

    return S_OK;
}

//
// MOCAServerAccess::Commit - Roll back the outstanding MOCA transaction.
//
STDMETHODIMP CMOCAServerAccess::Rollback(long *Status)
{
    int needToDetach;

    jni_GetEnv(&needToDetach);

    // Roll back our outstanding transaction.
    *Status = srvRollback();

    if (needToDetach) jni_ReleaseEnv();

    return S_OK;
}

//
// MOCAServerAccess::GetNeededElement - Look up the passed name in  
// our server context.
//
STDMETHODIMP CMOCAServerAccess::GetNeededElement(BSTR Name, BSTR AltName, VARIANT *pVal)
{
    int needToDetach;
    char *strName = NULL;
    char *strAltName = NULL;
    char dtype;
    void *data;
    long status;

    //Binary data variables
    long lLen;
    long l=0;
    BYTE *byteArray; 
    SAFEARRAY *saBinary;

    jni_GetEnv(&needToDetach);
    
    //
    // Don't allow bad values to crash us.  If we get a bad string value,
    // use a NULL string (the default).
    //
    strName = util_GetMultiByte(Name);
    if (AltName)
	strAltName = util_GetMultiByte(AltName);

    // Initialize the outgoing variant to NULL, so that if we don't
    // find anything, it'll have a reasonable value.
    VariantInit(pVal);
    pVal->vt = VT_EMPTY;

    // Look up the Name(s) in our context.
    status = srvGetNeededElement(strName, strAltName, &dtype, &data);

    // Free the allocated strings.
    if (strName)
	free(strName);
    if (strAltName)
	free(strAltName);

    // If we actually found anything, get the data out.
    if (status == eOK)
    {
	//
	// Check the datatype of the returned element.  If the data pointer
	// is unset, that means we have a NULL value.
	//
	if (data)
	{
	    switch (dtype)
	    {
	    	case COMTYP_INT:
	    	case COMTYP_LONG:
		    pVal->vt = VT_I4;
		    pVal->lVal = *(long *)data;
		    break;

	    	case COMTYP_FLOAT:
		    pVal->vt = VT_R8;
		    pVal->dblVal = *(double *)data;
		    break;

	    	case COMTYP_CHAR:
	    	case COMTYP_DATTIM:
		    pVal->vt = VT_BSTR;
		    pVal->bstrVal = util_AllocOleString((char *)data);
		    break;

	    	case COMTYP_BOOLEAN:
		    pVal->vt = VT_BOOL;
		    pVal->boolVal = (*(moca_bool_t *)data)?VARIANT_TRUE:VARIANT_FALSE;
		    break;

		case COMTYP_BINARY:
		    // First 8 bytes are length
                    lLen = sqlDecodeBinaryLen(data);
            	    byteArray = (BYTE *)sqlDecodeBinary(data);
            	    saBinary = SafeArrayCreateVector(VT_UI1, 0, lLen);
            
            	    while (l < lLen)
            	    {
			HRESULT h = SafeArrayPutElement(saBinary, &l, &byteArray[l]);
                	l++;
            	    }
            	    pVal->vt = VT_ARRAY | VT_UI1;
            	    pVal->parray = saBinary;
            	    break;

	    	default:
		    // Set the value to NULL...we don't know what it is.
		    pVal->vt = VT_NULL;
		    break;
	    }
	}
	else
	{
	    //
	    // We've found the variable, but it has no value, so make
	    // it NULL.
	    //
	    pVal->vt = VT_NULL;
	}
    }

    if (needToDetach) jni_ReleaseEnv();

    return S_OK;
}


//
// MOCAServerAccess::LogError - Log an error message in the server log.
//
STDMETHODIMP CMOCAServerAccess::LogError(BSTR Message)
{
    int needToDetach;
    char *strMessage;

    jni_GetEnv(&needToDetach);

    strMessage = util_GetMultiByte(Message);
    misLogError("%s", strMessage);
    free(strMessage);

    if (needToDetach) jni_ReleaseEnv();

    return S_OK;
}

//
// MOCAServerAccess::LogWarning - Log a warning message in the server log.
//
STDMETHODIMP CMOCAServerAccess::LogWarning(BSTR Message)
{
    int needToDetach;
    char *strMessage;

    jni_GetEnv(&needToDetach);

    strMessage = util_GetMultiByte(Message);
    misLogWarning("%s", strMessage);
    free(strMessage);

    if (needToDetach) jni_ReleaseEnv();

    return S_OK;
}

//
// MOCAServerAccess::LogInfo - Log an informational message in the server log.
//
STDMETHODIMP CMOCAServerAccess::LogInfo(BSTR Message)
{
    int needToDetach;
    char *strMessage;

    jni_GetEnv(&needToDetach);

    strMessage = util_GetMultiByte(Message);
    misLogInfo("%s", strMessage);
    free(strMessage);

    if (needToDetach) jni_ReleaseEnv();

    return S_OK;
}

//
// MOCAServerAccess::TraceFlow - Put a trace message in the current
// server trace log, if one exists.
//
STDMETHODIMP CMOCAServerAccess::TraceFlow(BSTR Message)
{
    int needToDetach;

    jni_GetEnv(&needToDetach);

    if (misGetTraceLevel() & T_FLOW)
    {
	char *strMessage = util_GetMultiByte(Message);
	misTrc(T_FLOW, "%s", strMessage);
	free(strMessage);
    }

    if (needToDetach) jni_ReleaseEnv();

    return S_OK;
}

//
// MOCAServerAccess::GetVariable - Get the value of a MOCA process variable.
//
STDMETHODIMP CMOCAServerAccess::GetVariable(BSTR VarName, BSTR *Value)
{
    int needToDetach;
    char *strVarName;
    char *strValue;

    jni_GetEnv(&needToDetach);

    strVarName = util_GetMultiByte(VarName);
    strValue = osGetVar(strVarName);
    free(strVarName);

    if (!strValue)
    {
        if (needToDetach) jni_ReleaseEnv();
	return E_INVALIDARG;
    }

    *Value = util_AllocOleString(strValue);

    if (needToDetach) jni_ReleaseEnv();

    return S_OK;
}

//
// MOCAServerAccess::InitiateClean - Execute a MOCA command in a clean
// server context.
//
STDMETHODIMP CMOCAServerAccess::InitiateClean(BSTR ServerCommand, IMOCAResults **Results)
{
    int needToDetach;
    long srvStatus;
    char *strServerCommand;
    RETURN_STRUCT *srvRes = NULL;

    jni_GetEnv(&needToDetach);

    strServerCommand = util_GetMultiByte(ServerCommand);
    srvStatus = srvInitiateCommand(strServerCommand, &srvRes);

    // Use a MOCAResults smart pointer
    IMOCAResultsPtr spResults;
    spResults.CreateInstance(CLSID_MOCAResults);

    // Put our results pointer (as a long) into the MOCAResults
    // object.
    spResults->put_Results((long) srvRes);
    
    // We're passing this back, so let's add a reference to this object
    // (that way, it won't get deleted).
    spResults.AddRef();

    // Pass it back.
    *Results = spResults;

    // Free up our temporary multibyte string.
    free(strServerCommand);

    if (needToDetach) jni_ReleaseEnv();

    return S_OK;
}

