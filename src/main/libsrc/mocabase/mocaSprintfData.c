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
 *  Copyright (c) 2005
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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <common.h>
#include <mocaerr.h>
#include <srvlib.h>
#include <sqllib.h>
#include <mislib.h>

/* NOTE: Do not include %S format */
#define STRING_SPECIFIERS "s"  
#define INTEGER_SPECIFIERS "diouxXcCp"
/* NOTE: Do not include %a, %A format */
#define FLOAT_SPECIFIERS "eEfFgG"
/* NOTE: Do not include %n format */

#define DYNSPRINTF(cnt,str,fmt,data,wid1,wid2) \
        if(cnt==1) \
        { \
            misDynSprintf(str, fmt, data); \
        } \
        else if(cnt==2) \
        { \
            misDynSprintf(str, fmt, wid1, data); \
        } \
        else if(cnt==3) \
        { \
            misDynSprintf(str, fmt, wid1, wid2, data); \
        } 

/* Count the number of arguments this format needs to consume 
 *  and return data type 
 *
 * NOTE: handle formats with dynamic widths (e.g. %*d or %*.*s)
 */
static long CountFormatArgs(char *i_format, char *o_DataType)
{   long count = 1;
    /* Default to unsupported datatype */
    char dtype = COMTYP_GENERIC;
    long lcv;
    char *p,*e,*one,*two;

    /* Look for last percent character in format */
    p=NULL;
    for(lcv=strlen(i_format)-1;lcv>=0 && !(p);lcv--)
    {
        if(i_format[lcv]=='%')
        {
            if(lcv>0 && i_format[lcv-1]=='%')
            {
                lcv--;
            }
            else
            {
                p=i_format+lcv;
            }
        }
    }
    if(p)
    {   int c;
        /* Find end of format (conversion specifier) */
        c=strcspn(p, STRING_SPECIFIERS INTEGER_SPECIFIERS FLOAT_SPECIFIERS);
        if(c>0)
        {   char conversion_specifier = p[c];
            e=p+c;
            two=strstr(p,"*.*");
            if(two && two < e)
            {
                count++;
                count++;
            }
            else
            {
                one=strstr(p,"*");
                if(one && one < e)
                {
                     count++;
                }
            }
            if(strchr(STRING_SPECIFIERS, conversion_specifier))
            {
                dtype = COMTYP_STRING;
            }
            if(strchr(FLOAT_SPECIFIERS, conversion_specifier))
            {
                dtype = COMTYP_FLOAT;
            }
            if(strchr(INTEGER_SPECIFIERS, conversion_specifier))
            {
                dtype = COMTYP_INT;
            }
            if(o_DataType)
            {
                *o_DataType = dtype;
            }
        }
    }
    return count;
}

/* Format long data into using format and width arguments provided
 *   the long data will be converted to the format's data type provided,  
 *   so sprintf will not crash.
 */
static void DynSprintf_ldata(char Formats_DataType, 
    char **tmp_formatted_data_ptr, char *format, 
    long format_arg_count, long width_arg1, long width_arg2, 
    long ldata)
{
    switch(Formats_DataType)
    {
    case COMTYP_STRING:
        {   char TempString[100];
            sprintf(TempString, "%ld", ldata);
            DYNSPRINTF(format_arg_count,tmp_formatted_data_ptr, 
                format, TempString, width_arg1, width_arg2);
            break;
        }
    case COMTYP_INT:
            DYNSPRINTF(format_arg_count,tmp_formatted_data_ptr, 
                format, ldata, width_arg1, width_arg2);
        break;
    case COMTYP_FLOAT:
        {   double ddata = (double)ldata;
            DYNSPRINTF(format_arg_count,tmp_formatted_data_ptr, 
                format, ddata, width_arg1, width_arg2);
            break;
        }
    default:
            /* Invalid data type just print format out */
            DYNSPRINTF(1,tmp_formatted_data_ptr, 
                "%s", format, width_arg1, width_arg2);
    }
    return;
}

/* Format double data into using format and width arguments provided
 *   the double data will be converted to the format's data type provided,  
 *   so sprintf will not crash.
 */
