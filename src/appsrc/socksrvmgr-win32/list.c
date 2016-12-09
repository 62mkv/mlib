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

static int called_atexit;

static void FreeConnIdleList(void)
{
    CONN *curr,
         *next;

    for (curr=ConnIdleTop; curr; curr=next)
    {
	next = curr->next;
	free(curr);
    }

    return;
}

static void FreeServFreeList(void)
{
    SERV *curr,
         *next;

    for (curr=ServFreeTop; curr; curr=next)
    {
	next = curr->next;
	free(curr);
    }

    return;
}

static void FreeServBusyList(void)
{
    SERV *curr,
         *next;

    for (curr=ServBusyFirst; curr; curr=next)
    {
	next = curr->next;
	free(curr);
    }

    return;
}

static void FreeLists(void)
{
    FreeConnIdleList( );
    FreeServFreeList( );
    FreeServBusyList( );

    return;
}

/*
 * Link/Unlink routines. We have to be able to link and unlink from all the
 * lists.
 */
static void LinkConnIdle(CONN *conn)
{
    conn->next = ConnIdleTop;
    conn->prev = NULL;

    if (ConnIdleTop)
    {
	ConnIdleTop->prev = conn;
    }

    ConnIdleTop = conn;
}

static void UnlinkConnIdle(CONN *conn)
{
    if (conn->prev)
	conn->prev->next = conn->next;
    if (conn->next)
	conn->next->prev = conn->prev;
    if (conn == ConnIdleTop)
	ConnIdleTop = conn->next;
}

static void LinkServFree(SERV *serv)
{
    serv->next = ServFreeTop;
    serv->prev = NULL;

    if (ServFreeTop)
    {
        ServFreeTop->prev = serv;
    }

    ServFreeTop = serv;

    ServFreeCount++;
}

static void UnlinkServFree(SERV *serv)
{
    if (serv->prev)
	serv->prev->next = serv->next;
    if (serv->next)
	serv->next->prev = serv->prev;
    if (serv == ServFreeTop)
	ServFreeTop = serv->next;
    ServFreeCount--;
}

static void UnlinkServBusy(SERV *serv)
{
    if (serv->prev)
	serv->prev->next = serv->next;
    if (serv->next)
	serv->next->prev = serv->prev;
    if (serv == ServBusyLast)
	ServBusyLast = serv->prev;
    if (serv == ServBusyFirst)
	ServBusyFirst = serv->next;
    ServBusyCount--;
}

static void LinkServBusy(SERV *serv)
{
    serv->next = NULL;
    serv->prev = ServBusyLast;

    if (ServBusyLast)
	ServBusyLast->next = serv;

    if (!ServBusyFirst)
	ServBusyFirst = serv;

    ServBusyLast = serv;
    ServBusyCount++;
}

static void LinkConnPending(CONN *conn)
{
    conn->next = NULL;
    conn->prev = ConnPendingLast;

    if (ConnPendingLast)
	ConnPendingLast->next = conn;

    if (!ConnPendingFirst)
	ConnPendingFirst = conn;

    ConnPendingLast = conn;
}

static void UnlinkConnPending(CONN *conn)
{
    if (conn->prev)
	conn->prev->next = conn->next;
    if (conn->next)
	conn->next->prev = conn->prev;
    if (conn == ConnPendingLast)
	ConnPendingLast = conn->prev;
    if (conn == ConnPendingFirst)
	ConnPendingFirst = conn->next;
}

/*
 * Add to the Idle Connections list.  All Connections are added to the Idle
 * list, and it's implementing a stack structure, so we always add at the top.
 */
void AddConnIdle(CONN *conn)
{
    CONN           *tmp;

    tmp = malloc(sizeof(CONN));
    *tmp = *conn;

    if (! called_atexit)
    {
	called_atexit++;
	osAtexit(FreeLists);
    }

    LinkConnIdle(tmp);
}

/*
 * Delete from the Idle Connections List.
 */
void DelConnIdle(CONN *conn)
{
    UnlinkConnIdle(conn);
    free(conn);
}

/*
 * Move an unattached Connection to the Idle List.
 */
void MoveConnIdle(CONN *conn)
{
    LinkConnIdle(conn);
}

/*
 * Remove an Idle Connection from the Idle List.
 */
void DetachConnIdle(CONN *conn)
{
    UnlinkConnIdle(conn);
}

/*
 * Add to the ServFree list.  All Servers are added to the Free list (ince 
 * they haven't had a chance to get busy), and it's implementing a stack
 * structure, so we always add at the top.
 */
void AddServFree(SERV *serv)
{
    SERV           *tmp;

    tmp = malloc(sizeof(SERV));
    *tmp = *serv;

    if (! called_atexit)
    {
	called_atexit++;
	osAtexit(FreeLists);
    }

    LinkServFree(tmp);
}

/*
 * Delete from the Free Server List.
 */
void DelServFree(SERV *serv)
{
    UnlinkServFree(serv);
    free(serv);
}

/*
 * Delete from the Busy Server List. This can only happen when a busy 
 * server process dies unexpectedly
 */
void DelServBusy(SERV *serv)
{
    UnlinkServBusy(serv);
    free(serv);
}

void MoveServFree(SERV *serv)
{
    UnlinkServBusy(serv);
    LinkServFree(serv);
}

/*
 * Move the Server structure from the Free List to the Busy List.
 */
void MoveServBusy(SERV *serv)
{
    UnlinkServFree(serv);
    LinkServBusy(serv);
}

void MoveConnPending(CONN *conn)
{
    UnlinkConnIdle(conn);
    LinkConnPending(conn);
}

void DetachConnPending(CONN *conn)
{
    UnlinkConnPending(conn);
}
