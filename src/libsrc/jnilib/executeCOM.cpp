static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions used by the server and server applications
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2002-2009
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

#define _WIN32_DCOM
#include <moca.h>

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <mocaerr.h>
#include <mislib.h>
#include <srvlib.h>

#include "jniprivate.h"

#include <ole2.h>
#include <oleauto.h>

#include <MOCASrvObj.h>

static BSTR AllocOleString(char* pStr)
{
    int length = strlen(pStr);
    int count = MultiByteToWideChar(CP_ACP, 0, pStr, length, NULL, 0);
    BSTR bstr = SysAllocStringLen(NULL, count);
    MultiByteToWideChar(CP_ACP, 0, pStr, length, bstr, count);
    return bstr;
}

static OLECHAR *GetWideChar(char* pStr)
{
    int count = MultiByteToWideChar(CP_ACP, 0, pStr, -1, NULL, 0);
    OLECHAR *oStr = (OLECHAR *)calloc(count, sizeof(OLECHAR));
    MultiByteToWideChar(CP_ACP, 0, pStr, -1, oStr, count);
    return oStr;
}

static char *GetMultiByte(BSTR bstrIn)
{
    int length = SysStringLen(bstrIn);
    int count = WideCharToMultiByte(CP_ACP, 0, bstrIn, length, NULL, 0, NULL, NULL);
    char *pStr = (char *)calloc(count+1, sizeof(char));
    WideCharToMultiByte(CP_ACP, 0, bstrIn, length, pStr, count, NULL, NULL);
    return pStr;
}

static HRESULT CreateObject(LPSTR pszProgID, IDispatch ** ppdisp)
{
    CLSID clsid;                  // CLSID of ActiveX object.
    HRESULT hr;
    LPUNKNOWN punk = NULL;        // IUnknown of ActiveX object.
    LPDISPATCH pdisp = NULL;      // IDispatch of ActiveX object.
    OLECHAR *wstrProgID = GetWideChar(pszProgID);

    *ppdisp = NULL;

    // Retrieve CLSID from the ProgID that the user specified.
    hr = CLSIDFromProgID(wstrProgID, &clsid);
    free(wstrProgID);

    if (FAILED(hr))
        goto error;

    // Create an instance of the ActiveX object.
    hr = CoCreateInstance(clsid, NULL, CLSCTX_SERVER, 
                          IID_IUnknown, (void **)&punk);
    if (FAILED(hr))
        goto error;

    // Ask the ActiveX object for the IDispatch interface.
    hr = punk->QueryInterface(IID_IDispatch, (void **)&pdisp);
    if (FAILED(hr))
        goto error;

    *ppdisp = pdisp;
    punk->Release();
    return NOERROR;
    
error:
    if (punk) punk->Release();
    if (pdisp) pdisp->Release();
    return hr;
}

