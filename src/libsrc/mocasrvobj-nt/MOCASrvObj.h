

/* this ALWAYS GENERATED file contains the definitions for the interfaces */


 /* File created by MIDL compiler version 7.00.0500 */
/* at Wed Aug 24 00:06:53 2016
 */
/* Compiler settings for MOCASrvObj.idl:
    Oicf, W1, Zp8, env=Win32 (32b run)
    protocol : dce , ms_ext, c_ext, robust
    error checks: allocation ref bounds_check enum stub_data 
    VC __declspec() decoration level: 
         __declspec(uuid()), __declspec(selectany), __declspec(novtable)
         DECLSPEC_UUID(), MIDL_INTERFACE()
*/
//@@MIDL_FILE_HEADING(  )

#pragma warning( disable: 4049 )  /* more than 64k source lines */


/* verify that the <rpcndr.h> version is high enough to compile this file*/
#ifndef __REQUIRED_RPCNDR_H_VERSION__
#define __REQUIRED_RPCNDR_H_VERSION__ 475
#endif

#include "rpc.h"
#include "rpcndr.h"

#ifndef __RPCNDR_H_VERSION__
#error this stub requires an updated version of <rpcndr.h>
#endif // __RPCNDR_H_VERSION__

#ifndef COM_NO_WINDOWS_H
#include "windows.h"
#include "ole2.h"
#endif /*COM_NO_WINDOWS_H*/

#ifndef __MOCASrvObj_h__
#define __MOCASrvObj_h__

#if defined(_MSC_VER) && (_MSC_VER >= 1020)
#pragma once
#endif

/* Forward Declarations */ 

#ifndef __IMOCAResults_FWD_DEFINED__
#define __IMOCAResults_FWD_DEFINED__
typedef interface IMOCAResults IMOCAResults;
#endif 	/* __IMOCAResults_FWD_DEFINED__ */


#ifndef __IMOCAServerAccess_FWD_DEFINED__
#define __IMOCAServerAccess_FWD_DEFINED__
typedef interface IMOCAServerAccess IMOCAServerAccess;
#endif 	/* __IMOCAServerAccess_FWD_DEFINED__ */


#ifndef __MOCAResults_FWD_DEFINED__
#define __MOCAResults_FWD_DEFINED__

#ifdef __cplusplus
typedef class MOCAResults MOCAResults;
#else
typedef struct MOCAResults MOCAResults;
#endif /* __cplusplus */

#endif 	/* __MOCAResults_FWD_DEFINED__ */


#ifndef __MOCAServerAccess_FWD_DEFINED__
#define __MOCAServerAccess_FWD_DEFINED__

#ifdef __cplusplus
typedef class MOCAServerAccess MOCAServerAccess;
#else
typedef struct MOCAServerAccess MOCAServerAccess;
#endif /* __cplusplus */

#endif 	/* __MOCAServerAccess_FWD_DEFINED__ */


/* header files for imported files */
#include "oaidl.h"
#include "ocidl.h"

