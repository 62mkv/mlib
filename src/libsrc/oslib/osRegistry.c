static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to support reading a registry file.
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
#include <string.h>
#include <ctype.h>
#include <errno.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <mislib.h>

#include "osprivate.h"

/*
 *  Local Type Definitions
 */

typedef struct RegistryEntry
{
    char *text;
    char *comment;
    char *section;
    char *name;
    char *value;
    char *realValue;
    short lastInSection;
    struct RegistryEntry *prev,
                         *next;
} RegistryEntry;

/*
 *  Local Static Variables
 */

static RegistryEntry *gRegistry;
static short gLoaded;


static char gRegistryFile[PATHNAME_LEN];

static long sRegistryExists(void)
{
    static int exists = -1;

    int status;

    char *regfile = NULL;

    /* If we've already! determined this, don't bother with everything. */
    if (exists >= 0)
        return exists;

    /*
     * Get the pathname of the registry file from...
     *
     *  1 - The local registry file variable.
     *  2 - The MOCA_REGISTRY environment variable.
     */
    if (strlen(gRegistryFile))
        regfile = gRegistryFile;

    if (!regfile || !strlen(regfile))
        regfile = getenv(ENV_REGISTRY);

    if (!regfile || !strlen(regfile))
        return 0;

    /* Check for the existence of the registry file. */
    status = osAccess(regfile, OS_ACCESS_READ);

    /* Set the static exists flag. */
    exists = status ? 0 : 1;

    return exists;
}

static void sFreeRegistryListEntry(RegistryEntry *this)
{
    free(this->text);
    free(this->comment);
    free(this->section);
    free(this->name);
    free(this->value);
    free(this->realValue);

    free(this);

    return;
}

static void sFreeRegistryList(void)
{
    RegistryEntry *this,
                  *next;

    /* Cycle through each entry in the registry list. */
    for (this=gRegistry; this; this=next)
    {
        next = this->next;
        sFreeRegistryListEntry(this);
    }

    /* NULL out the registry list. */
    gRegistry = NULL;

    return;
}

static void sAddRegistryListEntry(RegistryEntry *after, RegistryEntry *this)
{
    static short calledAtexit;

    /* Validate our arguments. */
    if (!this)
        return;

    /* Make sure we clean up after ourselves. */
    if (!calledAtexit)
    {
        calledAtexit = 1;
        osAtexit(sFreeRegistryList);
    }

    /* 
     *  If we weren't given an entry to add this entry after, just find
     *  the end of the registry list.
     */
    if (!after)
    {
        for (after=gRegistry; after && after->next; after=after->next)
            ;  /* noop */
    }

    /* Connect this entry to the registry list. */
    if (!after)
    {
        gRegistry  = this;
        this->prev = NULL;
        this->next = NULL;
    }
    else
    {
        this->prev  = after;
        this->next  = after->next;
        after->next = this;
    }

    /* Set the "last in section" attribute. */
    this->lastInSection = 1;
  
    /* Set the previous node's "last in section" attribute. */
    if (this->prev)
    {
        if (this->prev->comment && this->section)
        {
            this->prev->lastInSection = 0;
            this->prev->section = malloc(strlen(this->section) + 1);
            strcpy(this->prev->section, this->section);
        }
        else if (this->prev->section && this->section && 
                 misCiStrcmp(this->prev->section, this->section) == 0)
        {
            this->prev->lastInSection = 0;
        }
    }

    return;
}

static void sRemoveRegistryListEntry(RegistryEntry *this)
{
    /* Validate our arguments. */
    if (!this)
        return;
  
    /* Set the previous node's "last in section" attribute. */
    if (this->lastInSection && this->prev && 
        this->section       && this->prev->section &&
        misCiStrcmp(this->section, this->prev->section) == 0)
    {
        this->prev->lastInSection = 1;
    }

    /* Connect the previous and next entries in the registry list. */
    if (this->prev && this->next)
    {
        this->prev->next = this->next;
        this->next->prev = this->prev;
    }
    else if (this->prev)
    {
        this->prev->next = NULL;
    }
    else if (this->next)
    {
        this->next->prev = NULL;
        gRegistry = this->next;
    }
    else
    {
        gRegistry = NULL;
    }

    /* Free memory associated with this registry list entry. */
    sFreeRegistryListEntry(this);

    return;
}

static long sDumpRegistryList(FILE *outfile)
{
    RegistryEntry *this;

    /* Validate our arguments. */
    if (!outfile)
        return eINVALID_ARGS;

    /* Cycle through each entry in the registry list. */
    for (this=gRegistry; this; this=this->next)
    {
        fprintf(outfile, "%s\n", this->text);
    }

    return eOK;
}

