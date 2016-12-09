static char RCS_Id[] = "$Id$";
/*#START***********************************************************************
 *
 *  $URL$
 *  $Revision$
 *  $Author$
 *
 *  Description: MOCA Server Object library
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

// MOCAResults.cpp : Implementation of CMOCAResults
#include "stdafx.h"
#include "MOCASrvObj.h"
#include "MOCAResults.h"
#include "Utils.h"
#include "oslib.h"
//
// CMOCAResults
//

_COM_SMARTPTR_TYPEDEF(IMOCAResults, IID_IMOCAResults);

//
// getColNum - translate a variant column index into a column number.
//
static int getColNum(mocaDataRes *res, VARIANT *vIndex)
{
    int ColNum = -1;
    char *ColName;

    switch (V_VT(vIndex))
    {
    	case VT_I2:
	    ColNum = V_I2(vIndex);
	    break;

    	case VT_I4:
	    ColNum = V_I4(vIndex);
	    break;

    	case VT_INT:
	    ColNum = V_INT(vIndex);
	    break;

    	case VT_BSTR:
	    ColName = util_GetMultiByte(V_BSTR(vIndex));
	    ColNum = sqlFindColumn(res, ColName);
	    free(ColName);
	    break;
    }

    return ColNum;
}

//
// MOCAResults::get_Results - Results Property Read
//
STDMETHODIMP CMOCAResults::get_Results(long *pVal)
{
    *pVal = (long)Results;

    return S_OK;
}

//
// MOCAResults::get_Results - Results Property Write. Note that
// this does not free the existing results structure.
//
STDMETHODIMP CMOCAResults::put_Results(long newVal)
{
    Results = (RETURN_STRUCT *)newVal;
    RawResults = srvGetResults(Results);
    Current = sqlGetRow(RawResults);

    return S_OK;
}

//
// MOCAResults::get_Status - Status Property Read
//
STDMETHODIMP CMOCAResults::get_Status(long *pVal)
{
    if (Results)
	*pVal = srvGetReturnStatus(Results);

    return S_OK;;
}

//
// MOCAResults::get_Status - Status Property Write
//
STDMETHODIMP CMOCAResults::put_Status(long newVal)
{
    if (Results)
	srvSetReturnStatus(Results, newVal);

    return S_OK;
}

//
// MOCAResults::get_ResultsEOF - EOF Property Read
//
STDMETHODIMP CMOCAResults::get_ResultsEOF(BOOL *pVal)
{
    if (Current)
	*pVal = FALSE;
    else
	*pVal = TRUE;

    return S_OK;
}

//
// MOCAResults::MoveFirst - Set our current row pointer to the first
// row returned in the results.
//
STDMETHODIMP CMOCAResults::MoveFirst()
{
    Current = sqlGetRow(RawResults);

    return S_OK;
}

//
// MOCAResults::MoveNext - Move our current row pointer to the next row
// in the results.
//
STDMETHODIMP CMOCAResults::MoveNext()
{
    Current = sqlGetNextRow(Current);

    return S_OK;
}


//
// MOCAResults::get_Value - Get a value from a column in the current row.
// This "property" returns a variant, which allows it to return whatever 
// type the underlying data object contains.
//
STDMETHODIMP CMOCAResults::get_Value(VARIANT Index, VARIANT *pVal)
{
    int ColNum = -1;
    char *strValue;

    // Binary varaibles
    long lLen;
    BYTE *byteArray; 
    long l=0;
    SAFEARRAY *saBinary;

    // Initialize the result (output) variant.
    VariantInit(pVal);
	pVal->vt = VT_NULL;
    //V_VT(pVal) = VT_NULL;

    if (RawResults && Current)
    {
	// Lookup the column, if necessary.
	ColNum = getColNum(RawResults, &Index);

	//
	// If it's not one of these datatypes, we can't set the value
	// this way.
	//
	if (!sqlIsNullByPos(RawResults, Current, ColNum))
	{
	    switch (sqlGetDataTypeByPos(RawResults, ColNum))
	    {
		case COMTYP_INT:
		case COMTYP_LONG:
		    pVal->vt = VT_I4;
		    pVal->lVal = sqlGetLongByPos(RawResults, Current, ColNum);
		    break;

		case COMTYP_FLOAT:
		    pVal->vt = VT_R8;
		    pVal->dblVal = sqlGetFloatByPos(RawResults, Current, ColNum);
		    break;

		case COMTYP_CHAR:
		case COMTYP_DATTIM:
		    strValue = sqlGetStringByPos(RawResults, Current, ColNum);

		    if (strValue)
		    {
                        pVal->vt = VT_BSTR; 
                        pVal->bstrVal = util_AllocOleString((char *)strValue);
		    }
		    break;

		case COMTYP_BOOLEAN:
		    pVal->vt = VT_BOOL;
		    pVal->boolVal = (sqlGetBooleanByPos(RawResults, Current, ColNum)?TRUE:FALSE);
		    break;

		case COMTYP_BINARY:
		    // First 8 bytes are length
		    lLen = sqlGetBinaryDataLenByPos(RawResults, Current, ColNum);
		    byteArray = (BYTE *)sqlGetBinaryDataByPos(RawResults, Current, ColNum);
		    saBinary = SafeArrayCreateVector(VT_UI1, 0, lLen);           
            
		    while (l < lLen)
		    {
			HRESULT h = SafeArrayPutElement(saBinary, &l, &byteArray[l]);
			l++;
		    }
            
		    pVal->vt = VT_ARRAY | VT_UI1;
		    pVal->parray = saBinary;
		    break;

		case COMTYP_RESULTS:
	   	    /* PAUL */
		    mocaDataRes *sqlRes = NULL;
		    RETURN_STRUCT *srvRes = NULL;

		    // Use a MOCAResults smart pointer
		    IMOCAResultsPtr spResults;
		    spResults.CreateInstance(CLSID_MOCAResults);

		    // Retrieve a pointer to the mocaDataRes pointer from the
		    // RawResults
		    sqlRes = *(mocaDataRes **) sqlGetValueByPos(RawResults, Current, ColNum);

		    // We keep a Server Results Structure, so we need
		    // to build one from the execution results.
		    srvRes = srvAddSQLResults(sqlRes, eOK);

		    // Put our results pointer (as a long) into the MOCAResults
		    // object.
		    spResults->put_Results((long) srvRes);

		    // We're passing this back, so let's add a
		    // reference to this object
		    // (that way, it won't get deleted).
		    spResults.AddRef();

		    // Pass it back.
		    pVal->vt = VT_DISPATCH;
		    pVal->pdispVal = (IDispatch *) spResults;
		
		    break;
	    }
	}
    }

    return S_OK;
}

