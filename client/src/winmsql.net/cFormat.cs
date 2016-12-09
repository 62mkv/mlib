using System;

namespace RedPrairie.MCS.WinMSQL
{
    /// <summary>
    /// Summary description for Class1.
    /// </summary>
    public class cFormat
    {
        private const int cMaxItr = 9999; //Loop iterations within each recursion level
        private const int cMaxRec = 999; //Recursion iterations

        private cLookup[] colLookup = new cLookup[39];

        private int gItrCnt; //Counts # Iterations within loop
        private int gRecCnt; //Counts # Recutions

        private int gLookupNum;
        private int gLookupPrevNum;

        private bool gDebug; //Output Debuging information
        private string gDebugPre; //Output Debuging information
        private string gDebugSuf; //Output Debuging information

        private int gMaxCmntWidth; //Maximum Comment Width
        private bool gBraceNewLine; //Put { brace on new line
        private bool gPipeNewLine; //put | pipe on new line
        private bool gTrimSpace; //Trim Spaces within input MSQL
        private bool gLowerCase; //Conver input MSQL to lower case
        private bool gTrim; //Trim without Conversion
        private string gSQL_Output; //This is the output of the program

        private bool gNoStrParse;
        private bool gInString;
        private bool gDisableComma;
        private int gErrorByte;
        private bool gSmallCmdWrap;
        private int gSmallCmdLen;

		public cFormat(ctlOptions opts)
		{
			gTrimSpace = opts.TrimExtraSpaces;
			gLowerCase = opts.ConvertToLower;
			gTrim = opts.TrimSource;
			gDebug = opts.Debug;
			gMaxCmntWidth = opts.ColumnsPerComment;
			gBraceNewLine = opts.BracketOnNewLine;
			gPipeNewLine = opts.PipeOnNewLine;
			gNoStrParse = opts.ParseInStrings;
			gDisableComma = !opts.CommaOnNewLine;
		}


        public string MSQL_Format(string sMSQL_Input)
        {
            // The following used to be parameters, but are hard-coded for now.
            /*bool bTrimSpace = true;
            bool bLowerCase = true;
            bool bTrim = false;
            bool lDebug = false;
            int lMaxCommentWidth = 40;
            bool bBraceNewLine = true;
            bool bPipeNewLine = true;
            bool bNoStrParse = false;
            string sColor = "blue";*/
            int lErrorByte = 0;
            bool bSmallCmdWrap = false;
            int lSmallCmdLen = 60;

			gErrorByte = lErrorByte;
			gSmallCmdWrap = bSmallCmdWrap;
			gSmallCmdLen = lSmallCmdLen;
			bool bColorParse = false; //TODO: Make this work
			string sColor = "blue";


            gRecCnt = 0;
            // set to value of ']' (note was 20 for '[')
            gLookupNum = 21; //prevent initial failure in finding '['
            gInString = false;

            gSQL_Output = "";
            Init_Lookup();
            if (gTrim)
            {
                //If Not gTrimSpace Then MsgBox "This is only effective if '" & chkTrimSpace.Caption & "' is checked"
                gSQL_Output = FullTrim(sMSQL_Input).Trim(" \t".ToCharArray());
            }
            else
            {
                string strLocal = " " + FullTrim(sMSQL_Input);
                Parse_Select(ref strLocal, 0, 0);
            }
            if (bColorParse)
            {
                if (! gDebug & gErrorByte > 0 & gSQL_Output.Length > gErrorByte)
                {
                    gSQL_Output = ColorSpecificByte(gSQL_Output, gErrorByte);
                }
                gSQL_Output = Color_Output(sColor);
            }
            return gSQL_Output;
        }

