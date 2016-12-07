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

static void LinkServFree(SERV *serv, int addAtEnd)
{
    SERV *s;
    if (addAtEnd)
    {
	for (s = ServFreeTop; s && s->next; s = s->next)
	    ;
	if (!s)
	    ServFreeTop = serv;
	else
	    s->next = serv;

	serv->prev = s;
	serv->next = NULL;
    }
    else
    {
	serv->next = ServFreeTop;
	serv->prev = NULL;

	if (ServFreeTop)
	{
	    ServFreeTop->prev = serv;
	}

	ServFreeTop = serv;
    }
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

static void LinkCons(CONS *cons)
{
    cons->next = ConsTop;
    cons->prev = NULL;

    if (ConsTop)
    {
	ConsTop->prev = cons;
    }

    ConsTop = cons;
}

static void UnlinkCons(CONS *cons)
{
    if (cons->prev)
	cons->prev->next = cons->next;
    if (cons->next)
	cons->next->prev = cons->prev;
    if (cons == ConsTop)
	ConsTop = cons->next;
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
    CONN *tmp;

    tmp = malloc(sizeof(CONN));
    *tmp = *conn;

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
 * Remove a Pending Connection from the Pending List.
 */
void DetachConnPending(CONN *conn)
{
    UnlinkConnPending(conn);
}

/*
 * Move an Idle connection to the Pending List.
 */
void MoveConnPending(CONN *conn)
{
    UnlinkConnIdle(conn);
    LinkConnPending(conn);
}

/*
 * Add to the ServFree list.  All Servers are added to the Free list (ince 
 * they haven't had a chance to get busy), and it's implementing a stack
 * structure, so we always add at the top.
 */
void AddServFree(SERV *serv)
{
    SERV *tmp;

    tmp = malloc(sizeof(SERV));
    *tmp = *serv;

    LinkServFree(tmp, 1);
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
    LinkServFree(serv, 0);
}

/*
 * Move the Server structure from the Free List to the Busy List.
 */
void MoveServBusy(SERV *serv)
{
    UnlinkServFree(serv);
    LinkServBusy(serv);
}

/*
 * Add to the ConsTop list.  All Console sessions are added to the list.
 * It's implementing a stack structure, so we always add at the top.
 */
void AddCons(CONS *cons)
{
    CONS *tmp;

    tmp = malloc(sizeof(CONS));
    *tmp = *cons;

    LinkCons(tmp);
}

/*
 * Delete from the Console List.
 */
void DelCons(CONS *cons)
{
    UnlinkCons(cons);
    free(cons);
}

