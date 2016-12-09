/*#START***********************************************************************
 *
 *
 *  Description: Component to get logical drives.
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2004
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
 *#END************************************************************************/
#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <oslib.h>
#include <srvlib.h>

LIBEXPORT
RETURN_STRUCT *mocaListDiskDrives()
{
    RETURN_STRUCT *ret = NULL;
    char * drive = NULL;
    long len = 0;    
    char buf[1000] = "";

    memset(buf, 0, sizeof(buf));

    ret = srvResultsInit(eOK, 
                        "drive_name",   COMTYP_CHAR, 4,
                        "drive_type",   COMTYP_STRING, 20,
                        "drive_type_descr", COMTYP_STRING, 100,
                         NULL);
	
#ifdef WIN32 
    /* If the system is windows,then get logical drives ,
       else return -1403(No Rows found). */

    len = (GetLogicalDriveStrings(sizeof(buf)/sizeof(char),buf))/4;
   
    for ( drive = buf; strlen(drive) > 0 ; drive += 4)
    { 
        char * descr = NULL;
        char * type = NULL;

	long ltype = GetDriveType(drive);

        switch(ltype)
        {
           case DRIVE_NO_ROOT_DIR:
              type = "DRIVE_NO_ROOT_DIR";
              descr = "The root path is invalid."
                      " For example, no volume is mounted at the path.";
              break;
           case DRIVE_REMOVABLE:
              type = "DRIVE_REMOVABLE";
              descr = "The disk can be removed from the drive."; 
              break;
           case DRIVE_FIXED:
              type = "DRIVE_FIXED";
              descr = "The disk cannot be removed from the drive.";
              break;
           case DRIVE_REMOTE:
              type = "DRIVE_REMOTE";
              descr = "The drive is a remote (network) drive.";
              break;
           case DRIVE_CDROM:
              type = "DRIVE_CDROM";
              descr = "The drive is a CD-ROM drive.";
              break;
           case DRIVE_RAMDISK:
              type = "DRIVE_RAMDISK";
              descr = "The drive is a RAM disk.";
              break;
           case DRIVE_UNKNOWN: /* fall through */
           default:
              type = "DRIVE_UNKNOWN";
              descr = "The drive type cannot be determined.";
              break;
        }
              

        srvResultsAdd(ret, drive, 
                           type ? type : "", 
                           descr ? descr : "", 
                           NULL);
    }
	return ret;
#else
	return srvResults(eDB_NO_ROWS_AFFECTED, NULL);	
#endif
}	

