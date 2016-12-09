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

#define LOAD_FACTOR 0.75

typedef struct mis_CacheList CACHE_LIST;

struct mis_CacheList
{
    unsigned long hashval;
    char *key;
    void *payload;
    CACHE_LIST *down, *up;
    CACHE_LIST *next, *prev;
};

struct mis_Cache
{
    int maxsize;
    int entries;
    int threshold;

    int tablesize;
    CACHE_LIST **table;

    CACHE_LIST *head, *tail;

    unsigned long (*hashfunc)(char *);
    int (*cmpfunc)(char *, char *);
    void (*freefunc)(void *);

    unsigned int hitCount;
    unsigned int missCount;

    CACHE_LIST *currententry; /* Iterator */
    int iterating;
};

static void *sDeleteNode(MISCACHE *cache, CACHE_LIST *ll, unsigned long modval)
{
    void *payload;

    free(ll->key);

    /* remove this node from the hash table */
    if (ll->down)
        ll->down->up = ll->up;

    if (ll->up)
        ll->up->down = ll->down;
    else
        cache->table[modval] = ll->down;

    /* Remove this node from the LRU list */
    if (ll->next)
        ll->next->prev = ll->prev;
    else
        cache->tail = ll->prev;

    if (ll->prev)
        ll->prev->next = ll->next;
    else
        cache->head = ll->next;

    payload = ll->payload;
    free(ll);

    cache->entries--;

    if (cache->freefunc)
    {
        cache->freefunc(payload);
        return NULL;
    }
    else
    {
        return payload;
    }
}

static void sResize(MISCACHE *cache, int tablesize)
{
    CACHE_LIST *ll;

    /* free the old table ... we're building a new one */
    free(cache->table);
    cache->tablesize = tablesize;
    cache->threshold = (int) (cache->tablesize * LOAD_FACTOR);
    cache->table = calloc(tablesize, sizeof(CACHE_LIST *));
    for(ll=cache->head;ll;ll = ll->next)
    {
        unsigned long hashval, modval;
        hashval = cache->hashfunc(ll->key);
        modval  = hashval % cache->tablesize;

        /* Put it at the top of our hashtable list */
        ll->down = cache->table[modval];
        if (ll->down)
            ll->down->up = ll;
        cache->table[modval] = ll;
        ll->up = NULL;
    }
}

/*
 * These functions are designed to produce the effect of an LRU cache capability
 * in C.  The concept here is one of "keys" and "payloads".  When a cache "put"
 * takes place, the payload is stored, along with the key in the correct hash
 * slot for that key.
 */
long misCachePut(MISCACHE *cache, char *key, void *payload)
{
    unsigned long hashval, modval;
    register CACHE_LIST *ll;

    if (!cache || !cache->tablesize)
        return eERROR;

    /* Look for this key */
    hashval = cache->hashfunc(key);
    modval  = hashval % cache->tablesize;
    for (ll=cache->table[modval];ll;ll=ll->down)
    {
        /*
         * The key is found...we don't want to make a duplicate, and
         * we don't have garbage collection, so return an error.
         */
        if (ll->hashval == hashval && ll->key && 
                0 == cache->cmpfunc(ll->key, key))
        {
            return eERROR;
        }
    }

    /* New Node */
    ll = calloc(1, sizeof(CACHE_LIST));
    ll->key = malloc(strlen(key)+1);
    strcpy(ll->key, key);
    ll->payload = payload;
    ll->hashval = hashval;

    /* Put it at the top of our hashtable list */
    ll->down = cache->table[modval];
    if (ll->down)
        ll->down->up = ll;
    cache->table[modval] = ll;
    ll->up = NULL;

    /* Put it at the top of our LRU linked list too */
    ll->next = cache->head;
    if (ll->next)
        ll->next->prev = ll;
    cache->head = ll;
    ll->prev = NULL;

    if (!cache->tail) 
        cache->tail = ll;

    cache->entries++;
    if (cache->maxsize > 0 && cache->entries > cache->maxsize)
    {
        hashval = cache->hashfunc(cache->tail->key);
        modval  = hashval % cache->tablesize;
        sDeleteNode(cache, cache->tail, modval);
    }

    if (cache->entries > cache->threshold)
    {
        sResize(cache, cache->tablesize * 2);
    }

    return eOK;
}

