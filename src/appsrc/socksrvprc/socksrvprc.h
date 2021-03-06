/*#START***********************************************************************
 *
 *  $URL$
 *  $Author$
 *
 *  Description: 
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

#ifndef SOCKSRVPRC_H
#define SOCKSRVPRC_H

#ifdef MAIN
#  define STORAGE_CLASS
#else
#  define STORAGE_CLASS extern
#endif

#define APPNAME "Socket Server Manager"

#define SRV_STATUS_DONE 1

/*
 * Globals
 */

STORAGE_CLASS int gShutdown;

/*
 * Function Prototypes
 */

/* execute.c */
long ExecuteCommand(char *command, 
	            SOCKET_FD fd, 
		    unsigned short port, 
		    int dedicated);

/* signal.c */
void InitializeSignalHandling(void);

#endif