        private void Parse_Select(ref string fSql, int iStart, int lStart)
        {
            int oStart = 0;
            int vRet = 0;
            bool vSQL = false;
            bool vCmd = false;
            bool vNotDone;
            int vReturn = 0;

            vNotDone = true;
            while (vNotDone)
            {
                if (gItrCnt > cMaxItr)
                {
                    gSQL_Output = "Error: Max Iteration Count:" + cMaxRec + " reached";
                    return;
                }

                gLookupPrevNum = gLookupNum;
                vNotDone = Check_Info(fSql, ref vRet, ref gLookupNum);

                if (vNotDone)
                {
                    if (colLookup[gLookupNum].InSQL > 0) vSQL = (colLookup[gLookupNum].InSQL == 1);
                    if (colLookup[gLookupNum].InCMD > 0) vCmd = (colLookup[gLookupNum].InCMD == 1);

                    int vStart = iStart + colLookup[gLookupNum].Indent;

                    if (vSQL) vStart = vStart + colLookup[gLookupNum].InSQL_Indent;
                    if (vCmd) vStart = vStart + colLookup[gLookupNum].InCMD_Indent;

                    //********** PARSE START **********
                    if ((colLookup[gLookupNum].AppendLookup == 0) & vReturn > 0)
                    {
                        //gSQL_Output = gSQL_Output & Space(oStart)
                        if (gDebug)
                        {
                            gSQL_Output = gSQL_Output + gDebugPre + "-" + gDebugSuf;
                        }
                    }
                    vReturn = 0;
                    bool vTrimSpace = false; //did you just trim a space
                    
                    int vSpace = gSQL_Output.Length;

                    if (colLookup[gLookupNum].CommentWrap != 3)
                    {
                        if (colLookup[gLookupPrevNum].SpaceReq & colLookup[gLookupPrevNum].AppendLookup == 0)
                        {
                            if (gSQL_Output.Length > 1 &&
                                gSQL_Output.Substring(gSQL_Output.Length - 2) != Environment.NewLine &
                                gSQL_Output.Substring(gSQL_Output.Length - 1) != " ")
                            {
                                gSQL_Output = gSQL_Output + " ";
                            }
                        }
                        if (colLookup[gLookupPrevNum].IgnoreParsing != 0 & gNoStrParse)
                        {
                            gSQL_Output = gSQL_Output + fSql.Substring(0, vRet - 1);
                        }
                        else
                        {
                            gSQL_Output = gSQL_Output +
                                          padnewline(
                                              Convert.ToString(fSql.Substring(0, vRet - 1)).Trim(" \t".ToCharArray()),
                                              iStart);
                        }
                        if (vRet > 1)
                        {
                            vTrimSpace = fSql.Substring((vRet - 1) - 1, 1) == " ";
                        }
                        fSql = fSql.Substring(fSql.Length - (fSql.Length - (vRet - 1)));
                    }
                    else
                    {
                        if (colLookup[gLookupPrevNum].CommentWrap == 2)
                        {
                            gSQL_Output = gSQL_Output +
                                          string.Empty.PadLeft(colLookup[colLookup.GetUpperBound(0)].Indent);
                        }
                        if (((Len_from_last_newline() - iStart + vRet) < Math.Abs(gMaxCmntWidth)) |
                            colLookup[gLookupNum].CommentWrap == 4)
                        {
                            gSQL_Output = gSQL_Output +
                                          Convert.ToString(fSql.Substring(0, vRet - 1)).Trim(" \t".ToCharArray());
                            fSql = fSql.Substring(fSql.Length - (fSql.Length - (vRet - 1)));
                        }
                        else
                        {
                            int tRet;
                            int tRet2;

                            fSql = fSql.Trim(" \t".ToCharArray());
                            tRet = (fSql.IndexOf(" ", ((fSql.Substring(0, 1) == " ") ? 2 : 1) - 1) + 1);
                            tRet2 = 1;
                            vRet =
                                (fSql.IndexOf(colLookup[gLookupNum].lookup.ToLower(),
                                              ((fSql.Substring(0, 1) == " ") ? 2 : 1) - 1) + 1);
                            while ((iStart + colLookup[colLookup.GetUpperBound(0)].Indent + tRet) <
                                   Math.Abs(gMaxCmntWidth))
                            {
                                if (tRet < (vRet - 1) & tRet > 1)
                                {
                                    gSQL_Output = gSQL_Output +
                                                  Convert.ToString(fSql.Substring(tRet2 - 1, tRet - tRet2)).Trim(
                                                      " \t".ToCharArray()) + " ";
                                }
                                else
                                {
                                    gSQL_Output = gSQL_Output +
                                                  Convert.ToString(fSql.Substring(tRet2 - 1, vRet - tRet2)).Trim(
                                                      " \t".ToCharArray()) + " ";
                                    tRet = tRet2 + 1;
                                }
                                tRet2 = tRet;
                                tRet = (fSql.IndexOf(" ", tRet2) + 1);
                            }
                            if (tRet2 == 1)
                            {
                                gSQL_Output = gSQL_Output +
                                              Convert.ToString(fSql.Substring(0, tRet - 1)).Trim(" \t".ToCharArray()) +
                                              " ";
                                fSql = fSql.Substring(fSql.Length - (fSql.Length - (tRet - 1)));
                            }
                            else
                            {
                                fSql = fSql.Substring(fSql.Length - (fSql.Length - (tRet2 - 1)));
                            }
                            gLookupNum = colLookup.GetUpperBound(0);
                        }
                    }

                    vSpace = vSpace - gSQL_Output.Length;
                    if (colLookup[gLookupNum].NewLineBefore != 0)
                    {
                        if (gSQL_Output.Length > 0 & colLookup[gLookupNum].NewLineBefore < 3 &
                            !gSQL_Output.Trim(" \t".ToCharArray()).EndsWith(Environment.NewLine))
                        {
                            gSQL_Output = gSQL_Output + Environment.NewLine;
                        }
                        else if (gSQL_Output.Length > 0 & colLookup[gLookupNum].NewLineBefore > 3 &
                                 !gSQL_Output.Trim(" \t".ToCharArray()).EndsWith(Environment.NewLine))
                        {
                            gSQL_Output = FTrim(gSQL_Output) + Environment.NewLine;
                        }

                        if ((colLookup[gLookupNum].NewLineBefore%3) == 1)
                        {
                            gSQL_Output = gSQL_Output.Trim(" \t".ToCharArray()) + string.Empty.PadLeft(lStart);
                        }
                        else if ((colLookup[gLookupNum].NewLineBefore%3) == 2)
                        {
                            gSQL_Output = gSQL_Output.Trim(" \t".ToCharArray()) + string.Empty.PadLeft(iStart);
                        }
                        if (gDebug)
                        {
                            gSQL_Output = gSQL_Output + gDebugPre + ">" + gDebugSuf;
                        }
                    }

                    if (colLookup[gLookupNum].AppendLookup != 0)
                    {
                        // remove space so "if ((true) )" will become "if ((true))"
                        if ((colLookup[gLookupNum].AppendLookup == 3 | colLookup[gLookupNum].AppendLookup == 4) &
                            (colLookup[gLookupPrevNum].AppendLookup == 3 | colLookup[gLookupPrevNum].AppendLookup == 4) &
                            colLookup[gLookupNum].IgnoreParsing == 0)
                        {
                            if (gSQL_Output.Substring(gSQL_Output.Length - 1) == " ")
                            {
                                gSQL_Output = gSQL_Output.Substring(0, gSQL_Output.Length - 1);
                            }
                        }
                        if ((colLookup[gLookupNum].AppendLookup == 2 | colLookup[gLookupNum].AppendLookup == 4) &
                            vSpace != 0 &
                            ((gInString & vTrimSpace) | (! gInString & colLookup[gLookupNum].IgnoreParsing == 0)))
                        {
                            gSQL_Output = gSQL_Output + " ";
                        }
                        if (colLookup[gLookupNum].SpaceReq) //And colLookup[gLookupNum].AppendLookup = 0 Then
                        {
                            if (gSQL_Output.Length > 1 &&
                                (gSQL_Output.Substring(gSQL_Output.Length - 2) != Environment.NewLine &
                                gSQL_Output.Substring(gSQL_Output.Length - 1) != " "))
                            {
                                gSQL_Output = gSQL_Output + " ";
                            }
                        }

                        gSQL_Output = gSQL_Output +
                                      fSql.Substring(0, colLookup[gLookupNum].lookup.Length).Trim(" \t".ToCharArray());
                        fSql = fSql.Substring(fSql.Length - (fSql.Length - colLookup[gLookupNum].lookup.Length));

                        if ((colLookup[gLookupNum].AppendLookup == 3 | colLookup[gLookupNum].AppendLookup == 4) &
                            colLookup[gLookupNum].IgnoreParsing == 0)
                        {
                            gSQL_Output = gSQL_Output + " ";
                        }
                    }

                    if (colLookup[gLookupNum].NewLineAfter != 0 & (vCmd | colLookup[gLookupNum].InCMD_Indent == 0))
                    {
                        if ((vSpace != 0 | colLookup[gLookupPrevNum].AppendLookup <= 0 |
                             colLookup[gLookupPrevNum].NewLineAfter <= 0) & colLookup[gLookupNum].CommentWrap != 1)
                        {
                            if (gSQL_Output.Trim(" \t".ToCharArray()).Length > 1 &&
                                (gSQL_Output.Length > 0 &
                                gSQL_Output.Trim(" \t".ToCharArray()).Substring(
                                    gSQL_Output.Trim(" \t".ToCharArray()).Length - 2) != Environment.NewLine))
                            {
                                gSQL_Output = gSQL_Output + Environment.NewLine;
                            }
                            if (colLookup[gLookupNum].NewLineAfter == 1)
                            {
                                gSQL_Output = gSQL_Output.Trim(" \t".ToCharArray()) + string.Empty.PadLeft(vStart);
                            }
                            else
                            {
                                gSQL_Output = gSQL_Output.Trim(" \t".ToCharArray()) + string.Empty.PadLeft(lStart);
                            }
                            if (gDebug)
                            {
                                gSQL_Output = gSQL_Output + gDebugPre + "<" + gDebugSuf;
                            }
                        }
                    }

                    if (! gInString)
                    {
                        fSql = fSql.Trim(" \t".ToCharArray());
                        fSql = fSql + " ";
                    }
                    if ((colLookup[gLookupNum].AppendLookup != 0 | colLookup[gLookupNum].Prefix != "") & ! gInString)
                    {
                        fSql = " " + fSql;
                    }
                    //********** PARSE END **********
                    //debuging info output
                    if (gDebug)
                    {
                        if (vSQL)
                        {
                            gSQL_Output = gSQL_Output + gDebugPre + "~" + gDebugSuf;
                        }
                        else
                        {
                            gSQL_Output = gSQL_Output + gDebugPre + "^" + gDebugSuf;
                        }
                    }

                    //Recursively parse parenthesis
                    if (colLookup[gLookupNum].Recurse_Start)
                    {
                        vReturn = colLookup[gLookupNum].NewLineAfter;
                        if (colLookup[gLookupNum].Indent >= 0)
                        {
                            Parse_Select(ref fSql, oStart + colLookup[gLookupNum].Indent, iStart);
                        }
                        else
                        {
                            Parse_Select(ref fSql, Len_from_last_newline() + Math.Abs(colLookup[gLookupNum].Indent), 0);
                        }
                        if (gRecCnt > cMaxRec)
                        {
                            return;
                        }
                        if (vSQL)
                        {
                            vReturn = 0;
                        }
                        vStart = oStart;
                    }
                    else if (colLookup[gLookupNum].Recurse_Stop)
                    {
                        return;
                    }

                    oStart = vStart;
                    gItrCnt++;
                }
            }
            if (iStart == 0)
            {
                gSQL_Output = gSQL_Output + fSql.Trim(" \t".ToCharArray());
                gSQL_Output = gSQL_Output.Trim(" \t".ToCharArray());
            }
        }

