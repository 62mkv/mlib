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

// MOCAServerAccess.h : Declaration of the CMOCAServerAccess

#ifndef __MOCASERVERACCESS_H_
#define __MOCASERVERACCESS_H_

#include "resource.h"       // main symbols

#include <moca.h>
#include <common.h>
#include <mocaerr.h>
#include <sqllib.h>
#include <mislib.h>
#include <oslib.h>
#include <srvlib.h>

/////////////////////////////////////////////////////////////////////////////
// CMOCAServerAccess
class ATL_NO_VTABLE CMOCAServerAccess : 
	public CComObjectRootEx<CComSingleThreadModel>,
	public CComCoClass<CMOCAServerAccess, &CLSID_MOCAServerAccess>,
	public IDispatchImpl<IMOCAServerAccess, &IID_IMOCAServerAccess, &LIBID_MOCASrvObjLibNG>
{
public:
	CMOCAServerAccess()
	{
	}

DECLARE_REGISTRY_RESOURCEID(IDR_MOCASERVERACCESS)

DECLARE_PROTECT_FINAL_CONSTRUCT()

BEGIN_COM_MAP(CMOCAServerAccess)
	COM_INTERFACE_ENTRY(IMOCAServerAccess)
	COM_INTERFACE_ENTRY(IDispatch)
END_COM_MAP()

// IMOCAServerAccess
public:
	STDMETHOD(GetVariable)(/*[in]*/BSTR VarName, /*[out, retval]*/BSTR *Value);
	STDMETHOD(TraceFlow)(/*[in]*/BSTR Message);
	STDMETHOD(LogInfo)(/*[in]*/BSTR Message);
	STDMETHOD(LogWarning)(/*[in]*/BSTR Message);
	STDMETHOD(LogError)(/*[in]*/BSTR Message);
	STDMETHOD(GetNeededElement)(/*[in]*/ BSTR Name, /*[in, optional]*/ BSTR AltName, /*[out, retval]*/ VARIANT *pVal);
	STDMETHOD(Rollback)(/*[out, retval]*/ long *Status);
	STDMETHOD(Commit)(/*[out, retval]*/ long *Status);
	STDMETHOD(Initiate)(/*[in]*/BSTR ServerCommand, /*[out, retval]*/ IMOCAResults **Results);
	STDMETHOD(SQLExecute)(/*[in]*/BSTR SqlCommand, /*[out, retval]*/ IMOCAResults **Results);
	STDMETHOD(InitiateClean)(/*[in]*/BSTR ServerCommand, /*[out, retval]*/ IMOCAResults **Results);
};

#endif //__MOCASERVERACCESS_H_