static char *sGetRegistryListSection(char *section)
{
    RegistryEntry *this;

    /* Validate our arguments. */
    if (!section)
        return NULL;

    /* Cycle through each entry in the registry list. */
    for (this=gRegistry; this; this=this->next)
    {
        /* Don't bother if this isn't a named value entry. */
        if (!this->section)
            continue;

        /* Have we found a match? */
        if (misCiStrcmp(this->section, section) == 0 &&
            !this->name && !this->value && !this->comment)
        {
            return this->section;
        }
    }

    return NULL;
}

static char *sGetRegistryListValue(char *section, char *name)
{
    RegistryEntry *this;

    /* Validate our arguments. */
    if (!section || !name)
        return NULL;

    /* Cycle through each entry in the registry list. */
    for (this=gRegistry; this; this=this->next)
    {
        /* Don't bother if this isn't a named value entry. */
        if (!this->section || !this->name)
            continue;

        /* Have we found a match? */
        if (misCiStrcmp(this->section, section) == 0 &&
            misCiStrcmp(this->name,    name)    == 0)
        {
            /* We reexpand the value every time to be safe. */
            {
                char *temp = misDynExpandVars(this->value, NULL);
		if (this->realValue == NULL || 0 != strcmp(temp, this->realValue))
		{
		    if (this->realValue) free(this->realValue);
		    this->realValue = temp;
		}
		else
		{
                    free(temp);
		}
            }

            return this->realValue;
        }
    }

    return NULL;
}

static char *sGetRegistryListValueNotExpanded(char *section, char *name)
{
    RegistryEntry *this;

    /* Validate our arguments. */
    if (!section || !name)
        return NULL;

    /* Cycle through each entry in the registry list. */
    for (this=gRegistry; this; this=this->next)
    {
        /* Don't bother if this isn't a named value entry. */
        if (!this->section || !this->name)
            continue;

        /* Have we found a match? */
        if (misCiStrcmp(this->section, section) == 0 &&
            misCiStrcmp(this->name,    name)    == 0)
        {
            return this->value;
        }
    }

    return NULL;
}

static long sUpdateRegistryListValue(char *section, char *name, char *value)
{
    RegistryEntry *this;

    /* Validate our arguments. */
    if (!section || !name)
        return eINVALID_ARGS;

    /* Handle empty values. */
    if (!value)
        value = "";

    /* Cycle through each entry in the registry list. */
    for (this=gRegistry; this; this=this->next)
    {
        /* Don't bother if this isn't a named value entry. */
        if (!this->section || !this->name)
            continue;

        /* Have we found a match? */
        if (misCiStrcmp(this->section, section) == 0 &&
            misCiStrcmp(this->name,    name)    == 0)
        {
            /* Reset the value. */
            this->value = realloc(this->value, strlen(value) + 1);
            strcpy(this->value, value);

            /* Reset the text. */
            this->text = realloc(this->text, strlen(name) + strlen(value) + 2);
            sprintf(this->text, "%s=%s", name, value);

            return eOK;
        }
    }

    return eERROR;
}

static long sAddRegistryListComment(char *comment)
{
    RegistryEntry *this;

    /* Validate our arguments. */
    if (!comment)
        return eINVALID_ARGS;

    /* Allocate space for a new registry entry. */
    this = calloc(1, sizeof(RegistryEntry));
  
    /* Populate the comment for this registry entry. */
    this->comment = malloc(strlen(comment) + 1);
    strcpy(this->comment, comment);

    /* Populate the text for this registry entry. */
    this->text = malloc(strlen(comment) + 1);
    sprintf(this->text, "%s", comment);

    /* Add this entry to the registry list. */
    sAddRegistryListEntry(NULL, this);

    return eOK;
}

static long sAddRegistryListSection(char *section)
{
    long status;

    RegistryEntry *this;

    /* Validate our arguments. */
    if (!section)
        return eINVALID_ARGS;

    /* Don't bother if the section already exists in the registry list. */
    for (this=gRegistry; this; this=this->next)
    {
        if (this->section && misCiStrcmp(this->section, section) == 0)
            return eOK;
    }

    /* Add a blank line to the registry list. */
    status = sAddRegistryListComment("");
    if (status != eOK)
    {
        misLogError("sAddRegistryListSection: Could not add comment");
        return status;
    }

    /* Allocate space for a new registry entry. */
    this = calloc(1, sizeof(RegistryEntry));
  
    /* Populate the section for this registry entry. */
    this->section = malloc(strlen(section) + 1);
    strcpy(this->section, section);

    /* Populate the text for this registry entry. */
    this->text = malloc(strlen(section) + 3);
    sprintf(this->text, "[%s]", section);

    /* Add this entry to the registry list. */
    sAddRegistryListEntry(NULL, this);

    return eOK;
}