        private bool Check_Info(string fSql, ref int oRet, ref int oLookupNum)
        {
            bool tempCheck_Info = false;
            int vBeg;
            int x;
            int vStop;
            int vStopLookupNum = 0;
            int vStart;
            vStart = gSmallCmdLen;
            vStop = gSmallCmdLen;
            oRet = 100000;
            if (gLookupPrevNum == colLookup.GetUpperBound(0))
            {
                vBeg = ((colLookup[gLookupPrevNum].AppendLookup != 0) ? 0 : 2);
            }
            else
            {
                vBeg = ((colLookup[gLookupPrevNum].AppendLookup != 0) ? 0 : colLookup[gLookupPrevNum].lookup.Length);
            }
            int tempFor1 = colLookup.GetUpperBound(0);
            for (x = colLookup.GetLowerBound(0); x <= tempFor1; x++)
            {
                if (colLookup[x].Disabled == false)
                {
                    int vRet = (fSql.ToLower().IndexOf(colLookup[x].lookup.ToLower(), vBeg) + 1);
                    if (vRet != 0 & vRet <= oRet)
                    {
                        if ((! gNoStrParse & colLookup[x].IgnoreParsing == 0) |
                            (gNoStrParse &
                             (! gInString |
                              (colLookup[gLookupPrevNum].IgnoreParsing > 0 &
                               colLookup[gLookupPrevNum].IgnoreParsing == colLookup[x].IgnoreParsing))))
                        {
                            if ((colLookup[gLookupPrevNum].CommentWrap != 1 & colLookup[gLookupPrevNum].CommentWrap != 2) |
                                colLookup[x].CommentWrap == 3 | colLookup[x].CommentWrap == 4)
                            {
                                oLookupNum = x;
                                oRet = vRet;
                                tempCheck_Info = true;
                            }
                        }
                    }
                    if (gSmallCmdWrap & colLookup[gLookupPrevNum].Recurse_Start & vRet != 0)
                    {
                        if (colLookup[x].Recurse_Stop & vRet < vStop)
                        {
                            vStopLookupNum = x;
                            vStop = vRet;
                        }
                        else if (colLookup[x].Recurse_Start & vRet < vStart)
                        {
                            vStart = vRet;
                        }
                    }
                }
            }
            if (gSmallCmdWrap & vStop < vStart & vStop < gSmallCmdLen)
            {
                oLookupNum = vStopLookupNum;
                oRet = vStop;
                tempCheck_Info = true;
            }
            if (gNoStrParse & colLookup[oLookupNum].IgnoreParsing > 0)
            {
                gInString = ! gInString;
            }
            return tempCheck_Info;
        }

