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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <jni.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <jnilib.h>
#include <sqllib.h>

#include "jniprivate.h"
#include "jnidefs.h"
#include "native.h"

jobject jni_ArgMap(JNIEnv *env, mocaBindList *bindList)
{
    mocaBindList *tmpBp;
    jobject mapObject = (*env)->NewObject(env, 
		                          HashMapClass, 
					  HashMap_constructor);
    if (mapObject == NULL)
    {
        return NULL;
    }

    /* Create a new scope for local references. */
    (*env)->PushLocalFrame(env, 5);

    /*
     * Zip through the passed-in bind list, setting values on the
     * Java BindList object.
     */
    for (tmpBp = bindList; tmpBp; tmpBp= tmpBp->next)
    {
        jstring nameString = (*env)->NewStringUTF(env, tmpBp->name);
        jstring dataString = NULL;
        jobject resultsObject;
	mocaDataRes *resultsPtr;

        if (tmpBp->nullind || !tmpBp->data)
        {
            (*env)->CallObjectMethod(env, 
		                     mapObject, 
				     Map_put,
                                     nameString, 
				     NULL);
        }
        else
        {
            switch (tmpBp->dtype) 
	    {
                case COMTYP_INT:
                case COMTYP_LONG:
                case COMTYP_LONGPTR:
                    (*env)->CallObjectMethod(env, 
			                     mapObject, 
					     Map_put,
                                             nameString, 
                                             jni_NewInteger(env, *((long *) tmpBp->data)));

                    break;

                case COMTYP_FLOAT:
                case COMTYP_FLOATPTR:
                    (*env)->CallObjectMethod(env, 
			                     mapObject, 
					     Map_put,
                                             nameString, 
                                             jni_NewDouble(env, *((double *) tmpBp->data)));
                    break;

                case COMTYP_BOOLEAN:
                    (*env)->CallObjectMethod(env, 
			                     mapObject, 
					     Map_put,
                                             nameString, 
                                             jni_NewBoolean(env, *((long *) tmpBp->data) != 0));
                    break;

                case COMTYP_CHAR:
                case COMTYP_CHARPTR:
                case COMTYP_DATTIM:
                    dataString = 
			jniNewStringFromBytes(env, (char *) tmpBp->data);
                    (*env)->CallObjectMethod(env, 
			                     mapObject, 
					     Map_put,
                                             nameString, 
                                             dataString);
                    break;

                case COMTYP_RESULTS:
		    resultsPtr = *((mocaDataRes **)tmpBp->data);
		    if (resultsPtr)
		    {
			/*
			 * FALSE means that the underlying results
			 * object will not be freed when the WrappedResults
			 * object is closed.  We can do this because the 
			 * argument map is guaranteed to be around longer than
			 * the object on the stack, so we can take
			 * responsibility for freeing it.
			 */
			resultsObject = (*env)->NewObject(env, 
							  WrappedResultsClass, 
							  WrappedResults_constructor, 
							  resultsPtr,
							  JNI_FALSE);
		    }
		    else
		    {
			resultsObject = NULL;
		    }

                    (*env)->CallObjectMethod(env, 
			                     mapObject, 
					     Map_put,
                                             nameString, 
                                             resultsObject);
                    break;

                default:
                    break;
            }
        }
    }

    /* Destroy the current scope for local references, freeing them all. */
    (*env)->PopLocalFrame(env, NULL);

    return mapObject;
}
