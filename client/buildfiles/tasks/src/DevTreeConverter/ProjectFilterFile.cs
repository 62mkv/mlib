using System;
using System.Collections.Generic;
using System.IO;
using System.Text.RegularExpressions;

namespace DevTreeConverter
{
    class ProjectFilterFile : FilterFile
    {
        private readonly Regex formRegex = new Regex("FORM *= *TRUE");
        private readonly Regex exeRegex = new Regex("TARGETTYPE *= *(win)?exe");

        public ProjectFilterFile(Dictionary<string, string> tokens)
            : base(tokens, "msbuild-template.proj", "msbuild.proj")
        {
            Console.WriteLine("Getting template file from '{0}'", templateFileName);
        }

        protected override Dictionary<string, string> GetTransientTokens(string outputPath, Dictionary<string, string> fileTokens)
        {
            string[] csProjFiles = Directory.GetFiles(outputPath, "*.csproj", SearchOption.TopDirectoryOnly);

            if (csProjFiles != null && csProjFiles.Length == 1)
            {
                fileTokens.Add("!PROJNAME!", Path.GetFileNameWithoutExtension(csProjFiles[0]));
            }

            string formType = "Form";
            string makeFile = Path.Combine(outputPath, "makefile.nt");

            if (File.Exists(makeFile))
            {
                string fileContent = File.ReadAllText(makeFile);

                if (fileContent.Contains("COREEXE") || 
                    fileContent.Contains("COREDLL"))
                {
                    formType = "CoreComponent";
                }
                else if (formRegex.IsMatch(fileContent))
                {
                    formType = "Form";
                }
                else if (exeRegex.IsMatch(fileContent))
                {
                    formType = "Executable";
                }
                else if (fileContent.Contains("NETMODULE"))
                {
                    formType = "LoaderModule";
                }
                else
                {
                    formType = "Component";
                }
            }

            fileTokens.Add("!FORMTYPE!", formType);
            Console.WriteLine("     Project type is: {0}", formType);

            return fileTokens;
        }
    }
}