        public string Color_Output(string vColor)
        {
            int vRet = 0;
            string sOut;
            string fSql;
            bool vNotDone;

            fSql = gSQL_Output;
            sOut = "";

            gLookupNum = 21; //prevent initial failure in finding '['
            gInString = false;

            vNotDone = true;
            while (vNotDone)
            {
                gLookupPrevNum = gLookupNum;
                vNotDone = Check_Info(" " + ColorTrim(fSql), ref vRet, ref gLookupNum);
                vRet--;
                vRet = vRet + colLookup[gLookupNum].Prefix.Length;
                if (vNotDone)
                {
                    if (colLookup[gLookupPrevNum].AppendLookup == 0)
                    {
                        sOut = sOut + fSql.Substring(0, colLookup[gLookupPrevNum].Lookup_Val.Length) + "</font>";
                        fSql = fSql.Substring(fSql.Length - (fSql.Length - colLookup[gLookupPrevNum].Lookup_Val.Length));
                        sOut = sOut + fSql.Substring(0, vRet - 1 - colLookup[gLookupPrevNum].Lookup_Val.Length);
                    }
                    else
                    {
                        sOut = sOut + fSql.Substring(0, vRet - 1);
                    }

                    sOut = sOut + "<font color=\"" + vColor + "\">";
                    if (colLookup[gLookupPrevNum].AppendLookup == 0)
                    {
                        fSql =
                            fSql.Substring(fSql.Length -
                                           (fSql.Length - (vRet - 1 - colLookup[gLookupPrevNum].Lookup_Val.Length)));
                    }
                    else
                    {
                        fSql = fSql.Substring(fSql.Length - (fSql.Length - (vRet - 1)));
                    }

                    if (colLookup[gLookupNum].AppendLookup != 0)
                    {
                        sOut = sOut + fSql.Substring(0, colLookup[gLookupNum].Lookup_Val.Length) + "</font>";
                        fSql = fSql.Substring(fSql.Length - (fSql.Length - colLookup[gLookupNum].Lookup_Val.Length));
                    }
                }
                else
                {
                    if (colLookup[gLookupPrevNum].AppendLookup == 0)
                    {
                        sOut = sOut + fSql.Substring(0, colLookup[gLookupPrevNum].Lookup_Val.Length) + "</font>";
                        fSql = fSql.Substring(fSql.Length - (fSql.Length - colLookup[gLookupPrevNum].Lookup_Val.Length));
                    }
                }
            }
            sOut = sOut + fSql;
            return sOut.Trim(" \t".ToCharArray());
        }

