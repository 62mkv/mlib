static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: 
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

#include <string.h>
#include <stdlib.h>
#include <ctype.h>

#include <mocaerr.h>

#include <mislib.h>

typedef struct mis_HashList HASH_LIST;

struct mis_HashList
{
    unsigned long hashval;
    char *key;
    void *payload;
    HASH_LIST *next, *prev;
};

struct mis_Hash
{
    int slots;
    HASH_LIST **list;
    unsigned long (*hashfunc)(char *);
    int (*cmpfunc)(char *, char *);
    int currentslot;
    HASH_LIST *currententry;
};

/*
 * misHash - This is a simple string hashing function.  This is not
 *           meant to be cryptographcally secure in any way, just to
 *           provide somewhat collision-free hashing, with a minimum
 *           of overhead.
 */
#define MULTIPLIER 123ul

unsigned long misHash(char *str)
{
    register char *p = str;
    register unsigned long hash = 0;

    while (*p)
    {
        hash = (hash * MULTIPLIER) + *p++;
    }
    return hash;
}

unsigned long misCiHash(char *str)
{
    register char *p = str;
    register unsigned long hash = 0;

    while (*p)
    {
        hash = (hash * MULTIPLIER) + tolower((unsigned char)*p++);
    }
    return hash;
}

/*
 * The rest of these functions use the Hash function.  The concept here
 * is one of "keys" and "payloads".  When a hash "put" takes place, the
 * payload is stored, along with the key in the correct hash slot for that
 * key.
 */
long misHashPut(MISHASH *table, char *key, void *payload)
{
    unsigned long hashval, modval;
    register HASH_LIST *ll;

    if (!table || !table->slots)
	return eERROR;

    /* Look for this key */
    hashval = table->hashfunc(key);
    modval  = hashval % table->slots;
    for (ll=table->list[modval];ll;ll=ll->next)
    {
	/*
	 * The key is found...we don't want to make a duplicate, and
	 * we don't have garbage collection, so return an error.
	 */
	if (ll->key && 0 == table->cmpfunc(ll->key, key))
	{
	    return eERROR;
	}
    }

    ll = calloc(1, sizeof(HASH_LIST));
    ll->key = malloc(strlen(key)+1);
    strcpy(ll->key, key);
    ll->payload = payload;
    ll->hashval = hashval;
    ll->next = table->list[modval];
    if (ll->next)
	ll->next->prev = ll;

    table->list[modval] = ll;
    ll->prev = NULL;

    return eOK;
}

void *misHashGet(MISHASH *table, char *key)
{
    unsigned long hashval, modval;
    register HASH_LIST *ll;

    if (!table || !table->slots)
	return NULL;

    /* Look for this key */
    hashval = table->hashfunc(key);
    modval  = hashval % table->slots;
    for (ll=table->list[modval];ll;ll=ll->next)
    {
	/* The key is found.  We need to return the payload. */
	if (ll->key && (ll->hashval == hashval) && (0 == table->cmpfunc(ll->key, key)))
	{
	    return ll->payload;
	}
    }

    return NULL;
}

void *misHashDelete(MISHASH *table, char *key)
{
    unsigned long hashval, modval;
    register HASH_LIST *ll;
    void *payload;

    if (!table || !table->slots)
	return NULL;

    /* Look for this key */
    hashval = table->hashfunc(key);
    modval  = hashval % table->slots;
    for (ll=table->list[modval];ll;ll=ll->next)
    {
	/* The key is found.  We need to delete the node. */
	if (ll->key && (ll->hashval == hashval) && 0 == table->cmpfunc(ll->key, key))
	{
	    free(ll->key);
	    if (ll->next)
		ll->next->prev = ll->prev;

	    if (ll->prev)
	    {
		ll->prev->next = ll->next;
	    }
	    else
	    {
		table->list[modval] = ll->next;
	    }
	    payload = ll->payload;
	    free(ll);
	    return payload;
	}
    }
    return NULL;
}

/*
 * Set up the hash table.  Initialize the table, given a certain size.
 */
MISHASH *misHashInit(int slots)
{
    MISHASH *table;
    table = (MISHASH *) malloc(sizeof(MISHASH));

    if (!slots) slots = 1;

    table->slots = slots;
    table->list = calloc(slots, sizeof(HASH_LIST *));
    table->currentslot = 0;
    table->currententry = NULL;
    table->hashfunc = misHash;
    table->cmpfunc = (int (*)(char *, char *))strcmp;
    return table;
}

MISHASH *misCiHashInit(int slots)
{
    MISHASH *table;
    table = misHashInit(slots);
    table->hashfunc = misCiHash;
    table->cmpfunc = (int (*)(char *, char *))misCiStrcmp;
    return table;
}

/*
 * Free up the hash table.
 */
void misHashFree(MISHASH *table)
{
    int ii;

    if (!table)
	return;

    for (ii = 0;ii < table->slots; ii++)
    {
	HASH_LIST *ll,*nextll;

	for(ll=table->list[ii];ll;ll = nextll)
	{
	    if (ll->key)
		free(ll->key);
	    nextll = ll->next;
	    free(ll);
	}
    }
    free(table->list);
    free(table);
}

char *misHashEnum(MISHASH *table, void **payload)
{
    HASH_LIST *tmp;

    if (table)
    {
	while (!table->currententry && table->currentslot < table->slots)
	{
	    table->currententry = table->list[table->currentslot++];
	}

	if (table->currententry)
	{
	    tmp = table->currententry;
	    table->currententry = table->currententry->next;
	    if (payload)
		*payload = tmp->payload;
	    return tmp->key;
	}

	table->currentslot = 0;
	table->currententry = NULL;
	if (payload)
	    *payload = NULL;
    }
    return NULL;
}