VARIANT CallInterfaceMethod(IDispatch *pDisp, 
                            char *progid, 
                            char *method, 
                            VARIANTARG *pvarg, 
			    long argcount,
                            RETURN_STRUCT *RetStr, 
                            bool bRetStruct)
{
    DISPID dispid;
    DISPPARAMS dispparams;
    EXCEPINFO exInvoke;
    VARIANT varReturn;
    OLECHAR *tmpMethod = NULL;
    HRESULT hr;
    unsigned int badparam;
    char *errtxt;
    int ErrNeedsFree = 0;

    VariantInit(&varReturn);
	
    /*
     * We need a real BSTR to pass in and get a dispatch ID for the
     * method. 
     */
    tmpMethod = GetWideChar(method);	

    /*
     * Find our method.
     */
    hr = pDisp->GetIDsOfNames(IID_NULL, &tmpMethod, 1,
			      LOCALE_SYSTEM_DEFAULT, &dispid);
    free(tmpMethod);
    if (FAILED(hr))
    {
    	//no such method	
	varReturn.vt = VT_EMPTY;
	if (bRetStruct)
	{
	    misTrc(T_SERVER, "Unable to look up method %s", method);
	    RetStr = srvErrorResults(eSRV_INVALID_FUNCTION,
              			     "Invalid Function (^function^)",
		        	     "function", COMTYP_STRING, method, 0,
				     NULL);
	}
	return varReturn;
    }

    /* no arguments for any of the init functions */
    memset(&dispparams, 0, sizeof dispparams);
    dispparams.rgvarg = pvarg;
    dispparams.cArgs = argcount;
	
    /* Make the call. */
    if (bRetStruct)
	misTrc(T_SERVER, "Calling Invoke: %s %s", progid, method);

    hr = pDisp->Invoke(dispid, IID_NULL, LOCALE_SYSTEM_DEFAULT, DISPATCH_METHOD,
                       &dispparams, &varReturn, &exInvoke, &badparam);

    if (bRetStruct)
	misTrc(T_SERVER, "Called Invoke: %d", hr);

    /*
     * We might have some automation error.  Most likely of these is that the
     * method's parameter list and our own list may not jive.
     */
    if (FAILED(hr))
    {
	switch (hr)
	{
	    case DISP_E_BADPARAMCOUNT:
		// The number of elements provided to DISPPARAMS is different 
                // from the number of arguments accepted by the method or 
                // property. 
		errtxt = "Parameter Count Mismatch";
		break;

    	    case DISP_E_EXCEPTION:
		// The application needs to raise an exception. In this case,
		// the structure passed in pExcepInfo should be filled in. 
		errtxt = GetMultiByte(exInvoke.bstrDescription);
		ErrNeedsFree = 1;
		break;

	    case DISP_E_MEMBERNOTFOUND:
		// The requested member does not exist, or the call to Invoke 
		// tried to set the value of a read-only property. 
		errtxt = "Method not found";
		break;

	    case DISP_E_PARAMNOTFOUND:
		// One of the parameter DISPIDs does not correspond to a
		// parameter on the method. In this case, puArgErr should be 
                // set to the first argument that contains the error. 
		errtxt = "Parameter not found";
		break;

	    case DISP_E_TYPEMISMATCH:
		// One or more of the arguments could not be coerced. The index
		// within rgvarg of the first parameter with the incorrect type
		// is returned in the puArgErr parameter. 
		errtxt = "Parameter Type Mismatch";
		break;

	    case DISP_E_UNKNOWNLCID:
		// The member being invoked interprets string arguments 
                // according to the LCID, and the LCID is not recognized. If 
                // the LCID is not needed to interpret arguments, this error 
                // should not be returned. 
		errtxt = "Locale Mismatch";
		break;

   	    case DISP_E_PARAMNOTOPTIONAL:
		// A required parameter was omitted. 
		errtxt = "Required Parameter Omitted";
		break;

		// These should never happen
  	    case DISP_E_OVERFLOW:
		// One of the arguments in rgvarg could not be coerced to the
		// specified type. 
	    case DISP_E_NONAMEDARGS:
		// This implementation of IDispatch does not support named
		// arguments. 
	    case DISP_E_UNKNOWNINTERFACE:
		// The interface identifier passed in riid is not IID_NULL. 
	    case DISP_E_BADVARTYPE:
		// One of the arguments in rgvarg is not a valid variant type.  
	    default:
		errtxt = "Unexpected Error";
		break;
	}

	misTrc(T_SERVER, "Automation error from COM: %s", errtxt);
	if (bRetStruct)
	    RetStr = srvErrorResults(eSRV_AUTOMATION_ERROR,
		        	     "Automation Error (^class^ ^function^): ^errtxt^",
		                     "class", COMTYP_STRING, progid, 0,
		                     "function", COMTYP_STRING, method, 0,
		                     "errtxt", COMTYP_STRING, errtxt, 0,
		                     NULL);
		
	// Free up the error text if we allocated it.
	if (ErrNeedsFree)
	    free(errtxt);

	varReturn.vt = VT_EMPTY;
	return varReturn;
    }



    return varReturn;

}