#ifdef __cplusplus
extern "C"{
#endif 


/* interface __MIDL_itf_MOCASrvObj_0000_0000 */
/* [local] */ 

typedef 
enum MOCADataTypes
    {	MOCAUnknown	= 0,
	MOCAInt	= ( MOCAUnknown + 1 ) ,
	MOCAFloat	= ( MOCAInt + 1 ) ,
	MOCAString	= ( MOCAFloat + 1 ) ,
	MOCADateTime	= ( MOCAString + 1 ) ,
	MOCABoolean	= ( MOCADateTime + 1 ) ,
	MOCAResults	= ( MOCABoolean + 1 ) ,
	MOCABinary	= ( MOCAResults + 1 ) 
    } 	MOCADataTypes;



extern RPC_IF_HANDLE __MIDL_itf_MOCASrvObj_0000_0000_v0_0_c_ifspec;
extern RPC_IF_HANDLE __MIDL_itf_MOCASrvObj_0000_0000_v0_0_s_ifspec;

#ifndef __IMOCAResults_INTERFACE_DEFINED__
#define __IMOCAResults_INTERFACE_DEFINED__

/* interface IMOCAResults */
/* [unique][helpstring][dual][uuid][object] */ 


EXTERN_C const IID IID_IMOCAResults;

#if defined(__cplusplus) && !defined(CINTERFACE)
    
    MIDL_INTERFACE("270ac420-3c89-40da-9987-7cc1d48e43f1")
    IMOCAResults : public IDispatch
    {
    public:
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_Results( 
            /* [retval][out] */ long *pVal) = 0;
        
        virtual /* [helpstring][id][propput] */ HRESULT STDMETHODCALLTYPE put_Results( 
            /* [in] */ long newVal) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_Status( 
            /* [retval][out] */ long *pVal) = 0;
        
        virtual /* [helpstring][id][propput] */ HRESULT STDMETHODCALLTYPE put_Status( 
            /* [in] */ long newVal) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_ResultsEOF( 
            /* [retval][out] */ BOOL *pVal) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE MoveFirst( void) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE MoveNext( void) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_Value( 
            /* [in] */ VARIANT Index,
            /* [retval][out] */ VARIANT *pVal) = 0;
        
        virtual /* [helpstring][id][propput] */ HRESULT STDMETHODCALLTYPE put_Value( 
            /* [in] */ VARIANT Index,
            /* [in] */ VARIANT newVal) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE AddColumn( 
            /* [in] */ BSTR ColumnName,
            /* [in] */ MOCADataTypes DataType) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE Open( 
            /* [defaultvalue][optional][in] */ long Status = 0,
            /* [defaultvalue][optional][in] */ BSTR DefaultErrorText = L"") = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE Close( void) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE AddRow( void) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_DataType( 
            /* [in] */ VARIANT Index,
            /* [retval][out] */ MOCADataTypes *pVal) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_Rows( 
            /* [retval][out] */ long *pVal) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_Columns( 
            /* [retval][out] */ long *pVal) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_ColNum( 
            /* [in] */ BSTR ColName,
            /* [retval][out] */ long *pVal) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_ColName( 
            /* [in] */ long ColNum,
            /* [retval][out] */ BSTR *pVal) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE AddErrorArg( 
            /* [in] */ BSTR Name,
            /* [in] */ VARIANT Value,
            /* [defaultvalue][optional][in] */ BOOL DoLookup = FALSE) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE AddColumn2( 
            /* [in] */ BSTR ColumnName,
            /* [in] */ MOCADataTypes DataType,
            /* [in] */ long ColumnLength) = 0;
        
        virtual /* [helpstring][id][propget] */ HRESULT STDMETHODCALLTYPE get_DefinedLength( 
            VARIANT Index,
            /* [retval][out] */ long *pVal) = 0;
        
    };
    
#else 	/* C style interface */

    typedef struct IMOCAResultsVtbl
    {
        BEGIN_INTERFACE
        
        HRESULT ( STDMETHODCALLTYPE *QueryInterface )( 
            IMOCAResults * This,
            /* [in] */ REFIID riid,
            /* [iid_is][out] */ 
            __RPC__deref_out  void **ppvObject);
        
        ULONG ( STDMETHODCALLTYPE *AddRef )( 
            IMOCAResults * This);
        
        ULONG ( STDMETHODCALLTYPE *Release )( 
            IMOCAResults * This);
        
        HRESULT ( STDMETHODCALLTYPE *GetTypeInfoCount )( 
            IMOCAResults * This,
            /* [out] */ UINT *pctinfo);
        
        HRESULT ( STDMETHODCALLTYPE *GetTypeInfo )( 
            IMOCAResults * This,
            /* [in] */ UINT iTInfo,
            /* [in] */ LCID lcid,
            /* [out] */ ITypeInfo **ppTInfo);
        
        HRESULT ( STDMETHODCALLTYPE *GetIDsOfNames )( 
            IMOCAResults * This,
            /* [in] */ REFIID riid,
            /* [size_is][in] */ LPOLESTR *rgszNames,
            /* [range][in] */ UINT cNames,
            /* [in] */ LCID lcid,
            /* [size_is][out] */ DISPID *rgDispId);
        
        /* [local] */ HRESULT ( STDMETHODCALLTYPE *Invoke )( 
            IMOCAResults * This,
            /* [in] */ DISPID dispIdMember,
            /* [in] */ REFIID riid,
            /* [in] */ LCID lcid,
            /* [in] */ WORD wFlags,
            /* [out][in] */ DISPPARAMS *pDispParams,
            /* [out] */ VARIANT *pVarResult,
            /* [out] */ EXCEPINFO *pExcepInfo,
            /* [out] */ UINT *puArgErr);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_Results )( 
            IMOCAResults * This,
            /* [retval][out] */ long *pVal);
        
        /* [helpstring][id][propput] */ HRESULT ( STDMETHODCALLTYPE *put_Results )( 
            IMOCAResults * This,
            /* [in] */ long newVal);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_Status )( 
            IMOCAResults * This,
            /* [retval][out] */ long *pVal);
        
        /* [helpstring][id][propput] */ HRESULT ( STDMETHODCALLTYPE *put_Status )( 
            IMOCAResults * This,
            /* [in] */ long newVal);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_ResultsEOF )( 
            IMOCAResults * This,
            /* [retval][out] */ BOOL *pVal);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *MoveFirst )( 
            IMOCAResults * This);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *MoveNext )( 
            IMOCAResults * This);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_Value )( 
            IMOCAResults * This,
            /* [in] */ VARIANT Index,
            /* [retval][out] */ VARIANT *pVal);
        
        /* [helpstring][id][propput] */ HRESULT ( STDMETHODCALLTYPE *put_Value )( 
            IMOCAResults * This,
            /* [in] */ VARIANT Index,
            /* [in] */ VARIANT newVal);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *AddColumn )( 
            IMOCAResults * This,
            /* [in] */ BSTR ColumnName,
            /* [in] */ MOCADataTypes DataType);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *Open )( 
            IMOCAResults * This,
            /* [defaultvalue][optional][in] */ long Status,
            /* [defaultvalue][optional][in] */ BSTR DefaultErrorText);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *Close )( 
            IMOCAResults * This);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *AddRow )( 
            IMOCAResults * This);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_DataType )( 
            IMOCAResults * This,
            /* [in] */ VARIANT Index,
            /* [retval][out] */ MOCADataTypes *pVal);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_Rows )( 
            IMOCAResults * This,
            /* [retval][out] */ long *pVal);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_Columns )( 
            IMOCAResults * This,
            /* [retval][out] */ long *pVal);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_ColNum )( 
            IMOCAResults * This,
            /* [in] */ BSTR ColName,
            /* [retval][out] */ long *pVal);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_ColName )( 
            IMOCAResults * This,
            /* [in] */ long ColNum,
            /* [retval][out] */ BSTR *pVal);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *AddErrorArg )( 
            IMOCAResults * This,
            /* [in] */ BSTR Name,
            /* [in] */ VARIANT Value,
            /* [defaultvalue][optional][in] */ BOOL DoLookup);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *AddColumn2 )( 
            IMOCAResults * This,
            /* [in] */ BSTR ColumnName,
            /* [in] */ MOCADataTypes DataType,
            /* [in] */ long ColumnLength);
        
        /* [helpstring][id][propget] */ HRESULT ( STDMETHODCALLTYPE *get_DefinedLength )( 
            IMOCAResults * This,
            VARIANT Index,
            /* [retval][out] */ long *pVal);
        
        END_INTERFACE
    } IMOCAResultsVtbl;

    interface IMOCAResults
    {
        CONST_VTBL struct IMOCAResultsVtbl *lpVtbl;
    };

    

