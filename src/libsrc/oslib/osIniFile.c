static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: Functions to support reading a Win32-formatted ini file.
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
 * Local Type Definitions
 */

typedef OS_INI_FILE IniEntry;

void sFreeIniEntry(IniEntry *this)
{
    free(this->section);
    free(this->name);
    free(this->value);
    free(this->realValue);

    free(this);

    return;
}

void sFreeIniEntryList(IniEntry *entryList)
{
    IniEntry *this,
             *next;

    /* Cycle through each entry in the ini entry list. */
    for (this=entryList; this; this=next)
    {
        next = this->next;
        sFreeIniEntry(this);
    }

    return;
}

void sAddToIniEntryList(IniEntry **entryList, IniEntry *after, IniEntry *this)
{
    /* Validate our arguments. */
    if (!this)
	return;

    /* 
     * If we weren't given an entry to add this entry after, just find
     * the end of the ini entry list.
     */
    if (!after)
    {
	for (after=*entryList; after && after->next; after=after->next)
	    ;  /* noop */
    }

    /* Connect this entry to the ini entry list. */
    if (!after)
    {
	*entryList = this;
	this->prev = NULL;
	this->next = NULL;
    }
    else
    {
	this->prev  = after;
	this->next  = after->next;
	after->next = this;
    }

    return;
}

static long sAddIniEntryListSection(IniEntry **entryList, char *section)
{
    IniEntry *this;

    /* Validate our arguments. */
    if (!section)
	return eINVALID_ARGS;

    /* Don't bother if the section already exists in the ini entry list. */
    for (this=*entryList; this; this=this->next)
    {
        if (this->section && misCiStrcmp(this->section, section) == 0)
	    return eOK;
    }

    /* Allocate space for a new ini entry. */
    this = calloc(1, sizeof(IniEntry));
  
    /* Populate the section for this ini entry. */
    this->section = malloc(strlen(section) + 1);
    strcpy(this->section, section);

    /* Add this entry to the ini entry list. */
    sAddToIniEntryList(entryList, NULL, this);

    return eOK;
}

static long sAddIniEntryListValue(IniEntry **entryList,
				  char *section, 
			          char *name, 
			          char *value)
{
    long status;

    IniEntry *this,
	     *last;

    /* Validate our arguments. */
    if (!section || !name)
	return eINVALID_ARGS;
    
    /* Handle empty values. */
    if (!value)
	value = "";

    /* Find the first entry in the ini entry list for this section. */
    for (last=*entryList; last; last=last->next)
    {
	/* Have we found this section? */
        if (misCiStrcmp(last->section, section) == 0)
	    break;
    }

    /* Add this entry to the ini entry list. */
    if (!last)
    {
        status = sAddIniEntryListSection(entryList, section);
	if (status != eOK)
	{
	    misLogError("sAddRegistryListValue: Could not add section");
	    return status;
	}
    }

    /* Allocate space for a new ini entry. */
    this = calloc(1, sizeof(IniEntry));
  
    /* Populate the section for this ini entry. */
    this->section = malloc(strlen(section) + 1);
    strcpy(this->section, section);

    /* Populate the name for this ini entry. */
    this->name = malloc(strlen(name) + 1);
    strcpy(this->name, name);

    /* Populate the value for this ini entry. */
    this->value = malloc(strlen(value) + 1);
    strcpy(this->value, value);

    /* Add this entry to the ini entry list. */
    sAddToIniEntryList(entryList, last, this);

    return eOK;
}

static long sLoadIniEntryList(IniEntry **entryList, char *pathname)
{
    long status;

    char  line[2048],
         *ptr,
         *buffer,
         *name,
         *value,
         *section;

    FILE *infile;

    *entryList = NULL;

    /* Initialize some of our local variables. */
    section = NULL;

    /* Open the ini file */
    infile = fopen(pathname, "r");
    if (!infile)
    {
        misLogError("fopen(%s): %s", pathname, osError( ));
        misLogError("sLoadIniEntryList: Could not open ini file");
        return osErrno( );
    }

    /* Cycle through each line in the ini file. */
    while (fgets(line, sizeof line, infile) != NULL)
    {
        /* Reset the name/value pair on each iteration. */
        buffer = NULL, name = NULL, value = NULL;

        /* Strip linefeeds, formfeeds and carriage returns. */
        misReplaceChars(line, "\n", "");
        misReplaceChars(line, "\f", " ");
        misReplaceChars(line, "\r", " ");

        /* Make a copy of the line that we can play with. */
        misDynStrcpy(&buffer, line);

        /* Get a pointer to the buffer that we can play with. */
        ptr = buffer;

        /* Skip past leading white-space. */
        while (isspace(*ptr))
            ptr++;

	/* Handle comments. */
        if (*ptr == '#' || *ptr == ';')
	{
	    /* Don't bother doing anything - just skip it. */
	    ;
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

            status = sAddIniEntryListSection(entryList, section);
            if (status != eOK)
            {
                misLogError("sLoadIniEntryList: Could not add section");
                goto cleanup;
            }
        }

        /* Handle a name/value pair. */
        else if (section && (strchr(ptr, '=') != NULL))
        {
	    name  = strtok(ptr, "=");
            value = strtok(NULL, "\0");

	    /* Trim each side of the name and value. */
            misTrimLR(name);
            misTrimLR(value);

            /* Add a new ini list entry. */
            status = sAddIniEntryListValue(entryList, section, name, value);
            if (status != eOK)
            {
                misLogError("sLoadIniEntryList: Could not add value");
	        return status;
            }
        }

        /* Free the copy of the line that we were playing with. */
        free(buffer);
    }

cleanup:

    /* Close the ini file. */
    fclose(infile);

    /* Free the last section that we were using. */
    free(section);

    return status;
}

