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
 *  Copyright (c) 2009
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

#ifdef UNIX
# include <unistd.h>
#else
# include <process.h>
#endif

#include <mislib.h>
#include <mocaerr.h>
#include <mocagendef.h>
#include "osprivate.h"

#define MAX_ARGS 512

static void getJavaVM(int is32bit, char **javaPath) 
{
    char *vmName = NULL;

    /* 
     * We'd really like the first argument (argv[0]) to be the name of the
     * executable itself. (e.g. mbuild)  Unfortunately the non-Sun Java
     * launchers that I've tested on AIX and HP-UX use argv[0] blindly and
     * assume it's set to "java", which results in an error when those
     * launchers do their own exec( ) call.  The SUN Java launcher is real
     * nice about this and makes sure it calls "java" or "java.exe" and
     * still leaves our hacked argument vector intact.
     */

    if (is32bit)
    {
        vmName = osGetRegistryValue(REGSEC_JAVA, REGKEY_JAVA_VM32);
    }

    if (vmName == NULL || !strlen(vmName))
    {
        vmName = osGetRegistryValue(REGSEC_JAVA, REGKEY_JAVA_VM);
    }

    if (vmName == NULL || !strlen(vmName))
    {
#ifdef UNIX
        vmName = "java";
#else
        vmName = "java.exe";
#endif
    }

    /* Point the caller's Java path to the VM name. */
    *javaPath = vmName;
}

static void getJavaVMArgs(int is32bit, char **javaVMArgs, char *appname) 
{
    /* 
     * The next set of arguments are the JVM arguments from the 
     * MOCA_JAVA_VMARGS environment variable if is set.
     */

    /* Get the value of the environment variable. */
    char *env = osGetVar(ENV_JAVA_VMARGS);

    if (!env && appname)
    {
	char appKey[512];
	sprintf(appKey, "%s.%s", REGKEY_JAVA_VMARGS, appname);
        env = osGetRegistryValueNotExpanded(REGSEC_JAVA, appKey);
    }

    /* If no vmargs in the environment, defalt to the server args */
    if (!env && is32bit)
    {
        env = osGetRegistryValueNotExpanded(REGSEC_JAVA, REGKEY_JAVA_VMARGS32);
    }

    if (!env)
    {
        env = osGetRegistryValueNotExpanded(REGSEC_JAVA, REGKEY_JAVA_VMARGS);
    }

    *javaVMArgs = env;
}

/*
 * Build the argument vector to use when launching the application.
 *
 * This involves taking the caller's argument vector and playing some games
 * with it to reorder some of the arguments.  We also add the value of the
 * MOCA_JAVA_VMARGS environment variable if it is set, however if the same 
 * JVM argument is provided via both the caller's argument vector and the
 * environment variable one provided via the caller's argument vector will
 * take precedence.
 *
 * The caller's argument vector will look something like this:
 *
 *     <app_name> <app_arg1> ... <app_argN> [ -vmargs <vm_arg1> ... <vm_argN> ]
 *
 * And we change it to the following:
 *
 *     java <vm_arg1> ... <vm_argN> <classname> <app_arg1> ... <app_argN>
 *
 * EXAMPLE 1
 *
 *     mocaserver -t*
 *
 *     java com.redprairie.moca.server.MocaServerMain -t*
 *
 * EXAMPLE 2
 *
 *     mocaserver -t* -vmargs -Xmx2048M
 *
 *     java -Xmx2048M com.redprairie.moca.server.MocaServerMain -t*
 */