#ifdef COBJMACROS


#define IMOCAResults_QueryInterface(This,riid,ppvObject)	\
    ( (This)->lpVtbl -> QueryInterface(This,riid,ppvObject) ) 

#define IMOCAResults_AddRef(This)	\
    ( (This)->lpVtbl -> AddRef(This) ) 

#define IMOCAResults_Release(This)	\
    ( (This)->lpVtbl -> Release(This) ) 


#define IMOCAResults_GetTypeInfoCount(This,pctinfo)	\
    ( (This)->lpVtbl -> GetTypeInfoCount(This,pctinfo) ) 

#define IMOCAResults_GetTypeInfo(This,iTInfo,lcid,ppTInfo)	\
    ( (This)->lpVtbl -> GetTypeInfo(This,iTInfo,lcid,ppTInfo) ) 

#define IMOCAResults_GetIDsOfNames(This,riid,rgszNames,cNames,lcid,rgDispId)	\
    ( (This)->lpVtbl -> GetIDsOfNames(This,riid,rgszNames,cNames,lcid,rgDispId) ) 

#define IMOCAResults_Invoke(This,dispIdMember,riid,lcid,wFlags,pDispParams,pVarResult,pExcepInfo,puArgErr)	\
    ( (This)->lpVtbl -> Invoke(This,dispIdMember,riid,lcid,wFlags,pDispParams,pVarResult,pExcepInfo,puArgErr) ) 


#define IMOCAResults_get_Results(This,pVal)	\
    ( (This)->lpVtbl -> get_Results(This,pVal) ) 

#define IMOCAResults_put_Results(This,newVal)	\
    ( (This)->lpVtbl -> put_Results(This,newVal) ) 

#define IMOCAResults_get_Status(This,pVal)	\
    ( (This)->lpVtbl -> get_Status(This,pVal) ) 

#define IMOCAResults_put_Status(This,newVal)	\
    ( (This)->lpVtbl -> put_Status(This,newVal) ) 

#define IMOCAResults_get_ResultsEOF(This,pVal)	\
    ( (This)->lpVtbl -> get_ResultsEOF(This,pVal) ) 

#define IMOCAResults_MoveFirst(This)	\
    ( (This)->lpVtbl -> MoveFirst(This) ) 

#define IMOCAResults_MoveNext(This)	\
    ( (This)->lpVtbl -> MoveNext(This) ) 

#define IMOCAResults_get_Value(This,Index,pVal)	\
    ( (This)->lpVtbl -> get_Value(This,Index,pVal) ) 

#define IMOCAResults_put_Value(This,Index,newVal)	\
    ( (This)->lpVtbl -> put_Value(This,Index,newVal) ) 

#define IMOCAResults_AddColumn(This,ColumnName,DataType)	\
    ( (This)->lpVtbl -> AddColumn(This,ColumnName,DataType) ) 

#define IMOCAResults_Open(This,Status,DefaultErrorText)	\
    ( (This)->lpVtbl -> Open(This,Status,DefaultErrorText) ) 

#define IMOCAResults_Close(This)	\
    ( (This)->lpVtbl -> Close(This) ) 

#define IMOCAResults_AddRow(This)	\
    ( (This)->lpVtbl -> AddRow(This) ) 

#define IMOCAResults_get_DataType(This,Index,pVal)	\
    ( (This)->lpVtbl -> get_DataType(This,Index,pVal) ) 

#define IMOCAResults_get_Rows(This,pVal)	\
    ( (This)->lpVtbl -> get_Rows(This,pVal) ) 

#define IMOCAResults_get_Columns(This,pVal)	\
    ( (This)->lpVtbl -> get_Columns(This,pVal) ) 

#define IMOCAResults_get_ColNum(This,ColName,pVal)	\
    ( (This)->lpVtbl -> get_ColNum(This,ColName,pVal) ) 

#define IMOCAResults_get_ColName(This,ColNum,pVal)	\
    ( (This)->lpVtbl -> get_ColName(This,ColNum,pVal) ) 

#define IMOCAResults_AddErrorArg(This,Name,Value,DoLookup)	\
    ( (This)->lpVtbl -> AddErrorArg(This,Name,Value,DoLookup) ) 

#define IMOCAResults_AddColumn2(This,ColumnName,DataType,ColumnLength)	\
    ( (This)->lpVtbl -> AddColumn2(This,ColumnName,DataType,ColumnLength) ) 

