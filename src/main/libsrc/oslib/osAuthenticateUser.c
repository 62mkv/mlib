static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to support authenticating users.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2002-2007
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

#define MOCA_ALL_SOURCE

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>

#ifndef UNIX
# define SECURITY_WIN32
# include <rpc.h>
# include <tchar.h>
# include <sspi.h>
#endif

#include <mocaerr.h>
#include <mocagendef.h>
#include <oslib.h>

#ifdef UNIX

/*
 *  FUNCTION: osAuthenticateUser
 *
 *  PURPOSE:  Authenticate the given logname and password against a PAM
 *            service module.
 *
 *  RETURNS:  eOS_AUTH_NOT_SUPPORTED - O/S authentication is not supported
 *                                     on UNIX systems.
 */

long osAuthenticateUser(char *i_logname, char *i_password)
{
    return eOS_AUTH_NOT_SUPPORTED;
}

#else 

/*
 *  Type Definitions
 */

typedef struct _AUTH_SEQ {
    BOOL fInitialized;
    BOOL fHaveCredHandle;
    BOOL fHaveCtxtHandle;
    CredHandle hcred;
    struct _SecHandle hctxt;
} AUTH_SEQ, *PAUTH_SEQ;

/*
 *  Global Variables
 */

ACCEPT_SECURITY_CONTEXT_FN       _AcceptSecurityContext      = NULL;
REVERT_SECURITY_CONTEXT_FN       _RevertSecurityContext      = NULL;
ACQUIRE_CREDENTIALS_HANDLE_FN    _AcquireCredentialsHandle   = NULL;
COMPLETE_AUTH_TOKEN_FN           _CompleteAuthToken          = NULL;
DELETE_SECURITY_CONTEXT_FN       _DeleteSecurityContext      = NULL;
FREE_CONTEXT_BUFFER_FN           _FreeContextBuffer          = NULL;
FREE_CREDENTIALS_HANDLE_FN       _FreeCredentialsHandle      = NULL;
INITIALIZE_SECURITY_CONTEXT_FN   _InitializeSecurityContext  = NULL;
QUERY_SECURITY_PACKAGE_INFO_FN   _QuerySecurityPackageInfo   = NULL;
IMPERSONATE_SECURITY_CONTEXT_FN  _ImpersonateSecurityContext = NULL;

/*
 *  Local Function Prototypes
 */

static HMODULE sLoadSecurityDll(void);

static void sUnloadSecurityDll(HMODULE hModule);

static BOOL sGenClientContext(PAUTH_SEQ pAS,
                              SEC_WINNT_AUTH_IDENTITY *pAuthority,
                              PVOID pIn,
                              DWORD cbIn,
                              PVOID pOut,
                              PDWORD pcbOut,
                              PBOOL pfDone);

static BOOL sGenServerContext(PAUTH_SEQ pAS,
                              PVOID pIn,
                              DWORD cbIn,
                              PVOID pOut,
                              PDWORD pcbOut,
                              PBOOL pfDone);

static BOOL sImpersonateContext(PAUTH_SEQ pAS);

static HANDLE sSSPLogonUser(LPTSTR szDomain, LPTSTR szUser, LPTSTR szPassword);

static BOOL sGetTokenUserInfo(char *pUser, char *pDomain, HANDLE hToken);

/*
 *  FUNCTION: sUnloadSecurityDll
 *
 *  PURPOSE:  Unload the security library that we previously loaded.
 *
 *  RETURNS:  void
 */

static void sUnloadSecurityDll(HMODULE hModule) 
{
    if (hModule)
        FreeLibrary(hModule);

    _AcceptSecurityContext      = NULL;
    _RevertSecurityContext      = NULL;
    _AcquireCredentialsHandle   = NULL;
    _CompleteAuthToken          = NULL;
    _DeleteSecurityContext      = NULL;
    _FreeContextBuffer          = NULL;
    _FreeCredentialsHandle      = NULL;
    _InitializeSecurityContext  = NULL;
    _QuerySecurityPackageInfo   = NULL;
    _ImpersonateSecurityContext = NULL;
}