//
// MOCAResults::put_Value - Insert a new value into a column in the current
// row.  This assumes that the result set has been opened (see
// MOCAResults::Open), and that a current row does exist. (e.g.
// MOCAResults::AddRow() has been called.).
//
STDMETHODIMP CMOCAResults::put_Value(VARIANT Index, VARIANT newVal)
{
    int ColNum = -1;
    VARIANT tmpVal;
    HRESULT hr;

    //Handle binary data
    void *TempAddr;
    long TempLong;
    SAFEARRAY *saBinary;
    BYTE *byteArray = NULL; 
    long lLen;
    long l = 0;
    HRESULT h;

    // If we don't have a valid result structure or row, we can't very well
    // set values, now, can we?
    if (!RawResults || !Current)
    {
	return E_INVALIDARG;
    }
    
    //
    // Determine if the index passed in is an integer (column number) or
    // a string (column name).  If a string, perform column number lookup.
    //
    ColNum = getColNum(RawResults, &Index);

    //
    // Only do the following if we have a valid column.
    //
    if (ColNum >= 0 && ColNum < sqlGetNumColumns(RawResults))
    {
        long lVal;
	char *strVal;
	double fVal;
	
	// Set up a smart VARIANT, since we don't know exactly what type of value was
	// passed.
	bool isNull = ( newVal.vt == VT_NULL || newVal.vt == VT_EMPTY);

	//
	// Determine what datatype we're supposed to see.
	//
	switch (sqlGetDataTypeByPos(RawResults, ColNum))
	{
	    case COMTYP_INT:
	    case COMTYP_LONG:
	    	if (newVal.vt == VT_I4)
	    	{
		    lVal = newVal.lVal;
	    	}
	    	else if (!isNull)
	    	{
		    VariantInit(&tmpVal);
		    hr = VariantChangeType(&tmpVal, &newVal, 0, VT_I4);
		    if (FAILED(hr))
		    {
			return E_INVALIDARG;
		    }
		    lVal = tmpVal.lVal;
		    VariantClear(&tmpVal);
	    	}
	    	else
	    	{
		    lVal = 0;
	        }
	        
		sql_AddRowItem(RawResults, Current, ColNum, sizeof(long), &lVal);
	    	break;

 	    case COMTYP_BOOLEAN:
	    	if (newVal.vt == VT_BOOL)
	    	{
		    lVal = newVal.boolVal?1:0;
	    	}
	    	else if (!isNull)
	    	{
		    VariantInit(&tmpVal);
		    hr = VariantChangeType(&tmpVal, &newVal, 0, VT_BOOL);
		    if (FAILED(hr))
		    {
			return E_INVALIDARG;
		    }
		    lVal = tmpVal.boolVal?1:0;
		    VariantClear(&tmpVal);
	    	}
	    	else
	    	{
		    lVal = 0;
	    	}
	    
		sql_AddRowItem(RawResults, Current, ColNum, sizeof(long), &lVal);
	        break;

    	    case COMTYP_FLOAT:
	    	if (newVal.vt == VT_R8)
	    	{
		    fVal = newVal.dblVal;
	    	}
	    	else if (!isNull)
	   	{
		    VariantInit(&tmpVal);
		    hr = VariantChangeType(&tmpVal, &newVal, 0, VT_R8);
		    if (FAILED(hr))
		    {
			return E_INVALIDARG;
		    }
		    fVal = tmpVal.dblVal;
		    VariantClear(&tmpVal);
	        }
	    	else
	    	{
		    fVal = 0.0;
	    	}
	    
		sql_AddRowItem(RawResults, Current, ColNum, sizeof(double), &fVal);
	        break;
	    
	    case COMTYP_CHAR:
	    case COMTYP_DATTIM:
	    	if (isNull)
	    	{
		    sql_AddRowItem(RawResults, Current, ColNum, 1, "");
	    	}
	    	else
	    	{
		    if (newVal.vt == VT_BSTR)
		    {
			strVal = util_GetMultiByte(newVal.bstrVal);
		    }
		    else
		    {
			VariantInit(&tmpVal);
			hr = VariantChangeType(&tmpVal, &newVal, 0, VT_BSTR);
			if (FAILED(hr))
			{
			    return E_INVALIDARG;
			}
			strVal = util_GetMultiByte(newVal.bstrVal);
			VariantClear(&tmpVal);
		    }
		    
		    sql_AddRowItem(RawResults, Current, ColNum, strlen(strVal) + 1, strVal);
		    free(strVal);
	    	}
	    	
		break;

	    case COMTYP_RESULTS:
	    /* PAUL */
	    	if (isNull)
	    	{
		    sql_AddRowItem(RawResults, Current, ColNum, 1, "");
	    	}
	    	else
	    	{
	            RETURN_STRUCT *resVal;
		    // Use a MOCAResults smart pointer
		    IMOCAResultsPtr spResults;
		    spResults.CreateInstance(CLSID_MOCAResults);
		    IDispatch *object;

		    if (newVal.vt == VT_DISPATCH)
		    {
			object = newVal.pdispVal;
			hr = (object)->QueryInterface(IID_IMOCAResults, 
		                                      (void **) &spResults);
			if (FAILED(hr)) 
			{
			    return E_INVALIDARG;
			}

			spResults->get_Results((long *) &resVal);
		    }
		    else
			return E_INVALIDARG;
			
		    sql_AddRowItem(RawResults, Current, ColNum, sizeof(void *), &(resVal->ReturnedData));
	    	}
	    	
		break;

	    case COMTYP_BINARY:
		
		if (isNull)
	    	{
		    sql_AddRowItem(RawResults, Current, ColNum, 1, "");
	    	}
		else
		{
		    if ( newVal.vt == (VT_ARRAY | VT_UI1) )
		    {				
			saBinary = newVal.parray;
			h = SafeArrayGetUBound(saBinary, 1, &lLen);
			if (FAILED(h))
		  	    return E_INVALIDARG;
				
			//aloocate the array
			byteArray = (BYTE *)calloc(lLen+1, sizeof(BYTE));
			while (l <= lLen)
			{
			    h = SafeArrayGetElement(saBinary, &l, &byteArray[l]);
			    l++;
			}
				

		    }
		    else
		    {
			return E_INVALIDARG;
		    }
		}
		
		TempAddr = sqlEncodeBinary(byteArray, lLen+1);
		TempLong = sqlEncodeBinaryLen(TempAddr);
		
		sql_AddRowItem(RawResults, Current, ColNum, TempLong, TempAddr);

		free(byteArray);
		free(TempAddr);
	}

    }

    return S_OK;
}

