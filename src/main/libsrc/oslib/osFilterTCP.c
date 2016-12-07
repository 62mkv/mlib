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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include <mocaerr.h>

#include "osprivate.h"

#define RULE_ALLOW 1
#define RULE_DENY  2
#define RULE_LOG   4
#define RULE_SOFT  8

#define ALLOWDENY_SET (RULE_ALLOW | RULE_DENY)
#define OPTIONS_SET (RULE_LOG)

#define MAX_RULES 40
#define MAX_SETS  10

typedef struct
{
    char Name[50];
    struct
    {
	long RuleType;
	int octet_begin[4];
	int octet_end[4];
    } Rules[MAX_RULES];
    int nrules;
} FILTER_RULESET;

static FILTER_RULESET Set[MAX_SETS];
static int nsets;

static void os_LogConnection(char *connection, int Rule)
{
    fprintf(stderr, "Connection from %s %sed\n", connection,
	    (Rule&RULE_ALLOW)?"Allow":(Rule & RULE_DENY)?"Deni":"Logg");
}
/*
 * Rules are formatted as follows:
 *     IP_ADDRESS TYPE
 *     Where IP_ADDRESS is a dotted-decimal format IP address (or range),
 *     and TYPE is a comma-separated list of rule types ({ALLOW|DENY|LOG|SOFT})
 *     which are OR'd together.  Some combinations (ALLOW,DENY) are
 *     meaningless.  The Na
 */
long osFilterTCPAdd(char *ruleset, char *ruletext)
{
    char *typepos;
    char *p, *start;
    char octet[100];
    char tmptype[100];
    int  onum;
    int  nfound;
    int  nrules;
    int  s,i;

    /*
     * Find the RuleSet with this name on it.
     */
    for(s=0;s<nsets;s++)
    {
	if (strcmp(Set[s].Name,ruleset) >= 0)
	    break;
    }

    /*
     * If we don't have one, put it in.
     */
    if (s >= nsets || 0 != strcmp(Set[s].Name, ruleset))
    {
	if (nsets >= MAX_SETS)
	    return eERROR;

	for (i=nsets;i > s; i--)
	{
	    Set[i] = Set[i-1];
	}

	strncpy(Set[s].Name, ruleset, sizeof Set[s].Name-1);
	Set[s].nrules = 0;

	nsets++;
    }

    if (Set[s].nrules >= MAX_RULES)
	return eERROR;

    nrules = Set[s].nrules;

    Set[s].Rules[nrules].RuleType = 0;
    if ((typepos = strpbrk(ruletext, " \t")))
    {
	typepos += strspn(typepos, " \t");

	strncpy(tmptype, typepos, sizeof tmptype);
	tmptype[sizeof tmptype - 1] = '\0';

	for(p=tmptype; *p; p++)
	{
	    *p = tolower(*p);
	}

	if (strstr(tmptype, "allow"))
	    Set[s].Rules[nrules].RuleType |= RULE_ALLOW;
	if (strstr(tmptype, "deny"))
	    Set[s].Rules[nrules].RuleType |= RULE_DENY;
	if (strstr(tmptype, "log"))
	    Set[s].Rules[nrules].RuleType |= RULE_LOG;
	if (strstr(tmptype, "soft"))
	    Set[s].Rules[nrules].RuleType |= RULE_SOFT;
    }

    start = ruletext + strspn(ruletext, " \t");
    onum = 0;

    for (p = start; ; p++)
    {
	if (*p == '.' || *p == ' ' || *p == '\t' || !*p)
	{
	    octet[p-start] = '\0';
	    start = p+1;
	    nfound = sscanf(octet, "%d-%d", &Set[s].Rules[nrules].octet_begin[onum],
			    &Set[s].Rules[nrules].octet_end[onum]);
	    switch(nfound)
	    {
	    case 2:
		break;
	    case 1:
		Set[s].Rules[nrules].octet_end[onum] = Set[s].Rules[nrules].octet_begin[onum];
		break;
	    case 0:
		if (0 == strcmp(octet, "*"))
		{
		    Set[s].Rules[nrules].octet_begin[onum] = 0;
		    Set[s].Rules[nrules].octet_end[onum] = 255;
		}
		break;
	    }
	    onum ++;
	
	    if (onum >= 4 || *p == ' ' || *p == '\t' || !*p)
		break;
	}
	else
	{
	    octet[p-start] = *p;
	}
    }

    for (;onum < 4;onum++)
    {
	Set[s].Rules[nrules].octet_begin[onum] = 0;
	Set[s].Rules[nrules].octet_end[onum] = 255;
    }

    Set[s].nrules ++;
    return eOK;
}