#define IMOCAResults_get_DefinedLength(This,Index,pVal)	\
    ( (This)->lpVtbl -> get_DefinedLength(This,Index,pVal) ) 

#endif /* COBJMACROS */


#endif 	/* C style interface */




#endif 	/* __IMOCAResults_INTERFACE_DEFINED__ */


#ifndef __IMOCAServerAccess_INTERFACE_DEFINED__
#define __IMOCAServerAccess_INTERFACE_DEFINED__

/* interface IMOCAServerAccess */
/* [unique][helpstring][dual][uuid][object] */ 


EXTERN_C const IID IID_IMOCAServerAccess;

#if defined(__cplusplus) && !defined(CINTERFACE)
    
    MIDL_INTERFACE("5086aa22-caff-4569-8620-38423555f1dc")
    IMOCAServerAccess : public IDispatch
    {
    public:
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE SQLExecute( 
            /* [in] */ BSTR SqlCommand,
            /* [retval][out] */ IMOCAResults **Results) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE Initiate( 
            /* [in] */ BSTR ServerCommand,
            /* [retval][out] */ IMOCAResults **Results) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE Commit( 
            /* [retval][out] */ long *Status) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE Rollback( 
            /* [retval][out] */ long *Status) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE GetNeededElement( 
            /* [in] */ BSTR Name,
            /* [optional][in] */ BSTR AltName,
            /* [retval][out] */ VARIANT *pVal) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE LogError( 
            /* [in] */ BSTR Message) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE LogWarning( 
            /* [in] */ BSTR Message) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE LogInfo( 
            /* [in] */ BSTR Message) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE TraceFlow( 
            /* [in] */ BSTR Message) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE GetVariable( 
            /* [in] */ BSTR VarName,
            /* [retval][out] */ BSTR *Value) = 0;
        
        virtual /* [helpstring][id] */ HRESULT STDMETHODCALLTYPE InitiateClean( 
            /* [in] */ BSTR ServerCommand,
            /* [retval][out] */ IMOCAResults **Results) = 0;
        
    };
    
#else 	/* C style interface */

    typedef struct IMOCAServerAccessVtbl
    {
        BEGIN_INTERFACE
        
        HRESULT ( STDMETHODCALLTYPE *QueryInterface )( 
            IMOCAServerAccess * This,
            /* [in] */ REFIID riid,
            /* [iid_is][out] */ 
            __RPC__deref_out  void **ppvObject);
        
        ULONG ( STDMETHODCALLTYPE *AddRef )( 
            IMOCAServerAccess * This);
        
        ULONG ( STDMETHODCALLTYPE *Release )( 
            IMOCAServerAccess * This);
        
        HRESULT ( STDMETHODCALLTYPE *GetTypeInfoCount )( 
            IMOCAServerAccess * This,
            /* [out] */ UINT *pctinfo);
        
        HRESULT ( STDMETHODCALLTYPE *GetTypeInfo )( 
            IMOCAServerAccess * This,
            /* [in] */ UINT iTInfo,
            /* [in] */ LCID lcid,
            /* [out] */ ITypeInfo **ppTInfo);
        
        HRESULT ( STDMETHODCALLTYPE *GetIDsOfNames )( 
            IMOCAServerAccess * This,
            /* [in] */ REFIID riid,
            /* [size_is][in] */ LPOLESTR *rgszNames,
            /* [range][in] */ UINT cNames,
            /* [in] */ LCID lcid,
            /* [size_is][out] */ DISPID *rgDispId);
        
        /* [local] */ HRESULT ( STDMETHODCALLTYPE *Invoke )( 
            IMOCAServerAccess * This,
            /* [in] */ DISPID dispIdMember,
            /* [in] */ REFIID riid,
            /* [in] */ LCID lcid,
            /* [in] */ WORD wFlags,
            /* [out][in] */ DISPPARAMS *pDispParams,
            /* [out] */ VARIANT *pVarResult,
            /* [out] */ EXCEPINFO *pExcepInfo,
            /* [out] */ UINT *puArgErr);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *SQLExecute )( 
            IMOCAServerAccess * This,
            /* [in] */ BSTR SqlCommand,
            /* [retval][out] */ IMOCAResults **Results);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *Initiate )( 
            IMOCAServerAccess * This,
            /* [in] */ BSTR ServerCommand,
            /* [retval][out] */ IMOCAResults **Results);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *Commit )( 
            IMOCAServerAccess * This,
            /* [retval][out] */ long *Status);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *Rollback )( 
            IMOCAServerAccess * This,
            /* [retval][out] */ long *Status);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *GetNeededElement )( 
            IMOCAServerAccess * This,
            /* [in] */ BSTR Name,
            /* [optional][in] */ BSTR AltName,
            /* [retval][out] */ VARIANT *pVal);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *LogError )( 
            IMOCAServerAccess * This,
            /* [in] */ BSTR Message);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *LogWarning )( 
            IMOCAServerAccess * This,
            /* [in] */ BSTR Message);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *LogInfo )( 
            IMOCAServerAccess * This,
            /* [in] */ BSTR Message);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *TraceFlow )( 
            IMOCAServerAccess * This,
            /* [in] */ BSTR Message);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *GetVariable )( 
            IMOCAServerAccess * This,
            /* [in] */ BSTR VarName,
            /* [retval][out] */ BSTR *Value);
        
        /* [helpstring][id] */ HRESULT ( STDMETHODCALLTYPE *InitiateClean )( 
            IMOCAServerAccess * This,
            /* [in] */ BSTR ServerCommand,
            /* [retval][out] */ IMOCAResults **Results);
        
        END_INTERFACE
    } IMOCAServerAccessVtbl;

    interface IMOCAServerAccess
    {
        CONST_VTBL struct IMOCAServerAccessVtbl *lpVtbl;
    };

    

