/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Typedefs, prototypes for OS-specific development.
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

#ifndef OSPRIVATE_H
#define OSPRIVATE_H

#include <oslib.h>

#include "../jnilib/oscalls.h"

#define NUM_MBX 500
#define MBX_FILE "mailboxes.mem"
#define MBX_SEPARATOR '|'

#ifdef WIN32
typedef struct
{
    long controlword;
    union
    {
        WSAPROTOCOL_INFO info;
        OS_TCP_ADDR tcp_addr;
    } data;
} OS__SENDFILE_STRUCT;

#endif

/*
 * Structure Defintions
 */

struct osIniEntry_s
{
    char *section;
    char *name;
    char *value;
    char *realValue;
    struct osIniEntry_s *prev,
                        *next;
};

/* Prototypes */
#if defined (__cplusplus)
extern "C" {
#endif
#ifdef WIN32
long os_PutMapInfo(void *mem, unsigned long size, HANDLE hMutex, long mode);
long os_GetMapInfo(void *mem, unsigned long *size, HANDLE *hMutex, long *mode);
#else
long os_PutMapInfo(void *mem, unsigned long size, int fd, long mode);
long os_GetMapInfo(void *mem, unsigned long *size, int *fd, long *mode);
#endif
long os_DelMapInfo(void *mem);
void os_FreeVarList(void);
void os_PutVar(char *name, char *value, char *tag, int onJNISide);
#if defined (__cplusplus)
}
#endif

#endif /* OSPRIVATE_H */