static long sAddRegistryListValueSpecial(char *section, 
                                         char *name, 
                                         char *value, 
                                         int insertOnly)
{
    int found;

    char *ptr,
         *temp,
         *oneValue;

    RegistryEntry *this;

    /* Don't bother unless this is a special value. */
    if (misCiStrcmp(section, "conmgr") != 0 ||
        (misCiStrcmp(name, "task-list") != 0 &&
         misCiStrcmp(name, "conmgr-list") != 0 &&
         misCiStrcmp(name, "job-list") != 0))

    {
        return eERROR;
    }

    /* Cycle through each entry in the registry list. */
    for (this=gRegistry; this; this=this->next)
    {
        /* Don't bother if this isn't a named value entry. */
        if (!this->section || !this->name)
            continue;

        /* Have we found a match? */
        if (misCiStrcmp(this->section, section) == 0 &&
            misCiStrcmp(this->name,    name)    == 0)
        {
            /* 
             *  Don't bother if this value is already in the current value. 
             */

            /* 
             *  The value passed in is a string that may contain multiple, 
             *  comma-separated values.  So, we have to pull each individual
             *  value from the value passed in and deal with them one at a time.
             */
            while ((oneValue = misStrsep(&value, ",'\0'")) != NULL)
            {
                /* Get a temporary pointer to this value in the registry. */
                temp = this->value;

                /* Reset the found flag for the new individual value. */
                found = 0;

                /* Trim spaces for the left and right. */
                misTrimLR(oneValue);

                /* Look for this value in the current value. */
                while ((ptr = strstr(temp, oneValue)) != NULL)
                {
                    /* Advance past the matching string we found. */
                    temp = ptr + strlen(oneValue);
    
                    /* Make sure we didn't match against just a substring. */
                    if (*temp == ',' || *temp == '\0' || isspace(*temp))
                    {
                        found++;
                        break;
                    }
                }

                /* 
                 *  Move on to the next individual value if this one is 
                 *  already a part of the registry value. 
                 */
                if (found)
                {
                    char object[50];
                    char *c;
                    long length = sizeof(object)-1;
                    long i;

                    c=strrchr(name, '-');
                    if(c)
                    {
                        if(c-name < length)
                        {
                            length = c-name;
                        }
                    }
                    sprintf(object, "%.*s", length, name);
                    for (i=0; i<length; i++)
                    {
                        object[i] = (i==0)?toupper(object[i]):tolower(object[i]);
                    }

                    /* Warn user that task, job or conmgr already existed and did not overwrite */
                    printf("WARNING: %s %s already exists\n", object, oneValue);
                    printf("         It was not added to the registry\n");
                    printf("         Please check differences between the configurations\n");
                    printf("\n");
                    continue;
                }

                /* Add this value to the current value. */
                this->value = 
                  realloc(this->value, strlen(this->value)+strlen(oneValue)+2);
                if (misTrimLen(this->value, strlen(this->value)))
                    strcat(this->value, ",");
                strcat(this->value, oneValue);
    
                /* Reset the text. */
                this->text = 
                  realloc(this->text, strlen(this->name)+strlen(this->value)+2);
                sprintf(this->text, "%s=%s", this->name, this->value);

                /* Log the addition of this task/job. */
                if (insertOnly)
                    printf("WARNING: ");
                printf("Task/job %s was added to the registry\n", oneValue);
            }

            return eOK;
        }
    }

    return eERROR;
}

