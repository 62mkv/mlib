static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL: https://athena.redprairie.com/svn/prod/moca/trunk/src/libsrc/mislib/mi
sGetSleepPassword.c $
 *  $Revision$
 *  $Author$
 *
 *  Description: 
 *
 *  $Copyright-Start$
 *
 *  Copyright (c) 2012
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
 *#END************************************************************************/

#include <moca.h>

#ifdef WIN32
#include <windows.h>
#endif

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <oslib.h>
#include <srvlib.h>
#include <madlib.h>


static void set_os_version(void)
{
#ifdef WIN32
    madStringSet("com.redprairie.moca", "OS", "version", NULL,
            "Windows");
#else

    FILE *file = NULL;
    char buf[1024] = {'\0'};

    file = popen("uname -a", "r");

    if (file != NULL)
    {
        if (fgets(buf, sizeof(buf), file) != NULL)
        {
            madStringSet("com.redprairie.moca", 
                    "OS", 
                    "version", 
                    NULL, 
                    buf);

        }

        pclose(file);
    }
#endif
}

static void set_last_pid_used(void)
{
#ifdef UNIX
    madIntegerSet("com.redprairie.moca", "C", "last-native-pid", NULL,
            (unsigned int)getpid());
#endif
}

LIBEXPORT 
RETURN_STRUCT *testMad(long *duration)
{
    RETURN_STRUCT *results = NULL;
    MadTimeInterval interval;
    static int on = 1;

    madBooleanSet("madTest", "boolean", "true", NULL, MOCA_TRUE);
    madBooleanSet("madTest", "boolean", "false", NULL, MOCA_FALSE);
    madIntegerSet("madTest", "integer", "five", NULL, 5);
    madFloatSet("madTest", "float", "π", NULL, 3.141592654f);
    madStringSet("madTest", "string", "hello-word", NULL, "Hello world");
    
    madStringSet("madTest", "façade", "résumé", NULL, 
            "Répondez s'il vous plaît.");

    madCounterInc("madTest", "counter", "three", NULL, 1);
    madCounterInc("madTest", "counter", "three", NULL, 1);
    madCounterInc("madTest", "counter", "three", NULL, 1);
    madCounterInc("madTest", "counter", "three", NULL, 1);
    madCounterDec("madTest", "counter", "three", NULL, 1);

    madHistogramAdd("madTest", "histogram", "uniform", NULL,
            MOCA_FALSE, 1);
    madHistogramAdd("madTest", "histogram", "biased", NULL,
            MOCA_TRUE, 1);
   
    madMeterMark("madTest", "meter", "meter", NULL, MAD_SECONDS, 1);

    madTimeIntervalClear(&interval);
    madTimeIntervalSetSeconds(&interval, 1);

    madTimerAdd("madTest", "timer", "timer", NULL, MAD_SECONDS,
            MAD_SECONDS, &interval);

    set_os_version();
    set_last_pid_used();

    madNotify("key", "value");

    if (on) {
        madCounterDec("madTest", "delete", "counter", NULL, -40);
        madCounterDec("madTest", "delete", "counter1", "tree", -40);
        madCounterDec("madTest", "delete", "counter2", "tree", -40);
        madCounterDec("madTest", "delete", "counter3", "tree", -40);
    } else {
        madDelete("madTest", "delete", "counter", NULL);
        madDeleteTree("madTest", "delete", "tree");
    }

    on = !on;

    madHistogramAddContext("madTest", "histocontext", "value", NULL,
            MOCA_FALSE,
            -40, "-40");
    madHistogramAddContext("madTest", "histocontext", "value", NULL,
            MOCA_FALSE,
            40, "40");

    madTimeIntervalClear(&interval);
    madTimeIntervalSetNanoseconds(&interval, 1);

    madTimerAddContext("madTest", "timercontext", "timer", NULL, MAD_SECONDS,
            MAD_SECONDS, &interval, "minimum");

    
    madTimeIntervalClear(&interval);
    madTimeIntervalSetSeconds(&interval, 1);

    madTimerAddContext("madTest", "timercontext", "timer", NULL, MAD_SECONDS,
            MAD_SECONDS, &interval, "maximum");

    results = srvResults(eOK, "value", COMTYP_INT, sizeof(int), 1, NULL); 

    return results;
}
