#include <moca.h>

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <srvlib.h>
#include <sqllib.h>
#include <mislib.h>
#include <mocaerr.h>

LIBEXPORT RETURN_STRUCT *testRetrieveResults(char *name1, char *name2)
{
    void *value = NULL;
    mocaDataRes *res1 = NULL, *res2 = NULL;
    mocaDataRow *row;
    RETURN_STRUCT *results = NULL;
    char dtype;
    long status;
    char buffer[300];

    status = srvGetNeededElement(name1, name1, &dtype, &value);
    if (status != eOK || dtype != COMTYP_RESULTS || value == NULL)
    {
        return srvResults(status, NULL);
    }

    res1 = value;

    status = srvGetNeededElement(name2, name2, &dtype, &value);
    if (status != eOK || dtype != COMTYP_RESULTS || value == NULL)
    {
        return srvResults(status, NULL);
    }

    res2 = value;

    /* Build our results */
    row = sqlGetRow(res1);
    strcpy(buffer, sqlGetString(res1, row, "A"));
    row = sqlGetRow(res2);
    strcat(buffer, sqlGetString(res2, row, "B"));

    results = srvResults(eOK, "result", COMTYP_CHAR, strlen(buffer), buffer, NULL);
    return results;
}