static long sAddRegistryListValue(char *section, 
                                  char *name, 
                                  char *value, 
                                  int insertOnly)
{
    long status;

    RegistryEntry *this,
                  *last;

    /* Validate our arguments. */
    if (!section || !name)
        return eINVALID_ARGS;
    
    /* Handle empty values. */
    if (!value)
        value = "";

    /* We have a couple of exceptional cases we have to handle. */
    if (sAddRegistryListValueSpecial(section, name, value, insertOnly) == eOK)
        return eOK;

    /* Just update the value if it already exists in the registry list. */
    if (insertOnly)
    {
        if (sGetRegistryListValue(section, name))
            return eOK;
    }
    else
    {
        if (sUpdateRegistryListValue(section, name, value) == eOK)
            return eOK;
    }
    /* Find the first entry in the registry for this section. */
    for (last=gRegistry; last; last=last->next)
    {
        /* Don't bother if this isn't part of a section entry. */
        if (!last->section) 
            continue;
 
        /* Have we found this section? */
        if (last->lastInSection && misCiStrcmp(last->section, section) == 0)
            break;
    }

    /* Add this entry to the registry list. */
    if (!last)
    {
        status = sAddRegistryListSection(section);
        if (status != eOK)
        {
            misLogError("sAddRegistryListValue: Could not add section");
            return status;
        }
    }

    /* Allocate space for a new registry entry. */
    this = calloc(1, sizeof(RegistryEntry));
  
    /* Populate the section for this registry entry. */
    this->section = malloc(strlen(section) + 1);
    strcpy(this->section, section);

    /* Populate the name for this registry entry. */
    this->name = malloc(strlen(name) + 1);
    strcpy(this->name, name);

    /* Populate the value for this registry entry. */
    this->value = malloc(strlen(value) + 1);
    strcpy(this->value, value);

    /* Populate the text for this registry entry. */
    this->text = malloc(strlen(name) + strlen(value) + 2);
    sprintf(this->text, "%s=%s", name, value);

    /* Add this entry to the registry list. */
    sAddRegistryListEntry(last, this);

    return eOK;
}

static long sLoadRegistryListFromFile(char *pathname, int insertOnly)
{
    short appendmode = 0;

    long status = eOK;

    char  line[2048],
         *ptr,
         *buffer,
         *name,
         *value,
         *section;

    FILE *infile;

    /* Initialize some of our local variables. */
    section = NULL;

    /* Open the registry file */
    infile = fopen(pathname, "r");
    if (!infile)
    {
        misLogError("fopen(%s): %s", pathname, osError( ));
        misLogError("sLoadRegistryListFromFile: Could not open registry file");
        return osErrno( );
    }

    /* Cycle through each line in the registry file. */
    while (fgets(line, sizeof line, infile) != NULL)
    {
        /* Reset the name/value pair on each iteration. */
        if (!appendmode) 
        {
           name = NULL; 
           value = NULL; 
           buffer = NULL; 
        } 

        /* 
         * Lines longer that 2047 will exhibit this behavior so need to 
         * keep reading the remainder on the next pass 
         * We only do this if we weren't on the end of the file though, so
         * this way we can use the last line if there were characters
         */
        if (!feof(infile) && line[strlen(line) - 1] != '\n' 
            && line[strlen(line) - 1] != '\r') 
        {    
            if (appendmode) 
            {
                /* If multiple times needed append. */ 
                buffer = misDynStrcat(&buffer, line);
            } 
            else 
            {
                /* First time line read in. */
                misDynStrcpy(&buffer, line);
            }

            appendmode = 1;

            continue;
        } 
        else if (appendmode)  /* and buffer not full */ 
        {
            buffer = misDynStrcat(&buffer,line);
        }
        else       /* buffer was null and line not full */
        {
            misDynStrcpy(&buffer,line);
        }
        
        appendmode = 0;
           
        /* Strip linefeeds, formfeeds and carriage returns. */
        misReplaceChars(buffer, "\n", "");
        misReplaceChars(buffer, "\f", " ");
        misReplaceChars(buffer, "\r", " ");
           
        /* Get a pointer to the buffer that we can play with. */
        ptr = buffer;

        /* Skip past leading white-space. */
        while (isspace(*ptr))
            ptr++;

        /* Handle a comment. */
        if (*ptr == '#')
        {
            status = sAddRegistryListComment(ptr);
            if (status != eOK)
            {
                misLogError("sLoadRegistryListFromFile: Could not add comment");
                goto cleanup;
            }
        }

        /* Handle a new section. */
        else if (*ptr == '[')
        {
            ptr++;
            ptr = strtok(ptr, "]");

            misTrimLR(ptr);

            free(section);
            section = NULL;

            misDynStrcpy(&section, ptr);

            status = sAddRegistryListSection(section);
            if (status != eOK)
            {
                misLogError("sLoadRegistryListFromFile: Could not add section");
                goto cleanup;
            }
        }

        /* Handle a name/value pair. */
        else if (section && (name = strtok(ptr, "=")) != NULL)
        {
            value = strtok(NULL, "\0");

            /* Trim each side of the name and value. */
            misTrimLR(name);
            misTrimLR(value);

            /* Add a new registry list entry. */
            status = sAddRegistryListValue(section, name, value, insertOnly);
            if (status != eOK)
            {
                misLogError("sLoadRegistryListFromFile: Could not add value");
                return status;
            }
        }

        /* Free the copy of the line that we were playing with. */
        free(buffer);
    }

cleanup:

    /* Close the registry file. */
    fclose(infile);

    /* Free the last section that we were using. */
    free(section);
    return status;
}