/*
 *  FUNCTION: sLoadSecurityDll
 *
 *  PURPOSE:  Load the security library.
 *
 *  RETURNS:  void
 */

static HMODULE sLoadSecurityDll(void) 
{
    HMODULE hModule;
    BOOL    fAllFunctionsLoaded = FALSE; 
    TCHAR   lpszDLL[MAX_PATH];
    OSVERSIONINFO VerInfo;

    /* 
     *  Find out which security DLL to use, depending on whether we 
     *  are on NT or Win95 or 2000 or XP or .NET Server.  We have to 
     *  use security.dll on Windows NT 4.0.  All other Win32 operating 
     *  systems, we have to use Secur32.dll
     */
    VerInfo.dwOSVersionInfoSize = sizeof(OSVERSIONINFO);
    if (!GetVersionEx (&VerInfo))
    {
        misLogError("GetVersionEx: %s", osError( ));
        return FALSE;
    }

    /* Determine which library to use. */
    if (VerInfo.dwPlatformId == VER_PLATFORM_WIN32_NT &&
        VerInfo.dwMajorVersion == 4 &&
        VerInfo.dwMinorVersion == 0)
    {
        lstrcpy(lpszDLL, _T("security.dll"));
    }
    else
    {
	lstrcpy(lpszDLL, _T("secur32.dll"));
    }


    /* Load the security library. */
    hModule = LoadLibrary(lpszDLL);
    if (!hModule)
    {
	misLogError("LoadLibrary: %s", osError( ));
        return NULL;
    }

    /* Set our function pointers. */
    __try 
    {
	_AcceptSecurityContext = (ACCEPT_SECURITY_CONTEXT_FN) 
            GetProcAddress(hModule, "AcceptSecurityContext");
	if (!_AcceptSecurityContext)
	    __leave;

	_RevertSecurityContext = (REVERT_SECURITY_CONTEXT_FN) 
            GetProcAddress(hModule, "RevertSecurityContext");
	if (!_RevertSecurityContext)
	    __leave;

	_AcquireCredentialsHandle = (ACQUIRE_CREDENTIALS_HANDLE_FN)
            GetProcAddress(hModule, "AcquireCredentialsHandleA");
	if (!_AcquireCredentialsHandle)
	    __leave;

	/* 
         *  CompleteAuthToken is not present on Windows 9x Secur32.dll, so
         *  we don't check for the availablity of the function.
         */
	_CompleteAuthToken = (COMPLETE_AUTH_TOKEN_FN) 
            GetProcAddress(hModule, "CompleteAuthToken");

	_ImpersonateSecurityContext = (IMPERSONATE_SECURITY_CONTEXT_FN) 
            GetProcAddress(hModule, "ImpersonateSecurityContext");
	if (!_ImpersonateSecurityContext)
	    __leave;
      
	_DeleteSecurityContext = (DELETE_SECURITY_CONTEXT_FN) 
            GetProcAddress(hModule, "DeleteSecurityContext");
	if (!_DeleteSecurityContext)
            __leave;

	_FreeContextBuffer = (FREE_CONTEXT_BUFFER_FN) 
            GetProcAddress(hModule, "FreeContextBuffer");
	if (!_FreeContextBuffer)
	    __leave;

	_FreeCredentialsHandle = (FREE_CREDENTIALS_HANDLE_FN) 
            GetProcAddress(hModule, "FreeCredentialsHandle");
	if (!_FreeCredentialsHandle)
            __leave;

	_InitializeSecurityContext = (INITIALIZE_SECURITY_CONTEXT_FN) 
            GetProcAddress(hModule, "InitializeSecurityContextA");
	if (!_InitializeSecurityContext)
            __leave;

	_QuerySecurityPackageInfo = (QUERY_SECURITY_PACKAGE_INFO_FN)
            GetProcAddress(hModule, "QuerySecurityPackageInfoA");
	if (!_QuerySecurityPackageInfo)
            __leave;

	fAllFunctionsLoaded = TRUE;
    }
    __finally 
    {
	if (!fAllFunctionsLoaded) 
	{
	     misLogError("Unable to load function: %s", osError( ));
             sUnloadSecurityDll(hModule);
             hModule = NULL;
	}
    }
   
    return hModule;

}