static char **sBuildArgumentVector(int is32bit, 
		                   char *appname, 
				   char *classname, 
				   char **javaPath,
				   int inArgc, 
				   char **inArgv)
{
    int ii, argc = 0, vmArgsIndex = 0;
    char *arg, *ptr, *env, *envCopy = NULL, *javaPathCopy = NULL;
    static *argv[MAX_ARGS];

    getJavaVM(is32bit, javaPath);
    
    /* Make a copy of the value that we can play with. */
    misDynStrcpy(&javaPathCopy, *javaPath);
    ptr = javaPathCopy;

    /* 
     * Split the vm path because it could actually contain
     * a vm argument as well.  (e.g. -d32)
     */
    while (argc < MAX_ARGS && (arg = misParseArgument(&javaPathCopy)))
    {
        if (*arg != '\0')
            argv[argc++] = arg;
    }

    getJavaVMArgs(is32bit, &env, appname);

    /* Add the VM arguments to the argument vector. */
    if (env && strlen(env))
    {
	/* Make a copy of the value that we can play with. */
        misDynStrcpy(&envCopy, env);
        ptr = envCopy;

	/* Pick out each argument. */
        while (argc < MAX_ARGS && (arg = misParseArgument(&ptr)))
        {
            if (*arg != '\0')
                argv[argc++] = arg;
        }
    }

    /* 
     * Let's make sure we aren't going to overflow the argument vector when
     * we add in the remaining arguments below.
     */

    if (argc + inArgc + 1 > MAX_ARGS)
    {
        fprintf(stderr, "Argument vector too large\n");
        exit(EXIT_FAILURE);
    }

    /* 
     * The next set of arguments are the JVM arguments if a "-vmargs"
     * command line option was found in the argument vector.  These 
     * VM arguments take precedence over the ones from the MOCA_JAVA_VMARGS
     * environment variable.
     */

    /* Look for a "-vmargs" command line argument. */
    for (ii = 0; ii < inArgc; ii++)
    {
	if (strcmp(inArgv[ii], "-vmargs") == 0)
	{
	    vmArgsIndex = ii;
	    break;
        }
    }

    /* Add the VM arguments to the argument vector. */
    if (vmArgsIndex)
    {
        for (ii = vmArgsIndex + 1; ii < inArgc; ii++)
            argv[argc++] = inArgv[ii];
    }

#ifdef WIN32
    /*
     * We need to reduce the signal set for versions of
     * Windows prior to Windows 7 and Windows Server 2008
     * so we don't exit on console logout events.
     */
    {
    OSVERSIONINFOEX vi;
    ZeroMemory(&vi, sizeof(OSVERSIONINFOEX));
    vi.dwOSVersionInfoSize = sizeof(OSVERSIONINFOEX);
        
    if (GetVersionEx((OSVERSIONINFOEX *) &vi))
    {
        if (vi.dwMajorVersion < 6)
            argv[argc++] = "-Xrs";
    }
    }
#endif

    /* The next argument is always the classname. */
    argv[argc++] = classname;

    /* 
     * The last set of arguments are the ones that are passed 
     * to the application itself.
     */
    if (vmArgsIndex)
    {
        for (ii = 1; ii < vmArgsIndex; ii++) 
            argv[argc++] = inArgv[ii];
    }
    else
    {
        for (ii = 1; ii < inArgc; ii++) 
            argv[argc++] = inArgv[ii];
    }

    argv[argc] = NULL;

    return argv;
}

/*
 * Dump some debug information if the _JAVA_LAUNCHER_DEBUG environment 
 * variable is set.  This environment variable is the same one used by
 * the standard Java launcher that we turn around and call, so it's a 
 * nice way to provide full debug information starting with our custom
 * launcher and flowing through to the standard launcher.
 */
 
void sDebugLauncher_Args(char *classname, 
                         int argc, 
                         char *argv[], 
                         char *myArgv[])
{
    int ii;

    /* Don't bother if the launcher debug environment variable isn't set. */
    if (getenv("_JAVA_LAUNCHER_DEBUG") == NULL) 
	return;

    printf("----_JAVA_LAUNCHER_DEBUG----\n");

    /*
     * Dump the original argument vector. 
     */
    printf("Original Argument Vector\n");

    for (ii = 0; ii < argc; ii++)
    {
        printf("    argv[%d]: %s\n", ii, argv[ii]);
    }

    /*
     * Dump the tweaked argument vector. 
     */
    printf("New Argument Vector\n");

    for (ii = 0; myArgv[ii]; ii++)
    {
        printf("    argv[%d]: %s\n", ii, myArgv[ii]);
    }

    fflush(stdout);
}

