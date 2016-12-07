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
 *#END*************************************************************************/

#include <moca.h>

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#ifdef UNIX	
# include <sys/wait.h>
#endif

#include <mocagendef.h>
#include <mocaerr.h>
#include "osprivate.h"

#ifdef UNIX			/* { */

long osPrintFile(char *filename, 
	         char *device, 
		 int copies, 
		 char *options,
		 int raw)
{
    int status;
    char buffer[2000];

    strcpy(buffer, LPCOMMAND);

    if (options)
    {
	strcat(buffer, " ");
	strcat(buffer, options);
    }

    if (device)
    {
	strcat(buffer, " -d");
	strcat(buffer, device);
    }

    if (copies > 1)
    {
	strcat(buffer, " -n");
	sprintf(buffer + strlen(buffer), "%d", copies);
    }

    strcat(buffer, " \"");
    strcat(buffer, filename);
    strcat(buffer, "\"");

    status = system(buffer);
    if (status == -1)
    {
	misLogError("ERROR: %d - %s", osErrno(), osError());
	misLogError("%s", buffer);
	return ePRINT_FILE_ERROR;
    }
    else if (status != 0)
    {
	misLogError("ERROR: %d - %s", WEXITSTATUS(status), osError());
	misLogError("%s", buffer);
	return ePRINT_FILE_ERROR;
    }

    return eOK;
}

#else	/* WIN32 */ /* }{ */
#include <winspool.h>

#define LEFT_MARGIN 6
#define RIGHT_MARGIN 8
#define TOP_MARGIN  3
#define BOTTOM_MARGIN  3
#define TAB_POS 8

static long os_PrintTextFile(FILE * fp, char *device, char *filename)
{
    HDC PrintDC;
    DOCINFO di;			/* Needed for Printer Control Functions */
    TEXTMETRIC tm;
    int nCharsPerLine, nLinesPerPage, yChar, nIndentAmt;
    int nTopMargin = TOP_MARGIN, nRightMargin = RIGHT_MARGIN, nBottomMargin = BOTTOM_MARGIN,
      nLeftMargin = LEFT_MARGIN;
    int LineNum = 0;
    char inline[1024], outline[1024];

    if ((PrintDC = CreateDC("WINSPOOL", device, NULL, NULL)) == NULL)
    {
        misLogError("CreateDC: %s", osError());
        misLogError("ERROR: Could not create device context");
        return ePRINT_FILE_ERROR;
    }

    /*
     * Set up info for Print job starting.
     */
    memset(&di, 0, sizeof di);
    di.cbSize = sizeof di;
    di.lpszDocName = filename;
    di.lpszOutput = NULL;

    if (StartDoc(PrintDC, &di) <= 0)
    {
        misLogError("StartDoc: %s", osError());
        misLogError("ERROR: Could not start print job");
        return ePRINT_FILE_ERROR;
    }

    /*
     * Get the Font information.
     */
    GetTextMetrics(PrintDC, &tm);

    yChar = tm.tmHeight + tm.tmExternalLeading;
    nCharsPerLine = GetDeviceCaps(PrintDC, HORZRES) / tm.tmAveCharWidth;
    nLinesPerPage = GetDeviceCaps(PrintDC, VERTRES) / yChar;
    nIndentAmt = nLeftMargin * tm.tmAveCharWidth;
    nCharsPerLine -= nRightMargin;
    nLinesPerPage -= (nTopMargin + nBottomMargin);

    while (fgets(inline, sizeof inline, fp))
    {
	char *ii, *oo;
	/*
	 * Since TextOut is pretty dumb, we need to massage the output a
	 * bit.
	 */
	for (ii = inline, oo = outline; *ii; ii++)
	{
	    switch (*ii)
	    {
		/*
		 * Certain Charaters don't get printed.
		 */
	    case '\r':
	    case '\n':
	    case 26:		/* Ctrl-Z...This is DOS, after all */
		break;
		/*
		 * If we get a form feed, eject the page. This might have some
		 * problems with text like:
		 *
		 * TextTexttext<\f>TextTextText
		 *
		 * As both sides of the \f will be put onto the same line on 
		 * the next page.
		 */
	    case '\f':
		if (LineNum == 0)
		    StartPage(PrintDC);
		LineNum = 0;
		EndPage(PrintDC);
		break;
		/*
		 * We'll only support a standard tab size for the whole
		 * document. (No tab ruler will be defined)
		 */
	    case '\t':
		/*
		 * Add spaces to the output until we've hit a tabstop.
		 */
		do
		    *oo++ = ' ';
		while (((oo - outline) % TAB_POS) != 0);
		break;

		/*
		 * Every other character gets added as-is.
		 */
	    default:
		*oo++ = *ii;
		break;
	    }
	}

	/* 
	 * Null Terminate the output String 
	 */
	*oo = '\0';

	/*
	 * Start a new page if we're at line 0.
	 */
	if (LineNum == 0)
	{
	    StartPage(PrintDC);
	}

	/*
	 * Oh yeah, put out the line...
	 */
	TextOut(PrintDC, nIndentAmt, yChar * (nTopMargin + LineNum) + 1,
		outline, (int) strlen(outline) > nCharsPerLine ? nCharsPerLine : (int) strlen(outline));

	/*
	 * If we've gone past the max # of lines/page, finish up.
	 */
	LineNum++;
	if (LineNum >= nLinesPerPage)
	{
	    LineNum = 0;
	    EndPage(PrintDC);
	}
    }
    /*
     * We don't want to end with a blank page.
     */
    if (LineNum > 1)
	EndPage(PrintDC);

    if (EndDoc(PrintDC) <= 0)
    {
        misLogError("EndDoc: %s", osError());
        misLogError("ERROR: Could not end print job");
        return ePRINT_FILE_ERROR;
    }

    if (!DeleteDC(PrintDC))
    {
        misLogError("DeleteDC: %s", osError());
        misLogError("ERROR: Could not delete device context");
        return ePRINT_FILE_ERROR;
    }

    return eOK;
}