/*
 *  FUNCTION: sGenClientContext
 *
 *  PURPOSE:  Optionally takes an input buffer coming from the server 
 *            and returns a buffer of information to send back to the 
 *            server.  Also returns an indication of whether or not the 
 *            context is complete.
 *
 *  RETURNS:  TRUE   Successful
 *            FALSE  Failed
 */

static BOOL sGenClientContext(PAUTH_SEQ pAS, 
                              PSEC_WINNT_AUTH_IDENTITY pAuthIdentity,
                              PVOID pIn, 
                              DWORD cbIn, 
                              PVOID pOut, 
                              PDWORD pcbOut, 
                              PBOOL pfDone) 
{
    SECURITY_STATUS ss;
    TimeStamp       tsExpiry;
    SecBufferDesc   sbdOut;
    SecBuffer       sbOut;
    SecBufferDesc   sbdIn;
    SecBuffer       sbIn;
    ULONG           fContextAttr;

    /* Get a credentials handle if we don't have one yet. */
    if (!pAS->fInitialized) 
    {
	ss = _AcquireCredentialsHandle(NULL, 
                                       _T("NTLM"), 
                                       SECPKG_CRED_OUTBOUND, 
                                       NULL, 
                                       pAuthIdentity, 
                                       NULL, 
                                       NULL,
                                       &pAS->hcred, &tsExpiry);
	if (ss < 0) 
	{
	    misTrc(T_FLOW, "AcquireCredentialsHandle: %s", osError( ));
            return FALSE;
	}

	pAS->fHaveCredHandle = TRUE;
    }

    /* Prepare output buffer. */
    sbdOut.ulVersion = 0;
    sbdOut.cBuffers  = 1;
    sbdOut.pBuffers  = &sbOut;

    sbOut.cbBuffer   = *pcbOut;
    sbOut.BufferType = SECBUFFER_TOKEN;
    sbOut.pvBuffer   = pOut;

    /* Prepare input buffer. */
    if (pAS->fInitialized)  
    {
	sbdIn.ulVersion = 0;
	sbdIn.cBuffers  = 1;
	sbdIn.pBuffers  = &sbIn;

	sbIn.cbBuffer   = cbIn;
	sbIn.BufferType = SECBUFFER_TOKEN;
	sbIn.pvBuffer   = pIn;
    }

    ss = _InitializeSecurityContext(&pAS->hcred, 
                                    pAS->fInitialized ? &pAS->hctxt : NULL, 
                                    NULL,
                                    0, 
                                    0, 
                                    SECURITY_NATIVE_DREP, 
                                    pAS->fInitialized ? &sbdIn : NULL,
                                    0, 
                                    &pAS->hctxt, 
                                    &sbdOut, 
                                    &fContextAttr, 
                                    &tsExpiry);
    if (ss < 0)  
    { 
	misTrc(T_FLOW, "InitializeSecurityContext: %s", osError( ));
	return FALSE;
    }

    pAS->fHaveCtxtHandle = TRUE;

    /* If necessary, complete token. */
    if (ss == SEC_I_COMPLETE_NEEDED || ss == SEC_I_COMPLETE_AND_CONTINUE) 
    {
	if (_CompleteAuthToken) 
	{
	    ss = _CompleteAuthToken(&pAS->hctxt, &sbdOut);
	    if (ss < 0)  
 	    {
		misTrc(T_FLOW, "CompleteAuthToken: %s", osError( ));
		return FALSE;
	    }
	}
	else 
	{
	    misTrc(T_FLOW, "CompleteAuthToken not supported");
            return FALSE;
	}
    }

    *pcbOut = sbOut.cbBuffer;

    if (!pAS->fInitialized)
	pAS->fInitialized = TRUE;

    *pfDone = !(ss == SEC_I_CONTINUE_NEEDED  || 
                ss == SEC_I_COMPLETE_AND_CONTINUE );

    return TRUE;
}

/*
 *  FUNCTION: sGenServerContext
 *
 *  PURPOSE:  Takes an input buffer coming from the client and returns a 
 *            buffer to be sent to the client.  Also returns an indication 
 *            of whether or not the context is complete.
 *
 *  RETURNS:  TRUE   Successful
 *            FALSE  Failed
 */