        private static string ColorTrim(string inString)
        {
            int nLen = inString.Length;
            if (nLen > 0)
            {
                int nPos = 1;
                while (nPos <= nLen & nPos < 40)
                {
                    if (Convert.ToInt32(inString[nPos - 1]) < 32)
                    {
                        inString = inString.Substring(0, nPos - 1) + " " + inString.Substring(nPos);
                    }
                    nPos++;
                }
            }
            return inString;
        }

        private string FullTrim(string inString)
        {
            bool sComment = false;
            bool sSingleQuotes = false;
            bool sDoubleQuotes = false;

            int nLen = inString.Length;
            if (nLen > 0)
            {
                int nPos = 1;
                while (nPos < nLen)
                {
                    //INSTANT C# WARNING: C# only evaluates the one required value of the '?' operator, while VB.NET always evaluates both values of an 'IIf' statement.
                    int nPos1 = ((nPos == nLen) ? nPos : nPos + 1);
                    if ((inString.Substring(nPos - 1, 1) + inString.Substring(nPos1 - 1, 1)) == "/*")
                    {
                        sComment = (gMaxCmntWidth < 0);
                    }
                    else if ((inString.Substring(nPos - 1, 1) + inString.Substring(nPos1 - 1, 1)) == "*/")
                    {
                        if (sComment & nPos > 4)
                        {
                            nPos = nPos - 3;
                        }
                        sComment = false;
                    }
                    if (Convert.ToInt32(inString[nPos - 1]) == 13 & sComment)
                    {
                        inString = inString.Substring(0, nPos - 1) + Environment.NewLine + inString.Substring(nPos);
                        nPos = nPos + 2;
                    }
                    else if (Convert.ToInt32(inString[nPos - 1]) == 32 & Convert.ToInt32(inString[nPos1 - 1]) == 32 &
                             gTrimSpace & ! sSingleQuotes & ! sDoubleQuotes)
                    {
                        inString = inString.Substring(0, nPos - 1) + inString.Substring(nPos);
                        nLen = inString.Length;
                    }
                    else if (inString.Substring(nPos - 1, 2) == "''")
                    {
                        // Skip past escaped single quotes.
                        nPos = nPos + 2;
                    }
                    else if (inString.Substring(nPos - 1, 2) == "\"\"")
                    {
                        // Skip past escaped double quotes.
                        nPos = nPos + 2;
                    }
                    else if (inString.Substring(nPos - 1, 1) == "'" & ! sDoubleQuotes)
                    {
                        // Mark the beginning/end of a single-quoted literal.
                        if (sSingleQuotes)
                        {
                            sSingleQuotes = false;
                        }
                        else
                        {
                            sSingleQuotes = true;
                        }
                        nPos++;
                    }
                    else if (inString.Substring(nPos - 1, 1) == "\"" & ! sSingleQuotes)
                    {
                        // Mark the beginning/end of a double-quoted literal.
                        if (sDoubleQuotes)
                        {
                            sDoubleQuotes = false;
                        }
                        else
                        {
                            sDoubleQuotes = true;
                        }
                        nPos++;
                        // This is just a normal character.
                    }
                    else
                    {
                        if (gLowerCase & ! sDoubleQuotes & ! sSingleQuotes)
                        {
                            string sLeft = inString.Substring(0, nPos - 1);
                            string sMiddle = inString.Substring(nPos - 1, 1).ToLower();
                            string sRight = inString.Substring(nPos);
                            inString = sLeft + sMiddle + sRight;
                        }
                        nPos++;
                    }
                }
            }
            return inString;
        }