static long os_PrintRawFile(FILE * fp, char *device, char *filename)
{
    HANDLE hPrinter;
    DOC_INFO_1 DocInfo;
    DWORD dwJob;
    DWORD dwBytesWritten;
    char buff[2048];
    unsigned long len;

    /* Need a handle to the printer. */
    if (!OpenPrinter(device, &hPrinter, NULL))
    {
        misLogError("OpenPrinter: %s", osError());
        misLogError("ERROR: Could not open printer");
        return ePRINT_FILE_ERROR;
    }

    /* Fill in the structure with info about this "document." */
    DocInfo.pDocName = filename;
    DocInfo.pOutputFile = NULL;
    DocInfo.pDatatype = "RAW";

    /* Inform the spooler the document is beginning. */
    if ((dwJob = StartDocPrinter(hPrinter, 1, (LPSTR) & DocInfo)) == 0)
    {
        misLogError("StartDocPrinter: %s", osError());
        misLogError("ERROR: Could not notify printer of new document");
        ClosePrinter(hPrinter);
        return ePRINT_FILE_ERROR;
    }

    /* Start a page. */
    if (!StartPagePrinter(hPrinter))
    {
        misLogError("StartPagePrinter: %s", osError());
        misLogError("ERROR: Could not notify printer of new page");
        EndDocPrinter(hPrinter);
        ClosePrinter(hPrinter);
        return ePRINT_FILE_ERROR;
    }

    while (0 != (len = fread(buff, 1, sizeof buff, fp)))
    {
        /* Send the data to the printer. */
        if (!WritePrinter(hPrinter, buff, len, &dwBytesWritten))
        {
            misLogError("WritePrinter: %s", osError());
            misLogError("ERROR: Could not write to printer");
            EndPagePrinter(hPrinter);
            EndDocPrinter(hPrinter);
            ClosePrinter(hPrinter);
            return ePRINT_FILE_ERROR;
        }
    }

    /* End the page. */
    if (!EndPagePrinter(hPrinter))
    {
        misLogError("EndPagePrinter: %s", osError());
        misLogError("ERROR: Could not notify printer of end of page");
        EndDocPrinter(hPrinter);
        ClosePrinter(hPrinter);
        return ePRINT_FILE_ERROR;
    }

    /* Inform the spooler that the document is ending. */
    if (!EndDocPrinter(hPrinter))
    {
        misLogError("EndDocPrinter: %s", osError());
        misLogError("ERROR: Could not notify printer of end of document");
        ClosePrinter(hPrinter);
        return ePRINT_FILE_ERROR;
    }

    /* Tidy up the printer handle. */
    if (!ClosePrinter(hPrinter))
    {
        misLogError("ClosePrinter: %s", osError());
        misLogError("ERROR: Could not close printer");
        return ePRINT_FILE_ERROR;
    }

    return eOK;
}

long osPrintFile(char *filename, 
                 char *device, 
                 int copies, 
                 char *options,
		 int raw)
{
    FILE *fp;
    long status, 
         retStatus = eOK;

    if ((fp = osFopen(filename, "r")) == NULL)
	return eFILE_OPENING_ERROR;

    do {
        rewind(fp);

        if (raw)
	    status = os_PrintRawFile(fp, device, filename);
        else
	    status = os_PrintTextFile(fp, device, filename);

        retStatus = (status != eOK) ? status : retStatus;

    } while (--copies > 0);

    fclose(fp);

    return retStatus;
}

#endif /* } */