static BOOL sGenServerContext(PAUTH_SEQ pAS, 
                              PVOID pIn, 
                              DWORD cbIn, 
                              PVOID pOut,
                              PDWORD pcbOut, 
                              PBOOL pfDone) 
{
    SECURITY_STATUS ss;
    TimeStamp       tsExpiry;
    SecBufferDesc   sbdOut;
    SecBuffer       sbOut;
    SecBufferDesc   sbdIn;
    SecBuffer       sbIn;
    ULONG           fContextAttr;

    /* Get a credentials handle if we don't have one yet. */
    if (!pAS->fInitialized)  
    {
	ss = _AcquireCredentialsHandle(NULL, 
                                       _T("NTLM"), 
                                       SECPKG_CRED_INBOUND, 
                                       NULL, 
                                       NULL, 
                                       NULL, 
                                       NULL, 
                                       &pAS->hcred, 
                                       &tsExpiry);
	if (ss < 0) 
	{
	    misTrc(T_FLOW, "AcquireCredentialsHandle: %s", osError( ));
	    return FALSE;
	}

	pAS->fHaveCredHandle = TRUE;
    }

    /* Prepare output buffer. */
    sbdOut.ulVersion = 0;
    sbdOut.cBuffers  = 1;
    sbdOut.pBuffers  = &sbOut;
 
    sbOut.cbBuffer   = *pcbOut;
    sbOut.BufferType = SECBUFFER_TOKEN;
    sbOut.pvBuffer   = pOut;

    /* Prepare input buffer. */
    sbdIn.ulVersion = 0;
    sbdIn.cBuffers  = 1;
    sbdIn.pBuffers  = &sbIn;
 
    sbIn.cbBuffer   = cbIn;
    sbIn.BufferType = SECBUFFER_TOKEN;
    sbIn.pvBuffer   = pIn;

    ss = _AcceptSecurityContext(&pAS->hcred, 
                                pAS->fInitialized ? &pAS->hctxt : NULL, 
                                &sbdIn, 
                                0, 
                                SECURITY_NATIVE_DREP, 
                                &pAS->hctxt, 
                                &sbdOut, 
                                &fContextAttr, 
                                &tsExpiry);
    if (ss < 0)  
    {
	misTrc(T_FLOW, "AcceptSecurityContext: %s", osError( ));
	return FALSE;
    }

    pAS->fHaveCtxtHandle = TRUE;

    /* If necessary, complete token. */
    if (ss == SEC_I_COMPLETE_NEEDED || ss == SEC_I_COMPLETE_AND_CONTINUE) 
    {
	if (_CompleteAuthToken) 
	{
	    ss = _CompleteAuthToken(&pAS->hctxt, &sbdOut);
            if (ss < 0)  
	    {
		misTrc(T_FLOW, "CompleteAuthToken: %s", osError( ));
            	return FALSE;
	    }
	}
	else 
	{
	    misTrc(T_FLOW, "CompleteAuthToken not supported");
	    return FALSE;
	}
    }

    *pcbOut = sbOut.cbBuffer;

    if (!pAS->fInitialized)
	pAS->fInitialized = TRUE;

    *pfDone = !(ss = SEC_I_CONTINUE_NEEDED || 
                ss == SEC_I_COMPLETE_AND_CONTINUE);

    return TRUE;
}

/*
 *  FUNCTION: sImpersonateContext
 *
 *  PURPOSE:
 *
 *  RETURNS:  TRUE   Successful
 *            FALSE  Failed
 *
 */

static BOOL sImpersonateContext(PAUTH_SEQ pAS)
{
    SECURITY_STATUS ss;

    ss = _ImpersonateSecurityContext(&pAS->hctxt);
    if (ss < 0)
    {
	misTrc(T_FLOW, "ImpersonateSecurityContext: %s", osError( ));
      	return FALSE;
    }

    return TRUE;
}

/*
 *  FUNCTION: sRevertContext
 *
 *  PURPOSE:
 *
 *  RETURNS:  TRUE   Successful
 *            FALSE  Failed
 *
 */

