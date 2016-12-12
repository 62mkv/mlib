using System.Collections.Generic;
using System.IO;
using System.Text;

namespace DevTreeConverter
{
    class DirectoryFilterFile : FilterFile
    {
        private readonly DirectoryInfo info;
        private const string includeTemplate = "\t<SubProjects Include=\".\\{0}\\*.proj\" />";
        
        public DirectoryFilterFile(Dictionary<string, string> tokens, DirectoryInfo info)
            : base(tokens, "msbuild-folder.proj", "msbuild.proj")
        {
            this.info = info;
        }

        protected override Dictionary<string, string> GetTransientTokens(string outputPath, Dictionary<string, string> fileTokens)
        {
            
            string subDirs = "\t<SubProjects Include=\".\\*.*\\*.proj\" Exclude=\"$(MSBuildProjectFile)\" />";
            string stopOnFailure = "false";

            string folderName = info.Name.ToLower().Trim();
            

            if (!folderName.Equals("mnt") && !folderName.Equals("dsp") &&
                !folderName.Equals("opr") && !folderName.Equals("wiz"))
            {
                //Stop on first failure for these directories
                stopOnFailure = "true";

                StringBuilder builder = new StringBuilder();
                string makeFile = Path.Combine(outputPath, "makefile.nt");
                if (File.Exists(makeFile))
                {
                    bool foundDirs = false;
                    foreach (string line in File.ReadAllLines(makeFile))
                    {
                        if (line.Contains("SUBDIR") || foundDirs)
                        {
                            if (line.StartsWith("!"))
                                continue;

                            string cleanLine = line;
                            if (!foundDirs)
                            {
                                cleanLine = cleanLine.Replace("SUBDIR", "").Replace("=", "");
                                foundDirs = true;
                            }

                            cleanLine = cleanLine.Replace("\\", "").Trim();

                            if (!string.IsNullOrEmpty(cleanLine))
                                builder.AppendLine(string.Format(includeTemplate, cleanLine));

                            if (!line.Contains("\\"))
                                break;
                        }
                    }
                }
                else
                {
                    foreach (DirectoryInfo directory in info.GetDirectories())
                    {
                        builder.AppendLine(string.Format(includeTemplate, directory.Name));
                    }
                }
                subDirs = builder.ToString();
            }
            
            fileTokens.Add("!SUBDIRS!", subDirs);
            fileTokens.Add("!STOPONFAILURE!", stopOnFailure);

            return fileTokens;
        }

        public void RemoveFile(string outputPath)
        {
            string filePath = Path.Combine(outputPath, outputFileName);
            if (File.Exists(filePath))
            {
                File.Delete(filePath);
            }
        }
    }
}