static void DynSprintf_ddata(char Formats_DataType, 
    char **tmp_formatted_data_ptr, char *format, 
    long format_arg_count, long width_arg1, long width_arg2, 
    double ddata)
{
    switch(Formats_DataType)
    {
    case COMTYP_STRING:
        {   char TempString[100];
            sprintf(TempString, "%lf", ddata);
            DYNSPRINTF(format_arg_count,tmp_formatted_data_ptr, 
                format, TempString, width_arg1, width_arg2);
            break;
        }
    case COMTYP_INT:
        {   long ldata = (long)ddata;
            DYNSPRINTF(format_arg_count,tmp_formatted_data_ptr, 
                format, ldata, width_arg1, width_arg2);
        break;
        }
    case COMTYP_FLOAT:
            DYNSPRINTF(format_arg_count,tmp_formatted_data_ptr, 
                format, ddata, width_arg1, width_arg2);
            break;
    default:
            /* Invalid data type just print format out */
            DYNSPRINTF(1,tmp_formatted_data_ptr, 
                "%s", format, width_arg1, width_arg2);
    }
    return;
}

/* Format string data into using format and width arguments provided
 *   the string data will be converted to the format's data type provided,  
 *   so sprintf will not crash.
 */
static void DynSprintf_data(char Formats_DataType, 
    char **tmp_formatted_data_ptr, char *format, 
    long format_arg_count, long width_arg1, long width_arg2, 
    char *data)
{
    switch(Formats_DataType)
    {
    case COMTYP_STRING:
            DYNSPRINTF(format_arg_count,tmp_formatted_data_ptr, 
                format, data, width_arg1, width_arg2);
            break;
    case COMTYP_INT:
        {   long ldata=atol(data);
            DYNSPRINTF(format_arg_count,tmp_formatted_data_ptr, 
                format, ldata, width_arg1, width_arg2);
        break;
        }
    case COMTYP_FLOAT:
        {   double ddata=atof(data);
            DYNSPRINTF(format_arg_count,tmp_formatted_data_ptr, 
                format, ddata, width_arg1, width_arg2);
            break;
        }
    default:
            /* Invalid data type just print format out */
            DYNSPRINTF(1,tmp_formatted_data_ptr, 
                "%s", format, width_arg1, width_arg2);
    }
    return;
}