static BOOL sRevertContext(PAUTH_SEQ pAS)
{
    SECURITY_STATUS ss;

    ss = _RevertSecurityContext(&pAS->hctxt);
    if (ss < 0)
    {
	misTrc(T_FLOW, "RevertSecurityContext: %s", osError( ));
      	return FALSE;
    }

    return TRUE;
}

/*
 *  FUNCTION: sSSPLogonUser
 *
 *  PURPOSE:
 *
 *  RETURNS:  Logon handle.
 *            NULL - An error occurred.
 */

static HANDLE sSSPLogonUser(LPTSTR szDomain, LPTSTR szUser, LPTSTR szPassword) 
{
    HANDLE htok = NULL;
    AUTH_SEQ    asServer   = {0};
    AUTH_SEQ    asClient   = {0};
    BOOL        fDone      = FALSE;
    BOOL        fResult    = FALSE;
    DWORD       cbOut      = 0;
    DWORD       cbIn       = 0;
    DWORD       cbMaxToken = 0;
    PVOID       pClientBuf = NULL;
    PVOID       pServerBuf = NULL;
    PSecPkgInfo pSPI       = NULL;
    HMODULE     hModule    = NULL;

    SEC_WINNT_AUTH_IDENTITY ai;

     __try 
    {

	hModule = sLoadSecurityDll( );
        if (!hModule)
            __leave;

	/* Get max token size. */
	_QuerySecurityPackageInfo(_T("NTLM"), &pSPI);
	cbMaxToken = pSPI->cbMaxToken;
	_FreeContextBuffer(pSPI);

	/* Allocate buffers for client and server messages. */
	pClientBuf = HeapAlloc(GetProcessHeap( ), HEAP_ZERO_MEMORY, cbMaxToken);
	pServerBuf = HeapAlloc(GetProcessHeap( ), HEAP_ZERO_MEMORY, cbMaxToken);

	/* Initialize auth identity structure. */
	ZeroMemory(&ai, sizeof(ai));

	ai.Domain         = (unsigned char *)szDomain;
	ai.DomainLength   = lstrlen(szDomain);
	ai.User           = (unsigned char *)szUser;
	ai.UserLength     = lstrlen(szUser);
	ai.Password       = (unsigned char *)szPassword;
	ai.PasswordLength = lstrlen(szPassword);
	ai.Flags          = SEC_WINNT_AUTH_IDENTITY_ANSI;

	/* Prepare client message (negotiate). */
	cbOut = cbMaxToken;
	if (!sGenClientContext(&asClient, 
                               &ai, 
                               NULL, 
                               0, 
                               pClientBuf, 
                               &cbOut, 
                               &fDone))
	    __leave;

	/* Prepare server message (challenge). */
	cbIn = cbOut;
	cbOut = cbMaxToken;
	if (!sGenServerContext(&asServer, 
                               pClientBuf, 
                               cbIn, 
                               pServerBuf, 
                               &cbOut, 
                               &fDone))
	    __leave;
	    /*  Most likely failure: AcceptServerContext fails with 
             *                       SEC_E_LOGON_DENIED in the case of bad 
             *                       szUser or szPassword.
             *
             *  Unexpected Result: Logon will succeed if you pass in a bad 
             *                     szUser and the guest account is enabled 
             *                     in the specified domain. 
             */

	/* Prepare client message (authenticate). */
	cbIn = cbOut;
	cbOut = cbMaxToken;
	if (!sGenClientContext(&asClient, 
                               &ai, 
                               pServerBuf, 
                               cbIn, 
                               pClientBuf, 
                               &cbOut,
			       &fDone))
	    __leave;

	/* Prepare server message (authentication). */
	cbIn = cbOut;
	cbOut = cbMaxToken;
	if (!sGenServerContext(&asServer, 
                               pClientBuf, 
                               cbIn, 
                               pServerBuf, 
                               &cbOut, 
	  		       &fDone))
	    __leave;

	if (!sImpersonateContext(&asServer))
	    __leave;
      
	if (!OpenThreadToken(GetCurrentThread( ),
                             TOKEN_QUERY,
                             TRUE, &htok))
	{
	    misTrc(T_FLOW, "OpenThreadToken: %s", osError( ));
	    __leave;
	}

	if (!sRevertContext(&asServer))
	    __leave;

    }
    __finally 
    {
	/* Clean up resources */
	if (asClient.fHaveCtxtHandle)
	    _DeleteSecurityContext(&asClient.hctxt);

	if (asClient.fHaveCredHandle)
	    _FreeCredentialsHandle(&asClient.hcred);

	if (asServer.fHaveCtxtHandle)
	    _DeleteSecurityContext(&asServer.hctxt);

	if (asServer.fHaveCredHandle)
	    _FreeCredentialsHandle(&asServer.hcred);

	if (hModule)
	    sUnloadSecurityDll(hModule);

	HeapFree(GetProcessHeap( ), 0, pClientBuf);
	HeapFree(GetProcessHeap( ), 0, pServerBuf);
    }

    return htok;
}

