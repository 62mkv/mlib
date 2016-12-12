using System;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using Microsoft.Build.Framework;
using Microsoft.Build.Utilities;

namespace RedPrairie.MSBuild.Tasks
{
    /// <summary>
    /// A WIX task that creates item File fragements based on 
    /// a template
    /// </summary>
    public class WixFragments:Task
    {
        #region Private Fields
        private readonly MD5CryptoServiceProvider hashProvider = new MD5CryptoServiceProvider();
        private string outputPath;
        private ITaskItem[] sourceFiles;
        private ITaskItem wixFile;
        #endregion

        #region Public Properties

        /// <summary>
        /// Gets or sets the output path.
        /// </summary>
        /// <value>The output path.</value>
        public string OutputPath
        {
            get { return this.outputPath; }
            set { this.outputPath = value; }
        }
                
        /// <summary>
        /// Gets or sets the source files.
        /// </summary>
        /// <value>The source files.</value>
        public ITaskItem[] SourceFiles
        {
            get { return this.sourceFiles; }
            set { this.sourceFiles = value; }
        }

        /// <summary>
        /// Gets or sets the wix template file.
        /// </summary>
        /// <value>The wix template file.</value>
        public ITaskItem WixFile
        {
            get { return this.wixFile; }
            set { this.wixFile = value; }
        }

        #endregion

        #region Public Methods
        ///<summary>
        ///Executes the task.
        ///</summary>
        ///
        ///<returns>
        ///true if the task successfully executed; otherwise, false.
        ///</returns>
        ///
        public override bool Execute()
        {
            if (sourceFiles == null)
                return true;

            if (CheckProperties())
            {
                string fileContent = File.ReadAllText(wixFile.ItemSpec);
                string templateFile = GetReplacementTemplate(fileContent);

                bool skipFinalMerge = false;
                if (String.IsNullOrEmpty(templateFile))
                {
                    templateFile = fileContent;
                    skipFinalMerge = true;
                }
                
                StringBuilder builder = new StringBuilder();
                foreach (ITaskItem sourceFile in sourceFiles)
                {
                    try
                    {
                        builder.Append(ReplaceVariables(sourceFile, templateFile));
                    }
                    catch (Exception e)
                    {
                        Log.LogError("{0}\r\n{1}", e.Message, e.StackTrace);
                        return false;
                    }
                }

                if (skipFinalMerge)
                {
                    fileContent = builder.ToString();
                }
                else
                {
                    fileContent = MergeReplacedText(fileContent, builder);
                }

                File.WriteAllText(outputPath, fileContent);
            }

            return true;
        }

        private static string MergeReplacedText(string fileContent, StringBuilder builder)
        {
            int tagStart = fileContent.IndexOf("%ReplaceStart%");
            int tagEnd = fileContent.IndexOf("%ReplaceEnd%");

            if (tagStart > -1)
            {
                builder.Insert(0, fileContent.Substring(0, tagStart));
            }

            if (tagEnd != -1)
            {
                tagEnd += 12;
                builder.Append(fileContent.Substring(tagEnd));
            }

            return builder.ToString();
        }

        #endregion

        #region Private Methods
        /// <summary>
        /// Checks the properties to ensure they are correct.
        /// </summary>
        /// <returns>true if all properties are valid, otherwise false</returns>
        private bool CheckProperties()
        {
            if (sourceFiles == null)
            {
                Log.LogWarning("No source files specified to include");
                return false;
            }

            string outputDirectory = Path.GetDirectoryName(outputPath);
            if (String.IsNullOrEmpty(outputPath) ||
                !Directory.Exists(outputDirectory))
            {
                Log.LogError("Output Path '{0}' is invalid or does not exist", outputDirectory);
                return false;
            }

            if (wixFile == null || !File.Exists(wixFile.ItemSpec))
            {
                Log.LogError("Template File '{0}' is invalid or does not exist", wixFile);
                return false;
            }

            return true;
        }


        private static string GetReplacementTemplate(string fileContent)
        {
            int tagStart = fileContent.IndexOf("%ReplaceStart%");
            int tagEnd = fileContent.IndexOf("%ReplaceEnd%");

            if (tagStart != -1) tagStart += 15;
            if (tagEnd != -1) tagEnd = tagEnd - tagStart;

            if(tagEnd > -1 && tagStart > -1)
            {
                return fileContent.Substring(tagStart, tagEnd);
            }

            return null;
        }

        /// <summary>
        /// Replaces the variables in the file template.
        /// </summary>
        /// <param name="file">The file referenced.</param>
        /// <param name="templateFile">The template file.</param>
        /// <returns>The file content with the templates replaced</returns>
        private string ReplaceVariables(ITaskItem file, ICloneable templateFile)
        {
            string fileContent = templateFile.Clone() as string;

            if (fileContent == null)
            {
                return string.Empty;
            }

            foreach (object metadataName in file.MetadataNames)
            {
                string dataName = string.Format("%{0}%", metadataName);

                if (dataName != "%%")
                {
                    Log.LogMessage(MessageImportance.Low, "Replacing '{0}' if it exists", dataName);
                    fileContent =
                        fileContent.Replace(dataName, file.GetMetadata(metadataName as string));
                }
            }

            //Replace item with a unique ID if needed
            string fileName = Path.GetFileName(file.ItemSpec);
            Log.LogMessage(MessageImportance.Low, "File Name is '{0}'", fileName);
            fileContent = fileContent.Replace("%FILE_NAME%", fileName);

            
            string fileGUID = ComputeFileID(fileName);
            Log.LogMessage(MessageImportance.Low, "File GUID is '{0}'", fileGUID);
            fileContent = fileContent.Replace("%FILE_ID%", fileGUID);

            Version fileVersion = Utils.GetFileAssemblyVersion(file.ItemSpec);
            string fileString = fileVersion.ToString().Replace("200", "");

            Log.LogMessage(MessageImportance.Low, "File Version is '{0}'", fileString);
            fileContent = fileContent.Replace("%FILE_VERSION%", fileString);

                        
            return fileContent;
        }

        private string ComputeFileID(string fileName)
        {
            byte[] hash = hashProvider.ComputeHash(Encoding.UTF8.GetBytes(fileName));
            StringBuilder result = new StringBuilder(36);

            for (int i = 0; i < Math.Min(16, hash.Length); i++)
            {
                if (i > 2 && i < 12 && i % 2 == 0)
                {
                    result.Append("-");
                }

                result.AppendFormat("{0:x2}", hash[i]);
            }

            return result.ToString();
        }
        #endregion

 

        
    }
}