/*
 * Dump some debug information if the _JAVA_LAUNCHER_DEBUG environment 
 * variable is set.  This environment variable is the same one used by
 * the standard Java launcher that we turn around and call, so it's a 
 * nice way to provide full debug information starting with our custom
 * launcher and flowing through to the standard launcher.
 */
 
void sDebugLauncher_CommandLine(char *origCommandLine, char *newCommandLine)
{
    /* Don't bother if the launcher debug environment variable isn't set. */
    if (getenv("_JAVA_LAUNCHER_DEBUG") == NULL) 
	return;

    printf("----_JAVA_LAUNCHER_DEBUG----\n");

    /*
     * Dump the original command line. 
     */
    printf("Original Command Line\n");

    printf("%s\n\n", origCommandLine);

    /*
     * Dump the tweaked command line. 
     */
    printf("New Command Line\n");

    printf("%s\n", newCommandLine);

    fflush(stdout);
}

#ifdef UNIX

int osLaunchJavaApplication(int is32bit, 
	                    char *appname, 
	                    char *classname, 
			    int argc, 
			    char *argv[])
{
    int status;

    char *javaPath, **myArgv;

    /* Build the argument vector. */
    myArgv = sBuildArgumentVector(is32bit, 
		                  appname, 
				  classname, 
				  &javaPath, 
				  argc, 
				  argv);

    /* Dump a nice set of debug messages if necessary. */
    sDebugLauncher_Args(classname, argc, argv, myArgv);

    /* Use the standard Java launcher to start the application. */
    status = execvp(myArgv[0], myArgv);
    if (status == -1)
    {
        fprintf(stderr, "execvp: %s\n", osError( ));
        fprintf(stderr, "Could not launch Java application\n");
        fprintf(stderr, "Class: %s\n", classname);
        return EXIT_FAILURE;
    }

    return status;
}

#else

