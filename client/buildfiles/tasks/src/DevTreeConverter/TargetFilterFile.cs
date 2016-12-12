using System.Collections.Generic;
using System.IO;

namespace DevTreeConverter
{
    class TargetFilterFile:FilterFile
    {
        public TargetFilterFile(Dictionary<string, string> tokens, string templateFileName, string outputFileName) 
            : base(tokens, templateFileName, outputFileName)
        {
        }

        protected override Dictionary<string, string> GetTransientTokens(string outputPath, Dictionary<string, string> fileTokens)
        {
            fileTokens.Add("!ROOTTARGET!", string.Format(@"$(MOCADIR)\client\buildfiles\{0}", new FileInfo(templateFileName).Name));

            return fileTokens;
        }
    }
}