//
// MOCAResults::AddColumn - Add a column into the list of columns
// available in this result set.  The results are not actually created
// until MOCAResults::Open() is called.
//
STDMETHODIMP CMOCAResults::AddColumn(BSTR ColumnName, MOCADataTypes DataType)
{
	return AddColumn2(ColumnName, DataType, 0);
}

//
// MOCAResults::AddColumn2 - Add a column into the list of columns
// available in this result set.  The results are not actually created
// until MOCAResults::Open() is called.
//

STDMETHODIMP CMOCAResults::AddColumn2(BSTR ColumnName, MOCADataTypes DataType, long ColumnLength)
{
    char *strColumnName;

    //
    // If we've already got results, we've already opened the Results object.
    //
    if (Results)
    {
	return E_INVALIDARG;
    }

    //
    // If we haven't got a results list yet (used for building column info),
    // allocate it.
    //
    if (!ResultsList)
    {
	ResultsList = srvCreateResultsList(100);
    }
    
    //
    // Copy the column name into the structure.
    //
    strColumnName = util_GetMultiByte(ColumnName);
    
    switch (DataType)
    {
    case MOCAInt:
	srvBuildResultsList(ResultsList, ResultsListCount, strColumnName,
    			    COMTYP_INT, sizeof(long), 0, 0);
	break;
    case MOCAFloat:
	srvBuildResultsList(ResultsList, ResultsListCount, strColumnName,
    			    COMTYP_FLOAT, sizeof(double), 0, 0);
	break;
    case MOCAString:
	srvBuildResultsList(ResultsList, ResultsListCount, strColumnName,
    			    COMTYP_STRING, ColumnLength, 0, "");
	break;
    case MOCADateTime:
	srvBuildResultsList(ResultsList, ResultsListCount, strColumnName,
    			    COMTYP_DATTIM, 15, 0, "");
	break;
    case MOCABoolean:
	srvBuildResultsList(ResultsList, ResultsListCount, strColumnName,
    			    COMTYP_BOOLEAN, sizeof(moca_bool_t), 0, 0);
	break;
    case MOCAResults:
	srvBuildResultsList(ResultsList, ResultsListCount, strColumnName,
    			    COMTYP_RESULTS, sizeof(mocaDataRes), 0, 0);
	break;
    case MOCABinary:
	srvBuildResultsList(ResultsList, ResultsListCount, strColumnName,
    			    COMTYP_BINARY, ColumnLength, 0, 0);
	
    }
    
    free(strColumnName);
    ResultsListCount++;
    
    return S_OK;
}