#ifdef COBJMACROS


#define IMOCAServerAccess_QueryInterface(This,riid,ppvObject)	\
    ( (This)->lpVtbl -> QueryInterface(This,riid,ppvObject) ) 

#define IMOCAServerAccess_AddRef(This)	\
    ( (This)->lpVtbl -> AddRef(This) ) 

#define IMOCAServerAccess_Release(This)	\
    ( (This)->lpVtbl -> Release(This) ) 


#define IMOCAServerAccess_GetTypeInfoCount(This,pctinfo)	\
    ( (This)->lpVtbl -> GetTypeInfoCount(This,pctinfo) ) 

#define IMOCAServerAccess_GetTypeInfo(This,iTInfo,lcid,ppTInfo)	\
    ( (This)->lpVtbl -> GetTypeInfo(This,iTInfo,lcid,ppTInfo) ) 

#define IMOCAServerAccess_GetIDsOfNames(This,riid,rgszNames,cNames,lcid,rgDispId)	\
    ( (This)->lpVtbl -> GetIDsOfNames(This,riid,rgszNames,cNames,lcid,rgDispId) ) 

#define IMOCAServerAccess_Invoke(This,dispIdMember,riid,lcid,wFlags,pDispParams,pVarResult,pExcepInfo,puArgErr)	\
    ( (This)->lpVtbl -> Invoke(This,dispIdMember,riid,lcid,wFlags,pDispParams,pVarResult,pExcepInfo,puArgErr) ) 


#define IMOCAServerAccess_SQLExecute(This,SqlCommand,Results)	\
    ( (This)->lpVtbl -> SQLExecute(This,SqlCommand,Results) ) 

#define IMOCAServerAccess_Initiate(This,ServerCommand,Results)	\
    ( (This)->lpVtbl -> Initiate(This,ServerCommand,Results) ) 

#define IMOCAServerAccess_Commit(This,Status)	\
    ( (This)->lpVtbl -> Commit(This,Status) ) 

#define IMOCAServerAccess_Rollback(This,Status)	\
    ( (This)->lpVtbl -> Rollback(This,Status) ) 

#define IMOCAServerAccess_GetNeededElement(This,Name,AltName,pVal)	\
    ( (This)->lpVtbl -> GetNeededElement(This,Name,AltName,pVal) ) 

#define IMOCAServerAccess_LogError(This,Message)	\
    ( (This)->lpVtbl -> LogError(This,Message) ) 

#define IMOCAServerAccess_LogWarning(This,Message)	\
    ( (This)->lpVtbl -> LogWarning(This,Message) ) 

#define IMOCAServerAccess_LogInfo(This,Message)	\
    ( (This)->lpVtbl -> LogInfo(This,Message) ) 

#define IMOCAServerAccess_TraceFlow(This,Message)	\
    ( (This)->lpVtbl -> TraceFlow(This,Message) ) 

#define IMOCAServerAccess_GetVariable(This,VarName,Value)	\
    ( (This)->lpVtbl -> GetVariable(This,VarName,Value) ) 

#define IMOCAServerAccess_InitiateClean(This,ServerCommand,Results)	\
    ( (This)->lpVtbl -> InitiateClean(This,ServerCommand,Results) ) 

#endif /* COBJMACROS */


#endif 	/* C style interface */




#endif 	/* __IMOCAServerAccess_INTERFACE_DEFINED__ */



#ifndef __MOCASrvObjLibNG_LIBRARY_DEFINED__
#define __MOCASrvObjLibNG_LIBRARY_DEFINED__

/* library MOCASrvObjLibNG */
/* [helpstring][version][uuid] */ 

