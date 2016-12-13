static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: MOCA mailbox administrator.
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

#define APPNAME	"MOCA Mailbox Administrator"

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mislib.h>
#include <oslib.h>


/*
 *  Function prototypes
 */

static void Usage(void);
static long ListMailboxes(void);
static long RemoveMailbox(char *mbxname);


/*
 *  FUNCTION: main
 *
 *  PURPOSE:  See above.
 */

int main(int argc, char *argv[])
{
    int   c,
          flagList,
	  flagRemove;
    char *mbxname;

    extern char *osOptarg;

    /* Initialize variables. */
    flagList = 0; flagRemove = 0;

    /* Get command line arguments. */
    while ((c = osGetopt(argc, argv, "hlr:v?")) != -1)
    {
        switch(c)
	{
	case 'l':
	    flagList++;
	    break;
	case 'r':
	    flagRemove++;
	    mbxname = osOptarg;
	    break;
	case 'v':
	    printf(misGetVersionBanner(APPNAME));
	    exit(EXIT_SUCCESS);
	case '?':
	case 'h':
	default:
	    Usage( );
	    exit(EXIT_FAILURE);
	}
    }

    /* Check command line arguments. */
    if ((!flagList  && !flagRemove) ||
        (flagList   && flagRemove)  || 
	(flagRemove && !mbxname))
    {
    	Usage( );
	exit(EXIT_FAILURE);
    }

    printf(misGetStartBanner(APPNAME));

    /* List mailboxes if requested. */
    if (flagList && ListMailboxes( ) != eOK)
	exit(EXIT_FAILURE);

    /* Remove mailbox if requested. */
    if (flagRemove && RemoveMailbox(mbxname) != eOK)
	exit(EXIT_FAILURE);

    exit(EXIT_SUCCESS);
}

/*
 *  FUNCTION: Usage
 *
 *  PURPOSE:  Display command line usage.
 *
 *  RETURNS:  void
 */

static void Usage(void)
{
   
    printf("Usage: mbxadmin [ -l ]\n"
	   "                [ -r <mbxname> ]\n"
           "\t-l             List all mailboxes\n"
	   "\t-r <mbxname>   Remove mailbox\n"
	   "\t-h             Show help\n"
	   "\t-v             Show version information\n");

    return;
}


/*
 *  FUNCTION: ListMailboxes
 *
 *  PURPOSE:  Display a list of existing mailboxes.
 */

static long ListMailboxes(void)
{
    int ii = 0;
    OS_MBX_LIST *mbxList;

    /* Get the list of existing mailboxes. */
    if ((mbxList = osMBXGetList( )) == NULL)
    {
	printf("Could not get mailbox list: %s\n", osError());
	return(eERROR);
    }

    printf("No. Id         Name                                \n");
    printf("--- ---------- --------------------------------\n");
    while (mbxList[ii].mbxname[0] != '\0')
    {
        printf("%3d %-*s %-*s\n", (ii+1), 
	                          OS_QUEUEID_LEN,
	                          mbxList[ii].queue_id, 
	                          OS_MBXNAME_LEN,
	                          mbxList[ii].mbxname);
	ii++;
    }
    if (ii == 0)
    {
        printf("No mailboxes currently exist\n");
    }

    return(eOK);
}


/*
 *  FUNCTION: RemoveMailbox
 *
 *  PURPOSE:  Remove a mailbox from the system.
 */

static long RemoveMailbox(char *mbxname)
{
    long status;

    status = osMBXKill(mbxname);
    if (status != eOK)
    {
	printf("Could not remove mailbox %s: %s\n", mbxname, osError());
    }
    else
    {
	printf("Removed mailbox %s\n", mbxname);
    }

    return(status);
}