//
// MOCAResults::Open - Create a new result set based on the column list
// added with MOCAResults::AddColumn.
//
STDMETHODIMP CMOCAResults::Open(long Status, BSTR ErrorText)
{
    //
    // If we've already got results, we've already opened the Results object.
    //
    if (Results)
    {
	return E_INVALIDARG;
    }

    // Open a result set.
    if (!ResultsListCount)
    {
	char *strErrorText = util_GetMultiByte(ErrorText);
	Results = srvErrorResults(Status, strErrorText, NULL);
	free(strErrorText);
    }
    else
    {
	Results = srvResultsInitList(Status, ResultsListCount, ResultsList);
    }

    RawResults = srvGetResults(Results);
    Current = NULL;
    
    //
    // Free up the list of columns.  From this point forward, they're not needed.
    //
    if (ResultsList)
	free(ResultsList);
    ResultsList = NULL;
    ResultsListCount = 0;

    return S_OK;
}

//
// MOCAResults::Close - We have an open, we may as well have a close.
// This frees the underlying structures and resets all pointers to
// NULL.
//
STDMETHODIMP CMOCAResults::Close()
{
    if (Results)
    {
	if (Results)
	    srvFreeMemory(SRVRET_STRUCT, Results);
	Results = NULL;
	RawResults = NULL;
	Current = NULL;

	if (ResultsList)
	    free(ResultsList);
	ResultsList = NULL;
	ResultsListCount = 0;
    }
    return S_OK;
}

//
// MOCAResults::AddRow - Allocate a new row for our results.  This row's
// data can then be set by calling MOCAResults::put_Value()
//
STDMETHODIMP CMOCAResults::AddRow()
{

    Current = sql_AllocateRow(RawResults);
    RawResults->NumOfRows++;
    Results->rows++;

    return S_OK;
}

