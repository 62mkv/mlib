static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Command interface to the MOCA XML library.
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
#include <ctype.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <sqllib.h>
#include <srvlib.h>

#include "stdafx.h"

#include "WAPIWrapperCpp.h"

#include "atlstr.h"
#include "atlcomtime.h"
#include <iostream>

long logonID = 0;
long reqID1 = 0;
long reqID2 = 0;
/*
 *  FUNCTION: doWindTrade
 *
 *  PURPOSE:  Command interface to the doWindTrade( ) function.
 */
extern "C"
LIBEXPORT
RETURN_STRUCT *doWindTrade(char *acntID)
{
    misTrc(T_FLOW, "Now doWindTrade begin...,%s", acntID);
        long errorCode = CWAPIWrapperCpp::start();
    CString str = "abc";
    if (errorCode == 0)
    {
        misTrc(T_FLOW, "Terminal auth success!");
    }
    else
    {
        //printErrMsg(errorCode);
    }
    
        WindData wd;
    errorCode = CWAPIWrapperCpp::tlogon(wd, L"00000010", L"0", CT2CW(acntID), L"111111", L"SHSZ", L"");
    if (errorCode == 0)
    {
        logonID = wd.GetLogonID();
        misTrc(T_FLOW, "Login trade account success, Login ID: %d", logonID);
    }
    else
    {
        misTrc(T_FLOW, "Login trade account failed：%s", wd.GetErrorMsg());
    }

    misTrc(T_FLOW, "Test put order:");
    WCHAR* errMsg = NULL;
    errorCode = CWAPIWrapperCpp::torder(wd, L"600000.SH", L"Buy", L"10.3", L"200", L"");//单账户登录可以不指定LogonId
    if (errorCode == 0)//如果没有错误，errMsg仍然为NULL
    {
        reqID1 = wd.GetOrderRequestID();
        misTrc(T_FLOW, "put order 600000.SH success, reqID1:%d", reqID1);
    }
    else
    {
        misTrc(T_FLOW, "error for put order:%s", wd.GetErrorMsg());
    }

    errorCode = CWAPIWrapperCpp::torder(reqID2, L"600718.SH", L"Sell", 10.0, 300, L"", &errMsg);
    if (errorCode == 0)//如果没有错误，errMsg仍然为NULL
    {
        misTrc(T_FLOW, "put order 600718.SH success, reqID2:%d", reqID2);
    }
    else
    {
        misTrc(T_FLOW, (char*)errMsg);
        //需要手动释放
        delete[] errMsg;
        errMsg = NULL;
    }
    return srvResults(eOK,NULL);
}

// void printErrMsg(long errorCode)
// {
//     WCHAR buffer[64];
//     int length = 64;
//     CWAPIWrapperCpp::getErrorMsg(errorCode, eCHN, buffer, length);
//     misTrc(T_FLOW, "%s", buffer);
// }
