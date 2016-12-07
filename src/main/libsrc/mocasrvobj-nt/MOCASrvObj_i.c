

/* this ALWAYS GENERATED file contains the IIDs and CLSIDs */

/* link this file in with the server and any clients */


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


#ifdef __cplusplus
extern "C"{
#endif 


#include <rpc.h>
#include <rpcndr.h>

#ifdef _MIDL_USE_GUIDDEF_

#ifndef INITGUID
#define INITGUID
#include <guiddef.h>
#undef INITGUID
#else
#include <guiddef.h>
#endif

#define MIDL_DEFINE_GUID(type,name,l,w1,w2,b1,b2,b3,b4,b5,b6,b7,b8) \
        DEFINE_GUID(name,l,w1,w2,b1,b2,b3,b4,b5,b6,b7,b8)

#else // !_MIDL_USE_GUIDDEF_

#ifndef __IID_DEFINED__
#define __IID_DEFINED__

typedef struct _IID
{
    unsigned long x;
    unsigned short s1;
    unsigned short s2;
    unsigned char  c[8];
} IID;

#endif // __IID_DEFINED__

#ifndef CLSID_DEFINED
#define CLSID_DEFINED
typedef IID CLSID;
#endif // CLSID_DEFINED

#define MIDL_DEFINE_GUID(type,name,l,w1,w2,b1,b2,b3,b4,b5,b6,b7,b8) \
        const type name = {l,w1,w2,{b1,b2,b3,b4,b5,b6,b7,b8}}

#endif !_MIDL_USE_GUIDDEF_

MIDL_DEFINE_GUID(IID, IID_IMOCAResults,0x270ac420,0x3c89,0x40da,0x99,0x87,0x7c,0xc1,0xd4,0x8e,0x43,0xf1);


MIDL_DEFINE_GUID(IID, IID_IMOCAServerAccess,0x5086aa22,0xcaff,0x4569,0x86,0x20,0x38,0x42,0x35,0x55,0xf1,0xdc);


MIDL_DEFINE_GUID(IID, LIBID_MOCASrvObjLibNG,0xe401f9e5,0xe6b6,0x483f,0xa7,0x40,0xf4,0x25,0x60,0x43,0x95,0x8f);


MIDL_DEFINE_GUID(CLSID, CLSID_MOCAResults,0x1208d54e,0x193c,0x44b1,0xab,0x58,0xe0,0x8f,0x73,0x6c,0xc1,0x7b);


MIDL_DEFINE_GUID(CLSID, CLSID_MOCAServerAccess,0xc8b8c9e3,0xb4c8,0x46e3,0xb2,0xef,0xde,0x24,0xe4,0x35,0x9d,0x7b);

#undef MIDL_DEFINE_GUID

#ifdef __cplusplus
}
#endif