        public string FTrim(string inString)
        {
            if (inString == " ")
            {
                inString = "";
            }
            return RTrim(LTrim(inString));
        }

        public string RTrim(string inString)
        {
            string tempRTrim = null;
            int nPos = inString.Length;
            if (nPos > 0)
            {
                while (Convert.ToInt32(inString[nPos - 1]) <= 32)
                {
                    nPos--;
                    if (nPos == 0)
                    {
                        return null;
                    }
                }
                tempRTrim = inString.Substring(0, nPos);
            }
            return tempRTrim;
        }

        public string LTrim(string inString)
        {
            string tempLTrim = null;
            int nLen = inString.Length;
            if (nLen > 0)
            {
                int nPos = 1;
                while (Convert.ToInt32(inString[nPos - 1]) <= 32)
                {
                    nPos++;
                    if (nPos == nLen)
                    {
                        return null;
                    }
                }
                tempLTrim = inString.Substring(nPos - 1);
            }
            return tempLTrim;
        }

        private void Init_Lookup()
        {
            gDebugPre = "";
            gDebugSuf = "";

            // note: fix new line after in sql for the ","
            colLookup[0] = new cLookup(0, 0, 1, 0, 1, 0, "select", 0, 0, "", " ", false, false, 0, 0, false, false);
            colLookup[1] = new cLookup(0, 0, 1, 0, 0, 2, "into", 1, 0, " ", " ", false, false, 0, 0, false, true);
            colLookup[2] = new cLookup(0, 2, 1, 0, 1, 0, "from", 1, 0, " ", " ", false, false, 0, 0, false, true);
            colLookup[3] = new cLookup(0, 1, 1, 0, 2, 0, "where", 1, 0, " ", " ", false, false, 0, 0, false, true);
            colLookup[4] = new cLookup(0, 0, 0, 3, 2, 0, "and", 1, 0, " ", " ", false, false, 0, 0, false, true);
            colLookup[5] = new cLookup(0, 0, 0, 4, 2, 0, "or", 1, 0, " ", " ", false, false, 0, 0, false, true);
            colLookup[6] = new cLookup(0, 0, 1, 0, 2, 0, "having", 1, 0, " ", " ", false, false, 0, 0, false, true);
            colLookup[7] = new cLookup(0, 1, 1, 0, 1, 0, "group by", 1, 0, " ", " ", false, false, 0, 0, false, true);
            colLookup[8] = new cLookup(0, 1, 1, 0, 1, 0, "order by", 1, 0, " ", " ", false, false, 0, 0, false, true);
            colLookup[9] = new cLookup(1, 0, 1, 0, 2, 0, "union", 1, 2, " ", " ", false, false, 0, 0, false, true);
            colLookup[10] = new cLookup(1, 0, 1, 0, 2, 0, "union all", 1, 2, " ", " ", false, false, 0, 0, false, true);
            colLookup[11] = new cLookup(2, -1, 0, 0, 0, 0, "(", 0, 0, "", "", true, false, 0, 0, false, false);
            colLookup[12] = new cLookup(3, 0, 0, 0, 0, 0, ")", 0, 0, "", "", false, true, 0, 0, false, false);
            colLookup[13] = new cLookup(1, 0, 1, 0, 0, 7, ",", 1, 0, "", "", false, false, 0, 0, gDisableComma, false);
            colLookup[14] =
                new cLookup(((gPipeNewLine) ? -1 : -2), 0, 2, 0, 0, 0, "|", 1, ((gPipeNewLine) ? 2 : 0),
                            " ", " ", false, false, 0, 0, false, true);
            colLookup[15] =
                new cLookup(((gBraceNewLine) ? 1 : 2), 4, 2, 0, 0, 0, "{", 1, ((gBraceNewLine) ? 2 : 0),
                            "", "", true, false, 0, 0, false, true);
            colLookup[16] = new cLookup(1, 0, 2, 0, 0, 0, "}", 0, 1, "", "", false, true, 0, 0, false, false);
            colLookup[17] = new cLookup(0, 0, 2, 0, 2, 0, "if", 1, 0, " ", " ", false, false, 0, 0, false, false);
            colLookup[18] = new cLookup(0, 0, 2, 0, 2, 0, "else", 1, 0, " ", " ", false, false, 0, 0, false, false);
            colLookup[19] = new cLookup(0, 0, 2, 0, 2, 0, "else if", 1, 0, " ", " ", false, false, 0, 0, false, false);
            colLookup[20] = new cLookup(1, 1, 2, 0, 0, 0, "[", 0, 2, "", "", true, false, 0, 0, false, false);
            colLookup[21] = new cLookup(3, 0, 2, 0, 0, 0, "]", 0, 0, "", " ", false, true, 0, 0, false, false);

            colLookup[22] =
                new cLookup(4, 0, 0, 0, 0, 0, "\"", 0, 0, "", "", false, false, 0, 1, (! gNoStrParse), false);
            colLookup[23] = new cLookup(4, 0, 0, 0, 0, 0, "'", 0, 0, "", "", false, false, 0, 2, (! gNoStrParse), false);

            colLookup[24] = new cLookup(3, 0, 0, 0, 0, 0, "/*", 1, 5, "", "", true, false, 1, 0, false, false);
            colLookup[25] =
                new cLookup(2, 0, 0, 0, 0, 0, "*/", 1, 0, "", "", false, true, ((gMaxCmntWidth == -1) ? 4 : 3), 0, false,
                            false);
            colLookup[26] = new cLookup(1, 0, 2, 0, 0, 0, ";", 1, 0, "", "", false, false, 0, 0, false, false);
            colLookup[27] = new cLookup(0, 0, 2, 0, 0, 0, "#include", 1, 0, "", " ", false, false, 0, 0, false, false);

            colLookup[28] = new cLookup(3, 0, 1, 0, 1, 0, "insert", 0, 0, "", " ", false, false, 0, 0, false, false);
            colLookup[29] = new cLookup(3, 0, 1, 0, 1, 0, "update", 0, 0, "", " ", false, false, 0, 0, false, false);
            colLookup[30] = new cLookup(3, 0, 1, 0, 1, 0, "delete", 0, 0, "", " ", false, false, 0, 0, false, false);
            colLookup[31] = new cLookup(0, 0, 1, 0, 0, 3, "set", 1, 0, " ", " ", false, false, 0, 0, false, true);
            colLookup[32] = new cLookup(0, 0, 1, 0, 0, 1, "values", 1, 0, " ", " ", false, false, 0, 0, false, true);
            colLookup[33] = new cLookup(1, 0, 2, 0, 0, 0, "catch", 0, 0, " ", "", false, false, 0, 0, false, true);
            colLookup[34] = new cLookup(1, 0, 2, 0, 2, 0, "#", 1, 0, " ", "", false, false, 1, 0, false, false);
            colLookup[35] = new cLookup(3, 0, 2, 0, 2, 0, "#", 1, 0, "", " ", false, false, 4, 0, false, false);
            colLookup[36] = new cLookup(0, 0, 2, 0, 2, 0, "try", 1, 0, " ", " ", false, false, 0, 0, false, false);
            colLookup[37] = new cLookup(0, 0, 2, 0, 2, 0, "finally", 1, 1, " ", " ", false, false, 0, 0, false, false);

            //must not move to anything other than last
            colLookup[38] = new cLookup(0, 3, 0, 0, 0, 0, "<=-JUNK-=>", 1, 0, "", " ", false, false, 2);
        }