//
// MOCAResults::get_DataType - Find out the datatype of a column.
// 
STDMETHODIMP CMOCAResults::get_DataType(VARIANT Index, MOCADataTypes *pVal)
{
    int ColNum = -1;

    *pVal = MOCAUnknown;

    //
    // Determine if the index passed in is an integer (column number) or
    // a string (column name).  If a string, perform column number lookup.
    //
    if (RawResults)
    {
	ColNum = getColNum(RawResults, &Index);

	switch (sqlGetDataTypeByPos(RawResults, ColNum))
	{
	case COMTYP_INT:
	case COMTYP_LONG:
	    *pVal = MOCAInt;
	    break;

	case COMTYP_FLOAT:
	    *pVal = MOCAFloat;
	    break;

	case COMTYP_CHAR:
	    *pVal = MOCAString;
	    break;

	case COMTYP_DATTIM:
	    *pVal = MOCADateTime;
	    break;

	case COMTYP_BOOLEAN:
	    *pVal = MOCABoolean;
	    break;

	case COMTYP_RESULTS:
	    *pVal = MOCAResults;
	    break;	
	
	case COMTYP_BINARY:
	    *pVal = MOCABinary;
	    break;	
	}
    }
    return S_OK;
}

//
// MOCAResults::get_Rows - Get the number of rows in a result set.
//
STDMETHODIMP CMOCAResults::get_Rows(long *pVal)
{
    if (Results && RawResults)
	*pVal = sqlGetNumRows(RawResults);
    else
	*pVal = 0;
    return S_OK;
}

//
// MOCAResults::get_Columns - Get the number of columns in a result set.
//
STDMETHODIMP CMOCAResults::get_Columns(long *pVal)
{
    if (Results && RawResults)
	*pVal = sqlGetNumColumns(RawResults);
    else
	*pVal = 0;
    return S_OK;
}

//
// MOCAResults::get_ColumnPosition - Get the position of a column, by name
//
STDMETHODIMP CMOCAResults::get_ColNum(BSTR ColName, long *pVal)
{
    char *strColName = util_GetMultiByte(ColName);
    if (Results)
	*pVal = sqlFindColumn(RawResults, strColName);
    else
	*pVal = -1;

    free(strColName);

    return S_OK;
}

//
// MOCAResults::get_ColumnName - Get the name of a column, by position
//
STDMETHODIMP CMOCAResults::get_ColName(long ColNum, BSTR *pVal)
{
    char *strColName = NULL;

    if (Results)
	strColName = sqlGetColumnName(RawResults, ColNum);
    
    *pVal = util_AllocOleString(strColName?strColName:"");

    return S_OK;
}

//
// MOCAResults::AddErrorArg - Add an error argument to the current result set.
//
STDMETHODIMP CMOCAResults::AddErrorArg(BSTR Name, VARIANT Value, BOOL DoLookup)
{
    // Get the argument name
    char *strName = util_GetMultiByte(Name);

    long lValue;
    char *strValue;
    double dValue;

    switch(Value.vt)
    {
    case VT_I2:
	lValue = (long) V_I2(&Value);
	srvResultsErrorArg(Results, strName, COMTYP_INT, lValue, 0);
	break;
    case VT_I4:
	lValue = (long) V_I4(&Value);
	srvResultsErrorArg(Results, strName, COMTYP_INT, lValue, 0);
	break;
    case VT_INT:
	lValue = (long) V_INT(&Value);
	srvResultsErrorArg(Results, strName, COMTYP_INT, lValue, 0);
	break;

    case VT_R4:
	dValue = (double) V_R4(&Value);
	break;
    case VT_R8:
	dValue = (double) V_R8(&Value);
	srvResultsErrorArg(Results, strName, COMTYP_FLOAT, dValue, 0);
	break;

    case VT_BSTR:
	strValue = util_GetMultiByte(V_BSTR(&Value));
	srvResultsErrorArg(Results, strName, COMTYP_STRING, strValue, DoLookup);
	free(strValue);
	break;
    }
    free(strName);

    return S_OK;
}


STDMETHODIMP CMOCAResults::get_DefinedLength(VARIANT Index, long *pVal)
{
    int ColNum = -1;

    *pVal = 0;

    //
    // Determine if the index passed in is an integer (column number) or
    // a string (column name).  If a string, perform column number lookup.
    //
    if (RawResults)
    {
	ColNum = getColNum(RawResults, &Index);

	*pVal = sqlGetDefinedColumnLenByPos(RawResults, ColNum);
    }

	return S_OK;
}
