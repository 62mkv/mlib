/*#START***********************************************************************
 *
 *  $URL$
 *  $Author$
 *
 *  Description: 
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2009
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

#include "socksrvmgr.h"

/*
 * Fork a server and assign him one connection for life.
 */
BOOL fork_dedicated(SOCKET_FD fd)
{
    PROCESS_INFORMATION procinfo;
    STARTUPINFO     NewInfo;
    char            command_line[512];
    char            conn_str[32];
    int             j;
    unsigned long   nonblock = FALSE;
    char            *envstr;

    /* 
     * Set socket to blocking
     */
    WSAEventSelect(fd, (WSAEVENT)NULL, 0);
    ioctlsocket(fd,FIONBIO,&nonblock);

    /*
     * Set the inherit flag on the socket connection to TRUE. 
     */
    if (!SetHandleInformation((HANDLE)fd, HANDLE_FLAG_INHERIT, HANDLE_FLAG_INHERIT))
    {
	misLogError("SetHandleInformation:%s", osError());
	return FALSE;
    }

    /*
     * Process startup information.  This is used by CreateProcessA to
     * tell what sort of process we want to create.  We're creating a
     * console child process that inherits most of our handles.
     */
    NewInfo.cb = sizeof NewInfo;
    NewInfo.lpReserved = NULL;
    NewInfo.lpDesktop = NULL;
    NewInfo.lpTitle = NULL;
    NewInfo.dwFlags = 0;
    NewInfo.cbReserved2 = 0;
    NewInfo.lpReserved2 = NULL;

    /*
     * The command-line is the only way we can communicate to our
     * child process at this point.
     */
    osEncodeDescriptor((HANDLE)fd, conn_str);
    sprintf(command_line, "%s%s%s -f%s -p%d", 
                                          param.server_program, 
     (param.console_section && *param.console_section)?" -S":"",
     (param.console_section && *param.console_section)?param.console_section:"",
					  conn_str,
					  param.port);

    if (param.memfile[0])
    {
	strcat(command_line, " -m");
	strcat(command_line, param.memfile);
    }
    
    if (param.tracelevel[0])
    {
	strcat(command_line, " -t");
	strcat(command_line, param.tracelevel);
    }
    
    for (j = 0; j < param.nopts; j++)
    {
	strcat(command_line, " ");
	strcat(command_line, param.opts[j]);
    }

    /*
     * Get the environment strings for this server process.
     */
    envstr = get_environment_strings(param.server_program);

    if (!CreateProcess(NULL, command_line, NULL, NULL, TRUE,
		 CREATE_NEW_PROCESS_GROUP, envstr, NULL, &NewInfo, &procinfo))
    {
	misLogError("CreateProcess: %s", osError());
        free(envstr);
	return FALSE;
    }

    osSockClose(fd);

    free(envstr);

    misTrc(T_MGR, "Created dedicated server (PID: %d)", procinfo.dwProcessId);

    return TRUE;
}