static char *sGetIniEntryListValue(IniEntry *entryList, 
				   char *section, 
				   char *name)
{
    char temp[4096];

    IniEntry *this;

    /* Validate our arguments. */
    if (!section || !name)
	return NULL;

    /* Cycle through each entry in the ini entry list. */
    for (this=entryList; this; this=this->next)
    {
	/* Don't bother if this isn't a named value entry. */
	if (!this->section || !this->name)
	    continue;

	/* Have we found a match? */
	if (misCiStrcmp(this->section, section) == 0 &&
	    misCiStrcmp(this->name,    name)    == 0)
	{
	    /* We reexpand the value every time to be safe. */
	    memset(temp, 0, sizeof temp);
	    misExpandVars(temp, this->value, sizeof temp, NULL);

	    /* Reset the real/expaned value. */
	    this->realValue = realloc(this->realValue, strlen(temp) + 1);
	    strcpy(this->realValue, temp);

	    return this->realValue;
	}
    }

    return NULL;
}

/*
 *
 *  P U B L I C   A P I
 *
 */

/*
 *  FUNCTION: osOpenIniFile
 *
 *  PURPOSE:  
 *
 *  RETURNS:  eOK
 */

long osOpenIniFile(IniEntry **entryList, char *iniFile)
{
    long status;

    char temp[PATHNAME_LEN],
	 pathname[PATHNAME_LEN];

    /* Validate our arguments. */
    if (!iniFile)
        return eINVALID_ARGS;

    /* 
     * If the ini filename passed isn't a full or relative pathname
     * then we assume the file exists in $LESDIR/data.
     */
    if (!strchr(iniFile, '\\') && !strchr(iniFile, '/'))
    {
        sprintf(temp, "%s%c%s%c%s", "$LESDIR",
                                    PATH_SEPARATOR,
                                    "data",
                                    PATH_SEPARATOR,
                                    iniFile);
    }
    else
    {
        sprintf(temp, "%s", iniFile);
    }

    /* Expand the pathname and fix it. */
    misExpandVars(pathname, temp, sizeof pathname, NULL);
    misFixFilePath(pathname);

    /* Make sure the pathname exists. */
    status = access(pathname, 4);
    if (status != 0)
    {
        misLogError("access: %s", strerror(errno));
        misLogError("osOpenIniFile: Could not access ini file");
	return eFILE_OPENING_ERROR;
    }

    /* Load the ini entry list. */
    status = sLoadIniEntryList(entryList, pathname);
    if (status != eOK)
    {
        misLogError("osOpenIniFile: Could not load ini file");
	return eERROR;
    }

    return eOK;
}

/*
 *  FUNCTION: osCloseIniFile
 *
 *  PURPOSE:  
 *
 *  RETURNS:  eOK
 */

void osCloseIniFile(IniEntry *entryList)
{
    /* Validate our arguments. */
    if (!entryList)
        return;

    /* Free memory associated with this ini entry list. */
    sFreeIniEntryList(entryList);

    return;
}

/*
 *  FUNCTION: osGetIniValue
 *
 *  PURPOSE:  Get the given ini value.
 *
 *  RETURNS:  Pointer to the value
 *            NULL - An error occurred
 */

char *osGetIniValue(IniEntry *entryList, char *section, char *name)
{
    char *value;

    /* Validate our arguments. */
    if (!section || !name)
        return NULL;

    /* Get the value from the ini entry list. */
    value = sGetIniEntryListValue(entryList, section, name);

    return value;
}

/*
 *  FUNCTION: osEnumerateIniSections
 *
 *  PURPOSE:  Enumerate the given registry.
 *
 *  RETURNS:  eOK
 */

char *osEnumerateIniSections(IniEntry *entryList,
			     void **context, 
			     char **section)
{
    IniEntry *this = *context;

    /* Validate our arguments. */
    if (!section)
	return NULL;

    /* If we're starting from scratch, point to the ini entry list. */
    if (!this)
        this = entryList;
    else
        this = this->next;

    /* Enumerate through the ini entry list. */
    for (this; this; this=this->next)
    {
        /* Have we found a section? */
        if (!this->name && !this->value)
        {
            *context = this;

            *section = this->section;
       
            return this->section; 
        }
    }

    *context = NULL;

    return NULL;
}

/*
 *  FUNCTION: osEnumerateIniValues
 *
 *  PURPOSE:  Enumerate the given registry section.
 *
 *  RETURNS:  eOK
 */

char *osEnumerateIniValues(IniEntry *entryList,
			   void **context, 
			   char *section, 
			   char **name, 
			   char **value)
{
    char temp[4096];

    IniEntry *this = *context;

    /* Validate our arguments. */
    if (!name || !value)
	return NULL;

    /* If we're starting from scratch, point to the ini entry list. */
    if (!this)
        this = entryList;
    else
        this = this->next;

    /* Enumerate through the ini entry list. */
    for (this; this; this=this->next)
    {
        /* Have we found a matching section? */
        if (this->name && 
	    this->value && 
	    misCiStrcmp(section, this->section) == 0)
        {
            *context = this;

	    /* We reexpand the value every time to be safe. */
	    memset(temp, 0, sizeof temp);
    	    misExpandVars(temp, this->value, sizeof temp, NULL);

    	    /* Reset the real/expaned value. */
	    this->realValue = realloc(this->realValue, strlen(temp) + 1);
	    strcpy(this->realValue, temp);

            *name  = this->name;
            *value = this->realValue;

            return this->section;
        }
    }

    *context = NULL;

    return NULL;
}