LIBEXPORT RETURN_STRUCT *mocaSprintfData(char *i_format, 
     char *i_args, 
     char *i_output_column_name)
{
    SRV_RESULTS_LIST *reslist;
    RETURN_STRUCT *results;
    char *tmp_formatted_data = NULL;
    char *formatted_data = NULL;
    char *name;
    char *format;
    char *formats[300];
    int   format_count;
    char *names[300];
    int   count;
    char *p,*q;
    char  dtype;
    void *data;
    int   ii,jj;

    /* Missing arguments */
    if(!i_format)
    {
	results = srvResults(eOK, NULL);
    }

    memset(names, '\0', sizeof(names));
    memset(formats, '\0', sizeof(formats));

    /*
     * Count the columns and keep column names in a local list
     */
    for (count = 0, p = i_args; p && (names[count] = misStrsep(&p, ","));)
    {
	/* Trim the string, and if it's not empty, store it */
	misTrim(names[count]);
	if (names[count][0])
	    count++;
    }

    /* Only parse and allocate formats if arguments are passed */
    if(count > 0)
    {
        /*
         * Count the formats and keep formats names in a local list
         */
        for (format_count = 0, p = NULL, q = i_format;(q = strchr(q, '%'));q++)
        {
            /* Ignore escaped % */
	    if(q[1] == '%')
            {
                 q++;
            }
            else
            {
                /* Start picking up formats after first non-escaped % */
                if(p)
                {
                    formats[format_count] = calloc(1, q-p+1);
                    strncpy(formats[format_count++], p, q-p);
                    /* Save start of next format */
                    p=q;
                }
                else
                {
                    /* Save start of first format */
                    p = i_format;
                }
            }
        }
        if(p)
        {
            formats[format_count] = calloc(1, strlen(p)+1);
            strcpy(formats[format_count++], p);
        }
    }

    reslist = srvCreateResultsList(1);

    /* Only format data if have data and formats */
    if (count>0 && format_count>0)
    {
        /* Loop thru each individual format */
	for (ii=0,jj=0;ii<count && jj<format_count;jj++ /* only inc formats */)
	{   int format_arg_count, kk;
	    char Formats_DataType;
	    long width_arg1=0, width_arg2=0; /* width args */
	    format = formats[jj];

	    /* handle formats with dynamic widths  (e.g. %*d or %*.*s)  */
            format_arg_count = CountFormatArgs(format, &Formats_DataType);

            /* Loop thru arguments for current format (handle width arguments) */
	    for (kk=0;kk<format_arg_count;kk++,ii++)
	    {
	        name = names[ii];
		
                /* Get data off of MOCA stack */
                if(srvGetNeededElement(name, NULL, &dtype, &data)!=eOK || !(data))
                {
                    /*
                     * If column not on MOCA stack or value is null
                     * assume empty string for data, to elimate crash
                     */
                    dtype = COMTYP_STRING;
                    data = "";
                }

	        switch(dtype)
	        {
	        case COMTYP_INT:
	        case COMTYP_LONG:
	        case COMTYP_BOOLEAN:
                {   long ldata = *(long *)data;

                    if(format_arg_count==1)
                    {
                        DynSprintf_ldata(Formats_DataType, 
                            &tmp_formatted_data, format, 
                            format_arg_count, width_arg1, width_arg2, 
                            ldata);
                    }
		    else /* Handle width arguments */
		    {
                        /* If last argument */
                        if(kk==format_arg_count-1)
                        {
                            DynSprintf_ldata(Formats_DataType, 
                                 &tmp_formatted_data, format, 
                                 format_arg_count, width_arg1, width_arg2, 
                                 ldata);
                        }
                        else
                        {
                            if(kk==0)
                            {
                                width_arg1 = ldata;
                            }
                            else
                            {
                                width_arg2 = ldata;
                            }
                        }
		    }
		    break;
                }

	        case COMTYP_FLOAT:
                {   double fdata = *(double *)data;
                    if(format_arg_count==1)
                    {
                        DynSprintf_ddata(Formats_DataType, 
                                 &tmp_formatted_data, format, 
                                 format_arg_count, width_arg1, width_arg2, 
                                 fdata);
                    }
		    else /* Handle width arguments */
		    {
                        /* If last argument */
                        if(kk==format_arg_count-1)
                        {
                            DynSprintf_ddata(Formats_DataType, 
                                 &tmp_formatted_data, format, 
                                 format_arg_count, width_arg1, width_arg2, 
                                 fdata);
                        }
                        else
                        {
                            if(kk==0)
                            {
                                width_arg1 = (long)fdata;
                            }
                            else
                            {
                                width_arg2 = (long)fdata;
                            }
                        }
		    }
		    break;
                }

	        case COMTYP_DATTIM:
	        case COMTYP_STRING:
                {
                    if(format_arg_count==1)
                    {
                        DynSprintf_data(Formats_DataType, 
                                 &tmp_formatted_data, format, 
                                 format_arg_count, width_arg1, width_arg2, 
                                 (char*)data);
                    }
		    else /* Handle width arguments */
		    {
                        /* If last argument */
                        if(kk==format_arg_count-1)
                        {
                            DynSprintf_data(Formats_DataType, 
                                 &tmp_formatted_data, format, 
                                 format_arg_count, width_arg1, width_arg2, 
                                 (char *)data);
                        }
                        else
                        {
                            if(kk==0)
                            {
                                width_arg1 = (long)atol((char*)data);
                            }
                            else
                            {
                                width_arg2 = (long)atol((char*)data);
                            }
                        }
		    }
		    break;
                }

	        default:
	            /* Unsupported datatype just print format */
                    misDynStrcpy(&tmp_formatted_data, format);
	        }
            }
            misDynStrcat(&formatted_data, tmp_formatted_data);
	    free(formats[jj]);
	    formats[jj]=NULL;
	}
        /* Use up to reset of the formats */
	for (;jj<format_count;jj++)
	{
            misDynStrcat(&formatted_data, formats[jj]);
	    free(formats[jj]);
	    formats[jj]=NULL;
	}
    }
    else
    {
        /* Just return format back */
        misDynStrcpy(&formatted_data, i_format);
    }
    srvBuildResultsList(reslist, 0, 
       i_output_column_name?i_output_column_name:"list", COMTYP_STRING, 
       strlen (formatted_data), 0, (char *)formatted_data);
    results = srvResultsList(eOK, 1, reslist);
    srvFreeResultsList(reslist);
    free(formatted_data);
    formatted_data=NULL;
    free(tmp_formatted_data);
    tmp_formatted_data=NULL;

    return results;
}
