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
#include <stddef.h>
#include <string.h>

#include <mocaerr.h>
#include <mocagendef.h>
#include <oslib.h>

static char b36array[] = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

/*
 * Divides dividend (a string) by a divisor (an integer) and returns
 * a remainder as an integer.
 *
 * We expect "dividend" to be a string of digits in the array base36array.
 * (i.e. no sign, and no leading space)  Also, we expect both "base" and
 * "divisor" to be positive integers, and for base to be between 2 and 36
 * (to ensure that we can use our "base36" array to generate numbers).
 */

static int divide(char *dividend,
                  int base,
                  int divisor,
                  char *quotient,
                  int *remainder)
{
    int num = 0;

    int charval;

    char *dpos,
         *qpos;

    *remainder = 0;

    /* Remove Leading Zeros. */
    num = 0;
    qpos = quotient;
    for (dpos=dividend; *dpos && num < divisor; dpos++)
    {
        charval = strchr(b36array, *dpos) - b36array;
        if (charval < 0 || charval > base)
            return -1;

        num = num*base + charval;
    }

    /* We need to at least determine that the quotient is zero, if it is. */
    do
    {
        *qpos++ = b36array[num/divisor];

        num = num % divisor;

        if (*dpos)
        {
            charval = strchr(b36array, *dpos) - b36array;
            if (charval < 0 || charval > base)
                return -1;

            num = num*base + charval;
        }

    } while (*dpos++);

    *qpos = '\0';

    *remainder = num;

    return 0;
}

/*
 * This does pretty basic base conversion. The only unusual thing is the way
 * it represents numbers (As Strings) and manipulates them without trying to
 * represent them as integers. As a result, this is slightly slower than
 * integer base conversion, but it works for ANY size input number.
 * (Currently, we don't have a convention for representing any base higher
 * than 36 or lower than 2 (Is there one?) so passing a base outside that
 * range is an error.
 */

long misBaseConv(const char *inum, int ibase, char *onum, int obase, int size)
{
    int status;

    int pos = size - 1;

    char neg;

    char *ptr,
         *quot;

    /*
     * We can't handle a base outside this range...
     */
    if (ibase < 2 || ibase > 36 || obase < 2 || obase > 36)
        return eERROR;

    /*
     * If it's negative, ignore the sign on conversion, and put it back later.
     * The fact that our divide function expects positive numbers only greatly
     * simplifies things, and makes things run somewhat faster.
     */
    neg = (*inum == '-');

    /*
     * We know our input string is the longest the quotient will ever be -
     * dividing will only make it shorter.
     */
    quot = malloc(strlen(inum) + 1);
    if (!quot)
        OS_PANIC;

    strcpy(quot, inum + neg);

    /*
     * OK, here's the weird part...we start at the END of the string and work
     * our way backwards. This way we avoid recursion at the expense of a
     * memmove() call later.
     */

    onum[pos--] = '\0';

    while (pos >= 0)
    {
        int remainder;

        status = divide(quot, ibase, obase, quot, &remainder);
        if (status != 0)
        {
            misLogError("misBaseConv: Could not devide numbers");
            return eERROR;
        }

        onum[pos--] = b36array[remainder];
    }

    /*
     * Let's free the temporary variable.
     */
    free(quot);

    ptr = onum;
    while (*ptr == '0' && *(ptr+1))
        ptr++;

    /*
     * If the "in" number is negative, make the "out" number negative.
     */
    if (neg)
    {
        *onum = '-';
        memmove(onum+1, ptr, strlen(ptr) + 1);
    }
    else
    {
        memmove(onum, ptr, strlen(ptr) + 1);
    }

    return eOK;
}

/*
 * FUNCTION: misDecToBin
 *
 * PURPOSE:  Convert a decimal string to a binary string.
 *
 * RETURNS:  eOK - All ok.
 *           Some other error code.
 */

long misDecToBin(char *decString, char *binString, long binLength)
{
    long status;

    char *temp;

    /* Don't bother if we don't have a decimal string to work with. */
    if (!strlen(decString) && binLength)
    {
	*binString = '\0';
	return eOK;
    }

    /* Convert the decimal string to a binary string. */
    status = misBaseConv(decString, 10, binString, 2, binLength);
    if (status != eOK)
        return status;

    /* Allocate space for our temporary buffer. */
    temp = malloc(binLength);
    if (!temp)
        OS_PANIC;

    /* Make a temporary copy of the binary string. */
    strcpy(temp, binString);

    /* Zero pad the binary string. */
    sprintf(temp, "%0*s", binLength - 1, binString);

    /* Copy the binary string back in for the caller. */
    strcpy(binString, temp);

    free(temp);

    return eOK;
}

/*
 * FUNCTION: misBinToDec
 *
 * PURPOSE:  Convert a binary string to a decimal string.
 *
 * RETURNS:  eOK - All ok.
 *           Some other error code.
 */

long misBinToDec(char *binString, char *decString, long decLength)
{
    long status;

    /* Don't bother if we don't have a binary string to work with. */
    if (!strlen(binString) && decLength)
    {
	*decString = '\0';
	return eOK;
    }

    /* Convert the binary string to a decimal string. */
    status = misBaseConv(binString, 2, decString, 10, decLength);
    if (status != eOK)
        return status;

    return eOK;
}

/*
 * FUNCTION: misBinToHex
 *
 * PURPOSE:  Convert a binary string to a hexadecimal string.
 *
 * RETURNS:  eOK - All ok.
 *           Some other error code.
 */

long misBinToHex(char *binString, char *hexString, long hexLength)
{
    long status;

    char *temp;

    /* Don't bother if we don't have a binary string to work with. */
    if (!strlen(binString) && hexLength)
    {
	*hexString = '\0';
	return eOK;
    }

    /* Convert the binary string to a hexadecimal string. */
    status = misBaseConv(binString, 2, hexString, 16, hexLength);
    if (status != eOK)
        return status;

    /* Allocate space for our temporary buffer. */
    temp = malloc(hexLength);
    if (!temp)
        OS_PANIC;

    /* Make a temporary copy of the hexadecimal string. */
    strcpy(temp, hexString);

    /* Zero pad the hexadecimal string. */
    sprintf(temp, "%0*s", hexLength - 1, hexString);

    /* Copy the hexadecimal string back in for the caller. */
    strcpy(hexString, temp);

    free(temp);

    return eOK;
}

/*
 * FUNCTION: misHexToBin
 *
 * PURPOSE:  Convert a hexadecimal string to a binary string.
 *
 * RETURNS:  eOK - All ok.
 *           Some other error code.
 */

long misHexToBin(char *hexString, char *binString, long binLength)
{
    long status;

    char *temp;

    /* Don't bother if we don't have a hexadecimal string to work with. */
    if (!strlen(hexString) && binLength)
    {
	*binString = '\0';
	return eOK;
    }

    /* Convert the hexadecimal string to a binary string. */
    status = misBaseConv(hexString, 16, binString, 2, binLength);
    if (status != eOK)
        return status;

    /* Allocate space for our temporary buffer. */
    temp = malloc(binLength);
    if (!temp)
        OS_PANIC;

    /* Make a temporary copy of the binary string. */
    strcpy(temp, binString);

    /* Zero pad the binary string. */
    sprintf(temp, "%0*s", binLength - 1, binString);

    /* Copy the binary string back in for the caller. */
    strcpy(binString, temp);

    free(temp);

    return eOK;
}
