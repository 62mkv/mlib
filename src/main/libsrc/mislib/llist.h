/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Linked List support functions...part of misDMalloc
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

#ifndef LLIST_H
#  define LLIST_H

#include <limits.h>

#ifndef ULONG_T
#define ULONG_T
typedef unsigned long ULONG;
#endif

#ifndef ULONG_BIT
#define ULONG_BIT (8 * sizeof (ULONG)) /* bits per unsigned long */
#endif

/* a generic linked list node...
   embed this type as the 1st member in user defined nodes for use with
   ll_ functions */
typedef struct ll__lnode
{
   struct ll__lnode *next;
   struct ll__lnode *prev;
   ULONG type;
} LNODE;

/* a generic linked list...
   be sure to use ll_init before performing any operations on the list */
typedef struct
{
   LNODE head,
	 tail;
   ULONG type;
   ULONG nnodes;
} LLIST;

/* bit-wise or this mask with a user defined value to define the list type,
   if your lists nodes are application specific */
#define LL_USER_TYPE_MASK (1ul << (ULONG_BIT-1))

#define LL__LLIST_H

#define ll_init(LIST, TYPE) \
			 do \
			 { \
			    if (0 == LIST) break; \
			    (LIST)->head.next = &(LIST)->tail; \
			    (LIST)->head.prev = 0; \
			    (LIST)->head.type = (TYPE); \
			    (LIST)->tail.next = 0; \
			    (LIST)->tail.prev = &(LIST)->head; \
			    (LIST)->tail.type = (TYPE); \
			    (LIST)->type = (TYPE); \
			    (LIST)->nnodes = 0; \
			 } while (0)

#define ll_isempty(LIST) \
	((LIST)? (0 == (LIST)->nnodes) : 1)

#define ll_islinked(NODE) ((NODE)? (((NODE)->prev)? 1 : 0) : 0)

#define ll_head(LIST) \
	((LIST)? ((0 == (LIST)->nnodes)? 0 : (LIST)->head.next) : 0)

#define ll_tail(LIST) \
	((LIST)? ((0 == (LIST)->nnodes)? 0 : (LIST)->tail.prev) : 0)

#define ll_add_head(LIST, NODE) \
	((LIST)? ll_insert(LIST, &((LIST)->head), NODE) : 0)

#define ll_add_tail(LIST, NODE) \
	((LIST)? ll_insert(LIST, (LIST)->tail.prev, NODE) : 0)

#define ll_rem_head(LIST) \
	((LIST)? ll_remove(LIST, (LIST)->head.next) : 0)

#define ll_rem_tail(LIST) \
	((LIST)? ll_remove(LIST, (LIST)->tail.prev) : 0)

static LNODE *ll_insert(LLIST *list, LNODE *prev, LNODE *node); 
static LNODE *ll_remove(LLIST *list, LNODE *node);

#endif