long osFilterTCP(SOCKET_FD fd)
{
    int octet[4];
    char addr[20];
    unsigned short port;
    int defrule = RULE_ALLOW;
    int options = 0;
    int s,r;

    if (!nsets)
	return 1;

    osSockAddress(fd, addr, sizeof addr, &port);

    if (4 != sscanf(addr, "%d.%d.%d.%d",
		    &octet[0], &octet[1], &octet[2], &octet[3]))
	return 0;
    
    for (s=0;s<nsets;s++)
    {
	for (r=0;r<Set[s].nrules;r++)
	{
	    if (octet[0] >= Set[s].Rules[r].octet_begin[0] &&
		octet[0] <= Set[s].Rules[r].octet_end[0] &&
		octet[1] >= Set[s].Rules[r].octet_begin[1] &&
		octet[1] <= Set[s].Rules[r].octet_end[1] &&
		octet[2] >= Set[s].Rules[r].octet_begin[2] &&
		octet[2] <= Set[s].Rules[r].octet_end[2] &&
		octet[3] >= Set[s].Rules[r].octet_begin[3] &&
		octet[3] <= Set[s].Rules[r].octet_end[3])
	    {
		if (Set[s].Rules[r].RuleType == RULE_LOG)
		{
		    options |= RULE_LOG;
		}
		else if (Set[s].Rules[r].RuleType & RULE_SOFT)
		{
		    defrule = Set[s].Rules[r].RuleType & ALLOWDENY_SET;
		    options |= Set[s].Rules[r].RuleType & OPTIONS_SET;
		}
		else
		{
		    options |= Set[s].Rules[r].RuleType & OPTIONS_SET;
		    if (options & RULE_LOG)
			os_LogConnection(addr, Set[s].Rules[r].RuleType);
		    return(Set[s].Rules[r].RuleType & RULE_ALLOW);
		}
	    }
	}
    }

    if (options & RULE_LOG)
	os_LogConnection(addr, defrule);
    return(defrule & RULE_ALLOW);
}

long osFilterTCPClear(char *ruleset)
{
    int s,i;

    if (strcmp(ruleset, "ALL") == 0)
    {
	nsets = 0;
	return eOK;
    }

    for (s=0;s<nsets;s++)
    {
	if (strcmp(Set[s].Name,ruleset) == 0)
	{
	    for (i=s+1; i<nsets; i++)
	    {
		Set[i-1] = Set[i];
	    }
	    nsets--;
	    return eOK;
	}
    }
    return eERROR;
}

char *osFilterTCPList(int n)
{
    static char RuleText[500];
    int i,j;

    for (j=0;j<nsets;j++)
    {
	for (i=0;i<Set[j].nrules;i++)
	{
	    if (!n--)
	    {
		sprintf(RuleText, "%s: %d.%d.%d.%d - %d.%d.%d.%d - %s%s%s%s",
		        Set[j].Name,
		        Set[j].Rules[i].octet_begin[0],
		        Set[j].Rules[i].octet_begin[1],
		        Set[j].Rules[i].octet_begin[2],
		        Set[j].Rules[i].octet_begin[3],
		        Set[j].Rules[i].octet_end[0],
		        Set[j].Rules[i].octet_end[1],
		        Set[j].Rules[i].octet_end[2],
		        Set[j].Rules[i].octet_end[3],
		        (Set[j].Rules[i].RuleType & RULE_ALLOW)?"ALLOW ":"",
		        (Set[j].Rules[i].RuleType & RULE_DENY)?"DENY ":"",
		        (Set[j].Rules[i].RuleType & RULE_SOFT)?"SOFT ":"",
		        (Set[j].Rules[i].RuleType & RULE_LOG)?"LOG ":"");
		return RuleText;
	    }
	}
    }
    return NULL;
}