int osLaunchJavaApplication(int is32bit, 
	                    char *appname, 
			    char *classname, 
			    int argc, 
			    char *argv[])
{
    DWORD status;
    char *p, *tmp, *originalCommandLine, *commandLine = NULL;
    int inquote = 0, slashes = 0, commandsize = 0;
    char *javaVM, *javaVMArgs, *javaXrsArg, *vmargsloc, *vmargs = NULL;

    STARTUPINFO si;
    PROCESS_INFORMATION pi;
    OSVERSIONINFOEX vi;


    /* Make a copy of the command line that we can play with. */
    tmp = GetCommandLine();
    originalCommandLine = malloc(strlen(tmp) + 1);
    strcpy(originalCommandLine, tmp);

    p = originalCommandLine;

    /* Strip leading whitespace. */
    while (*p && (*p == ' ' || *p == '\t'))
        p++;

    /* Now we skip the executable name. */
    while (*p && (inquote || !(*p == ' ' || *p == '\t'))) 
    {
        if (*p == '\\' && *(p+1) == '"' && slashes % 2 == 0)
            p++;
        else if (*p == '"')
            inquote = !inquote;
        slashes = (*p == '\\') ? slashes + 1 : 0;
	p++;
    }

    getJavaVM(is32bit, &javaVM);

    getJavaVMArgs(is32bit, &javaVMArgs, appname);

    /* We have to find if there is a -vmargs with whitespace around it. */
    inquote = 0;
    for (vmargsloc = p; *vmargsloc && vmargs == NULL; vmargsloc++)
    {
	if (*vmargsloc == '"') inquote = !inquote;

	if (!inquote && 0 == strncmp(vmargsloc, " -vmargs ", 9))
	{
	    /* We also then move up vmargs to point at the real vmargs. */
	    vmargs = vmargsloc + 9;
	    *vmargsloc = '\0';
	}
    }

    /* Strip that whitespace as well after the exe. */
    while (*p && (*p == ' ' || *p == '\t'))
        p++;

    /*
     * We need to reduce the signal set for versions of
     * Windows prior to Windows 7 and Windows Server 2008
     * so we don't exit on console logout events.
     */
    ZeroMemory(&vi, sizeof(OSVERSIONINFOEX));
    vi.dwOSVersionInfoSize = sizeof(OSVERSIONINFOEX);
    
    javaXrsArg = NULL;    

    if (GetVersionEx((OSVERSIONINFOEX *) &vi))
    {
        if (vi.dwMajorVersion < 6)
            javaXrsArg = "-Xrs";
    }

    misDynSprintf(&commandLine, "\"%s\" %s %s %s %s %s", javaVM,
	                                       javaVMArgs ? javaVMArgs : "",
	                                       javaXrsArg ? javaXrsArg : "",
	                                       vmargs ? vmargs : "",
	                                       classname,
	                                       p );


    /* We also free up the other values, but we don't need to do anything special */
    free(originalCommandLine);

    /*
     * Process startup information.  This is used by CreateProcess to
     * tell what sort of process we want to create.  We're creating a
     * console child process that inherits most of our handles.
     */
    memset(&si, 0, sizeof si);
    si.cb = sizeof si;
    si.lpReserved = NULL;
    si.lpDesktop = NULL;
    si.lpTitle = NULL;
    si.dwFlags = STARTF_USESTDHANDLES;
    si.cbReserved2 = 0;
    si.lpReserved2 = NULL;
    si.hStdOutput = GetStdHandle(STD_OUTPUT_HANDLE);
    si.hStdInput = GetStdHandle(STD_INPUT_HANDLE);
    si.hStdError = GetStdHandle(STD_ERROR_HANDLE);

    ZeroMemory( &pi, sizeof(pi) );

    sDebugLauncher_CommandLine(GetCommandLine(), commandLine);

    /* Start the child process. */
    if( !CreateProcess( NULL,   /* No module name (use command line) */
        commandLine,            /* Command line */
        NULL,                   /* Process handle not inheritable */
        NULL,                   /* Thread handle not inheritable */
        TRUE,                   /* Set handle inheritance to TRUE */
        0,                      /* No creation flags */
        NULL,                   /* Use parent's environment block */
        NULL,                   /* Use parent's starting directory */
        &si,                    /* Pointer to STARTUPINFO structure */
        &pi )                   /* Pointer to PROCESS_INFORMATION structure */
    ) 
    {
        fprintf(stderr, "CreateProcess: %s", osError( ));
        fprintf(stderr, "Could not launch Java application\n");
        fprintf(stderr, "Class: %s\n", classname);
	free(commandLine);
        return EXIT_FAILURE;
    }

    free(commandLine);

    /* Wait until child process exits. */
    WaitForSingleObject( pi.hProcess, INFINITE );

    GetExitCodeProcess( pi.hProcess, &status );

    /* Close process and thread handles. */
    CloseHandle( pi.hProcess );
    CloseHandle( pi.hThread );

    return status;
}

#endif

/*
 * This method is to be used when programmatically invoking 
 * osLaunchJavaApplication.  It will attempt to use the argc, and argv
 * as they are passed in.  In windows spaces will not work correctly
 * when using this.
 */
int osLaunchJavaApplicationUseArgs(int is32bit, 
	                    char *appname, 
			    char *classname, 
			    int argc, 
			    char *argv[])
{
#ifdef UNIX
    return osLaunchJavaApplication(is32bit, 
	                    appname, 
			    classname, 
			    argc, 
			    argv);
#else
    int status;

    char *javaPath, **myArgv;
    static char quotedVm[1024];

    /* Build the argument vector. */
    myArgv = sBuildArgumentVector(is32bit,  
		                  appname, 
				  classname, 
				  &javaPath, 
				  argc, 
				  argv);

    sprintf(quotedVm, "\"%s\"", *(myArgv));
    *(myArgv) = quotedVm;

    /* Dump a nice set of debug messages if necessary. */
    sDebugLauncher_Args(classname, argc, argv, myArgv);

    /* Use the standard Java launcher to start the application. */
    status = _spawnvp(_P_WAIT, javaPath, myArgv);
    if (status == -1)
    {
        fprintf(stderr, "_spawnvp: %s\n", osError( ));
        fprintf(stderr, "Could not launch Java application\n");
        fprintf(stderr, "Class: %s\n", classname);
        return EXIT_FAILURE;
    }

    return status;
#endif
}
