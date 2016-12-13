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

// MOCAResults.h : Declaration of the CMOCAResults

#ifndef __MOCARESULTS_H_
#define __MOCARESULTS_H_

#include "resource.h"       // main symbols

#include <moca.h>
#include <mislib.h>
#include <common.h>
#include <mocaerr.h>
#include <sqllib.h>
#include <srvlib.h>

/////////////////////////////////////////////////////////////////////////////
// CMOCAResults
class ATL_NO_VTABLE CMOCAResults : 
	public CComObjectRootEx<CComSingleThreadModel>,
	public CComCoClass<CMOCAResults, &CLSID_MOCAResults>,
	public IDispatchImpl<IMOCAResults, &IID_IMOCAResults, &LIBID_MOCASrvObjLibNG>
{
public:
	CMOCAResults()
	{
	    Current = NULL;
	    RawResults = NULL;
	    Results = NULL;
	    ResultsList = NULL;
	    ResultsListCount = 0;
	}
	~CMOCAResults()
	{
	    if (Results)
		srvFreeMemory(SRVRET_STRUCT, Results);
	    if (ResultsList)
		free(ResultsList);
	    Current = NULL;
	    RawResults = NULL;
	    Results = NULL;
	    ResultsList = NULL;
	    ResultsListCount = 0;
	}


DECLARE_REGISTRY_RESOURCEID(IDR_MOCARESULTS)

DECLARE_PROTECT_FINAL_CONSTRUCT()

BEGIN_COM_MAP(CMOCAResults)
	COM_INTERFACE_ENTRY(IMOCAResults)
	COM_INTERFACE_ENTRY(IDispatch)
END_COM_MAP()

// IMOCAResults
public:
	STDMETHOD(get_DefinedLength)(VARIANT Index, /*[out, retval]*/ long *pVal);
	STDMETHOD(AddColumn2)(/*[in]*/ BSTR ColumnName, /*[in]*/ MOCADataTypes DataType, /*[in]*/ long ColumnLength);
	STDMETHOD(AddErrorArg)(/*[in]*/BSTR Name, /*[in]*/VARIANT Value, /*[in, optional]*/BOOL DoLookup);
	STDMETHOD(get_ColName)(/*[in]*/long ColNum, /*[out, retval]*/ BSTR *pVal);
	STDMETHOD(get_ColNum)(/*[in]*/BSTR ColName, /*[out, retval]*/ long *pVal);
	STDMETHOD(get_Columns)(/*[out, retval]*/ long *pVal);
	STDMETHOD(get_Rows)(/*[out, retval]*/ long *pVal);
	STDMETHOD(get_DataType)(/*[in]*/VARIANT Index, /*[out, retval]*/ MOCADataTypes *pVal);
	STDMETHOD(AddRow)();
	STDMETHOD(Close)();
	STDMETHOD(Open)(/*[in, optional, defaultvalue(0)]*/long Status = eOK, /*[in, optional, defaultvalue(NULL)]*/ BSTR ErrorText = NULL);
	STDMETHOD(AddColumn)(/*[in]*/BSTR ColumnName, /*[in]*/MOCADataTypes DataType);
	STDMETHOD(get_Value)(/*[in]*/ VARIANT Index, /*[out, retval]*/ VARIANT *pVal);
	STDMETHOD(put_Value)(/*[in]*/ VARIANT Index, /*[in]*/ VARIANT newVal);
	STDMETHOD(MoveNext)();
	STDMETHOD(MoveFirst)();
	STDMETHOD(get_ResultsEOF)(/*[out, retval]*/ BOOL *pVal);
	STDMETHOD(get_Status)(/*[out, retval]*/ long *pVal);
	STDMETHOD(put_Status)(/*[in]*/ long newVal);
	STDMETHOD(get_Results)(/*[out, retval]*/ long *pVal);
	STDMETHOD(put_Results)(/*[in]*/ long newVal);
private:
	mocaDataRow * Current;
	mocaDataRes * RawResults;
	RETURN_STRUCT * Results;
	SRV_RESULTS_LIST *ResultsList;
	int ResultsListCount;
};

#endif //__MOCARESULTS_H_
