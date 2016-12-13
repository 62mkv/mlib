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

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>
#include <sqllib.h>

#include "srvprivate.h"

/* Global Structures and Variables */

/*
 * The COLUMN structure contains elements that we are storing for
 * each of the columns in the final result set.
 */

typedef struct column
{
    long columnPos;        /* The position of the column in the result set */
    long length;           /* The defined length of the column */
    char dtype;            /* The data type of the column */
    moca_bool_t desc_flg;  /* Sort descending? */
    struct column *next;   /* A pointer to the next column */
} column;

/*
 * The SORT_ELEMENT structure defines an array element that will be
 * used for sorting.
 */

typedef struct sort_element
{
    column *columnHead;
    mocaDataRes *res;
    mocaDataRow *row;   /* A pointer to the row from the passed in result set */
} sort_element;

/*
 *  Function:  sCompareDataColumns
 *
 *  Purpose:   Comparison function used by qsort. This function will
 *             loop through all of the defined sort columns in the
 *             2 rows passed into the function.
 *
 *  Inputs:    sortArray1_i,
 *             sortArray2_i - Pointers to structures that represent 1 row
 *                            in the array we are sorting.
 *
 *  Returns:   -1, 0 or 1 depending on whether the values of the keys
 *             in the first element is alphabetically lower, equal to or
 *             greater than the second element.
 */

static int sCompareDataColumns (sort_element *sortArray1_i,
                                sort_element *sortArray2_i)
{
    column *curNode;
    mocaDataRow *row1;
    mocaDataRow *row2;
    mocaDataRes *res;
    int result;
    long longValue1;
    long longValue2;
    double floatValue1;
    double floatValue2;
    long is_null1;
    long is_null2;

    res = sortArray1_i->res;
    row1 = sortArray1_i->row;
    row2 = sortArray2_i->row;
    result = 0;

    /* Loop on all of the columns and perform the correct
     * comparison to determine which row is greatest.
     */
    for (curNode = sortArray1_i->columnHead;
         curNode && result == 0;
         curNode = curNode->next)
    {
        /* Determine if either of the values for the current column
         * is NULL.
         * The following logic is used to compare columns with NULLS
         *    - NULL = NULL
         *    - NULL < any value
         */
        is_null1 = sqlIsNullByPos(res, row1, curNode->columnPos);
        is_null2 = sqlIsNullByPos(res, row2, curNode->columnPos);

        if (is_null1 || is_null2)
        {
            if (is_null1 && is_null2)
            {
                /* They are both NULL so it is equal */
                result = 0;
            }
            else if (is_null1)
            {
                /* Only the first value is NULL */
                result = -1;
            }
            else
            {
                /* Only the second value is NULL */
                result = 1;
            }
        }
        else
        {
            /*
             * No values are NULL so we need to compare the 2 values.
             * Use the correct comparison depending on the data type.
             */
            switch (curNode->dtype)
            {
                case COMTYP_STRING:
                case COMTYP_DATTIM:

                    /*
                     * The function strcmp will return the correct status to
                     * determine which value is sorted in front of the other.
                     * -1 - The first value is less than the second value.
                     *  0 - The values are equal
                     *  1 - The first value is greater than the second value.
                     */
                    result = strcmp(sqlGetStringByPos(res, row1, curNode->columnPos),
                                    sqlGetStringByPos(res, row2, curNode->columnPos));

                    break;
                case COMTYP_INT:
                case COMTYP_LONG:
                case COMTYP_BOOLEAN:

                    longValue1 = sqlGetLongByPos(res, row1, curNode->columnPos);
                    longValue2 = sqlGetLongByPos(res, row2, curNode->columnPos);

                    if (longValue1 < longValue2)
                        result = -1;
                    else if (longValue1 > longValue2)
                        result = 1;

                    break;
                case COMTYP_FLOAT:

                    floatValue1 = sqlGetFloatByPos(res, row1, curNode->columnPos);
                    floatValue2 = sqlGetFloatByPos(res, row2, curNode->columnPos);

                    if (floatValue1 < floatValue2)
                        result = -1;
                    else if (floatValue1 > floatValue2)
                        result = 1;

                    break;
                default:
                    result = 0;
            }
        }

        /*
         * If we are doing a descending sort on this column
         * then multiply the result by -1 to flip the sort
         * order of the row.
         */
        if (curNode->desc_flg)
            result *= -1;
    }

    return (result);
}

/*
 *  Function:  sFreeColumnList
 *
 *  Purpose:   This function will free all of the allocated memory used to
 *             build the linked list of columns. Each node contains pointers
 *             to allocated memory that must be freed and the node itself
 *             needs to be freed.
 *
 *  Inputs:    columnHead_i - Pointer to the head of the linked list that
 *                            needs to be freed.
 */

static void sFreeColumnList(column *columnHead_i)
{
    column *curNode;
    column *nextNode;

    /* Loop through all nodes in the passed in linked list */
    for (curNode = columnHead_i; curNode; curNode = nextNode)
    {
        /* Save a pointer to the next node */
        nextNode = curNode->next;

        /* Free the node */
        free(curNode);
    }
    return;
}

/*
 *  Function:  sFreeArray
 *
 *  Purpose:   This function will free all of the allocated memory used to
 *             build the dynamic array used for sorting purposes. If an
 *             element of the array contains a pointer to allocated memory
 *             then it must be freed and the memory for the array itself
 *             needs to be freed.
 *
 *  Inputs:    sortArray_i   - Pointer to the beginning of the array that will
 *                             be freed.
 *             row_count_i   - The number of elements contained in the array.
 */