static long sLoadRegistryList(void)
{
    long status;

    char *regfile;

    /* Don't bother if the registry list has already been loaded. */
    if (gLoaded)
        return eOK;

    /* Set our loaded flag. */
    gLoaded = 1;

    /* Get the pathname of the registry file if necessary. */
    if (!strlen(gRegistryFile))
    {
        regfile = getenv(ENV_REGISTRY);
        if (regfile)
        {
            memset(gRegistryFile, 0, sizeof gRegistryFile);
            strncpy(gRegistryFile, regfile, sizeof gRegistryFile - 1);
        }
        else
        {
            misLogError("sLoadRegistryList: MOCA_REGISTRY not set");
            return eERROR;
        }
    }

    /* Load the registry list from the registry file. */
    status = sLoadRegistryListFromFile(gRegistryFile, 0);
    if (status != eOK)
    {
        misLogError("sLoadRegistryList: Could not load registry list");
        return status;
    }

    return eOK;
}

/*
 *
 *  P U B L I C   A P I
 *
 */

/*
 *  FUNCTION: osGetRegistryValue
 *
 *  PURPOSE:  Get the given registry value.
 *
 *  RETURNS:  Pointer to the value
 *            NULL - An error occurred
 */

char *osGetRegistryValue(char *section, char *name)
{
    long status;

    char *value;

    /* Validate our arguments. */
    if (!section || !name)
        return NULL;

    /* Don't bother if a registry doesn't exist. */
    if (!sRegistryExists( ))
        return NULL;

    /* Load the registry list if necessary. */
    status = sLoadRegistryList( );
    if (status != eOK)
    {
        misLogError("osGetRegistryValue: Could not load registry list");
        return NULL;
    }

    /* Get the value from the registry list. */
    value = sGetRegistryListValue(section, name);

    return value;
}

char *osGetReg(char *section, char *name)
{
    return osGetRegistryValue(section, name);
}

/*
 *  FUNCTION: osGetRegistryValueNotExpanded
 *
 *  PURPOSE:  Get the given registry value but do not expand any 
 *            variables.
 *
 *  RETURNS:  Pointer to the value
 *            NULL - An error occurred
 */

char *osGetRegistryValueNotExpanded(char *section, char *name)
{
    long status;

    char *value;

    /* Validate our arguments. */
    if (!section || !name)
        return NULL;

    /* Don't bother if a registry doesn't exist. */
    if (!sRegistryExists( ))
        return NULL;

    /* Load the registry list if necessary. */
    status = sLoadRegistryList( );
    if (status != eOK)
    {
        misLogError("osGetRegistryValue: Could not load registry list");
        return NULL;
    }

    /* Get the value from the registry list. */
    value = sGetRegistryListValueNotExpanded(section, name);

    return value;
}

char *osGetRegNotExpanded(char *section, char *name)
{
    return osGetRegistryValueNotExpanded(section, name);
}

/*
 *  FUNCTION: osEnumerateRegistry
 *
 *  PURPOSE:  Enumerate the given registry section.
 *
 *  RETURNS:  eOK
 */

char *osEnumerateRegistry(char *section, void **context, 
                         char **name, char **value)
{
    long status;

    RegistryEntry *this = *context;

    /* Validate our arguments. */
    if (!name || !value)
        return NULL;

    /* Load the registry list if necessary. */
    status = sLoadRegistryList( );
    if (status != eOK)
    {
        misLogError("osEnumerateRegistry: Could not load registry");
        return NULL;
    }

    /* If we're starting from scratch, point to the registry. */
    if (!this)
        this = gRegistry;
    else
        this = this->next;

    /* Enumerate through the registry. */
    for (this; this; this=this->next)
    {
        /* Skip past comment lines. */
        if (!this->section || !this->name || !this->value)
            continue;

        /* Have we found a matching section? */
        if (!section || misCiStrcmp(section, this->section) == 0)
        {
            *context = this;

            /* We reexpand the value every time to be safe. */
            {
                char *temp = misDynExpandVars(this->value, NULL);
		if (this->realValue == NULL || 0 != strcmp(temp, this->realValue))
		{
		    if (this->realValue) free(this->realValue);
		    this->realValue = temp;
		}
		else
		{
                    free(temp);
		}
            }

            *name  = this->name;
            *value = this->realValue;

            return this->section;
        }
    }

    *context = NULL;

    return NULL;
}