extern "C"
RETURN_STRUCT *jni_ExecuteCOM(char *progid, char *method,
        int argCount, char argTypes[], void *args[])
{
    int ii;
    RETURN_STRUCT *ret = NULL;
    HRESULT hr;
    IDispatch *pDisp = NULL;
    VARIANT varReturn;
    VARIANTARG *pvarg = NULL;
    int initialized = 0;

    /*
     * Initialize OLE 
     */  
    HRESULT initStatus = CoInitializeEx(NULL, COINIT_MULTITHREADED);
    if (initStatus == S_OK || initStatus == S_FALSE)
    {
        initialized = 1;
    }
	
    /*
     * Initialize the return value. This will allow us to clear it
     * later.
     */
    VariantInit(&varReturn);

    /*
     * We have a ProgID, from the COMMAND_DESCRIPTOR.  We use that to create
     * an instance of the object it represents.  This will come back with an
     * interface pointer to the IDispatch interface.
     */
    hr = CreateObject(progid, &pDisp);

    if (FAILED(hr))
    {
	misTrc(T_SERVER, "Unable to create object %s", progid);
	ret = srvErrorResults(eSRV_INVALID_OBJECT,
			"Invalid Object (^class^)",
		        "class", COMTYP_STRING, progid, 0,
			NULL);
	goto cleanup;
    }

    /*
     * We need to push in arguments for the method.  Arguments are passed
     * in through the DISPPARAM structure.  The operative member is an
     * array of VARIANTs.
     */
    if (argCount)
    {
	pvarg = new VARIANTARG[argCount];
	memset(pvarg, 0, sizeof(VARIANTARG) * argCount);
	
	for (ii = 0; ii < argCount; ii++)
	{
	    int varpos = argCount - ii - 1;

  	    VariantInit(&pvarg[varpos]);

	    /*
   	     * If the argument is not available, set up the variant as if it's
	     * not passed.
	     */
	    if (!args[ii])
	    {
		V_VT(&pvarg[varpos]) = VT_ERROR;
		V_ERROR(&pvarg[varpos]) = DISP_E_PARAMNOTFOUND;
	    }	
	    else
	    {
		/*
		 * Only support a subset of argument types.  Integer, float
		 * and string should be enough.
		 */
		switch(argTypes[ii])
		{
		    case ARGTYP_FLAG:
		    case ARGTYP_INT:
			V_VT(&pvarg[varpos]) = VT_I4;
			V_I4(&pvarg[varpos]) = 
                            * (long *)args[ii];
			break;
	            case ARGTYP_FLOAT:
			V_VT(&pvarg[varpos]) = VT_R8;
			V_R8(&pvarg[varpos]) =  
                            * (double *)args[ii];
			break;
		    case ARGTYP_STR:

			V_VT(&pvarg[varpos]) = VT_BSTR;
			V_BSTR(&pvarg[varpos]) = 
                            AllocOleString((char *)args[ii]);
			break;
   		    default:
			/*
		 	 * If not a supported type, pretend it wasn't passed.
			 */
			V_VT(&pvarg[varpos]) = VT_ERROR;
			V_ERROR(&pvarg[varpos]) = DISP_E_PARAMNOTFOUND;
			break;
		}
	    }
	}
    }
	
    /* call CallInterfaceMethod */
    varReturn = CallInterfaceMethod(pDisp, 
                                    progid, 
                                    method,
				    pvarg,
				    argCount,
                                    ret,
                                    true);

    /*
     * If the returned data (in a VARIANT) is an IDispatch pointer, let's 
     * See if we can get results out of it.
     */
    if (varReturn.vt == VT_DISPATCH)
    {
	IMOCAResults *pRes;
	IDispatch *pResDisp;

	/*
 	 * We got an IDispatch pointer from the call, that probably
	 * means we got one of our "Results" objects back.
	 */
	pResDisp = varReturn.pdispVal;

	hr = pResDisp->QueryInterface(__uuidof(IMOCAResults), (void **) &pRes);

	/*
	 * Is this a Results object?
	 */
	if (!FAILED(hr))
	{
	    /*
	     * Extract the results from the object
	     */
	    hr = pRes->get_Results((long *)&ret);

	    /*
	     * CHEAT! Put a NULL into the results object so that when it
	     * gets cleaned up on object destruction, our pointer remains
	     * valid.
	     */
	    hr = pRes->put_Results(0L);
	    pRes->Release();
	 } 
	/*
	 * Don't release the Dispatch Pointer, as it's coming out of
	 * a variant, which will get cleared with VariantClear() later on.
	 */
    }
    else if (varReturn.vt == VT_I4)
    {
	/*
	 * We just got an error code
	 */
	ret = srvResults(varReturn.lVal, NULL);
    }
    else if (varReturn.vt = VT_BSTR)
    {
        char *buffer = GetMultiByte(varReturn.bstrVal);

        ret = srvResults(eOK,
                         "result", COMTYP_CHAR, strlen(buffer), buffer,
                         NULL);

        free(buffer);
    }

cleanup:

    /*
     * This call clears the variant, freeing up resource, including
     * releasing the IDispatch pointer if one was returned.
     */
    VariantClear(&varReturn);

    if (argCount && pvarg)
    {
	for (ii = 0; ii < argCount; ii++)
	    VariantClear(&pvarg[ii]);

	delete [] pvarg;
    }

    if (pDisp)
	pDisp->Release();

    if (initialized)
    {
        CoUninitialize();
    }

    return ret;
}
