using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;

namespace DevTreeConverter
{
    class FilterFile
    {
        private readonly Dictionary<string, string> tokens = new Dictionary<string, string>();
        protected readonly string templateFileName;
        protected readonly string templateFileContent;
        protected readonly string outputFileName;


        public FilterFile(Dictionary<string, string> tokens, string templateFileName, string outputFileName)
        {
            this.tokens = tokens;
            this.outputFileName = outputFileName;
            
            if (File.Exists(templateFileName))
            {
                this.templateFileName = templateFileName;
                templateFileContent = File.ReadAllText(templateFileName);
            }
            else
            {
                string assemblyPath = Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location);
                assemblyPath = Path.Combine(assemblyPath, templateFileName);

                if (File.Exists(assemblyPath))
                {
                    this.templateFileName = assemblyPath;
                    templateFileContent = File.ReadAllText(assemblyPath); 
                }
                else
                {
                    throw new FileNotFoundException("Template File Not Found", templateFileName);
                }
            }
        }


        public string OutputFileName
        {
            get { return outputFileName; }
        }

        public void ConvertFile(string outputPath)
        {
            String outputFileContent = templateFileContent.Clone() as String;
            if (Directory.Exists(outputPath) && !String.IsNullOrEmpty(outputFileContent))
            {
                
                Dictionary<string, string> fileTokens = new Dictionary<string, string>(tokens);

                fileTokens = GetTransientTokens(outputPath, fileTokens);

                foreach (KeyValuePair<string, string> token in fileTokens)
                {
                    outputFileContent = outputFileContent.Replace(token.Key, token.Value);
                }

                File.WriteAllText(Path.Combine(outputPath, outputFileName), outputFileContent, System.Text.Encoding.UTF8);
            }
        }

        protected virtual Dictionary<string, string> GetTransientTokens(string outputPath, Dictionary<string, string> fileTokens)
        {
            return fileTokens;
        }
    }
}
