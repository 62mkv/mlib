namespace RedPrairie.MCS.WinMSQL
{
    /// <summary>
    /// Summary description for Class1.
    /// </summary>
    public class cLookup
    {
        //local variable(s) to hold property value(s)
        private int mAppendLookup; //local copy
        private int mIndent; //local copy
        private int mInCMD; //local copy
        private int mInCMD_Indent; //local copy
        private int mInSQL_Indent; //local copy
        private int mInSQL; //local copy
        private string mLookup_Val; //local copy
        private int mNewLineAfter; //local copy
        private int mNewLineBefore; //local copy
        private string mPrefix; //local copy
        private bool mRecurse_Start; //local copy
        private bool mRecurse_Stop; //local copy
        private string mSufix; //local copy
        private int mCommentWrap; //local copy
        private int mIgnoreParsing; //local copy
        private bool mDisabled; //local copy
        private bool mSpaceReq; //local copy

        // 0 = don't append
        // 1 = append with no space pading
        // 2 = append with left side space(1) padded
        // 3 = append with right side space(1) padded
        // 4 = append with left and right side space(1) padded
        public int AppendLookup
        {
            get { return mAppendLookup; }
            set { mAppendLookup = value; }
        }

        // 0 < don't space for recursion.
        // 0 >= line padded with this many spaces from current begin line
        public int Indent
        {
            get { return mIndent; }
            set { mIndent = value; }
        }

        //this is for 'and, 'or' single line in if statment
        //multiple line in where clause
        // 0 = don't set whether in command
        // 1 = set in command to true
        // 2 = set in command to false
        public int InCMD
        {
            get { return mInCMD; }
            set { mInCMD = value; }
        }

        //indent when in command
        public int InCMD_Indent
        {
            get { return mInCMD_Indent; }
            set { mInCMD_Indent = value; }
        }

        //this is for ',' no intdent within parentisis
        //when by them self, indented within select, into, ...
        // 0 = don't set whether in sql
        // 1 = set in sql to true
        // 2 = set in sql to false
        public int InSQL
        {
            get { return mInSQL; }
            set { mInSQL = value; }
        }

        //indent when in sql
        public int InSQL_Indent
        {
            get { return mInSQL_Indent; }
            set { mInSQL_Indent = value; }
        }

        // Parser search phase
        public string Lookup_Val
        {
            get { return mLookup_Val; }
            set { mLookup_Val = value; }
        }

        // this is the lookup_val concat. with prefix and sufix
        public string lookup
        {
            get { return mPrefix + mLookup_Val + mSufix; }
        }

        //plase new line after line output
        public int NewLineAfter
        {
            get { return mNewLineAfter; }
            set { mNewLineAfter = value; }
        }

        //plase new line before line output
        public int NewLineBefore
        {
            get { return mNewLineBefore; }
            set { mNewLineBefore = value; }
        }

        // prefix to lookup value (ex. padding " "
        public string Prefix
        {
            get { return mPrefix; }
            set { mPrefix = value; }
        }

        // does lookup start a new recusive level (begining partheses, brackets, etc.)
        public bool Recurse_Start
        {
            get { return mRecurse_Start; }
            set { mRecurse_Start = value; }
        }

        // does lookup goback to a prev recusive level
        // (ending partheses, brackets, etc.)
        public bool Recurse_Stop
        {
            get { return mRecurse_Stop; }
            set { mRecurse_Stop = value; }
        }

        // sufix to lookup value (ex. padding " "
        public string Sufix
        {
            get { return mSufix; }
            set { mSufix = value; }
        }

        // Does CommentWrap if line gets to int
        // 0 = no CommentWrap
        // 1 = start/continue CommentWrap
        // 2 = start/continue CommentWrap with indent
        // 3 = stop CommentWrap
        // 4 = stop CommentWrap with raw output
        public int CommentWrap
        {
            get { return mCommentWrap; }
            set { mCommentWrap = value; }
        }

        public int IgnoreParsing
        {
            get { return mIgnoreParsing; }
            set { mIgnoreParsing = value; }
        }

        // does lookup start a new recusive level (begining partheses, brackets, etc.)
        public bool Disabled
        {
            get { return mDisabled; }
            set { mDisabled = value; }
        }

        // Space/Newline prior to the variable is required
        public bool SpaceReq
        {
            get { return mSpaceReq; }
            set { mSpaceReq = value; }
        }

        public cLookup(int vAppendLookup,
                       int vIndent,
                       int vInCMD,
                       int vInCMD_Indent,
                       int vInSQL,
                       int vInSQL_Indent,
                       string vLookup_Val,
                       int vNewLineAfter,
                       int vNewLineBefore,
                       string vPrefix,
                       string vSufix,
                       bool vRecurse_Start,
                       bool vRecurse_Stop,
                       int vCommentWrap,
                       int vIgnoreParsing, bool vDisabled, bool vSpaceReq)
        {
            AppendLookup = vAppendLookup;
            Indent = vIndent;
            InCMD = vInCMD;
            InCMD_Indent = vInCMD_Indent;
            InSQL_Indent = vInSQL_Indent;
            InSQL = vInSQL;
            Lookup_Val = vLookup_Val;
            NewLineAfter = vNewLineAfter;
            NewLineBefore = vNewLineBefore;
            Prefix = vPrefix;
            Sufix = vSufix;
            Recurse_Start = vRecurse_Start;
            Recurse_Stop = vRecurse_Stop;
            CommentWrap = vCommentWrap;
            IgnoreParsing = vIgnoreParsing;
            Disabled = vDisabled;
            SpaceReq = vSpaceReq;
        }

        public cLookup(int vAppendLookup,
                       int vIndent,
                       int vInCMD,
                       int vInCMD_Indent,
                       int vInSQL,
                       int vInSQL_Indent,
                       string vLookup_Val,
                       int vNewLineAfter,
                       int vNewLineBefore,
                       string vPrefix,
                       string vSufix,
                       bool vRecurse_Start,
                       bool vRecurse_Stop,
                       int vCommentWrap)
        {
            //colLookup[36] = new cLkpUp(0, 3, 0, 0, 0, 0, "<=-JUNK-=>", 1, 0, "", " ", false, false, 2);
            AppendLookup = vAppendLookup;
            Indent = vIndent;
            InCMD = vInCMD;
            InCMD_Indent = vInCMD_Indent;
            InSQL_Indent = vInSQL_Indent;
            InSQL = vInSQL;
            Lookup_Val = vLookup_Val;
            NewLineAfter = vNewLineAfter;
            NewLineBefore = vNewLineBefore;
            Prefix = vPrefix;
            Sufix = vSufix;
            Recurse_Start = vRecurse_Start;
            Recurse_Stop = vRecurse_Stop;
            CommentWrap = vCommentWrap;
            //IgnoreParsing = vIgnoreParsing;
            //Disabled = vDisabled;
            //SpaceReq = vSpaceReq;
        }
    }
}