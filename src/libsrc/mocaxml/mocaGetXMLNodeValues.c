static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Command interface to the MOCA XML library.
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
 *#END************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include <common.h>
#include <mocaerr.h>
#include <mocagendef.h>
#include <mxmllib.h>
#include <sqllib.h>
#include <srvlib.h>

#include "mocaxml.h"

static short indent = 0;

typedef struct
{
    char tag[100];
    mocaDataRes *res;
}
mocadatares_info_typ;

static long add_list_to_res(SRV_RESULTS_LIST *reslist, 
	                    RETURN_STRUCT **ret, 
			    long numColumns)
{
    long status = eOK;

    RETURN_STRUCT *tempret = NULL;

    if ((tempret = srvResultsList(eOK, numColumns, reslist)) == NULL)
    {
        status = eNO_MEMORY;
    }
    else
    {
        status = srvCombineResults(ret, &tempret);
    }

    misTrc(T_SRVARGS, "%*s (%d)Adding list %p to the ret %p - status %d",
           indent, " ", indent,
           reslist,
           *ret,
           status);

    return status;
}

/*
 *  FUNCTION: XmlToResultSet
 *
 *  PURPOSE:  
 */

static long XmlToResultSet(mxmlCtxt *ctxt, 
		           mxmlNode *node, 
		           char *catThisSibling, 
		           RETURN_STRUCT **ret)
{
    long ii;
    long status = eOK;
    long numColumns = 0;
    long mocadatares_info_siz = 0;
    char *first_tag = NULL;

    mocadatares_info_typ *mocadatares_info = NULL;

    mxmlNodeDataList *nodeDataList;

    SRV_RESULTS_LIST *reslist = NULL;

    nodeDataList = mxmlGetNodeDataList(ctxt, node, catThisSibling, TRUE);


    indent++;

    misTrc(T_SRVARGS, "%*s (%d) Called XmlToResultSet with node %p", 
	   indent, 
	   " ", 
	   indent, 
	   node);

    if (!nodeDataList)
    {
	status = eSRV_NO_ROWS_AFFECTED;
	goto end_of_function;
    }

    for (ii = 0; ii < nodeDataList->length; ii++)
    {
        long jj;
	short already_counted = FALSE;

	mxmlNode *nodeII, 
		 *nodeJJ;
      
	nodeII = mxmlGetNodeDataListNameNode(ctxt, nodeDataList, ii);
	
	for (jj = 0; jj < ii; jj++)
	{
	    nodeJJ = mxmlGetNodeDataListNameNode(ctxt, nodeDataList, jj);

	    if (strcmp(mxmlGetNodeName(ctxt, nodeII),
		       mxmlGetNodeName(ctxt, nodeJJ)) == 0)
	    {
	        already_counted = TRUE;
		break;
	    }
	}

	if (!already_counted)
	    numColumns++;
    }

    misTrc(T_SRVARGS, "%*s (%d) # columns is = %d, list length %d", 
           indent, " ", indent, 
           numColumns, 
           nodeDataList->length);


    for (ii = 0; ii<nodeDataList->length; ii++)
    {
	char     *valueII;
	mxmlNode *nodeII;


	nodeII  = mxmlGetNodeDataListNameNode(ctxt, nodeDataList, ii);
	valueII = mxmlGetNodeDataListData(ctxt, nodeDataList, ii);

        /*
         * Create the reslist whenever we get the first column
         */
        if (first_tag == NULL || 
            strcmp(first_tag, mxmlGetNodeName(ctxt, nodeII)) == 0)
        {
            misTrc(T_SRVARGS, "%*s (%d) First tag (%s) was null or repeated, new row at col %d",
                   indent, 
		   " ", 
		   indent, 
                   first_tag ? first_tag : "(null)",
                   ii);

            if (first_tag != NULL)
            {
                if ((status = add_list_to_res(reslist, ret, numColumns)) != eOK)
                   goto end_of_function;

                srvFreeResultsList(reslist);
                reslist = NULL;


                misTrc(T_SRVARGS, "%*s (%d) Since first tag repeated, clear out our cache of mocadatares",
                       indent, 
		       " ", 
		       indent);

                if (mocadatares_info)
                {
	            free(mocadatares_info);
	            mocadatares_info = NULL;
	            mocadatares_info_siz = 0;
                }
            }

            reslist = srvCreateResultsList(numColumns);

            /*
             * Save the first_tag
             */
            if ( first_tag == NULL )
            {
                misDynStrcpy ( &first_tag, mxmlGetNodeName(ctxt, nodeII) );
            }
        }

	if (valueII)
	{
            /*
             * We could have a case where our segment had something like
             * <part>xxx</part>
             * <qty>123</qty>
             * <part>xxx</part>
             * <qty>456</qty>
             */
            misTrc(T_SRVARGS, "%*s (%d)1. Node %d, name is %s value is %s", 
                   indent, 
		   " ", 
		   indent, 
                   ii,
                   mxmlGetNodeName(ctxt, nodeII) ? mxmlGetNodeName(ctxt, nodeII) : "(null)",
                   valueII ? valueII : "(null)");

	    srvBuildResultsList(reslist,
				-1, /* -1 here tells the function to lookup by name */
				mxmlGetNodeName(ctxt, nodeII),
				COMTYP_STRING,
				strlen(valueII) + 1, 0,
				valueII);

	}
	else
	{
	    long jj = 0;
	    mocaDataRes *child_res = NULL;
	    RETURN_STRUCT *child_rp = NULL;

            misTrc(T_SRVARGS, "%*s (%d)2.Node %d does not have a value so call recursively", 
                   indent, 
		   " ", 
		   indent,
                   ii);

	    XmlToResultSet(ctxt, nodeII, catThisSibling, &child_rp);

            misTrc ( T_SRVARGS, "%*s (%d)2. Returned resultset %p", 
                     indent, " ", indent,
                     child_rp 
                   );
	    if (child_rp)
	    {
	        if (child_res = srvGetResults(child_rp))
		{
		    mocaDataRes *found_res = NULL;

                    misTrc(T_SRVARGS, "%*s (%d)2. Current size of array of moca res = %d", 
                           indent, 
			   " ", 
			   indent,
                           mocadatares_info_siz);

		    /* find to see if we already did this named col, if so,
		     * this is a new row
		     */
		    for (jj = 0; jj < mocadatares_info_siz; jj++)
		    {
		        if (misCiStrcmp
				(mxmlGetNodeName(ctxt, nodeII),
				 mocadatares_info[jj].tag) == 0)
			{
			    found_res = mocadatares_info[jj].res;
			    break;
		        }
		    }

		    /* if we found an existing res, add to that else a new col */
		    if (found_res)
		    {
                        misTrc(T_SRVARGS, "%*s (%d)2. We found this resultset in the ones we had, add this as a row",
                               indent, 
			       " ", 
			       indent);

		        status = sqlCombineResults(&found_res, &child_res);
		    }
		    else
		    {
                        misTrc(T_SRVARGS, 
                               "%*s (%d)2. We did not find this resultset in the ones we had, "
                               "build it as type res at col %d - name = %s", 
                               indent, 
			       " ", 
			       indent,
                               ii,
                               mxmlGetNodeName(ctxt, nodeII) ? mxmlGetNodeName(ctxt, nodeII) : "(null)");

		        srvBuildResultsList(reslist,
					    -1, /* Lets find it by name */
					    mxmlGetNodeName(ctxt, nodeII),
					    COMTYP_RESULTS,
					    sizeof(mocaDataRes *), 
					    0,
					    child_res);


			mocadatares_info =
			    realloc(mocadatares_info,
				    (mocadatares_info_siz + 1) * sizeof(mocadatares_info_typ));
			strcpy(mocadatares_info[mocadatares_info_siz].tag, mxmlGetNodeName(ctxt, nodeII));
			mocadatares_info[mocadatares_info_siz].res = child_res;

                        misTrc(T_SRVARGS, 
                               "%*s (%d)2. Now add this res (%p) to array of res at %d with tag %s",
                               indent, " ", indent,
                               mocadatares_info[mocadatares_info_siz].res,
                               mocadatares_info_siz,
                               mocadatares_info[mocadatares_info_siz].tag);

			mocadatares_info_siz++;
		    }
		    if (status != eOK)
		        goto end_of_function;
		}/* got child datares */
	        /* if (child_rp) srvFreeMemory(SRVRET_STRUCT, child_rp); */
	    }/* got a child reslist */
        }/* child aggregate */
    }/* next ii - node value */

    /*
     * When get out of the loop add the result that we were building to the ret
     */
    status = add_list_to_res ( reslist, ret, numColumns );

end_of_function:

    if (status == eOK && *ret == NULL )
	status = eSRV_NO_ROWS_AFFECTED;

    if (reslist)
    {
        /* Now we create the resultset above */
	/**ret = srvResultsList(eOK, numColumns, reslist);*/
	srvFreeResultsList(reslist);
    }

    mxmlGetNodeDataListFree(ctxt, &nodeDataList);

    if (mocadatares_info)
    {
	free(mocadatares_info);
	mocadatares_info = NULL;
	mocadatares_info_siz = 0;
    }

    if (first_tag != NULL)
       free(first_tag);

    indent--;
    return status;
}


/*
 *  FUNCTION: mocaGetXMLNodeValues
 *
 *  PURPOSE:  
 */

LIBEXPORT 
RETURN_STRUCT *mocaGetXMLNodeValues(void **inCtxt, 
				    void **inNode, 
				    char *inCatThisSibling)
{
    long status;
    mxmlCtxt *ctxt;
    mxmlNode *node;
    RETURN_STRUCT *ret = NULL;

    ctxt = (inCtxt && *inCtxt) ? *inCtxt : NULL;
    node = (inNode && *inNode) ? *inNode : mxmlGetDocumentElement(ctxt);

    indent = 0;

    status = XmlToResultSet(ctxt, node, inCatThisSibling, &ret);
    if (!ret || status != eOK)
        ret = srvResults(status, NULL);

    return ret;
}