void *misCacheGet(MISCACHE *cache, char *key)
{
    unsigned long hashval, modval;
    register CACHE_LIST *ll;

    if (!cache || !cache->tablesize)
        return NULL;

    /* Look for this key */
    hashval = cache->hashfunc(key);
    modval  = hashval % cache->tablesize;
    for (ll=cache->table[modval];ll;ll=ll->down)
    {
        /* The key is found.  We need to return the payload. */
        if (ll->key && (ll->hashval == hashval) && (0 == cache->cmpfunc(ll->key, key)))
        {
            /* Move this node to the front of the list */
            if (ll->prev)
            {
                ll->prev->next = ll->next;

                if (ll->next)
                    ll->next->prev = ll->prev;
                else
                    cache->tail = ll->prev;

                ll->prev = NULL;
                ll->next = cache->head;
                if (ll->next)
                    ll->next->prev = ll;
                cache->head = ll;
            }

            cache->hitCount++;
            return ll->payload;
        }
    }

    cache->missCount++;
    return NULL;
}

void *misCacheDelete(MISCACHE *cache, char *key)
{
    unsigned long hashval, modval;
    register CACHE_LIST *ll;

    if (!cache || !cache->tablesize)
        return NULL;

    /* Look for this key */
    hashval = cache->hashfunc(key);
    modval  = hashval % cache->tablesize;
    for (ll=cache->table[modval];ll;ll=ll->down)
    {
        /* The key is found.  We need to delete the node. */
        if (ll->key && (ll->hashval == hashval) && 0 == cache->cmpfunc(ll->key, key))
        {
            return sDeleteNode(cache, ll, modval);
        }
    }
    return NULL;
}

static void myfree(void *ptr)
{
    free(ptr);
}

/*
 * Set up the cache.  Initialize the hash table, given a certain size.
 */
MISCACHE *misCacheInit(int maxsize, void (*freefunc)(void *))
{
    MISCACHE *cache;
    cache = (MISCACHE *) calloc(1, sizeof(MISCACHE));

    cache->tablesize = 16;
    cache->threshold = (int) (cache->tablesize * LOAD_FACTOR);
    cache->maxsize = maxsize;
    cache->entries = 0;
    if (!freefunc) freefunc = myfree;
    cache->freefunc = freefunc;
    cache->table = calloc(cache->tablesize, sizeof(CACHE_LIST *));
    cache->head = cache->tail = NULL;
    cache->currententry = NULL;
    cache->iterating = 0;
    cache->hashfunc = misHash;
    cache->cmpfunc = (int (*)(char *, char *))strcmp;
    return cache;
}

/*
 * Get the cache hit count
 */
unsigned int misCacheHits(MISCACHE *cache)
{
    return cache->hitCount;
}

/*
 * Get the cache miss count
 */
unsigned int misCacheMisses(MISCACHE *cache)
{
    return cache->missCount;
}

/*
 * Get the cache size
 */
int misCacheSize(MISCACHE *cache)
{
    return cache->entries;
}

/*
 * Clear the cache counters
 */
void misCacheResetStats(MISCACHE *cache)
{
    cache->hitCount = 0;
    cache->missCount = 0;
}

/*
 * Free up the cache.
 */
void misCacheFree(MISCACHE *cache)
{
    CACHE_LIST *ll,*nextll;

    if (!cache)
        return;

    for(ll=cache->head;ll;ll = nextll)
    {
        if (ll->key)
            free(ll->key);
        if (cache->freefunc)
            cache->freefunc(ll->payload);
        nextll = ll->next;
        free(ll);
    }

    free(cache->table);
    free(cache);
}