#ifndef MOCAERR_H
#define MOCAERR_H
typedef 
enum MOCAErrors
    {	eFLAG_ERROR	= -1,
	eLENGTH_ERROR	= -1,
	eOK	= 0,
	eFILE_OPENING_ERROR	= 1,
	eERROR	= 2,
	eINVALID_ARGS	= 3,
	eNO_DEFAULT_PRINTER	= 4,
	eNO_MEMORY	= 5,
	eNO_MORE_FILENAMES	= 7,
	eINVALID_MSGTYPE	= 8,
	eNO_VALIDATE	= 9,
	eINVALID_OPERATION	= 10,
	eSTARTUP_FAILURE	= 11,
	ePERMISSION_DENIED	= 12,
	ePARSE_ERROR	= 13,
	eNOT_IMPLEMENTED	= 14,
	eUNKNOWN_CONS_COMMAND	= 15,
	eCONSOLE_EXCEPTION	= 16,
	ePRINT_FILE_ERROR	= 17,
	eSQL_BASE	= 100,
	eSQL_TOO_LONG	= 101,
	eSQL_ABORTED	= 102,
	eMCC_BASE	= 200,
	eMCC_FAILED_TO_RELEASE	= 201,
	eMCC_FAILED_TO_CONNECT	= 202,
	eMCC_SEND_ERROR	= 203,
	eMCC_RECV_ERROR	= 204,
	eMCC_PROTO_NOT_SUPPORTED	= 205,
	eMCC_HTTP_ERROR	= 206,
	eMCC_PROTOCOL_ERROR	= 207,
	eMCC_CRYPT_DISCOVERY	= 208,
	eMCC_CONSTRUCT_CLIENT	= 213,
	eMCC_CLOSE_CLIENT	= 214,
	eMCC_LOGIN	= 215,
	eMCC_LOGOUT	= 216,
	eJNI_FIND_CLASS	= 250,
	eJNI_FIND_METHOD	= 251,
	eJNI_FIND_FIELD	= 252,
	eJNI_SERVER_ADAPTER	= 253,
	eJNI_EXCEPTION	= 254,
	eJNI_UNEXPECTED_ERROR	= 255,
	eJNI_UNEXPECTED_EXCEPTION	= 256,
	eJNI_INVALID_RESULTS_OBJECT	= 257,
	eJNI_INVALID_JNIENV	= 258,
	eOS_BASE	= 300,
	eOS_TIMEOUT	= 301,
	eOS_EGAIN	= 302,
	eOS_AUTH_FAILED	= 303,
	eOS_AUTH_NOT_SUPPORTED	= 304,
	eOS_MBX_READ_FILE	= 310,
	eOS_MBX_WRITE_FILE	= 311,
	eOS_MBX_CREATE_FILE	= 312,
	eOS_MBX_CREATE_MAILSLOT	= 313,
	eOS_MBX_MAP_FILE	= 314,
	eOS_MBX_NOT_FOUND	= 315,
	eOS_MBX_EXISTS	= 316,
	eOS_MBX_NOMEM	= 317,
	eOS_MBX_READ	= 318,
	eOS_MBX_MSGCTL	= 319,
	eOS_MBX_MSGGET	= 320,
	eOS_MBX_MSGSND	= 321,
	eOS_MBX_PROTOCOL	= 322,
	eOS_MBX_QUEUE_ID	= 323,
	eMIS_BASE	= 350,
	eMIS_HTTP_ERROR	= 351,
	eMIS_HTTP_FAILED_TO_CONNECT	= 352,
	eMIS_HTTP_NOT_HTTP_RESPONSE	= 353,
	eMIS_SOCKET_DISCONNECTED	= 354,
	eEVT_BASE	= 400,
	eEVT_INVALID_PARAMS	= 401,
	eEVT_MBX_FAILURE	= 402,
	eEVT_SYSERR	= 403,
	eEVT_ABORT	= 404,
	eSRV_BASE	= 500,
	eSRV_INVALID_COMMAND	= 501,
	eSRV_UNEXPECTED_ERROR	= 502,
	eSRV_PROTOCOL_ERROR	= 503,
	eSRV_TYPE_MISMATCH	= 504,
	eSRV_PARSE_ERROR	= 505,
	eSRV_LICENSE_INVALID	= 506,
	eSRV_INSUFF_ARGUMENTS	= 507,
	eSRV_LICENSE_EXPIRED	= 508,
	eSRV_LICENSE_NOTFOUND	= 509,
	eSRV_NO_ROWS_AFFECTED	= 510,
	eSRV_DB_ERROR	= 511,
	eSRV_DUPLICATE	= 512,
	eSRV_LICENSE_PRODUCT_INVALID	= 513,
	eSRV_MISSING_WHERE	= 514,
	eSRV_NULL_RETURNED	= 515,
	eSRV_INVALID_FUNCTION	= 516,
	eSRV_REMOTE_SYSTEM_REQUIRED	= 517,
	eSRV_REMOTE_CONNECT_FAILURE	= 518,
	eSRV_INVALID_OBJECT	= 519,
	eSRV_AUTOMATION_ERROR	= 520,
	eSRV_DIVIDE_BY_ZERO	= 521,
	eSRV_DISCONNECTED	= 522,
	eSRV_AUTHENTICATE	= 523,
	eSRV_INVALID_VERSION	= 524,
	eSRV_SYSTEM_LIST_INVALID	= 525,
	eSRV_UNHANDLED_TYPE	= 526,
	eSRV_REMOTE_PREPARE	= 527,
	eSRV_REMOTE_COMMIT	= 528,
	eSRV_UNKNOWN_ADAPTER_CODE	= 529,
	eSRV_INVALID_METHOD	= 530,
	eSRV_SCRIPT_ERROR	= 531,
	eSRV_ACCUM_HIDDEN	= 532,
	eSRV_ACCUM_MIXED_HIDDEN	= 533,
	eSRV_LOGIN_FAILED	= 534,
	eSRV_JOB_FAILED	= 535,
	eSRV_NATIVE_TIMEOUT	= 536,
	eSRV_NATIVE_COMMUNICATION	= 537,
	eSRV_INVALID_MANAGED_METHOD	= 538,
	eSRV_INTERRUPTED	= 539,
	eSRV_ROW_LIMIT_EXCEEDED	= 540,
	eSRV_REMOTE_AUTHENTICATION	= 541,
	eSRV_SESSION_CLOSED	= 542,
	eSRV_REMOTE_SESSION_CLOSED	= 543,
	eSRV_COMMAND_AUTHORIZATION	= 544,
	eMXML_BASE	= 600,
	eMXML_INITIALIZE	= 601,
	eMXML_CONSTRUCT	= 602,
	eMXML_CONFIGURE	= 603,
	eMXML_PARSE_ERROR	= 604,
	eMXML_STRING	= 605,
	eMXML_RAW_STRING	= 606,
	eMXML_WRITE_FILE	= 607,
	eMXML_RAW_WRITE_FILE	= 608,
	eMXML_ADD_ELEMENT	= 610,
	eMXML_ADD_TEXT_NODE	= 611,
	eMXML_ADD_COMMENT	= 612,
	eMXML_ADD_PI	= 613,
	eMXML_ADD_ATTRIBUTE	= 614,
	eMXML_APPLY_XSLT_FILE	= 615,
	eMXML_APPLY_XSLT_STRING	= 616,
	eMXML_INSERT_BEFORE	= 620,
	eMXML_APPEND_CHILD	= 621,
	eMXML_REMOVE_CHILD	= 622,
	eMXML_REPLACE_CHILD	= 623,
	eMXML_CLONE_NODE	= 624,
	eMXML_HAS_CHILD_NODES	= 625,
	eMXML_CREATE_ATTR	= 630,
	eMXML_CREATE_COMMENT	= 631,
	eMXML_CREATE_DOCUMENT	= 632,
	eMXML_CREATE_DOCFRAG	= 633,
	eMXML_CREATE_ELEMENT	= 634,
	eMXML_CREATE_TEXT_NODE	= 635,
	eMXML_CREATE_CDATA_SECTION	= 636,
	eMXML_CREATE_PI	= 637,
	eMXML_GET_DOCUMENT	= 640,
	eMXML_GET_DOCUMENT_ELEMENT	= 641,
	eMXML_GET_ELEMENTS	= 642,
	eMXML_GET_TAG_NAME	= 643,
	eMXML_GET_NODE_NAME	= 644,
	eMXML_GET_NODE_VALUE	= 645,
	eMXML_GET_NODE_TYPE	= 646,
	eMXML_GET_PARENT_NODE	= 647,
	eMXML_GET_CHILD_NODES	= 648,
	eMXML_GET_FIRST_CHILD	= 649,
	eMXML_GET_LAST_CHILD	= 650,
	eMXML_GET_PREVIOUS_SIBLING	= 651,
	eMXML_GET_NEXT_SIBLING	= 652,
	eMXML_GET_ATTRS	= 653,
	eMXML_GET_ATTR	= 654,
	eMXML_GET_ATTR_NAME	= 655,
	eMXML_GET_ATTR_VALUE	= 656,
	eMXML_GET_ATTR_NODE	= 657,
	eMXML_GET_ATTR_SPECIFIED	= 658,
	eMXML_GET_LIST_LENGTH	= 659,
	eMXML_GET_ITEM	= 660,
	eMXML_GET_NAMED_ITEM	= 661,
	eMXML_GET_CDATA	= 662,
	eMXML_GET_CDATA_LENGTH	= 663,
	eMXML_GET_PI_TARGET	= 664,
	eMXML_GET_PI_DATA	= 665,
	eMXML_SET_ATTR	= 670,
	eMXML_SET_ATTR_VALUE	= 671,
	eMXML_SET_ATTR_NODE	= 672,
	eMXML_SET_NODE_VALUE	= 673,
	eMXML_SET_NAMED_ITEM	= 674,
	eMXML_SET_CDATA	= 675,
	eMXML_SET_PI_DATA	= 676,
	eMXML_REMOVE_ATTR	= 680,
	eMXML_REMOVE_ATTR_NODE	= 681,
	eMXML_REMOVE_NAMED_ITEM	= 682,
	eCMD_BASE	= 700,
	eCMD_REPOSITORY_NOT_LOADED	= 701,
	eCMD_TOO_MANY_MATCHES	= 702,
	eCMD_MISSING_REPOSITORY	= 710,
	eCMD_MISSING_LEVEL	= 711,
	eCMD_MISSING_COMMAND	= 712,
	eCMD_MISSING_TRIGGER	= 713,
	eCMD_MISSING_ARGUMENT	= 714,
	eCMD_MISSING_TYPE	= 715,
	eCMD_MISSING_DATATYPE	= 716,
	eCMD_MISSING_NAMESPACE	= 717,
	eCMD_MISSING_PROGID	= 718,
	eCMD_MISSING_SYNTAX	= 719,
	eCMD_MISSING_FUNCTION	= 720,
	eCMD_MISSING_METHOD	= 721,
	eCMD_MISSING_DIRECTORY	= 722,
	eCMD_MISSING_NODE	= 723,
	eCMD_MISSING_VALUE	= 724,
	eCMD_MISSING_ATTRIBUTE	= 725,
	eCMD_MISSING_PACKAGE	= 726,
	eCMD_DUPLICATE_LEVEL	= 730,
	eCMD_DUPLICATE_COMMAND	= 731,
	eCMD_DUPLICATE_TRIGGER	= 732,
	eCMD_DUPLICATE_ARGUMENT	= 733,
	eCMD_UNKNOWN_LEVEL	= 740,
	eCMD_UNKNOWN_COMMAND	= 741,
	eCMD_UNKNOWN_TRIGGER	= 742,
	eCMD_UNKNOWN_ARGUMENT	= 743,
	eCMD_UNKNOWN_TYPE	= 744,
	eCMD_UNKNOWN_DATATYPE	= 745,
	eCMD_UNKNOWN_FILE_FORMAT	= 746,
	eMOCA_LDAP_CLIENT	= 750,
	eMOCA_LDAP_CLIENT_BIND	= 751,
	eMOCA_LDAP_CLIENT_AUTH	= 752,
	eMOCA_LDAP_CLIENT_SEARCH	= 753,
	eMOCA_BASE	= 800,
	eMOCA_INVALID_PASSWORD	= 801,
	eMOCA_MISSING_ARG	= 802,
	eMOCA_SMTP_COM_FAILURE	= 803,
	eMOCA_FTP_COM_FAILURE	= 804,
	eMOCA_SOAP_UNKNOWN_ERROR	= 805,
	eMOCA_INVALID_COLUMN	= 806,
	eMOCA_SFTP_COM_FAILURE	= 807,
	eMOCA_LDAP_FAILURE	= 808,
	eMOCA_COPY_FILE_FAILED	= 809,
	eMOCA_INVALID_ARG	= 810,
	eMOCA_TASK_NOT_FOUND	= 811,
	eMOCA_TASK_INVALID_STATE	= 812,
	eMOCA_LOAD_DATA_FAILED	= 813,
	eMOCA_SERVICE_MANAGER	= 815,
	eMOCA_SERVICE_READER	= 816,
	eMOCA_SQL_CONTAINS_REFS	= 820,
	eMOCA_LDAP_MISSING_CONFIG	= 830,
	eMOCA_FAILED_FED_SECURITY	= 833,
	eMOCA_FAILED_ADMIN_SECURITY	= 834,
	eEMS_MISSING_HOST	= 850,
	eEMS_MISSING_PORT	= 851,
	eEMS_MISSING_EVENT_NAME	= 852,
	eEMS_MISSING_SOURCE_SYSTEM	= 853,
	eEMS_MISSING_KEY_VALUE	= 854,
	eEMS_INVALID_EVENT_NAME	= 855,
	eEMS_BAD_XML	= 856,
	eEMS_PRIME_LOCK_EVENT	= 857,
	eEMS_FILE_CREATE	= 858,
	eEMS_FILE_REMOVE	= 859,
	eEMS_GENERATE_XML	= 860,
	eEMS_LOG_EVENT	= 861,
	eBPP_NO_SERVER_CONFIGURED	= 900,
	eBPP_SERVER_URL_INVALID	= 901,
	eBPP_SERVICE_UNAVAILABLE	= 902,
	eBPP_UNKNOWN_ENTITY_SET	= 903,
	eBPP_STORAGE_RESULTS_ERROR	= 904,
	eBPP_STORAGE_ERROR	= 905,
	eMOCA_LAST_ONE_DONT_REMOVE	= 999,
	eDB_NORMAL	= 0,
	eDB_UNIQUE_CONS_VIO	= -1,
	eDB_LOCK_TIMEOUT	= -54,
	eDB_DEADLOCK	= -60,
	eDB_NOT_A_GROUPBY_EXPR	= -979,
	eDB_MAX_CURSORS	= -1000,
	eDB_INVALID_CURSOR	= -1001,
	eDB_NOT_LOGGED_IN	= -1012,
	eDB_INTERRUPTED	= -1013,
	eDB_NO_ROWS_AFFECTED	= -1403,
	eDB_EOF_ON_COMMCHANNEL	= -3113,
	eDB_NOT_CONNECTED	= -3114,
	eDB_INVALID_NLS_PARAMETER	= -12705,
	eDB_NON_ORACLE_CONN_ERROR	= -28500,
	eDB_INCORRECT_SYNTAX	= -102,
	eDB_ADO_NOT_CONNECTED	= -2147467259,
	eDB_JDBC_EXCEPTION	= -2
    } 	MOCAErrors;