        private int Len_from_last_newline()
        {
            int tempLen_from_last_newline;
            if (gSQL_Output.Length != 0)
            {
                int retold = 0;
                int ret = gSQL_Output.IndexOf("\r\n", retold + 1) + 1;
                while (ret != 0)
                {
                    ret = gSQL_Output.IndexOf("\r\n", retold + 1) + 1;
                    if (ret != 0)
                    {
                        retold = ret;
                    }
                }
                tempLen_from_last_newline = gSQL_Output.Length - retold - Environment.NewLine.Length;
            }
            else
            {
                tempLen_from_last_newline = 0;
            }
            return tempLen_from_last_newline;
        }

        private int check_debug(string iString, string lookup)
        {
            int tempcheck_debug;
            if (iString.Length != 0)
            {
                int retold = 0;
                int retcnt = 0;
                int ret = (iString.IndexOf(lookup, retold) + 1);
                while (ret != 0)
                {
                    retold = ret;
                    retcnt++;
                    ret = (iString.IndexOf(lookup, retold) + 1);
                }
                tempcheck_debug = retcnt*lookup.Length;
            }
            else
            {
                tempcheck_debug = 0;
            }
            return tempcheck_debug;
        }

        private static string ColorSpecificByte(string inString, int lErrorByte)
        {
            int nLen = inString.Length;
            if (nLen > 0)
            {
                //If gLowerCase Then inString = LCase(inString)
                int nPos = 1;
                while (nPos <= nLen & nPos > 0)
                {
                    if (Convert.ToInt32(inString[nPos - 1]) >= 32)
                    {
                        if (nPos > lErrorByte)
                        {
                            inString = inString.Substring(0, nPos - 1) + "<font style=background:red>" +
                                       inString.Substring(nPos - 1, 1) + "</font>" + inString.Substring(nPos);
                            nPos = -1;
                        }
                    }
                    nPos++;
                }
            }
            return inString;
        }

        private static string padnewline(string iString, int iPad)
        {
            if (iString.Length != 0)
            {
                int retold = 0;
                int ret = 1;
                while (ret != 0)
                {
                    ret = (iString.IndexOf(Environment.NewLine, retold) + 1);
                    if (ret != 0)
                    {
                        iString = iString.Substring(0, ret + 1) + string.Empty.PadLeft(iPad) +
                                  iString.Substring(ret + 2 - 1);
                        retold = ret;
                    }
                }
            }
            return iString;
        }
    }
}