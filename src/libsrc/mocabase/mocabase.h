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
 *  Copyright (c) 20168
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

#ifndef MOCABASE_H
#define MOCABASE_H

/*
 *  The following are used by all components in the MOCA base component library.
 */

#define MOCAMissingArg(argid) \
        srvErrorResults(eMOCA_MISSING_ARG, \
                        "^errnum^ - " \
                        "Missing argument:  ^argdsc^ (^argid^)", \
                        "errnum", COMTYP_INT, eMOCA_MISSING_ARG, 0, \
                        "argdsc", COMTYP_CHAR, argid, 1, \
                        "argid", COMTYP_CHAR, argid, 0, \
                        NULL)

#define MOCAInvalidArg(name) \
        srvErrorResults(eINVALID_ARGS, \
			"Invalid value for argument '^name^'", \
                        "name", COMTYP_CHAR, name, 0, \
                        NULL)

/*
 * Function Prototypes
 */

RETURN_STRUCT *mocaPublishData(void);

#endif