static void sFreeArray(sort_element *sortArray_i, long row_count_i)
{
    /*
     * Loop on all rows in the array and free any
     * memory that was allocated for the elements
     * of the array.
     *
     * NOTE: This array does not have any elements
     *       that need it's memory freed.
     */

    /* Free the dynamically allocated array space. */
    free(sortArray_i);

    return;
}

/*
 *  Function:  srvSortResultSet
 *
 *  Purpose:   This will take any result set and sort the rows according
 *             to the colums passed in a "sort" list.
 *
 *  Inputs:    ret      - A pointer to a result set.
 *                        This is the result set that will be sorted.
 *
 *             sortList - A string that represents the columns that
 *                        will be used for sorting. The format
 *                        of this string must be:
 *
 *                        <column name>[ D...][,<column name>[ D...]]...
 *
 *                        Where <column name> must be a valid column
 *                        in the passed in result set and D..., a word that
 *                        starts with "D", specifies descending ordering.
 *                        Ascending is default.
 *
 *  Returns:   eOK, eINVALID_ARGS, eERROR
 */

long srvSortResultSet(mocaDataRes *res, char *sortList)
{
    mocaDataRow *row;

    typedef int (*compfn) (const void*, const void*);

    column *columnHead;
    column *lastNode;
    column *curNode;

    sort_element *sortArray = NULL;

    long columnPos;
    long rowCount;
    long arrayCount;
    long ii;

    char *tempList = NULL;
    char *sListStart = NULL;
    char *curColumn = NULL;
    char *curDirection = NULL;

    columnHead = NULL;
    lastNode = NULL;
    curNode = NULL;


    /* Validate the input parameters */
    if (!res)
        return eINVALID_ARGS;

    misDynStrcpy(&tempList, sortList);
    sListStart = tempList;
    if (!tempList || strlen(misTrimLR(tempList)) == 0)
        return eINVALID_ARGS;

    /*
     * Parse out the list of columns passed in the Sort List and store
     * them in a linked list.
     * The dilimiter character is ",".
     */
    while ((curColumn = misTrimLR(misStrsep(&tempList, ","))) != NULL)
    {
        /*
         * If the user has specified a direction (ASC or DESC)
         * for the sort then it will be directly after the column
         * name but before the delimiter for the next column.
         */
        curDirection = curColumn;

        /*
         * The delimiter character for the sort direction
         * is a space " ". If there is a space after the
         * column name and some text between the space and
         * the next "," then the curColumn variable will
         * be pointing to the sort direction.
         */
        curColumn = misTrimLR(misStrsep(&curDirection, " "));
        columnPos = sqlFindColumn(res, curColumn);

        /* Validate column name */
        if (-1 == columnPos)
        {
            sFreeColumnList(columnHead);
            return eERROR;
        }

        /* Allocate memory for the new node */
        curNode = calloc(1, sizeof(column));

        curNode->columnPos = columnPos;
        curNode->dtype = sqlGetDataType(res, curColumn);
        curNode->length = sqlGetDefinedColumnLen(res, curColumn);
        curNode->next = NULL;

        /*
         * Determine if this is an ascending or descending
         * sort. If they specify "D"ecsending then it will
         * be descending. Anything else will default to
         * ascending.
         * We aren't trying to completely error proof the
         * sort direction values. Basically any string
         * starting with a "D" or "d" will cause a descending
         * sort. If nothing is specified or a string starting
         * with any other character we will default to
         * the ascending sort.
         */
        if (curDirection &&
            !misTrimIsNull(curDirection, 1) &&
            misCiStrncmp("D", curDirection, 1) == 0)
        {
            /* Descending sort */
            curNode->desc_flg = MOCA_TRUE;
        }
        else
        {
            /* Ascending sort */
            curNode->desc_flg = MOCA_FALSE;
        }

        /* Add the new node to the linked list */
        if (columnHead)
        {
            lastNode->next = curNode;
            lastNode = curNode;
        }
        else
        {
            columnHead = curNode;
            lastNode = curNode;
        }
    }

    /* free the temporary column list. */
    free(sListStart);
    sListStart = NULL;
    tempList = NULL;

    /* Build an array of the pointers to the data rows */
    rowCount = sqlGetNumRows(res);

    /* Define the memory space for the array */
    sortArray = calloc(rowCount, sizeof(sort_element));

    for (row = sqlGetRow(res), arrayCount = 0;
         row;
         row = sqlGetNextRow(row), arrayCount++)
    {
        sortArray[arrayCount].columnHead = columnHead;
        sortArray[arrayCount].res = res;
        sortArray[arrayCount].row = row;
    }

    /*
     * Sort the result set by the columns defined in the Sort List
     *
     * sortArray                --> The array of data rows we are sorting
     * arrayCount               --> The number of rows in the result set
     * sizeof (sort_element)    --> The size of one element of the structure
     * sCompareDataColumns      --> The function that will compare the columns
     */
    qsort ((void*) sortArray,
           (size_t) rowCount,
           sizeof (sort_element),
           (compfn) sCompareDataColumns );

    /* Restore the first row, next row pointers, and last row of res */
    res->Data = sortArray[0].row;
    for (ii = 0; ii < rowCount - 1; ii++)
    {
        sortArray[ii].row->NextRow = sortArray[ii+1].row;
    }
    sortArray[ii].row->NextRow = NULL;
    res->LastRow = sortArray[ii].row;

    /* Cleanup our linked list of columns */
    sFreeColumnList(columnHead);

    /* Cleanup the memory used by the array */
    sFreeArray(sortArray, rowCount);

    return eOK;
}
