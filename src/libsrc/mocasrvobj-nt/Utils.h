/* Default code page beginning with MOCA NG is UTF-8 (65001) */
#define DEFAULT_CODEPAGE 65001

BSTR util_AllocOleString(char* pStr);
char *util_GetMultiByte(BSTR bstrIn);

