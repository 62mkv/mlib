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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <common.h>
#include <mislib.h>
#include <oslib.h>
#include "srvprivate.h"

static int srv_Keepalive,
           srv_KeepaliveNum;

struct mis_Keepalive srv_KeepaliveList[20];

void srvRequestKeepalive(char *id, void (*func)(void))
{
    int i;
    if (id)
    {
	for (i=0;i<srv_KeepaliveNum;i++)
	    if (0 == srv_KeepaliveList[i].counter || 
		0 == strncmp(id, srv_KeepaliveList[i].id, 24))
		break;

	if (i >= srv_KeepaliveNum)
	    srv_KeepaliveNum++;

	strncpy(srv_KeepaliveList[i].id, id, 24);
	srv_KeepaliveList[i].rollback = func;
	srv_KeepaliveList[i].counter++;
    }
    srv_Keepalive++;
    misUpdateStatus(MOCASTAT_KEEPALIVE,
                    srv_Keepalive,
                    srv_KeepaliveNum,
                    srv_KeepaliveList);
}

void srvReleaseKeepalive(char *id)
{
    int i;

    if (id)
    {
	for (i=0;i<srv_KeepaliveNum;i++)
	    if (0 == strncmp(id, srv_KeepaliveList[i].id, 24))
		break;
	if (i < srv_KeepaliveNum)
	{
	    if (--srv_KeepaliveList[i].counter <= 0)
	    {
		memset(&srv_KeepaliveList[i], 0, sizeof srv_KeepaliveList[i]);
		if (i == srv_KeepaliveNum - 1)
		    srv_KeepaliveNum--;
	    }
	}
    }

    if (--srv_Keepalive < 0)
    {
	misLogError("Someone's messing up the keepalive counter");
	srv_Keepalive = 0;
    }

    misUpdateStatus(MOCASTAT_KEEPALIVE,
                    srv_Keepalive,
                    srv_KeepaliveNum,
                    srv_KeepaliveList);
}

void srvResetKeepalive(void)
{
    int i;
    for (i=0;i<srv_KeepaliveNum;i++)
    {
	if (srv_KeepaliveList[i].counter)
	{
	    misLogWarning("Someone's keepalive (%s) needs cleaning",
		        srv_KeepaliveList[i].id?srv_KeepaliveList[i].id:"?");
	    if (srv_KeepaliveList[i].rollback)
		srv_KeepaliveList[i].rollback();
	    memset(&srv_KeepaliveList[i], 0, sizeof srv_KeepaliveList[i]);
	}
    }

    srv_KeepaliveNum = 0;
    srv_Keepalive = 0;
    misUpdateStatus(MOCASTAT_KEEPALIVE,
                    srv_Keepalive,
                    srv_KeepaliveNum,
                    srv_KeepaliveList);
}

int srv_KeepaliveLevel(void)
{
    return srv_KeepaliveNum;
}
