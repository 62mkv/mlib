#include <moca.h>

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <srvlib.h>
#include <mislib.h>
#include <mocaerr.h>

static char data[] = "This is a test, y'all";

LIBEXPORT RETURN_STRUCT *testRetrievePointer(char *arg)
{
    void *value = NULL;
    RETURN_STRUCT *results = NULL;
    char dtype;
    long status;
    char buffer[300];

    status = srvGetNeededElement(arg, arg, &dtype, &value);
    if (status == eOK && dtype == COMTYP_GENERIC && value != NULL)
    {
        misTrc(T_FLOW, "  ARG =%p", value);
        misTrc(T_FLOW, "(*ARG)=%p", *((void **)value));
        misTrc(T_FLOW, "(*ARG)=[%s]", *((char **)value));
        sprintf(buffer, "DATA=%p, ARG=%p, (*ARG)=%p", data, value, *((void **)value));
    }
    else
    {
        sprintf(buffer, "VALUE is NULL");
    }

    results = srvResults(eOK, "result", COMTYP_CHAR, strlen(buffer), buffer, NULL);
    return results;
}

LIBEXPORT RETURN_STRUCT *testPublishPointer(char *arg)
{
    RETURN_STRUCT *results = NULL;

    results = srvResults(eOK, arg, COMTYP_GENERIC, sizeof(void *), data, NULL);

    return results;
}

LIBEXPORT RETURN_STRUCT *testTakePointerArgument(void **value)
{
    RETURN_STRUCT *results = NULL;
    char buffer[300];

    if (value != NULL)
    {
        misTrc(T_FLOW, "  ARG =%p", value);
        misTrc(T_FLOW, "(*ARG)=%p", *((void **)value));
        misTrc(T_FLOW, "(*ARG)=[%s]", *((char **)value));
        sprintf(buffer, "DATA=%p, ARG=%p, (*ARG)=%p", data, value, *((void **)value));
    }
    else
    {
        sprintf(buffer, "VALUE is NULL");
    }

    results = srvResults(eOK, "result", COMTYP_CHAR, strlen(buffer), buffer, NULL);
    return results;
}

LIBEXPORT RETURN_STRUCT *testEnumeratePointer(char *arg)
{
    SRV_ARGSLIST *args;
    void *value = NULL;
    RETURN_STRUCT *results = NULL;
    char dtype;
    char buffer[300];
    char name[100];
    int oper;

    args = NULL;
    buffer[0] = '\0';

    while (eOK == srvEnumerateAllArgs(&args, name, &oper, &value, &dtype))
    {
        if (dtype == COMTYP_GENERIC && value != NULL)
        {
            misTrc(T_FLOW, "  ARG =%p", value);
            misTrc(T_FLOW, "(*ARG)=%p", *((void **)value));
            misTrc(T_FLOW, "(*ARG)=[%s]", *((char **)value));
            sprintf(buffer, "DATA=%p, ARG=%p, (*ARG)=%p", data, value, *((void **)value));
        }
    }

    results = srvResults(eOK, "result", COMTYP_CHAR, strlen(buffer), buffer, NULL);

    srvFreeArgList(args);
    return results;
}