#endif

EXTERN_C const IID LIBID_MOCASrvObjLibNG;

EXTERN_C const CLSID CLSID_MOCAResults;

#ifdef __cplusplus

class DECLSPEC_UUID("1208d54e-193c-44b1-ab58-e08f736cc17b")
MOCAResults;
#endif

EXTERN_C const CLSID CLSID_MOCAServerAccess;

#ifdef __cplusplus

class DECLSPEC_UUID("c8b8c9e3-b4c8-46e3-b2ef-de24e4359d7b")
MOCAServerAccess;
#endif
#endif /* __MOCASrvObjLibNG_LIBRARY_DEFINED__ */

/* Additional Prototypes for ALL interfaces */

unsigned long             __RPC_USER  BSTR_UserSize(     unsigned long *, unsigned long            , BSTR * ); 
unsigned char * __RPC_USER  BSTR_UserMarshal(  unsigned long *, unsigned char *, BSTR * ); 
unsigned char * __RPC_USER  BSTR_UserUnmarshal(unsigned long *, unsigned char *, BSTR * ); 
void                      __RPC_USER  BSTR_UserFree(     unsigned long *, BSTR * ); 

unsigned long             __RPC_USER  VARIANT_UserSize(     unsigned long *, unsigned long            , VARIANT * ); 
unsigned char * __RPC_USER  VARIANT_UserMarshal(  unsigned long *, unsigned char *, VARIANT * ); 
unsigned char * __RPC_USER  VARIANT_UserUnmarshal(unsigned long *, unsigned char *, VARIANT * ); 
void                      __RPC_USER  VARIANT_UserFree(     unsigned long *, VARIANT * ); 

/* end of Additional Prototypes */

#ifdef __cplusplus
}
#endif

#endif