/*
 *  FUNCTION: sGetTokenUserInfo
 *
 *  PURPOSE:
 *
 *  RETURNS:  TRUE   Successful
 *            FALSE  Failed
 */

static BOOL sGetTokenUserInfo(char *pUser, char *pDomain, HANDLE hToken)
{	
    SID_NAME_USE snu;
    PTOKEN_USER ptiUser = NULL;
    DWORD cbti = 0;

    char szUser[256];
    char szDomain[256];

    DWORD pcchUser = sizeof(szUser);
    DWORD pcchDomain = sizeof(szDomain);
		
	
    /* Obtain the size of the user information in the token. */
    if (GetTokenInformation(hToken, TokenUser, NULL, 0, &cbti))
    {
	/* 
         *  If we passed with zero length buffer, that's bad
         *  should have failed due to zero length buffer.
         */
	return FALSE;
    }
    else
    {
        /* Should have been error insufficient buffer. */
	if (GetLastError( ) != ERROR_INSUFFICIENT_BUFFER)
	{
	    return FALSE;
	}
    }

    ptiUser = (PTOKEN_USER) HeapAlloc(GetProcessHeap( ), 0, cbti);
    if (!ptiUser)
	return FALSE;

    if (!GetTokenInformation(hToken, TokenUser, ptiUser, cbti, &cbti))
	return FALSE;

    if (!LookupAccountSid(NULL, ptiUser->User.Sid, szUser, &pcchUser, 
                          szDomain, &pcchDomain, &snu))
    {					
	return FALSE;
    } 

    strcpy(pUser,   szUser);
    strcpy(pDomain, szDomain);

    return TRUE;
}

/*
 *  FUNCTION: osAuthenticateUser
 *
 *  PURPOSE:  Authenticate the given logname and password using SSPI.
 *
 *  RETURNS:  eOK - All ok
 *            Some error code
 */

long osAuthenticateUser(char *i_logname, char *i_password)
{
    char *logname,
         *password;

    char szUser[256],
    szDomain[256],
    szCompName[256];

    DWORD cbn = sizeof(szCompName);

    HANDLE hlogon;
	
    /* Firewall our arguments. */
    logname  = i_logname  ? i_logname  : "nobody";
    password = i_password ? i_password : "nopass";

    hlogon = sSSPLogonUser(_T(""), logname, password);
    if (!hlogon)
        return eOS_AUTH_FAILED;
 
    if (!sGetTokenUserInfo(szUser, szDomain, hlogon))
    {
        misTrc(T_FLOW, "Unable to get token user information");
        CloseHandle(hlogon);
        return eOS_AUTH_FAILED;
    }

    /* 
     *  Get the computer name to make sure this is not a local account 
     *  trying to impersonate a domain account.
     */
    if (!GetComputerName(szCompName, &cbn))
    {
        misLogError("GetComputerName: %s", osError( ));
        return eOS_AUTH_FAILED;
    }
	
    if (strcmp(szDomain, szCompName) == 0)
    {
        /* Invalid user, exit. */
        misTrc(T_FLOW, "User logged in to local account");
        CloseHandle(hlogon);
        return eOS_AUTH_FAILED;
    }

    CloseHandle(hlogon);
	
    return eOK;
}

#endif 
